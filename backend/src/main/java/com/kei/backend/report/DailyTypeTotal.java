package com.kei.backend.report;

import com.kei.backend.category.CategoryType;
import java.time.LocalDate;

/** Tong thu hoac tong chi cua 1 ngay. */
public record DailyTypeTotal(LocalDate date, CategoryType type, Long total) {
}
