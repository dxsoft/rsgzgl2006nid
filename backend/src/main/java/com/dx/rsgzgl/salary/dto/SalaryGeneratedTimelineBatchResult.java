package com.dx.rsgzgl.salary.dto;

import java.util.List;

public record SalaryGeneratedTimelineBatchResult(
        String orgCode,
        String keyword,
        int checkedCount,
        int okCount,
        int issueCount,
        int differentCount,
        int missingHistoryCount,
        int errorCount,
        int unsupportedHistoryCount,
        List<SalaryGeneratedTimelineBatchItem> items
) {
}
