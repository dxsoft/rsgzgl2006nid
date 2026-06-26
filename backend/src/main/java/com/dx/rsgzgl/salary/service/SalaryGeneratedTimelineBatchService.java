package com.dx.rsgzgl.salary.service;

import com.dx.rsgzgl.salary.dto.SalaryGeneratedTimelineBatchResult;

public interface SalaryGeneratedTimelineBatchService {

    SalaryGeneratedTimelineBatchResult scan(String orgCode, String keyword, Integer limit, Integer eventLimit);
}
