package com.dx.rsgzgl.system.dto;

public record WorkbenchHistoryWriteBatchLedgerResponse(
        String batchNo,
        String action,
        Integer total,
        Integer success,
        Integer failed,
        Integer skipped,
        Integer matched,
        Integer mismatched,
        String summary,
        String operator,
        String createdAt
) {
}
