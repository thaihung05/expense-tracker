package com.kei.backend.budget;

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

/** Han muc chi moi thang cho 1 danh muc (ap dung lap lai cho moi thang). */
@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "amount_limit", nullable = false)
    private long amountLimit;

    protected Budget() {
    }

    public Budget(Long userId, Category category, long amountLimit) {
        this.userId = userId;
        this.category = category;
        this.amountLimit = amountLimit;
    }

    public void changeLimit(long amountLimit) {
        this.amountLimit = amountLimit;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Category getCategory() { return category; }
    public long getAmountLimit() { return amountLimit; }
}
