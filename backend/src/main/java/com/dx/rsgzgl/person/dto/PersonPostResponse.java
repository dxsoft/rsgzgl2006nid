package com.dx.rsgzgl.person.dto;

public record PersonPostResponse(
        Long id,
        String personCode,
        String personName,
        String orgCode,
        String postCode,
        String postName,
        String postLevel,
        String rankCode,
        String currentPostCode,
        String currentPostName,
        String postCategory,
        String startDate,
        Integer excludedYears,
        String currentPostFlag,
        String payrollFlag
) {
}
