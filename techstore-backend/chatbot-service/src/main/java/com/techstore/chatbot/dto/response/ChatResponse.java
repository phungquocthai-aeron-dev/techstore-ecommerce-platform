package com.techstore.chatbot.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techstore.chatbot.constant.ResponseType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response trả về cho frontend sau mỗi lượt chat.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponse {

    /**
     * Loại response: TEXT | PRODUCT_LIST | COMPARE | STOCK
     * Frontend dùng để render UI phù hợp
     */
    private ResponseType type;

    /**
     * Nội dung text chatbot trả về
     */
    private String message;

    /**
     * Data kèm theo (danh sách sản phẩm, kết quả so sánh, ...)
     * Object linh hoạt, frontend parse theo type
     */
    private Object data;

    /**
     * Metadata bổ sung (tổng số kết quả, trang, ...)
     */
    private Object metadata;
}
