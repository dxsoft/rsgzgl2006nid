DELIMITER $$

DROP PROCEDURE IF EXISTS add_index_if_missing$$

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

CALL add_index_if_missing('salary_business_case', 'idx_salary_case_done_page', 'status, event_year, event_month, handled_at, case_no');
CALL add_index_if_missing('salary_data_governance_task_review', 'idx_governance_task_done_page', 'review_status, reviewed_at, retested_at, created_at, id');
CALL add_index_if_missing('salary_report_print_batch_item', 'idx_salary_report_print_item_case_latest', 'case_no, created_at, id');

DROP PROCEDURE add_index_if_missing;
