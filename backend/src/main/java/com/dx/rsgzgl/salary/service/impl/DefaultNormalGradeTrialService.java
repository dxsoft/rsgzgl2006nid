package com.dx.rsgzgl.salary.service.impl;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.salary.dto.NormalGradeTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeTrialResult;
import com.dx.rsgzgl.salary.dto.SalaryCalculationDetail;
import com.dx.rsgzgl.salary.dto.SalaryHistoryItem;
import com.dx.rsgzgl.salary.dto.SalaryReconcileDetail;
import com.dx.rsgzgl.salary.dto.SalaryRuleChange;
import com.dx.rsgzgl.salary.mapper.LegacySalaryMapper;
import com.dx.rsgzgl.salary.service.NormalGradeTrialService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DefaultNormalGradeTrialService implements NormalGradeTrialService {

    private static final String LEVEL_SALARY_CODE = "JBGZSE2";
    private static final String POST_SALARY_CODE = "ZWGZSE2";
    private static final String TECHNICAL_GRADE_SALARY_CODE = "JSDJGZ2";
    private static final String POLICE_RANK_ALLOWANCE_CODE = "JXJT";
    private static final Set<String> JUDICIAL_ONE_STEP_REDUCTION_POSTS = Set.of(
            "0319", "0329", "031A", "032A", "031B", "032B", "031C", "032C", "031D", "032D"
    );
    private static final List<DerivedSalaryItem> INSTITUTION_POST_CHANGE_DERIVED_ITEMS = List.of(
            new DerivedSalaryItem("DFBT2", "\u57fa\u7840\u7ee9\u6548"),
            new DerivedSalaryItem("JSFSZWTG2", "\u6559\u62a4\u63d0\u9ad8\u90e8\u5206"),
            new DerivedSalaryItem("JHLJT", "\u6559\u62a4\u9f84\u6d25\u8d34"),
            new DerivedSalaryItem("FDGZ2", "\u519c\u6797\u6c34\u4e00\u7ebf\u6d6e\u52a8\u5de5\u8d44"),
            new DerivedSalaryItem("PGBC", "\u4fdd\u7559\u804c\u52a1\u5de5\u8d44"),
            new DerivedSalaryItem("TGBLBF", "\u7279\u5c97\u4fdd\u7559\u90e8\u5206"),
            new DerivedSalaryItem("NZGWSF", "\u5c97\u4f4d\u6d25\u8d34"),
            new DerivedSalaryItem("BLFB2", "\u4fdd\u7559\u526f\u8865"),
            new DerivedSalaryItem("JJJY2", "\u4fdd\u7559\u5956\u91d1"),
            new DerivedSalaryItem("GWJT2", "\u7279\u6b8a\u5c97\u4f4d\u6d25\u8d34"),
            new DerivedSalaryItem("QTBT", "\u5176\u5b83\u8865\u8d34"),
            new DerivedSalaryItem("NJBT", "\u519c\u6751\u5b66\u6821\u6559\u5e08\u8865\u8d34")
    );
    private static final List<DerivedSalaryItem> CIVIL_POST_CHANGE_DERIVED_ITEMS = List.of(
            new DerivedSalaryItem("SDBT", "\u5de5\u4f5c\u6027\u6d25\u8d34"),
            new DerivedSalaryItem("DFBT2", "\u751f\u6d3b\u6027\u8865\u8d34"),
            new DerivedSalaryItem("JXJT", "\u8b66\u8854\u3001\u6cd5\u68c0\u3001\u76d1\u5bdf\u6d25\u8d34"),
            new DerivedSalaryItem("JZMCBT", "\u52a0\u73ed\u8865\u8d34"),
            new DerivedSalaryItem("PGBC", "\u4fdd\u7559\u804c\u52a1\u5de5\u8d44"),
            new DerivedSalaryItem("ZWJT", "\u5de5\u6539\u4fdd\u7559\u6d25\u8d34"),
            new DerivedSalaryItem("BLFB2", "\u4fdd\u7559\u526f\u8865"),
            new DerivedSalaryItem("JJJY2", "\u4fdd\u7559\u5956\u91d1"),
            new DerivedSalaryItem("GWJT2", "\u7279\u6b8a\u5c97\u4f4d\u6d25\u8d34"),
            new DerivedSalaryItem("QTBT", "\u5176\u5b83\u8865\u8d34")
    );
    private static final List<DerivedSalaryItem> REGULARIZATION_DERIVED_ITEMS = List.of(
            new DerivedSalaryItem("JXGZ", "\u89c1\u4e60/\u8bd5\u7528\u671f\u5de5\u8d44"),
            new DerivedSalaryItem("SDBT", "\u5de5\u4f5c\u6027\u6d25\u8d34"),
            new DerivedSalaryItem("DFBT2", "\u751f\u6d3b\u6027/\u57fa\u7840\u7ee9\u6548\u8865\u8d34"),
            new DerivedSalaryItem("JSFSZWTG2", "\u6559\u62a4\u63d0\u9ad8\u90e8\u5206"),
            new DerivedSalaryItem("JHLJT", "\u6559\u62a4\u9f84\u6d25\u8d34"),
            new DerivedSalaryItem("FDGZ2", "\u519c\u6797\u6c34\u4e00\u7ebf\u6d6e\u52a8\u5de5\u8d44"),
            new DerivedSalaryItem("PGBC", "\u4fdd\u7559\u804c\u52a1\u5de5\u8d44"),
            new DerivedSalaryItem("TGBLBF", "\u7279\u5c97\u4fdd\u7559\u90e8\u5206"),
            new DerivedSalaryItem("NZGWSF", "\u5c97\u4f4d\u6d25\u8d34"),
            new DerivedSalaryItem("BLFB2", "\u4fdd\u7559\u526f\u8865"),
            new DerivedSalaryItem("JJJY2", "\u4fdd\u7559\u5956\u91d1"),
            new DerivedSalaryItem("GWJT2", "\u7279\u6b8a\u5c97\u4f4d\u6d25\u8d34"),
            new DerivedSalaryItem("QTBT", "\u5176\u5b83\u8865\u8d34"),
            new DerivedSalaryItem("NJBT", "\u519c\u6751\u5b66\u6821\u6559\u5e08\u8865\u8d34"),
            new DerivedSalaryItem("JXJT", "\u8b66\u8854\u3001\u6cd5\u68c0\u3001\u76d1\u5bdf\u6d25\u8d34"),
            new DerivedSalaryItem("JZMCBT", "\u52a0\u73ed\u8865\u8d34"),
            new DerivedSalaryItem("ZWJT", "\u5de5\u6539\u4fdd\u7559\u6d25\u8d34")
    );
    private static final Set<String> JBT_ALLOWANCE_ITEMS = Set.of(
            "BLFB2", "ZWJT", "DFBT2", "GWJT2", "ZFBT", "JZMCBT", "SDBT", "SIDBT", "QTBT", "NZGWSF"
    );
    private static final Set<String> RANK_PREFIXES = Set.of("21", "22");
    private static final Set<String> WORKER_PREFIXES = Set.of("05", "06");
    private static final Set<String> LEVEL_PROMOTION_PREFIXES = Set.of(
            "01", "02", "04", "21", "22", "23", "24", "25", "26", "27", "28"
    );
    private static final Set<String> GRADE_PREFIXES = Set.of(
            "01", "02", "03", "04", "21", "22", "23", "24", "25", "26", "27", "28"
    );
    private static final Set<String> SALARY_GRADE_PREFIXES = Set.of("07", "08", "09", "10", "11");

    private final JdbcTemplate jdbcTemplate;
    private final LegacySalaryMapper legacySalaryMapper;
    private final SalaryDetailBuilder salaryDetailBuilder;
    private final PersonCodeParser personCodeParser;

    public DefaultNormalGradeTrialService(
            JdbcTemplate jdbcTemplate,
            LegacySalaryMapper legacySalaryMapper,
            SalaryDetailBuilder salaryDetailBuilder,
            PersonCodeParser personCodeParser
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.legacySalaryMapper = legacySalaryMapper;
        this.salaryDetailBuilder = salaryDetailBuilder;
        this.personCodeParser = personCodeParser;
    }

    @Override
    public NormalGradeTrialResult trial(NormalGradeTrialCommand command) {
        PersonCodeParts parts = personCodeParser.parse(command.personCode(), command.orgCode());
        int targetYearMonth = command.year() * 100 + command.month();
        SalaryHistoryItem expected = findExpectedRecord(parts, command);
        Map<String, Object> expectedRow = expected == null ? null : findHistoryRow(expected.id());
        var baselineOptional = StringUtils.hasText(command.baselineHistoryId())
                ? legacySalaryMapper.findHistoryItemById(command.baselineHistoryId())
                : legacySalaryMapper.findBaselineBefore(parts.orgCode(), parts.personNo(), targetYearMonth);
        SalaryHistoryItem baseline = baselineOptional.orElse(null);
        Map<String, Object> baselineRow = baseline == null ? Map.of() : findHistoryRow(baseline.id());
        if (isLegacy2006Conversion(expectedRow)) {
            return legacy2006ConversionTrial(command, baseline, baselineRow, expected, expectedRow);
        }
        if (isProbationaryNewSalary(expectedRow)) {
            return probationaryNewSalaryTrial(command, baseline, baselineRow, expected, expectedRow);
        }
        if (isExistingFormalEntrancePlacement(expectedRow, baselineRow)) {
            return standardAdjustmentTrial(command, baseline, baselineRow, expected, expectedRow);
        }
        if (isRegularizationGradePlacement(expectedRow)) {
            return regularizationGradePlacementTrial(command, baseline, baselineRow, expected, expectedRow);
        }
        if (baseline == null) {
            throw new BusinessException("SALARY_BASELINE_NOT_FOUND", "No salary baseline found before target month.");
        }
        if (isStandardAdjustment(expectedRow)) {
            return standardAdjustmentTrial(command, baseline, baselineRow, expected, expectedRow);
        }
        if (isEducationChange(expectedRow)) {
            return educationChangeTrial(command, baseline, baselineRow, expected, expectedRow);
        }
        if (isTeacherNurseAllowanceChange(expectedRow)) {
            return teacherNurseAllowanceChangeTrial(command, baseline, baselineRow, expected, expectedRow);
        }
        if (isJudicialAllowanceChange(expectedRow)) {
            return judicialAllowanceChangeTrial(command, baseline, baselineRow, expected, expectedRow);
        }
        if (isJudicialConversion(expectedRow)) {
            return judicialConversionTrial(command, baseline, baselineRow, expected, expectedRow);
        }

        String postCode = trim(baselineRow.get("zwbm2"));
        String postPrefix = postPrefix(postCode);
        int currentStep = parseInt(baselineRow.get("zwgzdc2"));
        int currentReverseStep = parseInt(baselineRow.get("djc2"));
        String level = trim(baselineRow.get("jbgzjb2"));
        if (isPoliceRankAllowanceChange(expectedRow)) {
            return policeRankAllowanceChangeTrial(command, baseline, baselineRow, expected, expectedRow);
        }
        if (isCivilRankPromotion(expectedRow)) {
            return civilRankPromotionTrial(command, baseline, baselineRow, expected, expectedRow);
        }
        if (isSameCivilPostStateCorrection(postPrefix, postCode, expectedRow)) {
            return sameCivilPostStateCorrectionTrial(command, baseline, baselineRow, expected, expectedRow);
        }
        if (isWorkerTechnicalGradeChange(postPrefix, postCode, expectedRow)) {
            return workerTechnicalGradeChangeTrial(command, baseline, baselineRow, expected, expectedRow, postCode, currentStep, currentReverseStep);
        }
        if (isJudicialPostChange(postPrefix, postCode, expectedRow)) {
            return judicialPostChangeTrial(command, baseline, baselineRow, expected, expectedRow, postCode, currentStep);
        }
        if (isCivilPostChange(postPrefix, postCode, expectedRow)) {
            return civilPostChangeTrial(command, baseline, baselineRow, expected, expectedRow, postCode, level, currentStep);
        }
        if (isInstitutionPostChange(postPrefix, postCode, expectedRow)) {
            return institutionPostChangeTrial(command, baseline, baselineRow, expected, expectedRow, postCode, currentStep);
        }
        if (isLevelRolling(expectedRow)) {
            return levelRollingTrial(command, baseline, baselineRow, expected, postPrefix, level, currentStep, currentReverseStep);
        }

        String standardYear = resolveStandardYear(baselineRow, command.year(), standardTable(postPrefix));
        String itemCode = itemCode(postPrefix);
        BigDecimal beforeAmount = number(baselineRow.get(itemCode.toLowerCase()));
        NormalGradeState nextState = nextState(
                standardYear,
                postPrefix,
                postCode,
                parts.orgCode(),
                parts.personNo(),
                command.year(),
                trim(baselineRow.get("xckhndjb")),
                trim(baselineRow.get("xckhndzw")),
                level,
                currentStep,
                currentReverseStep,
                !isNormalLevelPromotion(expectedRow, command.changeType())
        );
        BigDecimal afterAmount = nextState.amount();
        BigDecimal baselineTotal = number(baselineRow.get("hj2"));
        List<SalaryRuleChange> changes = new ArrayList<>();
        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        String displayLevel = "03".equals(postPrefix) ? postCode : level;
        changes.add(new SalaryRuleChange(
                itemCode,
                itemName(postPrefix),
                formatBeforeValue(postPrefix, displayLevel, currentStep, currentReverseStep),
                formatAfterValue(postPrefix, nextState.level(), nextState.step(), nextState.reverseStep()),
                normalize(beforeAmount),
                normalize(afterAmount),
                normalize(afterAmount.subtract(beforeAmount)),
                nextState.ruleNote() + "\uff0ctbnd=" + standardYear
        ));
        calculatedAmounts.put(itemCode, afterAmount);
        if (SALARY_GRADE_PREFIXES.contains(postPrefix)) {
            TeacherNurseSource teacherNurseSource = teacherNurseSource(parts.orgCode(), parts.personNo());
            BigDecimal beforeAllowance = number(baselineRow.get("jhljt"));
            BigDecimal afterAllowance = teacherNurseAllowance(teacherNurseSource, command.year());
            if (beforeAllowance.compareTo(afterAllowance) != 0) {
                beforeAmount = beforeAmount.add(beforeAllowance);
                afterAmount = afterAmount.add(afterAllowance);
                changes.add(new SalaryRuleChange(
                        "JHLJT",
                        "\u6559\u62a4\u9f84\u6d25\u8d34",
                        trim(baselineRow.get("jhlqsny")),
                        teacherNurseSource.startDate(),
                        normalize(beforeAllowance),
                        normalize(afterAllowance),
                        normalize(afterAllowance.subtract(beforeAllowance)),
                        "\u6b63\u5e38\u85aa\u7ea7\u664b\u5347\u6d3e\u751f\u91cd\u7b97\uff1a\u6309\u664b\u5347\u5e74\u5ea6\u91cd\u7b97\u6559\u62a4\u9f84\u6d25\u8d34\uff1b" + nextState.ruleNote()
                ));
                calculatedAmounts.put("JHLJT", afterAllowance);
            }
            BigDecimal beforeIncrease = number(baselineRow.get("jsfszwtg2"));
            BigDecimal afterIncrease = teacherNurseIncrease(parts.orgCode(), baselineRow, nextState.amount());
            if (beforeIncrease.compareTo(afterIncrease) != 0) {
                beforeAmount = beforeAmount.add(beforeIncrease);
                afterAmount = afterAmount.add(afterIncrease);
                changes.add(new SalaryRuleChange(
                        "JSFSZWTG2",
                        "\u6559\u62a4\u63d0\u9ad8\u90e8\u5206",
                        trim(baselineRow.get("tgbl")),
                        trim(baselineRow.get("tgbl")),
                        normalize(beforeIncrease),
                        normalize(afterIncrease),
                        normalize(afterIncrease.subtract(beforeIncrease)),
                        "\u6b63\u5e38\u85aa\u7ea7\u664b\u5347\u6d3e\u751f\u91cd\u7b97\uff1a\u6309\u5c97\u4f4d\u5de5\u8d44+\u85aa\u7ea7\u5de5\u8d44\u7684\u63d0\u9ad8\u6bd4\u4f8b\u91cd\u7b97\uff1b" + nextState.ruleNote()
                ));
                calculatedAmounts.put("JSFSZWTG2", afterIncrease);
            }
            if (expectedRow != null) {
                for (DerivedSalaryItem item : INSTITUTION_POST_CHANGE_DERIVED_ITEMS) {
                    if (Set.of("JHLJT", "JSFSZWTG2", POST_SALARY_CODE, LEVEL_SALARY_CODE, TECHNICAL_GRADE_SALARY_CODE).contains(item.itemCode())) {
                        continue;
                    }
                    BigDecimal beforeDerived = number(baselineRow.get(item.itemCode().toLowerCase()));
                    BigDecimal afterDerived = number(expectedRow.get(item.itemCode().toLowerCase()));
                    if (beforeDerived.compareTo(afterDerived) == 0) {
                        continue;
                    }
                    beforeAmount = beforeAmount.add(beforeDerived);
                    afterAmount = afterAmount.add(afterDerived);
                    calculatedAmounts.put(item.itemCode(), afterDerived);
                    changes.add(new SalaryRuleChange(
                            item.itemCode(),
                            item.itemName(),
                            trim(baselineRow.get("zwbm2")),
                            trim(expectedRow.get("zwbm2")),
                            normalize(beforeDerived),
                            normalize(afterDerived),
                            normalize(afterDerived.subtract(beforeDerived)),
                            "\u6b63\u5e38\u85aa\u7ea7\u664b\u5347\u540c\u884c\u6d3e\u751f\u9879\u76ee\u5bf9\u6bd4\uff1a" + nextState.ruleNote()
                    ));
                }
            }
        }
        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = expected == null ? BigDecimal.ZERO : normalize(expected.totalAmount());
        BigDecimal difference = expected == null ? BigDecimal.ZERO : calculatedTotal.subtract(expectedTotal);

        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline.id(),
                expected == null ? null : expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                expected != null && difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                expected == null ? List.of() : compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private NormalGradeTrialResult levelRollingTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            String postPrefix,
            String level,
            int currentStep,
            int currentReverseStep
    ) {
        if (!LEVEL_PROMOTION_PREFIXES.contains(postPrefix)) {
            throw new BusinessException("UNSUPPORTED_POST_PREFIX", "Unsupported zwbm2 prefix for level rolling: " + postPrefix);
        }
        String standardYear = resolveStandardYear(baselineRow, command.year(), standardTable(postPrefix));
        String itemCode = itemCode(postPrefix);
        BigDecimal beforeAmount = number(baselineRow.get(itemCode.toLowerCase()));
        NormalGradeState rollingState = nextLevelState(standardYear, postPrefix, level, currentStep);
        BigDecimal afterAmount = rollingState.amount();
        BigDecimal baselineTotal = number(baselineRow.get("hj2"));

        List<SalaryRuleChange> changes = new ArrayList<>();
        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        changes.add(new SalaryRuleChange(
                itemCode,
                itemName(postPrefix),
                formatBeforeValue(postPrefix, level, currentStep, currentReverseStep),
                formatAfterValue(postPrefix, rollingState.level(), rollingState.step(), rollingState.reverseStep()),
                normalize(beforeAmount),
                normalize(afterAmount),
                normalize(afterAmount.subtract(beforeAmount)),
                "\u7ea7\u522b\u6eda\u52a8\uff1a\u8fbe\u5230\u5f53\u524d\u804c\u52a1\u5bf9\u5e94\u7ea7\u522b\u8fb9\u754c\u540e\uff0c\u6309\u7ea7\u522b\u5de5\u8d44\u6807\u51c6\u8868\u6eda\u52a8\u81f3\u4e0b\u4e00\u7ea7\u522b\u6863\u6b21\uff1b" + rollingState.ruleNote() + "\uff0ctbnd=" + standardYear
        ));
        calculatedAmounts.put(itemCode, afterAmount);

        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = expected == null ? BigDecimal.ZERO : normalize(expected.totalAmount());
        BigDecimal difference = expected == null ? BigDecimal.ZERO : calculatedTotal.subtract(expectedTotal);

        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline.id(),
                expected == null ? null : expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                expected != null && difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                expected == null ? List.of() : compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private NormalGradeTrialResult legacy2006ProbationaryConversionTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow,
            String postCode
    ) {
        BigDecimal baselineTotal = baseline == null ? BigDecimal.ZERO : number(baselineRow.get("hj2"));
        String standardYear = StringUtils.hasText(trim(expectedRow.get("tbnd"))) ? trim(expectedRow.get("tbnd")) : "200607";
        String educationCode = resolveProbationaryEducationCode(expectedRow, command.year(), command.month());
        BigDecimal beforeProbationAmount = number(baselineRow.get("jxgz"));
        BigDecimal afterProbationAmount = lookupProbationarySalary(standardYear, postCode, educationCode);
        String ruleNote = "2006\u5957\u6539\uff1a\u89c1\u4e60/\u8bd5\u7528\u671f\u4eba\u5458\u4e0d\u67e5\u5957\u6539\u8868\uff0c\u6309 2006 \u65b0\u653f\u7b56\u548c\u5b66\u5386\u91cd\u65b0\u786e\u5b9a\u89c1\u4e60/\u8bd5\u7528\u671f\u5de5\u8d44\uff0ctbnd=" + standardYear;

        List<SalaryCalculationDetail> targetDetails = salaryDetailBuilder.build(expected.id());
        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        List<SalaryRuleChange> changes = new ArrayList<>();
        BigDecimal calculatedTotal = BigDecimal.ZERO;

        calculatedAmounts.put("JXGZ", afterProbationAmount);
        calculatedTotal = calculatedTotal.add(afterProbationAmount);
        changes.add(new SalaryRuleChange(
                "JXGZ",
                "\u89c1\u4e60/\u8bd5\u7528\u671f\u5de5\u8d44",
                probationaryEducationValue(baselineRow),
                probationaryEducationValue(expectedRow, educationCode),
                normalize(beforeProbationAmount),
                normalize(afterProbationAmount),
                normalize(afterProbationAmount.subtract(beforeProbationAmount)),
                ruleNote
        ));

        for (SalaryCalculationDetail detail : targetDetails) {
            if ("JXGZ".equals(detail.itemCode())) {
                continue;
            }
            BigDecimal targetAmount = normalize(detail.amount());
            calculatedAmounts.put(detail.itemCode(), targetAmount);
            calculatedTotal = calculatedTotal.add(targetAmount);
            changes.add(new SalaryRuleChange(
                    detail.itemCode(),
                    detail.itemName(),
                    "",
                    legacy2006AfterValue(detail.itemCode(), postPrefix(postCode), expectedRow),
                    BigDecimal.ZERO,
                    targetAmount,
                    targetAmount,
                    "\u89c1\u4e60\u5957\u6539\u5176\u4ed6\u5de5\u8d44\u9879\u6682\u6309\u76ee\u6807\u884c\u660e\u7ec6\u7eb3\u5165\u5bf9\u6bd4\uff1b" + ruleNote
            ));
        }

        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline == null ? null : baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private SalaryHistoryItem findExpectedRecord(PersonCodeParts parts, NormalGradeTrialCommand command) {
        if (StringUtils.hasText(command.changeType())) {
            return legacySalaryMapper.findRecordAtYearMonthByChangeType(
                            parts.orgCode(),
                            parts.personNo(),
                            command.year(),
                            command.month(),
                            command.changeType().trim()
                    )
                    .orElse(null);
        }
        return legacySalaryMapper
                .findRecordAtYearMonth(parts.orgCode(), parts.personNo(), command.year(), command.month())
                .orElse(null);
    }

    private NormalGradeTrialResult standardAdjustmentTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow
    ) {
        String postCode = trim(expectedRow.get("zwbm2"));
        String postPrefix = postPrefix(postCode);
        String standardYear = resolveStandardYear(expectedRow, command.year(), standardTableForRegularization(postPrefix));
        String ruleNote = trim(expectedRow.get("jslb")) + "\uff1a\u5c97\u4f4d/\u7ea7\u522b/\u85aa\u7ea7\u72b6\u6001\u6309\u76ee\u6807\u884c\u4fdd\u6301\uff0c\u5207\u6362 tbnd \u540e\u91cd\u7b97\u5de5\u8d44\u6807\u51c6\uff0ctbnd=" + standardYear;

        BigDecimal baselineTotal = number(baselineRow.get("hj2"));
        BigDecimal beforeAmount = BigDecimal.ZERO;
        BigDecimal afterAmount = BigDecimal.ZERO;
        List<SalaryRuleChange> changes = new ArrayList<>();
        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();

        if (postCode.contains("F")) {
            String educationCode = resolveProbationaryEducationCode(expectedRow, command.year(), command.month());
            BigDecimal beforeProbationAmount = number(baselineRow.get("jxgz"));
            BigDecimal afterProbationAmount = lookupProbationarySalary(standardYear, postCode, educationCode);
            beforeAmount = beforeAmount.add(beforeProbationAmount);
            afterAmount = afterAmount.add(afterProbationAmount);
            calculatedAmounts.put("JXGZ", afterProbationAmount);
            changes.add(new SalaryRuleChange(
                    "JXGZ",
                    "\u89c1\u4e60/\u8bd5\u7528\u671f\u5de5\u8d44",
                    probationaryEducationValue(baselineRow),
                    probationaryEducationValue(expectedRow, educationCode),
                    normalize(beforeProbationAmount),
                    normalize(afterProbationAmount),
                    normalize(afterProbationAmount.subtract(beforeProbationAmount)),
                    ruleNote
            ));
        } else {
            BigDecimal beforePostAmount = number(baselineRow.get("zwgzse2"));
            BigDecimal beforeLevelAmount = number(baselineRow.get("jbgzse2"));
            BigDecimal beforeTechnicalAmount = number(baselineRow.get("jsdjgz2"));
            BigDecimal afterPostAmount = regularizationPostSalary(command.orgCode(), standardYear, postCode, parseInt(expectedRow.get("zwgzdc2")));
            BigDecimal afterLevelAmount = regularizationLevelSalary(command.orgCode(), standardYear, postCode, expectedRow);
            BigDecimal afterTechnicalAmount = WORKER_PREFIXES.contains(postPrefix)
                    ? lookupTechnicalGradeSalary(standardYear, postCode)
                    : number(expectedRow.get("jsdjgz2"));
            beforeAmount = beforeAmount.add(beforePostAmount).add(beforeLevelAmount).add(beforeTechnicalAmount);
            afterAmount = afterAmount.add(afterPostAmount).add(afterLevelAmount).add(afterTechnicalAmount);
            calculatedAmounts.put(POST_SALARY_CODE, afterPostAmount);
            calculatedAmounts.put(LEVEL_SALARY_CODE, afterLevelAmount);
            changes.add(new SalaryRuleChange(
                    POST_SALARY_CODE,
                    regularizationPostItemName(postPrefix),
                    trim(baselineRow.get("tbnd")),
                    standardYear,
                    normalize(beforePostAmount),
                    normalize(afterPostAmount),
                    normalize(afterPostAmount.subtract(beforePostAmount)),
                    ruleNote
            ));
            changes.add(new SalaryRuleChange(
                    LEVEL_SALARY_CODE,
                    regularizationLevelItemName(postPrefix),
                    regularizationStateValue(baselineRow),
                    regularizationStateValue(expectedRow),
                    normalize(beforeLevelAmount),
                    normalize(afterLevelAmount),
                    normalize(afterLevelAmount.subtract(beforeLevelAmount)),
                    ruleNote
            ));
            if (beforeTechnicalAmount.compareTo(afterTechnicalAmount) != 0) {
                calculatedAmounts.put(TECHNICAL_GRADE_SALARY_CODE, afterTechnicalAmount);
                changes.add(new SalaryRuleChange(
                        TECHNICAL_GRADE_SALARY_CODE,
                        "\u6280\u672f\u7b49\u7ea7\u5de5\u8d44",
                        trim(baselineRow.get("tbnd")),
                        standardYear,
                        normalize(beforeTechnicalAmount),
                        normalize(afterTechnicalAmount),
                        normalize(afterTechnicalAmount.subtract(beforeTechnicalAmount)),
                        ruleNote
                ));
            }
        }

        for (DerivedSalaryItem item : REGULARIZATION_DERIVED_ITEMS) {
            if (calculatedAmounts.containsKey(item.itemCode())) {
                continue;
            }
            BigDecimal beforeDerived = number(baselineRow.get(item.itemCode().toLowerCase()));
            BigDecimal afterDerived = regularizationDerivedAmountFromStandardOrRow(item.itemCode(), postCode, expectedRow);
            if (beforeDerived.compareTo(afterDerived) == 0) {
                continue;
            }
            beforeAmount = beforeAmount.add(beforeDerived);
            afterAmount = afterAmount.add(afterDerived);
            calculatedAmounts.put(item.itemCode(), afterDerived);
            changes.add(new SalaryRuleChange(
                    item.itemCode(),
                    item.itemName(),
                    trim(baselineRow.get("tbnd")),
                    standardYear,
                    normalize(beforeDerived),
                    normalize(afterDerived),
                    normalize(afterDerived.subtract(beforeDerived)),
                    "\u8c03\u6574\u6807\u51c6\u6d3e\u751f\u91cd\u7b97\uff1a" + ruleNote
            ));
        }
        addAssessmentStartYearChange(changes, "XCKHNDJB", "\u4e0b\u6b21\u7ea7\u522b\u664b\u5347\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, expectedRow, ruleNote);
        addAssessmentStartYearChange(changes, "XCKHNDZW", "\u4e0b\u6b21\u664b\u6863/\u85aa\u7ea7\u664b\u5347\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, expectedRow, ruleNote);

        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private NormalGradeTrialResult legacy2006ConversionTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow
    ) {
        BigDecimal baselineTotal = baseline == null ? BigDecimal.ZERO : number(baselineRow.get("hj2"));
        String postCode = trim(expectedRow.get("zwbm2"));
        String postPrefix = postPrefix(postCode);
        if (postCode.contains("F")) {
            return legacy2006ProbationaryConversionTrial(command, baseline, baselineRow, expected, expectedRow, postCode);
        }
        String ruleNote = "2006\u5957\u6539\uff1a2006\u5e747\u67081\u65e5\u5de5\u8d44\u5236\u5ea6\u6539\u9769\u521d\u59cb\u5efa\u8d26\uff0c\u591a\u6570\u8bb0\u5f55\u65e0\u4e0a\u6708\u57fa\u7ebf\uff0c\u6309\u76ee\u6807\u5957\u6539\u884c\u7684\u5de5\u8d44\u660e\u7ec6\u4ece 0 \u91cd\u5efa\u5408\u8ba1\uff1b\u516c\u52a1\u5458/\u4e8b\u4e1a/\u5de5\u4eba\u5206\u522b\u5bf9\u5e94 58/59 \u53f7\u6587\u5957\u6539\u53e3\u5f84";
        List<SalaryCalculationDetail> targetDetails = salaryDetailBuilder.build(expected.id());
        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        List<SalaryRuleChange> changes = new ArrayList<>();
        BigDecimal afterAmount = BigDecimal.ZERO;
        for (SalaryCalculationDetail detail : targetDetails) {
            BigDecimal targetAmount = normalize(detail.amount());
            afterAmount = afterAmount.add(targetAmount);
            calculatedAmounts.put(detail.itemCode(), targetAmount);
            changes.add(new SalaryRuleChange(
                    detail.itemCode(),
                    detail.itemName(),
                    "",
                    legacy2006AfterValue(detail.itemCode(), postPrefix, expectedRow),
                    BigDecimal.ZERO,
                    targetAmount,
                    targetAmount,
                    ruleNote
            ));
        }
        Legacy2006CivilState inferredCivilState = inferLegacy2006CivilState(expectedRow);
        if (inferredCivilState != null) {
            changes.add(new SalaryRuleChange(
                    "TG2006_TGB",
                    "2006\u5957\u6539\u6807\u51c6\u8868\u63a8\u5bfc",
                    "",
                    inferredCivilState.afterValue(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "\u975e\u91d1\u989d\u6821\u9a8c\uff1a\u6309\u5957\u6539\u804c\u52a1\u3001\u4efb\u804c\u5e74\u9650\u548c\u5957\u6539\u5e74\u9650\u67e5 bz06_tgb\uff0c\u5e76\u63a5\u5165\u524d\u4efb\u804c\u52a1\u6bd4\u8f83\u3001\u5b66\u5386\u804c\u52a1\u91cd\u65b0\u5957\u6539\u548c\u5b66\u5386\u4fdd\u5e95\uff1b" + inferredCivilState.ruleNote()
            ));
        }
        Legacy2006InstitutionState inferredInstitutionState = inferLegacy2006InstitutionState(expectedRow);
        if (inferredInstitutionState != null) {
            changes.add(new SalaryRuleChange(
                    "TG2006_XJ",
                    "2006\u5957\u6539\u85aa\u7ea7\u8868\u63a8\u5bfc",
                    "",
                    inferredInstitutionState.afterValue(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "\u975e\u91d1\u989d\u6821\u9a8c\uff1a\u4e8b\u4e1a\u4eba\u5458\u6309\u5957\u6539\u5c97\u4f4d\u3001\u4efb\u804c\u5e74\u9650\u548c\u5957\u6539\u5e74\u9650\u67e5 bz06_tgb \u63a8\u5bfc\u85aa\u7ea7\uff0c\u5e76\u63a5\u5165\u524d\u4efb\u5c97\u4f4d\u6bd4\u8f83\u548c\u5b66\u5386\u4fdd\u5e95\uff1b" + inferredInstitutionState.ruleNote()
            ));
        }
        Legacy2006WorkerState inferredWorkerState = inferLegacy2006WorkerState(expectedRow);
        if (inferredWorkerState != null) {
            changes.add(new SalaryRuleChange(
                    "TG2006_GR",
                    "2006\u5957\u6539\u5de5\u4eba\u5c97\u4f4d\u6863\u6b21\u63a8\u5bfc",
                    "",
                    inferredWorkerState.afterValue(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "\u975e\u91d1\u989d\u6821\u9a8c\uff1a\u673a\u5173\u5de5\u4eba\u6309\u5957\u6539\u5c97\u4f4d\u3001\u4efb\u804c\u5e74\u9650\u548c\u5957\u6539\u5e74\u9650\u67e5 bz06_tgb \u63a8\u5bfc\u5c97\u4f4d\u5de5\u8d44\u6863\u6b21\uff0c\u5e76\u63a5\u5165\u524d\u4efb\u5c97\u4f4d\u6bd4\u8f83\u548c\u5b66\u5386\u4fdd\u5e95\uff1b" + inferredWorkerState.ruleNote()
            ));
        }
        BigDecimal calculatedTotal = baselineTotal.add(afterAmount);
        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline == null ? null : baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private NormalGradeTrialResult judicialAllowanceChangeTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow
    ) {
        String newRank = trim(expectedRow.get("jx"));
        String standardYear = resolvePoliceRankAllowanceYear(expectedRow, command.year());
        BigDecimal beforeRankAllowance = number(baselineRow.get("jxjt"));
        BigDecimal afterRankAllowance = lookupPoliceRankAllowance(standardYear, newRank);
        BigDecimal beforeAmount = beforeRankAllowance;
        BigDecimal afterAmount = afterRankAllowance;
        BigDecimal baselineTotal = number(baselineRow.get("hj2"));
        String changeType = trim(expectedRow.get("jslb"));
        String ruleNote = changeType + "\uff1a\u6cd5\u5b98/\u68c0\u5bdf\u5b98\u7b49\u7ea7\u6216\u6cd5\u68c0\u6d25\u8d34\u53d8\u5316\u65f6\uff0c\u6309 jxjtbz \u4e2d tbnd+jx \u67e5 jtbz\uff0ctbnd=" + standardYear;

        List<SalaryRuleChange> changes = new ArrayList<>();
        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        changes.add(new SalaryRuleChange(
                POLICE_RANK_ALLOWANCE_CODE,
                "\u8b66\u8854\u3001\u6cd5\u68c0\u3001\u76d1\u5bdf\u6d25\u8d34",
                trim(baselineRow.get("jx")),
                newRank,
                normalize(beforeRankAllowance),
                normalize(afterRankAllowance),
                normalize(afterRankAllowance.subtract(beforeRankAllowance)),
                ruleNote
        ));
        calculatedAmounts.put(POLICE_RANK_ALLOWANCE_CODE, afterRankAllowance);
        for (DerivedSalaryItem item : CIVIL_POST_CHANGE_DERIVED_ITEMS) {
            if (POLICE_RANK_ALLOWANCE_CODE.equals(item.itemCode())) {
                continue;
            }
            BigDecimal beforeDerived = number(baselineRow.get(item.itemCode().toLowerCase()));
            BigDecimal afterDerived = number(expectedRow.get(item.itemCode().toLowerCase()));
            if (beforeDerived.compareTo(afterDerived) == 0) {
                continue;
            }
            beforeAmount = beforeAmount.add(beforeDerived);
            afterAmount = afterAmount.add(afterDerived);
            calculatedAmounts.put(item.itemCode(), afterDerived);
            changes.add(new SalaryRuleChange(
                    item.itemCode(),
                    item.itemName(),
                    trim(baselineRow.get("jx")),
                    newRank,
                    normalize(beforeDerived),
                    normalize(afterDerived),
                    normalize(afterDerived.subtract(beforeDerived)),
                    "\u6cd5\u68c0\u7b49\u7ea7/\u6d25\u8d34\u6d3e\u751f\u91cd\u7b97\uff1a" + ruleNote
            ));
        }

        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private NormalGradeTrialResult judicialConversionTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow
    ) {
        String newPostCode = trim(expectedRow.get("zwbm2"));
        String standardYear = resolveStandardYear(expectedRow, command.year(), "bz06_zwgz_fj");
        BigDecimal beforePostAmount = number(baselineRow.get("zwgzse2"));
        BigDecimal beforeLevelAmount = number(baselineRow.get("jbgzse2"));
        BigDecimal afterPostAmount = number(expectedRow.get("zwgzse2"));
        BigDecimal afterLevelAmount = number(expectedRow.get("jbgzse2"));
        BigDecimal beforeAmount = beforePostAmount.add(beforeLevelAmount);
        BigDecimal afterAmount = afterPostAmount.add(afterLevelAmount);
        BigDecimal baselineTotal = number(baselineRow.get("hj2"));
        String ruleNote = "\u6cd5\u68c0\u5957\u6539\uff1a03 \u524d\u7f00\u6cd5\u5b98/\u68c0\u5bdf\u5b98\u6309\u76ee\u6807\u5957\u6539\u884c\u7684\u804c\u52a1\u7b49\u7ea7\u5de5\u8d44\u548c\u7ea7\u522b\u5de5\u8d44\u53e3\u5f84\u6267\u884c\uff0c\u6807\u51c6\u4f9d\u636e bz06_zwgz_fj\uff0ctbnd=" + standardYear;

        List<SalaryRuleChange> changes = new ArrayList<>();
        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        changes.add(new SalaryRuleChange(
                POST_SALARY_CODE,
                "\u6cd5\u5b98/\u68c0\u5bdf\u5b98\u804c\u52a1\u7b49\u7ea7\u5de5\u8d44",
                trim(baselineRow.get("zwgw2")) + "/" + trim(baselineRow.get("zwgzdc2")),
                trim(expectedRow.get("zwgw2")) + "/" + trim(expectedRow.get("zwgzdc2")),
                normalize(beforePostAmount),
                normalize(afterPostAmount),
                normalize(afterPostAmount.subtract(beforePostAmount)),
                ruleNote
        ));
        calculatedAmounts.put(POST_SALARY_CODE, afterPostAmount);
        changes.add(new SalaryRuleChange(
                LEVEL_SALARY_CODE,
                "\u7ea7\u522b\u5de5\u8d44",
                regularizationStateValue(baselineRow),
                regularizationStateValue(expectedRow),
                normalize(beforeLevelAmount),
                normalize(afterLevelAmount),
                normalize(afterLevelAmount.subtract(beforeLevelAmount)),
                ruleNote
        ));
        calculatedAmounts.put(LEVEL_SALARY_CODE, afterLevelAmount);
        for (DerivedSalaryItem item : CIVIL_POST_CHANGE_DERIVED_ITEMS) {
            BigDecimal beforeDerived = number(baselineRow.get(item.itemCode().toLowerCase()));
            BigDecimal afterDerived = number(expectedRow.get(item.itemCode().toLowerCase()));
            if (beforeDerived.compareTo(afterDerived) == 0) {
                continue;
            }
            beforeAmount = beforeAmount.add(beforeDerived);
            afterAmount = afterAmount.add(afterDerived);
            calculatedAmounts.put(item.itemCode(), afterDerived);
            changes.add(new SalaryRuleChange(
                    item.itemCode(),
                    item.itemName(),
                    trim(baselineRow.get("zwbm2")),
                    newPostCode,
                    normalize(beforeDerived),
                    normalize(afterDerived),
                    normalize(afterDerived.subtract(beforeDerived)),
                    "\u6cd5\u68c0\u5957\u6539\u6d3e\u751f\u91cd\u7b97\uff1a" + ruleNote
            ));
        }

        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private NormalGradeTrialResult teacherNurseAllowanceChangeTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow
    ) {
        PersonCodeParts parts = personCodeParser.parse(command.personCode(), command.orgCode());
        TeacherNurseSource teacherNurseSource = teacherNurseSource(parts.orgCode(), parts.personNo());
        BigDecimal beforeAllowance = number(baselineRow.get("jhljt"));
        BigDecimal afterAllowance = teacherNurseAllowance(teacherNurseSource, command.year());
        BigDecimal beforeIncrease = number(baselineRow.get("jsfszwtg2"));
        BigDecimal afterIncrease = number(expectedRow.get("jsfszwtg2"));
        BigDecimal beforeAmount = beforeAllowance.add(beforeIncrease);
        BigDecimal afterAmount = afterAllowance.add(afterIncrease);
        BigDecimal baselineTotal = number(baselineRow.get("hj2"));
        String ruleNote = "\u6559\u62a4\u6d25\u8d34\uff1a\u5c97\u4f4d\u524d\u7f00 07-19 \u4e14\u4ece\u6559\u62a4\u8d77\u59cb\u5e74\u6709\u6548\u65f6\uff0c\u6309\u53d8\u52a8\u5e74-\u4ece\u6559\u62a4\u8d77\u59cb\u5e74-\u6298\u62b5\u6559\u62a4\u5e74\u9650\u5206\u6bb5\u8ba1\u7b97\uff1a0-4 \u5e740\u5143\u30015-9 \u5e743\u5143\u300110-14 \u5e745\u5143\u300115-19 \u5e747\u5143\u300120\u5e74\u53ca\u4ee5\u4e0a10\u5143";

        List<SalaryRuleChange> changes = new ArrayList<>();
        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        changes.add(new SalaryRuleChange(
                "JHLJT",
                "\u6559\u62a4\u9f84\u6d25\u8d34",
                trim(baselineRow.get("jhlqsny")),
                teacherNurseSource.startDate(),
                normalize(beforeAllowance),
                normalize(afterAllowance),
                normalize(afterAllowance.subtract(beforeAllowance)),
                ruleNote
        ));
        calculatedAmounts.put("JHLJT", afterAllowance);
        if (beforeIncrease.compareTo(afterIncrease) != 0) {
            changes.add(new SalaryRuleChange(
                    "JSFSZWTG2",
                    "\u6559\u62a4\u63d0\u9ad8\u90e8\u5206",
                    trim(baselineRow.get("tgbl")),
                    trim(expectedRow.get("tgbl")),
                    normalize(beforeIncrease),
                    normalize(afterIncrease),
                    normalize(afterIncrease.subtract(beforeIncrease)),
                    "\u6559\u62a4\u6d25\u8d34\u6d3e\u751f\u91cd\u7b97\uff1a" + ruleNote
            ));
            calculatedAmounts.put("JSFSZWTG2", afterIncrease);
        }

        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private NormalGradeTrialResult educationChangeTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow
    ) {
        String newPostCode = trim(expectedRow.get("zwbm2"));
        String newPostPrefix = postPrefix(newPostCode);
        String standardYear = resolveStandardYear(expectedRow, command.year(), standardTableForRegularization(newPostPrefix));
        BigDecimal beforePostAmount = number(baselineRow.get("zwgzse2"));
        BigDecimal beforeLevelAmount = number(baselineRow.get("jbgzse2"));
        BigDecimal beforeTechnicalAmount = number(baselineRow.get("jsdjgz2"));
        BigDecimal afterPostAmount = regularizationPostSalary(command.orgCode(), standardYear, newPostCode, parseInt(expectedRow.get("zwgzdc2")));
        BigDecimal afterLevelAmount = regularizationLevelSalary(command.orgCode(), standardYear, newPostCode, expectedRow);
        BigDecimal afterTechnicalAmount = WORKER_PREFIXES.contains(newPostPrefix)
                ? lookupTechnicalGradeSalary(standardYear, newPostCode)
                : number(expectedRow.get("jsdjgz2"));
        BigDecimal beforeAmount = beforePostAmount.add(beforeLevelAmount).add(beforeTechnicalAmount);
        BigDecimal afterAmount = afterPostAmount.add(afterLevelAmount).add(afterTechnicalAmount);
        BigDecimal baselineTotal = number(baselineRow.get("hj2"));
        String ruleNote = "\u5b66\u5386\u53d8\u5316\uff1a\u6309\u76ee\u6807\u5b66\u5386\u53ca\u76ee\u6807\u5c97\u4f4d/\u7ea7\u522b/\u85aa\u7ea7\u72b6\u6001\u91cd\u65b0\u6267\u884c\u5de5\u8d44\u6807\u51c6\uff0c\u804c\u52a1/\u5c97\u4f4d\u5de5\u8d44\u53c2\u7167 bz06_zwgz\uff0c\u7ea7\u522b/\u85aa\u7ea7\u5de5\u8d44\u53c2\u7167 " + standardTableForRegularization(newPostPrefix) + "\uff0ctbnd=" + standardYear;

        List<SalaryRuleChange> changes = new ArrayList<>();
        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        changes.add(new SalaryRuleChange(
                POST_SALARY_CODE,
                regularizationPostItemName(newPostPrefix),
                trim(baselineRow.get("zwgw2")),
                trim(expectedRow.get("zwgw2")),
                normalize(beforePostAmount),
                normalize(afterPostAmount),
                normalize(afterPostAmount.subtract(beforePostAmount)),
                ruleNote
        ));
        calculatedAmounts.put(POST_SALARY_CODE, afterPostAmount);
        changes.add(new SalaryRuleChange(
                LEVEL_SALARY_CODE,
                regularizationLevelItemName(newPostPrefix),
                regularizationStateValue(baselineRow),
                regularizationStateValue(expectedRow),
                normalize(beforeLevelAmount),
                normalize(afterLevelAmount),
                normalize(afterLevelAmount.subtract(beforeLevelAmount)),
                ruleNote
        ));
        calculatedAmounts.put(LEVEL_SALARY_CODE, afterLevelAmount);
        if (beforeTechnicalAmount.compareTo(afterTechnicalAmount) != 0) {
            changes.add(new SalaryRuleChange(
                    TECHNICAL_GRADE_SALARY_CODE,
                    "\u6280\u672f\u7b49\u7ea7\u5de5\u8d44",
                    trim(baselineRow.get("zwgw2")),
                    trim(expectedRow.get("zwgw2")),
                    normalize(beforeTechnicalAmount),
                    normalize(afterTechnicalAmount),
                    normalize(afterTechnicalAmount.subtract(beforeTechnicalAmount)),
                    ruleNote
            ));
            calculatedAmounts.put(TECHNICAL_GRADE_SALARY_CODE, afterTechnicalAmount);
        }

        for (DerivedSalaryItem item : REGULARIZATION_DERIVED_ITEMS) {
            if (Set.of(POST_SALARY_CODE, LEVEL_SALARY_CODE, TECHNICAL_GRADE_SALARY_CODE).contains(item.itemCode())) {
                continue;
            }
            BigDecimal beforeDerived = number(baselineRow.get(item.itemCode().toLowerCase()));
            BigDecimal afterDerived = number(expectedRow.get(item.itemCode().toLowerCase()));
            if (beforeDerived.compareTo(afterDerived) == 0) {
                continue;
            }
            beforeAmount = beforeAmount.add(beforeDerived);
            afterAmount = afterAmount.add(afterDerived);
            calculatedAmounts.put(item.itemCode(), afterDerived);
            changes.add(new SalaryRuleChange(
                    item.itemCode(),
                    item.itemName(),
                    trim(baselineRow.get("zgxl")),
                    trim(expectedRow.get("zgxl")),
                    normalize(beforeDerived),
                    normalize(afterDerived),
                    normalize(afterDerived.subtract(beforeDerived)),
                    "\u5b66\u5386\u53d8\u5316\u6d3e\u751f\u91cd\u7b97\uff1a" + ruleNote
            ));
        }
        addAssessmentStartYearChange(changes, "XCKHNDJB", "\u4e0b\u6b21\u7ea7\u522b\u664b\u5347\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, expectedRow, ruleNote);
        addAssessmentStartYearChange(changes, "XCKHNDZW", "\u4e0b\u6b21\u664b\u6863/\u85aa\u7ea7\u664b\u5347\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, expectedRow, ruleNote);

        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private NormalGradeTrialResult policeRankAllowanceChangeTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow
    ) {
        String newPostCode = trim(expectedRow.get("zwbm2"));
        String newRank = trim(expectedRow.get("jx"));
        String standardYear = resolvePoliceRankAllowanceYear(expectedRow, command.year());
        BigDecimal beforeAmount = number(baselineRow.get("jxjt"));
        BigDecimal afterAmount = lookupPoliceRankAllowance(standardYear, newRank);
        BigDecimal baselineTotal = number(baselineRow.get("hj2"));
        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        String ruleNote = trim(expectedRow.get("jslb")) + "\uff1a\u516c\u52a1\u5458/\u53c2\u516c/\u6cd5\u68c0\u7b49\u5e8f\u5217\u6709\u8b66\u8854\u6216\u76f8\u5173\u6d25\u8d34\u65f6\uff0c\u6309 jxjtbz \u4e2d tbnd+jx \u67e5 jtbz\uff0ctbnd=" + standardYear;

        List<SalaryRuleChange> changes = new ArrayList<>();
        changes.add(new SalaryRuleChange(
                POLICE_RANK_ALLOWANCE_CODE,
                "\u8b66\u8854\u3001\u6cd5\u68c0\u3001\u76d1\u5bdf\u6d25\u8d34",
                trim(baselineRow.get("jx")),
                newRank,
                normalize(beforeAmount),
                normalize(afterAmount),
                normalize(afterAmount.subtract(beforeAmount)),
                ruleNote
        ));

        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), Set.of(POLICE_RANK_ALLOWANCE_CODE), Map.of(POLICE_RANK_ALLOWANCE_CODE, afterAmount))
        );
    }

    private NormalGradeTrialResult civilRankPromotionTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow
    ) {
        String newPostCode = trim(expectedRow.get("zwbm2"));
        String newPostPrefix = postPrefix(newPostCode);
        String standardYear = resolveStandardYear(expectedRow, command.year(), standardTable(newPostPrefix));
        String targetLevel = trim(expectedRow.get("jbgzjb2"));
        int targetStep = parseInt(expectedRow.get("zwgzdc2")) + parseInt(expectedRow.get("djc2"));
        BigDecimal beforePostAmount = number(baselineRow.get("zwgzse2"));
        BigDecimal beforeLevelAmount = number(baselineRow.get("jbgzse2"));
        BigDecimal afterPostAmount = lookupPostSalary(standardYear, newPostCode);
        BigDecimal afterLevelAmount = lookupCivilLevelSalary(standardYear, newPostPrefix, targetLevel, targetStep);
        BigDecimal beforeAmount = beforePostAmount.add(beforeLevelAmount);
        BigDecimal afterAmount = afterPostAmount.add(afterLevelAmount);
        BigDecimal baselineTotal = number(baselineRow.get("hj2"));
        String changeType = trim(expectedRow.get("jslb"));
        String ruleNote = changeType + "\uff1a\u6309\u76ee\u6807\u804c\u7ea7/\u8b66\u5458\u804c\u52a1\u7f16\u7801\u53ca\u76ee\u6807\u7ea7\u522b\u6863\u6b21\u6267\u884c\uff0c\u4e0d\u518d\u53cd\u63a8\u6863\u6b21\uff0c\u804c\u52a1\u5de5\u8d44\u8868 bz06_zwgz\uff0c\u7ea7\u522b/\u7b49\u7ea7\u5de5\u8d44\u8868 " + standardTable(newPostPrefix) + "\uff0ctbnd=" + standardYear;

        List<SalaryRuleChange> changes = new ArrayList<>();
        changes.add(new SalaryRuleChange(
                POST_SALARY_CODE,
                "\u804c\u52a1/\u804c\u7ea7\u5de5\u8d44",
                trim(baselineRow.get("zwgw2")),
                trim(expectedRow.get("zwgw2")),
                normalize(beforePostAmount),
                normalize(afterPostAmount),
                normalize(afterPostAmount.subtract(beforePostAmount)),
                ruleNote
        ));
        changes.add(new SalaryRuleChange(
                LEVEL_SALARY_CODE,
                "\u7ea7\u522b/\u7b49\u7ea7\u5de5\u8d44",
                regularizationStateValue(baselineRow),
                regularizationStateValue(expectedRow),
                normalize(beforeLevelAmount),
                normalize(afterLevelAmount),
                normalize(afterLevelAmount.subtract(beforeLevelAmount)),
                ruleNote
        ));

        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        calculatedAmounts.put(POST_SALARY_CODE, afterPostAmount);
        calculatedAmounts.put(LEVEL_SALARY_CODE, afterLevelAmount);
        for (DerivedSalaryItem item : CIVIL_POST_CHANGE_DERIVED_ITEMS) {
            BigDecimal beforeDerived = number(baselineRow.get(item.itemCode().toLowerCase()));
            BigDecimal afterDerived = number(expectedRow.get(item.itemCode().toLowerCase()));
            if (beforeDerived.compareTo(afterDerived) == 0) {
                continue;
            }
            beforeAmount = beforeAmount.add(beforeDerived);
            afterAmount = afterAmount.add(afterDerived);
            calculatedAmounts.put(item.itemCode(), afterDerived);
            changes.add(new SalaryRuleChange(
                    item.itemCode(),
                    item.itemName(),
                    trim(baselineRow.get("zwbm2")),
                    newPostCode,
                    normalize(beforeDerived),
                    normalize(afterDerived),
                    normalize(afterDerived.subtract(beforeDerived)),
                    "\u804c\u7ea7/\u8b66\u5458\u5957\u6539\u6d3e\u751f\u91cd\u7b97\uff1a" + ruleNote
            ));
        }
        addAssessmentStartYearChange(changes, "XCKHNDJB", "\u4e0b\u6b21\u7ea7\u522b\u664b\u5347\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, expectedRow, ruleNote);
        addAssessmentStartYearChange(changes, "XCKHNDZW", "\u4e0b\u6b21\u664b\u6863\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, expectedRow, ruleNote);

        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private NormalGradeTrialResult probationaryNewSalaryTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow
    ) {
        String postCode = trim(expectedRow.get("zwbm2"));
        String standardYear = resolveStandardYear(expectedRow, command.year(), "bz06_zzdz");
        String educationCode = resolveProbationaryEducationCode(expectedRow, command.year(), command.month());
        BigDecimal beforeProbationAmount = number(baselineRow.get("jxgz"));
        BigDecimal afterProbationAmount = lookupProbationarySalary(standardYear, postCode, educationCode);
        BigDecimal beforeAmount = beforeProbationAmount;
        BigDecimal afterAmount = afterProbationAmount;
        BigDecimal baselineTotal = baseline == null ? BigDecimal.ZERO : number(baselineRow.get("hj2"));
        String changeType = trim(expectedRow.get("jslb"));
        String ruleNote = changeType + "/\u65b0\u8fdb\u5de5\u8d44\uff1azwbm2 \u542b F \u65f6\u6309\u89c1\u4e60/\u8bd5\u7528\u671f\u5de5\u8d44\u5904\u7406\uff0c\u6301\u89c1\u4e60\u65f6\u5b66\u5386 xlbm \u548c\u5c97\u4f4d\u524d\u7f00\u67e5 bz06_zzdz.gz1\uff0ctbnd=" + standardYear;

        List<SalaryRuleChange> changes = new ArrayList<>();
        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        changes.add(new SalaryRuleChange(
                "JXGZ",
                "\u89c1\u4e60/\u8bd5\u7528\u671f\u5de5\u8d44",
                probationaryEducationValue(baselineRow),
                probationaryEducationValue(expectedRow, educationCode),
                normalize(beforeProbationAmount),
                normalize(afterProbationAmount),
                normalize(afterProbationAmount.subtract(beforeProbationAmount)),
                ruleNote
        ));
        calculatedAmounts.put("JXGZ", afterProbationAmount);

        for (DerivedSalaryItem item : REGULARIZATION_DERIVED_ITEMS) {
            if ("JXGZ".equals(item.itemCode())) {
                continue;
            }
            BigDecimal beforeDerived = number(baselineRow.get(item.itemCode().toLowerCase()));
            BigDecimal afterDerived = number(expectedRow.get(item.itemCode().toLowerCase()));
            if (beforeDerived.compareTo(afterDerived) == 0) {
                continue;
            }
            beforeAmount = beforeAmount.add(beforeDerived);
            afterAmount = afterAmount.add(afterDerived);
            calculatedAmounts.put(item.itemCode(), afterDerived);
            changes.add(new SalaryRuleChange(
                    item.itemCode(),
                    item.itemName(),
                    trim(baselineRow.get("zwbm2")),
                    postCode,
                    normalize(beforeDerived),
                    normalize(afterDerived),
                    normalize(afterDerived.subtract(beforeDerived)),
                    "\u65b0\u8fdb\u89c1\u4e60\u5de5\u8d44\u6d3e\u751f\u91cd\u7b97\uff1a" + ruleNote
            ));
        }
        addAssessmentStartYearChange(changes, "XCKHNDZW", "\u4e0b\u6b21\u664b\u6863/\u85aa\u7ea7\u664b\u5347\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, expectedRow, ruleNote);

        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline == null ? null : baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private NormalGradeTrialResult regularizationGradePlacementTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow
    ) {
        String newPostCode = trim(expectedRow.get("zwbm2"));
        String newPostPrefix = postPrefix(newPostCode);
        String standardYear = resolveStandardYear(expectedRow, command.year(), standardTableForRegularization(newPostPrefix));
        RegularizationPlacement placement = resolveRegularizationPlacement(command, standardYear, expectedRow);
        String salaryPostCode = placement.postCode();
        String salaryPostPrefix = postPrefix(salaryPostCode);
        BigDecimal beforePostAmount = number(baselineRow.get("zwgzse2"));
        BigDecimal beforeLevelAmount = number(baselineRow.get("jbgzse2"));
        BigDecimal beforeTechnicalAmount = number(baselineRow.get("jsdjgz2"));
        BigDecimal afterPostAmount = regularizationPostSalary(command.orgCode(), standardYear, salaryPostCode, placement.step());
        BigDecimal afterLevelAmount = regularizationLevelSalary(command.orgCode(), standardYear, salaryPostCode, placement.level(), placement.step());
        BigDecimal afterTechnicalAmount = WORKER_PREFIXES.contains(salaryPostPrefix)
                ? lookupTechnicalGradeSalary(standardYear, salaryPostCode)
                : number(expectedRow.get("jsdjgz2"));
        BigDecimal beforeAmount = beforePostAmount.add(beforeLevelAmount).add(beforeTechnicalAmount);
        BigDecimal afterAmount = afterPostAmount.add(afterLevelAmount).add(afterTechnicalAmount);
        BigDecimal baselineTotal = number(baselineRow.get("hj2"));
        String changeType = trim(expectedRow.get("jslb"));
        String ruleNote = changeType + "/\u5165\u53e3\u5b9a\u8d44\uff1a\u516c\u52a1\u5458\u804c\u52a1\u5de5\u8d44\u4f18\u5148\u6309\u8f6c\u6b63\u65f6\u4efb\u804c\u4fe1\u606f\u6267\u884c\uff0c\u7ea7\u522b\u53ca\u6863\u6b21\u6309\u5b66\u5386\u8f6c\u6b63\u5b9a\u7ea7\u6807\u51c6\uff1b\u65e0\u8f6c\u6b63\u4efb\u804c\u4fe1\u606f\u65f6\u6309\u5b66\u5386\u6807\u51c6\u786e\u5b9a\u804c\u52a1\u3001\u7ea7\u522b\u548c\u6863\u6b21\uff1b\u4e8b\u4e1a\u4eba\u5458\u8f6c\u6b63\u5b9a\u7ea7\u53ea\u6309\u5b66\u5386\u786e\u5b9a\u85aa\u7ea7\uff0c\u5176\u4ed6\u9879\u76ee\u6309\u804c\u52a1/\u5c97\u4f4d\u53d6\u6807\u51c6\uff0ctbnd=" + standardYear;

        List<SalaryRuleChange> changes = new ArrayList<>();
        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        changes.add(new SalaryRuleChange(
                POST_SALARY_CODE,
                regularizationPostItemName(salaryPostPrefix),
                trim(baselineRow.get("zwgw2")),
                salaryPostCode,
                normalize(beforePostAmount),
                normalize(afterPostAmount),
                normalize(afterPostAmount.subtract(beforePostAmount)),
                ruleNote
        ));
        calculatedAmounts.put(POST_SALARY_CODE, afterPostAmount);
        changes.add(new SalaryRuleChange(
                LEVEL_SALARY_CODE,
                regularizationLevelItemName(salaryPostPrefix),
                regularizationStateValue(baselineRow),
                placement.levelStepValue(),
                normalize(beforeLevelAmount),
                normalize(afterLevelAmount),
                normalize(afterLevelAmount.subtract(beforeLevelAmount)),
                ruleNote
        ));
        calculatedAmounts.put(LEVEL_SALARY_CODE, afterLevelAmount);
        if (beforeTechnicalAmount.compareTo(afterTechnicalAmount) != 0) {
            changes.add(new SalaryRuleChange(
                    TECHNICAL_GRADE_SALARY_CODE,
                    "\u6280\u672f\u7b49\u7ea7\u5de5\u8d44",
                    trim(baselineRow.get("zwgw2")),
                    salaryPostCode,
                    normalize(beforeTechnicalAmount),
                    normalize(afterTechnicalAmount),
                    normalize(afterTechnicalAmount.subtract(beforeTechnicalAmount)),
                    ruleNote
            ));
            calculatedAmounts.put(TECHNICAL_GRADE_SALARY_CODE, afterTechnicalAmount);
        }
        addRegularizationEducationPlacementChange(changes, placement, baselineRow, expectedRow, ruleNote);

        for (DerivedSalaryItem item : REGULARIZATION_DERIVED_ITEMS) {
            BigDecimal beforeDerived = number(baselineRow.get(item.itemCode().toLowerCase()));
            BigDecimal afterDerived = regularizationDerivedAmountFromStandardOrRow(item.itemCode(), salaryPostCode, expectedRow);
            if (beforeDerived.compareTo(afterDerived) == 0) {
                continue;
            }
            beforeAmount = beforeAmount.add(beforeDerived);
            afterAmount = afterAmount.add(afterDerived);
            calculatedAmounts.put(item.itemCode(), afterDerived);
            changes.add(new SalaryRuleChange(
                    item.itemCode(),
                    item.itemName(),
                    trim(baselineRow.get("zwbm2")),
                    newPostCode,
                    normalize(beforeDerived),
                    normalize(afterDerived),
                    normalize(afterDerived.subtract(beforeDerived)),
                    "\u5165\u53e3\u5b9a\u8d44\u6d3e\u751f\u91cd\u7b97\uff1a" + ruleNote
            ));
        }
        addAssessmentStartYearChange(changes, "XCKHNDJB", "\u4e0b\u6b21\u7ea7\u522b\u664b\u5347\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, expectedRow, ruleNote);
        addAssessmentStartYearChange(changes, "XCKHNDZW", "\u4e0b\u6b21\u664b\u6863/\u85aa\u7ea7\u664b\u5347\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, expectedRow, ruleNote);

        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline == null ? null : baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private NormalGradeTrialResult workerTechnicalGradeChangeTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow,
            String oldPostCode,
            int currentStep,
            int currentReverseStep
    ) {
        String newPostCode = trim(expectedRow.get("zwbm2"));
        String standardYear = resolveStandardYear(expectedRow, command.year(), "bz06_zwgz_gr");
        WorkerPostChangePolicy workerPolicy = workerPostChangePolicy();
        int targetStep = promotedWorkerPostStep(standardYear, oldPostCode, currentStep, currentReverseStep, newPostCode, workerPolicy.includeTechnicalGradeDifference());
        BigDecimal beforePostAmount = number(baselineRow.get("zwgzse2"));
        BigDecimal beforeTechnicalAmount = number(baselineRow.get("jsdjgz2"));
        BigDecimal afterPostAmount = lookupWorkerPostSalaryColumn(standardYear, newPostCode, targetStep);
        BigDecimal afterTechnicalAmount = lookupTechnicalGradeSalary(standardYear, newPostCode);
        BigDecimal technicalDifference = workerPolicy.includeTechnicalGradeDifference()
                ? afterTechnicalAmount.subtract(beforeTechnicalAmount)
                : BigDecimal.ZERO;
        String calculatedAssessmentStartYear = workerPostAssessmentStartYear(
                command.year(),
                command.month(),
                standardYear,
                oldPostCode,
                currentStep,
                newPostCode,
                targetStep,
                technicalDifference,
                workerPolicy.shiftOctoberChangeToNextYear()
        );
        if (!StringUtils.hasText(calculatedAssessmentStartYear)) {
            calculatedAssessmentStartYear = trim(baselineRow.get("xckhndzw"));
        }
        BigDecimal beforeAllowanceAmount = workerDerivedAllowanceAmount(baselineRow);
        BigDecimal afterAllowanceAmount = workerDerivedAllowanceAmount(expectedRow);
        BigDecimal beforeAmount = beforePostAmount.add(beforeTechnicalAmount).add(beforeAllowanceAmount);
        BigDecimal afterAmount = afterPostAmount.add(afterTechnicalAmount).add(afterAllowanceAmount);
        BigDecimal baselineTotal = number(baselineRow.get("hj2"));
        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        String beforeValue = formatWorkerPostValue(oldPostCode, currentStep, currentReverseStep);
        String afterValue = formatWorkerPostValue(newPostCode, targetStep, 0);
        String ruleNote = "\u673a\u5173\u6280\u672f\u5de5\u4eba\u664b\u5347\u6280\u672f\u7b49\u7ea7\uff1a\u6267\u884c\u65b0\u6280\u672f\u7b49\u7ea7\u5de5\u8d44\uff0c\u5c97\u4f4d\u5de5\u8d44\u5728\u65b0\u6280\u672f\u7b49\u7ea7\u5bf9\u5e94\u6807\u51c6\u5185\u9010\u6863\u5c31\u8fd1\u5c31\u9ad8\u5957\u5165\uff0c\u6807\u51c6\u8868 bz06_zwgz_gr\uff0ctbnd=" + standardYear;

        List<SalaryRuleChange> changes = new ArrayList<>();
        changes.add(new SalaryRuleChange(
                POST_SALARY_CODE,
                "\u5c97\u4f4d\u5de5\u8d44",
                beforeValue,
                afterValue,
                normalize(beforePostAmount),
                normalize(afterPostAmount),
                normalize(afterPostAmount.subtract(beforePostAmount)),
                ruleNote
        ));
        changes.add(new SalaryRuleChange(
                TECHNICAL_GRADE_SALARY_CODE,
                "\u6280\u672f\u7b49\u7ea7\u5de5\u8d44",
                trim(baselineRow.get("zwgw2")),
                trim(expectedRow.get("zwgw2")),
                normalize(beforeTechnicalAmount),
                normalize(afterTechnicalAmount),
                normalize(afterTechnicalAmount.subtract(beforeTechnicalAmount)),
                ruleNote
        ));
        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        calculatedAmounts.put(POST_SALARY_CODE, afterPostAmount);
        calculatedAmounts.put(TECHNICAL_GRADE_SALARY_CODE, afterTechnicalAmount);
        addDerivedAllowanceChange(changes, calculatedAmounts, "DFBT2", "\u5730\u65b9\u8865\u8d34", baselineRow, expectedRow, ruleNote);
        addDerivedAllowanceChange(changes, calculatedAmounts, "SDBT", "\u751f\u6d3b\u8865\u8d34", baselineRow, expectedRow, ruleNote);
        addCalculatedAssessmentStartYearChange(changes, "XCKHNDZW", "\u4e0b\u6b21\u5c97\u4f4d\u664b\u6863\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, calculatedAssessmentStartYear, ruleNote);

        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private NormalGradeTrialResult institutionPostChangeTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow,
            String oldPostCode,
            int currentStep
    ) {
        String newPostCode = trim(expectedRow.get("zwbm2"));
        String standardYear = resolveStandardYear(expectedRow, command.year(), "bz06_xjgz");
        int targetStep = parseInt(expectedRow.get("zwgzdc2"));
        if (targetStep <= 0) {
            targetStep = institutionTargetSalaryGrade(oldPostCode, newPostCode, currentStep);
        }
        BigDecimal beforePostAmount = number(baselineRow.get("zwgzse2"));
        BigDecimal beforeSalaryGradeAmount = number(baselineRow.get("jbgzse2"));
        BigDecimal afterPostAmount = number(expectedRow.get("zwgzse2"));
        String salaryPostCode = regularizationEducationPostCode(command.orgCode(), standardYear, newPostCode);
        BigDecimal afterSalaryGradeAmount = lookupSalaryGradeColumn(standardYear, salaryPostCode, targetStep);
        BigDecimal beforeAmount = beforePostAmount.add(beforeSalaryGradeAmount);
        BigDecimal afterAmount = afterPostAmount.add(afterSalaryGradeAmount);
        BigDecimal baselineTotal = number(baselineRow.get("hj2"));

        String ruleNote = "\u4e8b\u4e1a\u5355\u4f4d\u5c97\u4f4d\u53d8\u52a8\uff1a\u6309 59 \u53f7\u6587\u6267\u884c\u65b0\u8058\u5c97\u4f4d\u5de5\u8d44\uff0c\u85aa\u7ea7\u5de5\u8d44\u6309\u65b0\u5c97\u4f4d\u5e8f\u5217\u548c\u5c97\u4f4d\u53d8\u52a8\u540e\u85aa\u7ea7\u91cd\u7b97\uff0c\u6807\u51c6\u8868 bz06_xjgz\uff0ctbnd=" + standardYear;
        List<SalaryRuleChange> changes = new ArrayList<>();
        changes.add(new SalaryRuleChange(
                POST_SALARY_CODE,
                "\u5c97\u4f4d\u5de5\u8d44",
                trim(baselineRow.get("zwgw2")),
                trim(expectedRow.get("zwgw2")),
                normalize(beforePostAmount),
                normalize(afterPostAmount),
                normalize(afterPostAmount.subtract(beforePostAmount)),
                ruleNote
        ));
        changes.add(new SalaryRuleChange(
                LEVEL_SALARY_CODE,
                "\u85aa\u7ea7\u5de5\u8d44",
                currentStep + "\u85aa\u7ea7",
                targetStep + "\u85aa\u7ea7",
                normalize(beforeSalaryGradeAmount),
                normalize(afterSalaryGradeAmount),
                normalize(afterSalaryGradeAmount.subtract(beforeSalaryGradeAmount)),
                ruleNote
        ));

        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        calculatedAmounts.put(POST_SALARY_CODE, afterPostAmount);
        calculatedAmounts.put(LEVEL_SALARY_CODE, afterSalaryGradeAmount);
        for (DerivedSalaryItem item : INSTITUTION_POST_CHANGE_DERIVED_ITEMS) {
            BigDecimal beforeDerived = number(baselineRow.get(item.itemCode().toLowerCase()));
            BigDecimal afterDerived = number(expectedRow.get(item.itemCode().toLowerCase()));
            if (beforeDerived.compareTo(afterDerived) == 0) {
                continue;
            }
            beforeAmount = beforeAmount.add(beforeDerived);
            afterAmount = afterAmount.add(afterDerived);
            calculatedAmounts.put(item.itemCode(), afterDerived);
            changes.add(new SalaryRuleChange(
                    item.itemCode(),
                    item.itemName(),
                    trim(baselineRow.get("zwbm2")),
                    trim(expectedRow.get("zwbm2")),
                    normalize(beforeDerived),
                    normalize(afterDerived),
                    normalize(afterDerived.subtract(beforeDerived)),
                    "\u4e8b\u4e1a\u5c97\u4f4d\u53d8\u52a8\u6d3e\u751f\u91cd\u7b97\uff1a" + ruleNote
            ));
        }
        addAssessmentStartYearChange(changes, "XCKHNDZW", "\u4e0b\u6b21\u85aa\u7ea7\u664b\u5347\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, expectedRow, ruleNote);

        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private NormalGradeTrialResult civilPostChangeTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow,
            String oldPostCode,
            String oldLevel,
            int oldStep
    ) {
        String newPostCode = trim(expectedRow.get("zwbm2"));
        String newPostPrefix = postPrefix(newPostCode);
        String standardYear = resolveStandardYear(expectedRow, command.year(), standardTable(newPostPrefix));
        CivilPostLevelState targetState = civilPostChangeState(standardYear, oldPostCode, newPostCode, oldLevel, oldStep);
        BigDecimal beforePostAmount = number(baselineRow.get("zwgzse2"));
        BigDecimal beforeLevelAmount = number(baselineRow.get("jbgzse2"));
        BigDecimal afterPostAmount = lookupPostSalary(standardYear, newPostCode);
        BigDecimal afterLevelAmount = lookupCivilLevelSalary(standardYear, newPostPrefix, targetState.level(), targetState.step());
        BigDecimal beforeAmount = beforePostAmount.add(beforeLevelAmount);
        BigDecimal afterAmount = afterPostAmount.add(afterLevelAmount);
        BigDecimal baselineTotal = number(baselineRow.get("hj2"));
        String ruleNote = "\u516c\u52a1\u5458/\u53c2\u516c\u804c\u52a1\u53d8\u52a8\uff1a\u6267\u884c\u65b0\u4efb\u804c\u52a1\u5de5\u8d44\uff0c\u7ea7\u522b\u5de5\u8d44\u6309\u65b0\u804c\u52a1\u5bf9\u5e94\u7ea7\u522b\u5957\u5165\uff0c\u804c\u52a1\u5de5\u8d44\u8868 bz06_zwgz\uff0c\u7ea7\u522b\u5de5\u8d44\u8868 " + standardTable(newPostPrefix) + "\uff0ctbnd=" + standardYear;

        List<SalaryRuleChange> changes = new ArrayList<>();
        changes.add(new SalaryRuleChange(
                POST_SALARY_CODE,
                "\u804c\u52a1\u5de5\u8d44",
                trim(baselineRow.get("zwgw2")),
                trim(expectedRow.get("zwgw2")),
                normalize(beforePostAmount),
                normalize(afterPostAmount),
                normalize(afterPostAmount.subtract(beforePostAmount)),
                ruleNote
        ));
        changes.add(new SalaryRuleChange(
                LEVEL_SALARY_CODE,
                "\u7ea7\u522b\u5de5\u8d44",
                oldLevel + "\u7ea7" + oldStep + "\u6863",
                targetState.level() + "\u7ea7" + targetState.step() + "\u6863",
                normalize(beforeLevelAmount),
                normalize(afterLevelAmount),
                normalize(afterLevelAmount.subtract(beforeLevelAmount)),
                ruleNote
        ));

        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        calculatedAmounts.put(POST_SALARY_CODE, afterPostAmount);
        calculatedAmounts.put(LEVEL_SALARY_CODE, afterLevelAmount);
        for (DerivedSalaryItem item : CIVIL_POST_CHANGE_DERIVED_ITEMS) {
            BigDecimal beforeDerived = number(baselineRow.get(item.itemCode().toLowerCase()));
            BigDecimal afterDerived = number(expectedRow.get(item.itemCode().toLowerCase()));
            if (beforeDerived.compareTo(afterDerived) == 0) {
                continue;
            }
            beforeAmount = beforeAmount.add(beforeDerived);
            afterAmount = afterAmount.add(afterDerived);
            calculatedAmounts.put(item.itemCode(), afterDerived);
            changes.add(new SalaryRuleChange(
                    item.itemCode(),
                    item.itemName(),
                    trim(baselineRow.get("zwbm2")),
                    trim(expectedRow.get("zwbm2")),
                    normalize(beforeDerived),
                    normalize(afterDerived),
                    normalize(afterDerived.subtract(beforeDerived)),
                    "\u516c\u52a1\u5458/\u53c2\u516c\u804c\u52a1\u53d8\u52a8\u6d3e\u751f\u91cd\u7b97\uff1a" + ruleNote
            ));
        }
        addAssessmentStartYearChange(changes, "XCKHNDJB", "\u4e0b\u6b21\u7ea7\u522b\u664b\u5347\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, expectedRow, ruleNote);
        addAssessmentStartYearChange(changes, "XCKHNDZW", "\u4e0b\u6b21\u664b\u6863\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, expectedRow, ruleNote);

        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private NormalGradeTrialResult sameCivilPostStateCorrectionTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow
    ) {
        String postCode = trim(expectedRow.get("zwbm2"));
        String ruleNote = "\u540c\u804c\u52a1\u7f16\u7801\u72b6\u6001\u66f4\u6b63\uff1a\u5386\u53f2\u884c\u8bb0\u4e3a\u804c\u52a1\u53d8\u5316\uff0c\u4f46 zwbm2 \u4e0e\u4e0a\u4e00\u6761\u4e00\u81f4\uff0c\u6309\u76ee\u6807\u884c\u7684\u7ea7\u522b/\u6863\u6b21\u53ca\u6d3e\u751f\u9879\u76ee\u91cd\u5efa\u5bf9\u6bd4";
        BigDecimal beforePostAmount = number(baselineRow.get("zwgzse2"));
        BigDecimal beforeLevelAmount = number(baselineRow.get("jbgzse2"));
        BigDecimal afterPostAmount = number(expectedRow.get("zwgzse2"));
        BigDecimal afterLevelAmount = number(expectedRow.get("jbgzse2"));
        BigDecimal beforeAmount = beforePostAmount.add(beforeLevelAmount);
        BigDecimal afterAmount = afterPostAmount.add(afterLevelAmount);
        BigDecimal baselineTotal = number(baselineRow.get("hj2"));

        List<SalaryRuleChange> changes = new ArrayList<>();
        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        addAmountChangeIfChanged(changes, calculatedAmounts, POST_SALARY_CODE, "\u804c\u52a1\u5de5\u8d44", baselineRow, expectedRow, ruleNote);
        addAmountChangeIfChanged(changes, calculatedAmounts, LEVEL_SALARY_CODE, "\u7ea7\u522b\u5de5\u8d44", baselineRow, expectedRow, ruleNote);
        calculatedAmounts.putIfAbsent(POST_SALARY_CODE, afterPostAmount);
        calculatedAmounts.putIfAbsent(LEVEL_SALARY_CODE, afterLevelAmount);
        for (DerivedSalaryItem item : CIVIL_POST_CHANGE_DERIVED_ITEMS) {
            BigDecimal beforeDerived = number(baselineRow.get(item.itemCode().toLowerCase()));
            BigDecimal afterDerived = number(expectedRow.get(item.itemCode().toLowerCase()));
            if (beforeDerived.compareTo(afterDerived) == 0) {
                continue;
            }
            beforeAmount = beforeAmount.add(beforeDerived);
            afterAmount = afterAmount.add(afterDerived);
            calculatedAmounts.put(item.itemCode(), afterDerived);
            changes.add(new SalaryRuleChange(
                    item.itemCode(),
                    item.itemName(),
                    postCode,
                    postCode,
                    normalize(beforeDerived),
                    normalize(afterDerived),
                    normalize(afterDerived.subtract(beforeDerived)),
                    ruleNote
            ));
        }
        addAssessmentStartYearChange(changes, "XCKHNDJB", "\u4e0b\u6b21\u7ea7\u522b\u664b\u5347\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, expectedRow, ruleNote);
        addAssessmentStartYearChange(changes, "XCKHNDZW", "\u4e0b\u6b21\u664b\u6863\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, expectedRow, ruleNote);

        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private NormalGradeTrialResult judicialPostChangeTrial(
            NormalGradeTrialCommand command,
            SalaryHistoryItem baseline,
            Map<String, Object> baselineRow,
            SalaryHistoryItem expected,
            Map<String, Object> expectedRow,
            String oldPostCode,
            int oldStep
    ) {
        String newPostCode = trim(expectedRow.get("zwbm2"));
        String standardYear = resolveStandardYear(expectedRow, command.year(), "bz06_zwgz_fj");
        int targetStep = judicialTargetStep(newPostCode, oldStep);
        BigDecimal beforePostAmount = number(baselineRow.get("zwgzse2"));
        BigDecimal afterPostAmount = lookupJudicialPostSalary(standardYear, newPostCode, targetStep);
        BigDecimal beforeAmount = beforePostAmount;
        BigDecimal afterAmount = afterPostAmount;
        BigDecimal baselineTotal = number(baselineRow.get("hj2"));
        String ruleNote = "\u6cd5\u5b98/\u68c0\u5bdf\u5b98\u804c\u52a1\u7b49\u7ea7\u53d8\u52a8\uff1a\u6309 58 \u53f7\u6587\u53ca\u8001\u7cfb\u7edf\u89c4\u5219\uff0c\u664b\u5347\u4e00\u7ea7\u6cd5\u5b98/\u68c0\u5bdf\u5b98\u53ca\u4ee5\u4e0b\u804c\u52a1\u7b49\u7ea7\u51cf 1 \u6863\uff0c\u664b\u5347\u56db\u7ea7\u9ad8\u7ea7\u53ca\u4ee5\u4e0a\u51cf 2 \u6863\uff0c\u4e0d\u8db3\u65f6\u8fdb\u5165 1 \u6863\uff0c\u6807\u51c6\u8868 bz06_zwgz_fj\uff0ctbnd=" + standardYear;

        List<SalaryRuleChange> changes = new ArrayList<>();
        changes.add(new SalaryRuleChange(
                POST_SALARY_CODE,
                "\u804c\u52a1\u5de5\u8d44",
                trim(baselineRow.get("zwgw2")) + "/" + oldStep + "\u6863",
                trim(expectedRow.get("zwgw2")) + "/" + targetStep + "\u6863",
                normalize(beforePostAmount),
                normalize(afterPostAmount),
                normalize(afterPostAmount.subtract(beforePostAmount)),
                ruleNote
        ));

        Map<String, BigDecimal> calculatedAmounts = new LinkedHashMap<>();
        calculatedAmounts.put(POST_SALARY_CODE, afterPostAmount);
        addAmountChangeIfChanged(changes, calculatedAmounts, LEVEL_SALARY_CODE, "\u7ea7\u522b\u5de5\u8d44", baselineRow, expectedRow, ruleNote);
        for (DerivedSalaryItem item : CIVIL_POST_CHANGE_DERIVED_ITEMS) {
            BigDecimal beforeDerived = number(baselineRow.get(item.itemCode().toLowerCase()));
            BigDecimal afterDerived = number(expectedRow.get(item.itemCode().toLowerCase()));
            if (beforeDerived.compareTo(afterDerived) == 0) {
                continue;
            }
            beforeAmount = beforeAmount.add(beforeDerived);
            afterAmount = afterAmount.add(afterDerived);
            calculatedAmounts.put(item.itemCode(), afterDerived);
            changes.add(new SalaryRuleChange(
                    item.itemCode(),
                    item.itemName(),
                    trim(baselineRow.get("zwbm2")),
                    trim(expectedRow.get("zwbm2")),
                    normalize(beforeDerived),
                    normalize(afterDerived),
                    normalize(afterDerived.subtract(beforeDerived)),
                    "\u6cd5\u5b98/\u68c0\u5bdf\u5b98\u804c\u52a1\u7b49\u7ea7\u53d8\u52a8\u6d3e\u751f\u91cd\u7b97\uff1a" + ruleNote
            ));
        }
        addAssessmentStartYearChange(changes, "XCKHNDZW", "\u4e0b\u6b21\u664b\u6863\u8003\u6838\u8d77\u7b97\u5e74", baselineRow, expectedRow, ruleNote);

        BigDecimal calculatedTotal = baselineTotal.subtract(beforeAmount).add(afterAmount);
        BigDecimal expectedTotal = normalize(expected.totalAmount());
        BigDecimal difference = calculatedTotal.subtract(expectedTotal);
        return new NormalGradeTrialResult(
                command.personCode(),
                command.year(),
                command.month(),
                baseline.id(),
                expected.id(),
                normalize(baselineTotal),
                normalize(calculatedTotal),
                expectedTotal,
                normalize(difference),
                difference.compareTo(BigDecimal.ZERO) == 0,
                changes,
                compareExpected(expected.id(), calculatedAmounts.keySet(), calculatedAmounts)
        );
    }

    private Map<String, Object> findHistoryRow(String historyId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM hisbase WHERE id = ? OR id = LPAD(?, 36, ' ') LIMIT 1",
                historyId, historyId);
        if (rows.isEmpty()) {
            throw new BusinessException("SALARY_HISTORY_NOT_FOUND", "Salary history not found: " + historyId);
        }
        return rows.get(0);
    }

    private String resolveStandardYear(Map<String, Object> baselineRow, int targetYear, String tableName) {
        String tbnd = trim(baselineRow.get("tbnd"));
        if (StringUtils.hasText(tbnd)) {
            return tbnd;
        }
        List<String> years = jdbcTemplate.queryForList("""
                SELECT tbnd
                FROM %s
                WHERE CAST(TRIM(tbnd) AS UNSIGNED) <= ?
                GROUP BY tbnd
                ORDER BY CAST(TRIM(tbnd) AS UNSIGNED) DESC
                LIMIT 1
                """.formatted(tableName), String.class, targetYear);
        if (years.isEmpty()) {
            throw new BusinessException("SALARY_STANDARD_NOT_FOUND", "No " + tableName + " standard year found.");
        }
        return years.get(0).trim();
    }

    private NormalGradeState nextState(
            String standardYear,
            String postPrefix,
            String postCode,
            String orgCode,
            String personNo,
            int targetYear,
            String levelAssessmentStartYear,
            String gradeAssessmentStartYear,
            String level,
            int currentStep,
            int currentReverseStep,
            boolean allowSameMonthGradeIncrement
    ) {
        if (LEVEL_PROMOTION_PREFIXES.contains(postPrefix)) {
            Integer highestLevel = highestLevelForPost(postCode);
            int qualifiedLevelYears = qualifiedAssessmentYears(orgCode, personNo, levelAssessmentStartYear, targetYear);
            boolean hasLevelBoundary = highestLevel != null;
            boolean canPromoteLevel = hasLevelBoundary && parseInt(level) > highestLevel;
            boolean alreadyAtHighestLevel = hasLevelBoundary && parseInt(level) <= highestLevel;
            if (qualifiedLevelYears >= 5 && canPromoteLevel) {
                NormalGradeState promoted = nextLevelState(standardYear, postPrefix, level, currentStep);
                if (allowSameMonthGradeIncrement && qualifiedAssessmentYears(orgCode, personNo, gradeAssessmentStartYear, targetYear) >= 2) {
                    NormalGradeState incremented = nextGradeState(
                            standardYear,
                            postPrefix,
                            promoted.level(),
                            promoted.step(),
                            promoted.reverseStep()
                    );
                    return new NormalGradeState(
                            incremented.level(),
                            incremented.step(),
                            incremented.reverseStep(),
                            incremented.amount(),
                            promoted.ruleNote() + "\uff1b\u540c\u6708\u664b\u6863\uff1a\u6309 58 \u53f7\u6587\u5148\u664b\u5347\u7ea7\u522b\uff0c\u518d\u664b\u5347\u7ea7\u522b\u5de5\u8d44\u6863\u6b21\uff1b" + incremented.ruleNote()
                    );
                }
                return promoted;
            }
            if (qualifiedLevelYears >= 5 && alreadyAtHighestLevel) {
                return nextHighestLevelGradeState(standardYear, postPrefix, level, currentStep, currentReverseStep);
            }
        }
        if (WORKER_PREFIXES.contains(postPrefix)) {
            if (qualifiedAssessmentYears(orgCode, personNo, gradeAssessmentStartYear, targetYear) < 2) {
                throw new BusinessException("PROMOTION_NOT_ELIGIBLE", "Not enough qualified assessment years for worker post-grade increment.");
            }
            return nextWorkerPostGradeState(standardYear, postCode, currentStep, currentReverseStep);
        }
        if ("03".equals(postPrefix)) {
            if (qualifiedAssessmentYears(orgCode, personNo, gradeAssessmentStartYear, targetYear) < 2) {
                throw new BusinessException("PROMOTION_NOT_ELIGIBLE", "Not enough qualified assessment years for judicial grade increment.");
            }
            return nextJudicialPostGradeState(standardYear, postCode, currentStep, currentReverseStep);
        }
        if (GRADE_PREFIXES.contains(postPrefix)) {
            if (qualifiedAssessmentYears(orgCode, personNo, gradeAssessmentStartYear, targetYear) < 2) {
                throw new BusinessException("PROMOTION_NOT_ELIGIBLE", "Not enough qualified assessment years for grade increment.");
            }
            return nextGradeState(standardYear, postPrefix, level, currentStep, currentReverseStep);
        }
        if (SALARY_GRADE_PREFIXES.contains(postPrefix)) {
            if (qualifiedAssessmentYears(orgCode, personNo, gradeAssessmentStartYear, targetYear) < 1) {
                throw new BusinessException("PROMOTION_NOT_ELIGIBLE", "Not enough qualified assessment years for salary-grade increment.");
            }
            return nextSalaryGradeState(standardYear, orgCode, postCode, currentStep, currentReverseStep);
        }
        throw new BusinessException("UNSUPPORTED_POST_PREFIX", "Unsupported zwbm2 prefix for normal promotion: " + postPrefix);
    }

    private NormalGradeState nextLevelState(String standardYear, String postPrefix, String currentLevel, int currentStep) {
        int targetLevel = parseInt(currentLevel) - 1;
        int targetStep = promotedLevelStep(standardYear, postPrefix, currentLevel, currentStep, targetLevel);
        BigDecimal amount = lookupGradeSalaryColumn(standardYear, postPrefix, String.valueOf(targetLevel), targetStep);
        return new NormalGradeState(
                String.valueOf(targetLevel),
                targetStep,
                0,
                amount,
                "\u7ea7\u522b\u664b\u5347\uff1a\u7d2f\u8ba1 5 \u5e74\u8003\u6838\u79f0\u804c\u53ca\u4ee5\u4e0a\uff0c\u664b\u5347 1 \u4e2a\u7ea7\u522b\uff0c\u6807\u51c6\u8868 " + standardTable(postPrefix)
        );
    }

    private NormalGradeState nextHighestLevelGradeState(
            String standardYear,
            String postPrefix,
            String level,
            int currentStep,
            int currentReverseStep
    ) {
        NormalGradeState incremented = nextGradeState(standardYear, postPrefix, level, currentStep, currentReverseStep);
        return new NormalGradeState(
                incremented.level(),
                incremented.step(),
                incremented.reverseStep(),
                incremented.amount(),
                "\u6700\u9ad8\u7ea7\u522b\u8f6c\u6863\uff1a\u5df2\u8fbe\u5230\u6240\u4efb\u804c\u52a1\u6700\u9ad8\u7ea7\u522b\uff0c\u7d2f\u8ba1 5 \u5e74\u8003\u6838\u79f0\u804c\u53ca\u4ee5\u4e0a\uff0c\u4e0d\u518d\u664b\u5347\u7ea7\u522b\uff0c\u6539\u5728\u672c\u7ea7\u522b\u5185\u664b\u5347 1 \u4e2a\u5de5\u8d44\u6863\u6b21\uff1b" + incremented.ruleNote()
        );
    }

    private NormalGradeState nextGradeState(String standardYear, String postPrefix, String level, int currentStep, int currentReverseStep) {
        int nextStep = currentStep + 1;
        BigDecimal nextStepAmount = lookupGradeSalaryOrZero(standardYear, postPrefix, level, nextStep);
        if (nextStepAmount.compareTo(BigDecimal.ZERO) > 0) {
            return new NormalGradeState(
                    level,
                    nextStep,
                    0,
                    nextStepAmount,
                    "\u664b\u6863\uff1a\u7d2f\u8ba1 2 \u5e74\u8003\u6838\u79f0\u804c\u53ca\u4ee5\u4e0a\uff0c\u4e0b\u4e00\u6863\u6709\u6807\u51c6\uff0czwgzdc2 +1\uff0cdjc2 \u5f52 0\uff0c\u6807\u51c6\u8868 " + standardTable(postPrefix)
            );
        }

        int nextReverseStep = currentReverseStep + 1;
        BigDecimal currentAmount = lookupGradeSalaryColumn(standardYear, postPrefix, level, currentStep);
        BigDecimal previousAmount = lookupGradeSalaryColumn(standardYear, postPrefix, level, currentStep - 1);
        BigDecimal stepDifference = currentAmount.subtract(previousAmount);
        BigDecimal amount = currentAmount.add(stepDifference.multiply(BigDecimal.valueOf(nextReverseStep)));
        return new NormalGradeState(
                level,
                currentStep,
                nextReverseStep,
                amount,
                "\u664b\u6863\uff1a\u7d2f\u8ba1 2 \u5e74\u8003\u6838\u79f0\u804c\u53ca\u4ee5\u4e0a\uff0c\u4e0b\u4e00\u6863\u65e0\u6807\u51c6\uff0czwgzdc2 \u4e0d\u53d8\uff0cdjc2 +1\uff0c\u6309\u6863\u5dee\u8ba1\u7b97\uff0c\u6807\u51c6\u8868 " + standardTable(postPrefix)
        );
    }

    private NormalGradeState nextJudicialPostGradeState(String standardYear, String postCode, int currentStep, int currentReverseStep) {
        int nextStep = currentStep + 1;
        BigDecimal nextStepAmount = lookupJudicialPostSalaryOrZero(standardYear, postCode, nextStep);
        if (nextStepAmount.compareTo(BigDecimal.ZERO) > 0) {
            return new NormalGradeState(
                    trim(postCode),
                    nextStep,
                    0,
                    nextStepAmount,
                    "\u6cd5\u5b98/\u68c0\u5bdf\u5b98\u7b49\u7ea7\u5de5\u8d44\u664b\u6863\uff1a\u7d2f\u8ba1 2 \u5e74\u8003\u6838\u79f0\u804c\u53ca\u4ee5\u4e0a\uff0c\u4e0b\u4e00\u6863\u6709\u6807\u51c6\uff0czwgzdc2 +1\uff0cdjc2 \u5f52 0\uff0c\u6807\u51c6\u8868 bz06_zwgz_fj"
            );
        }

        int nextReverseStep = currentReverseStep + 1;
        BigDecimal currentAmount = lookupJudicialPostSalary(standardYear, postCode, currentStep);
        BigDecimal previousAmount = lookupJudicialPostSalary(standardYear, postCode, currentStep - 1);
        BigDecimal stepDifference = currentAmount.subtract(previousAmount);
        BigDecimal amount = currentAmount.add(stepDifference.multiply(BigDecimal.valueOf(nextReverseStep)));
        return new NormalGradeState(
                trim(postCode),
                currentStep,
                nextReverseStep,
                amount,
                "\u6cd5\u5b98/\u68c0\u5bdf\u5b98\u7b49\u7ea7\u5de5\u8d44\u664b\u6863\uff1a\u7d2f\u8ba1 2 \u5e74\u8003\u6838\u79f0\u804c\u53ca\u4ee5\u4e0a\uff0c\u4e0b\u4e00\u6863\u65e0\u6807\u51c6\uff0czwgzdc2 \u4e0d\u53d8\uff0cdjc2 +1\uff0c\u6309\u6863\u5dee\u8ba1\u7b97\uff0c\u6807\u51c6\u8868 bz06_zwgz_fj"
        );
    }

    private NormalGradeState nextWorkerPostGradeState(String standardYear, String postCode, int currentStep, int currentReverseStep) {
        int nextStep = currentStep + 1;
        BigDecimal nextStepAmount = lookupWorkerPostSalaryOrZero(standardYear, postCode, nextStep);
        if (nextStepAmount.compareTo(BigDecimal.ZERO) > 0) {
            return new NormalGradeState(
                    "",
                    nextStep,
                    0,
                    nextStepAmount,
                    "\u673a\u5173\u5de5\u4eba\u5c97\u4f4d\u5de5\u8d44\u664b\u6863\uff1a\u7d2f\u8ba1 2 \u5e74\u8003\u6838\u5408\u683c\u53ca\u4ee5\u4e0a\uff0c\u4e0b\u4e00\u6863\u6709\u6807\u51c6\uff0czwgzdc2 +1\uff0cdjc2 \u5f52 0\uff0c\u6807\u51c6\u8868 bz06_zwgz_gr"
            );
        }

        int nextReverseStep = currentReverseStep + 1;
        BigDecimal currentAmount = lookupWorkerPostSalaryColumn(standardYear, postCode, currentStep);
        BigDecimal previousAmount = lookupWorkerPostSalaryColumn(standardYear, postCode, currentStep - 1);
        BigDecimal stepDifference = currentAmount.subtract(previousAmount);
        BigDecimal amount = currentAmount.add(stepDifference.multiply(BigDecimal.valueOf(nextReverseStep)));
        return new NormalGradeState(
                "",
                currentStep,
                nextReverseStep,
                amount,
                "\u673a\u5173\u5de5\u4eba\u5c97\u4f4d\u5de5\u8d44\u664b\u6863\uff1a\u7d2f\u8ba1 2 \u5e74\u8003\u6838\u5408\u683c\u53ca\u4ee5\u4e0a\uff0c\u4e0b\u4e00\u6863\u65e0\u6807\u51c6\uff0czwgzdc2 \u4e0d\u53d8\uff0cdjc2 +1\uff0c\u6309\u6863\u5dee\u8ba1\u7b97\uff0c\u6807\u51c6\u8868 bz06_zwgz_gr"
        );
    }

    private NormalGradeState nextSalaryGradeState(String standardYear, String orgCode, String postCode, int currentStep, int currentReverseStep) {
        String salaryPostCode = regularizationEducationPostCode(orgCode, standardYear, postCode);
        String salaryPostPrefix = postPrefix(salaryPostCode);
        int nextStep = currentStep + 1;
        BigDecimal nextStepAmount = lookupSalaryGradeByPrefixOrZero(standardYear, salaryPostPrefix, nextStep);
        if (nextStepAmount.compareTo(BigDecimal.ZERO) > 0) {
            return new NormalGradeState(
                    "",
                    nextStep,
                    0,
                    nextStepAmount,
                    "\u4e8b\u4e1a\u4eba\u5458\u664b\u5347\u85aa\u7ea7\uff1a\u7d2f\u8ba1 1 \u5e74\u8003\u6838\u79f0\u804c\u53ca\u4ee5\u4e0a\uff0c\u4e0b\u4e00\u85aa\u7ea7\u6709\u6807\u51c6\uff0czwgzdc2 +1\uff0cdjc2 \u5f52 0\uff0c\u6807\u51c6\u8868 bz06_xjgz"
            );
        }

        int nextReverseStep = currentReverseStep + 1;
        BigDecimal currentAmount = lookupSalaryGradeByPrefix(standardYear, salaryPostPrefix, currentStep);
        BigDecimal stepDifference = lookupSalaryGradeDifference(standardYear, salaryPostPrefix);
        BigDecimal amount = currentAmount.add(stepDifference.multiply(BigDecimal.valueOf(nextReverseStep)));
        return new NormalGradeState(
                "",
                currentStep,
                nextReverseStep,
                amount,
                "\u4e8b\u4e1a\u4eba\u5458\u664b\u5347\u85aa\u7ea7\uff1a\u7d2f\u8ba1 1 \u5e74\u8003\u6838\u79f0\u804c\u53ca\u4ee5\u4e0a\uff0c\u4e0b\u4e00\u85aa\u7ea7\u65e0\u6807\u51c6\uff0czwgzdc2 \u4e0d\u53d8\uff0cdjc2 +1\uff0c\u6309\u85aa\u7ea7\u5dee\u8ba1\u7b97\uff0c\u6807\u51c6\u8868 bz06_xjgz"
        );
    }

    private BigDecimal lookupGradeSalaryColumn(String standardYear, String postPrefix, String level, int step) {
        int maxStep = maxGradeSalaryStep(postPrefix);
        if (step < 1 || step > maxStep) {
            throw new BusinessException("GRADE_OUT_OF_RANGE", "Step is out of " + standardTable(postPrefix) + " dc1-dc" + maxStep + " range: " + step);
        }
        String column = "dc" + step;
        String tableName = standardTable(postPrefix);
        List<Number> values = jdbcTemplate.queryForList(
                "SELECT " + column + " FROM " + tableName + " WHERE tbnd = ? AND CAST(TRIM(jb) AS UNSIGNED) = ? LIMIT 1",
                Number.class,
                standardYear,
                parseInt(level)
        );
        if (values.isEmpty()) {
            throw new BusinessException("SALARY_STANDARD_NOT_FOUND", "No " + tableName + " row for level " + level + ", tbnd=" + standardYear);
        }
        return values.get(0) == null ? BigDecimal.ZERO : BigDecimal.valueOf(values.get(0).longValue());
    }

    private BigDecimal lookupGradeSalaryOrZero(String standardYear, String postPrefix, String level, int step) {
        if (step < 1 || step > maxGradeSalaryStep(postPrefix)) {
            return BigDecimal.ZERO;
        }
        return lookupGradeSalaryColumn(standardYear, postPrefix, level, step);
    }

    private int maxGradeSalaryStep(String postPrefix) {
        return RANK_PREFIXES.contains(postPrefix) ? 14 : 20;
    }

    private BigDecimal lookupWorkerPostSalaryColumn(String standardYear, String postCode, int step) {
        if (step < 1 || step > 20) {
            throw new BusinessException("WORKER_POST_GRADE_OUT_OF_RANGE", "Worker post grade is out of bz06_zwgz_gr dc1-dc20 range: " + step);
        }
        String column = "dc" + step;
        List<Number> values = jdbcTemplate.queryForList(
                "SELECT " + column + " FROM bz06_zwgz_gr WHERE tbnd = ? AND zwbm = ? LIMIT 1",
                Number.class,
                standardYear,
                trim(postCode)
        );
        if (values.isEmpty()) {
            throw new BusinessException("SALARY_STANDARD_NOT_FOUND", "No bz06_zwgz_gr row for post code " + postCode + ", tbnd=" + standardYear);
        }
        return values.get(0) == null ? BigDecimal.ZERO : BigDecimal.valueOf(values.get(0).longValue());
    }

    private BigDecimal lookupWorkerPostSalaryOrZero(String standardYear, String postCode, int step) {
        if (step < 1 || step > 20) {
            return BigDecimal.ZERO;
        }
        return lookupWorkerPostSalaryColumn(standardYear, postCode, step);
    }

    private BigDecimal lookupWorkerPostSalaryForCompare(String standardYear, String postCode, int step, int reverseStep) {
        BigDecimal amount = lookupWorkerPostSalaryColumn(standardYear, postCode, step);
        if (reverseStep <= 0) {
            return amount;
        }
        BigDecimal previousAmount = lookupWorkerPostSalaryColumn(standardYear, postCode, step - 1);
        return amount.add(amount.subtract(previousAmount));
    }

    private BigDecimal lookupTechnicalGradeSalary(String standardYear, String postCode) {
        List<Number> values = jdbcTemplate.queryForList(
                "SELECT jsdjgz FROM bz06_zwgz_gr WHERE tbnd = ? AND zwbm = ? LIMIT 1",
                Number.class,
                standardYear,
                trim(postCode)
        );
        if (values.isEmpty()) {
            throw new BusinessException("SALARY_STANDARD_NOT_FOUND", "No bz06_zwgz_gr technical-grade row for post code " + postCode + ", tbnd=" + standardYear);
        }
        return values.get(0) == null ? BigDecimal.ZERO : BigDecimal.valueOf(values.get(0).longValue());
    }

    private BigDecimal lookupPostSalary(String standardYear, String postCode) {
        String normalizedPostCode = normalizePostSalaryCode(postCode);
        List<Number> values = jdbcTemplate.queryForList(
                "SELECT bz FROM bz06_zwgz WHERE tbnd = ? AND zwbm = ? LIMIT 1",
                Number.class,
                standardYear,
                normalizedPostCode
        );
        if (values.isEmpty()) {
            throw new BusinessException("SALARY_STANDARD_NOT_FOUND", "No bz06_zwgz row for post code " + normalizedPostCode + ", tbnd=" + standardYear);
        }
        return values.get(0) == null ? BigDecimal.ZERO : BigDecimal.valueOf(values.get(0).longValue());
    }

    private BigDecimal regularizationPostSalary(String orgCode, String standardYear, String postCode, int targetStep) {
        String postPrefix = postPrefix(postCode);
        if ("03".equals(postPrefix)) {
            return lookupJudicialPostSalary(standardYear, postCode, targetStep);
        }
        if (WORKER_PREFIXES.contains(postPrefix)) {
            return lookupWorkerPostSalaryColumn(standardYear, postCode, targetStep);
        }
        return lookupPostSalary(standardYear, regularizationEducationPostCode(orgCode, standardYear, postCode));
    }

    private BigDecimal regularizationLevelSalary(String orgCode, String standardYear, String postCode, Map<String, Object> expectedRow) {
        String postPrefix = postPrefix(postCode);
        if ("03".equals(postPrefix) || WORKER_PREFIXES.contains(postPrefix)) {
            return BigDecimal.ZERO;
        }
        int targetStep = parseInt(expectedRow.get("zwgzdc2")) + parseInt(expectedRow.get("djc2"));
        if (SALARY_GRADE_PREFIXES.contains(postPrefix)) {
            String salaryPrefix = postPrefix(regularizationEducationPostCode(orgCode, standardYear, postCode));
            return lookupSalaryGradeByPrefix(standardYear, salaryPrefix, targetStep);
        }
        return lookupCivilLevelSalary(standardYear, postPrefix, trim(expectedRow.get("jbgzjb2")), targetStep);
    }

    private BigDecimal regularizationLevelSalary(String orgCode, String standardYear, String postCode, String level, int step) {
        String postPrefix = postPrefix(postCode);
        if ("03".equals(postPrefix) || WORKER_PREFIXES.contains(postPrefix)) {
            return BigDecimal.ZERO;
        }
        if (SALARY_GRADE_PREFIXES.contains(postPrefix)) {
            String salaryPrefix = postPrefix(regularizationEducationPostCode(orgCode, standardYear, postCode));
            return lookupSalaryGradeByPrefix(standardYear, salaryPrefix, step);
        }
        return lookupCivilLevelSalary(standardYear, postPrefix, level, step);
    }

    private String regularizationEducationPostCode(String orgCode, String standardYear, String postCode) {
        String text = trim(postCode);
        if (text.startsWith("10")
                && standardYear.compareTo("201807") >= 0
                && educationOrgType(orgCode) == 2
                && text.length() >= 4) {
            return "11" + text.substring(2);
        }
        return text;
    }

    private BigDecimal regularizationDerivedAmountFromStandardOrRow(String itemCode, String postCode, Map<String, Object> expectedRow) {
        String normalizedCode = itemCode.toUpperCase();
        if (!JBT_ALLOWANCE_ITEMS.contains(normalizedCode)) {
            return number(expectedRow.get(itemCode.toLowerCase()));
        }
        String allowanceYear = trim(expectedRow.get("jbtbz"));
        if (!StringUtils.hasText(allowanceYear)) {
            return number(expectedRow.get(itemCode.toLowerCase()));
        }
        BigDecimal standardAmount = lookupAllowanceStandardOrNull(allowanceYear, postCode, normalizedCode);
        return standardAmount == null ? number(expectedRow.get(itemCode.toLowerCase())) : standardAmount;
    }

    private BigDecimal lookupAllowanceStandardOrNull(String allowanceYear, String postCode, String itemCode) {
        String column = itemCode.toLowerCase();
        String normalizedPostCode = normalizeAllowancePostCode(postCode);
        List<Number> values = jdbcTemplate.queryForList(
                "SELECT " + column + " FROM jbtbz WHERE tbnd = ? AND bm = ? LIMIT 1",
                Number.class,
                allowanceYear,
                normalizedPostCode
        );
        if (values.isEmpty()) {
            values = jdbcTemplate.queryForList(
                    "SELECT " + column + " FROM jbtbz WHERE tbnd = ? AND bm = ? LIMIT 1",
                    Number.class,
                    allowanceYear,
                    trim(postCode)
            );
        }
        if (values.isEmpty()) {
            return null;
        }
        return values.get(0) == null ? BigDecimal.ZERO : BigDecimal.valueOf(values.get(0).longValue());
    }

    private String normalizeAllowancePostCode(String postCode) {
        String text = trim(postCode);
        if (text.length() >= 4 && text.compareTo("10") > 0) {
            return "10" + text.substring(text.length() - 2);
        }
        return text;
    }

    private void addRegularizationEducationPlacementChange(
            List<SalaryRuleChange> changes,
            RegularizationPlacement placement,
            Map<String, Object> baselineRow,
            Map<String, Object> expectedRow,
            String ruleNote
    ) {
        if (placement.educationPlacement() == null) {
            return;
        }
        changes.add(new SalaryRuleChange(
                "ZZDJ06",
                "\u5b66\u5386\u8f6c\u6b63\u5b9a\u7ea7\u6807\u51c6",
                trim(baselineRow.get("zgxl")),
                placement.describeTarget(expectedRow),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "\u975e\u91d1\u989d\u6821\u9a8c\uff1a\u6309\u65e7\u7cfb\u7edf zzdj06.prg \u53e3\u5f84\uff0c\u7528 xlbm \u67e5 bz06_zzdz \u63a8\u5bfc\u8f6c\u6b63\u5b9a\u7ea7\u5c97\u4f4d\u3001\u7ea7\u522b/\u85aa\u7ea7\u548c\u6863\u6b21\uff1b" + ruleNote
        ));
    }

    private RegularizationPlacement resolveRegularizationPlacement(
            NormalGradeTrialCommand command,
            String standardYear,
            Map<String, Object> row
    ) {
        String targetPostCode = trim(row.get("zwbm2"));
        String targetPrefix = postPrefix(targetPostCode);
        RegularizationEducationPlacement educationPlacement = inferRegularizationEducationPlacement(standardYear, targetPostCode, row);
        int targetStep = parseInt(row.get("zwgzdc2")) + parseInt(row.get("djc2"));
        if (targetStep == 0) {
            targetStep = parseInt(row.get("zwgzdc2"));
        }
        String educationLevel = educationPlacement == null ? trim(row.get("jbgzjb2")) : educationPlacement.level();
        int educationStep = educationPlacement == null ? targetStep : educationPlacement.step();

        if (SALARY_GRADE_PREFIXES.contains(targetPrefix)) {
            return new RegularizationPlacement(
                    targetPostCode,
                    "",
                    educationStep,
                    "\u4e8b\u4e1a\u4eba\u5458\u8f6c\u6b63\u5b9a\u7ea7\u6309\u5b66\u5386\u786e\u5b9a\u85aa\u7ea7",
                    educationPlacement
            );
        }

        String appointmentPostCode = regularizationAppointmentPostCode(command, targetPostCode);
        if (StringUtils.hasText(appointmentPostCode)) {
            return new RegularizationPlacement(
                    appointmentPostCode,
                    educationLevel,
                    educationStep,
                    "\u8f6c\u6b63\u65f6\u4efb\u804c\u4fe1\u606f\u786e\u5b9a\u804c\u52a1\uff0c\u5b66\u5386\u6807\u51c6\u786e\u5b9a\u7ea7\u522b/\u6863\u6b21",
                    educationPlacement
            );
        }

        String educationPostCode = educationPlacement == null ? targetPostCode : educationPlacement.standardPostCode();
        return new RegularizationPlacement(
                StringUtils.hasText(educationPostCode) ? educationPostCode : targetPostCode,
                educationLevel,
                educationStep,
                "\u65e0\u8f6c\u6b63\u65f6\u4efb\u804c\u4fe1\u606f\uff0c\u6309\u5b66\u5386\u6807\u51c6\u786e\u5b9a\u804c\u52a1\u3001\u7ea7\u522b/\u85aa\u7ea7\u548c\u6863\u6b21",
                educationPlacement
        );
    }

    private String regularizationAppointmentPostCode(NormalGradeTrialCommand command, String targetPostCode) {
        String targetPrefix = postPrefix(targetPostCode);
        if (SALARY_GRADE_PREFIXES.contains(targetPrefix)) {
            return "";
        }
        PersonCodeParts parts = personCodeParser.parse(command.personCode(), command.orgCode());
        String targetMonth = "%04d.%02d".formatted(command.year(), command.month());
        List<String> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(zwbm)
                FROM dryzwbh
                WHERE dwbm = ?
                  AND grbm = ?
                  AND LEFT(TRIM(zwbm), 2) = ?
                  AND REPLACE(TRIM(srny), '.', '') = ?
                  AND TRIM(COALESCE(zwbm, '')) <> ''
                ORDER BY srny DESC, zwbm
                LIMIT 1
                """, String.class, parts.orgCode(), parts.personNo(), targetPrefix, "%04d%02d".formatted(command.year(), command.month()));
        if (!rows.isEmpty() && StringUtils.hasText(rows.get(0))) {
            return rows.get(0).trim();
        }
        List<String> judicialRows = jdbcTemplate.queryForList("""
                SELECT TRIM(zjbm)
                FROM jdzw
                WHERE dwbm = ?
                  AND grbm = ?
                  AND LEFT(TRIM(zjbm), 2) = ?
                  AND REPLACE(TRIM(rzsj), '.', '') = ?
                  AND TRIM(COALESCE(zjbm, '')) <> ''
                ORDER BY rzsj DESC, zjbm
                LIMIT 1
                """, String.class, parts.orgCode(), parts.personNo(), targetPrefix, "%04d%02d".formatted(command.year(), command.month()));
        return judicialRows.isEmpty() ? "" : trim(judicialRows.get(0));
    }

    private RegularizationEducationPlacement inferRegularizationEducationPlacement(String standardYear, String postCode, Map<String, Object> row) {
        String educationCode = trim(row.get("xlbm"));
        if (!StringUtils.hasText(educationCode)) {
            return null;
        }
        String lookupPostCode = regularizationEducationLookupPostCode(postCode);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT zzzwbm, zzjb, zzdc
                FROM bz06_zzdz
                WHERE LEFT(zzzwbm, 2) = LEFT(?, 2)
                  AND xlbm = ?
                ORDER BY tbnd DESC
                LIMIT 1
                """, lookupPostCode, educationCode);
        if (rows.isEmpty()) {
            return null;
        }
        String standardPostCode = normalizeRegularizationEducationPostCode(trim(rows.get(0).get("zzzwbm")));
        int step = parseInt(rows.get(0).get("zzdc")) + regularizationAdditionalStep(lookupPostCode, educationCode);
        return new RegularizationEducationPlacement(
                standardPostCode,
                trim(rows.get(0).get("zzjb")),
                step,
                educationCode,
                standardYear
        );
    }

    private String regularizationEducationLookupPostCode(String postCode) {
        String text = trim(postCode);
        String prefix = postPrefix(text);
        if (text.compareTo("10") > 0 && !Set.of("21", "22", "23", "24", "25", "26", "27", "28").contains(prefix)) {
            text = "10" + text.substring(2);
            prefix = postPrefix(text);
        }
        if ("03".equals(prefix) || "04".equals(prefix)) {
            text = "01" + text.substring(2);
        }
        return text;
    }

    private String normalizeRegularizationEducationPostCode(String postCode) {
        String text = trim(postCode);
        if (text.startsWith("07") && text.length() >= 4 && "0".equals(text.substring(3, 4))) {
            return "070" + text.substring(2, 3);
        }
        return text;
    }

    private int regularizationAdditionalStep(String lookupPostCode, String educationCode) {
        if (educationCode.compareTo("60") >= 0) {
            return 0;
        }
        RegularizationPolicy policy = regularizationPolicy();
        if (policy.additionalStep() <= 0) {
            return 0;
        }
        String prefix = postPrefix(lookupPostCode);
        if (policy.includeWorkerAdditionalStep() || !Set.of("05", "06", "08", "09").contains(prefix)) {
            return policy.additionalStep();
        }
        return 0;
    }

    private RegularizationPolicy regularizationPolicy() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT jqdm, zzrs
                FROM cyxx
                ORDER BY id DESC
                LIMIT 1
                """);
        if (rows.isEmpty()) {
            return new RegularizationPolicy(0, false);
        }
        int jqdm = parseInt(rows.get(0).get("jqdm"));
        int additionalStep = Math.max(0, jqdm - 1);
        int flags = parseInt(rows.get(0).get("zzrs"));
        return new RegularizationPolicy(additionalStep, (flags & 2) != 0);
    }

    private int educationOrgType(String orgCode) {
        List<Number> values = jdbcTemplate.queryForList(
                "SELECT jxlb FROM dwbm WHERE dwbm = ? LIMIT 1",
                Number.class,
                trim(orgCode)
        );
        if (values.isEmpty() || values.get(0) == null) {
            return 0;
        }
        return values.get(0).intValue();
    }

    private BigDecimal lookupProbationarySalary(String standardYear, String postCode, String educationCode) {
        if (!trim(postCode).contains("F")) {
            return BigDecimal.ZERO;
        }
        if (!StringUtils.hasText(educationCode)) {
            throw new BusinessException("EDUCATION_NOT_FOUND", "Probationary salary requires xlbm education code.");
        }
        String standardPrefix = probationaryStandardPrefix(postCode);
        List<Number> values = jdbcTemplate.queryForList("""
                SELECT gz1
                FROM bz06_zzdz
                WHERE tbnd = ?
                  AND LEFT(zzzwbm, 2) = ?
                  AND xlbm = ?
                LIMIT 1
                """, Number.class, standardYear, standardPrefix, educationCode);
        if (values.isEmpty()) {
            throw new BusinessException("SALARY_STANDARD_NOT_FOUND", "No bz06_zzdz probationary salary for prefix " + standardPrefix + ", xlbm=" + educationCode + ", tbnd=" + standardYear);
        }
        return values.get(0) == null ? BigDecimal.ZERO : BigDecimal.valueOf(values.get(0).longValue());
    }

    private String resolveProbationaryEducationCode(Map<String, Object> row, int targetYear, int targetMonth) {
        String targetYearMonth = "%04d%02d".formatted(targetYear, targetMonth);
        List<String> values = jdbcTemplate.queryForList("""
                SELECT TRIM(xlbm)
                FROM dxl
                WHERE dwbm = ? AND grbm = ?
                  AND TRIM(COALESCE(xlbm, '')) <> ''
                  AND TRIM(COALESCE(xllb, '')) NOT IN ('\u5176\u5b83', '\u5176\u4ed6')
                  AND TRIM(COALESCE(bysj, '')) <> ''
                  AND REPLACE(bysj, '.', '') <= ?
                ORDER BY xlbm, bysj DESC
                LIMIT 1
                """, String.class, trim(row.get("dwbm")), trim(row.get("grbm")), targetYearMonth);
        if (!values.isEmpty()) {
            return trim(values.get(0));
        }
        return trim(row.get("xlbm"));
    }

    private String probationaryEducationValue(Map<String, Object> row) {
        return probationaryEducationValue(row, trim(row.get("xlbm")));
    }

    private String probationaryEducationValue(Map<String, Object> row, String educationCode) {
        String educationName = trim(row.get("zgxl"));
        if (StringUtils.hasText(educationName) && StringUtils.hasText(educationCode)) {
            return educationCode + "/" + educationName;
        }
        if (StringUtils.hasText(educationName)) {
            return educationName;
        }
        return educationCode;
    }

    private String probationaryStandardPrefix(String postCode) {
        String prefix = postPrefix(postCode);
        if (Set.of("01", "02", "21", "22", "23", "24", "25", "26", "27", "28").contains(prefix)) {
            return "01";
        }
        return trim(postCode).compareTo("10") > 0 ? "10" : prefix;
    }

    private String resolvePoliceRankAllowanceYear(Map<String, Object> expectedRow, int targetYear) {
        String tbnd = trim(expectedRow.get("jxjtbz"));
        if (StringUtils.hasText(tbnd)) {
            return tbnd;
        }
        return resolveStandardYear(expectedRow, targetYear, "jxjtbz");
    }

    private BigDecimal lookupPoliceRankAllowance(String standardYear, String rankName) {
        if (!StringUtils.hasText(rankName)) {
            return BigDecimal.ZERO;
        }
        List<Number> values = jdbcTemplate.queryForList(
                "SELECT jtbz FROM jxjtbz WHERE tbnd = ? AND jx = ? LIMIT 1",
                Number.class,
                standardYear,
                rankName
        );
        if (values.isEmpty()) {
            throw new BusinessException("SALARY_STANDARD_NOT_FOUND", "No jxjtbz row for rank " + rankName + ", tbnd=" + standardYear);
        }
        return values.get(0) == null ? BigDecimal.ZERO : BigDecimal.valueOf(values.get(0).longValue());
    }

    private BigDecimal lookupJudicialPostSalary(String standardYear, String postCode, int step) {
        if (step < 1 || step > 17) {
            throw new BusinessException("JUDICIAL_POST_GRADE_OUT_OF_RANGE", "Judicial post grade is out of bz06_zwgz_fj dc1-dc17 range: " + step);
        }
        String text = trim(postCode);
        String standardPostCode = text.length() >= 4 ? postPrefix(text) + "0" + text.substring(3) : text;
        List<Number> values = jdbcTemplate.queryForList(
                "SELECT dc" + step + " FROM bz06_zwgz_fj WHERE tbnd = ? AND zwbm = ? LIMIT 1",
                Number.class,
                standardYear,
                standardPostCode
        );
        if (values.isEmpty()) {
            throw new BusinessException("SALARY_STANDARD_NOT_FOUND", "No bz06_zwgz_fj row for post code " + standardPostCode + ", tbnd=" + standardYear);
        }
        return values.get(0) == null ? BigDecimal.ZERO : BigDecimal.valueOf(values.get(0).longValue());
    }

    private BigDecimal lookupJudicialPostSalaryOrZero(String standardYear, String postCode, int step) {
        if (step < 1 || step > 17) {
            return BigDecimal.ZERO;
        }
        return lookupJudicialPostSalary(standardYear, postCode, step);
    }

    private String normalizePostSalaryCode(String postCode) {
        return switch (trim(postCode)) {
            case "0416", "0426" -> "0161";
            case "0417", "0427", "0437" -> "0171";
            case "0418", "0428", "0438" -> "0181";
            case "0419", "0429", "0439" -> "0191";
            case "041A", "042A", "043A" -> "01A1";
            case "041B", "042B", "043B" -> "01B0";
            case "043C" -> "01C0";
            default -> trim(postCode);
        };
    }

    private int promotedWorkerPostStep(
            String standardYear,
            String oldPostCode,
            int oldStep,
            int oldReverseStep,
            String newPostCode,
            boolean includeTechnicalGradeDifference
    ) {
        BigDecimal oldPostAmount = lookupWorkerPostSalaryForCompare(standardYear, oldPostCode, oldStep, oldReverseStep);
        BigDecimal technicalDifference = includeTechnicalGradeDifference
                ? lookupTechnicalGradeSalary(standardYear, newPostCode).subtract(lookupTechnicalGradeSalary(standardYear, oldPostCode))
                : BigDecimal.ZERO;
        for (int step = 1; step <= 20; step++) {
            BigDecimal newPostAmount = lookupWorkerPostSalaryForCompare(standardYear, newPostCode, step, oldReverseStep);
            if (newPostAmount.add(technicalDifference).compareTo(oldPostAmount) > 0) {
                return step;
            }
        }
        throw new BusinessException("SALARY_STANDARD_NOT_FOUND", "No worker promoted post grade found for post code " + newPostCode + ", tbnd=" + standardYear);
    }

    private String workerPostAssessmentStartYear(
            int changeYear,
            int changeMonth,
            String standardYear,
            String oldPostCode,
            int oldStep,
            String newPostCode,
            int newStep,
            BigDecimal technicalDifference,
            boolean shiftOctoberChangeToNextYear
    ) {
        BigDecimal oldNextStepAmount = lookupWorkerPostSalaryOrZero(standardYear, oldPostCode, oldStep + 1);
        if (oldNextStepAmount.compareTo(BigDecimal.ZERO) == 0) {
            return "";
        }
        BigDecimal newComparableAmount = lookupWorkerPostSalaryColumn(standardYear, newPostCode, newStep).add(technicalDifference);
        if (newComparableAmount.compareTo(oldNextStepAmount) <= 0) {
            return "";
        }
        return String.valueOf(changeMonth >= 10 && shiftOctoberChangeToNextYear ? changeYear + 1 : changeYear);
    }

    private WorkerPostChangePolicy workerPostChangePolicy() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT zwbhhjsdj, cdchjsdj, zwbh10
                FROM cyxx
                ORDER BY id DESC
                LIMIT 1
                """);
        if (rows.isEmpty()) {
            return new WorkerPostChangePolicy(false, false);
        }
        Map<String, Object> row = rows.get(0);
        boolean includeTechnicalGradeDifference = checked(row.get("cdchjsdj")) || checked(row.get("zwbhhjsdj"));
        return new WorkerPostChangePolicy(includeTechnicalGradeDifference, checked(row.get("zwbh10")));
    }

    private BigDecimal workerDerivedAllowanceAmount(Map<String, Object> row) {
        return number(row.get("dfbt2")).add(number(row.get("sdbt")));
    }

    private void addDerivedAllowanceChange(
            List<SalaryRuleChange> changes,
            Map<String, BigDecimal> calculatedAmounts,
            String itemCode,
            String itemName,
            Map<String, Object> baselineRow,
            Map<String, Object> expectedRow,
            String ruleNote
    ) {
        BigDecimal beforeAmount = number(baselineRow.get(itemCode.toLowerCase()));
        BigDecimal afterAmount = number(expectedRow.get(itemCode.toLowerCase()));
        if (beforeAmount.compareTo(afterAmount) == 0) {
            return;
        }
        calculatedAmounts.put(itemCode, afterAmount);
        changes.add(new SalaryRuleChange(
                itemCode,
                itemName,
                trim(baselineRow.get("zwbm2")),
                trim(expectedRow.get("zwbm2")),
                normalize(beforeAmount),
                normalize(afterAmount),
                normalize(afterAmount.subtract(beforeAmount)),
                "\u673a\u5173\u6280\u672f\u7b49\u7ea7\u53d8\u52a8\u6d3e\u751f\u91cd\u7b97\uff1a" + ruleNote
        ));
    }

    private void addAssessmentStartYearChange(
            List<SalaryRuleChange> changes,
            String itemCode,
            String itemName,
            Map<String, Object> baselineRow,
            Map<String, Object> expectedRow,
            String ruleNote
    ) {
        String beforeValue = trim(baselineRow.get(itemCode.toLowerCase()));
        String afterValue = trim(expectedRow.get(itemCode.toLowerCase()));
        if (!StringUtils.hasText(beforeValue) && !StringUtils.hasText(afterValue)) {
            return;
        }
        changes.add(new SalaryRuleChange(
                itemCode,
                itemName,
                beforeValue,
                afterValue,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "\u975e\u91d1\u989d\u72b6\u6001\uff1a\u7528\u4e8e\u5224\u65ad\u4e0b\u4e00\u6b21\u6b63\u5e38\u664b\u6863/\u664b\u5347\u85aa\u7ea7\u7684\u8003\u6838\u8d77\u7b97\u5e74\uff1b" + ruleNote
        ));
    }

    private void addCalculatedAssessmentStartYearChange(
            List<SalaryRuleChange> changes,
            String itemCode,
            String itemName,
            Map<String, Object> baselineRow,
            String calculatedAfterValue,
            String ruleNote
    ) {
        String beforeValue = trim(baselineRow.get(itemCode.toLowerCase()));
        String afterValue = trim(calculatedAfterValue);
        if (!StringUtils.hasText(beforeValue) && !StringUtils.hasText(afterValue)) {
            return;
        }
        if (beforeValue.equals(afterValue)) {
            return;
        }
        changes.add(new SalaryRuleChange(
                itemCode,
                itemName,
                beforeValue,
                afterValue,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "\u975e\u91d1\u989d\u72b6\u6001\uff1a\u6839\u636e\u8d85\u6863\u5dee\u89c4\u5219\u63a8\u5bfc\u4e0b\u4e00\u6b21\u6b63\u5e38\u5c97\u4f4d\u664b\u6863\u7684\u8003\u6838\u8d77\u7b97\u5e74\uff1b" + ruleNote
        ));
    }

    private void addAmountChangeIfChanged(
            List<SalaryRuleChange> changes,
            Map<String, BigDecimal> calculatedAmounts,
            String itemCode,
            String itemName,
            Map<String, Object> baselineRow,
            Map<String, Object> expectedRow,
            String ruleNote
    ) {
        BigDecimal beforeAmount = number(baselineRow.get(itemCode.toLowerCase()));
        BigDecimal afterAmount = number(expectedRow.get(itemCode.toLowerCase()));
        if (beforeAmount.compareTo(afterAmount) == 0) {
            return;
        }
        calculatedAmounts.put(itemCode, afterAmount);
        changes.add(new SalaryRuleChange(
                itemCode,
                itemName,
                trim(baselineRow.get("zwbm2")),
                trim(expectedRow.get("zwbm2")),
                normalize(beforeAmount),
                normalize(afterAmount),
                normalize(afterAmount.subtract(beforeAmount)),
                "\u6d3e\u751f\u91cd\u7b97\uff1a" + ruleNote
        ));
    }

    private BigDecimal lookupSalaryGradeColumn(String standardYear, String postCode, int salaryGrade) {
        return lookupSalaryGradeByPrefix(standardYear, postPrefix(postCode), salaryGrade);
    }

    private BigDecimal lookupSalaryGradeByPrefix(String standardYear, String postPrefix, int salaryGrade) {
        if (salaryGrade < 1 || salaryGrade > 99) {
            throw new BusinessException("SALARY_GRADE_OUT_OF_RANGE", "Salary grade is out of supported range: " + salaryGrade);
        }
        List<Number> values = jdbcTemplate.queryForList(
                "SELECT bz FROM bz06_xjgz WHERE tbnd = ? AND gwflbm = ? AND xj = ? LIMIT 1",
                Number.class,
                standardYear,
                postPrefix,
                String.format("%02d", salaryGrade)
        );
        if (values.isEmpty()) {
            throw new BusinessException("SALARY_STANDARD_NOT_FOUND", "No bz06_xjgz row for post prefix " + postPrefix + ", xj=" + salaryGrade + ", tbnd=" + standardYear);
        }
        return values.get(0) == null ? BigDecimal.ZERO : BigDecimal.valueOf(values.get(0).longValue());
    }

    private BigDecimal lookupSalaryGradeOrZero(String standardYear, String postCode, int salaryGrade) {
        return lookupSalaryGradeByPrefixOrZero(standardYear, postPrefix(postCode), salaryGrade);
    }

    private BigDecimal lookupSalaryGradeByPrefixOrZero(String standardYear, String postPrefix, int salaryGrade) {
        if (salaryGrade < 1 || salaryGrade > 99) {
            return BigDecimal.ZERO;
        }
        return lookupSalaryGradeByPrefix(standardYear, postPrefix, salaryGrade);
    }

    private BigDecimal lookupSalaryGradeDifference(String standardYear, String postPrefix) {
        List<Number> values = jdbcTemplate.queryForList("""
                SELECT bz
                FROM bz06_xjgz
                WHERE tbnd = ? AND gwflbm = ?
                ORDER BY bz DESC
                LIMIT 2
                """, Number.class, standardYear, postPrefix);
        if (values.size() < 2 || values.get(0) == null || values.get(1) == null) {
            throw new BusinessException("SALARY_STANDARD_NOT_FOUND", "Cannot calculate bz06_xjgz salary-grade difference for prefix " + postPrefix + ", tbnd=" + standardYear);
        }
        return BigDecimal.valueOf(values.get(0).longValue() - values.get(1).longValue());
    }

    private int promotedLevelStep(String standardYear, String postPrefix, String currentLevel, int currentStep, int targetLevel) {
        BigDecimal currentAmount = lookupGradeSalaryColumn(standardYear, postPrefix, currentLevel, currentStep);
        int maxStep = maxGradeSalaryStep(postPrefix);
        for (int step = 1; step <= maxStep; step++) {
            if (lookupGradeSalaryColumn(standardYear, postPrefix, String.valueOf(targetLevel), step).compareTo(currentAmount) > 0) {
                return step;
            }
        }
        throw new BusinessException("SALARY_STANDARD_NOT_FOUND", "No promoted level step found for level " + targetLevel + ", tbnd=" + standardYear);
    }

    private Integer highestLevelForPost(String postCode) {
        List<Number> values = jdbcTemplate.queryForList(
                "SELECT CAST(TRIM(max) AS UNSIGNED) FROM bz06_zw_jb_xj WHERE zwbm = ? LIMIT 1",
                Number.class,
                trim(postCode)
        );
        if (values.isEmpty() || values.get(0) == null) {
            return null;
        }
        return values.get(0).intValue();
    }

    private int minimumLevelForPost(String postCode) {
        List<Number> values = jdbcTemplate.queryForList(
                "SELECT CAST(TRIM(min) AS UNSIGNED) FROM bz06_zw_jb_xj WHERE zwbm = ? LIMIT 1",
                Number.class,
                trim(postCode)
        );
        if (values.isEmpty() || values.get(0) == null) {
            throw new BusinessException("SALARY_STANDARD_NOT_FOUND", "No bz06_zw_jb_xj minimum level for post code " + postCode);
        }
        return values.get(0).intValue();
    }

    private int minimumSalaryGradeForPost(String postCode) {
        List<Number> values = jdbcTemplate.queryForList(
                "SELECT CAST(TRIM(min) AS UNSIGNED) FROM bz06_zw_jb_xj WHERE zwbm = ? LIMIT 1",
                Number.class,
                trim(postCode)
        );
        if (values.isEmpty() || values.get(0) == null) {
            return 0;
        }
        return values.get(0).intValue();
    }

    private int institutionTargetSalaryGrade(String oldPostCode, String newPostCode, int currentStep) {
        int minimumStep = minimumSalaryGradeForPost(newPostCode);
        if (minimumStep <= 0) {
            return currentStep;
        }
        String oldPrefix = postPrefix(oldPostCode);
        String newPrefix = postPrefix(newPostCode);
        boolean upwardChange;
        if ("07".equals(oldPrefix) && "07".equals(newPrefix)) {
            upwardChange = removeThirdChar(oldPostCode).compareTo(removeThirdChar(newPostCode)) > 0;
        } else if (isInstitutionParallelPostSwitch(oldPrefix, newPrefix)) {
            upwardChange = true;
        } else {
            upwardChange = trim(oldPostCode).compareTo(trim(newPostCode)) > 0;
        }
        if (!upwardChange) {
            return currentStep;
        }
        return currentStep < minimumStep ? minimumStep : currentStep;
    }

    private int judicialTargetStep(String newPostCode, int oldStep) {
        int reduction = JUDICIAL_ONE_STEP_REDUCTION_POSTS.contains(trim(newPostCode)) ? 1 : 2;
        return Math.max(1, oldStep - reduction);
    }

    private boolean isInstitutionParallelPostSwitch(String oldPrefix, String newPrefix) {
        return ("07".equals(oldPrefix) && "10".equals(newPrefix))
                || ("10".equals(oldPrefix) && "07".equals(newPrefix))
                || ("08".equals(oldPrefix) && "09".equals(newPrefix))
                || ("09".equals(oldPrefix) && "08".equals(newPrefix));
    }

    private String removeThirdChar(String value) {
        String text = trim(value);
        return text.length() >= 3 ? text.substring(0, 2) + text.substring(3) : text;
    }

    private CivilPostLevelState civilPostChangeState(
            String standardYear,
            String oldPostCode,
            String newPostCode,
            String oldLevel,
            int oldStep
    ) {
        int oldLevelValue = parseInt(oldLevel);
        if (isCivilPostChangeWithoutLevelChange(oldPostCode, newPostCode)) {
            return new CivilPostLevelState(String.valueOf(oldLevelValue), oldStep);
        }
        int targetLevel;
        if (isCivilUpwardPostChange(oldPostCode, newPostCode)) {
            int minimumLevel = minimumLevelForPost(newPostCode);
            if (oldLevelValue > minimumLevel) {
                targetLevel = minimumLevel;
            } else if (isCivilSameLevelPromotion(oldPostCode, newPostCode)) {
                targetLevel = oldLevelValue;
            } else {
                targetLevel = oldLevelValue - 1;
            }
        } else {
            Integer highestLevel = highestLevelForPost(newPostCode);
            if (highestLevel != null && oldLevelValue < highestLevel) {
                targetLevel = highestLevel;
            } else {
                targetLevel = oldLevelValue;
            }
        }
        int targetStep = targetLevel == oldLevelValue
                ? oldStep
                : civilPromotedLevelStep(standardYear, postPrefix(newPostCode), oldLevelValue, oldStep, targetLevel);
        return new CivilPostLevelState(String.valueOf(targetLevel), targetStep);
    }

    private boolean isCivilPostChangeWithoutLevelChange(String oldPostCode, String newPostCode) {
        String oldPost = trim(oldPostCode);
        String newPost = trim(newPostCode);
        String oldPrefix = postPrefix(oldPost);
        String newPrefix = postPrefix(newPost);
        if (Set.of("01", "02").contains(oldPrefix)
                && Set.of("01", "02").contains(newPrefix)
                && oldPost.length() >= 3
                && newPost.length() >= 3
                && oldPost.substring(0, 3).equals(newPost.substring(0, 3))) {
            return true;
        }
        return Set.of("23", "24", "25", "26", "27", "28").contains(oldPrefix)
                && Set.of("01", "02", "04").contains(newPrefix);
    }

    private boolean isCivilUpwardPostChange(String oldPostCode, String newPostCode) {
        String oldPrefix = postPrefix(oldPostCode);
        String newPrefix = postPrefix(newPostCode);
        if ("01".equals(oldPrefix) || "04".equals(oldPrefix)) {
            return civilAdministrativeRankCode(oldPostCode).compareTo(civilAdministrativeRankCode(newPostCode)) > 0;
        }
        return trim(oldPostCode).compareTo(trim(newPostCode)) > 0;
    }

    private String civilAdministrativeRankCode(String postCode) {
        String text = trim(postCode);
        if (text.length() < 4) {
            return text;
        }
        if (text.startsWith("04")) {
            return text.substring(3, 4) + "0";
        }
        return text.substring(2, 4);
    }

    private boolean isCivilSameLevelPromotion(String oldPostCode, String newPostCode) {
        String oldPost = trim(oldPostCode);
        String newPost = trim(newPostCode);
        if ("0205".equals(oldPost) && "0204".equals(newPost)) {
            return true;
        }
        if (Set.of("2104", "2106", "2108", "2110", "2204", "2206", "2208", "2210").contains(oldPost)) {
            return true;
        }
        if (Set.of("23", "24", "25", "26", "27", "28").contains(postPrefix(oldPost))) {
            String oldRank = oldPost.length() >= 4 ? oldPost.substring(2, 4) : "";
            String newRank = newPost.length() >= 4 ? newPost.substring(2, 4) : "";
            return ("04".equals(oldRank) && "03".equals(newRank))
                    || ("06".equals(oldRank) && "05".equals(newRank))
                    || ("08".equals(oldRank) && "07".equals(newRank))
                    || ("10".equals(oldRank) && "09".equals(newRank));
        }
        return false;
    }

    private int civilPromotedLevelStep(String standardYear, String newPostPrefix, int oldLevel, int oldStep, int targetLevel) {
        BigDecimal previousAmount = lookupCivilLevelSalary(standardYear, newPostPrefix, String.valueOf(oldLevel), oldStep);
        int currentLevel = oldLevel;
        int currentStep = oldStep;
        if (oldLevel > targetLevel) {
            for (int level = oldLevel - 1; level >= targetLevel; level--) {
                int step = 1;
                while (lookupCivilLevelSalary(standardYear, newPostPrefix, String.valueOf(level), step).compareTo(previousAmount) <= 0) {
                    step++;
                    if (step > maxGradeSalaryStep(newPostPrefix) + 10) {
                        throw new BusinessException("SALARY_STANDARD_NOT_FOUND", "No promoted level step found for level " + level + ", tbnd=" + standardYear);
                    }
                }
                previousAmount = lookupCivilLevelSalary(standardYear, newPostPrefix, String.valueOf(level), step);
                currentLevel = level;
                currentStep = step;
            }
        } else if (oldLevel < targetLevel) {
            for (int level = oldLevel + 1; level <= targetLevel; level++) {
                int step = 0;
                while (lookupCivilLevelSalary(standardYear, newPostPrefix, String.valueOf(level), step + 1).compareTo(previousAmount) <= 0) {
                    step++;
                    if (step > maxGradeSalaryStep(newPostPrefix) + 10) {
                        throw new BusinessException("SALARY_STANDARD_NOT_FOUND", "No demoted level step found for level " + level + ", tbnd=" + standardYear);
                    }
                }
                previousAmount = lookupCivilLevelSalary(standardYear, newPostPrefix, String.valueOf(level), step);
                currentLevel = level;
                currentStep = step;
            }
        }
        return currentStep;
    }

    private BigDecimal lookupCivilLevelSalary(String standardYear, String postPrefix, String level, int step) {
        if (step <= 0) {
            return BigDecimal.ZERO;
        }
        int maxStep = maxGradeSalaryStep(postPrefix);
        if (step <= maxStep) {
            return lookupGradeSalaryColumn(standardYear, postPrefix, level, step);
        }
        BigDecimal currentAmount = lookupGradeSalaryColumn(standardYear, postPrefix, level, maxStep);
        BigDecimal previousAmount = lookupGradeSalaryColumn(standardYear, postPrefix, level, maxStep - 1);
        return currentAmount.add(currentAmount.subtract(previousAmount).multiply(BigDecimal.valueOf(step - maxStep)));
    }

    private int qualifiedAssessmentYears(String orgCode, String personNo, String startYear, int targetYear) {
        if (!StringUtils.hasText(startYear)) {
            return 0;
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(DISTINCT khnd)
                FROM dndkh
                WHERE dwbm = ?
                  AND grbm = ?
                  AND khnd BETWEEN ? AND ?
                  AND khjg IN ('\u4f18\u79c0', '\u79f0\u804c', '\u5408\u683c')
                """, Integer.class, orgCode, personNo, startYear, String.valueOf(targetYear - 1));
        return count == null ? 0 : count;
    }

    private String standardTable(String postPrefix) {
        if ("03".equals(postPrefix)) {
            return "bz06_zwgz_fj";
        }
        if (RANK_PREFIXES.contains(postPrefix)) {
            return "bz06_djgz";
        }
        if (WORKER_PREFIXES.contains(postPrefix)) {
            return "bz06_zwgz_gr";
        }
        if (GRADE_PREFIXES.contains(postPrefix)) {
            return "bz06_jbgz";
        }
        if (SALARY_GRADE_PREFIXES.contains(postPrefix)) {
            return "bz06_xjgz";
        }
        throw new BusinessException("UNSUPPORTED_POST_PREFIX", "Unsupported zwbm2 prefix for normal promotion: " + postPrefix);
    }

    private String standardTableForRegularization(String postPrefix) {
        if ("03".equals(postPrefix)) {
            return "bz06_zwgz_fj";
        }
        if (WORKER_PREFIXES.contains(postPrefix)) {
            return "bz06_zwgz_gr";
        }
        if (SALARY_GRADE_PREFIXES.contains(postPrefix)) {
            return "bz06_xjgz";
        }
        return standardTable(postPrefix);
    }

    private String itemCode(String postPrefix) {
        return WORKER_PREFIXES.contains(postPrefix) || "03".equals(postPrefix) ? POST_SALARY_CODE : LEVEL_SALARY_CODE;
    }

    private String itemName(String postPrefix) {
        if ("03".equals(postPrefix)) {
            return "\u7b49\u7ea7\u5de5\u8d44";
        }
        if (WORKER_PREFIXES.contains(postPrefix)) {
            return "\u5c97\u4f4d\u5de5\u8d44";
        }
        return SALARY_GRADE_PREFIXES.contains(postPrefix) ? "\u85aa\u7ea7\u5de5\u8d44" : "\u7ea7\u522b\u5de5\u8d44";
    }

    private String regularizationPostItemName(String postPrefix) {
        return WORKER_PREFIXES.contains(postPrefix) || SALARY_GRADE_PREFIXES.contains(postPrefix)
                ? "\u5c97\u4f4d\u5de5\u8d44"
                : "\u804c\u52a1\u5de5\u8d44";
    }

    private String regularizationLevelItemName(String postPrefix) {
        if (SALARY_GRADE_PREFIXES.contains(postPrefix)) {
            return "\u85aa\u7ea7\u5de5\u8d44";
        }
        return WORKER_PREFIXES.contains(postPrefix) ? "\u7ea7\u522b/\u85aa\u7ea7\u5de5\u8d44" : "\u7ea7\u522b\u5de5\u8d44";
    }

    private String regularizationStateValue(Map<String, Object> row) {
        String postCode = trim(row.get("zwbm2"));
        if (!StringUtils.hasText(postCode)) {
            return "";
        }
        String prefix = postPrefix(postCode);
        int step = parseInt(row.get("zwgzdc2"));
        int reverseStep = parseInt(row.get("djc2"));
        if (SALARY_GRADE_PREFIXES.contains(prefix)) {
            return step + "\u85aa\u7ea7" + (reverseStep > 0 ? "\u5012" + reverseStep : "");
        }
        if (WORKER_PREFIXES.contains(prefix)) {
            return step + "\u7ea7" + (reverseStep > 0 ? "\u5012" + reverseStep : "");
        }
        String level = trim(row.get("jbgzjb2"));
        return level + "\u7ea7" + step + "\u6863" + (reverseStep > 0 ? "\u5012" + reverseStep : "");
    }

    private String legacy2006AfterValue(String itemCode, String postPrefix, Map<String, Object> expectedRow) {
        return switch (itemCode) {
            case POST_SALARY_CODE -> trim(expectedRow.get("zwgw2")) + "/" + trim(expectedRow.get("zwgzdc2"));
            case LEVEL_SALARY_CODE -> regularizationStateValue(expectedRow);
            case TECHNICAL_GRADE_SALARY_CODE -> trim(expectedRow.get("zwgw2"));
            case "JXGZ" -> trim(expectedRow.get("zgxl"));
            case POLICE_RANK_ALLOWANCE_CODE -> trim(expectedRow.get("jx"));
            default -> trim(expectedRow.get("zwbm2"));
        };
    }

    private Legacy2006CivilState inferLegacy2006CivilState(Map<String, Object> row) {
        String postCode = trim(row.get("zwbm2"));
        if (postCode.length() < 2 || !"01".equals(postCode.substring(0, 2)) || postCode.contains("F")) {
            return null;
        }
        Legacy2006CurrentPostYears currentPostYears = legacy2006CurrentPostYears(row, postCode);
        int conversionYears = currentPostYears == null ? 0 : currentPostYears.conversionYears();
        int postYears = currentPostYears == null ? 0 : currentPostYears.postYears();
        String tgbPostCode = currentPostYears == null ? postCode : currentPostYears.postCode();
        if (conversionYears <= 0 || postYears <= 0) {
            return null;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT jb, dc
                FROM bz06_tgb
                WHERE zwbm = ?
                  AND ? BETWEEN rzns AND rznz
                  AND ? BETWEEN tgns AND tgnz
                LIMIT 1
                """, tgbPostCode, postYears, conversionYears);
        if (rows.isEmpty()) {
            return null;
        }
        String inferredLevel = trim(rows.get(0).get("jb"));
        String inferredStep = trim(rows.get(0).get("dc"));
        String baseLevel = inferredLevel;
        String baseStep = inferredStep;
        String note = "zwbm2=" + postCode + "\uff0c\u5957\u6539\u804c\u52a1=" + tgbPostCode
                + "\uff0crznx=" + postYears + "\uff0ctgnx=" + conversionYears
                + "\uff0c\u5e74\u9650\u6765\u6e90=" + currentPostYears.source();
        Legacy2006LevelStep previousAdjusted = applyLegacy2006PreviousPost(row, tgbPostCode, inferredLevel, inferredStep, conversionYears);
        if (!previousAdjusted.level().equals(inferredLevel) || !previousAdjusted.step().equals(inferredStep)) {
            note += "\uff0c\u524d\u4efb\u804c\u52a1\u6bd4\u8f83\u751f\u6548\uff1a"
                    + inferredLevel + "\u7ea7" + inferredStep + "\u6863 -> "
                    + previousAdjusted.level() + "\u7ea7" + previousAdjusted.step() + "\u6863";
            inferredLevel = previousAdjusted.level();
            inferredStep = previousAdjusted.step();
        }
        Legacy2006EducationState educationState = legacy2006EducationState(row, tgbPostCode);
        if (educationState != null) {
            if (tgbPostCode.compareTo(educationState.postCode()) > 0) {
                Legacy2006LevelStep educationPostState = lookupLegacy2006CivilTgbState(
                        educationState.postCode(),
                        1,
                        conversionYears
                );
                if (educationPostState != null
                        && parseInt(educationPostState.level()) > 0
                        && parseInt(educationPostState.level()) < parseInt(inferredLevel)) {
                    note += "\uff0c\u5b66\u5386\u804c\u52a1 " + educationState.postCode()
                            + " \u91cd\u65b0\u5957\u6539\u751f\u6548\uff1a"
                            + inferredLevel + "\u7ea7" + inferredStep + "\u6863 -> "
                            + educationPostState.level() + "\u7ea7" + educationPostState.step() + "\u6863";
                    inferredLevel = educationPostState.level();
                    inferredStep = educationPostState.step();
                }
            }
            Legacy2006LevelStep adjusted = applyLegacy2006EducationFloor(
                    inferredLevel,
                    inferredStep,
                    educationState.level(),
                    educationState.step()
            );
            if (!adjusted.level().equals(inferredLevel) || !adjusted.step().equals(inferredStep)) {
                note += "\uff0c\u5b66\u5386\u4fdd\u5e95 " + educationState.level() + "\u7ea7" + educationState.step()
                        + "\u6863\u751f\u6548";
                inferredLevel = adjusted.level();
                inferredStep = adjusted.step();
            } else {
                note += "\uff0c\u5b66\u5386\u4fdd\u5e95\u672a\u6539\u53d8\u7ea7\u522b\u6863\u6b21";
            }
        }
        String targetLevel = trim(row.get("jbgzjb2"));
        String targetStep = trim(row.get("zwgzdc2"));
        String afterValue = inferredLevel + "\u7ea7" + inferredStep + "\u6863"
                + "\uff08\u76ee\u6807\u884c " + targetLevel + "\u7ea7" + targetStep + "\u6863\uff09";
        if (inferredLevel.equals(targetLevel) && inferredStep.equals(targetStep)) {
            note += "\uff0c\u4e0e\u76ee\u6807\u884c\u4e00\u81f4";
        } else {
            note += "\uff0c\u4e0e\u76ee\u6807\u884c\u4e0d\u4e00\u81f4\uff0c\u57fa\u7840\u5957\u6539\u4e3a "
                    + baseLevel + "\u7ea7" + baseStep + "\u6863\uff0c\u9700\u7ee7\u7eed\u6821\u51c6\u5176\u4ed6 tg06 \u4e8c\u6b21\u8c03\u6574";
        }
        return new Legacy2006CivilState(afterValue, note);
    }

    private Legacy2006LevelStep applyLegacy2006PreviousPost(
            Map<String, Object> row,
            String currentPostCode,
            String currentLevel,
            String currentStep,
            int conversionYears
    ) {
        List<Legacy2006Appointment> appointments = legacy2006Appointments(row, currentPostCode.substring(0, 2));
        Legacy2006Appointment previousAppointment = appointments.size() > 1 ? appointments.get(1) : null;
        if (previousAppointment == null) {
            return new Legacy2006LevelStep(currentLevel, currentStep);
        }
        String previousPostCode = previousAppointment.postCode();
        if (previousPostCode.length() < 2 || !"01".equals(previousPostCode.substring(0, 2))) {
            return new Legacy2006LevelStep(currentLevel, currentStep);
        }
        int previousPostYears = previousAppointment.postYears();
        if (previousPostYears <= 0) {
            return new Legacy2006LevelStep(currentLevel, currentStep);
        }
        Legacy2006LevelStep previousState = lookupLegacy2006CivilTgbState(previousPostCode, previousPostYears, conversionYears);
        if (previousState == null) {
            return new Legacy2006LevelStep(currentLevel, currentStep);
        }
        int currentLevelValue = parseInt(currentLevel);
        int previousLevelValue = parseInt(previousState.level());
        if (currentLevelValue <= 0 || previousLevelValue <= 0) {
            return new Legacy2006LevelStep(currentLevel, currentStep);
        }
        BigDecimal currentAmount = lookupCivilLevelSalary("200607", "01", currentLevel, parseInt(currentStep));
        BigDecimal previousAmount = lookupCivilLevelSalary("200607", "01", previousState.level(), parseInt(previousState.step()));
        if (currentLevelValue < previousLevelValue && currentAmount.compareTo(previousAmount) < 0) {
            int adjustedStep = civilPromotedLevelStep(
                    "200607",
                    "01",
                    previousLevelValue,
                    parseInt(previousState.step()),
                    currentLevelValue
            );
            return new Legacy2006LevelStep(currentLevel, String.valueOf(adjustedStep));
        }
        if (currentLevelValue >= previousLevelValue) {
            int targetLevel = previousLevelValue - 1;
            if (targetLevel <= 0) {
                return new Legacy2006LevelStep(currentLevel, currentStep);
            }
            int adjustedStep = currentAmount.compareTo(previousAmount) >= 0
                    ? civilPromotedLevelStep("200607", "01", currentLevelValue, parseInt(currentStep), targetLevel)
                    : civilPromotedLevelStep("200607", "01", previousLevelValue, parseInt(previousState.step()), targetLevel);
            return new Legacy2006LevelStep(String.valueOf(targetLevel), String.valueOf(adjustedStep));
        }
        return new Legacy2006LevelStep(currentLevel, currentStep);
    }

    private Legacy2006LevelStep lookupLegacy2006CivilTgbState(String postCode, int postYears, int conversionYears) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT jb, dc
                FROM bz06_tgb
                WHERE zwbm = ?
                  AND ? BETWEEN rzns AND rznz
                  AND ? BETWEEN tgns AND tgnz
                LIMIT 1
                """, postCode, postYears, conversionYears);
        if (rows.isEmpty()) {
            return null;
        }
        return new Legacy2006LevelStep(trim(rows.get(0).get("jb")), trim(rows.get(0).get("dc")));
    }

    private Legacy2006InstitutionState inferLegacy2006InstitutionState(Map<String, Object> row) {
        String postCode = trim(row.get("zwbm2"));
        if (postCode.length() < 2 || !SALARY_GRADE_PREFIXES.contains(postCode.substring(0, 2)) || postCode.contains("F")) {
            return null;
        }
        Legacy2006CurrentPostYears currentPostYears = legacy2006CurrentPostYears(row, postCode);
        int conversionYears = currentPostYears == null ? 0 : currentPostYears.conversionYears();
        int postYears = currentPostYears == null ? 0 : currentPostYears.postYears();
        String tgbPostCode = currentPostYears == null ? postCode : legacy2006InstitutionTgbPostCode(currentPostYears.postCode());
        if (conversionYears <= 0 || postYears <= 0 || !StringUtils.hasText(tgbPostCode)) {
            return null;
        }
        Legacy2006LevelStep currentState = lookupLegacy2006CivilTgbState(tgbPostCode, postYears, conversionYears);
        if (currentState == null || !StringUtils.hasText(currentState.step())) {
            return null;
        }
        String inferredStep = currentState.step();
        String baseStep = inferredStep;
        String note = "zwbm2=" + postCode + "\uff0c\u5957\u6539\u5c97\u4f4d=" + tgbPostCode
                + "\uff0crznx=" + postYears + "\uff0ctgnx=" + conversionYears
                + "\uff0c\u5e74\u9650\u6765\u6e90=" + currentPostYears.source();

        Legacy2006LevelStep previousAdjusted = applyLegacy2006InstitutionPreviousPost(row, tgbPostCode, inferredStep, conversionYears);
        if (!previousAdjusted.step().equals(inferredStep)) {
            note += "\uff0c\u524d\u4efb\u5c97\u4f4d\u6bd4\u8f83\u751f\u6548\uff1a"
                    + inferredStep + "\u85aa\u7ea7 -> " + previousAdjusted.step() + "\u85aa\u7ea7";
            inferredStep = previousAdjusted.step();
        }

        Legacy2006EducationState educationState = legacy2006EducationState(row, tgbPostCode);
        if (educationState != null && StringUtils.hasText(educationState.step())
                && parseInt(inferredStep) < parseInt(educationState.step())) {
            note += "\uff0c\u5b66\u5386\u4fdd\u5e95 " + educationState.step() + "\u85aa\u7ea7\u751f\u6548";
            inferredStep = educationState.step();
        }

        String targetStep = trim(row.get("zwgzdc2"));
        String afterValue = inferredStep + "\u85aa\u7ea7\uff08\u76ee\u6807\u884c " + targetStep + "\u85aa\u7ea7\uff09";
        if (inferredStep.equals(targetStep)) {
            note += "\uff0c\u4e0e\u76ee\u6807\u884c\u4e00\u81f4";
        } else {
            note += "\uff0c\u4e0e\u76ee\u6807\u884c\u4e0d\u4e00\u81f4\uff0c\u57fa\u7840\u5957\u6539\u4e3a "
                    + baseStep + "\u85aa\u7ea7\uff0c\u9700\u7ee7\u7eed\u6821\u51c6\u4e8b\u4e1a tg06 \u4e8c\u6b21\u8c03\u6574";
        }
        return new Legacy2006InstitutionState(afterValue, note);
    }

    private Legacy2006LevelStep applyLegacy2006InstitutionPreviousPost(
            Map<String, Object> row,
            String currentPostCode,
            String currentStep,
            int conversionYears
    ) {
        String postPrefix = postPrefix(trim(row.get("zwbm2")));
        List<Legacy2006Appointment> appointments = legacy2006Appointments(row, postPrefix);
        Legacy2006Appointment previousAppointment = appointments.size() > 1 ? appointments.get(1) : null;
        if (previousAppointment == null) {
            return new Legacy2006LevelStep("", currentStep);
        }
        String previousPostCode = legacy2006InstitutionTgbPostCode(previousAppointment.postCode());
        if (!StringUtils.hasText(previousPostCode)) {
            return new Legacy2006LevelStep("", currentStep);
        }
        int previousPostYears = previousAppointment.postYears();
        if (previousPostYears <= 0) {
            return new Legacy2006LevelStep("", currentStep);
        }
        Legacy2006LevelStep previousState = lookupLegacy2006CivilTgbState(previousPostCode, previousPostYears, conversionYears);
        if (previousState == null || parseInt(previousState.step()) <= parseInt(currentStep)) {
            return new Legacy2006LevelStep("", currentStep);
        }
        return new Legacy2006LevelStep("", previousState.step());
    }

    private Legacy2006WorkerState inferLegacy2006WorkerState(Map<String, Object> row) {
        String postCode = trim(row.get("zwbm2"));
        if (postCode.length() < 2 || !WORKER_PREFIXES.contains(postCode.substring(0, 2)) || postCode.contains("F")) {
            return null;
        }
        Legacy2006CurrentPostYears currentPostYears = legacy2006CurrentPostYears(row, postCode);
        int conversionYears = currentPostYears == null ? 0 : currentPostYears.conversionYears();
        int postYears = currentPostYears == null ? 0 : currentPostYears.postYears();
        String tgbPostCode = currentPostYears == null ? postCode : currentPostYears.postCode();
        if (conversionYears <= 0 || postYears <= 0 || !StringUtils.hasText(tgbPostCode)) {
            return null;
        }
        Legacy2006LevelStep currentState = lookupLegacy2006CivilTgbState(tgbPostCode, postYears, conversionYears);
        if (currentState == null || !StringUtils.hasText(currentState.step())) {
            return null;
        }
        String inferredStep = currentState.step();
        String baseStep = inferredStep;
        String note = "zwbm2=" + postCode + "\uff0c\u5957\u6539\u5c97\u4f4d=" + tgbPostCode
                + "\uff0crznx=" + postYears + "\uff0ctgnx=" + conversionYears
                + "\uff0c\u5e74\u9650\u6765\u6e90=" + currentPostYears.source();

        String previousStep = legacy2006WorkerPreviousStep(row, tgbPostCode, inferredStep, conversionYears);
        if (StringUtils.hasText(previousStep) && parseInt(previousStep) > parseInt(inferredStep)) {
            note += "\uff0c\u524d\u4efb\u5c97\u4f4d\u6bd4\u8f83\u751f\u6548\uff1a"
                    + inferredStep + "\u7ea7 -> " + previousStep + "\u7ea7";
            inferredStep = previousStep;
        }

        Legacy2006EducationState educationState = legacy2006EducationState(row, tgbPostCode);
        if (educationState != null && StringUtils.hasText(educationState.step())
                && parseInt(inferredStep) < parseInt(educationState.step())) {
            note += "\uff0c\u5b66\u5386\u4fdd\u5e95 " + educationState.step() + "\u7ea7\u751f\u6548";
            inferredStep = educationState.step();
        }

        String targetStep = trim(row.get("zwgzdc2"));
        String afterValue = inferredStep + "\u7ea7\uff08\u76ee\u6807\u884c " + targetStep + "\u7ea7\uff09";
        if (inferredStep.equals(targetStep)) {
            note += "\uff0c\u4e0e\u76ee\u6807\u884c\u4e00\u81f4";
        } else {
            note += "\uff0c\u4e0e\u76ee\u6807\u884c\u4e0d\u4e00\u81f4\uff0c\u57fa\u7840\u5957\u6539\u4e3a "
                    + baseStep + "\u7ea7\uff0c\u9700\u7ee7\u7eed\u6821\u51c6\u5de5\u4eba tg06 \u4e8c\u6b21\u8c03\u6574";
        }
        return new Legacy2006WorkerState(afterValue, note);
    }

    private String legacy2006WorkerPreviousStep(Map<String, Object> row, String currentPostCode, String currentStep, int conversionYears) {
        String postPrefix = postPrefix(trim(row.get("zwbm2")));
        List<Legacy2006Appointment> appointments = legacy2006Appointments(row, postPrefix);
        Legacy2006Appointment previousAppointment = appointments.size() > 1 ? appointments.get(1) : null;
        if (previousAppointment == null || previousAppointment.postYears() <= 0) {
            return "";
        }
        String previousPostCode = previousAppointment.postCode();
        if (previousPostCode.length() < 2 || !WORKER_PREFIXES.contains(previousPostCode.substring(0, 2))) {
            return "";
        }
        Legacy2006LevelStep previousState = lookupLegacy2006CivilTgbState(previousPostCode, previousAppointment.postYears(), conversionYears);
        if (previousState == null || !StringUtils.hasText(previousState.step())) {
            return "";
        }
        if ("05".equals(postPrefix)) {
            BigDecimal currentAmount = lookupWorkerPostSalaryColumn("200607", currentPostCode, parseInt(currentStep));
            BigDecimal previousAmount = lookupWorkerPostSalaryColumn("200607", previousPostCode, parseInt(previousState.step()));
            return previousAmount.compareTo(currentAmount) > 0
                    ? legacy2006WorkerPromotedStep(previousPostCode, previousState.step(), currentPostCode)
                    : "";
        }
        return previousState.step();
    }

    private String legacy2006WorkerPromotedStep(String previousPostCode, String previousStep, String currentPostCode) {
        BigDecimal previousAmount = lookupWorkerPostSalaryColumn("200607", previousPostCode, parseInt(previousStep));
        for (int step = 1; step <= 20; step++) {
            BigDecimal currentAmount = lookupWorkerPostSalaryOrZero("200607", currentPostCode, step);
            if (currentAmount.compareTo(BigDecimal.ZERO) > 0 && currentAmount.compareTo(previousAmount) > 0) {
                return String.valueOf(step);
            }
        }
        return previousStep;
    }

    private String legacy2006InstitutionTgbPostCode(String postCode) {
        String normalized = trim(postCode);
        if (normalized.compareTo("1000") <= 0 || "10FF".equals(normalized)) {
            return normalized;
        }
        String lookupPostCode = normalized.length() >= 2 && normalized.substring(0, 2).compareTo("10") > 0
                ? "10" + normalized.substring(Math.max(0, normalized.length() - 2))
                : normalized;
        List<String> values = jdbcTemplate.queryForList("""
                SELECT gwbm
                FROM bz06_zw_gw
                WHERE zwbm = ? AND tj1 = '1'
                LIMIT 1
                """, String.class, lookupPostCode);
        return values.isEmpty() ? "" : trim(values.get(0));
    }

    private Legacy2006EducationState legacy2006EducationState(Map<String, Object> row, String postCode) {
        String educationCode = legacy2006EducationCode(row);
        if (!StringUtils.hasText(educationCode)) {
            return null;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT zzzwbm, zzjb, zzdc
                FROM bz06_zzdz
                WHERE LEFT(zzzwbm, 2) = LEFT(?, 2)
                  AND xlbm = ?
                LIMIT 1
                """, postCode, educationCode);
        if (rows.isEmpty()) {
            return null;
        }
        String level = trim(rows.get(0).get("zzjb"));
        String step = trim(rows.get(0).get("zzdc"));
        if (!StringUtils.hasText(step)) {
            return null;
        }
        String educationPostCode = trim(rows.get(0).get("zzzwbm"));
        if (educationPostCode.length() >= 2 && "07".equals(educationPostCode.substring(0, 2))
                && educationPostCode.length() >= 4 && "0".equals(educationPostCode.substring(3, 4))) {
            educationPostCode = "070" + educationPostCode.substring(2, 3);
        }
        return new Legacy2006EducationState(educationPostCode, level, step);
    }

    private String legacy2006EducationCode(Map<String, Object> row) {
        List<String> values = jdbcTemplate.queryForList("""
                SELECT TRIM(xlbm)
                FROM dxl
                WHERE dwbm = ? AND grbm = ?
                  AND TRIM(COALESCE(xllb, '')) NOT IN ('\u5176\u5b83', '\u5176\u4ed6')
                  AND TRIM(COALESCE(bysj, '')) <> ''
                  AND REPLACE(bysj, '.', '') <= '200607'
                ORDER BY xlbm, bysj DESC
                LIMIT 1
                """, String.class, trim(row.get("dwbm")), trim(row.get("grbm")));
        if (!values.isEmpty()) {
            return trim(values.get(0));
        }
        Integer educationRows = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM dxl
                WHERE dwbm = ? AND grbm = ?
                  AND TRIM(COALESCE(bysj, '')) <> ''
                  AND REPLACE(bysj, '.', '') <= '200607'
                """, Integer.class, trim(row.get("dwbm")), trim(row.get("grbm")));
        if (educationRows != null && educationRows > 0) {
            return "";
        }
        return trim(row.get("xlbm"));
    }

    private Legacy2006LevelStep applyLegacy2006EducationFloor(
            String currentLevel,
            String currentStep,
            String educationLevel,
            String educationStep
    ) {
        int currentLevelValue = parseInt(currentLevel);
        int currentStepValue = parseInt(currentStep);
        int educationLevelValue = parseInt(educationLevel);
        int educationStepValue = parseInt(educationStep);
        if (currentLevelValue <= 0 || currentStepValue <= 0 || educationLevelValue <= 0 || educationStepValue <= 0) {
            return new Legacy2006LevelStep(currentLevel, currentStep);
        }
        BigDecimal currentAmount = lookupCivilLevelSalary("200607", "01", currentLevel, currentStepValue);
        BigDecimal educationAmount = lookupCivilLevelSalary("200607", "01", educationLevel, educationStepValue);
        if (currentLevelValue > educationLevelValue) {
            int targetStep = currentStepValue <= educationStepValue || educationAmount.compareTo(currentAmount) >= 0
                    ? educationStepValue
                    : civilPromotedLevelStep("200607", "01", currentLevelValue, currentStepValue, educationLevelValue);
            return new Legacy2006LevelStep(educationLevel, String.valueOf(targetStep));
        }
        if (currentLevelValue == educationLevelValue && currentStepValue < educationStepValue) {
            return new Legacy2006LevelStep(currentLevel, educationStep);
        }
        if (currentLevelValue < educationLevelValue && educationAmount.compareTo(currentAmount) >= 0) {
            int targetStep = civilPromotedLevelStep("200607", "01", educationLevelValue, educationStepValue, currentLevelValue);
            return new Legacy2006LevelStep(currentLevel, String.valueOf(targetStep));
        }
        return new Legacy2006LevelStep(currentLevel, currentStep);
    }

    private String legacy2006StartWorkDate(Map<String, Object> row) {
        String fromRow = trim(row.get("cjgzny"));
        if (StringUtils.hasText(fromRow)) {
            return fromRow;
        }
        List<String> values = jdbcTemplate.queryForList("""
                SELECT cjgzny
                FROM dryjbxx
                WHERE dwbm = ? AND grbm = ?
                LIMIT 1
                """, String.class, trim(row.get("dwbm")), trim(row.get("grbm")));
        return values.isEmpty() ? "" : trim(values.get(0));
    }

    private Legacy2006CurrentPostYears legacy2006CurrentPostYears(Map<String, Object> row, String postCode) {
        String startWorkDate = legacy2006StartWorkDate(row);
        int startWorkYear = parseYear(startWorkDate);
        if (startWorkYear <= 0) {
            return null;
        }
        int conversionYears = 2006 - startWorkYear + 1 + parseInt(row.get("bjglxlnx"))
                - legacy2006AssessmentDeductionYears(trim(row.get("dwbm")), trim(row.get("grbm")), startWorkYear);
        List<Legacy2006Appointment> appointments = legacy2006Appointments(row, postCode.substring(0, 2));
        if (!appointments.isEmpty()) {
            Legacy2006Appointment appointment = appointments.get(0);
            return new Legacy2006CurrentPostYears(
                    appointment.postCode(),
                    conversionYears,
                    appointment.postYears(),
                    "\u4efb\u804c\u7ecf\u5386\u53cd\u7b97"
            );
        }
        Legacy2006EducationState educationState = legacy2006EducationState(row, postCode);
        if (educationState != null && StringUtils.hasText(educationState.postCode())) {
            String regularizationDate = legacy2006RegularizationDate(row);
            int regularizationYear = parseYear(regularizationDate);
            int postYears = regularizationYear > 0
                    ? 2006 - regularizationYear + 1
                    - legacy2006AssessmentDeductionYears(trim(row.get("dwbm")), trim(row.get("grbm")), regularizationYear)
                    : 1;
            return new Legacy2006CurrentPostYears(
                    educationState.postCode(),
                    conversionYears,
                    Math.max(postYears, 1),
                    "\u5b66\u5386\u8f6c\u6b63\u5b9a\u7ea7\u804c\u52a1\uff0c\u4efb\u804c\u5e74\u9650\u6309\u8f6c\u6b63\u65f6\u95f4\u8d77\u7b97"
            );
        }
        String postStartDate = trim(row.get("srny"));
        int postStartYear = parseYear(postStartDate);
        if (postStartYear <= 0) {
            return null;
        }
        int postYears = 2006 - postStartYear + 1
                - legacy2006AssessmentDeductionYears(trim(row.get("dwbm")), trim(row.get("grbm")), postStartYear);
        return new Legacy2006CurrentPostYears(postCode, conversionYears, postYears, "\u4eba\u5458\u57fa\u7840\u4fe1\u606f\u53cd\u7b97");
    }

    private String legacy2006RegularizationDate(Map<String, Object> row) {
        List<String> values = jdbcTemplate.queryForList("""
                SELECT zzny
                FROM dryjbxx
                WHERE dwbm = ? AND grbm = ?
                LIMIT 1
                """, String.class, trim(row.get("dwbm")), trim(row.get("grbm")));
        String regularizationDate = values.isEmpty() ? "" : trim(values.get(0));
        if (parseYear(regularizationDate) > 0) {
            return regularizationDate;
        }
        return legacy2006StartWorkDate(row);
    }

    private List<Legacy2006Appointment> legacy2006Appointments(Map<String, Object> row, String postPrefix) {
        String orgCode = trim(row.get("dwbm"));
        String personCode = trim(row.get("grbm"));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT post_code, start_date, judicial_post, excluded_years
                FROM (
                    SELECT zjbm AS post_code, srny AS start_date, false AS judicial_post, COALESCE(kjnx, 0) AS excluded_years
                    FROM dryzwbh
                    WHERE dwbm = ? AND grbm = ?
                      AND LEFT(zjbm, 2) = ?
                      AND TRIM(COALESCE(srny, '')) <> ''
                      AND REPLACE(srny, '.', '') < '200607'
                      AND REPLACE(srny, '.', '') > '195001'
                    UNION ALL
                    SELECT CONCAT(?, SUBSTRING(zjbm, 3, 1),
                                  CASE WHEN ? = '07' OR zjbm IN ('01B0', '01C0', '01B1', '01C1') THEN '0' ELSE '1' END) AS post_code,
                           rzsj AS start_date,
                           true AS judicial_post,
                           0 AS excluded_years
                    FROM jdzw
                    WHERE dwbm = ? AND grbm = ?
                      AND LEFT(zjbm, 2) = CASE WHEN ? < '10' THEN ? ELSE '10' END
                      AND TRIM(COALESCE(rzsj, '')) <> ''
                      AND REPLACE(rzsj, '.', '') < '200607'
                      AND REPLACE(rzsj, '.', '') > '195001'
                ) t
                ORDER BY REPLACE(start_date, '.', '') DESC, post_code DESC
                """, orgCode, personCode, postPrefix,
                postPrefix, postPrefix,
                orgCode, personCode, postPrefix, postPrefix);
        List<Legacy2006AppointmentRow> historyRows = rows.stream()
                .map(historyRow -> new Legacy2006AppointmentRow(
                        trim(historyRow.get("post_code")),
                        trim(historyRow.get("start_date")),
                        parseBoolean(historyRow.get("judicial_post")),
                        parseInt(historyRow.get("excluded_years"))
                ))
                .toList();
        List<Legacy2006Appointment> appointments = new ArrayList<>();
        Legacy2006Appointment current = legacy2006AppointmentFromHistory(historyRows, 0, orgCode, personCode);
        if (current != null) {
            appointments.add(current);
            Legacy2006Appointment previous = legacy2006AppointmentFromHistory(historyRows, current.consumedIndex() + 1, orgCode, personCode);
            if (previous != null) {
                appointments.add(previous);
            }
        }
        return appointments;
    }

    private Legacy2006Appointment legacy2006AppointmentFromHistory(
            List<Legacy2006AppointmentRow> rows,
            int startIndex,
            String orgCode,
            String personCode
    ) {
        if (startIndex < 0 || startIndex >= rows.size()) {
            return null;
        }
        Legacy2006AppointmentRow selected = rows.get(startIndex);
        String postCode = selected.postCode();
        String startDate = selected.startDate();
        boolean judicialPost = selected.judicialPost();
        int excludedYears = selected.excludedYears();
        int consumedIndex = startIndex;
        int lowerPostYears = 0;
        for (int i = startIndex + 1; i < rows.size(); i++) {
            Legacy2006AppointmentRow candidate = rows.get(i);
            if (legacy2006ComparablePostCode(candidate.postCode()).compareTo(legacy2006ComparablePostCode(postCode)) <= 0) {
                if (candidate.judicialPost()) {
                    postCode = candidate.postCode();
                    startDate = candidate.startDate();
                    judicialPost = true;
                    excludedYears += candidate.excludedYears();
                } else {
                    startDate = candidate.startDate();
                    judicialPost = false;
                    excludedYears += candidate.excludedYears() + lowerPostYears;
                    lowerPostYears = 0;
                }
                consumedIndex = i;
            } else {
                lowerPostYears += Math.max(0, parseYear(rows.get(i - 1).startDate()) - parseYear(candidate.startDate()));
            }
        }
        int postStartYear = parseYear(startDate);
        int assessmentExcludedYears = postStartYear > 0
                ? legacy2006AssessmentDeductionYears(orgCode, personCode, postStartYear)
                : 0;
        int postYears = postStartYear > 0 ? 2006 - postStartYear + 1 - excludedYears - assessmentExcludedYears : 0;
        return new Legacy2006Appointment(postCode, Math.max(postYears, 0), consumedIndex);
    }

    private int legacy2006AssessmentDeductionYears(String orgCode, String personCode, int startYear) {
        if (startYear <= 0) {
            return 0;
        }
        int fromYear = Math.max(1993, startYear);
        if (fromYear > 2005) {
            return 0;
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM dndkh
                WHERE dwbm = ? AND grbm = ?
                  AND khnd BETWEEN ? AND '2005'
                  AND TRIM(khjg) NOT IN (
                      '\u4f18\u79c0',
                      '\u79f0\u804c',
                      '\u5408\u683c',
                      '\u57fa\u672c\u79f0\u804c',
                      '\u57fa\u672c\u5408\u683c',
                      '\u672a\u5b9a\u7b49\u6b21(\u8bd5\u7528\u671f)',
                      '\u672a\u5b9a\u7b49\u6b21(\u89c1\u4e60\u671f)'
                  )
                """, Integer.class, orgCode, personCode, String.valueOf(fromYear));
        return count == null ? 0 : count;
    }

    private String legacy2006ComparablePostCode(String postCode) {
        String normalized = trim(postCode);
        if (normalized.length() >= 3 && Set.of("01", "02", "03").contains(normalized.substring(0, 2))) {
            return normalized.substring(0, 3) + "0";
        }
        return normalized;
    }

    private boolean parseBoolean(Object value) {
        String text = trim(value);
        return "1".equals(text) || "true".equalsIgnoreCase(text);
    }

    private int parseYear(String dateText) {
        String text = trim(dateText);
        if (text.length() < 4) {
            return 0;
        }
        String year = text.substring(0, 4);
        if (!year.chars().allMatch(Character::isDigit)) {
            return 0;
        }
        return Integer.parseInt(year);
    }

    private String formatBeforeValue(String postPrefix, String level, int step, int reverseStep) {
        return formatValue(postPrefix, level, step, reverseStep);
    }

    private String formatAfterValue(String postPrefix, String level, int step, int reverseStep) {
        return formatValue(postPrefix, level, step, reverseStep);
    }

    private String formatValue(String postPrefix, String level, int step, int reverseStep) {
        String value = SALARY_GRADE_PREFIXES.contains(postPrefix)
                ? step + "\u85aa\u7ea7"
                : WORKER_PREFIXES.contains(postPrefix)
                ? step + "\u7ea7"
                : "03".equals(postPrefix)
                ? trim(level) + "/" + step + "\u6863"
                : level + "\u7ea7" + step + "\u6863";
        return reverseStep > 0 ? value + "\u5012" + reverseStep : value;
    }

    private String formatWorkerPostValue(String postCode, int step, int reverseStep) {
        String value = trim(postCode) + "/" + step + "\u7ea7";
        return reverseStep > 0 ? value + "\u5012" + reverseStep : value;
    }

    private List<SalaryReconcileDetail> compareExpected(String expectedHistoryId, String postPrefix, String itemCode, BigDecimal afterAmount) {
        return compareExpected(expectedHistoryId, Set.of(itemCode), Map.of(itemCode, afterAmount));
    }

    private List<SalaryReconcileDetail> compareExpected(String expectedHistoryId, Set<String> itemCodes, Map<String, BigDecimal> calculatedAmounts) {
        Map<String, SalaryCalculationDetail> expected = byCode(salaryDetailBuilder.build(expectedHistoryId));
        return itemCodes.stream()
                .map(itemCode -> {
                    BigDecimal expectedAmount = expected.containsKey(itemCode)
                            ? expected.get(itemCode).amount()
                            : BigDecimal.ZERO;
                    BigDecimal calculatedAmount = calculatedAmounts.getOrDefault(itemCode, BigDecimal.ZERO);
                    return new SalaryReconcileDetail(
                            itemCode,
                            expected.containsKey(itemCode) ? expected.get(itemCode).itemName() : itemCode,
                            normalize(expectedAmount),
                            normalize(calculatedAmount),
                            normalize(calculatedAmount.subtract(expectedAmount)),
                            calculatedAmount.compareTo(expectedAmount) == 0
                    );
                })
                .toList();
    }

    private Map<String, SalaryCalculationDetail> byCode(List<SalaryCalculationDetail> details) {
        Map<String, SalaryCalculationDetail> result = new LinkedHashMap<>();
        for (SalaryCalculationDetail detail : details) {
            result.put(detail.itemCode(), detail);
        }
        return result;
    }

    private String postPrefix(String postCode) {
        String text = trim(postCode);
        if (text.length() < 2) {
            throw new BusinessException("INVALID_POST_CODE", "Missing zwbm2 post code.");
        }
        return text.substring(0, 2);
    }

    private boolean isWorkerTechnicalGradeChange(String oldPostPrefix, String oldPostCode, Map<String, Object> expectedRow) {
        if (!WORKER_PREFIXES.contains(oldPostPrefix) || expectedRow == null) {
            return false;
        }
        String changeType = trim(expectedRow.get("jslb"));
        String newPostCode = trim(expectedRow.get("zwbm2"));
        return changeType.contains("\u804c\u52a1\u53d8\u5316")
                && newPostCode.length() >= 2
                && WORKER_PREFIXES.contains(newPostCode.substring(0, 2))
                && !trim(oldPostCode).equals(newPostCode);
    }

    private boolean isLegacy2006Conversion(Map<String, Object> expectedRow) {
        if (expectedRow == null) {
            return false;
        }
        String changeType = trim(expectedRow.get("jslb"));
        String postCode = trim(expectedRow.get("zwbm2"));
        return changeType.contains("2006\u5957\u6539")
                && postCode.length() >= 2;
    }

    private boolean isRegularizationGradePlacement(Map<String, Object> expectedRow) {
        if (expectedRow == null) {
            return false;
        }
        String changeType = trim(expectedRow.get("jslb"));
        String postCode = trim(expectedRow.get("zwbm2"));
        return changeType.contains("\u8f6c\u6b63\u5b9a\u7ea7")
                || changeType.contains("\u8c03\u5165\u5b9a\u8d44")
                || changeType.contains("\u8f6c\u4e1a\u5b9a\u8d44")
                || changeType.contains("\u9000\u4f0d\u5b9a\u8d44")
                || (changeType.contains("\u65b0\u8fdb\u5de5\u8d44") && !postCode.contains("F"));
    }

    private boolean isExistingFormalEntrancePlacement(Map<String, Object> expectedRow, Map<String, Object> baselineRow) {
        if (expectedRow == null || baselineRow == null || baselineRow.isEmpty()) {
            return false;
        }
        String changeType = trim(expectedRow.get("jslb"));
        String targetPostCode = trim(expectedRow.get("zwbm2"));
        String baselinePostCode = trim(baselineRow.get("zwbm2"));
        return (changeType.contains("\u65b0\u8fdb\u5de5\u8d44")
                || changeType.contains("\u8c03\u5165\u5b9a\u8d44")
                || changeType.contains("\u8f6c\u4e1a\u5b9a\u8d44")
                || changeType.contains("\u9000\u4f0d\u5b9a\u8d44"))
                && !targetPostCode.contains("F")
                && StringUtils.hasText(baselinePostCode)
                && !baselinePostCode.contains("F")
                && number(baselineRow.get("hj2")).compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isProbationaryNewSalary(Map<String, Object> expectedRow) {
        if (expectedRow == null) {
            return false;
        }
        String changeType = trim(expectedRow.get("jslb"));
        String postCode = trim(expectedRow.get("zwbm2"));
        return postCode.contains("F")
                && (changeType.contains("\u89c1\u4e60\u5de5\u8d44") || changeType.contains("\u65b0\u8fdb\u5de5\u8d44"));
    }

    private boolean isStandardAdjustment(Map<String, Object> expectedRow) {
        if (expectedRow == null) {
            return false;
        }
        String changeType = trim(expectedRow.get("jslb"));
        return changeType.contains("\u8c03\u6807\u664b\u5347")
                || changeType.contains("\u8c03\u6574\u6807\u51c6")
                || changeType.contains("\u6d25\u8d34\u53d8\u5316")
                || changeType.contains("\u964d\u8d44\u5904\u5206")
                || changeType.contains("\u5956\u52b1\u664b\u5347")
                || changeType.contains("\u5176\u5b83\u60c5\u51b5");
    }

    private boolean isNormalLevelPromotion(Map<String, Object> expectedRow, String commandChangeType) {
        if (expectedRow == null) {
            return "\u6b63\u5e38\u7ea7\u522b".equals(trim(commandChangeType));
        }
        String changeType = trim(expectedRow.get("jslb"));
        if ("\u6b63\u5e38\u7ea7\u522b".equals(changeType)) {
            return true;
        }
        return "正常级别".equals(changeType);
    }

    private boolean isLevelRolling(Map<String, Object> expectedRow) {
        if (expectedRow == null) {
            return false;
        }
        String changeType = trim(expectedRow.get("jslb"));
        return "\u7ea7\u522b\u6eda\u52a8".equals(changeType);
    }

    private boolean isPoliceRankAllowanceChange(Map<String, Object> expectedRow) {
        if (expectedRow == null) {
            return false;
        }
        String changeType = trim(expectedRow.get("jslb"));
        String postCode = trim(expectedRow.get("zwbm2"));
        if (!changeType.contains("\u8b66\u8854\u53d8\u5316") && !changeType.contains("\u8b66\u8854\u6d25\u8d34")) {
            return false;
        }
        if (postCode.length() < 2) {
            return false;
        }
        return Set.of("01", "02", "03", "21", "22", "23", "24", "25", "26", "27", "28")
                .contains(postCode.substring(0, 2));
    }

    private boolean isEducationChange(Map<String, Object> expectedRow) {
        if (expectedRow == null) {
            return false;
        }
        String changeType = trim(expectedRow.get("jslb"));
        String postCode = trim(expectedRow.get("zwbm2"));
        return changeType.contains("\u5b66\u5386\u53d8\u5316")
                && postCode.length() >= 2
                && Set.of("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "21", "22", "23", "24", "25", "26", "27", "28")
                .contains(postCode.substring(0, 2));
    }

    private boolean isTeacherNurseAllowanceChange(Map<String, Object> expectedRow) {
        if (expectedRow == null) {
            return false;
        }
        String changeType = trim(expectedRow.get("jslb"));
        String postCode = trim(expectedRow.get("zwbm2"));
        if (!changeType.contains("\u6559\u62a4\u6d25\u8d34") || postCode.length() < 2) {
            return false;
        }
        String prefix = postCode.substring(0, 2);
        return prefix.compareTo("07") >= 0 && prefix.compareTo("19") < 0;
    }

    private boolean isJudicialAllowanceChange(Map<String, Object> expectedRow) {
        if (expectedRow == null) {
            return false;
        }
        String changeType = trim(expectedRow.get("jslb"));
        String postCode = trim(expectedRow.get("zwbm2"));
        if (!changeType.contains("\u6cd5\u5b98\u7b49\u7ea7")
                && !changeType.contains("\u68c0\u5bdf\u7b49\u7ea7")
                && !changeType.contains("\u5ba1\u5224\u6d25\u8d34")
                && !changeType.contains("\u68c0\u5bdf\u6d25\u8d34")) {
            return false;
        }
        if (postCode.length() < 2) {
            return false;
        }
        return Set.of("01", "02", "03", "21", "22", "23", "24", "25", "26", "27", "28")
                .contains(postCode.substring(0, 2));
    }

    private boolean isJudicialConversion(Map<String, Object> expectedRow) {
        if (expectedRow == null) {
            return false;
        }
        String changeType = trim(expectedRow.get("jslb"));
        String postCode = trim(expectedRow.get("zwbm2"));
        return changeType.contains("\u6cd5\u68c0\u5957\u6539")
                && postCode.length() >= 2
                && "03".equals(postCode.substring(0, 2));
    }

    private boolean isCivilRankPromotion(Map<String, Object> expectedRow) {
        if (expectedRow == null) {
            return false;
        }
        String changeType = trim(expectedRow.get("jslb"));
        if (!changeType.contains("\u804c\u7ea7\u664b\u5347")
                && !changeType.contains("\u804c\u7ea7\u5957\u6539")
                && !changeType.contains("\u8b66\u5458\u5957\u6539")) {
            return false;
        }
        String postCode = trim(expectedRow.get("zwbm2"));
        if (postCode.length() < 2) {
            return false;
        }
        return Set.of("01", "02", "21", "22", "23", "24", "25", "26", "27", "28")
                .contains(postCode.substring(0, 2));
    }

    private boolean isCivilPostChange(String oldPostPrefix, String oldPostCode, Map<String, Object> expectedRow) {
        if (!LEVEL_PROMOTION_PREFIXES.contains(oldPostPrefix) || expectedRow == null) {
            return false;
        }
        String newPostCode = trim(expectedRow.get("zwbm2"));
        if (newPostCode.length() < 2) {
            return false;
        }
        String newPostPrefix = newPostCode.substring(0, 2);
        String changeType = trim(expectedRow.get("jslb"));
        return changeType.contains("\u804c\u52a1\u53d8\u5316")
                && LEVEL_PROMOTION_PREFIXES.contains(newPostPrefix)
                && !trim(oldPostCode).equals(newPostCode);
    }

    private boolean isSameCivilPostStateCorrection(String oldPostPrefix, String oldPostCode, Map<String, Object> expectedRow) {
        if (!LEVEL_PROMOTION_PREFIXES.contains(oldPostPrefix) || expectedRow == null) {
            return false;
        }
        String newPostCode = trim(expectedRow.get("zwbm2"));
        if (!trim(oldPostCode).equals(newPostCode)) {
            return false;
        }
        String changeType = trim(expectedRow.get("jslb"));
        return changeType.contains("\u804c\u52a1\u53d8\u5316")
                && (
                parseInt(expectedRow.get("jbgzjb2")) != 0
                        || parseInt(expectedRow.get("zwgzdc2")) != 0
                        || number(expectedRow.get("zwgzse2")).compareTo(BigDecimal.ZERO) != 0
                        || number(expectedRow.get("jbgzse2")).compareTo(BigDecimal.ZERO) != 0
        );
    }

    private boolean isJudicialPostChange(String oldPostPrefix, String oldPostCode, Map<String, Object> expectedRow) {
        if (!"03".equals(oldPostPrefix) || expectedRow == null) {
            return false;
        }
        String newPostCode = trim(expectedRow.get("zwbm2"));
        String changeType = trim(expectedRow.get("jslb"));
        return changeType.contains("\u804c\u52a1\u53d8\u5316")
                && newPostCode.length() >= 2
                && "03".equals(newPostCode.substring(0, 2))
                && !trim(oldPostCode).equals(newPostCode);
    }

    private boolean isInstitutionPostChange(String oldPostPrefix, String oldPostCode, Map<String, Object> expectedRow) {
        if (!SALARY_GRADE_PREFIXES.contains(oldPostPrefix) || expectedRow == null) {
            return false;
        }
        String changeType = trim(expectedRow.get("jslb"));
        String newPostCode = trim(expectedRow.get("zwbm2"));
        return changeType.contains("\u804c\u52a1\u53d8\u5316")
                && newPostCode.length() >= 2
                && SALARY_GRADE_PREFIXES.contains(newPostCode.substring(0, 2))
                && !trim(oldPostCode).equals(newPostCode);
    }

    private BigDecimal number(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        String text = trim(value);
        return StringUtils.hasText(text) ? new BigDecimal(text) : BigDecimal.ZERO;
    }

    private int parseInt(Object value) {
        String text = trim(value);
        return StringUtils.hasText(text) ? Integer.parseInt(text) : 0;
    }

    private TeacherNurseSource teacherNurseSource(String orgCode, String personNo) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(jhlqsny) AS jhlqsny, zdjhlnx
                FROM dryjbxx
                WHERE dwbm = ? AND grbm = ?
                LIMIT 1
                """, trim(orgCode), trim(personNo));
        if (rows.isEmpty()) {
            return new TeacherNurseSource("", 0);
        }
        Map<String, Object> row = rows.get(0);
        return new TeacherNurseSource(trim(row.get("jhlqsny")), parseInt(row.get("zdjhlnx")));
    }

    private BigDecimal teacherNurseAllowance(TeacherNurseSource source, int targetYear) {
        String start = source.startDate();
        if (!validYearMonthStart(start)) {
            return BigDecimal.ZERO;
        }
        int startYear = parseInt(start.substring(0, 4));
        if (startYear <= 0 || targetYear - startYear <= 0) {
            return BigDecimal.ZERO;
        }
        int years = targetYear - startYear - source.deductYears();
        if (years >= 20) {
            return BigDecimal.TEN;
        }
        if (years >= 15) {
            return BigDecimal.valueOf(7);
        }
        if (years >= 10) {
            return BigDecimal.valueOf(5);
        }
        if (years >= 5) {
            return BigDecimal.valueOf(3);
        }
        return BigDecimal.ZERO;
    }

    private boolean validYearMonthStart(String value) {
        String text = trim(value);
        return text.length() >= 4 && text.substring(0, 4).chars().allMatch(Character::isDigit);
    }

    private BigDecimal teacherNurseIncrease(String orgCode, Map<String, Object> row, BigDecimal levelSalaryAmount) {
        if (educationOrgType(orgCode) == 2) {
            return BigDecimal.ZERO;
        }
        int rate = parseInt(row.get("tgbl"));
        if (rate <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal postSalaryAmount = number(row.get("zwgzse2"));
        return postSalaryAmount.add(levelSalaryAmount)
                .multiply(BigDecimal.valueOf(rate))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    private BigDecimal normalize(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.stripTrailingZeros();
    }

    private String trim(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private static String trimStatic(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private static int parseIntStatic(Object value) {
        String text = trimStatic(value);
        return StringUtils.hasText(text) ? Integer.parseInt(text) : 0;
    }

    private boolean checked(Object value) {
        String text = trim(value);
        return "√".equals(text)
                || "1".equals(text)
                || "true".equalsIgnoreCase(text)
                || "\u662f".equals(text)
                || "\u5bf9".equals(text);
    }

    private record NormalGradeState(String level, int step, int reverseStep, BigDecimal amount, String ruleNote) {
    }

    private record TeacherNurseSource(String startDate, int deductYears) {
    }

    private record WorkerPostChangePolicy(boolean includeTechnicalGradeDifference, boolean shiftOctoberChangeToNextYear) {
    }

    private record RegularizationPolicy(int additionalStep, boolean includeWorkerAdditionalStep) {
    }

    private record RegularizationPlacement(
            String postCode,
            String level,
            int step,
            String source,
            RegularizationEducationPlacement educationPlacement
    ) {

        private String levelStepValue() {
            return StringUtils.hasText(level) ? level + "\u7ea7" + step + "\u6863" : step + "\u85aa\u7ea7";
        }

        private String describeTarget(Map<String, Object> expectedRow) {
            String targetPostCode = expectedRow == null ? "" : trimStatic(expectedRow.get("zwbm2"));
            String targetLevel = expectedRow == null ? "" : trimStatic(expectedRow.get("jbgzjb2"));
            int targetStep = expectedRow == null ? 0 : parseIntStatic(expectedRow.get("zwgzdc2")) + parseIntStatic(expectedRow.get("djc2"));
            if (targetStep == 0 && expectedRow != null) {
                targetStep = parseIntStatic(expectedRow.get("zwgzdc2"));
            }
            String targetState = StringUtils.hasText(targetLevel)
                    ? targetLevel + "\u7ea7" + targetStep + "\u6863"
                    : targetStep + "\u85aa\u7ea7";
            String educationText = educationPlacement == null
                    ? "\u672a\u627e\u5230\u5b66\u5386\u6807\u51c6"
                    : "\u5b66\u5386\u7f16\u7801=" + educationPlacement.educationCode()
                    + "\uff0c\u6807\u51c6\u5e74=" + educationPlacement.standardYear()
                    + "\uff0c\u5b66\u5386\u6807\u51c6\u5c97\u4f4d=" + educationPlacement.standardPostCode();
            return educationText
                    + "\uff0c\u6267\u884c\u5c97\u4f4d=" + postCode
                    + "\uff0c\u76ee\u6807\u5c97\u4f4d=" + targetPostCode
                    + "\uff0c\u63a8\u5bfc=" + levelStepValue()
                    + "\uff0c\u76ee\u6807=" + targetState
                    + "\uff0c\u6765\u6e90=" + source
                    + (levelStepValue().equals(targetState) ? "\uff0c\u7ea7\u522b/\u85aa\u7ea7\u4e00\u81f4" : "\uff0c\u7ea7\u522b/\u85aa\u7ea7\u4e0d\u4e00\u81f4");
        }
    }

    private record RegularizationEducationPlacement(String standardPostCode, String level, int step, String educationCode, String standardYear) {

        private String describeTarget(Map<String, Object> expectedRow) {
            String targetPostCode = expectedRow == null ? "" : trimStatic(expectedRow.get("zwbm2"));
            String targetLevel = expectedRow == null ? "" : trimStatic(expectedRow.get("jbgzjb2"));
            int targetStep = expectedRow == null ? 0 : parseIntStatic(expectedRow.get("zwgzdc2")) + parseIntStatic(expectedRow.get("djc2"));
            if (targetStep == 0 && expectedRow != null) {
                targetStep = parseIntStatic(expectedRow.get("zwgzdc2"));
            }
            String inferredState = StringUtils.hasText(level)
                    ? level + "\u7ea7" + step + "\u6863"
                    : step + "\u85aa\u7ea7";
            String targetState = StringUtils.hasText(targetLevel)
                    ? targetLevel + "\u7ea7" + targetStep + "\u6863"
                    : targetStep + "\u85aa\u7ea7";
            return "\u5b66\u5386\u7f16\u7801=" + educationCode
                    + "\uff0c\u6807\u51c6\u5e74=" + standardYear
                    + "\uff0c\u6807\u51c6\u5c97\u4f4d=" + standardPostCode
                    + "\uff0c\u76ee\u6807\u5c97\u4f4d=" + targetPostCode
                    + "\uff0c\u63a8\u5bfc=" + inferredState
                    + "\uff0c\u76ee\u6807=" + targetState
                    + (inferredState.equals(targetState) ? "\uff0c\u4e00\u81f4" : "\uff0c\u4e0d\u4e00\u81f4");
        }
    }

    private record DerivedSalaryItem(String itemCode, String itemName) {
    }

    private record CivilPostLevelState(String level, int step) {
    }

    private record Legacy2006CivilState(String afterValue, String ruleNote) {
    }

    private record Legacy2006InstitutionState(String afterValue, String ruleNote) {
    }

    private record Legacy2006WorkerState(String afterValue, String ruleNote) {
    }

    private record Legacy2006EducationState(String postCode, String level, String step) {
    }

    private record Legacy2006LevelStep(String level, String step) {
    }

    private record Legacy2006CurrentPostYears(String postCode, int conversionYears, int postYears, String source) {
    }

    private record Legacy2006AppointmentRow(String postCode, String startDate, boolean judicialPost, int excludedYears) {
    }

    private record Legacy2006Appointment(String postCode, int postYears, int consumedIndex) {
    }
}
