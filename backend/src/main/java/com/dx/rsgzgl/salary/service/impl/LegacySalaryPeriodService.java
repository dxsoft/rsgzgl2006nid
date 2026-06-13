package com.dx.rsgzgl.salary.service.impl;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.salary.dto.SalaryPeriodItem;
import com.dx.rsgzgl.salary.mapper.LegacySalaryMapper;
import com.dx.rsgzgl.salary.service.SalaryPeriodService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class LegacySalaryPeriodService implements SalaryPeriodService {

    private static final int DEFAULT_LIMIT = 24;
    private static final int MAX_LIMIT = 120;

    private final LegacySalaryMapper legacySalaryMapper;

    public LegacySalaryPeriodService(LegacySalaryMapper legacySalaryMapper) {
        this.legacySalaryMapper = legacySalaryMapper;
    }

    @Override
    public List<SalaryPeriodItem> periods(String orgCode, Integer limit) {
        if (!StringUtils.hasText(orgCode)) {
            throw new BusinessException("ORG_CODE_REQUIRED", "Organization code is required.");
        }
        int safeLimit = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        return legacySalaryMapper.findPeriodsByOrg(orgCode.trim(), safeLimit);
    }
}
