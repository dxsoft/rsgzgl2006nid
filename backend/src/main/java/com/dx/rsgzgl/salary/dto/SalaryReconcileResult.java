package com.dx.rsgzgl.salary.dto;

import java.math.BigDecimal;
import java.util.List;

public record SalaryReconcileResult(
        String personCode,
        int year,
        int month,
        String legacyHistoryId,
        BigDecimal legacyTotalAmount,
        BigDecimal calculatedTotalAmount,
        BigDecimal difference,
        boolean passed,
        List<SalaryReconcileDetail> details
) {
}
