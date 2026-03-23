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

    // ─── Gọi từ NlpIntentService — keyword + giá đã được parse sẵn ───────────
    public ChatResponse handleProductSearchFromIntent(String keyword, Double minPrice, Double maxPrice) {
        return doProductSearch(keyword, minPrice, maxPrice);
    }

    // ─── Gọi trực tiếp từ message gốc (fallback khi không qua NLP) ───────────
    public ChatResponse handleProductSearch(String message) {
        String keyword = messageParser.extractSearchKeyword(message);
        Double minPrice = messageParser.parseMinPrice(message);
        Double maxPrice = messageParser.parseMaxPrice(message);
        return doProductSearch(keyword, minPrice, maxPrice);
    }

    // ─── Core logic tìm kiếm ─────────────────────────────────────────────────
    private ChatResponse doProductSearch(String keyword, Double minPrice, Double maxPrice) {
        try {
            log.info("  [ProductSearch] keyword=\"{}\", minPrice={}, maxPrice={}", keyword, minPrice, maxPrice);
            log.info(
                    "  [ProductSearch] → GET product-service/products/search" + "?keyword={}&minPrice={}&maxPrice={}",
                    keyword,
                    minPrice,
                    maxPrice);

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
                return ChatResponse.builder()
                        .type(ResponseType.TEXT)
                        .message("😔 Xin lỗi, mình không tìm thấy sản phẩm phù hợp. Bạn mô tả chi tiết hơn không?")
                        .build();
            }

            List<ProductListResponseDTO> products = result.getContent();
            log.info("  [ProductSearch] Products:");
            products.forEach(p -> log.info(
                    "    • [{}] {} | {} | {} đ",
                    p.getId(),
                    p.getName(),
                    p.getBrandName(),
                    p.getBasePrice() != null ? String.format("%,.0f", p.getBasePrice()) : "N/A"));

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

    // ─── Stock check ──────────────────────────────────────────────────────────
    public ChatResponse handleStockCheck(String keyword) {
        try {
            log.info("  [StockCheck] keyword=\"{}\"", keyword);
            log.info("  [StockCheck] → GET product-service/products/search?keyword={}&size=5", keyword);

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

            ProductListResponseDTO firstProduct = productResult.getContent().get(0);
            log.info("  [StockCheck] → GET product-service/products/{}/variants", firstProduct.getId());

            List<VariantInfo> variants = productServiceClient
                    .getVariantsByProductId(firstProduct.getId())
                    .getResult();

            if (variants == null || variants.isEmpty()) {
                return ChatResponse.builder()
                        .type(ResponseType.STOCK)
                        .message("😔 Sản phẩm \"" + firstProduct.getName() + "\" chưa có phiên bản nào.")
                        .build();
            }

            List<Long> variantIds = variants.stream().map(VariantInfo::getId).toList();
            log.info("  [StockCheck] → POST warehouse-service/inventory/variant/total-stock/batch: {}", variantIds);

            List<VariantStockResponse> stocks =
                    warehouseServiceClient.getTotalStockByVariants(variantIds).getResult();

            log.info("  [StockCheck] ← Stock:");
            stocks.forEach(s -> log.info("    • variantId={} stock={}", s.getVariantId(), s.getStock()));

            Map<Long, Long> stockMap = stocks.stream()
                    .collect(Collectors.toMap(VariantStockResponse::getVariantId, VariantStockResponse::getStock));

            StringBuilder sb = new StringBuilder("📦 **Tồn kho: ")
                    .append(firstProduct.getName())
                    .append("**\n\n");
            boolean anyInStock = false;
            for (VariantInfo v : variants) {
                Long stock = stockMap.getOrDefault(v.getId(), 0L);
                if (stock > 0) anyInStock = true;
                sb.append("• **").append(v.getName()).append("**");
                if (v.getColor() != null) sb.append(" (").append(v.getColor()).append(")");
                sb.append(": ")
                        .append(stock > 0 ? "✅ Còn " + stock + " sp" : "❌ Hết hàng")
                        .append("\n");
            }
            if (!anyInStock) sb.append("\n💡 Tất cả đang hết hàng. Bạn có muốn xem sản phẩm tương tự không?");

            return ChatResponse.builder()
                    .type(ResponseType.STOCK)
                    .message(sb.toString())
                    .data(stocks)
                    .metadata(Map.of(
                            "productId",
                            firstProduct.getId(),
                            "productName",
                            firstProduct.getName(),
                            "variantCount",
                            variants.size()))
                    .build();

        } catch (Exception ex) {
            log.error("  [StockCheck] ✗ Failed: {}", ex.getMessage(), ex);
            return ChatResponse.builder()
                    .type(ResponseType.STOCK)
                    .message("⚠️ Không thể kiểm tra tồn kho. Vui lòng thử lại!")
                    .build();
        }
    }

    private String buildSearchSummary(
            List<ProductListResponseDTO> products, String keyword, Double minPrice, Double maxPrice) {
        StringBuilder sb =
                new StringBuilder("🔍 Tìm thấy **").append(products.size()).append(" sản phẩm**");
        if (keyword != null && !keyword.isBlank())
            sb.append(" cho \"").append(keyword).append("\"");
        if (maxPrice != null && minPrice != null)
            sb.append(String.format(" khoảng %.0f–%.0f triệu", minPrice / 1e6, maxPrice / 1e6));
        else if (maxPrice != null) sb.append(String.format(" dưới %.0f triệu", maxPrice / 1e6));
        else if (minPrice != null) sb.append(String.format(" trên %.0f triệu", minPrice / 1e6));
        sb.append(":\n\n");
        products.stream().limit(5).forEach(p -> {
            sb.append("• **").append(p.getName()).append("**");
            if (p.getBrandName() != null) sb.append(" – ").append(p.getBrandName());
            if (p.getBasePrice() != null) sb.append(String.format(" – %,.0f đ", p.getBasePrice()));
            sb.append("\n");
        });
        return sb.toString();
    }
}
