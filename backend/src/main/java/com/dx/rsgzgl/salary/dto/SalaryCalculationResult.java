package com.dx.rsgzgl.salary.dto;

import java.math.BigDecimal;
import java.util.List;

public record SalaryCalculationResult(
        String personCode,
        int year,
        int month,
        BigDecimal totalAmount,
        List<SalaryCalculationDetail> details
) {
}
