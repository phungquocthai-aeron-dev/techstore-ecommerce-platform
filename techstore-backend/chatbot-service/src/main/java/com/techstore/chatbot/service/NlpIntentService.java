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
            log.warn("  [NLP] FULL EXCEPTION:", ex);
        }

        // Fallback: MessageParser rule-based
        return analyzeWithRules(message);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Gemini-based analysis
    // ─────────────────────────────────────────────────────────────────────────

    private IntentAnalysisResult analyzeWithGemini(String message) throws Exception {
        log.info(">>> [NLP] TRY Gemini với message: {}", message);
        String prompt = buildAnalysisPrompt(message);

        log.info("🧠 [Gemini] PROMPT:\n{}", prompt);
        log.info(">>> [NLP] TRY Gemini với message: {}", message);
        String rawJson = geminiService.generateContent(prompt);

        // 🔥 LOG QUAN TRỌNG NHẤT
        log.info("🔥 [Gemini] RAW RESPONSE:\n{}", rawJson);

        // Clean markdown nếu có
        String cleanJson =
                rawJson.replaceAll("(?s)```json\\s*", "").replaceAll("```", "").trim();

        log.info("🧹 [Gemini] CLEAN JSON:\n{}", cleanJson);

        try {
            IntentAnalysisResult result = objectMapper.readValue(cleanJson, IntentAnalysisResult.class);

            // 🔥 LOG KẾT QUẢ PARSE
            log.info("✅ [Gemini] PARSED RESULT: {}", result);

            return result;

        } catch (Exception ex) {
            log.warn("❌ [Gemini] Parse failed, retrying...");
            log.warn("❌ [Gemini] ERROR:", ex);

            String retryPrompt = buildRetryPrompt(message);

            log.info("🔁 [Gemini] RETRY PROMPT:\n{}", retryPrompt);

            String retryJson = geminiService.generateContent(retryPrompt);

            log.info("🔥 [Gemini] RETRY RAW:\n{}", retryJson);

            String retryClean = retryJson
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("```", "")
                    .trim();

            log.info("🧹 [Gemini] RETRY CLEAN:\n{}", retryClean);

            IntentAnalysisResult retryResult = objectMapper.readValue(retryClean, IntentAnalysisResult.class);

            log.info("✅ [Gemini] RETRY PARSED: {}", retryResult);

            return retryResult;
        }
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
    //    private String buildAnalysisPrompt(String message) {
    //        return """
    //				Bạn là hệ thống phân tích NLP cho TechStore — cửa hàng bán smartphone, laptop, linh kiện PC, phụ kiện.
    //
    //				Phân tích câu sau và trả về JSON theo đúng format bên dưới. CHỈ trả về JSON, không giải thích.
    //
    //				Câu cần phân tích: "%s"
    //
    //				Các intent có thể có:
    //				- PRODUCT_SEARCH: tìm/mua/xem sản phẩm (có thể kèm giá)
    //				- STOCK_CHECK: hỏi còn hàng/tồn kho
    //				- COMPARE: so sánh 2 sản phẩm
    //				- FAQ: hỏi chính sách (bảo hành, đổi trả, giao hàng, thanh toán, trả góp, khuyến mãi, liên hệ)
    //				- COUPON: hỏi mã giảm giá, coupon, voucher, ưu đãi
    //				- AI_ADVICE: tư vấn, gợi ý, câu hỏi kỹ thuật, chào hỏi
    //
    //				Lưu ý đặc biệt:
    //				- Chuẩn hóa tiếng lóng: "củ" = triệu, "em" = tôi muốn mua, "chơi game" = gaming
    //				- Sửa lỗi chính tả: "bào dứ" → "bao nhiêu", "lấp top" → "laptop"
    //				- Hiểu câu dài phức tạp, đại từ ("nó", "cái đó"): trích xuất entity chính
    //				- Giá: chuyển về đơn vị VNĐ (20 triệu = 20000000, 20 củ = 20000000)
    //				- keyword: chỉ tên sản phẩm, KHÔNG chứa từ chỉ giá/số tiền
    //				- Luôn suy luận và chuẩn hóa ngôn ngữ người dùng về dạng chuẩn trong lĩnh vực công nghệ.
    //				- Nếu gặp tiếng lóng, từ viết tắt, hoặc cách nói không chính xác, hãy suy đoán nghĩa hợp lý nhất dựa trên ngữ
    // cảnh.
    //				- Nếu câu có nhiều cách hiểu, chọn cách hiểu phổ biến nhất trong ngữ cảnh mua bán thiết bị công nghệ.
    //				- Nếu thiếu thông tin (ví dụ: "con này", "cái đó"), hãy cố gắng suy ra sản phẩm chính từ câu hoặc ngữ cảnh gần
    // nhất.
    //				- Nếu không chắc chắn hoàn toàn, vẫn phải đưa ra kết quả tốt nhất kèm confidence thấp hơn.
    //				- Được phép suy luận để chuẩn hóa câu (ví dụ: sửa lỗi chính tả, hiểu tiếng lóng, viết tắt)
    //				- Chỉ suy luận trong phạm vi hợp lý dựa trên ngữ cảnh câu
    //				- Không được tự tạo thông tin hoàn toàn mới không liên quan đến câu
    //				- Nếu có nhiều cách hiểu, chọn cách phổ biến nhất trong ngữ cảnh mua bán công nghệ
    //				- Nếu vẫn không chắc chắn, giữ giá trị null và giảm confidence
    //				- keyword: chỉ chứa tên sản phẩm hoặc loại sản phẩm (ví dụ: "laptop", "iphone 15", "laptop gaming"), KHÔNG
    // chứa giá, số tiền hoặc từ mô tả khác.
    //				- Nếu intent là COMPARE:
    //						+ compareProductA và compareProductB bắt buộc phải có nếu có thể xác định
    //						+ keyword phải để null
    //				- Nếu intent là FAQ:
    //						+ faqTopic phải là một trong: "bảo hành", "đổi trả", "giao hàng", "thanh toán", "trả góp", "khuyến mãi",
    // "liên hệ"
    //				- Trả về JSON hợp lệ, không có markdown, không có giải thích, không có text ngoài JSON.
    //				- Tất cả field phải tồn tại, nếu không có giá trị thì để null.
    //
    //				Trả về JSON:
    //				{
    //				"intent": "PRODUCT_SEARCH",
    //				"keyword": "laptop gaming",
    //				"minPrice": null,
    //				"maxPrice": 20000000,
    //				"compareProductA": null,
    //				"compareProductB": null,
    //				"faqTopic": null,
    //				"confidence": 0.95
    //				}
    //				"""
    //                .formatted(message);
    //    }

    private String buildAnalysisPrompt(String message) {
        return """
		Bạn là hệ thống phân tích NLP cho TechStore — cửa hàng bán smartphone, laptop, linh kiện PC, phụ kiện.

		Phân tích câu sau và trả về JSON theo đúng format bên dưới. CHỈ trả về JSON, không giải thích.

		Câu cần phân tích: "%s"

		Các intent:
		- PRODUCT_SEARCH: tìm/mua/xem sản phẩm (có thể kèm giá hoặc tên hãng nếu có)
		- STOCK_CHECK: hỏi còn hàng/tồn kho
		- COMPARE: so sánh 2 sản phẩm
		- FAQ: hỏi chính sách (bảo hành, đổi trả, giao hàng, thanh toán, trả góp, liên hệ, hướng dẫn hoặc cách mua hàng)
		- COUPON: hỏi mã giảm giá, ưu đãi, khuyến mãi
		- AI_ADVICE: tư vấn, gợi ý, kỹ thuật, chào hỏi

		Quy tắc xử lý:
		- Chuẩn hóa ngôn ngữ: sửa lỗi chính tả, hiểu tiếng lóng ("củ"=triệu, "lấp top"=laptop, "chơi game"=gaming)
		- Hiểu ngữ cảnh: xử lý câu phức tạp, đại từ ("nó", "cái đó")
		- Giá: chuyển về VNĐ (20 triệu / 20 củ → 20000000)
		- keyword: chỉ chứa tên sản phẩm hoặc loại sản phẩm, không chứa giá hoặc số tiền
		- Nếu câu người dùng nhắc tới tên hãng sản phẩm, điền tất cả hãng hợp lệ vào field "brandNames" (mảng, null nếu không có), chỉ tính các hãng thuộc smartphone, laptop, PC, linh kiện PC, phụ kiện điện tử
		Ví dụ: "tìm laptop Dell và ASUS dưới 20 triệu" → brandNames: ["Dell", "ASUS"].


		Quy tắc suy luận:
		- Suy luận hợp lý dựa trên ngữ cảnh, không bịa thông tin
		- Nếu nhiều cách hiểu phải chọn cách phổ biến nhất
		- Nếu thiếu dữ liệu phải suy đoán hợp lý hoặc để null
		- Nếu không chắc chắn phải giảm confidence

		Quy tắc theo intent:
		- COMPARE:
		+ compareProductA và compareProductB phải có nếu xác định được
		+ keyword = null
		- FAQ:
		+ faqTopic ∈ ["bảo hành","đổi trả","giao hàng","thanh toán","trả góp","khuyến mãi","liên hệ"]

		Trả về JSON hợp lệ, tất cả field phải tồn tại (null nếu không có):

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

    private String buildRetryPrompt(String message) {
        return """
			Phân tích câu sau và trả về JSON hợp lệ.

			Câu: "%s"

			Chỉ trả về JSON, không markdown, không giải thích.

			{
			"intent": "PRODUCT_SEARCH",
			"keyword": null,
			"minPrice": null,
			"maxPrice": null,
			"compareProductA": null,
			"compareProductB": null,
			"faqTopic": null,
			"confidence": 0.5
			}
			"""
                .formatted(message);
    }
}
