package com.dx.rsgzgl.salary.dto;

import java.math.BigDecimal;

public record SalaryRecordSummary(String personCode, int year, int month, BigDecimal totalAmount) {
}
