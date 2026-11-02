package com.kei.backend.quickentry;

import com.kei.backend.category.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/** 1 tu khoa nguoi dung da "day" cho app, vd "tra sua" -> An uong. */
@Entity
@Table(name = "keyword_rules")
public class KeywordRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 60)
    private String keyword;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private int hits;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected KeywordRule() {
    }

    public KeywordRule(Long userId, String keyword, Category category) {
        this.userId = userId;
        this.keyword = keyword;
        this.category = category;
        this.hits = 1;
        this.updatedAt = Instant.now();
    }

    /** Dung lai tu khoa: neu nguoi dung doi sang danh muc khac thi lay danh muc moi. */
    public void reinforce(Category newCategory) {
        if (!newCategory.getId().equals(category.getId())) {
            this.category = newCategory;
            this.hits = 1;
        } else {
            this.hits++;
        }
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getKeyword() { return keyword; }
    public Category getCategory() { return category; }
    public int getHits() { return hits; }
}
