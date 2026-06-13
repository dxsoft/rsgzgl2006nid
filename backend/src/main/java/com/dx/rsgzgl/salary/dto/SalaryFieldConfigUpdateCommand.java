package com.dx.rsgzgl.salary.dto;

public record SalaryFieldConfigUpdateCommand(
        String fieldCap,
        String fieldCaps,
        String category,
        String category6,
        String activeFlag,
        String activeFlag2006,
        Integer sequence
) {
}
