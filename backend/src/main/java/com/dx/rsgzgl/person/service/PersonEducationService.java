package com.dx.rsgzgl.person.service;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.person.dto.PersonBaseChangeRequest;
import com.dx.rsgzgl.person.dto.PersonDetail;
import com.dx.rsgzgl.person.dto.PersonEducationRequest;
import com.dx.rsgzgl.person.dto.PersonEducationResponse;
import com.dx.rsgzgl.system.service.SystemAuditService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PersonEducationService {

    private final JdbcTemplate jdbcTemplate;
    private final PersonQueryService personQueryService;
    private final PersonBaseChangeService personBaseChangeService;
    private final SystemAuditService systemAuditService;

    public PersonEducationService(
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

    public List<PersonEducationResponse> list(String personCode) {
        PersonDetail person = personQueryService.detail(personCode);
        return jdbcTemplate.query("""
                SELECT id,
                       TRIM(dwbm) AS org_code,
                       TRIM(xlbm) AS education_code,
                       TRIM(xl) AS education_name,
                       TRIM(byyx) AS school,
                       TRIM(rxsj) AS enroll_date,
                       TRIM(bysj) AS graduation_date,
                       xz AS study_years,
                       TRIM(xllb) AS education_type,
                       TRIM(bz) AS note
                FROM dxl
                WHERE dwbm = ? AND grbm = ?
                ORDER BY bysj DESC, id DESC
                """, (rs, rowNum) -> new PersonEducationResponse(
                rs.getLong("id"),
                person.personCode(),
                person.personName(),
                rs.getString("org_code"),
                rs.getString("education_code"),
                rs.getString("education_name"),
                rs.getString("school"),
                rs.getString("enroll_date"),
                rs.getString("graduation_date"),
                rs.getInt("study_years"),
                rs.getString("education_type"),
                rs.getString("note")
        ), person.orgCode(), personNo(person.personCode(), person.orgCode()));
    }

    @Transactional
    public PersonEducationResponse create(String personCode, PersonEducationRequest request) {
        PersonDetail person = personQueryService.detail(personCode);
        NormalizedEducation education = normalize(request);
        String personNo = personNo(person.personCode(), person.orgCode());
        jdbcTemplate.update("""
                INSERT INTO dxl(dwbm, grbm, xlbm, xl, byyx, rxsj, bysj, xz, xllb, bz)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                person.orgCode(),
                personNo,
                education.educationCode(),
                education.educationName(),
                education.school(),
                education.enrollDate(),
                education.graduationDate(),
                education.studyYears(),
                education.educationType(),
                education.note());
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        recordBaseChange(person, id, education, changeSummary("create education", education, request.summary()));
        systemAuditService.record("person", "person-education-create", "PERSON_EDUCATION", String.valueOf(id),
                person.personCode() + " " + education.educationCode() + " " + education.graduationDate());
        return findOwned(id);
    }

    @Transactional
    public PersonEducationResponse update(Long id, PersonEducationRequest request) {
        PersonEducationResponse existing = findOwned(id);
        NormalizedEducation education = normalize(request);
        jdbcTemplate.update("""
                UPDATE dxl
                SET xlbm = ?,
                    xl = ?,
                    byyx = ?,
                    rxsj = ?,
                    bysj = ?,
                    xz = ?,
                    xllb = ?,
                    bz = ?
                WHERE id = ?
                """,
                education.educationCode(),
                education.educationName(),
                education.school(),
                education.enrollDate(),
                education.graduationDate(),
                education.studyYears(),
                education.educationType(),
                education.note(),
                id);
        PersonDetail person = personQueryService.detail(existing.personCode());
        recordBaseChange(person, id, education, changeSummary("update education", education, request.summary()));
        systemAuditService.record("person", "person-education-update", "PERSON_EDUCATION", String.valueOf(id),
                existing.personCode() + " " + education.educationCode() + " " + education.graduationDate());
        return findOwned(id);
    }

    private PersonEducationResponse findOwned(Long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT e.id,
                       TRIM(e.dwbm) AS org_code,
                       TRIM(e.grbm) AS person_no,
                       TRIM(e.xlbm) AS education_code,
                       TRIM(e.xl) AS education_name,
                       TRIM(e.byyx) AS school,
                       TRIM(e.rxsj) AS enroll_date,
                       TRIM(e.bysj) AS graduation_date,
                       e.xz AS study_years,
                       TRIM(e.xllb) AS education_type,
                       TRIM(e.bz) AS note
                FROM dxl e
                WHERE e.id = ?
                LIMIT 1
                """, id);
        if (rows.isEmpty()) {
            throw new BusinessException("PERSON_EDUCATION_NOT_FOUND", "Person education not found: " + id);
        }
        Map<String, Object> row = rows.getFirst();
        String orgCode = text(row.get("org_code"));
        String personNo = text(row.get("person_no"));
        PersonDetail person = personQueryService.detail(orgCode + "-" + personNo);
        return new PersonEducationResponse(
                longValue(row.get("id")),
                person.personCode(),
                person.personName(),
                orgCode,
                text(row.get("education_code")),
                text(row.get("education_name")),
                text(row.get("school")),
                text(row.get("enroll_date")),
                text(row.get("graduation_date")),
                intValue(row.get("study_years")),
                text(row.get("education_type")),
                text(row.get("note"))
        );
    }

    private void recordBaseChange(PersonDetail person, Long id, NormalizedEducation education, String summary) {
        int year = Integer.parseInt(education.graduationDate().substring(0, 4));
        int month = Integer.parseInt(education.graduationDate().substring(5, 7));
        personBaseChangeService.create(person.personCode(), new PersonBaseChangeRequest(
                "dxl",
                year,
                month,
                "dxl",
                String.valueOf(id),
                summary
        ));
    }

    private NormalizedEducation normalize(PersonEducationRequest request) {
        if (request == null) {
            throw new BusinessException("INVALID_PERSON_EDUCATION", "request body is required.");
        }
        String educationCode = trimToLength(requireText(request.educationCode(), "educationCode"), 2);
        String educationName = trimToLength(defaultText(request.educationName(), educationCode), 18);
        String school = trimToLength(request.school(), 60);
        String enrollDate = normalizeOptionalDate(request.enrollDate(), "enrollDate");
        String graduationDate = normalizeRequiredDate(request.graduationDate(), "graduationDate");
        int studyYears = request.studyYears() == null ? 0 : Math.max(0, request.studyYears());
        String educationType = trimToLength(request.educationType(), 10);
        String note = trimToLength(request.note(), 10);
        return new NormalizedEducation(educationCode, educationName, school, enrollDate,
                graduationDate, studyYears, educationType, note);
    }

    private String changeSummary(String action, NormalizedEducation education, String summary) {
        String text = trim(summary);
        if (!text.isBlank()) {
            return text;
        }
        return action + " " + education.educationCode() + " " + education.educationName() + " " + education.graduationDate();
    }

    private String normalizeRequiredDate(String value, String field) {
        return normalizeDate(requireText(value, field), field);
    }

    private String normalizeOptionalDate(String value, String field) {
        String text = trim(value);
        return text.isBlank() ? "" : normalizeDate(text, field);
    }

    private String normalizeDate(String value, String field) {
        String text = value.replace(".", "").replace("-", "");
        if (!text.matches("\\d{6}")) {
            throw new BusinessException("INVALID_PERSON_EDUCATION", field + " must be yyyyMM or yyyy.MM.");
        }
        int month = Integer.parseInt(text.substring(4, 6));
        if (month < 1 || month > 12) {
            throw new BusinessException("INVALID_PERSON_EDUCATION", field + " month is out of range.");
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
            throw new BusinessException("INVALID_PERSON_EDUCATION", field + " is required.");
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

    private record NormalizedEducation(
            String educationCode,
            String educationName,
            String school,
            String enrollDate,
            String graduationDate,
            int studyYears,
            String educationType,
            String note
    ) {
    }
}
