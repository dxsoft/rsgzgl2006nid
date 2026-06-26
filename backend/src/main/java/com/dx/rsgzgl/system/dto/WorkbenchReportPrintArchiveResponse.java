package com.dx.rsgzgl.system.dto;

public record WorkbenchReportPrintArchiveResponse(
        Boolean printable,
        Boolean printed,
        Boolean reprinted,
        String latestAction,
        String latestBatchNo,
        String latestTargetType,
        String latestTargetCode,
        String latestOperator,
        String latestPrintedAt,
        Integer printCount,
        String status,
        String message
) {
}
