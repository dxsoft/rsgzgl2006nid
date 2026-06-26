CREATE TABLE IF NOT EXISTS sys_user_org (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    org_code VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_user_org (username, org_code),
    KEY idx_sys_user_org_org (org_code)
);

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
);

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

CREATE TABLE IF NOT EXISTS sys_user_work_state (
    username VARCHAR(64) NOT NULL,
    state_key VARCHAR(64) NOT NULL,
    state_json LONGTEXT NOT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (username, state_key),
    KEY idx_sys_user_work_state_updated (updated_at)
);

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
);

CREATE TABLE IF NOT EXISTS salary_business_case_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    case_no VARCHAR(64) NOT NULL,
    work_item_id VARCHAR(255) NOT NULL,
    person_code VARCHAR(128) NOT NULL,
    org_code VARCHAR(64) NOT NULL,
    event_year INT NULL,
    event_month INT NULL,
    business_type VARCHAR(128) NOT NULL,
    trial_status VARCHAR(32) NULL,
    trial_matched TINYINT NULL,
    trial_difference DECIMAL(18,2) NULL,
    trial_baseline_total DECIMAL(18,2) NULL,
    trial_calculated_total DECIMAL(18,2) NULL,
    trial_expected_total DECIMAL(18,2) NULL,
    trial_changes_json LONGTEXT NULL,
    salary_items_json LONGTEXT NULL,
    snapshot_json LONGTEXT NOT NULL,
    snapshot_by VARCHAR(64) NULL,
    snapshot_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_salary_case_snapshot_work_item (work_item_id),
    KEY idx_salary_case_snapshot_case (case_no),
    KEY idx_salary_case_snapshot_person (person_code),
    KEY idx_salary_case_snapshot_org_period (org_code, event_year, event_month)
);

CREATE TABLE IF NOT EXISTS salary_history_write_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_no VARCHAR(128) NOT NULL,
    case_no VARCHAR(64) NOT NULL,
    work_item_id VARCHAR(255) NOT NULL,
    person_code VARCHAR(128) NOT NULL,
    org_code VARCHAR(64) NOT NULL,
    event_year INT NULL,
    event_month INT NULL,
    business_type VARCHAR(128) NOT NULL,
    preview_status VARCHAR(32) NOT NULL,
    writable TINYINT NOT NULL DEFAULT 0,
    existing_history_id VARCHAR(128) NULL,
    sid_plan VARCHAR(1024) NULL,
    fields_json LONGTEXT NULL,
    issues_json LONGTEXT NULL,
    preview_json LONGTEXT NOT NULL,
    plan_status VARCHAR(32) NOT NULL DEFAULT 'PREPARED',
    prepared_by VARCHAR(64) NULL,
    prepared_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    executed_by VARCHAR(64) NULL,
    executed_at DATETIME NULL,
    execution_result VARCHAR(32) NULL,
    execution_message VARCHAR(1024) NULL,
    inserted_history_id VARCHAR(128) NULL,
    previous_history_id VARCHAR(128) NULL,
    next_history_id VARCHAR(128) NULL,
    rolled_back_by VARCHAR(64) NULL,
    rolled_back_at DATETIME NULL,
    rollback_message VARCHAR(1024) NULL,
    comparison_review_status VARCHAR(32) NULL,
    comparison_review_category VARCHAR(64) NULL,
    comparison_review_reason VARCHAR(1024) NULL,
    comparison_reviewed_by VARCHAR(64) NULL,
    comparison_reviewed_at DATETIME NULL,
    UNIQUE KEY uk_salary_history_write_plan_no (plan_no),
    UNIQUE KEY uk_salary_history_write_plan_case (case_no),
    KEY idx_salary_history_write_plan_person (person_code),
    KEY idx_salary_history_write_plan_status (plan_status, preview_status, writable)
);

CREATE TABLE IF NOT EXISTS salary_todo_candidate_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_item_id VARCHAR(255) NOT NULL,
    source VARCHAR(64) NOT NULL,
    source_id VARCHAR(128) NULL,
    person_code VARCHAR(128) NOT NULL,
    org_code VARCHAR(64) NOT NULL,
    person_no VARCHAR(64) NULL,
    person_name VARCHAR(128) NULL,
    event_year INT NOT NULL,
    event_month INT NOT NULL,
    change_type VARCHAR(128) NOT NULL,
    note VARCHAR(1024) NULL,
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_salary_todo_cache_work_item (work_item_id),
    KEY idx_salary_todo_cache_org_period (org_code, event_year, event_month),
    KEY idx_salary_todo_cache_change (change_type),
    KEY idx_salary_todo_cache_person (person_code)
);

CREATE TABLE IF NOT EXISTS salary_todo_cache_meta (
    cache_key VARCHAR(64) PRIMARY KEY,
    last_refreshed_at DATETIME NOT NULL,
    total_count BIGINT NOT NULL DEFAULT 0,
    cache_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    dirty_at DATETIME NULL
);

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
);

CREATE TABLE IF NOT EXISTS application_case (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    case_no VARCHAR(64) NOT NULL,
    source VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    business_type VARCHAR(128) NOT NULL,
    person_code VARCHAR(128) NOT NULL,
    person_name VARCHAR(128) NULL,
    org_code VARCHAR(64) NOT NULL,
    event_year INT NULL,
    event_month INT NULL,
    title VARCHAR(255) NOT NULL,
    summary VARCHAR(1024) NULL,
    review_reason VARCHAR(1024) NULL,
    workflow_status VARCHAR(32) NOT NULL DEFAULT 'APPLICATION_TODO',
    created_by VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    handled_by VARCHAR(64) NULL,
    handled_at DATETIME NULL,
    UNIQUE KEY uk_application_case_no (case_no),
    KEY idx_application_case_status (status, created_at),
    KEY idx_application_case_org (org_code, status)
);

CREATE TABLE IF NOT EXISTS migration_acceptance_run (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    run_no VARCHAR(64) NOT NULL,
    org_code VARCHAR(64) NOT NULL,
    checked_at DATETIME NOT NULL,
    sample_limit INT NOT NULL DEFAULT 100,
    overall_status VARCHAR(32) NOT NULL,
    salary_todo BIGINT NOT NULL DEFAULT 0,
    salary_done BIGINT NOT NULL DEFAULT 0,
    history_prepared BIGINT NOT NULL DEFAULT 0,
    history_executed BIGINT NOT NULL DEFAULT 0,
    history_blocked BIGINT NOT NULL DEFAULT 0,
    review_pending BIGINT NOT NULL DEFAULT 0,
    data_governance_issues BIGINT NOT NULL DEFAULT 0,
    warning_count INT NOT NULL DEFAULT 0,
    gate_count INT NOT NULL DEFAULT 0,
    summary_json JSON NULL,
    issues_json JSON NULL,
    created_by VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_migration_acceptance_run_no (run_no),
    KEY idx_migration_acceptance_org_time (org_code, checked_at),
    KEY idx_migration_acceptance_status (overall_status, checked_at)
);

CREATE TABLE IF NOT EXISTS migration_acceptance_gate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    run_no VARCHAR(64) NOT NULL,
    gate_code VARCHAR(64) NOT NULL,
    gate_title VARCHAR(255) NOT NULL,
    gate_status VARCHAR(32) NOT NULL,
    gate_count BIGINT NOT NULL DEFAULT 0,
    message VARCHAR(1024) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_migration_acceptance_gate (run_no, gate_code),
    KEY idx_migration_acceptance_gate_status (gate_status)
);

CREATE TABLE IF NOT EXISTS migration_acceptance_issue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    run_no VARCHAR(64) NOT NULL,
    person_code VARCHAR(128) NULL,
    person_name VARCHAR(128) NULL,
    org_code VARCHAR(64) NOT NULL,
    issue_type VARCHAR(64) NOT NULL,
    message VARCHAR(1024) NULL,
    review_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    review_reason VARCHAR(1024) NULL,
    reviewed_by VARCHAR(64) NULL,
    reviewed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_migration_acceptance_issue_run (run_no, review_status),
    KEY idx_migration_acceptance_issue_person (person_code),
    KEY idx_migration_acceptance_issue_org (org_code, review_status)
);

CREATE TABLE IF NOT EXISTS salary_generated_timeline_issue_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_item_id VARCHAR(255) NOT NULL,
    person_code VARCHAR(128) NOT NULL,
    org_code VARCHAR(64) NOT NULL,
    review_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    review_reason VARCHAR(1024) NULL,
    reviewed_by VARCHAR(64) NULL,
    reviewed_at DATETIME NULL,
    retest_status VARCHAR(32) NULL,
    retest_summary VARCHAR(1024) NULL,
    retested_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_generated_issue_work_item (work_item_id),
    KEY idx_generated_issue_person (person_code),
    KEY idx_generated_issue_org_status (org_code, review_status)
);

CREATE TABLE IF NOT EXISTS salary_data_governance_task_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_item_id VARCHAR(255) NOT NULL,
    person_code VARCHAR(128) NOT NULL,
    org_code VARCHAR(64) NOT NULL,
    issue_type VARCHAR(64) NOT NULL,
    review_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    review_reason VARCHAR(1024) NULL,
    reviewed_by VARCHAR(64) NULL,
    reviewed_at DATETIME NULL,
    retest_status VARCHAR(32) NULL,
    retest_summary VARCHAR(1024) NULL,
    retested_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_governance_task_work_item (work_item_id),
    KEY idx_governance_task_person (person_code),
    KEY idx_governance_task_org_status (org_code, review_status),
    KEY idx_governance_task_issue_type (issue_type)
);

DELIMITER $$

DROP PROCEDURE IF EXISTS add_column_if_missing$$
DROP PROCEDURE IF EXISTS add_index_if_missing$$

CREATE PROCEDURE add_column_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN `', p_column_name, '` ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE add_index_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_columns TEXT
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND index_name = p_index_name
    ) THEN
        SET @ddl = CONCAT('CREATE INDEX `', p_index_name, '` ON `', p_table_name, '` (', p_columns, ')');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

CALL add_column_if_missing('salary_business_case', 'trial_status', 'VARCHAR(32) NULL');
CALL add_column_if_missing('salary_business_case', 'trial_matched', 'TINYINT NULL');
CALL add_column_if_missing('salary_business_case', 'trial_difference', 'DECIMAL(18,2) NULL');
CALL add_column_if_missing('salary_business_case', 'trial_summary', 'VARCHAR(2048) NULL');
CALL add_column_if_missing('salary_business_case', 'trial_baseline_total', 'DECIMAL(18,2) NULL');
CALL add_column_if_missing('salary_business_case', 'trial_calculated_total', 'DECIMAL(18,2) NULL');
CALL add_column_if_missing('salary_business_case', 'trial_expected_total', 'DECIMAL(18,2) NULL');
CALL add_column_if_missing('salary_business_case', 'trial_changes_json', 'LONGTEXT NULL');
CALL add_column_if_missing('salary_business_case', 'force_reason', 'VARCHAR(1024) NULL');
CALL add_column_if_missing('salary_business_case', 'difference_reason', 'VARCHAR(1024) NULL');
CALL add_column_if_missing('salary_business_case', 'cancel_reason', 'VARCHAR(1024) NULL');
CALL add_column_if_missing('salary_business_case', 'review_status', 'VARCHAR(32) NULL');
CALL add_column_if_missing('salary_business_case', 'review_reason', 'VARCHAR(1024) NULL');
CALL add_column_if_missing('salary_business_case', 'reviewed_by', 'VARCHAR(64) NULL');
CALL add_column_if_missing('salary_business_case', 'reviewed_at', 'DATETIME NULL');

CALL add_column_if_missing('salary_business_case_snapshot', 'salary_items_json', 'LONGTEXT NULL');

CALL add_column_if_missing('salary_history_write_plan', 'execution_result', 'VARCHAR(32) NULL');
CALL add_column_if_missing('salary_history_write_plan', 'execution_message', 'VARCHAR(1024) NULL');
CALL add_column_if_missing('salary_history_write_plan', 'inserted_history_id', 'VARCHAR(128) NULL');
CALL add_column_if_missing('salary_history_write_plan', 'previous_history_id', 'VARCHAR(128) NULL');
CALL add_column_if_missing('salary_history_write_plan', 'next_history_id', 'VARCHAR(128) NULL');
CALL add_column_if_missing('salary_history_write_plan', 'rolled_back_by', 'VARCHAR(64) NULL');
CALL add_column_if_missing('salary_history_write_plan', 'rolled_back_at', 'DATETIME NULL');
CALL add_column_if_missing('salary_history_write_plan', 'rollback_message', 'VARCHAR(1024) NULL');
CALL add_column_if_missing('salary_history_write_plan', 'comparison_review_status', 'VARCHAR(32) NULL');
CALL add_column_if_missing('salary_history_write_plan', 'comparison_review_category', 'VARCHAR(64) NULL');
CALL add_column_if_missing('salary_history_write_plan', 'comparison_review_reason', 'VARCHAR(1024) NULL');
CALL add_column_if_missing('salary_history_write_plan', 'comparison_reviewed_by', 'VARCHAR(64) NULL');
CALL add_column_if_missing('salary_history_write_plan', 'comparison_reviewed_at', 'DATETIME NULL');

CALL add_column_if_missing('salary_todo_cache_meta', 'cache_status', 'VARCHAR(32) NOT NULL DEFAULT ''ACTIVE''');
CALL add_column_if_missing('salary_todo_cache_meta', 'dirty_at', 'DATETIME NULL');
CALL add_column_if_missing('application_case', 'workflow_status', 'VARCHAR(32) NOT NULL DEFAULT ''APPLICATION_TODO''');

CALL add_index_if_missing('application_case', 'idx_application_case_workflow', 'workflow_status, status');
CALL add_index_if_missing('salary_business_case', 'idx_salary_case_work_status', 'work_item_id, status');
CALL add_index_if_missing('salary_business_case', 'idx_salary_case_status_trial_review_org', 'status, trial_status, review_status, org_code');
CALL add_index_if_missing('hisbase', 'idx_hisbase_workbench_person_period_type', 'dwbm, grbm, jsnf, jsyf, jslb');
CALL add_index_if_missing('dryzwbh', 'idx_dryzwbh_workbench_person_date', 'dwbm, grbm, srny, id');
CALL add_index_if_missing('dxl', 'idx_dxl_workbench_person_date', 'dwbm, grbm, bysj, xllb');
CALL add_index_if_missing('dndkh', 'idx_dndkh_workbench_person_year_result', 'dwbm, grbm, khnd, khjg');

DROP PROCEDURE add_index_if_missing;
DROP PROCEDURE add_column_if_missing;
