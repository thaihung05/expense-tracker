package com.kei.backend.transaction;

import com.kei.backend.category.CategoryResponse;
import com.kei.backend.category.CategoryType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;

public final class TransactionDtos {

    private TransactionDtos() {
    }

    /**
     * learnKeyword = true: app ghi nho ghi chu -> danh muc, lan sau go tuong tu se tu chon danh muc.
     * Frontend bat co nay khi nguoi dung nhap bang o "go cau nhanh".
     */
    public record TransactionRequest(
            @NotNull(message = "Vui lòng nhập số tiền") @Positive(message = "Số tiền phải lớn hơn 0")
            @Max(value = 999_999_999_999L, message = "Số tiền quá lớn") Long amount,
            @NotNull(message = "Vui lòng chọn danh mục") Long categoryId,
            @NotNull(message = "Vui lòng chọn ngày") LocalDate occurredOn,
            @Size(max = 255, message = "Ghi chú tối đa 255 ký tự") String note,
            Boolean learnKeyword) {
    }

    public record TransactionResponse(
            Long id, long amount, CategoryType type, String note, LocalDate occurredOn,
            CategoryResponse category, Instant createdAt) {

        public static TransactionResponse from(Transaction t) {
            return new TransactionResponse(t.getId(), t.getAmount(), t.getCategory().getType(), t.getNote(),
                    t.getOccurredOn(), CategoryResponse.from(t.getCategory()), t.getCreatedAt());
        }
    }

    public record ParseRequest(@NotBlank(message = "Vui lòng nhập nội dung") @Size(max = 255) String text) {
    }

    /**
     * Ket qua doc cau "cafe 35k hom qua". Cac truong co the null neu khong doc duoc.
     * source: LEARNED (tu tu khoa da hoc), DICTIONARY (tu dien co san) hoac null (chua doan duoc).
     */
    public record ParseResponse(
            Long amount, LocalDate occurredOn, String note, CategoryResponse category,
            String matchedKeyword, String source) {
    }
}
