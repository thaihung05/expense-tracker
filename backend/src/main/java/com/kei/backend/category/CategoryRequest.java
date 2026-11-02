package com.kei.backend.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Vui lòng nhập tên danh mục") @Size(max = 50, message = "Tên tối đa 50 ký tự") String name,
        @NotNull(message = "Vui lòng chọn loại thu hoặc chi") CategoryType type,
        @Size(max = 10) String icon) {
}
