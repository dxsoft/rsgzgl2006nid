package com.dx.rsgzgl.salary.dto;

public record SalaryExpectedEventCandidate(
        String source,
        String sourceId,
        String personCode,
        int year,
        int month,
        String changeType,
        String note
) {
}
