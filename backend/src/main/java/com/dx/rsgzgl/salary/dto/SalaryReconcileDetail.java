package com.dx.rsgzgl.salary.dto;

import java.math.BigDecimal;

public record SalaryReconcileDetail(
        String itemCode,
        String itemName,
        BigDecimal legacyAmount,
        BigDecimal calculatedAmount,
        BigDecimal difference,
        boolean passed
) {
}
