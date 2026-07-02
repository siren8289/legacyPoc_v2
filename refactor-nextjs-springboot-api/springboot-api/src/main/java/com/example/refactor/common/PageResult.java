package com.example.refactor.common;

import java.util.List;

/** 페이지 조회 결과 — SELECT projection + LIMIT/OFFSET. */
public record PageResult<T>(
        List<T> items,
        long totalCount,
        int page,
        int size
) {
    public static <T> PageResult<T> of(List<T> items, long totalCount, int page, int size) {
        return new PageResult<>(items, totalCount, page, size);
    }
}
