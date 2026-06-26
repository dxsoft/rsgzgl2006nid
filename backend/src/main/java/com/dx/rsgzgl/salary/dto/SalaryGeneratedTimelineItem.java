package com.dx.rsgzgl.salary.dto;

import java.math.BigDecimal;
import java.util.List;

public record SalaryGeneratedTimelineItem(
        String source,
        String sourceId,
        int year,
        int month,
        String changeType,
        String note,
        String historyId,
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
