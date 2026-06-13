package com.dx.rsgzgl.salary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SalaryReconcileCommand(
        @NotBlank String personCode,
        @NotBlank String orgCode,
        @Min(2006) int year,
        @Min(1) @Max(12) int month,
        String changeType
) {

    public SalaryCalculationCommand toCalculationCommand() {
        return new SalaryCalculationCommand(personCode, orgCode, year, month, changeType);
    }
}
