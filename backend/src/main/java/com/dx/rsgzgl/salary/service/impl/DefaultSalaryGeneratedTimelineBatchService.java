package com.dx.rsgzgl.salary.service.impl;

import com.dx.rsgzgl.salary.dto.SalaryGeneratedTimelineBatchItem;
import com.dx.rsgzgl.salary.dto.SalaryGeneratedTimelineBatchResult;
import com.dx.rsgzgl.salary.dto.SalaryGeneratedTimelineItem;
import com.dx.rsgzgl.salary.dto.SalaryGeneratedTimelineResult;
import com.dx.rsgzgl.salary.service.SalaryGeneratedTimelineBatchService;
import com.dx.rsgzgl.salary.service.SalaryGeneratedTimelineService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DefaultSalaryGeneratedTimelineBatchService implements SalaryGeneratedTimelineBatchService {

    private static final String STATUS_OK = "OK";
    private static final String STATUS_ISSUE = "ISSUE";
    private static final String STATUS_ERROR = "ERROR";

    private final JdbcTemplate jdbcTemplate;
    private final SalaryGeneratedTimelineService salaryGeneratedTimelineService;

    public DefaultSalaryGeneratedTimelineBatchService(
            JdbcTemplate jdbcTemplate,
            SalaryGeneratedTimelineService salaryGeneratedTimelineService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.salaryGeneratedTimelineService = salaryGeneratedTimelineService;
    }

    @Override
    public SalaryGeneratedTimelineBatchResult scan(String orgCode, String keyword, Integer limit, Integer eventLimit) {
        String safeOrgCode = text(orgCode);
        String safeKeyword = text(keyword);
        int safeLimit = Math.max(1, Math.min(limit == null ? 50 : limit, 500));
        int safeEventLimit = Math.max(1, Math.min(eventLimit == null ? 160 : eventLimit, 500));
        List<Map<String, Object>> persons = findPersons(safeOrgCode, safeKeyword, safeLimit);

        List<SalaryGeneratedTimelineBatchItem> items = new ArrayList<>();
        int ok = 0;
        int issue = 0;
        int different = 0;
        int missing = 0;
        int errors = 0;
        int unsupported = 0;

        for (Map<String, Object> person : persons) {
            String personCode = text(person.get("person_code"));
            String personName = text(person.get("person_name"));
            String personOrgCode = text(person.get("org_code"));
            try {
                SalaryGeneratedTimelineResult result = salaryGeneratedTimelineService.generateAndCompare(personCode, safeEventLimit);
                List<SalaryGeneratedTimelineItem> issueItems = result.items().stream()
                        .filter(item -> !"MATCH".equalsIgnoreCase(text(item.status())))
                        .toList();
                String status = issueItems.isEmpty() ? STATUS_OK : STATUS_ISSUE;
                if (STATUS_OK.equals(status)) {
                    ok++;
                } else {
                    issue++;
                }
                different += result.differentCount();
                missing += result.missingHistoryCount();
                errors += result.errorCount();
                unsupported += result.unsupportedHistoryCount();
                items.add(new SalaryGeneratedTimelineBatchItem(
                        personCode,
                        personName,
                        personOrgCode,
                        result.expectedCount(),
                        result.matchedCount(),
                        result.differentCount(),
                        result.missingHistoryCount(),
                        result.errorCount(),
                        result.unsupportedHistoryCount(),
                        status,
                        firstIssue(issueItems),
                        issueItems
                ));
            } catch (RuntimeException ex) {
                issue++;
                errors++;
                items.add(new SalaryGeneratedTimelineBatchItem(
                        personCode,
                        personName,
                        personOrgCode,
                        0,
                        0,
                        0,
                        0,
                        1,
                        0,
                        STATUS_ERROR,
                        left(text(ex.getMessage()), 500),
                        List.of()
                ));
            }
        }

        return new SalaryGeneratedTimelineBatchResult(
                safeOrgCode,
                safeKeyword,
                items.size(),
                ok,
                issue,
                different,
                missing,
                errors,
                unsupported,
                items
        );
    }

    private List<Map<String, Object>> findPersons(String orgCode, String keyword, int limit) {
        String keywordSql = keyword.isBlank() ? "" : "%" + keyword + "%";
        return jdbcTemplate.queryForList("""
                SELECT CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS person_code,
                       TRIM(COALESCE(xm, '')) AS person_name,
                       TRIM(dwbm) AS org_code
                FROM dryjbxx
                WHERE dwbm LIKE CONCAT(?, '%')
                  AND (? = ''
                       OR CONCAT(TRIM(dwbm), '-', TRIM(grbm)) LIKE ?
                       OR TRIM(COALESCE(xm, '')) LIKE ?)
                ORDER BY dwbm, grbm
                LIMIT ?
                """, orgCode, keyword, keywordSql, keywordSql, limit);
    }

    private String firstIssue(List<SalaryGeneratedTimelineItem> issueItems) {
        if (issueItems.isEmpty()) {
            return "";
        }
        SalaryGeneratedTimelineItem first = issueItems.getFirst();
        String period = first.year() + "." + String.format("%02d", first.month());
        String message = text(first.message());
        return left(period + " " + text(first.changeType()) + " " + text(first.status())
                + (message.isBlank() ? "" : " " + message), 500);
    }

    private String text(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value).trim();
    }

    private String left(String value, int maxLength) {
        String text = StringUtils.hasText(value) ? value.trim() : "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}
