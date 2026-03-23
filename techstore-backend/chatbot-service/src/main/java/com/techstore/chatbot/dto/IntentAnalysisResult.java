package com.techstore.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Kết quả phân tích intent từ Gemini API.
 * Gemini trả về JSON, deserialize vào object này.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntentAnalysisResult {

    /**
     * Intent chính của message:
     * PRODUCT_SEARCH | STOCK_CHECK | COMPARE | FAQ | COUPON | AI_ADVICE
     */
    private String intent;

    /**
     * Keyword sản phẩm đã được chuẩn hóa.
     * VD: "bào dứ 20 củ" → keyword="laptop", maxPrice=20000000
     */
    private String keyword;

    /** Giá tối thiểu (đơn vị VNĐ), null nếu không đề cập */
    private Double minPrice;

    /** Giá tối đa (đơn vị VNĐ), null nếu không đề cập */
    private Double maxPrice;

    /** Tên sản phẩm A khi so sánh */
    private String compareProductA;

    /** Tên sản phẩm B khi so sánh */
    private String compareProductB;

    /** FAQ topic nếu intent là FAQ */
    private String faqTopic;

    /**
     * Độ tin cậy của phân tích (0.0 - 1.0).
     * Nếu < 0.5 thì fallback về rule-based.
     */
    private Double confidence;
}
