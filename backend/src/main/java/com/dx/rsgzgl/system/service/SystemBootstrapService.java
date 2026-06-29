package com.dx.rsgzgl.system.service;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemBootstrapService {

    private final JdbcTemplate jdbcTemplate;
    private final SystemAuditService systemAuditService;

    public SystemBootstrapService(JdbcTemplate jdbcTemplate, SystemAuditService systemAuditService) {
        this.jdbcTemplate = jdbcTemplate;
        this.systemAuditService = systemAuditService;
    }

    @PostConstruct
    public void initialize() {
        createTables();
        systemAuditService.ensureTable();
        seedMenus();
        seedRoles();
    }

    private void createTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_user (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(64) NOT NULL,
                    display_name VARCHAR(128) NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_sys_user_username (username)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_menu (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    code VARCHAR(64) NOT NULL,
                    parent_code VARCHAR(64) NULL,
                    title VARCHAR(128) NOT NULL,
                    icon VARCHAR(64) NULL,
                    view_name VARCHAR(64) NULL,
                    sequence_no INT NOT NULL DEFAULT 0,
                    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_sys_menu_code (code),
                    KEY idx_sys_menu_parent (parent_code),
                    KEY idx_sys_menu_status_seq (status, sequence_no)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_role (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    code VARCHAR(64) NOT NULL,
                    name VARCHAR(128) NOT NULL,
                    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_sys_role_code (code)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_role_menu (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    role_code VARCHAR(64) NOT NULL,
                    menu_code VARCHAR(64) NOT NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_sys_role_menu (role_code, menu_code),
                    KEY idx_sys_role_menu_menu (menu_code)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_user_role (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(64) NOT NULL,
                    role_code VARCHAR(64) NOT NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_sys_user_role (username, role_code),
                    KEY idx_sys_user_role_role (role_code)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_user_org (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(64) NOT NULL,
                    org_code VARCHAR(64) NOT NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_sys_user_org (username, org_code),
                    KEY idx_sys_user_org_org (org_code)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS salary_business_case (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    case_no VARCHAR(64) NOT NULL,
                    work_item_id VARCHAR(255) NOT NULL,
                    source VARCHAR(64) NOT NULL,
                    status VARCHAR(32) NOT NULL DEFAULT 'DONE',
                    business_type VARCHAR(128) NOT NULL,
                    person_code VARCHAR(128) NOT NULL,
                    person_name VARCHAR(128) NULL,
                    org_code VARCHAR(64) NOT NULL,
                    event_year INT NULL,
                    event_month INT NULL,
                    title VARCHAR(255) NULL,
                    summary VARCHAR(1024) NULL,
                    trial_status VARCHAR(32) NULL,
                    trial_matched TINYINT NULL,
                    trial_difference DECIMAL(18,2) NULL,
                    trial_summary VARCHAR(2048) NULL,
                    trial_baseline_total DECIMAL(18,2) NULL,
                    trial_calculated_total DECIMAL(18,2) NULL,
                    trial_expected_total DECIMAL(18,2) NULL,
                    trial_changes_json LONGTEXT NULL,
                    force_reason VARCHAR(1024) NULL,
                    difference_reason VARCHAR(1024) NULL,
                    cancel_reason VARCHAR(1024) NULL,
                    review_status VARCHAR(32) NULL,
                    review_reason VARCHAR(1024) NULL,
                    reviewed_by VARCHAR(64) NULL,
                    reviewed_at DATETIME NULL,
                    handled_by VARCHAR(64) NULL,
                    handled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_salary_business_case_work_item (work_item_id),
                    KEY idx_salary_business_case_status_time (status, handled_at),
                    KEY idx_salary_business_case_person (person_code),
                    KEY idx_salary_business_case_org (org_code)
                )
                """);
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

    private void seedMenus() {
        List<MenuSeed> menus = List.of(
                new MenuSeed("WORKBENCH", null, "\u5de5\u4f5c\u53f0", "desktop", "workbench", 10),
                new MenuSeed("SALARY", null, "\u5de5\u8d44\u4e1a\u52a1", "salary", "", 20),
                new MenuSeed("SALARY_PERSON", "SALARY", "\u4eba\u5458\u5de5\u8d44", "people", "salary", 10),
                new MenuSeed("SALARY_TODO", "SALARY", "\u5f85\u529e\u5de5\u8d44\u53d8\u52a8", "todo", "workbench", 20),
                new MenuSeed("SALARY_DONE", "SALARY", "\u5df2\u529e\u5de5\u8d44\u53d8\u52a8", "done", "workbench", 30),
                new MenuSeed("SALARY_TRIAL", "SALARY", "\u5de5\u8d44\u8bd5\u7b97", "trial", "salary", 40),
                new MenuSeed("SALARY_RECONCILE", "SALARY", "\u5de5\u8d44\u5bf9\u8d26", "reconcile", "salary", 50),
                new MenuSeed("SALARY_EXPORT", "SALARY", "\u5de5\u8d44\u5bfc\u51fa", "export", "salary", 60),
                new MenuSeed("SALARY_REPORT", "SALARY", "\u62a5\u8868\u6253\u5370", "print", "workbench", 70),
                new MenuSeed("SALARY_HISTORY_WRITE", "SALARY", "\u5386\u53f2\u5199\u5165", "write", "workbench", 80),
                new MenuSeed("SALARY_HISTORY_ROLLBACK", "SALARY", "\u5386\u53f2\u5199\u5165\u64a4\u9500", "rollback", "workbench", 90),
                new MenuSeed("MIGRATION", null, "\u8fc1\u79fb\u6838\u9a8c", "migration", "", 25),
                new MenuSeed("SALARY_GOVERNANCE", "MIGRATION", "\u6570\u636e\u6cbb\u7406", "governance", "workbench", 10),
                new MenuSeed("SALARY_ACCEPTANCE", "MIGRATION", "\u8fc1\u79fb\u9a8c\u6536", "acceptance", "workbench", 20),
                new MenuSeed("SALARY_DELIVERY_ARCHIVE", "MIGRATION", "\u4ea4\u4ed8\u5f52\u6863", "archive", "workbench", 30),
                new MenuSeed("APPLICATION", null, "\u7533\u529e\u4e1a\u52a1", "application", "", 30),
                new MenuSeed("APPLICATION_TODO", "APPLICATION", "\u7533\u529e\u5f85\u529e", "todo", "workbench", 10),
                new MenuSeed("APPLICATION_DONE", "APPLICATION", "\u7533\u529e\u5df2\u529e", "done", "workbench", 20),
                new MenuSeed("SYSTEM", null, "\u7cfb\u7edf\u7ba1\u7406", "system", "", 90),
                new MenuSeed("SYSTEM_MENU", "SYSTEM", "\u83dc\u5355\u7ba1\u7406", "menu", "system", 10),
                new MenuSeed("SYSTEM_ROLE", "SYSTEM", "\u89d2\u8272\u6743\u9650", "role", "system", 20),
                new MenuSeed("SYSTEM_USER", "SYSTEM", "\u7528\u6237\u7ba1\u7406", "user", "system", 30),
                new MenuSeed("SYSTEM_AUDIT", "SYSTEM", "\u64cd\u4f5c\u5ba1\u8ba1", "audit", "system", 40),
                new MenuSeed("SALARY_CONFIG", "SYSTEM", "\u5de5\u8d44\u9879\u76ee\u914d\u7f6e", "config", "system", 50)
        );
        for (MenuSeed menu : menus) {
            jdbcTemplate.update("""
                    INSERT INTO sys_menu(code, parent_code, title, icon, view_name, sequence_no, status)
                    VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')
                    ON DUPLICATE KEY UPDATE
                        parent_code = VALUES(parent_code),
                        title = VALUES(title),
                        icon = VALUES(icon),
                        view_name = VALUES(view_name),
                        sequence_no = VALUES(sequence_no)
                    """, menu.code(), menu.parentCode(), menu.title(), menu.icon(), menu.viewName(), menu.sequence());
        }
    }

    private void seedRoles() {
        jdbcTemplate.update("""
                INSERT INTO sys_user(username, display_name, password_hash, status)
                VALUES ('admin', 'System User', '{noop}admin', 'ACTIVE')
                ON DUPLICATE KEY UPDATE display_name = VALUES(display_name), status = 'ACTIVE'
                """);
        jdbcTemplate.update("""
                INSERT INTO sys_role(code, name, status)
                VALUES ('ADMIN', '\u7cfb\u7edf\u7ba1\u7406\u5458', 'ACTIVE')
                ON DUPLICATE KEY UPDATE name = VALUES(name), status = 'ACTIVE'
                """);
        for (RoleSeed role : roleSeeds()) {
            jdbcTemplate.update("""
                    INSERT INTO sys_role(code, name, status)
                    VALUES (?, ?, 'ACTIVE')
                    ON DUPLICATE KEY UPDATE name = VALUES(name)
                    """, role.code(), role.name());
            for (String menuCode : role.menuCodes()) {
                jdbcTemplate.update("""
                        INSERT IGNORE INTO sys_role_menu(role_code, menu_code)
                        VALUES (?, ?)
                        """, role.code(), menuCode);
            }
        }
        jdbcTemplate.update("""
                INSERT IGNORE INTO sys_user_role(username, role_code)
                VALUES ('admin', 'ADMIN')
                """);
        jdbcTemplate.update("""
                INSERT IGNORE INTO sys_role_menu(role_code, menu_code)
                SELECT 'ADMIN', code
                FROM sys_menu
                WHERE status = 'ACTIVE'
                """);
    }

    private List<RoleSeed> roleSeeds() {
        return List.of(
                new RoleSeed("SALARY_OPERATOR", "\u5de5\u8d44\u7ecf\u529e", List.of(
                        "WORKBENCH", "SALARY_PERSON", "SALARY_TODO", "SALARY_DONE", "SALARY_TRIAL", "SALARY_RECONCILE", "SALARY_REPORT", "MIGRATION", "SALARY_GOVERNANCE"
                )),
                new RoleSeed("SALARY_REVIEWER", "\u5de5\u8d44\u5ba1\u6838", List.of(
                        "WORKBENCH", "SALARY_PERSON", "SALARY_TODO", "SALARY_DONE", "SALARY_RECONCILE", "SALARY_EXPORT", "SALARY_REPORT", "MIGRATION", "SALARY_GOVERNANCE", "SALARY_ACCEPTANCE", "SALARY_DELIVERY_ARCHIVE"
                )),
                new RoleSeed("SALARY_WRITER", "\u5386\u53f2\u5199\u5165\u6267\u884c", List.of(
                        "WORKBENCH", "SALARY_PERSON", "SALARY_DONE", "SALARY_HISTORY_WRITE", "SALARY_HISTORY_ROLLBACK", "SALARY_EXPORT", "SALARY_REPORT", "MIGRATION", "SALARY_GOVERNANCE", "SALARY_ACCEPTANCE", "SALARY_DELIVERY_ARCHIVE"
                )),
                new RoleSeed("DATA_STEWARD", "\u6570\u636e\u6cbb\u7406\u5458", List.of(
                        "WORKBENCH", "SALARY_PERSON", "SALARY_TODO", "SALARY_DONE", "SALARY_REPORT", "MIGRATION", "SALARY_GOVERNANCE", "SALARY_ACCEPTANCE"
                )),
                new RoleSeed("RULE_STEWARD", "\u89c4\u5219\u6807\u51c6\u7ef4\u62a4", List.of(
                        "WORKBENCH", "SALARY_PERSON", "SALARY_TRIAL", "SALARY_RECONCILE", "SALARY_CONFIG", "SALARY_REPORT", "MIGRATION", "SALARY_GOVERNANCE", "SALARY_ACCEPTANCE"
                )),
                new RoleSeed("SALARY_VIEWER", "\u5de5\u8d44\u67e5\u8be2", List.of(
                        "WORKBENCH", "SALARY_PERSON", "SALARY_DONE", "SALARY_REPORT"
                )),
                new RoleSeed("APPLICATION_OPERATOR", "\u7533\u529e\u7ecf\u529e", List.of(
                        "WORKBENCH", "APPLICATION_TODO", "APPLICATION_DONE"
                )),
                new RoleSeed("SYSTEM_AUDITOR", "\u7cfb\u7edf\u5ba1\u8ba1", List.of(
                        "WORKBENCH", "SYSTEM_AUDIT"
                ))
        );
    }

    private record MenuSeed(String code, String parentCode, String title, String icon, String viewName, int sequence) {
    }

    private record RoleSeed(String code, String name, List<String> menuCodes) {
    }
}
