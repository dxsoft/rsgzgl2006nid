package com.dx.rsgzgl.system.dto;

import java.util.List;

public record WorkbenchHistoryWriteBatchExecuteResponse(
        Integer total,
        Integer success,
        Integer failed,
        Integer skipped,
        List<WorkbenchHistoryWriteExecuteResponse> items
) {
}
