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
);
