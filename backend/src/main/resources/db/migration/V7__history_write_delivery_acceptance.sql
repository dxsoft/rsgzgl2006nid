CREATE TABLE IF NOT EXISTS history_write_delivery_acceptance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    acceptance_no VARCHAR(64) NOT NULL,
    export_type VARCHAR(32) NOT NULL,
    exported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pending_count BIGINT NOT NULL DEFAULT 0,
    closed_count BIGINT NOT NULL DEFAULT 0,
    active_queue_count BIGINT NOT NULL DEFAULT 0,
    evidence_file_count BIGINT NOT NULL DEFAULT 0,
    conclusion VARCHAR(512) NULL,
    summary_json JSON NULL,
    exported_by VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_history_write_delivery_acceptance_no (acceptance_no),
    KEY idx_history_write_delivery_acceptance_type_time (export_type, exported_at),
    KEY idx_history_write_delivery_acceptance_operator (exported_by, exported_at)
);
