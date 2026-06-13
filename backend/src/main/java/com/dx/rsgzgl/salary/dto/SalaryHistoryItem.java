package com.dx.rsgzgl.salary.dto;

import java.math.BigDecimal;

public record SalaryHistoryItem(String id, String personCode, int year, int month, String changeType, BigDecimal totalAmount) {
}
