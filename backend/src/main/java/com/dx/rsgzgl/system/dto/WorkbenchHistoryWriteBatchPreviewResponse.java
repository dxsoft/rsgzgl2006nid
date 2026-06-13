package com.dx.rsgzgl.system.dto;

import java.util.List;

public record WorkbenchHistoryWriteBatchPreviewResponse(
        Integer total,
        Integer ready,
        Integer blocked,
        Integer warning,
        List<WorkbenchHistoryWritePreviewResponse> items
) {
}
