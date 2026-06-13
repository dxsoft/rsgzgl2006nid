package com.dx.rsgzgl.system.dto;

public record WorkbenchHistoryWriteExecuteResponse(
        String caseNo,
        String workItemId,
        String personCode,
        String orgCode,
        String writePlanId,
        String historyId,
        String status,
        Boolean sidUpdateRequired,
        String message
) {
}
