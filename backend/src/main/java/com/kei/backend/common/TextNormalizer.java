package com.kei.backend.common;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** Chuan hoa chu tieng Viet de so sanh: "Trà Sữa  " -> "tra sua". */
public final class TextNormalizer {

    private static final Pattern MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_WORD = Pattern.compile("[^a-z0-9]+");

    private TextNormalizer() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String noAccents = MARKS.matcher(Normalizer.normalize(text, Normalizer.Form.NFD)).replaceAll("");
        String lower = noAccents.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
        return NON_WORD.matcher(lower).replaceAll(" ").trim();
    }

    /** true neu "keyword" xuat hien nhu 1 cum tu tron ven trong "text" (ca 2 da chuan hoa). */
    public static boolean containsPhrase(String normalizedText, String normalizedKeyword) {
        if (normalizedKeyword.isEmpty()) {
            return false;
        }
        return (" " + normalizedText + " ").contains(" " + normalizedKeyword + " ");
    }
}
