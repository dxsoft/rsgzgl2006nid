package com.dx.rsgzgl.system.dto;

public record WorkbenchGeneratedIssueReviewResponse(
        String workItemId,
        String personCode,
        String orgCode,
        String reviewStatus,
        String reviewReason,
        String reviewedBy,
        String reviewedAt,
        String retestStatus,
        String retestSummary,
        String retestedAt
) {
}
