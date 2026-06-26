package com.dx.rsgzgl.person.service;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.person.dto.PersonAssessmentRequest;
import com.dx.rsgzgl.person.dto.PersonAssessmentResponse;
import com.dx.rsgzgl.person.dto.PersonBaseChangeRequest;
import com.dx.rsgzgl.person.dto.PersonDetail;
import com.dx.rsgzgl.system.service.SystemAuditService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PersonAssessmentService {

    private final JdbcTemplate jdbcTemplate;
    private final PersonQueryService personQueryService;
    private final PersonBaseChangeService personBaseChangeService;
    private final SystemAuditService systemAuditService;

    public PersonAssessmentService(
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

    public List<PersonAssessmentResponse> list(String personCode) {
        PersonDetail person = personQueryService.detail(personCode);
        return jdbcTemplate.query("""
                SELECT id,
                       TRIM(dwbm) AS org_code,
                       TRIM(khnd) AS assessment_year,
                       TRIM(khjg) AS assessment_result
                FROM dndkh
                WHERE dwbm = ? AND grbm = ?
                ORDER BY khnd DESC, id DESC
                """, (rs, rowNum) -> new PersonAssessmentResponse(
                rs.getLong("id"),
                person.personCode(),
                person.personName(),
                rs.getString("org_code"),
                rs.getString("assessment_year"),
                rs.getString("assessment_result")
        ), person.orgCode(), personNo(person.personCode(), person.orgCode()));
    }

    @Transactional
    public PersonAssessmentResponse save(String personCode, PersonAssessmentRequest request) {
        PersonDetail person = personQueryService.detail(personCode);
        NormalizedAssessment assessment = normalize(request);
        String personNo = personNo(person.personCode(), person.orgCode());
        Long existingId = existingId(person.orgCode(), personNo, assessment.year());
        if (existingId == null) {
            jdbcTemplate.update("""
                    INSERT INTO dndkh(dwbm, grbm, khnd, khjg)
                    VALUES (?, ?, ?, ?)
                    """, person.orgCode(), personNo, assessment.year(), assessment.result());
            existingId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            systemAuditService.record("person", "person-assessment-create", "PERSON_ASSESSMENT", String.valueOf(existingId),
                    person.personCode() + " " + assessment.year() + " " + assessment.result());
        } else {
            jdbcTemplate.update("UPDATE dndkh SET khjg = ? WHERE id = ?", assessment.result(), existingId);
            systemAuditService.record("person", "person-assessment-update", "PERSON_ASSESSMENT", String.valueOf(existingId),
                    person.personCode() + " " + assessment.year() + " " + assessment.result());
        }
        recordBaseChange(person, existingId, assessment, changeSummary("save assessment", assessment, request.summary()));
        return findOwned(existingId);
    }

    @Transactional
    public PersonAssessmentResponse update(Long id, PersonAssessmentRequest request) {
        PersonAssessmentResponse existing = findOwned(id);
        NormalizedAssessment assessment = normalize(request);
        jdbcTemplate.update("UPDATE dndkh SET khnd = ?, khjg = ? WHERE id = ?", assessment.year(), assessment.result(), id);
        PersonDetail person = personQueryService.detail(existing.personCode());
        recordBaseChange(person, id, assessment, changeSummary("update assessment", assessment, request.summary()));
        systemAuditService.record("person", "person-assessment-update", "PERSON_ASSESSMENT", String.valueOf(id),
                existing.personCode() + " " + assessment.year() + " " + assessment.result());
        return findOwned(id);
    }

    private PersonAssessmentResponse findOwned(Long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id,
                       TRIM(dwbm) AS org_code,
                       TRIM(grbm) AS person_no,
                       TRIM(khnd) AS assessment_year,
                       TRIM(khjg) AS assessment_result
                FROM dndkh
                WHERE id = ?
                LIMIT 1
                """, id);
        if (rows.isEmpty()) {
            throw new BusinessException("PERSON_ASSESSMENT_NOT_FOUND", "Person assessment not found: " + id);
        }
        Map<String, Object> row = rows.getFirst();
        String orgCode = text(row.get("org_code"));
        String personNo = text(row.get("person_no"));
        PersonDetail person = personQueryService.detail(orgCode + "-" + personNo);
        return new PersonAssessmentResponse(
                longValue(row.get("id")),
                person.personCode(),
                person.personName(),
                orgCode,
                text(row.get("assessment_year")),
                text(row.get("assessment_result"))
        );
    }

    private Long existingId(String orgCode, String personNo, String year) {
        List<Long> ids = jdbcTemplate.queryForList("""
                SELECT id
                FROM dndkh
                WHERE dwbm = ? AND grbm = ? AND khnd = ?
                ORDER BY id DESC
                LIMIT 1
                """, Long.class, orgCode, personNo, year);
        return ids.isEmpty() ? null : ids.getFirst();
    }

    private void recordBaseChange(PersonDetail person, Long id, NormalizedAssessment assessment, String summary) {
        personBaseChangeService.create(person.personCode(), new PersonBaseChangeRequest(
                "dndkh",
                Integer.parseInt(assessment.year()),
                12,
                "dndkh",
                assessment.year() + ":assessment",
                summary + " #" + id
        ));
    }

    private NormalizedAssessment normalize(PersonAssessmentRequest request) {
        if (request == null) {
            throw new BusinessException("INVALID_PERSON_ASSESSMENT", "request body is required.");
        }
        String year = requireText(request.year(), "year");
        if (!year.matches("\\d{4}")) {
            throw new BusinessException("INVALID_PERSON_ASSESSMENT", "year must be yyyy.");
        }
        int yearValue = Integer.parseInt(year);
        if (yearValue < 1900 || yearValue > 2099) {
            throw new BusinessException("INVALID_PERSON_ASSESSMENT", "year is out of range.");
        }
        String result = trimToLength(requireText(request.result(), "result"), 16);
        return new NormalizedAssessment(year, result);
    }

    private String changeSummary(String action, NormalizedAssessment assessment, String summary) {
        String text = trim(summary);
        if (!text.isBlank()) {
            return text;
        }
        return action + " " + assessment.year() + " " + assessment.result();
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
            throw new BusinessException("INVALID_PERSON_ASSESSMENT", field + " is required.");
        }
        return text;
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

    private record NormalizedAssessment(String year, String result) {
    }
}
