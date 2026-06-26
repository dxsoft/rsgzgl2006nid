package com.dx.rsgzgl.system.dto;

import java.math.BigDecimal;
import java.util.List;

public record WorkbenchHistoryWriteComparisonResponse(
        String caseNo,
        String workItemId,
        String personCode,
        String orgCode,
        Integer year,
        Integer month,
        String businessType,
        String planNo,
        String planStatus,
        String executionResult,
        String insertedHistoryId,
        BigDecimal expectedTotal,
        BigDecimal actualTotal,
        Boolean totalMatched,
        String reviewStatus,
        String reviewCategory,
        String reviewReason,
        String reviewedBy,
        String reviewedAt,
        WorkbenchHistoryWritePreviewHistoryRow previousHistory,
        WorkbenchHistoryWritePreviewHistoryRow nextHistory,
        List<WorkbenchHistoryWriteComparisonField> fields
) {
}
