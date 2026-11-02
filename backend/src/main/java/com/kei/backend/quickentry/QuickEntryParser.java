package com.kei.backend.quickentry;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Doc cau nhap nhanh kieu "cafe 35k hom qua" thanh: so tien, ngay, ghi chu.
 *
 * <p>So tien hieu duoc: 35k, 2.5k, 35 nghin, 1tr, 1tr2 (=1.200.000), 1.5tr, 2 trieu, 200.000, 200000.
 * So tron < 1000 hieu la nghin dong ("cafe 35" = 35.000d).
 * <p>Ngay hieu duoc: hom nay, hom qua, hom kia, 30/10, 30-10, 30/10/2026 (co dau hay khong dau deu duoc).
 * Khong co ngay thi lay hom nay.
 *
 * <p>Class thuan Java (khong dung database) nen test rat de: xem QuickEntryParserTest.
 */
public final class QuickEntryParser {

    // (?<![\p{L}\d]) va (?![\p{L}\d]): so phai dung rieng, khong dinh vao chu khac (vd "4g", "k8").
    private static final String START = "(?<![\\p{L}\\d.,/])";
    private static final String END = "(?![\\p{L}\\d])";

    private static final Pattern MILLION = Pattern.compile(
            START + "(\\d+(?:[.,]\\d+)?)\\s*(?:tr|triệu|trieu|củ|cu|m)(\\d{1,3})?" + END, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern THOUSAND = Pattern.compile(
            START + "(\\d+(?:[.,]\\d+)?)\\s*(?:k|nghìn|nghin|ngàn|ngan)" + END, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern GROUPED = Pattern.compile(
            START + "(\\d{1,3}(?:[.,]\\d{3})+)(?![\\p{L}\\d.,/])");
    private static final Pattern PLAIN = Pattern.compile(
            START + "(\\d+)(?![\\p{L}\\d.,/])");

    private static final Pattern DATE = Pattern.compile(
            "(?<![\\d/-])(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{4}|\\d{2}))?(?![\\d/-])");
    private static final Pattern TODAY = Pattern.compile("hôm nay|hom nay", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern YESTERDAY = Pattern.compile("hôm qua|hom qua", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern TWO_DAYS_AGO = Pattern.compile("hôm kia|hom kia", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final long MAX_AMOUNT = 999_999_999_999L;

    public ParsedEntry parse(String text, LocalDate today) {
        StringBuilder rest = new StringBuilder(text == null ? "" : text);
        LocalDate date = extractDate(rest, today);
        Long amount = extractAmount(rest);
        String note = rest.toString()
                .replaceAll("\\s+", " ")
                .replaceAll("^[\\s,.;:-]+|[\\s,.;:-]+$", "");
        return new ParsedEntry(amount, date, note);
    }

    private LocalDate extractDate(StringBuilder rest, LocalDate today) {
        if (cut(rest, TWO_DAYS_AGO) != null) {
            return today.minusDays(2);
        }
        if (cut(rest, YESTERDAY) != null) {
            return today.minusDays(1);
        }
        if (cut(rest, TODAY) != null) {
            return today;
        }
        Matcher m = DATE.matcher(rest);
        if (m.find()) {
            try {
                int day = Integer.parseInt(m.group(1));
                int month = Integer.parseInt(m.group(2));
                int year = today.getYear();
                if (m.group(3) != null) {
                    year = Integer.parseInt(m.group(3));
                    if (year < 100) {
                        year += 2000;
                    }
                }
                LocalDate date = LocalDate.of(year, month, day);
                rest.delete(m.start(), m.end());
                return date;
            } catch (DateTimeException ignored) {
                // vd "35/13" khong phai ngay hop le -> bo qua
            }
        }
        return today;
    }

    private Long extractAmount(StringBuilder rest) {
        Matcher m = MILLION.matcher(rest);
        if (m.find()) {
            long value = multiply(m.group(1), 1_000_000);
            if (m.group(2) != null) {
                // "1tr2" -> 200.000; "1tr25" -> 250.000; "1tr250" -> 250.000
                String tail = (m.group(2) + "000").substring(0, 3);
                value += Long.parseLong(tail) * 1_000;
            }
            return take(rest, m, value);
        }
        m = THOUSAND.matcher(rest);
        if (m.find()) {
            return take(rest, m, multiply(m.group(1), 1_000));
        }
        m = GROUPED.matcher(rest);
        if (m.find()) {
            return take(rest, m, Long.parseLong(m.group(1).replaceAll("[.,]", "")));
        }
        m = PLAIN.matcher(rest);
        if (m.find()) {
            long value = Long.parseLong(m.group(1));
            return take(rest, m, value < 1_000 ? value * 1_000 : value);
        }
        return null;
    }

    private static long multiply(String number, long unit) {
        return new BigDecimal(number.replace(',', '.')).multiply(BigDecimal.valueOf(unit)).longValue();
    }

    private static Long take(StringBuilder rest, Matcher m, long value) {
        rest.delete(m.start(), m.end());
        return value > 0 && value <= MAX_AMOUNT ? value : null;
    }

    private static String cut(StringBuilder rest, Pattern pattern) {
        Matcher m = pattern.matcher(rest);
        if (m.find()) {
            String found = m.group();
            rest.delete(m.start(), m.end());
            return found;
        }
        return null;
    }

    /** amount = null neu khong tim thay so tien. */
    public record ParsedEntry(Long amount, LocalDate date, String note) {
    }
}
