package com.dx.rsgzgl.system;

import com.dx.rsgzgl.salary.dto.NormalGradeTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeTrialResult;
import com.dx.rsgzgl.salary.service.NormalGradeTrialService;
import com.dx.rsgzgl.system.service.AuthSessionService;
import com.dx.rsgzgl.system.service.WorkbenchService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipInputStream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SystemPermissionRegressionTests {

    private static final String ADMIN_USER = "admin";
    private static final String WORKBENCH_USER = "tmp_test_workbench_only";
    private static final String WORKBENCH_ROLE = "TMP_TEST_WORKBENCH_ONLY";
    private static final String TODO_USER = "tmp_test_workbench_todo";
    private static final String TODO_ROLE = "TMP_TEST_WORKBENCH_TODO";
    private static final String SCOPED_WORKBENCH_USER = "tmp_test_workbench_scoped";
    private static final String SCOPED_WORKBENCH_ROLE = "TMP_TEST_WORKBENCH_SCOPED";
    private static final String ORG_USER = "tmp_test_org_001";
    private static final String ORG_ROLE = "TMP_TEST_ORG_001";
    private static final String TRIAL_USER = "tmp_test_trial_001";
    private static final String TRIAL_ROLE = "TMP_TEST_TRIAL_001";
    private static final String RECONCILE_USER = "tmp_test_reconcile_001";
    private static final String RECONCILE_ROLE = "TMP_TEST_RECONCILE_001";
    private static final String CREATED_USER = "tmp_test_created_user";
    private static final String CASE_WORK_ITEM = "tmp-test-salary-case-001";
    private static final String HISTORY_WRITE_WORK_ITEM = "tmp-test-history-write-success";
    private static final String RANK_ALLOWANCE_WORK_ITEM = "tmp-test-rank-allowance-write";
    private static final String RANK_ALLOWANCE_CASE_NO = "GZ-TMP-RANK-ALLOWANCE";
    private static final String HISTORY_WRITE_CASE_NO = "GZ-TMP-HISTORY-WRITE";
    private static final String HISTORY_WRITE_SOURCE_ID = "TMP-HIS-SOURCE-00055";
    private static final String TODO_DUPLICATE_CASE_WORK_ITEM = "salary-todo-001-00055-2026-2-职务变化";
    private static final String TODO_LATER_HISTORY_ID = "TMP-TODO-LATER-HISTORY";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NormalGradeTrialService normalGradeTrialService;

    @Autowired
    private WorkbenchService workbenchService;

    @BeforeEach
    void setUp() {
        cleanup();
        createUserRole(WORKBENCH_USER, WORKBENCH_ROLE, "WORKBENCH");
        createUserRole(TODO_USER, TODO_ROLE, "WORKBENCH", "SALARY_TODO");
        createUserRole(SCOPED_WORKBENCH_USER, SCOPED_WORKBENCH_ROLE, "WORKBENCH", "SALARY_PERSON", "SALARY_TODO", "SALARY_DONE", "SALARY_HISTORY_WRITE", "SALARY_HISTORY_ROLLBACK", "SALARY_EXPORT", "SALARY_TRIAL", "SALARY_CONFIG", "SALARY_GOVERNANCE", "SALARY_ACCEPTANCE", "APPLICATION_TODO", "APPLICATION_DONE");
        createUserRole(ORG_USER, ORG_ROLE, "SALARY_PERSON");
        createUserRole(TRIAL_USER, TRIAL_ROLE, "SALARY_TRIAL");
        createUserRole(RECONCILE_USER, RECONCILE_ROLE, "SALARY_RECONCILE");
        jdbcTemplate.update("""
                INSERT IGNORE INTO sys_user_org(username, org_code)
                VALUES (?, '001')
                """, SCOPED_WORKBENCH_USER);
        jdbcTemplate.update("""
                INSERT IGNORE INTO sys_user_org(username, org_code)
                VALUES (?, '001')
                """, ORG_USER);
        jdbcTemplate.update("""
                INSERT IGNORE INTO sys_user_org(username, org_code)
                VALUES (?, '001')
                """, TRIAL_USER);
        jdbcTemplate.update("""
                INSERT IGNORE INTO sys_user_org(username, org_code)
                VALUES (?, '001')
                """, RECONCILE_USER);
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @Test
    void workbenchWithoutSalaryTodoDoesNotReturnSalaryItems() throws Exception {
        mockMvc.perform(get("/api/workbench/summary")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("SALARY_TODO"))))
                .andExpect(content().string(not(containsString("SALARY_DONE"))));

        mockMvc.perform(get("/api/workbench/items?status=TODO&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")))
                .andExpect(content().string(containsString("\"items\":[]")));

        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, WORKBENCH_USER))
                .andExpect(status().isBadRequest());
    }

    @Test
    void workbenchCsvRequiresExportPermission() throws Exception {
        mockMvc.perform(get("/api/workbench/summary")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SALARY_TODO")))
                .andExpect(content().string(not(containsString("\"count\":-1"))));

        mockMvc.perform(get("/api/workbench/metrics/salary-todo")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":\"SALARY_TODO\"")))
                .andExpect(content().string(not(containsString("\"count\":-1"))));

        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":\"SALARY_TODO\"")))
                .andExpect(content().string(not(containsString("\"count\":-1"))))
                .andExpect(content().string(containsString("\"hint\":\"")))
                .andExpect(content().string(containsString("T")));

        mockMvc.perform(post("/api/workbench/salary-todo-cache/dirty")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":\"SALARY_TODO\"")))
                .andExpect(content().string(not(containsString("\"count\":-1"))))
                .andExpect(content().string(containsString("\"hint\":\"")))
                .andExpect(content().string(containsString("T")));

        assertAudit("workbench", "salary-todo-cache-dirty", "SALARY_TODO_CACHE", "ALL", TODO_USER, "marked dirty");

        mockMvc.perform(get("/api/workbench/items.csv?status=TODO&limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/workbench/history-write-plans.csv?limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isForbidden());

        Long deniedHistoryDeliveryExportAuditBaseline = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM sys_audit_log
                WHERE operator = ?
                  AND action_name IN ('history-write-delivery-overview-csv', 'history-write-closure-acceptance-package', 'salary-migration-closure-checklist-csv', 'salary-migration-delivery-package')
                """, Long.class, TODO_USER);

        mockMvc.perform(get("/api/workbench/history-write-delivery-overview.csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/workbench/salary-migration-closure-checklist.csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/workbench/salary-migration-delivery-package.zip")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/workbench/history-write-closure-acceptance-package.zip")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isForbidden());

        Long deniedHistoryDeliveryExportAuditCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM sys_audit_log
                WHERE operator = ?
                  AND action_name IN ('history-write-delivery-overview-csv', 'history-write-closure-acceptance-package', 'salary-migration-closure-checklist-csv', 'salary-migration-delivery-package')
                """, Long.class, TODO_USER);
        assertEquals(deniedHistoryDeliveryExportAuditBaseline, deniedHistoryDeliveryExportAuditCount);

        mockMvc.perform(post("/api/workbench/history-write-plans/batch-preview?limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Salary done permission is required")));

        mockMvc.perform(post("/api/workbench/history-write-plans/batch-execute?limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Salary history write permission is required")));

        mockMvc.perform(post("/api/workbench/history-write-plans/batch-rollback?limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                        .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Salary history rollback permission is required")));
    }

    @Test
    void historyDeliveryExportButtonsArePermissionGatedInStaticUi() throws Exception {
        String app = Files.readString(Path.of("src", "main", "resources", "static", "app.js"), StandardCharsets.UTF_8);
        String styles = Files.readString(Path.of("src", "main", "resources", "static", "styles.css"), StandardCharsets.UTF_8);
        int deliveryStart = app.indexOf("    showHistoryDeliveryOverview() {");
        int closureStart = app.indexOf("    showHistoryClosureAcceptance() {");
        String deliverySection = app.substring(
                deliveryStart,
                closureStart
        );
        String closureSection = app.substring(
                closureStart,
                app.indexOf("    async exportClosureQueue")
        );

        assertTrue(deliverySection.contains("const canExport = Permissions.has(\"SALARY_EXPORT\");"));
        assertTrue(deliverySection.contains("const canExportAcceptancePackage = canExport && Permissions.has(\"SALARY_ACCEPTANCE\");"));
        assertTrue(deliverySection.contains("${canExport ? `<button type=\"button\" class=\"case-snapshot-button\" data-history-delivery-export-summary>"));
        assertTrue(deliverySection.contains("${canExport ? `<button type=\"button\" class=\"case-snapshot-button\" data-salary-migration-closure-checklist>"));
        assertTrue(deliverySection.contains("${canExport ? `<button type=\"button\" class=\"case-snapshot-button\" data-salary-migration-delivery-ledger>"));
        assertTrue(deliverySection.contains("${canExport ? `<button type=\"button\" class=\"case-snapshot-button\" data-salary-migration-delivery-self-check>"));
        assertTrue(deliverySection.contains("${canExportAcceptancePackage ? `<button type=\"button\" class=\"case-snapshot-button\" data-salary-migration-delivery-package>"));
        assertTrue(deliverySection.contains("${canExportAcceptancePackage ? `<button type=\"button\" class=\"case-snapshot-button\" data-history-delivery-package>"));
        assertEquals(2, countOccurrences(deliverySection, "data-history-delivery-export-summary"));
        assertEquals(2, countOccurrences(deliverySection, "data-salary-migration-closure-checklist"));
        assertEquals(2, countOccurrences(deliverySection, "data-salary-migration-delivery-package"));
        assertEquals(2, countOccurrences(deliverySection, "data-history-delivery-package"));
        assertTrue(deliverySection.contains("/api/workbench/salary-migration-closure-checklist.csv"));
        assertTrue(deliverySection.contains("/api/workbench/salary-migration-delivery-ledger.csv"));
        assertTrue(deliverySection.contains("/api/workbench/salary-migration-delivery-self-check.csv"));
        assertTrue(deliverySection.contains("/api/workbench/salary-migration-delivery-package.zip"));
        assertTrue(deliverySection.contains("data-salary-migration-delivery-audits"));
        assertTrue(deliverySection.contains("openSalaryMigrationDeliveryAudits()"));
        assertTrue(deliverySection.contains("data-salary-migration-delivery-audit-list"));
        assertTrue(deliverySection.contains("loadSalaryMigrationDeliveryPackageAudits(overlay)"));
        assertTrue(deliverySection.contains("/api/system/audits?module=workbench&action=salary-migration-delivery-package&targetCode=SALARY_MIGRATION_DELIVERY&limit=5"));
        assertTrue(deliverySection.contains("scope.historyStatus"));
        assertTrue(deliverySection.contains("scope.reportStatus"));
        assertTrue(deliverySection.contains("historyStatus === \"ERROR\""));
        assertTrue(deliverySection.contains("reportStatus === \"ERROR\""));
        assertTrue(deliverySection.contains("salary-migration-delivery-row ${hasError ? \"blocked\" : \"ready\"}"));
        assertTrue(deliverySection.contains("data-salary-migration-delivery-history-audits"));
        assertTrue(deliverySection.contains("data-salary-migration-delivery-report-audits"));
        assertTrue(deliverySection.contains("openHistoryClosureAcceptanceAudits()"));
        assertTrue(deliverySection.contains("openReportMigrationDeliveryAudits()"));
        assertTrue(deliverySection.contains("data-salary-migration-delivery-audit-id"));
        assertTrue(deliverySection.contains("openReportAuditById(salaryMigrationDeliveryAuditButton.dataset.salaryMigrationDeliveryAuditId"));
        assertTrue(deliverySection.contains("data-history-delivery-acceptances"));
        assertTrue(deliverySection.contains("loadHistoryDeliveryAcceptances(overlay)"));
        assertTrue(deliverySection.contains("loadHistoryDeliveryAcceptanceDetail(overlay"));
        assertTrue(deliverySection.contains("data-history-delivery-acceptance-keyword"));
        assertTrue(deliverySection.contains("data-history-delivery-acceptance-export-type"));
        assertTrue(deliverySection.contains("data-history-delivery-acceptance-exported-from"));
        assertTrue(deliverySection.contains("data-history-delivery-acceptance-exported-to"));
        assertTrue(deliverySection.contains("data-history-delivery-date-preset=\"current-month\""));
        assertTrue(deliverySection.contains("data-history-delivery-date-preset=\"previous-month\""));
        assertTrue(deliverySection.contains("data-history-delivery-date-preset=\"last-30-days\""));
        assertTrue(deliverySection.contains("data-history-delivery-date-preset=\"clear\""));
        assertTrue(deliverySection.contains("data-history-delivery-acceptance-index-export"));
        assertTrue(deliverySection.contains("data-history-delivery-acceptance-batch-print"));
        assertTrue(deliverySection.contains("data-history-delivery-acceptance-batch-audits"));
        assertTrue(deliverySection.contains("applyHistoryDeliveryDatePreset(overlay"));
        assertTrue(deliverySection.contains("historyDeliveryAcceptanceQuery(overlay, 20)"));
        assertTrue(deliverySection.contains("data-history-delivery-acceptance-more"));
        assertTrue(deliverySection.contains("loadMoreHistoryDeliveryAcceptances(overlay)"));
        assertTrue(app.contains("showHistoryDeliveryArchive()"));
        assertTrue(app.contains("state.activeMenuCode === \"SALARY_DELIVERY_ARCHIVE\""));
        assertTrue(app.contains("data-history-delivery-archive"));
        assertTrue(app.contains("data-history-delivery-archive-overview"));
        assertTrue(app.contains("history-delivery-closure-panel"));
        assertTrue(app.contains("data-history-delivery-audit-id"));
        assertTrue(app.contains("data-history-delivery-audit-lookup"));
        assertTrue(app.contains("SystemShell.selectView(\"system\", \"SYSTEM_AUDIT\")"));
        assertTrue(app.contains("WorkbenchPanel.loadHistoryDeliveryAcceptances(els.migrationToolResult, { reset: true })"));
        assertTrue(app.contains("WorkbenchPanel.loadHistoryDeliveryAcceptanceDetail(els.migrationToolResult"));
        assertTrue(app.contains("applyHistoryDeliveryDatePreset(els.migrationToolResult"));
        assertTrue(app.contains("historyDeliveryDateText(date)"));
        assertTrue(app.contains("fromInput.value = WorkbenchPanel.historyDeliveryDateText(from);"));
        assertTrue(app.contains("toInput.value = WorkbenchPanel.historyDeliveryDateText(to);"));
        assertTrue(app.contains("historyDeliveryAcceptanceQuery(els.migrationToolResult, 50)"));
        assertTrue(app.contains("loadMoreHistoryDeliveryAcceptances(els.migrationToolResult)"));
        assertTrue(app.contains("overlay.dataset.historyDeliveryAcceptanceLimit"));
        assertTrue(app.contains("const requestLimit = Math.min(limit + 1, 100);"));
        assertTrue(app.contains("const visibleRows = rows.slice(0, limit);"));
        assertTrue(app.contains("const hasMore = rows.length > visibleRows.length;"));
        assertTrue(app.contains("historyDeliveryAcceptanceFilterSummary(overlay)"));
        assertTrue(app.contains("history-delivery-acceptance-scope"));
        assertTrue(app.contains("history-delivery-acceptance-tags"));
        assertTrue(app.contains("\\u5f53\\u524d\\u8303\\u56f4"));
        assertTrue(app.contains("Math.min(current + 8, 100)"));
        assertTrue(app.contains("params.set(\"exportedFrom\", exportedFrom);"));
        assertTrue(app.contains("params.set(\"exportedTo\", exportedTo);"));
        assertTrue(app.contains("openHistoryDeliveryAcceptanceAudits("));
        assertTrue(app.contains("openHistoryDeliveryAcceptanceBatchAudits()"));
        assertTrue(app.contains("data-audit-preset=\"salaryMigrationDeliveryPackage\""));
        assertTrue(app.contains("action: \"salary-migration-delivery-package\""));
        assertTrue(app.contains("targetCode: \"SALARY_MIGRATION_DELIVERY\""));
        assertTrue(app.contains("String(item.id || \"\").startsWith(\"salary-migration-delivery-error-\")"));
        assertTrue(app.contains("migration-delivery-governance"));
        assertTrue(app.contains("data-workbench-audit-salary-migration-delivery"));
        assertTrue(app.contains("data-workbench-audit-history-closure"));
        assertTrue(app.contains("data-workbench-audit-report-delivery"));
        assertTrue(app.contains("openMigrationDeliveryGovernanceDetail(item)"));
        assertTrue(app.contains("/migration-delivery-detail"));
        assertTrue(app.contains("data-migration-delivery-governance-retest"));
        assertTrue(app.contains("data-migration-delivery-governance-result"));
        assertTrue(app.contains("data-migration-delivery-governance-history-audits"));
        assertTrue(app.contains("data-migration-delivery-governance-report-audits"));
        assertTrue(app.contains("detail.closeSuggested"));
        assertTrue(app.contains("\\u786e\\u8ba4\\u5173\\u95ed"));
        assertTrue(app.contains("migration-delivery-close-suggested"));
        assertTrue(app.contains("data-history-delivery-acceptance-audits"));
        assertTrue(app.contains("data-audit-preset=\"historyDeliveryAcceptance\""));
        assertTrue(app.contains("auditScopeValues(summary = \"\")"));
        assertTrue(app.contains("auditSummaryHtml(summary = \"\")"));
        assertTrue(app.contains("values[scopedKey] = scopeRest.join(\"=\").trim() || scoped.trim();"));
        assertTrue(app.contains("audit-scope-tags"));
        assertTrue(app.contains("audit-raw-summary"));
        assertTrue(app.contains("name=\"auditId\""));
        assertTrue(app.contains("auditId: (values.auditId || \"\").trim()"));
        assertTrue(app.contains("SystemPanel.auditSummaryHtml(item.summary || \"-\")"));
        assertTrue(app.contains("openReportAuditById(auditId)"));
        assertTrue(app.contains("openReportAuditByTarget(targetCode)"));
        assertTrue(app.contains("data-case-report-audit-id"));
        assertTrue(app.contains("data-case-report-audit-target"));
        assertTrue(app.contains("data-report-center-audit-id"));
        assertTrue(app.contains("data-report-center-audit-target"));
        assertTrue(app.contains("dataset.reportCenterAuditId"));
        assertTrue(app.contains("dataset.reportCenterAuditTarget"));
        assertTrue(app.contains("const scope = ["));
        assertTrue(app.contains("result.targetCode ? `\\u5bf9\\u8c61 ${result.targetCode}`"));
        assertTrue(app.contains("result.action ? `\\u52a8\\u4f5c ${Format.auditActionText(result.action)}`"));
        assertTrue(app.contains("result.operator ? `\\u64cd\\u4f5c\\u4eba ${result.operator}`"));
        assertTrue(app.contains("result.start ? `\\u8d77 ${result.start}`"));
        assertTrue(app.contains("result.end ? `\\u6b62 ${result.end}`"));
        assertTrue(app.contains("auditId: safeAuditId"));
        assertTrue(app.contains("targetCode: safeTargetCode"));
        assertTrue(app.contains("\\u5df2\\u6309\\u5ba1\\u8ba1\\u53f7 ${safeAuditId}"));
        assertTrue(app.contains("\\u5df2\\u6309\\u5bf9\\u8c61 ${safeTargetCode}"));
        assertTrue(app.contains("report-batch-summary"));
        assertTrue(app.contains("report-batch-statuses"));
        assertTrue(app.contains("item.historyWritten === true || item.historyWritten === 1"));
        assertTrue(app.contains("item.closureReady === true || item.closureReady === 1"));
        assertTrue(app.contains("data-report-batch-filter=\"unwritten\""));
        assertTrue(app.contains("data-report-batch-filter=\"unclosed\""));
        assertTrue(app.contains("data-report-batch-filter=\"blocked\""));
        assertTrue(app.contains("data-history-batch-queue-unwritten"));
        assertTrue(app.contains("data-history-batch-queue-unclosed"));
        assertTrue(app.contains("data-report-batch-row"));
        assertTrue(app.contains("reportBatchReturnContext"));
        assertTrue(app.contains("report-batch-return-summary"));
        assertTrue(app.contains("report-batch-returned"));
        assertTrue(app.contains("returnContext: {"));
        assertTrue(app.contains("result.audits || []"));
        assertTrue(app.contains("report-batch-audit-list"));
        assertTrue(app.contains("data-report-batch-audit-id"));
        assertTrue(app.contains("const canExportAcceptancePackage = Permissions.has(\"SALARY_EXPORT\") && Permissions.has(\"SALARY_ACCEPTANCE\");"));
        assertTrue(app.contains("${items.length && canExportAcceptancePackage ? `<button type=\"button\" class=\"primary\" data-report-print-batch-bulk-package>"));
        assertTrue(app.contains("${canExportAcceptancePackage ? `<button type=\"button\" data-report-print-batch-package=\"${Format.html(item.batchNo || \"\")}\">"));
        assertTrue(app.contains("${canExportAcceptancePackage ? `<button type=\"button\" class=\"case-snapshot-button\" data-report-print-batch-package=\"${Format.html(batchNo)}\">"));
        assertTrue(app.contains("data-report-print-batch-package"));
        assertTrue(app.contains("data-report-print-batch-bulk-package"));
        assertTrue(app.contains("/api/reports/print-batches/${encodeURIComponent(safeBatchNo)}/acceptance-package.zip"));
        assertTrue(app.contains("/api/reports/print-batches/acceptance-packages.zip"));
        assertTrue(app.contains("report-print-batch-acceptance-package"));
        assertTrue(app.contains("report-print-batch-acceptance-package-bulk"));
        assertTrue(app.contains("salary-history-print"));
        assertTrue(app.contains("salary-history-csv"));
        assertTrue(app.contains("salary-change-ledger-print"));
        assertTrue(app.contains("salary-change-ledger-csv"));
        assertTrue(app.contains("report-print-archive-csv"));
        assertTrue(app.contains("report-print-batch-csv"));
        assertTrue(app.contains("reportPrintBatchAcceptancePackagesUrl()"));
        assertTrue(app.contains("showReportBatchBulkExportResult()"));
        assertTrue(app.contains("showReportBatchAcceptanceExportResult(safeBatchNo)"));
        assertTrue(app.contains("data-report-batch-export-audits"));
        assertTrue(app.contains("data-audit-action=\"report-print-batch-acceptance-package\""));
        assertTrue(app.contains("data-audit-action=\"report-print-batch-acceptance-package-bulk\""));
        assertTrue(app.contains("WorkbenchPanel.loadReportAudits({"));
        assertTrue(app.contains("targetCode: reportBatchExportAuditsButton.dataset.auditTarget || \"\""));
        assertTrue(app.contains("targetCode: auditButton.dataset.auditTarget || \"\""));
        assertTrue(app.contains("data-case-report-archive-export"));
        assertTrue(app.contains("case-row-actions"));
        assertTrue(app.contains("data-report-acceptance-audit-id"));
        assertTrue(app.contains("dataset.reportAcceptanceAuditId"));
        assertTrue(app.contains("acceptanceStatusHtml(batch)"));
        assertTrue(app.contains("\\u9a8c\\u6536\\u72b6\\u6001"));
        assertTrue(app.contains("data-report-batch-bulk-result"));
        assertTrue(app.contains("data-report-batch-refresh-pending"));
        assertTrue(app.contains("data-report-batch-show-exported"));
        assertTrue(app.contains("switchReportBatchAcceptanceStatus(\"PENDING\")"));
        assertTrue(app.contains("switchReportBatchAcceptanceStatus(\"EXPORTED\")"));
        assertTrue(app.contains("item.acceptanceExported"));
        assertTrue(app.contains("item.acceptanceAuditId"));
        assertTrue(app.contains("acceptanceStatus: value(\"acceptanceStatus\") || \"ALL\""));
        assertTrue(app.contains("acceptanceStatus: values.acceptanceStatus || \"ALL\""));
        assertTrue(app.contains("auditAction: value(\"auditAction\")"));
        assertTrue(app.contains("auditOperator: value(\"auditOperator\")"));
        assertTrue(app.contains("auditStart: value(\"auditStart\")"));
        assertTrue(app.contains("auditEnd: value(\"auditEnd\")"));
        assertTrue(app.contains("action: values.auditAction || values.keyword"));
        assertTrue(app.contains("operator: values.auditOperator"));
        assertTrue(app.contains("start: values.auditStart"));
        assertTrue(app.contains("end: values.auditEnd"));
        assertTrue(app.contains("name=\"auditAction\""));
        assertTrue(app.contains("name=\"auditOperator\""));
        assertTrue(app.contains("name=\"auditStart\" type=\"datetime-local\""));
        assertTrue(app.contains("name=\"auditEnd\" type=\"datetime-local\""));
        assertTrue(app.contains("auditActionOption(\"salary-history-print\""));
        assertTrue(app.contains("auditActionOption(\"salary-change-ledger-csv\""));
        assertTrue(app.contains("auditActionOption(\"report-print-self-check-csv\""));
        assertTrue(app.contains("auditActionOption(\"report-migration-delivery-package\""));
        assertTrue(app.contains("auditActionOption(\"salary-migration-delivery-package\""));
        assertTrue(styles.contains(".salary-migration-delivery-actions"));
        assertTrue(styles.contains(".history-delivery-acceptance-row.salary-migration-delivery-row.blocked"));
        assertTrue(styles.contains(".work-item.migration-delivery-governance"));
        assertTrue(styles.contains(".work-item-delivery-warning"));
        assertTrue(styles.contains(".migration-delivery-governance-detail"));
        assertTrue(styles.contains(".migration-delivery-detail-tag.blocked"));
        assertTrue(styles.contains(".migration-delivery-close-suggested"));
        assertTrue(app.contains("name=\"acceptanceStatus\""));
        assertTrue(app.contains("acceptanceStatusOption(\"PENDING\""));
        assertTrue(app.contains("acceptanceStatusOption(\"EXPORTED\""));
        assertTrue(app.contains("batch.pendingAcceptanceBatches"));
        assertTrue(app.contains("batch.acceptanceExportedBatches"));
        assertTrue(app.contains("batch.latestAcceptanceExportedAt"));
        assertTrue(app.contains("openPendingReportBatchAcceptance()"));
        assertTrue(app.contains("data-report-pending-acceptance-batches"));
        assertTrue(app.contains("acceptanceSelect.value = \"PENDING\""));
        assertTrue(app.contains("data-report-print-self-check"));
        assertTrue(app.contains("data-report-migration-guide"));
        assertTrue(app.contains("data-report-migration-guide-export"));
        assertTrue(app.contains("data-report-migration-matrix"));
        assertTrue(app.contains("data-report-migration-matrix-export"));
        assertTrue(app.contains("data-report-acceptance-checklist"));
        assertTrue(app.contains("data-report-acceptance-checklist-export"));
        assertTrue(app.contains("data-report-sample-evidence"));
        assertTrue(app.contains("data-report-sample-evidence-export"));
        assertTrue(app.contains("data-report-migration-delivery-package"));
        assertTrue(app.contains("data-report-print-self-check-export"));
        assertTrue(app.contains("loadReportPrintSelfCheck()"));
        assertTrue(app.contains("showReportMigrationGuide()"));
        assertTrue(app.contains("/api/reports/migration-guide.csv"));
        assertTrue(app.contains("/api/reports/migration-matrix"));
        assertTrue(app.contains("/api/reports/migration-matrix.csv"));
        assertTrue(app.contains("/api/reports/migration-delivery-package.zip"));
        assertTrue(app.contains("reportMigrationDeliveryPackageUrl()"));
        assertTrue(app.contains("reportMigrationMatrixCsvUrl()"));
        assertTrue(app.contains("loadReportMigrationMatrix()"));
        assertTrue(app.contains("reportMigrationAcceptanceChecklistUrl("));
        assertTrue(app.contains("loadReportMigrationAcceptanceChecklist()"));
        assertTrue(app.contains("reportMigrationSampleEvidenceUrl("));
        assertTrue(app.contains("loadReportMigrationSampleEvidence()"));
        assertTrue(app.contains("report-migration-guide-csv"));
        assertTrue(app.contains("report-migration-matrix-csv"));
        assertTrue(app.contains("report-migration-acceptance-checklist-csv"));
        assertTrue(app.contains("report-migration-sample-evidence-csv"));
        assertTrue(app.contains("report-migration-delivery-package"));
        assertTrue(app.contains("data-audit-action=\"report-migration-delivery-package\""));
        assertTrue(app.contains("reportPrintSelfCheckCsvUrl()"));
        assertTrue(app.contains("/api/reports/migration-print-self-check.csv"));
        assertTrue(app.contains("report-print-self-check-csv"));
        assertTrue(app.contains("data-audit-action=\"report-print-self-check-csv\""));
        assertTrue(app.contains("\\u62a5\\u8868\\u6253\\u5370\\u8fc1\\u79fb\\u8bf4\\u660e"));
        assertTrue(app.contains("\\u4ea4\\u4ed8\\u4e3b\\u7ebf"));
        assertTrue(app.contains("\\u6253\\u5370\\u81ea\\u68c0"));
        assertTrue(app.contains("\\u5ba1\\u6279\\u8868\\u5f52\\u6863"));
        assertTrue(app.contains("\\u6253\\u5370\\u6279\\u6b21"));
        assertTrue(app.contains("\\u9a8c\\u6536\\u5305"));
        assertTrue(app.contains("\\u64cd\\u4f5c\\u7559\\u75d5"));
        assertTrue(app.contains("\\u95ed\\u73af\\u98ce\\u9669"));
        assertTrue(app.contains("/api/workbench/history-write-plans/report-batch-queue-audit"));
        assertTrue(app.contains("history-write-report-batch-queue"));
        assertTrue(app.contains("/api/workbench/history-write-delivery-acceptances?${WorkbenchPanel.historyDeliveryAcceptanceQuery(overlay, requestLimit).toString()}"));
        assertTrue(app.contains("/api/workbench/history-write-delivery-acceptances/index.csv?${query.toString()}"));
        assertTrue(app.contains("/api/workbench/history-write-delivery-acceptances/${encodeURIComponent(acceptanceNo)}"));
        assertTrue(app.contains("/api/workbench/history-write-delivery-acceptances/${encodeURIComponent(acceptanceNo)}.csv"));
        assertTrue(app.contains("/api/workbench/history-write-delivery-acceptances/${encodeURIComponent(acceptanceNo)}/print"));
        assertTrue(app.contains("/api/workbench/history-write-delivery-acceptances/print-batch?${query.toString()}"));
        assertTrue(app.contains("data-history-delivery-acceptance-detail"));
        assertTrue(app.contains("data-history-delivery-acceptance-export"));
        assertTrue(app.contains("data-history-delivery-acceptance-print"));
        assertTrue(app.contains("history-delivery-acceptance-detail-metrics"));
        assertTrue(app.contains("history-delivery-acceptance-proof-actions"));
        assertTrue(app.contains("history-delivery-acceptance-proof-buttons"));
        assertTrue(app.contains("\\u8bc1\\u636e\\u94fe\\u64cd\\u4f5c"));
        assertTrue(app.contains("const selectedAcceptanceNo = data.acceptanceNo || acceptanceNo;"));

        assertTrue(closureSection.contains("const canExport = Permissions.has(\"SALARY_EXPORT\");"));
        assertTrue(closureSection.contains("const canExportAcceptancePackage = canExport && Permissions.has(\"SALARY_ACCEPTANCE\");"));
        assertTrue(closureSection.contains("${canExport ? `<button type=\"button\" class=\"case-snapshot-button\" data-history-closure-acceptance-export-summary>"));
        assertTrue(closureSection.contains("${canExportAcceptancePackage ? `<button type=\"button\" class=\"case-snapshot-button\" data-history-closure-acceptance-package>"));
        assertEquals(2, countOccurrences(closureSection, "data-history-closure-acceptance-export-summary"));
        assertEquals(2, countOccurrences(closureSection, "data-history-closure-acceptance-package"));
    }

    @Test
    void governanceAcceptanceAndGeneratedIssueActionsRequireDedicatedMenus() throws Exception {
        mockMvc.perform(post("/api/workbench/data-governance/tasks/refresh?orgCode=001&limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Salary data governance permission is required")));

        mockMvc.perform(get("/api/workbench/data-governance/scan.csv?orgCode=001&limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Salary data governance permission is required")));

        mockMvc.perform(post("/api/workbench/generated-timeline-issues/generated-timeline-unit-test/retest")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Salary trial permission is required")));

        mockMvc.perform(get("/api/workbench/migration-acceptance")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, WORKBENCH_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Salary acceptance permission is required")));

        mockMvc.perform(get("/api/workbench/migration-acceptance/runs/MIG-ACC-UNIT.csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, WORKBENCH_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Salary acceptance permission is required")));
    }

    @Test
    void salaryTodoCacheCanBeMarkedDirtyByBaseDataChange() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());

        workbenchService.markSalaryTodoCacheDirtyForDataChange("base data changed: dryzwbh");

        String status = jdbcTemplate.queryForObject("""
                SELECT cache_status
                FROM salary_todo_cache_meta
                WHERE cache_key = 'salary-todo'
                """, String.class);
        org.junit.jupiter.api.Assertions.assertEquals("DIRTY", status);
        assertAudit("workbench", "salary-todo-cache-dirty", "SALARY_TODO_CACHE", "ALL", "system", "dryzwbh");
    }

    @Test
    void personBaseChangeRegistrationMarksSalaryTodoCacheDirty() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/persons/001-00055/base-changes")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dataType":"dryzwbh","changeYear":2026,"changeMonth":1,"sourceTable":"dryzwbh","sourceId":"unit-test","summary":"unit-test base change"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"dataType\":\"dryzwbh\"")))
                .andExpect(content().string(containsString("\"summary\":\"unit-test base change\"")));

        String cacheStatus = jdbcTemplate.queryForObject("""
                SELECT cache_status
                FROM salary_todo_cache_meta
                WHERE cache_key = 'salary-todo'
                """, String.class);
        org.junit.jupiter.api.Assertions.assertEquals("DIRTY", cacheStatus);
        assertAudit("person", "person-base-change", "PERSON", "001-00055", ORG_USER, "unit-test base change");
        assertAudit("workbench", "salary-todo-cache-dirty", "SALARY_TODO_CACHE", "ALL", ORG_USER, "001-00055");

        mockMvc.perform(get("/api/persons/001-00055/base-changes?limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("unit-test base change")));

        mockMvc.perform(post("/api/persons/00806-00868/base-changes")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dataType":"dryzwbh","summary":"unit-test denied base change"}
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void workbenchUserStatePersistsPerCurrentUser() throws Exception {
        String payload = """
                {"state":{"queueFilter":{"caseNos":["CASE-A","CASE-B"],"autoSelect":true},"selected":[{"caseNo":"CASE-A","personCode":"001-00001","actionCode":"MAINTAIN_AND_RETEST"}]}}
                """;

        mockMvc.perform(put("/api/workbench/user-states/history-plan-queue")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"stateKey\":\"history-plan-queue\"")))
                .andExpect(content().string(containsString("\"CASE-A\"")));

        mockMvc.perform(get("/api/workbench/user-states/history-plan-queue")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"CASE-B\"")))
                .andExpect(content().string(containsString("\"MAINTAIN_AND_RETEST\"")));

        mockMvc.perform(get("/api/workbench/user-states/history-plan-queue")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"state\":{}")));

        mockMvc.perform(delete("/api/workbench/user-states/history-plan-queue")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"state\":{}")));

        mockMvc.perform(get("/api/workbench/user-states/history-plan-queue")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"state\":{}")));
    }

    @Test
    void salaryTodoItemIncludesLatestBaseChangeSummary() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());

        jdbcTemplate.update("DELETE FROM salary_todo_candidate_cache WHERE work_item_id = 'tmp-test-todo-latest-change'");
        mockMvc.perform(post("/api/persons/001-00055/base-changes")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dataType":"dryzwbh","changeYear":2026,"changeMonth":1,"sourceTable":"dryzwbh","sourceId":"unit-test","summary":"unit-test latest base summary"}
                                """))
                .andExpect(status().isOk());

        jdbcTemplate.update("""
                INSERT INTO salary_todo_candidate_cache(work_item_id, source, source_id, person_code, org_code,
                                                        person_no, person_name, event_year, event_month, change_type, note)
                VALUES ('tmp-test-todo-latest-change', 'dryzwbh', 'unit-test', '001-00055', '001',
                        '00055', 'Unit Test', 2026, 1, 'unit-test-change', 'unit-test todo note')
                ON DUPLICATE KEY UPDATE note = VALUES(note)
                """);

        mockMvc.perform(get("/api/workbench/items?status=TODO&keyword=unit-test todo note&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"id\":\"tmp-test-todo-latest-change\"")))
                .andExpect(content().string(containsString("unit-test todo note")))
                .andExpect(content().string(containsString("unit-test latest base summary")));

        mockMvc.perform(get("/api/workbench/items?status=TODO&keyword=unit-test latest base summary&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"id\":\"tmp-test-todo-latest-change\"")))
                .andExpect(content().string(containsString("unit-test latest base summary")));

        mockMvc.perform(post("/api/workbench/salary-cases/preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-todo-latest-change","source":"SALARY_EVENT","businessType":"unit-test-change","personCode":"001-00055","personName":"Unit Test","orgCode":"001","year":2026,"month":1,"title":"unit-test-change","summary":"stale request summary"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"workItemId\":\"tmp-test-todo-latest-change\"")))
                .andExpect(content().string(containsString("unit-test todo note")))
                .andExpect(content().string(containsString("unit-test latest base summary")))
                .andExpect(content().string(not(containsString("stale request summary"))));

        mockMvc.perform(post("/api/workbench/salary-cases/preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-todo-latest-change","source":"dryzwbh","businessType":"unit-test-change","personCode":"001-00055","personName":"Unit Test","orgCode":"001","year":2026,"month":1,"title":"unit-test-change","summary":"raw source request summary"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"source\":\"SALARY_EVENT\"")))
                .andExpect(content().string(containsString("unit-test todo note")))
                .andExpect(content().string(containsString("unit-test latest base summary")))
                .andExpect(content().string(not(containsString("raw source request summary"))));

        jdbcTemplate.update("DELETE FROM salary_todo_candidate_cache WHERE work_item_id = 'tmp-test-todo-latest-change'");
    }

    @Test
    void personPostMaintenanceMarksSalaryTodoCacheDirty() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/persons/001-00055/posts")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postCode":"0190","postName":"unit-test-post","postLevel":"unit-test-level","rankCode":"0190","currentPostCode":"0190","startDate":"2026.01","excludedYears":0,"currentPostFlag":"1","payrollFlag":"UTEST","summary":"unit-test post create"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"postCode\":\"0190\"")))
                .andExpect(content().string(containsString("\"payrollFlag\":\"UTEST\"")));

        Long postId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM dryzwbh
                WHERE dwbm = '001' AND grbm = '00055' AND jsbz = 'UTEST'
                ORDER BY id DESC
                LIMIT 1
                """, Long.class);
        org.junit.jupiter.api.Assertions.assertNotNull(postId);
        assertAudit("person", "person-post-create", "PERSON_POST", String.valueOf(postId), ORG_USER, "001-00055");
        assertAudit("person", "person-base-change", "PERSON", "001-00055", ORG_USER, "unit-test post create");
        assertAudit("workbench", "salary-todo-cache-dirty", "SALARY_TODO_CACHE", "ALL", ORG_USER, "001-00055");

        String cacheStatus = jdbcTemplate.queryForObject("""
                SELECT cache_status
                FROM salary_todo_cache_meta
                WHERE cache_key = 'salary-todo'
                """, String.class);
        org.junit.jupiter.api.Assertions.assertEquals("DIRTY", cacheStatus);

        mockMvc.perform(put("/api/persons/posts/" + postId)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postCode":"0191","postName":"unit-test-post-edit","postLevel":"unit-test-level-edit","rankCode":"0191","currentPostCode":"0191","startDate":"2026-02","excludedYears":1,"currentPostFlag":"","payrollFlag":"UTEST2","summary":"unit-test post update"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"postCode\":\"0191\"")))
                .andExpect(content().string(containsString("\"startDate\":\"2026.02\"")))
                .andExpect(content().string(containsString("\"payrollFlag\":\"UTEST2\"")));

        assertAudit("person", "person-post-update", "PERSON_POST", String.valueOf(postId), ORG_USER, "001-00055");
        assertAudit("person", "person-base-change", "PERSON", "001-00055", ORG_USER, "unit-test post update");

        mockMvc.perform(get("/api/persons/001-00055/posts")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"id\":" + postId)))
                .andExpect(content().string(containsString("\"postCode\":\"0191\"")));

        mockMvc.perform(post("/api/persons/00806-00868/posts")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postCode":"0190","postName":"unit-test denied","startDate":"2026.01","summary":"unit-test denied post"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void personEducationMaintenanceMarksSalaryTodoCacheDirty() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/persons/001-00055/educations")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"educationCode":"23","educationName":"unit-test-edu","school":"unit-test-school","enrollDate":"2025.09","graduationDate":"2026.07","studyYears":1,"educationType":"普通全日制","note":"UTEST","summary":"unit-test education create"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"educationCode\":\"23\"")))
                .andExpect(content().string(containsString("\"note\":\"UTEST\"")));

        Long educationId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM dxl
                WHERE dwbm = '001' AND grbm = '00055' AND bz = 'UTEST'
                ORDER BY id DESC
                LIMIT 1
                """, Long.class);
        org.junit.jupiter.api.Assertions.assertNotNull(educationId);
        assertAudit("person", "person-education-create", "PERSON_EDUCATION", String.valueOf(educationId), ORG_USER, "001-00055");
        assertAudit("person", "person-base-change", "PERSON", "001-00055", ORG_USER, "unit-test education create");
        assertAudit("workbench", "salary-todo-cache-dirty", "SALARY_TODO_CACHE", "ALL", ORG_USER, "001-00055");

        String cacheStatus = jdbcTemplate.queryForObject("""
                SELECT cache_status
                FROM salary_todo_cache_meta
                WHERE cache_key = 'salary-todo'
                """, String.class);
        org.junit.jupiter.api.Assertions.assertEquals("DIRTY", cacheStatus);

        mockMvc.perform(put("/api/persons/educations/" + educationId)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"educationCode":"31","educationName":"unit-test-edu-edit","school":"unit-test-school-edit","enrollDate":"2025-10","graduationDate":"2026-08","studyYears":2,"educationType":"成人教育","note":"UTEST2","summary":"unit-test education update"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"educationCode\":\"31\"")))
                .andExpect(content().string(containsString("\"graduationDate\":\"2026.08\"")))
                .andExpect(content().string(containsString("\"note\":\"UTEST2\"")));

        assertAudit("person", "person-education-update", "PERSON_EDUCATION", String.valueOf(educationId), ORG_USER, "001-00055");
        assertAudit("person", "person-base-change", "PERSON", "001-00055", ORG_USER, "unit-test education update");

        mockMvc.perform(get("/api/persons/001-00055/educations")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"id\":" + educationId)))
                .andExpect(content().string(containsString("\"educationCode\":\"31\"")));

        mockMvc.perform(post("/api/persons/00806-00868/educations")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"educationCode":"23","educationName":"unit-test denied","graduationDate":"2026.07","summary":"unit-test denied education"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void personAssessmentMaintenanceMarksSalaryTodoCacheDirty() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/persons/001-00055/assessments")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"year":"2098","result":"UTEST","summary":"unit-test assessment create"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"year\":\"2098\"")))
                .andExpect(content().string(containsString("\"result\":\"UTEST\"")));

        Long assessmentId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM dndkh
                WHERE dwbm = '001' AND grbm = '00055' AND khnd = '2098'
                ORDER BY id DESC
                LIMIT 1
                """, Long.class);
        org.junit.jupiter.api.Assertions.assertNotNull(assessmentId);
        assertAudit("person", "person-assessment-create", "PERSON_ASSESSMENT", String.valueOf(assessmentId), ORG_USER, "001-00055");
        assertAudit("person", "person-base-change", "PERSON", "001-00055", ORG_USER, "unit-test assessment create");
        assertAudit("workbench", "salary-todo-cache-dirty", "SALARY_TODO_CACHE", "ALL", ORG_USER, "001-00055");

        String cacheStatus = jdbcTemplate.queryForObject("""
                SELECT cache_status
                FROM salary_todo_cache_meta
                WHERE cache_key = 'salary-todo'
                """, String.class);
        org.junit.jupiter.api.Assertions.assertEquals("DIRTY", cacheStatus);

        mockMvc.perform(put("/api/persons/assessments/" + assessmentId)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"year":"2098","result":"UTEST2","summary":"unit-test assessment update"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"year\":\"2098\"")))
                .andExpect(content().string(containsString("\"result\":\"UTEST2\"")));

        assertAudit("person", "person-assessment-update", "PERSON_ASSESSMENT", String.valueOf(assessmentId), ORG_USER, "001-00055");
        assertAudit("person", "person-base-change", "PERSON", "001-00055", ORG_USER, "unit-test assessment update");

        mockMvc.perform(get("/api/persons/001-00055/assessments")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"id\":" + assessmentId)))
                .andExpect(content().string(containsString("\"result\":\"UTEST2\"")));

        mockMvc.perform(post("/api/persons/00806-00868/assessments")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"year":"2098","result":"合格","summary":"unit-test denied assessment"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void personBaseInfoMaintenanceMarksSalaryTodoCacheDirty() throws Exception {
        createTemporaryPerson();
        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/persons/001-UT001/base-info")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personCategory":"unit-test","organizationType":"23","postCategory":"unit-post","workStartDate":"2020.01","joinOrgDate":"2021.02","teacherNurseStartDate":"2022.03","teacherNurseFixedYears":1,"educationCode":"23","education":"unit-edu","rankCode":"2306","currentPost":"unit-current-post","postLevel":"unit-level","postStartDate":"2023.04","summary":"unit-test base info update"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"personCode\":\"001-UT001\"")))
                .andExpect(content().string(containsString("\"personCategory\":\"unit-test\"")))
                .andExpect(content().string(containsString("\"postStartDate\":\"2023.04\"")));

        assertAudit("person", "person-base-info-update", "PERSON", "001-UT001", ORG_USER, "unit-test base info update");
        assertAudit("person", "person-base-change", "PERSON", "001-UT001", ORG_USER, "unit-test base info update");
        assertAudit("workbench", "salary-todo-cache-dirty", "SALARY_TODO_CACHE", "ALL", ORG_USER, "001-UT001");

        String cacheStatus = jdbcTemplate.queryForObject("""
                SELECT cache_status
                FROM salary_todo_cache_meta
                WHERE cache_key = 'salary-todo'
                """, String.class);
        org.junit.jupiter.api.Assertions.assertEquals("DIRTY", cacheStatus);

        mockMvc.perform(get("/api/persons/001-UT001/base-info")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"teacherNurseStartDate\":\"2022.03\"")));

        mockMvc.perform(get("/api/persons/001-UT001/base-status")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"personCode\":\"001-UT001\"")))
                .andExpect(content().string(containsString("\"latestChangeType\":\"dryjbxx\"")))
                .andExpect(content().string(containsString("\"latestChangeSummary\":\"unit-test base info update\"")))
                .andExpect(content().string(containsString("\"todoCacheStatus\":\"DIRTY\"")));

        mockMvc.perform(put("/api/persons/00806-00868/base-info")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personCategory":"denied","summary":"unit-test denied base info"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/persons/00806-00868/base-status")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void workbenchItemsRespectOrganizationScope() throws Exception {
        mockMvc.perform(get("/api/workbench/items?status=DONE&keyword=001-&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"orgCode\":\"001")))
                .andExpect(content().string(not(containsString("\"orgCode\":\"00806\""))));

        mockMvc.perform(get("/api/workbench/items?status=DONE&keyword=00806-&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")))
                .andExpect(content().string(containsString("\"items\":[]")));
    }

    @Test
    void salaryTodoCanBeCompletedIntoWorkbenchDoneCase() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-cases/preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-preview","source":"SALARY_EVENT","businessType":"姝ｅ父妗ｆ","personCode":"001-00055","personName":"娴嬭瘯浜哄憳","orgCode":"001","year":2026,"month":1,"title":"姝ｅ父妗ｆ鍔炵悊","summary":"unit-test salary case preview"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"workItemId\":\"tmp-test-salary-case-preview\"")))
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"trialStatus\":")))
                .andExpect(content().string(containsString("\"trialChanges\":")));

        org.junit.jupiter.api.Assertions.assertEquals(0, countBusinessCase("tmp-test-salary-case-preview"),
                "Preview must not create salary business case.");
        mockMvc.perform(post("/api/workbench/salary-cases")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-requires-force","source":"SALARY_EVENT","businessType":"normal-grade","personCode":"001-00055","personName":"Force Test","orgCode":"001","year":2026,"month":1,"title":"Force Test","summary":"unit-test salary case requires force"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));
        mockMvc.perform(post("/api/workbench/salary-cases")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-missing-force-reason","source":"SALARY_EVENT","businessType":"normal-grade","personCode":"001-00055","personName":"Force Test","orgCode":"001","year":2026,"month":1,"title":"Force Test","summary":"unit-test missing force reason","force":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        mockMvc.perform(post("/api/workbench/salary-cases")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-001","source":"SALARY_EVENT","businessType":"正常档次","personCode":"001-00055","personName":"测试人员","orgCode":"001","year":2026,"month":1,"title":"正常档次办理","summary":"unit-test salary case done","force":true,"forceReason":"unit-test force reason"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"source\":\"SALARY_CASE\"")))
                .andExpect(content().string(containsString("\"status\":\"DONE\"")))
                .andExpect(content().string(containsString("\"workflowStatus\":\"HISTORY_BLOCKED\"")))
                .andExpect(content().string(containsString("unit-test salary case done")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"source\":\"SALARY_CASE\"")))
                .andExpect(content().string(containsString("\"trialStatus\":\"ERROR\"")))
                .andExpect(content().string(containsString("\"workflowStatus\":\"HISTORY_BLOCKED\"")))
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("unit-test salary case done")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&trialStatus=ERROR&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"trialStatus\":\"ERROR\"")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&trialStatus=MATCH&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")))
                .andExpect(content().string(containsString("\"items\":[]")));

        mockMvc.perform(get("/api/workbench/summary")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SALARY_TRIAL_ERROR")))
                .andExpect(content().string(containsString("SALARY_TRIAL_DIFFERENT")))
                .andExpect(content().string(containsString("SALARY_REVIEW_PENDING")));

        mockMvc.perform(get("/api/workbench/summary")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("HISTORY_PLAN_PREPARED")))
                .andExpect(content().string(containsString("HISTORY_PLAN_EXECUTED")))
                .andExpect(content().string(containsString("HISTORY_PLAN_ROLLED_BACK")))
                .andExpect(content().string(containsString("HISTORY_PLAN_BLOCKED")))
                .andExpect(content().string(containsString("HISTORY_PLAN_REVIEW_PENDING")));

        mockMvc.perform(get("/api/workbench/history-write-review-ledger?comparisonStatus=MISMATCHED&reviewStatus=PENDING&limit=20")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\"")))
                .andExpect(content().string(containsString("\"pending\"")))
                .andExpect(content().string(containsString("\"byReviewCategory\"")))
                .andExpect(content().string(containsString("\"topMismatchFields\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans?comparisonStatus=MISMATCHED&mismatchField=hj2&limit=20")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/workbench/items?status=TODO&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")))
                .andExpect(content().string(containsString("\"items\":[]")));

        assertAudit("workbench", "salary-case-done", "SALARY_CASE", caseNo(CASE_WORK_ITEM), SCOPED_WORKBENCH_USER, "001-00055");
        assertTrialSnapshot(CASE_WORK_ITEM);
        assertBusinessCaseSnapshot(CASE_WORK_ITEM);

        String caseNo = caseNo(CASE_WORK_ITEM);
        assertHistoryWritePlan(caseNo, CASE_WORK_ITEM);
        assertAudit("workbench", "history-write-auto-preview", "SALARY_CASE", caseNo, SCOPED_WORKBENCH_USER, "status=BLOCKED");
        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"caseNo\":\"" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"workItemId\":\"" + CASE_WORK_ITEM + "\"")))
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"handledBy\":\"" + SCOPED_WORKBENCH_USER + "\"")))
                .andExpect(content().string(containsString("\"trialStatus\":")))
                .andExpect(content().string(containsString("\"trialSummary\":")))
                .andExpect(content().string(containsString("\"trialBaselineTotal\":")))
                .andExpect(content().string(containsString("\"trialCalculatedTotal\":")))
                .andExpect(content().string(containsString("\"trialExpectedTotal\":")))
                .andExpect(content().string(containsString("\"trialChanges\":")))
                .andExpect(content().string(containsString("\"salaryItems\":")))
                .andExpect(content().string(containsString("\"workflowStatus\":\"HISTORY_BLOCKED\"")))
                .andExpect(content().string(containsString("\"closureStatus\"")))
                .andExpect(content().string(containsString("\"status\":\"BLOCKED\"")))
                .andExpect(content().string(containsString("\"nextStep\"")))
                .andExpect(content().string(containsString("\"nextActions\"")))
                .andExpect(content().string(containsString("\"code\":\"VIEW_HISTORY_PLAN\"")))
                .andExpect(content().string(containsString("\"code\":\"history-write\"")))
                .andExpect(content().string(containsString("\"audits\":")))
                .andExpect(content().string(containsString("\"snapshotExists\":true")))
                .andExpect(content().string(containsString("\"snapshotBy\":\"" + SCOPED_WORKBENCH_USER + "\"")))
                .andExpect(content().string(containsString("\"snapshotAt\":")))
                .andExpect(content().string(containsString("salary-case-done")))
                .andExpect(content().string(containsString("history-write-auto-preview")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"PENDING\"")))
                .andExpect(content().string(containsString("\"forceReason\":\"unit-test force reason\"")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/snapshot")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"caseNo\":\"" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"workItemId\":\"" + CASE_WORK_ITEM + "\"")))
                .andExpect(content().string(containsString("\"snapshotBy\":\"" + SCOPED_WORKBENCH_USER + "\"")))
                .andExpect(content().string(containsString("\"salaryItems\":")))
                .andExpect(content().string(containsString("\"snapshotJson\":")))
                .andExpect(content().string(containsString("trialCalculatedTotal")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"caseNo\":\"" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"workItemId\":\"" + CASE_WORK_ITEM + "\"")))
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"status\":\"BLOCKED\"")))
                .andExpect(content().string(containsString("\"writable\":false")))
                .andExpect(content().string(containsString("\"writePlanId\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("trial risk must be reviewed")))
                .andExpect(content().string(containsString("\"sidPlan\":")))
                .andExpect(content().string(containsString("\"fields\":")))
                .andExpect(content().string(containsString("\"issues\":")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-confirm")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"executable\":false")))
                .andExpect(content().string(containsString("\"confirmMessage\"")))
                .andExpect(content().string(containsString("trial risk must be reviewed")));

        String blockedBatchSafetyToken = batchSafetyToken("status=PREPARED&keyword=" + caseNo + "&limit=5");
        mockMvc.perform(post("/api/workbench/history-write-plans/batch-execute?status=PREPARED&keyword=" + caseNo + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"safetyToken\":\"" + blockedBatchSafetyToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"batchNo\":\"HWB-WRITE-")))
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"success\":0")))
                .andExpect(content().string(containsString("\"failed\":0")))
                .andExpect(content().string(containsString("\"skipped\":1")))
                .andExpect(content().string(containsString("\"status\":\"SKIPPED\"")))
                .andExpect(content().string(containsString("trial risk must be reviewed")));

        String selectedPreviewBody = mockMvc.perform(post("/api/workbench/history-write-plans/selected-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNos\":[\"" + caseNo + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"blocked\":1")))
                .andExpect(content().string(containsString("trial risk must be reviewed")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String selectedSafetyToken = jsonString(selectedPreviewBody, "safetyToken");

        mockMvc.perform(post("/api/workbench/history-write-plans/selected-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNos\":[\"" + caseNo + "\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Batch write safety token is required")));

        mockMvc.perform(post("/api/workbench/history-write-plans/selected-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNos\":[\"" + caseNo + "\"],\"safetyToken\":\"" + selectedSafetyToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"batchNo\":\"HWB-WRITE-")))
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"success\":0")))
                .andExpect(content().string(containsString("\"failed\":0")))
                .andExpect(content().string(containsString("\"skipped\":1")))
                .andExpect(content().string(containsString("\"status\":\"SKIPPED\"")))
                .andExpect(content().string(containsString("trial risk must be reviewed")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"workflowStatus\":\"HISTORY_BLOCKED\"")))
                .andExpect(content().string(containsString("\"closureStatus\":\"BLOCKED\"")))
                .andExpect(content().string(containsString("\"nextActionCode\":\"VIEW_HISTORY_PLAN\"")))
                .andExpect(content().string(containsString("\"closureMessage\":\"")))
                .andExpect(content().string(containsString("\u5386\u53f2\u5199\u5165\u5b58\u5728\u963b\u65ad")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&workflowStatus=HISTORY_BLOCKED&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"workflowStatus\":\"HISTORY_BLOCKED\"")));
        mockMvc.perform(get("/api/workbench/items?status=DONE&closureStatus=BLOCKED&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"closureStatus\":\"BLOCKED\"")));
        mockMvc.perform(get("/api/workbench/items?status=DONE&nextAction=VIEW_HISTORY_PLAN&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"nextActionCode\":\"VIEW_HISTORY_PLAN\"")));
        mockMvc.perform(get("/api/workbench/items.csv?status=DONE&nextAction=VIEW_HISTORY_PLAN&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u4e0b\u4e00\u6b65")))
                .andExpect(content().string(containsString("\u67e5\u770b\u5199\u5165\u8ba1\u5212")));
        mockMvc.perform(get("/api/workbench/summary")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"source\":\"SALARY_CLOSURE\"")))
                .andExpect(content().string(containsString("\"closureStatus\":\"BLOCKED\"")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&workflowStatus=HISTORY_WRITTEN&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("History write preview is not writable")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&reviewStatus=PENDING&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"PENDING\"")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/review")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/review")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reviewReason":"unit-test review reason"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"reviewStatus\":\"REVIEWED\"")))
                .andExpect(content().string(containsString("\"reviewReason\":\"unit-test review reason\"")))
                .andExpect(content().string(containsString("salary-case-review")));
        assertAudit("workbench", "history-write-auto-preview", "SALARY_CASE", caseNo, SCOPED_WORKBENCH_USER, "status=");
        insertPrintedApprovalReport(caseNo);

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-confirm")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"executable\":true")))
                .andExpect(content().string(not(containsString("trial risk must be reviewed"))));

        mockMvc.perform(get("/api/workbench/items?status=DONE&reviewStatus=REVIEWED&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"REVIEWED\"")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&reviewStatus=PENDING&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")))
                .andExpect(content().string(containsString("\"items\":[]")));

        assertAudit("workbench", "salary-case-review", "SALARY_CASE", caseNo, SCOPED_WORKBENCH_USER, "unit-test review reason");

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/cancel")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/cancel")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cancelReason":"unit-test cancel reason"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"caseNo\":\"" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"status\":\"CANCELLED\"")))
                .andExpect(content().string(containsString("\"cancelReason\":\"unit-test cancel reason\"")));

        Map<String, Object> cancelledPlan = jdbcTemplate.queryForMap("""
                SELECT preview_status, writable, plan_status, execution_result, execution_message
                FROM salary_history_write_plan
                WHERE case_no = ?
                LIMIT 1
                """, caseNo);
        org.junit.jupiter.api.Assertions.assertEquals("BLOCKED", String.valueOf(cancelledPlan.get("preview_status")));
        org.junit.jupiter.api.Assertions.assertEquals(0, ((Number) cancelledPlan.get("writable")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals("BLOCKED", String.valueOf(cancelledPlan.get("plan_status")));
        org.junit.jupiter.api.Assertions.assertEquals("BLOCKED", String.valueOf(cancelledPlan.get("execution_result")));
        org.junit.jupiter.api.Assertions.assertTrue(String.valueOf(cancelledPlan.get("execution_message")).contains("unit-test cancel reason"));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-confirm")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"executable\":false")))
                .andExpect(content().string(containsString("only DONE salary business cases can be written to history")));

        Map<String, Object> cancelledPlanAfterConfirm = jdbcTemplate.queryForMap("""
                SELECT preview_status, writable, plan_status
                FROM salary_history_write_plan
                WHERE case_no = ?
                LIMIT 1
                """, caseNo);
        org.junit.jupiter.api.Assertions.assertEquals("BLOCKED", String.valueOf(cancelledPlanAfterConfirm.get("preview_status")));
        org.junit.jupiter.api.Assertions.assertEquals(0, ((Number) cancelledPlanAfterConfirm.get("writable")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals("BLOCKED", String.valueOf(cancelledPlanAfterConfirm.get("plan_status")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("History write preview is not writable")));

        String cancelledBatchSafetyToken = batchSafetyToken("status=BLOCKED&keyword=" + caseNo + "&limit=5");
        mockMvc.perform(post("/api/workbench/history-write-plans/batch-execute?status=BLOCKED&keyword=" + caseNo + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"safetyToken\":\"" + cancelledBatchSafetyToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"success\":0")))
                .andExpect(content().string(containsString("\"failed\":0")))
                .andExpect(content().string(containsString("\"skipped\":1")))
                .andExpect(content().string(containsString("\"status\":\"SKIPPED\"")))
                .andExpect(content().string(containsString("salary case status is CANCELLED")));

        String cancelledSelectedSafetyToken = selectedSafetyToken(caseNo);
        mockMvc.perform(post("/api/workbench/history-write-plans/selected-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNos\":[\"" + caseNo + "\"],\"safetyToken\":\"" + cancelledSelectedSafetyToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"success\":0")))
                .andExpect(content().string(containsString("\"failed\":0")))
                .andExpect(content().string(containsString("\"skipped\":1")))
                .andExpect(content().string(containsString("\"status\":\"SKIPPED\"")))
                .andExpect(content().string(containsString("salary case status is CANCELLED")));

        String cancelledSelectedRollbackPreviewBody = mockMvc.perform(post("/api/workbench/history-write-plans/selected-rollback-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNos\":[\"" + caseNo + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"rollbackable\":0")))
                .andExpect(content().string(containsString("\"blocked\":1")))
                .andExpect(content().string(containsString("\"safetyToken\"")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String cancelledSelectedRollbackSafetyToken = jsonString(cancelledSelectedRollbackPreviewBody, "safetyToken");

        mockMvc.perform(post("/api/workbench/history-write-plans/selected-rollback")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNos\":[\"" + caseNo + "\"],\"safetyToken\":\"" + cancelledSelectedRollbackSafetyToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"success\":0")))
                .andExpect(content().string(containsString("\"failed\":0")))
                .andExpect(content().string(containsString("\"skipped\":1")))
                .andExpect(content().string(containsString("\"status\":\"SKIPPED\"")))
                .andExpect(content().string(containsString("plan status is BLOCKED")));

        Map<String, Object> cancelledPlanAfterExecute = jdbcTemplate.queryForMap("""
                SELECT preview_status, writable, plan_status, execution_result
                FROM salary_history_write_plan
                WHERE case_no = ?
                LIMIT 1
                """, caseNo);
        org.junit.jupiter.api.Assertions.assertEquals("BLOCKED", String.valueOf(cancelledPlanAfterExecute.get("preview_status")));
        org.junit.jupiter.api.Assertions.assertEquals(0, ((Number) cancelledPlanAfterExecute.get("writable")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals("BLOCKED", String.valueOf(cancelledPlanAfterExecute.get("plan_status")));
        org.junit.jupiter.api.Assertions.assertEquals("BLOCKED", String.valueOf(cancelledPlanAfterExecute.get("execution_result")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")))
                .andExpect(content().string(containsString("\"items\":[]")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&caseStatus=CANCELLED&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"status\":\"CANCELLED\"")))
                .andExpect(content().string(containsString("\"workflowStatus\":\"CASE_CANCELLED\"")))
                .andExpect(content().string(containsString("unit-test cancel reason")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&caseStatus=ALL&workflowStatus=CASE_CANCELLED&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"workflowStatus\":\"CASE_CANCELLED\"")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&caseStatus=ALL&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"status\":\"CANCELLED\"")));

        mockMvc.perform(get("/api/workbench/items.csv?status=DONE&caseStatus=CANCELLED&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u529e\u7406\u8fdb\u5ea6")))
                .andExpect(content().string(containsString("\u8bd5\u7b97\u72b6\u6001")))
                .andExpect(content().string(containsString("\u8bd5\u7b97\u5f02\u5e38")))
                .andExpect(content().string(containsString("\u5df2\u64a4\u56de")))
                .andExpect(content().string(not(containsString("\"CANCELLED\""))));

        assertAudit("workbench", "salary-case-cancel", "SALARY_CASE", caseNo, SCOPED_WORKBENCH_USER, "unit-test cancel reason");
        assertAudit("workbench", "history-write-plan-cancel-blocked", "SALARY_CASE", caseNo, SCOPED_WORKBENCH_USER, "unit-test cancel reason");

        mockMvc.perform(post("/api/workbench/salary-cases")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-001","source":"SALARY_EVENT","businessType":"姝ｅ父妗ｆ","personCode":"001-00055","personName":"娴嬭瘯浜哄憳","orgCode":"001","year":2026,"month":1,"title":"姝ｅ父妗ｆ鍔炵悊","summary":"unit-test salary case done","force":true,"forceReason":"unit-test force reason again"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"DONE\"")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"DONE\"")))
                .andExpect(content().string(containsString("\"cancelReason\":\"\"")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"PENDING\"")))
                .andExpect(content().string(containsString("\"reviewReason\":\"\"")))
                .andExpect(content().string(containsString("salary-case-cancel")))
                .andExpect(content().string(containsString("unit-test cancel reason")))
                .andExpect(content().string(containsString("\"forceReason\":\"unit-test force reason again\"")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"status\":\"DONE\"")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&caseStatus=CANCELLED&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")))
                .andExpect(content().string(containsString("\"items\":[]")));
    }

    @Test
    void salaryTodoRefreshExcludesBlockedBasePostCandidates() throws Exception {
        String sourceId = insertTemporaryPostChange("2026.01", "0701", "UTEST");

        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertEquals(1, countTodoBySourceId(sourceId),
                "controlled dryzwbh row should enter salary todo before blockers are added");

        jdbcTemplate.update("""
                INSERT INTO salary_business_case(case_no, work_item_id, source, status, business_type,
                                                 person_code, person_name, org_code, event_year, event_month,
                                                 title, summary)
                VALUES ('GZ-TMP-TODO-DUP', ?, 'SALARY_EVENT', 'TODO', '职务变化',
                        '001-00055', 'Todo Duplicate', '001', 2026, 2, '职务变化', 'unit-test duplicate todo blocker')
                ON DUPLICATE KEY UPDATE status = 'TODO', summary = VALUES(summary)
                """, TODO_DUPLICATE_CASE_WORK_ITEM);

        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertEquals(0, countTodoBySourceId(sourceId),
                "non-cancelled salary business case should block duplicate salary todo");

        jdbcTemplate.update("DELETE FROM salary_business_case WHERE work_item_id = ?", TODO_DUPLICATE_CASE_WORK_ITEM);
        insertTemporaryLaterHistory();

        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertEquals(0, countTodoBySourceId(sourceId),
                "later hisbase rows should block earlier-month generated salary todo");

        jdbcTemplate.update("DELETE FROM hisbase WHERE id = ?", TODO_LATER_HISTORY_ID);
        String samePostSourceId = insertTemporaryPostChange("2026.01", "2306", "UTEST2");

        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertEquals(0, countTodoBySourceId(samePostSourceId),
                "dryzwbh target post already matching latest salary post should not become a salary todo");
    }

    @Test
    void historyWriteExecuteCreatesHisbaseRowAndUpdatesSidChain() throws Exception {
        insertTemporaryHistoryTemplate();
        String caseNo = HISTORY_WRITE_CASE_NO;

        mockMvc.perform(post("/api/workbench/salary-cases/preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"%s","source":"SALARY_EVENT","businessType":"测试写入","personCode":"001-00055","personName":"History Write","orgCode":"001","year":2099,"month":1,"title":"History Write","summary":"unit-test history write success"}
                                """.formatted(HISTORY_WRITE_WORK_ITEM)))
                .andExpect(status().isOk());
        jdbcTemplate.update("""
                INSERT INTO salary_business_case(case_no, work_item_id, source, status, business_type,
                                                 person_code, person_name, org_code, event_year, event_month,
                                                 title, summary, trial_status, trial_matched,
                                                 trial_baseline_total, trial_calculated_total, trial_expected_total,
                                                 trial_difference, trial_summary, trial_changes_json,
                                                 review_status, handled_by)
                VALUES (?, ?, 'SALARY_EVENT', 'DONE', '测试写入',
                        '001-00055', 'History Write', '001', 2099, 1,
                        'History Write', 'unit-test history write success', 'MATCH', 1,
                        1200, 1290, 1290, 0, 'unit-test history write success', '[]',
                        'PENDING', ?)
                """, caseNo, HISTORY_WRITE_WORK_ITEM, SCOPED_WORKBENCH_USER);
        ensureSnapshotTableForTest();
        jdbcTemplate.update("""
                INSERT INTO salary_business_case_snapshot(case_no, work_item_id, person_code, org_code,
                                                          event_year, event_month, business_type, trial_status,
                                                          trial_matched, trial_difference, trial_baseline_total,
                                                          trial_calculated_total, trial_expected_total,
                                                          trial_changes_json, salary_items_json, snapshot_json,
                                                          snapshot_by)
                VALUES (?, ?, '001-00055', '001', 2099, 1, '测试写入', 'MATCH',
                        1, 0, 1200, 1290, 1290,
                        '[]',
                        '[{"itemCode":"JCGZ2","itemName":"基础工资","amount":1234,"ruleNote":"unit-test"},{"itemCode":"GLGZ2","itemName":"工龄工资","amount":61,"ruleNote":"unit-test"}]',
                        '{"workItemId":"tmp-test-history-write-success","trialCalculatedTotal":1290,"salaryItems":[{"itemCode":"JCGZ2","amount":1234},{"itemCode":"GLGZ2","amount":61}]}',
                        ?)
                """, caseNo, HISTORY_WRITE_WORK_ITEM, SCOPED_WORKBENCH_USER);
        insertPrintedApprovalReport(caseNo);

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"READY\"")))
                .andExpect(content().string(containsString("\"writable\":true")))
                .andExpect(content().string(containsString("\"writePlanId\":\"HWP-" + caseNo + "\"")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-confirm")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"READY\"")))
                .andExpect(content().string(containsString("\"executable\":true")))
                .andExpect(content().string(containsString("\"totalAmount\":1295")))
                .andExpect(content().string(containsString("\"fieldCount\":2")))
                .andExpect(content().string(containsString("\"confirmMessage\"")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-plan")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"planStatus\":\"PREPARED\"")))
                .andExpect(content().string(containsString("\"comparisonStatus\":\"NOT_WRITTEN\"")))
                .andExpect(content().string(containsString("\"comparisonMismatchCount\":0")))
                .andExpect(content().string(containsString("\"previewStatus\":\"READY\"")));

        jdbcTemplate.update("""
                INSERT INTO salary_history_write_plan(plan_no, case_no, work_item_id, person_code, org_code,
                                                      event_year, event_month, business_type, preview_status,
                                                      writable, sid_plan, preview_json, plan_status, prepared_by)
                VALUES ('HWP-GZ-TMP-HISTORY-DENIED', 'GZ-TMP-HISTORY-DENIED', 'tmp-test-salary-case-denied',
                        '00806-00868', '00806', 2099, 1, 'unit-test denied history',
                        'READY', 1, 'unit-test denied sid plan', '{}', 'PREPARED', 'admin')
                """);

        mockMvc.perform(get("/api/workbench/history-write-plans?status=PREPARED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"comparisonStatus\":\"NOT_WRITTEN\"")))
                .andExpect(content().string(containsString("\"nextActionCode\":\"WRITE_HISTORY\"")))
                .andExpect(content().string(not(containsString("HWP-GZ-TMP-HISTORY-DENIED"))))
                .andExpect(content().string(not(containsString("00806-00868"))));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=PREPARED&actionCode=WRITE_HISTORY&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"nextActionCode\":\"WRITE_HISTORY\"")))
                .andExpect(content().string(not(containsString("HWP-GZ-TMP-HISTORY-DENIED"))));
        mockMvc.perform(get("/api/workbench/history-write-plans.csv?status=PREPARED&actionCode=WRITE_HISTORY&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("HWP-" + caseNo)))
                .andExpect(content().string(containsString("WRITE_HISTORY")))
                .andExpect(content().string(not(containsString("HWP-GZ-TMP-HISTORY-DENIED"))));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=PREPARED&comparisonStatus=NOT_WRITTEN&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"comparisonStatus\":\"NOT_WRITTEN\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans.csv?status=PREPARED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u8ba1\u5212\u53f7")))
                .andExpect(content().string(containsString("\u5bf9\u7167\u72b6\u6001")))
                .andExpect(content().string(containsString("\u5dee\u5f02\u6570\u91cf")))
                .andExpect(content().string(containsString("HWP-" + caseNo)))
                .andExpect(content().string(containsString("001-00055")))
                .andExpect(content().string(containsString("\u672a\u5199\u5165")))
                .andExpect(content().string(not(containsString("HWP-GZ-TMP-HISTORY-DENIED"))))
                .andExpect(content().string(not(containsString("00806-00868"))));
        mockMvc.perform(get("/api/workbench/history-write-plans.csv?status=PREPARED&actionCode=WRITE_HISTORY&printQueue=PRINTED_READY&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("HWP-" + caseNo)))
                .andExpect(content().string(containsString("001-00055")));
        mockMvc.perform(get("/api/workbench/history-write-plans.csv?status=PREPARED&actionCode=WRITE_HISTORY&printQueue=UNPRINTED_BLOCKED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("HWP-" + caseNo))));
        String planCsvAuditSummary = jdbcTemplate.queryForObject("""
                SELECT summary
                FROM sys_audit_log
                WHERE module_name = 'workbench'
                  AND action_name = 'history-write-plans-csv'
                  AND target_type = 'HISTORY_WRITE_PLAN'
                ORDER BY id DESC
                LIMIT 1
                """, String.class);
        org.junit.jupiter.api.Assertions.assertTrue(planCsvAuditSummary.contains("printQueue=UNPRINTED_BLOCKED"));
        org.junit.jupiter.api.Assertions.assertTrue(planCsvAuditSummary.contains("count=0"));

        mockMvc.perform(get("/api/workbench/items?status=DONE&nextAction=EXECUTE_HISTORY_WRITE&keyword=History Write&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"workflowStatus\":\"HISTORY_READY\"")))
                .andExpect(content().string(containsString("\"nextActionCode\":\"EXECUTE_HISTORY_WRITE\"")));

        String batchPreviewBody = mockMvc.perform(post("/api/workbench/history-write-plans/batch-preview?status=PREPARED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"ready\":1")))
                .andExpect(content().string(containsString("\"blocked\":0")))
                .andExpect(content().string(containsString("\"caseNo\":\"" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String safetyToken = jsonString(batchPreviewBody, "safetyToken");
        String writeSafetyPreviewSummary = jdbcTemplate.queryForObject("""
                SELECT summary
                FROM sys_audit_log
                WHERE module_name = 'workbench'
                  AND action_name = 'history-write-batch-safety-preview'
                  AND target_type = 'HISTORY_WRITE_BATCH_SAFETY'
                  AND summary LIKE '%operation=WRITE%'
                ORDER BY id DESC
                LIMIT 1
                """, String.class);
        org.junit.jupiter.api.Assertions.assertTrue(writeSafetyPreviewSummary.contains("status=PREVIEW_CREATED"));
        org.junit.jupiter.api.Assertions.assertTrue(writeSafetyPreviewSummary.contains("tokenRef="));
        org.junit.jupiter.api.Assertions.assertTrue(writeSafetyPreviewSummary.contains("caseCount=1"));
        org.junit.jupiter.api.Assertions.assertFalse(writeSafetyPreviewSummary.contains(safetyToken));

        mockMvc.perform(post("/api/workbench/history-write-plans/batch-execute?status=PREPARED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Batch write safety token is required")));

        String batchExecuteBody = mockMvc.perform(post("/api/workbench/history-write-plans/batch-execute?status=PREPARED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"safetyToken\":\"" + safetyToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"batchNo\":\"HWB-WRITE-")))
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"success\":1")))
                .andExpect(content().string(containsString("\"failed\":0")))
                .andExpect(content().string(containsString("\"skipped\":0")))
                .andExpect(content().string(containsString("\"status\":\"EXECUTED\"")))
                .andExpect(content().string(containsString("\"writePlanId\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("Inserted hisbase row")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String writeBatchNo = jsonString(batchExecuteBody, "batchNo");
        mockMvc.perform(get("/api/workbench/history-write-batches?queue=SALARY_NEXT_EXECUTE_WRITE&limit=20")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"batchNo\":\"" + writeBatchNo + "\"")))
                .andExpect(content().string(containsString("\"action\":\"history-write-batch-execute\"")));
        mockMvc.perform(get("/api/workbench/history-write-batches?queue=HISTORY_PLAN_ROLLED_BACK&limit=20")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"batchNo\":\"" + writeBatchNo + "\""))));
        mockMvc.perform(get("/api/workbench/history-write-batches.csv?queue=SALARY_NEXT_EXECUTE_WRITE&limit=20")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(writeBatchNo)))
                .andExpect(content().string(containsString("history-write-batch-execute")));
        mockMvc.perform(get("/api/workbench/history-write-batches.csv?queue=HISTORY_PLAN_ROLLED_BACK&limit=20")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(writeBatchNo))));
        String batchCsvAuditSummary = jdbcTemplate.queryForObject("""
                SELECT summary
                FROM sys_audit_log
                WHERE module_name = 'workbench'
                  AND action_name = 'history-write-batch-ledger-csv'
                  AND target_type = 'HISTORY_WRITE_BATCH_LEDGER'
                ORDER BY id DESC
                LIMIT 1
                """, String.class);
        org.junit.jupiter.api.Assertions.assertTrue(batchCsvAuditSummary.contains("queue=HISTORY_PLAN_ROLLED_BACK"));
        mockMvc.perform(get("/api/workbench/history-write-batches/" + writeBatchNo + "/audits.csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(writeBatchNo)));
        mockMvc.perform(get("/api/workbench/history-write-batches/" + writeBatchNo + "/audits")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"history-write-batch-audits-csv\"")))
                .andExpect(content().string(containsString("batchNo=" + writeBatchNo)));
        mockMvc.perform(get("/api/system/audits.csv?module=workbench&targetCode=HISTORY_WRITE_&limit=20")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u6a21\u5757\u7f16\u7801")))
                .andExpect(content().string(containsString("\u52a8\u4f5c\u7f16\u7801")))
                .andExpect(content().string(containsString("\u5bfc\u51fa\u5386\u53f2\u5199\u5165\u8ba1\u5212")))
                .andExpect(content().string(containsString("\u5386\u53f2\u5199\u5165\u6279\u6b21\u53f0\u8d26")))
                .andExpect(content().string(containsString("history-write-plans-csv")));
        String writeSafetyConsumeSummary = jdbcTemplate.queryForObject("""
                SELECT summary
                FROM sys_audit_log
                WHERE module_name = 'workbench'
                  AND action_name = 'history-write-batch-safety-consume'
                  AND target_type = 'HISTORY_WRITE_BATCH_SAFETY'
                  AND summary LIKE '%operation=WRITE%'
                ORDER BY id DESC
                LIMIT 1
                """, String.class);
        org.junit.jupiter.api.Assertions.assertTrue(writeSafetyConsumeSummary.contains("status=CONSUMED"));
        org.junit.jupiter.api.Assertions.assertTrue(writeSafetyConsumeSummary.contains("signatureRef="));
        org.junit.jupiter.api.Assertions.assertFalse(writeSafetyConsumeSummary.contains(safetyToken));

        mockMvc.perform(post("/api/workbench/history-write-plans/batch-execute?status=PREPARED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"safetyToken\":\"" + safetyToken + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Batch write safety token is invalid or already used")));

        Map<String, Object> plan = jdbcTemplate.queryForMap("""
                SELECT plan_status, execution_result, inserted_history_id, previous_history_id, next_history_id
                FROM salary_history_write_plan
                WHERE work_item_id = ?
                LIMIT 1
                """, HISTORY_WRITE_WORK_ITEM);
        org.junit.jupiter.api.Assertions.assertEquals("EXECUTED", String.valueOf(plan.get("plan_status")));
        org.junit.jupiter.api.Assertions.assertEquals("SUCCESS", String.valueOf(plan.get("execution_result")));
        org.junit.jupiter.api.Assertions.assertEquals(HISTORY_WRITE_SOURCE_ID, String.valueOf(plan.get("previous_history_id")));
        org.junit.jupiter.api.Assertions.assertEquals("null", String.valueOf(plan.get("next_history_id")));

        String insertedId = String.valueOf(plan.get("inserted_history_id"));
        Map<String, Object> inserted = jdbcTemplate.queryForMap("""
                SELECT TRIM(id) AS id, TRIM(COALESCE(sid, '')) AS sid, hj2, jcgz2, glgz2
                FROM hisbase
                WHERE id = ?
                  AND dwbm = '001'
                  AND grbm = '00055'
                  AND TRIM(jsnf) = '2099'
                  AND TRIM(jsyf) = '1'
                  AND TRIM(jslb) = '测试写入'
                LIMIT 1
                """, insertedId);
        org.junit.jupiter.api.Assertions.assertEquals(insertedId, String.valueOf(inserted.get("id")));
        org.junit.jupiter.api.Assertions.assertEquals(1295, ((Number) inserted.get("hj2")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals(1234, ((Number) inserted.get("jcgz2")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals(61, ((Number) inserted.get("glgz2")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals("", String.valueOf(inserted.get("sid")));

        String sourceSid = jdbcTemplate.queryForObject("""
                SELECT TRIM(COALESCE(sid, ''))
                FROM hisbase
                WHERE id = ?
                """, String.class, HISTORY_WRITE_SOURCE_ID);
        org.junit.jupiter.api.Assertions.assertEquals(insertedId, sourceSid);

        org.junit.jupiter.api.Assertions.assertEquals(insertedId, String.valueOf(plan.get("inserted_history_id")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"writePlanId\":\"HWP-" + caseNo + "\"")));

        Map<String, Object> planAfterExecutedPreview = jdbcTemplate.queryForMap("""
                SELECT plan_status, execution_result, inserted_history_id
                FROM salary_history_write_plan
                WHERE work_item_id = ?
                LIMIT 1
                """, HISTORY_WRITE_WORK_ITEM);
        org.junit.jupiter.api.Assertions.assertEquals("EXECUTED", String.valueOf(planAfterExecutedPreview.get("plan_status")));
        org.junit.jupiter.api.Assertions.assertEquals("SUCCESS", String.valueOf(planAfterExecutedPreview.get("execution_result")));
        org.junit.jupiter.api.Assertions.assertEquals(insertedId, String.valueOf(planAfterExecutedPreview.get("inserted_history_id")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-plan")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"comparisonStatus\":\"MATCHED\"")));

        jdbcTemplate.update("DELETE FROM salary_report_print_batch_item WHERE case_no = ?", caseNo);
        jdbcTemplate.update("DELETE FROM salary_report_print_batch WHERE batch_no = ?", "UT-PRINT-" + caseNo);
        mockMvc.perform(get("/api/workbench/items?status=DONE&workflowStatus=HISTORY_CLOSED&keyword=History Write&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")));
        insertPrintedApprovalReport(caseNo);

        mockMvc.perform(get("/api/workbench/items?status=DONE&workflowStatus=HISTORY_CLOSED&keyword=History Write&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"workflowStatus\":\"HISTORY_CLOSED\"")))
                .andExpect(content().string(containsString("\"closureStatus\":\"CLOSED\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=EXECUTED&comparisonStatus=MATCHED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"comparisonStatus\":\"MATCHED\"")))
                .andExpect(content().string(containsString("\"comparisonMismatchCount\":0")));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=EXECUTED&comparisonStatus=MATCHED&reviewStatus=PENDING&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(not(containsString("\"comparisonReviewStatus\":\"REVIEWED\""))));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-comparison")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"insertedHistoryId\":\"" + insertedId + "\"")))
                .andExpect(content().string(containsString("\"historyField\":\"jcgz2\"")))
                .andExpect(content().string(containsString("\"expectedAmount\":1234")))
                .andExpect(content().string(containsString("\"actualAmount\":1234")))
                .andExpect(content().string(containsString("\"totalMatched\":true")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"historyWritePlan\"")))
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"planStatus\":\"EXECUTED\"")))
                .andExpect(content().string(containsString("\"executionResult\":\"SUCCESS\"")))
                .andExpect(content().string(containsString("\"insertedHistoryId\":\"" + insertedId + "\"")))
                .andExpect(content().string(containsString("\"reportPrintArchive\"")))
                .andExpect(content().string(containsString("\"printed\":true")))
                .andExpect(content().string(containsString("\"latestBatchNo\":\"UT-PRINT-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"closureStatus\"")))
                .andExpect(content().string(containsString("\"status\":\"CLOSED\"")))
                .andExpect(content().string(containsString("\"closed\":true")))
                .andExpect(content().string(containsString("\"nextStep\":null")))
                .andExpect(content().string(containsString("\"nextActions\":[]")))
                .andExpect(content().string(containsString("\"code\":\"report-print\"")))
                .andExpect(content().string(containsString("\"historyWriteAudits\"")))
                .andExpect(content().string(containsString("\"action\":\"history-write-batch-execute\"")));

        mockMvc.perform(get("/api/reports/print-batches/UT-PRINT-" + caseNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"batchNo\":\"UT-PRINT-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"caseNo\":\"" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"caseStatus\":\"DONE\"")))
                .andExpect(content().string(containsString("\"planStatus\":\"EXECUTED\"")))
                .andExpect(content().string(containsString("\"executionResult\":\"SUCCESS\"")))
                .andExpect(content().string(containsString("\"insertedHistoryId\":\"" + insertedId + "\"")))
                .andExpect(content().string(containsString("\"historyWritten\":1")))
                .andExpect(content().string(containsString("\"closureReady\":1")));

        mockMvc.perform(get("/api/reports/print-batches/UT-PRINT-" + caseNo + ".csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"filter\",\"batchNo\",\"UT-PRINT-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"filter\",\"rows\",\"1\"")))
                .andExpect(content().string(containsString("\"filter\",\"blocked\",\"0\"")))
                .andExpect(content().string(containsString("\u529e\u7406\u72b6\u6001")))
                .andExpect(content().string(containsString("\u5199\u5165\u5386\u53f2ID")))
                .andExpect(content().string(containsString(insertedId)));

        mockMvc.perform(get("/api/reports/print-batches/UT-PRINT-" + caseNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"audits\"")))
                .andExpect(content().string(containsString("\"actionName\":\"report-print-batch-csv\"")))
                .andExpect(content().string(containsString("UT-PRINT-" + caseNo)));

        mockMvc.perform(post("/api/workbench/history-write-plans/report-batch-queue-audit")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"batchNo":"UT-PRINT-%s","label":"unit-test batch queue","caseNos":["%s"]}
                                """.formatted(caseNo, caseNo)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"auditNo\":\"SYS-")))
                .andExpect(content().string(containsString("\"rows\":1")));

        mockMvc.perform(get("/api/reports/print-batches/UT-PRINT-" + caseNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"actionName\":\"history-write-report-batch-queue\"")))
                .andExpect(content().string(containsString("unit-test batch queue")));

        mockMvc.perform(get("/api/reports/print-batches/acceptance-packages.zip?keyword=UT-PRINT-" + caseNo + "&acceptanceStatus=ALL&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, WORKBENCH_USER))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/api/reports/print-batches/UT-PRINT-" + caseNo + "/acceptance-package.zip")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, WORKBENCH_USER))
                .andExpect(status().is4xxClientError());

        var reportPrintBatchBulkAcceptancePackage = mockMvc.perform(get("/api/reports/print-batches/acceptance-packages.zip?keyword=UT-PRINT-" + caseNo + "&acceptanceStatus=ALL&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("report-print-batch-acceptance-packages-bulk-")))
                .andReturn();
        Set<String> reportPrintBatchBulkAcceptanceEntries = new HashSet<>();
        String reportPrintBatchBulkAcceptanceIndex = "";
        String reportPrintBatchBulkAcceptanceMeta = "";
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(reportPrintBatchBulkAcceptancePackage.getResponse().getContentAsByteArray()))) {
            var entry = zip.getNextEntry();
            while (entry != null) {
                reportPrintBatchBulkAcceptanceEntries.add(entry.getName());
                if ("report-print-batch-acceptance-package-index.csv".equals(entry.getName())) {
                    reportPrintBatchBulkAcceptanceIndex = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if (("UT-PRINT-" + caseNo + "/acceptance-package-meta.csv").equals(entry.getName())) {
                    reportPrintBatchBulkAcceptanceMeta = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
                entry = zip.getNextEntry();
            }
        }
        assertTrue(reportPrintBatchBulkAcceptanceEntries.contains("README.txt"));
        assertTrue(reportPrintBatchBulkAcceptanceEntries.contains("report-print-batch-acceptance-package-index.csv"));
        assertTrue(reportPrintBatchBulkAcceptanceEntries.contains("UT-PRINT-" + caseNo + "/README.txt"));
        assertTrue(reportPrintBatchBulkAcceptanceEntries.contains("UT-PRINT-" + caseNo + "/acceptance-package-meta.csv"));
        assertTrue(reportPrintBatchBulkAcceptanceEntries.contains("UT-PRINT-" + caseNo + "/report-print-batch-detail-UT-PRINT-" + caseNo + ".csv"));
        assertTrue(reportPrintBatchBulkAcceptanceIndex.contains("\"filter\",\"acceptanceStatus\",\"ALL\""));
        assertTrue(reportPrintBatchBulkAcceptanceIndex.contains("\"filter\",\"limit\",\"5\""));
        assertTrue(reportPrintBatchBulkAcceptanceIndex.contains("\"filter\",\"rows\",\"1\""));
        assertTrue(reportPrintBatchBulkAcceptanceIndex.contains("UT-PRINT-" + caseNo));
        assertTrue(reportPrintBatchBulkAcceptanceMeta.contains("\"meta\",\"exportNo\",\"bulk-"));
        assertTrue(reportPrintBatchBulkAcceptanceMeta.contains("\"meta\",\"batchNo\",\"UT-PRINT-" + caseNo + "\""));

        mockMvc.perform(get("/api/reports/print-batches/UT-PRINT-" + caseNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"actionName\":\"report-print-batch-acceptance-package-bulk\"")))
                .andExpect(content().string(containsString("\"acceptanceExported\":1")))
                .andExpect(content().string(containsString("\"acceptanceAuditId\":\"SYS-")))
                .andExpect(content().string(containsString("bulkExport=")));

        var reportPrintBatchAcceptancePackage = mockMvc.perform(get("/api/reports/print-batches/UT-PRINT-" + caseNo + "/acceptance-package.zip")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("report-print-batch-acceptance-package-UT-PRINT-" + caseNo + ".zip")))
                .andReturn();
        Set<String> reportPrintBatchAcceptanceEntries = new HashSet<>();
        String reportPrintBatchAcceptanceReadme = "";
        String reportPrintBatchAcceptanceDetail = "";
        String reportPrintBatchAcceptanceAudits = "";
        String reportPrintBatchAcceptanceUnwritten = "";
        String reportPrintBatchAcceptanceMeta = "";
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(reportPrintBatchAcceptancePackage.getResponse().getContentAsByteArray()))) {
            var entry = zip.getNextEntry();
            while (entry != null) {
                reportPrintBatchAcceptanceEntries.add(entry.getName());
                if ("README.txt".equals(entry.getName())) {
                    reportPrintBatchAcceptanceReadme = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if ("acceptance-package-meta.csv".equals(entry.getName())) {
                    reportPrintBatchAcceptanceMeta = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if (("report-print-batch-detail-UT-PRINT-" + caseNo + ".csv").equals(entry.getName())) {
                    reportPrintBatchAcceptanceDetail = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if (("report-print-batch-audits-UT-PRINT-" + caseNo + ".csv").equals(entry.getName())) {
                    reportPrintBatchAcceptanceAudits = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if (("report-print-batch-unwritten-UT-PRINT-" + caseNo + ".csv").equals(entry.getName())) {
                    reportPrintBatchAcceptanceUnwritten = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
                entry = zip.getNextEntry();
            }
        }
        assertTrue(reportPrintBatchAcceptanceEntries.contains("README.txt"));
        assertTrue(reportPrintBatchAcceptanceEntries.contains("acceptance-package-meta.csv"));
        assertTrue(reportPrintBatchAcceptanceEntries.contains("report-print-batch-detail-UT-PRINT-" + caseNo + ".csv"));
        assertTrue(reportPrintBatchAcceptanceEntries.contains("report-print-batch-audits-UT-PRINT-" + caseNo + ".csv"));
        assertTrue(reportPrintBatchAcceptanceEntries.contains("report-print-batch-unwritten-UT-PRINT-" + caseNo + ".csv"));
        assertTrue(reportPrintBatchAcceptanceEntries.contains("report-print-batch-unclosed-UT-PRINT-" + caseNo + ".csv"));
        assertTrue(reportPrintBatchAcceptanceEntries.contains("report-print-batch-blocked-UT-PRINT-" + caseNo + ".csv"));
        assertTrue(reportPrintBatchAcceptanceReadme.contains("打印批次验收包"));
        assertTrue(reportPrintBatchAcceptanceReadme.contains("UT-PRINT-" + caseNo));
        assertTrue(reportPrintBatchAcceptanceMeta.contains("\"meta\",\"exportNo\",\"single-"));
        assertTrue(reportPrintBatchAcceptanceMeta.contains("\"meta\",\"batchNo\",\"UT-PRINT-" + caseNo + "\""));
        assertTrue(reportPrintBatchAcceptanceDetail.contains("\"filter\",\"batchNo\",\"UT-PRINT-" + caseNo + "\""));
        assertTrue(reportPrintBatchAcceptanceDetail.contains("\"filter\",\"rows\",\"1\""));
        assertTrue(reportPrintBatchAcceptanceDetail.contains("\"filter\",\"blocked\",\"0\""));
        assertTrue(reportPrintBatchAcceptanceAudits.contains("\"filter\",\"batchNo\",\"UT-PRINT-" + caseNo + "\""));
        assertTrue(reportPrintBatchAcceptanceAudits.contains("\"filter\",\"rows\","));
        assertTrue(reportPrintBatchAcceptanceUnwritten.contains("\"filter\",\"batchNo\",\"UT-PRINT-" + caseNo + "\""));
        assertTrue(reportPrintBatchAcceptanceUnwritten.contains("\"filter\",\"list\","));
        assertTrue(reportPrintBatchAcceptanceUnwritten.contains("\"filter\",\"rows\","));

        mockMvc.perform(get("/api/reports/print-batches/UT-PRINT-" + caseNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"actionName\":\"report-print-batch-acceptance-package\"")))
                .andExpect(content().string(containsString("exportNo=single-")))
                .andExpect(content().string(containsString("UT-PRINT-" + caseNo)));

        mockMvc.perform(get("/api/reports/audits.csv?action=report-print-batch-acceptance-package&targetCode=UT-PRINT-" + caseNo + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"filter\",\"action\",\"report-print-batch-acceptance-package\"")))
                .andExpect(content().string(containsString("\"filter\",\"target\",\"UT-PRINT-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"filter\",\"limit\",\"5\"")))
                .andExpect(content().string(containsString("report-print-batch-acceptance-package")))
                .andExpect(content().string(containsString("UT-PRINT-" + caseNo)));

        mockMvc.perform(get("/api/reports/print-batches?keyword=UT-PRINT-" + caseNo + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"acceptanceExported\":1")))
                .andExpect(content().string(containsString("\"acceptanceAuditId\":\"SYS-")))
                .andExpect(content().string(containsString("UT-PRINT-" + caseNo)));

        mockMvc.perform(get("/api/reports/print-batches?keyword=UT-PRINT-" + caseNo + "&acceptanceStatus=EXPORTED&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"acceptanceStatus\":\"EXPORTED\"")))
                .andExpect(content().string(containsString("UT-PRINT-" + caseNo)));

        mockMvc.perform(get("/api/reports/print-batches?keyword=UT-PRINT-" + caseNo + "&acceptanceStatus=PENDING&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"acceptanceStatus\":\"PENDING\"")))
                .andExpect(content().string(containsString("\"items\":[]")));

        mockMvc.perform(get("/api/reports/print-archive?keyword=" + caseNo + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"acceptanceExported\":1")))
                .andExpect(content().string(containsString("\"acceptanceAuditId\":\"SYS-")))
                .andExpect(content().string(containsString("UT-PRINT-" + caseNo)));

        mockMvc.perform(get("/api/reports/print-archive.csv?keyword=" + caseNo + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"filter\",\"keyword\",\"" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"filter\",\"printStatus\",\"ALL\"")))
                .andExpect(content().string(containsString("\"filter\",\"limit\",\"5\"")))
                .andExpect(content().string(containsString("\"filter\",\"rows\",\"1\"")))
                .andExpect(content().string(containsString("acceptanceAuditId")))
                .andExpect(content().string(containsString("SYS-")))
                .andExpect(content().string(containsString("UT-PRINT-" + caseNo)));

        mockMvc.perform(get("/api/reports/migration-closure?orgCode=001&year=2099&month=1&keyword=UT-PRINT-" + caseNo + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"pendingAcceptanceBatches\":0")))
                .andExpect(content().string(containsString("\"acceptanceExportedBatches\":1")))
                .andExpect(content().string(containsString("\"latestAcceptanceExportedAt\":")));

        mockMvc.perform(get("/api/reports/migration-closure.csv?orgCode=001&year=2099&month=1&keyword=UT-PRINT-" + caseNo + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("pendingAcceptanceBatches")))
                .andExpect(content().string(containsString("acceptanceExportedBatches")))
                .andExpect(content().string(containsString("latestAcceptanceExportedAt")));

        mockMvc.perform(get("/api/reports/migration-print-self-check.csv?orgCode=001&year=2099&month=1&keyword=UT-PRINT-" + caseNo + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("salary-report-print-self-check-001.csv")))
                .andExpect(content().string(containsString("\"filter\",\"keyword\",\"UT-PRINT-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"item\",\"status\",\"metric\",\"detail\"")))
                .andExpect(content().string(containsString("\"archive\"")))
                .andExpect(content().string(containsString("\"batch\"")))
                .andExpect(content().string(containsString("\"acceptance\"")))
                .andExpect(content().string(containsString("\"audit\"")))
                .andExpect(content().string(containsString("\"risk\"")));

        mockMvc.perform(get("/api/reports/audits?action=report-print-self-check-csv&targetCode=001&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"actionName\":\"report-print-self-check-csv\"")))
                .andExpect(content().string(containsString("pendingAcceptanceBatches")));

        mockMvc.perform(get("/api/reports/migration-guide.csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("salary-report-migration-guide.csv")))
                .andExpect(content().string(containsString("\"section\",\"status\",\"detail\"")))
                .andExpect(content().string(containsString("\"report-entry\",\"READY\"")))
                .andExpect(content().string(containsString("\"acceptance-package\",\"READY\"")))
                .andExpect(content().string(containsString("\"audit-trace\",\"READY\"")));

        mockMvc.perform(get("/api/reports/audits?action=report-migration-guide-csv&targetCode=REPORT_PRINT&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"actionName\":\"report-migration-guide-csv\"")))
                .andExpect(content().string(containsString("sections=6")));

        mockMvc.perform(get("/api/reports/migration-matrix")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"items\"")))
                .andExpect(content().string(containsString("\"status\":\"MIGRATED\"")))
                .andExpect(content().string(containsString("SALARY_CASE_APPROVAL_PRINT")))
                .andExpect(content().string(containsString("salary-case-approvals-print")));

        mockMvc.perform(get("/api/reports/migration-matrix.csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("salary-report-migration-matrix.csv")))
                .andExpect(content().string(containsString("\"code\",\"title\",\"category\",\"legacyTemplate\",\"status\"")))
                .andExpect(content().string(containsString("SALARY_CASE_APPROVAL_PRINT")))
                .andExpect(content().string(containsString("salary-case-approvals-print")));

        mockMvc.perform(get("/api/reports/audits?action=report-migration-matrix-csv&targetCode=REPORT_PRINT&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"actionName\":\"report-migration-matrix-csv\"")))
                .andExpect(content().string(containsString("rows=")));

        mockMvc.perform(get("/api/reports/migration-acceptance-checklist?orgCode=001&year=2099&month=1&keyword=UT-PRINT-" + caseNo + "&personCode=001-00055&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"items\"")))
                .andExpect(content().string(containsString("\"code\":\"approvalBatch\"")))
                .andExpect(content().string(containsString("\"status\":\"PASS\"")))
                .andExpect(content().string(containsString("salary-case-approvals-print")));

        mockMvc.perform(get("/api/reports/migration-acceptance-checklist.csv?orgCode=001&year=2099&month=1&keyword=UT-PRINT-" + caseNo + "&personCode=001-00055&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("salary-report-migration-acceptance-checklist-001.csv")))
                .andExpect(content().string(containsString("\"code\",\"title\",\"status\",\"sampleCount\"")))
                .andExpect(content().string(containsString("approvalBatch")))
                .andExpect(content().string(containsString("salary-case-approvals-print")));

        mockMvc.perform(get("/api/reports/audits?action=report-migration-acceptance-checklist-csv&targetCode=001&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"actionName\":\"report-migration-acceptance-checklist-csv\"")))
                .andExpect(content().string(containsString("pass=")));

        mockMvc.perform(get("/api/reports/migration-sample-evidence?orgCode=001&year=2099&month=1&keyword=UT-PRINT-" + caseNo + "&personCode=001-00055&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"items\"")))
                .andExpect(content().string(containsString("\"reportCode\":\"approvalBatch\"")))
                .andExpect(content().string(containsString("\"sourceTable\":\"salary_business_case\"")))
                .andExpect(content().string(containsString(caseNo)));

        mockMvc.perform(get("/api/reports/migration-sample-evidence.csv?orgCode=001&year=2099&month=1&keyword=UT-PRINT-" + caseNo + "&personCode=001-00055&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("salary-report-migration-sample-evidence-001.csv")))
                .andExpect(content().string(containsString("\"reportCode\",\"sampleKey\",\"personCode\"")))
                .andExpect(content().string(containsString("approvalBatch")))
                .andExpect(content().string(containsString(caseNo)));

        mockMvc.perform(get("/api/reports/audits?action=report-migration-sample-evidence-csv&targetCode=001&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"actionName\":\"report-migration-sample-evidence-csv\"")))
                .andExpect(content().string(containsString("rows=")));

        var reportMigrationDeliveryPackage = mockMvc.perform(get("/api/reports/migration-delivery-package.zip?orgCode=001&year=2099&month=1&keyword=UT-PRINT-" + caseNo + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("salary-report-migration-delivery-001.zip")))
                .andReturn();
        Set<String> reportMigrationDeliveryPackageEntries = new HashSet<>();
        String reportMigrationDeliveryReadme = "";
        String reportMigrationDeliverySelfCheck = "";
        String reportMigrationDeliveryMatrix = "";
        String reportMigrationDeliveryAcceptanceChecklist = "";
        String reportMigrationDeliverySampleEvidence = "";
        String reportMigrationDeliveryMeta = "";
        String reportMigrationDeliveryAudits = "";
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(reportMigrationDeliveryPackage.getResponse().getContentAsByteArray()))) {
            var entry = zip.getNextEntry();
            while (entry != null) {
                reportMigrationDeliveryPackageEntries.add(entry.getName());
                if ("README.txt".equals(entry.getName())) {
                    reportMigrationDeliveryReadme = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if ("delivery-package-meta.csv".equals(entry.getName())) {
                    reportMigrationDeliveryMeta = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if ("delivery-package-audits.csv".equals(entry.getName())) {
                    reportMigrationDeliveryAudits = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if ("salary-report-migration-matrix.csv".equals(entry.getName())) {
                    reportMigrationDeliveryMatrix = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if ("salary-report-migration-acceptance-checklist-001.csv".equals(entry.getName())) {
                    reportMigrationDeliveryAcceptanceChecklist = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if ("salary-report-migration-sample-evidence-001.csv".equals(entry.getName())) {
                    reportMigrationDeliverySampleEvidence = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if ("salary-report-print-self-check-001.csv".equals(entry.getName())) {
                    reportMigrationDeliverySelfCheck = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
                entry = zip.getNextEntry();
            }
        }
        assertTrue(reportMigrationDeliveryPackageEntries.contains("README.txt"));
        assertTrue(reportMigrationDeliveryPackageEntries.contains("salary-report-catalog.csv"));
        assertTrue(reportMigrationDeliveryPackageEntries.contains("salary-report-migration-matrix.csv"));
        assertTrue(reportMigrationDeliveryPackageEntries.contains("salary-report-migration-acceptance-checklist-001.csv"));
        assertTrue(reportMigrationDeliveryPackageEntries.contains("salary-report-migration-sample-evidence-001.csv"));
        assertTrue(reportMigrationDeliveryPackageEntries.contains("salary-report-migration-guide.csv"));
        assertTrue(reportMigrationDeliveryPackageEntries.contains("salary-report-migration-closure-001.csv"));
        assertTrue(reportMigrationDeliveryPackageEntries.contains("salary-report-print-self-check-001.csv"));
        assertTrue(reportMigrationDeliveryPackageEntries.contains("delivery-package-meta.csv"));
        assertTrue(reportMigrationDeliveryPackageEntries.contains("delivery-package-audits.csv"));
        assertTrue(reportMigrationDeliveryReadme.contains("Report print migration delivery package"));
        assertTrue(reportMigrationDeliveryReadme.contains("orgCode: 001"));
        assertTrue(reportMigrationDeliveryReadme.contains("fileCount: 10"));
        assertTrue(reportMigrationDeliveryReadme.contains("salary-report-migration-matrix.csv"));
        assertTrue(reportMigrationDeliveryReadme.contains("salary-report-migration-acceptance-checklist-001.csv"));
        assertTrue(reportMigrationDeliveryReadme.contains("salary-report-migration-sample-evidence-001.csv"));
        assertTrue(reportMigrationDeliveryReadme.contains("delivery-package-audits.csv"));
        assertTrue(reportMigrationDeliveryMatrix.contains("SALARY_CASE_APPROVAL_PRINT"));
        assertTrue(reportMigrationDeliveryMatrix.contains("salary-case-approvals-print"));
        assertTrue(reportMigrationDeliveryAcceptanceChecklist.contains("approvalBatch"));
        assertTrue(reportMigrationDeliveryAcceptanceChecklist.contains("salary-case-approvals-print"));
        assertTrue(reportMigrationDeliverySampleEvidence.contains("approvalBatch"));
        assertTrue(reportMigrationDeliverySampleEvidence.contains(caseNo));
        assertTrue(reportMigrationDeliveryMeta.contains("\"meta\",\"orgCode\",\"001\""));
        assertTrue(reportMigrationDeliveryMeta.contains("\"meta\",\"keyword\",\"UT-PRINT-" + caseNo + "\""));
        assertTrue(reportMigrationDeliveryMeta.contains("\"meta\",\"auditAction\",\"report-migration-delivery-package\""));
        assertTrue(reportMigrationDeliveryMeta.contains("\"meta\",\"auditTargetType\",\"REPORT_MIGRATION_DELIVERY\""));
        assertTrue(reportMigrationDeliveryMeta.contains("\"meta\",\"fileCount\",\"10\""));
        assertTrue(reportMigrationDeliveryAudits.contains("\"filter\",\"targetCode\",\"001\""));
        assertTrue(reportMigrationDeliveryAudits.contains("report-migration-delivery-package"));
        assertTrue(reportMigrationDeliverySelfCheck.contains("\"filter\",\"keyword\",\"UT-PRINT-" + caseNo + "\""));

        mockMvc.perform(get("/api/reports/audits?action=report-migration-delivery-package&targetCode=001&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"actionName\":\"report-migration-delivery-package\"")))
                .andExpect(content().string(containsString("files=10")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-comparison.csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u5199\u5165\u8ba1\u5212\u53f7")))
                .andExpect(content().string(containsString("HWP-" + caseNo)))
                .andExpect(content().string(containsString("jcgz2")))
                .andExpect(content().string(containsString("1234")))
                .andExpect(content().string(containsString(insertedId)));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-rollback-preview.csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u662f\u5426\u53ef\u64a4\u9500")))
                .andExpect(content().string(containsString("HWP-" + caseNo)))
                .andExpect(content().string(containsString(insertedId)))
                .andExpect(content().string(containsString("sid\u6062\u590d\u65b9\u6848")))
                .andExpect(content().string(containsString("\u5199\u5165\u884c")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-audits")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"history-write-rollback-preview-csv\"")))
                .andExpect(content().string(containsString("rollbackable=true")));

        var historyClosureAcceptancePackage = mockMvc.perform(get("/api/workbench/history-write-closure-acceptance-package.zip")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("history-write-closure-acceptance-package.zip")))
                .andReturn();
        Set<String> historyClosureAcceptanceEntries = new HashSet<>();
        String historyClosureAcceptanceReadme = "";
        String historyWriteSafetyPolicyCsv = "";
        String salaryMigrationClosureChecklistCsv = "";
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(historyClosureAcceptancePackage.getResponse().getContentAsByteArray()))) {
            var entry = zip.getNextEntry();
            while (entry != null) {
                historyClosureAcceptanceEntries.add(entry.getName());
                if ("README.txt".equals(entry.getName())) {
                    historyClosureAcceptanceReadme = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if ("salary-migration-closure-checklist.csv".equals(entry.getName())) {
                    salaryMigrationClosureChecklistCsv = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if ("history-write-safety-policy.csv".equals(entry.getName())) {
                    historyWriteSafetyPolicyCsv = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
                entry = zip.getNextEntry();
            }
        }
        assertTrue(historyClosureAcceptanceEntries.contains("README.txt"));
        assertTrue(historyClosureAcceptanceEntries.contains("salary-migration-closure-checklist.csv"));
        assertTrue(historyClosureAcceptanceEntries.contains("history-write-closure-acceptance-summary.csv"));
        assertTrue(historyClosureAcceptanceEntries.contains("history-write-safety-policy.csv"));
        assertTrue(historyClosureAcceptanceEntries.contains("history-write-plans-unprinted-or-plan.csv"));
        assertTrue(historyClosureAcceptanceEntries.contains("history-write-plans-ready-to-write.csv"));
        assertTrue(historyClosureAcceptanceEntries.contains("history-write-plans-review-difference.csv"));
        assertTrue(historyClosureAcceptanceEntries.contains("history-write-plans-rolled-back.csv"));
        assertTrue(historyClosureAcceptanceEntries.contains("history-write-batch-ledger-write.csv"));
        assertTrue(historyClosureAcceptanceEntries.contains("history-write-batch-ledger-review.csv"));
        assertTrue(historyClosureAcceptanceEntries.contains("history-write-batch-ledger-rolled-back.csv"));
        assertTrue(historyClosureAcceptanceReadme.contains("\u5386\u53f2\u5199\u5165\u95ed\u73af\u9a8c\u6536\u5305"));
        assertTrue(historyClosureAcceptanceReadme.contains("salary-migration-closure-checklist.csv"));
        assertTrue(historyClosureAcceptanceReadme.contains("history-write-safety-policy.csv"));
        assertTrue(historyClosureAcceptanceReadme.contains("history-write-plans-ready-to-write.csv"));
        assertTrue(salaryMigrationClosureChecklistCsv.contains("\u95ed\u73af\u73af\u8282"));
        assertTrue(salaryMigrationClosureChecklistCsv.contains("\u5386\u53f2\u5199\u5165"));
        assertTrue(salaryMigrationClosureChecklistCsv.contains("salary-migration-closure-checklist-csv"));
        assertTrue(salaryMigrationClosureChecklistCsv.contains("salary-migration-delivery-ledger.csv"));
        assertTrue(historyWriteSafetyPolicyCsv.contains("history-write-batch-safety-preview"));
        assertTrue(historyWriteSafetyPolicyCsv.contains("history-write-batch-safety-consume"));
        assertTrue(historyWriteSafetyPolicyCsv.contains("history-write-batch-rollback-safety-preview"));
        assertTrue(historyWriteSafetyPolicyCsv.contains("history-write-batch-rollback-safety-consume"));
        assertTrue(historyWriteSafetyPolicyCsv.contains("safetyToken"));
        assertTrue(historyWriteSafetyPolicyCsv.contains("blocked-skipped"));

        mockMvc.perform(get("/api/system/audits?action=history-write-closure-acceptance-package&targetCode=PACKAGE&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"history-write-closure-acceptance-package\"")))
                .andExpect(content().string(containsString("acceptanceNo=HWD-")))
                .andExpect(content().string(containsString("pending=")))
                .andExpect(content().string(containsString("closed=")));

        mockMvc.perform(get("/api/system/audits.csv?action=history-write-closure-acceptance-package&targetCode=PACKAGE&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("system-audits.csv")))
                .andExpect(content().string(containsString("\u5bfc\u51fa\u5386\u53f2\u5199\u5165\u95ed\u73af\u9a8c\u6536\u5305")))
                .andExpect(content().string(containsString("history-write-closure-acceptance-package")))
                .andExpect(content().string(containsString("acceptanceNo=HWD-")))
                .andExpect(content().string(containsString("pending=")))
                .andExpect(content().string(containsString("closed=")));

        mockMvc.perform(get("/api/workbench/history-write-delivery-overview.csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("history-write-delivery-overview.csv")))
                .andExpect(content().string(containsString("\u4ea4\u4ed8\u7ed3\u8bba")))
                .andExpect(content().string(containsString("\u4ea4\u4ed8\u961f\u5217")))
                .andExpect(content().string(containsString("\u4ea4\u4ed8\u8bc1\u636e")))
                .andExpect(content().string(containsString("history-write-closure-acceptance-summary.csv")))
                .andExpect(content().string(containsString("history-write-batch-ledger-rolled-back.csv")));

        mockMvc.perform(get("/api/workbench/salary-migration-closure-checklist.csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("salary-migration-closure-checklist.csv")))
                .andExpect(content().string(containsString("\u95ed\u73af\u73af\u8282")))
                .andExpect(content().string(containsString("\u62a5\u8868\u6253\u5370\u5f52\u6863")))
                .andExpect(content().string(containsString("history-write-safety-policy.csv")))
                .andExpect(content().string(containsString("salary-migration-closure-checklist-csv")))
                .andExpect(content().string(containsString("salary-migration-delivery-ledger.csv")));

        mockMvc.perform(get("/api/system/audits?action=salary-migration-closure-checklist-csv&targetCode=CHECKLIST&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"salary-migration-closure-checklist-csv\"")))
                .andExpect(content().string(containsString("pending=")))
                .andExpect(content().string(containsString("closed=")));

        var salaryMigrationDeliveryPackage = mockMvc.perform(get("/api/workbench/salary-migration-delivery-package.zip?orgCode=001&year=2099&month=1&keyword=UT-PRINT-" + caseNo + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("salary-migration-delivery-package.zip")))
                .andReturn();
        Set<String> salaryMigrationDeliveryEntries = new HashSet<>();
        String salaryMigrationDeliveryReadme = "";
        String salaryMigrationDeliveryIndex = "";
        String salaryMigrationDeliveryChecklist = "";
        String salaryMigrationDeliveryLedger = "";
        String salaryMigrationDeliverySelfCheck = "";
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(salaryMigrationDeliveryPackage.getResponse().getContentAsByteArray()))) {
            var entry = zip.getNextEntry();
            while (entry != null) {
                salaryMigrationDeliveryEntries.add(entry.getName());
                if ("README.txt".equals(entry.getName())) {
                    salaryMigrationDeliveryReadme = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if ("salary-migration-delivery-index.csv".equals(entry.getName())) {
                    salaryMigrationDeliveryIndex = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if ("salary-migration-closure-checklist.csv".equals(entry.getName())) {
                    salaryMigrationDeliveryChecklist = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if ("salary-migration-delivery-ledger.csv".equals(entry.getName())) {
                    salaryMigrationDeliveryLedger = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if ("salary-migration-delivery-self-check.csv".equals(entry.getName())) {
                    salaryMigrationDeliverySelfCheck = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
                entry = zip.getNextEntry();
            }
        }
        assertTrue(salaryMigrationDeliveryEntries.contains("README.txt"));
        assertTrue(salaryMigrationDeliveryEntries.contains("salary-migration-delivery-index.csv"));
        assertTrue(salaryMigrationDeliveryEntries.contains("salary-migration-closure-checklist.csv"));
        assertTrue(salaryMigrationDeliveryEntries.contains("salary-migration-delivery-ledger.csv")
                || salaryMigrationDeliveryEntries.contains("salary-migration-delivery-ledger-error.txt"));
        assertTrue(salaryMigrationDeliveryEntries.contains("salary-migration-delivery-self-check.csv")
                || salaryMigrationDeliveryEntries.contains("salary-migration-delivery-self-check-error.txt"));
        assertTrue(salaryMigrationDeliveryEntries.contains("history-write-closure-acceptance-package.zip")
                || salaryMigrationDeliveryEntries.contains("history-write-closure-acceptance-package-error.txt"));
        assertTrue(salaryMigrationDeliveryEntries.contains("salary-report-migration-delivery-package.zip")
                || salaryMigrationDeliveryEntries.contains("salary-report-migration-delivery-package-error.txt"));
        assertTrue(salaryMigrationDeliveryReadme.contains("\u5de5\u8d44\u8fc1\u79fb\u603b\u4ea4\u4ed8\u5305"));
        assertTrue(salaryMigrationDeliveryReadme.contains("\u5355\u4f4d\u8303\u56f4\uff1a001"));
        assertTrue(salaryMigrationDeliveryReadme.contains("salary-migration-delivery-ledger.csv"));
        assertTrue(salaryMigrationDeliveryReadme.contains("salary-migration-delivery-self-check.csv"));
        assertTrue(salaryMigrationDeliveryIndex.contains("salary-report-migration-delivery-package"));
        assertTrue(salaryMigrationDeliveryIndex.contains("history-write-closure-acceptance-package"));
        assertTrue(salaryMigrationDeliveryIndex.contains("salary-migration-delivery-package"));
        assertTrue(salaryMigrationDeliveryIndex.contains("salary-migration-delivery-ledger.csv"));
        assertTrue(salaryMigrationDeliveryIndex.contains("salary-migration-delivery-self-check.csv"));
        assertTrue(salaryMigrationDeliveryChecklist.contains("salary-migration-closure-checklist-csv"));
        if (salaryMigrationDeliveryEntries.contains("salary-migration-delivery-ledger.csv")) {
            assertTrue(salaryMigrationDeliveryLedger.contains("salary-migration-delivery-error-"));
        }
        if (salaryMigrationDeliveryEntries.contains("salary-migration-delivery-self-check.csv")) {
            assertTrue(salaryMigrationDeliverySelfCheck.contains("Package file count"));
            assertTrue(salaryMigrationDeliverySelfCheck.contains("PASS"));
        }

        mockMvc.perform(get("/api/system/audits?action=salary-migration-delivery-package&targetCode=001&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"salary-migration-delivery-package\"")))
                .andExpect(content().string(containsString("files=7")))
                .andExpect(content().string(containsString("historyStatus=")))
                .andExpect(content().string(containsString("reportStatus=")))
                .andExpect(content().string(containsString("pending=")))
                .andExpect(content().string(containsString("closed=")))
                .andExpect(content().string(containsString("governanceWorkItemId=salary-migration-delivery-error-")));

        mockMvc.perform(get("/api/workbench/salary-migration-delivery-ledger.csv?orgCode=001&limit=20")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("salary-migration-delivery-ledger.csv")))
                .andExpect(content().string(containsString("审计号")))
                .andExpect(content().string(containsString("治理任务号")))
                .andExpect(content().string(containsString("复测状态")))
                .andExpect(content().string(containsString("salary-migration-delivery-error-")));

        mockMvc.perform(get("/api/workbench/salary-migration-delivery-self-check.csv?orgCode=001")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("salary-migration-delivery-self-check.csv")))
                .andExpect(content().string(containsString("Total delivery package audit")))
                .andExpect(content().string(containsString("Package file count")))
                .andExpect(content().string(containsString("PASS")));

        mockMvc.perform(get("/api/system/audits?action=salary-migration-delivery-self-check-csv&targetCode=001&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"salary-migration-delivery-self-check-csv\"")))
                .andExpect(content().string(containsString("warn=")));

        mockMvc.perform(get("/api/system/audits?action=salary-migration-delivery-ledger-csv&targetCode=001&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"salary-migration-delivery-ledger-csv\"")))
                .andExpect(content().string(containsString("count=")));

        if (salaryMigrationDeliveryEntries.contains("history-write-closure-acceptance-package-error.txt")
                || salaryMigrationDeliveryEntries.contains("salary-report-migration-delivery-package-error.txt")) {
            String deliveryGovernanceWorkItemId = jdbcTemplate.queryForObject("""
                    SELECT work_item_id
                    FROM salary_todo_candidate_cache
                    WHERE source = 'DATA_GOVERNANCE'
                      AND source_id = 'MIGRATION_DELIVERY_PACKAGE'
                      AND org_code = '001'
                      AND work_item_id LIKE 'salary-migration-delivery-error-001-2099-1-%'
                    ORDER BY generated_at DESC
                    LIMIT 1
                    """, String.class);
            assertNotNull(deliveryGovernanceWorkItemId);
            mockMvc.perform(get("/api/workbench/data-governance/tasks/" + deliveryGovernanceWorkItemId + "/migration-delivery-detail")
                            .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("\"workItemId\":\"" + deliveryGovernanceWorkItemId + "\"")))
                    .andExpect(content().string(containsString("\"historyStatus\"")))
                    .andExpect(content().string(containsString("\"reportStatus\"")))
                    .andExpect(content().string(containsString("\"closeSuggested\"")))
                    .andExpect(content().string(containsString("\"latestAudit\"")));
            mockMvc.perform(post("/api/workbench/data-governance/tasks/" + deliveryGovernanceWorkItemId + "/retest")
                            .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("\"workItemId\":\"" + deliveryGovernanceWorkItemId + "\"")))
                    .andExpect(content().string(containsString("\"retestStatus\"")));
        }

        mockMvc.perform(get("/api/system/audits?action=history-write-delivery-overview-csv&targetCode=OVERVIEW&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"history-write-delivery-overview-csv\"")))
                .andExpect(content().string(containsString("acceptanceNo=HWD-")))
                .andExpect(content().string(containsString("activeQueues=")))
                .andExpect(content().string(containsString("evidence=9")));

        mockMvc.perform(get("/api/system/audits.csv?action=history-write-delivery-overview-csv&targetCode=OVERVIEW&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("system-audits.csv")))
                .andExpect(content().string(containsString("\u5bfc\u51fa\u5386\u53f2\u5199\u5165\u4ea4\u4ed8\u603b\u89c8")))
                .andExpect(content().string(containsString("\u5ba1\u8ba1\u53f7,\u65f6\u95f4")))
                .andExpect(content().string(containsString("\u9a8c\u6536\u53f7,\u5173\u952e\u5b57,\u5bfc\u51fa\u7c7b\u578b,\u5f00\u59cb\u65e5\u671f,\u7ed3\u675f\u65e5\u671f")))
                .andExpect(content().string(containsString("history-write-delivery-overview-csv")))
                .andExpect(content().string(containsString("acceptanceNo=HWD-")))
                .andExpect(content().string(containsString("activeQueues=")))
                .andExpect(content().string(containsString("evidence=9")));

        Long historyDeliveryAcceptanceRecords = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM history_write_delivery_acceptance
                WHERE exported_by = ?
                  AND export_type IN ('PACKAGE', 'OVERVIEW')
                  AND acceptance_no LIKE 'HWD-%'
                  AND evidence_file_count = 9
                """, Long.class, SCOPED_WORKBENCH_USER);
        assertTrue(historyDeliveryAcceptanceRecords >= 2);

        mockMvc.perform(get("/api/workbench/history-write-delivery-acceptances?limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"acceptanceNo\":\"HWD-")))
                .andExpect(content().string(containsString("\"exportType\":\"PACKAGE\"")))
                .andExpect(content().string(containsString("\"exportType\":\"OVERVIEW\"")))
                .andExpect(content().string(containsString("\"evidenceFileCount\":9")));

        mockMvc.perform(get("/api/workbench/history-write-delivery-acceptances?exportType=OVERVIEW&keyword=HWD-&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"exportType\":\"OVERVIEW\"")))
                .andExpect(content().string(not(containsString("\"exportType\":\"PACKAGE\""))));

        mockMvc.perform(get("/api/workbench/history-write-delivery-acceptances?exportType=OVERVIEW&keyword=HWD-&exportedFrom=2000-01-01&exportedTo=2199-12-31&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"exportType\":\"OVERVIEW\"")))
                .andExpect(content().string(not(containsString("\"exportType\":\"PACKAGE\""))));

        String historyDeliveryAcceptanceNo = jdbcTemplate.queryForObject("""
                SELECT acceptance_no
                FROM history_write_delivery_acceptance
                WHERE exported_by = ?
                  AND export_type = 'OVERVIEW'
                ORDER BY exported_at DESC, id DESC
                LIMIT 1
                """, String.class, SCOPED_WORKBENCH_USER);
        mockMvc.perform(get("/api/workbench/history-write-delivery-acceptances/" + historyDeliveryAcceptanceNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"acceptanceNo\":\"" + historyDeliveryAcceptanceNo + "\"")))
                .andExpect(content().string(containsString("\"rows\"")))
                .andExpect(content().string(containsString("\"evidence\"")))
                .andExpect(content().string(containsString("history-write-closure-acceptance-summary.csv")))
                .andExpect(content().string(containsString("SALARY_NEXT_EXECUTE_WRITE")));

        mockMvc.perform(get("/api/workbench/history-write-delivery-acceptances/" + historyDeliveryAcceptanceNo + ".csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("history-write-delivery-acceptance-" + historyDeliveryAcceptanceNo + ".csv")))
                .andExpect(content().string(containsString("\u9a8c\u6536\u53f7")))
                .andExpect(content().string(containsString(historyDeliveryAcceptanceNo)))
                .andExpect(content().string(containsString("\u961f\u5217\u5feb\u7167")))
                .andExpect(content().string(containsString("\u8bc1\u636e\u6587\u4ef6")))
                .andExpect(content().string(containsString("history-write-closure-acceptance-summary.csv")));

        mockMvc.perform(get("/api/workbench/history-write-delivery-acceptances/" + historyDeliveryAcceptanceNo + "/print")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("\u5386\u53f2\u5199\u5165\u4ea4\u4ed8\u786e\u8ba4\u5355")))
                .andExpect(content().string(containsString(historyDeliveryAcceptanceNo)))
                .andExpect(content().string(containsString("\u961f\u5217\u5feb\u7167")))
                .andExpect(content().string(containsString("\u8bc1\u636e\u6587\u4ef6")))
                .andExpect(content().string(containsString("history-write-closure-acceptance-summary.csv")))
                .andExpect(content().string(containsString("window.print")));

        mockMvc.perform(get("/api/workbench/history-write-delivery-acceptances/index.csv?exportType=OVERVIEW&keyword=HWD-&exportedFrom=2000-01-01&exportedTo=2199-12-31&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("history-write-delivery-acceptance-index.csv")))
                .andExpect(content().string(containsString("\u9a8c\u6536\u53f7")))
                .andExpect(content().string(containsString(historyDeliveryAcceptanceNo)))
                .andExpect(content().string(containsString("\u4ea4\u4ed8\u7ed3\u8bba")))
                .andExpect(content().string(not(containsString("PACKAGE"))));

        mockMvc.perform(get("/api/workbench/history-write-delivery-acceptances/print-batch?exportType=OVERVIEW&keyword=HWD-&exportedFrom=2000-01-01&exportedTo=2199-12-31&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("\u5386\u53f2\u5199\u5165\u4ea4\u4ed8\u786e\u8ba4\u5355\u5f52\u6863\u7d22\u5f15")))
                .andExpect(content().string(containsString("\u5f52\u6863\u6e05\u5355")))
                .andExpect(content().string(containsString(historyDeliveryAcceptanceNo)))
                .andExpect(content().string(containsString("2000-01-01")))
                .andExpect(content().string(containsString("2199-12-31")))
                .andExpect(content().string(containsString("\u7eb8\u8d28\u5f52\u6863\u6838\u5bf9")))
                .andExpect(content().string(containsString("\u5bfc\u51fa\u5ba1\u8ba1\u53f7")))
                .andExpect(content().string(containsString("SYS-")))
                .andExpect(content().string(containsString("\u786e\u8ba4\u5355\u6570\u91cf")))
                .andExpect(content().string(containsString("\u6253\u5370\u5168\u90e8")));

        String batchPrintAuditNo = jdbcTemplate.queryForObject("""
                SELECT CONCAT('SYS-', id)
                FROM sys_audit_log
                WHERE module_name = 'workbench'
                  AND action_name = 'history-write-delivery-acceptance-batch-print'
                  AND target_code = 'BATCH'
                ORDER BY id DESC
                LIMIT 1
                """, String.class);
        mockMvc.perform(get("/api/system/audits?auditId=" + batchPrintAuditNo + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"id\":\"" + batchPrintAuditNo + "\"")))
                .andExpect(content().string(containsString("\"action\":\"history-write-delivery-acceptance-batch-print\"")))
                .andExpect(content().string(containsString("scope=keyword=HWD-")));

        mockMvc.perform(get("/api/system/audits.csv?auditId=" + batchPrintAuditNo + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(batchPrintAuditNo)))
                .andExpect(content().string(containsString("\u4ea4\u4ed8\u603b\u89c8")))
                .andExpect(content().string(containsString("2000-01-01")));

        mockMvc.perform(get("/api/system/audits?action=history-write-delivery-acceptance-detail-csv&targetCode=" + historyDeliveryAcceptanceNo + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"history-write-delivery-acceptance-detail-csv\"")))
                .andExpect(content().string(containsString("scope=acceptanceNo=" + historyDeliveryAcceptanceNo)))
                .andExpect(content().string(containsString("rows=")))
                .andExpect(content().string(containsString("evidence=")));

        mockMvc.perform(get("/api/system/audits?action=history-write-delivery-acceptance-print&targetCode=" + historyDeliveryAcceptanceNo + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"history-write-delivery-acceptance-print\"")))
                .andExpect(content().string(containsString("scope=acceptanceNo=" + historyDeliveryAcceptanceNo)))
                .andExpect(content().string(containsString("rows=")))
                .andExpect(content().string(containsString("evidence=")));

        mockMvc.perform(get("/api/system/audits?action=history-write-delivery-acceptance-index-csv&targetCode=INDEX&limit=5")
                .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"history-write-delivery-acceptance-index-csv\"")))
                .andExpect(content().string(containsString("scope=keyword=HWD-")))
                .andExpect(content().string(containsString("exportType=OVERVIEW")))
                .andExpect(content().string(containsString("exportedFrom=2000-01-01")))
                .andExpect(content().string(containsString("exportedTo=2199-12-31")))
                .andExpect(content().string(containsString("count=")));

        mockMvc.perform(get("/api/system/audits?action=history-write-delivery-acceptance-batch-print&targetCode=BATCH&limit=5")
                .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"history-write-delivery-acceptance-batch-print\"")))
                .andExpect(content().string(containsString("scope=keyword=HWD-")))
                .andExpect(content().string(containsString("exportType=OVERVIEW")))
                .andExpect(content().string(containsString("exportedFrom=2000-01-01")))
                .andExpect(content().string(containsString("exportedTo=2199-12-31")))
                .andExpect(content().string(containsString("count=")));

        mockMvc.perform(get("/api/system/audits.csv?action=history-write-delivery-acceptance-index-csv&targetCode=INDEX&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("system-audits.csv")))
                .andExpect(content().string(containsString("\u5ba1\u8ba1\u53f7,\u65f6\u95f4")))
                .andExpect(content().string(containsString("\u9a8c\u6536\u53f7,\u5173\u952e\u5b57,\u5bfc\u51fa\u7c7b\u578b,\u5f00\u59cb\u65e5\u671f,\u7ed3\u675f\u65e5\u671f")))
                .andExpect(content().string(containsString("HWD-")))
                .andExpect(content().string(containsString("\u4ea4\u4ed8\u603b\u89c8")))
                .andExpect(content().string(containsString("2000-01-01")))
                .andExpect(content().string(containsString("2199-12-31")));

        jdbcTemplate.update("UPDATE hisbase SET glgz2 = ? WHERE id = ?", 99, insertedId);

        jdbcTemplate.update("""
                INSERT INTO sys_audit_log(module_name, action_name, target_type, target_code, summary, operator)
                VALUES ('workbench', 'history-write-comparison-retest', 'SALARY_CASE', ?, 'unit-test retest mismatch', ?)
                """, caseNo, SCOPED_WORKBENCH_USER);

        mockMvc.perform(get("/api/workbench/history-write-plans?status=EXECUTED&comparisonStatus=MISMATCHED&maintenanceTarget=base&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"comparisonStatus\":\"MISMATCHED\"")))
                .andExpect(content().string(containsString("\"comparisonRetestStatus\":\"RETEST_MISMATCHED\"")))
                .andExpect(content().string(containsString("\"maintenanceSuggestionJson\"")))
                .andExpect(content().string(containsString("\\\"target\\\":\\\"base\\\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=EXECUTED&comparisonStatus=MISMATCHED&maintenanceTarget=post&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"planNo\":\"HWP-" + caseNo + "\""))));

        mockMvc.perform(get("/api/workbench/history-write-review-ledger?status=EXECUTED&comparisonStatus=MISMATCHED&maintenanceTarget=base&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"byMaintenanceTarget\"")))
                .andExpect(content().string(containsString("\"key\":\"base\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans.csv?status=EXECUTED&comparisonStatus=MISMATCHED&maintenanceTarget=base&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u5efa\u8bae\u68c0\u67e5\u65b9\u5411")))
                .andExpect(content().string(containsString("\u5efa\u8bae\u5b57\u6bb5")))
                .andExpect(content().string(containsString("HWP-" + caseNo)));

        mockMvc.perform(get("/api/workbench/items?status=DONE&workflowStatus=HISTORY_REVIEW_PENDING&keyword=History Write&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"workflowStatus\":\"HISTORY_REVIEW_PENDING\"")));
        mockMvc.perform(get("/api/workbench/items?status=DONE&nextAction=REVIEW_DIFFERENCE&keyword=History Write&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"workflowStatus\":\"HISTORY_REVIEW_PENDING\"")))
                .andExpect(content().string(containsString("\"nextActionCode\":\"REVIEW_DIFFERENCE\"")));

        mockMvc.perform(post("/api/workbench/history-write-plans/batch-review?status=EXECUTED&comparisonStatus=MISMATCHED&reviewStatus=PENDING&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewCategory\":\"BASE_CHANGED\",\"reviewReason\":\"unit-test comparison reviewed\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"success\":1")))
                .andExpect(content().string(containsString("\"status\":\"REVIEWED\"")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&workflowStatus=HISTORY_CLOSED&keyword=History Write&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"workflowStatus\":\"HISTORY_CLOSED\"")));
        mockMvc.perform(get("/api/workbench/items?status=DONE&closureStatus=CLOSED&keyword=History Write&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"closureStatus\":\"CLOSED\"")));
        mockMvc.perform(get("/api/workbench/items.csv?status=DONE&closureStatus=CLOSED&keyword=History Write&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u95ed\u73af\u72b6\u6001")))
                .andExpect(content().string(containsString("\u4e0b\u4e00\u6b65")))
                .andExpect(content().string(containsString("\u5df2\u95ed\u73af")));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=EXECUTED&comparisonStatus=MISMATCHED&reviewStatus=REVIEWED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"comparisonReviewStatus\":\"REVIEWED\"")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"workflowStatus\":\"HISTORY_CLOSED\"")))
                .andExpect(content().string(containsString("\"comparisonStatus\":\"MISMATCHED\"")))
                .andExpect(content().string(containsString("\"comparisonReviewStatus\":\"REVIEWED\"")))
                .andExpect(content().string(containsString("\"comparisonReviewCategory\":\"BASE_CHANGED\"")))
                .andExpect(content().string(containsString("\"comparisonReviewReason\":\"unit-test comparison reviewed\"")))
                .andExpect(content().string(containsString("\"action\":\"history-write-comparison-review\"")));

        jdbcTemplate.update("UPDATE hisbase SET glgz2 = ? WHERE id = ?", 61, insertedId);

        mockMvc.perform(post("/api/workbench/history-write-plans/selected-retest-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNos\":[\"" + caseNo + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"matched\":1")))
                .andExpect(content().string(containsString("\"status\":\"MATCHED\"")));

        mockMvc.perform(post("/api/workbench/history-write-plans/selected-retest-approve")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNos\":[\"" + caseNo + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"success\":0")))
                .andExpect(content().string(containsString("\"skipped\":1")))
                .andExpect(content().string(containsString("\"status\":\"SKIPPED\"")))
                .andExpect(content().string(containsString("comparison has already been reviewed")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-comparison-retest")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"caseNo\":\"" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"totalMatched\":true")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-comparison-retest-approve")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"reviewStatus\":\"REVIEWED\"")))
                .andExpect(content().string(containsString("\"reviewCategory\":\"BASE_CHANGED\"")))
                .andExpect(content().string(containsString("\"reviewReason\"")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-plan")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"comparisonRetestStatus\":\"RETEST_MATCHED\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=EXECUTED&comparisonStatus=MATCHED&retestStatus=RETEST_MATCHED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"comparisonRetestStatus\":\"RETEST_MATCHED\"")));

        mockMvc.perform(get("/api/workbench/history-write-review-ledger?status=EXECUTED&comparisonStatus=MATCHED&retestStatus=RETEST_MATCHED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"retestMatched\":1")))
                .andExpect(content().string(containsString("\"byRetestStatus\"")))
                .andExpect(content().string(containsString("\"key\":\"RETEST_MATCHED\"")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-comparison-review")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewCategory\":\"BASE_CHANGED\",\"reviewReason\":\"unit-test comparison reviewed\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"reviewStatus\":\"REVIEWED\"")))
                .andExpect(content().string(containsString("\"reviewCategory\":\"BASE_CHANGED\"")))
                .andExpect(content().string(containsString("\"reviewReason\":\"unit-test comparison reviewed\"")))
                .andExpect(content().string(containsString("\"reviewedBy\":\"" + SCOPED_WORKBENCH_USER + "\"")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-plan")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"comparisonReviewStatus\":\"REVIEWED\"")))
                .andExpect(content().string(containsString("\"comparisonReviewCategory\":\"BASE_CHANGED\"")))
                .andExpect(content().string(containsString("\"comparisonReviewReason\":\"unit-test comparison reviewed\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=EXECUTED&comparisonStatus=MATCHED&reviewStatus=REVIEWED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"comparisonReviewStatus\":\"REVIEWED\"")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-audits")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"history-write-comparison-review\"")))
                .andExpect(content().string(containsString("unit-test comparison reviewed")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("already been executed")));

        Integer duplicateWriteCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM hisbase
                WHERE dwbm = '001'
                  AND grbm = '00055'
                  AND TRIM(jsnf) = '2099'
                  AND TRIM(jsyf) = '1'
                  /*
                  AND TRIM(jslb) = '娴嬭瘯鍐欏叆'
                  */
                """, Integer.class);
        org.junit.jupiter.api.Assertions.assertEquals(1, duplicateWriteCount);

        Integer insertedStillExists = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM hisbase
                WHERE id = ?
                """, Integer.class, insertedId);
        org.junit.jupiter.api.Assertions.assertEquals(1, insertedStillExists);

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-rollback-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rollbackable\":true")))
                .andExpect(content().string(containsString("\"historyId\":\"" + insertedId + "\"")))
                .andExpect(content().string(containsString("\"sidPlan\"")))
                .andExpect(content().string(containsString("\"confirmMessage\"")));

        String rollbackBatchPreviewBody = mockMvc.perform(post("/api/workbench/history-write-plans/batch-rollback-preview?status=EXECUTED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"rollbackable\":1")))
                .andExpect(content().string(containsString("\"blocked\":0")))
                .andExpect(content().string(containsString("\"historyId\":\"" + insertedId + "\"")))
                .andExpect(content().string(containsString("\"safetyToken\"")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String rollbackSafetyToken = jsonString(rollbackBatchPreviewBody, "safetyToken");
        String rollbackSafetyPreviewSummary = jdbcTemplate.queryForObject("""
                SELECT summary
                FROM sys_audit_log
                WHERE module_name = 'workbench'
                  AND action_name = 'history-write-batch-rollback-safety-preview'
                  AND target_type = 'HISTORY_WRITE_BATCH_SAFETY'
                  AND summary LIKE '%operation=ROLLBACK%'
                ORDER BY id DESC
                LIMIT 1
                """, String.class);
        org.junit.jupiter.api.Assertions.assertTrue(rollbackSafetyPreviewSummary.contains("status=PREVIEW_CREATED"));
        org.junit.jupiter.api.Assertions.assertTrue(rollbackSafetyPreviewSummary.contains("caseCount=1"));
        org.junit.jupiter.api.Assertions.assertFalse(rollbackSafetyPreviewSummary.contains(rollbackSafetyToken));

        mockMvc.perform(post("/api/workbench/history-write-plans/batch-rollback?status=EXECUTED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Batch rollback safety token is required")));

        mockMvc.perform(post("/api/workbench/history-write-plans/batch-rollback?status=EXECUTED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"safetyToken\":\"" + rollbackSafetyToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"batchNo\":\"HWB-ROLLBACK-")))
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"success\":1")))
                .andExpect(content().string(containsString("\"failed\":0")))
                .andExpect(content().string(containsString("\"skipped\":0")))
                .andExpect(content().string(containsString("\"status\":\"ROLLED_BACK\"")))
                .andExpect(content().string(containsString("\"historyId\":\"" + insertedId + "\"")))
                .andExpect(content().string(containsString("restore check passed")));
        String rollbackSafetyConsumeSummary = jdbcTemplate.queryForObject("""
                SELECT summary
                FROM sys_audit_log
                WHERE module_name = 'workbench'
                  AND action_name = 'history-write-batch-rollback-safety-consume'
                  AND target_type = 'HISTORY_WRITE_BATCH_SAFETY'
                  AND summary LIKE '%operation=ROLLBACK%'
                ORDER BY id DESC
                LIMIT 1
                """, String.class);
        org.junit.jupiter.api.Assertions.assertTrue(rollbackSafetyConsumeSummary.contains("status=CONSUMED"));
        org.junit.jupiter.api.Assertions.assertTrue(rollbackSafetyConsumeSummary.contains("tokenRef="));
        org.junit.jupiter.api.Assertions.assertFalse(rollbackSafetyConsumeSummary.contains(rollbackSafetyToken));

        Integer insertedCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM hisbase
                WHERE id = ?
                """, Integer.class, insertedId);
        org.junit.jupiter.api.Assertions.assertEquals(0, insertedCount);
        sourceSid = jdbcTemplate.queryForObject("""
                SELECT TRIM(COALESCE(sid, ''))
                FROM hisbase
                WHERE id = ?
                """, String.class, HISTORY_WRITE_SOURCE_ID);
        org.junit.jupiter.api.Assertions.assertEquals("", sourceSid);

        Map<String, Object> rolledBackPlan = jdbcTemplate.queryForMap("""
                SELECT plan_status, execution_result, inserted_history_id, rollback_message
                FROM salary_history_write_plan
                WHERE work_item_id = ?
                LIMIT 1
                """, HISTORY_WRITE_WORK_ITEM);
        org.junit.jupiter.api.Assertions.assertEquals("ROLLED_BACK", String.valueOf(rolledBackPlan.get("plan_status")));
        org.junit.jupiter.api.Assertions.assertEquals("ROLLED_BACK", String.valueOf(rolledBackPlan.get("execution_result")));
        org.junit.jupiter.api.Assertions.assertEquals(insertedId, String.valueOf(rolledBackPlan.get("inserted_history_id")));
        org.junit.jupiter.api.Assertions.assertTrue(String.valueOf(rolledBackPlan.get("rollback_message")).contains(insertedId));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-audits")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"history-write-batch-execute\"")))
                .andExpect(content().string(containsString("\"action\":\"history-write-batch-rollback\"")))
                .andExpect(content().string(containsString(insertedId)));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-audits.csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u5ba1\u8ba1ID")))
                .andExpect(content().string(containsString("history-write-batch-execute")))
                .andExpect(content().string(containsString("history-write-batch-rollback")))
                .andExpect(content().string(containsString(insertedId)));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-plan")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planStatus\":\"ROLLED_BACK\"")))
                .andExpect(content().string(containsString("\"executionResult\":\"ROLLED_BACK\"")))
                .andExpect(content().string(containsString("\"comparisonStatus\":\"ROLLED_BACK\"")))
                .andExpect(content().string(containsString("\"rollbackMessage\":")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"historyWritePlan\":")))
                .andExpect(content().string(containsString("\"historyWriteAudits\":")))
                .andExpect(content().string(containsString("\"planStatus\":\"ROLLED_BACK\"")))
                .andExpect(content().string(containsString("history-write-batch-rollback")))
                .andExpect(content().string(containsString(insertedId)));

        mockMvc.perform(post("/api/workbench/history-write-plans/selected-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNos\":[\"" + caseNo + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"ready\":1")))
                .andExpect(content().string(containsString("\"blocked\":0")))
                .andExpect(content().string(containsString("\"status\":\"READY\"")))
                .andExpect(content().string(containsString("\"writable\":true")))
                .andExpect(content().string(containsString("\"writePlanId\":\"HWP-" + caseNo + "\"")));

        Map<String, Object> rolledBackPlanAfterPreview = jdbcTemplate.queryForMap("""
                SELECT plan_status, execution_result, inserted_history_id, rolled_back_by, rollback_message
                FROM salary_history_write_plan
                WHERE work_item_id = ?
                LIMIT 1
                """, HISTORY_WRITE_WORK_ITEM);
        org.junit.jupiter.api.Assertions.assertEquals("PREPARED", String.valueOf(rolledBackPlanAfterPreview.get("plan_status")));
        org.junit.jupiter.api.Assertions.assertNull(rolledBackPlanAfterPreview.get("execution_result"));
        org.junit.jupiter.api.Assertions.assertNull(rolledBackPlanAfterPreview.get("inserted_history_id"));
        org.junit.jupiter.api.Assertions.assertNull(rolledBackPlanAfterPreview.get("rolled_back_by"));
        org.junit.jupiter.api.Assertions.assertNull(rolledBackPlanAfterPreview.get("rollback_message"));

        String reexecuteBody = mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"EXECUTED\"")))
                .andExpect(content().string(containsString("\"historyId\"")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String reinsertedId = jsonString(reexecuteBody, "historyId");
        org.junit.jupiter.api.Assertions.assertNotEquals(insertedId, reinsertedId);

        Integer reinsertedCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM hisbase
                WHERE id = ?
                """, Integer.class, reinsertedId);
        org.junit.jupiter.api.Assertions.assertEquals(1, reinsertedCount);

        Map<String, Object> repreparedExecutedPlan = jdbcTemplate.queryForMap("""
                SELECT plan_status, execution_result, inserted_history_id
                FROM salary_history_write_plan
                WHERE work_item_id = ?
                LIMIT 1
                """, HISTORY_WRITE_WORK_ITEM);
        org.junit.jupiter.api.Assertions.assertEquals("EXECUTED", String.valueOf(repreparedExecutedPlan.get("plan_status")));
        org.junit.jupiter.api.Assertions.assertEquals("SUCCESS", String.valueOf(repreparedExecutedPlan.get("execution_result")));
        org.junit.jupiter.api.Assertions.assertEquals(reinsertedId, String.valueOf(repreparedExecutedPlan.get("inserted_history_id")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-rollback")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"ROLLED_BACK\"")))
                .andExpect(content().string(containsString("\"historyId\":\"" + reinsertedId + "\"")));

        Integer reinsertedAfterRollbackCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM hisbase
                WHERE id = ?
                """, Integer.class, reinsertedId);
        org.junit.jupiter.api.Assertions.assertEquals(0, reinsertedAfterRollbackCount);
    }

    @Test
    void rankAllowanceHistoryWritePreviewEntersBlockedQueueWithJxjtMapping() throws Exception {
        ensureSnapshotTableForTest();
        jdbcTemplate.update("""
                INSERT INTO salary_business_case(case_no, work_item_id, source, status, business_type,
                                                 person_code, person_name, org_code, event_year, event_month,
                                                 title, summary, trial_status, trial_matched,
                                                 trial_baseline_total, trial_calculated_total, trial_expected_total,
                                                 trial_difference, trial_summary, trial_changes_json,
                                                 review_status, handled_by)
                VALUES (?, ?, 'SALARY_EVENT', 'DONE', '审判津贴',
                        '001-00055', 'Rank Allowance', '001', 2099, 2,
                        '审判津贴', 'unit-test rank allowance history write', 'DIFFERENT', 0,
                        1200, 1350, 1320, 30, 'unit-test rank allowance difference',
                        '[{"itemCode":"JXJT","itemName":"警衔、法检、监察津贴","beforeAmount":100,"afterAmount":130,"difference":30}]',
                        'PENDING', ?)
                """, RANK_ALLOWANCE_CASE_NO, RANK_ALLOWANCE_WORK_ITEM, SCOPED_WORKBENCH_USER);
        jdbcTemplate.update("""
                INSERT INTO salary_business_case_snapshot(case_no, work_item_id, person_code, org_code,
                                                          event_year, event_month, business_type, trial_status,
                                                          trial_matched, trial_difference, trial_baseline_total,
                                                          trial_calculated_total, trial_expected_total,
                                                          trial_changes_json, salary_items_json, snapshot_json,
                                                          snapshot_by)
                VALUES (?, ?, '001-00055', '001', 2099, 2, '审判津贴', 'DIFFERENT',
                        0, 30, 1200, 1350, 1320,
                        '[{"itemCode":"JXJT","itemName":"警衔、法检、监察津贴","beforeAmount":100,"afterAmount":130,"difference":30}]',
                        '[{"itemCode":"JXJT","itemName":"警衔、法检、监察津贴","amount":130,"ruleNote":"unit-test rank allowance"}]',
                        '{"workItemId":"tmp-test-rank-allowance-write","trialCalculatedTotal":1350,"salaryItems":[{"itemCode":"JXJT","amount":130}]}',
                        ?)
                """, RANK_ALLOWANCE_CASE_NO, RANK_ALLOWANCE_WORK_ITEM, SCOPED_WORKBENCH_USER);

        mockMvc.perform(post("/api/workbench/salary-cases/" + RANK_ALLOWANCE_CASE_NO + "/history-write-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"workItemId\":\"" + RANK_ALLOWANCE_WORK_ITEM + "\"")))
                .andExpect(content().string(containsString("\"status\":\"BLOCKED\"")))
                .andExpect(content().string(containsString("\"writable\":false")))
                .andExpect(content().string(containsString("\"itemCode\":\"JXJT\"")))
                .andExpect(content().string(containsString("\"historyField\":\"jxjt\"")))
                .andExpect(content().string(containsString("trial risk must be reviewed")));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=PREPARED&pendingQueue=blocked&keyword=" + RANK_ALLOWANCE_CASE_NO + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + RANK_ALLOWANCE_CASE_NO + "\"")))
                .andExpect(content().string(containsString("\"previewStatus\":\"BLOCKED\"")))
                .andExpect(content().string(containsString("\"nextActionCode\":\"VIEW_PLAN\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans?actionCode=VIEW_PLAN&keyword=" + RANK_ALLOWANCE_CASE_NO + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + RANK_ALLOWANCE_CASE_NO + "\"")))
                .andExpect(content().string(containsString("\"nextActionCode\":\"VIEW_PLAN\"")));
        mockMvc.perform(get("/api/workbench/history-write-plans.csv?actionCode=VIEW_PLAN&keyword=" + RANK_ALLOWANCE_CASE_NO + "&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("HWP-" + RANK_ALLOWANCE_CASE_NO)))
                .andExpect(content().string(containsString("VIEW_PLAN")));

        mockMvc.perform(get("/api/workbench/history-write-pending-queues?keyword=" + RANK_ALLOWANCE_CASE_NO)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":\"blocked\"")));

        String blockedRankAllowanceSelectedPreviewBody = mockMvc.perform(post("/api/workbench/history-write-plans/selected-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNos\":[\"" + RANK_ALLOWANCE_CASE_NO + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"blocked\":1")))
                .andExpect(content().string(containsString("\"safetyToken\"")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String blockedRankAllowanceSafetyToken = jsonString(blockedRankAllowanceSelectedPreviewBody, "safetyToken");

        mockMvc.perform(post("/api/workbench/history-write-plans/selected-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNos\":[\"" + RANK_ALLOWANCE_CASE_NO + "\"],\"safetyToken\":\"bad-rank-allowance-token\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Batch write safety token is invalid")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + RANK_ALLOWANCE_CASE_NO + "/review")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reviewReason":"unit-test rank allowance reviewed"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"reviewStatus\":\"REVIEWED\"")));
        insertPrintedApprovalReport(RANK_ALLOWANCE_CASE_NO);

        mockMvc.perform(post("/api/workbench/history-write-plans/selected-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNos\":[\"" + RANK_ALLOWANCE_CASE_NO + "\"],\"safetyToken\":\"" + blockedRankAllowanceSafetyToken + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Batch write preview has changed")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + RANK_ALLOWANCE_CASE_NO + "/history-write-confirm")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"READY\"")))
                .andExpect(content().string(containsString("\"writable\":true")))
                .andExpect(content().string(containsString("\"executable\":true")))
                .andExpect(content().string(containsString("\"fieldCount\":1")))
                .andExpect(content().string(containsString("\"itemCode\":\"JXJT\"")))
                .andExpect(content().string(containsString("\"historyField\":\"jxjt\"")))
                .andExpect(content().string(not(containsString("trial risk must be reviewed"))));

        mockMvc.perform(post("/api/workbench/salary-cases/" + RANK_ALLOWANCE_CASE_NO + "/history-write-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"EXECUTED\"")))
                .andExpect(content().string(containsString("Inserted hisbase row")));

        Map<String, Object> rankAllowancePlan = jdbcTemplate.queryForMap("""
                SELECT plan_status, execution_result, inserted_history_id
                FROM salary_history_write_plan
                WHERE work_item_id = ?
                LIMIT 1
                """, RANK_ALLOWANCE_WORK_ITEM);
        String insertedRankAllowanceHistoryId = String.valueOf(rankAllowancePlan.get("inserted_history_id"));
        org.junit.jupiter.api.Assertions.assertEquals("EXECUTED", String.valueOf(rankAllowancePlan.get("plan_status")));
        org.junit.jupiter.api.Assertions.assertEquals("SUCCESS", String.valueOf(rankAllowancePlan.get("execution_result")));
        org.junit.jupiter.api.Assertions.assertFalse(insertedRankAllowanceHistoryId.isBlank());

        Map<String, Object> insertedRankAllowanceHistory = jdbcTemplate.queryForMap("""
                SELECT TRIM(jsnf) AS year, TRIM(jsyf) AS month, hj2, jxjt
                FROM hisbase
                WHERE id = ? OR TRIM(id) = ?
                LIMIT 1
                """, insertedRankAllowanceHistoryId, insertedRankAllowanceHistoryId);
        org.junit.jupiter.api.Assertions.assertEquals("2099", String.valueOf(insertedRankAllowanceHistory.get("year")));
        org.junit.jupiter.api.Assertions.assertEquals("2", String.valueOf(insertedRankAllowanceHistory.get("month")));
        org.junit.jupiter.api.Assertions.assertEquals(130, ((Number) insertedRankAllowanceHistory.get("jxjt")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals(130, ((Number) insertedRankAllowanceHistory.get("hj2")).intValue());

        mockMvc.perform(get("/api/workbench/salary-cases/" + RANK_ALLOWANCE_CASE_NO + "/history-write-comparison")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"insertedHistoryId\":\"" + insertedRankAllowanceHistoryId + "\"")))
                .andExpect(content().string(containsString("\"historyField\":\"jxjt\"")))
                .andExpect(content().string(containsString("\"expectedAmount\":130")))
                .andExpect(content().string(containsString("\"actualAmount\":130")))
                .andExpect(content().string(containsString("\"matched\":true")))
                .andExpect(content().string(containsString("\"totalMatched\":true")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + RANK_ALLOWANCE_CASE_NO + "/history-write-rollback-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"rollbackable\":true")))
                .andExpect(content().string(containsString("\"historyId\":\"" + insertedRankAllowanceHistoryId + "\"")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + RANK_ALLOWANCE_CASE_NO + "/history-write-rollback")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"ROLLED_BACK\"")))
                .andExpect(content().string(containsString(insertedRankAllowanceHistoryId)));

        Integer rolledBackRankAllowanceCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM hisbase
                WHERE id = ? OR TRIM(id) = ?
                """, Integer.class, insertedRankAllowanceHistoryId, insertedRankAllowanceHistoryId);
        org.junit.jupiter.api.Assertions.assertEquals(0, rolledBackRankAllowanceCount);
    }

    @Test
    void historyWriteExecuteBlocksWhenSidChainChangesAfterPreview() throws Exception {
        insertTemporaryHistoryTemplate();
        String caseNo = HISTORY_WRITE_CASE_NO;

        jdbcTemplate.update("""
                INSERT INTO salary_business_case(case_no, work_item_id, source, status, business_type,
                                                 person_code, person_name, org_code, event_year, event_month,
                                                 title, summary, trial_status, trial_matched,
                                                 trial_baseline_total, trial_calculated_total, trial_expected_total,
                                                 trial_difference, trial_summary, trial_changes_json,
                                                 review_status, handled_by)
                VALUES (?, ?, 'SALARY_EVENT', 'DONE', '娴嬭瘯鍐欏叆',
                        '001-00055', 'History Write', '001', 2099, 1,
                        'History Write', 'unit-test history write sid changed', 'MATCH', 1,
                        1200, 1290, 1290, 0, 'unit-test history write sid changed', '[]',
                        'PENDING', ?)
                """, caseNo, HISTORY_WRITE_WORK_ITEM, SCOPED_WORKBENCH_USER);
        ensureSnapshotTableForTest();
        jdbcTemplate.update("""
                INSERT INTO salary_business_case_snapshot(case_no, work_item_id, person_code, org_code,
                                                          event_year, event_month, business_type, trial_status,
                                                          trial_matched, trial_difference, trial_baseline_total,
                                                          trial_calculated_total, trial_expected_total,
                                                          trial_changes_json, salary_items_json, snapshot_json,
                                                          snapshot_by)
                VALUES (?, ?, '001-00055', '001', 2099, 1, '娴嬭瘯鍐欏叆', 'MATCH',
                        1, 0, 1200, 1290, 1290,
                        '[]',
                        '[{"itemCode":"JCGZ2","itemName":"鍩虹宸ヨ祫","amount":1234,"ruleNote":"unit-test"},{"itemCode":"GLGZ2","itemName":"宸ラ緞宸ヨ祫","amount":56,"ruleNote":"unit-test"}]',
                        '{"workItemId":"tmp-test-history-write-success","trialCalculatedTotal":1290,"salaryItems":[{"itemCode":"JCGZ2","amount":1234},{"itemCode":"GLGZ2","amount":56}]}',
                        ?)
                """, caseNo, HISTORY_WRITE_WORK_ITEM, SCOPED_WORKBENCH_USER);

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"WARNING\"")))
                .andExpect(content().string(containsString("\"writable\":true")));

        jdbcTemplate.update("UPDATE hisbase SET sid = 'TMP-HIS-CHANGED-SID' WHERE id = ?", HISTORY_WRITE_SOURCE_ID);

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("History write preview is not writable")));

        Integer insertedCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM hisbase
                WHERE dwbm = '001'
                  AND grbm = '00055'
                  AND TRIM(jsnf) = '2099'
                  AND TRIM(jsyf) = '1'
                  AND TRIM(jslb) = '娴嬭瘯鍐欏叆'
                """, Integer.class);
        org.junit.jupiter.api.Assertions.assertEquals(0, insertedCount);

        Map<String, Object> plan = jdbcTemplate.queryForMap("""
                SELECT plan_status, execution_result, execution_message, inserted_history_id
                FROM salary_history_write_plan
                WHERE work_item_id = ?
                LIMIT 1
                """, HISTORY_WRITE_WORK_ITEM);
        org.junit.jupiter.api.Assertions.assertEquals("PREPARED", String.valueOf(plan.get("plan_status")));
        org.junit.jupiter.api.Assertions.assertEquals("BLOCKED", String.valueOf(plan.get("execution_result")));
        org.junit.jupiter.api.Assertions.assertTrue(String.valueOf(plan.get("execution_message")).contains("not writable"));
        org.junit.jupiter.api.Assertions.assertEquals("null", String.valueOf(plan.get("inserted_history_id")));
    }

    @Test
    void salaryDifferentTrialRequiresDifferenceReason() throws Exception {
        Optional<DifferentTrialSample> sample = differentTrialSample();
        assumeTrue(sample.isPresent(), "No DIFFERENT salary trial sample in org 001.");
        DifferentTrialSample item = sample.get();

        mockMvc.perform(post("/api/workbench/salary-cases")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-difference","source":"SALARY_EVENT","businessType":"%s","personCode":"%s","personName":"Difference Test","orgCode":"%s","year":%d,"month":%d,"title":"Difference Test","summary":"unit-test salary case difference"}
                                """.formatted(item.changeType(), item.personCode(), item.orgCode(), item.year(), item.month())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        mockMvc.perform(post("/api/workbench/salary-cases")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-difference","source":"SALARY_EVENT","businessType":"%s","personCode":"%s","personName":"Difference Test","orgCode":"%s","year":%d,"month":%d,"title":"Difference Test","summary":"unit-test salary case difference","differenceReason":"unit-test difference reason"}
                                """.formatted(item.changeType(), item.personCode(), item.orgCode(), item.year(), item.month())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"DONE\"")));

        String caseNo = caseNo("tmp-test-salary-case-difference");
        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"trialStatus\":\"DIFFERENT\"")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"PENDING\"")))
                .andExpect(content().string(containsString("\"differenceReason\":\"unit-test difference reason\"")))
                .andExpect(content().string(containsString("differenceReason=unit-test difference reason")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&trialStatus=DIFFERENT&keyword=unit-test salary case difference&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"trialStatus\":\"DIFFERENT\"")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&trialStatus=DIFFERENT&reviewStatus=PENDING&keyword=unit-test salary case difference&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"PENDING\"")));
    }

    @Test
    void salaryCaseCompletionRespectsOrganizationScope() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-cases/preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-denied-preview","source":"SALARY_EVENT","businessType":"姝ｅ父妗ｆ","personCode":"00806-00868","personName":"娴嬭瘯浜哄憳","orgCode":"00806","year":2026,"month":1,"title":"姝ｅ父妗ｆ鍔炵悊","summary":"unit-test denied salary case preview"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/workbench/salary-cases")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-denied","source":"SALARY_EVENT","businessType":"正常档次","personCode":"00806-00868","personName":"测试人员","orgCode":"00806","year":2026,"month":1,"title":"正常档次办理","summary":"unit-test denied salary case"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void salaryCaseDetailRespectsOrganizationScope() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO salary_business_case(case_no, work_item_id, source, status, business_type,
                                                 person_code, person_name, org_code, event_year, event_month,
                                                 title, summary, handled_by)
                VALUES ('GZ-UNIT-TEST-DENIED', 'tmp-test-denied-detail', 'SALARY_EVENT', 'DONE', '姝ｅ父妗ｆ',
                        '00806-00868', 'Denied Detail', '00806', 2026, 1,
                        'Denied Detail', 'unit-test denied detail', 'admin')
                """);

        mockMvc.perform(get("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/cancel")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cancelReason":"denied"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/review")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reviewReason":"denied"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/snapshot")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/history-write-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/history-write-confirm")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/history-write-plan")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/history-write-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/history-write-rollback")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/history-write-rollback-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void roleTemplatesRequireRolePermissionAndCanApplyTemplate() throws Exception {
        mockMvc.perform(get("/api/system/role-templates")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, WORKBENCH_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/system/role-templates")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SALARY_OPERATOR")))
                .andExpect(content().string(containsString("SALARY_HISTORY_WRITE")))
                .andExpect(content().string(containsString("SALARY_HISTORY_ROLLBACK")));

        mockMvc.perform(put("/api/system/roles/" + WORKBENCH_ROLE + "/template/SALARY_VIEWER")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SALARY_PERSON")))
                .andExpect(content().string(containsString("SALARY_DONE")));
    }

    @Test
    void createUserCanAssignInitialRolesAndOrganizations() throws Exception {
        mockMvc.perform(post("/api/system/users")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"tmp_test_created_user","displayName":"Created User","roleCodes":["%s"],"orgCodes":["001","00111"]}
                                """.formatted(WORKBENCH_ROLE)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(CREATED_USER)))
                .andExpect(content().string(containsString(WORKBENCH_ROLE)))
                .andExpect(content().string(containsString("\"001\"")));

        Integer roleCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys_user_role
                WHERE username = ? AND role_code = ?
                """, Integer.class, CREATED_USER, WORKBENCH_ROLE);
        Integer orgCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys_user_org
                WHERE username = ? AND org_code = '001'
                """, Integer.class, CREATED_USER);
        Integer childOrgCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys_user_org
                WHERE username = ? AND org_code = '00111'
                """, Integer.class, CREATED_USER);

        org.junit.jupiter.api.Assertions.assertEquals(1, roleCount);
        org.junit.jupiter.api.Assertions.assertEquals(1, orgCount);
        org.junit.jupiter.api.Assertions.assertEquals(0, childOrgCount);
    }

    @Test
    void authorizationChangesAreAudited() throws Exception {
        mockMvc.perform(put("/api/system/roles/" + WORKBENCH_ROLE + "/template/SALARY_VIEWER")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/system/users")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"tmp_test_created_user","displayName":"Created User","roleCodes":["%s"],"orgCodes":["001"]}
                                """.formatted(WORKBENCH_ROLE)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/system/users/" + CREATED_USER + "/orgs")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orgCodes":["001","00111"]}
                                """))
                .andExpect(status().isOk());

        assertAudit("role-template", "ROLE", WORKBENCH_ROLE, "ADMIN", "SALARY_VIEWER");
        assertAudit("user-create", "USER", CREATED_USER, "ADMIN", "Created User");
        assertAudit("user-roles", "USER", CREATED_USER, "ADMIN", WORKBENCH_ROLE);
        assertAudit("user-orgs", "USER", CREATED_USER, "ADMIN", "001");
    }

    @Test
    void organizationScopeRestrictsPeopleApis() throws Exception {
        mockMvc.perform(get("/api/persons?orgCode=001&page=1&size=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/persons?orgCode=00806&page=1&size=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/persons/001-00055")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/persons/00806-00868")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizationScopeRestrictsOrganizationTree() throws Exception {
        mockMvc.perform(get("/api/org/tree")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"orgCode\":\"001\"")))
                .andExpect(content().string(containsString("\"orgCode\":\"00111\"")))
                .andExpect(content().string(not(containsString("\"orgCode\":\"00806\""))));
    }

    @Test
    void organizationScopeRestrictsSalaryApis() throws Exception {
        mockMvc.perform(get("/api/salary/periods?orgCode=001&limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/salary/periods?orgCode=00806&limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/salary/history/001-00055")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/salary/history/00806-00868")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizationScopeRestrictsSalaryHistoryDetails() throws Exception {
        String allowedHistoryId = historyId("001", "00055");
        String deniedHistoryId = historyId("00806", "00868");

        mockMvc.perform(get("/api/salary/history-records/" + allowedHistoryId)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/salary/history-records/" + deniedHistoryId)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizationScopeRestrictsSalaryActionCommands() throws Exception {
        mockMvc.perform(post("/api/salary/trial-calc")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TRIAL_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personCode":"00806-00868","orgCode":"00806","year":2024,"month":11,"changeType":"见习工资"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/salary/reconcile")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, RECONCILE_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personCode":"00806-00868","orgCode":"00806","year":2024,"month":11,"changeType":"见习工资"}
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void generatedTimelineBatchRespectsOrganizationScopeAndExportsCsv() throws Exception {
        mockMvc.perform(get("/api/salary/timeline-generated-batch?orgCode=001&keyword=00105-00008&limit=2&eventLimit=8")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TRIAL_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"orgCode\":\"001\"")))
                .andExpect(content().string(containsString("\"checkedCount\":1")))
                .andExpect(content().string(containsString("\"personCode\":\"00105-00008\"")))
                .andExpect(content().string(containsString("\"status\":\"OK\"")))
                .andExpect(content().string(not(containsString("\"personCode\":\"00806-"))));

        mockMvc.perform(get("/api/salary/timeline-generated-batch.csv?orgCode=001&keyword=00105-00008&limit=2&eventLimit=8")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u4eba\u5458\u7f16\u7801")))
                .andExpect(content().string(containsString("00105-00008")))
                .andExpect(content().string(containsString("OK")));

        mockMvc.perform(get("/api/salary/timeline-generated-batch?orgCode=00806&limit=1&eventLimit=8")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TRIAL_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/salary/timeline-generated-batch.csv?orgCode=00806&limit=1&eventLimit=8")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void generatedTimelineIssueTodoCanBeReviewedAndRetested() throws Exception {
        mockMvc.perform(post("/api/workbench/generated-timeline-issues/refresh?orgCode=001&keyword=00105-00008&limit=1&eventLimit=8")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SALARY_TODO")));

        insertGeneratedTimelineTodo("generated-timeline-unit-test", "unit-test generated issue");

        mockMvc.perform(get("/api/workbench/items?status=TODO&keyword=unit-test generated issue&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"source\":\"GENERATED_TIMELINE\"")))
                .andExpect(content().string(containsString("generated-timeline-unit-test")));

        mockMvc.perform(post("/api/workbench/generated-timeline-issues/generated-timeline-unit-test/retest")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"workItemId\":\"generated-timeline-unit-test\"")))
                .andExpect(content().string(containsString("\"retestStatus\":")));

        insertGeneratedTimelineTodo("generated-timeline-unit-test-review", "unit-test generated issue review");

        mockMvc.perform(post("/api/workbench/generated-timeline-issues/generated-timeline-unit-test-review/review")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reviewStatus":"IGNORED","reviewReason":"unit-test generated issue ignored"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"reviewStatus\":\"IGNORED\"")))
                .andExpect(content().string(containsString("unit-test generated issue ignored")));

        mockMvc.perform(get("/api/workbench/items?status=TODO&keyword=unit-test generated issue review&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("generated-timeline-unit-test-review"))));
    }

    @Test
    void migrationSupportEndpointsCoverFormsApplicationGovernanceReportsAndAcceptance() throws Exception {
        mockMvc.perform(get("/api/workbench/salary-business-forms")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("NORMAL_GRADE")))
                .andExpect(content().string(containsString("GENERATED_TIMELINE_REVIEW")));

        mockMvc.perform(get("/api/workbench/salary-business-flows")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ENTRY_PROBATION")))
                .andExpect(content().string(containsString("PUNISHMENT_REDUCTION")))
                .andExpect(content().string(containsString("RETIREMENT_DEFERRED")));

        mockMvc.perform(get("/api/workbench/salary-rule-maintenance")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("STANDARD_TABLES")))
                .andExpect(content().string(containsString("FIELD_MAPPING")));

        mockMvc.perform(get("/api/workbench/migration-readiness")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CORE_READY")))
                .andExpect(content().string(containsString("\"code\":\"10\"")))
                .andExpect(content().string(containsString("DEFERRED")));

        mockMvc.perform(get("/api/workbench/summary")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SALARY_CLOSURE_PENDING")))
                .andExpect(content().string(containsString("SALARY_CLOSURE_BLOCKED")))
                .andExpect(content().string(containsString("SALARY_CLOSURE_CLOSED")))
                .andExpect(content().string(containsString("SALARY_NEXT_REVIEW_TRIAL")))
                .andExpect(content().string(containsString("SALARY_NEXT_PRINT_OR_PLAN")))
                .andExpect(content().string(containsString("SALARY_NEXT_EXECUTE_WRITE")))
                .andExpect(content().string(containsString("SALARY_NEXT_REVIEW_DIFFERENCE")));

        mockMvc.perform(post("/api/workbench/normal-grade-applications/preview?orgCode=001&year=2024&month=1&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"checkedCount\":")))
                .andExpect(content().string(containsString("\"eligibleCount\":")))
                .andExpect(content().string(containsString("\"items\":")));

        mockMvc.perform(post("/api/workbench/normal-grade-applications/generate?orgCode=001&year=2024&month=1&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"generatedCount\":")))
                .andExpect(content().string(containsString("\"eligibleCount\":")));

        mockMvc.perform(post("/api/workbench/entry-salary-applications/preview?orgCode=001&year=2024&month=1&limit=3&changeType=%E6%96%B0%E8%BF%9B%E5%B7%A5%E8%B5%84")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"probationaryCount\":")))
                .andExpect(content().string(containsString("\"regularizationCount\":")))
                .andExpect(content().string(containsString("\"eligibleCount\":")));

        mockMvc.perform(post("/api/workbench/entry-salary-applications/generate?orgCode=001&year=2024&month=1&limit=3&changeType=%E6%96%B0%E8%BF%9B%E5%B7%A5%E8%B5%84")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"generatedCount\":")))
                .andExpect(content().string(containsString("\"eligibleCount\":")));

        mockMvc.perform(post("/api/workbench/post-change-applications/preview?orgCode=001&year=2024&month=1&limit=3&changeType=%E8%81%8C%E5%8A%A1%E5%8F%98%E5%8C%96")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"civilPostCount\":")))
                .andExpect(content().string(containsString("\"rankPromotionCount\":")))
                .andExpect(content().string(containsString("\"eligibleCount\":")));

        mockMvc.perform(post("/api/workbench/post-change-applications/generate?orgCode=001&year=2024&month=1&limit=3&changeType=%E8%81%8C%E5%8A%A1%E5%8F%98%E5%8C%96")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"generatedCount\":")))
                .andExpect(content().string(containsString("\"eligibleCount\":")));

        mockMvc.perform(post("/api/workbench/allowance-change-applications/preview?orgCode=001&year=2024&month=1&limit=3&changeType=%E6%B4%A5%E8%B4%B4%E5%8F%98%E5%8C%96")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"standardAdjustmentCount\":")))
                .andExpect(content().string(containsString("\"teacherNurseAllowanceCount\":")))
                .andExpect(content().string(containsString("\"eligibleCount\":")));

        mockMvc.perform(post("/api/workbench/allowance-change-applications/generate?orgCode=001&year=2024&month=1&limit=3&changeType=%E6%B4%A5%E8%B4%B4%E5%8F%98%E5%8C%96")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"generatedCount\":")))
                .andExpect(content().string(containsString("\"eligibleCount\":")));

        mockMvc.perform(post("/api/workbench/transfer-salary-applications/preview?orgCode=001&year=2024&month=1&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"transferCount\":")))
                .andExpect(content().string(containsString("\"demobilizedCadreCount\":")))
                .andExpect(content().string(containsString("\"eligibleCount\":")));

        mockMvc.perform(post("/api/workbench/transfer-salary-applications/generate?orgCode=001&year=2024&month=1&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"generatedCount\":")))
                .andExpect(content().string(containsString("\"eligibleCount\":")));

        mockMvc.perform(post("/api/workbench/punishment-reduction-applications/preview?orgCode=001&year=2024&month=1&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"punishmentCount\":")))
                .andExpect(content().string(containsString("\"rewardCount\":")))
                .andExpect(content().string(containsString("\"eligibleCount\":")));

        mockMvc.perform(post("/api/workbench/punishment-reduction-applications/generate?orgCode=001&year=2024&month=1&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"generatedCount\":")))
                .andExpect(content().string(containsString("\"eligibleCount\":")));

        mockMvc.perform(post("/api/workbench/application-cases")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-application-case","source":"APPLICATION_CASE","businessType":"unit-test application","personCode":"001-00055","personName":"Application Test","orgCode":"001","year":2026,"month":1,"title":"Application Test","summary":"unit-test application create"}
                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"TODO\"")))
                .andExpect(content().string(containsString("\"source\":\"APPLICATION_CASE\"")))
                .andExpect(content().string(containsString("\"workflowStatus\":\"APPLICATION_TODO\"")));

        String appCaseNo = jdbcTemplate.queryForObject("""
                SELECT case_no
                FROM application_case
                WHERE summary = 'unit-test application create'
                ORDER BY id DESC
                LIMIT 1
                """, String.class);

        mockMvc.perform(post("/api/workbench/application-cases/" + appCaseNo + "/complete")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reviewReason":"unit-test application done"}
                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"DONE\"")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"REVIEWED\"")))
                .andExpect(content().string(containsString("\"workflowStatus\":\"APPLICATION_DONE\"")));

        mockMvc.perform(get("/api/workbench/application-cases?status=DONE&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(appCaseNo)));

        mockMvc.perform(get("/api/workbench/data-governance/scan?orgCode=001&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"orgCode\":\"001\"")))
                .andExpect(content().string(containsString("\"issueCount\":")))
                .andExpect(content().string(containsString("\"standardReviewCount\":")))
                .andExpect(content().string(containsString("\"blockedHistoryReviewCount\":")))
                .andExpect(content().string(containsString("\"specialHistoryReviewCount\":")))
                .andExpect(content().string(containsString("\"retirementDeferredNote\":")));

        mockMvc.perform(get("/api/workbench/data-governance/scan.csv?orgCode=001&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u95ee\u9898\u6570")));

        mockMvc.perform(get("/api/reports/migration-closure?orgCode=001&year=2024&month=1&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":")))
                .andExpect(content().string(containsString("\"catalogTotal\":")))
                .andExpect(content().string(containsString("\"pendingReports\":")))
                .andExpect(content().string(containsString("\"archiveTotal\":")))
                .andExpect(content().string(containsString("\"batchSummary\":")))
                .andExpect(content().string(containsString("\"auditSummary\":")));
        mockMvc.perform(get("/api/reports/migration-closure.csv?orgCode=001&year=2024&month=1&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("salary-report-migration-closure-001.csv")))
                .andExpect(content().string(containsString("summary")))
                .andExpect(content().string(containsString("pendingReports")))
                .andExpect(content().string(containsString("auditCount")));
        mockMvc.perform(get("/api/reports/audits?action=report-migration-closure-csv&targetCode=001&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("report-migration-closure-csv")))
                .andExpect(content().string(containsString("REPORT_MIGRATION_CLOSURE")));

        mockMvc.perform(get("/api/workbench/migration-acceptance")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("READY_FOR_CORE_ACCEPTANCE")))
                .andExpect(content().string(containsString("1-application-flow")))
                .andExpect(content().string(containsString("10-launch-readiness")));

        mockMvc.perform(get("/api/workbench/migration-regression-samples?orgCode=001&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"runNo\":\"MIG-REG-")))
                .andExpect(content().string(containsString("\"sampleCount\":10")))
                .andExpect(content().string(containsString("normal-grade")))
                .andExpect(content().string(containsString("history-write-executed")))
                .andExpect(content().string(containsString("report-print")));

        mockMvc.perform(post("/api/workbench/migration-regression/run?orgCode=001&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"runNo\":\"MIG-REG-RUN-")))
                .andExpect(content().string(containsString("\"overallStatus\":")))
                .andExpect(content().string(containsString("\"samples\":")))
                .andExpect(content().string(containsString("data-governance")));

        mockMvc.perform(post("/api/workbench/migration-regression/sample-library/refresh?orgCode=001&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"refreshedCount\":")))
                .andExpect(content().string(containsString("\"libraryCount\":")));

        mockMvc.perform(get("/api/workbench/migration-regression/sample-library?orgCode=001&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/workbench/migration-regression/sample-library?orgCode=001&sampleCode=normal-grade&enabled=true&keyword=001&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/workbench/migration-regression/sample-library.csv?orgCode=001&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("sampleCode,sampleTitle")))
                .andExpect(content().string(containsString("expectedStatus")));

        MockMultipartFile regressionImportFile = new MockMultipartFile(
                "file",
                "migration-regression-samples.csv",
                "text/csv",
                """
                        sampleCode,sampleTitle,sampleDomain,sampleId,personCode,personName,orgCode,sampleType,batchNo,sampleSource,expectedStatus,expectedAmount,expectedPayload,enabled,note
                        unit-import,Imported Sample,test,UNIT-IMPORT-1,001-00055,Imported Person,001,UNIT,unit-batch,UNIT_TEST,PRESENT,12.50,IMPORT,true,unit import
                        """.getBytes()
        );
        mockMvc.perform(multipart("/api/workbench/migration-regression/sample-library/import")
                        .file(regressionImportFile)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .param("orgCode", "001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"importedCount\":1")))
                .andExpect(content().string(containsString("\"libraryCount\":")));

        mockMvc.perform(get("/api/workbench/migration-regression/sample-library?orgCode=001&batchNo=unit-batch&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"batchNo\":\"unit-batch\"")))
                .andExpect(content().string(containsString("\"sampleSource\":\"UNIT_TEST\"")));

        List<Map<String, Object>> regressionSamples = jdbcTemplate.queryForList("""
                SELECT sample_code, sample_id, person_code, org_code
                FROM migration_regression_sample
                WHERE org_code LIKE '001%'
                  AND sample_code <> 'unit-test'
                LIMIT 1
                """);
        if (regressionSamples.isEmpty()) {
            jdbcTemplate.update("""
                    INSERT INTO salary_business_case(case_no, work_item_id, source, status, business_type,
                                                     person_code, person_name, org_code, event_year, event_month,
                                                     title, summary, trial_status, trial_expected_total, trial_summary)
                    VALUES ('UNIT-SAMPLE-1', 'unit-regression-sample-1', 'UNIT_TEST', 'DONE', 'unit-test',
                            '001-00055', 'Regression Sample Test', '001', 2026, 1,
                            'Unit Regression Sample', 'unit-test regression sample source', 'MATCHED', 100.00, 'unit')
                    ON DUPLICATE KEY UPDATE trial_status = VALUES(trial_status),
                                            trial_expected_total = VALUES(trial_expected_total),
                                            trial_summary = VALUES(trial_summary)
                    """);
            jdbcTemplate.update("""
                    INSERT INTO migration_regression_sample(sample_code, sample_title, sample_domain, sample_id,
                                                            person_code, person_name, org_code, sample_type,
                                                            expected_status, expected_payload)
                    VALUES ('unit-test', 'Unit Test', 'test', 'UNIT-SAMPLE-1',
                            '001-00055', 'Regression Sample Test', '001', 'UNIT',
                            'PRESENT', 'UNIT')
                    ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP
                    """);
            regressionSamples = jdbcTemplate.queryForList("""
                    SELECT sample_code, sample_id, person_code, org_code
                    FROM migration_regression_sample
                    WHERE sample_code = 'unit-test'
                    LIMIT 1
                    """);
        }
        Map<String, Object> regressionSample = regressionSamples.getFirst();
        mockMvc.perform(post("/api/workbench/migration-regression/sample-library")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .param("orgCode", String.valueOf(regressionSample.get("org_code")))
                        .param("sampleCode", String.valueOf(regressionSample.get("sample_code")))
                        .param("sampleId", String.valueOf(regressionSample.get("sample_id")))
                        .param("personCode", String.valueOf(regressionSample.get("person_code")))
                        .param("title", "unit-test manual regression sample")
                        .param("note", "unit-test manual add"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("unit-test manual regression sample")))
                .andExpect(content().string(containsString("\"expectedStatus\":")));

        mockMvc.perform(post("/api/workbench/migration-regression/sample-library/enabled")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .param("orgCode", String.valueOf(regressionSample.get("org_code")))
                        .param("sampleCode", String.valueOf(regressionSample.get("sample_code")))
                        .param("sampleId", String.valueOf(regressionSample.get("sample_id")))
                        .param("personCode", String.valueOf(regressionSample.get("person_code")))
                        .param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"enabled\":false")));

        mockMvc.perform(post("/api/workbench/migration-regression/sample-library/enabled")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .param("orgCode", String.valueOf(regressionSample.get("org_code")))
                        .param("sampleCode", String.valueOf(regressionSample.get("sample_code")))
                        .param("sampleId", String.valueOf(regressionSample.get("sample_id")))
                        .param("personCode", String.valueOf(regressionSample.get("person_code")))
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"enabled\":true")));

        mockMvc.perform(post("/api/workbench/migration-regression/sample-library/run?orgCode=001&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"runNo\":\"MIG-REG-LIB-")))
                .andExpect(content().string(containsString("\"sampleCount\":")));

        mockMvc.perform(post("/api/workbench/migration-regression/sample-library/run?orgCode=001&batchNo=unit-batch&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"runNo\":\"MIG-REG-LIB-")))
                .andExpect(content().string(containsString("\"batchNo\":\"unit-batch\"")))
                .andExpect(content().string(containsString("\"sampleCount\":")));

        mockMvc.perform(get("/api/workbench/migration-regression/sample-library/runs?orgCode=001&batchNo=unit-batch&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"batchNo\":\"unit-batch\"")))
                .andExpect(content().string(containsString("\"overallStatus\":")));

        String regressionRunNo = jdbcTemplate.queryForObject("""
                SELECT run_no
                FROM migration_regression_run
                WHERE org_code = '001'
                  AND batch_no = 'unit-batch'
                ORDER BY checked_at DESC, id DESC
                LIMIT 1
                """, String.class);
        mockMvc.perform(get("/api/workbench/migration-regression/sample-library/runs/" + regressionRunNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"runNo\":\"" + regressionRunNo + "\"")))
                .andExpect(content().string(containsString("\"reviewSummary\":")))
                .andExpect(content().string(containsString("\"pendingCount\":")))
                .andExpect(content().string(containsString("\"samples\":")));

        mockMvc.perform(post("/api/workbench/migration-regression/sample-library/runs/" + regressionRunNo + "/review")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .param("sampleCode", "unit-import")
                        .param("sampleId", "UNIT-IMPORT-1")
                        .param("personCode", "001-00055")
                        .param("reviewCategory", "BASE_CHANGED")
                        .param("reviewStatus", "REVIEWED")
                        .param("reviewNote", "unit review"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"reviewCategory\":\"BASE_CHANGED\"")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"REVIEWED\"")));

        mockMvc.perform(get("/api/workbench/migration-regression/sample-library/runs/" + regressionRunNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"reviewSummary\":")))
                .andExpect(content().string(containsString("\"reviewedCount\":")))
                .andExpect(content().string(containsString("\"reviewCategory\":\"BASE_CHANGED\"")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"REVIEWED\"")));

        mockMvc.perform(post("/api/workbench/migration-regression/sample-library/runs/" + regressionRunNo + "/governance-task")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .param("sampleCode", "unit-import")
                        .param("sampleId", "UNIT-IMPORT-1")
                        .param("personCode", "001-00055")
                        .param("reviewCategory", "NEED_FIX")
                        .param("reviewNote", "create governance task"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"workItemId\":\"regression-governance-")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"FIXING\"")));
        Long regressionGovernanceTaskCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_todo_candidate_cache
                WHERE source = 'DATA_GOVERNANCE'
                  AND work_item_id LIKE 'regression-governance-%'
                  AND person_code = '001-00055'
                """, Long.class);
        assertNotNull(regressionGovernanceTaskCount);
        assertTrue(regressionGovernanceTaskCount > 0);
        String regressionGovernanceWorkItemId = jdbcTemplate.queryForObject("""
                SELECT work_item_id
                FROM salary_todo_candidate_cache
                WHERE source = 'DATA_GOVERNANCE'
                  AND work_item_id LIKE 'regression-governance-%'
                  AND person_code = '001-00055'
                ORDER BY generated_at DESC
                LIMIT 1
                """, String.class);
        mockMvc.perform(post("/api/workbench/data-governance/tasks/" + regressionGovernanceWorkItemId + "/retest")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"retestStatus\":")));
        String regressionRetestStatus = jdbcTemplate.queryForObject("""
                SELECT retest_status
                FROM migration_regression_run_sample
                WHERE governance_work_item_id = ?
                LIMIT 1
                """, String.class, regressionGovernanceWorkItemId);
        assertNotNull(regressionRetestStatus);
        Long regressionRunCountBeforeRerun = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM migration_regression_run
                WHERE org_code = '001'
                  AND batch_no = 'unit-batch'
                """, Long.class);
        mockMvc.perform(post("/api/workbench/migration-regression/sample-library/run?orgCode=001&batchNo=unit-batch&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"batchNo\":\"unit-batch\"")))
                .andExpect(content().string(containsString("\"runNo\":\"MIG-REG-LIB-")));
        Long regressionRunCountAfterRerun = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM migration_regression_run
                WHERE org_code = '001'
                  AND batch_no = 'unit-batch'
                """, Long.class);
        assertNotNull(regressionRunCountBeforeRerun);
        assertNotNull(regressionRunCountAfterRerun);
        assertTrue(regressionRunCountAfterRerun > regressionRunCountBeforeRerun);
        mockMvc.perform(get("/api/workbench/migration-regression/sample-library/dashboard?orgCode=001&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"summary\":")))
                .andExpect(content().string(containsString("\"runs\":")))
                .andExpect(content().string(containsString("\"pendingCount\":")))
                .andExpect(content().string(containsString("\"retestResolvedCount\":")));
        mockMvc.perform(get("/api/workbench/migration-quality-overview?orgCode=001")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"summary\":")))
                .andExpect(content().string(containsString("\"gates\":")))
                .andExpect(content().string(containsString("\"regression\":")))
                .andExpect(content().string(containsString("\"archiveSummary\":")))
                .andExpect(content().string(containsString("\"governance\":")));
        mockMvc.perform(post("/api/workbench/migration-quality-overview/snapshots?orgCode=001")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"snapshotNo\":\"MIG-QUALITY-")))
                .andExpect(content().string(containsString("\"decision\":")))
                .andExpect(content().string(containsString("\"summary\":")))
                .andExpect(content().string(containsString("\"gates\":")));
        String qualitySnapshotNo = jdbcTemplate.queryForObject("""
                SELECT snapshot_no
                FROM migration_quality_snapshot
                WHERE org_code = '001'
                ORDER BY id DESC
                LIMIT 1
                """, String.class);
        assertNotNull(qualitySnapshotNo);
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots?orgCode=001&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(qualitySnapshotNo)))
                .andExpect(content().string(containsString("\"preflightTitle\":")))
                .andExpect(content().string(containsString("\"regressionWarnings\":")));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"overview\":")))
                .andExpect(content().string(containsString("\"decision\":")))
                .andExpect(content().string(containsString("\"preflightTitle\":")));
        mockMvc.perform(post("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/print")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(qualitySnapshotNo)))
                .andExpect(content().string(containsString("\"printedBy\":")));
        mockMvc.perform(post("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/archive?note=unit-archive")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"archiveStatus\":\"ARCHIVED\"")))
                .andExpect(content().string(containsString("unit-archive")));
        Long qualityReportArchiveAuditCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM sys_audit_log
                WHERE action_name = 'migration-quality-report-archive'
                  AND target_code = ?
                """, Long.class, qualitySnapshotNo);
        assertNotNull(qualityReportArchiveAuditCount);
        assertTrue(qualityReportArchiveAuditCount > 0);
        String qualityReportArchivedAt = jdbcTemplate.queryForObject("""
                SELECT CAST(archived_at AS CHAR)
                FROM migration_quality_snapshot
                WHERE snapshot_no = ?
                """, String.class, qualitySnapshotNo);
        assertNotNull(qualityReportArchivedAt);
        mockMvc.perform(post("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/archive?note=second-archive")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"archiveLocked\":true")))
                .andExpect(content().string(containsString("unit-archive")));
        String qualityReportArchivedAtAfterSecondArchive = jdbcTemplate.queryForObject("""
                SELECT CAST(archived_at AS CHAR)
                FROM migration_quality_snapshot
                WHERE snapshot_no = ?
                """, String.class, qualitySnapshotNo);
        assertEquals(qualityReportArchivedAt, qualityReportArchivedAtAfterSecondArchive);
        Long qualityReportArchiveAuditCountAfterSecondArchive = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM sys_audit_log
                WHERE action_name = 'migration-quality-report-archive'
                  AND target_code = ?
                """, Long.class, qualitySnapshotNo);
        assertEquals(qualityReportArchiveAuditCount, qualityReportArchiveAuditCountAfterSecondArchive);
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots?orgCode=001&archivedOnly=true&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(qualitySnapshotNo)))
                .andExpect(content().string(containsString("\"archiveStatus\":\"ARCHIVED\"")));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots.csv?orgCode=001&archivedOnly=true&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(qualitySnapshotNo)))
                .andExpect(content().string(containsString("ARCHIVED")))
                .andExpect(content().string(containsString("\u5f53\u524d\u5f52\u6863")));
        String qualitySnapshotLevel = jdbcTemplate.queryForObject("""
                SELECT preflight_level
                FROM migration_quality_snapshot
                WHERE snapshot_no = ?
                """, String.class, qualitySnapshotNo);
        assertNotNull(qualitySnapshotLevel);
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots?orgCode=001&archivedOnly=true&limit=5&preflightLevel=" + qualitySnapshotLevel + "&archivedBy=" + SCOPED_WORKBENCH_USER)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(qualitySnapshotNo)))
                .andExpect(content().string(containsString("\"preflightLevel\":\"" + qualitySnapshotLevel + "\"")));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots.csv?orgCode=001&archivedOnly=true&limit=5&preflightLevel=" + qualitySnapshotLevel + "&archivedBy=" + SCOPED_WORKBENCH_USER)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(qualitySnapshotNo)))
                .andExpect(content().string(containsString(SCOPED_WORKBENCH_USER)));
        Long qualityReportPrintAuditCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM sys_audit_log
                WHERE action_name = 'migration-quality-report-print'
                  AND target_code = ?
                """, Long.class, qualitySnapshotNo);
        assertNotNull(qualityReportPrintAuditCount);
        assertTrue(qualityReportPrintAuditCount > 0);
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/print-audits")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("migration-quality-report-archive")))
                .andExpect(content().string(containsString("migration-quality-report-print")))
                .andExpect(content().string(containsString(qualitySnapshotNo)));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/print-audits.csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("migration-quality-report-archive")))
                .andExpect(content().string(containsString("migration-quality-report-print")))
                .andExpect(content().string(containsString(qualitySnapshotNo)));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/print-audits?action=migration-quality-report-archive")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("migration-quality-report-archive")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("migration-quality-report-print"))));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/print-audits.csv?action=migration-quality-report-archive")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("migration-quality-report-archive")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("migration-quality-report-print"))));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + ".csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(qualitySnapshotNo)))
                .andExpect(content().string(containsString("巡检结论")));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/acceptance-package.zip")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, WORKBENCH_USER))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/acceptance-package/preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"nextExportNo\":1")))
                .andExpect(content().string(containsString("\"fileCount\":7")))
                .andExpect(content().string(containsString("\"hasComparison\":false")))
                .andExpect(content().string(containsString("salary-report-migration-closure-001.csv")));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/print-audits?action=migration-quality-acceptance-package-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("migration-quality-acceptance-package-preview")))
                .andExpect(content().string(containsString("exportNo=1")))
                .andExpect(content().string(containsString("files=7")))
                .andExpect(content().string(containsString("hasComparison=false")));
        var qualityAcceptancePackage = mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/acceptance-package.zip")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("migration-quality-acceptance-package-" + qualitySnapshotNo + "-v1.zip")))
                .andReturn();
        Set<String> qualityAcceptancePackageEntries = new HashSet<>();
        String qualityAcceptancePackageReadme = "";
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(qualityAcceptancePackage.getResponse().getContentAsByteArray()))) {
            var entry = zip.getNextEntry();
            while (entry != null) {
                qualityAcceptancePackageEntries.add(entry.getName());
                if ("README.txt".equals(entry.getName())) {
                    qualityAcceptancePackageReadme = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
                entry = zip.getNextEntry();
            }
        }
        assertTrue(qualityAcceptancePackageEntries.contains("README.txt"));
        assertTrue(qualityAcceptancePackageReadme.contains(qualitySnapshotNo));
        assertTrue(qualityAcceptancePackageReadme.contains("v1"));
        assertTrue(qualityAcceptancePackageReadme.contains(SCOPED_WORKBENCH_USER));
        assertTrue(qualityAcceptancePackageEntries.contains("migration-quality-final-summary-" + qualitySnapshotNo + ".csv"));
        assertTrue(qualityAcceptancePackageEntries.contains("migration-quality-summary-" + qualitySnapshotNo + ".csv"));
        assertTrue(qualityAcceptancePackageEntries.contains("migration-quality-report-" + qualitySnapshotNo + ".csv"));
        assertTrue(qualityAcceptancePackageEntries.contains("migration-quality-print-audits-" + qualitySnapshotNo + ".csv"));
        assertTrue(qualityAcceptancePackageEntries.contains("migration-quality-archive-ledger-001.csv"));
        assertTrue(qualityAcceptancePackageEntries.contains("salary-report-migration-closure-001.csv"));
        Long qualityAcceptancePackageAuditCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM sys_audit_log
                WHERE action_name = 'migration-quality-acceptance-package-export'
                  AND target_code = ?
                """, Long.class, qualitySnapshotNo);
        assertNotNull(qualityAcceptancePackageAuditCount);
        assertTrue(qualityAcceptancePackageAuditCount > 0);
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/print-audits")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("migration-quality-acceptance-package-export")))
                .andExpect(content().string(containsString(qualitySnapshotNo)))
                .andExpect(content().string(containsString("exportNo=1")))
                .andExpect(content().string(containsString("files=7")))
                .andExpect(content().string(containsString("migration-quality-archive-ledger-001.csv")))
                .andExpect(content().string(containsString("salary-report-migration-closure-001.csv")));
        mockMvc.perform(get("/api/reports/migration-closure?orgCode=001&keyword=migration-quality-acceptance-package-export&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("migration-quality-acceptance-package-export")))
                .andExpect(content().string(containsString("\"auditCount\":")));
        mockMvc.perform(post("/api/workbench/migration-quality-overview/snapshots?orgCode=001")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"snapshotNo\":\"MIG-QUALITY-")));
        List<String> qualitySnapshotNos = jdbcTemplate.queryForList("""
                SELECT snapshot_no
                FROM migration_quality_snapshot
                WHERE org_code = '001'
                ORDER BY id DESC
                LIMIT 2
                """, String.class);
        assertTrue(qualitySnapshotNos.size() >= 2);
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNos.get(0) + "/acceptance-package.zip")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("can only be exported after the snapshot is archived")));
        Long unarchivedQualityAcceptancePackageAuditCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM sys_audit_log
                WHERE action_name = 'migration-quality-acceptance-package-export'
                  AND target_code = ?
                """, Long.class, qualitySnapshotNos.get(0));
        assertNotNull(unarchivedQualityAcceptancePackageAuditCount);
        assertEquals(0L, unarchivedQualityAcceptancePackageAuditCount);
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/compare")
                        .param("baseSnapshotNo", qualitySnapshotNos.get(1))
                        .param("targetSnapshotNo", qualitySnapshotNos.get(0))
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"deltas\":")))
                .andExpect(content().string(containsString("\"governanceIssues\"")))
                .andExpect(content().string(containsString("\"summary\":")));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/compare.csv")
                        .param("baseSnapshotNo", qualitySnapshotNos.get(1))
                        .param("targetSnapshotNo", qualitySnapshotNos.get(0))
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(qualitySnapshotNos.get(1))))
                .andExpect(content().string(containsString(qualitySnapshotNos.get(0))))
                .andExpect(content().string(containsString("governanceIssues")));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/acceptance-package/preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"nextExportNo\":2")))
                .andExpect(content().string(containsString("\"fileCount\":8")))
                .andExpect(content().string(containsString("\"hasComparison\":true")))
                .andExpect(content().string(containsString("migration-quality-snapshot-compare-" + qualitySnapshotNo + "-" + qualitySnapshotNos.get(0) + ".csv")));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/print-audits?action=migration-quality-acceptance-package-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("migration-quality-acceptance-package-preview")))
                .andExpect(content().string(containsString("exportNo=2")))
                .andExpect(content().string(containsString("files=8")))
                .andExpect(content().string(containsString("hasComparison=true")));
        var supersededQualityAcceptancePackage = mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/acceptance-package.zip")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("migration-quality-acceptance-package-" + qualitySnapshotNo + "-v2.zip")))
                .andReturn();
        Set<String> supersededQualityAcceptancePackageEntries = new HashSet<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(supersededQualityAcceptancePackage.getResponse().getContentAsByteArray()))) {
            var entry = zip.getNextEntry();
            while (entry != null) {
                supersededQualityAcceptancePackageEntries.add(entry.getName());
                entry = zip.getNextEntry();
            }
        }
        assertTrue(supersededQualityAcceptancePackageEntries.contains("migration-quality-snapshot-compare-" + qualitySnapshotNo + "-" + qualitySnapshotNos.get(0) + ".csv"));
        assertTrue(supersededQualityAcceptancePackageEntries.contains("salary-report-migration-closure-001.csv"));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/print-audits")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("exportNo=2")))
                .andExpect(content().string(containsString("files=8")))
                .andExpect(content().string(containsString("migration-quality-snapshot-compare-" + qualitySnapshotNo + "-" + qualitySnapshotNos.get(0) + ".csv")));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/acceptance-package/preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"nextExportNo\":3")))
                .andExpect(content().string(containsString("\"fileCount\":8")))
                .andExpect(content().string(containsString("\"hasComparison\":true")));
        mockMvc.perform(get("/api/workbench/migration-quality-overview?orgCode=001")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"archiveSummary\":")))
                .andExpect(content().string(containsString("\"latestExportSnapshotNo\":\"" + qualitySnapshotNo + "\"")))
                .andExpect(content().string(containsString("\"latestExportNo\":\"2\"")))
                .andExpect(content().string(containsString("\"latestExportFileCount\":8")))
                .andExpect(content().string(containsString("\"latestExportHasComparison\":true")))
                .andExpect(content().string(containsString("\"latestPreviewExportNo\":\"3\"")))
                .andExpect(content().string(containsString("\"latestPreviewFileCount\":8")))
                .andExpect(content().string(containsString("\"latestPreviewPendingExport\":true")));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/final-summary.csv?orgCode=001")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("migration-quality-final-summary-001.csv")))
                .andExpect(content().string(containsString(qualitySnapshotNo)))
                .andExpect(content().string(containsString("v2")))
                .andExpect(content().string(containsString("WARN")));
        mockMvc.perform(post("/api/workbench/migration-quality-overview/final-summary/print?orgCode=001")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"snapshotNo\":\"" + qualitySnapshotNo + "\"")))
                .andExpect(content().string(containsString("\"exportNo\":\"2\"")));
        mockMvc.perform(get("/api/workbench/migration-quality-overview/snapshots/" + qualitySnapshotNo + "/print-audits?action=migration-quality-final-summary-print")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("migration-quality-final-summary-print")))
                .andExpect(content().string(containsString("exportNo=2")));

        mockMvc.perform(post("/api/workbench/migration-acceptance/run?orgCode=001&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"runNo\":\"MIG-ACC-")))
                .andExpect(content().string(containsString("\"overallStatus\":")))
                .andExpect(content().string(containsString("\"dataGovernanceIssues\":")))
                .andExpect(content().string(containsString("1-application-flow")))
                .andExpect(content().string(containsString("10-launch-readiness")));

        String acceptanceRunNo = jdbcTemplate.queryForObject("""
                SELECT run_no
                FROM migration_acceptance_run
                WHERE org_code = '001'
                ORDER BY id DESC
                LIMIT 1
                """, String.class);
        List<Long> acceptanceIssueIds = jdbcTemplate.queryForList("""
                SELECT id
                FROM migration_acceptance_issue
                WHERE run_no = ?
                ORDER BY id
                LIMIT 1
                """, Long.class, acceptanceRunNo);
        if (acceptanceIssueIds.isEmpty()) {
            jdbcTemplate.update("""
                    INSERT INTO migration_acceptance_issue(run_no, person_code, person_name, org_code, issue_type, message)
                    VALUES (?, '001-00055', 'Acceptance Issue Test', '001', 'UNIT_TEST', 'unit-test acceptance issue')
                    """, acceptanceRunNo);
            acceptanceIssueIds = jdbcTemplate.queryForList("""
                    SELECT id
                    FROM migration_acceptance_issue
                    WHERE run_no = ?
                    ORDER BY id DESC
                    LIMIT 1
                    """, Long.class, acceptanceRunNo);
        }
        Long acceptanceIssueId = acceptanceIssueIds.getFirst();

        mockMvc.perform(get("/api/workbench/migration-acceptance/runs?orgCode=001&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(acceptanceRunNo)))
                .andExpect(content().string(containsString("\"warningCount\":")));

        mockMvc.perform(get("/api/workbench/migration-acceptance/runs/" + acceptanceRunNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(acceptanceRunNo)))
                .andExpect(content().string(containsString("\"gates\":")))
                .andExpect(content().string(containsString("1-application-flow")))
                .andExpect(content().string(containsString("10-launch-readiness")));

        mockMvc.perform(get("/api/workbench/migration-acceptance/runs/" + acceptanceRunNo + "/issues?status=PENDING&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(String.valueOf(acceptanceIssueId))))
                .andExpect(content().string(containsString("\"reviewStatus\":\"PENDING\"")));

        mockMvc.perform(post("/api/workbench/migration-acceptance/issues/" + acceptanceIssueId + "/review")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reviewStatus":"REVIEWED","reviewReason":"unit-test acceptance issue reviewed"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"reviewStatus\":\"REVIEWED\"")))
                .andExpect(content().string(containsString("unit-test acceptance issue reviewed")));

        mockMvc.perform(get("/api/workbench/migration-acceptance/runs/" + acceptanceRunNo + ".csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u9a8c\u6536\u6279\u6b21")))
                .andExpect(content().string(containsString(acceptanceRunNo)));

        mockMvc.perform(get("/api/workbench/migration-acceptance/run.csv?orgCode=001&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u9a8c\u6536\u6279\u6b21")))
                .andExpect(content().string(containsString("1-application-flow")))
                .andExpect(content().string(containsString("10-launch-readiness")));
    }

    private void insertGeneratedTimelineTodo(String workItemId, String note) {
        jdbcTemplate.update("""
                INSERT INTO salary_todo_candidate_cache(work_item_id, source, source_id, person_code, org_code,
                                                        person_no, person_name, event_year, event_month, change_type, note)
                VALUES (?, 'GENERATED_TIMELINE', 'ISSUE', '00105-00008', '00105',
                        '00008', 'Generated Issue', 2026, 1, 'generated timeline review', ?)
                ON DUPLICATE KEY UPDATE source = VALUES(source), note = VALUES(note)
                """, workItemId, note);
    }

    private String insertTemporaryPostChange(String startMonth, String targetPostCode, String marker) {
        jdbcTemplate.update("""
                INSERT INTO dryzwbh(dwbm, grbm, xrzwbm, xrzw, zwjb, zjbm, zwbm, xzzw, zwlb,
                                    srny, kjnx, xrzwbz, jsbz)
                SELECT dwbm, grbm, xrzwbm, xrzw, zwjb, zjbm, ?, xzzw, zwlb,
                       ?, kjnx, xrzwbz, ?
                FROM dryzwbh
                WHERE dwbm = '001' AND grbm = '00055'
                ORDER BY TRIM(srny) DESC, id DESC
                LIMIT 1
                """, targetPostCode, startMonth, marker);
        return jdbcTemplate.queryForObject("""
                SELECT CAST(id AS CHAR)
                FROM dryzwbh
                WHERE dwbm = '001'
                  AND grbm = '00055'
                  AND TRIM(jsbz) = ?
                  AND TRIM(srny) = ?
                ORDER BY id DESC
                LIMIT 1
                """, String.class, marker, startMonth);
    }

    private void insertTemporaryLaterHistory() {
        List<String> columns = jdbcTemplate.queryForList("""
                SELECT LOWER(column_name)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = 'hisbase'
                ORDER BY ordinal_position
                """, String.class);
        String columnSql = String.join(", ", columns);
        String selectSql = String.join(", ", columns.stream()
                .map(column -> switch (column) {
                    case "id" -> "?";
                    case "jsnf" -> "'2026'";
                    case "jsyf" -> "'3'";
                    case "jslb" -> "'测试'";
                    case "sid" -> "NULL";
                    default -> column;
                })
                .toList());
        jdbcTemplate.update("DELETE FROM hisbase WHERE id = ?", TODO_LATER_HISTORY_ID);
        jdbcTemplate.update("""
                INSERT INTO hisbase (__COLUMNS__)
                SELECT __SELECTS__
                FROM hisbase
                WHERE dwbm = '001'
                  AND grbm = '00055'
                ORDER BY CAST(TRIM(jsnf) AS UNSIGNED) DESC,
                         CAST(TRIM(jsyf) AS UNSIGNED) DESC,
                         id DESC
                LIMIT 1
                """.replace("__COLUMNS__", columnSql).replace("__SELECTS__", selectSql), TODO_LATER_HISTORY_ID);
    }

    private int countTodoBySourceId(String sourceId) {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_todo_candidate_cache
                WHERE source = 'dryzwbh'
                  AND source_id = ?
                """, Integer.class, sourceId);
    }

    private void createUserRole(String username, String roleCode, String... menuCodes) {
        jdbcTemplate.update("""
                INSERT INTO sys_user(username, display_name, password_hash, status)
                VALUES (?, ?, '{noop}123456', 'ACTIVE')
                ON DUPLICATE KEY UPDATE
                    display_name = VALUES(display_name),
                    status = VALUES(status)
                """, username, username);
        jdbcTemplate.update("""
                INSERT INTO sys_role(code, name, status)
                VALUES (?, ?, 'ACTIVE')
                ON DUPLICATE KEY UPDATE
                    name = VALUES(name),
                    status = VALUES(status)
                """, roleCode, roleCode);
        jdbcTemplate.update("""
                INSERT IGNORE INTO sys_user_role(username, role_code)
                VALUES (?, ?)
                """, username, roleCode);
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_code = ?", roleCode);
        for (String menuCode : menuCodes) {
            jdbcTemplate.update("""
                    INSERT IGNORE INTO sys_role_menu(role_code, menu_code)
                    VALUES (?, ?)
                    """, roleCode, menuCode);
        }
    }

    private void cleanup() {
        jdbcTemplate.update("DELETE FROM salary_business_case WHERE work_item_id IN (?, ?, ?, ?, ?, ?, ?, ?)",
                CASE_WORK_ITEM, "tmp-test-salary-case-denied", "tmp-test-denied-detail",
                "tmp-test-salary-case-requires-force", "tmp-test-salary-case-missing-force-reason",
                "tmp-test-salary-case-difference", HISTORY_WRITE_WORK_ITEM, RANK_ALLOWANCE_WORK_ITEM);
        jdbcTemplate.update("DELETE FROM salary_business_case WHERE work_item_id = ?", TODO_DUPLICATE_CASE_WORK_ITEM);
        if (tableExists("salary_business_case_snapshot")) {
            jdbcTemplate.update("DELETE FROM salary_business_case_snapshot WHERE work_item_id IN (?, ?, ?, ?, ?, ?, ?, ?)",
                    CASE_WORK_ITEM, "tmp-test-salary-case-denied", "tmp-test-denied-detail",
                    "tmp-test-salary-case-requires-force", "tmp-test-salary-case-missing-force-reason",
                    "tmp-test-salary-case-difference", HISTORY_WRITE_WORK_ITEM, RANK_ALLOWANCE_WORK_ITEM);
        }
        if (tableExists("salary_history_write_plan")) {
            jdbcTemplate.update("DELETE FROM salary_history_write_plan WHERE work_item_id IN (?, ?, ?, ?, ?, ?, ?, ?)",
                    CASE_WORK_ITEM, "tmp-test-salary-case-denied", "tmp-test-denied-detail",
                    "tmp-test-salary-case-requires-force", "tmp-test-salary-case-missing-force-reason",
                    "tmp-test-salary-case-difference", HISTORY_WRITE_WORK_ITEM, RANK_ALLOWANCE_WORK_ITEM);
        }
        if (tableExists("salary_report_print_batch_item")) {
            jdbcTemplate.update("DELETE FROM salary_report_print_batch_item WHERE case_no IN (?, ?, ?)",
                    HISTORY_WRITE_CASE_NO, CASE_WORK_ITEM, RANK_ALLOWANCE_CASE_NO);
        }
        if (tableExists("salary_report_print_batch")) {
            jdbcTemplate.update("DELETE FROM salary_report_print_batch WHERE batch_no LIKE 'UT-PRINT-%'");
        }
        cleanupRankAllowanceHistoryRows();
        if (tableExists("salary_generated_timeline_issue_review")) {
            jdbcTemplate.update("""
                    DELETE FROM salary_generated_timeline_issue_review
                    WHERE work_item_id IN ('generated-timeline-unit-test', 'generated-timeline-unit-test-review')
                       OR work_item_id LIKE 'generated-timeline-00105-00008%'
                    """);
        }
        if (tableExists("application_case")) {
            jdbcTemplate.update("DELETE FROM application_case WHERE summary LIKE 'unit-test application%'");
        }
        jdbcTemplate.update("""
                DELETE FROM hisbase
                WHERE id = ?
                   OR id = ?
                   OR (dwbm = '001' AND grbm = '00055' AND TRIM(jsnf) = '2099' AND TRIM(jsyf) = '1' AND TRIM(jslb) = '测试写入')
                """, HISTORY_WRITE_SOURCE_ID, TODO_LATER_HISTORY_ID);
        if (tableExists("salary_todo_candidate_cache")) {
            jdbcTemplate.update("""
                    DELETE FROM salary_todo_candidate_cache
                    WHERE work_item_id = 'tmp-test-todo-latest-change'
                       OR work_item_id IN ('generated-timeline-unit-test', 'generated-timeline-unit-test-review')
                       OR work_item_id LIKE 'generated-timeline-00105-00008%'
                       OR work_item_id LIKE 'salary-migration-delivery-error-001-2099-1-%'
                    """);
        }
        if (tableExists("salary_data_governance_task_review")) {
            jdbcTemplate.update("""
                    DELETE FROM salary_data_governance_task_review
                    WHERE work_item_id LIKE 'salary-migration-delivery-error-001-2099-1-%'
                    """);
        }
        if (tableExists("sys_user_work_state")) {
            jdbcTemplate.update("""
                    DELETE FROM sys_user_work_state
                    WHERE username IN (?, ?, ?, ?, ?, ?, ?, ?)
                    """, ADMIN_USER, WORKBENCH_USER, TODO_USER, SCOPED_WORKBENCH_USER,
                    ORG_USER, TRIAL_USER, RECONCILE_USER, CREATED_USER);
        }
        jdbcTemplate.update("DELETE FROM dryjbxx WHERE dwbm = '001' AND grbm = 'UT001'");
        jdbcTemplate.update("""
                DELETE FROM dryzwbh
                WHERE dwbm = '001'
                  AND grbm = '00055'
                  AND TRIM(jsbz) IN ('UTEST', 'UTEST2')
                """);
        jdbcTemplate.update("""
                DELETE FROM dxl
                WHERE dwbm = '001'
                  AND grbm = '00055'
                  AND TRIM(bz) IN ('UTEST', 'UTEST2')
                """);
        jdbcTemplate.update("""
                DELETE FROM dndkh
                WHERE dwbm = '001'
                  AND grbm = '00055'
                  AND khnd = '2098'
                """);
        jdbcTemplate.update("""
                DELETE FROM person_base_change_log
                WHERE source_id = 'unit-test'
                   OR summary LIKE '%unit-test base change%'
                   OR summary LIKE '%unit-test denied base change%'
                   OR summary LIKE '%unit-test post create%'
                   OR summary LIKE '%unit-test post update%'
                   OR summary LIKE '%unit-test education create%'
                   OR summary LIKE '%unit-test education update%'
                   OR summary LIKE '%unit-test assessment create%'
                   OR summary LIKE '%unit-test assessment update%'
                   OR summary LIKE '%unit-test base info update%'
                   OR summary LIKE '%unit-test latest base summary%'
                """);
        jdbcTemplate.update("DELETE FROM sys_user_org WHERE username IN (?, ?, ?, ?, ?, ?, ?)",
                WORKBENCH_USER, TODO_USER, SCOPED_WORKBENCH_USER, ORG_USER, TRIAL_USER, RECONCILE_USER, CREATED_USER);
        jdbcTemplate.update("DELETE FROM sys_user_role WHERE username IN (?, ?, ?, ?, ?, ?, ?)",
                WORKBENCH_USER, TODO_USER, SCOPED_WORKBENCH_USER, ORG_USER, TRIAL_USER, RECONCILE_USER, CREATED_USER);
        jdbcTemplate.update("DELETE FROM sys_user WHERE username IN (?, ?, ?, ?, ?, ?, ?)",
                WORKBENCH_USER, TODO_USER, SCOPED_WORKBENCH_USER, ORG_USER, TRIAL_USER, RECONCILE_USER, CREATED_USER);
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_code IN (?, ?, ?, ?, ?, ?)",
                WORKBENCH_ROLE, TODO_ROLE, SCOPED_WORKBENCH_ROLE, ORG_ROLE, TRIAL_ROLE, RECONCILE_ROLE);
        jdbcTemplate.update("DELETE FROM sys_role WHERE code IN (?, ?, ?, ?, ?, ?)",
                WORKBENCH_ROLE, TODO_ROLE, SCOPED_WORKBENCH_ROLE, ORG_ROLE, TRIAL_ROLE, RECONCILE_ROLE);
        jdbcTemplate.update("DELETE FROM sys_audit_log WHERE target_code IN (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                WORKBENCH_USER, TODO_USER, SCOPED_WORKBENCH_USER, ORG_USER, TRIAL_USER, RECONCILE_USER,
                CREATED_USER, WORKBENCH_ROLE, TODO_ROLE, SCOPED_WORKBENCH_ROLE, ORG_ROLE, TRIAL_ROLE, RECONCILE_ROLE,
                HISTORY_WRITE_CASE_NO, RANK_ALLOWANCE_CASE_NO);
        jdbcTemplate.update("""
                DELETE FROM sys_audit_log
                WHERE summary LIKE '%unit-test base change%'
                   OR summary LIKE '%unit-test denied base change%'
                   OR summary LIKE '%unit-test post create%'
                   OR summary LIKE '%unit-test post update%'
                   OR summary LIKE '%unit-test denied post%'
                   OR summary LIKE '%unit-test education create%'
                   OR summary LIKE '%unit-test education update%'
                   OR summary LIKE '%unit-test denied education%'
                   OR summary LIKE '%unit-test assessment create%'
                   OR summary LIKE '%unit-test assessment update%'
                   OR summary LIKE '%unit-test denied assessment%'
                   OR summary LIKE '%unit-test base info update%'
                   OR summary LIKE '%unit-test denied base info%'
                   OR summary LIKE '%unit-test latest base summary%'
                   OR target_type = 'PERSON_POST'
                   OR target_type = 'PERSON_EDUCATION'
                   OR target_type = 'PERSON_ASSESSMENT'
                   OR target_code = '001-UT001'
                   OR summary LIKE '%001-00055 dryzwbh%'
                """);
    }

    private void cleanupRankAllowanceHistoryRows() {
        List<String> ids = jdbcTemplate.queryForList("""
                SELECT CAST(id AS CHAR)
                FROM hisbase
                WHERE dwbm = '001'
                  AND grbm = '00055'
                  AND TRIM(jsnf) = '2099'
                  AND TRIM(jsyf) = '2'
                  AND jxjt IN (130, 131)
                """, String.class);
        for (String id : ids) {
            jdbcTemplate.update("""
                    UPDATE hisbase
                    SET sid = NULL
                    WHERE sid = ? OR TRIM(sid) = ?
                    """, id, id);
            jdbcTemplate.update("DELETE FROM hisbase WHERE id = ? OR TRIM(id) = ?", id, id);
        }
    }

    private void createTemporaryPerson() {
        cleanupTemporaryPersonOnly();
        jdbcTemplate.update("""
                INSERT INTO dryjbxx(dwbm, grbm, xm, sfzh, xb, csny, ryfl, dwsx, gwfl, cjgzny,
                                    zzny, jrny, jrfs, zdgznx, gznx, jhlqsny, zdjhlnx, xlbm,
                                    zgxl, bjglxlnx, tc, txsj, bgdwjc, zwjb, zjbm, xrzw, srny,
                                    tgbl, jtbl, fddc, khqk, dynkh, denkh, bbz, bh, gryhzh,
                                    spdw, mz, zzmm, fdgd, fdsj, jzgb, ydwzw, yzwrzsj, dah,
                                    sfjzgb, yctxsj)
                SELECT dwbm, 'UT001', 'UTEST', sfzh, xb, csny, ryfl, dwsx, gwfl, cjgzny,
                       zzny, jrny, jrfs, zdgznx, gznx, jhlqsny, zdjhlnx, xlbm,
                       zgxl, bjglxlnx, tc, txsj, bgdwjc, zwjb, zjbm, xrzw, srny,
                       tgbl, jtbl, fddc, khqk, dynkh, denkh, bbz, bh, gryhzh,
                       spdw, mz, zzmm, fdgd, fdsj, jzgb, ydwzw, yzwrzsj, dah,
                       sfjzgb, yctxsj
                FROM dryjbxx
                WHERE dwbm = '001' AND grbm = '00055'
                LIMIT 1
                """);
    }

    private void cleanupTemporaryPersonOnly() {
        jdbcTemplate.update("DELETE FROM dryjbxx WHERE dwbm = '001' AND grbm = 'UT001'");
    }

    private void insertTemporaryHistoryTemplate() {
        jdbcTemplate.update("DELETE FROM hisbase WHERE id = ?", HISTORY_WRITE_SOURCE_ID);
        List<String> columns = jdbcTemplate.queryForList("""
                SELECT LOWER(column_name)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = 'hisbase'
                ORDER BY ordinal_position
                """, String.class);
        String columnSql = String.join(", ", columns);
        String selectSql = String.join(", ", columns.stream()
                .map(column -> switch (column) {
                    case "id" -> "?";
                    case "jsnf" -> "'2098'";
                    case "jsyf" -> "'12'";
                    case "jslb" -> "'测试模板'";
                    case "sid" -> "NULL";
                    default -> column;
                })
                .toList());
        jdbcTemplate.update("""
                INSERT INTO hisbase (__COLUMNS__)
                SELECT __SELECTS__
                FROM hisbase
                WHERE dwbm = '001'
                  AND grbm = '00055'
                ORDER BY CAST(TRIM(jsnf) AS UNSIGNED) DESC,
                         CAST(TRIM(jsyf) AS UNSIGNED) DESC,
                         id DESC
                LIMIT 1
                """.replace("__COLUMNS__", columnSql).replace("__SELECTS__", selectSql), HISTORY_WRITE_SOURCE_ID);
    }

    private void ensureSnapshotTableForTest() {
        jdbcTemplate.execute("""
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
                )
                """);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                """, Integer.class, tableName);
        return count != null && count > 0;
    }

    private void assertAudit(String action, String targetType, String targetCode, String operator, String summaryPart) {
        assertAudit("system", action, targetType, targetCode, operator, summaryPart);
    }

    private void assertAudit(String module, String action, String targetType, String targetCode, String operator, String summaryPart) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys_audit_log
                WHERE module_name = ?
                  AND action_name = ?
                  AND target_type = ?
                  AND target_code = ?
                  AND operator = ?
                  AND summary LIKE CONCAT('%', ?, '%')
                """, Integer.class, module, action, targetType, targetCode, operator, summaryPart);
        org.junit.jupiter.api.Assertions.assertTrue(count != null && count > 0,
                "Missing audit log for " + action + " " + targetCode);
    }

    private String caseNo(String workItemId) {
        return jdbcTemplate.queryForObject("""
                SELECT case_no
                FROM salary_business_case
                WHERE work_item_id = ?
                LIMIT 1
                """, String.class, workItemId);
    }

    private int countBusinessCase(String workItemId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM salary_business_case
                WHERE work_item_id = ?
                """, Integer.class, workItemId);
        return count == null ? 0 : count;
    }

    private void assertTrialSnapshot(String workItemId) {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT trial_status, trial_summary, trial_changes_json
                FROM salary_business_case
                WHERE work_item_id = ?
                LIMIT 1
                """, workItemId);
        String status = String.valueOf(row.get("trial_status"));
        org.junit.jupiter.api.Assertions.assertTrue(
                status.equals("MATCH") || status.equals("DIFFERENT") || status.equals("ERROR"),
                "Unexpected trial status: " + status);
        org.junit.jupiter.api.Assertions.assertFalse(String.valueOf(row.get("trial_summary")).isBlank());
        org.junit.jupiter.api.Assertions.assertNotNull(row.get("trial_changes_json"));
    }

    private void assertBusinessCaseSnapshot(String workItemId) {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT trial_status, trial_changes_json, salary_items_json, snapshot_json
                FROM salary_business_case_snapshot
                WHERE work_item_id = ?
                LIMIT 1
                """, workItemId);
        String status = String.valueOf(row.get("trial_status"));
        org.junit.jupiter.api.Assertions.assertTrue(
                status.equals("MATCH") || status.equals("DIFFERENT") || status.equals("ERROR"),
                "Unexpected snapshot trial status: " + status);
        org.junit.jupiter.api.Assertions.assertNotNull(row.get("trial_changes_json"));
        String snapshotJson = String.valueOf(row.get("snapshot_json"));
        org.junit.jupiter.api.Assertions.assertTrue(snapshotJson.contains(workItemId));
        org.junit.jupiter.api.Assertions.assertTrue(snapshotJson.contains("trialCalculatedTotal"));
        org.junit.jupiter.api.Assertions.assertNotNull(row.get("salary_items_json"));
        org.junit.jupiter.api.Assertions.assertTrue(snapshotJson.contains("salaryItems"));
    }

    private void assertHistoryWritePlan(String caseNo, String workItemId) {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT plan_no, preview_status, writable, plan_status, fields_json, issues_json, preview_json
                FROM salary_history_write_plan
                WHERE work_item_id = ?
                LIMIT 1
                """, workItemId);
        org.junit.jupiter.api.Assertions.assertEquals("HWP-" + caseNo, String.valueOf(row.get("plan_no")));
        org.junit.jupiter.api.Assertions.assertEquals("BLOCKED", String.valueOf(row.get("preview_status")));
        org.junit.jupiter.api.Assertions.assertEquals("PREPARED", String.valueOf(row.get("plan_status")));
        org.junit.jupiter.api.Assertions.assertEquals(0, ((Number) row.get("writable")).intValue());
        org.junit.jupiter.api.Assertions.assertTrue(String.valueOf(row.get("fields_json")).contains("itemCode"));
        org.junit.jupiter.api.Assertions.assertTrue(String.valueOf(row.get("issues_json")).contains("trial risk must be reviewed"));
        org.junit.jupiter.api.Assertions.assertTrue(String.valueOf(row.get("preview_json")).contains("\"writePlanId\":\"HWP-" + caseNo + "\""));
    }

    private String jsonString(String body, String fieldName) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\"" + java.util.regex.Pattern.quote(fieldName) + "\"\\s*:\\s*\"([^\"]*)\"")
                .matcher(body);
        org.junit.jupiter.api.Assertions.assertTrue(matcher.find(), "Missing JSON field: " + fieldName);
        return matcher.group(1);
    }

    private int countOccurrences(String text, String needle) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }

    private String batchSafetyToken(String query) throws Exception {
        String body = mockMvc.perform(post("/api/workbench/history-write-plans/batch-preview?" + query)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return jsonString(body, "safetyToken");
    }

    private String selectedSafetyToken(String caseNo) throws Exception {
        String body = mockMvc.perform(post("/api/workbench/history-write-plans/selected-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNos\":[\"" + caseNo + "\"]}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return jsonString(body, "safetyToken");
    }

    private void insertPrintedApprovalReport(String caseNo) {
        String batchNo = "UT-PRINT-" + caseNo;
        jdbcTemplate.update("""
                INSERT INTO salary_report_print_batch(batch_no, report_type, org_code, event_year, event_month,
                                                       business_type, keyword, limit_count, printed_count,
                                                       blocked_count, warning_count, printed_by, summary)
                VALUES (?, 'SALARY_CASE_APPROVAL', '001', 2099, 1, '测试写入', ?, 1, 1, 0, 0, ?, 'unit-test printed approval')
                ON DUPLICATE KEY UPDATE
                    printed_count = VALUES(printed_count),
                    blocked_count = VALUES(blocked_count),
                    warning_count = VALUES(warning_count),
                    printed_at = CURRENT_TIMESTAMP,
                    summary = VALUES(summary)
                """, batchNo, caseNo, SCOPED_WORKBENCH_USER);
        jdbcTemplate.update("""
                INSERT INTO salary_report_print_batch_item(batch_no, case_no, person_code, person_name, org_code,
                                                           business_type, validation_status, issue_count,
                                                           warning_count, summary)
                VALUES (?, ?, '001-00055', 'History Write', '001', '测试写入', 'READY', 0, 0, 'unit-test printed approval item')
                ON DUPLICATE KEY UPDATE
                    validation_status = VALUES(validation_status),
                    issue_count = VALUES(issue_count),
                    warning_count = VALUES(warning_count),
                    summary = VALUES(summary)
                """, batchNo, caseNo);
    }

    private String historyId(String orgCode, String personNo) {
        return jdbcTemplate.queryForObject("""
                SELECT TRIM(id)
                FROM hisbase
                WHERE dwbm = ? AND grbm = ?
                ORDER BY CAST(TRIM(jsnf) AS UNSIGNED) DESC,
                         CAST(TRIM(jsyf) AS UNSIGNED) DESC,
                         id DESC
                LIMIT 1
                """, String.class, orgCode, personNo);
    }

    private Optional<DifferentTrialSample> differentTrialSample() {
        List<DifferentTrialSample> samples = jdbcTemplate.query("""
                SELECT CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS person_code,
                       TRIM(dwbm) AS org_code,
                       CAST(TRIM(jsnf) AS UNSIGNED) AS event_year,
                       CAST(TRIM(jsyf) AS UNSIGNED) AS event_month,
                       TRIM(jslb) AS change_type
                FROM hisbase
                WHERE TRIM(dwbm) = '001'
                  AND CAST(TRIM(jsnf) AS UNSIGNED) >= 2006
                  AND TRIM(jslb) NOT IN ('\u6d25\u8d34\u53d8\u5316', '\u8c03\u6807\u664b\u5347')
                ORDER BY CAST(TRIM(jsnf) AS UNSIGNED) DESC,
                         CAST(TRIM(jsyf) AS UNSIGNED) DESC
                LIMIT 1000
                """, (rs, rowNum) -> new DifferentTrialSample(
                rs.getString("person_code"),
                rs.getString("org_code"),
                rs.getInt("event_year"),
                rs.getInt("event_month"),
                rs.getString("change_type")
        ));
        for (DifferentTrialSample sample : samples) {
            try {
                NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                        sample.personCode(),
                        sample.orgCode(),
                        sample.year(),
                        sample.month(),
                        sample.changeType()
                ));
                if (!result.matchedExpected() && result.expectedHistoryId() != null && !result.expectedHistoryId().isBlank()) {
                    return Optional.of(sample);
                }
            } catch (RuntimeException ignored) {
                // Keep looking; not every legacy row can be replayed by the current rule path.
            }
        }
        return Optional.empty();
    }

    private record DifferentTrialSample(String personCode, String orgCode, int year, int month, String changeType) {
    }
}
