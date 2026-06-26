package com.dx.rsgzgl.system.dto;

import java.util.List;

public record WorkbenchHistoryWriteRollbackPreviewResponse(
        String caseNo,
        String workItemId,
        String personCode,
        String orgCode,
        Integer year,
        Integer month,
        String businessType,
        String status,
        Boolean rollbackable,
        String writePlanId,
        String historyId,
        WorkbenchHistoryWritePreviewHistoryRow previousHistory,
        WorkbenchHistoryWritePreviewHistoryRow insertedHistory,
        WorkbenchHistoryWritePreviewHistoryRow nextHistory,
        Boolean sidUpdateRequired,
        String sidPlan,
        List<String> issues,
        String confirmMessage
) {
}
