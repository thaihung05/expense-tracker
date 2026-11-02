package com.kei.backend.report;

import com.kei.backend.category.CategoryType;

/** Tong tien cua 1 danh muc trong 1 khoang thoi gian. */
public record CategoryTotal(Long categoryId, String name, String icon, CategoryType type, Long total) {
}
