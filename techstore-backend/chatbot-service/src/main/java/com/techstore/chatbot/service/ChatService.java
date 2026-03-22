package com.techstore.chatbot.service;

import org.springframework.stereotype.Service;

import com.techstore.chatbot.constant.ResponseType;
import com.techstore.chatbot.dto.request.ChatRequest;
import com.techstore.chatbot.dto.response.ChatResponse;
import com.techstore.chatbot.util.MessageParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ChatService là orchestrator trung tâm của chatbot.
 *
 * <p>Nhận request từ controller → phân tích intent → điều hướng đến service phù hợp.
 *
 * <p>Thứ tự ưu tiên xử lý intent (rule-based trước, AI sau):
 * <pre>
 * 1. FAQ (bảo hành, đổi trả, giao hàng, ...)  → FaqService
 * 2. Kiểm tra tồn kho ("còn hàng không")      → ProductChatService
 * 3. So sánh sản phẩm ("so sánh A và B")      → CompareChatService
 * 4. Tìm sản phẩm (laptop, iPhone, ...)       → ProductChatService
 * 5. Tư vấn AI (mọi thứ còn lại)             → AiAdvisorService
 * </pre>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final FaqService faqService;
    private final ProductChatService productChatService;
    private final CompareChatService compareChatService;
    private final AiAdvisorService aiAdvisorService;
    private final ChatHistoryService chatHistoryService;
    private final MessageParser messageParser;

    /**
     * Xử lý một lượt chat từ user.
     *
     * @param request  ChatRequest chứa message và sessionId
     * @param userId   userId từ JWT (null nếu anonymous)
     * @return ChatResponse trả về cho frontend
     */
    public ChatResponse processMessage(ChatRequest request, Long userId) {
        String message = request.getMessage().trim();
        String sessionId = request.getSessionId();

        log.info("Processing chat: userId={}, sessionId={}, message='{}'", userId, sessionId, message);

        // === Lưu tin nhắn USER vào DB ===
        chatHistoryService.saveUserMessage(message, userId, sessionId);

        // === Phân tích intent và điều hướng ===
        ChatResponse response = routeIntent(message);

        // === Lưu BOT response vào DB ===
        chatHistoryService.saveBotResponse(response, userId, sessionId);

        return response;
    }

    /**
     * Điều hướng message đến handler phù hợp theo thứ tự ưu tiên.
     */
    private ChatResponse routeIntent(String message) {

        // ── 1. FAQ: câu hỏi chính sách ──────────────────────────────────
        // Ưu tiên cao nhất vì đây là rule-based đơn giản, nhanh nhất
        if (faqService.isFaqQuestion(message)) {
            log.debug("Intent: FAQ");
            String answer = faqService.findAnswer(message);
            return ChatResponse.builder()
                    .type(ResponseType.TEXT)
                    .message(answer)
                    .build();
        }

        // ── 2. Kiểm tra tồn kho ──────────────────────────────────────────
        if (messageParser.isStockCheck(message)) {
            log.debug("Intent: STOCK_CHECK");
            return productChatService.handleStockCheck(message);
        }

        // ── 3. So sánh sản phẩm ──────────────────────────────────────────
        if (messageParser.isCompare(message)) {
            log.debug("Intent: COMPARE");
            return compareChatService.handleCompare(message);
        }

        // ── 4. Tìm kiếm sản phẩm ─────────────────────────────────────────
        if (messageParser.isProductSearch(message)) {
            log.debug("Intent: PRODUCT_SEARCH");
            return productChatService.handleProductSearch(message);
        }

        // ── 5. Tư vấn AI (fallback) ───────────────────────────────────────
        log.debug("Intent: AI_ADVICE (fallback)");
        return aiAdvisorService.handleAiAdvice(message);
    }

    /**
     * Lấy lịch sử chat.
     * Ưu tiên userId nếu đã đăng nhập, fallback về sessionId.
     */
    public Object getChatHistory(Long userId, String sessionId) {
        if (userId != null) {
            return chatHistoryService.getHistoryByUser(userId);
        }
        if (sessionId != null && !sessionId.isBlank()) {
            return chatHistoryService.getHistoryBySession(sessionId);
        }
        return java.util.List.of();
    }
}
