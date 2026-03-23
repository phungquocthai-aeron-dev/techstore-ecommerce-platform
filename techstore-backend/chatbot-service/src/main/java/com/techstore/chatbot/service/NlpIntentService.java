package com.techstore.chatbot.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.chatbot.dto.IntentAnalysisResult;
import com.techstore.chatbot.util.MessageParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service phân tích intent bằng Gemini AI thay cho regex thuần.
 *
 * Giải quyết 3 vấn đề NLP:
 * 1. Câu phức tạp / dài → Gemini hiểu ngữ nghĩa
 * 2. Tiếng lóng / lỗi chính tả → Gemini chuẩn hóa
 * 3. Paraphrase → Gemini nhận dạng ý định tương đương
 *
 * Strategy: Hybrid
 * - Gemini phân tích intent + extract entity
 * - Nếu Gemini lỗi hoặc confidence thấp → fallback về MessageParser (rule-based)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NlpIntentService {

    private final GeminiService geminiService;
    private final MessageParser messageParser; // fallback
    private final ObjectMapper objectMapper;

    /**
     * Phân tích message và trả về IntentAnalysisResult.
     * Luôn trả về kết quả (không throw) — worst case là fallback AI_ADVICE.
     */
    public IntentAnalysisResult analyze(String message) {
        log.info("  [NLP] Analyzing: \"{}\"", message);

        try {
            // Thử Gemini trước
            IntentAnalysisResult result = analyzeWithGemini(message);

            if (result != null && result.getConfidence() != null && result.getConfidence() >= 0.6) {
                log.info(
                        "  [NLP] Gemini result: intent={}, keyword=\"{}\", minPrice={}, maxPrice={}, confidence={}",
                        result.getIntent(),
                        result.getKeyword(),
                        result.getMinPrice(),
                        result.getMaxPrice(),
                        result.getConfidence());
                return result;
            }

            log.info(
                    "  [NLP] Gemini confidence too low ({}), falling back to rule-based",
                    result != null ? result.getConfidence() : "null");

        } catch (Exception ex) {
            log.warn("  [NLP] Gemini analysis failed: {}, falling back to rule-based", ex.getMessage());
        }

        // Fallback: MessageParser rule-based
        return analyzeWithRules(message);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Gemini-based analysis
    // ─────────────────────────────────────────────────────────────────────────

    private IntentAnalysisResult analyzeWithGemini(String message) throws Exception {
        String prompt = buildAnalysisPrompt(message);
        String rawJson = geminiService.generateContent(prompt);

        // Gemini đôi khi wrap JSON trong ```json ... ``` — cần strip
        String cleanJson =
                rawJson.replaceAll("(?s)```json\\s*", "").replaceAll("```", "").trim();

        return objectMapper.readValue(cleanJson, IntentAnalysisResult.class);
    }

    /**
     * Prompt yêu cầu Gemini phân tích intent + extract entity dạng JSON thuần.
     *
     * Thiết kế prompt:
     * - Cho Gemini biết context (TechStore bán tech)
     * - Liệt kê rõ các intent có thể có
     * - Yêu cầu chuẩn hóa tiếng lóng, lỗi chính tả
     * - Yêu cầu trả về JSON thuần không có markdown
     */
    private String buildAnalysisPrompt(String message) {
        return """
				Bạn là hệ thống phân tích NLP cho TechStore — cửa hàng bán smartphone, laptop, linh kiện PC, phụ kiện.

				Phân tích câu sau và trả về JSON theo đúng format bên dưới. CHỈ trả về JSON, không giải thích.

				Câu cần phân tích: "%s"

				Các intent có thể có:
				- PRODUCT_SEARCH: tìm/mua/xem sản phẩm (có thể kèm giá)
				- STOCK_CHECK: hỏi còn hàng/tồn kho
				- COMPARE: so sánh 2 sản phẩm
				- FAQ: hỏi chính sách (bảo hành, đổi trả, giao hàng, thanh toán, trả góp, khuyến mãi, liên hệ)
				- COUPON: hỏi mã giảm giá, coupon, voucher, ưu đãi
				- AI_ADVICE: tư vấn, gợi ý, câu hỏi kỹ thuật, chào hỏi

				Lưu ý đặc biệt:
				- Chuẩn hóa tiếng lóng: "củ" = triệu, "em" = tôi muốn mua, "chơi game" = gaming
				- Sửa lỗi chính tả: "bào dứ" → "bao nhiêu", "lấp top" → "laptop"
				- Hiểu câu dài phức tạp, đại từ ("nó", "cái đó"): trích xuất entity chính
				- Giá: chuyển về đơn vị VNĐ (20 triệu = 20000000, 20 củ = 20000000)
				- keyword: chỉ tên sản phẩm, KHÔNG chứa từ chỉ giá/số tiền

				Trả về JSON:
				{
				"intent": "PRODUCT_SEARCH",
				"keyword": "laptop gaming",
				"minPrice": null,
				"maxPrice": 20000000,
				"compareProductA": null,
				"compareProductB": null,
				"faqTopic": null,
				"confidence": 0.95
				}
				"""
                .formatted(message);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Rule-based fallback
    // ─────────────────────────────────────────────────────────────────────────

    private IntentAnalysisResult analyzeWithRules(String message) {
        log.info("  [NLP] Using rule-based fallback");
        IntentAnalysisResult result = new IntentAnalysisResult();

        if (isCouponQuery(message)) {
            result.setIntent("COUPON");
            result.setConfidence(0.9);
        } else if (messageParser.isStockCheck(message)) {
            result.setIntent("STOCK_CHECK");
            result.setConfidence(0.9);
        } else if (messageParser.isCompare(message)) {
            result.setIntent("COMPARE");
            String[] products = messageParser.extractCompareProducts(message);
            if (products != null) {
                result.setCompareProductA(products[0]);
                result.setCompareProductB(products[1]);
            }
            result.setConfidence(0.85);
        } else if (messageParser.isProductSearch(message)) {
            result.setIntent("PRODUCT_SEARCH");
            result.setKeyword(messageParser.extractSearchKeyword(message));
            result.setMinPrice(messageParser.parseMinPrice(message));
            result.setMaxPrice(messageParser.parseMaxPrice(message));
            result.setConfidence(0.8);
        } else {
            result.setIntent("AI_ADVICE");
            result.setConfidence(0.5);
        }

        log.info(
                "  [NLP] Rule-based result: intent={}, keyword=\"{}\", confidence={}",
                result.getIntent(),
                result.getKeyword(),
                result.getConfidence());
        return result;
    }

    private boolean isCouponQuery(String message) {
        String lower = message.toLowerCase();
        return lower.matches(".*\\b(mã giảm giá|coupon|voucher|ưu đãi|mã khuyến mãi|giảm giá|mã|code giảm).*");
    }
}
