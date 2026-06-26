package com.dx.rsgzgl.system.dto;

public record WorkbenchMetricResponse(
        String code,
        String title,
        long count,
        String hint
) {
}
