package com.dx.rsgzgl.person.service;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.person.dto.PersonBaseChangeRequest;
import com.dx.rsgzgl.person.dto.PersonDetail;
import com.dx.rsgzgl.person.dto.PersonPostRequest;
import com.dx.rsgzgl.person.dto.PersonPostResponse;
import com.dx.rsgzgl.system.service.SystemAuditService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PersonPostService {

    private final JdbcTemplate jdbcTemplate;
    private final PersonQueryService personQueryService;
    private final PersonBaseChangeService personBaseChangeService;
    private final SystemAuditService systemAuditService;

    public PersonPostService(
            JdbcTemplate jdbcTemplate,
            PersonQueryService personQueryService,
            PersonBaseChangeService personBaseChangeService,
            SystemAuditService systemAuditService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.personQueryService = personQueryService;
        this.personBaseChangeService = personBaseChangeService;
        this.systemAuditService = systemAuditService;
    }

    public List<PersonPostResponse> list(String personCode) {
        PersonDetail person = personQueryService.detail(personCode);
        return jdbcTemplate.query("""
                SELECT z.id,
                       TRIM(z.dwbm) AS org_code,
                       TRIM(z.grbm) AS person_no,
                       TRIM(z.xrzwbm) AS current_post_code,
                       TRIM(z.xrzw) AS current_post_name,
                       TRIM(z.zwjb) AS post_level,
                       TRIM(z.zjbm) AS rank_code,
                       TRIM(z.zwbm) AS post_code,
                       TRIM(z.xzzw) AS post_name,
                       TRIM(z.zwlb) AS post_category,
                       TRIM(z.srny) AS start_date,
                       z.kjnx AS excluded_years,
                       TRIM(z.xrzwbz) AS current_post_flag,
                       TRIM(z.jsbz) AS payroll_flag
                FROM dryzwbh z
                WHERE z.dwbm = ? AND z.grbm = ?
                ORDER BY z.srny DESC, z.id DESC
                """, (rs, rowNum) -> new PersonPostResponse(
                rs.getLong("id"),
                person.personCode(),
                person.personName(),
                rs.getString("org_code"),
                rs.getString("post_code"),
                rs.getString("post_name"),
                rs.getString("post_level"),
                rs.getString("rank_code"),
                rs.getString("current_post_code"),
                rs.getString("current_post_name"),
                rs.getString("post_category"),
                rs.getString("start_date"),
                rs.getInt("excluded_years"),
                rs.getString("current_post_flag"),
                rs.getString("payroll_flag")
        ), person.orgCode(), personNo(person.personCode(), person.orgCode()));
    }

    @Transactional
    public PersonPostResponse create(String personCode, PersonPostRequest request) {
        PersonDetail person = personQueryService.detail(personCode);
        NormalizedPost post = normalize(request);
        String personNo = personNo(person.personCode(), person.orgCode());
        jdbcTemplate.update("""
                INSERT INTO dryzwbh(dwbm, grbm, xrzwbm, xrzw, zwjb, zjbm, zwbm,
                                    xzzw, zwlb, srny, kjnx, xrzwbz, jsbz)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                person.orgCode(),
                personNo,
                post.currentPostCode(),
                post.currentPostName(),
                post.postLevel(),
                post.rankCode(),
                post.postCode(),
                post.postName(),
                post.postCategory(),
                post.startDate(),
                post.excludedYears(),
                post.currentPostFlag(),
                post.payrollFlag());
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        recordBaseChange(person, id, post, changeSummary("新增任职信息", post, request.summary()));
        systemAuditService.record("person", "person-post-create", "PERSON_POST", String.valueOf(id),
                person.personCode() + " " + post.postCode() + " " + post.startDate());
        return findOwned(id);
    }

    @Transactional
    public PersonPostResponse update(Long id, PersonPostRequest request) {
        PersonPostResponse existing = findOwned(id);
        NormalizedPost post = normalize(request);
        jdbcTemplate.update("""
                UPDATE dryzwbh
                SET xrzwbm = ?,
                    xrzw = ?,
                    zwjb = ?,
                    zjbm = ?,
                    zwbm = ?,
                    xzzw = ?,
                    zwlb = ?,
                    srny = ?,
                    kjnx = ?,
                    xrzwbz = ?,
                    jsbz = ?
                WHERE id = ?
                """,
                post.currentPostCode(),
                post.currentPostName(),
                post.postLevel(),
                post.rankCode(),
                post.postCode(),
                post.postName(),
                post.postCategory(),
                post.startDate(),
                post.excludedYears(),
                post.currentPostFlag(),
                post.payrollFlag(),
                id);
        PersonDetail person = personQueryService.detail(existing.personCode());
        recordBaseChange(person, id, post, changeSummary("编辑任职信息", post, request.summary()));
        systemAuditService.record("person", "person-post-update", "PERSON_POST", String.valueOf(id),
                existing.personCode() + " " + post.postCode() + " " + post.startDate());
        return findOwned(id);
    }

    private PersonPostResponse findOwned(Long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT z.id,
                       TRIM(z.dwbm) AS org_code,
                       TRIM(z.grbm) AS person_no,
                       TRIM(COALESCE(p.xm, '')) AS person_name,
                       TRIM(z.xrzwbm) AS current_post_code,
                       TRIM(z.xrzw) AS current_post_name,
                       TRIM(z.zwjb) AS post_level,
                       TRIM(z.zjbm) AS rank_code,
                       TRIM(z.zwbm) AS post_code,
                       TRIM(z.xzzw) AS post_name,
                       TRIM(z.zwlb) AS post_category,
                       TRIM(z.srny) AS start_date,
                       z.kjnx AS excluded_years,
                       TRIM(z.xrzwbz) AS current_post_flag,
                       TRIM(z.jsbz) AS payroll_flag
                FROM dryzwbh z
                LEFT JOIN dryjbxx p ON p.dwbm = z.dwbm AND p.grbm = z.grbm
                WHERE z.id = ?
                LIMIT 1
                """, id);
        if (rows.isEmpty()) {
            throw new BusinessException("PERSON_POST_NOT_FOUND", "Person post not found: " + id);
        }
        Map<String, Object> row = rows.getFirst();
        String orgCode = text(row.get("org_code"));
        String personNo = text(row.get("person_no"));
        String personCode = orgCode + "-" + personNo;
        PersonDetail person = personQueryService.detail(personCode);
        return new PersonPostResponse(
                longValue(row.get("id")),
                person.personCode(),
                person.personName(),
                orgCode,
                text(row.get("post_code")),
                text(row.get("post_name")),
                text(row.get("post_level")),
                text(row.get("rank_code")),
                text(row.get("current_post_code")),
                text(row.get("current_post_name")),
                text(row.get("post_category")),
                text(row.get("start_date")),
                intValue(row.get("excluded_years")),
                text(row.get("current_post_flag")),
                text(row.get("payroll_flag"))
        );
    }

    private void recordBaseChange(PersonDetail person, Long id, NormalizedPost post, String summary) {
        int year = Integer.parseInt(post.startDate().substring(0, 4));
        int month = Integer.parseInt(post.startDate().substring(5, 7));
        personBaseChangeService.create(person.personCode(), new PersonBaseChangeRequest(
                "dryzwbh",
                year,
                month,
                "dryzwbh",
                String.valueOf(id),
                summary
        ));
    }

    private NormalizedPost normalize(PersonPostRequest request) {
        if (request == null) {
            throw new BusinessException("INVALID_PERSON_POST", "request body is required.");
        }
        String postCode = requireText(request.postCode(), "postCode");
        String postName = defaultText(request.postName(), postCode);
        String postLevel = defaultText(request.postLevel(), postName);
        String rankCode = defaultText(request.rankCode(), postCode);
        String currentPostCode = defaultText(request.currentPostCode(), postCode);
        String currentPostName = postName;
        String postCategory = trimToLength(request.postCategory(), 4);
        String startDate = normalizeStartDate(request.startDate());
        int excludedYears = request.excludedYears() == null ? 0 : Math.max(0, request.excludedYears());
        String currentPostFlag = trimToLength(request.currentPostFlag(), 1);
        String payrollFlag = trimToLength(request.payrollFlag(), 7);
        return new NormalizedPost(
                trimToLength(postCode, 4),
                trimToLength(postName, 20),
                trimToLength(postLevel, 20),
                trimToLength(rankCode, 4),
                trimToLength(currentPostCode, 4),
                trimToLength(currentPostName, 50),
                postCategory,
                startDate,
                excludedYears,
                currentPostFlag,
                payrollFlag
        );
    }

    private String changeSummary(String action, NormalizedPost post, String summary) {
        String text = trim(summary);
        if (!text.isBlank()) {
            return text;
        }
        return action + " " + post.postCode() + " " + post.postName() + " " + post.startDate();
    }

    private String normalizeStartDate(String value) {
        String text = requireText(value, "startDate").replace(".", "").replace("-", "");
        if (!text.matches("\\d{6}")) {
            throw new BusinessException("INVALID_PERSON_POST", "startDate must be yyyyMM or yyyy.MM.");
        }
        int month = Integer.parseInt(text.substring(4, 6));
        if (month < 1 || month > 12) {
            throw new BusinessException("INVALID_PERSON_POST", "startDate month is out of range.");
        }
        return text.substring(0, 4) + "." + text.substring(4, 6);
    }

    private String personNo(String personCode, String orgCode) {
        String prefix = orgCode + "-";
        if (personCode != null && personCode.startsWith(prefix)) {
            return personCode.substring(prefix.length());
        }
        int separator = personCode == null ? -1 : personCode.indexOf('-');
        return separator >= 0 ? personCode.substring(separator + 1) : trim(personCode);
    }

    private String requireText(String value, String field) {
        String text = trim(value);
        if (text.isBlank()) {
            throw new BusinessException("INVALID_PERSON_POST", field + " is required.");
        }
        return text;
    }

    private String defaultText(String value, String fallback) {
        String text = trim(value);
        return text.isBlank() ? fallback : text;
    }

    private String trimToLength(String value, int maxLength) {
        String text = trim(value);
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private String trim(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private String text(Object value) {
        return trim(value);
    }

    private Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private Integer intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null || value.toString().isBlank()) {
            return 0;
        }
        return Integer.parseInt(value.toString());
    }

    private record NormalizedPost(
            String postCode,
            String postName,
            String postLevel,
            String rankCode,
            String currentPostCode,
            String currentPostName,
            String postCategory,
            String startDate,
            int excludedYears,
            String currentPostFlag,
            String payrollFlag
    ) {
    }
}
