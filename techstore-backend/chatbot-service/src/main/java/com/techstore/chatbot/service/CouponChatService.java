package com.techstore.chatbot.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.techstore.chatbot.client.CouponServiceClient;
import com.techstore.chatbot.constant.ResponseType;
import com.techstore.chatbot.dto.response.ChatResponse;
import com.techstore.chatbot.dto.response.CouponResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponChatService {

    private final CouponServiceClient couponServiceClient;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ChatResponse handleCouponQuery() {
        try {
            log.info("  [Coupon] → GET order-service/coupons/available");

            List<CouponResponse> coupons =
                    couponServiceClient.getAvailableCoupons().getResult();

            log.info("  [Coupon] ← {} mã đang available", coupons != null ? coupons.size() : 0);

            if (coupons == null || coupons.isEmpty()) {
                return ChatResponse.builder()
                        .type(ResponseType.TEXT)
                        .message(
                                """
								😔 Hiện tại chưa có mã giảm giá nào đang hoạt động.

								💡 Bạn có thể tham gia **mini-game hàng ngày** để đổi điểm lấy mã giảm giá:
								👉 http://localhost:4200/coupons

								Mỗi ngày có **3 lượt chơi miễn phí** — trả lời đúng câu hỏi để tích điểm!
								""")
                        .build();
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🎟️ **Mã giảm giá đang có hiệu lực:**\n\n");

            coupons.stream().limit(3).forEach(c -> {
                // ✅ Dùng getName() thay getCode() — CouponResponse không có field code
                sb.append("• **").append(c.getName()).append("**");

                if ("PERCENTAGE".equals(c.getDiscountType())) {
                    sb.append(" — Giảm ")
                            .append(c.getDiscountValue().intValue())
                            .append("%");
                    // Hiển thị giảm tối đa nếu có
                    if (c.getMaxDiscount() != null && c.getMaxDiscount() > 0) {
                        sb.append(String.format(" (tối đa %,.0f đ)", c.getMaxDiscount()));
                    }
                } else {
                    sb.append(String.format(" — Giảm %,.0f đ", c.getDiscountValue()));
                }

                if (c.getMinOrderValue() != null && c.getMinOrderValue() > 0) {
                    sb.append(String.format(" | đơn từ %,.0f đ", c.getMinOrderValue()));
                }

                // ✅ Dùng getUsageLimit() thay getMaxUsage() — field đã đổi tên
                if (c.getUsageLimit() != null) {
                    int used = c.getUsedCount() != null ? c.getUsedCount() : 0;
                    int remaining = c.getUsageLimit() - used;
                    sb.append(" | còn ").append(remaining).append(" lượt");
                }

                // Hiển thị hạn sử dụng
                if (c.getEndDate() != null) {
                    sb.append(" | HSD: ").append(c.getEndDate().format(DATE_FMT));
                }

                sb.append("\n");
            });

            if (coupons.size() > 3) {
                sb.append("\n📋 Còn **").append(coupons.size() - 3).append(" mã khác** — xem đầy đủ tại:\n");
                sb.append("👉 **http://localhost:4200/coupons**\n");
            }

            sb.append(
                    """

					---
					🎮 **Mini-game đổi điểm lấy mã giảm giá:**
					Trả lời câu hỏi đúng → tích điểm → đổi mã độc quyền!
					Mỗi ngày **3 lượt chơi miễn phí**
					👉 http://localhost:4200/coupons
					""");

            return ChatResponse.builder()
                    .type(ResponseType.TEXT)
                    .message(sb.toString())
                    .data(coupons.stream().limit(3).toList())
                    .metadata(java.util.Map.of("total", coupons.size(), "showing", Math.min(3, coupons.size())))
                    .build();

        } catch (Exception ex) {
            log.error("  [Coupon] ✗ Failed: {}", ex.getMessage(), ex);
            return ChatResponse.builder()
                    .type(ResponseType.TEXT)
                    .message(
                            """
							⚠️ Không thể tải mã giảm giá lúc này.

							Bạn có thể xem trực tiếp tại:
							👉 http://localhost:4200/coupons

							🎮 Và đừng quên chơi mini-game hàng ngày để đổi mã giảm giá nhé!
							""")
                    .build();
        }
    }
}
