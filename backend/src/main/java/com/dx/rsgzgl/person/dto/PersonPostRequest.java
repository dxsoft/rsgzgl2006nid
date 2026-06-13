package com.dx.rsgzgl.person.dto;

public record PersonPostRequest(
        String postCode,
        String postName,
        String postLevel,
        String rankCode,
        String currentPostCode,
        String currentPostFlag,
        String postCategory,
        String startDate,
        Integer excludedYears,
        String payrollFlag,
        String summary
) {
}
