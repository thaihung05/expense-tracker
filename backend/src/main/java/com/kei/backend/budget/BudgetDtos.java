package com.kei.backend.budget;

import com.kei.backend.category.CategoryResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public final class BudgetDtos {

    private BudgetDtos() {
    }

    public record BudgetRequest(
            @NotNull(message = "Vui lòng chọn danh mục") Long categoryId,
            @NotNull(message = "Vui lòng nhập hạn mức") @Positive(message = "Hạn mức phải lớn hơn 0")
            @Max(value = 999_999_999_999L, message = "Hạn mức quá lớn") Long limit) {
    }

    /** OK: duoi 80%, WARNING: tu 80% den duoi 100%, EXCEEDED: tu 100% tro len. */
    public enum Status { OK, WARNING, EXCEEDED }

    public record BudgetResponse(
            Long id, CategoryResponse category, long limit, long spent, long remaining, int percent, Status status) {
    }
}
