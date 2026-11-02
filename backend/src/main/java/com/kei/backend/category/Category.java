package com.kei.backend.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CategoryType type;

    @Column(length = 10)
    private String icon;

    /** null = danh muc mac dinh dung chung cho moi nguoi. */
    @Column(name = "user_id")
    private Long userId;

    protected Category() {
    }

    public Category(Long userId, String name, CategoryType type, String icon) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.icon = icon;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public CategoryType getType() { return type; }
    public String getIcon() { return icon; }
    public Long getUserId() { return userId; }

    public boolean isDefault() {
        return userId == null;
    }

    /** Nguoi dung nay co duoc dung danh muc nay khong (mac dinh hoac cua chinh ho). */
    public boolean isVisibleTo(Long currentUserId) {
        return userId == null || userId.equals(currentUserId);
    }
}
