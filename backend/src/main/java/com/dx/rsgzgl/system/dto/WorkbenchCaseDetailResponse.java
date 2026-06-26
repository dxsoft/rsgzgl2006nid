package com.dx.rsgzgl.system.dto;

import java.math.BigDecimal;
import java.util.List;

public record WorkbenchCaseDetailResponse(
        String caseNo,
        String workItemId,
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
        String workflowStatus,
        String trialStatus,
        Boolean trialMatched,
        BigDecimal trialBaselineTotal,
        BigDecimal trialCalculatedTotal,
        BigDecimal trialExpectedTotal,
        BigDecimal trialDifference,
        String trialSummary,
        List<WorkbenchCaseTrialChangeResponse> trialChanges,
        List<WorkbenchCaseSnapshotItemResponse> salaryItems,
        String forceReason,
        String differenceReason,
        String cancelReason,
        String reviewStatus,
        String reviewReason,
        String reviewedBy,
        String reviewedAt,
        Boolean snapshotExists,
        String snapshotBy,
        String snapshotAt,
        WorkbenchHistoryWritePlanResponse historyWritePlan,
        WorkbenchReportPrintArchiveResponse reportPrintArchive,
        WorkbenchCaseClosureStatusResponse closureStatus,
        List<SystemAuditLogResponse> historyWriteAudits,
        List<SystemAuditLogResponse> reportAudits,
        List<SystemAuditLogResponse> audits,
        String handledBy,
        String handledAt
) {
}
