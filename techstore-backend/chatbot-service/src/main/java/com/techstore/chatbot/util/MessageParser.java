package com.techstore.chatbot.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class MessageParser {

    // ─── Keyword lists ────────────────────────────────────────────────────────

    private static final List<String> STOCK_KEYWORDS =
            List.of("còn hàng", "còn không", "hết hàng", "tồn kho", "có sẵn", "in stock");

    private static final List<String> COMPARE_KEYWORDS =
            List.of("so sánh", "compare", "khác nhau", "nên mua cái nào", "tốt hơn");

    private static final List<String> PRODUCT_KEYWORDS = List.of(
            "laptop",
            "macbook",
            "surface",
            "notebook",
            "điện thoại",
            "iphone",
            "samsung",
            "xiaomi",
            "oppo",
            "vivo",
            "realme",
            "smartphone",
            "phone",
            "cpu",
            "i3",
            "i5",
            "i7",
            "i9",
            "ryzen",
            "core",
            "gpu",
            "vga",
            "rtx",
            "gtx",
            "radeon",
            "ram",
            "ssd",
            "hdd",
            "ổ cứng",
            "bộ nhớ",
            "mainboard",
            "bo mạch",
            "psu",
            "nguồn",
            "case",
            "thùng máy",
            "tai nghe",
            "chuột",
            "bàn phím",
            "máy tính",
            "pc",
            "gaming",
            "linh kiện");

    private static final List<String> PRICE_KEYWORDS =
            List.of("giá", "triệu", "tr", "nghìn", "dưới", "trên", "tầm", "khoảng", "budget");

    // Regex giá: "15 triệu", "15tr", "15.5 triệu"
    private static final Pattern PRICE_MILLION =
            Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(?:triệu|tr)\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern PRICE_FROM =
            Pattern.compile("(?:từ|trên|hơn)\\s*(\\d+(?:[.,]\\d+)?)\\s*(?:triệu|tr)\\b", Pattern.CASE_INSENSITIVE);

    // ─── Intent detection ────────────────────────────────────────────────────

    public boolean isStockCheck(String message) {
        String lower = message.toLowerCase();
        return STOCK_KEYWORDS.stream().anyMatch(lower::contains);
    }

    public boolean isCompare(String message) {
        String lower = message.toLowerCase();
        return COMPARE_KEYWORDS.stream().anyMatch(lower::contains);
    }

    public boolean isProductSearch(String message) {
        String lower = message.toLowerCase();
        return PRODUCT_KEYWORDS.stream().anyMatch(lower::contains) || hasPriceKeyword(lower);
    }

    public boolean hasPriceKeyword(String message) {
        String lower = message.toLowerCase();
        return PRICE_KEYWORDS.stream().anyMatch(lower::contains);
    }

    // ─── Price parsing ───────────────────────────────────────────────────────

    /**
     * Parse giá tối đa.
     * "dưới 20 triệu" / "20tr" / "tầm 15 triệu" → 20_000_000.0
     */
    public Double parseMaxPrice(String message) {
        String lower = message.toLowerCase();
        // Nếu có "từ/trên/hơn X" thì X là min, bỏ qua khi tìm max
        Matcher fromMatcher = PRICE_FROM.matcher(lower);
        double fromValue =
                fromMatcher.find() ? Double.parseDouble(fromMatcher.group(1).replace(",", ".")) * 1_000_000 : -1;

        Matcher m = PRICE_MILLION.matcher(lower);
        Double maxPrice = null;
        while (m.find()) {
            double val = Double.parseDouble(m.group(1).replace(",", ".")) * 1_000_000;
            if (val == fromValue) continue; // bỏ qua giá min
            if (maxPrice == null || val > maxPrice) maxPrice = val;
        }
        return maxPrice;
    }

    /**
     * Parse giá tối thiểu.
     * "từ 10 triệu" / "trên 5tr" → 10_000_000.0
     */
    public Double parseMinPrice(String message) {
        Matcher m = PRICE_FROM.matcher(message.toLowerCase());
        if (m.find()) {
            return Double.parseDouble(m.group(1).replace(",", ".")) * 1_000_000;
        }
        return null;
    }

    // ─── Keyword / category extraction ──────────────────────────────────────

    /**
     * Lấy keyword sản phẩm thuần túy — bỏ hết từ chỉ giá/số tiền/số lượng.
     *
     * "laptop gaming dưới 20 triệu" → "laptop gaming"
     * "iphone 15 tầm 25 triệu"      → "iphone 15"
     */
    public String extractSearchKeyword(String message) {
        String result = message.toLowerCase()

                // Loại cụm chỉ giá (phải làm trước khi xóa số đơn lẻ)
                .replaceAll("(?i)\\b(dưới|trên|tầm|khoảng|từ|đến|hơn)\\s+\\d+(?:[.,]\\d+)?\\s*(?:triệu|tr)\\b", "")
                .replaceAll("(?i)\\d+(?:[.,]\\d+)?\\s*(?:triệu|tr)\\b", "")

                // Loại từ phụ không liên quan
                .replaceAll(
                        "(?i)\\b(tôi muốn|cho tôi|giúp tôi|tìm|mua|xem|cần|muốn mua|có không|còn không|giá|budget)\\b",
                        "")

                // Xóa khoảng trắng thừa
                .replaceAll("\\s{2,}", " ")
                .trim();

        return result.isEmpty() ? message.trim() : result;
    }

    /**
     * Nhận diện categoryType cho filter.
     * Trả về: LAPTOP | SMARTPHONE | PC_COMPONENT | ACCESSORY | null
     */
    public String extractCategoryType(String message) {
        String lower = message.toLowerCase();
        if (lower.matches(".*\\b(laptop|macbook|surface|notebook).*")) return "LAPTOP";
        if (lower.matches(".*\\b(điện thoại|iphone|samsung galaxy|xiaomi|smartphone|android).*")) return "SMARTPHONE";
        if (lower.matches(".*\\b(cpu|gpu|vga|ram|ssd|hdd|mainboard|psu|case|linh kiện).*")) return "PC_COMPONENT";
        if (lower.matches(".*\\b(tai nghe|chuột|bàn phím|webcam|loa|màn hình|phụ kiện).*")) return "ACCESSORY";
        return null;
    }

    /**
     * Nhận diện PC component type cụ thể.
     */
    public String extractPcComponentType(String message) {
        String lower = message.toLowerCase();
        if (lower.matches(".*\\b(cpu|bộ xử lý|processor|i3|i5|i7|i9|ryzen).*")) return "CPU";
        if (lower.matches(".*\\b(gpu|vga|card đồ họa|rtx|gtx|radeon).*")) return "GPU";
        if (lower.matches(".*\\b(ram|memory|ddr).*")) return "RAM";
        if (lower.matches(".*\\b(ssd|solid state).*")) return "SSD";
        if (lower.matches(".*\\b(hdd|ổ cứng|hard disk).*")) return "HDD";
        if (lower.matches(".*\\b(mainboard|bo mạch|motherboard).*")) return "MAINBOARD";
        if (lower.matches(".*\\b(psu|nguồn|power supply).*")) return "PSU";
        if (lower.matches(".*\\b(case|thùng máy|vỏ máy).*")) return "CASE";
        return null;
    }

    /**
     * Extract 2 tên sản phẩm từ câu so sánh.
     * "so sánh iPhone 15 và Samsung S24" → ["iphone 15", "samsung s24"]
     */
    public String[] extractCompareProducts(String message) {
        String cleaned =
                message.toLowerCase().replaceFirst("(?i)^\\s*so sánh\\s+", "").trim();

        Matcher m = Pattern.compile("(.+?)\\s+(?:và|vs\\.?|với)\\s+(.+)", Pattern.CASE_INSENSITIVE)
                .matcher(cleaned);

        if (m.find()) {
            return new String[] {m.group(1).trim(), m.group(2).trim()};
        }
        return null;
    }
}
