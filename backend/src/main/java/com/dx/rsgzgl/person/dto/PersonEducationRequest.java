package com.dx.rsgzgl.person.dto;

public record PersonEducationRequest(
        String educationCode,
        String educationName,
        String school,
        String enrollDate,
        String graduationDate,
        Integer studyYears,
        String educationType,
        String note,
        String summary
) {
}
