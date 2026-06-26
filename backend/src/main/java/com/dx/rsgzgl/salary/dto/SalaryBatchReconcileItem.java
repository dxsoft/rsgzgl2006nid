package com.dx.rsgzgl.salary.dto;

import java.math.BigDecimal;

public record SalaryBatchReconcileItem(
        String personCode,
        String personName,
        String orgCode,
        String orgName,
        BigDecimal legacyTotalAmount,
        BigDecimal calculatedTotalAmount,
        BigDecimal difference,
        boolean passed,
        String status,
        String message
) {
}
