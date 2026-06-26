package com.dx.rsgzgl.salary.dto;

public record SalaryGeneratedTimelineCoverage(
        String changeType,
        int expectedCount,
        int matchedHistoryCount,
        int missingHistoryCount,
        int unsupportedHistoryCount
) {
}
