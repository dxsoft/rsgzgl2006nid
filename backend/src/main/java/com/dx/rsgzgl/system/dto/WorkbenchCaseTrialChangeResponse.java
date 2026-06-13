package com.dx.rsgzgl.system.dto;

import java.math.BigDecimal;

public record WorkbenchCaseTrialChangeResponse(
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
