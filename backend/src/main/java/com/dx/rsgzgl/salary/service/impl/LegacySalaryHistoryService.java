package com.dx.rsgzgl.salary.service.impl;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.salary.dto.SalaryCalculationResult;
import com.dx.rsgzgl.salary.dto.SalaryHistoryItem;
import com.dx.rsgzgl.salary.dto.SalaryRecordSummary;
import com.dx.rsgzgl.salary.mapper.LegacySalaryMapper;
import com.dx.rsgzgl.salary.service.SalaryHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class LegacySalaryHistoryService implements SalaryHistoryService {

    private final LegacySalaryMapper legacySalaryMapper;
    private final SalaryDetailBuilder salaryDetailBuilder;
    private final PersonCodeParser personCodeParser;

    public LegacySalaryHistoryService(
            LegacySalaryMapper legacySalaryMapper,
            SalaryDetailBuilder salaryDetailBuilder,
            PersonCodeParser personCodeParser
    ) {
        this.legacySalaryMapper = legacySalaryMapper;
        this.salaryDetailBuilder = salaryDetailBuilder;
        this.personCodeParser = personCodeParser;
    }

    @Override
    public List<SalaryHistoryItem> history(String personCode) {
        PersonCodeParts parts = personCodeParser.parse(personCode);
        return legacySalaryMapper.findHistory(parts.orgCode(), parts.personNo());
    }

    @Override
    public SalaryCalculationResult detail(String historyId) {
        if (!StringUtils.hasText(historyId)) {
            throw new BusinessException("INVALID_HISTORY_ID", "Salary history id is required.");
        }
        SalaryRecordSummary summary = legacySalaryMapper.findSummaryById(historyId.trim())
                .orElseThrow(() -> new BusinessException("SALARY_HISTORY_NOT_FOUND", "Salary history not found: " + historyId));
        return new SalaryCalculationResult(
                summary.personCode(),
                summary.year(),
                summary.month(),
                summary.totalAmount(),
                salaryDetailBuilder.build(historyId.trim())
        );
    }

}
