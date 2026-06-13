package com.dx.rsgzgl.salary.service;

import com.dx.rsgzgl.salary.dto.SalaryPeriodItem;

import java.util.List;

public interface SalaryPeriodService {

    List<SalaryPeriodItem> periods(String orgCode, Integer limit);
}
