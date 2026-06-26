package com.dx.rsgzgl.system.dto;

public record WorkbenchHistoryWriteBatchRetestItemResponse(
        String caseNo,
        String personCode,
        String orgCode,
        String businessType,
        String status,
        Boolean totalMatched,
        Integer mismatchCount,
        String message
) {
}
