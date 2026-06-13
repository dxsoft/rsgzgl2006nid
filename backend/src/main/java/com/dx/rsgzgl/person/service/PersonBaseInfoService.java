package com.dx.rsgzgl.person.service;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.person.dto.PersonBaseChangeRequest;
import com.dx.rsgzgl.person.dto.PersonBaseInfoRequest;
import com.dx.rsgzgl.person.dto.PersonBaseInfoResponse;
import com.dx.rsgzgl.person.dto.PersonDetail;
import com.dx.rsgzgl.system.service.SystemAuditService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PersonBaseInfoService {

    private final JdbcTemplate jdbcTemplate;
    private final PersonQueryService personQueryService;
    private final PersonBaseChangeService personBaseChangeService;
    private final SystemAuditService systemAuditService;

    public PersonBaseInfoService(
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

    public PersonBaseInfoResponse get(String personCode) {
        PersonDetail person = personQueryService.detail(personCode);
        return findByPerson(person);
    }

    @Transactional
    public PersonBaseInfoResponse update(String personCode, PersonBaseInfoRequest request) {
        if (request == null) {
            throw new BusinessException("INVALID_PERSON_BASE_INFO", "request body is required.");
        }
        PersonDetail person = personQueryService.detail(personCode);
        NormalizedBaseInfo info = normalize(request);
        jdbcTemplate.update("""
                UPDATE dryjbxx
                SET ryfl = ?,
                    dwsx = ?,
                    gwfl = ?,
                    cjgzny = ?,
                    jrny = ?,
                    jhlqsny = ?,
                    zdjhlnx = ?,
                    xlbm = ?,
                    zgxl = ?,
                    zjbm = ?,
                    xrzw = ?,
                    zwjb = ?,
                    srny = ?
                WHERE dwbm = ? AND grbm = ?
                """,
                info.personCategory(),
                info.organizationType(),
                info.postCategory(),
                info.workStartDate(),
                info.joinOrgDate(),
                info.teacherNurseStartDate(),
                info.teacherNurseFixedYears(),
                info.educationCode(),
                info.education(),
                info.rankCode(),
                info.currentPost(),
                info.postLevel(),
                info.postStartDate(),
                person.orgCode(),
                personNo(person.personCode(), person.orgCode()));
        String summary = summary(request.summary(), info);
        personBaseChangeService.create(person.personCode(), new PersonBaseChangeRequest(
                "dryjbxx",
                changeYear(info),
                changeMonth(info),
                "dryjbxx",
                person.personCode(),
                summary
        ));
        systemAuditService.record("person", "person-base-info-update", "PERSON", person.personCode(), summary);
        return findByPerson(person);
    }

    private PersonBaseInfoResponse findByPerson(PersonDetail person) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(dwbm) AS org_code,
                       TRIM(grbm) AS person_no,
                       TRIM(xm) AS person_name,
                       TRIM(ryfl) AS person_category,
                       TRIM(dwsx) AS organization_type,
                       TRIM(gwfl) AS post_category,
                       TRIM(cjgzny) AS work_start_date,
                       TRIM(jrny) AS join_org_date,
                       TRIM(jhlqsny) AS teacher_nurse_start_date,
                       zdjhlnx AS teacher_nurse_fixed_years,
                       TRIM(xlbm) AS education_code,
                       TRIM(zgxl) AS education,
                       TRIM(zjbm) AS rank_code,
                       TRIM(xrzw) AS current_post,
                       TRIM(zwjb) AS post_level,
                       TRIM(srny) AS post_start_date
                FROM dryjbxx
                WHERE dwbm = ? AND grbm = ?
                LIMIT 1
                """, person.orgCode(), personNo(person.personCode(), person.orgCode()));
        if (rows.isEmpty()) {
            throw new BusinessException("PERSON_NOT_FOUND", "Person not found: " + person.personCode());
        }
        Map<String, Object> row = rows.getFirst();
        return new PersonBaseInfoResponse(
                person.personCode(),
                text(row.get("person_name")),
                text(row.get("org_code")),
                text(row.get("person_category")),
                text(row.get("organization_type")),
                text(row.get("post_category")),
                text(row.get("work_start_date")),
                text(row.get("join_org_date")),
                text(row.get("teacher_nurse_start_date")),
                intValue(row.get("teacher_nurse_fixed_years")),
                text(row.get("education_code")),
                text(row.get("education")),
                text(row.get("rank_code")),
                text(row.get("current_post")),
                text(row.get("post_level")),
                text(row.get("post_start_date"))
        );
    }

    private NormalizedBaseInfo normalize(PersonBaseInfoRequest request) {
        return new NormalizedBaseInfo(
                trimToLength(request.personCategory(), 12),
                trimToLength(request.organizationType(), 2),
                trimToLength(request.postCategory(), 16),
                normalizeOptionalDate(request.workStartDate(), "workStartDate"),
                normalizeOptionalDate(request.joinOrgDate(), "joinOrgDate"),
                normalizeOptionalDate(request.teacherNurseStartDate(), "teacherNurseStartDate"),
                request.teacherNurseFixedYears() == null ? 0 : Math.max(0, request.teacherNurseFixedYears()),
                trimToLength(request.educationCode(), 2),
                trimToLength(request.education(), 18),
                trimToLength(request.rankCode(), 4),
                trimToLength(request.currentPost(), 50),
                trimToLength(request.postLevel(), 20),
                normalizeOptionalDate(request.postStartDate(), "postStartDate")
        );
    }

    private Integer changeYear(NormalizedBaseInfo info) {
        String date = firstDate(info.postStartDate(), info.workStartDate(), info.joinOrgDate(), info.teacherNurseStartDate());
        return date.isBlank() ? null : Integer.parseInt(date.substring(0, 4));
    }

    private Integer changeMonth(NormalizedBaseInfo info) {
        String date = firstDate(info.postStartDate(), info.workStartDate(), info.joinOrgDate(), info.teacherNurseStartDate());
        return date.isBlank() ? null : Integer.parseInt(date.substring(5, 7));
    }

    private String firstDate(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String summary(String summary, NormalizedBaseInfo info) {
        String text = trim(summary);
        if (!text.isBlank()) {
            return text;
        }
        return "update base info "
                + info.personCategory() + " "
                + info.organizationType() + " "
                + info.currentPost() + " "
                + info.postStartDate();
    }

    private String normalizeOptionalDate(String value, String field) {
        String text = trim(value);
        if (text.isBlank()) {
            return "";
        }
        String normalized = text.replace(".", "").replace("-", "");
        if (!normalized.matches("\\d{6}")) {
            throw new BusinessException("INVALID_PERSON_BASE_INFO", field + " must be yyyyMM or yyyy.MM.");
        }
        int month = Integer.parseInt(normalized.substring(4, 6));
        if (month < 1 || month > 12) {
            throw new BusinessException("INVALID_PERSON_BASE_INFO", field + " month is out of range.");
        }
        return normalized.substring(0, 4) + "." + normalized.substring(4, 6);
    }

    private String personNo(String personCode, String orgCode) {
        String prefix = orgCode + "-";
        if (personCode != null && personCode.startsWith(prefix)) {
            return personCode.substring(prefix.length());
        }
        int separator = personCode == null ? -1 : personCode.indexOf('-');
        return separator >= 0 ? personCode.substring(separator + 1) : trim(personCode);
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

    private Integer intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null || value.toString().isBlank()) {
            return 0;
        }
        return Integer.parseInt(value.toString());
    }

    private record NormalizedBaseInfo(
            String personCategory,
            String organizationType,
            String postCategory,
            String workStartDate,
            String joinOrgDate,
            String teacherNurseStartDate,
            int teacherNurseFixedYears,
            String educationCode,
            String education,
            String rankCode,
            String currentPost,
            String postLevel,
            String postStartDate
    ) {
    }
}
