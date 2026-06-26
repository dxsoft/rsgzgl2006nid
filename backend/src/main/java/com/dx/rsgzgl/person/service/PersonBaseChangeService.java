package com.dx.rsgzgl.person.service;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.person.dto.PersonBaseChangeRequest;
import com.dx.rsgzgl.person.dto.PersonBaseChangeResponse;
import com.dx.rsgzgl.person.dto.PersonDetail;
import com.dx.rsgzgl.system.service.CurrentUserService;
import com.dx.rsgzgl.system.service.SystemAuditService;
import com.dx.rsgzgl.system.service.WorkbenchService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
public class PersonBaseChangeService {

    private final JdbcTemplate jdbcTemplate;
    private final PersonQueryService personQueryService;
    private final CurrentUserService currentUserService;
    private final SystemAuditService systemAuditService;
    private final WorkbenchService workbenchService;

    public PersonBaseChangeService(
            JdbcTemplate jdbcTemplate,
            PersonQueryService personQueryService,
            CurrentUserService currentUserService,
            SystemAuditService systemAuditService,
            WorkbenchService workbenchService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.personQueryService = personQueryService;
        this.currentUserService = currentUserService;
        this.systemAuditService = systemAuditService;
        this.workbenchService = workbenchService;
    }

    @Transactional
    public PersonBaseChangeResponse create(String personCode, PersonBaseChangeRequest request) {
        ensureTable();
        if (request == null) {
            throw new BusinessException("INVALID_BASE_CHANGE", "request body is required.");
        }
        PersonDetail person = personQueryService.detail(personCode);
        String dataType = requireText(request.dataType(), "dataType");
        String sourceTable = trim(request.sourceTable());
        String sourceId = trim(request.sourceId());
        String summary = requireText(request.summary(), "summary");
        Integer changeYear = request.changeYear();
        Integer changeMonth = request.changeMonth();
        validatePeriod(changeYear, changeMonth);
        String username = currentUserService.currentUsername();
        if (username == null || username.isBlank()) {
            username = "system";
        }

        jdbcTemplate.update("""
                INSERT INTO person_base_change_log(person_code, person_name, org_code, data_type,
                                                   change_year, change_month, source_table, source_id,
                                                   summary, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                person.personCode(),
                person.personName(),
                person.orgCode(),
                dataType,
                changeYear,
                changeMonth,
                sourceTable,
                sourceId,
                summary,
                username);
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        systemAuditService.record("person", "person-base-change", "PERSON", person.personCode(),
                dataType + " " + summary);
        workbenchService.markSalaryTodoCacheDirtyForDataChange(
                "base data changed: " + person.personCode() + " " + dataType
        );
        return findById(id);
    }

    public List<PersonBaseChangeResponse> list(String personCode, int limit) {
        ensureTable();
        PersonDetail person = personQueryService.detail(personCode);
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return jdbcTemplate.query("""
                SELECT *
                FROM person_base_change_log
                WHERE person_code = ?
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """, (rs, rowNum) -> mapChange(rs.getLong("id"), rs.getString("person_code"),
                rs.getString("person_name"), rs.getString("org_code"), rs.getString("data_type"),
                integerValue(rs.getObject("change_year")), integerValue(rs.getObject("change_month")),
                rs.getString("source_table"), rs.getString("source_id"), rs.getString("summary"),
                rs.getString("created_by"), rs.getTimestamp("created_at")), person.personCode(), safeLimit);
    }

    private PersonBaseChangeResponse findById(Long id) {
        return jdbcTemplate.queryForObject("""
                SELECT *
                FROM person_base_change_log
                WHERE id = ?
                """, (rs, rowNum) -> mapChange(rs.getLong("id"), rs.getString("person_code"),
                rs.getString("person_name"), rs.getString("org_code"), rs.getString("data_type"),
                integerValue(rs.getObject("change_year")), integerValue(rs.getObject("change_month")),
                rs.getString("source_table"), rs.getString("source_id"), rs.getString("summary"),
                rs.getString("created_by"), rs.getTimestamp("created_at")), id);
    }

    private void ensureTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS person_base_change_log (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    person_code VARCHAR(128) NOT NULL,
                    person_name VARCHAR(128) NULL,
                    org_code VARCHAR(64) NOT NULL,
                    data_type VARCHAR(64) NOT NULL,
                    change_year INT NULL,
                    change_month INT NULL,
                    source_table VARCHAR(64) NULL,
                    source_id VARCHAR(128) NULL,
                    summary VARCHAR(1024) NOT NULL,
                    created_by VARCHAR(64) NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    KEY idx_person_base_change_person (person_code, created_at),
                    KEY idx_person_base_change_org (org_code, created_at)
                )
                """);
    }

    private PersonBaseChangeResponse mapChange(
            Long id,
            String personCode,
            String personName,
            String orgCode,
            String dataType,
            Integer changeYear,
            Integer changeMonth,
            String sourceTable,
            String sourceId,
            String summary,
            String createdBy,
            Timestamp createdAt
    ) {
        return new PersonBaseChangeResponse(id, personCode, personName, orgCode, dataType,
                changeYear, changeMonth, sourceTable, sourceId, summary, createdBy,
                createdAt == null ? "" : createdAt.toLocalDateTime().toString());
    }

    private void validatePeriod(Integer year, Integer month) {
        if (year != null && (year < 1900 || year > 2099)) {
            throw new BusinessException("INVALID_BASE_CHANGE", "changeYear is out of range.");
        }
        if (month != null && (month < 1 || month > 12)) {
            throw new BusinessException("INVALID_BASE_CHANGE", "changeMonth is out of range.");
        }
    }

    private String requireText(String value, String field) {
        String text = trim(value);
        if (text.isBlank()) {
            throw new BusinessException("INVALID_BASE_CHANGE", field + " is required.");
        }
        return text;
    }

    private Integer integerValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        return Integer.parseInt(value.toString());
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
