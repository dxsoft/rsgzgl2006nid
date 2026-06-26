package com.dx.rsgzgl.salary;

import com.dx.rsgzgl.salary.dto.SalaryGeneratedTimelineResult;
import com.dx.rsgzgl.salary.dto.SalaryTimelineResult;
import com.dx.rsgzgl.salary.mapper.LegacySalaryMapper;
import com.dx.rsgzgl.salary.service.SalaryGeneratedTimelineService;
import com.dx.rsgzgl.salary.service.SalaryTimelineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
class SalaryTimelineRegressionTests {

    @Autowired
    private SalaryTimelineService salaryTimelineService;

    @Autowired
    private SalaryGeneratedTimelineService salaryGeneratedTimelineService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private LegacySalaryMapper legacySalaryMapper;

    @Test
    void replayReturnsStepByStepComparisonForPerson() {
        SalaryTimelineResult result = salaryTimelineService.replay("00105-00008", 8);

        assertThat(result.personCode()).isEqualTo("00105-00008");
        assertThat(result.checkedCount()).isEqualTo(8);
        assertThat(result.matchedCount()).isEqualTo(8);
        assertThat(result.differentCount()).isZero();
        assertThat(result.errorCount()).isZero();
        assertThat(result.items()).hasSize(8);
        assertThat(result.items())
                .allSatisfy(item -> {
                    assertThat(item.year()).isGreaterThanOrEqualTo(2006);
                    assertThat(item.changeType()).isNotNull();
                    assertThat(item.status()).isIn("MATCH", "DIFF", "ERROR");
                });
    }

    @Test
    void replayKeepsPaddedHistoryIdBaselineForSameMonthRows() {
        SalaryTimelineResult result = salaryTimelineService.replay("024-00042", 4);

        assertThat(result.checkedCount()).isEqualTo(4);
        assertThat(result.errorCount()).isZero();
        assertThat(result.items().get(0).changeType()).isEqualTo("2006套改");
        assertThat(result.items().get(1).changeType()).isEqualTo("警衔津贴");
        assertThat(result.items().get(0).status()).isEqualTo("MATCH");
        assertThat(result.items().get(1).status()).isEqualTo("MATCH");
    }

    @Test
    void generatedTimelineComparesBaseInfoEventsWithHistoryChain() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("00105-00008", 8);

        assertThat(result.personCode()).isEqualTo("00105-00008");
        assertThat(result.coverage()).isNotEmpty();
        assertThat(result.expectedCount()).isEqualTo(8);
        assertThat(result.matchedCount()).isEqualTo(8);
        assertThat(result.differentCount()).isZero();
        assertThat(result.missingHistoryCount()).isZero();
        assertThat(result.errorCount()).isZero();
        assertThat(result.items()).hasSize(8);
        assertThat(result.items().get(0).changeType()).isEqualTo("2006套改");
        assertThat(result.items().get(1).changeType()).isEqualTo("职务变化");
        assertThat(result.items().get(2).changeType()).isEqualTo("正常档次");
        assertThat(result.items())
                .allSatisfy(item -> {
                    assertThat(item.historyId()).isNotBlank();
                    assertThat(item.status()).isEqualTo("MATCH");
                });
    }

    @Test
    void generatedTimelineAlignsPostEventsToNearbyHistoryTypeByPostCode() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("107-00001", 200);

        assumeTrue(
                result.items().stream().anyMatch(item -> "225673".equals(item.sourceId())),
                "107-00001 legacy post-change sample has been removed from the current database."
        );
        assertThat(result.items())
                .filteredOn(item -> "225673".equals(item.sourceId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.year()).isEqualTo(2015);
                    assertThat(item.month()).isEqualTo(2);
                    assertThat(item.historyId()).isEqualTo("92291");
                    assertThat(item.status()).isEqualTo("MATCH");
                });
        assertThat(result.items())
                .filteredOn(item -> "225672".equals(item.sourceId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.year()).isEqualTo(2017);
                    assertThat(item.month()).isEqualTo(3);
                    assertThat(item.historyId()).isEqualTo("92282");
                    assertThat(item.status()).isEqualTo("MATCH");
                });
        assertThat(result.items())
                .filteredOn(item -> "225671".equals(item.sourceId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.year()).isEqualTo(2019);
                    assertThat(item.month()).isEqualTo(4);
                    assertThat(item.historyId()).isEqualTo("92294");
                    assertThat(item.status()).isEqualTo("MATCH");
                });
    }

    @Test
    void generatedTimelineLetsEducationEventOwnEducationHistoryRow() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("045-00260", 200);

        assertThat(result.items())
                .filteredOn(item -> "413159".equals(item.sourceId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.year()).isEqualTo(2015);
                    assertThat(item.month()).isEqualTo(8);
                    assertThat(item.historyId()).isEqualTo("77294");
                    assertThat(item.status()).isEqualTo("MATCH");
                });
        assertThat(result.items())
                .noneMatch(item -> "222888".equals(item.sourceId()) && "MISSING_HISTORY".equals(item.status()));
    }

    @Test
    void generatedTimelineSkipsEducationRecordWhenCurrentSalaryAlreadyMeetsEducationStandard() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("001-00252", 200);

        assertThat(result.items())
                .noneMatch(item -> "407421".equals(item.sourceId()) || "412523".equals(item.sourceId()));
        assertThat(result.missingHistoryCount()).isZero();
    }

    @Test
    void generatedTimelineDoesNotLetPostEventOwn2006ConversionRow() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("024-01506", 200);

        assertThat(result.items())
                .filteredOn(item -> "5336".equals(item.sourceId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.year()).isEqualTo(2006);
                    assertThat(item.month()).isEqualTo(7);
                    assertThat(item.historyId()).isEqualTo("121920");
                    assertThat(item.status()).isEqualTo("MATCH");
                });
        assertThat(result.items())
                .noneMatch(item -> "232623".equals(item.sourceId()) && "MISSING_HISTORY".equals(item.status()));
    }

    @Test
    void generatedTimelineMatchesCorrected2006ConversionForProbationaryHistory() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("01409-00171", 20);

        assertThat(result.items())
                .filteredOn(item -> "34000".equals(item.sourceId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.status()).isEqualTo("MATCH");
                    assertThat(item.historyId()).isNotBlank();
                });
    }

    @Test
    void generatedTimelineSkips2006ConversionForJulyStartOrJulyRegularization() {
        SalaryGeneratedTimelineResult julyStart = salaryGeneratedTimelineService.generateAndCompare("001-00276", 20);
        SalaryGeneratedTimelineResult julyRegularization = salaryGeneratedTimelineService.generateAndCompare("01414-00040", 20);

        assertThat(julyStart.items())
                .noneMatch(item -> "dryjbxx".equals(item.source()) && item.year() == 2006 && item.month() == 7);
        assertThat(julyRegularization.items())
                .noneMatch(item -> "dryjbxx".equals(item.source()) && item.year() == 2006 && item.month() == 7);
    }

    @Test
    void generatedTimelineDoesNotDuplicateRegularizationAsPostChange() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("00105-00019", 120);

        assertThat(result.items())
                .noneMatch(item -> "241053".equals(item.sourceId()) && "MISSING_HISTORY".equals(item.status()));
        assertThat(result.missingHistoryCount()).isZero();
    }

    @Test
    void generatedTimelineKeepsRankConversionWhenSameMonthRegularizationPostExists() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("001-00270", 120);

        assertThat(result.items())
                .noneMatch(item -> "212097".equals(item.sourceId()) && "MISSING_HISTORY".equals(item.status()));
        assertThat(result.missingHistoryCount()).isZero();
    }

    @Test
    void generatedTimelineDoesNotDuplicatePostChangeWhenTransferFixesSalary() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("002-00197", 240);

        assertThat(result.items())
                .noneMatch(item -> "226605".equals(item.sourceId()) && "MISSING_HISTORY".equals(item.status()));
    }

    @Test
    void generatedTimelineDoesNotDuplicateRankConversionWhenTransferFixesSalary() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("044-00404", 260);

        assertThat(result.items())
                .noneMatch(item -> "211088".equals(item.sourceId()) && "MISSING_HISTORY".equals(item.status()));
    }

    @Test
    void generatedTimelineDoesNotDuplicatePostChangeWhenSalaryReductionFixesSalary() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("02605-00021", 240);

        assertThat(result.items())
                .noneMatch(item -> "216170".equals(item.sourceId()) && "MISSING_HISTORY".equals(item.status()));
        assertThat(result.missingHistoryCount()).isZero();
    }

    @Test
    void generatedTimelineDoesNotDuplicatePostChangeWhenOtherSituationIsSalaryReductionPunishment() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("02401-00236", 260);

        assertThat(result.items())
                .noneMatch(item -> "219261".equals(item.sourceId()) && "MISSING_HISTORY".equals(item.status()));
    }

    @Test
    void generatedTimelineSkipsPostChangeWhenPostAlreadyExecutedByConversionEducationFloor() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("0010701-00007", 260);

        assertThat(result.items())
                .noneMatch(item -> "217005".equals(item.sourceId()) && "MISSING_HISTORY".equals(item.status()));
        assertThat(result.missingHistoryCount()).isZero();
    }

    @Test
    void generatedTimelineKeepsNormalGradeOwnedByAssessmentInsteadOfPostRecord() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("00111-00066", 260);

        assertThat(result.items())
                .filteredOn(item -> "dndkh".equals(item.source()) && "2006".equals(item.sourceId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.year()).isEqualTo(2007);
                    assertThat(item.month()).isEqualTo(1);
                    assertThat(item.status()).isEqualTo("MATCH");
                    assertThat(item.historyId()).isEqualTo("79026090-EE30-430F-9D45-04D5DCDD881F");
                });
        assertThat(result.items())
                .noneMatch(item -> "241065".equals(item.sourceId()) && "正常档次".equals(item.changeType()));
        assertThat(result.missingHistoryCount()).isZero();
    }

    @Test
    void generatedTimelineKeepsNormalLevelOwnedByAssessmentInsteadOfPostRecord() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("001-00263", 260);

        assertThat(result.items())
                .noneMatch(item -> "210614".equals(item.sourceId()) && "正常级别".equals(item.changeType()));
        assertThat(result.items())
                .noneMatch(item -> "210614".equals(item.sourceId()) && "MISSING_HISTORY".equals(item.status()));
        assertThat(result.missingHistoryCount()).isZero();
    }

    @Test
    void generatedTimelineDoesNotReportHighestLevelGradeTurnoverAsMissingNormalLevel() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("001-00263", 260);

        assertThat(result.items())
                .filteredOn(item -> "dndkh".equals(item.source()) && item.sourceId().endsWith(":level-base"))
                .allSatisfy(item -> {
                    assertThat(item.changeType()).isEqualTo("姝ｅ父绾у埆");
                    assertThat(item.status()).isNotEqualTo("MISSING_HISTORY");
                });
        assertThat(result.missingHistoryCount()).isZero();
    }

    @Test
    void generatedTimelineSkipsPostEventWhenSameMonthPunishmentReducesSalary() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("02602-00167", 260);

        assertThat(result.items())
                .noneMatch(item -> "231914".equals(item.sourceId()));
        assertThat(result.missingHistoryCount()).isZero();
    }

    @Test
    void generatedTimelineCreatesSalaryReductionPunishmentFromRewardPunishmentInfo() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("02602-00167", 260);

        assertThat(result.items())
                .filteredOn(item -> "hjxx".equals(item.source()) && "7775".equals(item.sourceId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.year()).isEqualTo(2024);
                    assertThat(item.month()).isEqualTo(7);
                    assertThat(item.changeType()).isEqualTo("降资处分");
                    assertThat(item.historyId()).isEqualTo("118435");
                    assertThat(item.status()).isEqualTo("MATCH");
                });
    }

    @Test
    void generatedTimelineAlignsRewardPromotionFromRewardPunishmentInfoToHistoryMonth() {
        SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare("00806-00089", 260);

        assertThat(result.items())
                .filteredOn(item -> "hjxx".equals(item.source()) && "7806".equals(item.sourceId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.year()).isEqualTo(2024);
                    assertThat(item.month()).isEqualTo(10);
                    assertThat(item.changeType()).isEqualTo("奖励晋升");
                    assertThat(item.historyId()).isEqualTo("747DBF5E-0339-4E6D-927A-C81C3140EE4D");
                    assertThat(item.status()).isEqualTo("MATCH");
                });
    }

    @Test
    void generatedTimelineCreatesJudicialConversionWhenPostEntersJudicialPrefix() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode
                FROM (
                    SELECT z.*,
                           LAG(LEFT(TRIM(zwbm), 2)) OVER (PARTITION BY dwbm, grbm ORDER BY srny, id) AS previousPrefix
                    FROM dryzwbh z
                    WHERE TRIM(COALESCE(zwbm, '')) <> ''
                      AND TRIM(COALESCE(srny, '')) <> ''
                      AND CAST(REPLACE(TRIM(srny), '.', '') AS UNSIGNED) >= 200607
                ) posts
                WHERE LEFT(TRIM(zwbm), 2) = '03'
                  AND COALESCE(previousPrefix, '') <> '03'
                ORDER BY srny, id
                LIMIT 200
                """);
        assumeTrue(!rows.isEmpty(), "No judicial-prefix entrance sample in the current legacy database.");

        boolean found = false;
        for (Map<String, Object> row : rows) {
            String personCode = String.valueOf(row.get("personCode"));
            String[] parts = personCode.split("-", 2);
            if (parts.length != 2) {
                continue;
            }
            found = legacySalaryMapper.findExpectedEventsFromBaseInfo(parts[0], parts[1]).stream()
                    .anyMatch(item -> "dryzwbh".equals(item.source())
                            && "\u6cd5\u68c0\u5957\u6539".equals(item.changeType()));
            if (found) {
                break;
            }
        }
        assumeTrue(found, "No generated judicial conversion candidate in the current legacy database sample.");
    }

    @Test
    void generatedTimelineCreatesRankAllowanceEventFromRankTable() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode
                FROM hisbase
                WHERE TRIM(jslb) IN (
                    '\u8b66\u8854\u53d8\u5316', '\u8b66\u8854\u6d25\u8d34',
                    '\u6cd5\u5b98\u7b49\u7ea7', '\u5ba1\u5224\u6d25\u8d34',
                    '\u68c0\u5bdf\u7b49\u7ea7', '\u68c0\u5bdf\u6d25\u8d34',
                    '\u76d1\u5bdf\u5b98\u7b49\u7ea7', '\u76d1\u5bdf\u7b49\u7ea7', '\u76d1\u5bdf\u6d25\u8d34'
                )
                ORDER BY jsnf DESC, jsyf DESC, dwbm, grbm
                LIMIT 200
                """);
        assumeTrue(!rows.isEmpty(), "No rank or allowance history sample in the current legacy database.");

        boolean found = false;
        for (Map<String, Object> row : rows) {
            String personCode = String.valueOf(row.get("personCode"));
            SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare(personCode, 1000);
            found = result.items().stream()
                    .anyMatch(item -> "jx".equals(item.source())
                            && item.sourceId().endsWith(":rank")
                            && isRankAllowanceChangeType(item.changeType())
                            && "MATCH".equals(item.status()));
            if (found) {
                break;
            }
        }
        assumeTrue(found, "No generated rank or allowance candidate in the current legacy database sample.");
    }

    private boolean isRankAllowanceChangeType(String changeType) {
        return List.of(
                "\u8b66\u8854\u53d8\u5316", "\u8b66\u8854\u6d25\u8d34",
                "\u6cd5\u5b98\u7b49\u7ea7", "\u5ba1\u5224\u6d25\u8d34",
                "\u68c0\u5bdf\u7b49\u7ea7", "\u68c0\u5bdf\u6d25\u8d34",
                "\u76d1\u5bdf\u5b98\u7b49\u7ea7", "\u76d1\u5bdf\u7b49\u7ea7", "\u76d1\u5bdf\u6d25\u8d34"
        ).contains(changeType);
    }
}
