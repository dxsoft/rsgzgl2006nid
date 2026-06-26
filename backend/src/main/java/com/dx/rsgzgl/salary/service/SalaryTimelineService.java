package com.dx.rsgzgl.salary.service;

import com.dx.rsgzgl.salary.dto.SalaryTimelineResult;

public interface SalaryTimelineService {

    SalaryTimelineResult replay(String personCode, Integer limit);
}
