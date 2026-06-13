package com.dx.rsgzgl.system.service;

import com.dx.rsgzgl.system.dto.MenuAdminItemResponse;
import com.dx.rsgzgl.system.dto.RolePermissionResponse;
import com.dx.rsgzgl.system.dto.RoleTemplateResponse;
import com.dx.rsgzgl.system.dto.UserRoleResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class SystemAdminQueryService {

    private final JdbcTemplate jdbcTemplate;
    private final AuthSessionService authSessionService;
    private final SystemAuditService systemAuditService;

    public SystemAdminQueryService(
            JdbcTemplate jdbcTemplate,
            AuthSessionService authSessionService,
            SystemAuditService systemAuditService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.authSessionService = authSessionService;
        this.systemAuditService = systemAuditService;
    }

    public List<MenuAdminItemResponse> menus() {
        return jdbcTemplate.query("""
                SELECT code,
                       COALESCE(parent_code, '') AS parent_code,
                       title,
                       COALESCE(icon, '') AS icon,
                       COALESCE(view_name, '') AS view_name,
                       sequence_no,
                       status
                FROM sys_menu
                ORDER BY COALESCE(parent_code, ''), sequence_no, code
                """, (rs, rowNum) -> new MenuAdminItemResponse(
                rs.getString("code"),
                rs.getString("parent_code"),
                rs.getString("title"),
                rs.getString("icon"),
                rs.getString("view_name"),
                rs.getInt("sequence_no"),
                rs.getString("status")
        ));
    }

    public List<RolePermissionResponse> roles() {
        return jdbcTemplate.query("""
                SELECT r.code,
                       r.name,
                       r.status,
                       COALESCE(GROUP_CONCAT(rm.menu_code ORDER BY rm.menu_code SEPARATOR ','), '') AS menu_codes
                FROM sys_role r
                LEFT JOIN sys_role_menu rm ON rm.role_code = r.code
                GROUP BY r.code, r.name, r.status
                ORDER BY r.code
                """, (rs, rowNum) -> new RolePermissionResponse(
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("status"),
                splitCodes(rs.getString("menu_codes"))
        ));
    }

    public List<RoleTemplateResponse> roleTemplates() {
        return List.of(
                new RoleTemplateResponse("SALARY_OPERATOR", "\u5de5\u8d44\u7ecf\u529e", "\u529e\u7406\u5de5\u8d44\u5f85\u529e\u3001\u8bd5\u7b97\u548c\u5bf9\u8d26\uff0c\u4e0d\u542b\u5bfc\u51fa\u548c\u7cfb\u7edf\u7ba1\u7406\u3002", List.of(
                        "WORKBENCH", "SALARY_PERSON", "SALARY_TODO", "SALARY_DONE", "SALARY_TRIAL", "SALARY_RECONCILE"
                )),
                new RoleTemplateResponse("SALARY_REVIEWER", "\u5de5\u8d44\u5ba1\u6838", "\u67e5\u770b\u5f85\u529e\u3001\u5df2\u529e\u3001\u5bf9\u8d26\u548c\u5bfc\u51fa\u7ed3\u679c\u3002", List.of(
                        "WORKBENCH", "SALARY_PERSON", "SALARY_TODO", "SALARY_DONE", "SALARY_RECONCILE", "SALARY_EXPORT"
                )),
                new RoleTemplateResponse("SALARY_VIEWER", "\u5de5\u8d44\u67e5\u8be2", "\u4ec5\u67e5\u770b\u4eba\u5458\u5de5\u8d44\u548c\u5df2\u529e\u4e1a\u52a1\u3002", List.of(
                        "WORKBENCH", "SALARY_PERSON", "SALARY_DONE"
                )),
                new RoleTemplateResponse("APPLICATION_OPERATOR", "\u7533\u529e\u7ecf\u529e", "\u5904\u7406\u7533\u529e\u5f85\u529e\u548c\u5df2\u529e\u4e1a\u52a1\u3002", List.of(
                        "WORKBENCH", "APPLICATION_TODO", "APPLICATION_DONE"
                )),
                new RoleTemplateResponse("SYSTEM_AUDITOR", "\u7cfb\u7edf\u5ba1\u8ba1", "\u67e5\u770b\u5de5\u4f5c\u53f0\u548c\u64cd\u4f5c\u5ba1\u8ba1\u3002", List.of(
                        "WORKBENCH", "SYSTEM_AUDIT"
                )),
                new RoleTemplateResponse("ADMIN", "\u7cfb\u7edf\u7ba1\u7406\u5458", "\u62e5\u6709\u5168\u90e8\u83dc\u5355\u6743\u9650\u548c\u5168\u90e8\u5355\u4f4d\u6570\u636e\u6743\u9650\u3002", activeMenuCodes())
        );
    }

    public List<UserRoleResponse> users() {
        return jdbcTemplate.query("""
                SELECT u.username,
                       u.display_name,
                       u.status,
                       COALESCE(GROUP_CONCAT(DISTINCT ur.role_code ORDER BY ur.role_code SEPARATOR ','), '') AS role_codes,
                       COALESCE(GROUP_CONCAT(DISTINCT uo.org_code ORDER BY uo.org_code SEPARATOR ','), '') AS org_codes
                FROM sys_user u
                LEFT JOIN sys_user_role ur ON ur.username = u.username
                LEFT JOIN sys_user_org uo ON uo.username = u.username
                GROUP BY u.username, u.display_name, u.status
                ORDER BY u.username
                """, (rs, rowNum) -> new UserRoleResponse(
                rs.getString("username"),
                rs.getString("display_name"),
                rs.getString("status"),
                splitCodes(rs.getString("role_codes")),
                splitCodes(rs.getString("org_codes"))
        ));
    }

    public void updateMenuStatus(String code, String status) {
        String normalizedStatus = normalizeStatus(status);
        String oldStatus = scalar("SELECT status FROM sys_menu WHERE code = ?", code);
        int updated = jdbcTemplate.update("""
                UPDATE sys_menu
                SET status = ?
                WHERE code = ?
                """, normalizedStatus, code);
        if (updated == 0) {
            throw new IllegalArgumentException("Menu not found: " + code);
        }
        systemAuditService.record("system", "menu-status", "MENU", code, oldStatus + " -> " + normalizedStatus);
    }

    @Transactional
    public void createRole(String code, String name, List<String> menuCodes) {
        String normalizedCode = normalizeCode(code, "role code");
        String normalizedName = normalizeRequiredText(name, "role name");
        jdbcTemplate.update("""
                INSERT INTO sys_role(code, name, status)
                VALUES (?, ?, 'ACTIVE')
                """, normalizedCode, normalizedName);
        systemAuditService.record("system", "role-create", "ROLE", normalizedCode, normalizedName);
        updateRoleMenus(normalizedCode, menuCodes);
    }

    public void updateRoleStatus(String code, String status) {
        String normalizedStatus = normalizeStatus(status);
        String oldStatus = scalar("SELECT status FROM sys_role WHERE code = ?", code);
        int updated = jdbcTemplate.update("""
                UPDATE sys_role
                SET status = ?
                WHERE code = ?
                """, normalizedStatus, code);
        if (updated == 0) {
            throw new IllegalArgumentException("Role not found: " + code);
        }
        systemAuditService.record("system", "role-status", "ROLE", code, oldStatus + " -> " + normalizedStatus);
    }

    public void applyRoleTemplate(String roleCode, String templateCode) {
        RoleTemplateResponse template = roleTemplates().stream()
                .filter(item -> item.code().equals(templateCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Role template not found: " + templateCode));
        updateRoleMenus(roleCode, template.menuCodes());
        systemAuditService.record("system", "role-template", "ROLE", roleCode, template.code());
    }

    @Transactional
    public void updateRoleMenus(String roleCode, List<String> menuCodes) {
        if (!roleExists(roleCode)) {
            throw new IllegalArgumentException("Role not found: " + roleCode);
        }
        List<String> oldCodes = roleMenuCodes(roleCode);
        List<String> normalizedCodes = normalizeMenuCodes(menuCodes);
        if (!normalizedCodes.isEmpty()) {
            Integer matched = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM sys_menu
                    WHERE code IN (%s)
                    """.formatted(placeholders(normalizedCodes.size())), Integer.class, normalizedCodes.toArray());
            if (matched == null || matched != normalizedCodes.size()) {
                throw new IllegalArgumentException("Role menus contain invalid menu code");
            }
        }
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_code = ?", roleCode);
        for (String menuCode : normalizedCodes) {
            jdbcTemplate.update("""
                    INSERT INTO sys_role_menu(role_code, menu_code)
                    VALUES (?, ?)
                    """, roleCode, menuCode);
        }
        systemAuditService.record("system", "role-menus", "ROLE", roleCode,
                String.join(",", oldCodes) + " -> " + String.join(",", normalizedCodes));
    }

    @Transactional
    public void updateUserRoles(String username, List<String> roleCodes) {
        if (!userExists(username)) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        List<String> oldCodes = userRoleCodes(username);
        List<String> normalizedCodes = normalizeCodes(roleCodes);
        if (!normalizedCodes.isEmpty()) {
            Integer matched = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM sys_role
                    WHERE code IN (%s)
                    """.formatted(placeholders(normalizedCodes.size())), Integer.class, normalizedCodes.toArray());
            if (matched == null || matched != normalizedCodes.size()) {
                throw new IllegalArgumentException("User roles contain invalid role code");
            }
        }
        jdbcTemplate.update("DELETE FROM sys_user_role WHERE username = ?", username);
        for (String roleCode : normalizedCodes) {
            jdbcTemplate.update("""
                    INSERT INTO sys_user_role(username, role_code)
                    VALUES (?, ?)
                    """, username, roleCode);
        }
        systemAuditService.record("system", "user-roles", "USER", username,
                String.join(",", oldCodes) + " -> " + String.join(",", normalizedCodes));
    }

    @Transactional
    public void createUser(String username, String displayName, List<String> roleCodes, List<String> orgCodes) {
        String normalizedUsername = normalizeCode(username, "username");
        String normalizedDisplayName = normalizeRequiredText(displayName, "display name");
        jdbcTemplate.update("""
                INSERT INTO sys_user(username, display_name, password_hash, status)
                VALUES (?, ?, '{noop}123456', 'ACTIVE')
                """, normalizedUsername, normalizedDisplayName);
        systemAuditService.record("system", "user-create", "USER", normalizedUsername, normalizedDisplayName);
        updateUserRoles(normalizedUsername, roleCodes);
        updateUserOrgCodes(normalizedUsername, orgCodes);
    }

    public void updateUserStatus(String username, String status) {
        String normalizedStatus = normalizeStatus(status);
        String oldStatus = scalar("SELECT status FROM sys_user WHERE username = ?", username);
        int updated = jdbcTemplate.update("""
                UPDATE sys_user
                SET status = ?
                WHERE username = ?
                """, normalizedStatus, username);
        if (updated == 0) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        systemAuditService.record("system", "user-status", "USER", username, oldStatus + " -> " + normalizedStatus);
    }

    @Transactional
    public void updateUserOrgCodes(String username, List<String> orgCodes) {
        if (!userExists(username)) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        List<String> oldCodes = userOrgCodes(username);
        List<String> normalizedCodes = normalizeOrgCodes(orgCodes);
        if (!normalizedCodes.isEmpty()) {
            Integer matched = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM dwbm
                    WHERE TRIM(dwbm) IN (%s)
                    """.formatted(placeholders(normalizedCodes.size())), Integer.class, normalizedCodes.toArray());
            if (matched == null || matched != normalizedCodes.size()) {
                throw new IllegalArgumentException("User organizations contain invalid organization code");
            }
        }
        jdbcTemplate.update("DELETE FROM sys_user_org WHERE username = ?", username);
        for (String orgCode : normalizedCodes) {
            jdbcTemplate.update("""
                    INSERT INTO sys_user_org(username, org_code)
                    VALUES (?, ?)
                    """, username, orgCode);
        }
        systemAuditService.record("system", "user-orgs", "USER", username,
                String.join(",", oldCodes) + " -> " + String.join(",", normalizedCodes));
    }

    public void resetUserPassword(String username, String newPassword) {
        if (!userExists(username)) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        String password = newPassword == null || newPassword.isBlank() ? "123456" : newPassword;
        jdbcTemplate.update("""
                UPDATE sys_user
                SET password_hash = ?
                WHERE username = ?
                """, authSessionService.encodePassword(password), username);
        systemAuditService.record("system", "password-reset", "USER", username, "password reset");
    }

    private List<String> splitCodes(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .toList();
    }

    private boolean roleExists(String roleCode) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_role WHERE code = ?",
                Integer.class,
                roleCode
        );
        return count != null && count > 0;
    }

    private List<String> roleMenuCodes(String roleCode) {
        return jdbcTemplate.queryForList("""
                SELECT menu_code
                FROM sys_role_menu
                WHERE role_code = ?
                ORDER BY menu_code
                """, String.class, roleCode);
    }

    private List<String> activeMenuCodes() {
        return jdbcTemplate.queryForList("""
                SELECT code
                FROM sys_menu
                WHERE status = 'ACTIVE'
                ORDER BY code
                """, String.class);
    }

    private boolean userExists(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE username = ?",
                Integer.class,
                username
        );
        return count != null && count > 0;
    }

    private List<String> userRoleCodes(String username) {
        return jdbcTemplate.queryForList("""
                SELECT role_code
                FROM sys_user_role
                WHERE username = ?
                ORDER BY role_code
                """, String.class, username);
    }

    private List<String> userOrgCodes(String username) {
        return jdbcTemplate.queryForList("""
                SELECT org_code
                FROM sys_user_org
                WHERE username = ?
                ORDER BY org_code
                """, String.class, username);
    }

    private String scalar(String sql, String value) {
        List<String> rows = jdbcTemplate.queryForList(sql, String.class, value);
        return rows.isEmpty() ? "" : rows.getFirst();
    }

    private String normalizeStatus(String status) {
        String value = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!value.equals("ACTIVE") && !value.equals("DISABLED")) {
            throw new IllegalArgumentException("Status must be ACTIVE or DISABLED");
        }
        return value;
    }

    private List<String> normalizeMenuCodes(List<String> menuCodes) {
        return normalizeCodes(menuCodes);
    }

    private List<String> normalizeOrgCodes(List<String> orgCodes) {
        if (orgCodes == null) {
            return List.of();
        }
        Set<String> seen = new HashSet<>();
        List<String> normalized = orgCodes.stream()
                .map(code -> code == null ? "" : code.trim())
                .filter(code -> !code.isEmpty())
                .filter(code -> code.matches("[A-Za-z0-9_\\-]{1,64}"))
                .filter(seen::add)
                .sorted()
                .toList();
        return normalized.stream()
                .filter(code -> normalized.stream()
                        .noneMatch(parent -> !parent.equals(code) && code.startsWith(parent)))
                .toList();
    }

    private String placeholders(int count) {
        return String.join(",", java.util.Collections.nCopies(count, "?"));
    }

    private List<String> normalizeCodes(List<String> codes) {
        if (codes == null) {
            return List.of();
        }
        Set<String> seen = new HashSet<>();
        return codes.stream()
                .map(code -> code == null ? "" : code.trim())
                .filter(code -> !code.isEmpty())
                .filter(seen::add)
                .sorted()
                .toList();
    }

    private String normalizeCode(String code, String label) {
        String value = code == null ? "" : code.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
        if (!value.matches("[A-Za-z0-9_\\-]{2,64}")) {
            throw new IllegalArgumentException(label + " must contain 2-64 letters, numbers, underscores, or hyphens");
        }
        return value;
    }

    private String normalizeRequiredText(String text, String label) {
        String value = text == null ? "" : text.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
        if (value.length() > 128) {
            throw new IllegalArgumentException(label + " must be 128 characters or fewer");
        }
        return value;
    }
}
