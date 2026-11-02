package com.kei.backend.transaction;

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
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** LAZY: chi tai danh muc khi can, tranh truy van thua. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** So tien (dong), luon duong. Thu hay chi xem theo category.type. */
    @Column(nullable = false)
    private long amount;

    @Column(length = 255)
    private String note;

    @Column(name = "occurred_on", nullable = false)
    private LocalDate occurredOn;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Transaction() {
    }

    public Transaction(Long userId, Category category, long amount, String note, LocalDate occurredOn) {
        this.userId = userId;
        this.createdAt = Instant.now();
        update(category, amount, note, occurredOn);
    }

    public void update(Category category, long amount, String note, LocalDate occurredOn) {
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.occurredOn = occurredOn;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Category getCategory() { return category; }
    public long getAmount() { return amount; }
    public String getNote() { return note; }
    public LocalDate getOccurredOn() { return occurredOn; }
    public Instant getCreatedAt() { return createdAt; }
}
