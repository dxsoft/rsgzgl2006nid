package com.dx.rsgzgl.person.dto;

public record PersonEducationResponse(
        Long id,
        String personCode,
        String personName,
        String orgCode,
        String educationCode,
        String educationName,
        String school,
        String enrollDate,
        String graduationDate,
        Integer studyYears,
        String educationType,
        String note
) {
}
