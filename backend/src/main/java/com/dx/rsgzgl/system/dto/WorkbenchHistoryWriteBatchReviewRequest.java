package com.dx.rsgzgl.system.dto;

import java.util.List;

public record WorkbenchHistoryWriteBatchReviewRequest(
        List<String> caseNos,
        String reviewCategory,
        String reviewReason
) {
}
