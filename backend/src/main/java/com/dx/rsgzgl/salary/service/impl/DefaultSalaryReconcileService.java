package com.dx.rsgzgl.salary.service.impl;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.salary.dto.SalaryCalculationDetail;
import com.dx.rsgzgl.salary.dto.SalaryCalculationResult;
import com.dx.rsgzgl.salary.dto.SalaryHistoryItem;
import com.dx.rsgzgl.salary.dto.SalaryReconcileCommand;
import com.dx.rsgzgl.salary.dto.SalaryReconcileDetail;
import com.dx.rsgzgl.salary.dto.SalaryReconcileResult;
import com.dx.rsgzgl.salary.mapper.LegacySalaryMapper;
import com.dx.rsgzgl.salary.service.SalaryCalculationService;
import com.dx.rsgzgl.salary.service.SalaryReconcileService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DefaultSalaryReconcileService implements SalaryReconcileService {

    private final LegacySalaryMapper legacySalaryMapper;
    private final SalaryCalculationService salaryCalculationService;
    private final SalaryDetailBuilder salaryDetailBuilder;
    private final PersonCodeParser personCodeParser;

    public DefaultSalaryReconcileService(
            LegacySalaryMapper legacySalaryMapper,
            SalaryCalculationService salaryCalculationService,
            SalaryDetailBuilder salaryDetailBuilder,
            PersonCodeParser personCodeParser
    ) {
        this.legacySalaryMapper = legacySalaryMapper;
        this.salaryCalculationService = salaryCalculationService;
        this.salaryDetailBuilder = salaryDetailBuilder;
        this.personCodeParser = personCodeParser;
    }

    @Override
    public SalaryReconcileResult reconcile(SalaryReconcileCommand command) {
        PersonCodeParts parts = personCodeParser.parse(command.personCode(), command.orgCode());
        SalaryHistoryItem legacyRecord = legacySalaryMapper
                .findRecordAtYearMonth(parts.orgCode(), parts.personNo(), command.year(), command.month())
                .orElseThrow(() -> new BusinessException(
                        "SALARY_HISTORY_NOT_FOUND",
                        "No legacy salary history found at " + command.year() + "-" + command.month() + " for " + command.personCode()
                ));

        SalaryCalculationResult calculated = salaryCalculationService.calculate(command.toCalculationCommand());
        List<SalaryCalculationDetail> legacyDetails = salaryDetailBuilder.build(legacyRecord.id());
        List<SalaryReconcileDetail> detailDiffs = compareDetails(legacyDetails, calculated.details());

        BigDecimal legacyTotal = normalize(legacyRecord.totalAmount());
        BigDecimal calculatedTotal = normalize(calculated.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(legacyTotal);
        boolean passed = difference.compareTo(BigDecimal.ZERO) == 0
                && detailDiffs.stream().allMatch(SalaryReconcileDetail::passed);

        return new SalaryReconcileResult(
                command.personCode(),
                command.year(),
                command.month(),
                legacyRecord.id(),
                legacyTotal,
                calculatedTotal,
                difference,
                passed,
                detailDiffs
        );
    }

    private List<SalaryReconcileDetail> compareDetails(
            List<SalaryCalculationDetail> legacyDetails,
            List<SalaryCalculationDetail> calculatedDetails
    ) {
        Map<String, SalaryCalculationDetail> legacyByCode = byCode(legacyDetails);
        Map<String, SalaryCalculationDetail> calculatedByCode = byCode(calculatedDetails);
        Set<String> codes = new LinkedHashSet<>();
        codes.addAll(legacyByCode.keySet());
        codes.addAll(calculatedByCode.keySet());

        return codes.stream().map(code -> {
            SalaryCalculationDetail legacy = legacyByCode.get(code);
            SalaryCalculationDetail calculated = calculatedByCode.get(code);
            BigDecimal legacyAmount = normalize(legacy == null ? null : legacy.amount());
            BigDecimal calculatedAmount = normalize(calculated == null ? null : calculated.amount());
            BigDecimal difference = calculatedAmount.subtract(legacyAmount);
            String itemName = calculated != null ? calculated.itemName() : legacy.itemName();
            return new SalaryReconcileDetail(
                    code,
                    itemName,
                    legacyAmount,
                    calculatedAmount,
                    difference,
                    difference.compareTo(BigDecimal.ZERO) == 0
            );
        }).toList();
    }

    private Map<String, SalaryCalculationDetail> byCode(List<SalaryCalculationDetail> details) {
        Map<String, SalaryCalculationDetail> result = new LinkedHashMap<>();
        for (SalaryCalculationDetail detail : details) {
            result.put(detail.itemCode(), detail);
        }
        return result;
    }

    private BigDecimal normalize(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.stripTrailingZeros();
    }
}
