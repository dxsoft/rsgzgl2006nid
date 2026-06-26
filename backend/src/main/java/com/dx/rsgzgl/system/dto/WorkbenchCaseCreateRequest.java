package com.dx.rsgzgl.system.dto;

public record WorkbenchCaseCreateRequest(
        String workItemId,
        String source,
        String businessType,
        String personCode,
        String personName,
        String orgCode,
        Integer year,
        Integer month,
        String title,
        String summary,
        Boolean force,
        String forceReason,
        String differenceReason
) {
}
