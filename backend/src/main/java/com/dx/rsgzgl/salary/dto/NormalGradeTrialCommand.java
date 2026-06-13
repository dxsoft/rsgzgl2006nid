package com.dx.rsgzgl.salary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record NormalGradeTrialCommand(
        @NotBlank String personCode,
        @NotBlank String orgCode,
        @Min(2006) int year,
        @Min(1) @Max(12) int month,
        String changeType,
        String baselineHistoryId
) {
    public NormalGradeTrialCommand(String personCode, String orgCode, int year, int month, String changeType) {
        this(personCode, orgCode, year, month, changeType, null);
    }

    public NormalGradeTrialCommand(String personCode, String orgCode, int year, int month) {
        this(personCode, orgCode, year, month, null, null);
    }
}
