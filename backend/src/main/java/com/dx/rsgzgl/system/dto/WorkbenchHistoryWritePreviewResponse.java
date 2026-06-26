package com.dx.rsgzgl.system.dto;

import java.util.List;

public record WorkbenchHistoryWritePreviewResponse(
        String caseNo,
        String workItemId,
        String personCode,
        String orgCode,
        Integer year,
        Integer month,
        String businessType,
        String status,
        Boolean writable,
        String writePlanId,
        String existingHistoryId,
        WorkbenchHistoryWritePreviewHistoryRow previousHistory,
        WorkbenchHistoryWritePreviewHistoryRow nextHistory,
        Boolean sidUpdateRequired,
        String sidPlan,
        List<WorkbenchHistoryWritePreviewField> fields,
        List<String> issues
) {
}
