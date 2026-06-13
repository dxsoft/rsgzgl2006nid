package com.dx.rsgzgl.system.controller;

import com.dx.rsgzgl.common.api.ApiResponse;
import com.dx.rsgzgl.system.dto.SystemAuditLogResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseCancelRequest;
import com.dx.rsgzgl.system.dto.WorkbenchCaseDetailResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseSnapshotResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchPreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteComparisonResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteExecuteResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWritePlanResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWritePreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteReviewLedgerResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteReviewRequest;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteSelectedRequest;
import com.dx.rsgzgl.system.dto.WorkbenchItemResponse;
import com.dx.rsgzgl.system.dto.WorkbenchUserStateRequest;
import com.dx.rsgzgl.system.dto.WorkbenchUserStateResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseCreateRequest;
import com.dx.rsgzgl.system.dto.WorkbenchCasePreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseReviewRequest;
import com.dx.rsgzgl.system.dto.WorkbenchItemsPageResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchExecuteResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchRetestResponse;
import com.dx.rsgzgl.system.dto.WorkbenchMetricResponse;
import com.dx.rsgzgl.system.dto.WorkbenchSummaryResponse;
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/workbench")
public class WorkbenchController {

    private final WorkbenchService workbenchService;
    private final ObjectMapper objectMapper;

    public WorkbenchController(WorkbenchService workbenchService, ObjectMapper objectMapper) {
        this.workbenchService = workbenchService;
        this.objectMapper = objectMapper;
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

    @GetMapping("/items")
    public ApiResponse<WorkbenchItemsPageResponse> items(
            @RequestParam(defaultValue = "TODO") String status,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String changeType,
            @RequestParam(defaultValue = "") String caseStatus,
            @RequestParam(defaultValue = "") String trialStatus,
            @RequestParam(defaultValue = "") String reviewStatus
    ) {
        return ApiResponse.ok(workbenchService.items(status, offset, limit, keyword, changeType, caseStatus, trialStatus, reviewStatus));
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
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ApiResponse.ok(workbenchService.historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit));
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
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ApiResponse.ok(workbenchService.batchExecuteHistoryWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit));
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
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ApiResponse.ok(workbenchService.batchRollbackHistoryWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit));
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
            @RequestParam(defaultValue = "500") int limit
    ) {
        int safeLimit = Math.min(Math.max(1, limit), 5000);
        return ApiResponse.ok(workbenchService.historyWriteReviewLedger(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, safeLimit));
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
            @RequestParam(defaultValue = "1000") int limit
    ) {
        int safeLimit = Math.min(Math.max(1, limit), 5000);
        List<WorkbenchHistoryWritePlanResponse> plans = workbenchService.exportHistoryWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, safeLimit);
        byte[] body = withUtf8Bom(toHistoryWritePlansCsv(plans));
        String normalizedStatus = status == null || status.isBlank() ? "all" : status.trim().toLowerCase();
        String filename = "history-write-plans-" + normalizedStatus + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @PostMapping("/salary-cases/{caseNo}/history-write-execute")
    public ApiResponse<WorkbenchHistoryWriteExecuteResponse> salaryCaseHistoryWriteExecute(@PathVariable String caseNo) {
        return ApiResponse.ok(workbenchService.executeHistoryWrite(caseNo));
    }

    @PostMapping("/salary-cases/{caseNo}/history-write-rollback")
    public ApiResponse<WorkbenchHistoryWriteExecuteResponse> salaryCaseHistoryWriteRollback(@PathVariable String caseNo) {
        return ApiResponse.ok(workbenchService.rollbackHistoryWrite(caseNo));
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
            @RequestParam(defaultValue = "") String caseStatus,
            @RequestParam(defaultValue = "") String trialStatus,
            @RequestParam(defaultValue = "") String reviewStatus,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        int safeLimit = Math.min(Math.max(1, limit), 5000);
        WorkbenchItemsPageResponse page = workbenchService.exportItems(status, safeLimit, keyword, changeType, caseStatus, trialStatus, reviewStatus);
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
        csv.append("\u72b6\u6001,\u8bd5\u7b97\u72b6\u6001,\u590d\u6838\u72b6\u6001,\u4e1a\u52a1\u7c7b\u578b,\u4eba\u5458\u7f16\u7801,\u59d3\u540d,\u5355\u4f4d\u7f16\u7801,\u5e74\u5ea6,\u6708\u4efd,\u6807\u9898,\u6458\u8981").append('\n');
        for (WorkbenchItemResponse item : page.items()) {
            csv.append(csv(statusText(item.status()))).append(',')
                    .append(csv(trialStatusText(item.trialStatus()))).append(',')
                    .append(csv(reviewStatusText(item.reviewStatus()))).append(',')
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

    private byte[] withUtf8Bom(String text) {
        byte[] csvBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] body = new byte[csvBytes.length + 3];
        body[0] = (byte) 0xEF;
        body[1] = (byte) 0xBB;
        body[2] = (byte) 0xBF;
        System.arraycopy(csvBytes, 0, body, 3, csvBytes.length);
        return body;
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
}
