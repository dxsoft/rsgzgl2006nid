package com.dx.rsgzgl.salary.dto;

import java.math.BigDecimal;
import java.util.List;

public record SalaryBatchReconcileResult(
        String orgCode,
        int year,
        int month,
        int checkedCount,
        int passedCount,
        int failedCount,
        int skippedCount,
        BigDecimal totalDifference,
        List<SalaryBatchReconcileItem> items
) {
}
