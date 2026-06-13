package com.dx.rsgzgl.system.dto;

import java.util.List;

public record WorkbenchItemsPageResponse(
        long total,
        int offset,
        int limit,
        List<WorkbenchItemResponse> items
) {
}
