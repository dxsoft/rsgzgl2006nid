package com.dx.rsgzgl.salary.dto;

import java.time.LocalDateTime;

public record SalaryFieldConfigAuditItem(
        Long id,
        String itemCode,
        String fieldName,
        String oldValue,
        String newValue,
        String changedBy,
        LocalDateTime changedAt
) {
}
