package com.dx.rsgzgl.salary.service;

import com.dx.rsgzgl.salary.dto.SalaryGeneratedTimelineResult;

public interface SalaryGeneratedTimelineService {

    SalaryGeneratedTimelineResult generateAndCompare(String personCode, Integer limit);
}
