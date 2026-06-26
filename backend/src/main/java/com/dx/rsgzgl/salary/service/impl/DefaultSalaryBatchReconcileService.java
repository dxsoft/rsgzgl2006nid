package com.dx.rsgzgl.salary.service.impl;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.person.dto.PersonSummary;
import com.dx.rsgzgl.person.mapper.LegacyPersonMapper;
import com.dx.rsgzgl.salary.dto.SalaryBatchReconcileCommand;
import com.dx.rsgzgl.salary.dto.SalaryBatchReconcileItem;
import com.dx.rsgzgl.salary.dto.SalaryBatchReconcileResult;
import com.dx.rsgzgl.salary.dto.SalaryReconcileCommand;
import com.dx.rsgzgl.salary.dto.SalaryReconcileResult;
import com.dx.rsgzgl.salary.service.SalaryBatchReconcileService;
import com.dx.rsgzgl.salary.service.SalaryReconcileService;
import com.dx.rsgzgl.system.service.OrganizationAccessService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultSalaryBatchReconcileService implements SalaryBatchReconcileService {

    private final LegacyPersonMapper legacyPersonMapper;
    private final SalaryReconcileService salaryReconcileService;
    private final OrganizationAccessService organizationAccessService;

    public DefaultSalaryBatchReconcileService(
            LegacyPersonMapper legacyPersonMapper,
            SalaryReconcileService salaryReconcileService,
            OrganizationAccessService organizationAccessService
    ) {
        this.legacyPersonMapper = legacyPersonMapper;
        this.salaryReconcileService = salaryReconcileService;
        this.organizationAccessService = organizationAccessService;
    }

    @Override
    public SalaryBatchReconcileResult reconcile(SalaryBatchReconcileCommand command) {
        String orgCode = command.orgCode().trim();
        organizationAccessService.requireOrgAccess(orgCode);
        List<PersonSummary> people = legacyPersonMapper.findPage(
                null,
                orgCode,
                organizationAccessService.hasFullAccess(),
                organizationAccessService.allowedOrgCodes(),
                0,
                command.safeLimit()
        );
        List<SalaryBatchReconcileItem> items = new ArrayList<>();
        BigDecimal totalDifference = BigDecimal.ZERO;
        int passed = 0;
        int failed = 0;
        int skipped = 0;

        for (PersonSummary person : people) {
            SalaryBatchReconcileItem item = reconcilePerson(command, person);
            items.add(item);
            totalDifference = totalDifference.add(item.difference());
            if ("PASSED".equals(item.status())) {
                passed++;
            } else if ("FAILED".equals(item.status())) {
                failed++;
            } else {
                skipped++;
            }
        }

        return new SalaryBatchReconcileResult(
                command.orgCode().trim(),
                command.year(),
                command.month(),
                items.size(),
                passed,
                failed,
                skipped,
                totalDifference,
                items
        );
    }

    private SalaryBatchReconcileItem reconcilePerson(SalaryBatchReconcileCommand command, PersonSummary person) {
        try {
            SalaryReconcileResult result = salaryReconcileService.reconcile(new SalaryReconcileCommand(
                    person.personCode(),
                    person.orgCode(),
                    command.year(),
                    command.month(),
                    command.changeType()
            ));
            String status = result.passed() ? "PASSED" : "FAILED";
            return new SalaryBatchReconcileItem(
                    person.personCode(),
                    person.personName(),
                    person.orgCode(),
                    person.orgName(),
                    result.legacyTotalAmount(),
                    result.calculatedTotalAmount(),
                    result.difference(),
                    result.passed(),
                    status,
                    result.passed() ? "OK" : "Difference found"
            );
        } catch (BusinessException ex) {
            return skipped(person, ex.getCode() + ": " + ex.getMessage());
        } catch (RuntimeException ex) {
            return skipped(person, ex.getMessage());
        }
    }

    private SalaryBatchReconcileItem skipped(PersonSummary person, String message) {
        return new SalaryBatchReconcileItem(
                person.personCode(),
                person.personName(),
                person.orgCode(),
                person.orgName(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                "SKIPPED",
                message
        );
    }
}
