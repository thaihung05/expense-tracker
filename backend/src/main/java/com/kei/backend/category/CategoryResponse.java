package com.kei.backend.category;

public record CategoryResponse(Long id, String name, CategoryType type, String icon, boolean isDefault) {

    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getType(), c.getIcon(), c.isDefault());
    }
}
