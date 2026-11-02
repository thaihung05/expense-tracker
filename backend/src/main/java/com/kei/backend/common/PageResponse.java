package com.kei.backend.common;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/** Ket qua phan trang gon gang tra ve cho client (khong lo cau truc noi bo cua Spring Data). */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
