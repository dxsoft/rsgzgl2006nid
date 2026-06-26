package com.dx.rsgzgl.person.dto;

public record PersonBaseInfoResponse(
        String personCode,
        String personName,
        String orgCode,
        String personCategory,
        String organizationType,
        String postCategory,
        String workStartDate,
        String joinOrgDate,
        String teacherNurseStartDate,
        Integer teacherNurseFixedYears,
        String educationCode,
        String education,
        String rankCode,
        String currentPost,
        String postLevel,
        String postStartDate
) {
}
