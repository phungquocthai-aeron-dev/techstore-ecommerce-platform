package com.techstore.chatbot.service;

import org.springframework.stereotype.Service;

import com.techstore.chatbot.constant.ResponseType;
import com.techstore.chatbot.dto.IntentAnalysisResult;
import com.techstore.chatbot.dto.request.ChatRequest;
import com.techstore.chatbot.dto.response.ChatResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrator chính. Thứ tự xử lý:
 *
 * 1. NlpIntentService phân tích message (Gemini → fallback rule-based)
 * 2. Dispatch theo intent đến đúng service
 * 3. Lưu lịch sử chat
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final NlpIntentService nlpIntentService;
    private final FaqService faqService;
    private final ProductChatService productChatService;
    private final CompareChatService compareChatService;
    private final CouponChatService couponChatService;
    private final AiAdvisorService aiAdvisorService;
    private final ChatHistoryService chatHistoryService;

    public ChatResponse processMessage(ChatRequest request, Long userId) {
        String message = request.getMessage().trim();
        String sessionId = request.getSessionId();

        log.info("╔══════════════════════════════════════════╗");
        log.info("║         CHATBOT REQUEST START            ║");
        log.info("╠══════════════════════════════════════════╣");
        log.info("║ userId    : {}", userId);
        log.info("║ sessionId : {}", sessionId);
        log.info("║ message   : \"{}\"", message);
        log.info("╚══════════════════════════════════════════╝");

        chatHistoryService.saveUserMessage(message, userId, sessionId);

        // ── Step 1: FAQ nhanh (rule-based, không tốn Gemini token) ──────────
        // FAQ dùng keyword matching đơn giản, không cần AI
        if (faqService.isFaqQuestion(message)) {
            log.info("──── [FAST PATH] FAQ matched, skip NLP ────");
            String answer = faqService.findAnswer(message);
            ChatResponse response = ChatResponse.builder()
                    .type(ResponseType.TEXT)
                    .message(answer)
                    .build();
            chatHistoryService.saveBotResponse(response, userId, sessionId);
            return response;
        }

        // ── Step 2: NLP phân tích intent (Gemini → fallback rule-based) ──────
        log.info("──── [STEP 1] NLP ANALYSIS ────");
        IntentAnalysisResult intent = nlpIntentService.analyze(message);

        // ── Step 3: Dispatch ──────────────────────────────────────────────────
        log.info("──── [STEP 2] ROUTING → {} (confidence={}) ────", intent.getIntent(), intent.getConfidence());

        long start = System.currentTimeMillis();
        ChatResponse response = dispatch(intent, message);
        long elapsed = System.currentTimeMillis() - start;

        chatHistoryService.saveBotResponse(response, userId, sessionId);

        String preview = response.getMessage() != null
                ? response.getMessage()
                                .replace("\n", "\\n")
                                .substring(0, Math.min(80, response.getMessage().length())) + "..."
                : "null";

        log.info("╔══════════════════════════════════════════╗");
        log.info("║         CHATBOT RESPONSE DONE            ║");
        log.info("╠══════════════════════════════════════════╣");
        log.info("║ intent    : {}", intent.getIntent());
        log.info("║ type      : {}", response.getType());
        log.info("║ elapsed   : {} ms", elapsed);
        log.info("║ preview   : \"{}\"", preview);
        log.info("╚══════════════════════════════════════════╝");

        return response;
    }

    private ChatResponse dispatch(IntentAnalysisResult intent, String originalMessage) {
        return switch (intent.getIntent()) {
            case "COUPON" -> {
                log.info("  → [CouponChatService] handleCouponQuery()");
                log.info("  → API call: order-service GET /coupons/available");
                yield couponChatService.handleCouponQuery();
            }

            case "STOCK_CHECK" -> {
                // Dùng keyword từ NLP (đã chuẩn hóa) thay vì original message
                String keyword = intent.getKeyword() != null ? intent.getKeyword() : originalMessage;
                log.info("  → [ProductChatService] handleStockCheck(\"{}\") [NLP keyword]", keyword);
                yield productChatService.handleStockCheck(keyword);
            }

            case "COMPARE" -> {
                log.info("  → [CompareChatService] handleCompare()");
                log.info(
                        "  →   productA=\"{}\", productB=\"{}\"",
                        intent.getCompareProductA(),
                        intent.getCompareProductB());

                // Nếu Gemini đã extract được 2 sản phẩm → build message chuẩn
                if (intent.getCompareProductA() != null && intent.getCompareProductB() != null) {
                    String normalizedMsg =
                            "so sánh " + intent.getCompareProductA() + " và " + intent.getCompareProductB();
                    yield compareChatService.handleCompare(normalizedMsg);
                }
                yield compareChatService.handleCompare(originalMessage);
            }

            case "PRODUCT_SEARCH" -> {
                log.info("  → [ProductChatService] handleProductSearch()");
                log.info(
                        "  →   keyword=\"{}\", min={}, max={}",
                        intent.getKeyword(),
                        intent.getMinPrice(),
                        intent.getMaxPrice());
                // Truyền intent đã parse sẵn để không cần parse lại
                yield productChatService.handleProductSearchFromIntent(
                        intent.getKeyword(), intent.getMinPrice(), intent.getMaxPrice());
            }

            default -> {
                // AI_ADVICE hoặc intent không xác định
                log.info("  → [AiAdvisorService] handleAiAdvice()");
                yield aiAdvisorService.handleAiAdvice(originalMessage);
            }
        };
    }

    public Object getChatHistory(Long userId, String sessionId) {
        if (userId != null) return chatHistoryService.getHistoryByUser(userId);
        if (sessionId != null && !sessionId.isBlank()) return chatHistoryService.getHistoryBySession(sessionId);
        return java.util.List.of();
    }
}
