package com.dx.rsgzgl.salary.dto;

import java.util.List;

public record SalaryTimelineResult(
        String personCode,
        int checkedCount,
        int matchedCount,
        int differentCount,
        int errorCount,
        List<SalaryTimelineItem> items
) {
}
