package com.dx.rsgzgl.salary.dto;

public record SalaryFieldConfigIssue(
        String itemCode,
        String itemName,
        String severity,
        String message
) {
}
