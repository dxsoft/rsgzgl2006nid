package com.dx.rsgzgl.system.dto;

import java.math.BigDecimal;
import java.util.List;

public record WorkbenchCaseSnapshotResponse(
        String caseNo,
        String workItemId,
        String personCode,
        String orgCode,
        Integer year,
        Integer month,
        String businessType,
        String trialStatus,
        Boolean trialMatched,
        BigDecimal trialBaselineTotal,
        BigDecimal trialCalculatedTotal,
        BigDecimal trialExpectedTotal,
        BigDecimal trialDifference,
        List<WorkbenchCaseTrialChangeResponse> trialChanges,
        List<WorkbenchCaseSnapshotItemResponse> salaryItems,
        String snapshotJson,
        String snapshotBy,
        String snapshotAt
) {
}
