package com.dx.rsgzgl.salary.dto;

import java.math.BigDecimal;
import java.util.List;

public record SalaryTimelineItem(
        String historyId,
        int year,
        int month,
        String changeType,
        BigDecimal historyTotalAmount,
        String baselineHistoryId,
        BigDecimal calculatedTotalAmount,
        BigDecimal differenceWithExpected,
        boolean matchedExpected,
        String status,
        String message,
        List<SalaryRuleChange> changes
) {
}
