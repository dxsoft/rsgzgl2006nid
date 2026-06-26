package com.dx.rsgzgl.salary.service.impl;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.person.dto.PersonSummary;
import com.dx.rsgzgl.person.mapper.LegacyPersonMapper;
import com.dx.rsgzgl.salary.dto.NormalGradeBatchTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeBatchTrialItem;
import com.dx.rsgzgl.salary.dto.NormalGradeBatchTrialResult;
import com.dx.rsgzgl.salary.dto.NormalGradeTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeTrialResult;
import com.dx.rsgzgl.salary.dto.SalaryRuleChange;
import com.dx.rsgzgl.salary.service.NormalGradeBatchTrialService;
import com.dx.rsgzgl.salary.service.NormalGradeTrialService;
import com.dx.rsgzgl.system.service.OrganizationAccessService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultNormalGradeBatchTrialService implements NormalGradeBatchTrialService {

    private static final String STATUS_MATCHED = "MATCHED";
    private static final String STATUS_DIFFERENT = "DIFFERENT";
    private static final String STATUS_NO_EXPECTED = "NO_EXPECTED";
    private static final String STATUS_SKIPPED = "SKIPPED";
    private static final String RULE_GRADE_INCREMENT = "GRADE_INCREMENT";
    private static final String RULE_LEVEL_PROMOTION = "LEVEL_PROMOTION";
    private static final String RULE_CIVIL_POST_CHANGE = "CIVIL_POST_CHANGE";
    private static final String RULE_JUDICIAL_POST_CHANGE = "JUDICIAL_POST_CHANGE";
    private static final String RULE_NOT_ELIGIBLE = "NOT_ELIGIBLE";
    private static final String RULE_INSTITUTION_POST_CHANGE = "INSTITUTION_POST_CHANGE";
    private static final String RULE_REGULARIZATION_GRADE_PLACEMENT = "REGULARIZATION_GRADE_PLACEMENT";
    private static final String RULE_PROBATIONARY_NEW_SALARY = "PROBATIONARY_NEW_SALARY";
    private static final String RULE_POLICE_RANK_ALLOWANCE_CHANGE = "POLICE_RANK_ALLOWANCE_CHANGE";
    private static final String RULE_CIVIL_RANK_PROMOTION = "CIVIL_RANK_PROMOTION";
    private static final String RULE_EDUCATION_CHANGE = "EDUCATION_CHANGE";
    private static final String RULE_TEACHER_NURSE_ALLOWANCE_CHANGE = "TEACHER_NURSE_ALLOWANCE_CHANGE";
    private static final String RULE_JUDICIAL_ALLOWANCE_CHANGE = "JUDICIAL_ALLOWANCE_CHANGE";
    private static final String RULE_JUDICIAL_CONVERSION = "JUDICIAL_CONVERSION";
    private static final String RULE_LEGACY_2006_CONVERSION = "LEGACY_2006_CONVERSION";
    private static final String RULE_STANDARD_ADJUSTMENT = "STANDARD_ADJUSTMENT";
    private static final String RULE_SALARY_GRADE_INCREMENT = "SALARY_GRADE_INCREMENT";
    private static final String RULE_WORKER_TECHNICAL_GRADE_PROMOTION = "WORKER_TECHNICAL_GRADE_PROMOTION";
    private static final String RULE_WORKER_POST_GRADE_INCREMENT = "WORKER_POST_GRADE_INCREMENT";

    private final LegacyPersonMapper legacyPersonMapper;
    private final NormalGradeTrialService normalGradeTrialService;
    private final OrganizationAccessService organizationAccessService;

    public DefaultNormalGradeBatchTrialService(
            LegacyPersonMapper legacyPersonMapper,
            NormalGradeTrialService normalGradeTrialService,
            OrganizationAccessService organizationAccessService
    ) {
        this.legacyPersonMapper = legacyPersonMapper;
        this.normalGradeTrialService = normalGradeTrialService;
        this.organizationAccessService = organizationAccessService;
    }

    @Override
    public NormalGradeBatchTrialResult trial(NormalGradeBatchTrialCommand command) {
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
        List<NormalGradeBatchTrialItem> items = new ArrayList<>();
        BigDecimal totalDifference = BigDecimal.ZERO;
        int matched = 0;
        int different = 0;
        int noExpected = 0;
        int skipped = 0;
        int reverseStep = 0;
        int levelPromotion = 0;
        int notEligible = 0;

        for (PersonSummary person : people) {
            NormalGradeBatchTrialItem item = trialPerson(command, person);
            items.add(item);
            totalDifference = totalDifference.add(item.differenceWithExpected());
            if (STATUS_MATCHED.equals(item.status())) {
                matched++;
            } else if (STATUS_DIFFERENT.equals(item.status())) {
                different++;
            } else if (STATUS_NO_EXPECTED.equals(item.status())) {
                noExpected++;
            } else {
                skipped++;
            }
            if (item.afterValue() != null && item.afterValue().contains("\u5012")) {
                reverseStep++;
            }
            if (RULE_LEVEL_PROMOTION.equals(item.ruleType())) {
                levelPromotion++;
            }
            if (RULE_NOT_ELIGIBLE.equals(item.ruleType())) {
                notEligible++;
            }
        }

        return new NormalGradeBatchTrialResult(
                orgCode,
                command.year(),
                command.month(),
                items.size(),
                matched,
                different,
                noExpected,
                skipped,
                reverseStep,
                levelPromotion,
                notEligible,
                totalDifference,
                items
        );
    }

    private NormalGradeBatchTrialItem trialPerson(NormalGradeBatchTrialCommand command, PersonSummary person) {
        try {
            NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                    person.personCode(),
                    person.orgCode(),
                    command.year(),
                    command.month(),
                    command.changeType()
            ));
            SalaryRuleChange change = representativeChange(result.changes());
            String status = status(result);
            return new NormalGradeBatchTrialItem(
                    person.personCode(),
                    person.personName(),
                    person.orgCode(),
                    person.orgName(),
                    result.baselineHistoryId(),
                    result.expectedHistoryId(),
                    result.baselineTotalAmount(),
                    result.calculatedTotalAmount(),
                    result.expectedTotalAmount(),
                    result.differenceWithExpected(),
                    result.matchedExpected(),
                    change == null ? null : change.beforeValue(),
                    change == null ? null : change.afterValue(),
                    change == null ? BigDecimal.ZERO : change.beforeAmount(),
                    change == null ? BigDecimal.ZERO : change.afterAmount(),
                    change == null ? BigDecimal.ZERO : change.difference(),
                    ruleType(change),
                    change == null ? null : change.ruleNote(),
                    status,
                    message(status)
            );
        } catch (BusinessException ex) {
            return skipped(person, skippedRuleType(ex), ex.getCode() + ": " + ex.getMessage());
        } catch (RuntimeException ex) {
            return skipped(person, null, ex.getMessage());
        }
    }

    private String ruleType(SalaryRuleChange change) {
        if (change == null || change.ruleNote() == null) {
            return null;
        }
        if (change.ruleNote().contains("\u7ea7\u522b\u664b\u5347")) {
            return RULE_LEVEL_PROMOTION;
        }
        if (change.ruleNote().contains("\u516c\u52a1\u5458/\u53c2\u516c\u804c\u52a1\u53d8\u52a8")) {
            return RULE_CIVIL_POST_CHANGE;
        }
        if (change.ruleNote().contains("\u6cd5\u5b98/\u68c0\u5bdf\u5b98\u804c\u52a1\u7b49\u7ea7\u53d8\u52a8")) {
            return RULE_JUDICIAL_POST_CHANGE;
        }
        if (change.ruleNote().contains("\u4e8b\u4e1a\u4eba\u5458\u664b\u5347\u85aa\u7ea7")) {
            return RULE_SALARY_GRADE_INCREMENT;
        }
        if (change.ruleNote().contains("\u4e8b\u4e1a\u5355\u4f4d\u5c97\u4f4d\u53d8\u52a8")) {
            return RULE_INSTITUTION_POST_CHANGE;
        }
        if (change.ruleNote().contains("\u5165\u53e3\u5b9a\u8d44")) {
            return RULE_REGULARIZATION_GRADE_PLACEMENT;
        }
        if (change.ruleNote().contains("\u65b0\u8fdb\u5de5\u8d44")) {
            return RULE_PROBATIONARY_NEW_SALARY;
        }
        if (change.ruleNote().contains("\u8b66\u8854\u53d8\u5316") || change.ruleNote().contains("\u8b66\u8854\u6d25\u8d34")) {
            return RULE_POLICE_RANK_ALLOWANCE_CHANGE;
        }
        if (change.ruleNote().contains("\u804c\u7ea7\u664b\u5347")
                || change.ruleNote().contains("\u804c\u7ea7\u5957\u6539")
                || change.ruleNote().contains("\u8b66\u5458\u5957\u6539")) {
            return RULE_CIVIL_RANK_PROMOTION;
        }
        if (change.ruleNote().contains("\u5b66\u5386\u53d8\u5316")) {
            return RULE_EDUCATION_CHANGE;
        }
        if (change.ruleNote().contains("\u6559\u62a4\u6d25\u8d34")) {
            return RULE_TEACHER_NURSE_ALLOWANCE_CHANGE;
        }
        if (change.ruleNote().contains("\u6cd5\u68c0\u5957\u6539")) {
            return RULE_JUDICIAL_CONVERSION;
        }
        if (change.ruleNote().contains("\u6cd5\u5b98\u7b49\u7ea7")
                || change.ruleNote().contains("\u68c0\u5bdf\u7b49\u7ea7")
                || change.ruleNote().contains("\u5ba1\u5224\u6d25\u8d34")
                || change.ruleNote().contains("\u68c0\u5bdf\u6d25\u8d34")) {
            return RULE_JUDICIAL_ALLOWANCE_CHANGE;
        }
        if (change.ruleNote().contains("2006\u5957\u6539")) {
            return RULE_LEGACY_2006_CONVERSION;
        }
        if (change.ruleNote().contains("\u8c03\u6807\u664b\u5347")
                || change.ruleNote().contains("\u8c03\u6574\u6807\u51c6")
                || change.ruleNote().contains("\u6d25\u8d34\u53d8\u5316")) {
            return RULE_STANDARD_ADJUSTMENT;
        }
        if (change.ruleNote().contains("\u673a\u5173\u5de5\u4eba\u5c97\u4f4d\u5de5\u8d44\u664b\u6863")) {
            return RULE_WORKER_POST_GRADE_INCREMENT;
        }
        if (change.ruleNote().contains("\u673a\u5173\u6280\u672f\u5de5\u4eba\u664b\u5347\u6280\u672f\u7b49\u7ea7")) {
            return RULE_WORKER_TECHNICAL_GRADE_PROMOTION;
        }
        if (change.ruleNote().contains("\u664b\u6863")) {
            return RULE_GRADE_INCREMENT;
        }
        return null;
    }

    private SalaryRuleChange representativeChange(List<SalaryRuleChange> changes) {
        if (changes.isEmpty()) {
            return null;
        }
        for (SalaryRuleChange change : changes) {
            if ("ZZDJ06".equals(change.itemCode())) {
                return change;
            }
        }
        for (SalaryRuleChange change : changes) {
            if (change.itemCode() != null && change.itemCode().startsWith("TG2006_")) {
                return change;
            }
        }
        return changes.get(0);
    }

    private String skippedRuleType(BusinessException ex) {
        return "PROMOTION_NOT_ELIGIBLE".equals(ex.getCode()) ? RULE_NOT_ELIGIBLE : null;
    }

    private String status(NormalGradeTrialResult result) {
        if (result.expectedHistoryId() == null) {
            return STATUS_NO_EXPECTED;
        }
        return result.matchedExpected() ? STATUS_MATCHED : STATUS_DIFFERENT;
    }

    private String message(String status) {
        return switch (status) {
            case STATUS_MATCHED -> "OK";
            case STATUS_DIFFERENT -> "Difference found";
            case STATUS_NO_EXPECTED -> "No target-month legacy record";
            default -> "";
        };
    }

    private NormalGradeBatchTrialItem skipped(PersonSummary person, String ruleType, String message) {
        return new NormalGradeBatchTrialItem(
                person.personCode(),
                person.personName(),
                person.orgCode(),
                person.orgName(),
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                ruleType,
                null,
                STATUS_SKIPPED,
                message
        );
    }
}
