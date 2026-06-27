DELIMITER $$

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

CALL add_index_if_missing('salary_history_write_plan', 'idx_history_plan_status_time', 'plan_status, prepared_at, id');
CALL add_index_if_missing('salary_history_write_plan', 'idx_history_plan_org_status_time', 'org_code, plan_status, prepared_at, id');
CALL add_index_if_missing('salary_history_write_plan', 'idx_history_plan_write_queue', 'plan_status, writable, comparison_status, comparison_review_status');
CALL add_index_if_missing('salary_history_write_plan', 'idx_history_plan_case_no', 'case_no');

DROP PROCEDURE add_index_if_missing;
