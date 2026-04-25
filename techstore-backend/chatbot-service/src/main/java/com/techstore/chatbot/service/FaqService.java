package com.techstore.chatbot.service;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

/**
 * Service xử lý FAQ (Frequently Asked Questions) bằng rule-based.
 *
 * <p>Không dùng AI cho phần này — câu trả lời được định nghĩa sẵn trong Map.
 * Matching theo keyword: nếu message chứa keyword nào → trả lời tương ứng.
 */
@Service
public class FaqService {

    /**
     * Map từ keyword → câu trả lời.
     * Dùng LinkedHashMap để giữ thứ tự ưu tiên khi match.
     */
    private final Map<String, String> faqMap = new LinkedHashMap<>();

    @PostConstruct
    public void initFaq() {
        // ===== Bảo hành =====
        faqMap.put(
                "bảo hành",
                """
				🛡️ **Chính sách bảo hành TechStore:**

				• **Smartphone & Laptop:** Bảo hành chính hãng 12 tháng tại các trung tâm bảo hành ủy quyền.
				• **Linh kiện PC (CPU, GPU, RAM, SSD...):** Bảo hành 24–36 tháng tùy hãng sản xuất.
				• **Phụ kiện (chuột, bàn phím, tai nghe):** Bảo hành 6–12 tháng.

				⚠️ Bảo hành không áp dụng cho: rơi vỡ, vào nước, tự ý sửa chữa.
				📞 Hotline bảo hành: 1800-xxxx (miễn phí, 8h–21h)
				""");

        // ===== Đổi trả =====
        faqMap.put(
                "đổi trả",
                """
				🔄 **Chính sách đổi trả TechStore:**

				• **Đổi hàng lỗi:** Trong vòng **7 ngày** kể từ ngày mua, sản phẩm lỗi do nhà sản xuất.
				• **Hoàn tiền:** Trong vòng **3 ngày** nếu sản phẩm lỗi không thể đổi.
				• **Điều kiện:** Còn nguyên hộp, đầy đủ phụ kiện, có hóa đơn mua hàng.

				❌ Không áp dụng cho: sản phẩm đã kích hoạt, bóc tem, hoặc có dấu hiệu tác động ngoại lực.
				""");

        // ===== Giao hàng =====
        faqMap.put(
                "giao hàng",
                """
				🚚 **Chính sách giao hàng TechStore:**

				• **Nội thành (Cần Thơ):** 2–4 giờ (giao nhanh) hoặc 1 ngày (giao tiêu chuẩn).
				• **Tỉnh thành khác:** 2–5 ngày làm việc.
				• **Miễn phí giao hàng:** Đơn từ 500.000đ trở lên.
				• **Phí ship:** 20.000đ – 50.000đ tùy khu vực.
				""");

        faqMap.put("ship", faqMap.get("giao hàng"));
        faqMap.put("vận chuyển", faqMap.get("giao hàng"));

        // ===== Thanh toán =====
        faqMap.put(
                "thanh toán",
                """
				💳 **Các phương thức thanh toán:**

				• Chuyển khoản ngân hàng
				• Thẻ tín dụng / ghi nợ (Visa, Mastercard, JCB)
				• Ví điện tử: VNPay
				• Trả góp 0% lãi suất qua thẻ tín dụng (đơn từ 3 triệu)
				""");

        // ===== Trả góp =====
        faqMap.put(
                "trả góp",
                """
				💰 **Chính sách trả góp:**

				• Trả góp 0% lãi suất qua thẻ tín dụng: Visa, Mastercard, JCB.
				• Kỳ hạn: 3, 6, 12, 24 tháng.
				• Đơn tối thiểu: 3.000.000đ.
				• Trả góp qua công ty tài chính: FE Credit, HD Saison (lãi suất thỏa thuận).
				""");

        // ===== Liên hệ =====
        faqMap.put(
                "liên hệ",
                """
				📞 **Thông tin liên hệ TechStore:**

				• **Hotline:** 1800-xxxx (miễn phí, 8h–21h mỗi ngày)
				• **Email:** support@techstore.vn
				• **Chat trực tuyến:** Ngay tại website này!
				• **Showroom:** 123 Nguyễn Văn Linh, Q.7, TP.HCM
				""");

        faqMap.put("hotline", faqMap.get("liên hệ"));
        faqMap.put("địa chỉ", faqMap.get("liên hệ"));
        // ===== Hướng dẫn mua hàng =====
        faqMap.put(
                "mua hàng",
                """
				🛒 **Hướng dẫn mua hàng tại TechStore:**

				**Bước 1:** Tìm sản phẩm bạn muốn (có thể dùng thanh tìm kiếm hoặc hỏi chatbot 😉)
				**Bước 2:** Chọn sản phẩm → nhấn **"Thêm vào giỏ hàng"** hoặc **"Mua ngay"**
				**Bước 3:** Kiểm tra giỏ hàng → nhập thông tin nhận hàng
				**Bước 4:** Chọn phương thức thanh toán phù hợp
				**Bước 5:** Xác nhận đơn hàng 🎉

				📦 Sau khi đặt hàng thành công:
				• Bạn sẽ nhận được xác nhận qua email hoặc điện thoại
				• Có thể theo dõi đơn hàng trực tiếp trên hệ thống

				💬 Nếu cần hỗ trợ, bạn có thể chat ngay tại đây hoặc gọi hotline!
				""");

        faqMap.put("mua như", faqMap.get("mua hàng"));
        faqMap.put("làm sao mua", faqMap.get("mua hàng"));
        faqMap.put("cách mua", faqMap.get("mua hàng"));
        faqMap.put("đặt hàng", faqMap.get("mua hàng"));
        faqMap.put("hướng dẫn mua", faqMap.get("mua hàng"));

        //        // ===== Khuyến mãi =====
        //        faqMap.put(
        //                "khuyến mãi",
        //                """
        //				🎁 **Chương trình khuyến mãi hiện tại:**
        //
        //				• Giảm 5% cho thành viên đăng ký mới.
        //				• Tặng tai nghe khi mua smartphone từ 10 triệu.
        //				• Giảm thêm 10% khi thanh toán qua MoMo.
        //				• Flash sale mỗi ngày 12h và 20h — giảm đến 30%!
        //
        //				🔔 Đăng ký nhận thông báo để không bỏ lỡ deal hot nhé!
        //				""");
        //
        //        faqMap.put("giảm giá", faqMap.get("khuyến mãi"));
        //        faqMap.put("sale", faqMap.get("khuyến mãi"));
    }

    /**
     * Tìm câu trả lời FAQ phù hợp với message.
     *
     * @param message tin nhắn của user (đã lowercase)
     * @return câu trả lời nếu match, null nếu không có FAQ phù hợp
     */
    public String findAnswer(String message) {
        String lower = message.toLowerCase();
        for (Map.Entry<String, String> entry : faqMap.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Kiểm tra message có phải câu hỏi FAQ không.
     */
    public boolean isFaqQuestion(String message) {
        return findAnswer(message) != null;
    }
}
