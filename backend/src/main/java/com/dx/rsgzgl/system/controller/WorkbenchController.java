package com.dx.rsgzgl.system.controller;

import com.dx.rsgzgl.common.api.ApiResponse;
import com.dx.rsgzgl.system.dto.SystemAuditLogResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseCancelRequest;
import com.dx.rsgzgl.system.dto.WorkbenchCaseDetailResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseSnapshotResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchPreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteConfirmResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteComparisonResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteExecuteResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWritePlanResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWritePreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteRollbackBatchPreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteRollbackPreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteReviewLedgerResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteReviewRequest;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteSelectedRequest;
import com.dx.rsgzgl.system.dto.WorkbenchGeneratedIssueReviewRequest;
import com.dx.rsgzgl.system.dto.WorkbenchGeneratedIssueReviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchItemResponse;
import com.dx.rsgzgl.system.dto.WorkbenchUserStateRequest;
import com.dx.rsgzgl.system.dto.WorkbenchUserStateResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseCreateRequest;
import com.dx.rsgzgl.system.dto.WorkbenchCasePreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseReviewRequest;
import com.dx.rsgzgl.system.dto.WorkbenchItemsPageResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchExecuteResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchExecuteRequest;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchLedgerResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchReviewRequest;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchRetestResponse;
import com.dx.rsgzgl.system.dto.WorkbenchMetricResponse;
import com.dx.rsgzgl.system.dto.WorkbenchSummaryResponse;
import com.dx.rsgzgl.system.service.SystemAuditService;
import com.dx.rsgzgl.system.service.WorkbenchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/workbench")
public class WorkbenchController {

    private final WorkbenchService workbenchService;
    private final SalaryReportController salaryReportController;
    private final ObjectMapper objectMapper;
    private final SystemAuditService systemAuditService;

    public WorkbenchController(WorkbenchService workbenchService, SalaryReportController salaryReportController, ObjectMapper objectMapper, SystemAuditService systemAuditService) {
        this.workbenchService = workbenchService;
        this.salaryReportController = salaryReportController;
        this.objectMapper = objectMapper;
        this.systemAuditService = systemAuditService;
    }

    @GetMapping("/summary")
    public ApiResponse<WorkbenchSummaryResponse> summary() {
        return ApiResponse.ok(workbenchService.summary());
    }

    @GetMapping("/metrics/salary-todo")
    public ApiResponse<WorkbenchMetricResponse> salaryTodoMetric() {
        return ApiResponse.ok(workbenchService.salaryTodoMetric());
    }

    @PostMapping("/salary-todo-cache/refresh")
    public ApiResponse<WorkbenchMetricResponse> refreshSalaryTodoCache() {
        return ApiResponse.ok(workbenchService.refreshSalaryTodoCache());
    }

    @PostMapping("/salary-todo-cache/dirty")
    public ApiResponse<WorkbenchMetricResponse> markSalaryTodoCacheDirty() {
        return ApiResponse.ok(workbenchService.markSalaryTodoCacheDirty());
    }

    @PostMapping("/generated-timeline-issues/refresh")
    public ApiResponse<WorkbenchMetricResponse> refreshGeneratedTimelineIssueTodos(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "160") int eventLimit
    ) {
        return ApiResponse.ok(workbenchService.refreshGeneratedTimelineIssueTodos(orgCode, keyword, limit, eventLimit));
    }

    @PostMapping("/generated-timeline-issues/{workItemId}/retest")
    public ApiResponse<WorkbenchGeneratedIssueReviewResponse> retestGeneratedTimelineIssue(@PathVariable String workItemId) {
        return ApiResponse.ok(workbenchService.retestGeneratedTimelineIssue(workItemId));
    }

    @PostMapping("/generated-timeline-issues/{workItemId}/review")
    public ApiResponse<WorkbenchGeneratedIssueReviewResponse> reviewGeneratedTimelineIssue(
            @PathVariable String workItemId,
            @RequestBody(required = false) WorkbenchGeneratedIssueReviewRequest request
    ) {
        return ApiResponse.ok(workbenchService.reviewGeneratedTimelineIssue(workItemId, request));
    }

    @PostMapping("/data-governance/tasks/refresh")
    public ApiResponse<WorkbenchMetricResponse> refreshDataGovernanceTasks(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(workbenchService.refreshDataGovernanceTasks(orgCode, limit));
    }

    @PostMapping("/data-governance/tasks/{workItemId}/retest")
    public ApiResponse<WorkbenchGeneratedIssueReviewResponse> retestDataGovernanceTask(@PathVariable String workItemId) {
        return ApiResponse.ok(workbenchService.retestDataGovernanceTask(workItemId));
    }

    @GetMapping("/data-governance/tasks/{workItemId}/migration-delivery-detail")
    public ApiResponse<Map<String, Object>> salaryMigrationDeliveryGovernanceTaskDetail(@PathVariable String workItemId) {
        return ApiResponse.ok(workbenchService.salaryMigrationDeliveryGovernanceTaskDetail(workItemId));
    }

    @GetMapping("/data-governance/tasks/{workItemId}/detail")
    public ApiResponse<Map<String, Object>> dataGovernanceTaskDetail(@PathVariable String workItemId) {
        return ApiResponse.ok(workbenchService.dataGovernanceTaskDetail(workItemId));
    }

    @PostMapping("/data-governance/tasks/{workItemId}/review")
    public ApiResponse<WorkbenchGeneratedIssueReviewResponse> reviewDataGovernanceTask(
            @PathVariable String workItemId,
            @RequestBody(required = false) WorkbenchGeneratedIssueReviewRequest request
    ) {
        return ApiResponse.ok(workbenchService.reviewDataGovernanceTask(workItemId, request));
    }

    @GetMapping("/user-states/{stateKey}")
    public ApiResponse<WorkbenchUserStateResponse> userState(@PathVariable String stateKey) {
        return ApiResponse.ok(workbenchService.userState(stateKey));
    }

    @PutMapping("/user-states/{stateKey}")
    public ApiResponse<WorkbenchUserStateResponse> saveUserState(
            @PathVariable String stateKey,
            @RequestBody WorkbenchUserStateRequest request
    ) {
        return ApiResponse.ok(workbenchService.saveUserState(stateKey, request.state()));
    }

    @DeleteMapping("/user-states/{stateKey}")
    public ApiResponse<WorkbenchUserStateResponse> deleteUserState(@PathVariable String stateKey) {
        return ApiResponse.ok(workbenchService.deleteUserState(stateKey));
    }

    @GetMapping("/salary-business-forms")
    public ApiResponse<List<Map<String, Object>>> salaryBusinessForms() {
        return ApiResponse.ok(workbenchService.salaryBusinessForms());
    }

    @GetMapping("/salary-business-flows")
    public ApiResponse<List<Map<String, Object>>> salaryBusinessFlows() {
        return ApiResponse.ok(workbenchService.salaryBusinessFlows());
    }

    @GetMapping("/salary-rule-maintenance")
    public ApiResponse<Map<String, Object>> salaryRuleMaintenance() {
        return ApiResponse.ok(workbenchService.salaryRuleMaintenance());
    }

    @GetMapping("/migration-readiness")
    public ApiResponse<Map<String, Object>> migrationReadiness() {
        return ApiResponse.ok(workbenchService.migrationReadiness());
    }

    @PostMapping("/normal-grade-applications/preview")
    public ApiResponse<Map<String, Object>> normalGradeApplicationPreview(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "") String changeType
    ) {
        return ApiResponse.ok(workbenchService.normalGradeApplicationPreview(orgCode, year, month, limit, changeType));
    }

    @PostMapping("/normal-grade-applications/generate")
    public ApiResponse<Map<String, Object>> generateNormalGradeApplications(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "") String changeType
    ) {
        return ApiResponse.ok(workbenchService.generateNormalGradeApplications(orgCode, year, month, limit, changeType));
    }

    @PostMapping("/salary-grade-applications/preview")
    public ApiResponse<Map<String, Object>> salaryGradeApplicationPreview(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(workbenchService.salaryGradeApplicationPreview(orgCode, year, month, limit));
    }

    @PostMapping("/salary-grade-applications/generate")
    public ApiResponse<Map<String, Object>> generateSalaryGradeApplications(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(workbenchService.generateSalaryGradeApplications(orgCode, year, month, limit));
    }

    @PostMapping("/entry-salary-applications/preview")
    public ApiResponse<Map<String, Object>> entrySalaryApplicationPreview(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "") String changeType
    ) {
        return ApiResponse.ok(workbenchService.entrySalaryApplicationPreview(orgCode, year, month, limit, changeType));
    }

    @PostMapping("/entry-salary-applications/generate")
    public ApiResponse<Map<String, Object>> generateEntrySalaryApplications(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "") String changeType
    ) {
        return ApiResponse.ok(workbenchService.generateEntrySalaryApplications(orgCode, year, month, limit, changeType));
    }

    @PostMapping("/post-change-applications/preview")
    public ApiResponse<Map<String, Object>> postChangeApplicationPreview(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "") String changeType
    ) {
        return ApiResponse.ok(workbenchService.postChangeApplicationPreview(orgCode, year, month, limit, changeType));
    }

    @PostMapping("/post-change-applications/generate")
    public ApiResponse<Map<String, Object>> generatePostChangeApplications(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "") String changeType
    ) {
        return ApiResponse.ok(workbenchService.generatePostChangeApplications(orgCode, year, month, limit, changeType));
    }

    @PostMapping("/allowance-change-applications/preview")
    public ApiResponse<Map<String, Object>> allowanceChangeApplicationPreview(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "") String changeType
    ) {
        return ApiResponse.ok(workbenchService.allowanceChangeApplicationPreview(orgCode, year, month, limit, changeType));
    }

    @PostMapping("/allowance-change-applications/generate")
    public ApiResponse<Map<String, Object>> generateAllowanceChangeApplications(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "") String changeType
    ) {
        return ApiResponse.ok(workbenchService.generateAllowanceChangeApplications(orgCode, year, month, limit, changeType));
    }

    @PostMapping("/transfer-salary-applications/preview")
    public ApiResponse<Map<String, Object>> transferSalaryApplicationPreview(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "") String changeType
    ) {
        return ApiResponse.ok(workbenchService.transferSalaryApplicationPreview(orgCode, year, month, limit, changeType));
    }

    @PostMapping("/transfer-salary-applications/generate")
    public ApiResponse<Map<String, Object>> generateTransferSalaryApplications(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "") String changeType
    ) {
        return ApiResponse.ok(workbenchService.generateTransferSalaryApplications(orgCode, year, month, limit, changeType));
    }

    @PostMapping("/punishment-reduction-applications/preview")
    public ApiResponse<Map<String, Object>> punishmentReductionApplicationPreview(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "") String changeType
    ) {
        return ApiResponse.ok(workbenchService.punishmentReductionApplicationPreview(orgCode, year, month, limit, changeType));
    }

    @PostMapping("/punishment-reduction-applications/generate")
    public ApiResponse<Map<String, Object>> generatePunishmentReductionApplications(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "") String changeType
    ) {
        return ApiResponse.ok(workbenchService.generatePunishmentReductionApplications(orgCode, year, month, limit, changeType));
    }

    @PostMapping("/application-cases")
    public ApiResponse<WorkbenchItemResponse> createApplicationCase(@RequestBody WorkbenchCaseCreateRequest request) {
        return ApiResponse.ok(workbenchService.createApplicationCase(request));
    }

    @PostMapping("/application-cases/{caseNo}/complete")
    public ApiResponse<WorkbenchItemResponse> completeApplicationCase(
            @PathVariable String caseNo,
            @RequestBody(required = false) WorkbenchCaseReviewRequest request
    ) {
        return ApiResponse.ok(workbenchService.completeApplicationCase(caseNo, request));
    }

    @GetMapping("/application-cases")
    public ApiResponse<List<WorkbenchItemResponse>> applicationCases(
            @RequestParam(defaultValue = "TODO") String status,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(workbenchService.applicationCases(status, limit));
    }

    @GetMapping("/data-governance/scan")
    public ApiResponse<Map<String, Object>> dataGovernanceScan(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(workbenchService.dataGovernanceScan(orgCode, limit));
    }

    @GetMapping(value = "/data-governance/scan.csv", produces = "text/csv")
    public ResponseEntity<byte[]> dataGovernanceScanCsv(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "100") int limit
    ) {
        byte[] body = withUtf8Bom(toDataGovernanceCsv(workbenchService.exportDataGovernanceScan(orgCode, limit)));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("data-governance-" + orgCode + ".csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping("/migration-acceptance")
    public ApiResponse<Map<String, Object>> migrationAcceptanceChecklist() {
        return ApiResponse.ok(workbenchService.migrationAcceptanceChecklist());
    }

    @PostMapping("/migration-acceptance/run")
    public ApiResponse<Map<String, Object>> migrationAcceptanceRun(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(workbenchService.migrationAcceptanceRun(orgCode, limit));
    }

    @PostMapping("/migration-acceptance/run-async")
    public ApiResponse<Map<String, Object>> startMigrationAcceptanceRun(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(workbenchService.startMigrationAcceptanceRun(orgCode, limit));
    }

    @GetMapping("/migration-regression-samples")
    public ApiResponse<Map<String, Object>> migrationRegressionSamples(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(workbenchService.migrationRegressionSamples(orgCode, limit));
    }

    @PostMapping("/migration-regression/run")
    public ApiResponse<Map<String, Object>> migrationRegressionRun(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(workbenchService.migrationRegressionRun(orgCode, limit));
    }

    @GetMapping("/migration-regression/sample-library")
    public ApiResponse<List<Map<String, Object>>> migrationRegressionSampleLibrary(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "") String sampleCode,
            @RequestParam(defaultValue = "") String enabled,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String batchNo,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(workbenchService.migrationRegressionSampleLibrary(orgCode, sampleCode, enabled, keyword, batchNo, limit));
    }

    @PostMapping("/migration-regression/sample-library/refresh")
    public ApiResponse<Map<String, Object>> refreshMigrationRegressionSampleLibrary(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(workbenchService.refreshMigrationRegressionSampleLibrary(orgCode, limit));
    }

    @PostMapping("/migration-regression/sample-library/run")
    public ApiResponse<Map<String, Object>> runMigrationRegressionSampleLibrary(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "") String batchNo,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(workbenchService.runMigrationRegressionSampleLibrary(orgCode, batchNo, limit));
    }

    @GetMapping("/migration-regression/sample-library/runs")
    public ApiResponse<List<Map<String, Object>>> migrationRegressionSampleRuns(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "") String batchNo,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.ok(workbenchService.migrationRegressionSampleRuns(orgCode, batchNo, status, limit));
    }

    @GetMapping("/migration-regression/sample-library/dashboard")
    public ApiResponse<Map<String, Object>> migrationRegressionDashboard(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ApiResponse.ok(workbenchService.migrationRegressionDashboard(orgCode, limit));
    }

    @GetMapping("/migration-quality-overview")
    public ApiResponse<Map<String, Object>> migrationQualityOverview(@RequestParam String orgCode) {
        return ApiResponse.ok(workbenchService.migrationQualityOverview(orgCode));
    }

    @GetMapping(value = "/migration-quality-overview/final-summary.csv", produces = "text/csv")
    public ResponseEntity<byte[]> migrationQualityFinalAcceptanceSummaryCsv(@RequestParam String orgCode) {
        Map<String, Object> overview = workbenchService.migrationQualityOverview(orgCode);
        byte[] body = withUtf8Bom(toMigrationQualityFinalAcceptanceSummaryCsv(overview));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("migration-quality-final-summary-" + orgCode + ".csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @PostMapping("/migration-quality-overview/final-summary/print")
    public ApiResponse<Map<String, Object>> printMigrationQualityFinalAcceptanceSummary(@RequestParam String orgCode) {
        return ApiResponse.ok(workbenchService.printMigrationQualityFinalAcceptanceSummary(orgCode));
    }

    @PostMapping("/migration-quality-overview/snapshots")
    public ApiResponse<Map<String, Object>> createMigrationQualitySnapshot(@RequestParam String orgCode) {
        return ApiResponse.ok(workbenchService.createMigrationQualitySnapshot(orgCode));
    }

    @GetMapping("/migration-quality-overview/snapshots")
    public ApiResponse<List<Map<String, Object>>> migrationQualitySnapshots(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "false") boolean archivedOnly,
            @RequestParam(defaultValue = "") String preflightLevel,
            @RequestParam(defaultValue = "") String archivedBy,
            @RequestParam(defaultValue = "") String archivedFrom,
            @RequestParam(defaultValue = "") String archivedTo
    ) {
        return ApiResponse.ok(workbenchService.migrationQualitySnapshots(orgCode, limit, archivedOnly, preflightLevel, archivedBy, archivedFrom, archivedTo));
    }

    @GetMapping(value = "/migration-quality-overview/snapshots.csv", produces = "text/csv")
    public ResponseEntity<byte[]> migrationQualitySnapshotsCsv(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "false") boolean archivedOnly,
            @RequestParam(defaultValue = "") String preflightLevel,
            @RequestParam(defaultValue = "") String archivedBy,
            @RequestParam(defaultValue = "") String archivedFrom,
            @RequestParam(defaultValue = "") String archivedTo
    ) {
        List<Map<String, Object>> rows = workbenchService.migrationQualitySnapshots(orgCode, limit, archivedOnly, preflightLevel, archivedBy, archivedFrom, archivedTo);
        String latestSnapshotNo = archivedOnly ? latestMigrationQualitySnapshotNo(orgCode) : "";
        byte[] body = withUtf8Bom(toMigrationQualitySnapshotLedgerCsv(rows, latestSnapshotNo));
        String archiveSuffix = archivedOnly ? "-archive" : "";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("migration-quality-snapshots" + archiveSuffix + "-" + orgCode + ".csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping("/migration-quality-overview/snapshots/compare")
    public ApiResponse<Map<String, Object>> compareMigrationQualitySnapshots(
            @RequestParam String baseSnapshotNo,
            @RequestParam String targetSnapshotNo
    ) {
        return ApiResponse.ok(workbenchService.compareMigrationQualitySnapshots(baseSnapshotNo, targetSnapshotNo));
    }

    @GetMapping(value = "/migration-quality-overview/snapshots/compare.csv", produces = "text/csv")
    public ResponseEntity<byte[]> compareMigrationQualitySnapshotsCsv(
            @RequestParam String baseSnapshotNo,
            @RequestParam String targetSnapshotNo
    ) {
        Map<String, Object> comparison = workbenchService.compareMigrationQualitySnapshots(baseSnapshotNo, targetSnapshotNo);
        byte[] body = withUtf8Bom(toMigrationQualitySnapshotComparisonCsv(comparison));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("migration-quality-snapshot-compare-" + baseSnapshotNo + "-" + targetSnapshotNo + ".csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping("/migration-quality-overview/snapshots/{snapshotNo}")
    public ApiResponse<Map<String, Object>> migrationQualitySnapshot(@PathVariable String snapshotNo) {
        return ApiResponse.ok(workbenchService.migrationQualitySnapshot(snapshotNo));
    }

    @PostMapping("/migration-quality-overview/snapshots/{snapshotNo}/print")
    public ApiResponse<Map<String, Object>> printMigrationQualityReport(@PathVariable String snapshotNo) {
        return ApiResponse.ok(workbenchService.printMigrationQualityReport(snapshotNo));
    }

    @PostMapping("/migration-quality-overview/snapshots/{snapshotNo}/archive")
    public ApiResponse<Map<String, Object>> archiveMigrationQualitySnapshot(
            @PathVariable String snapshotNo,
            @RequestParam(defaultValue = "") String note
    ) {
        return ApiResponse.ok(workbenchService.archiveMigrationQualitySnapshot(snapshotNo, note));
    }

    @GetMapping("/migration-quality-overview/snapshots/{snapshotNo}/print-audits")
    public ApiResponse<List<SystemAuditLogResponse>> migrationQualityReportPrintAudits(
            @PathVariable String snapshotNo,
            @RequestParam(defaultValue = "") String action
    ) {
        return ApiResponse.ok(workbenchService.migrationQualityReportPrintAudits(snapshotNo, action));
    }

    @GetMapping(value = "/migration-quality-overview/snapshots/{snapshotNo}/print-audits.csv", produces = "text/csv")
    public ResponseEntity<byte[]> migrationQualityReportPrintAuditsCsv(
            @PathVariable String snapshotNo,
            @RequestParam(defaultValue = "") String action
    ) {
        byte[] body = withUtf8Bom(toHistoryWriteAuditsCsv(workbenchService.migrationQualityReportPrintAudits(snapshotNo, action)));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("migration-quality-print-audits-" + snapshotNo + ".csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping(value = "/migration-quality-overview/snapshots/{snapshotNo}/acceptance-package.zip", produces = "application/zip")
    public ResponseEntity<byte[]> migrationQualityAcceptancePackage(@PathVariable String snapshotNo) throws IOException {
        Map<String, Object> snapshot = workbenchService.migrationQualitySnapshot(snapshotNo);
        String safeSnapshotNo = stringValue(snapshot.get("snapshotNo"));
        String orgCode = stringValue(snapshot.get("orgCode"));
        List<Map<String, Object>> ledger = workbenchService.migrationQualitySnapshots(orgCode, 100, true);
        String latestSnapshotNo = latestMigrationQualitySnapshotNo(orgCode);
        List<String> packageFiles = workbenchService.migrationQualityAcceptancePackageFiles(safeSnapshotNo, orgCode, latestSnapshotNo);
        snapshot = workbenchService.exportMigrationQualityAcceptancePackage(safeSnapshotNo, packageFiles);
        List<SystemAuditLogResponse> audits = workbenchService.migrationQualityReportPrintAudits(safeSnapshotNo);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            addZipEntry(zip, packageFiles.get(0), withUtf8Bom(toMigrationQualityAcceptancePackageReadme(snapshot)));
            addZipEntry(zip, packageFiles.get(1), withUtf8Bom(toMigrationQualityFinalAcceptanceSummaryCsv(workbenchService.migrationQualityOverview(orgCode))));
            addZipEntry(zip, packageFiles.get(2), withUtf8Bom(toMigrationQualityAcceptanceSummaryCsv(snapshot)));
            addZipEntry(zip, packageFiles.get(3), withUtf8Bom(toMigrationQualitySnapshotCsv(snapshot)));
            addZipEntry(zip, packageFiles.get(4), withUtf8Bom(toHistoryWriteAuditsCsv(audits)));
            addZipEntry(zip, packageFiles.get(5), withUtf8Bom(toMigrationQualitySnapshotLedgerCsv(ledger, latestSnapshotNo)));
            addZipEntry(zip, packageFiles.get(6), withUtf8Bom(workbenchService.reportMigrationClosureCsvForAcceptancePackage(orgCode)));
            if (!latestSnapshotNo.isBlank() && !latestSnapshotNo.equals(safeSnapshotNo)) {
                Map<String, Object> comparison = workbenchService.compareMigrationQualitySnapshots(safeSnapshotNo, latestSnapshotNo);
                addZipEntry(zip, packageFiles.get(7), withUtf8Bom(toMigrationQualitySnapshotComparisonCsv(comparison)));
            }
        }
        String exportNo = stringValue(snapshot.get("exportNo"));
        String versionSuffix = exportNo.isBlank() ? "" : "-v" + exportNo;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("migration-quality-acceptance-package-" + safeSnapshotNo + versionSuffix + ".zip", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("application", "zip"))
                .body(output.toByteArray());
    }

    @GetMapping("/migration-quality-overview/snapshots/{snapshotNo}/acceptance-package/preview")
    public ApiResponse<Map<String, Object>> migrationQualityAcceptancePackagePreview(@PathVariable String snapshotNo) {
        Map<String, Object> snapshot = workbenchService.migrationQualitySnapshot(snapshotNo);
        String orgCode = stringValue(snapshot.get("orgCode"));
        String latestSnapshotNo = latestMigrationQualitySnapshotNo(orgCode);
        return ApiResponse.ok(workbenchService.previewMigrationQualityAcceptancePackage(snapshotNo, latestSnapshotNo));
    }

    @GetMapping(value = "/migration-quality-overview/snapshots/{snapshotNo}.csv", produces = "text/csv")
    public ResponseEntity<byte[]> migrationQualitySnapshotCsv(@PathVariable String snapshotNo) {
        byte[] body = withUtf8Bom(toMigrationQualitySnapshotCsv(workbenchService.migrationQualitySnapshot(snapshotNo)));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("migration-quality-" + snapshotNo + ".csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping("/migration-regression/sample-library/runs/{runNo}")
    public ApiResponse<Map<String, Object>> migrationRegressionSampleRunDetail(@PathVariable String runNo) {
        return ApiResponse.ok(workbenchService.migrationRegressionSampleRunDetail(runNo));
    }

    @PostMapping("/migration-regression/sample-library/runs/{runNo}/review")
    public ApiResponse<Map<String, Object>> reviewMigrationRegressionSampleRun(
            @PathVariable String runNo,
            @RequestParam String sampleCode,
            @RequestParam String sampleId,
            @RequestParam String personCode,
            @RequestParam(defaultValue = "") String reviewCategory,
            @RequestParam(defaultValue = "REVIEWED") String reviewStatus,
            @RequestParam(defaultValue = "") String reviewNote
    ) {
        return ApiResponse.ok(workbenchService.reviewMigrationRegressionSampleRun(
                runNo, sampleCode, sampleId, personCode, reviewCategory, reviewStatus, reviewNote
        ));
    }

    @PostMapping("/migration-regression/sample-library/runs/{runNo}/governance-task")
    public ApiResponse<Map<String, Object>> createGovernanceTaskFromMigrationRegression(
            @PathVariable String runNo,
            @RequestParam String sampleCode,
            @RequestParam String sampleId,
            @RequestParam String personCode,
            @RequestParam(defaultValue = "") String reviewCategory,
            @RequestParam(defaultValue = "") String reviewNote
    ) {
        return ApiResponse.ok(workbenchService.createGovernanceTaskFromMigrationRegression(
                runNo, sampleCode, sampleId, personCode, reviewCategory, reviewNote
        ));
    }

    @PostMapping("/migration-regression/sample-library/enabled")
    public ApiResponse<Map<String, Object>> setMigrationRegressionSampleEnabled(
            @RequestParam String orgCode,
            @RequestParam String sampleCode,
            @RequestParam String sampleId,
            @RequestParam String personCode,
            @RequestParam boolean enabled
    ) {
        return ApiResponse.ok(workbenchService.setMigrationRegressionSampleEnabled(orgCode, sampleCode, sampleId, personCode, enabled));
    }

    @PostMapping("/migration-regression/sample-library")
    public ApiResponse<Map<String, Object>> addMigrationRegressionSample(
            @RequestParam String orgCode,
            @RequestParam String sampleCode,
            @RequestParam String sampleId,
            @RequestParam String personCode,
            @RequestParam(defaultValue = "") String title,
            @RequestParam(defaultValue = "") String note
    ) {
        return ApiResponse.ok(workbenchService.addMigrationRegressionSample(orgCode, sampleCode, sampleId, personCode, title, note));
    }

    @GetMapping(value = "/migration-regression/sample-library.csv", produces = "text/csv")
    public ResponseEntity<byte[]> migrationRegressionSampleLibraryCsv(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "") String sampleCode,
            @RequestParam(defaultValue = "") String enabled,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String batchNo,
            @RequestParam(defaultValue = "500") int limit
    ) {
        List<Map<String, Object>> rows = workbenchService.migrationRegressionSampleLibrary(orgCode, sampleCode, enabled, keyword, batchNo, limit);
        byte[] body = withUtf8Bom(toMigrationRegressionSampleLibraryCsv(rows));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("migration-regression-samples-" + orgCode + ".csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @PostMapping("/migration-regression/sample-library/import")
    public ApiResponse<Map<String, Object>> importMigrationRegressionSampleLibrary(
            @RequestParam String orgCode,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        String csvText = new String(file.getBytes(), StandardCharsets.UTF_8);
        return ApiResponse.ok(workbenchService.importMigrationRegressionSamples(orgCode, parseCsvRows(csvText)));
    }

    @GetMapping(value = "/migration-acceptance/run.csv", produces = "text/csv")
    public ResponseEntity<byte[]> migrationAcceptanceRunCsv(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "100") int limit
    ) {
        Map<String, Object> result = workbenchService.latestMigrationAcceptanceRunDetail(orgCode);
        byte[] body = withUtf8Bom(toMigrationAcceptanceRunCsv(result));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("migration-acceptance-" + result.getOrDefault("runNo", orgCode) + ".csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping("/migration-acceptance/runs")
    public ApiResponse<List<Map<String, Object>>> migrationAcceptanceRuns(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.ok(workbenchService.migrationAcceptanceRuns(orgCode, status, limit));
    }

    @GetMapping("/migration-acceptance/runs/{runNo}")
    public ApiResponse<Map<String, Object>> migrationAcceptanceRunDetail(@PathVariable String runNo) {
        return ApiResponse.ok(workbenchService.migrationAcceptanceRunDetail(runNo));
    }

    @GetMapping("/migration-acceptance/runs/{runNo}/issues")
    public ApiResponse<List<Map<String, Object>>> migrationAcceptanceRunIssues(
            @PathVariable String runNo,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(workbenchService.migrationAcceptanceRunIssues(runNo, status, limit));
    }

    @PostMapping("/migration-acceptance/issues/{issueId}/review")
    public ApiResponse<Map<String, Object>> reviewMigrationAcceptanceIssue(
            @PathVariable Long issueId,
            @RequestBody(required = false) WorkbenchGeneratedIssueReviewRequest request
    ) {
        return ApiResponse.ok(workbenchService.reviewMigrationAcceptanceIssue(issueId, request));
    }

    @GetMapping(value = "/migration-acceptance/runs/{runNo}.csv", produces = "text/csv")
    public ResponseEntity<byte[]> migrationAcceptanceRunDetailCsv(@PathVariable String runNo) {
        Map<String, Object> result = workbenchService.exportMigrationAcceptanceRunDetail(runNo);
        byte[] body = withUtf8Bom(toMigrationAcceptanceRunCsv(result));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("migration-acceptance-" + runNo + ".csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping("/items")
    public ApiResponse<WorkbenchItemsPageResponse> items(
            @RequestParam(defaultValue = "TODO") String status,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String changeType,
            @RequestParam(defaultValue = "") String source,
            @RequestParam(defaultValue = "") String caseStatus,
            @RequestParam(defaultValue = "") String trialStatus,
            @RequestParam(defaultValue = "") String reviewStatus,
            @RequestParam(defaultValue = "") String workflowStatus,
            @RequestParam(defaultValue = "") String closureStatus,
            @RequestParam(defaultValue = "") String nextAction
    ) {
        return ApiResponse.ok(workbenchService.items(status, offset, limit, keyword, changeType, source, caseStatus, trialStatus, reviewStatus, workflowStatus, closureStatus, nextAction));
    }

    @PostMapping("/salary-cases")
    public ApiResponse<WorkbenchItemResponse> completeSalaryCase(@RequestBody WorkbenchCaseCreateRequest request) {
        return ApiResponse.ok(workbenchService.completeSalaryCase(request));
    }

    @PostMapping("/salary-cases/preview")
    public ApiResponse<WorkbenchCasePreviewResponse> previewSalaryCase(@RequestBody WorkbenchCaseCreateRequest request) {
        return ApiResponse.ok(workbenchService.previewSalaryCase(request));
    }

    @GetMapping("/salary-cases/{caseNo}")
    public ApiResponse<WorkbenchCaseDetailResponse> salaryCaseDetail(@PathVariable String caseNo) {
        return ApiResponse.ok(workbenchService.caseDetail(caseNo));
    }

    @GetMapping("/salary-cases/{caseNo}/snapshot")
    public ApiResponse<WorkbenchCaseSnapshotResponse> salaryCaseSnapshot(@PathVariable String caseNo) {
        return ApiResponse.ok(workbenchService.caseSnapshot(caseNo));
    }

    @PostMapping("/salary-cases/{caseNo}/history-write-preview")
    public ApiResponse<WorkbenchHistoryWritePreviewResponse> salaryCaseHistoryWritePreview(@PathVariable String caseNo) {
        return ApiResponse.ok(workbenchService.historyWritePreview(caseNo));
    }

    @PostMapping("/salary-cases/{caseNo}/history-write-confirm")
    public ApiResponse<WorkbenchHistoryWriteConfirmResponse> salaryCaseHistoryWriteConfirm(@PathVariable String caseNo) {
        return ApiResponse.ok(workbenchService.historyWriteConfirm(caseNo));
    }

    @GetMapping("/history-write-plans")
    public ApiResponse<List<WorkbenchHistoryWritePlanResponse>> historyWritePlans(
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String comparisonStatus,
            @RequestParam(defaultValue = "") String reviewStatus,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String mismatchField,
            @RequestParam(defaultValue = "") String maintenanceTarget,
            @RequestParam(defaultValue = "") String retestStatus,
            @RequestParam(defaultValue = "") String priority,
            @RequestParam(defaultValue = "") String actionCode,
            @RequestParam(defaultValue = "") String pendingQueue,
            @RequestParam(defaultValue = "") String printQueue,
            @RequestParam(defaultValue = "") String statusQueue,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ApiResponse.ok(workbenchService.historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, pendingQueue, printQueue, statusQueue, limit));
    }

    @GetMapping("/history-write-pending-queues")
    public ApiResponse<List<Map<String, Object>>> historyWritePendingQueues(
            @RequestParam(defaultValue = "") String keyword
    ) {
        return ApiResponse.ok(workbenchService.historyWritePendingQueues(keyword));
    }

    @GetMapping("/history-write-batches")
    public ApiResponse<List<WorkbenchHistoryWriteBatchLedgerResponse>> historyWriteBatches(
            @RequestParam(defaultValue = "") String queue,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ApiResponse.ok(workbenchService.historyWriteBatchLedger(queue, limit));
    }

    @GetMapping("/history-write-delivery-acceptances")
    public ApiResponse<List<Map<String, Object>>> historyWriteDeliveryAcceptances(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String exportType,
            @RequestParam(defaultValue = "") String exportedFrom,
            @RequestParam(defaultValue = "") String exportedTo,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ApiResponse.ok(workbenchService.historyWriteDeliveryAcceptances(keyword, exportType, exportedFrom, exportedTo, limit));
    }

    @GetMapping(value = "/history-write-delivery-acceptances/index.csv", produces = "text/csv")
    public ResponseEntity<byte[]> historyWriteDeliveryAcceptanceIndexCsv(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String exportType,
            @RequestParam(defaultValue = "") String exportedFrom,
            @RequestParam(defaultValue = "") String exportedTo,
            @RequestParam(defaultValue = "200") int limit
    ) {
        workbenchService.requireHistoryDeliveryExportPermission();
        List<Map<String, Object>> rows = workbenchService.historyWriteDeliveryAcceptances(keyword, exportType, exportedFrom, exportedTo, limit);
        systemAuditService.record("workbench", "history-write-delivery-acceptance-index-csv", "HISTORY_WRITE_DELIVERY_ACCEPTANCE", "INDEX",
                historyDeliveryAcceptanceAuditScope(keyword, exportType, exportedFrom, exportedTo, limit) + ", count=" + rows.size());
        byte[] body = withUtf8Bom(toHistoryDeliveryAcceptanceIndexCsv(rows));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("history-write-delivery-acceptance-index.csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping(value = "/history-write-delivery-acceptances/print-batch", produces = "text/html")
    public ResponseEntity<String> historyWriteDeliveryAcceptanceBatchPrint(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String exportType,
            @RequestParam(defaultValue = "") String exportedFrom,
            @RequestParam(defaultValue = "") String exportedTo,
            @RequestParam(defaultValue = "20") int limit
    ) {
        workbenchService.requireHistoryDeliveryExportPermission();
        List<Map<String, Object>> acceptances = workbenchService.historyWriteDeliveryAcceptances(keyword, exportType, exportedFrom, exportedTo, limit);
        List<Map<String, Object>> details = acceptances.stream()
                .map(row -> workbenchService.historyWriteDeliveryAcceptanceDetail(stringValue(row.get("acceptanceNo"))))
                .toList();
        long auditId = systemAuditService.record("workbench", "history-write-delivery-acceptance-batch-print", "HISTORY_WRITE_DELIVERY_ACCEPTANCE", "BATCH",
                historyDeliveryAcceptanceAuditScope(keyword, exportType, exportedFrom, exportedTo, limit) + ", count=" + details.size());
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .body(toHistoryDeliveryAcceptanceBatchPrintHtml(details, keyword, exportType, exportedFrom, exportedTo, auditId));
    }

    @GetMapping("/history-write-delivery-acceptances/{acceptanceNo}")
    public ApiResponse<Map<String, Object>> historyWriteDeliveryAcceptanceDetail(@PathVariable String acceptanceNo) {
        return ApiResponse.ok(workbenchService.historyWriteDeliveryAcceptanceDetail(acceptanceNo));
    }

    @GetMapping(value = "/history-write-delivery-acceptances/{acceptanceNo}.csv", produces = "text/csv")
    public ResponseEntity<byte[]> historyWriteDeliveryAcceptanceDetailCsv(@PathVariable String acceptanceNo) {
        workbenchService.requireHistoryDeliveryExportPermission();
        Map<String, Object> detail = workbenchService.historyWriteDeliveryAcceptanceDetail(acceptanceNo);
        systemAuditService.record("workbench", "history-write-delivery-acceptance-detail-csv", "HISTORY_WRITE_DELIVERY_ACCEPTANCE", stringValue(detail.get("acceptanceNo")),
                historyDeliveryAcceptanceDetailAuditScope(detail));
        byte[] body = withUtf8Bom(toHistoryDeliveryAcceptanceDetailCsv(detail));
        String filename = "history-write-delivery-acceptance-" + stringValue(detail.get("acceptanceNo")) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping(value = "/history-write-delivery-acceptances/{acceptanceNo}/print", produces = "text/html")
    public ResponseEntity<String> historyWriteDeliveryAcceptancePrint(@PathVariable String acceptanceNo) {
        workbenchService.requireHistoryDeliveryExportPermission();
        Map<String, Object> detail = workbenchService.historyWriteDeliveryAcceptanceDetail(acceptanceNo);
        String resolvedAcceptanceNo = stringValue(detail.get("acceptanceNo"));
        systemAuditService.record("workbench", "history-write-delivery-acceptance-print", "HISTORY_WRITE_DELIVERY_ACCEPTANCE", resolvedAcceptanceNo,
                historyDeliveryAcceptanceDetailAuditScope(detail));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .body(toHistoryDeliveryAcceptancePrintHtml(detail));
    }

    @GetMapping(value = "/history-write-batches.csv", produces = "text/csv")
    public ResponseEntity<byte[]> historyWriteBatchesCsv(
            @RequestParam(defaultValue = "") String queue,
            @RequestParam(defaultValue = "200") int limit
    ) {
        List<WorkbenchHistoryWriteBatchLedgerResponse> batches = workbenchService.historyWriteBatchLedger(queue, limit);
        systemAuditService.record("workbench", "history-write-batch-ledger-csv", "HISTORY_WRITE_BATCH_LEDGER", stringValue(queue).isBlank() ? "ALL" : stringValue(queue),
                "queue=" + stringValue(queue) + ", limit=" + limit + ", count=" + batches.size());
        byte[] body = withUtf8Bom(toHistoryWriteBatchLedgerCsv(batches));
        String suffix = stringValue(queue).isBlank() ? "" : "-" + stringValue(queue);
        String filename = "history-write-batch-ledger" + suffix + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping(value = "/history-write-delivery-overview.csv", produces = "text/csv")
    public ResponseEntity<byte[]> historyWriteDeliveryOverviewCsv() {
        workbenchService.requireHistoryDeliveryExportPermission();
        WorkbenchSummaryResponse summary = workbenchService.summary();
        List<HistoryClosureAcceptanceRow> rows = historyClosureAcceptanceRows(summary);
        List<HistoryDeliveryEvidenceRow> evidenceRows = historyDeliveryEvidenceRows(rows);
        long pending = rows.stream().filter(row -> !"\u5df2\u95ed\u73af".equals(row.result())).mapToLong(HistoryClosureAcceptanceRow::count).sum();
        long closed = rows.stream().filter(row -> "\u5df2\u95ed\u73af".equals(row.result())).mapToLong(HistoryClosureAcceptanceRow::count).sum();
        long activeQueues = rows.stream().filter(row -> !"\u5df2\u95ed\u73af".equals(row.result()) && row.count() > 0).count();
        String conclusion = pending > 0
                ? "\u5c1a\u6709 " + pending + " \u9879\u5f85\u5904\u7406\uff0c\u9700\u6309\u961f\u5217\u7ee7\u7eed\u63a8\u8fdb"
                : "\u5f53\u524d\u5386\u53f2\u5199\u5165\u961f\u5217\u65e0\u5f85\u5904\u7406\u9879\uff0c\u53ef\u8fdb\u884c\u9a8c\u6536\u5305\u4ea4\u4ed8";
        String acceptanceNo = workbenchService.recordHistoryWriteDeliveryAcceptance("OVERVIEW", pending, closed, activeQueues, evidenceRows.size(), conclusion,
                Map.of("rows", rows, "evidence", evidenceRows));
        systemAuditService.record("workbench", "history-write-delivery-overview-csv", "HISTORY_WRITE_DELIVERY", "OVERVIEW",
                "acceptanceNo=" + acceptanceNo + ", rows=" + rows.size() + ", pending=" + pending + ", closed=" + closed + ", activeQueues=" + activeQueues + ", evidence=" + evidenceRows.size());
        byte[] body = withUtf8Bom(toHistoryDeliveryOverviewCsv(rows, evidenceRows));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("history-write-delivery-overview.csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping(value = "/salary-migration-closure-checklist.csv", produces = "text/csv")
    public ResponseEntity<byte[]> salaryMigrationClosureChecklistCsv() {
        workbenchService.requireHistoryDeliveryExportPermission();
        WorkbenchSummaryResponse summary = workbenchService.summary();
        List<HistoryClosureAcceptanceRow> rows = historyClosureAcceptanceRows(summary);
        long pending = rows.stream().filter(row -> !"\u5df2\u95ed\u73af".equals(row.result())).mapToLong(HistoryClosureAcceptanceRow::count).sum();
        long closed = rows.stream().filter(row -> "\u5df2\u95ed\u73af".equals(row.result())).mapToLong(HistoryClosureAcceptanceRow::count).sum();
        systemAuditService.record("workbench", "salary-migration-closure-checklist-csv", "SALARY_MIGRATION_CLOSURE", "CHECKLIST",
                "rows=9, pending=" + pending + ", closed=" + closed);
        byte[] body = withUtf8Bom(toSalaryMigrationClosureChecklistCsv(rows));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("salary-migration-closure-checklist.csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping(value = "/salary-migration-delivery-ledger.csv", produces = "text/csv")
    public ResponseEntity<byte[]> salaryMigrationDeliveryLedgerCsv(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "200") int limit
    ) {
        List<Map<String, Object>> rows = workbenchService.salaryMigrationDeliveryClosureLedger(orgCode, limit);
        String safeOrgCode = stringValue(orgCode).isBlank() ? "ALL" : stringValue(orgCode);
        systemAuditService.record("workbench", "salary-migration-delivery-ledger-csv", "SALARY_MIGRATION_DELIVERY", safeOrgCode,
                "org=" + safeOrgCode + ", count=" + rows.size());
        byte[] body = withUtf8Bom(toSalaryMigrationDeliveryLedgerCsv(rows));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("salary-migration-delivery-ledger.csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping(value = "/salary-migration-delivery-self-check.csv", produces = "text/csv")
    public ResponseEntity<byte[]> salaryMigrationDeliverySelfCheckCsv(
            @RequestParam(defaultValue = "") String orgCode
    ) {
        List<Map<String, Object>> rows = workbenchService.salaryMigrationDeliveryFinalSelfCheck(orgCode);
        String safeOrgCode = stringValue(orgCode).isBlank() ? "ALL" : stringValue(orgCode);
        long warn = rows.stream().filter(row -> !"PASS".equals(stringValue(row.get("status")))).count();
        systemAuditService.record("workbench", "salary-migration-delivery-self-check-csv", "SALARY_MIGRATION_DELIVERY", safeOrgCode,
                "org=" + safeOrgCode + ", rows=" + rows.size() + ", warn=" + warn);
        byte[] body = withUtf8Bom(toSalaryMigrationDeliverySelfCheckCsv(rows));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("salary-migration-delivery-self-check.csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping(value = "/salary-migration-delivery-package.zip", produces = "application/zip")
    public ResponseEntity<byte[]> salaryMigrationDeliveryPackage(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "300") int limit
    ) throws IOException {
        workbenchService.requireHistoryClosureAcceptancePackagePermission();
        WorkbenchSummaryResponse summary = workbenchService.summary();
        List<HistoryClosureAcceptanceRow> rows = historyClosureAcceptanceRows(summary);
        byte[] checklist = withUtf8Bom(toSalaryMigrationClosureChecklistCsv(rows));
        byte[] historyPackage = null;
        String historyPackageError = "";
        try {
            historyPackage = historyWriteClosureAcceptancePackage().getBody();
        } catch (RuntimeException ex) {
            historyPackageError = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        }
        byte[] reportPackage = null;
        String reportPackageError = "";
        try {
            reportPackage = salaryReportController.reportMigrationDeliveryPackage(orgCode, year, month, businessType, keyword, limit).getBody();
        } catch (RuntimeException ex) {
            reportPackageError = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        }
        String safeOrgCode = stringValue(orgCode).isBlank() ? "ALL" : stringValue(orgCode);
        long pending = rows.stream().filter(row -> !"\u5df2\u95ed\u73af".equals(row.result())).mapToLong(HistoryClosureAcceptanceRow::count).sum();
        long closed = rows.stream().filter(row -> "\u5df2\u95ed\u73af".equals(row.result())).mapToLong(HistoryClosureAcceptanceRow::count).sum();
        String governanceWorkItemId = workbenchService.recordSalaryMigrationDeliveryPackageGovernanceTask(
                safeOrgCode, year, month, businessType, keyword, limit, historyPackageError, reportPackageError);
        systemAuditService.record("workbench", "salary-migration-delivery-package", "SALARY_MIGRATION_DELIVERY", safeOrgCode,
                "files=7, org=" + safeOrgCode + ", year=" + year + ", month=" + month + ", keyword=" + stringValue(keyword)
                        + ", historyStatus=" + (historyPackageError.isBlank() ? "READY" : "ERROR")
                        + ", reportStatus=" + (reportPackageError.isBlank() ? "READY" : "ERROR")
                        + ", pending=" + pending + ", closed=" + closed
                        + ", governanceWorkItemId=" + governanceWorkItemId);
        byte[] deliveryLedger = null;
        String deliveryLedgerError = "";
        try {
            deliveryLedger = withUtf8Bom(toSalaryMigrationDeliveryLedgerCsv(workbenchService.salaryMigrationDeliveryClosureLedger(safeOrgCode, Math.max(limit, 200))));
        } catch (RuntimeException ex) {
            deliveryLedgerError = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        }
        byte[] selfCheck = null;
        String selfCheckError = "";
        try {
            selfCheck = withUtf8Bom(toSalaryMigrationDeliverySelfCheckCsv(workbenchService.salaryMigrationDeliveryFinalSelfCheck(safeOrgCode)));
        } catch (RuntimeException ex) {
            selfCheckError = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            addZipEntry(zip, "README.txt", withUtf8Bom(toSalaryMigrationDeliveryReadme(safeOrgCode, year, month, businessType, keyword, limit, pending, closed, historyPackageError, reportPackageError)));
            addZipEntry(zip, "salary-migration-delivery-index.csv", withUtf8Bom(toSalaryMigrationDeliveryIndexCsv(safeOrgCode, year, month, businessType, keyword, limit, historyPackageError, reportPackageError)));
            addZipEntry(zip, "salary-migration-closure-checklist.csv", checklist);
            if (deliveryLedgerError.isBlank()) {
                addZipEntry(zip, "salary-migration-delivery-ledger.csv", deliveryLedger == null ? new byte[0] : deliveryLedger);
            } else {
                addZipEntry(zip, "salary-migration-delivery-ledger-error.txt", withUtf8Bom(toSalaryMigrationDeliveryLedgerError(deliveryLedgerError, safeOrgCode)));
            }
            if (selfCheckError.isBlank()) {
                addZipEntry(zip, "salary-migration-delivery-self-check.csv", selfCheck == null ? new byte[0] : selfCheck);
            } else {
                addZipEntry(zip, "salary-migration-delivery-self-check-error.txt", withUtf8Bom(toSalaryMigrationDeliverySelfCheckError(selfCheckError, safeOrgCode)));
            }
            if (historyPackageError.isBlank()) {
                addZipEntry(zip, "history-write-closure-acceptance-package.zip", historyPackage == null ? new byte[0] : historyPackage);
            } else {
                addZipEntry(zip, "history-write-closure-acceptance-package-error.txt", withUtf8Bom(toSalaryMigrationDeliveryHistoryError(historyPackageError, safeOrgCode, year, month, businessType, keyword, limit)));
            }
            if (reportPackageError.isBlank()) {
                addZipEntry(zip, "salary-report-migration-delivery-package.zip", reportPackage == null ? new byte[0] : reportPackage);
            } else {
                addZipEntry(zip, "salary-report-migration-delivery-package-error.txt", withUtf8Bom(toSalaryMigrationDeliveryReportError(reportPackageError, safeOrgCode, year, month, businessType, keyword, limit)));
            }
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("salary-migration-delivery-package.zip", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("application", "zip"))
                .body(output.toByteArray());
    }

    @GetMapping(value = "/history-write-closure-acceptance-package.zip", produces = "application/zip")
    public ResponseEntity<byte[]> historyWriteClosureAcceptancePackage() throws IOException {
        workbenchService.requireHistoryClosureAcceptancePackagePermission();
        WorkbenchSummaryResponse summary = workbenchService.summary();
        List<HistoryClosureAcceptanceRow> rows = historyClosureAcceptanceRows(summary);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            addZipEntry(zip, "README.txt", withUtf8Bom(toHistoryClosureAcceptanceReadme(rows)));
            addZipEntry(zip, "salary-migration-closure-checklist.csv", withUtf8Bom(toSalaryMigrationClosureChecklistCsv(rows)));
            addZipEntry(zip, "history-write-closure-acceptance-summary.csv", withUtf8Bom(toHistoryClosureAcceptanceCsv(rows)));
            addZipEntry(zip, "history-write-safety-policy.csv", withUtf8Bom(toHistoryWriteSafetyPolicyCsv()));
            addZipEntry(zip, "history-write-plans-unprinted-or-plan.csv", withUtf8Bom(toHistoryWritePlansCsv(workbenchService.exportHistoryWritePlans("PREPARED", "", "", "", "", "", "", "", "WRITE_HISTORY", "", "UNPRINTED_BLOCKED", "", 5000))));
            addZipEntry(zip, "history-write-plans-ready-to-write.csv", withUtf8Bom(toHistoryWritePlansCsv(workbenchService.exportHistoryWritePlans("PREPARED", "", "", "", "", "", "", "", "WRITE_HISTORY", "", "PRINTED_READY", "", 5000))));
            addZipEntry(zip, "history-write-plans-review-difference.csv", withUtf8Bom(toHistoryWritePlansCsv(workbenchService.exportHistoryWritePlans("EXECUTED", "MISMATCHED", "PENDING", "", "", "", "", "", "", "review", "", "", 5000))));
            addZipEntry(zip, "history-write-plans-rolled-back.csv", withUtf8Bom(toHistoryWritePlansCsv(workbenchService.exportHistoryWritePlans("", "", "", "", "", "", "", "", "", "", "", "ROLLED_BACK", 5000))));
            addZipEntry(zip, "history-write-batch-ledger-write.csv", withUtf8Bom(toHistoryWriteBatchLedgerCsv(workbenchService.historyWriteBatchLedger("SALARY_NEXT_EXECUTE_WRITE", 1000))));
            addZipEntry(zip, "history-write-batch-ledger-review.csv", withUtf8Bom(toHistoryWriteBatchLedgerCsv(workbenchService.historyWriteBatchLedger("SALARY_NEXT_REVIEW_DIFFERENCE", 1000))));
            addZipEntry(zip, "history-write-batch-ledger-rolled-back.csv", withUtf8Bom(toHistoryWriteBatchLedgerCsv(workbenchService.historyWriteBatchLedger("HISTORY_PLAN_ROLLED_BACK", 1000))));
        }
        long pending = rows.stream().filter(row -> !"\u5df2\u95ed\u73af".equals(row.result())).mapToLong(HistoryClosureAcceptanceRow::count).sum();
        long closed = rows.stream().filter(row -> "\u5df2\u95ed\u73af".equals(row.result())).mapToLong(HistoryClosureAcceptanceRow::count).sum();
        List<HistoryDeliveryEvidenceRow> evidenceRows = historyDeliveryEvidenceRows(rows);
        long activeQueues = rows.stream().filter(row -> !"\u5df2\u95ed\u73af".equals(row.result()) && row.count() > 0).count();
        String conclusion = pending > 0
                ? "\u5c1a\u6709 " + pending + " \u9879\u5f85\u5904\u7406\uff0c\u9700\u6309\u961f\u5217\u7ee7\u7eed\u63a8\u8fdb"
                : "\u5f53\u524d\u5386\u53f2\u5199\u5165\u961f\u5217\u65e0\u5f85\u5904\u7406\u9879\uff0c\u53ef\u8fdb\u884c\u9a8c\u6536\u5305\u4ea4\u4ed8";
        String acceptanceNo = workbenchService.recordHistoryWriteDeliveryAcceptance("PACKAGE", pending, closed, activeQueues, evidenceRows.size(), conclusion,
                Map.of("rows", rows, "evidence", evidenceRows));
        systemAuditService.record("workbench", "history-write-closure-acceptance-package", "HISTORY_WRITE_ACCEPTANCE", "PACKAGE",
                "acceptanceNo=" + acceptanceNo + ", rows=" + rows.size() + ", pending=" + pending + ", closed=" + closed);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("history-write-closure-acceptance-package.zip", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("application", "zip"))
                .body(output.toByteArray());
    }

    @GetMapping("/history-write-batches/{batchNo}/audits")
    public ApiResponse<List<SystemAuditLogResponse>> historyWriteBatchAudits(@PathVariable String batchNo) {
        return ApiResponse.ok(workbenchService.historyWriteBatchAudits(batchNo));
    }

    @GetMapping(value = "/history-write-batches/{batchNo}/audits.csv", produces = "text/csv")
    public ResponseEntity<byte[]> historyWriteBatchAuditsCsv(@PathVariable String batchNo) {
        List<SystemAuditLogResponse> audits = workbenchService.historyWriteBatchAudits(batchNo);
        systemAuditService.record("workbench", "history-write-batch-audits-csv", "HISTORY_WRITE_BATCH", batchNo,
                "batchNo=" + batchNo + ", count=" + audits.size());
        byte[] body = withUtf8Bom(toHistoryWriteAuditsCsv(audits));
        String filename = "history-write-batch-audits-" + batchNo + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @PostMapping("/history-write-plans/batch-preview")
    public ApiResponse<WorkbenchHistoryWriteBatchPreviewResponse> batchPreviewHistoryWritePlans(
            @RequestParam(defaultValue = "PREPARED") String status,
            @RequestParam(defaultValue = "") String comparisonStatus,
            @RequestParam(defaultValue = "") String reviewStatus,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String mismatchField,
            @RequestParam(defaultValue = "") String maintenanceTarget,
            @RequestParam(defaultValue = "") String retestStatus,
            @RequestParam(defaultValue = "") String priority,
            @RequestParam(defaultValue = "") String actionCode,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ApiResponse.ok(workbenchService.batchPreviewHistoryWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit));
    }

    @PostMapping("/history-write-plans/batch-execute")
    public ApiResponse<WorkbenchHistoryWriteBatchExecuteResponse> batchExecuteHistoryWritePlans(
            @RequestParam(defaultValue = "PREPARED") String status,
            @RequestParam(defaultValue = "") String comparisonStatus,
            @RequestParam(defaultValue = "") String reviewStatus,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String mismatchField,
            @RequestParam(defaultValue = "") String maintenanceTarget,
            @RequestParam(defaultValue = "") String retestStatus,
            @RequestParam(defaultValue = "") String priority,
            @RequestParam(defaultValue = "") String actionCode,
            @RequestParam(defaultValue = "50") int limit,
            @RequestBody(required = false) WorkbenchHistoryWriteBatchExecuteRequest request
    ) {
        return ApiResponse.ok(workbenchService.batchExecuteHistoryWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit, request));
    }

    @PostMapping("/history-write-plans/selected-preview")
    public ApiResponse<WorkbenchHistoryWriteBatchPreviewResponse> selectedPreviewHistoryWritePlans(
            @RequestBody WorkbenchHistoryWriteSelectedRequest request
    ) {
        return ApiResponse.ok(workbenchService.batchPreviewSelectedHistoryWritePlans(request.caseNos()));
    }

    @PostMapping("/history-write-plans/selected-execute")
    public ApiResponse<WorkbenchHistoryWriteBatchExecuteResponse> selectedExecuteHistoryWritePlans(
            @RequestBody WorkbenchHistoryWriteSelectedRequest request
    ) {
        return ApiResponse.ok(workbenchService.batchExecuteSelectedHistoryWritePlans(request.caseNos(), request.safetyToken()));
    }

    @PostMapping("/history-write-plans/batch-retest-preview")
    public ApiResponse<WorkbenchHistoryWriteBatchRetestResponse> batchRetestHistoryWritePlans(
            @RequestParam(defaultValue = "EXECUTED") String status,
            @RequestParam(defaultValue = "MISMATCHED") String comparisonStatus,
            @RequestParam(defaultValue = "") String reviewStatus,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String mismatchField,
            @RequestParam(defaultValue = "") String maintenanceTarget,
            @RequestParam(defaultValue = "") String retestStatus,
            @RequestParam(defaultValue = "") String priority,
            @RequestParam(defaultValue = "RETEST_FIRST") String actionCode,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ApiResponse.ok(workbenchService.batchRetestHistoryWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit));
    }

    @PostMapping("/history-write-plans/selected-retest-preview")
    public ApiResponse<WorkbenchHistoryWriteBatchRetestResponse> selectedRetestHistoryWritePlans(
            @RequestBody WorkbenchHistoryWriteSelectedRequest request
    ) {
        return ApiResponse.ok(workbenchService.batchRetestSelectedHistoryWritePlans(request.caseNos()));
    }

    @PostMapping("/history-write-plans/batch-retest-approve")
    public ApiResponse<WorkbenchHistoryWriteBatchExecuteResponse> batchApproveRetestPassedHistoryWritePlans(
            @RequestParam(defaultValue = "EXECUTED") String status,
            @RequestParam(defaultValue = "MISMATCHED") String comparisonStatus,
            @RequestParam(defaultValue = "PENDING") String reviewStatus,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String mismatchField,
            @RequestParam(defaultValue = "") String maintenanceTarget,
            @RequestParam(defaultValue = "") String retestStatus,
            @RequestParam(defaultValue = "") String priority,
            @RequestParam(defaultValue = "RETEST_FIRST") String actionCode,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ApiResponse.ok(workbenchService.batchApproveRetestPassedHistoryWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit));
    }

    @PostMapping("/history-write-plans/selected-retest-approve")
    public ApiResponse<WorkbenchHistoryWriteBatchExecuteResponse> selectedApproveRetestPassedHistoryWritePlans(
            @RequestBody WorkbenchHistoryWriteSelectedRequest request
    ) {
        return ApiResponse.ok(workbenchService.batchApproveRetestPassedSelectedHistoryWritePlans(request.caseNos()));
    }

    @PostMapping("/history-write-plans/batch-review")
    public ApiResponse<WorkbenchHistoryWriteBatchExecuteResponse> batchReviewHistoryWriteComparisons(
            @RequestParam(defaultValue = "EXECUTED") String status,
            @RequestParam(defaultValue = "MISMATCHED") String comparisonStatus,
            @RequestParam(defaultValue = "PENDING") String reviewStatus,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String mismatchField,
            @RequestParam(defaultValue = "") String maintenanceTarget,
            @RequestParam(defaultValue = "") String retestStatus,
            @RequestParam(defaultValue = "") String priority,
            @RequestParam(defaultValue = "") String actionCode,
            @RequestParam(defaultValue = "50") int limit,
            @RequestBody WorkbenchHistoryWriteBatchReviewRequest request
    ) {
        return ApiResponse.ok(workbenchService.batchReviewHistoryWriteComparisons(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit, request));
    }

    @PostMapping("/history-write-plans/selected-review")
    public ApiResponse<WorkbenchHistoryWriteBatchExecuteResponse> selectedReviewHistoryWriteComparisons(
            @RequestBody WorkbenchHistoryWriteBatchReviewRequest request
    ) {
        return ApiResponse.ok(workbenchService.batchReviewSelectedHistoryWriteComparisons(request));
    }

    @PostMapping("/history-write-plans/batch-special-review")
    public ApiResponse<WorkbenchHistoryWriteBatchExecuteResponse> batchMarkSpecialHistoryWritePlans(
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String comparisonStatus,
            @RequestParam(defaultValue = "PENDING") String reviewStatus,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String mismatchField,
            @RequestParam(defaultValue = "") String maintenanceTarget,
            @RequestParam(defaultValue = "") String retestStatus,
            @RequestParam(defaultValue = "") String priority,
            @RequestParam(defaultValue = "") String actionCode,
            @RequestParam(defaultValue = "50") int limit,
            @RequestBody(required = false) WorkbenchHistoryWriteBatchReviewRequest request
    ) {
        return ApiResponse.ok(workbenchService.batchMarkSpecialHistoryWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit, request));
    }

    @PostMapping("/history-write-plans/selected-special-review")
    public ApiResponse<WorkbenchHistoryWriteBatchExecuteResponse> selectedMarkSpecialHistoryWritePlans(
            @RequestBody(required = false) WorkbenchHistoryWriteBatchReviewRequest request
    ) {
        return ApiResponse.ok(workbenchService.batchMarkSelectedSpecialHistoryWritePlans(request));
    }

    @PostMapping("/history-write-plans/batch-rollback-preview")
    public ApiResponse<WorkbenchHistoryWriteRollbackBatchPreviewResponse> batchRollbackPreviewHistoryWritePlans(
            @RequestParam(defaultValue = "EXECUTED") String status,
            @RequestParam(defaultValue = "") String comparisonStatus,
            @RequestParam(defaultValue = "") String reviewStatus,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String mismatchField,
            @RequestParam(defaultValue = "") String maintenanceTarget,
            @RequestParam(defaultValue = "") String retestStatus,
            @RequestParam(defaultValue = "") String priority,
            @RequestParam(defaultValue = "") String actionCode,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ApiResponse.ok(workbenchService.batchPreviewRollbackHistoryWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit));
    }

    @PostMapping("/history-write-plans/batch-rollback")
    public ApiResponse<WorkbenchHistoryWriteBatchExecuteResponse> batchRollbackHistoryWritePlans(
            @RequestParam(defaultValue = "EXECUTED") String status,
            @RequestParam(defaultValue = "") String comparisonStatus,
            @RequestParam(defaultValue = "") String reviewStatus,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String mismatchField,
            @RequestParam(defaultValue = "") String maintenanceTarget,
            @RequestParam(defaultValue = "") String retestStatus,
            @RequestParam(defaultValue = "") String priority,
            @RequestParam(defaultValue = "") String actionCode,
            @RequestParam(defaultValue = "50") int limit,
            @RequestBody(required = false) WorkbenchHistoryWriteBatchExecuteRequest request
    ) {
        return ApiResponse.ok(workbenchService.batchRollbackHistoryWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit, request));
    }

    @PostMapping("/history-write-plans/selected-rollback-preview")
    public ApiResponse<WorkbenchHistoryWriteRollbackBatchPreviewResponse> selectedRollbackPreviewHistoryWritePlans(
            @RequestBody WorkbenchHistoryWriteSelectedRequest request
    ) {
        return ApiResponse.ok(workbenchService.batchPreviewSelectedRollbackHistoryWritePlans(request.caseNos()));
    }

    @PostMapping("/history-write-plans/selected-rollback")
    public ApiResponse<WorkbenchHistoryWriteBatchExecuteResponse> selectedRollbackHistoryWritePlans(
            @RequestBody WorkbenchHistoryWriteSelectedRequest request
    ) {
        return ApiResponse.ok(workbenchService.batchRollbackSelectedHistoryWritePlans(request.caseNos(), request.safetyToken()));
    }

    @GetMapping("/history-write-review-ledger")
    public ApiResponse<WorkbenchHistoryWriteReviewLedgerResponse> historyWriteReviewLedger(
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String comparisonStatus,
            @RequestParam(defaultValue = "") String reviewStatus,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String mismatchField,
            @RequestParam(defaultValue = "") String maintenanceTarget,
            @RequestParam(defaultValue = "") String retestStatus,
            @RequestParam(defaultValue = "") String priority,
            @RequestParam(defaultValue = "") String actionCode,
            @RequestParam(defaultValue = "") String pendingQueue,
            @RequestParam(defaultValue = "500") int limit
    ) {
        int safeLimit = Math.min(Math.max(1, limit), 5000);
        return ApiResponse.ok(workbenchService.historyWriteReviewLedger(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, pendingQueue, safeLimit));
    }

    @GetMapping("/salary-cases/{caseNo}/history-write-plan")
    public ApiResponse<WorkbenchHistoryWritePlanResponse> salaryCaseHistoryWritePlan(@PathVariable String caseNo) {
        return ApiResponse.ok(workbenchService.historyWritePlan(caseNo));
    }

    @GetMapping("/salary-cases/{caseNo}/history-write-comparison")
    public ApiResponse<WorkbenchHistoryWriteComparisonResponse> salaryCaseHistoryWriteComparison(@PathVariable String caseNo) {
        return ApiResponse.ok(workbenchService.historyWriteComparison(caseNo));
    }

    @PostMapping("/salary-cases/{caseNo}/history-write-comparison-retest")
    public ApiResponse<WorkbenchHistoryWriteComparisonResponse> salaryCaseHistoryWriteComparisonRetest(@PathVariable String caseNo) {
        return ApiResponse.ok(workbenchService.retestHistoryWriteComparison(caseNo));
    }

    @PostMapping("/salary-cases/{caseNo}/history-write-comparison-retest-approve")
    public ApiResponse<WorkbenchHistoryWriteComparisonResponse> salaryCaseHistoryWriteComparisonRetestApprove(@PathVariable String caseNo) {
        return ApiResponse.ok(workbenchService.approveRetestPassedHistoryWriteComparison(caseNo));
    }

    @PostMapping("/salary-cases/{caseNo}/history-write-comparison-review")
    public ApiResponse<WorkbenchHistoryWriteComparisonResponse> salaryCaseHistoryWriteComparisonReview(
            @PathVariable String caseNo,
            @RequestBody(required = false) WorkbenchHistoryWriteReviewRequest request
    ) {
        return ApiResponse.ok(workbenchService.reviewHistoryWriteComparison(caseNo, request));
    }

    @PostMapping("/salary-cases/{caseNo}/history-write-blocked-review")
    public ApiResponse<WorkbenchHistoryWritePlanResponse> salaryCaseHistoryWriteBlockedReview(
            @PathVariable String caseNo,
            @RequestBody(required = false) WorkbenchHistoryWriteReviewRequest request
    ) {
        return ApiResponse.ok(workbenchService.reviewBlockedHistoryWritePlan(caseNo, request));
    }

    @GetMapping(value = "/salary-cases/{caseNo}/history-write-comparison.csv", produces = "text/csv")
    public ResponseEntity<byte[]> salaryCaseHistoryWriteComparisonCsv(@PathVariable String caseNo) {
        WorkbenchHistoryWriteComparisonResponse comparison = workbenchService.historyWriteComparison(caseNo);
        byte[] body = withUtf8Bom(toHistoryWriteComparisonCsv(comparison));
        String filename = "history-write-comparison-" + caseNo + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping("/salary-cases/{caseNo}/history-write-audits")
    public ApiResponse<List<SystemAuditLogResponse>> salaryCaseHistoryWriteAudits(@PathVariable String caseNo) {
        return ApiResponse.ok(workbenchService.historyWritePlanAudits(caseNo));
    }

    @GetMapping(value = "/salary-cases/{caseNo}/history-write-audits.csv", produces = "text/csv")
    public ResponseEntity<byte[]> salaryCaseHistoryWriteAuditsCsv(@PathVariable String caseNo) {
        List<SystemAuditLogResponse> audits = workbenchService.historyWritePlanAudits(caseNo);
        byte[] body = withUtf8Bom(toHistoryWriteAuditsCsv(audits));
        String filename = "history-write-audits-" + caseNo + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping(value = "/history-write-plans.csv", produces = "text/csv")
    public ResponseEntity<byte[]> historyWritePlansCsv(
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String comparisonStatus,
            @RequestParam(defaultValue = "") String reviewStatus,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String mismatchField,
            @RequestParam(defaultValue = "") String maintenanceTarget,
            @RequestParam(defaultValue = "") String retestStatus,
            @RequestParam(defaultValue = "") String priority,
            @RequestParam(defaultValue = "") String actionCode,
            @RequestParam(defaultValue = "") String pendingQueue,
            @RequestParam(defaultValue = "") String printQueue,
            @RequestParam(defaultValue = "") String statusQueue,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        int safeLimit = Math.min(Math.max(1, limit), 5000);
        List<WorkbenchHistoryWritePlanResponse> plans = workbenchService.exportHistoryWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, pendingQueue, printQueue, statusQueue, safeLimit);
        systemAuditService.record("workbench", "history-write-plans-csv", "HISTORY_WRITE_PLAN", stringValue(pendingQueue).isBlank() ? "ALL" : stringValue(pendingQueue),
                "status=" + stringValue(status)
                        + ", comparisonStatus=" + stringValue(comparisonStatus)
                        + ", reviewStatus=" + stringValue(reviewStatus)
                        + ", actionCode=" + stringValue(actionCode)
                        + ", pendingQueue=" + stringValue(pendingQueue)
                        + ", printQueue=" + stringValue(printQueue)
                        + ", statusQueue=" + stringValue(statusQueue)
                        + ", limit=" + safeLimit
                        + ", count=" + plans.size());
        byte[] body = withUtf8Bom(toHistoryWritePlansCsv(plans));
        String normalizedStatus = status == null || status.isBlank() ? "all" : status.trim().toLowerCase();
        String filename = "history-write-plans-" + normalizedStatus + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @PostMapping("/history-write-plans/report-batch-queue-audit")
    public ApiResponse<Map<String, Object>> reportBatchHistoryWriteQueueAudit(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> safeRequest = request == null ? Map.of() : request;
        String batchNo = stringValue(safeRequest.get("batchNo")).trim();
        String label = stringValue(safeRequest.get("label")).trim();
        Object caseNosObject = safeRequest.get("caseNos");
        List<?> rawCaseNos = caseNosObject instanceof List<?> list ? list : List.of();
        List<String> caseNos = rawCaseNos.stream()
                .map(this::stringValue)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .limit(500)
                .toList();
        if (batchNo.isBlank() || caseNos.isEmpty()) {
            throw new IllegalArgumentException("Report batch queue audit requires batchNo and caseNos.");
        }
        long auditId = systemAuditService.record("workbench", "history-write-report-batch-queue", "REPORT_PRINT_BATCH", batchNo,
                "batchNo=" + batchNo
                        + ", label=" + (label.isBlank() ? "report-batch-history-write-queue" : label)
                        + ", rows=" + caseNos.size()
                        + ", caseNos=" + String.join("|", caseNos.stream().limit(10).toList()));
        return ApiResponse.ok(Map.of(
                "auditNo", auditId > 0 ? "SYS-" + auditId : "",
                "batchNo", batchNo,
                "rows", caseNos.size()
        ));
    }

    @PostMapping("/salary-cases/{caseNo}/history-write-execute")
    public ApiResponse<WorkbenchHistoryWriteExecuteResponse> salaryCaseHistoryWriteExecute(@PathVariable String caseNo) {
        return ApiResponse.ok(workbenchService.executeHistoryWrite(caseNo));
    }

    @PostMapping("/salary-cases/{caseNo}/history-write-rollback")
    public ApiResponse<WorkbenchHistoryWriteExecuteResponse> salaryCaseHistoryWriteRollback(@PathVariable String caseNo) {
        return ApiResponse.ok(workbenchService.rollbackHistoryWrite(caseNo));
    }

    @PostMapping("/salary-cases/{caseNo}/history-write-rollback-preview")
    public ApiResponse<WorkbenchHistoryWriteRollbackPreviewResponse> salaryCaseHistoryWriteRollbackPreview(@PathVariable String caseNo) {
        return ApiResponse.ok(workbenchService.rollbackHistoryWritePreview(caseNo));
    }

    @GetMapping(value = "/salary-cases/{caseNo}/history-write-rollback-preview.csv", produces = "text/csv")
    public ResponseEntity<byte[]> salaryCaseHistoryWriteRollbackPreviewCsv(@PathVariable String caseNo) {
        WorkbenchHistoryWriteRollbackPreviewResponse preview = workbenchService.rollbackHistoryWritePreview(caseNo);
        systemAuditService.record("workbench", "history-write-rollback-preview-csv", "SALARY_CASE", preview.caseNo(),
                "historyId=" + stringValue(preview.historyId())
                        + ", rollbackable=" + Boolean.TRUE.equals(preview.rollbackable())
                        + ", issues=" + (preview.issues() == null ? 0 : preview.issues().size()));
        byte[] body = withUtf8Bom(toHistoryWriteRollbackPreviewCsv(preview));
        String filename = "history-write-rollback-preview-" + caseNo + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @PostMapping("/salary-cases/{caseNo}/cancel")
    public ApiResponse<WorkbenchCaseDetailResponse> cancelSalaryCase(
            @PathVariable String caseNo,
            @RequestBody(required = false) WorkbenchCaseCancelRequest request
    ) {
        return ApiResponse.ok(workbenchService.cancelSalaryCase(caseNo, request));
    }

    @PostMapping("/salary-cases/{caseNo}/review")
    public ApiResponse<WorkbenchCaseDetailResponse> reviewSalaryCase(
            @PathVariable String caseNo,
            @RequestBody(required = false) WorkbenchCaseReviewRequest request
    ) {
        return ApiResponse.ok(workbenchService.reviewSalaryCase(caseNo, request));
    }

    @GetMapping(value = "/items.csv", produces = "text/csv")
    public ResponseEntity<byte[]> itemsCsv(
            @RequestParam(defaultValue = "TODO") String status,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String changeType,
            @RequestParam(defaultValue = "") String source,
            @RequestParam(defaultValue = "") String caseStatus,
            @RequestParam(defaultValue = "") String trialStatus,
            @RequestParam(defaultValue = "") String reviewStatus,
            @RequestParam(defaultValue = "") String workflowStatus,
            @RequestParam(defaultValue = "") String closureStatus,
            @RequestParam(defaultValue = "") String nextAction,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        int safeLimit = Math.min(Math.max(1, limit), 5000);
        WorkbenchItemsPageResponse page = workbenchService.exportItems(status, safeLimit, keyword, changeType, source, caseStatus, trialStatus, reviewStatus, workflowStatus, closureStatus, nextAction);
        byte[] body = withUtf8Bom(toCsv(page));
        String normalizedStatus = "DONE".equalsIgnoreCase(status) ? "done" : "todo";
        String filename = "workbench-" + normalizedStatus + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    private String toCsv(WorkbenchItemsPageResponse page) {
        StringBuilder csv = new StringBuilder();
        csv.append("\u5de5\u4f5c\u9879ID,\u72b6\u6001,\u6765\u6e90,\u95ed\u73af\u72b6\u6001,\u95ed\u73af\u8bf4\u660e,\u4e0b\u4e00\u6b65,\u529e\u7406\u8fdb\u5ea6,\u8bd5\u7b97\u72b6\u6001,\u590d\u6838\u72b6\u6001,\u6838\u67e5\u8bf4\u660e,\u6838\u67e5\u4eba,\u6838\u67e5\u65f6\u95f4,\u590d\u6d4b\u72b6\u6001,\u590d\u6d4b\u6458\u8981,\u590d\u6d4b\u65f6\u95f4,\u4e1a\u52a1\u7c7b\u578b,\u4eba\u5458\u7f16\u7801,\u59d3\u540d,\u5355\u4f4d\u7f16\u7801,\u5e74\u5ea6,\u6708\u4efd,\u6807\u9898,\u6458\u8981").append('\n');
        for (WorkbenchItemResponse item : page.items()) {
            csv.append(csv(item.id())).append(',')
                    .append(csv(statusText(item.status()))).append(',')
                    .append(csv(sourceText(item.source()))).append(',')
                    .append(csv(closureStatusText(item.closureStatus()))).append(',')
                    .append(csv(item.closureMessage())).append(',')
                    .append(csv(item.nextActionLabel())).append(',')
                    .append(csv(workflowStatusText(item.workflowStatus()))).append(',')
                    .append(csv(trialStatusText(item.trialStatus()))).append(',')
                    .append(csv(reviewStatusText(item.reviewStatus()))).append(',')
                    .append(csv(item.reviewReason())).append(',')
                    .append(csv(item.reviewedBy())).append(',')
                    .append(csv(item.reviewedAt())).append(',')
                    .append(csv(retestStatusText(item.retestStatus()))).append(',')
                    .append(csv(item.retestSummary())).append(',')
                    .append(csv(item.retestedAt())).append(',')
                    .append(csv(item.businessType())).append(',')
                    .append(csv(item.personCode())).append(',')
                    .append(csv(item.personName())).append(',')
                    .append(csv(item.orgCode())).append(',')
                    .append(item.year() == null ? "" : item.year()).append(',')
                    .append(item.month() == null ? "" : item.month()).append(',')
                    .append(csv(item.title())).append(',')
                    .append(csv(item.summary())).append('\n');
        }
        return csv.toString();
    }

    private String retestStatusText(String status) {
        if ("RESOLVED".equalsIgnoreCase(status)) {
            return "\u590d\u6d4b\u5df2\u89e3\u51b3";
        }
        if ("STILL_DIFFERENT".equalsIgnoreCase(status)) {
            return "\u590d\u6d4b\u4ecd\u6709\u5dee\u5f02";
        }
        if ("ERROR".equalsIgnoreCase(status)) {
            return "\u590d\u6d4b\u5f02\u5e38";
        }
        if ("NOT_RETESTED".equalsIgnoreCase(status)) {
            return "\u672a\u590d\u6d4b";
        }
        return status == null ? "" : status;
    }

    private String sourceText(String source) {
        String safeSource = source == null ? "" : source.trim();
        return switch (safeSource) {
            case "SALARY_EVENT" -> "\u5de5\u8d44\u53d8\u52a8";
            case "SALARY_CASE" -> "\u5de5\u8d44\u7533\u529e";
            case "SALARY_CLOSURE" -> "\u5de5\u8d44\u95ed\u73af";
            case "DATA_GOVERNANCE" -> "\u6570\u636e\u6cbb\u7406";
            case "REPORT_SAMPLE_COMPARISON" -> "\u62a5\u8868\u6837\u672c\u5bf9\u7167";
            case "GENERATED_TIMELINE" -> "\u81ea\u52a8\u6f14\u7b97\u7f3a\u53e3";
            case "APPLICATION_TODO", "APPLICATION_DONE" -> "\u7533\u529e\u4e1a\u52a1";
            case "dryzwbh" -> "\u4efb\u804c\u4fe1\u606f";
            case "dndkh" -> "\u5e74\u5ea6\u8003\u6838";
            case "dxl" -> "\u5b66\u5386\u4fe1\u606f";
            case "hjxx" -> "\u5956\u60e9\u5904\u5206";
            default -> safeSource;
        };
    }

    private String toSalaryMigrationClosureChecklistCsv(List<HistoryClosureAcceptanceRow> rows) {
        long pending = rows.stream()
                .filter(row -> !"\u5df2\u95ed\u73af".equals(row.result()))
                .mapToLong(HistoryClosureAcceptanceRow::count)
                .sum();
        long closed = rows.stream()
                .filter(row -> "\u5df2\u95ed\u73af".equals(row.result()))
                .mapToLong(HistoryClosureAcceptanceRow::count)
                .sum();
        StringBuilder csv = new StringBuilder();
        csv.append("\u95ed\u73af\u73af\u8282,\u4e1a\u52a1\u76ee\u7684,\u4e3b\u8981\u5165\u53e3,\u6743\u9650\u8981\u6c42,\u5ba1\u8ba1\u52a8\u4f5c,\u9a8c\u6536\u8bc1\u636e,\u5f53\u524d\u53e3\u5f84").append('\n');
        csv.append(csv("\u5f85\u529e\u751f\u6210")).append(',')
                .append(csv("\u6839\u636e\u57fa\u7840\u4fe1\u606f\u81ea\u52a8\u751f\u6210\u53ef\u529e\u7406\u5de5\u8d44\u53d8\u52a8")).append(',')
                .append(csv("/api/workbench/summary, /api/workbench/items")).append(',')
                .append(csv("SALARY_TODO")).append(',')
                .append(csv("salary-todo-cache-refresh, salary-todo-cache-dirty")).append(',')
                .append(csv("\u5f85\u529e\u6307\u6807\u548c\u5de5\u4f5c\u53f0\u5217\u8868")).append(',')
                .append(csv("\u5f85\u529e/\u5df2\u529e\u4ece\u540c\u4e00\u95ed\u73af\u72b6\u6001\u6d41\u8f6c")).append('\n');
        csv.append(csv("\u8bd5\u7b97\u548c\u9884\u89c8")).append(',')
                .append(csv("\u6309\u5de5\u8d44\u653f\u7b56\u91cd\u7b97\u672c\u6b21\u53d8\u52a8\u5e76\u751f\u6210\u5199\u5165\u9884\u89c8")).append(',')
                .append(csv("/api/workbench/salary-cases/preview, /history-write-preview")).append(',')
                .append(csv("SALARY_TRIAL, SALARY_DONE")).append(',')
                .append(csv("salary-case-preview, history-write-preview")).append(',')
                .append(csv("\u529e\u7406\u8be6\u60c5\u3001\u5199\u5165\u9884\u68c0\u548c\u5b57\u6bb5\u6620\u5c04")).append(',')
                .append(csv("\u9884\u68c0\u53ef\u5199\u65f6\u624d\u8fdb\u5165\u5386\u53f2\u5199\u5165\u961f\u5217")).append('\n');
        csv.append(csv("\u62a5\u8868\u6253\u5370\u5f52\u6863")).append(',')
                .append(csv("先形成审批表/清册归档，作为写入前置证据")).append(',')
                .append(csv("/api/reports/print-batches, /api/reports/migration-delivery-package.zip")).append(',')
                .append(csv("SALARY_EXPORT, SALARY_ACCEPTANCE")).append(',')
                .append(csv("salary-case-approvals-print, report-print-batch-acceptance-package, report-migration-delivery-package")).append(',')
                .append(csv("\u6279\u6b21\u9a8c\u6536\u5305\u3001\u6253\u5370\u81ea\u68c0\u3001\u62a5\u8868\u8fc1\u79fb\u4ea4\u4ed8\u5305")).append(',')
                .append(csv("\u672a\u6253\u5370/\u672a\u5f52\u6863\u7684\u8ba1\u5212\u4e0d\u8fdb\u5165\u5199\u5165")).append('\n');
        csv.append(csv("\u5386\u53f2\u5199\u5165")).append(',')
                .append(csv("\u751f\u6210 hisbase \u5386\u53f2\u884c\u5e76\u7ef4\u62a4 sid \u94fe")).append(',')
                .append(csv("/api/workbench/history-write-plans/batch-preview, /batch-execute")).append(',')
                .append(csv("SALARY_HISTORY_WRITE")).append(',')
                .append(csv("history-write-batch-safety-preview, history-write-batch-safety-consume, history-write-batch-execute")).append(',')
                .append(csv("history-write-safety-policy.csv, history-write-batch-ledger-write.csv")).append(',')
                .append(csv("\u6279\u91cf\u5199\u5165\u5fc5\u987b\u5148\u9884\u68c0\u5e76\u6d88\u8d39 safetyToken")).append('\n');
        csv.append(csv("\u5dee\u5f02\u6838\u67e5")).append(',')
                .append(csv("\u5199\u5165\u540e\u5bf9\u7167\u5feb\u7167\u4e0e hisbase\uff0c\u4e0d\u4e00\u81f4\u8fdb\u5165\u6838\u67e5/\u590d\u6d4b")).append(',')
                .append(csv("/api/workbench/history-write-plans/batch-review, /batch-retest")).append(',')
                .append(csv("SALARY_DONE")).append(',')
                .append(csv("history-write-batch-review, history-write-batch-retest-approve, history-write-batch-special-review")).append(',')
                .append(csv("history-write-plans-review-difference.csv, history-write-batch-ledger-review.csv")).append(',')
                .append(csv("\u5dee\u5f02\u53ef\u590d\u6d4b\u901a\u8fc7\u3001\u767b\u8bb0\u6838\u67e5\u6216\u6807\u8bb0\u7279\u6b8a\u60c5\u51b5")).append('\n');
        csv.append(csv("\u56de\u6eda\u548c\u91cd\u5904\u7406")).append(',')
                .append(csv("\u5bf9\u5df2\u5199\u5165\u8bb0\u5f55\u6267\u884c\u64a4\u9500\u5e76\u6062\u590d sid \u94fe")).append(',')
                .append(csv("/api/workbench/history-write-plans/batch-rollback-preview, /batch-rollback")).append(',')
                .append(csv("SALARY_HISTORY_ROLLBACK")).append(',')
                .append(csv("history-write-batch-rollback-safety-preview, history-write-batch-rollback-safety-consume, history-write-batch-rollback")).append(',')
                .append(csv("history-write-plans-rolled-back.csv, history-write-batch-ledger-rolled-back.csv")).append(',')
                .append(csv("\u56de\u6eda\u540c\u6837\u9700\u5b89\u5168 token\uff0c\u56de\u6eda\u540e\u56de\u5230\u91cd\u65b0\u5904\u7406\u961f\u5217")).append('\n');
        csv.append(csv("\u6570\u636e\u6cbb\u7406")).append(',')
                .append(csv("\u5c06\u57fa\u7840\u4fe1\u606f\u7f3a\u5931/\u7f16\u7801\u5f02\u5e38/\u5bf9\u7167\u7f3a\u53e3\u4efb\u52a1\u5316")).append(',')
                .append(csv("/api/workbench/data-governance/tasks/refresh")).append(',')
                .append(csv("SALARY_DATA_GOVERNANCE")).append(',')
                .append(csv("data-governance-task-refresh, data-governance-task-review, data-governance-task-retest")).append(',')
                .append(csv("\u6570\u636e\u6cbb\u7406\u4efb\u52a1\u961f\u5217\u548c\u590d\u6d4b\u7ed3\u679c")).append(',')
                .append(csv("\u57fa\u7840\u6570\u636e\u95ee\u9898\u4e0d\u76f4\u63a5\u653e\u884c\u4e3a\u5199\u5165\u95ed\u73af")).append('\n');
        csv.append(csv("\u8fc1\u79fb\u4ea4\u4ed8\u53f0\u8d26")).append(',')
                .append(csv("\u6c47\u603b\u603b\u4ea4\u4ed8\u5bfc\u51fa\u3001\u5f02\u5e38\u6cbb\u7406\u3001\u590d\u6d4b\u548c\u786e\u8ba4\u5173\u95ed\u8bc1\u636e")).append(',')
                .append(csv("/api/workbench/salary-migration-delivery-ledger.csv")).append(',')
                .append(csv("SALARY_EXPORT, SALARY_DATA_GOVERNANCE")).append(',')
                .append(csv("salary-migration-delivery-package, salary-migration-delivery-ledger-csv, salary-migration-delivery-governance-task-retest")).append(',')
                .append(csv("salary-migration-delivery-ledger.csv")).append(',')
                .append(csv("\u53f0\u8d26\u5df2\u5185\u5d4c\u5230\u8fc1\u79fb\u603b\u4ea4\u4ed8\u5305\uff0c\u53ef\u8ffd\u6eaf\u5f02\u5e38\u5904\u7406\u95ed\u73af")).append('\n');
        csv.append(csv("\u6743\u9650\u548c\u5ba1\u8ba1")).append(',')
                .append(csv("\u6309\u83dc\u5355\u6743\u9650\u548c\u5355\u4f4d\u4eba\u5458\u6743\u9650\u9650\u5236\u529e\u7406\u3001\u5bfc\u51fa\u548c\u5199\u5165")).append(',')
                .append(csv("/api/system/audits, /api/system/menus")).append(',')
                .append(csv("SYSTEM_AUDIT, SALARY_EXPORT, SALARY_ACCEPTANCE")).append(',')
                .append(csv("system-audit-query, menu-role-update, user-org-codes-update")).append(',')
                .append(csv("\u5ba1\u8ba1\u4e2d\u5fc3\u3001\u5bfc\u51fa\u5ba1\u8ba1CSV\u3001\u4ea4\u4ed8\u5f52\u6863\u7d22\u5f15")).append(',')
                .append(csv("\u672a\u6388\u6743\u7684\u5bfc\u51fa/\u5199\u5165\u4e0d\u751f\u6210\u4ea4\u4ed8\u5ba1\u8ba1")).append('\n');
        csv.append(csv("\u95ed\u73af\u9a8c\u6536")).append(',')
                .append(csv("\u6c47\u603b\u5f85\u529e\u3001\u5df2\u529e\u3001\u963b\u65ad\u3001\u5199\u5165\u3001\u6838\u67e5\u3001\u56de\u6eda\u8bc1\u636e")).append(',')
                .append(csv("/api/workbench/history-write-delivery-overview.csv, /history-write-closure-acceptance-package.zip")).append(',')
                .append(csv("SALARY_EXPORT, SALARY_ACCEPTANCE")).append(',')
                .append(csv("history-write-delivery-overview-csv, history-write-closure-acceptance-package, salary-migration-closure-checklist-csv")).append(',')
                .append(csv("salary-migration-closure-checklist.csv, history-write-closure-acceptance-package.zip")).append(',')
                .append(csv("\u5f85\u5904\u7406\u5408\u8ba1=" + pending + "\uff1b\u5df2\u95ed\u73af\u5408\u8ba1=" + closed)).append('\n');
        return csv.toString();
    }

    private String toSalaryMigrationDeliveryReadme(String orgCode, int year, int month, String businessType, String keyword, int limit, long pending, long closed, String historyPackageError, String reportPackageError) {
        String historyFile = stringValue(historyPackageError).isBlank()
                ? "history-write-closure-acceptance-package.zip：历史写入闭环验收包"
                : "history-write-closure-acceptance-package-error.txt：历史写入闭环验收包生成异常说明";
        historyFile = "salary-migration-delivery-ledger.csv: total delivery audit, governance task, retest and close ledger\n                - salary-migration-delivery-self-check.csv: final delivery package PASS/WARN self-check\n                - " + historyFile;
        String reportFile = stringValue(reportPackageError).isBlank()
                ? "salary-report-migration-delivery-package.zip：报表打印迁移交付包"
                : "salary-report-migration-delivery-package-error.txt：报表打印迁移交付包生成异常说明";
        return """
                工资迁移总交付包

                生成时间：%s
                单位范围：%s
                年月范围：%s
                业务类型：%s
                关键字：%s
                样本上限：%d
                历史写入待处理合计：%d
                历史写入已闭环合计：%d

                包内文件：
                - salary-migration-delivery-index.csv：总交付索引
                - salary-migration-closure-checklist.csv：工资业务迁移核心闭环总清单
                - %s
                - %s

                验收口径：
                - 业务闭环以待办生成、试算预览、报表归档、历史写入、差异核查、回滚和审计为主线。
                - 报表打印迁移交付包保留原报表迁移验收、自检和审计证据。
                - 历史写入闭环验收包保留写入队列、批量安全机制、批次台账和回滚证据。
                """.formatted(
                java.time.LocalDateTime.now().withNano(0),
                stringValue(orgCode).isBlank() ? "ALL" : orgCode,
                year > 0 && month > 0 ? year + "-" + month : "ALL",
                stringValue(businessType).isBlank() ? "ALL" : stringValue(businessType),
                stringValue(keyword).isBlank() ? "ALL" : stringValue(keyword),
                limit,
                pending,
                closed,
                historyFile,
                reportFile
        );
    }

    private String toSalaryMigrationDeliveryIndexCsv(String orgCode, int year, int month, String businessType, String keyword, int limit, String historyPackageError, String reportPackageError) {
        StringBuilder csv = new StringBuilder();
        csv.append("\u6587\u4ef6,\u7c7b\u578b,\u7528\u9014,\u6765\u6e90\u63a5\u53e3,\u6743\u9650,\u5ba1\u8ba1\u52a8\u4f5c,\u8fc7\u6ee4\u53e3\u5f84").append('\n');
        String filter = "org=" + (stringValue(orgCode).isBlank() ? "ALL" : orgCode)
                + ", year=" + year
                + ", month=" + month
                + ", businessType=" + (stringValue(businessType).isBlank() ? "ALL" : stringValue(businessType))
                + ", keyword=" + (stringValue(keyword).isBlank() ? "ALL" : stringValue(keyword))
                + ", limit=" + limit;
        csv.append(csv("README.txt")).append(',')
                .append(csv("\u8bf4\u660e")).append(',')
                .append(csv("\u603b\u4ea4\u4ed8\u5305\u8bf4\u660e\u548c\u9a8c\u6536\u53e3\u5f84")).append(',')
                .append(csv("/api/workbench/salary-migration-delivery-package.zip")).append(',')
                .append(csv("SALARY_EXPORT, SALARY_ACCEPTANCE, REPORT")).append(',')
                .append(csv("salary-migration-delivery-package")).append(',')
                .append(csv(filter)).append('\n');
        csv.append(csv("salary-migration-closure-checklist.csv")).append(',')
                .append(csv("\u603b\u6e05\u5355")).append(',')
                .append(csv("\u4e32\u8054\u5de5\u8d44\u4e1a\u52a1\u8fc1\u79fb\u6838\u5fc3\u95ed\u73af")).append(',')
                .append(csv("/api/workbench/salary-migration-closure-checklist.csv")).append(',')
                .append(csv("SALARY_EXPORT")).append(',')
                .append(csv("salary-migration-closure-checklist-csv")).append(',')
                .append(csv(filter)).append('\n');
        csv.append(csv("salary-migration-delivery-ledger.csv")).append(',')
                .append(csv("\u95ed\u73af\u53f0\u8d26")).append(',')
                .append(csv("\u6c47\u603b\u603b\u4ea4\u4ed8\u5ba1\u8ba1\u3001\u5f02\u5e38\u6cbb\u7406\u4efb\u52a1\u3001\u590d\u6d4b\u548c\u786e\u8ba4\u5173\u95ed\u72b6\u6001")).append(',')
                .append(csv("/api/workbench/salary-migration-delivery-ledger.csv")).append(',')
                .append(csv("SALARY_EXPORT, SALARY_GOVERNANCE")).append(',')
                .append(csv("salary-migration-delivery-ledger-csv")).append(',')
                .append(csv(filter)).append('\n');
        csv.append(csv("salary-migration-delivery-self-check.csv")).append(',')
                .append(csv("\u6700\u7ec8\u81ea\u68c0")).append(',')
                .append(csv("\u68c0\u67e5\u603b\u4ea4\u4ed8\u5305\u6587\u4ef6\u3001\u5ba1\u8ba1\u3001\u53f0\u8d26\u548c\u6cbb\u7406\u95ed\u73af\u662f\u5426\u9f50\u5168")).append(',')
                .append(csv("/api/workbench/salary-migration-delivery-self-check.csv")).append(',')
                .append(csv("SALARY_EXPORT, SALARY_GOVERNANCE")).append(',')
                .append(csv("salary-migration-delivery-self-check-csv")).append(',')
                .append(csv(filter)).append('\n');
        csv.append(csv(stringValue(historyPackageError).isBlank() ? "history-write-closure-acceptance-package.zip" : "history-write-closure-acceptance-package-error.txt")).append(',')
                .append(csv(stringValue(historyPackageError).isBlank() ? "\u5d4c\u5957ZIP" : "\u5f02\u5e38\u8bf4\u660e")).append(',')
                .append(csv(stringValue(historyPackageError).isBlank() ? "\u5386\u53f2\u5199\u5165\u95ed\u73af\u9a8c\u6536\u8bc1\u636e" : "\u5386\u53f2\u5199\u5165\u95ed\u73af\u9a8c\u6536\u5305\u751f\u6210\u5f02\u5e38\u8bc1\u636e")).append(',')
                .append(csv("/api/workbench/history-write-closure-acceptance-package.zip")).append(',')
                .append(csv("SALARY_EXPORT, SALARY_ACCEPTANCE")).append(',')
                .append(csv(stringValue(historyPackageError).isBlank() ? "history-write-closure-acceptance-package" : "history-write-closure-acceptance-package-error")).append(',')
                .append(csv(filter)).append('\n');
        csv.append(csv(stringValue(reportPackageError).isBlank() ? "salary-report-migration-delivery-package.zip" : "salary-report-migration-delivery-package-error.txt")).append(',')
                .append(csv(stringValue(reportPackageError).isBlank() ? "\u5d4c\u5957ZIP" : "\u5f02\u5e38\u8bf4\u660e")).append(',')
                .append(csv(stringValue(reportPackageError).isBlank() ? "\u62a5\u8868\u6253\u5370\u8fc1\u79fb\u4ea4\u4ed8\u8bc1\u636e" : "\u62a5\u8868\u6253\u5370\u8fc1\u79fb\u4ea4\u4ed8\u5305\u751f\u6210\u5f02\u5e38\u8bc1\u636e")).append(',')
                .append(csv("/api/reports/migration-delivery-package.zip")).append(',')
                .append(csv("REPORT")).append(',')
                .append(csv(stringValue(reportPackageError).isBlank() ? "report-migration-delivery-package" : "report-migration-delivery-package-error")).append(',')
                .append(csv(filter)).append('\n');
        return csv.toString();
    }

    private String toSalaryMigrationDeliveryLedgerCsv(List<Map<String, Object>> rows) {
        StringBuilder csv = new StringBuilder();
        csv.append(csv("审计号")).append(',')
                .append(csv("导出时间")).append(',')
                .append(csv("操作人")).append(',')
                .append(csv("单位")).append(',')
                .append(csv("年度")).append(',')
                .append(csv("月份")).append(',')
                .append(csv("关键字")).append(',')
                .append(csv("历史验收包状态")).append(',')
                .append(csv("报表交付包状态")).append(',')
                .append(csv("待处理")).append(',')
                .append(csv("已闭环")).append(',')
                .append(csv("治理任务号")).append(',')
                .append(csv("任务状态")).append(',')
                .append(csv("复测状态")).append(',')
                .append(csv("建议关闭")).append(',')
                .append(csv("确认状态")).append(',')
                .append(csv("确认说明")).append(',')
                .append(csv("确认人")).append(',')
                .append(csv("确认时间")).append(',')
                .append(csv("复测说明")).append(',')
                .append(csv("复测时间")).append(',')
                .append(csv("审计摘要")).append('\n');
        for (Map<String, Object> row : rows) {
            csv.append(csvValue(row.get("auditId"))).append(',')
                    .append(csvValue(row.get("createdAt"))).append(',')
                    .append(csvValue(row.get("operator"))).append(',')
                    .append(csvValue(row.get("orgCode"))).append(',')
                    .append(csvValue(row.get("year"))).append(',')
                    .append(csvValue(row.get("month"))).append(',')
                    .append(csvValue(row.get("keyword"))).append(',')
                    .append(csvValue(row.get("historyStatus"))).append(',')
                    .append(csvValue(row.get("reportStatus"))).append(',')
                    .append(csvValue(row.get("pending"))).append(',')
                    .append(csvValue(row.get("closed"))).append(',')
                    .append(csvValue(row.get("governanceWorkItemId"))).append(',')
                    .append(csvValue(row.get("taskStatus"))).append(',')
                    .append(csvValue(row.get("retestStatus"))).append(',')
                    .append(csvValue(row.get("closeSuggested"))).append(',')
                    .append(csvValue(row.get("reviewStatus"))).append(',')
                    .append(csvValue(row.get("reviewReason"))).append(',')
                    .append(csvValue(row.get("reviewedBy"))).append(',')
                    .append(csvValue(row.get("reviewedAt"))).append(',')
                    .append(csvValue(row.get("retestSummary"))).append(',')
                    .append(csvValue(row.get("retestedAt"))).append(',')
                    .append(csvValue(row.get("summary"))).append('\n');
        }
        return csv.toString();
    }

    private String toSalaryMigrationDeliverySelfCheckCsv(List<Map<String, Object>> rows) {
        StringBuilder csv = new StringBuilder();
        csv.append(csv("检查项编码")).append(',')
                .append(csv("检查项")).append(',')
                .append(csv("状态")).append(',')
                .append(csv("数量")).append(',')
                .append(csv("说明")).append(',')
                .append(csv("证据")).append('\n');
        for (Map<String, Object> row : rows) {
            csv.append(csvValue(row.get("code"))).append(',')
                    .append(csvValue(row.get("title"))).append(',')
                    .append(csvValue(row.get("status"))).append(',')
                    .append(csvValue(row.get("count"))).append(',')
                    .append(csvValue(row.get("message"))).append(',')
                    .append(csvValue(row.get("evidence"))).append('\n');
        }
        return csv.toString();
    }

    private String toSalaryMigrationDeliveryLedgerError(String error, String orgCode) {
        return """
                Migration delivery ledger export failed.
                The delivery package was generated, but the embedded closure ledger could not be generated.
                org=%s
                error=%s
                """.formatted(
                stringValue(orgCode).isBlank() ? "ALL" : orgCode,
                stringValue(error)
        );
    }

    private String toSalaryMigrationDeliverySelfCheckError(String error, String orgCode) {
        return """
                Migration delivery self-check export failed.
                The delivery package was generated, but the embedded final self-check could not be generated.
                org=%s
                error=%s
                """.formatted(
                stringValue(orgCode).isBlank() ? "ALL" : orgCode,
                stringValue(error)
        );
    }

    private String toSalaryMigrationDeliveryHistoryError(String error, String orgCode, int year, int month, String businessType, String keyword, int limit) {
        return """
                历史写入闭环验收包生成异常

                生成工资迁移总交付包时，历史写入闭环验收包未能成功生成。总交付包已保留闭环总清单、报表迁移交付结果和本异常说明，便于后续数据治理或单独复测历史写入验收包。

                错误信息：%s
                单位范围：%s
                年月范围：%s
                业务类型：%s
                关键字：%s
                样本上限：%d
                """.formatted(
                stringValue(error),
                stringValue(orgCode).isBlank() ? "ALL" : orgCode,
                year > 0 && month > 0 ? year + "-" + month : "ALL",
                stringValue(businessType).isBlank() ? "ALL" : stringValue(businessType),
                stringValue(keyword).isBlank() ? "ALL" : stringValue(keyword),
                limit
        );
    }

    private String toSalaryMigrationDeliveryReportError(String error, String orgCode, int year, int month, String businessType, String keyword, int limit) {
        return """
                报表打印迁移交付包生成异常

                生成工资迁移总交付包时，报表打印迁移交付包未能成功生成。总交付包已保留历史写入闭环包、总清单和本异常说明，便于后续数据治理或单独复测报表交付包。

                错误信息：%s
                单位范围：%s
                年月范围：%s
                业务类型：%s
                关键字：%s
                样本上限：%d
                """.formatted(
                stringValue(error),
                stringValue(orgCode).isBlank() ? "ALL" : orgCode,
                year > 0 && month > 0 ? year + "-" + month : "ALL",
                stringValue(businessType).isBlank() ? "ALL" : stringValue(businessType),
                stringValue(keyword).isBlank() ? "ALL" : stringValue(keyword),
                limit
        );
    }

    private String toHistoryWriteSafetyPolicyCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("policy,action,control,evidence").append('\n');
        csv.append(csv("batch-write-preview")).append(',')
                .append(csv("history-write-batch-safety-preview")).append(',')
                .append(csv("safetyToken-issued")).append(',')
                .append(csv("Batch write requires a safety preview token before execution.")).append('\n');
        csv.append(csv("batch-write-consume")).append(',')
                .append(csv("history-write-batch-safety-consume")).append(',')
                .append(csv("safetyToken-consumed")).append(',')
                .append(csv("Execution requires the matching safetyToken and rejects missing, invalid, or reused tokens.")).append('\n');
        csv.append(csv("batch-rollback-preview")).append(',')
                .append(csv("history-write-batch-rollback-safety-preview")).append(',')
                .append(csv("rollbackToken-issued")).append(',')
                .append(csv("Batch rollback requires a rollback safety preview token before execution.")).append('\n');
        csv.append(csv("batch-rollback-consume")).append(',')
                .append(csv("history-write-batch-rollback-safety-consume")).append(',')
                .append(csv("rollbackToken-consumed")).append(',')
                .append(csv("Rollback requires the matching safetyToken and rejects missing, invalid, or reused tokens.")).append('\n');
        csv.append(csv("blocked-skip")).append(',')
                .append(csv("history-write-batch-execute")).append(',')
                .append(csv("blocked-skipped")).append(',')
                .append(csv("Blocked plans are counted and skipped by batch execution.")).append('\n');
        csv.append(csv("audit-ledger")).append(',')
                .append(csv("history-write-batch-ledger-csv")).append(',')
                .append(csv("batch-ledger")).append(',')
                .append(csv("Write, review, and rollback batch ledgers are included in the acceptance package.")).append('\n');
        return csv.toString();
    }

    private String closureStatusText(String status) {
        String safeStatus = status == null ? "" : status.trim();
        return switch (safeStatus) {
            case "CLOSED" -> "\u5df2\u95ed\u73af";
            case "PENDING" -> "\u5f85\u95ed\u73af";
            case "BLOCKED" -> "\u95ed\u73af\u963b\u65ad";
            case "CANCELLED" -> "\u5df2\u64a4\u56de";
            default -> safeStatus;
        };
    }

    private String toHistoryWritePlansCsv(List<WorkbenchHistoryWritePlanResponse> plans) {
        StringBuilder csv = new StringBuilder();
        csv.append("\u8ba1\u5212\u53f7,\u529e\u7406\u7f16\u53f7,\u5f85\u529e\u6807\u8bc6,\u4eba\u5458\u7f16\u7801,\u5355\u4f4d\u7f16\u7801,\u5e74\u5ea6,\u6708\u4efd,\u53d8\u52a8\u7c7b\u522b,\u9884\u89c8\u72b6\u6001,\u662f\u5426\u53ef\u5199,\u8ba1\u5212\u72b6\u6001,\u6267\u884c\u7ed3\u679c,\u5bf9\u7167\u72b6\u6001,\u5dee\u5f02\u6570\u91cf,\u5904\u7406\u4f18\u5148\u7ea7,\u4e0b\u4e00\u6b65\u52a8\u4f5c\u7f16\u7801,\u4e0b\u4e00\u6b65\u52a8\u4f5c,\u5efa\u8bae\u68c0\u67e5\u65b9\u5411,\u5efa\u8bae\u5b57\u6bb5,\u5efa\u8bae\u539f\u56e0,\u6838\u67e5\u72b6\u6001,\u590d\u6d4b\u72b6\u6001,\u6838\u67e5\u6765\u6e90,\u6838\u67e5\u5206\u7c7b,\u6838\u67e5\u8bf4\u660e,\u6838\u67e5\u4eba,\u6838\u67e5\u65f6\u95f4,\u5199\u5165\u5386\u53f2ID,\u524d\u4e00\u6761ID,\u540e\u4e00\u6761ID,\u751f\u6210\u4eba,\u751f\u6210\u65f6\u95f4,\u6267\u884c\u4eba,\u6267\u884c\u65f6\u95f4,\u64a4\u9500\u4eba,\u64a4\u9500\u65f6\u95f4,\u6267\u884c\u8bf4\u660e,\u64a4\u9500\u8bf4\u660e").append('\n');
        for (WorkbenchHistoryWritePlanResponse plan : plans) {
            HistoryWriteWorkflow workflow = historyWriteWorkflow(plan);
            HistoryWriteMaintenanceSuggestionCsv suggestion = maintenanceSuggestionCsv(plan.maintenanceSuggestionJson());
            csv.append(csv(plan.planNo())).append(',')
                    .append(csv(plan.caseNo())).append(',')
                    .append(csv(plan.workItemId())).append(',')
                    .append(csv(plan.personCode())).append(',')
                    .append(csv(plan.orgCode())).append(',')
                    .append(plan.year() == null ? "" : plan.year()).append(',')
                    .append(plan.month() == null ? "" : plan.month()).append(',')
                    .append(csv(plan.businessType())).append(',')
                    .append(csv(plan.previewStatus())).append(',')
                    .append(Boolean.TRUE.equals(plan.writable()) ? "\u662f" : "\u5426").append(',')
                    .append(csv(plan.planStatus())).append(',')
                    .append(csv(plan.executionResult())).append(',')
                    .append(csv(comparisonStatusText(plan.comparisonStatus()))).append(',')
                    .append(plan.comparisonMismatchCount() == null ? 0 : plan.comparisonMismatchCount()).append(',')
                    .append(csv(workflow.priorityText())).append(',')
                    .append(csv(plan.nextActionCode())).append(',')
                    .append(csv(workflow.nextAction())).append(',')
                    .append(csv(suggestion.directions())).append(',')
                    .append(csv(suggestion.fields())).append(',')
                    .append(csv(suggestion.reasons())).append(',')
                    .append(csv(comparisonReviewStatusText(plan.comparisonReviewStatus()))).append(',')
                    .append(csv(comparisonRetestStatusText(plan.comparisonRetestStatus()))).append(',')
                    .append(csv(comparisonReviewSourceText(plan.comparisonReviewReason()))).append(',')
                    .append(csv(comparisonReviewCategoryText(plan.comparisonReviewCategory()))).append(',')
                    .append(csv(plan.comparisonReviewReason())).append(',')
                    .append(csv(plan.comparisonReviewedBy())).append(',')
                    .append(csv(plan.comparisonReviewedAt())).append(',')
                    .append(csv(plan.insertedHistoryId())).append(',')
                    .append(csv(plan.previousHistoryId())).append(',')
                    .append(csv(plan.nextHistoryId())).append(',')
                    .append(csv(plan.preparedBy())).append(',')
                    .append(csv(plan.preparedAt())).append(',')
                    .append(csv(plan.executedBy())).append(',')
                    .append(csv(plan.executedAt())).append(',')
                    .append(csv(plan.rolledBackBy())).append(',')
                    .append(csv(plan.rolledBackAt())).append(',')
                    .append(csv(plan.executionMessage())).append(',')
                    .append(csv(plan.rollbackMessage())).append('\n');
        }
        return csv.toString();
    }

    private String toHistoryWriteAuditsCsv(List<SystemAuditLogResponse> audits) {
        StringBuilder csv = new StringBuilder();
        csv.append("\u5ba1\u8ba1ID,\u6a21\u5757,\u52a8\u4f5c,\u5bf9\u8c61\u7c7b\u578b,\u5bf9\u8c61\u7f16\u7801,\u6458\u8981,\u64cd\u4f5c\u4eba,\u64cd\u4f5c\u65f6\u95f4").append('\n');
        for (SystemAuditLogResponse audit : audits) {
            csv.append(csv(audit.id())).append(',')
                    .append(csv(audit.module())).append(',')
                    .append(csv(audit.action())).append(',')
                    .append(csv(audit.targetType())).append(',')
                    .append(csv(audit.targetCode())).append(',')
                    .append(csv(audit.summary())).append(',')
                    .append(csv(audit.operator())).append(',')
                    .append(csv(audit.createdAt())).append('\n');
        }
        return csv.toString();
    }

    private String toHistoryWriteBatchLedgerCsv(List<WorkbenchHistoryWriteBatchLedgerResponse> batches) {
        StringBuilder csv = new StringBuilder();
        csv.append("\u6279\u6b21\u53f7,\u52a8\u4f5c,\u95ed\u73af\u72b6\u6001,\u603b\u6570,\u6210\u529f/\u6807\u8bb0,\u5931\u8d25,\u8df3\u8fc7,\u4e00\u81f4,\u4e0d\u4e00\u81f4,\u64cd\u4f5c\u4eba,\u64cd\u4f5c\u65f6\u95f4,\u6458\u8981").append('\n');
        for (WorkbenchHistoryWriteBatchLedgerResponse batch : batches) {
            csv.append(csv(batch.batchNo())).append(',')
                    .append(csv(batch.action())).append(',')
                    .append(csv(historyWriteBatchClosureText(batch))).append(',')
                    .append(batch.total()).append(',')
                    .append(batch.success()).append(',')
                    .append(batch.failed()).append(',')
                    .append(batch.skipped()).append(',')
                    .append(batch.matched()).append(',')
                    .append(batch.mismatched()).append(',')
                    .append(csv(batch.operator())).append(',')
                    .append(csv(batch.createdAt())).append(',')
                    .append(csv(batch.summary())).append('\n');
        }
        return csv.toString();
    }

    private List<HistoryClosureAcceptanceRow> historyClosureAcceptanceRows(WorkbenchSummaryResponse summary) {
        Map<String, Long> counts = new java.util.HashMap<>();
        for (var metric : summary.metrics()) {
            counts.put(metric.code(), metric.count());
        }
        return List.of(
                new HistoryClosureAcceptanceRow("SALARY_NEXT_PRINT_OR_PLAN", "\u5f85\u6253\u5370/\u9884\u68c0", counts.getOrDefault("SALARY_NEXT_PRINT_OR_PLAN", 0L), "\u5f85\u5904\u7406", "\u9700\u6253\u5370\u5ba1\u6279\u8868\u6216\u751f\u6210\u5199\u5165\u9884\u68c0"),
                new HistoryClosureAcceptanceRow("SALARY_NEXT_EXECUTE_WRITE", "\u5f85\u5199\u5165\u5386\u53f2", Math.max(counts.getOrDefault("SALARY_NEXT_EXECUTE_WRITE", 0L), counts.getOrDefault("HISTORY_PLAN_READY", 0L)), "\u5f85\u5904\u7406", "\u9700\u6267\u884c\u5386\u53f2\u5199\u5165\u5e76\u5b8c\u6210\u5bf9\u7167"),
                new HistoryClosureAcceptanceRow("SALARY_NEXT_REVIEW_DIFFERENCE", "\u5199\u5165\u540e\u5f85\u6838\u67e5", Math.max(counts.getOrDefault("SALARY_NEXT_REVIEW_DIFFERENCE", 0L), counts.getOrDefault("HISTORY_PLAN_REVIEW_PENDING", 0L)), "\u5f85\u5904\u7406", "\u9700\u590d\u6d4b\u6216\u767b\u8bb0\u6838\u67e5\u7ed3\u8bba"),
                new HistoryClosureAcceptanceRow("HISTORY_PLAN_ROLLED_BACK", "\u5df2\u56de\u6eda\u5f85\u91cd\u65b0\u5904\u7406", counts.getOrDefault("HISTORY_PLAN_ROLLED_BACK", 0L), "\u5f85\u5904\u7406", "\u9700\u91cd\u65b0\u9884\u68c0\u6216\u7559\u5b58\u64a4\u9500\u4f9d\u636e"),
                new HistoryClosureAcceptanceRow("SALARY_CLOSURE_CLOSED", "\u5df2\u95ed\u73af", counts.getOrDefault("SALARY_CLOSURE_CLOSED", 0L), "\u5df2\u95ed\u73af", "\u53ef\u4f5c\u4e3a\u4ea4\u4ed8\u5df2\u529e\u53f0\u8d26")
        );
    }

    private String toHistoryClosureAcceptanceCsv(List<HistoryClosureAcceptanceRow> rows) {
        long pending = rows.stream()
                .filter(row -> !"\u5df2\u95ed\u73af".equals(row.result()))
                .mapToLong(HistoryClosureAcceptanceRow::count)
                .sum();
        long closed = rows.stream()
                .filter(row -> "\u5df2\u95ed\u73af".equals(row.result()))
                .mapToLong(HistoryClosureAcceptanceRow::count)
                .sum();
        StringBuilder csv = new StringBuilder();
        csv.append("\u9a8c\u6536\u65f6\u95f4,").append(csv(java.time.LocalDateTime.now().toString())).append('\n');
        csv.append("\u5f85\u5904\u7406\u5408\u8ba1,").append(pending).append('\n');
        csv.append("\u5df2\u95ed\u73af\u5408\u8ba1,").append(closed).append('\n');
        csv.append('\n');
        csv.append("\u961f\u5217\u7f16\u7801,\u961f\u5217\u540d\u79f0,\u6570\u91cf,\u9a8c\u6536\u7ed3\u8bba,\u5904\u7406\u8bf4\u660e").append('\n');
        for (HistoryClosureAcceptanceRow row : rows) {
            csv.append(csv(row.code())).append(',')
                    .append(csv(row.title())).append(',')
                    .append(row.count()).append(',')
                    .append(csv(row.result())).append(',')
                    .append(csv(row.hint())).append('\n');
        }
        return csv.toString();
    }

    private List<HistoryDeliveryEvidenceRow> historyDeliveryEvidenceRows(List<HistoryClosureAcceptanceRow> rows) {
        Map<String, Long> counts = new java.util.HashMap<>();
        for (HistoryClosureAcceptanceRow row : rows) {
            counts.put(row.code(), row.count());
        }
        return List.of(
                new HistoryDeliveryEvidenceRow("README.txt", "\u9a8c\u6536\u8bf4\u660e", "-", "\u751f\u6210\u65f6\u95f4\u3001\u5f85\u5904\u7406/\u5df2\u95ed\u73af\u5408\u8ba1\u3001\u5305\u5185\u6587\u4ef6\u548c\u9a8c\u6536\u53e3\u5f84"),
                new HistoryDeliveryEvidenceRow("history-write-closure-acceptance-summary.csv", "\u95ed\u73af\u9a8c\u6536\u6c47\u603b", String.valueOf(rows.size()), "\u6309\u961f\u5217\u6c47\u603b\u5f85\u5904\u7406\u3001\u5df2\u95ed\u73af\u548c\u5904\u7406\u8bf4\u660e"),
                new HistoryDeliveryEvidenceRow("history-write-plans-unprinted-or-plan.csv", "\u5f85\u6253\u5370/\u9884\u68c0\u6e05\u5355", String.valueOf(counts.getOrDefault("SALARY_NEXT_PRINT_OR_PLAN", 0L)), "\u5199\u5165\u8ba1\u5212\u4e3a PREPARED\uff0c\u4f46\u5ba1\u6279\u8868\u672a\u5f52\u6863\u6216\u5c1a\u9700\u91cd\u65b0\u9884\u68c0"),
                new HistoryDeliveryEvidenceRow("history-write-plans-ready-to-write.csv", "\u5f85\u5199\u5165\u5386\u53f2\u6e05\u5355", String.valueOf(counts.getOrDefault("SALARY_NEXT_EXECUTE_WRITE", 0L)), "\u5df2\u6253\u5370\u5f52\u6863\u4e14\u5199\u5165\u9884\u68c0\u53ef\u6267\u884c\uff0c\u5f85\u8fdb\u884c\u5b89\u5168\u786e\u8ba4\u548c\u5199\u5165"),
                new HistoryDeliveryEvidenceRow("history-write-plans-review-difference.csv", "\u5199\u5165\u540e\u5f85\u6838\u67e5\u6e05\u5355", String.valueOf(counts.getOrDefault("SALARY_NEXT_REVIEW_DIFFERENCE", 0L)), "\u5df2\u5199\u5165\u5386\u53f2\uff0c\u4f46\u5feb\u7167\u4e0e hisbase \u5bf9\u7167\u4ecd\u9700\u590d\u6d4b\u6216\u6838\u67e5"),
                new HistoryDeliveryEvidenceRow("history-write-plans-rolled-back.csv", "\u5df2\u56de\u6eda\u5f85\u91cd\u65b0\u5904\u7406\u6e05\u5355", String.valueOf(counts.getOrDefault("HISTORY_PLAN_ROLLED_BACK", 0L)), "\u5df2\u64a4\u9500\u5199\u5165\u5e76\u6062\u590d sid \u94fe\uff0c\u9700\u91cd\u65b0\u9884\u68c0\u6216\u7559\u5b58\u64a4\u9500\u4f9d\u636e"),
                new HistoryDeliveryEvidenceRow("history-write-batch-ledger-write.csv", "\u5199\u5165\u6279\u6b21\u53f0\u8d26", "\u6279\u6b21", "\u6279\u91cf\u5199\u5165\u548c\u9009\u4e2d\u5199\u5165\u7684\u6267\u884c\u6279\u6b21\u4f9d\u636e"),
                new HistoryDeliveryEvidenceRow("history-write-batch-ledger-review.csv", "\u6838\u67e5/\u590d\u6d4b\u6279\u6b21\u53f0\u8d26", "\u6279\u6b21", "\u5dee\u5f02\u590d\u6d4b\u3001\u6838\u67e5\u767b\u8bb0\u548c\u7279\u6b8a\u60c5\u51b5\u5904\u7406\u4f9d\u636e"),
                new HistoryDeliveryEvidenceRow("history-write-batch-ledger-rolled-back.csv", "\u56de\u6eda\u6279\u6b21\u53f0\u8d26", "\u6279\u6b21", "\u64a4\u9500\u5199\u5165\u3001sid \u94fe\u6062\u590d\u548c\u56de\u6eda\u5904\u7406\u4f9d\u636e")
        );
    }

    private String toHistoryDeliveryOverviewCsv(List<HistoryClosureAcceptanceRow> rows, List<HistoryDeliveryEvidenceRow> evidenceRows) {
        long pending = rows.stream()
                .filter(row -> !"\u5df2\u95ed\u73af".equals(row.result()))
                .mapToLong(HistoryClosureAcceptanceRow::count)
                .sum();
        long closed = rows.stream()
                .filter(row -> "\u5df2\u95ed\u73af".equals(row.result()))
                .mapToLong(HistoryClosureAcceptanceRow::count)
                .sum();
        long activeQueues = rows.stream()
                .filter(row -> !"\u5df2\u95ed\u73af".equals(row.result()) && row.count() > 0)
                .count();
        String conclusion = pending > 0
                ? "\u5c1a\u6709 " + pending + " \u9879\u5f85\u5904\u7406\uff0c\u9700\u6309\u961f\u5217\u7ee7\u7eed\u63a8\u8fdb"
                : "\u5f53\u524d\u5386\u53f2\u5199\u5165\u961f\u5217\u65e0\u5f85\u5904\u7406\u9879\uff0c\u53ef\u8fdb\u884c\u9a8c\u6536\u5305\u4ea4\u4ed8";
        StringBuilder csv = new StringBuilder();
        csv.append("\u5bfc\u51fa\u65f6\u95f4,").append(csv(java.time.LocalDateTime.now().toString())).append('\n');
        csv.append("\u4ea4\u4ed8\u7ed3\u8bba,").append(csv(conclusion)).append('\n');
        csv.append("\u5f85\u5904\u7406\u5408\u8ba1,").append(pending).append('\n');
        csv.append("\u5df2\u95ed\u73af\u5408\u8ba1,").append(closed).append('\n');
        csv.append("\u9a8c\u6536\u5305\u6587\u4ef6\u6570,").append(evidenceRows.size()).append('\n');
        csv.append("\u6709\u6570\u636e\u961f\u5217\u6570,").append(activeQueues).append('\n');
        csv.append('\n');
        csv.append("\u4ea4\u4ed8\u961f\u5217").append('\n');
        csv.append("\u961f\u5217\u7f16\u7801,\u961f\u5217\u540d\u79f0,\u6570\u91cf,\u4ea4\u4ed8\u72b6\u6001,\u5904\u7406\u8bf4\u660e").append('\n');
        for (HistoryClosureAcceptanceRow row : rows) {
            csv.append(csv(row.code())).append(',')
                    .append(csv(row.title())).append(',')
                    .append(row.count()).append(',')
                    .append(csv("\u5df2\u95ed\u73af".equals(row.result()) ? "\u5df2\u95ed\u73af" : (row.count() > 0 ? "\u5f85\u5904\u7406" : "\u65e0\u5f85\u5904\u7406"))).append(',')
                    .append(csv(row.hint())).append('\n');
        }
        csv.append('\n');
        csv.append("\u4ea4\u4ed8\u8bc1\u636e").append('\n');
        csv.append("\u6587\u4ef6\u540d,\u8bc1\u636e\u540d\u79f0,\u6570\u91cf,\u53e3\u5f84\u8bf4\u660e").append('\n');
        for (HistoryDeliveryEvidenceRow row : evidenceRows) {
            csv.append(csv(row.file())).append(',')
                    .append(csv(row.title())).append(',')
                    .append(csv(row.count())).append(',')
                    .append(csv(row.scope())).append('\n');
        }
        return csv.toString();
    }

    @SuppressWarnings("unchecked")
    private String toHistoryDeliveryAcceptanceDetailCsv(Map<String, Object> detail) {
        StringBuilder csv = new StringBuilder();
        csv.append("\u9a8c\u6536\u53f7,").append(csvValue(detail.get("acceptanceNo"))).append('\n');
        csv.append("\u5bfc\u51fa\u7c7b\u578b,").append(csvValue(detail.get("exportType"))).append('\n');
        csv.append("\u5bfc\u51fa\u65f6\u95f4,").append(csvValue(detail.get("exportedAt"))).append('\n');
        csv.append("\u5bfc\u51fa\u4eba,").append(csvValue(detail.get("exportedBy"))).append('\n');
        csv.append("\u5f85\u5904\u7406\u5408\u8ba1,").append(csvValue(detail.get("pendingCount"))).append('\n');
        csv.append("\u5df2\u95ed\u73af\u5408\u8ba1,").append(csvValue(detail.get("closedCount"))).append('\n');
        csv.append("\u6709\u6570\u636e\u961f\u5217\u6570,").append(csvValue(detail.get("activeQueueCount"))).append('\n');
        csv.append("\u8bc1\u636e\u6587\u4ef6\u6570,").append(csvValue(detail.get("evidenceFileCount"))).append('\n');
        csv.append("\u4ea4\u4ed8\u7ed3\u8bba,").append(csvValue(detail.get("conclusion"))).append('\n');
        csv.append('\n');
        csv.append("\u961f\u5217\u5feb\u7167").append('\n');
        csv.append("\u961f\u5217\u7f16\u7801,\u961f\u5217\u540d\u79f0,\u6570\u91cf,\u72b6\u6001,\u8bf4\u660e").append('\n');
        List<Map<String, Object>> rows = detail.get("rows") instanceof List<?> list
                ? (List<Map<String, Object>>) list
                : List.of();
        for (Map<String, Object> row : rows) {
            csv.append(csvValue(row.get("code"))).append(',')
                    .append(csvValue(row.get("title"))).append(',')
                    .append(csvValue(row.get("count"))).append(',')
                    .append(csvValue(row.get("result"))).append(',')
                    .append(csvValue(row.get("hint"))).append('\n');
        }
        csv.append('\n');
        csv.append("\u8bc1\u636e\u6587\u4ef6").append('\n');
        csv.append("\u6587\u4ef6\u540d,\u8bc1\u636e\u540d\u79f0,\u6570\u91cf,\u53e3\u5f84\u8bf4\u660e").append('\n');
        List<Map<String, Object>> evidence = detail.get("evidence") instanceof List<?> list
                ? (List<Map<String, Object>>) list
                : List.of();
        for (Map<String, Object> row : evidence) {
            csv.append(csvValue(row.get("file"))).append(',')
                    .append(csvValue(row.get("title"))).append(',')
                    .append(csvValue(row.get("count"))).append(',')
                    .append(csvValue(row.get("scope"))).append('\n');
        }
        return csv.toString();
    }

    private String toHistoryDeliveryAcceptanceIndexCsv(List<Map<String, Object>> rows) {
        StringBuilder csv = new StringBuilder();
        csv.append("\u9a8c\u6536\u53f7,\u5bfc\u51fa\u7c7b\u578b,\u5bfc\u51fa\u65f6\u95f4,\u5bfc\u51fa\u4eba,\u5f85\u5904\u7406\u5408\u8ba1,\u5df2\u95ed\u73af\u5408\u8ba1,\u6709\u6570\u636e\u961f\u5217\u6570,\u8bc1\u636e\u6587\u4ef6\u6570,\u4ea4\u4ed8\u7ed3\u8bba").append('\n');
        for (Map<String, Object> row : rows) {
            csv.append(csvValue(row.get("acceptanceNo"))).append(',')
                    .append(csvValue(row.get("exportType"))).append(',')
                    .append(csvValue(row.get("exportedAt"))).append(',')
                    .append(csvValue(row.get("exportedBy"))).append(',')
                    .append(csvValue(row.get("pendingCount"))).append(',')
                    .append(csvValue(row.get("closedCount"))).append(',')
                    .append(csvValue(row.get("activeQueueCount"))).append(',')
                    .append(csvValue(row.get("evidenceFileCount"))).append(',')
                    .append(csvValue(row.get("conclusion"))).append('\n');
        }
        return csv.toString();
    }

    @SuppressWarnings("unchecked")
    private String historyDeliveryAcceptanceDetailAuditScope(Map<String, Object> detail) {
        List<Map<String, Object>> rows = detail.get("rows") instanceof List<?> value ? (List<Map<String, Object>>) value : List.of();
        List<Map<String, Object>> evidence = detail.get("evidence") instanceof List<?> value ? (List<Map<String, Object>>) value : List.of();
        return "scope=acceptanceNo=" + stringValue(detail.get("acceptanceNo"))
                + ", exportType=" + stringValue(detail.get("exportType"))
                + ", pending=" + stringValue(detail.get("pendingCount"))
                + ", closed=" + stringValue(detail.get("closedCount"))
                + ", rows=" + rows.size()
                + ", evidence=" + evidence.size();
    }

    private String historyDeliveryAcceptanceAuditScope(String keyword, String exportType, String exportedFrom, String exportedTo, int limit) {
        return "scope=keyword=" + stringValue(keyword)
                + ", exportType=" + stringValue(exportType)
                + ", exportedFrom=" + stringValue(exportedFrom)
                + ", exportedTo=" + stringValue(exportedTo)
                + ", limit=" + limit;
    }

    private String historyDeliveryAcceptanceExportTypeText(String exportType) {
        return switch (stringValue(exportType)) {
            case "OVERVIEW" -> "\u4ea4\u4ed8\u603b\u89c8";
            case "PACKAGE" -> "\u9a8c\u6536\u5305";
            default -> stringValue(exportType).isBlank() ? "\u5168\u90e8" : stringValue(exportType);
        };
    }

    @SuppressWarnings("unchecked")
    private String toHistoryDeliveryAcceptancePrintHtml(Map<String, Object> detail) {
        List<Map<String, Object>> rows = detail.get("rows") instanceof List<?> list
                ? (List<Map<String, Object>>) list
                : List.of();
        List<Map<String, Object>> evidence = detail.get("evidence") instanceof List<?> list
                ? (List<Map<String, Object>>) list
                : List.of();
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="utf-8">
                    <title>历史写入交付确认单</title>
                    <style>
                        * { box-sizing: border-box; }
                        body { margin: 0; color: #172033; background: #eef2f7; font-family: "Microsoft YaHei", "PingFang SC", Arial, sans-serif; }
                        main { width: 210mm; min-height: 297mm; margin: 16px auto; padding: 18mm; background: #fff; box-shadow: 0 12px 36px rgba(15, 23, 42, .12); }
                        header { display: flex; justify-content: space-between; gap: 18px; border-bottom: 2px solid #1f5f8b; padding-bottom: 14px; }
                        h1 { margin: 0; font-size: 26px; letter-spacing: 0; }
                        header span { display: block; margin-top: 8px; color: #526173; font-size: 13px; }
                        .print-actions { display: flex; align-items: start; }
                        button { border: 1px solid #1f5f8b; background: #1f5f8b; color: #fff; padding: 8px 14px; border-radius: 4px; cursor: pointer; }
                        .meta, .metrics { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-top: 18px; }
                        .cover-check { margin-top: 18px; border: 1px solid #abc4d8; background: #f4f9fc; padding: 12px; }
                        .cover-check strong { display: block; margin-bottom: 8px; color: #1f5f8b; }
                        .cover-check p { margin: 4px 0; color: #405064; font-size: 13px; }
                        .cell { border: 1px solid #d7dee9; padding: 10px; min-height: 58px; }
                        .cell b { display: block; color: #526173; font-size: 12px; font-weight: 500; }
                        .cell span { display: block; margin-top: 7px; font-size: 15px; font-weight: 700; word-break: break-word; }
                        .conclusion { margin-top: 14px; padding: 12px; border: 1px solid #abc4d8; background: #f4f9fc; font-weight: 700; }
                        h2 { margin: 22px 0 8px; font-size: 16px; }
                        table { width: 100%; border-collapse: collapse; font-size: 12px; }
                        th, td { border: 1px solid #d7dee9; padding: 8px; text-align: left; vertical-align: top; }
                        th { background: #f3f6fa; color: #405064; }
                        footer { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; margin-top: 26px; font-size: 13px; }
                        footer div { border-top: 1px solid #172033; padding-top: 8px; min-height: 34px; }
                        @media print {
                            body { background: #fff; }
                            main { width: auto; min-height: auto; margin: 0; padding: 10mm; box-shadow: none; }
                            .print-actions { display: none; }
                        }
                    </style>
                </head>
                <body>
                <main>
                    <header>
                        <div>
                            <h1>历史写入交付确认单</h1>
                            <span>用于历史写入交付归档，内容来自已落库验收快照。</span>
                        </div>
                        <div class="print-actions"><button type="button" onclick="window.print()">打印</button></div>
                    </header>
                """);
        html.append("<section class=\"meta\">")
                .append(printCell("验收号", detail.get("acceptanceNo")))
                .append(printCell("导出类型", "PACKAGE".equals(stringValue(detail.get("exportType"))) ? "验收包" : "交付总览"))
                .append(printCell("导出时间", detail.get("exportedAt")))
                .append(printCell("导出人", detail.get("exportedBy")))
                .append("</section>");
        html.append("<section class=\"metrics\">")
                .append(printCell("待处理合计", detail.get("pendingCount")))
                .append(printCell("已闭环合计", detail.get("closedCount")))
                .append(printCell("有数据队列数", detail.get("activeQueueCount")))
                .append(printCell("证据文件数", detail.get("evidenceFileCount")))
                .append("</section>");
        html.append("<div class=\"conclusion\">交付结论：").append(html(detail.get("conclusion"))).append("</div>");
        html.append("<h2>队列快照</h2><table><thead><tr><th>队列编码</th><th>队列名称</th><th>数量</th><th>状态</th><th>说明</th></tr></thead><tbody>");
        for (Map<String, Object> row : rows) {
            html.append("<tr><td>").append(html(row.get("code"))).append("</td><td>")
                    .append(html(row.get("title"))).append("</td><td>")
                    .append(html(row.get("count"))).append("</td><td>")
                    .append(html(row.get("result"))).append("</td><td>")
                    .append(html(row.get("hint"))).append("</td></tr>");
        }
        if (rows.isEmpty()) {
            html.append("<tr><td colspan=\"5\">无队列快照</td></tr>");
        }
        html.append("</tbody></table><h2>证据文件</h2><table><thead><tr><th>文件名</th><th>证据名称</th><th>数量</th><th>口径说明</th></tr></thead><tbody>");
        for (Map<String, Object> row : evidence) {
            html.append("<tr><td>").append(html(row.get("file"))).append("</td><td>")
                    .append(html(row.get("title"))).append("</td><td>")
                    .append(html(row.get("count"))).append("</td><td>")
                    .append(html(row.get("scope"))).append("</td></tr>");
        }
        if (evidence.isEmpty()) {
            html.append("<tr><td colspan=\"4\">无证据文件</td></tr>");
        }
        html.append("""
                    </tbody></table>
                    <footer>
                        <div>经办人：</div>
                        <div>复核人：</div>
                        <div>确认日期：</div>
                    </footer>
                </main>
                </body>
                </html>
                """);
        return html.toString();
    }

    @SuppressWarnings("unchecked")
    private String toHistoryDeliveryAcceptanceBatchPrintHtml(List<Map<String, Object>> details, String keyword, String exportType, String exportedFrom, String exportedTo, long auditId) {
        StringBuilder html = new StringBuilder();
        String generatedAt = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String auditNo = auditId > 0 ? "SYS-" + auditId : "-";
        String keywordText = stringValue(keyword).isBlank() ? "\u5168\u90e8" : stringValue(keyword);
        String exportTypeText = historyDeliveryAcceptanceExportTypeText(exportType);
        String dateScope = (stringValue(exportedFrom).isBlank() ? "\u4e0d\u9650" : stringValue(exportedFrom))
                + " \u81f3 " + (stringValue(exportedTo).isBlank() ? "\u4e0d\u9650" : stringValue(exportedTo));
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="utf-8">
                    <title>历史写入交付确认单归档索引</title>
                    <style>
                        * { box-sizing: border-box; }
                        body { margin: 0; color: #172033; background: #eef2f7; font-family: "Microsoft YaHei", "PingFang SC", Arial, sans-serif; }
                        main { width: 210mm; min-height: 297mm; margin: 16px auto; padding: 18mm; background: #fff; box-shadow: 0 12px 36px rgba(15, 23, 42, .12); page-break-after: always; }
                        header { display: flex; justify-content: space-between; gap: 18px; border-bottom: 2px solid #1f5f8b; padding-bottom: 14px; }
                        h1 { margin: 0; font-size: 24px; letter-spacing: 0; }
                        header span { display: block; margin-top: 8px; color: #526173; font-size: 13px; }
                        .print-actions { display: flex; align-items: start; }
                        button { border: 1px solid #1f5f8b; background: #1f5f8b; color: #fff; padding: 8px 14px; border-radius: 4px; cursor: pointer; }
                        .meta, .metrics { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-top: 18px; }
                        .cell { border: 1px solid #d7dee9; padding: 10px; min-height: 58px; }
                        .cell b { display: block; color: #526173; font-size: 12px; font-weight: 500; }
                        .cell span { display: block; margin-top: 7px; font-size: 15px; font-weight: 700; word-break: break-word; }
                        .conclusion { margin-top: 14px; padding: 12px; border: 1px solid #abc4d8; background: #f4f9fc; font-weight: 700; }
                        h2 { margin: 22px 0 8px; font-size: 16px; }
                        table { width: 100%; border-collapse: collapse; font-size: 12px; }
                        th, td { border: 1px solid #d7dee9; padding: 8px; text-align: left; vertical-align: top; }
                        th { background: #f3f6fa; color: #405064; }
                        footer { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; margin-top: 26px; font-size: 13px; }
                        footer div { border-top: 1px solid #172033; padding-top: 8px; min-height: 34px; }
                        @media print {
                            body { background: #fff; }
                            main { width: auto; min-height: auto; margin: 0; padding: 10mm; box-shadow: none; }
                            .print-actions { display: none; }
                            main:last-child { page-break-after: auto; }
                        }
                    </style>
                </head>
                <body>
                """);
        html.append("<main><header><div><h1>历史写入交付确认单归档索引</h1><span>按当前筛选条件生成，后续页为确认单正文。</span></div>")
                .append("<div class=\"print-actions\"><button type=\"button\" onclick=\"window.print()\">打印全部</button></div></header>")
                .append("<section class=\"meta\">")
                .append(printCell("关键字", stringValue(keyword).isBlank() ? "全部" : keyword))
                .append(printCell("导出类型", stringValue(exportType).isBlank() ? "全部" : exportType))
                .append(printCell("导出日期", (stringValue(exportedFrom).isBlank() ? "不限" : stringValue(exportedFrom)) + " 至 " + (stringValue(exportedTo).isBlank() ? "不限" : stringValue(exportedTo))))
                .append(printCell("确认单数量", details.size()))
                .append(printCell("生成时间", generatedAt))
                .append(printCell("导出审计号", auditNo))
                .append(printCell("纸质核对数", details.size()))
                .append(printCell("导出类型", exportTypeText))
                .append(printCell("筛选范围", keywordText + " / " + dateScope))
                .append("</section><h2>归档清单</h2><table><thead><tr><th>验收号</th><th>导出类型</th><th>导出时间</th><th>导出人</th><th>结论</th></tr></thead><tbody>");
        html.append("<caption style=\"caption-side:top;text-align:left;padding:0 0 8px;\"><strong>纸质归档核对</strong><br>")
                .append("筛选范围：").append(html(keywordText)).append(" / ").append(html(exportTypeText)).append(" / ").append(html(dateScope))
                .append("；系统审计号：").append(html(auditNo))
                .append("；生成时间：").append(html(generatedAt))
                .append("；确认单数量：").append(details.size()).append("</caption>");
        for (Map<String, Object> detail : details) {
            html.append("<tr><td>").append(html(detail.get("acceptanceNo"))).append("</td><td>")
                    .append(html("PACKAGE".equals(stringValue(detail.get("exportType"))) ? "验收包" : "交付总览")).append("</td><td>")
                    .append(html(detail.get("exportedAt"))).append("</td><td>")
                    .append(html(detail.get("exportedBy"))).append("</td><td>")
                    .append(html(detail.get("conclusion"))).append("</td></tr>");
        }
        if (details.isEmpty()) {
            html.append("<tr><td colspan=\"5\">无匹配确认单</td></tr>");
        }
        html.append("</tbody></table></main>");
        for (Map<String, Object> detail : details) {
            List<Map<String, Object>> rows = detail.get("rows") instanceof List<?> list
                    ? (List<Map<String, Object>>) list
                    : List.of();
            List<Map<String, Object>> evidence = detail.get("evidence") instanceof List<?> list
                    ? (List<Map<String, Object>>) list
                    : List.of();
            html.append("<main><header><div><h1>历史写入交付确认单</h1><span>用于历史写入交付归档，内容来自已落库验收快照。</span></div></header>")
                    .append("<section class=\"meta\">")
                    .append(printCell("验收号", detail.get("acceptanceNo")))
                    .append(printCell("导出类型", "PACKAGE".equals(stringValue(detail.get("exportType"))) ? "验收包" : "交付总览"))
                    .append(printCell("导出时间", detail.get("exportedAt")))
                    .append(printCell("导出人", detail.get("exportedBy")))
                    .append("</section><section class=\"metrics\">")
                    .append(printCell("待处理合计", detail.get("pendingCount")))
                    .append(printCell("已闭环合计", detail.get("closedCount")))
                    .append(printCell("有数据队列数", detail.get("activeQueueCount")))
                    .append(printCell("证据文件数", detail.get("evidenceFileCount")))
                    .append("</section><div class=\"conclusion\">交付结论：").append(html(detail.get("conclusion"))).append("</div>")
                    .append("<h2>队列快照</h2><table><thead><tr><th>队列编码</th><th>队列名称</th><th>数量</th><th>状态</th><th>说明</th></tr></thead><tbody>");
            for (Map<String, Object> row : rows) {
                html.append("<tr><td>").append(html(row.get("code"))).append("</td><td>")
                        .append(html(row.get("title"))).append("</td><td>")
                        .append(html(row.get("count"))).append("</td><td>")
                        .append(html(row.get("result"))).append("</td><td>")
                        .append(html(row.get("hint"))).append("</td></tr>");
            }
            if (rows.isEmpty()) {
                html.append("<tr><td colspan=\"5\">无队列快照</td></tr>");
            }
            html.append("</tbody></table><h2>证据文件</h2><table><thead><tr><th>文件名</th><th>证据名称</th><th>数量</th><th>口径说明</th></tr></thead><tbody>");
            for (Map<String, Object> row : evidence) {
                html.append("<tr><td>").append(html(row.get("file"))).append("</td><td>")
                        .append(html(row.get("title"))).append("</td><td>")
                        .append(html(row.get("count"))).append("</td><td>")
                        .append(html(row.get("scope"))).append("</td></tr>");
            }
            if (evidence.isEmpty()) {
                html.append("<tr><td colspan=\"4\">无证据文件</td></tr>");
            }
            html.append("</tbody></table><footer><div>经办人：</div><div>复核人：</div><div>确认日期：</div></footer></main>");
        }
        html.append("</body></html>");
        return html.toString();
    }

    private String printCell(String label, Object value) {
        return "<div class=\"cell\"><b>" + html(label) + "</b><span>" + html(value) + "</span></div>";
    }

    private String toHistoryClosureAcceptanceReadme(List<HistoryClosureAcceptanceRow> rows) {
        long pending = rows.stream()
                .filter(row -> !"\u5df2\u95ed\u73af".equals(row.result()))
                .mapToLong(HistoryClosureAcceptanceRow::count)
                .sum();
        long closed = rows.stream()
                .filter(row -> "\u5df2\u95ed\u73af".equals(row.result()))
                .mapToLong(HistoryClosureAcceptanceRow::count)
                .sum();
        String detail = rows.stream()
                .map(row -> "- " + row.title() + "\uff1a" + row.count() + "\uff0c" + row.result())
                .collect(java.util.stream.Collectors.joining("\n"));
        return """
                \u5386\u53f2\u5199\u5165\u95ed\u73af\u9a8c\u6536\u5305

                \u751f\u6210\u65f6\u95f4\uff1a%s
                \u5f85\u5904\u7406\u5408\u8ba1\uff1a%d
                \u5df2\u95ed\u73af\u5408\u8ba1\uff1a%d

                \u6587\u4ef6\u8bf4\u660e\uff1a
                - salary-migration-closure-checklist.csv\uff1a\u5de5\u8d44\u8fc1\u79fb\u6838\u5fc3\u95ed\u73af\u603b\u6e05\u5355
                - history-write-closure-acceptance-summary.csv\uff1a\u95ed\u73af\u9a8c\u6536\u6c47\u603b
                - history-write-safety-policy.csv\uff1a\u6279\u91cf\u5199\u5165/\u56de\u6eda\u5b89\u5168\u673a\u5236\u8bc1\u636e
                - history-write-plans-unprinted-or-plan.csv\uff1a\u5f85\u6253\u5370/\u9884\u68c0\u6e05\u5355
                - history-write-plans-ready-to-write.csv\uff1a\u5f85\u5199\u5165\u5386\u53f2\u6e05\u5355
                - history-write-plans-review-difference.csv\uff1a\u5199\u5165\u540e\u5f85\u6838\u67e5\u6e05\u5355
                - history-write-plans-rolled-back.csv\uff1a\u5df2\u56de\u6eda\u5f85\u91cd\u65b0\u5904\u7406\u6e05\u5355
                - history-write-batch-ledger-write.csv\uff1a\u5199\u5165\u6279\u6b21\u53f0\u8d26
                - history-write-batch-ledger-review.csv\uff1a\u6838\u67e5/\u590d\u6d4b\u6279\u6b21\u53f0\u8d26
                - history-write-batch-ledger-rolled-back.csv\uff1a\u56de\u6eda\u6279\u6b21\u53f0\u8d26

                \u9a8c\u6536\u53e3\u5f84\uff1a
                %s
                """.formatted(java.time.LocalDateTime.now(), pending, closed, detail);
    }

    private String toHistoryWriteRollbackPreviewCsv(WorkbenchHistoryWriteRollbackPreviewResponse preview) {
        StringBuilder csv = new StringBuilder();
        csv.append("\u9879\u76ee,\u503c").append('\n');
        csv.append(csv("\u529e\u7406\u7f16\u53f7")).append(',').append(csv(preview.caseNo())).append('\n');
        csv.append(csv("\u5199\u5165\u8ba1\u5212\u53f7")).append(',').append(csv(preview.writePlanId())).append('\n');
        csv.append(csv("\u4eba\u5458\u7f16\u7801")).append(',').append(csv(preview.personCode())).append('\n');
        csv.append(csv("\u5355\u4f4d\u7f16\u7801")).append(',').append(csv(preview.orgCode())).append('\n');
        csv.append(csv("\u6267\u884c\u5e74\u6708")).append(',').append(csv(period(preview.year(), preview.month()))).append('\n');
        csv.append(csv("\u53d8\u52a8\u7c7b\u522b")).append(',').append(csv(preview.businessType())).append('\n');
        csv.append(csv("\u9884\u68c0\u72b6\u6001")).append(',').append(csv(preview.status())).append('\n');
        csv.append(csv("\u662f\u5426\u53ef\u64a4\u9500")).append(',').append(csv(Boolean.TRUE.equals(preview.rollbackable()) ? "\u662f" : "\u5426")).append('\n');
        csv.append(csv("\u5199\u5165\u5386\u53f2ID")).append(',').append(csv(preview.historyId())).append('\n');
        csv.append(csv("sid\u662f\u5426\u9700\u6062\u590d")).append(',').append(csv(Boolean.TRUE.equals(preview.sidUpdateRequired()) ? "\u662f" : "\u5426")).append('\n');
        csv.append(csv("sid\u6062\u590d\u65b9\u6848")).append(',').append(csv(preview.sidPlan())).append('\n');
        csv.append(csv("\u786e\u8ba4\u63d0\u793a")).append(',').append(csv(preview.confirmMessage())).append('\n');
        csv.append('\n');
        csv.append("\u94fe\u8def\u4f4d\u7f6e,\u5386\u53f2ID,\u5e74\u5ea6,\u6708\u4efd,\u53d8\u52a8\u7c7b\u522b,\u5408\u8ba1,sid").append('\n');
        appendRollbackHistoryRow(csv, "\u524d\u4e00\u6761", preview.previousHistory());
        appendRollbackHistoryRow(csv, "\u5199\u5165\u884c", preview.insertedHistory());
        appendRollbackHistoryRow(csv, "\u540e\u4e00\u6761", preview.nextHistory());
        csv.append('\n');
        csv.append("\u963b\u65ad\u9879").append('\n');
        List<String> issues = preview.issues() == null ? List.of() : preview.issues();
        if (issues.isEmpty()) {
            csv.append(csv("\u65e0")).append('\n');
        } else {
            for (String issue : issues) {
                csv.append(csv(issue)).append('\n');
            }
        }
        return csv.toString();
    }

    private void appendRollbackHistoryRow(StringBuilder csv, String position, com.dx.rsgzgl.system.dto.WorkbenchHistoryWritePreviewHistoryRow row) {
        csv.append(csv(position)).append(',');
        if (row == null) {
            csv.append(",,,,,").append('\n');
            return;
        }
        csv.append(csv(row.historyId())).append(',')
                .append(row.year() == null ? "" : row.year()).append(',')
                .append(row.month() == null ? "" : row.month()).append(',')
                .append(csv(row.changeType())).append(',')
                .append(csv(amountText(row.totalAmount()))).append(',')
                .append(csv(row.nextId())).append('\n');
    }

    private String historyWriteBatchClosureText(WorkbenchHistoryWriteBatchLedgerResponse batch) {
        if (batch.failed() > 0 || batch.skipped() > 0 || batch.mismatched() > 0) {
            return "\u4ecd\u5f85\u5904\u7406";
        }
        String action = stringValue(batch.action());
        if (action.contains("rollback")) {
            return "\u5df2\u64a4\u9500";
        }
        if (action.contains("retest-approve")) {
            return "\u5df2\u901a\u8fc7\u5f52\u6863";
        }
        if (action.contains("retest-preview")) {
            return "\u5df2\u590d\u6d4b";
        }
        if (action.contains("execute")) {
            return "\u5df2\u5199\u5165";
        }
        return "\u5df2\u8bb0\u5f55";
    }

    @SuppressWarnings("unchecked")
    private String toDataGovernanceCsv(Map<String, Object> result) {
        StringBuilder csv = new StringBuilder();
        csv.append("\u5355\u4f4d,\u68c0\u67e5\u65f6\u95f4,\u95ee\u9898\u6570,\u7f3a\u4efb\u804c,\u5b66\u5386\u65e5\u671f\u5f02\u5e38,\u5386\u53f2\u94fe\u5f02\u5e38").append('\n');
        csv.append(csv(String.valueOf(result.get("orgCode")))).append(',')
                .append(csv(String.valueOf(result.get("checkedAt")))).append(',')
                .append(result.get("issueCount")).append(',')
                .append(result.get("missingPostCount")).append(',')
                .append(result.get("invalidEducationCount")).append(',')
                .append(result.get("brokenHistoryCount")).append('\n')
                .append('\n');
        csv.append("\u4eba\u5458\u7f16\u7801,\u59d3\u540d,\u5355\u4f4d,\u95ee\u9898\u7c7b\u578b,\u8bf4\u660e,\u6838\u67e5\u72b6\u6001,\u6838\u67e5\u8bf4\u660e,\u6838\u67e5\u4eba,\u6838\u67e5\u65f6\u95f4").append('\n');
        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.getOrDefault("issues", List.of());
        for (Map<String, Object> issue : issues) {
            csv.append(csv(stringValue(issue.get("personCode")))).append(',')
                    .append(csv(stringValue(issue.get("personName")))).append(',')
                    .append(csv(stringValue(issue.get("orgCode")))).append(',')
                    .append(csv(stringValue(issue.get("issueType")))).append(',')
                    .append(csv(stringValue(issue.get("message")))).append(',')
                    .append(csv(reviewStatusText(stringValue(issue.get("reviewStatus"))))).append(',')
                    .append(csv(stringValue(issue.get("reviewReason")))).append(',')
                    .append(csv(stringValue(issue.get("reviewedBy")))).append(',')
                    .append(csv(stringValue(issue.get("reviewedAt")))).append('\n');
        }
        return csv.toString();
    }

    @SuppressWarnings("unchecked")
    private String toMigrationAcceptanceRunCsv(Map<String, Object> result) {
        StringBuilder csv = new StringBuilder();
        Map<String, Object> summary = (Map<String, Object>) result.getOrDefault("summary", Map.of());
        csv.append("\u9a8c\u6536\u6279\u6b21,\u5355\u4f4d,\u68c0\u67e5\u65f6\u95f4,\u6837\u672c\u4e0a\u9650,\u7efc\u5408\u72b6\u6001,\u95f8\u53e3\u6570,\u9884\u8b66\u6570,\u5f85\u529e,\u5df2\u529e,\u5199\u5165\u5f85\u6267\u884c,\u5199\u5165\u5df2\u6267\u884c,\u5199\u5165\u963b\u65ad,\u5f85\u590d\u6838,\u6570\u636e\u6cbb\u7406\u95ee\u9898").append('\n');
        csv.append(csv(String.valueOf(result.get("runNo")))).append(',')
                .append(csv(String.valueOf(result.get("orgCode")))).append(',')
                .append(csv(String.valueOf(result.get("checkedAt")))).append(',')
                .append(result.get("sampleLimit")).append(',')
                .append(csv(String.valueOf(result.get("overallStatus")))).append(',')
                .append(result.getOrDefault("gateCount", 0)).append(',')
                .append(result.getOrDefault("warningCount", 0)).append(',')
                .append(summary.getOrDefault("salaryTodo", 0)).append(',')
                .append(summary.getOrDefault("salaryDone", 0)).append(',')
                .append(summary.getOrDefault("historyPrepared", 0)).append(',')
                .append(summary.getOrDefault("historyExecuted", 0)).append(',')
                .append(summary.getOrDefault("historyBlocked", 0)).append(',')
                .append(summary.getOrDefault("reviewPending", 0)).append(',')
                .append(summary.getOrDefault("dataGovernanceIssues", 0)).append('\n')
                .append('\n');
        csv.append("\u95f8\u53e3\u7f16\u7801,\u95f8\u53e3,\u72b6\u6001,\u6570\u91cf,\u8bf4\u660e").append('\n');
        List<Map<String, Object>> gates = (List<Map<String, Object>>) result.getOrDefault("gates", List.of());
        for (Map<String, Object> gate : gates) {
            csv.append(csv(String.valueOf(gate.get("code")))).append(',')
                    .append(csv(String.valueOf(gate.get("title")))).append(',')
                    .append(csv(String.valueOf(gate.get("status")))).append(',')
                    .append(gate.getOrDefault("count", 0)).append(',')
                    .append(csv(String.valueOf(gate.get("message")))).append('\n');
        }
        csv.append('\n');
        csv.append("\u4eba\u5458\u7f16\u7801,\u59d3\u540d,\u5355\u4f4d,\u95ee\u9898\u7c7b\u578b,\u8bf4\u660e,\u6838\u67e5\u72b6\u6001,\u6838\u67e5\u8bf4\u660e,\u6838\u67e5\u4eba,\u6838\u67e5\u65f6\u95f4").append('\n');
        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.getOrDefault("issues", List.of());
        for (Map<String, Object> issue : issues) {
            csv.append(csv(stringValue(issue.get("personCode")))).append(',')
                    .append(csv(stringValue(issue.get("personName")))).append(',')
                    .append(csv(stringValue(issue.get("orgCode")))).append(',')
                    .append(csv(stringValue(issue.get("issueType")))).append(',')
                    .append(csv(stringValue(issue.get("message")))).append(',')
                    .append(csv(reviewStatusText(stringValue(issue.get("reviewStatus"))))).append(',')
                    .append(csv(stringValue(issue.get("reviewReason")))).append(',')
                    .append(csv(stringValue(issue.get("reviewedBy")))).append(',')
                    .append(csv(stringValue(issue.get("reviewedAt")))).append('\n');
        }
        return csv.toString();
    }

    private String toMigrationRegressionSampleLibraryCsv(List<Map<String, Object>> rows) {
        StringBuilder csv = new StringBuilder();
        csv.append("sampleCode,sampleTitle,sampleDomain,sampleId,personCode,personName,orgCode,sampleType,batchNo,sampleSource,expectedStatus,expectedAmount,expectedPayload,enabled,note,lastRunStatus,lastRunMessage,lastRunAt").append('\n');
        for (Map<String, Object> row : rows) {
            csv.append(csv(stringValue(row.get("sampleCode")))).append(',')
                    .append(csv(stringValue(row.get("sampleTitle")))).append(',')
                    .append(csv(stringValue(row.get("sampleDomain")))).append(',')
                    .append(csv(stringValue(row.get("sampleId")))).append(',')
                    .append(csv(stringValue(row.get("personCode")))).append(',')
                    .append(csv(stringValue(row.get("personName")))).append(',')
                    .append(csv(stringValue(row.get("orgCode")))).append(',')
                    .append(csv(stringValue(row.get("sampleType")))).append(',')
                    .append(csv(stringValue(row.get("batchNo")))).append(',')
                    .append(csv(stringValue(row.get("sampleSource")))).append(',')
                    .append(csv(stringValue(row.get("expectedStatus")))).append(',')
                    .append(csv(stringValue(row.get("expectedAmount")))).append(',')
                    .append(csv(stringValue(row.get("expectedPayload")))).append(',')
                    .append(csv(stringValue(row.get("enabled")))).append(',')
                    .append(csv(stringValue(row.get("note")))).append(',')
                    .append(csv(stringValue(row.get("lastRunStatus")))).append(',')
                    .append(csv(stringValue(row.get("lastRunMessage")))).append(',')
                    .append(csv(stringValue(row.get("lastRunAt")))).append('\n');
        }
        return csv.toString();
    }

    private String toMigrationQualitySnapshotLedgerCsv(List<Map<String, Object>> rows) {
        return toMigrationQualitySnapshotLedgerCsv(rows, "");
    }

    private String toMigrationQualitySnapshotLedgerCsv(List<Map<String, Object>> rows, String latestSnapshotNo) {
        StringBuilder csv = new StringBuilder();
        csv.append("\u5feb\u7167\u7f16\u53f7,\u5355\u4f4d,\u68c0\u67e5\u65f6\u95f4,\u7efc\u5408\u72b6\u6001,\u5de1\u68c0\u7ed3\u8bba,\u5f52\u6863\u72b6\u6001,\u5f52\u6863\u6709\u6548\u6027,\u5f52\u6863\u4eba,\u5f52\u6863\u65f6\u95f4,\u5f52\u6863\u8bf4\u660e,\u6cbb\u7406\u95ee\u9898,\u56de\u5f52WARN,\u5386\u53f2\u963b\u65ad,\u5386\u53f2\u961f\u5217,\u5f85\u590d\u6838,\u5f85\u529e,\u5df2\u529e,\u751f\u6210\u4eba,\u751f\u6210\u65f6\u95f4").append('\n');
        for (Map<String, Object> row : rows) {
            String snapshotNo = stringValue(row.get("snapshotNo"));
            String archiveFreshness = "";
            if ("ARCHIVED".equals(stringValue(row.get("archiveStatus"))) && !latestSnapshotNo.isBlank()) {
                archiveFreshness = latestSnapshotNo.equals(snapshotNo) ? "\u5f53\u524d\u5f52\u6863" : "\u5df2\u88ab\u65b0\u5feb\u7167\u8d85\u8d8a";
            }
            csv.append(csv(stringValue(row.get("snapshotNo")))).append(',')
                    .append(csv(stringValue(row.get("orgCode")))).append(',')
                    .append(csv(stringValue(row.get("checkedAt")))).append(',')
                    .append(csv(stringValue(row.get("status")))).append(',')
                    .append(csv(stringValue(row.get("preflightTitle")))).append(',')
                    .append(csv(stringValue(row.get("archiveStatus")))).append(',')
                    .append(csv(archiveFreshness)).append(',')
                    .append(csv(stringValue(row.get("archivedBy")))).append(',')
                    .append(csv(stringValue(row.get("archivedAt")))).append(',')
                    .append(csv(stringValue(row.get("archiveNote")))).append(',')
                    .append(csv(stringValue(row.get("governanceIssues")))).append(',')
                    .append(csv(stringValue(row.get("regressionWarnings")))).append(',')
                    .append(csv(stringValue(row.get("historyBlocked")))).append(',')
                    .append(csv(stringValue(row.get("historyOpen")))).append(',')
                    .append(csv(stringValue(row.get("reviewPending")))).append(',')
                    .append(csv(stringValue(row.get("salaryTodo")))).append(',')
                    .append(csv(stringValue(row.get("salaryDone")))).append(',')
                    .append(csv(stringValue(row.get("createdBy")))).append(',')
                    .append(csv(stringValue(row.get("createdAt")))).append('\n');
        }
        return csv.toString();
    }

    @SuppressWarnings("unchecked")
    private String toMigrationQualityFinalAcceptanceSummaryCsv(Map<String, Object> overview) {
        Map<String, Object> summary = overview.get("summary") instanceof Map<?, ?> summaryMap
                ? (Map<String, Object>) summaryMap
                : Map.of();
        Map<String, Object> archiveSummary = overview.get("archiveSummary") instanceof Map<?, ?> archiveMap
                ? (Map<String, Object>) archiveMap
                : Map.of();
        boolean closed = !"".equals(stringValue(archiveSummary.get("latestExportNo")))
                && "0".equals(stringValue(summary.getOrDefault("historyBlocked", "0")))
                && "0".equals(stringValue(summary.getOrDefault("regressionWarnings", "0")))
                && "0".equals(stringValue(summary.getOrDefault("reviewPending", "0")))
                && "0".equals(stringValue(summary.getOrDefault("governanceIssues", "0")));
        StringBuilder csv = new StringBuilder();
        csv.append("\u9879\u76ee,\u503c").append('\n');
        csv.append(csv("\u5355\u4f4d")).append(',').append(csv(stringValue(overview.get("orgCode")))).append('\n');
        csv.append(csv("\u68c0\u67e5\u65f6\u95f4")).append(',').append(csv(stringValue(overview.get("checkedAt")))).append('\n');
        csv.append(csv("\u8d28\u91cf\u72b6\u6001")).append(',').append(csv(stringValue(overview.get("status")))).append('\n');
        csv.append(csv("\u5f52\u6863\u6570\u91cf")).append(',').append(csv(stringValue(archiveSummary.get("archivedCount")))).append('\n');
        csv.append(csv("\u6700\u8fd1\u5f52\u6863\u5feb\u7167")).append(',').append(csv(stringValue(archiveSummary.get("latestArchivedSnapshotNo")))).append('\n');
        csv.append(csv("\u6700\u8fd1\u5f52\u6863\u65f6\u95f4")).append(',').append(csv(stringValue(archiveSummary.get("latestArchivedAt")))).append('\n');
        csv.append(csv("\u6700\u8fd1\u9a8c\u6536\u5305\u5feb\u7167")).append(',').append(csv(stringValue(archiveSummary.get("latestExportSnapshotNo")))).append('\n');
        csv.append(csv("\u9a8c\u6536\u5305\u7248\u672c")).append(',').append(csv(stringValue(archiveSummary.get("latestExportNo")).isBlank() ? "" : "v" + stringValue(archiveSummary.get("latestExportNo")))).append('\n');
        csv.append(csv("\u9a8c\u6536\u5305\u5bfc\u51fa\u4eba")).append(',').append(csv(stringValue(archiveSummary.get("latestExportedBy")))).append('\n');
        csv.append(csv("\u9a8c\u6536\u5305\u5bfc\u51fa\u65f6\u95f4")).append(',').append(csv(stringValue(archiveSummary.get("latestExportedAt")))).append('\n');
        csv.append(csv("\u5386\u53f2\u963b\u65ad")).append(',').append(csv(stringValue(summary.get("historyBlocked")))).append('\n');
        csv.append(csv("\u56de\u5f52WARN")).append(',').append(csv(stringValue(summary.get("regressionWarnings")))).append('\n');
        csv.append(csv("\u5f85\u590d\u6838")).append(',').append(csv(stringValue(summary.get("reviewPending")))).append('\n');
        csv.append(csv("\u6570\u636e\u6cbb\u7406")).append(',').append(csv(stringValue(summary.get("governanceIssues")))).append('\n');
        csv.append(csv("\u7559\u6863\u7ed3\u8bba")).append(',').append(csv(closed ? "\u95ed\u73af\u5b8c\u6210" : "\u4ecd\u6709\u5f85\u5904\u7406\u9879")).append('\n');
        return csv.toString();
    }

    @SuppressWarnings("unchecked")
    private String toMigrationQualitySnapshotComparisonCsv(Map<String, Object> comparison) {
        Map<String, Object> base = comparison.get("base") instanceof Map<?, ?> baseMap
                ? (Map<String, Object>) baseMap
                : Map.of();
        Map<String, Object> target = comparison.get("target") instanceof Map<?, ?> targetMap
                ? (Map<String, Object>) targetMap
                : Map.of();
        Map<String, Object> summary = comparison.get("summary") instanceof Map<?, ?> summaryMap
                ? (Map<String, Object>) summaryMap
                : Map.of();
        List<Map<String, Object>> deltas = comparison.get("deltas") instanceof List<?> deltaList
                ? deltaList.stream()
                .filter(Map.class::isInstance)
                .map(item -> (Map<String, Object>) item)
                .toList()
                : List.of();

        StringBuilder csv = new StringBuilder();
        csv.append("\u9879\u76ee,\u503c").append('\n');
        csv.append(csv("\u57fa\u51c6\u5feb\u7167")).append(',').append(csv(stringValue(base.get("snapshotNo")))).append('\n');
        csv.append(csv("\u76ee\u6807\u5feb\u7167")).append(',').append(csv(stringValue(target.get("snapshotNo")))).append('\n');
        csv.append(csv("\u5bf9\u6bd4\u72b6\u6001")).append(',').append(csv(stringValue(summary.get("status")))).append('\n');
        csv.append(csv("\u4e0a\u5347\u9879")).append(',').append(csv(stringValue(summary.get("increased")))).append('\n');
        csv.append(csv("\u4e0b\u964d\u9879")).append(',').append(csv(stringValue(summary.get("decreased")))).append('\n');
        csv.append(csv("\u6301\u5e73\u9879")).append(',').append(csv(stringValue(summary.get("unchanged")))).append('\n');
        csv.append('\n');
        csv.append("\u6307\u6807\u7f16\u7801,\u5bf9\u6bd4\u9879,\u57fa\u51c6\u5feb\u7167,\u76ee\u6807\u5feb\u7167,\u53d8\u5316\u91cf,\u53d8\u5316\u65b9\u5411,\u5904\u7406\u52a8\u4f5c").append('\n');
        for (Map<String, Object> delta : deltas) {
            String code = stringValue(delta.get("code"));
            csv.append(csv(code)).append(',')
                    .append(csv(stringValue(delta.get("title")))).append(',')
                    .append(csv(stringValue(delta.get("base")))).append(',')
                    .append(csv(stringValue(delta.get("target")))).append(',')
                    .append(csv(stringValue(delta.get("delta")))).append(',')
                    .append(csv(migrationQualityDeltaDirectionText(stringValue(delta.get("direction"))))).append(',')
                    .append(csv(migrationQualityDeltaActionText(code))).append('\n');
        }
        return csv.toString();
    }

    private String migrationQualityDeltaDirectionText(String direction) {
        return switch (direction) {
            case "INCREASED" -> "\u4e0a\u5347";
            case "DECREASED" -> "\u4e0b\u964d";
            case "UNCHANGED" -> "\u6301\u5e73";
            default -> direction;
        };
    }

    private String migrationQualityDeltaActionText(String code) {
        return switch (code) {
            case "governanceIssues" -> "\u5904\u7406\u6cbb\u7406";
            case "regressionWarnings", "regressionPending", "regressionFixing" -> "\u5904\u7406\u56de\u5f52";
            case "historyBlocked", "historyOpen" -> "\u5904\u7406\u5199\u5165";
            case "reviewPending" -> "\u5904\u7406\u590d\u6838";
            case "salaryTodo" -> "\u67e5\u770b\u5f85\u529e";
            default -> "";
        };
    }

    @SuppressWarnings("unchecked")
    private String toMigrationQualityAcceptanceSummaryCsv(Map<String, Object> snapshot) {
        Map<String, Object> overview = snapshot.get("overview") instanceof Map<?, ?> overviewMap
                ? (Map<String, Object>) overviewMap
                : Map.of();
        Map<String, Object> summary = overview.get("summary") instanceof Map<?, ?> summaryMap
                ? (Map<String, Object>) summaryMap
                : Map.of();
        StringBuilder csv = new StringBuilder();
        csv.append("\u9879\u76ee,\u503c").append('\n');
        csv.append(csv("\u5feb\u7167\u7f16\u53f7")).append(',').append(csv(stringValue(snapshot.get("snapshotNo")))).append('\n');
        csv.append(csv("\u5355\u4f4d")).append(',').append(csv(stringValue(snapshot.get("orgCode")))).append('\n');
        csv.append(csv("\u68c0\u67e5\u65f6\u95f4")).append(',').append(csv(stringValue(overview.getOrDefault("checkedAt", snapshot.get("checkedAt"))))).append('\n');
        csv.append(csv("\u5de1\u68c0\u7ed3\u8bba")).append(',').append(csv(stringValue(snapshot.get("preflightTitle")))).append('\n');
        csv.append(csv("\u7ed3\u8bba\u8bf4\u660e")).append(',').append(csv(stringValue(snapshot.get("preflightMessage")))).append('\n');
        csv.append(csv("\u5f52\u6863\u72b6\u6001")).append(',').append(csv(stringValue(snapshot.get("archiveStatus")))).append('\n');
        csv.append(csv("\u5f52\u6863\u4eba")).append(',').append(csv(stringValue(snapshot.get("archivedBy")))).append('\n');
        csv.append(csv("\u5f52\u6863\u65f6\u95f4")).append(',').append(csv(stringValue(snapshot.get("archivedAt")))).append('\n');
        csv.append(csv("\u5f52\u6863\u8bf4\u660e")).append(',').append(csv(stringValue(snapshot.get("archiveNote")))).append('\n');
        csv.append(csv("\u5f85\u529e")).append(',').append(csv(stringValue(summary.get("salaryTodo")))).append('\n');
        csv.append(csv("\u5df2\u529e")).append(',').append(csv(stringValue(summary.get("salaryDone")))).append('\n');
        csv.append(csv("\u6570\u636e\u6cbb\u7406")).append(',').append(csv(stringValue(summary.get("governanceIssues")))).append('\n');
        csv.append(csv("\u56de\u5f52WARN")).append(',').append(csv(stringValue(summary.get("regressionWarnings")))).append('\n');
        csv.append(csv("\u5386\u53f2\u963b\u65ad")).append(',').append(csv(stringValue(summary.get("historyBlocked")))).append('\n');
        csv.append(csv("\u5386\u53f2\u961f\u5217")).append(',').append(csv(stringValue(summary.get("historyOpen")))).append('\n');
        csv.append(csv("\u5f85\u590d\u6838")).append(',').append(csv(stringValue(summary.get("reviewPending")))).append('\n');
        return csv.toString();
    }

    private String toMigrationQualityAcceptancePackageReadme(Map<String, Object> snapshot) {
        String snapshotNo = stringValue(snapshot.get("snapshotNo"));
        String exportNo = stringValue(snapshot.get("exportNo"));
        return """
                \u4e0a\u7ebf\u9a8c\u6536\u5305

                \u672c\u538b\u7f29\u5305\u7531\u7cfb\u7edf\u6839\u636e\u5df2\u4fdd\u5b58\u7684\u8fc1\u79fb\u8d28\u91cf\u5feb\u7167\u751f\u6210\uff0c\u7528\u4e8e\u4e0a\u7ebf\u9a8c\u6536\u7559\u6863\u3002
                \u5feb\u7167\u7f16\u53f7\uff1a%s
                \u5bfc\u51fa\u7248\u672c\uff1av%s
                \u5bfc\u51fa\u4eba\uff1a%s
                \u5bfc\u51fa\u65f6\u95f4\uff1a%s

                \u6587\u4ef6\u8bf4\u660e\uff1a
                - migration-quality-final-summary-%s.csv\uff1a\u6700\u7ec8\u9a8c\u6536\u6458\u8981
                - migration-quality-summary-%s.csv\uff1a\u9a8c\u6536\u6458\u8981
                - migration-quality-report-%s.csv\uff1a\u4e0a\u7ebf\u5de1\u68c0\u62a5\u544a
                - migration-quality-print-audits-%s.csv\uff1a\u62a5\u544a\u6253\u5370\u8bb0\u5f55
                - migration-quality-archive-ledger-%s.csv\uff1a\u5f53\u524d\u5355\u4f4d\u5f52\u6863\u53f0\u8d26
                - salary-report-migration-closure-%s.csv\uff1a\u62a5\u8868\u6253\u5370\u8fc1\u79fb\u95ed\u73af\u6458\u8981
                - migration-quality-snapshot-compare-*.csv\uff1a\u5f53\u524d\u5f52\u6863\u5feb\u7167\u5df2\u88ab\u65b0\u5feb\u7167\u8d85\u8d8a\u65f6\uff0c\u81ea\u52a8\u9644\u5e26\u5dee\u5f02\u5bf9\u6bd4
                """.formatted(
                snapshotNo,
                exportNo.isBlank() ? "-" : exportNo,
                stringValue(snapshot.get("exportedBy")),
                stringValue(snapshot.get("exportedAt")),
                snapshotNo,
                snapshotNo,
                snapshotNo,
                snapshotNo,
                stringValue(snapshot.get("orgCode")),
                stringValue(snapshot.get("orgCode"))
        );
    }

    @SuppressWarnings("unchecked")
    private String toMigrationQualitySnapshotCsv(Map<String, Object> snapshot) {
        Map<String, Object> overview = snapshot.get("overview") instanceof Map<?, ?> overviewMap
                ? (Map<String, Object>) overviewMap
                : Map.of();
        Map<String, Object> summary = overview.get("summary") instanceof Map<?, ?> summaryMap
                ? (Map<String, Object>) summaryMap
                : Map.of();
        List<Map<String, Object>> gates = overview.get("gates") instanceof List<?> gateList
                ? gateList.stream()
                .filter(Map.class::isInstance)
                .map(item -> (Map<String, Object>) item)
                .toList()
                : List.of();
        StringBuilder csv = new StringBuilder();
        csv.append("\u5feb\u7167\u7f16\u53f7,\u5355\u4f4d,\u68c0\u67e5\u65f6\u95f4,\u7efc\u5408\u72b6\u6001,\u5de1\u68c0\u7ed3\u8bba,\u7ed3\u8bba\u8bf4\u660e,\u5f52\u6863\u72b6\u6001,\u5f52\u6863\u4eba,\u5f52\u6863\u65f6\u95f4,\u5f52\u6863\u8bf4\u660e,\u5f85\u529e,\u5df2\u529e,\u6570\u636e\u6cbb\u7406,\u56de\u5f52WARN,\u56de\u5f52\u5f85\u6838\u67e5,\u5386\u53f2\u963b\u65ad,\u5386\u53f2\u961f\u5217,\u5f85\u590d\u6838,\u751f\u6210\u4eba,\u751f\u6210\u65f6\u95f4").append('\n');
        csv.append(csv(stringValue(snapshot.get("snapshotNo")))).append(',')
                .append(csv(stringValue(snapshot.get("orgCode")))).append(',')
                .append(csv(stringValue(overview.getOrDefault("checkedAt", snapshot.get("checkedAt"))))).append(',')
                .append(csv(stringValue(overview.getOrDefault("status", snapshot.get("status"))))).append(',')
                .append(csv(stringValue(snapshot.get("preflightTitle")))).append(',')
                .append(csv(stringValue(snapshot.get("preflightMessage")))).append(',')
                .append(csv(stringValue(snapshot.get("archiveStatus")))).append(',')
                .append(csv(stringValue(snapshot.get("archivedBy")))).append(',')
                .append(csv(stringValue(snapshot.get("archivedAt")))).append(',')
                .append(csv(stringValue(snapshot.get("archiveNote")))).append(',')
                .append(csv(stringValue(summary.get("salaryTodo")))).append(',')
                .append(csv(stringValue(summary.get("salaryDone")))).append(',')
                .append(csv(stringValue(summary.get("governanceIssues")))).append(',')
                .append(csv(stringValue(summary.get("regressionWarnings")))).append(',')
                .append(csv(stringValue(summary.get("regressionPending")))).append(',')
                .append(csv(stringValue(summary.get("historyBlocked")))).append(',')
                .append(csv(stringValue(summary.get("historyOpen")))).append(',')
                .append(csv(stringValue(summary.get("reviewPending")))).append(',')
                .append(csv(stringValue(snapshot.get("createdBy")))).append(',')
                .append(csv(stringValue(snapshot.get("createdAt")))).append('\n');
        csv.append('\n');
        csv.append("\u95f8\u53e3\u7f16\u7801,\u95f8\u53e3,\u72b6\u6001,\u6570\u91cf,\u8bf4\u660e").append('\n');
        for (Map<String, Object> gate : gates) {
            csv.append(csv(stringValue(gate.get("code")))).append(',')
                    .append(csv(stringValue(gate.get("title")))).append(',')
                    .append(csv(stringValue(gate.get("status")))).append(',')
                    .append(csv(stringValue(gate.get("count")))).append(',')
                    .append(csv(stringValue(gate.get("message")))).append('\n');
        }
        return csv.toString();
    }

    private HistoryWriteMaintenanceSuggestionCsv maintenanceSuggestionCsv(String suggestionJson) {
        String safeJson = suggestionJson == null ? "" : suggestionJson.trim();
        if (safeJson.isBlank() || "[]".equals(safeJson)) {
            return new HistoryWriteMaintenanceSuggestionCsv("", "", "");
        }
        try {
            List<Map<String, Object>> rows = objectMapper.readValue(safeJson, new TypeReference<>() {
            });
            Set<String> directions = new LinkedHashSet<>();
            List<String> fields = new ArrayList<>();
            List<String> reasons = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                String label = stringValue(row.get("label"));
                String count = stringValue(row.get("count"));
                if (!label.isBlank()) {
                    directions.add(label + (count.isBlank() ? "" : " " + count));
                }
                Object fieldValue = row.get("fields");
                if (fieldValue instanceof List<?> fieldList) {
                    for (Object field : fieldList) {
                        String text = stringValue(field);
                        if (!text.isBlank()) {
                            fields.add(text);
                        }
                    }
                }
                String reason = stringValue(row.get("reason"));
                if (!reason.isBlank()) {
                    reasons.add(reason);
                }
            }
            return new HistoryWriteMaintenanceSuggestionCsv(
                    String.join(" | ", directions),
                    String.join(" | ", fields),
                    String.join(" | ", reasons)
            );
        } catch (JsonProcessingException ex) {
            return new HistoryWriteMaintenanceSuggestionCsv("", "", "");
        }
    }

    private String toHistoryWriteComparisonCsv(WorkbenchHistoryWriteComparisonResponse comparison) {
        StringBuilder csv = new StringBuilder();
        csv.append("\u9879\u76ee,\u503c").append('\n');
        csv.append(csv("\u529e\u7406\u7f16\u53f7")).append(',').append(csv(comparison.caseNo())).append('\n');
        csv.append(csv("\u5199\u5165\u8ba1\u5212\u53f7")).append(',').append(csv(comparison.planNo())).append('\n');
        csv.append(csv("\u4eba\u5458\u7f16\u7801")).append(',').append(csv(comparison.personCode())).append('\n');
        csv.append(csv("\u5355\u4f4d\u7f16\u7801")).append(',').append(csv(comparison.orgCode())).append('\n');
        csv.append(csv("\u6267\u884c\u5e74\u6708")).append(',').append(csv(period(comparison.year(), comparison.month()))).append('\n');
        csv.append(csv("\u53d8\u52a8\u7c7b\u522b")).append(',').append(csv(comparison.businessType())).append('\n');
        csv.append(csv("\u8ba1\u5212\u72b6\u6001")).append(',').append(csv(comparison.planStatus())).append('\n');
        csv.append(csv("\u6267\u884c\u7ed3\u679c")).append(',').append(csv(comparison.executionResult())).append('\n');
        csv.append(csv("\u5199\u5165\u5386\u53f2ID")).append(',').append(csv(comparison.insertedHistoryId())).append('\n');
        csv.append(csv("\u9884\u671f\u5408\u8ba1")).append(',').append(csv(amountText(comparison.expectedTotal()))).append('\n');
        csv.append(csv("hisbase\u5408\u8ba1")).append(',').append(csv(amountText(comparison.actualTotal()))).append('\n');
        csv.append(csv("\u5408\u8ba1\u662f\u5426\u4e00\u81f4")).append(',').append(csv(Boolean.TRUE.equals(comparison.totalMatched()) ? "\u662f" : "\u5426")).append('\n');
        csv.append(csv("\u6838\u67e5\u5206\u7c7b")).append(',').append(csv(comparisonReviewCategoryText(comparison.reviewCategory()))).append('\n');
        csv.append(csv("\u6838\u67e5\u8bf4\u660e")).append(',').append(csv(comparison.reviewReason())).append('\n');
        csv.append('\n');
        csv.append("\u5de5\u8d44\u9879\u7f16\u7801,\u5de5\u8d44\u9879\u540d\u79f0,hisbase\u5b57\u6bb5,\u5feb\u7167\u9884\u671f\u91d1\u989d,hisbase\u5b9e\u9645\u91d1\u989d,\u662f\u5426\u6620\u5c04,\u662f\u5426\u4e00\u81f4,\u95ee\u9898").append('\n');
        comparison.fields().forEach(field -> csv.append(csv(field.itemCode())).append(',')
                .append(csv(field.itemName())).append(',')
                .append(csv(field.historyField())).append(',')
                .append(csv(amountText(field.expectedAmount()))).append(',')
                .append(csv(amountText(field.actualAmount()))).append(',')
                .append(csv(Boolean.TRUE.equals(field.mapped()) ? "\u662f" : "\u5426")).append(',')
                .append(csv(Boolean.TRUE.equals(field.matched()) ? "\u662f" : "\u5426")).append(',')
                .append(csv(field.issue())).append('\n'));
        return csv.toString();
    }

    private String statusText(String status) {
        if ("DONE".equalsIgnoreCase(status)) {
            return "\u5df2\u529e";
        }
        if ("CANCELLED".equalsIgnoreCase(status)) {
            return "\u5df2\u64a4\u56de";
        }
        if ("TODO".equalsIgnoreCase(status)) {
            return "\u5f85\u529e";
        }
        return status == null ? "" : status;
    }

    private String trialStatusText(String status) {
        if ("MATCH".equalsIgnoreCase(status)) {
            return "\u5339\u914d";
        }
        if ("DIFFERENT".equalsIgnoreCase(status)) {
            return "\u6709\u5dee\u5f02";
        }
        if ("ERROR".equalsIgnoreCase(status)) {
            return "\u8bd5\u7b97\u5f02\u5e38";
        }
        if ("SKIPPED".equalsIgnoreCase(status)) {
            return "\u672a\u8bd5\u7b97";
        }
        return status == null ? "" : status;
    }

    private String reviewStatusText(String status) {
        if ("PENDING".equalsIgnoreCase(status)) {
            return "\u5f85\u590d\u6838";
        }
        if ("REVIEWED".equalsIgnoreCase(status)) {
            return "\u5df2\u590d\u6838";
        }
        if ("IGNORED".equalsIgnoreCase(status)) {
            return "\u5df2\u5ffd\u7565";
        }
        if ("MATCHED".equalsIgnoreCase(status)) {
            return "\u5df2\u786e\u8ba4\u4e00\u81f4";
        }
        if ("SPECIAL".equalsIgnoreCase(status)) {
            return "\u7279\u6b8a\u60c5\u51b5";
        }
        if ("PENDING_LEGACY".equalsIgnoreCase(status)) {
            return "\u5f85\u6838\u5bf9\u65e7\u7cfb\u7edf";
        }
        return status == null ? "" : status;
    }

    private String workflowStatusText(String status) {
        if ("CASE_DONE".equalsIgnoreCase(status)) {
            return "\u5df2\u529e";
        }
        if ("REVIEW_PENDING".equalsIgnoreCase(status)) {
            return "\u5f85\u590d\u6838";
        }
        if ("HISTORY_READY".equalsIgnoreCase(status)) {
            return "\u53ef\u5199\u5165\u5386\u53f2";
        }
        if ("HISTORY_PREPARED".equalsIgnoreCase(status)) {
            return "\u5f85\u5199\u5165";
        }
        if ("HISTORY_WRITTEN".equalsIgnoreCase(status)) {
            return "\u5df2\u5199\u5165";
        }
        if ("HISTORY_REVIEW_PENDING".equalsIgnoreCase(status)) {
            return "\u5199\u5165\u540e\u5f85\u6838\u67e5";
        }
        if ("HISTORY_CLOSED".equalsIgnoreCase(status)) {
            return "\u5df2\u95ed\u73af";
        }
        if ("HISTORY_EXECUTED".equalsIgnoreCase(status)) {
            return "\u5df2\u6267\u884c";
        }
        if ("HISTORY_ROLLED_BACK".equalsIgnoreCase(status)) {
            return "\u5199\u5165\u5df2\u64a4\u9500";
        }
        if ("HISTORY_BLOCKED".equalsIgnoreCase(status)) {
            return "\u5199\u5165\u963b\u65ad";
        }
        if ("CASE_CANCELLED".equalsIgnoreCase(status)) {
            return "\u5df2\u64a4\u56de";
        }
        if ("DATA_GOVERNANCE_REVIEWED".equalsIgnoreCase(status)) {
            return "\u6570\u636e\u6cbb\u7406\u5df2\u6838\u67e5";
        }
        if ("DATA_GOVERNANCE_IGNORED".equalsIgnoreCase(status)) {
            return "\u6570\u636e\u6cbb\u7406\u5df2\u5ffd\u7565";
        }
        return status == null ? "" : status;
    }

    private String comparisonStatusText(String status) {
        if ("NOT_WRITTEN".equalsIgnoreCase(status)) {
            return "\u672a\u5199\u5165";
        }
        if ("MATCHED".equalsIgnoreCase(status)) {
            return "\u5df2\u4e00\u81f4";
        }
        if ("MISMATCHED".equalsIgnoreCase(status)) {
            return "\u5199\u5165\u540e\u4e0d\u4e00\u81f4";
        }
        if ("ROLLED_BACK".equalsIgnoreCase(status)) {
            return "\u5df2\u56de\u6eda";
        }
        if ("BLOCKED".equalsIgnoreCase(status)) {
            return "\u5df2\u963b\u65ad";
        }
        if ("UNKNOWN".equalsIgnoreCase(status)) {
            return "\u672a\u77e5";
        }
        return status == null ? "" : status;
    }

    private String comparisonReviewStatusText(String status) {
        if ("REVIEWED".equalsIgnoreCase(status)) {
            return "\u5df2\u6838\u67e5";
        }
        return status == null || status.isBlank() ? "\u672a\u6838\u67e5" : status;
    }

    private String comparisonRetestStatusText(String status) {
        if ("RETEST_MATCHED".equalsIgnoreCase(status)) {
            return "\u590d\u6d4b\u4e00\u81f4";
        }
        if ("RETEST_MISMATCHED".equalsIgnoreCase(status)) {
            return "\u590d\u6d4b\u4ecd\u6709\u5dee\u5f02";
        }
        if ("NOT_RETESTED".equalsIgnoreCase(status)) {
            return "\u672a\u590d\u6d4b";
        }
        return status == null || status.isBlank() ? "" : status;
    }

    private String comparisonReviewSourceText(String reason) {
        String value = reason == null ? "" : reason.trim();
        if (value.startsWith("\u6309\u5efa\u8bae\u68c0\u67e5\u65b9\u5411\u767b\u8bb0")) {
            return "\u5efa\u8bae\u5e26\u5165";
        }
        if (value.contains("\u590d\u6d4b\u5df2\u4e00\u81f4")) {
            return "\u590d\u6d4b\u901a\u8fc7";
        }
        return value.isBlank() ? "" : "\u624b\u5de5\u6838\u67e5";
    }

    private HistoryWriteWorkflow historyWriteWorkflow(WorkbenchHistoryWritePlanResponse plan) {
        if (plan.processingPriority() != null || plan.nextAction() != null || plan.nextActionCode() != null) {
            return new HistoryWriteWorkflow(
                    processingPriorityText(plan.processingPriority()),
                    plan.nextAction() == null ? "" : plan.nextAction()
            );
        }
        if (!"MISMATCHED".equalsIgnoreCase(plan.comparisonStatus())) {
            return new HistoryWriteWorkflow("\u5df2\u5b8c\u6210", "\u65e0\u9700\u6838\u67e5");
        }
        if ("REVIEWED".equalsIgnoreCase(plan.comparisonReviewStatus())) {
            return new HistoryWriteWorkflow("\u5df2\u5b8c\u6210", "\u5df2\u6838\u67e5");
        }
        if ("RETEST_MISMATCHED".equalsIgnoreCase(plan.comparisonRetestStatus())) {
            return new HistoryWriteWorkflow("\u9ad8", "\u68c0\u67e5\u57fa\u7840/\u4efb\u804c/\u5b66\u5386/\u8003\u6838\u540e\u590d\u6d4b");
        }
        if ("RETEST_MATCHED".equalsIgnoreCase(plan.comparisonRetestStatus())) {
            return new HistoryWriteWorkflow("\u4e2d", "\u6807\u8bb0\u590d\u6d4b\u901a\u8fc7");
        }
        int mismatchCount = plan.comparisonMismatchCount() == null ? 0 : plan.comparisonMismatchCount();
        if (mismatchCount >= 3) {
            return new HistoryWriteWorkflow("\u9ad8", "\u5148\u6309\u5f53\u524d\u57fa\u7840\u590d\u6d4b");
        }
        return new HistoryWriteWorkflow("\u4e2d", "\u6309\u5f53\u524d\u57fa\u7840\u590d\u6d4b");
    }

    private record HistoryWriteWorkflow(String priorityText, String nextAction) {
    }

    private record HistoryWriteMaintenanceSuggestionCsv(String directions, String fields, String reasons) {
    }

    private record HistoryClosureAcceptanceRow(String code, String title, long count, String result, String hint) {
    }

    private record HistoryDeliveryEvidenceRow(String file, String title, String count, String scope) {
    }

    private String processingPriorityText(String priority) {
        if ("HIGH".equalsIgnoreCase(priority)) {
            return "\u9ad8";
        }
        if ("MEDIUM".equalsIgnoreCase(priority)) {
            return "\u4e2d";
        }
        if ("DONE".equalsIgnoreCase(priority)) {
            return "\u5df2\u5b8c\u6210";
        }
        return priority == null ? "" : priority;
    }

    private String comparisonReviewCategoryText(String category) {
        if ("BASE_MISSING".equalsIgnoreCase(category)) {
            return "\u57fa\u7840\u4fe1\u606f\u7f3a\u5931";
        }
        if ("BASE_CHANGED".equalsIgnoreCase(category)) {
            return "\u57fa\u7840\u4fe1\u606f\u5df2\u53d8\u66f4";
        }
        if ("POLICY_DIFF".equalsIgnoreCase(category)) {
            return "\u653f\u7b56\u53d6\u503c\u5dee\u5f02";
        }
        if ("MANUAL_INPUT".equalsIgnoreCase(category)) {
            return "\u624b\u5de5\u5f55\u5165";
        }
        if ("HISTORY_SPECIAL".equalsIgnoreCase(category)) {
            return "\u5386\u53f2\u7279\u6b8a\u5904\u7406";
        }
        if ("OTHER".equalsIgnoreCase(category)) {
            return "\u5176\u4ed6";
        }
        return category == null ? "" : category;
    }

    private List<Map<String, String>> parseCsvRows(String csvText) {
        List<Map<String, String>> rows = new ArrayList<>();
        if (csvText == null || csvText.isBlank()) {
            return rows;
        }
        String normalized = csvText.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n");
        if (lines.length == 0) {
            return rows;
        }
        List<String> headers = parseCsvLine(lines[0].replace("\uFEFF", ""));
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isBlank()) {
                continue;
            }
            List<String> values = parseCsvLine(lines[i]);
            Map<String, String> row = new java.util.LinkedHashMap<>();
            for (int j = 0; j < headers.size(); j++) {
                row.put(headers.get(j), j < values.size() ? values.get(j) : "");
            }
            rows.add(row);
        }
        return rows;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values;
    }

    private byte[] withUtf8Bom(String text) {
        byte[] csvBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] body = new byte[csvBytes.length + 3];
        body[0] = (byte) 0xEF;
        body[1] = (byte) 0xBB;
        body[2] = (byte) 0xBF;
        System.arraycopy(csvBytes, 0, body, 3, csvBytes.length);
        return body;
    }

    private void addZipEntry(ZipOutputStream zip, String filename, byte[] body) throws IOException {
        zip.putNextEntry(new ZipEntry(filename));
        zip.write(body);
        zip.closeEntry();
    }

    private String latestMigrationQualitySnapshotNo(String orgCode) {
        List<Map<String, Object>> snapshots = workbenchService.migrationQualitySnapshots(orgCode, 1, false);
        return snapshots.isEmpty() ? "" : stringValue(snapshots.get(0).get("snapshotNo"));
    }

    private String period(Integer year, Integer month) {
        if (year == null) {
            return "";
        }
        return year + "-" + String.format("%02d", month == null ? 1 : month);
    }

    private String amountText(BigDecimal amount) {
        return amount == null ? "" : amount.stripTrailingZeros().toPlainString();
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private String csvValue(Object value) {
        return csv(stringValue(value));
    }

    private String html(Object value) {
        return stringValue(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
