package com.dx.rsgzgl.salary.dto;

import java.math.BigDecimal;
import java.util.List;

public record NormalGradeTrialResult(
        String personCode,
        int year,
        int month,
        String baselineHistoryId,
        String expectedHistoryId,
        BigDecimal baselineTotalAmount,
        BigDecimal calculatedTotalAmount,
        BigDecimal expectedTotalAmount,
        BigDecimal differenceWithExpected,
        boolean matchedExpected,
        List<SalaryRuleChange> changes,
        List<SalaryReconcileDetail> expectedDiffs
) {
}
