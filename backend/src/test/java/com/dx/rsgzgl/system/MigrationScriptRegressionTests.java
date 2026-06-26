package com.dx.rsgzgl.system;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationScriptRegressionTests {

    @Test
    void flywayScriptsContainWorkbenchRuntimeSchema() throws IOException {
        String migrationSql = readMigrationSql();

        assertContains(migrationSql, "create table if not exists salary_business_case");
        assertContains(migrationSql, "create table if not exists salary_business_case_snapshot");
        assertContains(migrationSql, "create table if not exists salary_history_write_plan");
        assertContains(migrationSql, "create table if not exists salary_todo_candidate_cache");
        assertContains(migrationSql, "create table if not exists salary_todo_cache_meta");
        assertContains(migrationSql, "create table if not exists application_case");
        assertContains(migrationSql, "create table if not exists migration_acceptance_run");
        assertContains(migrationSql, "create table if not exists migration_acceptance_gate");
        assertContains(migrationSql, "create table if not exists migration_acceptance_issue");
        assertContains(migrationSql, "create table if not exists history_write_delivery_acceptance");
        assertContains(migrationSql, "salary_delivery_archive");
        assertContains(migrationSql, "交付归档");
        assertContains(migrationSql, "create table if not exists salary_generated_timeline_issue_review");
        assertContains(migrationSql, "create table if not exists salary_data_governance_task_review");
        assertContains(migrationSql, "create table if not exists salary_report_print_batch");
        assertContains(migrationSql, "create table if not exists salary_report_print_batch_item");

        assertContains(migrationSql, "comparison_review_status");
        assertContains(migrationSql, "comparison_review_category");
        assertContains(migrationSql, "comparison_review_reason");
        assertContains(migrationSql, "comparison_status");
        assertContains(migrationSql, "comparison_mismatch_count");
        assertContains(migrationSql, "idx_salary_history_write_plan_comparison");
    }

    private String readMigrationSql() throws IOException {
        Path migrationDir = Path.of("src", "main", "resources", "db", "migration");
        StringBuilder sql = new StringBuilder();
        try (var files = Files.list(migrationDir)) {
            for (Path file : files
                    .filter(path -> path.getFileName().toString().endsWith(".sql"))
                    .sorted()
                    .toList()) {
                sql.append(Files.readString(file)).append('\n');
            }
        }
        return sql.toString().toLowerCase(Locale.ROOT);
    }

    private void assertContains(String actual, String expected) {
        assertTrue(actual.contains(expected.toLowerCase(Locale.ROOT)), () -> "Missing migration SQL: " + expected);
    }
}
