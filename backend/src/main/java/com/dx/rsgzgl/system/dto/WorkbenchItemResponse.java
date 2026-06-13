package com.dx.rsgzgl.system.dto;

public record WorkbenchItemResponse(
        String id,
        String source,
        String status,
        String businessType,
        String personCode,
        String personName,
        String orgCode,
        Integer year,
        Integer month,
        String title,
        String summary,
        String trialStatus,
        String reviewStatus
) {
}
