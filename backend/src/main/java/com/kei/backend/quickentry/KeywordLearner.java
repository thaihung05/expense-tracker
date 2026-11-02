package com.kei.backend.quickentry;

import com.kei.backend.category.Category;
import com.kei.backend.common.TextNormalizer;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ghi nho "ghi chu -> danh muc" sau moi lan nguoi dung luu giao dich tu o go nhanh.
 * Tu khoa = toi da 3 tu dau cua ghi chu (bo dau, bo so), vd "Trà sữa Phúc Long" -> "tra sua phuc".
 */
@Service
public class KeywordLearner {

    private static final int MAX_WORDS = 3;

    private final KeywordRuleRepository ruleRepository;

    public KeywordLearner(KeywordRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Transactional
    public void learn(Long userId, String note, Category category) {
        String keyword = toKeyword(note);
        if (keyword.isEmpty()) {
            return;
        }
        ruleRepository.findByUserIdAndKeyword(userId, keyword).ifPresentOrElse(
                rule -> rule.reinforce(category),
                () -> ruleRepository.save(new KeywordRule(userId, keyword, category)));
    }

    static String toKeyword(String note) {
        String keyword = Arrays.stream(TextNormalizer.normalize(note).split(" "))
                .filter(word -> !word.isBlank() && !word.chars().allMatch(Character::isDigit))
                .limit(MAX_WORDS)
                .collect(Collectors.joining(" "));
        return keyword.length() > 60 ? keyword.substring(0, 60).trim() : keyword;
    }
}
