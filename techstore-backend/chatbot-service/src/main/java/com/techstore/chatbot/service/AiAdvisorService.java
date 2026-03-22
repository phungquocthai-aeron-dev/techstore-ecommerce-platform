package com.techstore.chatbot.service;

import org.springframework.stereotype.Service;

import com.techstore.chatbot.constant.ResponseType;
import com.techstore.chatbot.dto.response.ChatResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service xử lý tư vấn AI tổng quát.
 *
 * <p>Được gọi khi message không match bất kỳ intent rule-based nào.
 * Gửi prompt lên Gemini như một nhân viên tư vấn bán hàng.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiAdvisorService {

    private final GeminiService geminiService;

    /**
     * Tư vấn AI như nhân viên bán hàng TechStore.
     *
     * @param userMessage tin nhắn gốc của user
     * @return chatbot response với câu trả lời từ AI
     */
    public ChatResponse handleAiAdvice(String userMessage) {
        String prompt = buildAdvisorPrompt(userMessage);

        log.info("AI advisor handling: '{}'", userMessage);

        try {
            String aiResponse = geminiService.generateContent(prompt);

            return ChatResponse.builder()
                    .type(ResponseType.TEXT)
                    .message(aiResponse)
                    .build();

        } catch (Exception ex) {
            log.error("AI advisor failed: {}", ex.getMessage(), ex);
            return ChatResponse.builder()
                    .type(ResponseType.TEXT)
                    .message("😊 Xin chào! Mình là trợ lý TechStore. "
                            + "Hiện tại mình đang gặp chút vấn đề kỹ thuật. "
                            + "Bạn vui lòng thử lại sau ít phút nhé! "
                            + "Hoặc gọi hotline 1800-xxxx để được hỗ trợ trực tiếp.")
                    .build();
        }
    }

    /**
     * Build prompt với context đầy đủ để Gemini đóng vai tư vấn viên.
     */
    private String buildAdvisorPrompt(String userMessage) {
        return """
				Bạn là Techie - trợ lý tư vấn bán hàng thông minh của TechStore, một cửa hàng công nghệ uy tín tại Việt Nam.

				**Danh mục sản phẩm TechStore:**
				- Smartphone: iPhone, Samsung, Xiaomi, OPPO, Vivo...
				- Laptop: Gaming, văn phòng, đồ họa, MacBook...
				- Linh kiện PC: CPU (Intel/AMD), GPU (NVIDIA/AMD), RAM, SSD, HDD, Mainboard, PSU, Case
				- Phụ kiện: Tai nghe, chuột, bàn phím, webcam, màn hình...

				**Phong cách tư vấn:**
				- Thân thiện, nhiệt tình như nhân viên bán hàng am hiểu công nghệ
				- Dùng emoji phù hợp (không lạm dụng)
				- Trả lời ngắn gọn, đúng trọng tâm
				- Nếu user hỏi về nhu cầu cụ thể (gaming, học tập, làm việc...) → gợi ý sản phẩm phù hợp
				- Nếu user hỏi về kỹ thuật → giải thích dễ hiểu

				**Câu hỏi của khách hàng:** %s

				Hãy trả lời bằng tiếng Việt.
				"""
                .formatted(userMessage);
    }
}
