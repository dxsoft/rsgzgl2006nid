package com.dx.rsgzgl.person.dto;

import java.util.List;

public record PersonAssessmentBatchResponse(
        String orgCode,
        String year,
        int checkedCount,
        int savedCount,
        int createdCount,
        int updatedCount,
        List<PersonAssessmentResponse> items
) {
}
