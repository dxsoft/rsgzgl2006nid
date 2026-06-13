package com.dx.rsgzgl.system.dto;

import java.math.BigDecimal;

public record WorkbenchCaseSnapshotItemResponse(
        String itemCode,
        String itemName,
        BigDecimal amount,
        String ruleNote
) {
}
