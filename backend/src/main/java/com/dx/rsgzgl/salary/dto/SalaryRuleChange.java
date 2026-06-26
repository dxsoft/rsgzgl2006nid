package com.dx.rsgzgl.salary.dto;

import java.math.BigDecimal;

public record SalaryRuleChange(
        String itemCode,
        String itemName,
        String beforeValue,
        String afterValue,
        BigDecimal beforeAmount,
        BigDecimal afterAmount,
        BigDecimal difference,
        String ruleNote
) {
}
