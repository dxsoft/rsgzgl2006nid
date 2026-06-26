package com.dx.rsgzgl.system.dto;

import java.util.List;

public record WorkbenchHistoryWriteBatchPreviewResponse(
        Integer total,
        Integer ready,
        Integer blocked,
        Integer warning,
        Integer executable,
        String safetyToken,
        String safetyExpiresAt,
        String safetySummary,
        List<WorkbenchHistoryWritePreviewResponse> items
) {
}
