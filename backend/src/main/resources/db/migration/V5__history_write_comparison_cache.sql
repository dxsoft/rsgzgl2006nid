SET @add_comparison_status = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'salary_history_write_plan'
              AND column_name = 'comparison_status'
        ),
        'SELECT 1',
        'ALTER TABLE salary_history_write_plan ADD COLUMN comparison_status VARCHAR(32) NULL'
    )
);
PREPARE stmt FROM @add_comparison_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_comparison_mismatch_count = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'salary_history_write_plan'
              AND column_name = 'comparison_mismatch_count'
        ),
        'SELECT 1',
        'ALTER TABLE salary_history_write_plan ADD COLUMN comparison_mismatch_count INT NULL'
    )
);
PREPARE stmt FROM @add_comparison_mismatch_count;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_comparison_index = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'salary_history_write_plan'
              AND index_name = 'idx_salary_history_write_plan_comparison'
        ),
        'SELECT 1',
        'CREATE INDEX idx_salary_history_write_plan_comparison ON salary_history_write_plan (comparison_status, comparison_review_status, plan_status, execution_result)'
    )
);
PREPARE stmt FROM @add_comparison_index;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
