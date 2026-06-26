package com.dx.rsgzgl.person.dto;

public record PersonBaseInfoRequest(
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
        String postStartDate,
        String summary
) {
}
