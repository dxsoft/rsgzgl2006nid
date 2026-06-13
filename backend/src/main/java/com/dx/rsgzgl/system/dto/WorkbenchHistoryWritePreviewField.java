package com.dx.rsgzgl.system.dto;

import java.math.BigDecimal;

public record WorkbenchHistoryWritePreviewField(
        String itemCode,
        String itemName,
        String historyField,
        BigDecimal amount,
        Boolean mapped,
        String issue
) {
}
