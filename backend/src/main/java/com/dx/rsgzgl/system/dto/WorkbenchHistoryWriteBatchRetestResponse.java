package com.dx.rsgzgl.system.dto;

import java.util.List;

public record WorkbenchHistoryWriteBatchRetestResponse(
        String batchNo,
        Integer total,
        Integer matched,
        Integer mismatched,
        Integer failed,
        List<WorkbenchHistoryWriteBatchRetestItemResponse> items
) {
}
