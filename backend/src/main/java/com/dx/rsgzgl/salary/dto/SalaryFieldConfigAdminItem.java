package com.dx.rsgzgl.salary.dto;

public record SalaryFieldConfigAdminItem(
        String itemCode,
        String fieldCap,
        String fieldCaps,
        String category,
        String category6,
        String activeFlag,
        String activeFlag2006,
        int sequence,
        long auditCount,
        String lastChangedAt
) {
}
