package com.dx.rsgzgl.system.dto;

import java.util.List;

public record WorkbenchHistoryWriteRollbackBatchPreviewResponse(
        Integer total,
        Integer rollbackable,
        Integer blocked,
        String safetyToken,
        String safetyTokenExpiresAt,
        String confirmMessage,
        List<WorkbenchHistoryWriteRollbackPreviewResponse> items
) {
}
