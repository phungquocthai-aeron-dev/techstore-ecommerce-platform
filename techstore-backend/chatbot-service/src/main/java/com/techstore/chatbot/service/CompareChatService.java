package com.techstore.chatbot.service;

import org.springframework.stereotype.Service;

import com.techstore.chatbot.client.ProductServiceClient;
import com.techstore.chatbot.constant.ResponseType;
import com.techstore.chatbot.dto.response.ChatResponse;
import com.techstore.chatbot.dto.response.PageResponseDTO;
import com.techstore.chatbot.dto.response.VariantInfo;
import com.techstore.chatbot.util.MessageParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service xử lý intent so sánh 2 sản phẩm.
 *
 * <p>Flow:
 * 1. Parse tên 2 sản phẩm từ message
 * 2. Search từng sản phẩm qua ProductService
 * 3. Build prompt so sánh chi tiết
 * 4. Gửi lên Gemini API
 * 5. Trả về kết quả so sánh
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompareChatService {

    private final ProductServiceClient productServiceClient;
    private final GeminiService geminiService;
    private final MessageParser messageParser;

    public ChatResponse handleCompare(String message) {
        // Bước 1: Parse tên 2 sản phẩm
        String[] products = messageParser.extractCompareProducts(message);

        if (products == null || products.length < 2) {
            return ChatResponse.builder()
                    .type(ResponseType.TEXT)
                    .message("🤔 Bạn muốn so sánh 2 sản phẩm nào? " + "Ví dụ: *\"So sánh iPhone 15 và Samsung S24\"*")
                    .build();
        }

        String nameA = products[0];
        String nameB = products[1];

        log.info("Comparing products: '{}' vs '{}'", nameA, nameB);

        try {
            // Bước 2: Tìm thông tin từng sản phẩm
            VariantInfo variantA = findFirstVariant(nameA);
            VariantInfo variantB = findFirstVariant(nameB);

            // Bước 3: Nếu không tìm thấy sản phẩm nào → thông báo
            if (variantA == null && variantB == null) {
                return ChatResponse.builder()
                        .type(ResponseType.TEXT)
                        .message("😔 Mình không tìm thấy thông tin của cả 2 sản phẩm \"" + nameA + "\" và \"" + nameB
                                + "\" trong hệ thống.")
                        .build();
            }

            // Bước 4: Build prompt gửi Gemini
            String prompt = buildComparePrompt(nameA, variantA, nameB, variantB);

            // Bước 5: Gọi Gemini
            String aiResponse = geminiService.generateContent(prompt);

            return ChatResponse.builder()
                    .type(ResponseType.COMPARE)
                    .message(aiResponse)
                    .data(new CompareData(variantA, variantB))
                    .build();

        } catch (Exception ex) {
            log.error("Compare failed: {}", ex.getMessage(), ex);
            // Fallback: so sánh bằng AI thuần (không có data từ DB)
            return fallbackCompareWithAi(nameA, nameB);
        }
    }

    /**
     * Tìm variant đầu tiên khớp với tên sản phẩm.
     */
    private VariantInfo findFirstVariant(String productName) {
        try {
            PageResponseDTO<VariantInfo> result = productServiceClient
                    .searchVariants(productName, 0, 1, "id", "DESC")
                    .getResult();

            if (result != null
                    && result.getContent() != null
                    && !result.getContent().isEmpty()) {
                return result.getContent().get(0);
            }
        } catch (Exception ex) {
            log.warn("Could not find product '{}': {}", productName, ex.getMessage());
        }
        return null;
    }

    /**
     * Build prompt so sánh dựa trên data từ DB.
     * Nếu không có data → chỉ dùng tên sản phẩm và để Gemini tự biết.
     */
    private String buildComparePrompt(String nameA, VariantInfo variantA, String nameB, VariantInfo variantB) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là chuyên gia tư vấn công nghệ của TechStore. ");
        prompt.append("Hãy so sánh 2 sản phẩm sau và đưa ra nhận xét khách quan, dễ hiểu.\n\n");

        prompt.append("**Sản phẩm 1: ").append(nameA).append("**\n");
        if (variantA != null) {
            prompt.append("- Tên variant: ").append(variantA.getName()).append("\n");
            if (variantA.getPrice() != null)
                prompt.append("- Giá: ")
                        .append(String.format("%,.0f đ", variantA.getPrice()))
                        .append("\n");
            if (variantA.getColor() != null)
                prompt.append("- Màu sắc: ").append(variantA.getColor()).append("\n");
        }

        prompt.append("\n**Sản phẩm 2: ").append(nameB).append("**\n");
        if (variantB != null) {
            prompt.append("- Tên variant: ").append(variantB.getName()).append("\n");
            if (variantB.getPrice() != null)
                prompt.append("- Giá: ")
                        .append(String.format("%,.0f đ", variantB.getPrice()))
                        .append("\n");
            if (variantB.getColor() != null)
                prompt.append("- Màu sắc: ").append(variantB.getColor()).append("\n");
        }

        prompt.append("\n**Yêu cầu so sánh:**\n");
        prompt.append("1. So sánh về hiệu năng\n");
        prompt.append("2. So sánh về giá trị đồng tiền\n");
        prompt.append("3. Phù hợp cho đối tượng nào\n");
        prompt.append("4. Kết luận: nên chọn sản phẩm nào và tại sao\n");
        prompt.append("\nTrả lời bằng tiếng Việt, ngắn gọn, dùng emoji cho sinh động.");

        return prompt.toString();
    }

    /**
     * Fallback khi không lấy được data từ DB.
     * Vẫn gọi Gemini nhưng chỉ dựa vào kiến thức của AI.
     */
    private ChatResponse fallbackCompareWithAi(String nameA, String nameB) {
        String prompt = String.format(
                "Bạn là tư vấn viên TechStore. Hãy so sánh %s và %s về: "
                        + "hiệu năng, giá trị, đối tượng phù hợp, và kết luận nên chọn cái nào. "
                        + "Trả lời bằng tiếng Việt, dùng emoji.",
                nameA, nameB);

        String aiResponse = geminiService.generateContent(prompt);

        return ChatResponse.builder()
                .type(ResponseType.COMPARE)
                .message(aiResponse)
                .build();
    }

    /**
     * Inner record để wrap data của 2 sản phẩm so sánh.
     */
    public record CompareData(VariantInfo productA, VariantInfo productB) {}
}
