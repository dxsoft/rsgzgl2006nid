package com.dx.rsgzgl.salary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SalaryBatchReconcileCommand(
        @NotBlank String orgCode,
        @Min(2006) int year,
        @Min(1) @Max(12) int month,
        @Min(1) @Max(500) Integer limit,
        String changeType
) {

    public int safeLimit() {
        return limit == null ? 100 : limit;
    }
}
