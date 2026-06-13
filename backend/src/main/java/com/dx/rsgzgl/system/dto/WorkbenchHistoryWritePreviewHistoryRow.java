package com.dx.rsgzgl.system.dto;

import java.math.BigDecimal;

public record WorkbenchHistoryWritePreviewHistoryRow(
        String historyId,
        String nextId,
        Integer year,
        Integer month,
        String changeType,
        BigDecimal totalAmount
) {
}
