package com.dx.rsgzgl.system.service;

import com.dx.rsgzgl.system.dto.SystemAuditLogResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SystemAuditService {

    private final JdbcTemplate jdbcTemplate;
    private final CurrentUserService currentUserService;

    public SystemAuditService(JdbcTemplate jdbcTemplate, CurrentUserService currentUserService) {
        this.jdbcTemplate = jdbcTemplate;
        this.currentUserService = currentUserService;
    }

    public void record(String module, String action, String targetType, String targetCode, String summary) {
        ensureTable();
        jdbcTemplate.update("""
                INSERT INTO sys_audit_log(module_name, action_name, target_type, target_code, summary, operator)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                trim(module),
                trim(action),
                trim(targetType),
                trim(targetCode),
                trim(summary),
                operator()
        );
    }

    public List<SystemAuditLogResponse> latest(int limit) {
        ensureTable();
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return latest("", "", "", "", "", safeLimit);
    }

    public List<SystemAuditLogResponse> latest(
            String module,
            String operator,
            String targetCode,
            String start,
            String end,
            int limit
    ) {
        ensureTable();
        int safeLimit = Math.max(1, Math.min(limit, 200));
        List<Object> params = new ArrayList<>();
        String where = auditWhere(module, operator, targetCode, start, end, params);
        params.add(safeLimit);
        return jdbcTemplate.query("""
                SELECT *
                FROM (
                    SELECT CONCAT('SYS-', id) AS audit_id,
                           module_name,
                           action_name,
                           target_type,
                           target_code,
                           summary,
                           operator,
                           created_at
                    FROM sys_audit_log
                    UNION ALL
                    SELECT CONCAT('SALARY-', id) AS audit_id,
                           'salary-config' AS module_name,
                           'field-config' AS action_name,
                           'SALARY_FIELD' AS target_type,
                           item_code AS target_code,
                           CONCAT(field_name, ': ', COALESCE(old_value, ''), ' -> ', COALESCE(new_value, '')) AS summary,
                           COALESCE(changed_by, 'system') AS operator,
                           changed_at AS created_at
                    FROM salary_field_config_audit
                ) audit_rows
                __AUDIT_WHERE__
                ORDER BY created_at DESC, audit_id DESC
                LIMIT ?
                """.replace("__AUDIT_WHERE__", where), (rs, rowNum) -> new SystemAuditLogResponse(
                rs.getString("audit_id"),
                rs.getString("module_name"),
                rs.getString("action_name"),
                rs.getString("target_type"),
                rs.getString("target_code"),
                rs.getString("summary"),
                rs.getString("operator"),
                rs.getTimestamp("created_at").toLocalDateTime().toString()
        ), params.toArray());
    }

    public void ensureTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_audit_log (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    module_name VARCHAR(64) NOT NULL,
                    action_name VARCHAR(64) NOT NULL,
                    target_type VARCHAR(64) NOT NULL,
                    target_code VARCHAR(128) NOT NULL,
                    summary VARCHAR(1024) NULL,
                    operator VARCHAR(64) NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    KEY idx_sys_audit_log_created_at (created_at),
                    KEY idx_sys_audit_log_target (target_type, target_code)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS salary_field_config_audit (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    item_code VARCHAR(64) NOT NULL,
                    field_name VARCHAR(64) NOT NULL,
                    old_value VARCHAR(512) NULL,
                    new_value VARCHAR(512) NULL,
                    changed_by VARCHAR(64) NULL,
                    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    KEY idx_salary_field_config_audit_item (item_code),
                    KEY idx_salary_field_config_audit_changed_at (changed_at)
                )
                """);
    }

    private String operator() {
        String username = currentUserService.currentUsername();
        return username == null || username.isBlank() ? "system" : username;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String auditWhere(
            String module,
            String operator,
            String targetCode,
            String start,
            String end,
            List<Object> params
    ) {
        List<String> clauses = new ArrayList<>();
        String moduleValue = trim(module);
        if (!moduleValue.isBlank()) {
            clauses.add("module_name = ?");
            params.add(moduleValue);
        }
        String operatorValue = trim(operator);
        if (!operatorValue.isBlank()) {
            clauses.add("operator LIKE CONCAT('%', ?, '%')");
            params.add(operatorValue);
        }
        String targetValue = trim(targetCode);
        if (!targetValue.isBlank()) {
            clauses.add("(target_code LIKE CONCAT('%', ?, '%') OR target_type LIKE CONCAT('%', ?, '%'))");
            params.add(targetValue);
            params.add(targetValue);
        }
        String startValue = normalizeDateTime(start);
        if (!startValue.isBlank()) {
            clauses.add("created_at >= ?");
            params.add(startValue);
        }
        String endValue = normalizeDateTime(end);
        if (!endValue.isBlank()) {
            clauses.add("created_at <= ?");
            params.add(endValue);
        }
        return clauses.isEmpty() ? "" : "WHERE " + String.join(" AND ", clauses);
    }

    private String normalizeDateTime(String value) {
        String text = trim(value);
        if (text.isBlank()) {
            return "";
        }
        return text.replace('T', ' ');
    }
}
