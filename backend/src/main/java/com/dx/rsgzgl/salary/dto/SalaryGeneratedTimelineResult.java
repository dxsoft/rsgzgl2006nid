package com.dx.rsgzgl.salary.dto;

import java.util.List;

public record SalaryGeneratedTimelineResult(
        String personCode,
        int expectedCount,
        int matchedCount,
        int differentCount,
        int missingHistoryCount,
        int errorCount,
        int unsupportedHistoryCount,
        List<SalaryGeneratedTimelineCoverage> coverage,
        List<SalaryGeneratedTimelineItem> items
) {
}
