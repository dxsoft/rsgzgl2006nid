package com.dx.rsgzgl.person.dto;

import java.util.List;

public record PersonAssessmentBatchRequest(
        String orgCode,
        String year,
        String defaultResult,
        String summary,
        Integer limit,
        List<PersonAssessmentBatchItem> items
) {
}
