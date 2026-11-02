package com.kei.backend.quickentry;

import com.kei.backend.common.TextNormalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tu dien co san: tu khoa (khong dau) -> ten danh muc mac dinh.
 * Neu nhieu tu khoa cung khop, uu tien tu khoa dai hon ("tien nuoc" thang "nuoc").
 */
public final class CategoryDictionary {

    private static final Map<String, List<String>> KEYWORDS_BY_CATEGORY = new LinkedHashMap<>();

    static {
        KEYWORDS_BY_CATEGORY.put("Ăn uống", List.of(
                "an", "an sang", "an trua", "an toi", "an vat", "an uong", "do an", "di cho",
                "cafe", "ca phe", "cf", "coffee", "tra", "tra sua", "tra da", "nuoc", "nuoc mia", "sinh to",
                "pho", "bun", "bun bo", "com", "com tam", "banh mi", "banh", "lau", "nuong", "nhau", "bia",
                "highlands", "starbucks", "phuc long", "katinat", "kfc", "lotteria", "jollibee", "pizza",
                "grabfood", "shopeefood", "baemin"));
        KEYWORDS_BY_CATEGORY.put("Đi lại", List.of(
                "grab", "be", "gojek", "xanh sm", "taxi", "xang", "do xang", "gui xe", "ve xe", "xe om",
                "xe buyt", "bus", "may bay", "ve may bay", "tau", "sua xe", "rua xe", "thay nhot", "di lai"));
        KEYWORDS_BY_CATEGORY.put("Nhà cửa", List.of(
                "tien nha", "thue nha", "tien phong", "nha", "phong tro", "do gia dung", "sua nha"));
        KEYWORDS_BY_CATEGORY.put("Hóa đơn", List.of(
                "dien", "tien dien", "tien nuoc", "internet", "wifi", "mang", "dien thoai", "nap the",
                "nap tien dien thoai", "4g", "5g", "hoa don", "gas", "truyen hinh"));
        KEYWORDS_BY_CATEGORY.put("Mua sắm", List.of(
                "shopee", "lazada", "tiki", "tiktok shop", "sendo", "mua sam", "quan ao", "ao", "quan",
                "giay", "dep", "tui", "my pham", "son", "sieu thi", "bach hoa xanh", "winmart", "co op"));
        KEYWORDS_BY_CATEGORY.put("Giải trí", List.of(
                "phim", "xem phim", "cgv", "lotte cinema", "netflix", "spotify", "youtube premium", "game",
                "karaoke", "du lich", "giai tri", "concert", "ve concert"));
        KEYWORDS_BY_CATEGORY.put("Sức khỏe", List.of(
                "thuoc", "nha thuoc", "kham", "kham benh", "benh vien", "phong kham", "nha khoa", "gym",
                "yoga", "bao hiem y te", "suc khoe"));
        KEYWORDS_BY_CATEGORY.put("Học tập", List.of(
                "sach", "hoc phi", "khoa hoc", "hoc", "udemy", "coursera", "hoc tieng", "luyen thi", "but", "vo"));
        KEYWORDS_BY_CATEGORY.put("Lương", List.of("luong", "nhan luong", "tra luong", "salary"));
        KEYWORDS_BY_CATEGORY.put("Thưởng", List.of("thuong", "bonus", "thuong tet"));
        KEYWORDS_BY_CATEGORY.put("Thu nhập khác", List.of(
                "li xi", "lixi", "hoan tien", "ban do", "thu nhap", "duoc cho", "tien lai", "lai", "freelance"));
    }

    private CategoryDictionary() {
    }

    /** Tra ve (tu khoa khop, ten danh muc) cua tu khoa dai nhat co trong ghi chu. */
    public static Optional<Match> lookup(String note) {
        String text = TextNormalizer.normalize(note);
        Match best = null;
        for (var entry : KEYWORDS_BY_CATEGORY.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (TextNormalizer.containsPhrase(text, keyword)
                        && (best == null || keyword.length() > best.keyword().length())) {
                    best = new Match(keyword, entry.getKey());
                }
            }
        }
        return Optional.ofNullable(best);
    }

    public record Match(String keyword, String categoryName) {
    }
}
