package com.kei.backend.quickentry;

import com.kei.backend.category.Category;
import com.kei.backend.category.CategoryRepository;
import com.kei.backend.common.TextNormalizer;
import java.util.Comparator;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Doan danh muc tu ghi chu, theo thu tu uu tien:
 * 1. Tu khoa nguoi dung da day (LEARNED) - vi no phan anh thoi quen rieng cua ho.
 * 2. Tu dien co san (DICTIONARY).
 * Moi buoc deu uu tien tu khoa dai nhat (cu the nhat).
 */
@Service
public class CategorySuggester {

    private final KeywordRuleRepository ruleRepository;
    private final CategoryRepository categoryRepository;

    public CategorySuggester(KeywordRuleRepository ruleRepository, CategoryRepository categoryRepository) {
        this.ruleRepository = ruleRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Suggestion> suggest(Long userId, String note) {
        String text = TextNormalizer.normalize(note);
        if (text.isEmpty()) {
            return Optional.empty();
        }

        Optional<Suggestion> learned = ruleRepository.findByUserId(userId).stream()
                .filter(rule -> TextNormalizer.containsPhrase(text, rule.getKeyword()))
                .max(Comparator.comparingInt((KeywordRule r) -> r.getKeyword().length())
                        .thenComparingInt(KeywordRule::getHits))
                .map(rule -> new Suggestion(rule.getCategory(), rule.getKeyword(), "LEARNED"));
        if (learned.isPresent()) {
            return learned;
        }

        return CategoryDictionary.lookup(note).flatMap(match -> findDefaultByName(match.categoryName())
                .map(category -> new Suggestion(category, match.keyword(), "DICTIONARY")));
    }

    private Optional<Category> findDefaultByName(String name) {
        String key = TextNormalizer.normalize(name);
        return categoryRepository.findByUserIdIsNull().stream()
                .filter(c -> TextNormalizer.normalize(c.getName()).equals(key))
                .findFirst();
    }

    public record Suggestion(Category category, String keyword, String source) {
    }
}
