package com.dx.rsgzgl.person.dto;

public record PersonAssessmentBatchCandidate(
        Long id,
        String personCode,
        String personName,
        String orgCode,
        String year,
        String result
) {
}
