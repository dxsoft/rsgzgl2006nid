package com.dx.rsgzgl.system.dto;

import java.math.BigDecimal;
import java.util.List;

public record WorkbenchHistoryWriteConfirmResponse(
        String caseNo,
        String workItemId,
        String personCode,
        String orgCode,
        Integer year,
        Integer month,
        String businessType,
        String status,
        Boolean writable,
        Boolean executable,
        String writePlanId,
        String existingHistoryId,
        WorkbenchHistoryWritePreviewHistoryRow previousHistory,
        WorkbenchHistoryWritePreviewHistoryRow nextHistory,
        Boolean sidUpdateRequired,
        String sidPlan,
        BigDecimal totalAmount,
        Integer fieldCount,
        List<WorkbenchHistoryWritePreviewField> fields,
        List<String> issues,
        String confirmMessage
) {
}
