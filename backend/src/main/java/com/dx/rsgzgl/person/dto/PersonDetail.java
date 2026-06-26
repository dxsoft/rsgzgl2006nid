package com.dx.rsgzgl.person.dto;

public record PersonDetail(
        String personCode,
        String personName,
        String orgCode,
        String orgName,
        String idCard,
        String gender,
        String birthDate,
        String personCategory,
        String organizationType,
        String postCategory,
        String workStartDate,
        String joinOrgDate,
        String currentPost,
        String postLevel,
        String postStartDate,
        Integer workYears,
        String education,
        String politicalStatus,
        String nation,
        String bankAccount
) {
}
