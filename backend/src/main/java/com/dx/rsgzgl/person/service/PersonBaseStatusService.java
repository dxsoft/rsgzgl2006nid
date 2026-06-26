package com.dx.rsgzgl.person.service;

import com.dx.rsgzgl.person.dto.PersonBaseStatusResponse;
import com.dx.rsgzgl.person.dto.PersonDetail;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PersonBaseStatusService {

    private final JdbcTemplate jdbcTemplate;
    private final PersonQueryService personQueryService;

    public PersonBaseStatusService(JdbcTemplate jdbcTemplate, PersonQueryService personQueryService) {
        this.jdbcTemplate = jdbcTemplate;
        this.personQueryService = personQueryService;
    }

    public PersonBaseStatusResponse get(String personCode) {
        PersonDetail person = personQueryService.detail(personCode);
        String personNo = personNo(person.personCode(), person.orgCode());
        Map<String, Object> latest = latestChange(person.personCode());
        Map<String, Object> cache = todoCacheMeta();
        return new PersonBaseStatusResponse(
                person.personCode(),
                person.personName(),
                person.orgCode(),
                count("dryzwbh", person.orgCode(), personNo),
                count("dxl", person.orgCode(), personNo),
                count("dndkh", person.orgCode(), personNo),
                text(latest.get("data_type")),
                text(latest.get("summary")),
                text(latest.get("created_at")),
                text(cache.get("cache_status")),
                text(cache.get("last_refreshed_at")),
                text(cache.get("dirty_at"))
        );
    }

    private long count(String table, String orgCode, String personNo) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM " + table + " WHERE dwbm = ? AND grbm = ?",
                Long.class,
                orgCode,
                personNo
        );
        return count == null ? 0 : count;
    }

    private Map<String, Object> latestChange(String personCode) {
        ensureBaseChangeTable();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT data_type, summary, created_at
                FROM person_base_change_log
                WHERE person_code = ?
                ORDER BY created_at DESC, id DESC
                LIMIT 1
                """, personCode);
        return rows.isEmpty() ? Map.of() : rows.getFirst();
    }

    private Map<String, Object> todoCacheMeta() {
        ensureCacheMetaTable();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT cache_status, last_refreshed_at, dirty_at
                FROM salary_todo_cache_meta
                WHERE cache_key = 'salary-todo'
                LIMIT 1
                """);
        return rows.isEmpty() ? Map.of() : rows.getFirst();
    }

    private void ensureBaseChangeTable() {
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

    private void ensureCacheMetaTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS salary_todo_cache_meta (
                    cache_key VARCHAR(64) PRIMARY KEY,
                    last_refreshed_at DATETIME NOT NULL,
                    total_count BIGINT NOT NULL DEFAULT 0,
                    cache_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
                    dirty_at DATETIME NULL
                )
                """);
    }

    private String personNo(String personCode, String orgCode) {
        String prefix = orgCode + "-";
        if (personCode != null && personCode.startsWith(prefix)) {
            return personCode.substring(prefix.length());
        }
        int separator = personCode == null ? -1 : personCode.indexOf('-');
        return separator >= 0 ? personCode.substring(separator + 1) : text(personCode);
    }

    private String text(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
