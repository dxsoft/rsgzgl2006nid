package com.dx.rsgzgl.salary.dto;

public record SalaryFieldConfigItem(
        String itemCode,
        String itemName,
        String category,
        String activeFlag,
        int sequence
) {
}
