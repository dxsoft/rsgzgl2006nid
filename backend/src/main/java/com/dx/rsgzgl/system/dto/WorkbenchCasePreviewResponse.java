package com.dx.rsgzgl.system.dto;

import java.math.BigDecimal;
import java.util.List;

public record WorkbenchCasePreviewResponse(
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
        String trialStatus,
        Boolean trialMatched,
        BigDecimal trialBaselineTotal,
        BigDecimal trialCalculatedTotal,
        BigDecimal trialExpectedTotal,
        BigDecimal trialDifference,
        String trialSummary,
        List<WorkbenchCaseTrialChangeResponse> trialChanges
) {
}
