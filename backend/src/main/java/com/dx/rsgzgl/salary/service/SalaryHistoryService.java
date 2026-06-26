package com.dx.rsgzgl.salary.service;

import com.dx.rsgzgl.salary.dto.SalaryHistoryItem;
import com.dx.rsgzgl.salary.dto.SalaryCalculationResult;

import java.util.List;

public interface SalaryHistoryService {

    List<SalaryHistoryItem> history(String personCode);

    SalaryCalculationResult detail(String historyId);
}
