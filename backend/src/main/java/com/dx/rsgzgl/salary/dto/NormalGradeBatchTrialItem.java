package com.dx.rsgzgl.salary.dto;

import java.math.BigDecimal;

public record NormalGradeBatchTrialItem(
        String personCode,
        String personName,
        String orgCode,
        String orgName,
        String baselineHistoryId,
        String expectedHistoryId,
        BigDecimal baselineTotalAmount,
        BigDecimal calculatedTotalAmount,
        BigDecimal expectedTotalAmount,
        BigDecimal differenceWithExpected,
        boolean matchedExpected,
        String beforeValue,
        String afterValue,
        BigDecimal beforeAmount,
        BigDecimal afterAmount,
        BigDecimal changeAmount,
        String ruleType,
        String ruleNote,
        String status,
        String message
) {
}
