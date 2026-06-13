package com.dx.rsgzgl.salary.service;

import com.dx.rsgzgl.salary.dto.SalaryCalculationCommand;
import com.dx.rsgzgl.salary.dto.SalaryCalculationResult;

public interface SalaryCalculationService {

    SalaryCalculationResult calculate(SalaryCalculationCommand command);
}
