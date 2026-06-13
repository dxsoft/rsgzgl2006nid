package com.dx.rsgzgl.system.service;

import com.dx.rsgzgl.salary.dto.NormalGradeTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeTrialResult;
import com.dx.rsgzgl.salary.dto.SalaryCalculationCommand;
import com.dx.rsgzgl.salary.dto.SalaryCalculationDetail;
import com.dx.rsgzgl.salary.dto.SalaryCalculationResult;
import com.dx.rsgzgl.salary.dto.SalaryRuleChange;
import com.dx.rsgzgl.salary.service.SalaryCalculationService;
import com.dx.rsgzgl.salary.service.NormalGradeTrialService;
import com.dx.rsgzgl.system.dto.WorkbenchCaseDetailResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseCancelRequest;
import com.dx.rsgzgl.system.dto.SystemAuditLogResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseTrialChangeResponse;
import com.dx.rsgzgl.system.dto.WorkbenchItemResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseCreateRequest;
import com.dx.rsgzgl.system.dto.WorkbenchCasePreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseReviewRequest;
import com.dx.rsgzgl.system.dto.WorkbenchCaseSnapshotItemResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseSnapshotResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchExecuteResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchPreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchRetestItemResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchRetestResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteComparisonField;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteComparisonResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteExecuteResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWritePlanResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteReviewLedgerResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWritePreviewField;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWritePreviewHistoryRow;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWritePreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteReviewRequest;
import com.dx.rsgzgl.system.dto.WorkbenchItemsPageResponse;
import com.dx.rsgzgl.system.dto.WorkbenchMetricResponse;
import com.dx.rsgzgl.system.dto.WorkbenchSummaryResponse;
import com.dx.rsgzgl.system.dto.WorkbenchUserStateResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class WorkbenchService {

    private final JdbcTemplate jdbcTemplate;
    private final OrganizationAccessService organizationAccessService;
    private final CurrentUserService currentUserService;
    private final UserPermissionService userPermissionService;
    private final SystemAuditService systemAuditService;
    private final NormalGradeTrialService normalGradeTrialService;
    private final SalaryCalculationService salaryCalculationService;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;
    private volatile boolean performanceIndexesEnsured;

    public WorkbenchService(
            JdbcTemplate jdbcTemplate,
            OrganizationAccessService organizationAccessService,
            CurrentUserService currentUserService,
            UserPermissionService userPermissionService,
            SystemAuditService systemAuditService,
            NormalGradeTrialService normalGradeTrialService,
            SalaryCalculationService salaryCalculationService,
            ObjectMapper objectMapper,
            PlatformTransactionManager transactionManager
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.organizationAccessService = organizationAccessService;
        this.currentUserService = currentUserService;
        this.userPermissionService = userPermissionService;
        this.systemAuditService = systemAuditService;
        this.normalGradeTrialService = normalGradeTrialService;
        this.salaryCalculationService = salaryCalculationService;
        this.objectMapper = objectMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public WorkbenchSummaryResponse summary() {
        ensureWorkbenchPerformanceIndexes();
        long applicationTodo = 0;
        long applicationDone = 0;
        List<WorkbenchMetricResponse> metrics = new ArrayList<>();
        if (hasMenu("APPLICATION_TODO")) {
            metrics.add(new WorkbenchMetricResponse("APPLICATION_TODO", "\u7533\u529e\u5f85\u529e", applicationTodo, "\u540e\u7eed\u63a5\u5165\u7533\u529e\u6d41\u7a0b"));
        }
        if (hasMenu("SALARY_TODO")) {
            metrics.add(new WorkbenchMetricResponse("SALARY_TODO", "\u5de5\u8d44\u53d8\u52a8\u5f85\u529e", -1, "\u57fa\u4e8e\u57fa\u7840\u4fe1\u606f\u548c\u8003\u6838\u6761\u4ef6\u63a8\u5bfc"));
        }
        if (hasMenu("APPLICATION_DONE")) {
            metrics.add(new WorkbenchMetricResponse("APPLICATION_DONE", "\u7533\u529e\u5df2\u529e", applicationDone, "\u540e\u7eed\u63a5\u5165\u7533\u529e\u6d41\u7a0b"));
        }
        if (hasMenu("SALARY_DONE")) {
            metrics.add(new WorkbenchMetricResponse("SALARY_DONE", "\u5de5\u8d44\u53d8\u52a8\u5df2\u529e", countSalaryDone(), "\u8fd1\u671f\u5386\u53f2\u5de5\u8d44\u53d8\u52a8"));
            metrics.add(new WorkbenchMetricResponse("SALARY_REVIEW_PENDING", "\u5f85\u590d\u6838\u98ce\u9669\u4e1a\u52a1", countPendingSalaryReview(), "\u8bd5\u7b97\u5dee\u5f02\u6216\u5f02\u5e38\u4e14\u5c1a\u672a\u590d\u6838"));
            metrics.add(new WorkbenchMetricResponse("SALARY_TRIAL_DIFFERENT", "\u8bd5\u7b97\u6709\u5dee\u5f02", countSalaryCaseTrialStatus("DIFFERENT"), "\u5df2\u529e\u8bb0\u5f55\u4e2d\u9700\u590d\u6838\u7684\u5dee\u5f02\u529e\u7406"));
            metrics.add(new WorkbenchMetricResponse("SALARY_TRIAL_ERROR", "\u8bd5\u7b97\u5f02\u5e38", countSalaryCaseTrialStatus("ERROR"), "\u5df2\u529e\u8bb0\u5f55\u4e2d\u5f3a\u5236\u529e\u7406\u7684\u5f02\u5e38"));
            metrics.add(new WorkbenchMetricResponse("HISTORY_PLAN_PREPARED", "\u5199\u5165\u8ba1\u5212\u5f85\u6267\u884c", countHistoryWritePlans("PREPARED"), "\u5df2\u751f\u6210\u4f46\u5c1a\u672a\u5199\u5165 hisbase"));
            metrics.add(new WorkbenchMetricResponse("HISTORY_PLAN_EXECUTED", "\u5386\u53f2\u5df2\u5199\u5165", countHistoryWritePlans("EXECUTED"), "\u5df2\u6267\u884c\u4e14\u53ef\u8ffd\u6eaf\u7684\u5199\u5165"));
            metrics.add(new WorkbenchMetricResponse("HISTORY_PLAN_ROLLED_BACK", "\u5199\u5165\u5df2\u64a4\u9500", countHistoryWritePlans("ROLLED_BACK"), "\u5df2\u64a4\u9500\u5e76\u6062\u590d sid \u94fe"));
            metrics.add(new WorkbenchMetricResponse("HISTORY_PLAN_BLOCKED", "\u5199\u5165\u5df2\u963b\u65ad", countHistoryWritePlans("BLOCKED"), "\u9884\u89c8\u6216\u6267\u884c\u9636\u6bb5\u88ab\u963b\u65ad"));
            metrics.add(new WorkbenchMetricResponse("HISTORY_PLAN_REVIEW_PENDING", "\u5199\u5165\u5f02\u5e38\u5f85\u6838\u67e5", countPendingHistoryWriteComparisonReviews(), "\u5199\u5165\u540e\u4e0d\u4e00\u81f4\u4e14\u5c1a\u672a\u6838\u67e5"));
        }
        return new WorkbenchSummaryResponse(
                metrics,
                List.of(),
                List.of()
        );
    }

    public WorkbenchMetricResponse salaryTodoMetric() {
        ensureWorkbenchPerformanceIndexes();
        if (!hasMenu("SALARY_TODO")) {
            return new WorkbenchMetricResponse("SALARY_TODO", "\u5de5\u8d44\u53d8\u52a8\u5f85\u529e", 0, "\u65e0\u5f85\u529e\u6743\u9650");
        }
        return new WorkbenchMetricResponse("SALARY_TODO", "\u5de5\u8d44\u53d8\u52a8\u5f85\u529e", countSalaryTodo(),
                salaryTodoCacheHint("\u57fa\u4e8e\u57fa\u7840\u4fe1\u606f\u548c\u8003\u6838\u6761\u4ef6\u63a8\u5bfc"));
    }

    @Transactional
    public WorkbenchMetricResponse refreshSalaryTodoCache() {
        ensureWorkbenchPerformanceIndexes();
        if (!hasMenu("SALARY_TODO")) {
            throw new IllegalArgumentException("Salary todo permission is required.");
        }
        refreshSalaryTodoCacheInternal();
        long count = countSalaryTodo();
        systemAuditService.record("workbench", "salary-todo-cache-refresh", "SALARY_TODO_CACHE", "ALL",
                "salary todo cache count=" + count);
        return new WorkbenchMetricResponse("SALARY_TODO", "\u5de5\u8d44\u53d8\u52a8\u5f85\u529e", count,
                salaryTodoCacheHint("\u5f85\u529e\u7f13\u5b58\u5df2\u5237\u65b0"));
    }

    @Transactional
    public WorkbenchMetricResponse markSalaryTodoCacheDirty() {
        ensureWorkbenchPerformanceIndexes();
        if (!hasMenu("SALARY_TODO")) {
            throw new IllegalArgumentException("Salary todo permission is required.");
        }
        ensureSalaryTodoCacheLoaded();
        markSalaryTodoCacheDirtyInternal("salary todo cache marked dirty");
        long count = countSalaryTodo();
        return new WorkbenchMetricResponse("SALARY_TODO", "\u5de5\u8d44\u53d8\u52a8\u5f85\u529e", count,
                salaryTodoCacheHint("\u5f85\u529e\u7f13\u5b58\u9700\u5237\u65b0"));
    }

    @Transactional
    public void markSalaryTodoCacheDirtyForDataChange(String summary) {
        ensureSalaryTodoCacheTable();
        if (!salaryTodoCacheReady()) {
            return;
        }
        markSalaryTodoCacheDirtyInternal(summary);
    }

    public WorkbenchUserStateResponse userState(String stateKey) {
        ensureUserWorkStateTable();
        String username = requireCurrentUsername();
        String safeKey = safeUserStateKey(stateKey);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT state_json
                FROM sys_user_work_state
                WHERE username = ? AND state_key = ?
                LIMIT 1
                """, username, safeKey);
        if (rows.isEmpty()) {
            return new WorkbenchUserStateResponse(safeKey, Map.of());
        }
        String stateJson = text(rows.getFirst().get("state_json"));
        if (stateJson.isBlank()) {
            return new WorkbenchUserStateResponse(safeKey, Map.of());
        }
        try {
            Map<String, Object> state = objectMapper.readValue(stateJson, new TypeReference<>() {
            });
            return new WorkbenchUserStateResponse(safeKey, state == null ? Map.of() : state);
        } catch (JsonProcessingException ex) {
            return new WorkbenchUserStateResponse(safeKey, Map.of());
        }
    }

    public WorkbenchUserStateResponse saveUserState(String stateKey, Map<String, Object> state) {
        ensureUserWorkStateTable();
        String username = requireCurrentUsername();
        String safeKey = safeUserStateKey(stateKey);
        Map<String, Object> safeState = state == null ? Map.of() : state;
        try {
            String stateJson = objectMapper.writeValueAsString(safeState);
            jdbcTemplate.update("""
                    INSERT INTO sys_user_work_state(username, state_key, state_json, updated_at)
                    VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                    ON DUPLICATE KEY UPDATE state_json = VALUES(state_json), updated_at = CURRENT_TIMESTAMP
                    """, username, safeKey, stateJson);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("User work state is not valid JSON.");
        }
        return new WorkbenchUserStateResponse(safeKey, safeState);
    }

    public WorkbenchUserStateResponse deleteUserState(String stateKey) {
        ensureUserWorkStateTable();
        String username = requireCurrentUsername();
        String safeKey = safeUserStateKey(stateKey);
        jdbcTemplate.update("""
                DELETE FROM sys_user_work_state
                WHERE username = ? AND state_key = ?
                """, username, safeKey);
        return new WorkbenchUserStateResponse(safeKey, Map.of());
    }

    public WorkbenchItemsPageResponse items(String status, int offset, int limit, String keyword, String changeType, String caseStatus, String trialStatus, String reviewStatus) {
        ensureWorkbenchPerformanceIndexes();
        int safeOffset = Math.max(0, offset);
        int safeLimit = Math.min(Math.max(1, limit), 100);
        if ("DONE".equalsIgnoreCase(status)) {
            if (!hasMenu("SALARY_DONE")) {
                return new WorkbenchItemsPageResponse(0, safeOffset, safeLimit, List.of());
            }
            return new WorkbenchItemsPageResponse(
                    countSalaryDone(keyword, changeType, caseStatus, trialStatus, reviewStatus),
                    safeOffset,
                    safeLimit,
                salaryDoneItems(safeOffset, safeLimit, keyword, changeType, caseStatus, trialStatus, reviewStatus)
            );
        }
        if (!hasMenu("SALARY_TODO")) {
            return new WorkbenchItemsPageResponse(0, safeOffset, safeLimit, List.of());
        }
        return salaryTodoPage(safeOffset, safeLimit, keyword, changeType);
    }

    public WorkbenchItemsPageResponse exportItems(String status, int limit, String keyword, String changeType, String caseStatus, String trialStatus, String reviewStatus) {
        ensureWorkbenchPerformanceIndexes();
        int safeLimit = Math.min(Math.max(1, limit), 5000);
        if ("DONE".equalsIgnoreCase(status)) {
            if (!hasMenu("SALARY_DONE")) {
                return new WorkbenchItemsPageResponse(0, 0, safeLimit, List.of());
            }
            List<WorkbenchItemResponse> items = salaryDoneItems(0, safeLimit, keyword, changeType, caseStatus, trialStatus, reviewStatus);
            return new WorkbenchItemsPageResponse(items.size(), 0, safeLimit, items);
        }
        if (!hasMenu("SALARY_TODO")) {
            return new WorkbenchItemsPageResponse(0, 0, safeLimit, List.of());
        }
        List<WorkbenchItemResponse> items = salaryTodoItems(0, safeLimit, keyword, changeType);
        return new WorkbenchItemsPageResponse(items.size(), 0, safeLimit, items);
    }

    public WorkbenchCaseDetailResponse caseDetail(String caseNo) {
        ensureBusinessCaseTable();
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        String safeCaseNo = text(caseNo);
        if (safeCaseNo.isBlank()) {
            throw new IllegalArgumentException("Salary business case number is required.");
        }
        Map<String, Object> row = businessCaseRow(safeCaseNo);
        organizationAccessService.requireOrgAccess(text(row.get("org_code")));
        String trialStatus = text(row.get("trial_status"));
        String reviewStatus = text(row.get("review_status"));
        if (reviewStatus.isBlank() && ("DIFFERENT".equals(trialStatus) || "ERROR".equals(trialStatus))) {
            reviewStatus = "PENDING";
        }
        Map<String, Object> snapshot = businessCaseSnapshotRow(text(row.get("work_item_id")));
        ensureHistoryWritePlanTable();
        Map<String, Object> historyWritePlanRow = historyWritePlanRowIfExists(safeCaseNo);
        WorkbenchHistoryWritePlanResponse historyWritePlan = historyWritePlanRow == null || historyWritePlanRow.isEmpty()
                ? null
                : historyWritePlanResponse(historyWritePlanRow);
        return new WorkbenchCaseDetailResponse(
                text(row.get("case_no")),
                text(row.get("work_item_id")),
                text(row.get("source")),
                text(row.get("status")),
                text(row.get("business_type")),
                text(row.get("person_code")),
                text(row.get("person_name")),
                text(row.get("org_code")),
                number(row.get("event_year")),
                number(row.get("event_month")),
                text(row.get("title")),
                text(row.get("summary")),
                trialStatus,
                booleanValue(row.get("trial_matched")),
                decimal(row.get("trial_baseline_total")),
                decimal(row.get("trial_calculated_total")),
                decimal(row.get("trial_expected_total")),
                decimal(row.get("trial_difference")),
                text(row.get("trial_summary")),
                trialChanges(text(row.get("trial_changes_json"))),
                text(row.get("force_reason")),
                text(row.get("difference_reason")),
                text(row.get("cancel_reason")),
                reviewStatus,
                text(row.get("review_reason")),
                text(row.get("reviewed_by")),
                text(row.get("reviewed_at")),
                !snapshot.isEmpty(),
                text(snapshot.get("snapshot_by")),
                text(snapshot.get("snapshot_at")),
                historyWritePlan,
                historyWriteAudits(text(row.get("case_no"))),
                caseAudits(text(row.get("case_no"))),
                text(row.get("handled_by")),
                text(row.get("handled_at"))
        );
    }

    private List<SystemAuditLogResponse> caseAudits(String caseNo) {
        return caseAudits(caseNo, false);
    }

    private List<SystemAuditLogResponse> historyWriteAudits(String caseNo) {
        return caseAudits(caseNo, true);
    }

    private List<SystemAuditLogResponse> caseAudits(String caseNo, boolean historyWriteOnly) {
        systemAuditService.ensureTable();
        return jdbcTemplate.query("""
                SELECT CONCAT('SYS-', id) AS audit_id,
                       module_name,
                       action_name,
                       target_type,
                       target_code,
                       summary,
                       operator,
                       created_at
                FROM sys_audit_log
                WHERE target_type = 'SALARY_CASE'
                  AND target_code = ?
                  AND (? = 0 OR action_name LIKE 'history-write%')
                ORDER BY created_at DESC, id DESC
                LIMIT 20
                """, (rs, rowNum) -> new SystemAuditLogResponse(
                rs.getString("audit_id"),
                rs.getString("module_name"),
                rs.getString("action_name"),
                rs.getString("target_type"),
                rs.getString("target_code"),
                rs.getString("summary"),
                rs.getString("operator"),
                rs.getTimestamp("created_at").toLocalDateTime().toString()
        ), caseNo, historyWriteOnly ? 1 : 0);
    }

    public WorkbenchCaseSnapshotResponse caseSnapshot(String caseNo) {
        ensureBusinessCaseTable();
        ensureBusinessCaseSnapshotTable();
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        String safeCaseNo = text(caseNo);
        if (safeCaseNo.isBlank()) {
            throw new IllegalArgumentException("Salary business case number is required.");
        }
        Map<String, Object> businessCase = businessCaseRow(safeCaseNo);
        organizationAccessService.requireOrgAccess(text(businessCase.get("org_code")));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT case_no,
                       work_item_id,
                       person_code,
                       org_code,
                       event_year,
                       event_month,
                       business_type,
                       trial_status,
                       trial_matched,
                       trial_difference,
                       trial_baseline_total,
                       trial_calculated_total,
                       trial_expected_total,
                       trial_changes_json,
                       salary_items_json,
                       snapshot_json,
                       snapshot_by,
                       snapshot_at
                FROM salary_business_case_snapshot
                WHERE work_item_id = ?
                LIMIT 1
                """, text(businessCase.get("work_item_id")));
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Salary business case snapshot not found: " + safeCaseNo);
        }
        Map<String, Object> row = rows.getFirst();
        return new WorkbenchCaseSnapshotResponse(
                text(row.get("case_no")),
                text(row.get("work_item_id")),
                text(row.get("person_code")),
                text(row.get("org_code")),
                number(row.get("event_year")),
                number(row.get("event_month")),
                text(row.get("business_type")),
                text(row.get("trial_status")),
                booleanValue(row.get("trial_matched")),
                decimal(row.get("trial_baseline_total")),
                decimal(row.get("trial_calculated_total")),
                decimal(row.get("trial_expected_total")),
                decimal(row.get("trial_difference")),
                trialChanges(text(row.get("trial_changes_json"))),
                snapshotSalaryItems(text(row.get("salary_items_json"))),
                text(row.get("snapshot_json")),
                text(row.get("snapshot_by")),
                text(row.get("snapshot_at"))
        );
    }

    public WorkbenchHistoryWritePreviewResponse historyWritePreview(String caseNo) {
        WorkbenchCaseSnapshotResponse snapshot = caseSnapshot(caseNo);
        ensureHistoryWritePlanTable();
        Map<String, Object> businessCase = businessCaseRow(snapshot.caseNo());
        Map<String, Object> existingPlan = historyWritePlanRowIfExists(snapshot.caseNo());
        List<String> issues = new ArrayList<>();
        String existingPlanStatus = existingPlan == null ? "" : text(existingPlan.get("plan_status"));
        String existingPlanResult = existingPlan == null ? "" : text(existingPlan.get("execution_result"));
        if ("EXECUTED".equalsIgnoreCase(existingPlanStatus) && "SUCCESS".equalsIgnoreCase(existingPlanResult)) {
            issues.add("BLOCKED: history write plan has already been executed.");
        }
        if ("ROLLED_BACK".equalsIgnoreCase(existingPlanStatus)) {
            issues.add("BLOCKED: rolled back history write plans cannot be executed again.");
        }
        String caseStatus = text(businessCase.get("status"));
        String reviewStatus = text(businessCase.get("review_status"));
        if (!"DONE".equalsIgnoreCase(caseStatus)) {
            issues.add("BLOCKED: only DONE salary business cases can be written to history.");
        }
        if (("DIFFERENT".equalsIgnoreCase(snapshot.trialStatus()) || "ERROR".equalsIgnoreCase(snapshot.trialStatus()))
                && !"REVIEWED".equalsIgnoreCase(reviewStatus)) {
            issues.add("BLOCKED: trial risk must be reviewed before writing history.");
        }
        if (snapshot.year() == null || snapshot.month() == null || snapshot.month() < 1 || snapshot.month() > 12) {
            issues.add("BLOCKED: snapshot year/month is incomplete.");
        }
        if (snapshot.trialCalculatedTotal() == null) {
            issues.add("WARNING: snapshot calculated total is empty.");
        }
        String existingHistoryId = existingHistoryId(snapshot);
        if (!existingHistoryId.isBlank()) {
            issues.add("BLOCKED: same person, period, and change type already exists in hisbase.");
        }
        WorkbenchHistoryWritePreviewHistoryRow previous = adjacentHistory(snapshot, true);
        WorkbenchHistoryWritePreviewHistoryRow next = adjacentHistory(snapshot, false);
        validateSidChain(snapshot, previous, next, issues);
        List<WorkbenchHistoryWritePreviewField> fields = previewMappedFields(snapshot, issues);
        boolean blocked = issues.stream().anyMatch(issue -> issue.startsWith("BLOCKED"));
        String status = blocked ? "BLOCKED" : (issues.isEmpty() ? "READY" : "WARNING");
        boolean sidUpdateRequired = previous != null || next != null;
        String sidPlan = sidPlan(previous, next);
        String writePlanId = historyWritePlanId(snapshot.caseNo());
        WorkbenchHistoryWritePreviewResponse response = new WorkbenchHistoryWritePreviewResponse(
                snapshot.caseNo(),
                snapshot.workItemId(),
                snapshot.personCode(),
                snapshot.orgCode(),
                snapshot.year(),
                snapshot.month(),
                snapshot.businessType(),
                status,
                !blocked,
                writePlanId,
                existingHistoryId,
                previous,
                next,
                sidUpdateRequired,
                sidPlan,
                fields,
                issues
        );
        persistHistoryWritePlan(response);
        return response;
    }

    public List<WorkbenchHistoryWritePlanResponse> historyWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit) {
        return historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit, 200);
    }

    private List<WorkbenchHistoryWritePlanResponse> historyWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit, int maxLimit) {
        ensureHistoryWritePlanTable();
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        int safeLimit = Math.min(Math.max(1, limit), Math.max(1, maxLimit));
        String safeStatus = text(status).toUpperCase();
        String safeComparisonStatus = text(comparisonStatus).toUpperCase();
        String safeReviewStatus = text(reviewStatus).toUpperCase();
        String safeKeyword = text(keyword);
        String safeMismatchField = text(mismatchField);
        String safeMaintenanceTarget = normalizeMaintenanceTarget(maintenanceTarget);
        String safeRetestStatus = normalizeHistoryWriteRetestStatus(retestStatus);
        String safePriority = normalizeHistoryWritePriority(priority);
        String safeActionCode = normalizeHistoryWriteActionCode(actionCode);
        int queryLimit = safeComparisonStatus.isBlank() && safeReviewStatus.isBlank() && safeMismatchField.isBlank() && safeMaintenanceTarget.isBlank() && safeRetestStatus.isBlank() && safePriority.isBlank() && safeActionCode.isBlank()
                ? safeLimit
                : Math.min(5000, Math.max(safeLimit * 10, 500));
        return jdbcTemplate.queryForList("""
                SELECT *
                FROM salary_history_write_plan p
                WHERE __ORG_ACCESS__
                  AND (? = '' OR UPPER(TRIM(p.plan_status)) = ? OR UPPER(TRIM(COALESCE(p.execution_result, ''))) = ?)
                  AND (? = ''
                       OR p.plan_no LIKE CONCAT('%', ?, '%')
                       OR p.case_no LIKE CONCAT('%', ?, '%')
                       OR p.work_item_id LIKE CONCAT('%', ?, '%')
                       OR p.person_code LIKE CONCAT('%', ?, '%')
                       OR p.business_type LIKE CONCAT('%', ?, '%')
                       OR p.comparison_review_category LIKE CONCAT('%', ?, '%')
                       OR p.comparison_review_reason LIKE CONCAT('%', ?, '%'))
                ORDER BY p.prepared_at DESC, p.id DESC
                LIMIT ?
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("p.org_code")),
                safeStatus, safeStatus, safeStatus,
                safeKeyword, safeKeyword, safeKeyword, safeKeyword, safeKeyword, safeKeyword, safeKeyword, safeKeyword,
                queryLimit).stream()
                .map(this::historyWritePlanResponse)
                .filter(plan -> safeComparisonStatus.isBlank() || safeComparisonStatus.equalsIgnoreCase(text(plan.comparisonStatus())))
                .filter(plan -> historyWriteReviewStatusMatches(plan, safeReviewStatus))
                .filter(plan -> historyWriteMismatchFieldMatches(plan, safeMismatchField))
                .filter(plan -> historyWriteMaintenanceTargetMatches(plan, safeMaintenanceTarget))
                .filter(plan -> safeRetestStatus.isBlank() || safeRetestStatus.equalsIgnoreCase(text(plan.comparisonRetestStatus())))
                .filter(plan -> safePriority.isBlank() || safePriority.equalsIgnoreCase(text(plan.processingPriority())))
                .filter(plan -> safeActionCode.isBlank() || safeActionCode.equalsIgnoreCase(text(plan.nextActionCode())))
                .limit(safeLimit)
                .toList();
    }

    public List<WorkbenchHistoryWritePlanResponse> exportHistoryWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit) {
        if (!hasMenu("SALARY_EXPORT")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Salary export permission is required.");
        }
        return historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit, 5000);
    }

    private boolean historyWriteReviewStatusMatches(WorkbenchHistoryWritePlanResponse plan, String reviewStatus) {
        if (reviewStatus.isBlank()) {
            return true;
        }
        String actual = text(plan.comparisonReviewStatus());
        if ("PENDING".equalsIgnoreCase(reviewStatus)) {
            return actual.isBlank() || "PENDING".equalsIgnoreCase(actual);
        }
        return reviewStatus.equalsIgnoreCase(actual);
    }

    private boolean historyWriteMismatchFieldMatches(WorkbenchHistoryWritePlanResponse plan, String mismatchField) {
        String safeMismatchField = text(mismatchField);
        if (safeMismatchField.isBlank()) {
            return true;
        }
        if (!"MISMATCHED".equalsIgnoreCase(text(plan.comparisonStatus())) || text(plan.caseNo()).isBlank()) {
            return false;
        }
        try {
            WorkbenchHistoryWriteComparisonResponse comparison = historyWriteComparison(plan.caseNo());
            for (WorkbenchHistoryWriteComparisonField field : comparison.fields()) {
                if (Boolean.TRUE.equals(field.matched())) {
                    continue;
                }
                if (safeMismatchField.equalsIgnoreCase(text(field.historyField()))
                        || safeMismatchField.equalsIgnoreCase(text(field.itemCode()))
                        || safeMismatchField.equalsIgnoreCase(text(field.itemName()))) {
                    return true;
                }
            }
        } catch (RuntimeException ignored) {
            return false;
        }
        return false;
    }

    private boolean historyWriteMaintenanceTargetMatches(WorkbenchHistoryWritePlanResponse plan, String maintenanceTarget) {
        String safeTarget = normalizeMaintenanceTarget(maintenanceTarget);
        if (safeTarget.isBlank()) {
            return true;
        }
        try {
            List<Map<String, Object>> rows = objectMapper.readValue(text(plan.maintenanceSuggestionJson()), new TypeReference<>() {
            });
            return rows.stream().anyMatch(row -> safeTarget.equalsIgnoreCase(text(row.get("target"))));
        } catch (JsonProcessingException | RuntimeException ex) {
            return false;
        }
    }

    private String normalizeMaintenanceTarget(String value) {
        String target = text(value).toLowerCase();
        return switch (target) {
            case "base", "post", "education", "assessment", "standard", "other" -> target;
            default -> "";
        };
    }

    public WorkbenchHistoryWriteReviewLedgerResponse historyWriteReviewLedger(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit) {
        List<WorkbenchHistoryWritePlanResponse> plans = historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit, 5000);
        Map<String, LedgerCounter> orgs = new LinkedHashMap<>();
        Map<String, LedgerCounter> businessTypes = new LinkedHashMap<>();
        Map<String, LedgerCounter> reviewStatuses = new LinkedHashMap<>();
        Map<String, LedgerCounter> reviewCategories = new LinkedHashMap<>();
        Map<String, LedgerCounter> retestStatuses = new LinkedHashMap<>();
        Map<String, LedgerCounter> reviewSources = new LinkedHashMap<>();
        Map<String, LedgerCounter> maintenanceTargets = new LinkedHashMap<>();
        Map<String, LedgerCounter> priorities = new LinkedHashMap<>();
        Map<String, LedgerCounter> nextActions = new LinkedHashMap<>();
        Map<String, FieldCounter> fields = new LinkedHashMap<>();
        int pending = 0;
        int reviewed = 0;
        int matched = 0;
        int mismatched = 0;
        int retested = 0;
        int retestMatched = 0;
        int retestMismatched = 0;
        int suggestedReviewed = 0;
        int retestReviewed = 0;
        int manualReviewed = 0;
        int pendingRetestFirst = 0;
        int pendingMaintainAndRetest = 0;
        int highPriority = 0;
        int mediumPriority = 0;
        int donePriority = 0;
        for (WorkbenchHistoryWritePlanResponse plan : plans) {
            String actualReviewStatus = historyWriteActualReviewStatus(plan);
            String actualRetestStatus = text(plan.comparisonRetestStatus());
            String actualReviewSource = historyWriteReviewSource(plan);
            String actualPriority = text(plan.processingPriority());
            String actualNextAction = text(plan.nextActionCode());
            boolean isReviewed = "REVIEWED".equals(actualReviewStatus);
            pending += "PENDING".equals(actualReviewStatus) ? 1 : 0;
            reviewed += isReviewed ? 1 : 0;
            suggestedReviewed += isReviewed && "SUGGESTED".equals(actualReviewSource) ? 1 : 0;
            retestReviewed += isReviewed && "RETEST".equals(actualReviewSource) ? 1 : 0;
            manualReviewed += isReviewed && "MANUAL".equals(actualReviewSource) ? 1 : 0;
            matched += "MATCHED".equalsIgnoreCase(text(plan.comparisonStatus())) ? 1 : 0;
            boolean isMismatched = "MISMATCHED".equalsIgnoreCase(text(plan.comparisonStatus()));
            mismatched += isMismatched ? 1 : 0;
            retested += "NOT_RETESTED".equalsIgnoreCase(actualRetestStatus) ? 0 : 1;
            retestMatched += "RETEST_MATCHED".equalsIgnoreCase(actualRetestStatus) ? 1 : 0;
            retestMismatched += "RETEST_MISMATCHED".equalsIgnoreCase(actualRetestStatus) ? 1 : 0;
            pendingRetestFirst += "PENDING".equals(actualReviewStatus) && "RETEST_FIRST".equalsIgnoreCase(actualNextAction) ? 1 : 0;
            pendingMaintainAndRetest += "PENDING".equals(actualReviewStatus) && "MAINTAIN_AND_RETEST".equalsIgnoreCase(actualNextAction) ? 1 : 0;
            highPriority += "HIGH".equalsIgnoreCase(actualPriority) ? 1 : 0;
            mediumPriority += "MEDIUM".equalsIgnoreCase(actualPriority) ? 1 : 0;
            donePriority += "DONE".equalsIgnoreCase(actualPriority) ? 1 : 0;
            addLedgerCounter(orgs, text(plan.orgCode()), actualReviewStatus);
            addLedgerCounter(businessTypes, text(plan.businessType()), actualReviewStatus);
            addLedgerCounter(reviewStatuses, actualReviewStatus, actualReviewStatus);
            addLedgerCounter(retestStatuses, actualRetestStatus, actualReviewStatus);
            addLedgerCounter(priorities, actualPriority, actualReviewStatus);
            addLedgerCounter(nextActions, actualNextAction, actualReviewStatus);
            addMaintenanceTargetCounters(maintenanceTargets, plan, actualReviewStatus);
            if ("REVIEWED".equals(actualReviewStatus)) {
                addLedgerCounter(reviewCategories, text(plan.comparisonReviewCategory()), actualReviewStatus);
                addLedgerCounter(reviewSources, actualReviewSource, actualReviewStatus);
            }
            if (isMismatched && !text(plan.caseNo()).isBlank()) {
                addMismatchFields(fields, plan.caseNo());
            }
        }
        return new WorkbenchHistoryWriteReviewLedgerResponse(
                plans.size(),
                pending,
                reviewed,
                matched,
                mismatched,
                retested,
                retestMatched,
                retestMismatched,
                suggestedReviewed,
                retestReviewed,
                manualReviewed,
                pendingRetestFirst,
                pendingMaintainAndRetest,
                highPriority,
                mediumPriority,
                donePriority,
                ledgerGroups(orgs, 8),
                ledgerGroups(businessTypes, 8),
                ledgerGroups(reviewStatuses, 4),
                ledgerGroups(reviewCategories, 8),
                ledgerGroups(retestStatuses, 4),
                ledgerGroups(reviewSources, 4),
                ledgerGroups(maintenanceTargets, 6),
                ledgerGroups(priorities, 4),
                ledgerGroups(nextActions, 6),
                fieldGroups(fields, 8)
        );
    }

    private String historyWriteActualReviewStatus(WorkbenchHistoryWritePlanResponse plan) {
        if (!"MISMATCHED".equalsIgnoreCase(text(plan.comparisonStatus()))) {
            return "NOT_REQUIRED";
        }
        String reviewStatus = text(plan.comparisonReviewStatus()).toUpperCase();
        return reviewStatus.isBlank() ? "PENDING" : reviewStatus;
    }

    private String historyWriteReviewSource(WorkbenchHistoryWritePlanResponse plan) {
        String reason = text(plan.comparisonReviewReason());
        if (reason.startsWith("按建议检查方向登记")) {
            return "SUGGESTED";
        }
        if (reason.contains("复测已一致")) {
            return "RETEST";
        }
        return "MANUAL";
    }

    private void addLedgerCounter(Map<String, LedgerCounter> counters, String key, String reviewStatus) {
        String safeKey = key.isBlank() ? "-" : key;
        LedgerCounter counter = counters.computeIfAbsent(safeKey, LedgerCounter::new);
        counter.count++;
        if ("REVIEWED".equalsIgnoreCase(reviewStatus)) {
            counter.reviewed++;
        } else if ("PENDING".equalsIgnoreCase(reviewStatus)) {
            counter.pending++;
        }
    }

    private void addMaintenanceTargetCounters(Map<String, LedgerCounter> counters, WorkbenchHistoryWritePlanResponse plan, String reviewStatus) {
        try {
            List<Map<String, Object>> rows = objectMapper.readValue(text(plan.maintenanceSuggestionJson()), new TypeReference<>() {
            });
            Set<String> targets = new HashSet<>();
            for (Map<String, Object> row : rows) {
                String target = normalizeMaintenanceTarget(text(row.get("target")));
                if (!target.isBlank()) {
                    targets.add(target);
                }
            }
            for (String target : targets) {
                addLedgerCounter(counters, target, reviewStatus);
            }
        } catch (JsonProcessingException | RuntimeException ignored) {
            // Suggestion grouping is best-effort; plan rows still appear in the ledger.
        }
    }

    private List<WorkbenchHistoryWriteReviewLedgerResponse.Group> ledgerGroups(Map<String, LedgerCounter> counters, int limit) {
        return counters.values().stream()
                .sorted(Comparator.comparingInt((LedgerCounter counter) -> counter.count).reversed())
                .limit(limit)
                .map(counter -> new WorkbenchHistoryWriteReviewLedgerResponse.Group(
                        counter.key,
                        counter.key,
                        counter.count,
                        counter.pending,
                        counter.reviewed
                ))
                .toList();
    }

    private void addMismatchFields(Map<String, FieldCounter> fields, String caseNo) {
        try {
            WorkbenchHistoryWriteComparisonResponse comparison = historyWriteComparison(caseNo);
            for (WorkbenchHistoryWriteComparisonField field : comparison.fields()) {
                if (!Boolean.TRUE.equals(field.matched())) {
                    String key = text(field.historyField());
                    if (key.isBlank()) {
                        key = text(field.itemCode());
                    }
                    if (key.isBlank()) {
                        key = "-";
                    }
                    FieldCounter counter = fields.computeIfAbsent(key, ignored -> new FieldCounter(
                            text(field.itemCode()),
                            text(field.itemName()),
                            text(field.historyField())
                    ));
                    counter.count++;
                }
            }
        } catch (RuntimeException ignored) {
            // A broken comparison should not hide the ledger; the plan row still appears in the list.
        }
    }

    private List<WorkbenchHistoryWriteReviewLedgerResponse.FieldGroup> fieldGroups(Map<String, FieldCounter> counters, int limit) {
        return counters.values().stream()
                .sorted(Comparator.comparingInt((FieldCounter counter) -> counter.count).reversed())
                .limit(limit)
                .map(counter -> new WorkbenchHistoryWriteReviewLedgerResponse.FieldGroup(
                        counter.itemCode,
                        counter.itemName,
                        counter.historyField,
                        counter.count
                ))
                .toList();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String safeValue = text(value);
            if (!safeValue.isBlank()) {
                return safeValue;
            }
        }
        return "";
    }

    private static final class LedgerCounter {
        private final String key;
        private int count;
        private int pending;
        private int reviewed;

        private LedgerCounter(String key) {
            this.key = key;
        }
    }

    private static final class FieldCounter {
        private final String itemCode;
        private final String itemName;
        private final String historyField;
        private int count;

        private FieldCounter(String itemCode, String itemName, String historyField) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.historyField = historyField;
        }
    }

    private record MaintenanceSuggestion(String target, String label, String reason) {
    }

    private static final class MaintenanceSuggestionCounter {
        private final MaintenanceSuggestion suggestion;
        private int count;
        private final List<String> fields = new ArrayList<>();

        private MaintenanceSuggestionCounter(MaintenanceSuggestion suggestion) {
            this.suggestion = suggestion;
        }
    }

    public WorkbenchHistoryWriteBatchPreviewResponse batchPreviewHistoryWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit) {
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        List<WorkbenchHistoryWritePlanResponse> plans = historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit);
        List<WorkbenchHistoryWritePreviewResponse> previews = plans.stream()
                .map(WorkbenchHistoryWritePlanResponse::caseNo)
                .filter(caseNo -> !text(caseNo).isBlank())
                .map(this::historyWritePreview)
                .toList();
        int ready = (int) previews.stream().filter(preview -> "READY".equalsIgnoreCase(text(preview.status()))).count();
        int blocked = (int) previews.stream().filter(preview -> "BLOCKED".equalsIgnoreCase(text(preview.status()))).count();
        int warning = (int) previews.stream().filter(preview -> "WARNING".equalsIgnoreCase(text(preview.status()))).count();
        return new WorkbenchHistoryWriteBatchPreviewResponse(previews.size(), ready, blocked, warning, previews);
    }

    public WorkbenchHistoryWriteBatchExecuteResponse batchExecuteHistoryWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit) {
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        WorkbenchHistoryWriteBatchPreviewResponse preview = batchPreviewHistoryWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit);
        List<WorkbenchHistoryWriteExecuteResponse> results = new ArrayList<>();
        int success = 0;
        int failed = 0;
        int skipped = 0;
        for (WorkbenchHistoryWritePreviewResponse item : preview.items()) {
            if (!Boolean.TRUE.equals(item.writable()) || !"READY".equalsIgnoreCase(text(item.status()))) {
                skipped += 1;
                results.add(new WorkbenchHistoryWriteExecuteResponse(
                        item.caseNo(),
                        item.workItemId(),
                        item.personCode(),
                        item.orgCode(),
                        item.writePlanId(),
                        "",
                        "SKIPPED",
                        item.sidUpdateRequired(),
                        "Skipped because preview status is " + text(item.status())
                ));
                continue;
            }
            try {
                WorkbenchHistoryWriteExecuteResponse executed = transactionTemplate.execute(transactionStatus -> executeHistoryWrite(item.caseNo()));
                if (executed == null) {
                    failed += 1;
                    results.add(batchFailedResult(item, "History write returned no result."));
                } else {
                    success += 1;
                    results.add(executed);
                    systemAuditService.record("workbench", "history-write-batch-execute", "SALARY_CASE", executed.caseNo(),
                            executed.personCode() + " historyId=" + executed.historyId());
                }
            } catch (RuntimeException ex) {
                failed += 1;
                results.add(batchFailedResult(item, ex.getMessage()));
            }
        }
        systemAuditService.record("workbench", "history-write-batch-execute", "SALARY_CASE", "BATCH",
                "total=" + preview.total() + ", success=" + success + ", failed=" + failed + ", skipped=" + skipped);
        return new WorkbenchHistoryWriteBatchExecuteResponse(preview.total(), success, failed, skipped, results);
    }

    public WorkbenchHistoryWriteBatchRetestResponse batchRetestHistoryWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit) {
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        List<WorkbenchHistoryWritePlanResponse> plans = historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit);
        return batchRetestHistoryWritePlans(plans, "history-write-batch-retest-preview");
    }

    public WorkbenchHistoryWriteBatchRetestResponse batchRetestSelectedHistoryWritePlans(List<String> caseNos) {
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        List<WorkbenchHistoryWritePlanResponse> plans = selectedHistoryWritePlans(caseNos);
        return batchRetestHistoryWritePlans(plans, "history-write-selected-retest-preview");
    }

    private WorkbenchHistoryWriteBatchRetestResponse batchRetestHistoryWritePlans(List<WorkbenchHistoryWritePlanResponse> plans, String auditAction) {
        List<WorkbenchHistoryWriteBatchRetestItemResponse> items = new ArrayList<>();
        int matched = 0;
        int mismatched = 0;
        int failed = 0;
        for (WorkbenchHistoryWritePlanResponse plan : plans) {
            try {
                WorkbenchHistoryWriteComparisonResponse comparison = retestHistoryWriteComparison(plan.caseNo(), false);
                int mismatchCount = (int) comparison.fields().stream()
                        .filter(field -> !Boolean.TRUE.equals(field.matched()))
                        .count();
                boolean allMatched = Boolean.TRUE.equals(comparison.totalMatched()) && mismatchCount == 0;
                matched += allMatched ? 1 : 0;
                mismatched += allMatched ? 0 : 1;
                items.add(new WorkbenchHistoryWriteBatchRetestItemResponse(
                        comparison.caseNo(),
                        comparison.personCode(),
                        comparison.orgCode(),
                        comparison.businessType(),
                        allMatched ? "MATCHED" : "MISMATCHED",
                        comparison.totalMatched(),
                        mismatchCount,
                        allMatched ? "\u5f53\u524d\u57fa\u7840\u590d\u6d4b\u4e00\u81f4" : "\u5f53\u524d\u57fa\u7840\u590d\u6d4b\u4ecd\u6709\u5dee\u5f02"
                ));
            } catch (RuntimeException ex) {
                failed += 1;
                items.add(new WorkbenchHistoryWriteBatchRetestItemResponse(
                        plan.caseNo(),
                        plan.personCode(),
                        plan.orgCode(),
                        plan.businessType(),
                        "FAILED",
                        false,
                        0,
                        ex.getMessage()
                ));
            }
        }
        systemAuditService.record("workbench", auditAction, "SALARY_CASE", "BATCH",
                "total=" + plans.size() + " matched=" + matched + " mismatched=" + mismatched + " failed=" + failed);
        return new WorkbenchHistoryWriteBatchRetestResponse(plans.size(), matched, mismatched, failed, items);
    }

    public WorkbenchHistoryWriteBatchExecuteResponse batchApproveRetestPassedHistoryWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit) {
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        List<WorkbenchHistoryWritePlanResponse> plans = historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit);
        return batchApproveRetestPassedHistoryWritePlans(plans, "history-write-batch-retest-approve");
    }

    public WorkbenchHistoryWriteBatchExecuteResponse batchApproveRetestPassedSelectedHistoryWritePlans(List<String> caseNos) {
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        List<WorkbenchHistoryWritePlanResponse> plans = selectedHistoryWritePlans(caseNos);
        return batchApproveRetestPassedHistoryWritePlans(plans, "history-write-selected-retest-approve");
    }

    private WorkbenchHistoryWriteBatchExecuteResponse batchApproveRetestPassedHistoryWritePlans(List<WorkbenchHistoryWritePlanResponse> plans, String auditAction) {
        List<WorkbenchHistoryWriteExecuteResponse> results = new ArrayList<>();
        int success = 0;
        int failed = 0;
        int skipped = 0;
        for (WorkbenchHistoryWritePlanResponse plan : plans) {
            if ("REVIEWED".equalsIgnoreCase(text(plan.comparisonReviewStatus()))) {
                skipped += 1;
                results.add(batchPlanResult(plan, "SKIPPED", "Skipped because comparison has already been reviewed."));
                continue;
            }
            try {
                WorkbenchHistoryWriteComparisonResponse approved = transactionTemplate.execute(transactionStatus -> approveRetestPassedHistoryWriteComparison(plan.caseNo()));
                if (approved == null) {
                    failed += 1;
                    results.add(batchPlanResult(plan, "FAILED", "Retest approve returned no result."));
                } else {
                    success += 1;
                    results.add(batchPlanResult(plan, "REVIEWED", "\u5f53\u524d\u57fa\u7840\u4fe1\u606f\u590d\u6d4b\u5df2\u4e00\u81f4\uff0c\u5df2\u6807\u8bb0\u590d\u6d4b\u901a\u8fc7"));
                }
            } catch (RuntimeException ex) {
                if (isRetestMismatch(ex)) {
                    skipped += 1;
                    results.add(batchPlanResult(plan, "SKIPPED", "\u5f53\u524d\u57fa\u7840\u4fe1\u606f\u590d\u6d4b\u4ecd\u6709\u5dee\u5f02\uff0c\u672a\u6807\u8bb0\u901a\u8fc7"));
                } else {
                    failed += 1;
                    results.add(batchPlanResult(plan, "FAILED", ex.getMessage()));
                }
            }
        }
        systemAuditService.record("workbench", auditAction, "SALARY_CASE", "BATCH",
                "total=" + plans.size() + ", success=" + success + ", failed=" + failed + ", skipped=" + skipped);
        return new WorkbenchHistoryWriteBatchExecuteResponse(plans.size(), success, failed, skipped, results);
    }

    private List<WorkbenchHistoryWritePlanResponse> selectedHistoryWritePlans(List<String> caseNos) {
        LinkedHashSet<String> safeCaseNos = new LinkedHashSet<>();
        if (caseNos != null) {
            for (String caseNo : caseNos) {
                String safeCaseNo = text(caseNo);
                if (!safeCaseNo.isBlank()) {
                    safeCaseNos.add(safeCaseNo);
                }
            }
        }
        if (safeCaseNos.isEmpty()) {
            throw new IllegalArgumentException("Selected history write plans are required.");
        }
        List<WorkbenchHistoryWritePlanResponse> plans = new ArrayList<>();
        for (String caseNo : safeCaseNos) {
            plans.add(historyWritePlan(caseNo));
        }
        return plans;
    }

    public WorkbenchHistoryWriteBatchExecuteResponse batchRollbackHistoryWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit) {
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        List<WorkbenchHistoryWritePlanResponse> plans = historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit);
        List<WorkbenchHistoryWriteExecuteResponse> results = new ArrayList<>();
        int success = 0;
        int failed = 0;
        int skipped = 0;
        for (WorkbenchHistoryWritePlanResponse plan : plans) {
            if (!"EXECUTED".equalsIgnoreCase(text(plan.planStatus())) || !"SUCCESS".equalsIgnoreCase(text(plan.executionResult()))) {
                skipped += 1;
                results.add(new WorkbenchHistoryWriteExecuteResponse(
                        plan.caseNo(),
                        plan.workItemId(),
                        plan.personCode(),
                        plan.orgCode(),
                        plan.planNo(),
                        plan.insertedHistoryId(),
                        "SKIPPED",
                        !text(plan.previousHistoryId()).isBlank() || !text(plan.nextHistoryId()).isBlank(),
                        "Skipped because plan status is " + text(plan.planStatus())
                ));
                continue;
            }
            try {
                WorkbenchHistoryWriteExecuteResponse rolledBack = transactionTemplate.execute(transactionStatus -> rollbackHistoryWrite(plan.caseNo()));
                if (rolledBack == null) {
                    failed += 1;
                    results.add(batchFailedResult(plan, "History rollback returned no result."));
                } else {
                    success += 1;
                    results.add(rolledBack);
                    systemAuditService.record("workbench", "history-write-batch-rollback", "SALARY_CASE", rolledBack.caseNo(),
                            rolledBack.personCode() + " historyId=" + rolledBack.historyId());
                }
            } catch (RuntimeException ex) {
                failed += 1;
                results.add(batchFailedResult(plan, ex.getMessage()));
            }
        }
        systemAuditService.record("workbench", "history-write-batch-rollback", "SALARY_CASE", "BATCH",
                "total=" + plans.size() + ", success=" + success + ", failed=" + failed + ", skipped=" + skipped);
        return new WorkbenchHistoryWriteBatchExecuteResponse(plans.size(), success, failed, skipped, results);
    }

    private WorkbenchHistoryWriteExecuteResponse batchFailedResult(WorkbenchHistoryWritePreviewResponse item, String message) {
        return new WorkbenchHistoryWriteExecuteResponse(
                item.caseNo(),
                item.workItemId(),
                item.personCode(),
                item.orgCode(),
                item.writePlanId(),
                "",
                "FAILED",
                item.sidUpdateRequired(),
                text(message).isBlank() ? "History write failed." : left(message, 1000)
        );
    }

    private WorkbenchHistoryWriteExecuteResponse batchFailedResult(WorkbenchHistoryWritePlanResponse item, String message) {
        return new WorkbenchHistoryWriteExecuteResponse(
                item.caseNo(),
                item.workItemId(),
                item.personCode(),
                item.orgCode(),
                item.planNo(),
                item.insertedHistoryId(),
                "FAILED",
                !text(item.previousHistoryId()).isBlank() || !text(item.nextHistoryId()).isBlank(),
                text(message).isBlank() ? "History rollback failed." : left(message, 1000)
        );
    }

    private WorkbenchHistoryWriteExecuteResponse batchPlanResult(WorkbenchHistoryWritePlanResponse item, String status, String message) {
        return new WorkbenchHistoryWriteExecuteResponse(
                item.caseNo(),
                item.workItemId(),
                item.personCode(),
                item.orgCode(),
                item.planNo(),
                item.insertedHistoryId(),
                status,
                false,
                text(message).isBlank() ? status : left(message, 1000)
        );
    }

    private boolean isRetestMismatch(RuntimeException ex) {
        String message = text(ex.getMessage());
        return message.contains("Current base retest still has differences");
    }

    public WorkbenchHistoryWritePlanResponse historyWritePlan(String caseNo) {
        ensureHistoryWritePlanTable();
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        String safeCaseNo = text(caseNo);
        if (safeCaseNo.isBlank()) {
            throw new IllegalArgumentException("Salary business case number is required.");
        }
        Map<String, Object> businessCase = businessCaseRow(safeCaseNo);
        organizationAccessService.requireOrgAccess(text(businessCase.get("org_code")));
        Map<String, Object> row = historyWritePlanRow(safeCaseNo);
        return historyWritePlanResponse(row);
    }

    public List<SystemAuditLogResponse> historyWritePlanAudits(String caseNo) {
        ensureHistoryWritePlanTable();
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        String safeCaseNo = text(caseNo);
        if (safeCaseNo.isBlank()) {
            throw new IllegalArgumentException("Salary business case number is required.");
        }
        Map<String, Object> businessCase = businessCaseRow(safeCaseNo);
        organizationAccessService.requireOrgAccess(text(businessCase.get("org_code")));
        return historyWriteAudits(safeCaseNo);
    }

    public WorkbenchHistoryWriteComparisonResponse historyWriteComparison(String caseNo) {
        ensureHistoryWritePlanTable();
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        String safeCaseNo = text(caseNo);
        if (safeCaseNo.isBlank()) {
            throw new IllegalArgumentException("Salary business case number is required.");
        }
        Map<String, Object> businessCase = businessCaseRow(safeCaseNo);
        organizationAccessService.requireOrgAccess(text(businessCase.get("org_code")));
        Map<String, Object> row = historyWritePlanRow(safeCaseNo);
        WorkbenchHistoryWritePreviewResponse preview = historyWritePreviewFromPlan(row);
        String insertedHistoryId = text(row.get("inserted_history_id"));
        Map<String, Object> insertedHistory = insertedHistoryId.isBlank() ? Map.of() : historyRowByIdIfExists(insertedHistoryId);
        List<WorkbenchHistoryWriteComparisonField> fields = comparisonFields(preview.fields(), insertedHistory);
        BigDecimal expectedTotal = preview.fields().stream()
                .map(WorkbenchHistoryWritePreviewField::amount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal actualTotal = insertedHistory.isEmpty() ? null : decimal(insertedHistory.get("hj2"));
        return new WorkbenchHistoryWriteComparisonResponse(
                preview.caseNo(),
                preview.workItemId(),
                preview.personCode(),
                preview.orgCode(),
                preview.year(),
                preview.month(),
                preview.businessType(),
                text(row.get("plan_no")),
                text(row.get("plan_status")),
                text(row.get("execution_result")),
                insertedHistoryId,
                expectedTotal,
                actualTotal,
                amountMatched(expectedTotal, actualTotal),
                text(row.get("comparison_review_status")),
                text(row.get("comparison_review_category")),
                text(row.get("comparison_review_reason")),
                text(row.get("comparison_reviewed_by")),
                text(row.get("comparison_reviewed_at")),
                preview.previousHistory(),
                preview.nextHistory(),
                fields
        );
    }

    public WorkbenchHistoryWriteComparisonResponse retestHistoryWriteComparison(String caseNo) {
        return retestHistoryWriteComparison(caseNo, true);
    }

    private WorkbenchHistoryWriteComparisonResponse retestHistoryWriteComparison(String caseNo, boolean recordAudit) {
        ensureHistoryWritePlanTable();
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        String safeCaseNo = text(caseNo);
        if (safeCaseNo.isBlank()) {
            throw new IllegalArgumentException("Salary business case number is required.");
        }
        Map<String, Object> businessCase = businessCaseRow(safeCaseNo);
        String orgCode = text(businessCase.get("org_code"));
        organizationAccessService.requireOrgAccess(orgCode);
        Integer year = number(businessCase.get("event_year"));
        Integer month = number(businessCase.get("event_month"));
        if (year == null || month == null) {
            throw new IllegalArgumentException("Salary business case year/month is incomplete.");
        }
        Map<String, Object> row = historyWritePlanRow(safeCaseNo);
        SalaryCalculationResult current = salaryCalculationService.calculate(new SalaryCalculationCommand(
                text(businessCase.get("person_code")),
                orgCode,
                year,
                month,
                text(businessCase.get("business_type"))
        ));
        List<String> issues = new ArrayList<>();
        List<WorkbenchHistoryWritePreviewField> previewFields = previewMappedFields(current.details(), issues);
        String insertedHistoryId = text(row.get("inserted_history_id"));
        Map<String, Object> insertedHistory = insertedHistoryId.isBlank() ? Map.of() : historyRowByIdIfExists(insertedHistoryId);
        List<WorkbenchHistoryWriteComparisonField> fields = comparisonFields(previewFields, insertedHistory);
        BigDecimal expectedTotal = current.totalAmount();
        BigDecimal actualTotal = insertedHistory.isEmpty() ? null : decimal(insertedHistory.get("hj2"));
        if (recordAudit) {
            systemAuditService.record("workbench", "history-write-comparison-retest", "SALARY_CASE", safeCaseNo,
                    text(businessCase.get("person_code")) + " " + text(businessCase.get("business_type")) + " currentBaseRetest");
        }
        return new WorkbenchHistoryWriteComparisonResponse(
                safeCaseNo,
                text(businessCase.get("work_item_id")),
                text(businessCase.get("person_code")),
                orgCode,
                year,
                month,
                text(businessCase.get("business_type")),
                text(row.get("plan_no")),
                text(row.get("plan_status")),
                text(row.get("execution_result")),
                insertedHistoryId,
                expectedTotal,
                actualTotal,
                amountMatched(expectedTotal, actualTotal),
                text(row.get("comparison_review_status")),
                text(row.get("comparison_review_category")),
                text(row.get("comparison_review_reason")),
                text(row.get("comparison_reviewed_by")),
                text(row.get("comparison_reviewed_at")),
                historyWritePreviewFromPlan(row).previousHistory(),
                historyWritePreviewFromPlan(row).nextHistory(),
                fields
        );
    }

    @Transactional
    public WorkbenchHistoryWriteComparisonResponse approveRetestPassedHistoryWriteComparison(String caseNo) {
        WorkbenchHistoryWriteComparisonResponse comparison = retestHistoryWriteComparison(caseNo);
        boolean hasMismatch = !Boolean.TRUE.equals(comparison.totalMatched())
                || comparison.fields().stream().anyMatch(field -> !Boolean.TRUE.equals(field.matched()));
        if (hasMismatch) {
            throw new IllegalArgumentException("Current base retest still has differences.");
        }
        String safeCaseNo = text(caseNo);
        String reviewReason = "\u5f53\u524d\u57fa\u7840\u4fe1\u606f\u590d\u6d4b\u5df2\u4e00\u81f4";
        jdbcTemplate.update("""
                UPDATE salary_history_write_plan
                SET comparison_review_status = 'REVIEWED',
                    comparison_review_category = 'BASE_CHANGED',
                    comparison_review_reason = ?,
                    comparison_reviewed_by = ?,
                    comparison_reviewed_at = CURRENT_TIMESTAMP
                WHERE case_no = ?
                """, reviewReason, text(currentUserService.currentUsername()), safeCaseNo);
        systemAuditService.record("workbench", "history-write-comparison-retest-approve", "SALARY_CASE", safeCaseNo,
                comparison.personCode() + " " + comparison.businessType() + " reviewCategory=BASE_CHANGED reviewReason=" + reviewReason);
        return historyWriteComparison(safeCaseNo);
    }

    private List<WorkbenchHistoryWriteComparisonField> comparisonFields(
            List<WorkbenchHistoryWritePreviewField> previewFields,
            Map<String, Object> insertedHistory
    ) {
        List<WorkbenchHistoryWriteComparisonField> fields = new ArrayList<>();
        for (WorkbenchHistoryWritePreviewField field : previewFields) {
            String historyField = text(field.historyField()).toLowerCase();
            BigDecimal actualAmount = insertedHistory.isEmpty() || historyField.isBlank()
                    ? null
                    : decimal(insertedHistory.get(historyField));
            fields.add(new WorkbenchHistoryWriteComparisonField(
                    field.itemCode(),
                    field.itemName(),
                    field.historyField(),
                    field.amount(),
                    actualAmount,
                    field.mapped(),
                    amountMatched(field.amount(), actualAmount),
                    field.issue()
            ));
        }
        return fields;
    }

    @Transactional
    public WorkbenchHistoryWriteComparisonResponse reviewHistoryWriteComparison(String caseNo, WorkbenchHistoryWriteReviewRequest request) {
        ensureHistoryWritePlanTable();
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        String safeCaseNo = text(caseNo);
        if (safeCaseNo.isBlank()) {
            throw new IllegalArgumentException("Salary business case number is required.");
        }
        Map<String, Object> businessCase = businessCaseRow(safeCaseNo);
        organizationAccessService.requireOrgAccess(text(businessCase.get("org_code")));
        Map<String, Object> row = historyWritePlanRow(safeCaseNo);
        String reviewCategory = normalizeHistoryWriteReviewCategory(request == null ? null : request.reviewCategory());
        String reviewReason = left(text(request == null ? null : request.reviewReason()), 1000);
        if (reviewCategory.isBlank()) {
            throw new IllegalArgumentException("Comparison review category is required.");
        }
        if (reviewReason.isBlank()) {
            throw new IllegalArgumentException("Comparison review reason is required.");
        }
        jdbcTemplate.update("""
                UPDATE salary_history_write_plan
                SET comparison_review_status = 'REVIEWED',
                    comparison_review_category = ?,
                    comparison_review_reason = ?,
                    comparison_reviewed_by = ?,
                    comparison_reviewed_at = CURRENT_TIMESTAMP
                WHERE case_no = ?
                """, reviewCategory, reviewReason, text(currentUserService.currentUsername()), safeCaseNo);
        systemAuditService.record("workbench", "history-write-comparison-review", "SALARY_CASE", safeCaseNo,
                text(row.get("person_code")) + " " + text(row.get("business_type")) + " reviewCategory=" + reviewCategory + " reviewReason=" + reviewReason);
        return historyWriteComparison(safeCaseNo);
    }

    private String normalizeHistoryWriteReviewCategory(String value) {
        String category = text(value).toUpperCase();
        return switch (category) {
            case "BASE_MISSING", "BASE_CHANGED", "POLICY_DIFF", "MANUAL_INPUT", "HISTORY_SPECIAL", "OTHER" -> category;
            default -> "";
        };
    }

    private WorkbenchHistoryWritePlanResponse historyWritePlanResponse(Map<String, Object> row) {
        String comparisonStatus = historyWriteComparisonStatus(row);
        Integer mismatchCount = historyWriteComparisonMismatchCount(row);
        String retestStatus = historyWriteRetestStatus(text(row.get("case_no")));
        HistoryWriteWorkflow workflow = historyWriteWorkflow(
                comparisonStatus,
                mismatchCount,
                text(row.get("comparison_review_status")),
                retestStatus
        );
        String maintenanceSuggestionJson = historyWriteMaintenanceSuggestionJson(
                text(row.get("case_no")),
                comparisonStatus,
                retestStatus,
                text(row.get("comparison_review_status"))
        );
        return new WorkbenchHistoryWritePlanResponse(
                text(row.get("plan_no")),
                text(row.get("case_no")),
                text(row.get("work_item_id")),
                text(row.get("person_code")),
                text(row.get("org_code")),
                number(row.get("event_year")),
                number(row.get("event_month")),
                text(row.get("business_type")),
                text(row.get("preview_status")),
                booleanValue(row.get("writable")),
                text(row.get("plan_status")),
                text(row.get("execution_result")),
                comparisonStatus,
                mismatchCount,
                text(row.get("inserted_history_id")),
                text(row.get("previous_history_id")),
                text(row.get("next_history_id")),
                text(row.get("prepared_by")),
                text(row.get("prepared_at")),
                text(row.get("executed_by")),
                text(row.get("executed_at")),
                text(row.get("rolled_back_by")),
                text(row.get("rolled_back_at")),
                text(row.get("execution_message")),
                text(row.get("rollback_message")),
                text(row.get("comparison_review_status")),
                text(row.get("comparison_review_category")),
                text(row.get("comparison_review_reason")),
                text(row.get("comparison_reviewed_by")),
                text(row.get("comparison_reviewed_at")),
                retestStatus,
                workflow.priority(),
                workflow.actionCode(),
                workflow.nextAction(),
                maintenanceSuggestionJson,
                text(row.get("issues_json")),
                text(row.get("preview_json"))
        );
    }

    private String historyWriteMaintenanceSuggestionJson(String caseNo, String comparisonStatus, String retestStatus, String reviewStatus) {
        if (!"MISMATCHED".equalsIgnoreCase(text(comparisonStatus))
                || !"RETEST_MISMATCHED".equalsIgnoreCase(text(retestStatus))
                || "REVIEWED".equalsIgnoreCase(text(reviewStatus))) {
            return "[]";
        }
        try {
            WorkbenchHistoryWriteComparisonResponse comparison = historyWriteComparison(caseNo);
            Map<String, MaintenanceSuggestionCounter> groups = new LinkedHashMap<>();
            for (WorkbenchHistoryWriteComparisonField field : comparison.fields()) {
                if (Boolean.TRUE.equals(field.matched())) {
                    continue;
                }
                MaintenanceSuggestion suggestion = maintenanceSuggestion(field);
                MaintenanceSuggestionCounter counter = groups.computeIfAbsent(suggestion.target(), ignored -> new MaintenanceSuggestionCounter(suggestion));
                counter.count++;
                String fieldName = firstNonBlank(field.itemName(), field.itemCode(), field.historyField());
                if (!fieldName.isBlank() && counter.fields.size() < 6) {
                    counter.fields.add(fieldName);
                }
            }
            return objectMapper.writeValueAsString(groups.values().stream()
                    .sorted(Comparator.comparingInt((MaintenanceSuggestionCounter counter) -> counter.count).reversed())
                    .map(counter -> Map.of(
                            "target", counter.suggestion.target(),
                            "label", counter.suggestion.label(),
                            "reason", counter.suggestion.reason(),
                            "count", counter.count,
                            "fields", counter.fields
                    ))
                    .toList());
        } catch (RuntimeException | JsonProcessingException ex) {
            return "[]";
        }
    }

    private MaintenanceSuggestion maintenanceSuggestion(WorkbenchHistoryWriteComparisonField field) {
        String combined = (text(field.itemCode()) + " " + text(field.itemName()) + " " + text(field.historyField())).toUpperCase();
        if (combined.contains("\u8003\u6838") || combined.contains("KH")) {
            return new MaintenanceSuggestion("assessment", "\u8003\u6838", "\u8003\u6838\u7ed3\u679c\u6216\u5e74\u9650\u6263\u51cf\u53ef\u80fd\u5f71\u54cd\u8be5\u9879");
        }
        if (combined.contains("\u5b66\u5386") || combined.contains("\u5b66\u4f4d") || combined.contains("\u6bd5\u4e1a") || combined.contains("\u89c1\u4e60") || combined.contains("\u8f6c\u6b63")
                || combined.contains("XLBM") || combined.contains("XL") || combined.contains("XW")) {
            return new MaintenanceSuggestion("education", "\u5b66\u5386", "\u5b66\u5386\u3001\u89c1\u4e60\u671f\u6216\u8f6c\u6b63\u5b9a\u7ea7\u53ef\u80fd\u5f71\u54cd\u8be5\u9879");
        }
        if (combined.contains("ZWGZ") || combined.contains("JBGZ") || combined.contains("DJGZ") || combined.contains("XJGZ") || combined.contains("ZWBM")
                || combined.contains("\u804c\u52a1") || combined.contains("\u5c97\u4f4d") || combined.contains("\u7ea7\u522b") || combined.contains("\u6863\u6b21") || combined.contains("\u85aa\u7ea7")) {
            return new MaintenanceSuggestion("post", "\u4efb\u804c", "\u804c\u52a1\u3001\u5c97\u4f4d\u3001\u7ea7\u522b\u6216\u6863\u6b21\u4fe1\u606f\u53ef\u80fd\u5f71\u54cd\u8be5\u9879");
        }
        if (combined.contains("GLGZ") || combined.contains("JHL") || combined.contains("NX")
                || combined.contains("\u5de5\u9f84") || combined.contains("\u6559\u62a4\u9f84") || combined.contains("\u5e74\u9650") || combined.contains("\u53c2\u52a0\u5de5\u4f5c")) {
            return new MaintenanceSuggestion("base", "\u57fa\u672c", "\u53c2\u52a0\u5de5\u4f5c\u3001\u6559\u62a4\u9f84\u6216\u5e74\u9650\u7c7b\u57fa\u7840\u4fe1\u606f\u53ef\u80fd\u5f71\u54cd\u8be5\u9879");
        }
        if (combined.contains("\u6d25\u8d34") || combined.contains("\u8865\u8d34") || combined.contains("\u6807\u51c6") || combined.contains("JT") || combined.contains("BT")) {
            return new MaintenanceSuggestion("standard", "\u6807\u51c6/\u6d25\u8865\u8d34", "\u53ef\u80fd\u4e0e\u6267\u884c\u6807\u51c6\u3001\u6d25\u8865\u8d34\u6807\u51c6\u6216\u624b\u5de5\u503c\u6709\u5173");
        }
        return new MaintenanceSuggestion("other", "\u5176\u4ed6", "\u6682\u672a\u80fd\u6839\u636e\u5b57\u6bb5\u81ea\u52a8\u5224\u65ad\uff0c\u5efa\u8bae\u7ed3\u5408\u4e1a\u52a1\u7c7b\u578b\u548c\u5386\u53f2\u6d41\u6c34\u6838\u67e5");
    }

    private HistoryWriteWorkflow historyWriteWorkflow(String comparisonStatus, Integer mismatchCount, String reviewStatus, String retestStatus) {
        if (!"MISMATCHED".equalsIgnoreCase(text(comparisonStatus))) {
            return new HistoryWriteWorkflow("DONE", "NOT_REQUIRED", "\u65e0\u9700\u6838\u67e5");
        }
        if ("REVIEWED".equalsIgnoreCase(text(reviewStatus))) {
            return new HistoryWriteWorkflow("DONE", "REVIEWED", "\u5df2\u6838\u67e5");
        }
        if ("RETEST_MISMATCHED".equalsIgnoreCase(text(retestStatus))) {
            return new HistoryWriteWorkflow("HIGH", "MAINTAIN_AND_RETEST", "\u68c0\u67e5\u57fa\u7840/\u4efb\u804c/\u5b66\u5386/\u8003\u6838\u540e\u590d\u6d4b");
        }
        if ("RETEST_MATCHED".equalsIgnoreCase(text(retestStatus))) {
            return new HistoryWriteWorkflow("MEDIUM", "APPROVE_RETEST", "\u6807\u8bb0\u590d\u6d4b\u901a\u8fc7");
        }
        int safeMismatchCount = mismatchCount == null ? 0 : mismatchCount;
        if (safeMismatchCount >= 3) {
            return new HistoryWriteWorkflow("HIGH", "RETEST_FIRST", "\u5148\u6309\u5f53\u524d\u57fa\u7840\u590d\u6d4b");
        }
        return new HistoryWriteWorkflow("MEDIUM", "RETEST_FIRST", "\u6309\u5f53\u524d\u57fa\u7840\u590d\u6d4b");
    }

    private String normalizeHistoryWritePriority(String value) {
        String priority = text(value).toUpperCase();
        return switch (priority) {
            case "HIGH", "MEDIUM", "LOW", "DONE" -> priority;
            default -> "";
        };
    }

    private String normalizeHistoryWriteActionCode(String value) {
        String actionCode = text(value).toUpperCase();
        return switch (actionCode) {
            case "RETEST_FIRST", "MAINTAIN_AND_RETEST", "APPROVE_RETEST", "REVIEWED", "NOT_REQUIRED" -> actionCode;
            default -> "";
        };
    }

    private record HistoryWriteWorkflow(String priority, String actionCode, String nextAction) {
    }

    private String historyWriteRetestStatus(String caseNo) {
        String safeCaseNo = text(caseNo);
        if (safeCaseNo.isBlank()) {
            return "NOT_RETESTED";
        }
        ensureAuditTable();
        Integer approved = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM sys_audit_log
                WHERE module_name = 'workbench'
                  AND action_name = 'history-write-comparison-retest-approve'
                  AND target_type = 'SALARY_CASE'
                  AND target_code = ?
                """, Integer.class, safeCaseNo);
        if (approved != null && approved > 0) {
            return "RETEST_MATCHED";
        }
        Integer retested = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM sys_audit_log
                WHERE module_name = 'workbench'
                  AND action_name = 'history-write-comparison-retest'
                  AND target_type = 'SALARY_CASE'
                  AND target_code = ?
                """, Integer.class, safeCaseNo);
        return retested != null && retested > 0 ? "RETEST_MISMATCHED" : "NOT_RETESTED";
    }

    private String normalizeHistoryWriteRetestStatus(String value) {
        String status = text(value).toUpperCase();
        return switch (status) {
            case "NOT_RETESTED", "RETEST_MISMATCHED", "RETEST_MATCHED" -> status;
            default -> "";
        };
    }

    private void ensureAuditTable() {
        systemAuditService.ensureTable();
    }

    private String historyWriteComparisonStatus(Map<String, Object> row) {
        String planStatus = text(row.get("plan_status"));
        String executionResult = text(row.get("execution_result"));
        String insertedHistoryId = text(row.get("inserted_history_id"));
        if ("ROLLED_BACK".equalsIgnoreCase(planStatus)) {
            return "ROLLED_BACK";
        }
        if ("BLOCKED".equalsIgnoreCase(planStatus) || "FAILED".equalsIgnoreCase(executionResult)) {
            return "BLOCKED";
        }
        if (!"EXECUTED".equalsIgnoreCase(planStatus) || !"SUCCESS".equalsIgnoreCase(executionResult)) {
            return "NOT_WRITTEN";
        }
        if (insertedHistoryId.isBlank()) {
            return "MISMATCHED";
        }
        try {
            WorkbenchHistoryWritePreviewResponse preview = historyWritePreviewFromPlan(row);
            Map<String, Object> insertedHistory = historyRowByIdIfExists(insertedHistoryId);
            if (insertedHistory.isEmpty()) {
                return "MISMATCHED";
            }
            for (WorkbenchHistoryWritePreviewField field : preview.fields()) {
                if (!Boolean.TRUE.equals(field.mapped())) {
                    continue;
                }
                String historyField = text(field.historyField()).toLowerCase();
                BigDecimal actualAmount = historyField.isBlank() ? null : decimal(insertedHistory.get(historyField));
                if (!amountMatched(field.amount(), actualAmount)) {
                    return "MISMATCHED";
                }
            }
            BigDecimal expectedTotal = preview.fields().stream()
                    .map(WorkbenchHistoryWritePreviewField::amount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (!amountMatched(expectedTotal, decimal(insertedHistory.get("hj2")))) {
                return "MISMATCHED";
            }
            return "MATCHED";
        } catch (RuntimeException ex) {
            return "UNKNOWN";
        }
    }

    private int historyWriteComparisonMismatchCount(Map<String, Object> row) {
        String planStatus = text(row.get("plan_status"));
        String executionResult = text(row.get("execution_result"));
        String insertedHistoryId = text(row.get("inserted_history_id"));
        if (!"EXECUTED".equalsIgnoreCase(planStatus) || !"SUCCESS".equalsIgnoreCase(executionResult)) {
            return 0;
        }
        if (insertedHistoryId.isBlank()) {
            return 1;
        }
        try {
            WorkbenchHistoryWritePreviewResponse preview = historyWritePreviewFromPlan(row);
            Map<String, Object> insertedHistory = historyRowByIdIfExists(insertedHistoryId);
            if (insertedHistory.isEmpty()) {
                return Math.max(1, preview.fields().size());
            }
            int mismatchCount = 0;
            for (WorkbenchHistoryWritePreviewField field : preview.fields()) {
                if (!Boolean.TRUE.equals(field.mapped())) {
                    continue;
                }
                String historyField = text(field.historyField()).toLowerCase();
                BigDecimal actualAmount = historyField.isBlank() ? null : decimal(insertedHistory.get(historyField));
                if (!amountMatched(field.amount(), actualAmount)) {
                    mismatchCount += 1;
                }
            }
            BigDecimal expectedTotal = preview.fields().stream()
                    .map(WorkbenchHistoryWritePreviewField::amount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (!amountMatched(expectedTotal, decimal(insertedHistory.get("hj2")))) {
                mismatchCount += 1;
            }
            return mismatchCount;
        } catch (RuntimeException ex) {
            return 1;
        }
    }

    private WorkbenchHistoryWritePreviewResponse historyWritePreviewFromPlan(Map<String, Object> row) {
        String previewJson = text(row.get("preview_json"));
        if (previewJson.isBlank()) {
            throw new IllegalArgumentException("History write plan preview is empty: " + text(row.get("case_no")));
        }
        try {
            return objectMapper.readValue(previewJson, WorkbenchHistoryWritePreviewResponse.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("History write plan preview cannot be parsed: " + text(row.get("case_no")), ex);
        }
    }

    private boolean amountMatched(BigDecimal expected, BigDecimal actual) {
        if (expected == null && actual == null) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }
        return expected.compareTo(actual) == 0;
    }

    private String historyWritePlanId(String caseNo) {
        return "HWP-" + text(caseNo);
    }

    private void persistHistoryWritePlan(WorkbenchHistoryWritePreviewResponse response) {
        try {
            String fieldsJson = objectMapper.writeValueAsString(response.fields());
            String issuesJson = objectMapper.writeValueAsString(response.issues());
            String previewJson = objectMapper.writeValueAsString(response);
            jdbcTemplate.update("""
                    INSERT INTO salary_history_write_plan(plan_no, case_no, work_item_id, person_code, org_code,
                                                          event_year, event_month, business_type, preview_status,
                                                          writable, existing_history_id, sid_plan, fields_json,
                                                          issues_json, preview_json, plan_status, prepared_by)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PREPARED', ?)
                    ON DUPLICATE KEY UPDATE
                        work_item_id = VALUES(work_item_id),
                        person_code = VALUES(person_code),
                        org_code = VALUES(org_code),
                        event_year = VALUES(event_year),
                        event_month = VALUES(event_month),
                        business_type = VALUES(business_type),
                        preview_status = VALUES(preview_status),
                        writable = VALUES(writable),
                        existing_history_id = VALUES(existing_history_id),
                        sid_plan = VALUES(sid_plan),
                        fields_json = VALUES(fields_json),
                        issues_json = VALUES(issues_json),
                        preview_json = VALUES(preview_json),
                        plan_status = CASE
                            WHEN plan_status IN ('EXECUTED', 'ROLLED_BACK') THEN plan_status
                            ELSE 'PREPARED'
                        END,
                        prepared_by = CASE
                            WHEN plan_status IN ('EXECUTED', 'ROLLED_BACK') THEN prepared_by
                            ELSE VALUES(prepared_by)
                        END,
                        prepared_at = CASE
                            WHEN plan_status IN ('EXECUTED', 'ROLLED_BACK') THEN prepared_at
                            ELSE CURRENT_TIMESTAMP
                        END
                    """,
                    response.writePlanId(),
                    response.caseNo(),
                    response.workItemId(),
                    response.personCode(),
                    response.orgCode(),
                    response.year(),
                    response.month(),
                    response.businessType(),
                    response.status(),
                    Boolean.TRUE.equals(response.writable()) ? 1 : 0,
                    response.existingHistoryId(),
                    response.sidPlan(),
                    fieldsJson,
                    issuesJson,
                    previewJson,
                    text(currentUserService.currentUsername()));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("History write plan serialization failed.", ex);
        }
    }

    @Transactional
    public WorkbenchHistoryWriteExecuteResponse executeHistoryWrite(String caseNo) {
        ensureHistoryWritePlanTable();
        Map<String, Object> existingPlan = historyWritePlanRowIfExists(caseNo);
        if (existingPlan != null) {
            String status = text(existingPlan.get("plan_status"));
            String result = text(existingPlan.get("execution_result"));
            String insertedHistoryId = text(existingPlan.get("inserted_history_id"));
            if ("EXECUTED".equalsIgnoreCase(status) && "SUCCESS".equalsIgnoreCase(result) && !insertedHistoryId.isBlank()) {
                throw new IllegalArgumentException("History write plan has already been executed: " + insertedHistoryId);
            }
            if ("ROLLED_BACK".equalsIgnoreCase(status)) {
                throw new IllegalArgumentException("Rolled back history write plans cannot be executed again.");
            }
        }
        WorkbenchHistoryWritePreviewResponse preview = historyWritePreview(caseNo);
        WorkbenchCaseSnapshotResponse snapshot = caseSnapshot(preview.caseNo());
        if (!Boolean.TRUE.equals(preview.writable())) {
            markHistoryWritePlanFailed(preview, "BLOCKED", "History write preview is not writable.");
            throw new IllegalArgumentException("History write preview is not writable.");
        }
        if (preview.issues().stream().anyMatch(issue -> text(issue).startsWith("BLOCKED"))) {
            markHistoryWritePlanFailed(preview, "BLOCKED", "History write preview contains blocked issues.");
            throw new IllegalArgumentException("History write preview contains blocked issues.");
        }
        WorkbenchHistoryWritePreviewHistoryRow sourceHistory = preview.previousHistory() != null
                ? preview.previousHistory()
                : preview.nextHistory();
        if (sourceHistory == null || text(sourceHistory.historyId()).isBlank()) {
            markHistoryWritePlanFailed(preview, "BLOCKED", "No adjacent history row can be used as write template.");
            throw new IllegalArgumentException("No adjacent history row can be used as write template.");
        }
        Map<String, Object> sourceRow = historyRowById(sourceHistory.historyId());
        String historyId = UUID.randomUUID().toString();
        Map<String, Object> targetRow = new LinkedHashMap<>(sourceRow);
        targetRow.put("id", historyId);
        targetRow.put("dwbm", preview.orgCode());
        targetRow.put("grbm", personNo(preview.personCode()));
        targetRow.put("jsnf", String.valueOf(preview.year()));
        targetRow.put("jsyf", String.valueOf(preview.month()));
        targetRow.put("jslb", preview.businessType());
        targetRow.put("sid", preview.nextHistory() == null ? null : preview.nextHistory().historyId());
        for (WorkbenchHistoryWritePreviewField field : preview.fields()) {
            if (Boolean.TRUE.equals(field.mapped()) && !text(field.historyField()).isBlank()) {
                targetRow.put(text(field.historyField()).toLowerCase(), field.amount());
            }
        }
        targetRow.put("hj2", snapshot.trialCalculatedTotal() == null
                ? preview.fields().stream()
                .map(WorkbenchHistoryWritePreviewField::amount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                : snapshot.trialCalculatedTotal());
        insertHistoryRow(targetRow);
        if (preview.previousHistory() != null) {
            jdbcTemplate.update("""
                    UPDATE hisbase
                    SET sid = ?
                    WHERE id = ?
                    """, historyId, preview.previousHistory().historyId());
        }
        jdbcTemplate.update("""
                UPDATE salary_history_write_plan
                SET plan_status = 'EXECUTED',
                    executed_by = ?,
                    executed_at = CURRENT_TIMESTAMP,
                    execution_result = 'SUCCESS',
                    execution_message = ?,
                    inserted_history_id = ?,
                    previous_history_id = ?,
                    next_history_id = ?
                WHERE plan_no = ?
                """,
                text(currentUserService.currentUsername()),
                "Inserted hisbase row " + historyId,
                historyId,
                preview.previousHistory() == null ? null : preview.previousHistory().historyId(),
                preview.nextHistory() == null ? null : preview.nextHistory().historyId(),
                preview.writePlanId());
        systemAuditService.record("workbench", "history-write-execute", "SALARY_CASE", preview.caseNo(),
                preview.personCode() + " " + preview.businessType() + " historyId=" + historyId);
        return new WorkbenchHistoryWriteExecuteResponse(
                preview.caseNo(),
                preview.workItemId(),
                preview.personCode(),
                preview.orgCode(),
                preview.writePlanId(),
                historyId,
                "EXECUTED",
                preview.sidUpdateRequired(),
                "Inserted hisbase row " + historyId
        );
    }

    private void markHistoryWritePlanFailed(WorkbenchHistoryWritePreviewResponse preview, String result, String message) {
        jdbcTemplate.update("""
                UPDATE salary_history_write_plan
                SET execution_result = ?,
                    execution_message = ?,
                    executed_by = ?,
                    executed_at = CURRENT_TIMESTAMP
                WHERE plan_no = ?
                """,
                result,
                left(message, 1000),
                text(currentUserService.currentUsername()),
                preview.writePlanId());
    }

    @Transactional
    public WorkbenchHistoryWriteExecuteResponse rollbackHistoryWrite(String caseNo) {
        WorkbenchCaseSnapshotResponse snapshot = caseSnapshot(caseNo);
        ensureHistoryWritePlanTable();
        Map<String, Object> plan = historyWritePlanRow(snapshot.caseNo());
        String planNo = text(plan.get("plan_no"));
        String insertedHistoryId = text(plan.get("inserted_history_id"));
        String previousHistoryId = text(plan.get("previous_history_id"));
        String nextHistoryId = text(plan.get("next_history_id"));
        if (!"EXECUTED".equalsIgnoreCase(text(plan.get("plan_status")))
                || !"SUCCESS".equalsIgnoreCase(text(plan.get("execution_result")))) {
            throw new IllegalArgumentException("Only successful executed history write plans can be rolled back.");
        }
        if (insertedHistoryId.isBlank()) {
            throw new IllegalArgumentException("History write plan has no inserted history id.");
        }
        Map<String, Object> inserted = historyRowById(insertedHistoryId);
        String insertedSid = text(inserted.get("sid"));
        if (!sameHistoryId(insertedSid, nextHistoryId)) {
            throw new IllegalArgumentException("Inserted history sid has changed; rollback is blocked.");
        }
        if (!previousHistoryId.isBlank()) {
            Map<String, Object> previous = historyRowById(previousHistoryId);
            if (!sameHistoryId(text(previous.get("sid")), insertedHistoryId)) {
                throw new IllegalArgumentException("Previous history sid no longer points to inserted row; rollback is blocked.");
            }
        }
        if (historyReferencedByOtherRow(snapshot, insertedHistoryId, previousHistoryId)) {
            throw new IllegalArgumentException("Inserted history row is referenced by another row; rollback is blocked.");
        }
        if (!previousHistoryId.isBlank()) {
            jdbcTemplate.update("""
                    UPDATE hisbase
                    SET sid = ?
                    WHERE id = ?
                    """, nextHistoryId.isBlank() ? null : nextHistoryId, previousHistoryId);
        }
        jdbcTemplate.update("DELETE FROM hisbase WHERE id = ?", insertedHistoryId);
        jdbcTemplate.update("""
                UPDATE salary_history_write_plan
                SET plan_status = 'ROLLED_BACK',
                    execution_result = 'ROLLED_BACK',
                    execution_message = ?,
                    rolled_back_by = ?,
                    rolled_back_at = CURRENT_TIMESTAMP,
                    rollback_message = ?
                WHERE plan_no = ?
                """,
                "Rolled back hisbase row " + insertedHistoryId,
                text(currentUserService.currentUsername()),
                "Deleted hisbase row " + insertedHistoryId,
                planNo);
        systemAuditService.record("workbench", "history-write-rollback", "SALARY_CASE", snapshot.caseNo(),
                snapshot.personCode() + " " + snapshot.businessType() + " historyId=" + insertedHistoryId);
        return new WorkbenchHistoryWriteExecuteResponse(
                snapshot.caseNo(),
                snapshot.workItemId(),
                snapshot.personCode(),
                snapshot.orgCode(),
                planNo,
                insertedHistoryId,
                "ROLLED_BACK",
                !previousHistoryId.isBlank() || !nextHistoryId.isBlank(),
                "Rolled back hisbase row " + insertedHistoryId
        );
    }

    private Map<String, Object> historyWritePlanRow(String caseNo) {
        Map<String, Object> row = historyWritePlanRowIfExists(caseNo);
        if (row == null) {
            throw new IllegalArgumentException("History write plan not found: " + caseNo);
        }
        return row;
    }

    private Map<String, Object> historyWritePlanRowIfExists(String caseNo) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM salary_history_write_plan
                WHERE case_no = ?
                LIMIT 1
                """, caseNo);
        if (rows.isEmpty()) {
            return null;
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        rows.getFirst().forEach((key, value) -> normalized.put(text(key).toLowerCase(), value));
        return normalized;
    }

    private Map<String, Object> historyRowById(String historyId) {
        Map<String, Object> row = historyRowByIdIfExists(historyId);
        if (row.isEmpty()) {
            throw new IllegalArgumentException("History template row not found: " + historyId);
        }
        return row;
    }

    private Map<String, Object> historyRowByIdIfExists(String historyId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM hisbase
                WHERE id = ?
                LIMIT 1
                """, historyId);
        if (rows.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        rows.getFirst().forEach((key, value) -> normalized.put(text(key).toLowerCase(), value));
        return normalized;
    }

    private void insertHistoryRow(Map<String, Object> row) {
        List<String> columns = hisbaseColumnsInOrder();
        String columnSql = String.join(", ", columns);
        String placeholderSql = String.join(", ", columns.stream().map(column -> "?").toList());
        Object[] values = columns.stream().map(column -> row.get(column.toLowerCase())).toArray();
        jdbcTemplate.update("INSERT INTO hisbase (" + columnSql + ") VALUES (" + placeholderSql + ")", values);
    }

    private List<String> hisbaseColumnsInOrder() {
        return jdbcTemplate.queryForList("""
                SELECT LOWER(column_name)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = 'hisbase'
                ORDER BY ordinal_position
                """, String.class);
    }

    private String existingHistoryId(WorkbenchCaseSnapshotResponse snapshot) {
        if (snapshot.year() == null || snapshot.month() == null) {
            return "";
        }
        List<String> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(id)
                FROM hisbase
                WHERE dwbm = ?
                  AND grbm = ?
                  AND TRIM(jsnf) = ?
                  AND TRIM(jsyf) = ?
                  AND TRIM(jslb) = ?
                LIMIT 1
                """, String.class,
                snapshot.orgCode(),
                personNo(snapshot.personCode()),
                String.valueOf(snapshot.year()),
                String.valueOf(snapshot.month()),
                snapshot.businessType());
        return rows.isEmpty() ? "" : text(rows.getFirst());
    }

    private WorkbenchHistoryWritePreviewHistoryRow adjacentHistory(WorkbenchCaseSnapshotResponse snapshot, boolean previous) {
        if (snapshot.year() == null || snapshot.month() == null) {
            return null;
        }
        String comparison = previous ? "<" : ">";
        String order = previous
                ? "ORDER BY CAST(TRIM(jsnf) AS UNSIGNED) DESC, CAST(TRIM(jsyf) AS UNSIGNED) DESC, id DESC"
                : "ORDER BY CAST(TRIM(jsnf) AS UNSIGNED), CAST(TRIM(jsyf) AS UNSIGNED), id";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(id) AS id,
                       TRIM(COALESCE(sid, '')) AS sid,
                       CAST(TRIM(jsnf) AS UNSIGNED) AS year,
                       CAST(TRIM(jsyf) AS UNSIGNED) AS month,
                       TRIM(jslb) AS change_type,
                       hj2 AS total_amount
                FROM hisbase
                WHERE dwbm = ?
                  AND grbm = ?
                  AND TRIM(jsnf) REGEXP '^[0-9]{4}$'
                  AND TRIM(jsyf) REGEXP '^[0-9]{1,2}$'
                  AND (CAST(TRIM(jsnf) AS UNSIGNED) * 100 + CAST(TRIM(jsyf) AS UNSIGNED)) __COMPARISON__ ?
                __ORDER__
                LIMIT 1
                """.replace("__COMPARISON__", comparison).replace("__ORDER__", order),
                snapshot.orgCode(),
                personNo(snapshot.personCode()),
                snapshot.year() * 100 + snapshot.month());
        return rows.isEmpty() ? null : historyPreviewRow(rows.getFirst());
    }

    private WorkbenchHistoryWritePreviewHistoryRow historyPreviewRow(Map<String, Object> row) {
        return new WorkbenchHistoryWritePreviewHistoryRow(
                text(row.get("id")),
                text(row.get("sid")),
                number(row.get("year")),
                number(row.get("month")),
                text(row.get("change_type")),
                decimal(row.get("total_amount"))
        );
    }

    private void validateSidChain(
            WorkbenchCaseSnapshotResponse snapshot,
            WorkbenchHistoryWritePreviewHistoryRow previous,
            WorkbenchHistoryWritePreviewHistoryRow next,
            List<String> issues
    ) {
        if (previous == null && next == null) {
            return;
        }
        if (previous != null && next != null) {
            if (!sameHistoryId(previous.nextId(), next.historyId())) {
                issues.add("BLOCKED: previous history sid does not point to the next history row.");
            }
            if (historyReferencedByOtherRow(snapshot, next.historyId(), previous.historyId())) {
                issues.add("BLOCKED: next history row is referenced by another row.");
            }
            return;
        }
        if (previous != null) {
            if (!text(previous.nextId()).isBlank()) {
                issues.add("BLOCKED: append target previous history already points to another row.");
            }
            return;
        }
        if (next != null && historyReferencedByOtherRow(snapshot, next.historyId(), "")) {
            issues.add("BLOCKED: first target next history is already referenced by another row.");
        }
    }

    private boolean historyReferencedByOtherRow(WorkbenchCaseSnapshotResponse snapshot, String targetHistoryId, String allowedPreviousId) {
        if (text(targetHistoryId).isBlank()) {
            return false;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM hisbase
                WHERE dwbm = ?
                  AND grbm = ?
                  AND TRIM(COALESCE(sid, '')) = ?
                  AND (? = '' OR TRIM(id) <> ?)
                """, Long.class,
                snapshot.orgCode(),
                personNo(snapshot.personCode()),
                text(targetHistoryId),
                text(allowedPreviousId),
                text(allowedPreviousId));
        return count != null && count > 0;
    }

    private boolean sameHistoryId(String left, String right) {
        return text(left).equals(text(right));
    }

    private List<WorkbenchHistoryWritePreviewField> previewMappedFields(
            WorkbenchCaseSnapshotResponse snapshot,
            List<String> issues
    ) {
        Set<String> hisbaseColumns = hisbaseColumns();
        List<WorkbenchHistoryWritePreviewField> fields = new ArrayList<>();
        List<WorkbenchCaseSnapshotItemResponse> salaryItems = snapshot.salaryItems().isEmpty()
                ? snapshot.trialChanges().stream()
                .map(change -> new WorkbenchCaseSnapshotItemResponse(
                        text(change.itemCode()).toUpperCase(),
                        text(change.itemName()),
                        change.afterAmount(),
                        text(change.ruleNote())
                ))
                .toList()
                : snapshot.salaryItems();
        for (WorkbenchCaseSnapshotItemResponse item : salaryItems) {
            String itemCode = text(item.itemCode()).toUpperCase();
            if (itemCode.isBlank()) {
                continue;
            }
            String historyField = itemCode.toLowerCase();
            boolean mapped = hisbaseColumns.contains(historyField);
            String issue = "";
            if (!mapped) {
                issue = "missing hisbase field";
                issues.add("BLOCKED: snapshot item " + itemCode + " has no hisbase column.");
            } else if (item.amount() == null) {
                issue = "empty amount";
                issues.add("WARNING: snapshot item " + itemCode + " amount is empty.");
            }
            fields.add(new WorkbenchHistoryWritePreviewField(
                    itemCode,
                    text(item.itemName()),
                    mapped ? historyField : "",
                    item.amount(),
                    mapped,
                    issue
            ));
        }
        if (fields.isEmpty()) {
            issues.add("WARNING: snapshot has no salary items.");
        }
        if (!hisbaseColumns.contains("hj2")) {
            issues.add("BLOCKED: hisbase total field hj2 is missing.");
        }
        return fields;
    }

    private List<WorkbenchHistoryWritePreviewField> previewMappedFields(
            List<SalaryCalculationDetail> salaryItems,
            List<String> issues
    ) {
        Set<String> hisbaseColumns = hisbaseColumns();
        List<WorkbenchHistoryWritePreviewField> fields = new ArrayList<>();
        for (SalaryCalculationDetail item : salaryItems) {
            String itemCode = text(item.itemCode()).toUpperCase();
            if (itemCode.isBlank()) {
                continue;
            }
            String historyField = itemCode.toLowerCase();
            boolean mapped = hisbaseColumns.contains(historyField);
            String issue = "";
            if (!mapped) {
                issue = "missing hisbase field";
                issues.add("BLOCKED: current item " + itemCode + " has no hisbase column.");
            } else if (item.amount() == null) {
                issue = "empty amount";
                issues.add("WARNING: current item " + itemCode + " amount is empty.");
            }
            fields.add(new WorkbenchHistoryWritePreviewField(
                    itemCode,
                    text(item.itemName()),
                    mapped ? historyField : "",
                    item.amount(),
                    mapped,
                    issue
            ));
        }
        if (fields.isEmpty()) {
            issues.add("WARNING: current calculation has no salary items.");
        }
        if (!hisbaseColumns.contains("hj2")) {
            issues.add("BLOCKED: hisbase total field hj2 is missing.");
        }
        return fields;
    }

    private Set<String> hisbaseColumns() {
        List<String> columns = jdbcTemplate.queryForList("""
                SELECT LOWER(column_name)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = 'hisbase'
                """, String.class);
        return new HashSet<>(columns);
    }

    private String sidPlan(
            WorkbenchHistoryWritePreviewHistoryRow previous,
            WorkbenchHistoryWritePreviewHistoryRow next
    ) {
        if (previous == null && next == null) {
            return "insert as only history row; new sid stays blank.";
        }
        if (previous == null) {
            return "insert before current first history row; new sid should point to " + next.historyId() + ".";
        }
        if (next == null) {
            return "append after " + previous.historyId() + "; previous sid should point to new history row, new sid stays blank.";
        }
        return "insert between " + previous.historyId() + " and " + next.historyId()
                + "; previous sid should point to new history row, new sid should point to " + next.historyId() + ".";
    }

    private String personNo(String personCode) {
        String value = text(personCode);
        int dash = value.indexOf('-');
        return dash >= 0 ? value.substring(dash + 1) : value;
    }

    private Map<String, Object> businessCaseRow(String caseNo) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT case_no,
                       work_item_id,
                       source,
                       status,
                       business_type,
                       person_code,
                       person_name,
                       org_code,
                       event_year,
                       event_month,
                       title,
                       summary,
                       trial_status,
                       trial_matched,
                       trial_baseline_total,
                       trial_calculated_total,
                       trial_expected_total,
                       trial_difference,
                       trial_summary,
                       trial_changes_json,
                       force_reason,
                       difference_reason,
                       cancel_reason,
                       review_status,
                       review_reason,
                       reviewed_by,
                       reviewed_at,
                       handled_by,
                       handled_at
                FROM salary_business_case
                WHERE case_no = ?
                LIMIT 1
                """, caseNo);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Salary business case not found: " + caseNo);
        }
        return rows.getFirst();
    }

    private Map<String, Object> businessCaseSnapshotRow(String workItemId) {
        ensureBusinessCaseSnapshotTable();
        if (text(workItemId).isBlank()) {
            return Map.of();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT snapshot_by,
                       snapshot_at
                FROM salary_business_case_snapshot
                WHERE work_item_id = ?
                LIMIT 1
                """, workItemId);
        return rows.isEmpty() ? Map.of() : rows.getFirst();
    }

    public WorkbenchCasePreviewResponse previewSalaryCase(WorkbenchCaseCreateRequest request) {
        ensureBusinessCaseTable();
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary todo and done permissions are required.");
        }
        CaseRequest normalized = normalizeCaseRequest(request);
        TrialSnapshot trial = trialSnapshot(normalized.personCode(), normalized.orgCode(), normalized.year(), normalized.month(), normalized.businessType());
        return new WorkbenchCasePreviewResponse(
                normalized.workItemId(),
                normalized.source(),
                normalized.businessType(),
                normalized.personCode(),
                normalized.personName(),
                normalized.orgCode(),
                normalized.year(),
                normalized.month(),
                normalized.title(),
                normalized.summary(),
                trial.status(),
                booleanFromInteger(trial.matched()),
                trial.baselineTotal(),
                trial.calculatedTotal(),
                trial.expectedTotal(),
                trial.difference(),
                trial.summary(),
                trialChanges(trial.changesJson())
        );
    }

    private CaseRequest normalizeCaseRequest(WorkbenchCaseCreateRequest request) {
        String workItemId = text(request == null ? null : request.workItemId());
        String personCode = text(request == null ? null : request.personCode());
        String orgCode = text(request == null ? null : request.orgCode());
        String businessType = text(request == null ? null : request.businessType());
        if (workItemId.isBlank() || personCode.isBlank() || orgCode.isBlank() || businessType.isBlank()) {
            throw new IllegalArgumentException("Work item id, person, organization, and business type are required.");
        }
        organizationAccessService.requireOrgAccess(orgCode);
        String source = text(request.source()).isBlank() ? "SALARY_EVENT" : text(request.source());
        String title = text(request.title()).isBlank() ? businessType : text(request.title());
        String summary = caseSummaryFromTodoCache(workItemId, source);
        if (summary.isBlank()) {
            summary = text(request.summary()).isBlank() ? "Salary work item is ready to be handled." : text(request.summary());
        }
        return new CaseRequest(
                workItemId,
                source,
                businessType,
                personCode,
                text(request.personName()),
                orgCode,
                request.year(),
                request.month(),
                title,
                summary
        );
    }

    private String caseSummaryFromTodoCache(String workItemId, String source) {
        if (!"SALARY_EVENT".equalsIgnoreCase(text(source)) || text(workItemId).isBlank()) {
            return "";
        }
        ensureSalaryTodoCacheTable();
        ensurePersonBaseChangeLogTable();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT todo.note,
                       latest_change.data_type AS latest_change_type,
                       latest_change.summary AS latest_change_summary,
                       latest_change.created_at AS latest_change_at
                FROM salary_todo_candidate_cache todo
                LEFT JOIN (
                    SELECT ranked.*
                    FROM (
                        SELECT log.*,
                               ROW_NUMBER() OVER (
                                   PARTITION BY log.person_code
                                   ORDER BY log.created_at DESC, log.id DESC
                               ) AS rn
                        FROM person_base_change_log log
                    ) ranked
                    WHERE ranked.rn = 1
                ) latest_change ON latest_change.person_code = todo.person_code
                WHERE todo.work_item_id = ?
                LIMIT 1
                """, workItemId);
        return rows.isEmpty() ? "" : todoSummary(rows.getFirst());
    }

    @Transactional
    public WorkbenchItemResponse completeSalaryCase(WorkbenchCaseCreateRequest request) {
        ensureBusinessCaseTable();
        ensureBusinessCaseSnapshotTable();
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary todo and done permissions are required.");
        }
        CaseRequest normalized = normalizeCaseRequest(request);
        String handledBy = text(currentUserService.currentUsername());
        TrialSnapshot trial = trialSnapshot(normalized.personCode(), normalized.orgCode(), normalized.year(), normalized.month(), normalized.businessType());
        String forceReason = left(text(request == null ? null : request.forceReason()), 1000);
        String differenceReason = left(text(request == null ? null : request.differenceReason()), 1000);
        String reviewStatus = ("DIFFERENT".equals(trial.status()) || "ERROR".equals(trial.status())) ? "PENDING" : "";
        if ("ERROR".equals(trial.status())) {
            if (request == null || !Boolean.TRUE.equals(request.force())) {
                throw new IllegalArgumentException("Trial calculation failed. Preview and confirm again to force handling.");
            }
            if (forceReason.isBlank()) {
                throw new IllegalArgumentException("Force reason is required when trial calculation failed.");
            }
        }
        if ("DIFFERENT".equals(trial.status()) && differenceReason.isBlank()) {
            throw new IllegalArgumentException("Difference reason is required when trial calculation is different.");
        }
        jdbcTemplate.update("""
                INSERT INTO salary_business_case(case_no, work_item_id, source, status, business_type,
                                                 person_code, person_name, org_code, event_year, event_month,
                                                 title, summary, trial_status, trial_matched, trial_difference,
                                                 trial_summary, trial_baseline_total, trial_calculated_total,
                                                 trial_expected_total, trial_changes_json, force_reason, difference_reason, review_status, handled_by)
                VALUES (CONCAT('GZ-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', LEFT(REPLACE(UUID(), '-', ''), 12)),
                        ?, ?, 'DONE', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    status = 'DONE',
                    business_type = VALUES(business_type),
                    person_code = VALUES(person_code),
                    person_name = VALUES(person_name),
                    org_code = VALUES(org_code),
                    event_year = VALUES(event_year),
                    event_month = VALUES(event_month),
                    title = VALUES(title),
                    summary = VALUES(summary),
                    trial_status = VALUES(trial_status),
                    trial_matched = VALUES(trial_matched),
                    trial_difference = VALUES(trial_difference),
                    trial_summary = VALUES(trial_summary),
                    trial_baseline_total = VALUES(trial_baseline_total),
                    trial_calculated_total = VALUES(trial_calculated_total),
                    trial_expected_total = VALUES(trial_expected_total),
                    trial_changes_json = VALUES(trial_changes_json),
                    force_reason = VALUES(force_reason),
                    difference_reason = VALUES(difference_reason),
                    review_status = VALUES(review_status),
                    cancel_reason = NULL,
                    review_reason = NULL,
                    reviewed_by = NULL,
                    reviewed_at = NULL,
                    handled_by = VALUES(handled_by),
                    handled_at = CURRENT_TIMESTAMP
                """, normalized.workItemId(), normalized.source(), normalized.businessType(),
                normalized.personCode(), normalized.personName(), normalized.orgCode(), normalized.year(), normalized.month(),
                normalized.title(), normalized.summary(),
                trial.status(), trial.matched(), trial.difference(), trial.summary(),
                trial.baselineTotal(), trial.calculatedTotal(), trial.expectedTotal(), trial.changesJson(), forceReason, differenceReason, reviewStatus, handledBy);
        removeSalaryTodoCache(normalized.workItemId());
        WorkbenchItemResponse item = findBusinessCase(normalized.workItemId());
        upsertBusinessCaseSnapshot(item.id(), normalized, trial, handledBy);
        String auditSummary = normalized.personCode() + " " + normalized.businessType()
                + (forceReason.isBlank() ? "" : " forceReason=" + forceReason)
                + (differenceReason.isBlank() ? "" : " differenceReason=" + differenceReason);
        systemAuditService.record("workbench", "salary-case-done", "SALARY_CASE", item.id(), auditSummary);
        return item;
    }

    private void upsertBusinessCaseSnapshot(String caseNo, CaseRequest request, TrialSnapshot trial, String handledBy) {
        String salaryItemsJson = salaryItemsJson(request, trial);
        String snapshotJson = businessCaseSnapshotJson(caseNo, request, trial, handledBy, salaryItemsJson);
        jdbcTemplate.update("""
                INSERT INTO salary_business_case_snapshot(case_no, work_item_id, person_code, org_code,
                                                          event_year, event_month, business_type,
                                                          trial_status, trial_matched, trial_difference,
                                                          trial_baseline_total, trial_calculated_total,
                                                          trial_expected_total, trial_changes_json,
                                                          salary_items_json, snapshot_json, snapshot_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    case_no = VALUES(case_no),
                    person_code = VALUES(person_code),
                    org_code = VALUES(org_code),
                    event_year = VALUES(event_year),
                    event_month = VALUES(event_month),
                    business_type = VALUES(business_type),
                    trial_status = VALUES(trial_status),
                    trial_matched = VALUES(trial_matched),
                    trial_difference = VALUES(trial_difference),
                    trial_baseline_total = VALUES(trial_baseline_total),
                    trial_calculated_total = VALUES(trial_calculated_total),
                    trial_expected_total = VALUES(trial_expected_total),
                    trial_changes_json = VALUES(trial_changes_json),
                    salary_items_json = VALUES(salary_items_json),
                    snapshot_json = VALUES(snapshot_json),
                    snapshot_by = VALUES(snapshot_by),
                    snapshot_at = CURRENT_TIMESTAMP
                """,
                caseNo,
                request.workItemId(),
                request.personCode(),
                request.orgCode(),
                request.year(),
                request.month(),
                request.businessType(),
                trial.status(),
                trial.matched(),
                trial.difference(),
                trial.baselineTotal(),
                trial.calculatedTotal(),
                trial.expectedTotal(),
                trial.changesJson(),
                salaryItemsJson,
                snapshotJson,
                handledBy);
    }

    private String businessCaseSnapshotJson(String caseNo, CaseRequest request, TrialSnapshot trial, String handledBy, String salaryItemsJson) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("caseNo", caseNo);
        snapshot.put("workItemId", request.workItemId());
        snapshot.put("source", request.source());
        snapshot.put("businessType", request.businessType());
        snapshot.put("personCode", request.personCode());
        snapshot.put("personName", request.personName());
        snapshot.put("orgCode", request.orgCode());
        snapshot.put("year", request.year());
        snapshot.put("month", request.month());
        snapshot.put("title", request.title());
        snapshot.put("summary", request.summary());
        snapshot.put("handledBy", handledBy);
        snapshot.put("trialStatus", trial.status());
        snapshot.put("trialMatched", booleanFromInteger(trial.matched()));
        snapshot.put("trialDifference", trial.difference());
        snapshot.put("trialBaselineTotal", trial.baselineTotal());
        snapshot.put("trialCalculatedTotal", trial.calculatedTotal());
        snapshot.put("trialExpectedTotal", trial.expectedTotal());
        snapshot.put("trialSummary", trial.summary());
        snapshot.put("trialChanges", trialChanges(trial.changesJson()));
        snapshot.put("salaryItems", snapshotSalaryItems(salaryItemsJson));
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private String salaryItemsJson(CaseRequest request, TrialSnapshot trial) {
        try {
            SalaryCalculationResult baseline = salaryCalculationService.calculate(new SalaryCalculationCommand(
                    request.personCode(), request.orgCode(), request.year(), request.month(), request.businessType()
            ));
            Map<String, WorkbenchCaseSnapshotItemResponse> items = new LinkedHashMap<>();
            for (SalaryCalculationDetail detail : baseline.details()) {
                String itemCode = text(detail.itemCode()).toUpperCase();
                if (!itemCode.isBlank()) {
                    items.put(itemCode, new WorkbenchCaseSnapshotItemResponse(
                            itemCode,
                            text(detail.itemName()),
                            detail.amount(),
                            text(detail.ruleNote())
                    ));
                }
            }
            for (WorkbenchCaseTrialChangeResponse change : trialChanges(trial.changesJson())) {
                String itemCode = text(change.itemCode()).toUpperCase();
                if (!itemCode.isBlank()) {
                    items.put(itemCode, new WorkbenchCaseSnapshotItemResponse(
                            itemCode,
                            text(change.itemName()),
                            change.afterAmount(),
                            text(change.ruleNote())
                    ));
                }
            }
            return objectMapper.writeValueAsString(new ArrayList<>(items.values()));
        } catch (Exception ex) {
            return "[]";
        }
    }

    private List<WorkbenchCaseSnapshotItemResponse> snapshotSalaryItems(String json) {
        if (text(json).isBlank()) {
            return List.of();
        }
        try {
            List<WorkbenchCaseSnapshotItemResponse> items = objectMapper.readValue(
                    json,
                    new TypeReference<>() {
                    });
            return items == null ? List.of() : items;
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }

    @Transactional
    public WorkbenchCaseDetailResponse cancelSalaryCase(String caseNo, WorkbenchCaseCancelRequest request) {
        ensureBusinessCaseTable();
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary todo and done permissions are required.");
        }
        String safeCaseNo = text(caseNo);
        if (safeCaseNo.isBlank()) {
            throw new IllegalArgumentException("Salary business case number is required.");
        }
        Map<String, Object> row = businessCaseRow(safeCaseNo);
        organizationAccessService.requireOrgAccess(text(row.get("org_code")));
        String personCode = text(row.get("person_code"));
        String businessType = text(row.get("business_type"));
        String cancelReason = left(text(request == null ? null : request.cancelReason()), 1000);
        if (cancelReason.isBlank()) {
            throw new IllegalArgumentException("Cancel reason is required.");
        }
        jdbcTemplate.update("""
                UPDATE salary_business_case
                SET status = 'CANCELLED',
                    cancel_reason = ?,
                    handled_by = ?,
                    handled_at = CURRENT_TIMESTAMP
                WHERE case_no = ?
                """, cancelReason, text(currentUserService.currentUsername()), safeCaseNo);
        systemAuditService.record("workbench", "salary-case-cancel", "SALARY_CASE", safeCaseNo,
                personCode + " " + businessType + " cancelReason=" + cancelReason);
        return caseDetail(safeCaseNo);
    }

    @Transactional
    public WorkbenchCaseDetailResponse reviewSalaryCase(String caseNo, WorkbenchCaseReviewRequest request) {
        ensureBusinessCaseTable();
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        String safeCaseNo = text(caseNo);
        if (safeCaseNo.isBlank()) {
            throw new IllegalArgumentException("Salary business case number is required.");
        }
        Map<String, Object> row = businessCaseRow(safeCaseNo);
        organizationAccessService.requireOrgAccess(text(row.get("org_code")));
        String status = text(row.get("status"));
        String trialStatus = text(row.get("trial_status"));
        if (!"DONE".equals(status) || (!"DIFFERENT".equals(trialStatus) && !"ERROR".equals(trialStatus))) {
            throw new IllegalArgumentException("Only done salary cases with different or error trial status can be reviewed.");
        }
        String reviewReason = left(text(request == null ? null : request.reviewReason()), 1000);
        if (reviewReason.isBlank()) {
            throw new IllegalArgumentException("Review reason is required.");
        }
        String reviewedBy = text(currentUserService.currentUsername());
        jdbcTemplate.update("""
                UPDATE salary_business_case
                SET review_status = 'REVIEWED',
                    review_reason = ?,
                    reviewed_by = ?,
                    reviewed_at = CURRENT_TIMESTAMP
                WHERE case_no = ?
                """, reviewReason, reviewedBy, safeCaseNo);
        systemAuditService.record("workbench", "salary-case-review", "SALARY_CASE", safeCaseNo,
                text(row.get("person_code")) + " " + text(row.get("business_type")) + " reviewReason=" + reviewReason);
        return caseDetail(safeCaseNo);
    }

    private long countSalaryTodo() {
        return countSalaryTodo("", "");
    }

    private long countSalaryTodo(String keyword, String changeType) {
        ensureSalaryTodoCacheLoaded();
        ensurePersonBaseChangeLogTable();
        Long cachedCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_todo_candidate_cache todo
                LEFT JOIN (
                    SELECT ranked.*
                    FROM (
                        SELECT log.*,
                               ROW_NUMBER() OVER (
                                   PARTITION BY log.person_code
                                   ORDER BY log.created_at DESC, log.id DESC
                               ) AS rn
                        FROM person_base_change_log log
                    ) ranked
                    WHERE ranked.rn = 1
                ) latest_change ON latest_change.person_code = todo.person_code
                WHERE __WORKBENCH_FILTER__
                """.replace("__WORKBENCH_FILTER__", salaryTodoFilter()), Long.class, salaryTodoFilterParams(keyword, changeType));
        return cachedCount == null ? 0 : cachedCount;
    }

    private long countSalaryDone() {
        return countSalaryDone("", "", "", "", "");
    }

    private long countSalaryCaseTrialStatus(String trialStatus) {
        ensureBusinessCaseTable();
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_business_case sc
                WHERE sc.status = 'DONE'
                  AND sc.trial_status = ?
                  AND __ORG_ACCESS__
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("sc.org_code")), Long.class, trialStatus);
        return count == null ? 0 : count;
    }

    private long countPendingSalaryReview() {
        ensureBusinessCaseTable();
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_business_case sc
                WHERE sc.status = 'DONE'
                  AND sc.trial_status IN ('DIFFERENT', 'ERROR')
                  AND COALESCE(NULLIF(sc.review_status, ''), 'PENDING') = 'PENDING'
                  AND __ORG_ACCESS__
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("sc.org_code")), Long.class);
        return count == null ? 0 : count;
    }

    private long countHistoryWritePlans(String status) {
        ensureHistoryWritePlanTable();
        String safeStatus = text(status).toUpperCase();
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_history_write_plan p
                WHERE __ORG_ACCESS__
                  AND (UPPER(TRIM(p.plan_status)) = ? OR UPPER(TRIM(COALESCE(p.execution_result, ''))) = ?)
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("p.org_code")),
                Long.class,
                safeStatus,
                safeStatus);
        return count == null ? 0 : count;
    }

    private long countPendingHistoryWriteComparisonReviews() {
        return historyWritePlans("EXECUTED", "MISMATCHED", "PENDING", "", "", "", "", "", "", 5000, 5000).size();
    }

    private long countSalaryDone(String keyword, String changeType, String caseStatus, String trialStatus, String reviewStatus) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM (
                    __SALARY_DONE_ALL__
                ) done
                WHERE __WORKBENCH_FILTER__
                  AND __TRIAL_FILTER__
                  AND __REVIEW_FILTER__
                """.replace("__SALARY_DONE_ALL__", salaryDoneAllSql(caseStatus))
                .replace("__WORKBENCH_FILTER__", workbenchFilter("done"))
                .replace("__TRIAL_FILTER__", trialStatusFilter("done"))
                .replace("__REVIEW_FILTER__", reviewStatusFilter("done")), Long.class, doneFilterParams(keyword, changeType, trialStatus, reviewStatus));
        return count == null ? 0 : count;
    }

    private List<WorkbenchItemResponse> salaryTodoItems(int offset, int limit) {
        return salaryTodoItems(offset, limit, "", "");
    }

    private List<WorkbenchItemResponse> salaryTodoItems(int offset, int limit, String keyword, String changeType) {
        return salaryTodoPage(offset, limit, keyword, changeType).items();
    }

    private WorkbenchItemsPageResponse salaryTodoPage(int offset, int limit, String keyword, String changeType) {
        ensureSalaryTodoCacheLoaded();
        return salaryTodoCachePage(offset, limit, keyword, changeType);
    }

    private WorkbenchItemsPageResponse salaryTodoCachePage(int offset, int limit, String keyword, String changeType) {
        ensurePersonBaseChangeLogTable();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM (
                    SELECT todo.*,
                           latest_change.data_type AS latest_change_type,
                           latest_change.summary AS latest_change_summary,
                           latest_change.created_at AS latest_change_at,
                           COUNT(1) OVER() AS total_count
                    FROM salary_todo_candidate_cache todo
                    LEFT JOIN (
                        SELECT ranked.*
                        FROM (
                            SELECT log.*,
                                   ROW_NUMBER() OVER (
                                       PARTITION BY log.person_code
                                       ORDER BY log.created_at DESC, log.id DESC
                                   ) AS rn
                            FROM person_base_change_log log
                        ) ranked
                        WHERE ranked.rn = 1
                    ) latest_change ON latest_change.person_code = todo.person_code
                    WHERE __WORKBENCH_FILTER__
                ) todo_page
                ORDER BY todo_page.event_year DESC,
                         todo_page.event_month DESC,
                         CASE todo_page.change_type
                             WHEN '\u804c\u52a1\u53d8\u5316' THEN 10
                              WHEN '\u8b66\u5458\u5957\u6539' THEN 15
                              WHEN '\u804c\u7ea7\u5957\u6539' THEN 16
                              WHEN '\u804c\u7ea7\u664b\u5347' THEN 17
                              WHEN '\u964d\u8d44\u5904\u5206' THEN 18
                              WHEN '\u5956\u52b1\u664b\u5347' THEN 19
                              WHEN '\u5b66\u5386\u53d8\u5316' THEN 20
                              WHEN '\u6b63\u5e38\u7ea7\u522b' THEN 30
                              WHEN '\u6b63\u5e38\u6863\u6b21' THEN 40
                             ELSE 99
                         END,
                         todo_page.org_code,
                         todo_page.person_no
                LIMIT ? OFFSET ?
                """.replace("__WORKBENCH_FILTER__", salaryTodoFilter()), salaryTodoFilterParams(keyword, changeType, limit, offset));
        List<WorkbenchItemResponse> items = new ArrayList<>();
        long total = rows.isEmpty() ? 0 : longValue(rows.getFirst().get("total_count"));
        for (Map<String, Object> row : rows) {
            String rowChangeType = text(row.get("change_type"));
            items.add(new WorkbenchItemResponse(
                    text(row.get("work_item_id")),
                    "SALARY_EVENT",
                    "TODO",
                    rowChangeType,
                    text(row.get("person_code")),
                    text(row.get("person_name")),
                    text(row.get("org_code")),
                    number(row.get("event_year")),
                    number(row.get("event_month")),
                    rowChangeType + "\u5f85\u529e",
                    todoSummary(row),
                    "",
                    ""
            ));
        }
        return new WorkbenchItemsPageResponse(total, offset, limit, items);
    }

    private String todoSummary(Map<String, Object> row) {
        String note = text(row.get("note"));
        String latestSummary = text(row.get("latest_change_summary"));
        if (latestSummary.isBlank()) {
            return note;
        }
        String latestType = text(row.get("latest_change_type"));
        String latestAt = text(row.get("latest_change_at"));
        String latestLabel = "\u6700\u8fd1\u57fa\u7840\u8d44\u6599"
                + (latestType.isBlank() ? "" : "(" + latestType + ")");
        String latestText = latestLabel + ": " + latestSummary
                + (latestAt.isBlank() ? "" : " " + latestAt);
        return left(note.isBlank() ? latestText : note + "\uff1b" + latestText, 1600);
    }

    private String salaryTodoCandidateSql(boolean ordered) {
        String sql = """
                SELECT ranked.source,
                       ranked.source_id,
                       ranked.person_code,
                       ranked.org_code,
                       ranked.person_no,
                       ranked.person_name,
                       ranked.event_year,
                       ranked.event_month,
                       ranked.change_type,
                       ranked.note
                FROM (
                    SELECT candidate.*,
                           ROW_NUMBER() OVER (
                               PARTITION BY candidate.person_code, candidate.event_year, candidate.event_month, candidate.change_type
                               ORDER BY CASE candidate.source
                                            WHEN 'hjxx' THEN 0
                                            WHEN 'dryzwbh' THEN 1
                                            WHEN 'dndkh' THEN 2
                                            ELSE 9
                                        END,
                                        candidate.source_id
                           ) AS rn
                    FROM (
                        SELECT
                        'dryzwbh' AS source,
                        CAST(id AS CHAR) AS source_id,
                        CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS person_code,
                        TRIM(dwbm) AS org_code,
                        TRIM(grbm) AS person_no,
                        TRIM(person_name) AS person_name,
                        CASE
                            WHEN LEFT(TRIM(zwbm), 2) IN ('23','24','25','26','27','28')
                                 AND COALESCE(previous_prefix, '') IN ('23','24','25','26','27','28')
                                 AND TRIM(COALESCE(xrzwbz, '')) = '1'
                                THEN YEAR(STR_TO_DATE(CONCAT(REPLACE(TRIM(srny), '.', ''), '01'), '%Y%m%d'))
                            ELSE YEAR(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(srny), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH))
                        END AS event_year,
                        CASE
                            WHEN LEFT(TRIM(zwbm), 2) IN ('23','24','25','26','27','28')
                                 AND COALESCE(previous_prefix, '') IN ('23','24','25','26','27','28')
                                 AND TRIM(COALESCE(xrzwbz, '')) = '1'
                                THEN MONTH(STR_TO_DATE(CONCAT(REPLACE(TRIM(srny), '.', ''), '01'), '%Y%m%d'))
                            ELSE MONTH(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(srny), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH))
                        END AS event_month,
                        CASE
                            WHEN LEFT(TRIM(zwbm), 2) IN ('23','24','25','26','27','28')
                                 AND COALESCE(previous_prefix, '') NOT IN ('23','24','25','26','27','28') THEN '\u804c\u7ea7\u5957\u6539'
                            WHEN LEFT(TRIM(zwbm), 2) IN ('23','24','25','26','27','28')
                                 AND COALESCE(previous_prefix, '') IN ('23','24','25','26','27','28')
                                 AND TRIM(COALESCE(previous_post_code, '')) <> ''
                                 AND TRIM(zwbm) <> TRIM(previous_post_code) THEN '\u804c\u7ea7\u664b\u5347'
                            WHEN LEFT(TRIM(zwbm), 2) IN ('21','22')
                                 AND COALESCE(previous_prefix, '') NOT IN ('21','22') THEN '\u8b66\u5458\u5957\u6539'
                            ELSE '\u804c\u52a1\u53d8\u5316'
                        END AS change_type,
                        CONCAT('\u4efb\u804c\u4fe1\u606f srny=', TRIM(srny), '\uff0c\u6309\u653f\u7b56\u5e94\u529e\u7406\u5de5\u8d44\u53d8\u52a8') AS note
                    FROM (
                        SELECT z.*,
                               p.xm AS person_name,
                               LAG(TRIM(z.zwbm)) OVER (PARTITION BY z.dwbm, z.grbm ORDER BY z.srny, z.id) AS previous_post_code,
                               LAG(LEFT(TRIM(z.zwbm), 2)) OVER (PARTITION BY z.dwbm, z.grbm ORDER BY z.srny, z.id) AS previous_prefix
                        FROM dryzwbh z
                        JOIN dryjbxx p ON p.dwbm = z.dwbm AND p.grbm = z.grbm
                    ) posts
                    WHERE previous_prefix IS NOT NULL
                      AND TRIM(COALESCE(zwbm, '')) <> ''
                      AND TRIM(COALESCE(srny, '')) REGEXP '^[0-9]{4}[.]?[0-9]{2}$'
                      AND CAST(REPLACE(TRIM(srny), '.', '') AS UNSIGNED) >= 200607
                      AND (
                          CASE
                              WHEN LEFT(TRIM(zwbm), 2) IN ('23','24','25','26','27','28')
                                   AND COALESCE(previous_prefix, '') IN ('23','24','25','26','27','28')
                                   AND TRIM(COALESCE(xrzwbz, '')) = '1'
                                  THEN CAST(REPLACE(TRIM(srny), '.', '') AS UNSIGNED)
                              ELSE (
                                  YEAR(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(srny), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) * 100
                                  + MONTH(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(srny), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH))
                              )
                          END
                      ) BETWEEN ((YEAR(CURDATE()) - 1) * 100 + 1) AND (YEAR(CURDATE()) * 100 + MONTH(CURDATE()))
                      AND NOT (
                          LEFT(TRIM(zwbm), 2) IN ('23','24','25','26','27','28')
                          AND TRIM(COALESCE(xrzwbz, '')) = ''
                          AND EXISTS (
                              SELECT 1
                              FROM dryzwbh same_month
                              WHERE same_month.dwbm = posts.dwbm
                                AND same_month.grbm = posts.grbm
                                AND TRIM(COALESCE(same_month.srny, '')) = TRIM(COALESCE(posts.srny, ''))
                                AND same_month.id <> posts.id
                                AND TRIM(COALESCE(same_month.zwbm, '')) <> ''
                                AND LEFT(TRIM(same_month.zwbm), 2) NOT IN ('23','24','25','26','27','28')
                          )
                       )
                      AND NOT EXISTS (
                          SELECT 1
                          FROM hjxx punishment
                          WHERE punishment.dwbm = posts.dwbm
                            AND punishment.grbm = posts.grbm
                            AND TRIM(COALESCE(punishment.hjsj, '')) = TRIM(COALESCE(posts.srny, ''))
                            AND (
                                HEX(CONVERT(CONCAT(
                                    TRIM(COALESCE(punishment.hjmc, '')),
                                    TRIM(COALESCE(punishment.jllx, '')),
                                    TRIM(COALESCE(punishment.qtqk, ''))
                                ) USING utf8mb4)) LIKE '%E5A484E58886%'
                                OR HEX(CONVERT(CONCAT(
                                    TRIM(COALESCE(punishment.hjmc, '')),
                                    TRIM(COALESCE(punishment.jllx, '')),
                                    TRIM(COALESCE(punishment.qtqk, ''))
                                ) USING utf8mb4)) LIKE '%E9998D%'
                            )
                      )

                    UNION ALL

                    SELECT
                        'dxl' AS source,
                        CAST(e.id AS CHAR) AS source_id,
                        CONCAT(TRIM(e.dwbm), '-', TRIM(e.grbm)) AS person_code,
                        TRIM(e.dwbm) AS org_code,
                        TRIM(e.grbm) AS person_no,
                        TRIM(p.xm) AS person_name,
                        YEAR(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(e.bysj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) AS event_year,
                        MONTH(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(e.bysj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) AS event_month,
                        '\u5b66\u5386\u53d8\u5316' AS change_type,
                        CONCAT('\u5b66\u5386\u53d6\u5f97 bysj=', TRIM(e.bysj), '\uff0c\u5b66\u5386\u7c7b\u522b=', TRIM(COALESCE(e.xllb, ''))) AS note
                    FROM dxl e
                    JOIN dryjbxx p ON p.dwbm = e.dwbm AND p.grbm = e.grbm
                    WHERE TRIM(COALESCE(e.bysj, '')) REGEXP '^[0-9]{4}[.]?[0-9]{2}$'
                      AND CAST(REPLACE(TRIM(e.bysj), '.', '') AS UNSIGNED) > 200607
                      AND (
                          YEAR(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(e.bysj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) * 100
                          + MONTH(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(e.bysj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH))
                      ) BETWEEN ((YEAR(CURDATE()) - 1) * 100 + 1) AND (YEAR(CURDATE()) * 100 + MONTH(CURDATE()))
                      AND TRIM(COALESCE(e.xllb, '')) NOT IN ('\u5176\u4ed6', '\u5176\u5b83')
                      AND EXISTS (
                          SELECT 1
                          FROM hisbase hb
                          WHERE hb.dwbm = e.dwbm
                            AND hb.grbm = e.grbm
                            AND TRIM(hb.jsnf) REGEXP '^[0-9]{4}$'
                            AND TRIM(hb.jsyf) REGEXP '^[0-9]{1,2}$'
                            AND (CAST(TRIM(hb.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(hb.jsyf) AS UNSIGNED))
                                < (
                                    YEAR(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(e.bysj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) * 100
                                    + MONTH(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(e.bysj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH))
                                )
                            AND TRIM(COALESCE(hb.zwbm2, '')) <> ''
                            AND TRIM(hb.zwbm2) NOT LIKE '%F%'
                            AND COALESCE(hb.hj2, 0) > 0
                          LIMIT 1
                      )

                    UNION ALL

                    SELECT
                        'hjxx' AS source,
                        CAST(h.id AS CHAR) AS source_id,
                        CONCAT(TRIM(h.dwbm), '-', TRIM(h.grbm)) AS person_code,
                        TRIM(h.dwbm) AS org_code,
                        TRIM(h.grbm) AS person_no,
                        TRIM(p.xm) AS person_name,
                        YEAR(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(h.hjsj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) AS event_year,
                        MONTH(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(h.hjsj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) AS event_month,
                        CASE
                            WHEN HEX(CONVERT(CONCAT(
                                TRIM(COALESCE(h.hjmc, '')),
                                TRIM(COALESCE(h.jllx, '')),
                                TRIM(COALESCE(h.qtqk, ''))
                            ) USING utf8mb4)) LIKE '%E9998D%'
                              OR HEX(CONVERT(CONCAT(
                                TRIM(COALESCE(h.hjmc, '')),
                                TRIM(COALESCE(h.jllx, '')),
                                TRIM(COALESCE(h.qtqk, ''))
                            ) USING utf8mb4)) LIKE '%E5A484E58886%'
                                THEN '\u964d\u8d44\u5904\u5206'
                            ELSE '\u5956\u52b1\u664b\u5347'
                        END AS change_type,
                        CONCAT('\u5956\u60e9\u4fe1\u606f hjsj=', TRIM(h.hjsj), '\uff0c', TRIM(COALESCE(h.jllx, h.hjmc, ''))) AS note
                    FROM hjxx h
                    JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                    WHERE TRIM(COALESCE(h.hjsj, '')) REGEXP '^[0-9]{4}[.]?[0-9]{2}$'
                      AND CAST(REPLACE(TRIM(h.hjsj), '.', '') AS UNSIGNED) >= 200607
                      AND (
                          YEAR(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(h.hjsj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) * 100
                          + MONTH(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(h.hjsj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH))
                      ) BETWEEN ((YEAR(CURDATE()) - 1) * 100 + 1) AND (YEAR(CURDATE()) * 100 + MONTH(CURDATE()))
                      AND (
                          HEX(CONVERT(CONCAT(
                              TRIM(COALESCE(h.hjmc, '')),
                              TRIM(COALESCE(h.jllx, '')),
                              TRIM(COALESCE(h.qtqk, ''))
                          ) USING utf8mb4)) LIKE '%E9998D%'
                          OR HEX(CONVERT(CONCAT(
                              TRIM(COALESCE(h.hjmc, '')),
                              TRIM(COALESCE(h.jllx, '')),
                              TRIM(COALESCE(h.qtqk, ''))
                          ) USING utf8mb4)) LIKE '%E5A484E58886%'
                          OR HEX(CONVERT(CONCAT(
                              TRIM(COALESCE(h.hjmc, '')),
                              TRIM(COALESCE(h.jllx, '')),
                              TRIM(COALESCE(h.qtqk, ''))
                          ) USING utf8mb4)) LIKE '%E5A596E58AB1E6998BE58D87%'
                      )

                    UNION ALL

                    SELECT
                        'dndkh' AS source,
                        CONCAT(TRIM(assess.khnd), ':grade') AS source_id,
                        CONCAT(TRIM(assess.dwbm), '-', TRIM(assess.grbm)) AS person_code,
                        TRIM(assess.dwbm) AS org_code,
                        TRIM(assess.grbm) AS person_no,
                        TRIM(assess.person_name) AS person_name,
                        CAST(TRIM(assess.khnd) AS UNSIGNED) + 1 AS event_year,
                        1 AS event_month,
                        '\u6b63\u5e38\u6863\u6b21' AS change_type,
                        CONCAT('\u5e74\u5ea6\u8003\u6838 ', TRIM(assess.khnd), '=', TRIM(assess.khjg), '\uff0c\u7b26\u5408\u6b63\u5e38\u664b\u6863/\u85aa\u7ea7\u6761\u4ef6') AS note
                    FROM (
                        SELECT k.*,
                               p.xm AS person_name,
                               LEFT(TRIM(COALESCE(p.zjbm, p.dwsx, '')), 2) AS person_prefix,
                               COUNT(1) OVER (
                                   PARTITION BY k.dwbm, k.grbm
                                   ORDER BY CAST(TRIM(k.khnd) AS UNSIGNED)
                                   ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                               ) AS qualified_years
                        FROM dndkh k
                        JOIN dryjbxx p ON p.dwbm = k.dwbm AND p.grbm = k.grbm
                        WHERE TRIM(k.khnd) REGEXP '^[0-9]{4}$'
                          AND CAST(TRIM(k.khnd) AS UNSIGNED) >= 2006
                          AND TRIM(k.khjg) IN ('\u4f18\u79c0', '\u79f0\u804c', '\u5408\u683c')
                    ) assess
                    WHERE (
                          assess.person_prefix IN ('07','08','09','10','11')
                          OR (
                              assess.person_prefix IN ('01','02','03','04','05','06','21','22','23','24','25','26','27','28')
                              AND assess.qualified_years MOD 2 = 0
                          )
                      )

                    UNION ALL

                    SELECT
                        'dndkh' AS source,
                        CONCAT(TRIM(assess.khnd), ':level') AS source_id,
                        CONCAT(TRIM(assess.dwbm), '-', TRIM(assess.grbm)) AS person_code,
                        TRIM(assess.dwbm) AS org_code,
                        TRIM(assess.grbm) AS person_no,
                        TRIM(assess.person_name) AS person_name,
                        CAST(TRIM(assess.khnd) AS UNSIGNED) + 1 AS event_year,
                        1 AS event_month,
                        '\u6b63\u5e38\u7ea7\u522b' AS change_type,
                        CONCAT('\u5e74\u5ea6\u8003\u6838 ', TRIM(assess.khnd), '=', TRIM(assess.khjg), '\uff0c\u7d2f\u8ba1\u7b26\u54085\u5e74\u7ea7\u522b\u664b\u5347\u6761\u4ef6') AS note
                    FROM (
                        SELECT k.*,
                               p.xm AS person_name,
                               LEFT(TRIM(COALESCE(p.zjbm, p.dwsx, '')), 2) AS person_prefix,
                               COUNT(1) OVER (
                                   PARTITION BY k.dwbm, k.grbm
                                   ORDER BY CAST(TRIM(k.khnd) AS UNSIGNED)
                                   ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                               ) AS qualified_years
                        FROM dndkh k
                        JOIN dryjbxx p ON p.dwbm = k.dwbm AND p.grbm = k.grbm
                        WHERE TRIM(k.khnd) REGEXP '^[0-9]{4}$'
                          AND CAST(TRIM(k.khnd) AS UNSIGNED) >= 2006
                          AND TRIM(k.khjg) IN ('\u4f18\u79c0', '\u79f0\u804c', '\u5408\u683c')
                    ) assess
                    WHERE assess.person_prefix IN ('01','02','04','21','22','23','24','25','26','27','28')
                      AND assess.qualified_years MOD 5 = 0
                    ) candidate
                    WHERE (candidate.event_year * 100 + candidate.event_month) BETWEEN ? AND ?
                      AND NOT EXISTS (
                          SELECT 1
                          FROM hisbase hb
                          WHERE hb.dwbm = candidate.org_code
                            AND hb.grbm = candidate.person_no
                            AND TRIM(hb.jsnf) REGEXP '^[0-9]{4}$'
                            AND TRIM(hb.jsyf) REGEXP '^[0-9]{1,2}$'
                            AND CAST(TRIM(hb.jsnf) AS UNSIGNED) = candidate.event_year
                            AND CAST(TRIM(hb.jsyf) AS UNSIGNED) = candidate.event_month
                            AND TRIM(hb.jslb) = candidate.change_type
                       )
                      AND NOT EXISTS (
                          SELECT 1
                          FROM salary_business_case sc
                          WHERE sc.work_item_id = CONCAT('salary-todo-', candidate.person_code, '-', candidate.event_year, '-', candidate.event_month, '-', candidate.change_type)
                            AND sc.status = 'DONE'
                      )
                ) ranked
                WHERE ranked.rn = 1
                """;
        if (!ordered) {
            return sql;
        }
        return sql + """
                ORDER BY candidate.event_year DESC,
                         candidate.event_month DESC,
                         CASE candidate.change_type
                             WHEN '\u804c\u52a1\u53d8\u5316' THEN 10
                              WHEN '\u8b66\u5458\u5957\u6539' THEN 15
                              WHEN '\u804c\u7ea7\u5957\u6539' THEN 16
                              WHEN '\u804c\u7ea7\u664b\u5347' THEN 17
                              WHEN '\u964d\u8d44\u5904\u5206' THEN 18
                              WHEN '\u5956\u52b1\u664b\u5347' THEN 19
                              WHEN '\u5b66\u5386\u53d8\u5316' THEN 20
                             WHEN '\u6b63\u5e38\u7ea7\u522b' THEN 30
                             WHEN '\u6b63\u5e38\u6863\u6b21' THEN 40
                             ELSE 99
                         END,
                         candidate.org_code,
                         candidate.person_no
                """;
    }

    private int minTodoYearMonth() {
        LocalDate today = LocalDate.now();
        return (today.getYear() - 1) * 100 + 1;
    }

    private int maxTodoYearMonth() {
        LocalDate today = LocalDate.now();
        return today.getYear() * 100 + today.getMonthValue();
    }

    private List<WorkbenchItemResponse> salaryDoneItems(int offset, int limit) {
        return salaryDoneItems(offset, limit, "", "", "", "", "");
    }

    private List<WorkbenchItemResponse> salaryDoneItems(int offset, int limit, String keyword, String changeType, String caseStatus, String trialStatus, String reviewStatus) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM (
                    __SALARY_DONE_ALL__
                ) done
                WHERE __WORKBENCH_FILTER__
                  AND __TRIAL_FILTER__
                  AND __REVIEW_FILTER__
                ORDER BY done.event_year DESC, done.event_month DESC, done.handled_at DESC, done.id DESC
                LIMIT ? OFFSET ?
                """.replace("__SALARY_DONE_ALL__", salaryDoneAllSql(caseStatus))
                .replace("__WORKBENCH_FILTER__", workbenchFilter("done"))
                .replace("__TRIAL_FILTER__", trialStatusFilter("done"))
                .replace("__REVIEW_FILTER__", reviewStatusFilter("done")), doneFilterParams(keyword, changeType, trialStatus, reviewStatus, limit, offset));
        List<WorkbenchItemResponse> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String rowChangeType = text(row.get("change_type"));
            items.add(new WorkbenchItemResponse(
                    text(row.get("id")),
                    text(row.get("source")),
                    text(row.get("business_status")).isBlank() ? "DONE" : text(row.get("business_status")),
                    rowChangeType,
                    text(row.get("person_code")),
                    text(row.get("person_name")),
                    text(row.get("org_code")),
                    number(row.get("event_year")),
                    number(row.get("event_month")),
                    rowChangeType,
                    text(row.get("note")),
                    text(row.get("trial_status")),
                    text(row.get("review_status"))
            ));
        }
        return items;
    }

    private String salaryDoneBaseSql() {
        return """
                SELECT hb.id,
                       'SALARY_EVENT' AS source,
                       'DONE' AS business_status,
                       CONCAT(TRIM(hb.dwbm), '-', TRIM(hb.grbm)) AS person_code,
                       TRIM(hb.dwbm) AS org_code,
                       TRIM(COALESCE(p.xm, '')) AS person_name,
                       CAST(TRIM(hb.jsnf) AS UNSIGNED) AS event_year,
                       CAST(TRIM(hb.jsyf) AS UNSIGNED) AS event_month,
                       TRIM(hb.jslb) AS change_type,
                       '' AS trial_status,
                       '' AS review_status,
                       hb.hj2,
                       hb.bbz AS handled_at,
                       CONCAT('\u5386\u53f2\u5de5\u8d44\u5df2\u529e\uff0c\u5408\u8ba1 ', hb.hj2) AS note
                FROM hisbase hb
                LEFT JOIN dryjbxx p ON p.dwbm = hb.dwbm AND p.grbm = hb.grbm
                WHERE CAST(TRIM(hb.jsnf) AS UNSIGNED) >= YEAR(CURDATE()) - 1
                  AND TRIM(hb.jslb) NOT IN ('\u6d25\u8d34\u53d8\u5316', '\u8c03\u6807\u664b\u5347')
                """;
    }

    private String salaryDoneAllSql(String caseStatus) {
        ensureBusinessCaseTable();
        String normalizedCaseStatus = text(caseStatus).toUpperCase();
        String caseStatusPredicate = switch (normalizedCaseStatus) {
            case "CANCELLED" -> "status = 'CANCELLED'";
            case "ALL" -> "status IN ('DONE', 'CANCELLED')";
            default -> "status = 'DONE'";
        };
        if ("CANCELLED".equals(normalizedCaseStatus)) {
            return salaryCaseDoneSql(caseStatusPredicate);
        }
        return """
                __SALARY_CASE_DONE__

                UNION ALL

                __SALARY_DONE_BASE__
                """.replace("__SALARY_CASE_DONE__", salaryCaseDoneSql(caseStatusPredicate))
                .replace("__SALARY_DONE_BASE__", salaryDoneBaseSql());
    }

    private String salaryCaseDoneSql(String caseStatusPredicate) {
        return """
                SELECT case_no AS id,
                       'SALARY_CASE' AS source,
                       status AS business_status,
                       person_code,
                       org_code,
                       person_name,
                       event_year,
                       event_month,
                       business_type AS change_type,
                       COALESCE(trial_status, '') AS trial_status,
                       CASE
                           WHEN trial_status IN ('DIFFERENT','ERROR') THEN COALESCE(NULLIF(review_status, ''), 'PENDING')
                           ELSE ''
                       END AS review_status,
                       0 AS hj2,
                       handled_at,
                       CONCAT(summary,
                              CASE WHEN COALESCE(trial_summary, '') = '' THEN '' ELSE CONCAT('；试算：', trial_summary) END,
                              CASE WHEN COALESCE(difference_reason, '') = '' THEN '' ELSE CONCAT('; differenceReason: ', difference_reason) END,
                              CASE WHEN status <> 'CANCELLED' OR COALESCE(cancel_reason, '') = '' THEN '' ELSE CONCAT('；撤回：', cancel_reason) END) AS note
                FROM salary_business_case
                WHERE __CASE_STATUS_PREDICATE__
                """.replace("__CASE_STATUS_PREDICATE__", caseStatusPredicate);
    }

    private String workbenchFilter(String alias) {
        return """
                (? = ''
                    OR __ALIAS__.person_code LIKE CONCAT('%', ?, '%')
                    OR __ALIAS__.person_name LIKE CONCAT('%', ?, '%')
                    OR __ALIAS__.org_code LIKE CONCAT('%', ?, '%')
                    OR __ALIAS__.change_type LIKE CONCAT('%', ?, '%')
                    OR __ALIAS__.note LIKE CONCAT('%', ?, '%'))
                AND (? = '' OR __ALIAS__.change_type = ?)
                AND __ORG_ACCESS__
                """.replace("__ALIAS__", alias)
                .replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql(alias + ".org_code"));
    }

    private String salaryTodoFilter() {
        return """
                (? = ''
                    OR todo.person_code LIKE CONCAT('%', ?, '%')
                    OR todo.person_name LIKE CONCAT('%', ?, '%')
                    OR todo.org_code LIKE CONCAT('%', ?, '%')
                    OR todo.change_type LIKE CONCAT('%', ?, '%')
                    OR todo.note LIKE CONCAT('%', ?, '%')
                    OR latest_change.data_type LIKE CONCAT('%', ?, '%')
                    OR latest_change.summary LIKE CONCAT('%', ?, '%'))
                AND (? = '' OR todo.change_type = ?)
                AND __ORG_ACCESS__
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("todo.org_code"));
    }

    private String trialStatusFilter(String alias) {
        return "(? = '' OR __ALIAS__.trial_status = ?)".replace("__ALIAS__", alias);
    }

    private String reviewStatusFilter(String alias) {
        return "(? = '' OR __ALIAS__.review_status = ?)".replace("__ALIAS__", alias);
    }

    private Object[] todoFilterParams(String keyword, String changeType, Object... tail) {
        return filterParams(minTodoYearMonth(), maxTodoYearMonth(), keyword, changeType, tail);
    }

    private Object[] doneFilterParams(String keyword, String changeType, String trialStatus, String reviewStatus, Object... tail) {
        return filterParams(keyword, changeType, trialStatus, reviewStatus, tail);
    }

    private Object[] filterParams(Object first, Object second, String keyword, String changeType, Object... tail) {
        List<Object> params = new ArrayList<>();
        params.add(first);
        params.add(second);
        addFilterParams(params, keyword, changeType);
        params.addAll(List.of(tail));
        return params.toArray();
    }

    private Object[] filterParams(String keyword, String changeType, Object... tail) {
        List<Object> params = new ArrayList<>();
        addFilterParams(params, keyword, changeType);
        params.addAll(List.of(tail));
        return params.toArray();
    }

    private Object[] salaryTodoFilterParams(String keyword, String changeType, Object... tail) {
        List<Object> params = new ArrayList<>();
        String safeKeyword = text(keyword);
        String safeChangeType = text(changeType);
        params.add(safeKeyword);
        for (int i = 0; i < 7; i++) {
            params.add(safeKeyword);
        }
        params.add(safeChangeType);
        params.add(safeChangeType);
        params.addAll(List.of(tail));
        return params.toArray();
    }

    private Object[] filterParams(String keyword, String changeType, String trialStatus, Object... tail) {
        List<Object> params = new ArrayList<>();
        addFilterParams(params, keyword, changeType);
        String safeTrialStatus = text(trialStatus).toUpperCase();
        params.add(safeTrialStatus);
        params.add(safeTrialStatus);
        params.addAll(List.of(tail));
        return params.toArray();
    }

    private Object[] filterParams(String keyword, String changeType, String trialStatus, String reviewStatus, Object... tail) {
        List<Object> params = new ArrayList<>();
        addFilterParams(params, keyword, changeType);
        String safeTrialStatus = text(trialStatus).toUpperCase();
        String safeReviewStatus = text(reviewStatus).toUpperCase();
        params.add(safeTrialStatus);
        params.add(safeTrialStatus);
        params.add(safeReviewStatus);
        params.add(safeReviewStatus);
        params.addAll(List.of(tail));
        return params.toArray();
    }

    private void addFilterParams(List<Object> params, String keyword, String changeType) {
        String safeKeyword = text(keyword);
        String safeChangeType = text(changeType);
        params.add(safeKeyword);
        for (int i = 0; i < 5; i++) {
            params.add(safeKeyword);
        }
        params.add(safeChangeType);
        params.add(safeChangeType);
    }

    private String text(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private String requireCurrentUsername() {
        String username = text(currentUserService.currentUsername());
        if (username.isBlank()) {
            throw new IllegalArgumentException("Login is required.");
        }
        return username;
    }

    private String safeUserStateKey(String stateKey) {
        String safeKey = text(stateKey);
        if (safeKey.isBlank() || safeKey.length() > 64 || !safeKey.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException("User work state key is invalid.");
        }
        return safeKey;
    }

    private void ensureUserWorkStateTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_user_work_state (
                    username VARCHAR(64) NOT NULL,
                    state_key VARCHAR(64) NOT NULL,
                    state_json LONGTEXT NOT NULL,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (username, state_key),
                    KEY idx_sys_user_work_state_updated (updated_at)
                )
                """);
    }

    private WorkbenchItemResponse findBusinessCase(String workItemId) {
        ensureBusinessCaseTable();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT case_no AS id,
                       person_code,
                       org_code,
                       person_name,
                       event_year,
                       event_month,
                       business_type AS change_type,
                       COALESCE(trial_status, '') AS trial_status,
                       CASE
                           WHEN trial_status IN ('DIFFERENT','ERROR') THEN COALESCE(NULLIF(review_status, ''), 'PENDING')
                           ELSE ''
                       END AS review_status,
                       summary AS note
                FROM salary_business_case
                WHERE work_item_id = ?
                LIMIT 1
                """, workItemId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Salary business case not found: " + workItemId);
        }
        Map<String, Object> row = rows.getFirst();
        String rowChangeType = text(row.get("change_type"));
        return new WorkbenchItemResponse(
                text(row.get("id")),
                "SALARY_CASE",
                "DONE",
                rowChangeType,
                text(row.get("person_code")),
                text(row.get("person_name")),
                text(row.get("org_code")),
                number(row.get("event_year")),
                number(row.get("event_month")),
                rowChangeType,
                text(row.get("note")),
                text(row.get("trial_status")),
                text(row.get("review_status"))
        );
    }

    private void ensureBusinessCaseTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS salary_business_case (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    case_no VARCHAR(64) NOT NULL,
                    work_item_id VARCHAR(255) NOT NULL,
                    source VARCHAR(64) NOT NULL,
                    status VARCHAR(32) NOT NULL DEFAULT 'DONE',
                    business_type VARCHAR(128) NOT NULL,
                    person_code VARCHAR(128) NOT NULL,
                    person_name VARCHAR(128) NULL,
                    org_code VARCHAR(64) NOT NULL,
                    event_year INT NULL,
                    event_month INT NULL,
                    title VARCHAR(255) NULL,
                    summary VARCHAR(1024) NULL,
                    trial_status VARCHAR(32) NULL,
                    trial_matched TINYINT NULL,
                    trial_difference DECIMAL(18,2) NULL,
                    trial_summary VARCHAR(2048) NULL,
                    trial_baseline_total DECIMAL(18,2) NULL,
                    trial_calculated_total DECIMAL(18,2) NULL,
                    trial_expected_total DECIMAL(18,2) NULL,
                    trial_changes_json LONGTEXT NULL,
                    force_reason VARCHAR(1024) NULL,
                    difference_reason VARCHAR(1024) NULL,
                    cancel_reason VARCHAR(1024) NULL,
                    review_status VARCHAR(32) NULL,
                    review_reason VARCHAR(1024) NULL,
                    reviewed_by VARCHAR(64) NULL,
                    reviewed_at DATETIME NULL,
                    handled_by VARCHAR(64) NULL,
                    handled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_salary_business_case_work_item (work_item_id),
                    KEY idx_salary_business_case_status_time (status, handled_at),
                    KEY idx_salary_business_case_person (person_code),
                    KEY idx_salary_business_case_org (org_code)
                )
                """);
        addColumnIfMissing("salary_business_case", "trial_status", "VARCHAR(32) NULL");
        addColumnIfMissing("salary_business_case", "trial_matched", "TINYINT NULL");
        addColumnIfMissing("salary_business_case", "trial_difference", "DECIMAL(18,2) NULL");
        addColumnIfMissing("salary_business_case", "trial_summary", "VARCHAR(2048) NULL");
        addColumnIfMissing("salary_business_case", "trial_baseline_total", "DECIMAL(18,2) NULL");
        addColumnIfMissing("salary_business_case", "trial_calculated_total", "DECIMAL(18,2) NULL");
        addColumnIfMissing("salary_business_case", "trial_expected_total", "DECIMAL(18,2) NULL");
        addColumnIfMissing("salary_business_case", "trial_changes_json", "LONGTEXT NULL");
        addColumnIfMissing("salary_business_case", "force_reason", "VARCHAR(1024) NULL");
        addColumnIfMissing("salary_business_case", "difference_reason", "VARCHAR(1024) NULL");
        addColumnIfMissing("salary_business_case", "cancel_reason", "VARCHAR(1024) NULL");
        addColumnIfMissing("salary_business_case", "review_status", "VARCHAR(32) NULL");
        addColumnIfMissing("salary_business_case", "review_reason", "VARCHAR(1024) NULL");
        addColumnIfMissing("salary_business_case", "reviewed_by", "VARCHAR(64) NULL");
        addColumnIfMissing("salary_business_case", "reviewed_at", "DATETIME NULL");
    }

    private void ensureBusinessCaseSnapshotTable() {
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
        addColumnIfMissing("salary_business_case_snapshot", "salary_items_json", "LONGTEXT NULL");
    }

    private void ensureHistoryWritePlanTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS salary_history_write_plan (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    plan_no VARCHAR(128) NOT NULL,
                    case_no VARCHAR(64) NOT NULL,
                    work_item_id VARCHAR(255) NOT NULL,
                    person_code VARCHAR(128) NOT NULL,
                    org_code VARCHAR(64) NOT NULL,
                    event_year INT NULL,
                    event_month INT NULL,
                    business_type VARCHAR(128) NOT NULL,
                    preview_status VARCHAR(32) NOT NULL,
                    writable TINYINT NOT NULL DEFAULT 0,
                    existing_history_id VARCHAR(128) NULL,
                    sid_plan VARCHAR(1024) NULL,
                    fields_json LONGTEXT NULL,
                    issues_json LONGTEXT NULL,
                    preview_json LONGTEXT NOT NULL,
                    plan_status VARCHAR(32) NOT NULL DEFAULT 'PREPARED',
                    prepared_by VARCHAR(64) NULL,
                    prepared_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    executed_by VARCHAR(64) NULL,
                    executed_at DATETIME NULL,
                    execution_result VARCHAR(32) NULL,
                    execution_message VARCHAR(1024) NULL,
                    inserted_history_id VARCHAR(128) NULL,
                    previous_history_id VARCHAR(128) NULL,
                    next_history_id VARCHAR(128) NULL,
                    rolled_back_by VARCHAR(64) NULL,
                    rolled_back_at DATETIME NULL,
                    rollback_message VARCHAR(1024) NULL,
                    comparison_review_status VARCHAR(32) NULL,
                    comparison_review_category VARCHAR(64) NULL,
                    comparison_review_reason VARCHAR(1024) NULL,
                    comparison_reviewed_by VARCHAR(64) NULL,
                    comparison_reviewed_at DATETIME NULL,
                    UNIQUE KEY uk_salary_history_write_plan_no (plan_no),
                    UNIQUE KEY uk_salary_history_write_plan_case (case_no),
                    KEY idx_salary_history_write_plan_person (person_code),
                    KEY idx_salary_history_write_plan_status (plan_status, preview_status, writable)
                )
                """);
        addColumnIfMissing("salary_history_write_plan", "execution_result", "VARCHAR(32) NULL");
        addColumnIfMissing("salary_history_write_plan", "execution_message", "VARCHAR(1024) NULL");
        addColumnIfMissing("salary_history_write_plan", "inserted_history_id", "VARCHAR(128) NULL");
        addColumnIfMissing("salary_history_write_plan", "previous_history_id", "VARCHAR(128) NULL");
        addColumnIfMissing("salary_history_write_plan", "next_history_id", "VARCHAR(128) NULL");
        addColumnIfMissing("salary_history_write_plan", "rolled_back_by", "VARCHAR(64) NULL");
        addColumnIfMissing("salary_history_write_plan", "rolled_back_at", "DATETIME NULL");
        addColumnIfMissing("salary_history_write_plan", "rollback_message", "VARCHAR(1024) NULL");
        addColumnIfMissing("salary_history_write_plan", "comparison_review_status", "VARCHAR(32) NULL");
        addColumnIfMissing("salary_history_write_plan", "comparison_review_category", "VARCHAR(64) NULL");
        addColumnIfMissing("salary_history_write_plan", "comparison_review_reason", "VARCHAR(1024) NULL");
        addColumnIfMissing("salary_history_write_plan", "comparison_reviewed_by", "VARCHAR(64) NULL");
        addColumnIfMissing("salary_history_write_plan", "comparison_reviewed_at", "DATETIME NULL");
    }

    private void ensureSalaryTodoCacheTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS salary_todo_candidate_cache (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    work_item_id VARCHAR(255) NOT NULL,
                    source VARCHAR(64) NOT NULL,
                    source_id VARCHAR(128) NULL,
                    person_code VARCHAR(128) NOT NULL,
                    org_code VARCHAR(64) NOT NULL,
                    person_no VARCHAR(64) NULL,
                    person_name VARCHAR(128) NULL,
                    event_year INT NOT NULL,
                    event_month INT NOT NULL,
                    change_type VARCHAR(128) NOT NULL,
                    note VARCHAR(1024) NULL,
                    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_salary_todo_cache_work_item (work_item_id),
                    KEY idx_salary_todo_cache_org_period (org_code, event_year, event_month),
                    KEY idx_salary_todo_cache_change (change_type),
                    KEY idx_salary_todo_cache_person (person_code)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS salary_todo_cache_meta (
                    cache_key VARCHAR(64) PRIMARY KEY,
                    last_refreshed_at DATETIME NOT NULL,
                    total_count BIGINT NOT NULL DEFAULT 0,
                    cache_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
                    dirty_at DATETIME NULL
                )
                """);
        addColumnIfMissing("salary_todo_cache_meta", "cache_status", "VARCHAR(32) NOT NULL DEFAULT 'ACTIVE'");
        addColumnIfMissing("salary_todo_cache_meta", "dirty_at", "DATETIME NULL");
    }

    private void ensurePersonBaseChangeLogTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS person_base_change_log (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    person_code VARCHAR(128) NOT NULL,
                    person_name VARCHAR(128) NULL,
                    org_code VARCHAR(64) NOT NULL,
                    data_type VARCHAR(64) NOT NULL,
                    change_year INT NULL,
                    change_month INT NULL,
                    source_table VARCHAR(64) NULL,
                    source_id VARCHAR(128) NULL,
                    summary VARCHAR(1024) NOT NULL,
                    created_by VARCHAR(64) NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    KEY idx_person_base_change_person (person_code, created_at),
                    KEY idx_person_base_change_org (org_code, created_at)
                )
                """);
    }

    private boolean salaryTodoCacheReady() {
        ensureSalaryTodoCacheTable();
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_todo_cache_meta
                WHERE cache_key = 'salary-todo'
                """, Long.class);
        return count != null && count > 0;
    }

    private synchronized void ensureSalaryTodoCacheLoaded() {
        ensureSalaryTodoCacheTable();
        if (!salaryTodoCacheReady()) {
            refreshSalaryTodoCacheInternal();
        }
    }

    private void refreshSalaryTodoCacheInternal() {
        ensureSalaryTodoCacheTable();
        jdbcTemplate.update("DELETE FROM salary_todo_candidate_cache");
        jdbcTemplate.update("""
                INSERT INTO salary_todo_candidate_cache(work_item_id, source, source_id, person_code, org_code,
                                                        person_no, person_name, event_year, event_month, change_type, note)
                SELECT CONCAT('salary-todo-', todo.person_code, '-', todo.event_year, '-', todo.event_month, '-', todo.change_type),
                       todo.source,
                       todo.source_id,
                       todo.person_code,
                       todo.org_code,
                       todo.person_no,
                       todo.person_name,
                       todo.event_year,
                       todo.event_month,
                       todo.change_type,
                       todo.note
                FROM (
                    __SALARY_TODO_CANDIDATES__
                ) todo
                """.replace("__SALARY_TODO_CANDIDATES__", salaryTodoCandidateSql(false)), minTodoYearMonth(), maxTodoYearMonth());
        jdbcTemplate.update("""
                REPLACE INTO salary_todo_cache_meta(cache_key, last_refreshed_at, total_count, cache_status, dirty_at)
                SELECT 'salary-todo', CURRENT_TIMESTAMP, COUNT(1), 'ACTIVE', NULL
                FROM salary_todo_candidate_cache
                """);
    }

    private void markSalaryTodoCacheDirtyInternal(String summary) {
        jdbcTemplate.update("""
                UPDATE salary_todo_cache_meta
                SET cache_status = 'DIRTY',
                    dirty_at = CURRENT_TIMESTAMP
                WHERE cache_key = 'salary-todo'
                """);
        systemAuditService.record("workbench", "salary-todo-cache-dirty", "SALARY_TODO_CACHE", "ALL",
                text(summary).isBlank() ? "salary todo cache marked dirty" : summary);
    }

    private String salaryTodoCacheHint(String fallback) {
        ensureSalaryTodoCacheTable();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT last_refreshed_at,
                       total_count,
                       cache_status,
                       dirty_at
                FROM salary_todo_cache_meta
                WHERE cache_key = 'salary-todo'
                LIMIT 1
                """);
        if (rows.isEmpty()) {
            return fallback;
        }
        Map<String, Object> row = rows.getFirst();
        String statusText = "DIRTY".equalsIgnoreCase(text(row.get("cache_status")))
                ? "\u5f85\u529e\u7f13\u5b58\u9700\u5237\u65b0\uff0c"
                : "";
        String dirtyText = "DIRTY".equalsIgnoreCase(text(row.get("cache_status"))) && !text(row.get("dirty_at")).isBlank()
                ? "\uff0c\u6807\u8bb0 " + text(row.get("dirty_at"))
                : "";
        return statusText + "\u7f13\u5b58\u5237\u65b0 " + text(row.get("last_refreshed_at"))
                + dirtyText
                + "\uff0c\u5237\u65b0\u65f6 " + longValue(row.get("total_count")) + " \u6761";
    }

    private void removeSalaryTodoCache(String workItemId) {
        ensureSalaryTodoCacheTable();
        jdbcTemplate.update("DELETE FROM salary_todo_candidate_cache WHERE work_item_id = ?", workItemId);
    }

    private TrialSnapshot trialSnapshot(String personCode, String orgCode, Integer year, Integer month, String businessType) {
        if (year == null || month == null || year < 2006 || month < 1 || month > 12) {
            return new TrialSnapshot("SKIPPED", null, null, "Missing year or month; trial calculation skipped.", null, null, null, "[]");
        }
        try {
            NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                    personCode, orgCode, year, month, businessType
            ));
            String status = result.matchedExpected() ? "MATCH" : "DIFFERENT";
            return new TrialSnapshot(
                    status,
                    result.matchedExpected() ? 1 : 0,
                    result.differenceWithExpected(),
                    trialSummary(result),
                    result.baselineTotalAmount(),
                    result.calculatedTotalAmount(),
                    result.expectedTotalAmount(),
                    trialChangesJson(result.changes())
            );
        } catch (Exception ex) {
            return new TrialSnapshot("ERROR", 0, null, left(text(ex.getMessage()), 512), null, null, null, "[]");
        }
    }
    private String trialSummary(NormalGradeTrialResult result) {
        StringBuilder summary = new StringBuilder();
        summary.append(result.matchedExpected() ? "匹配" : "差异");
        summary.append("，差额=").append(result.differenceWithExpected());
        if (result.changes() != null && !result.changes().isEmpty()) {
            summary.append("，变动=");
            summary.append(result.changes().stream()
                    .limit(5)
                    .map(SalaryRuleChange::itemName)
                    .filter(name -> name != null && !name.isBlank())
                    .toList());
        }
        return left(summary.toString(), 2000);
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """, Integer.class, tableName, columnName);
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    private synchronized void ensureWorkbenchPerformanceIndexes() {
        if (performanceIndexesEnsured) {
            return;
        }
        ensureBusinessCaseTable();
        addIndexIfMissing("salary_business_case", "idx_salary_case_work_status", "work_item_id, status");
        addIndexIfMissing("salary_business_case", "idx_salary_case_status_trial_review_org", "status, trial_status, review_status, org_code");
        addIndexIfMissing("hisbase", "idx_hisbase_workbench_person_period_type", "dwbm, grbm, jsnf, jsyf, jslb");
        addIndexIfMissing("dryzwbh", "idx_dryzwbh_workbench_person_date", "dwbm, grbm, srny, id");
        addIndexIfMissing("dxl", "idx_dxl_workbench_person_date", "dwbm, grbm, bysj, xllb");
        addIndexIfMissing("dndkh", "idx_dndkh_workbench_person_year_result", "dwbm, grbm, khnd, khjg");
        performanceIndexesEnsured = true;
    }

    private void addIndexIfMissing(String tableName, String indexName, String columns) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND index_name = ?
                """, Integer.class, tableName, indexName);
        if (count != null && count > 0) {
            return;
        }
        try {
            jdbcTemplate.execute("CREATE INDEX " + indexName + " ON " + tableName + " (" + columns + ")");
        } catch (RuntimeException ex) {
            Integer created = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM information_schema.statistics
                    WHERE table_schema = DATABASE()
                      AND table_name = ?
                      AND index_name = ?
                    """, Integer.class, tableName, indexName);
            if (created == null || created == 0) {
                throw ex;
            }
        }
    }

    private String left(String value, int maxLength) {
        String safe = text(value);
        return safe.length() <= maxLength ? safe : safe.substring(0, maxLength);
    }

    private String trialChangesJson(List<SalaryRuleChange> changes) {
        try {
            return objectMapper.writeValueAsString(changes == null ? List.of() : changes);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }

    private List<WorkbenchCaseTrialChangeResponse> trialChanges(String json) {
        if (json.isBlank()) {
            return List.of();
        }
        try {
            List<WorkbenchCaseTrialChangeResponse> changes = objectMapper.readValue(
                    json,
                    new TypeReference<>() {
                    }
            );
            return changes == null ? List.of() : changes;
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }

    private Boolean booleanFromInteger(Integer value) {
        return value == null ? null : value != 0;
    }

    private record CaseRequest(
            String workItemId,
            String source,
            String businessType,
            String personCode,
            String personName,
            String orgCode,
            Integer year,
            Integer month,
            String title,
            String summary
    ) {
    }

    private record TrialSnapshot(
            String status,
            Integer matched,
            BigDecimal difference,
            String summary,
            BigDecimal baselineTotal,
            BigDecimal calculatedTotal,
            BigDecimal expectedTotal,
            String changesJson
    ) {
    }

    private Integer number(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = text(value);
        return text.isEmpty() ? null : Integer.parseInt(text);
    }

    private long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = text(value);
        return text.isEmpty() ? 0 : Long.parseLong(text);
    }

    private BigDecimal decimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        String text = text(value);
        return text.isEmpty() ? null : new BigDecimal(text);
    }

    private Boolean booleanValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        String text = text(value);
        return text.isEmpty() ? null : !"0".equals(text) && !"false".equalsIgnoreCase(text);
    }

    private boolean hasMenu(String menuCode) {
        String username = currentUserService.currentUsername();
        return username != null && userPermissionService.hasMenu(username, menuCode);
    }
}

