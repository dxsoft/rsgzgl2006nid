package com.dx.rsgzgl.common.api;

import java.util.List;

public record PageResponse<T>(List<T> records, long total, long page, long size) {

    public static <T> PageResponse<T> of(List<T> records, long total, long page, long size) {
        return new PageResponse<>(records, total, page, size);
    }
}
