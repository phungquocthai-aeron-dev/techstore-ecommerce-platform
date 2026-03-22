package com.techstore.chatbot.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.techstore.chatbot.client.ProductServiceClient;
import com.techstore.chatbot.client.WarehouseServiceClient;
import com.techstore.chatbot.constant.ResponseType;
import com.techstore.chatbot.dto.response.ChatResponse;
import com.techstore.chatbot.dto.response.PageResponseDTO;
import com.techstore.chatbot.dto.response.ProductListResponseDTO;
import com.techstore.chatbot.dto.response.VariantInfo;
import com.techstore.chatbot.dto.response.VariantStockResponse;
import com.techstore.chatbot.util.MessageParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductChatService {

    private final ProductServiceClient productServiceClient;
    private final WarehouseServiceClient warehouseServiceClient;
    private final MessageParser messageParser;

    // ─────────────────────────────────────────────────────────────────────────
    // TÌM SẢN PHẨM — gọi /products/search (hỗ trợ filter giá server-side)
    // ─────────────────────────────────────────────────────────────────────────
    public ChatResponse handleProductSearch(String message) {
        try {
            String keyword = messageParser.extractSearchKeyword(message);
            Double minPrice = messageParser.parseMinPrice(message);
            Double maxPrice = messageParser.parseMaxPrice(message);

            log.info("  [ProductSearch] keyword=\"{}\", minPrice={}, maxPrice={}", keyword, minPrice, maxPrice);
            log.info(
                    "  [ProductSearch] → GET product-service/products/search"
                            + "?keyword={}&minPrice={}&maxPrice={}&page=0&size=10",
                    keyword,
                    minPrice,
                    maxPrice);

            // Gọi /products/search — server filter giá trực tiếp, không cần filter lại phía chatbot
            PageResponseDTO<ProductListResponseDTO> result = productServiceClient
                    .searchProducts(keyword, null, null, minPrice, maxPrice, 0, 10, "id", "DESC")
                    .getResult();

            int found = result != null && result.getContent() != null
                    ? result.getContent().size()
                    : 0;
            log.info(
                    "  [ProductSearch] ← Returned {} products (total={})",
                    found,
                    result != null ? result.getTotalElements() : 0);

            if (result == null
                    || result.getContent() == null
                    || result.getContent().isEmpty()) {
                log.info("  [ProductSearch] No results");
                return ChatResponse.builder()
                        .type(ResponseType.TEXT)
                        .message("😔 Xin lỗi, mình không tìm thấy sản phẩm phù hợp với yêu cầu của bạn. "
                                + "Bạn có thể mô tả chi tiết hơn không?")
                        .build();
            }

            List<ProductListResponseDTO> products = result.getContent();

            log.info("  [ProductSearch] Products found:");
            products.forEach(p -> log.info(
                    "    • [id={}] {} | brand={} | basePrice={} đ | status={}",
                    p.getId(),
                    p.getName(),
                    p.getBrandName(),
                    p.getBasePrice() != null ? String.format("%,.0f", p.getBasePrice()) : "N/A",
                    p.getStatus()));

            return ChatResponse.builder()
                    .type(ResponseType.PRODUCT_LIST)
                    .message(buildSearchSummary(products, keyword, minPrice, maxPrice))
                    .data(products)
                    .metadata(Map.of(
                            "total", result.getTotalElements(),
                            "showing", products.size(),
                            "keyword", keyword != null ? keyword : "",
                            "minPrice", minPrice != null ? minPrice : "",
                            "maxPrice", maxPrice != null ? maxPrice : ""))
                    .build();

        } catch (Exception ex) {
            log.error("  [ProductSearch] ✗ Failed: {}", ex.getMessage(), ex);
            return ChatResponse.builder()
                    .type(ResponseType.TEXT)
                    .message("⚠️ Đang có lỗi khi tìm kiếm sản phẩm. Vui lòng thử lại sau!")
                    .build();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // KIỂM TRA TỒN KHO — tìm variant rồi gọi warehouse
    // ─────────────────────────────────────────────────────────────────────────
    public ChatResponse handleStockCheck(String message) {
        try {
            // Lấy tên sản phẩm, bỏ các từ hỏi tồn kho
            String keyword = message.toLowerCase()
                    .replaceAll("(?i)còn hàng không|còn không|hết hàng chưa|có sẵn không|tồn kho", "")
                    .trim();

            log.info("  [StockCheck] keyword=\"{}\"", keyword);
            log.info("  [StockCheck] → GET product-service/products/search?keyword={}&page=0&size=5", keyword);

            // Dùng searchProducts để tìm sản phẩm trước
            PageResponseDTO<ProductListResponseDTO> productResult = productServiceClient
                    .searchProducts(keyword, null, null, null, null, 0, 5, "id", "DESC")
                    .getResult();

            int found = productResult != null && productResult.getContent() != null
                    ? productResult.getContent().size()
                    : 0;
            log.info("  [StockCheck] ← Found {} products", found);

            if (productResult == null
                    || productResult.getContent() == null
                    || productResult.getContent().isEmpty()) {
                return ChatResponse.builder()
                        .type(ResponseType.STOCK)
                        .message("😔 Không tìm thấy sản phẩm \"" + keyword + "\". Bạn kiểm tra lại tên nhé!")
                        .build();
            }

            // Lấy variants của product đầu tiên để check stock
            ProductListResponseDTO firstProduct = productResult.getContent().get(0);
            log.info(
                    "  [StockCheck] Checking stock for product: [id={}] {}",
                    firstProduct.getId(),
                    firstProduct.getName());
            log.info("  [StockCheck] → GET product-service/products/{}/variants", firstProduct.getId());

            List<VariantInfo> variants = productServiceClient
                    .getVariantsByProductId(firstProduct.getId())
                    .getResult();

            if (variants == null || variants.isEmpty()) {
                return ChatResponse.builder()
                        .type(ResponseType.STOCK)
                        .message("😔 Sản phẩm \"" + firstProduct.getName() + "\" chưa có phiên bản nào trong hệ thống.")
                        .build();
            }

            List<Long> variantIds = variants.stream().map(VariantInfo::getId).toList();

            log.info("  [StockCheck] → POST warehouse-service/inventory/variant/total-stock/batch");
            log.info("  [StockCheck]   variantIds = {}", variantIds);

            List<VariantStockResponse> stocks =
                    warehouseServiceClient.getTotalStockByVariants(variantIds).getResult();

            log.info("  [StockCheck] ← Stock data:");
            stocks.forEach(s -> log.info("    • variantId={} → stock={}", s.getVariantId(), s.getStock()));

            Map<Long, Long> stockMap = stocks.stream()
                    .collect(Collectors.toMap(VariantStockResponse::getVariantId, VariantStockResponse::getStock));

            StringBuilder sb = new StringBuilder();
            sb.append("📦 **Tồn kho: ").append(firstProduct.getName()).append("**\n\n");

            boolean anyInStock = false;
            for (VariantInfo v : variants) {
                Long stock = stockMap.getOrDefault(v.getId(), 0L);
                if (stock > 0) anyInStock = true;
                String status = stock > 0 ? "✅ Còn " + stock + " sản phẩm" : "❌ Hết hàng";
                sb.append("• **").append(v.getName()).append("**");
                if (v.getColor() != null) sb.append(" (").append(v.getColor()).append(")");
                sb.append(": ").append(status).append("\n");
            }

            if (!anyInStock) {
                sb.append("\n💡 Tất cả phiên bản đang hết hàng. Bạn có muốn xem sản phẩm tương tự không?");
            }

            return ChatResponse.builder()
                    .type(ResponseType.STOCK)
                    .message(sb.toString())
                    .data(stocks)
                    .metadata(Map.of(
                            "productId", firstProduct.getId(),
                            "productName", firstProduct.getName(),
                            "variantCount", variants.size()))
                    .build();

        } catch (Exception ex) {
            log.error("  [StockCheck] ✗ Failed: {}", ex.getMessage(), ex);
            return ChatResponse.builder()
                    .type(ResponseType.STOCK)
                    .message("⚠️ Không thể kiểm tra tồn kho lúc này. Vui lòng thử lại!")
                    .build();
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String buildSearchSummary(
            List<ProductListResponseDTO> products, String keyword, Double minPrice, Double maxPrice) {
        StringBuilder sb = new StringBuilder();
        sb.append("🔍 Tìm thấy **").append(products.size()).append(" sản phẩm**");
        if (keyword != null && !keyword.isBlank())
            sb.append(" cho \"").append(keyword).append("\"");
        String priceMsg = buildPriceRangeMsg(minPrice, maxPrice);
        if (!priceMsg.isBlank()) sb.append(" ").append(priceMsg);
        sb.append(":\n\n");

        products.stream().limit(5).forEach(p -> {
            sb.append("• **").append(p.getName()).append("**");
            if (p.getBrandName() != null) sb.append(" – ").append(p.getBrandName());
            if (p.getBasePrice() != null) sb.append(" – ").append(String.format("%,.0f đ", p.getBasePrice()));
            sb.append("\n");
        });
        return sb.toString();
    }

    private String buildPriceRangeMsg(Double minPrice, Double maxPrice) {
        if (minPrice != null && maxPrice != null)
            return String.format("khoảng %.0f – %.0f triệu", minPrice / 1_000_000, maxPrice / 1_000_000);
        if (maxPrice != null) return String.format("dưới %.0f triệu", maxPrice / 1_000_000);
        if (minPrice != null) return String.format("trên %.0f triệu", minPrice / 1_000_000);
        return "";
    }
}
