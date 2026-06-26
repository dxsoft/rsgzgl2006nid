package com.dx.rsgzgl.salary.dto;

import java.math.BigDecimal;

public record SalaryCalculationDetail(
        String itemCode,
        String itemName,
        BigDecimal amount,
        String ruleCode,
        String ruleNote
) {
}
