package com.dx.rsgzgl.salary.dto;

import java.math.BigDecimal;
import java.util.List;

public record NormalGradeBatchTrialResult(
        String orgCode,
        int year,
        int month,
        int checkedCount,
        int matchedCount,
        int differentCount,
        int noExpectedCount,
        int skippedCount,
        int reverseStepCount,
        int levelPromotionCount,
        int notEligibleCount,
        BigDecimal totalDifference,
        List<NormalGradeBatchTrialItem> items
) {
}
