package com.dx.rsgzgl.system.dto;

import java.math.BigDecimal;

public record WorkbenchHistoryWriteComparisonField(
        String itemCode,
        String itemName,
        String historyField,
        BigDecimal expectedAmount,
        BigDecimal actualAmount,
        Boolean mapped,
        Boolean matched,
        String issue
) {
}
