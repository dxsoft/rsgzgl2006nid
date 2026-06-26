package com.dx.rsgzgl.salary.service.impl;

import com.dx.rsgzgl.salary.dto.NormalGradeTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeTrialResult;
import com.dx.rsgzgl.salary.dto.SalaryExpectedEventCandidate;
import com.dx.rsgzgl.salary.dto.SalaryGeneratedTimelineCoverage;
import com.dx.rsgzgl.salary.dto.SalaryGeneratedTimelineItem;
import com.dx.rsgzgl.salary.dto.SalaryGeneratedTimelineResult;
import com.dx.rsgzgl.salary.dto.SalaryHistoryLinkItem;
import com.dx.rsgzgl.salary.dto.SalaryRuleChange;
import com.dx.rsgzgl.salary.mapper.LegacySalaryMapper;
import com.dx.rsgzgl.salary.service.NormalGradeTrialService;
import com.dx.rsgzgl.salary.service.SalaryGeneratedTimelineService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DefaultSalaryGeneratedTimelineService implements SalaryGeneratedTimelineService {

    private static final String STATUS_MATCH = "MATCH";
    private static final String STATUS_DIFF = "DIFF";
    private static final String STATUS_ERROR = "ERROR";
    private static final String STATUS_MISSING_HISTORY = "MISSING_HISTORY";
    private static final String SOURCE_ASSESSMENT = "dndkh";
    private static final String SOURCE_POST = "dryzwbh";
    private static final String CHANGE_2006_CONVERSION = "\u0032\u0030\u0030\u0036\u5957\u6539";
    private static final String CHANGE_POST_CHANGE = "\u804c\u52a1\u53d8\u5316";
    private static final String CHANGE_POLICE_CONVERSION = "\u8b66\u5458\u5957\u6539";
    private static final String CHANGE_EDUCATION_CHANGE = "\u5b66\u5386\u53d8\u5316";
    private static final String CHANGE_NORMAL_GRADE = "正常档次";
    private static final String CHANGE_NORMAL_LEVEL = "正常级别";
    private static final String CHANGE_LEVEL_ROLLING = "\u7ea7\u522b\u6eda\u52a8";
    private static final String CHANGE_REGULARIZATION = "\u8f6c\u6b63\u5b9a\u7ea7";
    private static final String CHANGE_CIVIL_RANK_CONVERSION = "\u804c\u7ea7\u5957\u6539";
    private static final String CHANGE_CIVIL_RANK_PROMOTION = "\u804c\u7ea7\u664b\u5347";
    private static final String CHANGE_JUDICIAL_CONVERSION = "\u6cd5\u68c0\u5957\u6539";
    private static final String CHANGE_POLICE_RANK_CHANGE = "\u8b66\u8854\u53d8\u5316";
    private static final String CHANGE_POLICE_RANK_ALLOWANCE = "\u8b66\u8854\u6d25\u8d34";
    private static final String CHANGE_JUDGE_RANK = "\u6cd5\u5b98\u7b49\u7ea7";
    private static final String CHANGE_JUDGE_ALLOWANCE = "\u5ba1\u5224\u6d25\u8d34";
    private static final String CHANGE_PROSECUTOR_RANK = "\u68c0\u5bdf\u7b49\u7ea7";
    private static final String CHANGE_PROSECUTOR_ALLOWANCE = "\u68c0\u5bdf\u6d25\u8d34";
    private static final String CHANGE_SUPERVISOR_RANK = "\u76d1\u5bdf\u5b98\u7b49\u7ea7";
    private static final String CHANGE_SUPERVISOR_RANK_LEGACY = "\u76d1\u5bdf\u7b49\u7ea7";
    private static final String CHANGE_SUPERVISOR_ALLOWANCE = "\u76d1\u5bdf\u6d25\u8d34";
    private static final String CHANGE_SALARY_REDUCTION_PUNISHMENT = "\u964d\u8d44\u5904\u5206";
    private static final String CHANGE_REWARD_PROMOTION = "\u5956\u52b1\u664b\u5347";
    private static final Set<String> NORMAL_LEVEL_PREFIXES = Set.of(
            "01", "02", "04", "21", "22", "23", "24", "25", "26", "27", "28"
    );
    private static final Set<String> GENERATED_CHANGE_TYPES = Set.of(
            "2006套改",
            "职务变化",
            "警员套改",
            "学历变化",
            CHANGE_CIVIL_RANK_CONVERSION,
            CHANGE_CIVIL_RANK_PROMOTION,
            CHANGE_JUDICIAL_CONVERSION,
            CHANGE_POLICE_RANK_CHANGE,
            CHANGE_POLICE_RANK_ALLOWANCE,
            CHANGE_JUDGE_RANK,
            CHANGE_JUDGE_ALLOWANCE,
            CHANGE_PROSECUTOR_RANK,
            CHANGE_PROSECUTOR_ALLOWANCE,
            CHANGE_SUPERVISOR_RANK,
            CHANGE_SUPERVISOR_RANK_LEGACY,
            CHANGE_SUPERVISOR_ALLOWANCE,
            CHANGE_NORMAL_LEVEL,
            CHANGE_LEVEL_ROLLING,
            CHANGE_SALARY_REDUCTION_PUNISHMENT,
            CHANGE_REWARD_PROMOTION,
            "正常档次"
    );

    private final LegacySalaryMapper legacySalaryMapper;
    private final NormalGradeTrialService normalGradeTrialService;
    private final PersonCodeParser personCodeParser;
    private final SalaryHistoryChainOrderer historyChainOrderer;
    private final JdbcTemplate jdbcTemplate;

    public DefaultSalaryGeneratedTimelineService(
            LegacySalaryMapper legacySalaryMapper,
            NormalGradeTrialService normalGradeTrialService,
            PersonCodeParser personCodeParser,
            SalaryHistoryChainOrderer historyChainOrderer,
            JdbcTemplate jdbcTemplate
    ) {
        this.legacySalaryMapper = legacySalaryMapper;
        this.normalGradeTrialService = normalGradeTrialService;
        this.personCodeParser = personCodeParser;
        this.historyChainOrderer = historyChainOrderer;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SalaryGeneratedTimelineResult generateAndCompare(String personCode, Integer limit) {
        PersonCodeParts parts = personCodeParser.parse(personCode);
        List<SalaryHistoryLinkItem> history = historyChainOrderer.orderBySidChain(
                legacySalaryMapper.findHistoryLinks(parts.orgCode(), parts.personNo())
        );
        Map<HistoryKey, List<SalaryHistoryLinkItem>> historyByKey = historyByKey(history);
        List<SalaryExpectedEventCandidate> baseEvents = addAssessmentLevelEvents(
                parts,
                legacySalaryMapper.findExpectedEventsFromBaseInfo(parts.orgCode(), parts.personNo())
        );
        List<SalaryExpectedEventCandidate> expectedEvents = expandAssessmentEvents(
                baseEvents,
                historyByKey
        );
        expectedEvents = alignPostEventsToNearbyHistory(expectedEvents, historyByKey);
        expectedEvents = alignPostEventsToNearbyHistoryType(expectedEvents, historyByKey);
        expectedEvents = alignRankEventsToNearbyHistoryType(expectedEvents, historyByKey);
        expectedEvents = alignEducationEventsToNearbyHistoryType(expectedEvents, historyByKey);
        expectedEvents = alignHjxxEventsToNearbyHistory(expectedEvents, historyByKey);
        expectedEvents = removeEducationEventsWithoutSalaryImpact(expectedEvents, historyByKey);
        expectedEvents = removePostEventsCoveredByBroaderHistory(expectedEvents, historyByKey);
        expectedEvents = preferPostEventsMatchingHistory(expectedEvents, historyByKey);
        int safeLimit = limit == null ? expectedEvents.size() : Math.max(1, limit);
        expectedEvents = expectedEvents.stream().limit(safeLimit).toList();

        Map<String, String> previousHistoryIdById = previousHistoryIds(history);
        Set<String> usedHistoryIds = new HashSet<>();

        List<SalaryGeneratedTimelineItem> items = new ArrayList<>();
        int matched = 0;
        int different = 0;
        int missing = 0;
        int errors = 0;

        for (SalaryExpectedEventCandidate event : expectedEvents) {
            SalaryHistoryLinkItem historyRow = takeFirstUnused(historyByKey.get(HistoryKey.of(event)), usedHistoryIds);
            if (historyRow == null) {
                missing++;
                items.add(new SalaryGeneratedTimelineItem(
                        event.source(),
                        event.sourceId(),
                        event.year(),
                        event.month(),
                        event.changeType(),
                        event.note(),
                        null,
                        BigDecimal.ZERO,
                        null,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        false,
                        STATUS_MISSING_HISTORY,
                        diagnosticMessage(parts, event, null, "基础信息推导出应发生变动，但历史链中没有同年月同类型记录。"),
                        List.of()
                ));
                continue;
            }

            try {
                NormalGradeTrialResult trial = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                        event.personCode(),
                        parts.orgCode(),
                        event.year(),
                        event.month(),
                        event.changeType(),
                        previousHistoryIdById.get(historyRow.id())
                ));
                String status = trial.matchedExpected() ? STATUS_MATCH : STATUS_DIFF;
                if (trial.matchedExpected()) {
                    matched++;
                } else {
                    different++;
                }
                items.add(new SalaryGeneratedTimelineItem(
                        event.source(),
                        event.sourceId(),
                        event.year(),
                        event.month(),
                        event.changeType(),
                        event.note(),
                        historyRow.id(),
                        historyRow.totalAmount(),
                        trial.baselineHistoryId(),
                        trial.calculatedTotalAmount(),
                        trial.differenceWithExpected(),
                        trial.matchedExpected(),
                        status,
                        trial.matchedExpected() ? "" : diagnosticMessage(parts, event, historyRow, ""),
                        trial.changes()
                ));
            } catch (RuntimeException error) {
                errors++;
                items.add(new SalaryGeneratedTimelineItem(
                        event.source(),
                        event.sourceId(),
                        event.year(),
                        event.month(),
                        event.changeType(),
                        event.note(),
                        historyRow.id(),
                        historyRow.totalAmount(),
                        previousHistoryIdById.get(historyRow.id()),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        false,
                        STATUS_ERROR,
                        diagnosticMessage(parts, event, historyRow, error.getMessage()),
                        List.<SalaryRuleChange>of()
                ));
            }
        }

        int unsupportedHistoryCount = (int) history.stream()
                .filter(row -> !GENERATED_CHANGE_TYPES.contains(row.changeType()))
                .count();
        List<SalaryGeneratedTimelineCoverage> coverage = generatedCoverage(expectedEvents, history, usedHistoryIds);

        return new SalaryGeneratedTimelineResult(
                personCode,
                items.size(),
                matched,
                different,
                missing,
                errors,
                unsupportedHistoryCount,
                coverage,
                items
        );
    }

    private List<SalaryGeneratedTimelineCoverage> generatedCoverage(
            List<SalaryExpectedEventCandidate> expectedEvents,
            List<SalaryHistoryLinkItem> history,
            Set<String> usedHistoryIds
    ) {
        Map<String, CoverageCounter> counters = new LinkedHashMap<>();
        for (SalaryExpectedEventCandidate event : expectedEvents) {
            coverageCounter(counters, event.changeType()).expected += 1;
        }
        for (SalaryHistoryLinkItem row : history) {
            CoverageCounter counter = coverageCounter(counters, row.changeType());
            if (usedHistoryIds.contains(row.id())) {
                counter.matchedHistory += 1;
            } else if (!GENERATED_CHANGE_TYPES.contains(row.changeType())) {
                counter.unsupportedHistory += 1;
            }
        }
        List<SalaryGeneratedTimelineCoverage> coverage = new ArrayList<>();
        for (Map.Entry<String, CoverageCounter> entry : counters.entrySet()) {
            CoverageCounter counter = entry.getValue();
            coverage.add(new SalaryGeneratedTimelineCoverage(
                    entry.getKey(),
                    counter.expected,
                    counter.matchedHistory,
                    Math.max(0, counter.expected - counter.matchedHistory),
                    counter.unsupportedHistory
            ));
        }
        return coverage;
    }

    private CoverageCounter coverageCounter(Map<String, CoverageCounter> counters, String changeType) {
        return counters.computeIfAbsent(trim(changeType), ignored -> new CoverageCounter());
    }

    private Map<String, String> previousHistoryIds(List<SalaryHistoryLinkItem> history) {
        Map<String, String> previousById = new HashMap<>();
        String previous = null;
        for (SalaryHistoryLinkItem row : history) {
            previousById.put(row.id(), previous);
            previous = row.id();
        }
        return previousById;
    }

    private List<SalaryExpectedEventCandidate> expandAssessmentEvents(
            List<SalaryExpectedEventCandidate> events,
            Map<HistoryKey, List<SalaryHistoryLinkItem>> historyByKey
    ) {
        List<SalaryExpectedEventCandidate> expanded = new ArrayList<>();
        Set<String> explicitAssessmentLevels = new HashSet<>();
        for (SalaryExpectedEventCandidate event : events) {
            if (SOURCE_ASSESSMENT.equals(event.source()) && CHANGE_NORMAL_LEVEL.equals(event.changeType())) {
                explicitAssessmentLevels.add(event.year() + "-" + event.month());
            }
        }
        for (SalaryExpectedEventCandidate event : events) {
            if (!SOURCE_ASSESSMENT.equals(event.source()) || !CHANGE_NORMAL_GRADE.equals(event.changeType())) {
                expanded.add(event);
                continue;
            }
            boolean hasLevelRolling = hasHistoryEvent(historyByKey, event.year(), event.month(), CHANGE_LEVEL_ROLLING);
            boolean hasNormalGrade = hasHistoryEvent(historyByKey, event.year(), event.month(), CHANGE_NORMAL_GRADE);
            boolean hasLevelPromotion = hasHistoryEvent(historyByKey, event.year(), event.month(), CHANGE_NORMAL_LEVEL);
            if (!hasLevelRolling && !hasLevelPromotion && !hasNormalGrade) {
                continue;
            }
            if (hasLevelRolling) {
                expanded.add(new SalaryExpectedEventCandidate(
                        event.source(),
                        event.sourceId() + ":rolling",
                        event.personCode(),
                        event.year(),
                        event.month(),
                        CHANGE_LEVEL_ROLLING,
                        event.note() + "\uff1b\u5386\u53f2\u94fe\u663e\u793a\u540c\u6708\u53d1\u751f\u7ea7\u522b\u6eda\u52a8"
                ));
            }
            if (hasLevelPromotion) {
                String levelKey = event.year() + "-" + event.month();
                if (!explicitAssessmentLevels.contains(levelKey)) {
                    expanded.add(new SalaryExpectedEventCandidate(
                        event.source(),
                        event.sourceId() + ":level",
                        event.personCode(),
                        event.year(),
                        event.month(),
                        CHANGE_NORMAL_LEVEL,
                        event.note() + "；历史链级别起算状态满足5年晋升级别"
                    ));
                }
            }
            if (hasNormalGrade) {
                expanded.add(event);
            }
        }
        return expanded;
    }

    private List<SalaryExpectedEventCandidate> alignPostEventsToNearbyHistory(
            List<SalaryExpectedEventCandidate> events,
            Map<HistoryKey, List<SalaryHistoryLinkItem>> historyByKey
    ) {
        List<SalaryExpectedEventCandidate> aligned = new ArrayList<>();
        for (SalaryExpectedEventCandidate event : events) {
            if (!SOURCE_POST.equals(event.source())
                    || hasHistoryEvent(historyByKey, event.year(), event.month(), event.changeType())) {
                aligned.add(event);
                continue;
            }
            YearMonth next = addMonths(event.year(), event.month(), 1);
            if (hasHistoryEvent(historyByKey, next.year(), next.month(), event.changeType())) {
                aligned.add(copyEventAt(event, next, "\uff1b\u5386\u53f2\u94fe\u540c\u7c7b\u4e8b\u4ef6\u5728\u6b21\u6708\uff0c\u5df2\u5bf9\u9f50\u6267\u884c\u6708"));
                continue;
            }
            YearMonth previous = addMonths(event.year(), event.month(), -1);
            if (hasHistoryEvent(historyByKey, previous.year(), previous.month(), event.changeType())) {
                aligned.add(copyEventAt(event, previous, "\uff1b\u5386\u53f2\u94fe\u540c\u7c7b\u4e8b\u4ef6\u5728\u4e0a\u6708\uff0c\u5df2\u5bf9\u9f50\u6267\u884c\u6708"));
                continue;
            }
            aligned.add(event);
        }
        return aligned;
    }

    private List<SalaryExpectedEventCandidate> addAssessmentLevelEvents(
            PersonCodeParts parts,
            List<SalaryExpectedEventCandidate> events
    ) {
        List<SalaryExpectedEventCandidate> result = new ArrayList<>(events);
        Set<HistoryKey> existingKeys = new HashSet<>();
        for (SalaryExpectedEventCandidate event : events) {
            existingKeys.add(HistoryKey.of(event));
        }
        List<Map<String, Object>> assessmentRows = jdbcTemplate.queryForList("""
                SELECT CAST(TRIM(khnd) AS SIGNED) AS khnd,
                       TRIM(COALESCE(khjg, '')) AS khjg
                FROM dndkh
                WHERE dwbm = ?
                  AND grbm = ?
                  AND TRIM(khnd) REGEXP '^[0-9]{4}$'
                  AND CAST(TRIM(khnd) AS SIGNED) >= 2006
                  AND TRIM(khjg) IN ('\u4f18\u79c0', '\u79f0\u804c', '\u5408\u683c')
                ORDER BY CAST(TRIM(khnd) AS SIGNED)
                """, parts.orgCode(), parts.personNo());
        int qualifiedCount = 0;
        for (Map<String, Object> row : assessmentRows) {
            int assessmentYear = parseInt(row.get("khnd"));
            if (assessmentYear <= 0) {
                continue;
            }
            qualifiedCount += 1;
            if (qualifiedCount < 5 || qualifiedCount % 5 != 0) {
                continue;
            }
            int targetYear = assessmentYear + 1;
            int targetMonth = 1;
            String prefix = latestSalaryPostPrefixAt(parts, targetYear, targetMonth);
            if (!NORMAL_LEVEL_PREFIXES.contains(prefix)) {
                continue;
            }
            if (!normalLevelPromotionWouldApply(parts, targetYear, targetMonth)) {
                continue;
            }
            SalaryExpectedEventCandidate event = new SalaryExpectedEventCandidate(
                    SOURCE_ASSESSMENT,
                    assessmentYear + ":level-base",
                    parts.orgCode() + "-" + parts.personNo(),
                    targetYear,
                    targetMonth,
                    CHANGE_NORMAL_LEVEL,
                    "\u5e74\u5ea6\u8003\u6838\u7d2f\u8ba1\u7b2c" + qualifiedCount + "\u4e2a\u5408\u683c\u5e74\u5ea6\uff0c\u6b21\u5e741\u6708\u6b63\u5e38\u7ea7\u522b\u664b\u5347"
            );
            if (existingKeys.add(HistoryKey.of(event))) {
                result.add(event);
            }
        }
        return result.stream()
                .sorted(Comparator
                        .comparingInt(SalaryExpectedEventCandidate::year)
                        .thenComparingInt(SalaryExpectedEventCandidate::month)
                        .thenComparingInt(event -> eventOrder(event.changeType()))
                        .thenComparing(SalaryExpectedEventCandidate::source)
                        .thenComparing(SalaryExpectedEventCandidate::sourceId))
                .toList();
    }

    private int eventOrder(String changeType) {
        String value = trim(changeType);
        if (CHANGE_CIVIL_RANK_CONVERSION.equals(value)) {
            return 16;
        }
        if (CHANGE_CIVIL_RANK_PROMOTION.equals(value)) {
            return 17;
        }
        if (CHANGE_JUDICIAL_CONVERSION.equals(value)) {
            return 18;
        }
        if (CHANGE_2006_CONVERSION.equals(value) || value.contains("2006")) {
            return 0;
        }
        if (CHANGE_POST_CHANGE.equals(value) || "鑱屽姟鍙樺寲".equals(value)) {
            return 10;
        }
        if (CHANGE_POLICE_CONVERSION.equals(value) || "璀﹀憳濂楁敼".equals(value)) {
            return 15;
        }
        if (CHANGE_EDUCATION_CHANGE.equals(value) || value.contains("\u5b66\u5386")) {
            return 20;
        }
        if (isRankAllowanceChange(value)) {
            return 21;
        }
        if (CHANGE_NORMAL_LEVEL.equals(value)) {
            return 30;
        }
        if (CHANGE_NORMAL_GRADE.equals(value)) {
            return 40;
        }
        return 99;
    }

    private String latestSalaryPostPrefixAt(PersonCodeParts parts, int year, int month) {
        List<String> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(COALESCE(zwbm2, ''))
                FROM hisbase
                WHERE dwbm = ? AND grbm = ?
                  AND (CAST(TRIM(jsnf) AS SIGNED) * 100 + CAST(TRIM(jsyf) AS SIGNED)) <= ?
                  AND TRIM(COALESCE(zwbm2, '')) <> ''
                  AND COALESCE(hj2, 0) > 0
                ORDER BY CAST(TRIM(jsnf) AS SIGNED) DESC,
                         CAST(TRIM(jsyf) AS SIGNED) DESC,
                         COALESCE(hj2, 0) DESC,
                         id DESC
                LIMIT 1
                """, String.class, parts.orgCode(), parts.personNo(), year * 100 + month);
        String postCode = rows.isEmpty() ? baseInfoPostCode(parts) : trim(rows.get(0));
        return postPrefix(postCode);
    }

    private String baseInfoPostCode(PersonCodeParts parts) {
        List<String> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(COALESCE(zwbm, zjbm, ''))
                FROM dryjbxx
                WHERE dwbm = ? AND grbm = ?
                LIMIT 1
                """, String.class, parts.orgCode(), parts.personNo());
        return rows.isEmpty() ? "" : trim(rows.get(0));
    }

    private boolean normalLevelPromotionWouldApply(PersonCodeParts parts, int year, int month) {
        try {
            NormalGradeTrialResult trial = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                    parts.orgCode() + "-" + parts.personNo(),
                    parts.orgCode(),
                    year,
                    month,
                    CHANGE_NORMAL_LEVEL,
                    null
            ));
            return trial.changes().stream()
                    .map(SalaryRuleChange::ruleNote)
                    .map(this::trim)
                    .anyMatch(note -> note.startsWith("\u7ea7\u522b\u664b\u5347\uff1a"));
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private List<SalaryExpectedEventCandidate> alignPostEventsToNearbyHistoryType(
            List<SalaryExpectedEventCandidate> events,
            Map<HistoryKey, List<SalaryHistoryLinkItem>> historyByKey
    ) {
        List<SalaryExpectedEventCandidate> aligned = new ArrayList<>();
        for (SalaryExpectedEventCandidate event : events) {
            if (!SOURCE_POST.equals(event.source())
                    || hasHistoryEvent(historyByKey, event.year(), event.month(), event.changeType())) {
                aligned.add(event);
                continue;
            }
            String sourcePostCode = postCode(event.sourceId());
            if (!StringUtils.hasText(sourcePostCode)) {
                aligned.add(event);
                continue;
            }
            HistoryTypeCandidate nearby = nearbyGeneratedHistoryWithPostCodeRelaxed(
                    event.personCode(),
                    event.year(),
                    event.month(),
                    sourcePostCode
            );
            if (nearby == null || event.changeType().equals(nearby.changeType())) {
                aligned.add(event);
                continue;
            }
            aligned.add(new SalaryExpectedEventCandidate(
                    event.source(),
                    event.sourceId(),
                    event.personCode(),
                    nearby.year(),
                    nearby.month(),
                    nearby.changeType(),
                    event.note() + "\uff1b\u5386\u53f2\u94fe\u540c\u5c97\u4f4d\u7f16\u7801\u4e8b\u4ef6\u7c7b\u578b\u4e3a" + nearby.changeType() + "\uff0c\u5df2\u5bf9\u9f50"
            ));
        }
        return aligned;
    }

    private List<SalaryExpectedEventCandidate> alignEducationEventsToNearbyHistoryType(
            List<SalaryExpectedEventCandidate> events,
            Map<HistoryKey, List<SalaryHistoryLinkItem>> historyByKey
    ) {
        List<SalaryExpectedEventCandidate> aligned = new ArrayList<>();
        for (SalaryExpectedEventCandidate event : events) {
            if (!"dxl".equals(event.source())
                    || hasHistoryEvent(historyByKey, event.year(), event.month(), event.changeType())) {
                aligned.add(event);
                continue;
            }
            HistoryTypeCandidate nearby = nearbyEducationHistory(event.personCode(), event.year(), event.month());
            if (nearby == null || event.changeType().equals(nearby.changeType())) {
                aligned.add(event);
                continue;
            }
            aligned.add(new SalaryExpectedEventCandidate(
                    event.source(),
                    event.sourceId(),
                    event.personCode(),
                    nearby.year(),
                    nearby.month(),
                    nearby.changeType(),
                    event.note() + "\uff1b\u5386\u53f2\u94fe\u5b66\u5386\u53d8\u5316\u7c7b\u578b\u5b57\u9762\u5df2\u5bf9\u9f50"
            ));
        }
        return aligned;
    }

    private List<SalaryExpectedEventCandidate> alignRankEventsToNearbyHistoryType(
            List<SalaryExpectedEventCandidate> events,
            Map<HistoryKey, List<SalaryHistoryLinkItem>> historyByKey
    ) {
        List<SalaryExpectedEventCandidate> aligned = new ArrayList<>();
        for (SalaryExpectedEventCandidate event : events) {
            if (!"jx".equals(event.source())
                    || !isRankAllowanceChange(event.changeType())
                    || hasHistoryEvent(historyByKey, event.year(), event.month(), event.changeType())) {
                aligned.add(event);
                continue;
            }
            HistoryTypeCandidate nearby = nearbyRankHistory(event.personCode(), event.year(), event.month(), event.changeType());
            if (nearby == null || event.changeType().equals(nearby.changeType())) {
                aligned.add(event);
                continue;
            }
            aligned.add(new SalaryExpectedEventCandidate(
                    event.source(),
                    event.sourceId(),
                    event.personCode(),
                    nearby.year(),
                    nearby.month(),
                    nearby.changeType(),
                    event.note() + "\uff1b\u5386\u53f2\u94fe\u8b66\u8854/\u6cd5\u68c0/\u76d1\u5bdf\u7b49\u7ea7\u6d25\u8d34\u7c7b\u578b\u5b57\u9762\u5df2\u5bf9\u9f50"
            ));
        }
        return aligned;
    }

    private List<SalaryExpectedEventCandidate> alignHjxxEventsToNearbyHistory(
            List<SalaryExpectedEventCandidate> events,
            Map<HistoryKey, List<SalaryHistoryLinkItem>> historyByKey
    ) {
        List<SalaryExpectedEventCandidate> aligned = new ArrayList<>();
        for (SalaryExpectedEventCandidate event : events) {
            if (!"hjxx".equals(event.source())
                    || hasHistoryEvent(historyByKey, event.year(), event.month(), event.changeType())) {
                aligned.add(event);
                continue;
            }
            YearMonth previous = addMonths(event.year(), event.month(), -1);
            if (hasHistoryEvent(historyByKey, previous.year(), previous.month(), event.changeType())) {
                aligned.add(copyEventAt(event, previous, "\uff1b\u5386\u53f2\u94fe\u540c\u7c7b\u5956\u60e9\u4e8b\u4ef6\u5728\u5f53\u6708\uff0c\u5df2\u5bf9\u9f50\u6267\u884c\u6708"));
                continue;
            }
            YearMonth next = addMonths(event.year(), event.month(), 1);
            if (hasHistoryEvent(historyByKey, next.year(), next.month(), event.changeType())) {
                aligned.add(copyEventAt(event, next, "\uff1b\u5386\u53f2\u94fe\u540c\u7c7b\u5956\u60e9\u4e8b\u4ef6\u5728\u6b21\u6708\uff0c\u5df2\u5bf9\u9f50\u6267\u884c\u6708"));
                continue;
            }
            aligned.add(event);
        }
        return aligned;
    }

    private SalaryExpectedEventCandidate copyEventAt(SalaryExpectedEventCandidate event, YearMonth yearMonth, String noteSuffix) {
        return new SalaryExpectedEventCandidate(
                event.source(),
                event.sourceId(),
                event.personCode(),
                yearMonth.year(),
                yearMonth.month(),
                event.changeType(),
                event.note() + noteSuffix
        );
    }

    private List<SalaryExpectedEventCandidate> preferPostEventsMatchingHistory(
            List<SalaryExpectedEventCandidate> events,
            Map<HistoryKey, List<SalaryHistoryLinkItem>> historyByKey
    ) {
        Map<HistoryKey, List<SalaryExpectedEventCandidate>> postEventsByKey = new HashMap<>();
        for (SalaryExpectedEventCandidate event : events) {
            if (SOURCE_POST.equals(event.source())) {
                postEventsByKey.computeIfAbsent(HistoryKey.of(event), ignored -> new ArrayList<>()).add(event);
            }
        }

        Set<SalaryExpectedEventCandidate> selected = new HashSet<>();
        for (Map.Entry<HistoryKey, List<SalaryExpectedEventCandidate>> entry : postEventsByKey.entrySet()) {
            List<SalaryExpectedEventCandidate> sameKeyEvents = entry.getValue();
            List<SalaryHistoryLinkItem> sameKeyHistory = historyByKey.getOrDefault(entry.getKey(), List.of());
            if (sameKeyEvents.size() <= sameKeyHistory.size() || sameKeyHistory.isEmpty()) {
                selected.addAll(sameKeyEvents);
                continue;
            }

            Set<String> historyPostCodes = new HashSet<>();
            for (SalaryHistoryLinkItem historyRow : sameKeyHistory) {
                String historyPostCode = historyPostCode(historyRow.id());
                if (StringUtils.hasText(historyPostCode)) {
                    historyPostCodes.add(historyPostCode);
                }
            }

            List<SalaryExpectedEventCandidate> preferred = new ArrayList<>();
            if (!historyPostCodes.isEmpty()) {
                for (SalaryExpectedEventCandidate event : sameKeyEvents) {
                    if (historyPostCodes.contains(postCode(event.sourceId()))) {
                        preferred.add(event);
                    }
                }
            }
            for (SalaryExpectedEventCandidate event : sameKeyEvents) {
                if (preferred.size() >= sameKeyHistory.size()) {
                    break;
                }
                if (!preferred.contains(event)) {
                    preferred.add(event);
                }
            }
            selected.addAll(preferred);
        }

        List<SalaryExpectedEventCandidate> filtered = new ArrayList<>();
        for (SalaryExpectedEventCandidate event : events) {
            if (!SOURCE_POST.equals(event.source()) || selected.contains(event)) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    private List<SalaryExpectedEventCandidate> removePostEventsCoveredByBroaderHistory(
            List<SalaryExpectedEventCandidate> events,
            Map<HistoryKey, List<SalaryHistoryLinkItem>> historyByKey
    ) {
        List<SalaryExpectedEventCandidate> filtered = new ArrayList<>();
        for (SalaryExpectedEventCandidate event : events) {
            if (SOURCE_POST.equals(event.source())
                    && isPostDerivedChangeType(event.changeType())
                    && !hasHistoryEvent(historyByKey, event.year(), event.month(), event.changeType())
                    && (postEventCoveredByBroaderHistory(event) || postEventAlreadyExecutedInSalary(event))) {
                continue;
            }
            filtered.add(event);
        }
        return filtered;
    }

    private boolean isPostChangeEventType(String changeType) {
        String value = trim(changeType);
        return CHANGE_POST_CHANGE.equals(value) || "鑱屽姟鍙樺寲".equals(value);
    }

    private boolean isPostDerivedChangeType(String changeType) {
        String value = trim(changeType);
        return isPostChangeEventType(value)
                || CHANGE_CIVIL_RANK_CONVERSION.equals(value)
                || CHANGE_CIVIL_RANK_PROMOTION.equals(value)
                || CHANGE_JUDICIAL_CONVERSION.equals(value);
    }

    private boolean postEventCoveredByBroaderHistory(SalaryExpectedEventCandidate event) {
        String sourcePostCode = postCode(event.sourceId());
        if (!StringUtils.hasText(sourcePostCode)) {
            return false;
        }
        PersonCodeParts parts = personCodeParser.parse(event.personCode());
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM hisbase
                WHERE dwbm = ? AND grbm = ?
                  AND TRIM(COALESCE(zwbm2, '')) = ?
                  AND (
                      TRIM(jslb) = ?
                      OR HEX(CONVERT(TRIM(jslb) USING utf8mb4)) IN (
                          'E8B083E585A5E5AE9AE8B584',
                          'E8BDACE4B89AE5AE9AE8B584',
                          'E696B0E8BF9BE5B7A5E8B584',
                          'E5ADA6E58E86E58F98E58C96',
                          'E9998DE8B584E5A484E58886',
                          'E585B6E5AE83E68385E586B5'
                      )
                  )
                  AND ABS((CAST(TRIM(jsnf) AS SIGNED) * 12 + CAST(TRIM(jsyf) AS SIGNED)) - (? * 12 + ?)) <= 1
                """,
                Integer.class,
                parts.orgCode(),
                parts.personNo(),
                sourcePostCode,
                CHANGE_REGULARIZATION,
                event.year(),
                event.month()
        );
        return count != null && count > 0;
    }

    private boolean postEventAlreadyExecutedInSalary(SalaryExpectedEventCandidate event) {
        String sourcePostCode = postCode(event.sourceId());
        if (!StringUtils.hasText(sourcePostCode)) {
            return false;
        }
        PersonCodeParts parts = personCodeParser.parse(event.personCode());
        List<String> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(COALESCE(zwbm2, ''))
                FROM hisbase
                WHERE dwbm = ? AND grbm = ?
                  AND (CAST(TRIM(jsnf) AS SIGNED) * 100 + CAST(TRIM(jsyf) AS SIGNED)) < ?
                  AND TRIM(COALESCE(zwbm2, '')) <> ''
                  AND COALESCE(hj2, 0) > 0
                ORDER BY CAST(TRIM(jsnf) AS SIGNED) DESC,
                         CAST(TRIM(jsyf) AS SIGNED) DESC,
                         COALESCE(hj2, 0) DESC,
                         id DESC
                LIMIT 1
                """,
                String.class,
                parts.orgCode(),
                parts.personNo(),
                event.year() * 100 + event.month()
        );
        return !rows.isEmpty() && sourcePostCode.equals(trim(rows.get(0)));
    }

    private List<SalaryExpectedEventCandidate> removeEducationEventsWithoutSalaryImpact(
            List<SalaryExpectedEventCandidate> events,
            Map<HistoryKey, List<SalaryHistoryLinkItem>> historyByKey
    ) {
        List<SalaryExpectedEventCandidate> filtered = new ArrayList<>();
        for (SalaryExpectedEventCandidate event : events) {
            if (!"dxl".equals(event.source())
                    || hasHistoryEvent(historyByKey, event.year(), event.month(), event.changeType())
                    || nearbyEducationHistory(event.personCode(), event.year(), event.month()) != null
                    || educationEventMayImproveSalary(event)) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    private boolean educationEventMayImproveSalary(SalaryExpectedEventCandidate event) {
        List<Map<String, Object>> educationRows = jdbcTemplate.queryForList("""
                SELECT TRIM(COALESCE(xlbm, '')) AS xlbm
                FROM dxl
                WHERE TRIM(id) = ?
                LIMIT 1
                """, event.sourceId());
        if (educationRows.isEmpty()) {
            return true;
        }
        String educationCode = trim(educationRows.get(0).get("xlbm"));
        if (!StringUtils.hasText(educationCode)) {
            return true;
        }
        PersonCodeParts parts = personCodeParser.parse(event.personCode());
        List<Map<String, Object>> baselineRows = jdbcTemplate.queryForList("""
                SELECT TRIM(COALESCE(zwbm2, '')) AS zwbm2,
                       TRIM(COALESCE(jbgzjb2, '')) AS jbgzjb2,
                       TRIM(COALESCE(zwgzdc2, '')) AS zwgzdc2,
                       TRIM(COALESCE(djc2, '')) AS djc2
                FROM hisbase
                WHERE dwbm = ? AND grbm = ?
                  AND (CAST(TRIM(jsnf) AS SIGNED) * 100 + CAST(TRIM(jsyf) AS SIGNED)) < ?
                  AND TRIM(COALESCE(zwbm2, '')) <> ''
                  AND COALESCE(hj2, 0) > 0
                ORDER BY CAST(TRIM(jsnf) AS SIGNED) DESC,
                         CAST(TRIM(jsyf) AS SIGNED) DESC,
                         COALESCE(hj2, 0) DESC,
                         id DESC
                LIMIT 1
                """,
                parts.orgCode(),
                parts.personNo(),
                event.year() * 100 + event.month()
        );
        if (baselineRows.isEmpty()) {
            return true;
        }
        Map<String, Object> baseline = baselineRows.get(0);
        String postCode = trim(baseline.get("zwbm2"));
        String prefix = postPrefix(postCode);
        List<Map<String, Object>> standardRows = educationStandardRows(
                event.year(),
                event.month(),
                educationCode,
                educationStandardPostPrefix(prefix)
        );
        if (standardRows.isEmpty()) {
            return true;
        }
        Map<String, Object> standard = standardRows.get(0);
        if (Set.of("01", "02", "21", "22", "23", "24", "25", "26", "27", "28").contains(prefix)) {
            int currentLevel = parseInt(baseline.get("jbgzjb2"));
            int currentStep = parseInt(baseline.get("zwgzdc2")) + parseInt(baseline.get("djc2"));
            int educationLevel = parseInt(standard.get("zzjb"));
            int educationStep = parseInt(standard.get("zzdc"));
            if (currentLevel <= 0 || currentStep <= 0 || educationLevel <= 0 || educationStep <= 0) {
                return true;
            }
            return currentLevel > educationLevel || (currentLevel == educationLevel && currentStep < educationStep);
        }
        if (Set.of("05", "06", "07", "08", "09", "10", "11").contains(prefix)) {
            int currentGrade = parseInt(baseline.get("zwgzdc2"));
            int educationGrade = parseInt(standard.get("zzdc"));
            if (currentGrade <= 0 || educationGrade <= 0) {
                return true;
            }
            return currentGrade < educationGrade;
        }
        return true;
    }

    private List<Map<String, Object>> educationStandardRows(int year, int month, String educationCode, String postPrefix) {
        String standardYear = educationStandardYear(year, month, educationCode);
        if (!StringUtils.hasText(standardYear)) {
            return List.of();
        }
        return jdbcTemplate.queryForList("""
                SELECT TRIM(COALESCE(zzzwbm, '')) AS zzzwbm,
                       TRIM(COALESCE(zzdc, '')) AS zzdc,
                       TRIM(COALESCE(zzjb, '')) AS zzjb
                FROM bz06_zzdz
                WHERE tbnd = ?
                  AND xlbm = ?
                  AND LEFT(TRIM(COALESCE(zzzwbm, '')), 2) = ?
                ORDER BY zzzwbm
                LIMIT 1
                """, standardYear, educationCode, postPrefix);
    }

    private String educationStandardPostPrefix(String postPrefix) {
        return Set.of("21", "22", "23", "24", "25", "26", "27", "28").contains(postPrefix) ? "01" : postPrefix;
    }

    private String educationStandardYear(int year, int month, String educationCode) {
        List<String> rows = jdbcTemplate.queryForList("""
                SELECT MAX(tbnd)
                FROM bz06_zzdz
                WHERE xlbm = ?
                  AND CAST(tbnd AS SIGNED) <= ?
                """, String.class, educationCode, year * 100 + month);
        return rows.isEmpty() ? "" : trim(rows.get(0));
    }

    private YearMonth addMonths(int year, int month, int offset) {
        int zeroBased = year * 12 + (month - 1) + offset;
        return new YearMonth(zeroBased / 12, zeroBased % 12 + 1);
    }

    private boolean hasHistoryEvent(
            Map<HistoryKey, List<SalaryHistoryLinkItem>> historyByKey,
            int year,
            int month,
            String changeType
    ) {
        List<SalaryHistoryLinkItem> rows = historyByKey.get(new HistoryKey(year, month, changeType));
        return rows != null && !rows.isEmpty();
    }

    private Map<HistoryKey, List<SalaryHistoryLinkItem>> historyByKey(List<SalaryHistoryLinkItem> history) {
        Map<HistoryKey, List<SalaryHistoryLinkItem>> result = new HashMap<>();
        for (SalaryHistoryLinkItem row : history) {
            result.computeIfAbsent(HistoryKey.of(row), ignored -> new ArrayList<>()).add(row);
        }
        return result;
    }

    private SalaryHistoryLinkItem takeFirstUnused(List<SalaryHistoryLinkItem> rows, Set<String> usedHistoryIds) {
        if (rows == null) {
            return null;
        }
        for (SalaryHistoryLinkItem row : rows) {
            if (usedHistoryIds.add(row.id())) {
                return row;
            }
        }
        return null;
    }

    private String diagnosticMessage(
            PersonCodeParts parts,
            SalaryExpectedEventCandidate event,
            SalaryHistoryLinkItem historyRow,
            String baseMessage
    ) {
        List<String> notes = new ArrayList<>();
        if (StringUtils.hasText(baseMessage)) {
            notes.add(baseMessage);
        }
        if (SOURCE_POST.equals(event.source())) {
            notes.addAll(postDiagnostics(parts, event, historyRow));
        } else if ("dxl".equals(event.source())) {
            notes.addAll(educationDiagnostics(parts, event, historyRow));
        } else if (SOURCE_ASSESSMENT.equals(event.source())) {
            notes.addAll(assessmentDiagnostics(parts, event, historyRow));
        } else if ("dryjbxx".equals(event.source())) {
            notes.addAll(basicInfoDiagnostics(parts, event, historyRow));
        }
        notes.addAll(sameMonthHistoryDiagnostics(parts, event, historyRow));
        return String.join("；", notes);
    }

    private List<String> postDiagnostics(PersonCodeParts parts, SalaryExpectedEventCandidate event, SalaryHistoryLinkItem historyRow) {
        List<String> notes = new ArrayList<>();
        List<Map<String, Object>> sourceRows = jdbcTemplate.queryForList("""
                SELECT id, TRIM(srny) AS srny, TRIM(COALESCE(zwbm, '')) AS zwbm,
                       TRIM(COALESCE(zjbm, '')) AS zjbm,
                       TRIM(COALESCE(xrzwbm, '')) AS xrzwbm,
                       TRIM(COALESCE(xrzwbz, '')) AS xrzwbz
                FROM dryzwbh
                WHERE TRIM(id) = ?
                """, event.sourceId());
        if (sourceRows.isEmpty()) {
            notes.add("基础任职信息缺失：dryzwbh.id=" + event.sourceId());
            return notes;
        }
        Map<String, Object> source = sourceRows.get(0);
        String sourcePostCode = trim(source.get("zwbm"));
        String sourceMonth = trim(source.get("srny"));
        if (!StringUtils.hasText(sourcePostCode)) {
            notes.add("基础任职信息 zwbm 为空：dryzwbh.id=" + event.sourceId());
        }
        List<Map<String, Object>> sameMonthPosts = jdbcTemplate.queryForList("""
                SELECT id, TRIM(COALESCE(zwbm, '')) AS zwbm, TRIM(COALESCE(xrzwbz, '')) AS xrzwbz
                FROM dryzwbh
                WHERE dwbm = ? AND grbm = ? AND TRIM(COALESCE(srny, '')) = ?
                ORDER BY id
                """, parts.orgCode(), parts.personNo(), sourceMonth);
        if (sameMonthPosts.size() > 1) {
            notes.add("同月存在多条任职信息：" + summarizeRows(sameMonthPosts, "id", "zwbm", "xrzwbz"));
        }
        if (historyRow != null) {
            Map<String, Object> history = historyRow(historyRow.id());
            String historyPostCode = trim(history.get("zwbm2"));
            if (StringUtils.hasText(sourcePostCode) && StringUtils.hasText(historyPostCode) && !sourcePostCode.equals(historyPostCode)) {
                notes.add("基础任职编码与历史工资编码不一致：dryzwbh.zwbm=" + sourcePostCode + "，hisbase.zwbm2=" + historyPostCode);
            }
        } else if (StringUtils.hasText(sourcePostCode)) {
            List<Map<String, Object>> nearbyHistory = jdbcTemplate.queryForList("""
                    SELECT id, jsnf, jsyf, TRIM(jslb) AS jslb, TRIM(COALESCE(zwbm2, '')) AS zwbm2
                    FROM hisbase
                    WHERE dwbm = ? AND grbm = ?
                      AND ABS((CAST(TRIM(jsnf) AS SIGNED) * 12 + CAST(TRIM(jsyf) AS SIGNED)) - (? * 12 + ?)) <= 1
                    ORDER BY CAST(TRIM(jsnf) AS SIGNED), CAST(TRIM(jsyf) AS SIGNED), id
                    """, parts.orgCode(), parts.personNo(), event.year(), event.month());
            if (!nearbyHistory.isEmpty()) {
                notes.add("应执行月前后已有历史工资行：" + summarizeRows(nearbyHistory, "jsnf", "jsyf", "jslb", "zwbm2"));
            }
        }
        if (isRankPost(sourcePostCode) && !StringUtils.hasText(trim(source.get("xrzwbz")))) {
            notes.add("职级任职行 xrzwbz 为空，可能只是基础任职登记，需确认是否实际执行工资");
        }
        if (historyRow == null && event.changeType().contains("2006")) {
            List<Map<String, Object>> conversionRows = jdbcTemplate.queryForList("""
                    SELECT id, TRIM(jslb) AS jslb, TRIM(COALESCE(zwbm2, '')) AS zwbm2, hj2
                    FROM hisbase
                    WHERE dwbm = ? AND grbm = ?
                      AND CAST(TRIM(jsnf) AS SIGNED) = 2006
                      AND CAST(TRIM(jsyf) AS SIGNED) = 7
                      AND HEX(CONVERT(TRIM(jslb) USING utf8mb4)) = '32303036E5A597E694B9'
                    ORDER BY id
                    """, parts.orgCode(), parts.personNo());
            if (conversionRows.isEmpty()) {
                notes.add("2006.07 history conversion row missing");
            } else {
                notes.add("2006.07 conversion rows exist but were not matched: " + summarizeRows(conversionRows, "id", "jslb", "zwbm2", "hj2"));
            }
            List<Map<String, Object>> firstHistoryRows = jdbcTemplate.queryForList("""
                    SELECT id, jsnf, jsyf, TRIM(jslb) AS jslb, TRIM(COALESCE(zwbm2, '')) AS zwbm2, hj2
                    FROM hisbase
                    WHERE dwbm = ? AND grbm = ?
                    ORDER BY CAST(TRIM(jsnf) AS SIGNED), CAST(TRIM(jsyf) AS SIGNED), id
                    LIMIT 5
                    """, parts.orgCode(), parts.personNo());
            if (!firstHistoryRows.isEmpty()) {
                notes.add("earliest history rows: " + summarizeRows(firstHistoryRows, "jsnf", "jsyf", "jslb", "zwbm2", "hj2"));
            }
            boolean probationaryAround2006 = firstHistoryRows.stream()
                    .filter(row -> periodValue(row) <= 200607)
                    .map(row -> trim(row.get("zwbm2")))
                    .anyMatch(value -> value.contains("F"));
            if (probationaryAround2006) {
                notes.add("probationary/F-post history exists before or at 2006.07; verify 2006 conversion as probationary policy row");
            }
        }
        return notes;
    }

    private List<String> educationDiagnostics(PersonCodeParts parts, SalaryExpectedEventCandidate event, SalaryHistoryLinkItem historyRow) {
        List<String> notes = new ArrayList<>();
        List<Map<String, Object>> sourceRows = jdbcTemplate.queryForList("SELECT * FROM dxl WHERE id = ?", event.sourceId());
        if (sourceRows.isEmpty()) {
            notes.add("学历基础信息缺失：dxl.id=" + event.sourceId());
            return notes;
        }
        Map<String, Object> source = sourceRows.get(0);
        String graduationMonth = firstText(source, "bysj", "BYSJ");
        String educationCode = firstText(source, "xlbm", "XLBM", "xl", "XL");
        String educationType = firstText(source, "xllb", "XLLB");
        if (!StringUtils.hasText(educationCode)) {
            notes.add("学历基础记录缺少 xlbm/学历编码");
        }
        if ("其他".equals(educationType) || "其它".equals(educationType)) {
            notes.add("学历类别为其他/其它，按口径不参与工资");
        }
        if (StringUtils.hasText(graduationMonth)) {
            List<Map<String, Object>> sameMonthEducations = jdbcTemplate.queryForList("""
                    SELECT id, TRIM(COALESCE(bysj, '')) AS bysj, TRIM(COALESCE(xllb, '')) AS xllb
                    FROM dxl
                    WHERE dwbm = ? AND grbm = ? AND TRIM(COALESCE(bysj, '')) = ?
                    ORDER BY id
                    """, parts.orgCode(), parts.personNo(), graduationMonth);
            if (sameMonthEducations.size() > 1) {
                notes.add("同一毕业年月存在多条学历记录：" + summarizeRows(sameMonthEducations, "id", "bysj", "xllb"));
            }
        }
        if (historyRow != null) {
            Map<String, Object> history = historyRow(historyRow.id());
            String historyEducation = firstText(history, "zgxl", "ZGXL", "xlbm", "XLBM");
            if (StringUtils.hasText(educationCode) && StringUtils.hasText(historyEducation) && !historyEducation.contains(educationCode)) {
                notes.add("学历基础编码与历史行学历字段可能不一致：dxl.xlbm=" + educationCode + "，hisbase=" + historyEducation);
            }
        }
        return notes;
    }

    private List<String> assessmentDiagnostics(PersonCodeParts parts, SalaryExpectedEventCandidate event, SalaryHistoryLinkItem historyRow) {
        List<String> notes = new ArrayList<>();
        int assessmentYear = assessmentYear(event.sourceId(), event.year());
        List<Map<String, Object>> assessmentRows = jdbcTemplate.queryForList("""
                SELECT khnd, TRIM(COALESCE(khjg, '')) AS khjg
                FROM dndkh
                WHERE dwbm = ? AND grbm = ? AND CAST(TRIM(khnd) AS SIGNED) BETWEEN ? AND ?
                ORDER BY CAST(TRIM(khnd) AS SIGNED)
                """, parts.orgCode(), parts.personNo(), assessmentYear - 5, assessmentYear);
        if (assessmentRows.isEmpty()) {
            notes.add("未找到相关年度考核基础信息");
            return notes;
        }
        long uncertainCount = assessmentRows.stream()
                .map(row -> trim(row.get("khjg")))
                .filter(value -> value.contains("未定") || value.contains("不定") || value.contains("不合格"))
                .count();
        if (uncertainCount > 0) {
            notes.add("近年考核存在未定等次/不合格记录，可能影响套改年限、任职年限或正常晋升：" + summarizeRows(assessmentRows, "khnd", "khjg"));
        }
        if (historyRow != null) {
            Map<String, Object> history = historyRow(historyRow.id());
            String startGrade = firstText(history, "xckhndzw", "XCKHNDZW");
            String startLevel = firstText(history, "xckhndjb", "XCKHNDJB");
            if (!StringUtils.hasText(startGrade) && !StringUtils.hasText(startLevel)) {
                notes.add("历史工资行缺少下次晋升起算年状态，可能影响后续自动推演");
            }
        }
        return notes;
    }

    private List<String> basicInfoDiagnostics(PersonCodeParts parts, SalaryExpectedEventCandidate event, SalaryHistoryLinkItem historyRow) {
        List<String> notes = new ArrayList<>();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM dryjbxx
                WHERE dwbm = ? AND grbm = ?
                """, parts.orgCode(), parts.personNo());
        if (rows.isEmpty()) {
            notes.add("人员基本信息缺失：dryjbxx 无记录");
        } else if (rows.size() > 1) {
            notes.add("人员基本信息存在多条 dryjbxx 记录：" + summarizeRows(rows, "uid", "cjgzny"));
        }
        if (historyRow != null && !rows.isEmpty()) {
            Map<String, Object> history = historyRow(historyRow.id());
            String basePostCode = firstText(rows.get(0), "zwbm", "ZWBM", "zwbm2", "ZWBM2");
            String historyPostCode = trim(history.get("zwbm2"));
            if (StringUtils.hasText(basePostCode) && StringUtils.hasText(historyPostCode) && !basePostCode.equals(historyPostCode)) {
                notes.add("当前基本信息职务与历史套改行职务不一致：dryjbxx.zwbm=" + basePostCode + "，hisbase.zwbm2=" + historyPostCode);
            }
        }
        add2006MissingDiagnostics(notes, parts, event, historyRow);
        return notes;
    }

    private void add2006MissingDiagnostics(
            List<String> notes,
            PersonCodeParts parts,
            SalaryExpectedEventCandidate event,
            SalaryHistoryLinkItem historyRow
    ) {
        if (historyRow != null || !event.changeType().contains("2006")) {
            return;
        }
        List<Map<String, Object>> conversionRows = jdbcTemplate.queryForList("""
                SELECT id, TRIM(jslb) AS jslb, TRIM(COALESCE(zwbm2, '')) AS zwbm2, hj2
                FROM hisbase
                WHERE dwbm = ? AND grbm = ?
                  AND CAST(TRIM(jsnf) AS SIGNED) = 2006
                  AND CAST(TRIM(jsyf) AS SIGNED) = 7
                  AND HEX(CONVERT(TRIM(jslb) USING utf8mb4)) = '32303036E5A597E694B9'
                ORDER BY id
                """, parts.orgCode(), parts.personNo());
        if (conversionRows.isEmpty()) {
            notes.add("2006.07 history conversion row missing");
        } else {
            notes.add("2006.07 conversion rows exist but were not matched: " + summarizeRows(conversionRows, "id", "jslb", "zwbm2", "hj2"));
        }
        List<Map<String, Object>> firstHistoryRows = jdbcTemplate.queryForList("""
                SELECT id, jsnf, jsyf, TRIM(jslb) AS jslb, TRIM(COALESCE(zwbm2, '')) AS zwbm2, hj2
                FROM hisbase
                WHERE dwbm = ? AND grbm = ?
                ORDER BY CAST(TRIM(jsnf) AS SIGNED), CAST(TRIM(jsyf) AS SIGNED), id
                LIMIT 5
                """, parts.orgCode(), parts.personNo());
        if (!firstHistoryRows.isEmpty()) {
            notes.add("earliest history rows: " + summarizeRows(firstHistoryRows, "jsnf", "jsyf", "jslb", "zwbm2", "hj2"));
        }
        boolean probationaryAround2006 = firstHistoryRows.stream()
                .filter(row -> periodValue(row) <= 200607)
                .map(row -> trim(row.get("zwbm2")))
                .anyMatch(value -> value.contains("F"));
        if (probationaryAround2006) {
            notes.add("probationary/F-post history exists before or at 2006.07; verify 2006 conversion as probationary policy row");
        }
    }

    private List<String> sameMonthHistoryDiagnostics(PersonCodeParts parts, SalaryExpectedEventCandidate event, SalaryHistoryLinkItem matchedHistory) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id, TRIM(jslb) AS jslb, TRIM(COALESCE(zwbm2, '')) AS zwbm2, hj2
                FROM hisbase
                WHERE dwbm = ? AND grbm = ?
                  AND CAST(TRIM(jsnf) AS SIGNED) = ?
                  AND CAST(TRIM(jsyf) AS SIGNED) = ?
                ORDER BY id
                """, parts.orgCode(), parts.personNo(), event.year(), event.month());
        if (rows.isEmpty()) {
            return List.of();
        }
        boolean hasDifferentType = rows.stream()
                .map(row -> trim(row.get("jslb")))
                .anyMatch(type -> !type.equals(event.changeType()));
        if (hasDifferentType || matchedHistory == null) {
            return List.of("同年月历史工资行：" + summarizeRows(rows, "id", "jslb", "zwbm2", "hj2"));
        }
        return List.of();
    }

    private Map<String, Object> historyRow(String historyId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM hisbase WHERE id = ? OR id = LPAD(?, 36, ' ') LIMIT 1",
                historyId, historyId);
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    private String historyPostCode(String historyId) {
        return trim(historyRow(historyId).get("zwbm2"));
    }

    private String postCode(String sourceId) {
        List<String> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(COALESCE(zwbm, ''))
                FROM dryzwbh
                WHERE TRIM(id) = ?
                LIMIT 1
                """, String.class, sourceId);
        return rows.isEmpty() ? "" : trim(rows.get(0));
    }

    private HistoryTypeCandidate nearbyGeneratedHistoryWithPostCode(String personCode, int year, int month, String postCode) {
        PersonCodeParts parts = personCodeParser.parse(personCode);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT CAST(TRIM(jsnf) AS SIGNED) AS year,
                       CAST(TRIM(jsyf) AS SIGNED) AS month,
                       TRIM(jslb) AS changeType
                FROM hisbase
                WHERE dwbm = ? AND grbm = ?
                  AND TRIM(COALESCE(zwbm2, '')) = ?
                  AND TRIM(jslb) IN (?, ?, ?, ?, ?, ?)
                  AND ABS((CAST(TRIM(jsnf) AS SIGNED) * 12 + CAST(TRIM(jsyf) AS SIGNED)) - (? * 12 + ?)) <= 1
                ORDER BY ABS((CAST(TRIM(jsnf) AS SIGNED) * 12 + CAST(TRIM(jsyf) AS SIGNED)) - (? * 12 + ?)),
                         CAST(TRIM(jsnf) AS SIGNED),
                         CAST(TRIM(jsyf) AS SIGNED),
                         id
                LIMIT 1
                """,
                parts.orgCode(),
                parts.personNo(),
                postCode,
                "鑱屽姟鍙樺寲",
                "璀﹀憳濂楁敼",
                CHANGE_CIVIL_RANK_CONVERSION,
                CHANGE_CIVIL_RANK_PROMOTION,
                CHANGE_JUDICIAL_CONVERSION,
                "瀛﹀巻鍙樺寲",
                year,
                month,
                year,
                month
        );
        if (rows.isEmpty()) {
            return null;
        }
        Map<String, Object> row = rows.get(0);
        return new HistoryTypeCandidate(
                Integer.parseInt(trim(row.get("year"))),
                Integer.parseInt(trim(row.get("month"))),
                trim(row.get("changeType"))
        );
    }

    private HistoryTypeCandidate nearbyGeneratedHistoryWithPostCodeRelaxed(String personCode, int year, int month, String postCode) {
        PersonCodeParts parts = personCodeParser.parse(personCode);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT CAST(TRIM(jsnf) AS SIGNED) AS year,
                       CAST(TRIM(jsyf) AS SIGNED) AS month,
                       TRIM(jslb) AS changeType
                FROM hisbase
                WHERE dwbm = ? AND grbm = ?
                  AND TRIM(COALESCE(zwbm2, '')) = ?
                  AND ABS((CAST(TRIM(jsnf) AS SIGNED) * 12 + CAST(TRIM(jsyf) AS SIGNED)) - (? * 12 + ?)) <= 1
                ORDER BY ABS((CAST(TRIM(jsnf) AS SIGNED) * 12 + CAST(TRIM(jsyf) AS SIGNED)) - (? * 12 + ?)),
                         CAST(TRIM(jsnf) AS SIGNED),
                         CAST(TRIM(jsyf) AS SIGNED),
                         id
                """,
                parts.orgCode(),
                parts.personNo(),
                postCode,
                year,
                month,
                year,
                month
        );
        for (Map<String, Object> row : rows) {
            String changeType = trim(row.get("changeType"));
            if (isGeneratedPostHistoryType(changeType)) {
                return new HistoryTypeCandidate(
                        Integer.parseInt(trim(row.get("year"))),
                        Integer.parseInt(trim(row.get("month"))),
                        changeType
                );
            }
        }
        return null;
    }

    private HistoryTypeCandidate nearbyEducationHistory(String personCode, int year, int month) {
        PersonCodeParts parts = personCodeParser.parse(personCode);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT CAST(TRIM(jsnf) AS SIGNED) AS year,
                       CAST(TRIM(jsyf) AS SIGNED) AS month,
                       TRIM(jslb) AS changeType
                FROM hisbase
                WHERE dwbm = ? AND grbm = ?
                  AND HEX(CONVERT(TRIM(jslb) USING utf8mb4)) = 'E5ADA6E58E86E58F98E58C96'
                  AND ABS((CAST(TRIM(jsnf) AS SIGNED) * 12 + CAST(TRIM(jsyf) AS SIGNED)) - (? * 12 + ?)) <= 1
                ORDER BY ABS((CAST(TRIM(jsnf) AS SIGNED) * 12 + CAST(TRIM(jsyf) AS SIGNED)) - (? * 12 + ?)),
                         CAST(TRIM(jsnf) AS SIGNED),
                         CAST(TRIM(jsyf) AS SIGNED),
                         id
                """,
                parts.orgCode(),
                parts.personNo(),
                year,
                month,
                year,
                month
        );
        if (!rows.isEmpty()) {
            Map<String, Object> row = rows.get(0);
            return new HistoryTypeCandidate(
                    Integer.parseInt(trim(row.get("year"))),
                    Integer.parseInt(trim(row.get("month"))),
                    trim(row.get("changeType"))
            );
        }
        return null;
    }

    private HistoryTypeCandidate nearbyRankHistory(String personCode, int year, int month, String generatedChangeType) {
        PersonCodeParts parts = personCodeParser.parse(personCode);
        String preferredType = trim(generatedChangeType);
        String pairedType = pairedRankAllowanceType(preferredType);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT CAST(TRIM(jsnf) AS SIGNED) AS year,
                       CAST(TRIM(jsyf) AS SIGNED) AS month,
                       TRIM(jslb) AS changeType
                FROM hisbase
                WHERE dwbm = ? AND grbm = ?
                  AND TRIM(jslb) IN (?, ?, ?, ?, ?, ?, ?, ?, ?)
                  AND ABS((CAST(TRIM(jsnf) AS SIGNED) * 12 + CAST(TRIM(jsyf) AS SIGNED)) - (? * 12 + ?)) <= 1
                ORDER BY ABS((CAST(TRIM(jsnf) AS SIGNED) * 12 + CAST(TRIM(jsyf) AS SIGNED)) - (? * 12 + ?)),
                         CASE TRIM(jslb)
                             WHEN ? THEN 0
                             WHEN ? THEN 1
                             ELSE 2
                         END,
                         CAST(TRIM(jsnf) AS SIGNED),
                         CAST(TRIM(jsyf) AS SIGNED),
                         id
                LIMIT 1
                """,
                parts.orgCode(),
                parts.personNo(),
                CHANGE_POLICE_RANK_CHANGE,
                CHANGE_POLICE_RANK_ALLOWANCE,
                CHANGE_JUDGE_RANK,
                CHANGE_JUDGE_ALLOWANCE,
                CHANGE_PROSECUTOR_RANK,
                CHANGE_PROSECUTOR_ALLOWANCE,
                CHANGE_SUPERVISOR_RANK,
                CHANGE_SUPERVISOR_RANK_LEGACY,
                CHANGE_SUPERVISOR_ALLOWANCE,
                year,
                month,
                year,
                month,
                preferredType,
                pairedType
        );
        if (rows.isEmpty()) {
            return null;
        }
        Map<String, Object> row = rows.get(0);
        return new HistoryTypeCandidate(
                Integer.parseInt(trim(row.get("year"))),
                Integer.parseInt(trim(row.get("month"))),
                trim(row.get("changeType"))
        );
    }

    private boolean isRankAllowanceChange(String changeType) {
        String value = trim(changeType);
        return CHANGE_POLICE_RANK_CHANGE.equals(value)
                || CHANGE_POLICE_RANK_ALLOWANCE.equals(value)
                || CHANGE_JUDGE_RANK.equals(value)
                || CHANGE_JUDGE_ALLOWANCE.equals(value)
                || CHANGE_PROSECUTOR_RANK.equals(value)
                || CHANGE_PROSECUTOR_ALLOWANCE.equals(value)
                || CHANGE_SUPERVISOR_RANK.equals(value)
                || CHANGE_SUPERVISOR_RANK_LEGACY.equals(value)
                || CHANGE_SUPERVISOR_ALLOWANCE.equals(value);
    }

    private String pairedRankAllowanceType(String changeType) {
        String value = trim(changeType);
        if (CHANGE_POLICE_RANK_CHANGE.equals(value)) {
            return CHANGE_POLICE_RANK_ALLOWANCE;
        }
        if (CHANGE_POLICE_RANK_ALLOWANCE.equals(value)) {
            return CHANGE_POLICE_RANK_CHANGE;
        }
        if (CHANGE_JUDGE_RANK.equals(value)) {
            return CHANGE_JUDGE_ALLOWANCE;
        }
        if (CHANGE_JUDGE_ALLOWANCE.equals(value)) {
            return CHANGE_JUDGE_RANK;
        }
        if (CHANGE_PROSECUTOR_RANK.equals(value)) {
            return CHANGE_PROSECUTOR_ALLOWANCE;
        }
        if (CHANGE_PROSECUTOR_ALLOWANCE.equals(value)) {
            return CHANGE_PROSECUTOR_RANK;
        }
        if (CHANGE_SUPERVISOR_RANK.equals(value) || CHANGE_SUPERVISOR_RANK_LEGACY.equals(value)) {
            return CHANGE_SUPERVISOR_ALLOWANCE;
        }
        if (CHANGE_SUPERVISOR_ALLOWANCE.equals(value)) {
            return CHANGE_SUPERVISOR_RANK;
        }
        return value;
    }

    private boolean isGeneratedPostHistoryType(String changeType) {
        String value = trim(changeType);
        if (CHANGE_2006_CONVERSION.equals(value)
                || value.contains("2006")
                || CHANGE_EDUCATION_CHANGE.equals(value)
                || value.contains("\u5b66\u5386")
                || CHANGE_NORMAL_GRADE.equals(value)
                || CHANGE_NORMAL_LEVEL.equals(value)) {
            return false;
        }
        return GENERATED_CHANGE_TYPES.contains(value)
                || CHANGE_POST_CHANGE.equals(value)
                || CHANGE_POLICE_CONVERSION.equals(value)
                || CHANGE_CIVIL_RANK_CONVERSION.equals(value)
                || CHANGE_CIVIL_RANK_PROMOTION.equals(value)
                || CHANGE_JUDICIAL_CONVERSION.equals(value);
    }

    private int assessmentYear(String sourceId, int eventYear) {
        String text = trim(sourceId);
        int colon = text.indexOf(':');
        if (colon >= 0) {
            text = text.substring(0, colon);
        }
        if (text.chars().allMatch(Character::isDigit) && text.length() == 4) {
            return Integer.parseInt(text);
        }
        return eventYear - 1;
    }

    private boolean isRankPost(String postCode) {
        String value = trim(postCode);
        if (value.length() < 2) {
            return false;
        }
        return Set.of("23", "24", "25", "26", "27", "28").contains(value.substring(0, 2));
    }

    private String postPrefix(String postCode) {
        String value = trim(postCode);
        return value.length() < 2 ? value : value.substring(0, 2);
    }

    private String summarizeRows(List<Map<String, Object>> rows, String... keys) {
        return rows.stream()
                .limit(5)
                .map(row -> summarizeRow(row, keys))
                .toList()
                .toString();
    }

    private String summarizeRow(Map<String, Object> row, String... keys) {
        List<String> parts = new ArrayList<>();
        for (String key : keys) {
            parts.add(key + "=" + trim(row.get(key)));
        }
        return String.join("/", parts);
    }

    private String firstText(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            String value = trim(row.get(key));
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private int periodValue(Map<String, Object> row) {
        return parseInt(row.get("jsnf")) * 100 + parseInt(row.get("jsyf"));
    }

    private String trim(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private int parseInt(Object value) {
        String text = trim(value);
        if (!StringUtils.hasText(text) || !text.chars().allMatch(Character::isDigit)) {
            return 0;
        }
        return Integer.parseInt(text);
    }

    private record YearMonth(int year, int month) {
    }

    private record HistoryTypeCandidate(int year, int month, String changeType) {
    }

    private static class CoverageCounter {
        private int expected;
        private int matchedHistory;
        private int unsupportedHistory;
    }

    private record HistoryKey(int year, int month, String changeType) {

        static HistoryKey of(SalaryExpectedEventCandidate event) {
            return new HistoryKey(event.year(), event.month(), safe(event.changeType()));
        }

        static HistoryKey of(SalaryHistoryLinkItem row) {
            return new HistoryKey(row.year(), row.month(), safe(row.changeType()));
        }

        private static String safe(String value) {
            return value == null ? "" : value;
        }
    }
}
