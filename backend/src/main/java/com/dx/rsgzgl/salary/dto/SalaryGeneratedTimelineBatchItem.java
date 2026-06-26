package com.dx.rsgzgl.salary.dto;

import java.util.List;

public record SalaryGeneratedTimelineBatchItem(
        String personCode,
        String personName,
        String orgCode,
        int expectedCount,
        int matchedCount,
        int differentCount,
        int missingHistoryCount,
        int errorCount,
        int unsupportedHistoryCount,
        String status,
        String firstIssue,
        List<SalaryGeneratedTimelineItem> issueItems
) {
}
