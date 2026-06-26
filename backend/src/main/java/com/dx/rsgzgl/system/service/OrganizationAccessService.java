package com.dx.rsgzgl.system.service;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrganizationAccessService {

    private final JdbcTemplate jdbcTemplate;
    private final CurrentUserService currentUserService;

    public OrganizationAccessService(JdbcTemplate jdbcTemplate, CurrentUserService currentUserService) {
        this.jdbcTemplate = jdbcTemplate;
        this.currentUserService = currentUserService;
    }

    public boolean hasFullAccess() {
        String username = currentUserService.currentUsername();
        if (!StringUtils.hasText(username)) {
            return true;
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM sys_user u
                JOIN sys_user_role ur ON ur.username = u.username
                JOIN sys_role r ON r.code = ur.role_code
                WHERE u.username = ?
                  AND u.status = 'ACTIVE'
                  AND r.status = 'ACTIVE'
                  AND r.code = 'ADMIN'
                """, Integer.class, username);
        return count != null && count > 0;
    }

    public List<String> allowedOrgCodes() {
        if (hasFullAccess()) {
            return List.of();
        }
        String username = currentUserService.currentUsername();
        if (!StringUtils.hasText(username)) {
            return List.of();
        }
        return jdbcTemplate.queryForList("""
                SELECT org_code
                FROM sys_user_org
                WHERE username = ?
                ORDER BY org_code
                """, String.class, username).stream()
                .map(code -> code == null ? "" : code.trim())
                .filter(code -> !code.isEmpty())
                .toList();
    }

    public boolean canAccessOrg(String orgCode) {
        if (hasFullAccess()) {
            return true;
        }
        String normalized = normalizeOrgCode(orgCode);
        if (!StringUtils.hasText(normalized)) {
            return false;
        }
        return allowedOrgCodes().stream().anyMatch(normalized::startsWith);
    }

    public void requireOrgAccess(String orgCode) {
        if (!canAccessOrg(orgCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No organization access: " + orgCode);
        }
    }

    public void requirePersonAccess(String personCode, String fallbackOrgCode) {
        String orgCode = orgCodeFromPersonCode(personCode, fallbackOrgCode);
        requireOrgAccess(orgCode);
    }

    public void requireHistoryAccess(String historyId) {
        List<String> orgCodes = jdbcTemplate.queryForList("""
                SELECT TRIM(dwbm)
                FROM hisbase
                WHERE id = ?
                   OR id = LPAD(?, 36, ' ')
                LIMIT 1
                """, String.class, historyId, historyId);
        if (orgCodes.isEmpty()) {
            return;
        }
        requireOrgAccess(orgCodes.getFirst());
    }

    public String orgAccessSql(String alias) {
        if (hasFullAccess()) {
            return "1 = 1";
        }
        List<String> orgCodes = allowedOrgCodes();
        if (orgCodes.isEmpty()) {
            return "1 = 0";
        }
        return "(" + String.join(" OR ", orgCodes.stream()
                .map(code -> alias + ".dwbm LIKE '" + sqlLiteral(code) + "%'")
                .toList()) + ")";
    }

    public String orgCodeAccessSql(String expression) {
        if (hasFullAccess()) {
            return "1 = 1";
        }
        List<String> orgCodes = allowedOrgCodes();
        if (orgCodes.isEmpty()) {
            return "1 = 0";
        }
        return "(" + String.join(" OR ", orgCodes.stream()
                .map(code -> expression + " LIKE '" + sqlLiteral(code) + "%'")
                .toList()) + ")";
    }

    public String orgCodeFromPersonCode(String personCode, String fallbackOrgCode) {
        String normalized = personCode == null ? "" : personCode.trim();
        int separator = normalized.indexOf('-');
        if (separator > 0) {
            return normalized.substring(0, separator);
        }
        return normalizeOrgCode(fallbackOrgCode);
    }

    private String normalizeOrgCode(String orgCode) {
        return orgCode == null ? "" : orgCode.trim();
    }

    private String sqlLiteral(String value) {
        return value.replace("'", "''");
    }
}
