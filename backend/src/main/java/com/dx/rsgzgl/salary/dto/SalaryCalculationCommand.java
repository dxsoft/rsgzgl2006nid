package com.dx.rsgzgl.salary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SalaryCalculationCommand(
        @NotBlank String personCode,
        String orgCode,
        @Min(2006) int year,
        @Min(1) @Max(12) int month,
        String changeType
) {
}
