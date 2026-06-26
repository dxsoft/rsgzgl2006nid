package com.dx.rsgzgl.salary.service.impl;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.salary.dto.SalaryCalculationCommand;
import com.dx.rsgzgl.salary.dto.SalaryCalculationResult;
import com.dx.rsgzgl.salary.dto.SalaryHistoryItem;
import com.dx.rsgzgl.salary.mapper.LegacySalaryMapper;
import com.dx.rsgzgl.salary.service.SalaryCalculationService;
import org.springframework.stereotype.Service;

@Service
public class DefaultSalaryCalculationService implements SalaryCalculationService {

    private final LegacySalaryMapper legacySalaryMapper;
    private final SalaryDetailBuilder salaryDetailBuilder;
    private final PersonCodeParser personCodeParser;

    public DefaultSalaryCalculationService(
            LegacySalaryMapper legacySalaryMapper,
            SalaryDetailBuilder salaryDetailBuilder,
            PersonCodeParser personCodeParser
    ) {
        this.legacySalaryMapper = legacySalaryMapper;
        this.salaryDetailBuilder = salaryDetailBuilder;
        this.personCodeParser = personCodeParser;
    }

    @Override
    public SalaryCalculationResult calculate(SalaryCalculationCommand command) {
        PersonCodeParts parts = personCodeParser.parse(command.personCode(), command.orgCode());
        int yearMonth = command.year() * 100 + command.month();
        SalaryHistoryItem baseline = legacySalaryMapper.findBaselineAtOrBefore(parts.orgCode(), parts.personNo(), yearMonth)
                .orElseThrow(() -> new BusinessException(
                        "SALARY_BASELINE_NOT_FOUND",
                        "No salary history found before " + command.year() + "-" + command.month() + " for " + command.personCode()
                ));
        return new SalaryCalculationResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline.totalAmount(),
                salaryDetailBuilder.build(baseline.id())
        );
    }
}
