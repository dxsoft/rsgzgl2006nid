package com.dx.rsgzgl.system.dto;

import java.util.List;

public record WorkbenchHistoryWriteBatchRetestResponse(
        Integer total,
        Integer matched,
        Integer mismatched,
        Integer failed,
        List<WorkbenchHistoryWriteBatchRetestItemResponse> items
) {
}
