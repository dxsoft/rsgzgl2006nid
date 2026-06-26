CREATE TABLE IF NOT EXISTS salary_report_print_batch (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    batch_no VARCHAR(64) NOT NULL,
    report_type VARCHAR(64) NOT NULL,
    org_code VARCHAR(64) NOT NULL,
    event_year INT NULL,
    event_month INT NULL,
    business_type VARCHAR(128) NULL,
    keyword VARCHAR(255) NULL,
    limit_count INT NOT NULL DEFAULT 0,
    printed_count INT NOT NULL DEFAULT 0,
    blocked_count INT NOT NULL DEFAULT 0,
    warning_count INT NOT NULL DEFAULT 0,
    printed_by VARCHAR(64) NULL,
    printed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    summary VARCHAR(1024) NULL,
    UNIQUE KEY uk_salary_report_print_batch_no (batch_no),
    KEY idx_salary_report_print_batch_org_period (org_code, event_year, event_month),
    KEY idx_salary_report_print_batch_type_time (report_type, printed_at)
);

CREATE TABLE IF NOT EXISTS salary_report_print_batch_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    batch_no VARCHAR(64) NOT NULL,
    case_no VARCHAR(64) NOT NULL,
    person_code VARCHAR(128) NOT NULL,
    person_name VARCHAR(128) NULL,
    org_code VARCHAR(64) NOT NULL,
    business_type VARCHAR(128) NULL,
    validation_status VARCHAR(32) NOT NULL,
    issue_count INT NOT NULL DEFAULT 0,
    warning_count INT NOT NULL DEFAULT 0,
    summary VARCHAR(1024) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_salary_report_print_batch_item (batch_no, case_no),
    KEY idx_salary_report_print_batch_item_case (case_no, created_at),
    KEY idx_salary_report_print_batch_item_person (person_code, created_at),
    KEY idx_salary_report_print_batch_item_batch (batch_no)
);
