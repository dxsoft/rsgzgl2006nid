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
        String reviewStatus,
        String workflowStatus,
        String closureStatus,
        String closureMessage,
        String nextActionCode,
        String nextActionLabel,
        String reviewReason,
        String reviewedBy,
        String reviewedAt,
        String retestStatus,
        String retestSummary,
        String retestedAt
) {
    public WorkbenchItemResponse(
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
            String reviewStatus,
            String workflowStatus,
            String closureStatus,
            String closureMessage,
            String nextActionCode,
            String nextActionLabel
    ) {
        this(id, source, status, businessType, personCode, personName, orgCode, year, month, title, summary,
                trialStatus, reviewStatus, workflowStatus, closureStatus, closureMessage, nextActionCode, nextActionLabel,
                "", "", "", "", "", "");
    }
}
