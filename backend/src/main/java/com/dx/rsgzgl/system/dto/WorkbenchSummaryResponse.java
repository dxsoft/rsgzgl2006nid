package com.dx.rsgzgl.system.dto;

import java.util.List;

public record WorkbenchSummaryResponse(
        List<WorkbenchMetricResponse> metrics,
        List<WorkbenchItemResponse> todoItems,
        List<WorkbenchItemResponse> doneItems
) {
}
