package com.dx.rsgzgl.salary.dto;

import java.math.BigDecimal;

public record SalaryHistoryLinkItem(
        String id,
        String nextId,
        String personCode,
        int year,
        int month,
        String changeType,
        BigDecimal totalAmount
) {
}
