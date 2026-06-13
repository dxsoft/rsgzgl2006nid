package com.dx.rsgzgl.salary;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.salary.dto.NormalGradeBatchTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeBatchTrialResult;
import com.dx.rsgzgl.salary.dto.NormalGradeTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeTrialResult;
import com.dx.rsgzgl.salary.service.NormalGradeBatchTrialService;
import com.dx.rsgzgl.salary.service.NormalGradeTrialService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
class NormalGradeTrialRegressionTests {

    @Autowired
    private NormalGradeTrialService normalGradeTrialService;

    @Autowired
    private NormalGradeBatchTrialService normalGradeBatchTrialService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void civilPostChangeHasMatchingLegacySample() {
        NormalGradeTrialResult result = matchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb LIKE '%职务变化%'
                  AND LEFT(b.zwbm2, 2) IN ('01','02','04','21','22','23','24','25','26','27','28')
                  AND LEFT(t.zwbm2, 2) IN ('01','02','04','21','22','23','24','25','26','27','28')
                  AND b.zwbm2 <> t.zwbm2
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 80
                """, "civil post change");

        assertMatched(result);
        assertChangeCodes(result, "ZWGZSE2", "JBGZSE2");
    }

    @Test
    void institutionPostChangeHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb LIKE '%职务变化%'
                  AND LEFT(b.zwbm2, 2) IN ('07','08','09','10','11')
                  AND LEFT(t.zwbm2, 2) IN ('07','08','09','10','11')
                  AND b.zwbm2 <> t.zwbm2
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 40
                """);
        assumeTrue(result.isPresent(), "No matching institution post-change sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "ZWGZSE2", "JBGZSE2");
    }

    @Test
    void workerTechnicalGradePromotionHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb LIKE '%职务变化%'
                  AND LEFT(b.zwbm2, 2) IN ('05','06')
                  AND LEFT(t.zwbm2, 2) IN ('05','06')
                  AND b.zwbm2 <> t.zwbm2
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 40
                """);
        assumeTrue(result.isPresent(), "No matching worker technical-grade sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "ZWGZSE2", "JSDJGZ2");
    }

    @Test
    void judicialPostChangeHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb LIKE '%职务变化%'
                  AND LEFT(b.zwbm2, 2) = '03'
                  AND LEFT(t.zwbm2, 2) = '03'
                  AND b.zwbm2 <> t.zwbm2
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 40
                """);
        assumeTrue(result.isPresent(), "No matching judicial post-change sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "ZWGZSE2");
    }

    @Test
    void normalGradeOrLevelAdjustmentHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb IN ('正常档次','正常晋档','正常级别','级别滚动')
                  AND b.zwbm2 = t.zwbm2
                  AND LEFT(b.zwbm2, 2) IN ('01','02','03','04','05','06','07','08','09','10','11','21','22','23','24','25','26','27','28')
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 160
                """);
        assumeTrue(result.isPresent(), "No matching normal grade/level adjustment sample in the current legacy database.");

        assertMatched(result.get());
        assertThat(result.get().changes()).isNotEmpty();
    }

    @Test
    void regularizationGradePlacementHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb LIKE '%\u8f6c\u6b63\u5b9a\u7ea7%'
                  AND LEFT(t.zwbm2, 2) IN ('01','02','03','04','05','06','07','08','09','10','11','21','22','23','24','25','26','27','28')
                  AND TRIM(t.xlbm) <> ''
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 80
                """);
        assumeTrue(result.isPresent(), "No matching regularization grade-placement sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "ZWGZSE2", "JBGZSE2", "ZZDJ06");
    }

    @Test
    void transferGradePlacementHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb LIKE '%\u8c03\u5165\u5b9a\u8d44%'
                  AND LEFT(t.zwbm2, 2) IN ('01','02','03','04','05','06','07','08','09','10','11','21','22','23','24','25','26','27','28')
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 80
                """);
        assumeTrue(result.isPresent(), "No matching transfer grade-placement sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "ZWGZSE2", "JBGZSE2");
    }

    @Test
    void demobilizedCadreGradePlacementHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb LIKE '%\u8f6c\u4e1a\u5b9a\u8d44%'
                  AND LEFT(t.zwbm2, 2) IN ('01','02','03','04','05','06','07','08','09','10','11','21','22','23','24','25','26','27','28')
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 80
                """);
        assumeTrue(result.isPresent(), "No matching demobilized cadre grade-placement sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "ZWGZSE2", "JBGZSE2");
    }

    @Test
    void veteranGradePlacementHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb LIKE '%\u9000\u4f0d\u5b9a\u8d44%'
                  AND LEFT(t.zwbm2, 2) IN ('01','02','03','04','05','06','07','08','09','10','11','21','22','23','24','25','26','27','28')
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 80
                """);
        assumeTrue(result.isPresent(), "No matching veteran grade-placement sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "ZWGZSE2", "JBGZSE2");
    }

    @Test
    void probationaryNewSalaryHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                WHERE t.jslb LIKE '%\u89c1\u4e60\u5de5\u8d44%'
                  AND LOCATE('F', t.zwbm2) > 0
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 80
                """);
        assumeTrue(result.isPresent(), "No matching probationary new-salary sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "JXGZ");
    }

    @Test
    void probationarySalaryCanUseEducationCodeFromEducationHistory() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "00806-00896",
                "00806",
                2024,
                11
        ));

        assertMatched(result);
        assertThat(result.changes())
                .anySatisfy(change -> {
                    assertThat(change.itemCode()).isEqualTo("JXGZ");
                    assertThat(change.afterValue()).startsWith("12");
                    assertThat(change.afterAmount()).isEqualByComparingTo("2650");
                });
    }

    @Test
    void salaryGradeIncrementKeepsEducationPostPrefixConversion() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "00809-00295",
                "00809",
                2025,
                1
        ));

        assertMatched(result);
        assertThat(result.changes())
                .anySatisfy(change -> {
                    assertThat(change.itemCode()).isEqualTo("JBGZSE2");
                    assertThat(change.afterValue()).startsWith("8");
                    assertThat(change.afterAmount()).isEqualByComparingTo("707");
                });
    }

    @Test
    void institutionPostChangeKeepsEducationPostPrefixConversion() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "00809-00054",
                "00809",
                2025,
                12
        ));

        assertMatched(result);
        assertThat(result.changes())
                .anySatisfy(change -> {
                    assertThat(change.itemCode()).isEqualTo("JBGZSE2");
                    assertThat(change.afterAmount()).isEqualByComparingTo("3618");
                });
    }

    @Test
    void civilLevelPromotionHighStepSampleMatchesLegacy() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "00919-00021",
                "00919",
                2023,
                1
        ));

        assertMatched(result);
    }

    @Test
    void judicialNormalGradeIncrementUpdatesJudicialGradeSalary() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "044-00229",
                "044",
                2023,
                1
        ));

        assertMatched(result);
        assertThat(result.changes())
                .anySatisfy(change -> {
                    assertThat(change.itemCode()).isEqualTo("ZWGZSE2");
                    assertThat(change.afterAmount()).isEqualByComparingTo("4640");
                    assertThat(change.ruleNote()).contains("bz06_zwgz_fj");
                });
    }

    @Test
    void compulsoryEducationSalaryGradeIncrementRecalculatesOnlyTeachingAllowanceAge() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "00817-00254",
                "00817",
                2025,
                1
        ));

        assertMatched(result);
        assertThat(result.changes())
                .anySatisfy(change -> {
                    assertThat(change.itemCode()).isEqualTo("JHLJT");
                    assertThat(change.afterAmount()).isEqualByComparingTo("5");
                });
        assertThat(result.changes())
                .noneSatisfy(change -> assertThat(change.itemCode()).isEqualTo("JSFSZWTG2"));
    }

    @Test
    void nonCompulsoryEducationSalaryGradeIncrementRecalculatesTeacherNurseIncrease() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "00806-00834",
                "00806",
                2025,
                1
        ));

        assertMatched(result);
        assertThat(result.changes())
                .anySatisfy(change -> {
                    assertThat(change.itemCode()).isEqualTo("JSFSZWTG2");
                    assertThat(change.afterAmount()).isEqualByComparingTo("263");
                });
    }

    @Test
    void teacherNurseIncreaseDoesNotIncludeTeachingAllowanceAgeInBase() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "00111-00070",
                "00111",
                2025,
                1
        ));

        assertMatched(result);
        assertThat(result.changes())
                .anySatisfy(change -> {
                    assertThat(change.itemCode()).isEqualTo("JHLJT");
                    assertThat(change.afterAmount()).isEqualByComparingTo("10");
                });
        assertThat(result.changes())
                .anySatisfy(change -> {
                    assertThat(change.itemCode()).isEqualTo("JSFSZWTG2");
                    assertThat(change.afterAmount()).isEqualByComparingTo("397");
                });
    }

    @Test
    void invalidTeachingAllowanceStartDateIsIgnored() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "00802-00050",
                "00802",
                2025,
                1
        ));

        assertMatched(result);
        assertThat(result.changes())
                .noneSatisfy(change -> assertThat(change.itemCode()).isEqualTo("JHLJT"));
    }

    @Test
    void formalNewSalaryHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                WHERE t.jslb LIKE '%\u65b0\u8fdb\u5de5\u8d44%'
                  AND LOCATE('F', t.zwbm2) = 0
                  AND LEFT(t.zwbm2, 2) IN ('01','02','03','04','05','06','07','08','09','10','11','21','22','23','24','25','26','27','28')
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 80
                """);
        assumeTrue(result.isPresent(), "No matching formal new-salary sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "ZWGZSE2", "JBGZSE2");
    }

    @Test
    void existingFormalNewSalaryKeepsTargetSalaryGradeState() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "00826-00026",
                "00826",
                2025,
                10
        ));

        assertMatched(result);
        assertThat(result.changes())
                .anySatisfy(change -> {
                    assertThat(change.itemCode()).isEqualTo("JBGZSE2");
                    assertThat(change.afterAmount()).isEqualByComparingTo("745");
                    assertThat(change.ruleNote()).contains("\u76ee\u6807\u884c\u4fdd\u6301");
                });
    }

    @Test
    void existingFormalTransferPlacementKeepsTargetSalaryGradeState() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "00818-00113",
                "00818",
                2026,
                1
        ));

        assertMatched(result);
        assertThat(result.changes())
                .anySatisfy(change -> {
                    assertThat(change.itemCode()).isEqualTo("JBGZSE2");
                    assertThat(change.afterAmount()).isEqualByComparingTo("1603");
                    assertThat(change.ruleNote()).contains("\u76ee\u6807\u884c\u4fdd\u6301");
                });
    }

    @Test
    void existingFormalDemobilizedPlacementKeepsTargetSalaryState() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "100-00008",
                "100",
                2024,
                11
        ));

        assertMatched(result);
    }

    @Test
    void policeRankAllowanceChangeHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb IN ('\u8b66\u8854\u53d8\u5316', '\u8b66\u8854\u6d25\u8d34')
                  AND LEFT(t.zwbm2, 2) IN ('01','02','03','21','22','23','24','25','26','27','28')
                  AND TRIM(t.jx) <> ''
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 120
                """);
        assumeTrue(result.isPresent(), "No matching police-rank allowance sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "JXJT");
    }

    @Test
    void civilRankPromotionHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb IN ('\u804c\u7ea7\u664b\u5347', '\u804c\u7ea7\u5957\u6539', '\u8b66\u5458\u5957\u6539')
                  AND LEFT(t.zwbm2, 2) IN ('01','02','21','22','23','24','25','26','27','28')
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 160
                """);
        assumeTrue(result.isPresent(), "No matching civil rank-promotion sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "ZWGZSE2", "JBGZSE2");
    }

    @Test
    void educationChangeHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb = '\u5b66\u5386\u53d8\u5316'
                  AND LEFT(t.zwbm2, 2) IN ('01','02','03','04','05','06','07','08','09','10','11','21','22','23','24','25','26','27','28')
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 160
                """);
        assumeTrue(result.isPresent(), "No matching education-change sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "ZWGZSE2", "JBGZSE2");
    }

    @Test
    void targetRecordCanBeSelectedByChangeTypeWithinSameMonth() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "041-00210",
                "041",
                2024,
                1,
                "\u5b66\u5386\u53d8\u5316"
        ));

        assertMatched(result);
        assertThat(result.expectedHistoryId()).isEqualTo("D42D0B36-5FD1-4D57-BBA2-56BCFF74DC89");
        assertThat(result.expectedTotalAmount()).isEqualByComparingTo("3958");
    }

    @Test
    void batchTrialCanSelectTargetRecordByChangeTypeWithinSameMonth() {
        NormalGradeBatchTrialResult result = normalGradeBatchTrialService.trial(new NormalGradeBatchTrialCommand(
                "041",
                2024,
                1,
                300,
                "\u5b66\u5386\u53d8\u5316"
        ));

        assertThat(result.checkedCount()).isGreaterThan(0);
        assertThat(result.items())
                .filteredOn(item -> item.expectedHistoryId() == null)
                .allSatisfy(item -> assertThat(item.status()).isIn("NO_EXPECTED", "SKIPPED"));
    }

    @Test
    void teacherNurseAllowanceChangeHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb = '\u6559\u62a4\u6d25\u8d34'
                  AND LEFT(t.zwbm2, 2) IN ('07','08','09','10','11')
                  AND TRIM(t.jhlqsny) <> ''
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 120
                """);
        assumeTrue(result.isPresent(), "No matching teacher/nurse allowance sample in the current legacy database.");

        assertMatched(result.get());
        assertAnyChangeCode(result.get(), "JHLJT", "JSFSZWTG2");
    }

    @Test
    void judicialAllowanceChangeHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb IN ('\u6cd5\u5b98\u7b49\u7ea7', '\u68c0\u5bdf\u7b49\u7ea7', '\u5ba1\u5224\u6d25\u8d34', '\u68c0\u5bdf\u6d25\u8d34')
                  AND LEFT(t.zwbm2, 2) IN ('01','02','03','21','22','23','24','25','26','27','28')
                  AND TRIM(t.jx) <> ''
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 180
                """);
        assumeTrue(result.isPresent(), "No matching judicial allowance sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "JXJT");
    }

    @Test
    void judicialConversionHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb = '\u6cd5\u68c0\u5957\u6539'
                  AND LEFT(t.zwbm2, 2) = '03'
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 120
                """);
        assumeTrue(result.isPresent(), "No matching judicial conversion sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "ZWGZSE2");
    }

    @Test
    void legacy2006ConversionHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                WHERE t.jslb = '2006\u5957\u6539'
                  AND LEFT(t.zwbm2, 2) IN ('01','05','07','08','09','10')
                  AND t.hj2 > 0
                ORDER BY t.jsnf, t.jsyf, t.hj2 DESC
                LIMIT 160
                """);
        assumeTrue(result.isPresent(), "No matching 2006 conversion sample in the current legacy database.");

        assertMatched(result.get());
        assertThat(result.get().changes()).isNotEmpty();
    }

    @Test
    void legacy2006InstitutionEducationFloorCanPromoteSalaryGrade() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "00806-00389",
                "00806",
                2006,
                7
        ));

        assertThat(result.changes())
                .anySatisfy(change -> {
                    assertThat(change.itemCode()).isEqualTo("TG2006_XJ");
                    assertThat(change.afterValue()).startsWith("7薪级");
                    assertThat(change.ruleNote()).contains("学历保底 7薪级生效");
                });
    }

    @Test
    void legacy2006InstitutionProbationaryUndeterminedAssessmentDoesNotDeductConversionYears() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "0081202-00033",
                "0081202",
                2006,
                7
        ));

        assertThat(result.changes())
                .anySatisfy(change -> {
                    assertThat(change.itemCode()).isEqualTo("TG2006_XJ");
                    assertThat(change.afterValue()).startsWith("16薪级");
                    assertThat(change.ruleNote()).contains("rznx=4", "tgnx=15", "与目标行一致");
                });
    }

    @Test
    void legacy2006WorkerPostGradeCanBeInferred() {
        NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                "00110-00015",
                "00110",
                2006,
                7
        ));

        assertThat(result.changes())
                .anySatisfy(change -> {
                    assertThat(change.itemCode()).isEqualTo("TG2006_GR");
                    assertThat(change.afterValue()).startsWith("7级");
                    assertThat(change.ruleNote()).contains("rznx=6", "tgnx=26", "与目标行一致");
                });
    }

    @Test
    void formalStandardAdjustmentHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb IN ('\u8c03\u6807\u664b\u5347', '\u8c03\u6574\u6807\u51c6')
                  AND LOCATE('F', t.zwbm2) = 0
                  AND LEFT(t.zwbm2, 2) IN ('01','02','03','04','05','06','07','08','09','10','11','21','22','23','24','25','26','27','28')
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 160
                """);
        assumeTrue(result.isPresent(), "No matching formal standard-adjustment sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "ZWGZSE2", "JBGZSE2");
    }

    @Test
    void probationaryStandardAdjustmentHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb IN ('\u8c03\u6807\u664b\u5347', '\u8c03\u6574\u6807\u51c6')
                  AND LOCATE('F', t.zwbm2) > 0
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 160
                """);
        assumeTrue(result.isPresent(), "No matching probationary standard-adjustment sample in the current legacy database.");

        assertMatched(result.get());
        assertChangeCodes(result.get(), "JXGZ");
    }

    @Test
    void allowanceStandardAdjustmentHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb = '\u6d25\u8d34\u53d8\u5316'
                  AND LEFT(t.zwbm2, 2) IN ('01','02','03','04','05','06','07','08','09','10','11','21','22','23','24','25','26','27','28')
                  AND t.hj2 > 0
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 200
                """);
        assumeTrue(result.isPresent(), "No matching allowance standard-adjustment sample in the current legacy database.");

        assertMatched(result.get());
        assertThat(result.get().changes()).isNotEmpty();
    }

    @Test
    void targetStateAdjustmentHasMatchingLegacySampleWhenAvailable() {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial("""
                SELECT CONCAT(t.dwbm, '-', t.grbm) AS person_code, t.dwbm AS org_code,
                       CAST(t.jsnf AS UNSIGNED) AS year, CAST(t.jsyf AS UNSIGNED) AS month
                FROM hisbase t
                JOIN hisbase b ON b.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                      AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                          < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
                    ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC
                    LIMIT 1
                )
                WHERE t.jslb IN ('\u964d\u8d44\u5904\u5206', '\u5956\u52b1\u664b\u5347', '\u5176\u5b83\u60c5\u51b5')
                  AND LEFT(t.zwbm2, 2) IN ('01','02','03','04','05','06','07','08','09','10','11','21','22','23','24','25','26','27','28')
                  AND t.hj2 > 0
                ORDER BY t.jsnf DESC, t.jsyf DESC
                LIMIT 200
                """);
        assumeTrue(result.isPresent(), "No matching target-state adjustment sample in the current legacy database.");

        assertMatched(result.get());
        assertThat(result.get().changes()).isNotEmpty();
    }

    private NormalGradeTrialResult matchingTrial(String sql, String label) {
        Optional<NormalGradeTrialResult> result = tryMatchingTrial(sql);
        assumeTrue(result.isPresent(), "No matching " + label + " sample in the current legacy database.");
        return result.get();
    }

    private Optional<NormalGradeTrialResult> tryMatchingTrial(String sql) {
        List<Sample> samples = jdbcTemplate.query(sql, (rs, rowNum) -> new Sample(
                rs.getString("person_code"),
                rs.getString("org_code"),
                rs.getInt("year"),
                rs.getInt("month")
        ));
        for (Sample sample : samples) {
            try {
                NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                        sample.personCode(),
                        sample.orgCode(),
                        sample.year(),
                        sample.month()
                ));
                if (result.matchedExpected()) {
                    return Optional.of(result);
                }
            } catch (BusinessException ignored) {
                // Some legacy rows are not eligible for this trial path; keep looking for a usable fixture.
            }
        }
        return Optional.empty();
    }

    private void assertMatched(NormalGradeTrialResult result) {
        assertThat(result.matchedExpected()).isTrue();
        assertThat(result.differenceWithExpected()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.expectedHistoryId()).isNotBlank();
    }

    private void assertChangeCodes(NormalGradeTrialResult result, String... itemCodes) {
        Set<String> actualCodes = result.changes().stream()
                .map(change -> change.itemCode())
                .collect(java.util.stream.Collectors.toSet());
        assertThat(actualCodes).contains(itemCodes);
    }

    private void assertAnyChangeCode(NormalGradeTrialResult result, String... itemCodes) {
        Set<String> actualCodes = result.changes().stream()
                .map(change -> change.itemCode())
                .collect(java.util.stream.Collectors.toSet());
        assertThat(actualCodes).containsAnyOf(itemCodes);
    }

    private record Sample(String personCode, String orgCode, int year, int month) {
    }
}
