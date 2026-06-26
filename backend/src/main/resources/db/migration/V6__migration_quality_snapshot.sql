CREATE TABLE IF NOT EXISTS migration_quality_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    snapshot_no VARCHAR(64) NOT NULL,
    org_code VARCHAR(64) NOT NULL,
    checked_at DATETIME NOT NULL,
    overall_status VARCHAR(32) NOT NULL,
    history_blocked BIGINT NOT NULL DEFAULT 0,
    history_open BIGINT NOT NULL DEFAULT 0,
    review_pending BIGINT NOT NULL DEFAULT 0,
    regression_warnings BIGINT NOT NULL DEFAULT 0,
    regression_pending BIGINT NOT NULL DEFAULT 0,
    regression_fixing BIGINT NOT NULL DEFAULT 0,
    governance_issues BIGINT NOT NULL DEFAULT 0,
    salary_todo BIGINT NOT NULL DEFAULT 0,
    salary_done BIGINT NOT NULL DEFAULT 0,
    preflight_level VARCHAR(32) NULL,
    preflight_title VARCHAR(128) NULL,
    preflight_message VARCHAR(1024) NULL,
    archive_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    archived_by VARCHAR(64) NULL,
    archived_at DATETIME NULL,
    archive_note VARCHAR(1024) NULL,
    snapshot_json JSON NULL,
    decision_json JSON NULL,
    created_by VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_migration_quality_snapshot_no (snapshot_no),
    KEY idx_migration_quality_snapshot_org_time (org_code, checked_at),
    KEY idx_migration_quality_snapshot_status (overall_status, checked_at)
);

SET @add_preflight_level = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'migration_quality_snapshot'
              AND column_name = 'preflight_level'
        ),
        'SELECT 1',
        'ALTER TABLE migration_quality_snapshot ADD COLUMN preflight_level VARCHAR(32) NULL'
    )
);
PREPARE stmt FROM @add_preflight_level;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_preflight_title = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'migration_quality_snapshot'
              AND column_name = 'preflight_title'
        ),
        'SELECT 1',
        'ALTER TABLE migration_quality_snapshot ADD COLUMN preflight_title VARCHAR(128) NULL'
    )
);
PREPARE stmt FROM @add_preflight_title;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_preflight_message = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'migration_quality_snapshot'
              AND column_name = 'preflight_message'
        ),
        'SELECT 1',
        'ALTER TABLE migration_quality_snapshot ADD COLUMN preflight_message VARCHAR(1024) NULL'
    )
);
PREPARE stmt FROM @add_preflight_message;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_decision_json = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'migration_quality_snapshot'
              AND column_name = 'decision_json'
        ),
        'SELECT 1',
        'ALTER TABLE migration_quality_snapshot ADD COLUMN decision_json JSON NULL'
    )
);
PREPARE stmt FROM @add_decision_json;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_archive_status = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'migration_quality_snapshot'
              AND column_name = 'archive_status'
        ),
        'SELECT 1',
        'ALTER TABLE migration_quality_snapshot ADD COLUMN archive_status VARCHAR(32) NOT NULL DEFAULT ''DRAFT'''
    )
);
PREPARE stmt FROM @add_archive_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_archived_by = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'migration_quality_snapshot'
              AND column_name = 'archived_by'
        ),
        'SELECT 1',
        'ALTER TABLE migration_quality_snapshot ADD COLUMN archived_by VARCHAR(64) NULL'
    )
);
PREPARE stmt FROM @add_archived_by;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_archived_at = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'migration_quality_snapshot'
              AND column_name = 'archived_at'
        ),
        'SELECT 1',
        'ALTER TABLE migration_quality_snapshot ADD COLUMN archived_at DATETIME NULL'
    )
);
PREPARE stmt FROM @add_archived_at;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_archive_note = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'migration_quality_snapshot'
              AND column_name = 'archive_note'
        ),
        'SELECT 1',
        'ALTER TABLE migration_quality_snapshot ADD COLUMN archive_note VARCHAR(1024) NULL'
    )
);
PREPARE stmt FROM @add_archive_note;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
