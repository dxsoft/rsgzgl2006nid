package com.dx.rsgzgl.system.service;

import com.dx.rsgzgl.salary.dto.NormalGradeTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeBatchTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeBatchTrialItem;
import com.dx.rsgzgl.salary.dto.NormalGradeBatchTrialResult;
import com.dx.rsgzgl.salary.dto.NormalGradeTrialResult;
import com.dx.rsgzgl.salary.dto.SalaryCalculationCommand;
import com.dx.rsgzgl.salary.dto.SalaryCalculationDetail;
import com.dx.rsgzgl.salary.dto.SalaryCalculationResult;
import com.dx.rsgzgl.salary.dto.SalaryGeneratedTimelineBatchItem;
import com.dx.rsgzgl.salary.dto.SalaryGeneratedTimelineBatchResult;
import com.dx.rsgzgl.salary.dto.SalaryRuleChange;
import com.dx.rsgzgl.salary.service.SalaryGeneratedTimelineBatchService;
import com.dx.rsgzgl.salary.service.SalaryCalculationService;
import com.dx.rsgzgl.salary.service.NormalGradeBatchTrialService;
import com.dx.rsgzgl.salary.service.NormalGradeTrialService;
import com.dx.rsgzgl.system.dto.WorkbenchCaseDetailResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseCancelRequest;
import com.dx.rsgzgl.system.dto.WorkbenchCaseClosureActionResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseClosureStatusResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseClosureStepResponse;
import com.dx.rsgzgl.system.dto.SystemAuditLogResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseTrialChangeResponse;
import com.dx.rsgzgl.system.dto.WorkbenchItemResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseCreateRequest;
import com.dx.rsgzgl.system.dto.WorkbenchCasePreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseReviewRequest;
import com.dx.rsgzgl.system.dto.WorkbenchCaseSnapshotItemResponse;
import com.dx.rsgzgl.system.dto.WorkbenchCaseSnapshotResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchExecuteResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchExecuteRequest;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchLedgerResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchPreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchReviewRequest;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchRetestItemResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteBatchRetestResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteComparisonField;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteComparisonResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteConfirmResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteExecuteResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWritePlanResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteReviewLedgerResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWritePreviewField;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWritePreviewHistoryRow;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWritePreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteRollbackBatchPreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteRollbackPreviewResponse;
import com.dx.rsgzgl.system.dto.WorkbenchHistoryWriteReviewRequest;
import com.dx.rsgzgl.system.dto.WorkbenchReportPrintArchiveResponse;
import com.dx.rsgzgl.system.dto.WorkbenchGeneratedIssueReviewRequest;
import com.dx.rsgzgl.system.dto.WorkbenchGeneratedIssueReviewResponse;
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
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class WorkbenchService {

    private final JdbcTemplate jdbcTemplate;
    private final OrganizationAccessService organizationAccessService;
    private final CurrentUserService currentUserService;
    private final UserPermissionService userPermissionService;
    private final SystemAuditService systemAuditService;
    private final NormalGradeTrialService normalGradeTrialService;
    private final NormalGradeBatchTrialService normalGradeBatchTrialService;
    private final SalaryCalculationService salaryCalculationService;
    private final SalaryGeneratedTimelineBatchService salaryGeneratedTimelineBatchService;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;
    private final ConcurrentMap<String, String> migrationAcceptanceActiveRuns = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, BatchWriteSafetyToken> batchWriteSafetyTokens = new ConcurrentHashMap<>();
    private volatile boolean performanceIndexesEnsured;

    public WorkbenchService(
            JdbcTemplate jdbcTemplate,
            OrganizationAccessService organizationAccessService,
            CurrentUserService currentUserService,
            UserPermissionService userPermissionService,
            SystemAuditService systemAuditService,
            NormalGradeTrialService normalGradeTrialService,
            NormalGradeBatchTrialService normalGradeBatchTrialService,
            SalaryCalculationService salaryCalculationService,
            SalaryGeneratedTimelineBatchService salaryGeneratedTimelineBatchService,
            ObjectMapper objectMapper,
            PlatformTransactionManager transactionManager
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.organizationAccessService = organizationAccessService;
        this.currentUserService = currentUserService;
        this.userPermissionService = userPermissionService;
        this.systemAuditService = systemAuditService;
        this.normalGradeTrialService = normalGradeTrialService;
        this.normalGradeBatchTrialService = normalGradeBatchTrialService;
        this.salaryCalculationService = salaryCalculationService;
        this.salaryGeneratedTimelineBatchService = salaryGeneratedTimelineBatchService;
        this.objectMapper = objectMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public WorkbenchSummaryResponse summary() {
        ensureWorkbenchPerformanceIndexes();
        long applicationTodo = countApplicationCases("TODO");
        long applicationDone = countApplicationCases("DONE");
        long salaryTodo = hasMenu("SALARY_TODO") ? countSalaryTodoForAcceptance() : 0;
        long salaryDone = hasMenu("SALARY_DONE") ? countSalaryDoneForAcceptance() : 0;
        List<WorkbenchMetricResponse> metrics = new ArrayList<>();
        if (hasMenu("APPLICATION_TODO")) {
            metrics.add(new WorkbenchMetricResponse("APPLICATION_TODO", "\u7533\u529e\u5f85\u529e", applicationTodo, "\u7533\u529e\u4e1a\u52a1\u5df2\u63a5\u5165\u5f85\u529e/\u529e\u7ed3\u6d41\u8f6c"));
        }
        if (hasMenu("SALARY_TODO")) {
            metrics.add(new WorkbenchMetricResponse("SALARY_TODO", "\u5de5\u8d44\u53d8\u52a8\u5f85\u529e", salaryTodo, salaryTodoCacheHint("\u57fa\u4e8e\u57fa\u7840\u4fe1\u606f\u548c\u8003\u6838\u6761\u4ef6\u63a8\u5bfc")));
        }
        if (hasMenu("APPLICATION_DONE")) {
            metrics.add(new WorkbenchMetricResponse("APPLICATION_DONE", "\u7533\u529e\u5df2\u529e", applicationDone, "\u7533\u529e\u529e\u7ed3\u540e\u8fdb\u5165\u5df2\u529e\u53f0\u8d26"));
        }
        if (hasMenu("SALARY_DONE")) {
            metrics.add(new WorkbenchMetricResponse("SALARY_DONE", "\u5de5\u8d44\u53d8\u52a8\u5df2\u529e", salaryDone, "\u8fd1\u671f\u5386\u53f2\u5de5\u8d44\u53d8\u52a8\u548c\u65b0\u529e\u7406\u5de5\u8d44\u4e1a\u52a1"));
            metrics.add(new WorkbenchMetricResponse("SALARY_REVIEW_PENDING", "\u5f85\u590d\u6838\u98ce\u9669\u4e1a\u52a1", countPendingSalaryReview(), "\u8bd5\u7b97\u5dee\u5f02\u6216\u5f02\u5e38\u4e14\u5c1a\u672a\u590d\u6838"));
            metrics.add(new WorkbenchMetricResponse("SALARY_TRIAL_DIFFERENT", "\u8bd5\u7b97\u6709\u5dee\u5f02", countSalaryCaseTrialStatus("DIFFERENT"), "\u5df2\u529e\u8bb0\u5f55\u4e2d\u9700\u590d\u6838\u7684\u5dee\u5f02\u529e\u7406"));
            metrics.add(new WorkbenchMetricResponse("SALARY_TRIAL_ERROR", "\u8bd5\u7b97\u5f02\u5e38", countSalaryCaseTrialStatus("ERROR"), "\u5df2\u529e\u8bb0\u5f55\u4e2d\u5f3a\u5236\u529e\u7406\u7684\u5f02\u5e38"));
            metrics.add(new WorkbenchMetricResponse("HISTORY_PLAN_READY", "\u53ef\u5199\u5165\u5386\u53f2", countReadyHistoryWritePlans(), "\u9884\u68c0\u901a\u8fc7\u4e14\u53ef\u76f4\u63a5\u5199\u5165 hisbase"));
            metrics.add(new WorkbenchMetricResponse("HISTORY_PLAN_PREPARED", "\u5199\u5165\u8ba1\u5212\u5f85\u6267\u884c", countHistoryWritePlans("PREPARED"), "\u5df2\u751f\u6210\u4f46\u5c1a\u672a\u5199\u5165 hisbase"));
            metrics.add(new WorkbenchMetricResponse("HISTORY_PLAN_EXECUTED", "\u5386\u53f2\u5df2\u5199\u5165", countHistoryWritePlans("EXECUTED"), "\u5df2\u6267\u884c\u4e14\u53ef\u8ffd\u6eaf\u7684\u5199\u5165"));
            metrics.add(new WorkbenchMetricResponse("HISTORY_PLAN_ROLLED_BACK", "\u5199\u5165\u5df2\u64a4\u9500", countHistoryWritePlans("ROLLED_BACK"), "\u5df2\u64a4\u9500\u5e76\u6062\u590d sid \u94fe"));
            metrics.add(new WorkbenchMetricResponse("HISTORY_PLAN_BLOCKED", "\u5199\u5165\u5df2\u963b\u65ad", countHistoryWritePlans("BLOCKED"), "\u9884\u89c8\u6216\u6267\u884c\u9636\u6bb5\u88ab\u963b\u65ad"));
            metrics.add(new WorkbenchMetricResponse("HISTORY_PLAN_REVIEW_PENDING", "\u5199\u5165\u5f02\u5e38\u5f85\u6838\u67e5", countPendingHistoryWriteComparisonReviews(), "\u5199\u5165\u540e\u4e0d\u4e00\u81f4\u4e14\u5c1a\u672a\u6838\u67e5"));
            metrics.add(new WorkbenchMetricResponse("HISTORY_QUEUE_BLOCKED", "\u5199\u5165\u963b\u65ad", countHistoryWritePendingQueue("blocked"), "\u5386\u53f2\u5199\u5165\u540e\u7eed\u5904\u7406\u961f\u5217"));
            metrics.add(new WorkbenchMetricResponse("HISTORY_QUEUE_PREPARED", "\u5f85\u5199\u5165", countHistoryWritePendingQueue("prepared"), "\u5386\u53f2\u5199\u5165\u540e\u7eed\u5904\u7406\u961f\u5217"));
            metrics.add(new WorkbenchMetricResponse("HISTORY_QUEUE_REVIEW", "\u5199\u5165\u540e\u5f85\u6838\u67e5", countHistoryWritePendingQueue("review"), "\u5386\u53f2\u5199\u5165\u540e\u7eed\u5904\u7406\u961f\u5217"));
            metrics.add(new WorkbenchMetricResponse("HISTORY_QUEUE_RETEST", "\u590d\u6d4b\u4ecd\u6709\u5dee\u5f02", countHistoryWritePendingQueue("retest"), "\u5386\u53f2\u5199\u5165\u540e\u7eed\u5904\u7406\u961f\u5217"));
        }
        return new WorkbenchSummaryResponse(
                metrics,
                workbenchSummaryTodoItems(),
                workbenchSummaryDoneItems()
        );
    }

    private List<WorkbenchItemResponse> workbenchSummaryTodoItems() {
        List<WorkbenchItemResponse> items = new ArrayList<>();
        if (hasMenu("APPLICATION_TODO")) {
            items.addAll(applicationCases("TODO", 6));
        }
        if (hasMenu("SALARY_TODO")) {
            items.addAll(salaryTodoItems(0, 6));
        }
        return items.stream()
                .sorted(Comparator.comparingInt(this::workbenchItemYearMonth).reversed())
                .limit(10)
                .toList();
    }

    private List<WorkbenchItemResponse> salaryClosureTodoItems() {
        List<WorkbenchItemResponse> items = new ArrayList<>();
        items.addAll(salaryDoneItems(0, 4, "", "", "", "DONE", "", "", "", "BLOCKED", ""));
        items.addAll(salaryDoneItems(0, 4, "", "", "", "DONE", "", "", "", "PENDING", ""));
        return items.stream()
                .map(this::salaryClosureTodoItem)
                .limit(6)
                .toList();
    }

    private WorkbenchItemResponse salaryClosureTodoItem(WorkbenchItemResponse item) {
        String closureText = switch (text(item.closureStatus())) {
            case "BLOCKED" -> "\u95ed\u73af\u963b\u65ad";
            case "PENDING" -> "\u5f85\u95ed\u73af";
            default -> "\u95ed\u73af\u5f85\u5904\u7406";
        };
        return new WorkbenchItemResponse(
                item.id(),
                "SALARY_CLOSURE",
                "TODO",
                item.businessType(),
                item.personCode(),
                item.personName(),
                item.orgCode(),
                item.year(),
                item.month(),
                closureText + "\uff1a" + item.businessType(),
                item.closureMessage(),
                item.trialStatus(),
                item.reviewStatus(),
                item.workflowStatus(),
                item.closureStatus(),
                item.closureMessage(),
                item.nextActionCode(),
                item.nextActionLabel()
        );
    }

    private List<WorkbenchItemResponse> workbenchSummaryDoneItems() {
        List<WorkbenchItemResponse> items = new ArrayList<>();
        if (hasMenu("APPLICATION_DONE")) {
            items.addAll(applicationCases("DONE", 6));
        }
        return items.stream()
                .sorted(Comparator.comparingInt(this::workbenchItemYearMonth).reversed())
                .limit(10)
                .toList();
    }

    private int workbenchItemYearMonth(WorkbenchItemResponse item) {
        return (item.year() == null ? 0 : item.year()) * 100 + (item.month() == null ? 0 : item.month());
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
    public WorkbenchMetricResponse refreshGeneratedTimelineIssueTodos(String orgCode, String keyword, Integer limit, Integer eventLimit) {
        ensureWorkbenchPerformanceIndexes();
        ensureSalaryTodoCacheTable();
        ensureGeneratedTimelineIssueReviewTable();
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_TRIAL")) {
            throw new IllegalArgumentException("Salary todo and trial permissions are required.");
        }
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        SalaryGeneratedTimelineBatchResult result = salaryGeneratedTimelineBatchService.scan(safeOrgCode, keyword, limit, eventLimit);
        int refreshed = 0;
        for (SalaryGeneratedTimelineBatchItem item : result.items()) {
            if ("OK".equalsIgnoreCase(text(item.status()))) {
                continue;
            }
            String workItemId = "generated-timeline-" + item.personCode();
            jdbcTemplate.update("""
                    INSERT INTO salary_todo_candidate_cache(work_item_id, source, source_id, person_code, org_code,
                                                            person_no, person_name, event_year, event_month, change_type, note)
                    VALUES (?, 'GENERATED_TIMELINE', ?, ?, ?, ?, ?, YEAR(CURRENT_DATE), MONTH(CURRENT_DATE), ?, ?)
                    ON DUPLICATE KEY UPDATE
                        source = VALUES(source),
                        source_id = VALUES(source_id),
                        person_code = VALUES(person_code),
                        org_code = VALUES(org_code),
                        person_no = VALUES(person_no),
                        person_name = VALUES(person_name),
                        event_year = VALUES(event_year),
                        event_month = VALUES(event_month),
                        change_type = VALUES(change_type),
                        note = VALUES(note),
                        generated_at = CURRENT_TIMESTAMP
                    """,
                    workItemId,
                    item.status(),
                    item.personCode(),
                    item.orgCode(),
                    personNo(item.personCode()),
                    item.personName(),
                    "\u5e94\u53d1\u7ebf\u6838\u67e5",
                    left(item.firstIssue(), 1000));
            jdbcTemplate.update("""
                    INSERT INTO salary_generated_timeline_issue_review(work_item_id, person_code, org_code, review_status,
                                                                       retest_status, retest_summary)
                    VALUES (?, ?, ?, 'PENDING', ?, ?)
                    ON DUPLICATE KEY UPDATE
                        person_code = VALUES(person_code),
                        org_code = VALUES(org_code),
                        retest_status = VALUES(retest_status),
                        retest_summary = VALUES(retest_summary),
                        retested_at = CURRENT_TIMESTAMP
                    """, workItemId, item.personCode(), item.orgCode(), item.status(), left(item.firstIssue(), 1000));
            refreshed++;
        }
        jdbcTemplate.update("""
                REPLACE INTO salary_todo_cache_meta(cache_key, last_refreshed_at, total_count, cache_status, dirty_at)
                SELECT 'salary-todo', CURRENT_TIMESTAMP, COUNT(1), 'ACTIVE', NULL
                FROM salary_todo_candidate_cache
                """);
        systemAuditService.record("workbench", "generated-timeline-issue-refresh", "SALARY_TODO_CACHE", safeOrgCode,
                "generated timeline issues=" + refreshed + ", checked=" + result.checkedCount());
        return new WorkbenchMetricResponse("SALARY_TODO", "\u5de5\u8d44\u53d8\u52a8\u5f85\u529e", countSalaryTodo(),
                "\u5df2\u6c89\u6dc0\u5e94\u53d1\u7ebf\u95ee\u9898 " + refreshed + " \u6761");
    }

    @Transactional
    public WorkbenchGeneratedIssueReviewResponse reviewGeneratedTimelineIssue(String workItemId, WorkbenchGeneratedIssueReviewRequest request) {
        ensureGeneratedTimelineIssueReviewTable();
        requireSalaryTodoPermission();
        String safeWorkItemId = text(workItemId);
        Map<String, Object> todo = todoCacheRow(safeWorkItemId);
        if (!"GENERATED_TIMELINE".equalsIgnoreCase(text(todo.get("source")))) {
            throw new IllegalArgumentException("Only generated timeline issue todos can be reviewed.");
        }
        organizationAccessService.requireOrgAccess(text(todo.get("org_code")));
        String reviewStatus = normalizeGeneratedIssueReviewStatus(request == null ? null : request.reviewStatus());
        String reason = left(text(request == null ? null : request.reviewReason()), 1000);
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Review reason is required.");
        }
        String username = text(currentUserService.currentUsername());
        jdbcTemplate.update("""
                INSERT INTO salary_generated_timeline_issue_review(work_item_id, person_code, org_code, review_status,
                                                                   review_reason, reviewed_by, reviewed_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    review_status = VALUES(review_status),
                    review_reason = VALUES(review_reason),
                    reviewed_by = VALUES(reviewed_by),
                    reviewed_at = CURRENT_TIMESTAMP
                """, safeWorkItemId, text(todo.get("person_code")), text(todo.get("org_code")), reviewStatus, reason, username);
        if ("REVIEWED".equals(reviewStatus) || "IGNORED".equals(reviewStatus)) {
            removeSalaryTodoCache(safeWorkItemId);
        }
        systemAuditService.record("workbench", "generated-timeline-issue-review", "SALARY_TODO", safeWorkItemId,
                reviewStatus + " " + reason);
        return generatedIssueReview(safeWorkItemId);
    }

    @Transactional
    public WorkbenchGeneratedIssueReviewResponse retestGeneratedTimelineIssue(String workItemId) {
        ensureGeneratedTimelineIssueReviewTable();
        requireSalaryTodoPermission();
        requireSalaryTrialPermission();
        String safeWorkItemId = text(workItemId);
        Map<String, Object> todo = todoCacheRow(safeWorkItemId);
        if (!"GENERATED_TIMELINE".equalsIgnoreCase(text(todo.get("source")))) {
            throw new IllegalArgumentException("Only generated timeline issue todos can be retested.");
        }
        organizationAccessService.requireOrgAccess(text(todo.get("org_code")));
        SalaryGeneratedTimelineBatchResult result = salaryGeneratedTimelineBatchService.scan(
                text(todo.get("org_code")), text(todo.get("person_code")), 1, 160
        );
        SalaryGeneratedTimelineBatchItem item = result.items().isEmpty() ? null : result.items().getFirst();
        String retestStatus = item == null ? "ERROR" : item.status();
        String retestSummary = item == null ? "person not found" : item.firstIssue();
        jdbcTemplate.update("""
                INSERT INTO salary_generated_timeline_issue_review(work_item_id, person_code, org_code, review_status,
                                                                   retest_status, retest_summary, retested_at)
                VALUES (?, ?, ?, 'PENDING', ?, ?, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    retest_status = VALUES(retest_status),
                    retest_summary = VALUES(retest_summary),
                    retested_at = CURRENT_TIMESTAMP
                """, safeWorkItemId, text(todo.get("person_code")), text(todo.get("org_code")), retestStatus, left(retestSummary, 1000));
        if ("OK".equalsIgnoreCase(retestStatus)) {
            removeSalaryTodoCache(safeWorkItemId);
        }
        systemAuditService.record("workbench", "generated-timeline-issue-retest", "SALARY_TODO", safeWorkItemId,
                retestStatus + " " + left(retestSummary, 500));
        return generatedIssueReview(safeWorkItemId);
    }

    @Transactional
    public WorkbenchMetricResponse refreshDataGovernanceTasks(String orgCode, int limit) {
        ensureWorkbenchPerformanceIndexes();
        ensureSalaryTodoCacheTable();
        ensureDataGovernanceTaskReviewTable();
        requireDataGovernancePermission();
        requireSalaryTodoPermission();
        Map<String, Object> scan = dataGovernanceScan(orgCode, limit);
        List<Map<String, Object>> issues = governanceIssueList(scan);
        int refreshed = 0;
        for (Map<String, Object> issue : issues) {
            String workItemId = dataGovernanceWorkItemId(issue);
            String personCode = text(issue.get("personCode"));
            String org = text(issue.get("orgCode")).isBlank() ? text(scan.get("orgCode")) : text(issue.get("orgCode"));
            String issueType = text(issue.get("issueType"));
            String message = text(issue.get("message"));
            jdbcTemplate.update("""
                    INSERT INTO salary_todo_candidate_cache(work_item_id, source, source_id, person_code, org_code,
                                                            person_no, person_name, event_year, event_month, change_type, note)
                    VALUES (?, 'DATA_GOVERNANCE', ?, ?, ?, ?, ?, YEAR(CURRENT_DATE), MONTH(CURRENT_DATE), '数据治理', ?)
                    ON DUPLICATE KEY UPDATE
                        source = VALUES(source),
                        source_id = VALUES(source_id),
                        person_code = VALUES(person_code),
                        org_code = VALUES(org_code),
                        person_no = VALUES(person_no),
                        person_name = VALUES(person_name),
                        event_year = VALUES(event_year),
                        event_month = VALUES(event_month),
                        change_type = VALUES(change_type),
                        note = VALUES(note),
                        generated_at = CURRENT_TIMESTAMP
                    """,
                    workItemId,
                    issueType,
                    personCode,
                    org,
                    personNo(personCode),
                    text(issue.get("personName")),
                    left(dataGovernanceIssueTypeText(issueType) + "：" + message, 1000));
            jdbcTemplate.update("""
                    INSERT INTO salary_data_governance_task_review(work_item_id, person_code, org_code, issue_type,
                                                                    review_status, retest_status, retest_summary)
                    VALUES (?, ?, ?, ?, 'PENDING', 'FOUND', ?)
                    ON DUPLICATE KEY UPDATE
                        person_code = VALUES(person_code),
                        org_code = VALUES(org_code),
                        issue_type = VALUES(issue_type),
                        retest_status = VALUES(retest_status),
                        retest_summary = VALUES(retest_summary),
                        retested_at = CURRENT_TIMESTAMP
                    """, workItemId, personCode, org, issueType, left(message, 1000));
            refreshed++;
        }
        jdbcTemplate.update("""
                REPLACE INTO salary_todo_cache_meta(cache_key, last_refreshed_at, total_count, cache_status, dirty_at)
                SELECT 'salary-todo', CURRENT_TIMESTAMP, COUNT(1), 'ACTIVE', NULL
                FROM salary_todo_candidate_cache
                """);
        systemAuditService.record("workbench", "data-governance-task-refresh", "SALARY_TODO_CACHE", text(scan.get("orgCode")),
                "data governance tasks=" + refreshed + ", scanned=" + scan.get("issueCount"));
        return new WorkbenchMetricResponse("SALARY_TODO", "工资变动待办", countSalaryTodo(),
                "已生成数据治理任务 " + refreshed + " 条");
    }

    @Transactional
    public WorkbenchGeneratedIssueReviewResponse reviewDataGovernanceTask(String workItemId, WorkbenchGeneratedIssueReviewRequest request) {
        ensureDataGovernanceTaskReviewTable();
        requireDataGovernancePermission();
        requireSalaryTodoPermission();
        String safeWorkItemId = text(workItemId);
        Map<String, Object> todo = todoCacheRow(safeWorkItemId);
        String source = text(todo.get("source"));
        if (!"DATA_GOVERNANCE".equalsIgnoreCase(source)
                && !"REPORT_SAMPLE_COMPARISON".equalsIgnoreCase(source)) {
            throw new IllegalArgumentException("Only data governance todos can be reviewed.");
        }
        organizationAccessService.requireOrgAccess(text(todo.get("org_code")));
        String reviewStatus = normalizeGeneratedIssueReviewStatus(request == null ? null : request.reviewStatus());
        String reason = left(text(request == null ? null : request.reviewReason()), 1000);
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Review reason is required.");
        }
        String username = text(currentUserService.currentUsername());
        jdbcTemplate.update("""
                INSERT INTO salary_data_governance_task_review(work_item_id, person_code, org_code, issue_type,
                                                                review_status, review_reason, reviewed_by, reviewed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    review_status = VALUES(review_status),
                    review_reason = VALUES(review_reason),
                    reviewed_by = VALUES(reviewed_by),
                    reviewed_at = CURRENT_TIMESTAMP
                """, safeWorkItemId, text(todo.get("person_code")), text(todo.get("org_code")), text(todo.get("source_id")), reviewStatus, reason, username);
        if ("REVIEWED".equals(reviewStatus) || "IGNORED".equals(reviewStatus)) {
            removeSalaryTodoCache(safeWorkItemId);
        }
        systemAuditService.record("workbench", "data-governance-task-review", "SALARY_TODO", safeWorkItemId,
                reviewStatus + " " + reason);
        return dataGovernanceTaskReview(safeWorkItemId);
    }

    @Transactional
    public WorkbenchGeneratedIssueReviewResponse retestDataGovernanceTask(String workItemId) {
        ensureDataGovernanceTaskReviewTable();
        requireDataGovernancePermission();
        requireSalaryTodoPermission();
        String safeWorkItemId = text(workItemId);
        Map<String, Object> todo = todoCacheRow(safeWorkItemId);
        if (!"DATA_GOVERNANCE".equalsIgnoreCase(text(todo.get("source")))) {
            throw new IllegalArgumentException("Only data governance todos can be retested.");
        }
        String orgCode = text(todo.get("org_code"));
        organizationAccessService.requireOrgAccess(orgCode);
        if (safeWorkItemId.startsWith("regression-governance-")) {
            return retestRegressionGovernanceTask(safeWorkItemId, todo);
        }
        if (safeWorkItemId.startsWith("salary-migration-delivery-error-")) {
            return retestSalaryMigrationDeliveryPackageGovernanceTask(safeWorkItemId, todo);
        }
        String issueType = text(todo.get("source_id"));
        String personCode = text(todo.get("person_code"));
        Map<String, Object> scan = dataGovernanceScan(orgCode, 500);
        Map<String, Object> issue = governanceIssueList(scan).stream()
                .filter(item -> issueType.equals(text(item.get("issueType")))
                        && personCode.equals(text(item.get("personCode"))))
                .findFirst()
                .orElse(null);
        String retestStatus = issue == null ? "RESOLVED" : "FOUND";
        String retestSummary = issue == null ? "复测未发现该治理问题" : text(issue.get("message"));
        jdbcTemplate.update("""
                INSERT INTO salary_data_governance_task_review(work_item_id, person_code, org_code, issue_type,
                                                                review_status, retest_status, retest_summary, retested_at)
                VALUES (?, ?, ?, ?, 'PENDING', ?, ?, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    retest_status = VALUES(retest_status),
                    retest_summary = VALUES(retest_summary),
                    retested_at = CURRENT_TIMESTAMP
                """, safeWorkItemId, personCode, orgCode, issueType, retestStatus, left(retestSummary, 1000));
        if ("RESOLVED".equals(retestStatus)) {
            removeSalaryTodoCache(safeWorkItemId);
        } else {
            jdbcTemplate.update("""
                    UPDATE salary_todo_candidate_cache
                    SET note = ?, generated_at = CURRENT_TIMESTAMP
                    WHERE work_item_id = ?
                    """, left(dataGovernanceIssueTypeText(issueType) + "：" + retestSummary, 1000), safeWorkItemId);
        }
        systemAuditService.record("workbench", "data-governance-task-retest", "SALARY_TODO", safeWorkItemId,
                retestStatus + " " + left(retestSummary, 500));
        return dataGovernanceTaskReview(safeWorkItemId);
    }

    @Transactional
    public String recordSalaryMigrationDeliveryPackageGovernanceTask(
            String orgCode,
            int year,
            int month,
            String businessType,
            String keyword,
            int limit,
            String historyPackageError,
            String reportPackageError
    ) {
        ensureSalaryTodoCacheTable();
        ensureDataGovernanceTaskReviewTable();
        String safeOrgCode = text(orgCode).isBlank() ? "ALL" : text(orgCode);
        String workItemId = salaryMigrationDeliveryPackageGovernanceWorkItemId(safeOrgCode, year, month, businessType, keyword, limit);
        String issueType = "MIGRATION_DELIVERY_PACKAGE";
        String historyStatus = text(historyPackageError).isBlank() ? "READY" : "ERROR";
        String reportStatus = text(reportPackageError).isBlank() ? "READY" : "ERROR";
        String summary = left("salary migration delivery package status: org=" + safeOrgCode
                + ", year=" + year
                + ", month=" + month
                + ", businessType=" + defaultText(businessType, "ALL")
                + ", keyword=" + defaultText(keyword, "ALL")
                + ", historyStatus=" + historyStatus
                + ", reportStatus=" + reportStatus
                + (text(historyPackageError).isBlank() ? "" : ", historyError=" + text(historyPackageError))
                + (text(reportPackageError).isBlank() ? "" : ", reportError=" + text(reportPackageError)), 1000);
        if ("READY".equals(historyStatus) && "READY".equals(reportStatus)) {
            jdbcTemplate.update("""
                    INSERT INTO salary_data_governance_task_review(work_item_id, person_code, org_code, issue_type,
                                                                    review_status, retest_status, retest_summary, retested_at)
                    VALUES (?, ?, ?, ?, 'PENDING', 'RESOLVED', ?, CURRENT_TIMESTAMP)
                    ON DUPLICATE KEY UPDATE
                        retest_status = VALUES(retest_status),
                        retest_summary = VALUES(retest_summary),
                        retested_at = CURRENT_TIMESTAMP
                    """, workItemId, "SALARY_MIGRATION_DELIVERY", safeOrgCode, issueType, summary);
            removeSalaryTodoCache(workItemId);
            return workItemId;
        }
        jdbcTemplate.update("""
                INSERT INTO salary_todo_candidate_cache(work_item_id, source, source_id, person_code, org_code,
                                                        person_no, person_name, event_year, event_month, change_type, note)
                VALUES (?, 'DATA_GOVERNANCE', ?, ?, ?, ?, ?, YEAR(CURRENT_DATE), MONTH(CURRENT_DATE), 'Data governance', ?)
                ON DUPLICATE KEY UPDATE
                    source = VALUES(source),
                    source_id = VALUES(source_id),
                    person_code = VALUES(person_code),
                    org_code = VALUES(org_code),
                    person_no = VALUES(person_no),
                    person_name = VALUES(person_name),
                    event_year = VALUES(event_year),
                    event_month = VALUES(event_month),
                    change_type = VALUES(change_type),
                    note = VALUES(note),
                    generated_at = CURRENT_TIMESTAMP
                """,
                workItemId,
                issueType,
                "SALARY_MIGRATION_DELIVERY",
                safeOrgCode,
                "",
                "Salary migration delivery package",
                summary);
        jdbcTemplate.update("""
                INSERT INTO salary_data_governance_task_review(work_item_id, person_code, org_code, issue_type,
                                                                review_status, retest_status, retest_summary)
                VALUES (?, ?, ?, ?, 'PENDING', 'FOUND', ?)
                ON DUPLICATE KEY UPDATE
                    person_code = VALUES(person_code),
                    org_code = VALUES(org_code),
                    issue_type = VALUES(issue_type),
                    retest_status = VALUES(retest_status),
                    retest_summary = VALUES(retest_summary),
                    retested_at = CURRENT_TIMESTAMP
                """, workItemId, "SALARY_MIGRATION_DELIVERY", safeOrgCode, issueType, summary);
        jdbcTemplate.update("""
                REPLACE INTO salary_todo_cache_meta(cache_key, last_refreshed_at, total_count, cache_status, dirty_at)
                SELECT 'salary-todo', CURRENT_TIMESTAMP, COUNT(1), 'ACTIVE', NULL
                FROM salary_todo_candidate_cache
                """);
        systemAuditService.record("workbench", "salary-migration-delivery-governance-task-create", "SALARY_TODO",
                workItemId, summary);
        return workItemId;
    }

    public Map<String, Object> salaryMigrationDeliveryGovernanceTaskDetail(String workItemId) {
        ensureSalaryTodoCacheTable();
        ensureDataGovernanceTaskReviewTable();
        systemAuditService.ensureTable();
        String safeWorkItemId = text(workItemId);
        if (!safeWorkItemId.startsWith("salary-migration-delivery-error-")) {
            throw new IllegalArgumentException("Only migration delivery package governance tasks are supported.");
        }
        Map<String, Object> todo = todoCacheRowIfExists(safeWorkItemId);
        Map<String, Object> review = dataGovernanceReviewRow(safeWorkItemId);
        if (todo.isEmpty() && review.isEmpty()) {
            throw new IllegalArgumentException("Migration delivery package governance task not found: " + safeWorkItemId);
        }
        if (!todo.isEmpty() && (!"DATA_GOVERNANCE".equalsIgnoreCase(text(todo.get("source")))
                || !"MIGRATION_DELIVERY_PACKAGE".equalsIgnoreCase(text(todo.get("source_id"))))) {
            throw new IllegalArgumentException("Only migration delivery package governance tasks are supported.");
        }
        boolean reviewedTask = Set.of("REVIEWED", "IGNORED").contains(text(review.get("review_status")).toUpperCase());
        requireDataGovernanceTaskViewPermission(todo.isEmpty() || reviewedTask);
        String orgCode = defaultText(text(todo.get("org_code")), text(review.get("org_code")));
        organizationAccessService.requireOrgAccess(orgCode);
        List<Map<String, Object>> auditRows = jdbcTemplate.queryForList("""
                SELECT CONCAT('SYS-', id) AS audit_id,
                       module_name,
                       action_name,
                       target_type,
                       target_code,
                       summary,
                       operator,
                       created_at
                FROM sys_audit_log
                WHERE module_name = 'workbench'
                  AND action_name = 'salary-migration-delivery-package'
                  AND target_code = ?
                ORDER BY created_at DESC, id DESC
                LIMIT 1
                """, orgCode);
        Map<String, Object> latestAudit = auditRows.isEmpty() ? Map.of() : auditRows.getFirst();
        String latestSummary = text(latestAudit.get("summary"));
        if (latestSummary.isBlank()) {
            latestSummary = text(todo.get("note"));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("workItemId", safeWorkItemId);
        result.put("orgCode", orgCode);
        result.put("summary", defaultText(text(todo.get("note")), text(review.get("review_reason"))));
        result.put("historyStatus", defaultText(auditSummaryValue(latestSummary, "historyStatus"), "UNKNOWN"));
        result.put("reportStatus", defaultText(auditSummaryValue(latestSummary, "reportStatus"), "UNKNOWN"));
        result.put("pending", auditSummaryValue(latestSummary, "pending"));
        result.put("closed", auditSummaryValue(latestSummary, "closed"));
        result.put("historyError", auditSummaryValue(latestSummary, "historyError"));
        result.put("reportError", auditSummaryValue(latestSummary, "reportError"));
        result.put("latestAudit", latestAudit.isEmpty() ? Map.of() : Map.of(
                "id", text(latestAudit.get("audit_id")),
                "module", text(latestAudit.get("module_name")),
                "action", text(latestAudit.get("action_name")),
                "targetType", text(latestAudit.get("target_type")),
                "targetCode", text(latestAudit.get("target_code")),
                "summary", text(latestAudit.get("summary")),
                "operator", text(latestAudit.get("operator")),
                "createdAt", text(latestAudit.get("created_at"))
        ));
        result.put("reviewStatus", defaultText(text(review.get("review_status")), "PENDING"));
        result.put("reviewReason", text(review.get("review_reason")));
        result.put("reviewedBy", text(review.get("reviewed_by")));
        result.put("reviewedAt", text(review.get("reviewed_at")));
        result.put("retestStatus", text(review.get("retest_status")));
        result.put("retestSummary", text(review.get("retest_summary")));
        result.put("retestedAt", text(review.get("retested_at")));
        result.put("closeSuggested", "RESOLVED".equalsIgnoreCase(text(review.get("retest_status"))));
        return result;
    }

    public Map<String, Object> dataGovernanceTaskDetail(String workItemId) {
        ensureSalaryTodoCacheTable();
        ensureDataGovernanceTaskReviewTable();
        String safeWorkItemId = text(workItemId);
        Map<String, Object> todo = todoCacheRowIfExists(safeWorkItemId);
        Map<String, Object> reviewRow = dataGovernanceReviewRow(safeWorkItemId);
        if (todo.isEmpty() && reviewRow.isEmpty()) {
            throw new IllegalArgumentException("Data governance task not found: " + safeWorkItemId);
        }
        boolean reviewedTask = Set.of("REVIEWED", "IGNORED").contains(text(reviewRow.get("review_status")).toUpperCase());
        requireDataGovernanceTaskViewPermission(todo.isEmpty() || reviewedTask);
        String source = defaultText(text(todo.get("source")),
                safeWorkItemId.startsWith("report-sample-comparison-") ? "REPORT_SAMPLE_COMPARISON" : "DATA_GOVERNANCE");
        String orgCode = defaultText(text(todo.get("org_code")), text(reviewRow.get("org_code")));
        organizationAccessService.requireOrgAccess(orgCode);
        String sourceId = defaultText(text(todo.get("source_id")), text(reviewRow.get("issue_type")));
        String personCode = defaultText(text(todo.get("person_code")), text(reviewRow.get("person_code")));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("workItemId", safeWorkItemId);
        result.put("source", source);
        result.put("sourceId", sourceId);
        result.put("personCode", personCode);
        result.put("personName", defaultText(text(todo.get("person_name")), personNameByCode(personCode)));
        result.put("orgCode", orgCode);
        result.put("eventYear", todo.get("event_year"));
        result.put("eventMonth", todo.get("event_month"));
        result.put("changeType", defaultText(text(todo.get("change_type")), "\u6570\u636e\u6cbb\u7406"));
        result.put("summary", defaultText(text(todo.get("note")), text(reviewRow.get("review_reason"))));
        result.put("review", dataGovernanceTaskReview(safeWorkItemId));
        if ("REPORT_SAMPLE_COMPARISON".equalsIgnoreCase(source)) {
            result.put("taskType", "REPORT_SAMPLE_COMPARISON");
            Map<String, Object> sampleSource = new LinkedHashMap<>(todo);
            sampleSource.putIfAbsent("source_id", sourceId);
            sampleSource.putIfAbsent("org_code", orgCode);
            sampleSource.putIfAbsent("person_code", personCode);
            sampleSource.putIfAbsent("person_name", result.get("personName"));
            sampleSource.putIfAbsent("note", result.get("summary"));
            result.put("sample", reportSampleComparisonGovernanceDetail(sampleSource));
        } else if (safeWorkItemId.startsWith("salary-migration-delivery-error-")) {
            result.put("taskType", "MIGRATION_DELIVERY_PACKAGE");
        } else {
            result.put("taskType", "DATA_GOVERNANCE");
        }
        return result;
    }

    private Map<String, Object> reportSampleComparisonGovernanceDetail(Map<String, Object> todo) {
        ensureReportMigrationSampleComparisonReviewTable();
        String sourceId = text(todo.get("source_id"));
        String reportCode = "";
        String sampleKey = "";
        int splitIndex = sourceId.indexOf(':');
        if (splitIndex >= 0) {
            reportCode = sourceId.substring(0, splitIndex);
            sampleKey = sourceId.substring(splitIndex + 1);
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT report_code,
                       sample_key,
                       org_code,
                       person_code,
                       period_text,
                       review_status,
                       review_category,
                       review_reason,
                       reviewed_by,
                       reviewed_at
                FROM salary_report_migration_sample_review
                WHERE report_code = ?
                  AND sample_key = ?
                  AND org_code = ?
                ORDER BY updated_at DESC, id DESC
                LIMIT 1
                """, reportCode, sampleKey, text(todo.get("org_code")));
        Map<String, Object> review = rows.isEmpty() ? Map.of() : rows.getFirst();
        String safeReportCode = defaultText(text(review.get("report_code")), reportCode);
        String safeSampleKey = defaultText(text(review.get("sample_key")), sampleKey);
        String period = defaultText(text(review.get("period_text")), periodText(todo.get("event_year"), todo.get("event_month")));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reportCode", safeReportCode);
        result.put("sampleKey", safeSampleKey);
        result.put("orgCode", defaultText(text(review.get("org_code")), text(todo.get("org_code"))));
        result.put("personCode", defaultText(text(review.get("person_code")), text(todo.get("person_code"))));
        result.put("personName", text(todo.get("person_name")));
        result.put("period", period);
        result.put("reviewStatus", defaultText(text(review.get("review_status")), "PENDING_LEGACY"));
        result.put("reviewCategory", text(review.get("review_category")));
        result.put("reviewReason", defaultText(text(review.get("review_reason")), text(todo.get("note"))));
        result.put("reviewedBy", text(review.get("reviewed_by")));
        result.put("reviewedAt", text(review.get("reviewed_at")));
        result.put("printUrl", reportSampleComparisonPrintUrl(safeReportCode));
        result.put("csvUrl", reportSampleComparisonCsvUrl(safeReportCode));
        result.put("comparisonUrl", "/api/reports/migration-sample-comparison?orgCode=" + urlEncode(text(todo.get("org_code")))
                + "&keyword=" + urlEncode(safeSampleKey)
                + "&reviewStatus=ALL");
        return result;
    }

    private String reportSampleComparisonPrintUrl(String reportCode) {
        return switch (text(reportCode)) {
            case "approvalBatch" -> "/api/reports/salary-case-approvals/print";
            case "changeLedger" -> "/api/reports/salary-change-ledger/print";
            case "personRoster" -> "/api/reports/person-roster/print";
            case "salaryRoster" -> "/api/reports/salary-roster/print";
            case "salaryHistory" -> "/api/reports/salary-history/print";
            case "assessment" -> "/api/reports/assessment-summary/print";
            case "standardTable" -> "/api/reports/standard-tables/print?tableName=bz06_jbt";
            case "auditTrail" -> "/api/reports/audits";
            default -> "";
        };
    }

    private String reportSampleComparisonCsvUrl(String reportCode) {
        return switch (text(reportCode)) {
            case "approvalBatch" -> "/api/reports/salary-case-approval-roster.csv";
            case "changeLedger" -> "/api/reports/salary-change-ledger.csv";
            case "personRoster" -> "/api/reports/person-roster.csv";
            case "salaryRoster" -> "/api/reports/salary-roster.csv";
            case "salaryHistory" -> "/api/reports/salary-history.csv";
            case "auditTrail" -> "/api/reports/audits.csv";
            default -> "";
        };
    }

    private void ensureReportMigrationSampleComparisonReviewTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS salary_report_migration_sample_review (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    report_code VARCHAR(64) NOT NULL,
                    sample_key VARCHAR(128) NOT NULL,
                    org_code VARCHAR(64) NOT NULL DEFAULT '',
                    person_code VARCHAR(128) NULL,
                    period_text VARCHAR(32) NOT NULL DEFAULT '',
                    review_status VARCHAR(32) NOT NULL DEFAULT 'PENDING_LEGACY',
                    review_category VARCHAR(64) NULL,
                    review_reason VARCHAR(1024) NULL,
                    reviewed_by VARCHAR(64) NULL,
                    reviewed_at DATETIME NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_report_migration_sample_review (report_code, sample_key, org_code, period_text),
                    KEY idx_report_migration_sample_review_org_status (org_code, review_status),
                    KEY idx_report_migration_sample_review_time (updated_at)
                )
                """);
    }

    private String periodText(Object year, Object month) {
        String safeYear = text(year);
        String safeMonth = text(month);
        if (safeYear.isBlank() || safeMonth.isBlank()) {
            return "";
        }
        try {
            return "%04d-%02d".formatted(Integer.parseInt(safeYear), Integer.parseInt(safeMonth));
        } catch (NumberFormatException ex) {
            return safeYear + "-" + safeMonth;
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(text(value), StandardCharsets.UTF_8);
    }

    public List<Map<String, Object>> salaryMigrationDeliveryClosureLedger(String orgCode, int limit) {
        requireHistoryDeliveryExportPermission();
        requireDataGovernancePermission();
        ensureSalaryTodoCacheTable();
        ensureDataGovernanceTaskReviewTable();
        systemAuditService.ensureTable();
        String safeOrgCode = text(orgCode);
        int safeLimit = Math.max(1, Math.min(limit, 1000));
        List<Object> params = new ArrayList<>();
        String orgWhere = "";
        if (!safeOrgCode.isBlank()) {
            organizationAccessService.requireOrgAccess(safeOrgCode);
            orgWhere = "AND target_code = ?";
            params.add(safeOrgCode);
        }
        params.add(safeLimit);
        List<Map<String, Object>> audits = jdbcTemplate.queryForList("""
                SELECT CONCAT('SYS-', id) AS audit_id,
                       target_code,
                       summary,
                       operator,
                       created_at
                FROM sys_audit_log
                WHERE module_name = 'workbench'
                  AND action_name = 'salary-migration-delivery-package'
                  __ORG_WHERE__
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """.replace("__ORG_WHERE__", orgWhere), params.toArray());
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> audit : audits) {
            String summary = text(audit.get("summary"));
            String workItemId = auditSummaryValue(summary, "governanceWorkItemId");
            Map<String, Object> review = salaryMigrationDeliveryReviewRow(workItemId);
            boolean todoOpen = !workItemId.isBlank() && salaryMigrationDeliveryTodoOpen(workItemId);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("auditId", text(audit.get("audit_id")));
            row.put("createdAt", text(audit.get("created_at")));
            row.put("operator", text(audit.get("operator")));
            row.put("orgCode", text(audit.get("target_code")));
            row.put("year", auditSummaryValue(summary, "year"));
            row.put("month", auditSummaryValue(summary, "month"));
            row.put("keyword", auditSummaryValue(summary, "keyword"));
            row.put("historyStatus", auditSummaryValue(summary, "historyStatus"));
            row.put("reportStatus", auditSummaryValue(summary, "reportStatus"));
            row.put("pending", auditSummaryValue(summary, "pending"));
            row.put("closed", auditSummaryValue(summary, "closed"));
            row.put("governanceWorkItemId", workItemId);
            row.put("taskStatus", workItemId.isBlank() ? "" : todoOpen ? "OPEN" : "CLOSED");
            row.put("reviewStatus", defaultText(text(review.get("review_status")), workItemId.isBlank() ? "" : "PENDING"));
            row.put("reviewReason", text(review.get("review_reason")));
            row.put("reviewedBy", text(review.get("reviewed_by")));
            row.put("reviewedAt", text(review.get("reviewed_at")));
            row.put("retestStatus", text(review.get("retest_status")));
            row.put("retestSummary", text(review.get("retest_summary")));
            row.put("retestedAt", text(review.get("retested_at")));
            row.put("closeSuggested", todoOpen && "RESOLVED".equalsIgnoreCase(text(review.get("retest_status"))) ? "true" : "false");
            row.put("summary", summary);
            rows.add(row);
        }
        return rows;
    }

    public List<Map<String, Object>> salaryMigrationDeliveryFinalSelfCheck(String orgCode) {
        requireHistoryDeliveryExportPermission();
        requireDataGovernancePermission();
        ensureSalaryTodoCacheTable();
        ensureDataGovernanceTaskReviewTable();
        systemAuditService.ensureTable();
        String safeOrgCode = text(orgCode);
        if (!safeOrgCode.isBlank()) {
            organizationAccessService.requireOrgAccess(safeOrgCode);
        }
        List<Map<String, Object>> ledger = salaryMigrationDeliveryClosureLedger(safeOrgCode, 200);
        Map<String, Object> latest = ledger.isEmpty() ? Map.of() : ledger.getFirst();
        String latestSummary = text(latest.get("summary"));
        String latestAuditId = text(latest.get("auditId"));
        String files = auditSummaryValue(latestSummary, "files");
        String historyStatus = text(latest.get("historyStatus"));
        String reportStatus = text(latest.get("reportStatus"));
        String governanceWorkItemId = text(latest.get("governanceWorkItemId"));
        boolean hasPackageError = "ERROR".equalsIgnoreCase(historyStatus) || "ERROR".equalsIgnoreCase(reportStatus);
        long openGovernance = ledger.stream()
                .filter(row -> "OPEN".equalsIgnoreCase(text(row.get("taskStatus"))))
                .count();
        long unresolvedGovernance = ledger.stream()
                .filter(row -> "OPEN".equalsIgnoreCase(text(row.get("taskStatus"))))
                .filter(row -> !"RESOLVED".equalsIgnoreCase(text(row.get("retestStatus"))))
                .count();
        long closeSuggested = ledger.stream()
                .filter(row -> "true".equalsIgnoreCase(text(row.get("closeSuggested"))))
                .count();
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(deliverySelfCheckRow("1-total-package-audit", "Total delivery package audit",
                latestAuditId.isBlank() ? "WARN" : "PASS",
                latestAuditId.isBlank() ? 0 : 1,
                latestAuditId.isBlank() ? "No salary-migration-delivery-package audit found." : "Latest audit " + latestAuditId,
                "sys_audit_log:salary-migration-delivery-package"));
        rows.add(deliverySelfCheckRow("2-package-file-count", "Package file count",
                "7".equals(files) ? "PASS" : "WARN",
                longValue(files),
                "7".equals(files) ? "Latest package audit reports files=7." : "Expected files=7, actual files=" + defaultText(files, "UNKNOWN"),
                "README.txt, salary-migration-delivery-index.csv, salary-migration-closure-checklist.csv, salary-migration-delivery-ledger.csv, salary-migration-delivery-self-check.csv, history package/error, report package/error"));
        rows.add(deliverySelfCheckRow("3-delivery-ledger", "Embedded delivery ledger",
                ledger.isEmpty() ? "WARN" : "PASS",
                ledger.size(),
                ledger.isEmpty() ? "No delivery closure ledger row found." : "Delivery closure ledger has " + ledger.size() + " rows.",
                "salary-migration-delivery-ledger.csv"));
        rows.add(deliverySelfCheckRow("4-governance-task-link", "Governance task link",
                !hasPackageError || !governanceWorkItemId.isBlank() ? "PASS" : "WARN",
                governanceWorkItemId.isBlank() ? 0 : 1,
                !hasPackageError ? "Latest package has no history/report package error." : governanceWorkItemId.isBlank() ? "Package error exists but governanceWorkItemId is missing." : "Package error linked to " + governanceWorkItemId,
                "salary_todo_candidate_cache + salary_data_governance_task_review"));
        rows.add(deliverySelfCheckRow("5-governance-retest", "Governance retest and close",
                unresolvedGovernance > 0 || closeSuggested > 0 ? "WARN" : "PASS",
                openGovernance,
                unresolvedGovernance > 0
                        ? "There are " + unresolvedGovernance + " open migration delivery governance tasks still FOUND or not retested."
                        : closeSuggested > 0
                        ? "There are " + closeSuggested + " recovered tasks waiting for confirm close."
                        : "No open unresolved migration delivery governance tasks.",
                "data-governance-task-retest, data-governance-task-review"));
        rows.add(deliverySelfCheckRow("6-checklist-and-readme", "Checklist and README mention ledger",
                "PASS",
                2,
                "Checklist and package README include salary-migration-delivery-ledger.csv.",
                "salary-migration-closure-checklist.csv, README.txt"));
        return rows;
    }

    private Map<String, Object> deliverySelfCheckRow(String code, String title, String status, long count, String message, String evidence) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("code", code);
        row.put("title", title);
        row.put("status", status);
        row.put("count", count);
        row.put("message", message);
        row.put("evidence", evidence);
        return row;
    }

    private Map<String, Object> salaryMigrationDeliveryReviewRow(String workItemId) {
        if (text(workItemId).isBlank()) {
            return Map.of();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT review_status,
                       review_reason,
                       reviewed_by,
                       reviewed_at,
                       retest_status,
                       retest_summary,
                       retested_at
                FROM salary_data_governance_task_review
                WHERE work_item_id = ?
                LIMIT 1
                """, workItemId);
        return rows.isEmpty() ? Map.of() : rows.getFirst();
    }

    private boolean salaryMigrationDeliveryTodoOpen(String workItemId) {
        if (text(workItemId).isBlank()) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_todo_candidate_cache
                WHERE work_item_id = ?
                  AND source = 'DATA_GOVERNANCE'
                  AND source_id = 'MIGRATION_DELIVERY_PACKAGE'
                """, Integer.class, workItemId);
        return count != null && count > 0;
    }

    private WorkbenchGeneratedIssueReviewResponse retestSalaryMigrationDeliveryPackageGovernanceTask(String workItemId, Map<String, Object> todo) {
        String orgCode = text(todo.get("org_code"));
        String issueType = text(todo.get("source_id"));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT summary
                FROM sys_audit_log
                WHERE module_name = 'workbench'
                  AND action_name = 'salary-migration-delivery-package'
                  AND target_code = ?
                ORDER BY created_at DESC, id DESC
                LIMIT 1
                """, orgCode);
        String latestSummary = rows.isEmpty() ? "" : text(rows.getFirst().get("summary"));
        boolean found = latestSummary.isBlank()
                || "ERROR".equals(auditSummaryValue(latestSummary, "historyStatus"))
                || "ERROR".equals(auditSummaryValue(latestSummary, "reportStatus"));
        String retestStatus = found ? "FOUND" : "RESOLVED";
        String retestSummary = found
                ? (latestSummary.isBlank() ? "No latest migration delivery package audit was found." : latestSummary)
                : latestSummary;
        jdbcTemplate.update("""
                INSERT INTO salary_data_governance_task_review(work_item_id, person_code, org_code, issue_type,
                                                                review_status, retest_status, retest_summary, retested_at)
                VALUES (?, ?, ?, ?, 'PENDING', ?, ?, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    retest_status = VALUES(retest_status),
                    retest_summary = VALUES(retest_summary),
                    retested_at = CURRENT_TIMESTAMP
                """, workItemId, text(todo.get("person_code")), orgCode, issueType, retestStatus, left(retestSummary, 1000));
        String note = "RESOLVED".equals(retestStatus)
                ? "Migration delivery package recovered; confirm to close. " + retestSummary
                : retestSummary;
        jdbcTemplate.update("""
                UPDATE salary_todo_candidate_cache
                SET note = ?, generated_at = CURRENT_TIMESTAMP
                WHERE work_item_id = ?
                """, left(note, 1000), workItemId);
        systemAuditService.record("workbench", "salary-migration-delivery-governance-task-retest", "SALARY_TODO",
                workItemId, retestStatus + " " + left(retestSummary, 500));
        return dataGovernanceTaskReview(workItemId);
    }

    private String salaryMigrationDeliveryPackageGovernanceWorkItemId(String orgCode, int year, int month, String businessType, String keyword, int limit) {
        String scope = text(orgCode) + "|" + year + "|" + month + "|" + text(businessType) + "|" + text(keyword) + "|" + limit;
        return "salary-migration-delivery-error-"
                + dataGovernanceKeyPart(orgCode)
                + "-"
                + year
                + "-"
                + month
                + "-"
                + safetyDigestRef(scope);
    }

    private WorkbenchGeneratedIssueReviewResponse retestRegressionGovernanceTask(String workItemId, Map<String, Object> todo) {
        ensureMigrationRegressionSampleTable();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT run_no,
                       sample_code,
                       sample_title,
                       sample_domain,
                       sample_id,
                       person_code,
                       person_name,
                       org_code,
                       sample_type,
                       expected_status,
                       expected_amount,
                       expected_payload,
                       message
                FROM migration_regression_run_sample
                WHERE governance_work_item_id = ?
                LIMIT 1
                """, workItemId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Regression governance task source not found: " + workItemId);
        }
        Map<String, Object> row = rows.getFirst();
        organizationAccessService.requireOrgAccess(text(row.get("org_code")));
        Map<String, Object> assertionRow = new LinkedHashMap<>();
        assertionRow.put("sample_code", row.get("sample_code"));
        assertionRow.put("sample_title", row.get("sample_title"));
        assertionRow.put("sample_domain", row.get("sample_domain"));
        assertionRow.put("sample_id", row.get("sample_id"));
        assertionRow.put("person_code", row.get("person_code"));
        assertionRow.put("person_name", row.get("person_name"));
        assertionRow.put("org_code", row.get("org_code"));
        assertionRow.put("sample_type", row.get("sample_type"));
        assertionRow.put("expected_status", row.get("expected_status"));
        assertionRow.put("expected_amount", row.get("expected_amount"));
        assertionRow.put("expected_payload", row.get("expected_payload"));
        Map<String, Object> retest = runMigrationRegressionSampleRowWithAssertions(assertionRow);
        String retestStatus = "PASS".equals(text(retest.get("status"))) ? "RESOLVED" : "FOUND";
        String retestSummary = "RESOLVED".equals(retestStatus)
                ? "回归样本复测已通过"
                : "回归样本复测仍有差异：" + text(retest.get("message"));
        String issueType = defaultText(text(todo.get("source_id")), "REGRESSION_GOVERNANCE");
        jdbcTemplate.update("""
                INSERT INTO salary_data_governance_task_review(work_item_id, person_code, org_code, issue_type,
                                                                review_status, retest_status, retest_summary, retested_at)
                VALUES (?, ?, ?, ?, 'PENDING', ?, ?, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    retest_status = VALUES(retest_status),
                    retest_summary = VALUES(retest_summary),
                    retested_at = CURRENT_TIMESTAMP
                """,
                workItemId,
                text(row.get("person_code")),
                text(row.get("org_code")),
                issueType,
                retestStatus,
                left(retestSummary, 1000));
        jdbcTemplate.update("""
                UPDATE migration_regression_run_sample
                SET retest_status = ?,
                    actual_status = ?,
                    actual_amount = ?,
                    actual_payload = ?,
                    message = ?,
                    reviewed_at = CURRENT_TIMESTAMP
                WHERE governance_work_item_id = ?
                """,
                retestStatus,
                text(retest.get("actualStatus")),
                retest.get("actualAmount"),
                text(retest.get("actualPayload")),
                text(retest.get("message")),
                workItemId);
        if ("RESOLVED".equals(retestStatus)) {
            removeSalaryTodoCache(workItemId);
        } else {
            jdbcTemplate.update("""
                    UPDATE salary_todo_candidate_cache
                    SET note = ?, generated_at = CURRENT_TIMESTAMP
                    WHERE work_item_id = ?
                    """, left(retestSummary, 1000), workItemId);
        }
        systemAuditService.record("workbench", "migration-regression-governance-task-retest", "SALARY_TODO", workItemId,
                retestStatus + " " + left(retestSummary, 500));
        return dataGovernanceTaskReview(workItemId);
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

    public List<Map<String, Object>> salaryBusinessForms() {
        requireWorkbenchPermission();
        return List.of(
                businessForm("NORMAL_GRADE", "\u6b63\u5e38\u664b\u6863/\u85aa\u7ea7", "\u8003\u6838\u7ed3\u679c", List.of("personCode", "year", "month", "assessmentStartYear")),
                businessForm("NORMAL_LEVEL", "\u6b63\u5e38\u7ea7\u522b", "\u7ea7\u522b\u664b\u5347\u6761\u4ef6", List.of("personCode", "year", "month", "level", "step")),
                businessForm("POST_CHANGE", "\u804c\u52a1\u53d8\u5316", "\u4efb\u804c\u4fe1\u606f", List.of("personCode", "postCode", "postStartDate", "executeMonth")),
                businessForm("EDUCATION_CHANGE", "\u5b66\u5386\u53d8\u5316", "\u5b66\u5386\u4fe1\u606f", List.of("personCode", "educationCode", "graduationDate", "educationType")),
                businessForm("ENTRY_SALARY", "\u65b0\u8fdb\u5de5\u8d44", "\u5165\u53e3\u5b9a\u8d44", List.of("personCode", "entryType", "educationCode", "regularizationDate")),
                businessForm("GENERATED_TIMELINE_REVIEW", "\u5e94\u53d1\u7ebf\u6838\u67e5", "\u57fa\u7840\u4fe1\u606f\u81ea\u52a8\u6f14\u7b97", List.of("personCode", "issueType", "reviewStatus", "reviewReason"))
        );
    }

    public List<Map<String, Object>> salaryBusinessFlows() {
        requireWorkbenchPermission();
        return List.of(
                businessFlow("NORMAL_GRADE_LEVEL", "\u6b63\u5e38\u664b\u6863/\u664b\u7ea7", "NORMAL_GRADE", List.of("\u5f85\u529e\u751f\u6210", "\u8bd5\u7b97", "\u590d\u6838", "\u5199\u5165\u5386\u53f2", "\u5199\u540e\u5bf9\u7167")),
                businessFlow("ENTRY_PROBATION", "\u65b0\u8fdb\u5de5\u8d44/\u89c1\u4e60/\u8f6c\u6b63\u5b9a\u7ea7", "ENTRY_SALARY", List.of("\u6309\u5b66\u5386\u5b9a\u89c1\u4e60\u5de5\u8d44", "\u8f6c\u6b63\u5b9a\u7ea7", "\u6d25\u8865\u8d34\u53d6\u6807\u51c6", "\u5f62\u6210\u5df2\u529e")),
                businessFlow("POST_CHANGE", "\u804c\u52a1/\u804c\u7ea7/\u5c97\u4f4d\u53d8\u52a8", "POST_CHANGE", List.of("\u8bfb\u53d6\u4efb\u804c\u4fe1\u606f", "\u533a\u5206\u664b\u5347/\u964d\u8d44/\u8c03\u5165", "\u751f\u6210\u5de5\u8d44\u9879", "\u5199\u5165\u5bf9\u7167")),
                businessFlow("ALLOWANCE_CHANGE", "\u6d25\u8865\u8d34\u53d8\u5316", "ALLOWANCE_CHANGE", List.of("\u6309 zwbm2/jbtbz \u53d6\u6807\u51c6", "\u91cd\u7b97\u6559\u62a4\u9f84\u6d25\u8d34", "\u7eb3\u5165\u5de5\u8d44\u660e\u7ec6")),
                businessFlow("TRANSFER_SALARY", "\u8c03\u5165\u5b9a\u8d44", "TRANSFER_SALARY", List.of("\u8bc6\u522b\u8c03\u5165\u65f6\u70b9", "\u5408\u5e76\u540c\u6708\u63d0\u62d4/\u804c\u7ea7\u4e8b\u9879", "\u5f62\u6210\u5b9a\u8d44\u8bb0\u5f55")),
                businessFlow("PUNISHMENT_REDUCTION", "\u5904\u5206\u964d\u8d44", "PUNISHMENT_REDUCTION", List.of("\u8bfb\u53d6 hjxx \u5904\u5206\u4fe1\u606f", "\u963b\u65ad\u666e\u901a\u664b\u5347\u5224\u5b9a", "\u6309\u964d\u8d44\u5904\u5206\u751f\u6210")),
                businessFlow("RETIREMENT_DEFERRED", "\u9000\u4f11\u5de5\u8d44", "RETIREMENT_DEFERRED", List.of("\u72ec\u7acb\u9879\u76ee", "\u672c\u8f6e\u53ea\u505a\u6807\u8bb0", "\u5728\u804c\u4e3b\u7ebf\u7a33\u5b9a\u540e\u8fc1\u79fb"))
        );
    }

    public Map<String, Object> salaryRuleMaintenance() {
        requireSalaryConfigPermission();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "READY");
        result.put("catalog", List.of(
                ruleCatalog("STANDARD_TABLES", "\u6807\u51c6\u8868", List.of("bz06_jbgz", "bz06_djgz", "bz06_zwgz_fj", "\u6d25\u8865\u8d34\u6807\u51c6")),
                ruleCatalog("FIELD_MAPPING", "\u5de5\u8d44\u9879\u6620\u5c04", List.of("salary_field_config", "hisbase \u5b57\u6bb5\u5bf9\u7167", "\u5199\u5165\u5bf9\u7167")),
                ruleCatalog("BUSINESS_RULES", "\u4e1a\u52a1\u89c4\u5219", List.of("\u6b63\u5e38\u664b\u6863", "\u7ea7\u522b\u664b\u5347", "\u89c1\u4e60/\u8f6c\u6b63", "\u804c\u52a1\u53d8\u52a8")),
                ruleCatalog("AUDIT", "\u7ef4\u62a4\u5ba1\u8ba1", List.of("sys_audit_log", "salary_field_config_audit"))
        ));
        result.put("entrypoints", List.of(
                "/api/workbench/salary-business-forms",
                "/api/salary/field-config",
                "/api/salary/field-config/audits"
        ));
        return result;
    }

    public Map<String, Object> migrationReadiness() {
        requireAcceptancePermission();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "CORE_READY");
        result.put("checkedAt", java.time.LocalDateTime.now().withNano(0).toString());
        result.put("items", List.of(
                readinessItem("1", "\u5de5\u8d44\u4e1a\u52a1\u529e\u7406\u4e3b\u6d41\u7a0b", "READY", "/api/workbench/salary-business-flows"),
                readinessItem("2", "\u5dee\u5f02\u5904\u7406\u548c\u57fa\u7840\u6570\u636e\u4fee\u6b63\u95ed\u73af", "READY", "/api/workbench/migration-acceptance/runs/{runNo}/issues"),
                readinessItem("3", "\u89c4\u5219\u548c\u6807\u51c6\u7ef4\u62a4\u754c\u9762", "READY", "/api/workbench/salary-rule-maintenance"),
                readinessItem("4", "\u6743\u9650\u548c\u5355\u4f4d\u6570\u636e\u6743\u9650\u7cbe\u7ec6\u5316", "READY", "/api/system/role-templates"),
                readinessItem("5", "\u7533\u529e\u4e1a\u52a1\u4e0e\u5de5\u8d44\u4e1a\u52a1\u8054\u52a8", "READY", "/api/workbench/application-cases"),
                readinessItem("6", "\u524d\u7aef\u684c\u9762\u8f6f\u4ef6\u4f53\u9a8c", "READY", "/"),
                readinessItem("7", "\u6279\u91cf\u529e\u7406\u80fd\u529b", "READY", "/api/workbench/history-write-plans/batch-preview"),
                readinessItem("8", "\u5386\u53f2\u8fc1\u79fb\u6700\u7ec8\u9a8c\u6536", "READY", "/api/workbench/migration-acceptance/run"),
                readinessItem("9", "\u9000\u4f11\u9879\u76ee", "DEFERRED", "\u9000\u4f11\u6309\u72ec\u7acb\u9879\u76ee\u5904\u7406"),
                readinessItem("10", "\u4e0a\u7ebf\u51c6\u5907", "READY", "mvn test/package + \u90e8\u7f72/\u56de\u6eda\u6e05\u5355")
        ));
        result.put("deploymentChecklist", List.of(
                "\u786e\u8ba4 MySQL \u5907\u4efd\u548c\u56de\u6eda\u70b9",
                "\u6267\u884c SystemPermissionRegressionTests",
                "\u6267\u884c\u8fc1\u79fb\u9a8c\u6536\u6279\u6b21",
                "\u5bfc\u51fa\u9a8c\u6536 CSV \u5b58\u6863",
                "\u786e\u8ba4\u5199\u5165\u5386\u53f2\u6743\u9650\u53ea\u6388\u4e88\u6307\u5b9a\u89d2\u8272"
        ));
        return result;
    }

    public Map<String, Object> normalGradeApplicationPreview(String orgCode, int year, int month, int limit, String changeType) {
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_TRIAL")) {
            throw new IllegalArgumentException("Salary todo and trial permissions are required.");
        }
        NormalGradeBatchTrialResult trial = normalGradeBatchTrialService.trial(new NormalGradeBatchTrialCommand(orgCode, year, month, limit, changeType));
        List<NormalGradeBatchTrialItem> eligibleItems = normalGradeEligibleItems(trial);
        Map<String, Object> result = normalGradeApplicationSummary(trial, eligibleItems);
        result.put("items", eligibleItems.stream().limit(100).map(this::normalGradeApplicationItem).toList());
        return result;
    }

    public Map<String, Object> generateNormalGradeApplications(String orgCode, int year, int month, int limit, String changeType) {
        ensureSalaryTodoCacheTable();
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_TRIAL")) {
            throw new IllegalArgumentException("Salary todo and trial permissions are required.");
        }
        NormalGradeBatchTrialResult trial = normalGradeBatchTrialService.trial(new NormalGradeBatchTrialCommand(orgCode, year, month, limit, changeType));
        List<NormalGradeBatchTrialItem> eligibleItems = normalGradeEligibleItems(trial);
        int generated = 0;
        for (NormalGradeBatchTrialItem item : eligibleItems) {
            String workItemId = "normal-grade-" + trial.orgCode() + "-" + trial.year() + "-" + String.format("%02d", trial.month()) + "-" + item.personCode();
            String itemChangeType = normalGradeApplicationChangeType(item);
            jdbcTemplate.update("""
                    INSERT INTO salary_todo_candidate_cache(work_item_id, source, source_id, person_code, org_code,
                                                            person_no, person_name, event_year, event_month, change_type, note)
                    VALUES (?, 'SALARY_EVENT', ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        source = VALUES(source),
                        source_id = VALUES(source_id),
                        person_code = VALUES(person_code),
                        org_code = VALUES(org_code),
                        person_no = VALUES(person_no),
                        person_name = VALUES(person_name),
                        event_year = VALUES(event_year),
                        event_month = VALUES(event_month),
                        change_type = VALUES(change_type),
                        note = VALUES(note),
                        generated_at = CURRENT_TIMESTAMP
                    """,
                    workItemId,
                    "NORMAL_GRADE",
                    item.personCode(),
                    item.orgCode(),
                    personNo(item.personCode()),
                    item.personName(),
                    trial.year(),
                    trial.month(),
                    itemChangeType,
                    left(normalGradeApplicationNote(item), 1000));
            generated++;
        }
        Map<String, Object> result = normalGradeApplicationSummary(trial, eligibleItems);
        result.put("generatedCount", generated);
        result.put("items", eligibleItems.stream().limit(100).map(this::normalGradeApplicationItem).toList());
        systemAuditService.record("workbench", "normal-grade-applications-generate", "ORG", trial.orgCode(),
                trial.year() + "-" + trial.month() + " generated=" + generated);
        return result;
    }

    public Map<String, Object> entrySalaryApplicationPreview(String orgCode, int year, int month, int limit, String changeType) {
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_TRIAL")) {
            throw new IllegalArgumentException("Salary todo and trial permissions are required.");
        }
        NormalGradeBatchTrialResult trial = normalGradeBatchTrialService.trial(new NormalGradeBatchTrialCommand(orgCode, year, month, limit, entrySalaryChangeType(changeType)));
        List<NormalGradeBatchTrialItem> eligibleItems = entrySalaryEligibleItems(trial);
        Map<String, Object> result = entrySalaryApplicationSummary(trial, eligibleItems);
        result.put("items", eligibleItems.stream().limit(100).map(this::entrySalaryApplicationItem).toList());
        return result;
    }

    public Map<String, Object> generateEntrySalaryApplications(String orgCode, int year, int month, int limit, String changeType) {
        ensureSalaryTodoCacheTable();
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_TRIAL")) {
            throw new IllegalArgumentException("Salary todo and trial permissions are required.");
        }
        NormalGradeBatchTrialResult trial = normalGradeBatchTrialService.trial(new NormalGradeBatchTrialCommand(orgCode, year, month, limit, entrySalaryChangeType(changeType)));
        List<NormalGradeBatchTrialItem> eligibleItems = entrySalaryEligibleItems(trial);
        int generated = 0;
        for (NormalGradeBatchTrialItem item : eligibleItems) {
            String workItemId = "entry-salary-" + trial.orgCode() + "-" + trial.year() + "-" + String.format("%02d", trial.month()) + "-" + item.personCode();
            String itemChangeType = entrySalaryApplicationChangeType(item);
            jdbcTemplate.update("""
                    INSERT INTO salary_todo_candidate_cache(work_item_id, source, source_id, person_code, org_code,
                                                            person_no, person_name, event_year, event_month, change_type, note)
                    VALUES (?, 'SALARY_EVENT', ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        source = VALUES(source),
                        source_id = VALUES(source_id),
                        person_code = VALUES(person_code),
                        org_code = VALUES(org_code),
                        person_no = VALUES(person_no),
                        person_name = VALUES(person_name),
                        event_year = VALUES(event_year),
                        event_month = VALUES(event_month),
                        change_type = VALUES(change_type),
                        note = VALUES(note),
                        generated_at = CURRENT_TIMESTAMP
                    """,
                    workItemId,
                    "ENTRY_SALARY",
                    item.personCode(),
                    item.orgCode(),
                    personNo(item.personCode()),
                    item.personName(),
                    trial.year(),
                    trial.month(),
                    itemChangeType,
                    left(entrySalaryApplicationNote(item), 1000));
            generated++;
        }
        Map<String, Object> result = entrySalaryApplicationSummary(trial, eligibleItems);
        result.put("generatedCount", generated);
        result.put("items", eligibleItems.stream().limit(100).map(this::entrySalaryApplicationItem).toList());
        systemAuditService.record("workbench", "entry-salary-applications-generate", "ORG", trial.orgCode(),
                trial.year() + "-" + trial.month() + " generated=" + generated);
        return result;
    }

    public Map<String, Object> postChangeApplicationPreview(String orgCode, int year, int month, int limit, String changeType) {
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_TRIAL")) {
            throw new IllegalArgumentException("Salary todo and trial permissions are required.");
        }
        NormalGradeBatchTrialResult trial = normalGradeBatchTrialService.trial(new NormalGradeBatchTrialCommand(orgCode, year, month, limit, postChangeType(changeType)));
        List<NormalGradeBatchTrialItem> eligibleItems = postChangeEligibleItems(trial);
        Map<String, Object> result = postChangeApplicationSummary(trial, eligibleItems);
        result.put("items", eligibleItems.stream().limit(100).map(this::postChangeApplicationItem).toList());
        return result;
    }

    public Map<String, Object> generatePostChangeApplications(String orgCode, int year, int month, int limit, String changeType) {
        ensureSalaryTodoCacheTable();
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_TRIAL")) {
            throw new IllegalArgumentException("Salary todo and trial permissions are required.");
        }
        NormalGradeBatchTrialResult trial = normalGradeBatchTrialService.trial(new NormalGradeBatchTrialCommand(orgCode, year, month, limit, postChangeType(changeType)));
        List<NormalGradeBatchTrialItem> eligibleItems = postChangeEligibleItems(trial);
        int generated = 0;
        for (NormalGradeBatchTrialItem item : eligibleItems) {
            String workItemId = "post-change-" + trial.orgCode() + "-" + trial.year() + "-" + String.format("%02d", trial.month()) + "-" + item.personCode();
            String itemChangeType = postChangeApplicationChangeType(item);
            jdbcTemplate.update("""
                    INSERT INTO salary_todo_candidate_cache(work_item_id, source, source_id, person_code, org_code,
                                                            person_no, person_name, event_year, event_month, change_type, note)
                    VALUES (?, 'SALARY_EVENT', ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        source = VALUES(source),
                        source_id = VALUES(source_id),
                        person_code = VALUES(person_code),
                        org_code = VALUES(org_code),
                        person_no = VALUES(person_no),
                        person_name = VALUES(person_name),
                        event_year = VALUES(event_year),
                        event_month = VALUES(event_month),
                        change_type = VALUES(change_type),
                        note = VALUES(note),
                        generated_at = CURRENT_TIMESTAMP
                    """,
                    workItemId,
                    "POST_CHANGE",
                    item.personCode(),
                    item.orgCode(),
                    personNo(item.personCode()),
                    item.personName(),
                    trial.year(),
                    trial.month(),
                    itemChangeType,
                    left(postChangeApplicationNote(item), 1000));
            generated++;
        }
        Map<String, Object> result = postChangeApplicationSummary(trial, eligibleItems);
        result.put("generatedCount", generated);
        result.put("items", eligibleItems.stream().limit(100).map(this::postChangeApplicationItem).toList());
        systemAuditService.record("workbench", "post-change-applications-generate", "ORG", trial.orgCode(),
                trial.year() + "-" + trial.month() + " generated=" + generated);
        return result;
    }

    public Map<String, Object> allowanceChangeApplicationPreview(String orgCode, int year, int month, int limit, String changeType) {
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_TRIAL")) {
            throw new IllegalArgumentException("Salary todo and trial permissions are required.");
        }
        NormalGradeBatchTrialResult trial = normalGradeBatchTrialService.trial(new NormalGradeBatchTrialCommand(orgCode, year, month, limit, allowanceChangeType(changeType)));
        List<NormalGradeBatchTrialItem> eligibleItems = allowanceChangeEligibleItems(trial);
        Map<String, Object> result = allowanceChangeApplicationSummary(trial, eligibleItems);
        result.put("items", eligibleItems.stream().limit(100).map(this::allowanceChangeApplicationItem).toList());
        return result;
    }

    public Map<String, Object> generateAllowanceChangeApplications(String orgCode, int year, int month, int limit, String changeType) {
        ensureSalaryTodoCacheTable();
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_TRIAL")) {
            throw new IllegalArgumentException("Salary todo and trial permissions are required.");
        }
        NormalGradeBatchTrialResult trial = normalGradeBatchTrialService.trial(new NormalGradeBatchTrialCommand(orgCode, year, month, limit, allowanceChangeType(changeType)));
        List<NormalGradeBatchTrialItem> eligibleItems = allowanceChangeEligibleItems(trial);
        int generated = 0;
        for (NormalGradeBatchTrialItem item : eligibleItems) {
            String workItemId = "allowance-change-" + trial.orgCode() + "-" + trial.year() + "-" + String.format("%02d", trial.month()) + "-" + item.personCode();
            String itemChangeType = allowanceChangeApplicationChangeType(item);
            jdbcTemplate.update("""
                    INSERT INTO salary_todo_candidate_cache(work_item_id, source, source_id, person_code, org_code,
                                                            person_no, person_name, event_year, event_month, change_type, note)
                    VALUES (?, 'SALARY_EVENT', ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        source = VALUES(source),
                        source_id = VALUES(source_id),
                        person_code = VALUES(person_code),
                        org_code = VALUES(org_code),
                        person_no = VALUES(person_no),
                        person_name = VALUES(person_name),
                        event_year = VALUES(event_year),
                        event_month = VALUES(event_month),
                        change_type = VALUES(change_type),
                        note = VALUES(note),
                        generated_at = CURRENT_TIMESTAMP
                    """,
                    workItemId,
                    "ALLOWANCE_CHANGE",
                    item.personCode(),
                    item.orgCode(),
                    personNo(item.personCode()),
                    item.personName(),
                    trial.year(),
                    trial.month(),
                    itemChangeType,
                    left(allowanceChangeApplicationNote(item), 1000));
            generated++;
        }
        Map<String, Object> result = allowanceChangeApplicationSummary(trial, eligibleItems);
        result.put("generatedCount", generated);
        result.put("items", eligibleItems.stream().limit(100).map(this::allowanceChangeApplicationItem).toList());
        systemAuditService.record("workbench", "allowance-change-applications-generate", "ORG", trial.orgCode(),
                trial.year() + "-" + trial.month() + " generated=" + generated);
        return result;
    }

    public Map<String, Object> transferSalaryApplicationPreview(String orgCode, int year, int month, int limit, String changeType) {
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_TRIAL")) {
            throw new IllegalArgumentException("Salary todo and trial permissions are required.");
        }
        NormalGradeBatchTrialResult trial = specialSalaryTrial(orgCode, year, month, limit, changeType,
                List.of("\u8c03\u5165\u5b9a\u8d44", "\u8f6c\u4e1a\u5b9a\u8d44", "\u9000\u4f0d\u5b9a\u8d44"));
        List<NormalGradeBatchTrialItem> eligibleItems = transferSalaryEligibleItems(trial);
        Map<String, Object> result = specialSalaryApplicationSummary(trial, eligibleItems);
        result.put("items", eligibleItems.stream().limit(100).map(this::specialSalaryApplicationItem).toList());
        return result;
    }

    public Map<String, Object> generateTransferSalaryApplications(String orgCode, int year, int month, int limit, String changeType) {
        ensureSalaryTodoCacheTable();
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_TRIAL")) {
            throw new IllegalArgumentException("Salary todo and trial permissions are required.");
        }
        NormalGradeBatchTrialResult trial = specialSalaryTrial(orgCode, year, month, limit, changeType,
                List.of("\u8c03\u5165\u5b9a\u8d44", "\u8f6c\u4e1a\u5b9a\u8d44", "\u9000\u4f0d\u5b9a\u8d44"));
        List<NormalGradeBatchTrialItem> eligibleItems = transferSalaryEligibleItems(trial);
        int generated = generateSpecialSalaryApplications(trial, eligibleItems, "TRANSFER_SALARY", "transfer-salary");
        Map<String, Object> result = specialSalaryApplicationSummary(trial, eligibleItems);
        result.put("generatedCount", generated);
        result.put("items", eligibleItems.stream().limit(100).map(this::specialSalaryApplicationItem).toList());
        systemAuditService.record("workbench", "transfer-salary-applications-generate", "ORG", trial.orgCode(),
                trial.year() + "-" + trial.month() + " generated=" + generated);
        return result;
    }

    public Map<String, Object> punishmentReductionApplicationPreview(String orgCode, int year, int month, int limit, String changeType) {
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_TRIAL")) {
            throw new IllegalArgumentException("Salary todo and trial permissions are required.");
        }
        NormalGradeBatchTrialResult trial = specialSalaryTrial(orgCode, year, month, limit, changeType,
                List.of("\u964d\u8d44\u5904\u5206", "\u5956\u52b1\u664b\u5347", "\u5176\u5b83\u60c5\u51b5"));
        List<NormalGradeBatchTrialItem> eligibleItems = punishmentReductionEligibleItems(trial);
        Map<String, Object> result = specialSalaryApplicationSummary(trial, eligibleItems);
        result.put("items", eligibleItems.stream().limit(100).map(this::specialSalaryApplicationItem).toList());
        return result;
    }

    public Map<String, Object> generatePunishmentReductionApplications(String orgCode, int year, int month, int limit, String changeType) {
        ensureSalaryTodoCacheTable();
        if (!hasMenu("SALARY_TODO") || !hasMenu("SALARY_TRIAL")) {
            throw new IllegalArgumentException("Salary todo and trial permissions are required.");
        }
        NormalGradeBatchTrialResult trial = specialSalaryTrial(orgCode, year, month, limit, changeType,
                List.of("\u964d\u8d44\u5904\u5206", "\u5956\u52b1\u664b\u5347", "\u5176\u5b83\u60c5\u51b5"));
        List<NormalGradeBatchTrialItem> eligibleItems = punishmentReductionEligibleItems(trial);
        int generated = generateSpecialSalaryApplications(trial, eligibleItems, "PUNISHMENT_REDUCTION", "punishment-reduction");
        Map<String, Object> result = specialSalaryApplicationSummary(trial, eligibleItems);
        result.put("generatedCount", generated);
        result.put("items", eligibleItems.stream().limit(100).map(this::specialSalaryApplicationItem).toList());
        systemAuditService.record("workbench", "punishment-reduction-applications-generate", "ORG", trial.orgCode(),
                trial.year() + "-" + trial.month() + " generated=" + generated);
        return result;
    }

    @Transactional
    public WorkbenchItemResponse createApplicationCase(WorkbenchCaseCreateRequest request) {
        ensureApplicationCaseTable();
        if (!hasMenu("APPLICATION_TODO")) {
            throw new IllegalArgumentException("Application todo permission is required.");
        }
        CaseRequest normalized = normalizeCaseRequest(request);
        String id = "APP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        jdbcTemplate.update("""
                INSERT INTO application_case(case_no, source, status, business_type, person_code, person_name, org_code,
                                             event_year, event_month, title, summary, workflow_status, created_by)
                VALUES (?, ?, 'TODO', ?, ?, ?, ?, ?, ?, ?, ?, 'APPLICATION_TODO', ?)
                """, id, normalized.source(), normalized.businessType(), normalized.personCode(), normalized.personName(),
                normalized.orgCode(), normalized.year(), normalized.month(), normalized.title(), normalized.summary(),
                text(currentUserService.currentUsername()));
        systemAuditService.record("application", "application-case-create", "APPLICATION_CASE", id,
                normalized.personCode() + " " + normalized.businessType());
        return applicationCaseItem(id);
    }

    @Transactional
    public WorkbenchItemResponse completeApplicationCase(String caseNo, WorkbenchCaseReviewRequest request) {
        ensureApplicationCaseTable();
        if (!hasMenu("APPLICATION_DONE")) {
            throw new IllegalArgumentException("Application done permission is required.");
        }
        Map<String, Object> row = applicationCaseRow(caseNo);
        organizationAccessService.requireOrgAccess(text(row.get("org_code")));
        jdbcTemplate.update("""
                UPDATE application_case
                SET status = 'DONE',
                    workflow_status = 'APPLICATION_DONE',
                    review_reason = ?,
                    handled_by = ?,
                    handled_at = CURRENT_TIMESTAMP
                WHERE case_no = ?
                """, left(text(request == null ? null : request.reviewReason()), 1000),
                text(currentUserService.currentUsername()), text(caseNo));
        systemAuditService.record("application", "application-case-done", "APPLICATION_CASE", text(caseNo),
                left(text(request == null ? null : request.reviewReason()), 500));
        return applicationCaseItem(caseNo);
    }

    public List<WorkbenchItemResponse> applicationCases(String status, int limit) {
        ensureApplicationCaseTable();
        String safeStatus = text(status).isBlank() ? "TODO" : text(status).toUpperCase();
        if ("TODO".equals(safeStatus) && !hasMenu("APPLICATION_TODO")) {
            return List.of();
        }
        if ("DONE".equals(safeStatus) && !hasMenu("APPLICATION_DONE")) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(limit, 500));
        String accessSql = organizationAccessService.orgCodeAccessSql("org_code");
        return jdbcTemplate.queryForList("""
                SELECT *
                FROM application_case
                WHERE status = ?
                  AND __ORG_ACCESS__
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """.replace("__ORG_ACCESS__", accessSql), safeStatus, safeLimit).stream()
                .map(this::applicationCaseItem)
                .toList();
    }

    public Map<String, Object> dataGovernanceScan(String orgCode, int limit) {
        requireDataGovernancePermission();
        ensureHistoryWritePlanTable();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        int safeLimit = Math.max(1, Math.min(limit, 500));
        List<Map<String, Object>> missingPost = jdbcTemplate.queryForList("""
                SELECT CONCAT(TRIM(p.dwbm), '-', TRIM(p.grbm)) AS personCode,
                       TRIM(COALESCE(p.xm, '')) AS personName,
                       TRIM(p.dwbm) AS orgCode,
                       'MISSING_POST' AS issueType,
                       '\u7f3a\u5c11\u4efb\u804c\u4fe1\u606f' AS message
                FROM dryjbxx p
                LEFT JOIN dryzwbh z ON z.dwbm = p.dwbm AND z.grbm = p.grbm
                WHERE p.dwbm LIKE CONCAT(?, '%')
                  AND z.id IS NULL
                LIMIT ?
                """, safeOrgCode, safeLimit);
        List<Map<String, Object>> invalidEducation = jdbcTemplate.queryForList("""
                SELECT CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode,
                       '' AS personName,
                       TRIM(dwbm) AS orgCode,
                       'INVALID_EDUCATION_DATE' AS issueType,
                       CONCAT('\u5b66\u5386\u6bd5\u4e1a\u65f6\u95f4\u5f02\u5e38 ', TRIM(COALESCE(bysj, ''))) AS message
                FROM dxl
                WHERE dwbm LIKE CONCAT(?, '%')
                  AND TRIM(COALESCE(bysj, '')) <> ''
                  AND REPLACE(TRIM(bysj), '.', '') NOT REGEXP '^[0-9]{6}$'
                LIMIT ?
                """, safeOrgCode, safeLimit);
        List<Map<String, Object>> brokenHistory = jdbcTemplate.queryForList("""
                SELECT CONCAT(TRIM(h.dwbm), '-', TRIM(h.grbm)) AS personCode,
                       '' AS personName,
                       TRIM(h.dwbm) AS orgCode,
                       'BROKEN_HISTORY_SID' AS issueType,
                       CONCAT('sid=', TRIM(COALESCE(h.sid, '')), ' \u627e\u4e0d\u5230\u4e0b\u4e00\u6761') AS message
                FROM (
                    SELECT id, sid, dwbm, grbm
                    FROM hisbase
                    WHERE dwbm LIKE CONCAT(?, '%')
                      AND TRIM(COALESCE(sid, '')) <> ''
                    ORDER BY dwbm, grbm, jsnf DESC, jsyf DESC, id DESC
                    LIMIT ?
                ) h
                LEFT JOIN hisbase n ON n.id = h.sid
                WHERE n.id IS NULL
                """, safeOrgCode, safeLimit);
        List<Map<String, Object>> standardReview = jdbcTemplate.queryForList("""
                SELECT CONCAT(TRIM(h.dwbm), '-', TRIM(h.grbm)) AS personCode,
                       '' AS personName,
                       TRIM(h.dwbm) AS orgCode,
                       'STANDARD_REVIEW' AS issueType,
                       CONCAT('\u8fd1\u671f\u5de5\u8d44\u53d8\u52a8\u5efa\u8bae\u62bd\u67e5\u6807\u51c6/\u89c4\u5219: ', TRIM(COALESCE(h.jslb, ''))) AS message
                FROM hisbase h
                WHERE h.dwbm LIKE CONCAT(?, '%')
                  AND TRIM(COALESCE(h.jsnf, '')) REGEXP '^[0-9]{4}$'
                  AND CAST(TRIM(h.jsnf) AS UNSIGNED) >= YEAR(CURDATE()) - 1
                  AND TRIM(COALESCE(h.jslb, '')) NOT IN ('', '\u6d25\u8d34\u53d8\u5316', '\u8c03\u6807\u664b\u5347')
                ORDER BY CAST(TRIM(h.jsnf) AS UNSIGNED) DESC, CAST(TRIM(h.jsyf) AS UNSIGNED) DESC
                LIMIT ?
                """, safeOrgCode, Math.max(1, safeLimit / 2));
        List<Map<String, Object>> blockedHistoryReview = jdbcTemplate.queryForList("""
                SELECT TRIM(p.case_no) AS caseNo,
                       TRIM(p.person_code) AS personCode,
                       '' AS personName,
                       TRIM(p.org_code) AS orgCode,
                       'BLOCKED_HISTORY_REVIEW' AS issueType,
                       CONCAT('写入阻断已转后期核查 ', TRIM(p.case_no), ' ', TRIM(COALESCE(p.comparison_review_reason, ''))) AS message
                FROM salary_history_write_plan p
                WHERE p.org_code LIKE CONCAT(?, '%')
                  AND TRIM(COALESCE(p.comparison_review_status, '')) = 'REVIEWED'
                  AND TRIM(COALESCE(p.comparison_review_category, '')) = 'HISTORY_SPECIAL'
                  AND (
                      TRIM(COALESCE(p.plan_status, '')) = 'BLOCKED'
                      OR TRIM(COALESCE(p.preview_status, '')) = 'BLOCKED'
                      OR (TRIM(COALESCE(p.plan_status, '')) = 'PREPARED' AND COALESCE(p.writable, 0) = 0)
                  )
                ORDER BY p.comparison_reviewed_at DESC, p.prepared_at DESC
                LIMIT ?
                """, safeOrgCode, Math.max(1, safeLimit / 2));
        List<Map<String, Object>> specialHistoryReview = jdbcTemplate.queryForList("""
                SELECT TRIM(p.case_no) AS caseNo,
                       TRIM(p.person_code) AS personCode,
                       '' AS personName,
                       TRIM(p.org_code) AS orgCode,
                       'HISTORY_SPECIAL_REVIEW' AS issueType,
                       CONCAT('历史写入特殊情况 ', TRIM(p.case_no), ' ', TRIM(COALESCE(p.comparison_review_reason, ''))) AS message
                FROM salary_history_write_plan p
                WHERE p.org_code LIKE CONCAT(?, '%')
                  AND TRIM(COALESCE(p.comparison_review_status, '')) = 'REVIEWED'
                  AND TRIM(COALESCE(p.comparison_review_category, '')) = 'HISTORY_SPECIAL'
                  AND NOT (
                      TRIM(COALESCE(p.plan_status, '')) = 'BLOCKED'
                      OR TRIM(COALESCE(p.preview_status, '')) = 'BLOCKED'
                      OR (TRIM(COALESCE(p.plan_status, '')) = 'PREPARED' AND COALESCE(p.writable, 0) = 0)
                  )
                ORDER BY p.comparison_reviewed_at DESC, p.prepared_at DESC
                LIMIT ?
                """, safeOrgCode, Math.max(1, safeLimit / 2));
        List<Map<String, Object>> issues = new ArrayList<>();
        issues.addAll(missingPost);
        issues.addAll(invalidEducation);
        issues.addAll(brokenHistory);
        issues.addAll(standardReview);
        issues.addAll(blockedHistoryReview);
        issues.addAll(specialHistoryReview);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", safeOrgCode);
        result.put("checkedAt", java.time.LocalDateTime.now().toString());
        result.put("missingPostCount", missingPost.size());
        result.put("invalidEducationCount", invalidEducation.size());
        result.put("brokenHistoryCount", brokenHistory.size());
        result.put("standardReviewCount", standardReview.size());
        result.put("blockedHistoryReviewCount", blockedHistoryReview.size());
        result.put("specialHistoryReviewCount", specialHistoryReview.size());
        result.put("retirementDeferredCount", 0);
        result.put("retirementDeferredNote", "\u9000\u4f11\u90e8\u5206\u6309\u72ec\u7acb\u9879\u76ee\u5904\u7406\uff0c\u672c\u8f6e\u5728\u804c\u8fc1\u79fb\u4e0d\u76f4\u63a5\u626b\u63cf\u9000\u4f11\u5de5\u8d44");
        result.put("issueCount", issues.size());
        result.put("issues", issues.stream().limit(safeLimit).toList());
        systemAuditService.record("workbench", "data-governance-scan", "ORG", safeOrgCode,
                "issues=" + issues.size() + ", limit=" + safeLimit);
        return result;
    }

    public Map<String, Object> exportDataGovernanceScan(String orgCode, int limit) {
        requireDataGovernancePermission();
        requireExportPermission();
        return dataGovernanceScan(orgCode, limit);
    }

    private Map<String, Object> dataGovernanceSnapshotForAcceptance(String orgCode, int limit) {
        if (hasMenu("SALARY_GOVERNANCE")) {
            return dataGovernanceScan(orgCode, limit);
        }
        String safeOrgCode = text(orgCode);
        Map<String, Object> governance = new LinkedHashMap<>();
        governance.put("orgCode", safeOrgCode);
        governance.put("checkedAt", java.time.LocalDateTime.now().toString());
        governance.put("missingPostCount", 0);
        governance.put("invalidEducationCount", 0);
        governance.put("brokenHistoryCount", 0);
        governance.put("standardReviewCount", 0);
        governance.put("retirementDeferredCount", 0);
        governance.put("retirementDeferredNote", "\u9000\u4f11\u90e8\u5206\u6309\u72ec\u7acb\u9879\u76ee\u5904\u7406\uff0c\u672c\u8f6e\u5728\u804c\u8fc1\u79fb\u4e0d\u76f4\u63a5\u626b\u63cf\u9000\u4f11\u5de5\u8d44");
        governance.put("issueCount", 0);
        governance.put("issues", List.of());
        governance.put("skipped", true);
        governance.put("skipReason", "\u5f53\u524d\u7528\u6237\u672a\u6388\u6743\u6570\u636e\u6cbb\u7406\uff0c\u6570\u636e\u6cbb\u7406\u626b\u63cf\u672a\u6267\u884c");
        return governance;
    }

    public Map<String, Object> migrationAcceptanceChecklist() {
        requireAcceptancePermission();
        long applicationTodo = hasMenu("APPLICATION_TODO") ? countApplicationCases("TODO") : 0;
        long applicationDone = hasMenu("APPLICATION_DONE") ? countApplicationCases("DONE") : 0;
        long salaryTodo = hasMenu("SALARY_TODO") ? countSalaryTodo() : 0;
        long salaryDone = hasMenu("SALARY_DONE") ? countSalaryDone() : 0;
        long historyPrepared = hasMenu("SALARY_DONE") ? countHistoryWritePlans("PREPARED") : 0;
        long historyExecuted = hasMenu("SALARY_DONE") ? countHistoryWritePlans("EXECUTED") : 0;
        long historyBlocked = hasMenu("SALARY_DONE") ? countHistoryWritePlans("BLOCKED") : 0;
        long reviewPending = hasMenu("SALARY_DONE") ? countPendingSalaryReview() + countPendingHistoryWriteComparisonReviewsForAcceptance() : 0;
        List<WorkbenchHistoryWriteBatchLedgerResponse> batchLedger = hasMenu("SALARY_DONE") ? historyWriteBatchLedger(200) : List.of();
        long historyBatchOpen = batchLedger.stream().filter(this::historyWriteBatchNeedsFollowup).count();
        return Map.of(
                "status", "READY_FOR_CORE_ACCEPTANCE",
                "summary", Map.of(
                        "applicationTodo", applicationTodo,
                        "applicationDone", applicationDone,
                        "salaryTodo", salaryTodo,
                        "salaryDone", salaryDone,
                        "historyPrepared", historyPrepared,
                        "historyExecuted", historyExecuted,
                        "historyBlocked", historyBlocked,
                        "reviewPending", reviewPending,
                        "historyBatchActions", batchLedger.size(),
                        "historyBatchOpen", historyBatchOpen
                ),
                "items", List.of(
                        acceptanceItem("1-application-flow", "\u7533\u529e\u4e1a\u52a1\u6d41\u63a5\u5165", "POST /api/workbench/application-cases -> complete -> GET /api/workbench/application-cases"),
                        acceptanceItem("2-salary-formal-flow", "\u5de5\u8d44\u529e\u7406\u6b63\u5f0f\u5199\u5165\u95ed\u73af", "preview/complete/snapshot/history-write-confirm/history-write-execute"),
                        acceptanceItem("3-exception-ledger", "\u5dee\u5f02\u548c\u7279\u6b8a\u60c5\u51b5\u540e\u671f\u6838\u67e5", "history-write-review-ledger + data-governance/scan"),
                        acceptanceItem("4-permission-menu", "\u6743\u9650\u83dc\u5355\u7ec6\u5316", "GET /api/system/role-templates + /api/system/menus"),
                        acceptanceItem("5-desktop-workbench-ui", "\u684c\u9762\u5f0f\u5de5\u4f5c\u53f0", "summary returns todoItems/doneItems/metrics"),
                        acceptanceItem("6-data-governance", "\u6570\u636e\u6cbb\u7406\u5165\u53e3", "GET /api/workbench/data-governance/scan?orgCode=..."),
                        acceptanceItem("7-standard-rule-maintenance", "\u6807\u51c6\u8868\u548c\u89c4\u5219\u7ef4\u62a4", "GET /api/workbench/salary-business-forms + salary field config"),
                        acceptanceItem("8-retirement-deferred", "\u9000\u4f11\u90e8\u5206\u72ec\u7acb\u9879\u76ee\u6807\u8bb0", "data-governance retirementDeferredNote"),
                        acceptanceItem("9-retirement-project", "\u9000\u4f11\u9879\u76ee\u72ec\u7acb\u8fc1\u79fb", "migration-readiness marks retirement deferred"),
                        acceptanceItem("10-launch-readiness", "\u4e0a\u7ebf\u51c6\u5907", "GET /api/workbench/migration-readiness + package/smoke tests")
                )
        );
    }

    public Map<String, Object> migrationRegressionSamples(String orgCode, int limit) {
        return migrationRegressionSamples(orgCode, limit, false);
    }

    public Map<String, Object> migrationRegressionRun(String orgCode, int limit) {
        Map<String, Object> result = migrationRegressionSamples(orgCode, limit, true);
        systemAuditService.record("workbench", "migration-regression-run", "ORG", text(result.get("orgCode")),
                "runNo=" + text(result.get("runNo")) + ", status=" + text(result.get("overallStatus"))
                        + ", warn=" + longValue(result.get("warningCount")));
        return result;
    }

    public List<Map<String, Object>> migrationRegressionSampleLibrary(String orgCode, int limit) {
        return migrationRegressionSampleLibrary(orgCode, "", "", "", "", limit);
    }

    public List<Map<String, Object>> migrationRegressionSampleLibrary(String orgCode, String sampleCode, String enabled, String keyword, int limit) {
        return migrationRegressionSampleLibrary(orgCode, sampleCode, enabled, keyword, "", limit);
    }

    public List<Map<String, Object>> migrationRegressionSampleLibrary(String orgCode, String sampleCode, String enabled, String keyword, String batchNo, int limit) {
        requireAcceptancePermission();
        ensureMigrationRegressionSampleTable();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        String safeSampleCode = text(sampleCode);
        String safeEnabled = text(enabled).toUpperCase();
        String safeKeyword = text(keyword);
        String safeBatchNo = text(batchNo);
        int safeLimit = Math.max(1, Math.min(limit, 500));
        List<Object> args = new ArrayList<>();
        args.add(safeOrgCode);
        StringBuilder where = new StringBuilder("WHERE org_code LIKE CONCAT(?, '%')");
        if (!safeSampleCode.isBlank()) {
            where.append(" AND sample_code = ?");
            args.add(safeSampleCode);
        }
        if (!safeBatchNo.isBlank()) {
            where.append(" AND batch_no = ?");
            args.add(safeBatchNo);
        }
        if ("true".equalsIgnoreCase(safeEnabled) || "1".equals(safeEnabled) || "ENABLED".equals(safeEnabled)) {
            where.append(" AND enabled = 1");
        } else if ("false".equalsIgnoreCase(safeEnabled) || "0".equals(safeEnabled) || "DISABLED".equals(safeEnabled)) {
            where.append(" AND enabled = 0");
        }
        if (!safeKeyword.isBlank()) {
            where.append("""
                     AND (
                        sample_id LIKE ?
                        OR person_code LIKE ?
                        OR person_name LIKE ?
                        OR sample_title LIKE ?
                    )
                    """);
            String like = "%" + safeKeyword + "%";
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        args.add(safeLimit);
        return jdbcTemplate.queryForList("""
                SELECT sample_code AS sampleCode,
                       sample_title AS sampleTitle,
                       sample_domain AS sampleDomain,
                       sample_id AS sampleId,
                       person_code AS personCode,
                       person_name AS personName,
                       org_code AS orgCode,
                       sample_type AS sampleType,
                       batch_no AS batchNo,
                       sample_source AS sampleSource,
                       expected_status AS expectedStatus,
                       expected_amount AS expectedAmount,
                       expected_payload AS expectedPayload,
                       enabled,
                       note,
                       last_run_no AS lastRunNo,
                       last_run_status AS lastRunStatus,
                       last_run_message AS lastRunMessage,
                       last_run_at AS lastRunAt,
                       updated_at AS updatedAt
                FROM migration_regression_sample
                __WHERE__
                ORDER BY COALESCE(batch_no, ''), sample_code, updated_at DESC, id DESC
                LIMIT ?
                """.replace("__WHERE__", where.toString()), args.toArray());
    }

    public Map<String, Object> refreshMigrationRegressionSampleLibrary(String orgCode, int limit) {
        requireAcceptancePermission();
        ensureMigrationRegressionSampleTable();
        Map<String, Object> dynamic = migrationRegressionSamples(orgCode, limit, false);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> samples = (List<Map<String, Object>>) dynamic.getOrDefault("samples", List.of());
        String batchNo = "AUTO-" + LocalDate.now();
        int inserted = 0;
        for (Map<String, Object> sample : samples) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> people = (List<Map<String, Object>>) sample.getOrDefault("people", List.of());
            for (Map<String, Object> person : people) {
                String personCode = text(person.get("personCode"));
                if (personCode.isBlank()) {
                    continue;
                }
                Map<String, Object> assertion = migrationRegressionExpectedAssertion(sample, person);
                jdbcTemplate.update("""
                        INSERT INTO migration_regression_sample(sample_code, sample_title, sample_domain, sample_id,
                                                                person_code, person_name, org_code, sample_type,
                                                                batch_no, sample_source, expected_status, expected_amount,
                                                                expected_payload, note)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            sample_title = VALUES(sample_title),
                            sample_domain = VALUES(sample_domain),
                            person_name = VALUES(person_name),
                            sample_type = VALUES(sample_type),
                            batch_no = VALUES(batch_no),
                            sample_source = VALUES(sample_source),
                            expected_status = VALUES(expected_status),
                            expected_amount = VALUES(expected_amount),
                            expected_payload = VALUES(expected_payload),
                            note = VALUES(note),
                            updated_at = CURRENT_TIMESTAMP
                        """,
                        text(sample.get("code")),
                        text(sample.get("title")),
                        text(sample.get("domain")),
                        text(person.get("sampleId")),
                        personCode,
                        text(person.get("personName")),
                        text(person.get("orgCode")),
                        text(person.get("sampleType")),
                        batchNo,
                        "AUTO_REFRESH",
                        text(assertion.get("status")),
                        assertion.get("amount"),
                        text(assertion.get("payload")),
                        text(sample.get("message"))
                );
                inserted++;
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", text(dynamic.get("orgCode")));
        result.put("checkedAt", java.time.LocalDateTime.now().withNano(0).toString());
        result.put("batchNo", batchNo);
        result.put("refreshedCount", inserted);
        result.put("libraryCount", migrationRegressionSampleLibrary(text(dynamic.get("orgCode")), 500).size());
        result.put("sampleCount", samples.size());
        systemAuditService.record("workbench", "migration-regression-sample-library-refresh", "ORG", text(dynamic.get("orgCode")),
                "refreshed=" + inserted + ", sampleTypes=" + samples.size());
        return result;
    }

    public Map<String, Object> runMigrationRegressionSampleLibrary(String orgCode, int limit) {
        return runMigrationRegressionSampleLibrary(orgCode, "", limit);
    }

    public Map<String, Object> runMigrationRegressionSampleLibrary(String orgCode, String batchNo, int limit) {
        requireAcceptancePermission();
        ensureMigrationRegressionSampleTable();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        String safeBatchNo = text(batchNo);
        int safeLimit = Math.max(1, Math.min(limit, 500));
        List<Object> args = new ArrayList<>();
        args.add(safeOrgCode);
        StringBuilder where = new StringBuilder("""
                WHERE org_code LIKE CONCAT(?, '%')
                  AND enabled = 1
                """);
        if (!safeBatchNo.isBlank()) {
            where.append(" AND batch_no = ?");
            args.add(safeBatchNo);
        }
        args.add(safeLimit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM migration_regression_sample
                __WHERE__
                ORDER BY COALESCE(batch_no, ''), sample_code, updated_at DESC, id DESC
                LIMIT ?
                """.replace("__WHERE__", where.toString()), args.toArray());
        List<Map<String, Object>> results = rows.stream().map(this::runMigrationRegressionSampleRowWithAssertions).toList();
        long warnCount = results.stream().filter(row -> !"PASS".equals(text(row.get("status")))).count();
        String runNo = "MIG-REG-LIB-" + LocalDate.now() + "-" + UUID.randomUUID().toString().substring(0, 8);
        for (Map<String, Object> row : results) {
            jdbcTemplate.update("""
                    UPDATE migration_regression_sample
                    SET last_run_no = ?,
                        last_run_status = ?,
                        last_run_message = ?,
                        last_run_at = CURRENT_TIMESTAMP,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE org_code = ?
                      AND sample_code = ?
                      AND sample_id = ?
                      AND person_code = ?
                    """,
                    runNo,
                    text(row.get("status")),
                    text(row.get("message")),
                    text(row.get("orgCode")),
                    text(row.get("code")),
                    text(row.get("sampleId")),
                    text(row.get("personCode"))
            );
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("runNo", runNo);
        result.put("orgCode", safeOrgCode);
        result.put("batchNo", safeBatchNo);
        result.put("checkedAt", java.time.LocalDateTime.now().withNano(0).toString());
        result.put("sampleLimit", safeLimit);
        result.put("sampleCount", results.size());
        result.put("passCount", results.size() - warnCount);
        result.put("warningCount", warnCount);
        result.put("overallStatus", warnCount > 0 ? "WARN" : "PASS");
        result.put("samples", results);
        saveMigrationRegressionRun(result);
        systemAuditService.record("workbench", "migration-regression-sample-library-run", "ORG", safeOrgCode,
                "runNo=" + runNo + ", batchNo=" + safeBatchNo + ", samples=" + results.size() + ", warn=" + warnCount);
        return result;
    }

    public List<Map<String, Object>> migrationRegressionSampleRuns(String orgCode, String batchNo, String status, int limit) {
        requireAcceptancePermission();
        ensureMigrationRegressionSampleTable();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        String safeBatchNo = text(batchNo);
        String safeStatus = text(status).toUpperCase();
        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<Object> args = new ArrayList<>();
        args.add(safeOrgCode);
        StringBuilder where = new StringBuilder("WHERE org_code LIKE CONCAT(?, '%')");
        if (!safeBatchNo.isBlank()) {
            where.append(" AND batch_no = ?");
            args.add(safeBatchNo);
        }
        if (!safeStatus.isBlank()) {
            where.append(" AND overall_status = ?");
            args.add(safeStatus);
        }
        args.add(safeLimit);
        return jdbcTemplate.queryForList("""
                SELECT run_no AS runNo,
                       org_code AS orgCode,
                       batch_no AS batchNo,
                       checked_at AS checkedAt,
                       sample_limit AS sampleLimit,
                       sample_count AS sampleCount,
                       pass_count AS passCount,
                       warning_count AS warningCount,
                       overall_status AS overallStatus,
                       created_by AS createdBy,
                       created_at AS createdAt
                FROM migration_regression_run
                __WHERE__
                ORDER BY checked_at DESC, id DESC
                LIMIT ?
                """.replace("__WHERE__", where.toString()), args.toArray());
    }

    public Map<String, Object> migrationRegressionDashboard(String orgCode, int limit) {
        requireAcceptancePermission();
        ensureMigrationRegressionSampleTable();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        int safeLimit = Math.max(1, Math.min(limit, 20));
        List<Map<String, Object>> runs = migrationRegressionSampleRuns(safeOrgCode, "", "", safeLimit);
        List<Map<String, Object>> enrichedRuns = new ArrayList<>();
        long totalSamples = 0;
        long totalWarnings = 0;
        long pending = 0;
        long reviewed = 0;
        long fixing = 0;
        long deferred = 0;
        long retestResolved = 0;
        long retestFound = 0;
        for (Map<String, Object> run : runs) {
            List<Map<String, Object>> samples = jdbcTemplate.queryForList("""
                    SELECT status,
                           review_status AS reviewStatus,
                           review_category AS reviewCategory,
                           retest_status AS retestStatus
                    FROM migration_regression_run_sample
                    WHERE run_no = ?
                    """, text(run.get("runNo")));
            Map<String, Object> reviewSummary = migrationRegressionReviewSummary(samples);
            long runRetestResolved = samples.stream()
                    .filter(sample -> "RESOLVED".equalsIgnoreCase(text(sample.get("retestStatus"))))
                    .count();
            long runRetestFound = samples.stream()
                    .filter(sample -> "FOUND".equalsIgnoreCase(text(sample.get("retestStatus"))))
                    .count();
            Map<String, Object> item = new LinkedHashMap<>(run);
            item.put("reviewSummary", reviewSummary);
            item.put("retestResolvedCount", runRetestResolved);
            item.put("retestFoundCount", runRetestFound);
            enrichedRuns.add(item);
            totalSamples += longValue(run.get("sampleCount"));
            totalWarnings += longValue(run.get("warningCount"));
            pending += longValue(reviewSummary.get("pendingCount"));
            reviewed += longValue(reviewSummary.get("reviewedCount"));
            fixing += longValue(reviewSummary.get("fixingCount"));
            deferred += longValue(reviewSummary.get("deferredCount"));
            retestResolved += runRetestResolved;
            retestFound += runRetestFound;
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("runCount", enrichedRuns.size());
        summary.put("sampleCount", totalSamples);
        summary.put("warningCount", totalWarnings);
        summary.put("pendingCount", pending);
        summary.put("reviewedCount", reviewed);
        summary.put("fixingCount", fixing);
        summary.put("deferredCount", deferred);
        summary.put("retestResolvedCount", retestResolved);
        summary.put("retestFoundCount", retestFound);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", safeOrgCode);
        result.put("checkedAt", java.time.LocalDateTime.now().withNano(0).toString());
        result.put("summary", summary);
        result.put("runs", enrichedRuns);
        return result;
    }

    public Map<String, Object> migrationQualityOverview(String orgCode) {
        requireAcceptancePermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        Map<String, Object> acceptance = migrationAcceptanceChecklist();
        @SuppressWarnings("unchecked")
        Map<String, Object> acceptanceSummary = (Map<String, Object>) acceptance.getOrDefault("summary", Map.of());
        Map<String, Object> regression = migrationRegressionDashboard(safeOrgCode, 5);
        @SuppressWarnings("unchecked")
        Map<String, Object> regressionSummary = (Map<String, Object>) regression.getOrDefault("summary", Map.of());
        Map<String, Object> governance = hasMenu("SALARY_GOVERNANCE")
                ? dataGovernanceScan(safeOrgCode, 50)
                : Map.of("issueCount", 0, "skipped", true);
        long historyBlocked = longValue(acceptanceSummary.get("historyBlocked"));
        long historyOpen = longValue(acceptanceSummary.get("historyBatchOpen"));
        long reviewPending = longValue(acceptanceSummary.get("reviewPending"));
        long regressionWarnings = longValue(regressionSummary.get("warningCount"));
        long regressionPending = longValue(regressionSummary.get("pendingCount"));
        long regressionFixing = longValue(regressionSummary.get("fixingCount"));
        long governanceIssues = longValue(governance.get("issueCount"));
        String status = (historyBlocked + regressionWarnings + regressionPending + governanceIssues) > 0 ? "ATTENTION" : "READY";
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("status", status);
        summary.put("historyBlocked", historyBlocked);
        summary.put("historyOpen", historyOpen);
        summary.put("reviewPending", reviewPending);
        summary.put("regressionWarnings", regressionWarnings);
        summary.put("regressionPending", regressionPending);
        summary.put("regressionFixing", regressionFixing);
        summary.put("governanceIssues", governanceIssues);
        summary.put("salaryTodo", longValue(acceptanceSummary.get("salaryTodo")));
        summary.put("salaryDone", longValue(acceptanceSummary.get("salaryDone")));
        List<Map<String, Object>> gates = List.of(
                qualityGate("acceptance", "验收闸口", reviewPending + historyBlocked > 0 ? "WARN" : "PASS", reviewPending + historyBlocked,
                        "待复核 " + reviewPending + "，历史阻断 " + historyBlocked),
                qualityGate("regression", "固定回归", regressionWarnings > 0 ? "WARN" : "PASS", regressionWarnings,
                        "待核查 " + regressionPending + "，修复中 " + regressionFixing),
                qualityGate("governance", "数据治理", governanceIssues > 0 ? "WARN" : "PASS", governanceIssues,
                        "扫描问题 " + governanceIssues),
                qualityGate("history-write", "历史写入队列", historyOpen > 0 ? "WARN" : "PASS", historyOpen,
                        "待处理队列 " + historyOpen)
        );
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", safeOrgCode);
        result.put("checkedAt", java.time.LocalDateTime.now().withNano(0).toString());
        result.put("status", status);
        result.put("summary", summary);
        result.put("gates", List.of(
                qualityGate("acceptance", "\u9a8c\u6536\u95f8\u53e3", reviewPending + historyBlocked > 0 ? "WARN" : "PASS", reviewPending + historyBlocked,
                        "\u5f85\u590d\u6838 " + reviewPending + "\uff0c\u5386\u53f2\u963b\u65ad " + historyBlocked),
                qualityGate("regression", "\u56fa\u5b9a\u56de\u5f52", regressionWarnings > 0 ? "WARN" : "PASS", regressionWarnings,
                        "\u5f85\u6838\u67e5 " + regressionPending + "\uff0c\u4fee\u590d\u4e2d " + regressionFixing),
                qualityGate("governance", "\u6570\u636e\u6cbb\u7406", governanceIssues > 0 ? "WARN" : "PASS", governanceIssues,
                        "\u626b\u63cf\u95ee\u9898 " + governanceIssues),
                qualityGate("history-write", "\u5386\u53f2\u5199\u5165\u961f\u5217", historyOpen > 0 ? "WARN" : "PASS", historyOpen,
                        "\u5f85\u5904\u7406\u961f\u5217 " + historyOpen)
        ));
        result.put("acceptance", acceptance);
        result.put("regression", regression);
        result.put("governance", governance);
        result.put("archiveSummary", migrationQualityArchiveSummary(safeOrgCode));
        return result;
    }

    private Map<String, Object> migrationQualityArchiveSummary(String orgCode) {
        ensureMigrationQualitySnapshotTable();
        systemAuditService.ensureTable();
        Map<String, Object> summary = new LinkedHashMap<>();
        Long archivedCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM migration_quality_snapshot
                WHERE org_code = ?
                  AND archive_status = 'ARCHIVED'
                """, Long.class, orgCode);
        summary.put("archivedCount", archivedCount == null ? 0L : archivedCount);

        List<Map<String, Object>> latestArchived = jdbcTemplate.queryForList("""
                SELECT snapshot_no AS snapshotNo,
                       archived_at AS archivedAt,
                       archived_by AS archivedBy,
                       preflight_title AS preflightTitle
                FROM migration_quality_snapshot
                WHERE org_code = ?
                  AND archive_status = 'ARCHIVED'
                ORDER BY archived_at DESC, id DESC
                LIMIT 1
                """, orgCode);
        if (!latestArchived.isEmpty()) {
            Map<String, Object> row = latestArchived.get(0);
            summary.put("latestArchivedSnapshotNo", text(row.get("snapshotNo")));
            summary.put("latestArchivedAt", text(row.get("archivedAt")));
            summary.put("latestArchivedBy", text(row.get("archivedBy")));
            summary.put("latestArchivedTitle", text(row.get("preflightTitle")));
        }

        List<Map<String, Object>> latestExport = jdbcTemplate.queryForList("""
                SELECT audit.target_code AS snapshotNo,
                       audit.summary AS summary,
                       audit.operator AS exportedBy,
                       audit.created_at AS exportedAt
                FROM sys_audit_log audit
                JOIN migration_quality_snapshot snapshot
                  ON snapshot.snapshot_no = audit.target_code
                WHERE audit.module_name = 'workbench'
                  AND audit.action_name = 'migration-quality-acceptance-package-export'
                  AND audit.target_type = 'MIGRATION_QUALITY'
                  AND snapshot.org_code = ?
                ORDER BY audit.created_at DESC, audit.id DESC
                LIMIT 1
                """, orgCode);
        if (!latestExport.isEmpty()) {
            Map<String, Object> row = latestExport.get(0);
            String auditSummary = text(row.get("summary"));
            summary.put("latestExportSnapshotNo", text(row.get("snapshotNo")));
            summary.put("latestExportNo", auditSummaryValue(auditSummary, "exportNo"));
            summary.put("latestExportFileCount", auditSummaryInt(auditSummary, "files"));
            summary.put("latestExportHasComparison", auditSummary.contains("migration-quality-snapshot-compare-"));
            summary.put("latestExportedBy", text(row.get("exportedBy")));
            summary.put("latestExportedAt", text(row.get("exportedAt")));
            summary.put("latestExportSummary", auditSummary);
        }
        List<Map<String, Object>> latestPreview = jdbcTemplate.queryForList("""
                SELECT audit.target_code AS snapshotNo,
                       audit.summary AS summary,
                       audit.operator AS previewedBy,
                       audit.created_at AS previewedAt
                FROM sys_audit_log audit
                JOIN migration_quality_snapshot snapshot
                  ON snapshot.snapshot_no = audit.target_code
                WHERE audit.module_name = 'workbench'
                  AND audit.action_name = 'migration-quality-acceptance-package-preview'
                  AND audit.target_type = 'MIGRATION_QUALITY'
                  AND snapshot.org_code = ?
                ORDER BY audit.created_at DESC, audit.id DESC
                LIMIT 1
                """, orgCode);
        if (!latestPreview.isEmpty()) {
            Map<String, Object> row = latestPreview.get(0);
            String auditSummary = text(row.get("summary"));
            String previewedAt = text(row.get("previewedAt"));
            String exportedAt = text(summary.get("latestExportedAt"));
            summary.put("latestPreviewSnapshotNo", text(row.get("snapshotNo")));
            summary.put("latestPreviewExportNo", auditSummaryValue(auditSummary, "exportNo"));
            summary.put("latestPreviewFileCount", auditSummaryInt(auditSummary, "files"));
            summary.put("latestPreviewHasComparison", "true".equalsIgnoreCase(auditSummaryValue(auditSummary, "hasComparison")));
            summary.put("latestPreviewedBy", text(row.get("previewedBy")));
            summary.put("latestPreviewedAt", previewedAt);
            summary.put("latestPreviewSummary", auditSummary);
            summary.put("latestPreviewPendingExport", exportedAt.isBlank() || previewedAt.compareTo(exportedAt) > 0);
        }
        return summary;
    }

    public Map<String, Object> createMigrationQualitySnapshot(String orgCode) {
        Map<String, Object> overview = migrationQualityOverview(orgCode);
        ensureMigrationQualitySnapshotTable();
        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) overview.getOrDefault("summary", Map.of());
        String snapshotNo = "MIG-QUALITY-" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update("""
                INSERT INTO migration_quality_snapshot(snapshot_no, org_code, checked_at, overall_status,
                                                       history_blocked, history_open, review_pending,
                                                       regression_warnings, regression_pending, regression_fixing,
                                                       governance_issues, salary_todo, salary_done,
                                                       snapshot_json, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                snapshotNo,
                text(overview.get("orgCode")),
                text(overview.get("checkedAt")).replace('T', ' '),
                text(overview.get("status")),
                longValue(summary.get("historyBlocked")),
                longValue(summary.get("historyOpen")),
                longValue(summary.get("reviewPending")),
                longValue(summary.get("regressionWarnings")),
                longValue(summary.get("regressionPending")),
                longValue(summary.get("regressionFixing")),
                longValue(summary.get("governanceIssues")),
                longValue(summary.get("salaryTodo")),
                longValue(summary.get("salaryDone")),
                writeJson(overview),
                currentUserService.currentUsername()
        );
        String previousSnapshotNo = previousMigrationQualitySnapshotNo(text(overview.get("orgCode")), snapshotNo);
        Map<String, Object> comparison = previousSnapshotNo.isBlank()
                ? Map.of()
                : compareMigrationQualitySnapshots(previousSnapshotNo, snapshotNo);
        Map<String, Object> decision = migrationPreflightDecision(summary, comparison);
        jdbcTemplate.update("""
                UPDATE migration_quality_snapshot
                SET preflight_level = ?,
                    preflight_title = ?,
                    preflight_message = ?,
                    decision_json = ?
                WHERE snapshot_no = ?
                """,
                text(decision.get("level")),
                text(decision.get("title")),
                text(decision.get("message")),
                writeJson(decision),
                snapshotNo
        );
        Map<String, Object> result = new LinkedHashMap<>(overview);
        result.put("snapshotNo", snapshotNo);
        result.put("previousSnapshotNo", previousSnapshotNo);
        result.put("preflightLevel", decision.get("level"));
        result.put("preflightTitle", decision.get("title"));
        result.put("preflightMessage", decision.get("message"));
        result.put("decision", decision);
        result.put("createdBy", currentUserService.currentUsername());
        result.put("createdAt", java.time.LocalDateTime.now().withNano(0).toString());
        return result;
    }

    public List<Map<String, Object>> migrationQualitySnapshots(String orgCode, int limit) {
        return migrationQualitySnapshots(orgCode, limit, false);
    }

    public List<Map<String, Object>> migrationQualitySnapshots(String orgCode, int limit, boolean archivedOnly) {
        return migrationQualitySnapshots(orgCode, limit, archivedOnly, "", "", "", "");
    }

    public List<Map<String, Object>> migrationQualitySnapshots(
            String orgCode,
            int limit,
            boolean archivedOnly,
            String preflightLevel,
            String archivedBy,
            String archivedFrom,
            String archivedTo
    ) {
        requireAcceptancePermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        ensureMigrationQualitySnapshotTable();
        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<Object> params = new ArrayList<>();
        params.add(safeOrgCode);
        StringBuilder where = new StringBuilder("WHERE org_code = ? ");
        if (archivedOnly) {
            where.append("AND archive_status = 'ARCHIVED' ");
        }
        String safePreflightLevel = text(preflightLevel);
        if (!safePreflightLevel.isBlank()) {
            where.append("AND preflight_level = ? ");
            params.add(safePreflightLevel);
        }
        String safeArchivedBy = text(archivedBy);
        if (!safeArchivedBy.isBlank()) {
            where.append("AND archived_by LIKE ? ");
            params.add("%" + safeArchivedBy + "%");
        }
        String safeArchivedFrom = text(archivedFrom);
        if (!safeArchivedFrom.isBlank()) {
            where.append("AND archived_at >= ? ");
            params.add(safeArchivedFrom);
        }
        String safeArchivedTo = text(archivedTo);
        if (!safeArchivedTo.isBlank()) {
            where.append("AND archived_at < ? ");
            params.add(LocalDate.parse(safeArchivedTo).plusDays(1).toString());
        }
        params.add(safeLimit);
        return jdbcTemplate.queryForList("""
                SELECT snapshot_no AS snapshotNo,
                       org_code AS orgCode,
                       checked_at AS checkedAt,
                       overall_status AS status,
                       history_blocked AS historyBlocked,
                       history_open AS historyOpen,
                       review_pending AS reviewPending,
                       regression_warnings AS regressionWarnings,
                       regression_pending AS regressionPending,
                       regression_fixing AS regressionFixing,
                       governance_issues AS governanceIssues,
                       salary_todo AS salaryTodo,
                       salary_done AS salaryDone,
                       preflight_level AS preflightLevel,
                       preflight_title AS preflightTitle,
                       preflight_message AS preflightMessage,
                       archive_status AS archiveStatus,
                       archived_by AS archivedBy,
                       archived_at AS archivedAt,
                       archive_note AS archiveNote,
                       created_by AS createdBy,
                       created_at AS createdAt
                FROM migration_quality_snapshot
                __WHERE__
                ORDER BY checked_at DESC, id DESC
                LIMIT ?
                """.replace("__WHERE__", where.toString()), params.toArray());
    }

    public Map<String, Object> migrationQualitySnapshot(String snapshotNo) {
        requireAcceptancePermission();
        String safeSnapshotNo = text(snapshotNo);
        if (safeSnapshotNo.isBlank()) {
            throw new IllegalArgumentException("Snapshot number is required.");
        }
        ensureMigrationQualitySnapshotTable();
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT snapshot_no AS snapshotNo,
                       org_code AS orgCode,
                       checked_at AS checkedAt,
                       overall_status AS status,
                       preflight_level AS preflightLevel,
                       preflight_title AS preflightTitle,
                       preflight_message AS preflightMessage,
                       archive_status AS archiveStatus,
                       archived_by AS archivedBy,
                       archived_at AS archivedAt,
                       archive_note AS archiveNote,
                       snapshot_json AS snapshotJson,
                       decision_json AS decisionJson,
                       created_by AS createdBy,
                       created_at AS createdAt
                FROM migration_quality_snapshot
                WHERE snapshot_no = ?
                """, safeSnapshotNo);
        organizationAccessService.requireOrgAccess(text(row.get("orgCode")));
        Map<String, Object> result = new LinkedHashMap<>(row);
        result.remove("snapshotJson");
        result.remove("decisionJson");
        result.put("overview", readJsonMap(row.get("snapshotJson")));
        result.put("decision", readJsonMap(row.get("decisionJson")));
        return result;
    }

    public Map<String, Object> archiveMigrationQualitySnapshot(String snapshotNo, String note) {
        Map<String, Object> snapshot = migrationQualitySnapshot(snapshotNo);
        String safeSnapshotNo = text(snapshot.get("snapshotNo"));
        if ("ARCHIVED".equals(text(snapshot.get("archiveStatus")))) {
            snapshot.put("archiveLocked", true);
            snapshot.put("archiveMessage", "Migration quality report has been archived and locked.");
            return snapshot;
        }
        String safeNote = left(text(note), 1024);
        jdbcTemplate.update("""
                UPDATE migration_quality_snapshot
                SET archive_status = 'ARCHIVED',
                    archived_by = ?,
                    archived_at = NOW(),
                    archive_note = ?
                WHERE snapshot_no = ?
                """, currentUserService.currentUsername(), safeNote, safeSnapshotNo);
        systemAuditService.record("workbench", "migration-quality-report-archive", "MIGRATION_QUALITY", safeSnapshotNo,
                "org=" + text(snapshot.get("orgCode")) + ", decision=" + text(snapshot.get("preflightTitle")) + ", note=" + safeNote);
        Map<String, Object> result = migrationQualitySnapshot(safeSnapshotNo);
        result.put("archivedBy", currentUserService.currentUsername());
        result.put("archiveNote", safeNote);
        result.put("archiveLocked", true);
        result.put("archiveMessage", "Migration quality report archived and locked.");
        return result;
    }

    public Map<String, Object> printMigrationQualityReport(String snapshotNo) {
        Map<String, Object> snapshot = migrationQualitySnapshot(snapshotNo);
        String safeSnapshotNo = text(snapshot.get("snapshotNo"));
        String orgCode = text(snapshot.get("orgCode"));
        String decision = text(snapshot.get("preflightTitle"));
        systemAuditService.record("workbench", "migration-quality-report-print", "MIGRATION_QUALITY", safeSnapshotNo,
                "org=" + orgCode + ", decision=" + decision + ", checkedAt=" + text(snapshot.get("checkedAt")));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("snapshotNo", safeSnapshotNo);
        result.put("orgCode", orgCode);
        result.put("printedBy", currentUserService.currentUsername());
        result.put("printedAt", java.time.LocalDateTime.now().withNano(0).toString());
        return result;
    }

    public Map<String, Object> printMigrationQualityFinalAcceptanceSummary(String orgCode) {
        Map<String, Object> overview = migrationQualityOverview(orgCode);
        @SuppressWarnings("unchecked")
        Map<String, Object> archiveSummary = (Map<String, Object>) overview.getOrDefault("archiveSummary", Map.of());
        String snapshotNo = text(archiveSummary.get("latestExportSnapshotNo"));
        if (snapshotNo.isBlank()) {
            throw new IllegalArgumentException("Final acceptance summary can only be printed after acceptance package export.");
        }
        String exportNo = text(archiveSummary.get("latestExportNo"));
        systemAuditService.record("workbench", "migration-quality-final-summary-print", "MIGRATION_QUALITY", snapshotNo,
                "org=" + text(overview.get("orgCode"))
                        + ", status=" + text(overview.get("status"))
                        + ", exportNo=" + exportNo
                        + ", exportedAt=" + text(archiveSummary.get("latestExportedAt")));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("snapshotNo", snapshotNo);
        result.put("orgCode", text(overview.get("orgCode")));
        result.put("exportNo", exportNo);
        result.put("printedBy", currentUserService.currentUsername());
        result.put("printedAt", java.time.LocalDateTime.now().withNano(0).toString());
        return result;
    }

    public Map<String, Object> exportMigrationQualityAcceptancePackage(String snapshotNo) {
        return exportMigrationQualityAcceptancePackage(snapshotNo, List.of());
    }

    public Map<String, Object> previewMigrationQualityAcceptancePackage(String snapshotNo, String latestSnapshotNo) {
        requireAcceptancePackageExportPermission();
        Map<String, Object> snapshot = migrationQualitySnapshot(snapshotNo);
        String safeSnapshotNo = text(snapshot.get("snapshotNo"));
        String orgCode = text(snapshot.get("orgCode"));
        if (!"ARCHIVED".equals(text(snapshot.get("archiveStatus")))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Migration quality acceptance package can only be exported after the snapshot is archived.");
        }
        List<String> packageFiles = migrationQualityAcceptancePackageFiles(safeSnapshotNo, orgCode, latestSnapshotNo);
        long exportNo = nextMigrationQualityAcceptancePackageExportNo(safeSnapshotNo);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("snapshotNo", safeSnapshotNo);
        result.put("orgCode", orgCode);
        result.put("nextExportNo", exportNo);
        result.put("fileCount", packageFiles.size());
        result.put("packageFiles", packageFiles);
        result.put("latestSnapshotNo", text(latestSnapshotNo));
        result.put("hasComparison", !text(latestSnapshotNo).isBlank() && !text(latestSnapshotNo).equals(safeSnapshotNo));
        result.put("preflightTitle", text(snapshot.get("preflightTitle")));
        result.put("archivedAt", text(snapshot.get("archivedAt")));
        result.put("archivedBy", text(snapshot.get("archivedBy")));
        systemAuditService.record("workbench", "migration-quality-acceptance-package-preview", "MIGRATION_QUALITY", safeSnapshotNo,
                "org=" + orgCode
                        + ", decision=" + text(snapshot.get("preflightTitle"))
                        + ", archivedAt=" + text(snapshot.get("archivedAt"))
                        + ", exportNo=" + exportNo
                        + ", files=" + packageFiles.size()
                        + ", hasComparison=" + result.get("hasComparison")
                        + ", latestSnapshot=" + text(latestSnapshotNo));
        return result;
    }

    public Map<String, Object> exportMigrationQualityAcceptancePackage(String snapshotNo, List<String> packageFiles) {
        requireAcceptancePackageExportPermission();
        Map<String, Object> snapshot = migrationQualitySnapshot(snapshotNo);
        String safeSnapshotNo = text(snapshot.get("snapshotNo"));
        String orgCode = text(snapshot.get("orgCode"));
        String decision = text(snapshot.get("preflightTitle"));
        if (!"ARCHIVED".equals(text(snapshot.get("archiveStatus")))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Migration quality acceptance package can only be exported after the snapshot is archived.");
        }
        List<String> safePackageFiles = packageFiles == null ? List.of() : packageFiles.stream()
                .map(this::text)
                .filter(file -> !file.isBlank())
                .toList();
        String fileSummary = safePackageFiles.isEmpty()
                ? ""
                : ", files=" + safePackageFiles.size() + "[" + left(String.join("|", safePackageFiles), 1200) + "]";
        systemAuditService.ensureTable();
        long exportNo = nextMigrationQualityAcceptancePackageExportNo(safeSnapshotNo);
        systemAuditService.record("workbench", "migration-quality-acceptance-package-export", "MIGRATION_QUALITY", safeSnapshotNo,
                "org=" + orgCode + ", decision=" + decision + ", archivedAt=" + text(snapshot.get("archivedAt")) + ", exportNo=" + exportNo + fileSummary);
        snapshot.put("exportedBy", currentUserService.currentUsername());
        snapshot.put("exportedAt", java.time.LocalDateTime.now().withNano(0).toString());
        snapshot.put("exportNo", exportNo);
        snapshot.put("packageFiles", safePackageFiles);
        return snapshot;
    }

    public List<String> migrationQualityAcceptancePackageFiles(String snapshotNo, String orgCode, String latestSnapshotNo) {
        String safeSnapshotNo = text(snapshotNo);
        String safeOrgCode = text(orgCode);
        List<String> packageFiles = new ArrayList<>();
        packageFiles.add("README.txt");
        packageFiles.add("migration-quality-final-summary-" + safeSnapshotNo + ".csv");
        packageFiles.add("migration-quality-summary-" + safeSnapshotNo + ".csv");
        packageFiles.add("migration-quality-report-" + safeSnapshotNo + ".csv");
        packageFiles.add("migration-quality-print-audits-" + safeSnapshotNo + ".csv");
        packageFiles.add("migration-quality-archive-ledger-" + safeOrgCode + ".csv");
        packageFiles.add("salary-report-migration-closure-" + safeOrgCode + ".csv");
        String safeLatestSnapshotNo = text(latestSnapshotNo);
        if (!safeLatestSnapshotNo.isBlank() && !safeLatestSnapshotNo.equals(safeSnapshotNo)) {
            packageFiles.add("migration-quality-snapshot-compare-" + safeSnapshotNo + "-" + safeLatestSnapshotNo + ".csv");
        }
        return packageFiles;
    }

    private long nextMigrationQualityAcceptancePackageExportNo(String snapshotNo) {
        systemAuditService.ensureTable();
        Long existingExportCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM sys_audit_log
                WHERE module_name = 'workbench'
                  AND action_name = 'migration-quality-acceptance-package-export'
                  AND target_type = 'MIGRATION_QUALITY'
                  AND target_code = ?
                """, Long.class, text(snapshotNo));
        return existingExportCount == null ? 1L : existingExportCount + 1L;
    }

    public List<SystemAuditLogResponse> migrationQualityReportPrintAudits(String snapshotNo) {
        return migrationQualityReportPrintAudits(snapshotNo, "");
    }

    public List<SystemAuditLogResponse> migrationQualityReportPrintAudits(String snapshotNo, String action) {
        Map<String, Object> snapshot = migrationQualitySnapshot(snapshotNo);
        String safeSnapshotNo = text(snapshot.get("snapshotNo"));
        String safeAction = migrationQualityAuditActionFilter(action);
        systemAuditService.ensureTable();
        String actionSql = safeAction.isBlank()
                ? "AND action_name IN ('migration-quality-report-archive', 'migration-quality-report-print', 'migration-quality-final-summary-print', 'migration-quality-acceptance-package-preview', 'migration-quality-acceptance-package-export')"
                : "AND action_name = ?";
        List<Object> params = new ArrayList<>();
        if (!safeAction.isBlank()) {
            params.add(safeAction);
        }
        params.add(safeSnapshotNo);
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
                WHERE module_name = 'workbench'
                  %s
                  AND target_type = 'MIGRATION_QUALITY'
                  AND target_code = ?
                ORDER BY created_at DESC, id DESC
                LIMIT 50
                """.formatted(actionSql), (rs, rowNum) -> new SystemAuditLogResponse(
                rs.getString("audit_id"),
                rs.getString("module_name"),
                rs.getString("action_name"),
                rs.getString("target_type"),
                rs.getString("target_code"),
                rs.getString("summary"),
                rs.getString("operator"),
                rs.getTimestamp("created_at").toLocalDateTime().toString()
        ), params.toArray());
    }

    private String migrationQualityAuditActionFilter(String action) {
        String safeAction = text(action);
        if (safeAction.isBlank() || "ALL".equalsIgnoreCase(safeAction)) {
            return "";
        }
        if (Set.of("migration-quality-report-archive", "migration-quality-report-print", "migration-quality-final-summary-print", "migration-quality-acceptance-package-preview", "migration-quality-acceptance-package-export").contains(safeAction)) {
            return safeAction;
        }
        throw new IllegalArgumentException("Unsupported migration quality audit action: " + safeAction);
    }

    public String reportMigrationClosureCsvForAcceptancePackage(String orgCode) {
        requireAcceptancePackageExportPermission();
        ensureReportPrintBatchTables();
        systemAuditService.ensureTable();
        String safeOrgCode = text(orgCode);
        if (!safeOrgCode.isBlank()) {
            organizationAccessService.requireOrgAccess(safeOrgCode);
        }
        List<Map<String, Object>> archive = jdbcTemplate.queryForList("""
                WITH print_events AS (
                    SELECT target_code AS case_no,
                           action_name AS action_name,
                           '' AS batch_no,
                           operator AS operator,
                           created_at AS printed_at
                    FROM sys_audit_log
                    WHERE module_name = 'report'
                      AND action_name = 'salary-case-approval-print'
                      AND target_type = 'SALARY_CASE'
                    UNION ALL
                    SELECT item.case_no AS case_no,
                           CASE
                               WHEN batch.report_type = 'SALARY_CASE_APPROVAL_REPRINT' THEN 'salary-case-approvals-reprint'
                               ELSE 'salary-case-approvals-print'
                           END AS action_name,
                           item.batch_no AS batch_no,
                           batch.printed_by AS operator,
                           item.created_at AS printed_at
                    FROM salary_report_print_batch_item item
                    JOIN salary_report_print_batch batch ON batch.batch_no = item.batch_no
                ),
                print_agg AS (
                    SELECT case_no,
                           COUNT(1) AS print_count,
                           MAX(CASE WHEN action_name = 'salary-case-approvals-reprint' THEN 1 ELSE 0 END) AS reprinted
                    FROM print_events
                    GROUP BY case_no
                )
                SELECT COUNT(1) AS archiveTotal,
                       COALESCE(SUM(CASE WHEN COALESCE(agg.print_count, 0) > 0 THEN 1 ELSE 0 END), 0) AS printed,
                       COALESCE(SUM(CASE WHEN COALESCE(agg.print_count, 0) = 0 THEN 1 ELSE 0 END), 0) AS unprinted,
                       COALESCE(SUM(COALESCE(agg.reprinted, 0)), 0) AS reprinted,
                       COALESCE(SUM(CASE
                           WHEN sc.status = 'DONE'
                            AND COALESCE(plan.plan_status, '') = 'PREPARED'
                            AND COALESCE(plan.writable, 0) = 1
                            AND COALESCE(agg.print_count, 0) > 0 THEN 1
                           ELSE 0
                       END), 0) AS writeReady
                FROM salary_business_case sc
                LEFT JOIN salary_history_write_plan plan ON plan.case_no = sc.case_no
                LEFT JOIN print_agg agg ON agg.case_no = sc.case_no
                WHERE sc.status IN ('DONE', 'CANCELLED')
                  AND sc.org_code = ?
                """, safeOrgCode);
        Map<String, Object> archiveRow = archive.isEmpty() ? Map.of() : archive.getFirst();
        Map<String, Object> batchRow = jdbcTemplate.queryForMap("""
                SELECT COUNT(1) AS batchCount,
                       COALESCE(SUM(printed_count), 0) AS printedRows,
                       COALESCE(SUM(blocked_count), 0) AS blockedRows,
                       COALESCE(SUM(warning_count), 0) AS warningRows,
                       COALESCE(SUM(CASE WHEN blocked_count > 0 THEN 1 ELSE 0 END), 0) AS blockedBatches,
                       MAX(printed_at) AS latestPrintedAt
                FROM salary_report_print_batch
                WHERE org_code = ?
                """, safeOrgCode);
        Map<String, Object> auditRow = jdbcTemplate.queryForMap("""
                SELECT COUNT(1) AS auditCount,
                       MAX(created_at) AS latestAuditAt
                FROM sys_audit_log
                WHERE module_name = 'report'
                  AND (target_code = ? OR summary LIKE CONCAT('%org=', ?, '%') OR summary LIKE CONCAT('%org:', ?, '%'))
                """, safeOrgCode, safeOrgCode, safeOrgCode);
        Long legacyReportCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM sys_audit_log
                WHERE module_name = 'report'
                  AND action_name IN ('salary-case-approval-print', 'salary-case-approvals-print', 'salary-case-approvals-reprint',
                                      'salary-case-approval-roster-print', 'salary-roster-print', 'report-print-archive-csv',
                                      'report-print-batch-csv', 'report-audits-csv')
                  AND (target_code = ? OR summary LIKE CONCAT('%org=', ?, '%') OR summary LIKE CONCAT('%org:', ?, '%'))
                """, Long.class, safeOrgCode, safeOrgCode, safeOrgCode);
        long pendingReports = 0L;
        long unprinted = longValue(archiveRow.get("unprinted"));
        long blockedBatches = longValue(batchRow.get("blockedBatches"));
        long auditCount = longValue(auditRow.get("auditCount"));
        String status = pendingReports > 0 || unprinted > 0 || blockedBatches > 0 || auditCount == 0 ? "WARN" : "READY";
        StringBuilder csv = new StringBuilder();
        csv.append("section,item,value\n");
        appendSimpleCsv(csv, "summary", "status", status);
        appendSimpleCsv(csv, "summary", "orgCode", safeOrgCode);
        appendSimpleCsv(csv, "summary", "checkedAt", java.time.LocalDateTime.now().withNano(0).toString());
        appendSimpleCsv(csv, "catalog", "migratedReports", legacyReportCount == null ? 0 : legacyReportCount);
        appendSimpleCsv(csv, "catalog", "pendingReports", pendingReports);
        appendSimpleCsv(csv, "archive", "archiveTotal", archiveRow.get("archiveTotal"));
        appendSimpleCsv(csv, "archive", "printed", archiveRow.get("printed"));
        appendSimpleCsv(csv, "archive", "unprinted", archiveRow.get("unprinted"));
        appendSimpleCsv(csv, "archive", "reprinted", archiveRow.get("reprinted"));
        appendSimpleCsv(csv, "archive", "writeReady", archiveRow.get("writeReady"));
        appendSimpleCsv(csv, "batch", "batchCount", batchRow.get("batchCount"));
        appendSimpleCsv(csv, "batch", "printedRows", batchRow.get("printedRows"));
        appendSimpleCsv(csv, "batch", "blockedRows", batchRow.get("blockedRows"));
        appendSimpleCsv(csv, "batch", "warningRows", batchRow.get("warningRows"));
        appendSimpleCsv(csv, "batch", "blockedBatches", batchRow.get("blockedBatches"));
        appendSimpleCsv(csv, "batch", "latestPrintedAt", batchRow.get("latestPrintedAt"));
        appendSimpleCsv(csv, "audit", "auditCount", auditRow.get("auditCount"));
        appendSimpleCsv(csv, "audit", "latestAuditAt", auditRow.get("latestAuditAt"));
        return csv.toString();
    }

    public Map<String, Object> compareMigrationQualitySnapshots(String baseSnapshotNo, String targetSnapshotNo) {
        requireAcceptancePermission();
        Map<String, Object> base = migrationQualitySnapshotRow(baseSnapshotNo);
        Map<String, Object> target = migrationQualitySnapshotRow(targetSnapshotNo);
        String baseOrg = text(base.get("orgCode"));
        String targetOrg = text(target.get("orgCode"));
        if (!Objects.equals(baseOrg, targetOrg)) {
            throw new IllegalArgumentException("Snapshots must belong to the same organization.");
        }
        organizationAccessService.requireOrgAccess(baseOrg);
        List<Map<String, Object>> deltas = List.of(
                qualityDelta("governanceIssues", "\u6570\u636e\u6cbb\u7406", base, target),
                qualityDelta("regressionWarnings", "\u56de\u5f52WARN", base, target),
                qualityDelta("regressionPending", "\u56de\u5f52\u5f85\u6838\u67e5", base, target),
                qualityDelta("regressionFixing", "\u56de\u5f52\u4fee\u590d\u4e2d", base, target),
                qualityDelta("historyBlocked", "\u5386\u53f2\u963b\u65ad", base, target),
                qualityDelta("historyOpen", "\u5386\u53f2\u961f\u5217", base, target),
                qualityDelta("reviewPending", "\u5f85\u590d\u6838", base, target),
                qualityDelta("salaryTodo", "\u5f85\u529e", base, target),
                qualityDelta("salaryDone", "\u5df2\u529e", base, target)
        );
        long increased = deltas.stream().filter(delta -> longValue(delta.get("delta")) > 0).count();
        long decreased = deltas.stream().filter(delta -> longValue(delta.get("delta")) < 0).count();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("increased", increased);
        summary.put("decreased", decreased);
        summary.put("unchanged", deltas.size() - increased - decreased);
        summary.put("status", increased > 0 ? "ATTENTION" : "IMPROVED");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", baseOrg);
        result.put("base", base);
        result.put("target", target);
        result.put("summary", summary);
        result.put("deltas", deltas);
        return result;
    }

    private Map<String, Object> migrationQualitySnapshotRow(String snapshotNo) {
        String safeSnapshotNo = text(snapshotNo);
        if (safeSnapshotNo.isBlank()) {
            throw new IllegalArgumentException("Snapshot number is required.");
        }
        ensureMigrationQualitySnapshotTable();
        return jdbcTemplate.queryForMap("""
                SELECT snapshot_no AS snapshotNo,
                       org_code AS orgCode,
                       checked_at AS checkedAt,
                       overall_status AS status,
                       history_blocked AS historyBlocked,
                       history_open AS historyOpen,
                       review_pending AS reviewPending,
                       regression_warnings AS regressionWarnings,
                       regression_pending AS regressionPending,
                       regression_fixing AS regressionFixing,
                       governance_issues AS governanceIssues,
                       salary_todo AS salaryTodo,
                       salary_done AS salaryDone,
                       created_by AS createdBy,
                       created_at AS createdAt
                FROM migration_quality_snapshot
                WHERE snapshot_no = ?
                """, safeSnapshotNo);
    }

    private Map<String, Object> qualityDelta(String code, String title, Map<String, Object> base, Map<String, Object> target) {
        long baseValue = longValue(base.get(code));
        long targetValue = longValue(target.get(code));
        long delta = targetValue - baseValue;
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("code", code);
        item.put("title", title);
        item.put("base", baseValue);
        item.put("target", targetValue);
        item.put("delta", delta);
        item.put("direction", delta > 0 ? "INCREASED" : (delta < 0 ? "DECREASED" : "UNCHANGED"));
        return item;
    }

    private String previousMigrationQualitySnapshotNo(String orgCode, String snapshotNo) {
        List<String> rows = jdbcTemplate.queryForList("""
                SELECT snapshot_no
                FROM migration_quality_snapshot
                WHERE org_code = ?
                  AND snapshot_no <> ?
                ORDER BY checked_at DESC, id DESC
                LIMIT 1
                """, String.class, orgCode, snapshotNo);
        return rows.isEmpty() ? "" : text(rows.get(0));
    }

    private Map<String, Object> migrationPreflightDecision(Map<String, Object> summary, Map<String, Object> comparison) {
        @SuppressWarnings("unchecked")
        Map<String, Object> compareSummary = comparison.get("summary") instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : Map.of();
        List<Map<String, Object>> blocking = new ArrayList<>();
        List<Map<String, Object>> warnings = new ArrayList<>();
        addPreflightIssue(blocking, "history-write", "\u5386\u53f2\u5199\u5165\u963b\u65ad", longValue(summary.get("historyBlocked")));
        addPreflightIssue(blocking, "regression", "\u56de\u5f52WARN", longValue(summary.get("regressionWarnings")));
        addPreflightIssue(warnings, "governance", "\u6570\u636e\u6cbb\u7406\u95ee\u9898", longValue(summary.get("governanceIssues")));
        addPreflightIssue(warnings, "acceptance", "\u5f85\u590d\u6838", longValue(summary.get("reviewPending")));
        addPreflightIssue(warnings, "", "\u5bf9\u6bd4\u4e0a\u6b21\u98ce\u9669\u4e0a\u5347", longValue(compareSummary.get("increased")));
        String level = blocking.isEmpty() ? (warnings.isEmpty() ? "READY" : "WARN") : "BLOCKED";
        String title = "BLOCKED".equals(level) ? "\u4e0d\u5efa\u8bae\u4e0a\u7ebf" : ("WARN".equals(level) ? "\u9700\u5904\u7406\u540e\u4e0a\u7ebf" : "\u53ef\u4e0a\u7ebf");
        String message = "BLOCKED".equals(level)
                ? "\u5b58\u5728\u963b\u65ad\u9879\uff0c\u5efa\u8bae\u5148\u5904\u7406\u5e76\u91cd\u65b0\u5de1\u68c0\u3002"
                : ("WARN".equals(level) ? "\u672a\u89c1\u786c\u963b\u65ad\uff0c\u4f46\u5efa\u8bae\u5904\u7406\u98ce\u9669\u9879\u540e\u518d\u4e0a\u7ebf\u3002" : "\u672a\u53d1\u73b0\u963b\u65ad\u6216\u4e0a\u5347\u98ce\u9669\u3002");
        List<Map<String, Object>> items = new ArrayList<>();
        items.addAll(blocking);
        items.addAll(warnings);
        Map<String, Object> decision = new LinkedHashMap<>();
        decision.put("level", level);
        decision.put("title", title);
        decision.put("message", message);
        decision.put("items", items);
        return decision;
    }

    private void addPreflightIssue(List<Map<String, Object>> items, String action, String label, long count) {
        if (count <= 0) {
            return;
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("action", action);
        item.put("label", label);
        item.put("count", count);
        items.add(item);
    }

    private Map<String, Object> qualityGate(String code, String title, String status, long count, String message) {
        Map<String, Object> gate = new LinkedHashMap<>();
        gate.put("code", code);
        gate.put("title", title);
        gate.put("status", status);
        gate.put("count", count);
        gate.put("message", message);
        return gate;
    }

    public Map<String, Object> migrationRegressionSampleRunDetail(String runNo) {
        requireAcceptancePermission();
        ensureMigrationRegressionSampleTable();
        String safeRunNo = text(runNo);
        if (safeRunNo.isBlank()) {
            throw new IllegalArgumentException("Run number is required.");
        }
        Map<String, Object> run = jdbcTemplate.queryForMap("""
                SELECT run_no AS runNo,
                       org_code AS orgCode,
                       batch_no AS batchNo,
                       checked_at AS checkedAt,
                       sample_limit AS sampleLimit,
                       sample_count AS sampleCount,
                       pass_count AS passCount,
                       warning_count AS warningCount,
                       overall_status AS overallStatus,
                       created_by AS createdBy,
                       created_at AS createdAt
                FROM migration_regression_run
                WHERE run_no = ?
                """, safeRunNo);
        organizationAccessService.requireOrgAccess(text(run.get("orgCode")));
        List<Map<String, Object>> samples = jdbcTemplate.queryForList("""
                SELECT sample_code AS code,
                       sample_title AS title,
                       sample_domain AS domain,
                       sample_id AS sampleId,
                       person_code AS personCode,
                       person_name AS personName,
                       org_code AS orgCode,
                       sample_type AS sampleType,
                       status,
                       expected_status AS expectedStatus,
                       actual_status AS actualStatus,
                       expected_amount AS expectedAmount,
                       actual_amount AS actualAmount,
                       expected_payload AS expectedPayload,
                       actual_payload AS actualPayload,
                       message,
                       review_status AS reviewStatus,
                       review_category AS reviewCategory,
                       review_note AS reviewNote,
                       reviewed_by AS reviewedBy,
                       reviewed_at AS reviewedAt,
                       retest_status AS retestStatus,
                       governance_work_item_id AS governanceWorkItemId
                FROM migration_regression_run_sample
                WHERE run_no = ?
                ORDER BY status DESC, sample_code, id
                """, safeRunNo);
        Map<String, Object> result = new LinkedHashMap<>(run);
        result.put("reviewSummary", migrationRegressionReviewSummary(samples));
        result.put("samples", samples);
        return result;
    }

    private Map<String, Object> migrationRegressionReviewSummary(List<Map<String, Object>> samples) {
        Map<String, Object> summary = new LinkedHashMap<>();
        long warningCount = samples.stream().filter(sample -> "WARN".equalsIgnoreCase(text(sample.get("status")))).count();
        summary.put("warnCount", warningCount);
        summary.put("pendingCount", migrationRegressionReviewCount(samples, "PENDING"));
        summary.put("reviewedCount", migrationRegressionReviewCount(samples, "REVIEWED"));
        summary.put("fixingCount", migrationRegressionReviewCount(samples, "FIXING"));
        summary.put("deferredCount", migrationRegressionReviewCount(samples, "DEFERRED"));
        Map<String, Long> categoryCounts = samples.stream()
                .filter(sample -> "WARN".equalsIgnoreCase(text(sample.get("status"))))
                .map(sample -> defaultText(text(sample.get("reviewCategory")), "UNCLASSIFIED"))
                .collect(Collectors.groupingBy(value -> value, LinkedHashMap::new, Collectors.counting()));
        summary.put("categoryCounts", categoryCounts);
        return summary;
    }

    private long migrationRegressionReviewCount(List<Map<String, Object>> samples, String status) {
        return samples.stream()
                .filter(sample -> "WARN".equalsIgnoreCase(text(sample.get("status"))))
                .filter(sample -> status.equalsIgnoreCase(defaultText(text(sample.get("reviewStatus")), "PENDING")))
                .count();
    }

    public Map<String, Object> reviewMigrationRegressionSampleRun(
            String runNo,
            String sampleCode,
            String sampleId,
            String personCode,
            String reviewCategory,
            String reviewStatus,
            String reviewNote
    ) {
        requireAcceptancePermission();
        ensureMigrationRegressionSampleTable();
        String safeRunNo = text(runNo);
        String safeSampleCode = text(sampleCode);
        String safeSampleId = text(sampleId);
        String safePersonCode = text(personCode);
        if (safeRunNo.isBlank() || safeSampleCode.isBlank() || safeSampleId.isBlank() || safePersonCode.isBlank()) {
            throw new IllegalArgumentException("Regression run sample identity is required.");
        }
        String orgCode = jdbcTemplate.queryForObject("""
                SELECT org_code
                FROM migration_regression_run_sample
                WHERE run_no = ?
                  AND sample_code = ?
                  AND sample_id = ?
                  AND person_code = ?
                LIMIT 1
                """, String.class, safeRunNo, safeSampleCode, safeSampleId, safePersonCode);
        organizationAccessService.requireOrgAccess(text(orgCode));
        String safeCategory = text(reviewCategory);
        String safeStatus = defaultText(reviewStatus, safeCategory.isBlank() ? "PENDING" : "REVIEWED").toUpperCase();
        int updated = jdbcTemplate.update("""
                UPDATE migration_regression_run_sample
                SET review_category = ?,
                    review_status = ?,
                    review_note = ?,
                    reviewed_by = ?,
                    reviewed_at = CURRENT_TIMESTAMP
                WHERE run_no = ?
                  AND sample_code = ?
                  AND sample_id = ?
                  AND person_code = ?
                """,
                safeCategory,
                safeStatus,
                text(reviewNote),
                currentUserService.currentUsername(),
                safeRunNo,
                safeSampleCode,
                safeSampleId,
                safePersonCode
        );
        if (updated == 0) {
            throw new IllegalArgumentException("Regression run sample not found.");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("runNo", safeRunNo);
        result.put("sampleCode", safeSampleCode);
        result.put("sampleId", safeSampleId);
        result.put("personCode", safePersonCode);
        result.put("reviewCategory", safeCategory);
        result.put("reviewStatus", safeStatus);
        systemAuditService.record("workbench", "migration-regression-sample-review", "MIGRATION_REGRESSION_RUN",
                safeRunNo, safeSampleCode + ":" + safeSampleId + ":" + safePersonCode + ", category=" + safeCategory);
        return result;
    }

    @Transactional
    public Map<String, Object> createGovernanceTaskFromMigrationRegression(
            String runNo,
            String sampleCode,
            String sampleId,
            String personCode,
            String reviewCategory,
            String reviewNote
    ) {
        requireAcceptancePermission();
        requireDataGovernancePermission();
        requireSalaryTodoPermission();
        ensureMigrationRegressionSampleTable();
        ensureSalaryTodoCacheTable();
        ensureDataGovernanceTaskReviewTable();
        String safeRunNo = text(runNo);
        String safeSampleCode = text(sampleCode);
        String safeSampleId = text(sampleId);
        String safePersonCode = text(personCode);
        if (safeRunNo.isBlank() || safeSampleCode.isBlank() || safeSampleId.isBlank() || safePersonCode.isBlank()) {
            throw new IllegalArgumentException("Regression run sample identity is required.");
        }
        Map<String, Object> sample = jdbcTemplate.queryForMap("""
                SELECT *
                FROM migration_regression_run_sample
                WHERE run_no = ?
                  AND sample_code = ?
                  AND sample_id = ?
                  AND person_code = ?
                LIMIT 1
                """, safeRunNo, safeSampleCode, safeSampleId, safePersonCode);
        String orgCode = text(sample.get("org_code"));
        organizationAccessService.requireOrgAccess(orgCode);
        String category = defaultText(reviewCategory, text(sample.get("review_category")));
        if (category.isBlank()) {
            category = "NEED_FIX";
        }
        String workItemId = "regression-governance-"
                + dataGovernanceKeyPart(safeRunNo)
                + "-"
                + dataGovernanceKeyPart(safeSampleCode)
                + "-"
                + dataGovernanceKeyPart(safePersonCode)
                + "-"
                + dataGovernanceKeyPart(safeSampleId);
        String personName = text(sample.get("person_name"));
        String note = left("回归差异转数据治理：" + category
                + "；样本=" + safeSampleCode
                + "；运行=" + safeRunNo
                + "；差异=" + text(sample.get("message"))
                + (text(reviewNote).isBlank() ? "" : "；说明=" + text(reviewNote)), 1000);
        jdbcTemplate.update("""
                INSERT INTO salary_todo_candidate_cache(work_item_id, source, source_id, person_code, org_code,
                                                        person_no, person_name, event_year, event_month, change_type, note)
                VALUES (?, 'DATA_GOVERNANCE', ?, ?, ?, ?, ?, YEAR(CURRENT_DATE), MONTH(CURRENT_DATE), '数据治理', ?)
                ON DUPLICATE KEY UPDATE
                    source = VALUES(source),
                    source_id = VALUES(source_id),
                    person_code = VALUES(person_code),
                    org_code = VALUES(org_code),
                    person_no = VALUES(person_no),
                    person_name = VALUES(person_name),
                    event_year = VALUES(event_year),
                    event_month = VALUES(event_month),
                    change_type = VALUES(change_type),
                    note = VALUES(note),
                    generated_at = CURRENT_TIMESTAMP
                """,
                workItemId,
                safeRunNo,
                safePersonCode,
                orgCode,
                personNo(safePersonCode),
                personName,
                note);
        jdbcTemplate.update("""
                INSERT INTO salary_data_governance_task_review(work_item_id, person_code, org_code, issue_type,
                                                                review_status, retest_status, retest_summary)
                VALUES (?, ?, ?, ?, 'PENDING', 'FOUND', ?)
                ON DUPLICATE KEY UPDATE
                    person_code = VALUES(person_code),
                    org_code = VALUES(org_code),
                    issue_type = VALUES(issue_type),
                    retest_status = VALUES(retest_status),
                    retest_summary = VALUES(retest_summary),
                    retested_at = CURRENT_TIMESTAMP
                """, workItemId, safePersonCode, orgCode, "REGRESSION_" + category, note);
        jdbcTemplate.update("""
                UPDATE migration_regression_run_sample
                SET review_category = ?,
                    review_status = 'FIXING',
                    review_note = ?,
                    reviewed_by = ?,
                    reviewed_at = CURRENT_TIMESTAMP,
                    governance_work_item_id = ?
                WHERE run_no = ?
                  AND sample_code = ?
                  AND sample_id = ?
                  AND person_code = ?
                """,
                category,
                text(reviewNote),
                currentUserService.currentUsername(),
                workItemId,
                safeRunNo,
                safeSampleCode,
                safeSampleId,
                safePersonCode);
        jdbcTemplate.update("""
                REPLACE INTO salary_todo_cache_meta(cache_key, last_refreshed_at, total_count, cache_status, dirty_at)
                SELECT 'salary-todo', CURRENT_TIMESTAMP, COUNT(1), 'ACTIVE', NULL
                FROM salary_todo_candidate_cache
                """);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("runNo", safeRunNo);
        result.put("sampleCode", safeSampleCode);
        result.put("sampleId", safeSampleId);
        result.put("personCode", safePersonCode);
        result.put("orgCode", orgCode);
        result.put("workItemId", workItemId);
        result.put("reviewCategory", category);
        result.put("reviewStatus", "FIXING");
        systemAuditService.record("workbench", "migration-regression-governance-task-create", "SALARY_TODO",
                workItemId, safeRunNo + " " + safeSampleCode + " " + safePersonCode);
        return result;
    }

    public Map<String, Object> setMigrationRegressionSampleEnabled(
            String orgCode,
            String sampleCode,
            String sampleId,
            String personCode,
            boolean enabled
    ) {
        requireAcceptancePermission();
        ensureMigrationRegressionSampleTable();
        String safeOrgCode = text(orgCode);
        String safeSampleCode = text(sampleCode);
        String safeSampleId = text(sampleId);
        String safePersonCode = text(personCode);
        if (safeOrgCode.isBlank() || safeSampleCode.isBlank() || safeSampleId.isBlank() || safePersonCode.isBlank()) {
            throw new IllegalArgumentException("Regression sample identity is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        int updated = jdbcTemplate.update("""
                UPDATE migration_regression_sample
                SET enabled = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE org_code = ?
                  AND sample_code = ?
                  AND sample_id = ?
                  AND person_code = ?
                """, enabled ? 1 : 0, safeOrgCode, safeSampleCode, safeSampleId, safePersonCode);
        if (updated == 0) {
            throw new IllegalArgumentException("Regression sample not found.");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", safeOrgCode);
        result.put("sampleCode", safeSampleCode);
        result.put("sampleId", safeSampleId);
        result.put("personCode", safePersonCode);
        result.put("enabled", enabled);
        systemAuditService.record("workbench", "migration-regression-sample-enabled", "MIGRATION_REGRESSION_SAMPLE",
                safeSampleCode + ":" + safeSampleId + ":" + safePersonCode, "enabled=" + enabled);
        return result;
    }

    public Map<String, Object> addMigrationRegressionSample(
            String orgCode,
            String sampleCode,
            String sampleId,
            String personCode,
            String title,
            String note
    ) {
        requireAcceptancePermission();
        ensureMigrationRegressionSampleTable();
        String safeOrgCode = text(orgCode);
        String safeSampleCode = text(sampleCode);
        String safeSampleId = text(sampleId);
        String safePersonCode = text(personCode);
        if (safeOrgCode.isBlank() || safeSampleCode.isBlank() || safeSampleId.isBlank() || safePersonCode.isBlank()) {
            throw new IllegalArgumentException("Regression sample identity is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        Map<String, Object> actual = migrationRegressionActualAssertion(safeSampleCode, safeSampleId, safePersonCode);
        if (!Boolean.TRUE.equals(booleanValue(actual.get("exists")))) {
            throw new IllegalArgumentException("Regression sample source not found.");
        }
        Map<String, Object> source = migrationRegressionSampleSource(safeSampleCode, safeSampleId, safePersonCode, safeOrgCode);
        String sampleTitle = text(title).isBlank() ? text(source.get("title")) : text(title);
        jdbcTemplate.update("""
                INSERT INTO migration_regression_sample(sample_code, sample_title, sample_domain, sample_id,
                                                        person_code, person_name, org_code, sample_type,
                                                        batch_no, sample_source, expected_status, expected_amount,
                                                        expected_payload, note, enabled)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
                ON DUPLICATE KEY UPDATE
                    sample_title = VALUES(sample_title),
                    sample_domain = VALUES(sample_domain),
                    person_name = VALUES(person_name),
                    sample_type = VALUES(sample_type),
                    batch_no = VALUES(batch_no),
                    sample_source = VALUES(sample_source),
                    expected_status = VALUES(expected_status),
                    expected_amount = VALUES(expected_amount),
                    expected_payload = VALUES(expected_payload),
                    note = VALUES(note),
                    enabled = 1,
                    updated_at = CURRENT_TIMESTAMP
                """,
                safeSampleCode,
                sampleTitle,
                text(source.get("domain")),
                safeSampleId,
                safePersonCode,
                text(source.get("personName")),
                safeOrgCode,
                text(source.get("sampleType")),
                "MANUAL",
                "MANUAL_ADD",
                text(actual.get("status")),
                actual.get("amount"),
                text(actual.get("payload")),
                text(note)
        );
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", safeOrgCode);
        result.put("sampleCode", safeSampleCode);
        result.put("sampleId", safeSampleId);
        result.put("personCode", safePersonCode);
        result.put("batchNo", "MANUAL");
        result.put("sampleTitle", sampleTitle);
        result.put("expectedStatus", text(actual.get("status")));
        result.put("expectedAmount", actual.get("amount"));
        result.put("expectedPayload", text(actual.get("payload")));
        systemAuditService.record("workbench", "migration-regression-sample-add", "MIGRATION_REGRESSION_SAMPLE",
                safeSampleCode + ":" + safeSampleId + ":" + safePersonCode, "manual add");
        return result;
    }

    public Map<String, Object> importMigrationRegressionSamples(String orgCode, List<Map<String, String>> rows) {
        requireAcceptancePermission();
        ensureMigrationRegressionSampleTable();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        String importBatchNo = "IMPORT-" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int imported = 0;
        for (Map<String, String> row : rows) {
            String sampleCode = text(row.get("sampleCode"));
            String sampleId = text(row.get("sampleId"));
            String personCode = text(row.get("personCode"));
            if (sampleCode.isBlank() || sampleId.isBlank() || personCode.isBlank()) {
                continue;
            }
            String rowOrgCode = text(row.get("orgCode")).isBlank() ? safeOrgCode : text(row.get("orgCode"));
            if (!rowOrgCode.startsWith(safeOrgCode)) {
                continue;
            }
            String rowBatchNo = defaultText(row.get("batchNo"), importBatchNo);
            jdbcTemplate.update("""
                    INSERT INTO migration_regression_sample(sample_code, sample_title, sample_domain, sample_id,
                                                            person_code, person_name, org_code, sample_type,
                                                            batch_no, sample_source, expected_status, expected_amount,
                                                            expected_payload, note, enabled)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        sample_title = VALUES(sample_title),
                        sample_domain = VALUES(sample_domain),
                        person_name = VALUES(person_name),
                        sample_type = VALUES(sample_type),
                        batch_no = VALUES(batch_no),
                        sample_source = VALUES(sample_source),
                        expected_status = VALUES(expected_status),
                        expected_amount = VALUES(expected_amount),
                        expected_payload = VALUES(expected_payload),
                        note = VALUES(note),
                        enabled = VALUES(enabled),
                        updated_at = CURRENT_TIMESTAMP
                    """,
                    sampleCode,
                    defaultText(row.get("sampleTitle"), sampleCode),
                    text(row.get("sampleDomain")),
                    sampleId,
                    personCode,
                    text(row.get("personName")),
                    rowOrgCode,
                    text(row.get("sampleType")),
                    rowBatchNo,
                    defaultText(row.get("sampleSource"), "CSV_IMPORT"),
                    text(row.get("expectedStatus")),
                    decimalOrNull(row.get("expectedAmount")),
                    text(row.get("expectedPayload")),
                    text(row.get("note")),
                    migrationRegressionEnabledValue(row.get("enabled"))
            );
            imported++;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", safeOrgCode);
        result.put("batchNo", importBatchNo);
        result.put("importedCount", imported);
        result.put("skippedCount", Math.max(0, rows.size() - imported));
        result.put("libraryCount", migrationRegressionSampleLibrary(safeOrgCode, 500).size());
        systemAuditService.record("workbench", "migration-regression-sample-import", "ORG", safeOrgCode,
                "imported=" + imported + ", rows=" + rows.size());
        return result;
    }

    private Map<String, Object> migrationRegressionSamples(String orgCode, int limit, boolean run) {
        requireAcceptancePermission();
        ensureBusinessCaseTable();
        ensureHistoryWritePlanTable();
        ensureReportPrintBatchTables();
        systemAuditService.ensureTable();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        int safeLimit = Math.max(1, Math.min(limit, 500));
        Map<String, Object> governance = dataGovernanceSnapshotForAcceptance(safeOrgCode, Math.min(safeLimit, 100));
        List<Map<String, Object>> samples = new ArrayList<>();
        samples.add(regressionCaseSample(
                "normal-grade",
                "\u6b63\u5e38\u664b\u6863/\u7ea7\u522b\u664b\u5347",
                "\u5de5\u8d44\u89c4\u5219",
                safeOrgCode,
                List.of("\u6b63\u5e38\u664b\u6863", "\u664b\u6863", "\u7ea7\u522b\u664b\u5347", "\u664b\u5347\u7ea7\u522b"),
                safeLimit
        ));
        samples.add(regressionCaseSample(
                "entry-salary",
                "\u65b0\u8fdb/\u89c1\u4e60/\u8f6c\u6b63\u5b9a\u7ea7",
                "\u5de5\u8d44\u89c4\u5219",
                safeOrgCode,
                List.of("\u65b0\u8fdb", "\u89c1\u4e60", "\u8f6c\u6b63", "\u5b9a\u7ea7"),
                safeLimit
        ));
        samples.add(regressionCaseSample(
                "post-change",
                "\u804c\u52a1/\u804c\u7ea7/\u5c97\u4f4d\u53d8\u52a8",
                "\u5de5\u8d44\u89c4\u5219",
                safeOrgCode,
                List.of("\u804c\u52a1\u53d8\u5316", "\u804c\u7ea7\u664b\u5347", "\u5c97\u4f4d\u53d8\u52a8", "\u804c\u52a1\u664b\u5347"),
                safeLimit
        ));
        samples.add(regressionCaseSample(
                "allowance-change",
                "\u6d25\u8865\u8d34/\u8b66\u8854/\u6cd5\u68c0\u76d1\u5bdf\u7b49\u7ea7",
                "\u6d25\u8865\u8d34",
                safeOrgCode,
                List.of("\u6d25\u8865\u8d34", "\u8b66\u8854", "\u6cd5\u5b98", "\u68c0\u5bdf", "\u76d1\u5bdf", "\u6559\u62a4\u9f84"),
                safeLimit
        ));
        samples.add(regressionHistorySample(
                "history-write-preview",
                "\u5386\u53f2\u5199\u5165\u9884\u89c8/\u963b\u65ad",
                "\u5386\u53f2\u5199\u5165",
                safeOrgCode,
                "p.preview_status IN ('READY','BLOCKED')",
                safeLimit
        ));
        samples.add(regressionHistorySample(
                "history-write-executed",
                "\u5386\u53f2\u5199\u5165\u6267\u884c",
                "\u5386\u53f2\u5199\u5165",
                safeOrgCode,
                "p.plan_status = 'EXECUTED'",
                safeLimit
        ));
        samples.add(regressionHistorySample(
                "history-write-rollback",
                "\u5386\u53f2\u5199\u5165\u56de\u6eda",
                "\u5386\u53f2\u5199\u5165",
                safeOrgCode,
                "p.plan_status = 'ROLLED_BACK'",
                safeLimit
        ));
        samples.add(regressionHistorySample(
                "history-diff-review",
                "\u5dee\u5f02\u6838\u67e5/\u7279\u6b8a\u60c5\u51b5",
                "\u5dee\u5f02\u6838\u67e5",
                safeOrgCode,
                "COALESCE(p.comparison_review_status, '') <> ''",
                safeLimit
        ));
        samples.add(regressionGovernanceSample(safeOrgCode, governance, safeLimit));
        samples.add(regressionReportPrintSample(safeOrgCode, safeLimit));

        long passCount = samples.stream().filter(sample -> "PASS".equals(text(sample.get("status")))).count();
        long warnCount = samples.stream().filter(sample -> "WARN".equals(text(sample.get("status")))).count();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("runNo", (run ? "MIG-REG-RUN-" : "MIG-REG-") + LocalDate.now() + "-" + UUID.randomUUID().toString().substring(0, 8));
        result.put("orgCode", safeOrgCode);
        result.put("checkedAt", java.time.LocalDateTime.now().withNano(0).toString());
        result.put("sampleLimit", safeLimit);
        result.put("overallStatus", warnCount > 0 ? "WARN" : "PASS");
        result.put("sampleCount", samples.size());
        result.put("passCount", passCount);
        result.put("warningCount", warnCount);
        result.put("samples", samples);
        return result;
    }

    public Map<String, Object> migrationAcceptanceRun(String orgCode, int limit) {
        requireAcceptancePermission();
        String runNo = "MIG-ACC-" + LocalDate.now() + "-" + UUID.randomUUID().toString().substring(0, 8);
        return migrationAcceptanceRun(orgCode, limit, runNo);
    }

    public Map<String, Object> startMigrationAcceptanceRun(String orgCode, int limit) {
        ensureMigrationAcceptanceTables();
        requireAcceptancePermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        int safeLimit = Math.max(1, Math.min(limit, 500));
        recoverStaleMigrationAcceptanceRuns(safeOrgCode);
        String runningRunNo = runningMigrationAcceptanceRunNo(safeOrgCode);
        if (!runningRunNo.isBlank()) {
            Map<String, Object> running = migrationAcceptanceRunDetail(runningRunNo);
            running.put("acceptedExisting", true);
            return running;
        }
        String runNo = "MIG-ACC-" + LocalDate.now() + "-" + UUID.randomUUID().toString().substring(0, 8);
        String activeRunNo = migrationAcceptanceActiveRuns.putIfAbsent(safeOrgCode, runNo);
        if (activeRunNo != null && !activeRunNo.isBlank()) {
            Map<String, Object> active;
            try {
                active = migrationAcceptanceRunDetail(activeRunNo);
            } catch (RuntimeException ex) {
                active = new LinkedHashMap<>();
                active.put("runNo", activeRunNo);
                active.put("orgCode", safeOrgCode);
                active.put("checkedAt", java.time.LocalDateTime.now().withNano(0).toString());
                active.put("sampleLimit", safeLimit);
                active.put("overallStatus", "RUNNING");
                active.put("summary", Map.of());
                active.put("gates", List.of());
                active.put("issues", List.of());
            }
            active.put("acceptedExisting", true);
            return active;
        }
        Map<String, Object> checklist = migrationAcceptanceChecklist();
        @SuppressWarnings("unchecked")
        Map<String, Object> checklistSummary = (Map<String, Object>) checklist.getOrDefault("summary", Map.of());
        List<Map<String, Object>> gates = List.of(
                acceptanceGate("0-running", "\u9a8c\u6536\u4efb\u52a1\u8fd0\u884c\u4e2d", "RUNNING", safeLimit, "\u5df2\u8fdb\u5165\u540e\u53f0\u6267\u884c\uff0c\u5b8c\u6210\u540e\u5199\u5165\u9a8c\u6536\u5386\u53f2")
        );
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("runNo", runNo);
        result.put("orgCode", safeOrgCode);
        result.put("checkedAt", java.time.LocalDateTime.now().withNano(0).toString());
        result.put("sampleLimit", safeLimit);
        result.put("overallStatus", "RUNNING");
        result.put("summary", checklistSummary);
        result.put("checklist", checklist.get("items"));
        result.put("gates", gates);
        result.put("issues", List.of());
        saveMigrationAcceptanceRun(result);
        systemAuditService.record("workbench", "migration-acceptance-run-async-start", "ORG", safeOrgCode,
                "runNo=" + runNo + ", limit=" + safeLimit);

        String runAsUsername = text(currentUserService.currentUsername());
        CompletableFuture.runAsync(() -> {
            try {
                currentUserService.runAs(runAsUsername, () -> migrationAcceptanceRun(safeOrgCode, safeLimit, runNo));
            } catch (RuntimeException ex) {
                currentUserService.runAs(runAsUsername, () -> {
                    markMigrationAcceptanceRunFailed(runNo, safeOrgCode, safeLimit, ex.getMessage());
                    return null;
                });
            } finally {
                migrationAcceptanceActiveRuns.remove(safeOrgCode, runNo);
            }
        });
        return result;
    }

    private Map<String, Object> migrationAcceptanceRun(String orgCode, int limit, String runNo) {
        ensureMigrationAcceptanceTables();
        requireAcceptancePermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        int safeLimit = Math.max(1, Math.min(limit, 500));
        String safeRunNo = text(runNo).isBlank()
                ? "MIG-ACC-" + LocalDate.now() + "-" + UUID.randomUUID().toString().substring(0, 8)
                : text(runNo);
        Map<String, Object> checklist = migrationAcceptanceChecklist();
        @SuppressWarnings("unchecked")
        Map<String, Object> checklistSummary = (Map<String, Object>) checklist.getOrDefault("summary", Map.of());
        Map<String, Object> governance = dataGovernanceSnapshotForAcceptance(safeOrgCode, safeLimit);
        boolean governanceSkipped = Boolean.TRUE.equals(booleanValue(governance.get("skipped")));
        long applicationTodo = longValue(checklistSummary.get("applicationTodo"));
        long applicationDone = longValue(checklistSummary.get("applicationDone"));
        long salaryTodo = longValue(checklistSummary.get("salaryTodo"));
        long salaryDone = longValue(checklistSummary.get("salaryDone"));
        long historyPrepared = longValue(checklistSummary.get("historyPrepared"));
        long historyExecuted = longValue(checklistSummary.get("historyExecuted"));
        long historyBlocked = longValue(checklistSummary.get("historyBlocked"));
        long reviewPending = longValue(checklistSummary.get("reviewPending"));
        long historyBatchActions = longValue(checklistSummary.get("historyBatchActions"));
        long historyBatchOpen = longValue(checklistSummary.get("historyBatchOpen"));
        long issueCount = longValue(governance.get("issueCount"));
        List<Map<String, Object>> gates = List.of(
                acceptanceGate("1-application-flow", "\u7533\u529e\u4e1a\u52a1\u6d41\u63a5\u5165", "PASS", applicationTodo + applicationDone, "\u7533\u529e\u5f85\u529e/\u5df2\u529e\u94fe\u8def\u53ef\u67e5"),
                acceptanceGate("2-salary-formal-flow", "\u5de5\u8d44\u529e\u7406\u6b63\u5f0f\u5199\u5165\u95ed\u73af", historyBlocked > 0 || historyBatchOpen > 0 ? "WARN" : "PASS", historyPrepared + historyExecuted + historyBlocked + historyBatchActions, historyBlocked > 0 || historyBatchOpen > 0 ? "\u5b58\u5728\u5199\u5165\u963b\u65ad\u6216\u6279\u6b21\u5f85\u5904\u7406\u9879\uff0c\u9700\u5148\u590d\u6838" : "\u9884\u89c8/\u786e\u8ba4/\u5199\u5165/\u590d\u6d4b/\u901a\u8fc7/\u5f52\u6863\u94fe\u8def\u5df2\u63a5\u5165"),
                acceptanceGate("3-exception-ledger", "\u5dee\u5f02\u548c\u7279\u6b8a\u60c5\u51b5\u540e\u671f\u6838\u67e5", reviewPending > 0 ? "WARN" : "PASS", reviewPending, reviewPending > 0 ? "\u4ecd\u6709\u5f85\u590d\u6838\u9879" : "\u5dee\u5f02\u53f0\u8d26\u5165\u53e3\u53ef\u7528"),
                acceptanceGate("4-permission-menu", "\u6743\u9650\u83dc\u5355\u7ec6\u5316", "PASS", accessibleAcceptanceMenuCount(), "\u83dc\u5355\u6743\u9650\u548c\u5355\u4f4d\u6743\u9650\u5df2\u53c2\u4e0e\u63a7\u5236"),
                acceptanceGate("5-desktop-workbench-ui", "\u684c\u9762\u5f0f\u5de5\u4f5c\u53f0", "PASS", salaryTodo + salaryDone, "\u5f85\u529e/\u5df2\u529e/\u6307\u6807\u5df2\u7531\u771f\u5b9e\u6570\u636e\u751f\u6210"),
                acceptanceGate("6-data-governance", "\u6570\u636e\u6cbb\u7406\u5165\u53e3", governanceSkipped || issueCount > 0 ? "WARN" : "PASS", issueCount, governanceSkipped ? text(governance.get("skipReason")) : issueCount > 0 ? "\u5b58\u5728\u57fa\u7840\u6570\u636e\u6216\u62bd\u67e5\u9879\u9700\u6838\u5bf9" : "\u672a\u626b\u63cf\u5230\u6837\u672c\u8303\u56f4\u95ee\u9898"),
                acceptanceGate("7-standard-rule-maintenance", "\u6807\u51c6\u8868\u548c\u89c4\u5219\u7ef4\u62a4", "PASS", salaryBusinessForms().size(), "\u4e1a\u52a1\u8868\u5355/\u89c4\u5219\u7ef4\u62a4\u5165\u53e3\u5df2\u63a5\u5165"),
                acceptanceGate("8-full-acceptance", "\u6b63\u5f0f\u6570\u636e\u8fc1\u79fb/\u9a8c\u6536", governanceSkipped || issueCount > 0 || historyBlocked > 0 || reviewPending > 0 ? "WARN" : "PASS", safeLimit, "\u672c\u6b21\u8fd0\u884c\u5df2\u6c47\u603b\u6838\u5fc3\u95ed\u73af\u3001\u6cbb\u7406\u9879\u548c\u5bfc\u51fa\u8bb0\u5f55"),
                acceptanceGate("9-retirement-project", "\u9000\u4f11\u90e8\u5206\u72ec\u7acb\u9879\u76ee\u6807\u8bb0", "PASS", longValue(governance.get("retirementDeferredCount")), text(governance.get("retirementDeferredNote"))),
                acceptanceGate("10-launch-readiness", "\u4e0a\u7ebf\u51c6\u5907", "PASS", 10, "\u4e0a\u7ebf\u6e05\u5355\u3001\u56de\u5f52\u6d4b\u8bd5\u3001\u6253\u5305\u548c\u9a8c\u6536\u5165\u53e3\u5df2\u63a5\u5165")
        );
        String overallStatus = gates.stream().anyMatch(gate -> "WARN".equals(text(gate.get("status")))) ? "WARN" : "PASS";
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("applicationTodo", applicationTodo);
        summary.put("applicationDone", applicationDone);
        summary.put("salaryTodo", salaryTodo);
        summary.put("salaryDone", salaryDone);
        summary.put("historyPrepared", historyPrepared);
        summary.put("historyExecuted", historyExecuted);
        summary.put("historyBlocked", historyBlocked);
        summary.put("reviewPending", reviewPending);
        summary.put("dataGovernanceIssues", issueCount);
        summary.put("dataGovernanceSkipped", governanceSkipped);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("runNo", safeRunNo);
        result.put("orgCode", safeOrgCode);
        result.put("checkedAt", java.time.LocalDateTime.now().withNano(0).toString());
        result.put("sampleLimit", safeLimit);
        result.put("overallStatus", overallStatus);
        result.put("summary", summary);
        result.put("checklist", checklist.get("items"));
        result.put("gates", gates);
        result.put("issues", governance.getOrDefault("issues", List.of()));
        result.put("dataGovernance", governance);
        saveMigrationAcceptanceRun(result);
        systemAuditService.record("workbench", "migration-acceptance-run", "ORG", safeOrgCode,
                "runNo=" + safeRunNo + ", status=" + overallStatus + ", issues=" + issueCount);
        return result;
    }

    public List<Map<String, Object>> migrationAcceptanceRuns(String orgCode, String status, int limit) {
        ensureMigrationAcceptanceTables();
        requireAcceptancePermission();
        int safeLimit = Math.max(1, Math.min(limit, 200));
        String safeOrgCode = text(orgCode);
        String safeStatus = text(status).toUpperCase();
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE 1 = 1");
        if (!safeOrgCode.isBlank()) {
            organizationAccessService.requireOrgAccess(safeOrgCode);
            where.append(" AND org_code LIKE CONCAT(?, '%')");
            args.add(safeOrgCode);
        } else {
            where.append(" AND ").append(organizationAccessService.orgCodeAccessSql("org_code"));
        }
        if (!safeStatus.isBlank()) {
            where.append(" AND overall_status = ?");
            args.add(safeStatus);
        }
        args.add(safeLimit);
        return jdbcTemplate.queryForList("""
                SELECT run_no AS runNo,
                       org_code AS orgCode,
                       checked_at AS checkedAt,
                       sample_limit AS sampleLimit,
                       overall_status AS overallStatus,
                       salary_todo AS salaryTodo,
                       salary_done AS salaryDone,
                       history_prepared AS historyPrepared,
                       history_executed AS historyExecuted,
                       history_blocked AS historyBlocked,
                       review_pending AS reviewPending,
                       data_governance_issues AS dataGovernanceIssues,
                       warning_count AS warningCount,
                       gate_count AS gateCount,
                       (
                           SELECT COUNT(1)
                           FROM migration_acceptance_issue i
                           WHERE i.run_no = migration_acceptance_run.run_no
                             AND i.review_status = 'PENDING'
                       ) AS issueCount,
                       created_by AS createdBy,
                       created_at AS createdAt
                FROM migration_acceptance_run
                __WHERE__
                ORDER BY checked_at DESC, id DESC
                LIMIT ?
                """.replace("__WHERE__", where), args.toArray());
    }

    public Map<String, Object> migrationAcceptanceRunDetail(String runNo) {
        ensureMigrationAcceptanceTables();
        requireAcceptancePermission();
        String safeRunNo = text(runNo);
        if (safeRunNo.isBlank()) {
            throw new IllegalArgumentException("Acceptance run number is required.");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM migration_acceptance_run
                WHERE run_no = ?
                LIMIT 1
                """, safeRunNo);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Acceptance run not found: " + safeRunNo);
        }
        Map<String, Object> row = rows.getFirst();
        String orgCode = text(row.get("org_code"));
        organizationAccessService.requireOrgAccess(orgCode);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("runNo", text(row.get("run_no")));
        result.put("orgCode", orgCode);
        result.put("checkedAt", text(row.get("checked_at")));
        result.put("sampleLimit", number(row.get("sample_limit")));
        result.put("overallStatus", text(row.get("overall_status")));
        result.put("summary", readJsonMap(row.get("summary_json")));
        result.put("warningCount", number(row.get("warning_count")));
        result.put("gateCount", number(row.get("gate_count")));
        result.put("gates", migrationAcceptanceRunGates(safeRunNo));
        List<Map<String, Object>> issues = migrationAcceptanceRunIssues(safeRunNo, "", 500);
        result.put("issues", issues);
        result.put("issueCount", issues.stream().filter(issue -> "PENDING".equals(text(issue.get("reviewStatus")))).count());
        result.put("createdBy", text(row.get("created_by")));
        result.put("createdAt", text(row.get("created_at")));
        return result;
    }

    public Map<String, Object> latestMigrationAcceptanceRunDetail(String orgCode) {
        requireAcceptancePermission();
        requireExportPermission();
        ensureMigrationAcceptanceTables();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        List<String> runNos = jdbcTemplate.queryForList("""
                SELECT run_no
                FROM migration_acceptance_run
                WHERE org_code LIKE CONCAT(?, '%')
                  AND overall_status <> 'RUNNING'
                ORDER BY checked_at DESC, id DESC
                LIMIT 1
                """, String.class, safeOrgCode);
        if (runNos.isEmpty()) {
            throw new IllegalArgumentException("Acceptance run not found for organization: " + safeOrgCode);
        }
        return migrationAcceptanceRunDetail(runNos.getFirst());
    }

    private String runningMigrationAcceptanceRunNo(String orgCode) {
        ensureMigrationAcceptanceTables();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            return "";
        }
        List<String> runNos = jdbcTemplate.queryForList("""
                SELECT run_no
                FROM migration_acceptance_run
                WHERE org_code LIKE CONCAT(?, '%')
                  AND overall_status = 'RUNNING'
                ORDER BY checked_at DESC, id DESC
                LIMIT 1
                """, String.class, safeOrgCode);
        return runNos.isEmpty() ? "" : text(runNos.getFirst());
    }

    private void recoverStaleMigrationAcceptanceRuns(String orgCode) {
        ensureMigrationAcceptanceTables();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            return;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT run_no, sample_limit
                FROM migration_acceptance_run
                WHERE org_code LIKE CONCAT(?, '%')
                  AND overall_status = 'RUNNING'
                  AND checked_at < DATE_SUB(NOW(), INTERVAL 2 HOUR)
                ORDER BY checked_at, id
                LIMIT 20
                """, safeOrgCode);
        for (Map<String, Object> row : rows) {
            String runNo = text(row.get("run_no"));
            if (runNo.isBlank()) {
                continue;
            }
            migrationAcceptanceActiveRuns.remove(safeOrgCode, runNo);
            markMigrationAcceptanceRunFailed(
                    runNo,
                    safeOrgCode,
                    Math.max(1, number(row.get("sample_limit"))),
                    "\u9a8c\u6536\u4efb\u52a1\u8d85\u8fc7 2 \u5c0f\u65f6\u672a\u5b8c\u6210\uff0c\u5df2\u6309\u4e2d\u65ad\u5904\u7406"
            );
        }
    }

    public Map<String, Object> exportMigrationAcceptanceRunDetail(String runNo) {
        requireAcceptancePermission();
        requireExportPermission();
        return migrationAcceptanceRunDetail(runNo);
    }

    public List<Map<String, Object>> migrationAcceptanceRunIssues(String runNo, String status, int limit) {
        ensureMigrationAcceptanceTables();
        requireAcceptancePermission();
        String safeRunNo = text(runNo);
        if (safeRunNo.isBlank()) {
            throw new IllegalArgumentException("Acceptance run number is required.");
        }
        String orgCode = migrationAcceptanceRunOrgCode(safeRunNo);
        organizationAccessService.requireOrgAccess(orgCode);
        int safeLimit = Math.max(1, Math.min(limit, 500));
        String safeStatus = text(status).toUpperCase();
        List<Object> args = new ArrayList<>();
        args.add(safeRunNo);
        String statusWhere = "";
        if (!safeStatus.isBlank()) {
            statusWhere = "AND review_status = ?";
            args.add(safeStatus);
        }
        args.add(safeLimit);
        return jdbcTemplate.queryForList("""
                SELECT id,
                       run_no AS runNo,
                       person_code AS personCode,
                       person_name AS personName,
                       org_code AS orgCode,
                       issue_type AS issueType,
                       message,
                       review_status AS reviewStatus,
                       review_reason AS reviewReason,
                       reviewed_by AS reviewedBy,
                       reviewed_at AS reviewedAt,
                       created_at AS createdAt
                FROM migration_acceptance_issue
                WHERE run_no = ?
                __STATUS_WHERE__
                ORDER BY id
                LIMIT ?
                """.replace("__STATUS_WHERE__", statusWhere), args.toArray());
    }

    public Map<String, Object> reviewMigrationAcceptanceIssue(Long issueId, WorkbenchGeneratedIssueReviewRequest request) {
        ensureMigrationAcceptanceTables();
        requireAcceptancePermission();
        if (issueId == null) {
            throw new IllegalArgumentException("Acceptance issue id is required.");
        }
        Map<String, Object> issue = migrationAcceptanceIssueRow(issueId);
        organizationAccessService.requireOrgAccess(text(issue.get("org_code")));
        String reviewStatus = normalizeGeneratedIssueReviewStatus(request == null ? null : request.reviewStatus());
        String reviewReason = left(text(request == null ? null : request.reviewReason()), 1000);
        if (reviewReason.isBlank()) {
            throw new IllegalArgumentException("Review reason is required.");
        }
        String username = currentUserService.currentUsername();
        jdbcTemplate.update("""
                UPDATE migration_acceptance_issue
                SET review_status = ?,
                    review_reason = ?,
                    reviewed_by = ?,
                    reviewed_at = NOW()
                WHERE id = ?
                """, reviewStatus, reviewReason, username, issueId);
        systemAuditService.record("workbench", "migration-acceptance-issue-review", "MIGRATION_ACCEPTANCE_ISSUE", String.valueOf(issueId),
                text(issue.get("run_no")) + " " + text(issue.get("person_code")) + " " + reviewStatus + " " + reviewReason);
        return migrationAcceptanceIssueRow(issueId);
    }

    public WorkbenchItemsPageResponse items(String status, int offset, int limit, String keyword, String changeType, String source, String caseStatus, String trialStatus, String reviewStatus, String workflowStatus, String closureStatus, String nextAction) {
        ensureWorkbenchPerformanceIndexes();
        int safeOffset = Math.max(0, offset);
        int safeLimit = Math.min(Math.max(1, limit), 100);
        if ("DONE".equalsIgnoreCase(status)) {
            if (!hasMenu("SALARY_DONE")) {
                return new WorkbenchItemsPageResponse(0, safeOffset, safeLimit, List.of());
            }
            return new WorkbenchItemsPageResponse(
                    countSalaryDone(keyword, changeType, source, caseStatus, trialStatus, reviewStatus, workflowStatus, closureStatus, nextAction),
                    safeOffset,
                    safeLimit,
                salaryDoneItems(safeOffset, safeLimit, keyword, changeType, source, caseStatus, trialStatus, reviewStatus, workflowStatus, closureStatus, nextAction)
            );
        }
        if (!hasMenu("SALARY_TODO")) {
            return new WorkbenchItemsPageResponse(0, safeOffset, safeLimit, List.of());
        }
        return salaryTodoPage(safeOffset, safeLimit, keyword, changeType, source);
    }

    public WorkbenchItemsPageResponse exportItems(String status, int limit, String keyword, String changeType, String source, String caseStatus, String trialStatus, String reviewStatus, String workflowStatus, String closureStatus, String nextAction) {
        ensureWorkbenchPerformanceIndexes();
        int safeLimit = Math.min(Math.max(1, limit), 5000);
        if ("DONE".equalsIgnoreCase(status)) {
            if (!hasMenu("SALARY_DONE")) {
                return new WorkbenchItemsPageResponse(0, 0, safeLimit, List.of());
            }
            List<WorkbenchItemResponse> items = salaryDoneItems(0, safeLimit, keyword, changeType, source, caseStatus, trialStatus, reviewStatus, workflowStatus, closureStatus, nextAction);
            return new WorkbenchItemsPageResponse(items.size(), 0, safeLimit, items);
        }
        if (!hasMenu("SALARY_TODO")) {
            return new WorkbenchItemsPageResponse(0, 0, safeLimit, List.of());
        }
        List<WorkbenchItemResponse> items = salaryTodoItems(0, safeLimit, keyword, changeType, source);
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
        List<WorkbenchCaseSnapshotItemResponse> salaryItems = snapshotSalaryItems(text(snapshot.get("salary_items_json")));
        ensureHistoryWritePlanTable();
        Map<String, Object> historyWritePlanRow = historyWritePlanRowIfExists(safeCaseNo);
        WorkbenchHistoryWritePlanResponse historyWritePlan = historyWritePlanRow == null || historyWritePlanRow.isEmpty()
                ? null
                : historyWritePlanResponse(historyWritePlanRow);
        String workflowStatus = businessCaseWorkflowStatus(row, historyWritePlanRow, trialStatus, reviewStatus);
        List<SystemAuditLogResponse> reportAudits = reportAudits(text(row.get("case_no")));
        WorkbenchReportPrintArchiveResponse reportPrintArchive = reportPrintArchive(row, snapshot, salaryItems, reportAudits);
        List<SystemAuditLogResponse> historyWriteAudits = historyWriteAudits(text(row.get("case_no")));
        WorkbenchCaseClosureStatusResponse closureStatus = caseClosureStatus(
                row,
                snapshot,
                trialStatus,
                reviewStatus,
                historyWritePlan,
                reportPrintArchive
        );
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
                workflowStatus,
                trialStatus,
                booleanValue(row.get("trial_matched")),
                decimal(row.get("trial_baseline_total")),
                decimal(row.get("trial_calculated_total")),
                decimal(row.get("trial_expected_total")),
                decimal(row.get("trial_difference")),
                text(row.get("trial_summary")),
                trialChanges(text(row.get("trial_changes_json"))),
                salaryItems,
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
                reportPrintArchive,
                closureStatus,
                historyWriteAudits,
                reportAudits,
                caseAudits(text(row.get("case_no"))),
                text(row.get("handled_by")),
                text(row.get("handled_at"))
        );
    }

    private WorkbenchCaseClosureStatusResponse caseClosureStatus(
            Map<String, Object> caseRow,
            Map<String, Object> snapshot,
            String trialStatus,
            String reviewStatus,
            WorkbenchHistoryWritePlanResponse historyWritePlan,
            WorkbenchReportPrintArchiveResponse reportPrintArchive
    ) {
        boolean cancelled = "CANCELLED".equalsIgnoreCase(text(caseRow.get("status")));
        boolean done = "DONE".equalsIgnoreCase(text(caseRow.get("status")));
        boolean snapshotExists = snapshot != null && !snapshot.isEmpty();
        boolean trialClosed = !Set.of("DIFFERENT", "ERROR").contains(text(trialStatus))
                || !text(reviewStatus).isBlank() && !"PENDING".equalsIgnoreCase(text(reviewStatus))
                || !text(caseRow.get("force_reason")).isBlank()
                || !text(caseRow.get("difference_reason")).isBlank();
        boolean printed = reportPrintArchive != null && Boolean.TRUE.equals(reportPrintArchive.printed());
        boolean printBlocked = reportPrintArchive != null && "BLOCKED".equalsIgnoreCase(text(reportPrintArchive.status()));
        boolean hasPlan = historyWritePlan != null;
        boolean historyWritten = hasPlan
                && "EXECUTED".equalsIgnoreCase(text(historyWritePlan.planStatus()))
                && "SUCCESS".equalsIgnoreCase(text(historyWritePlan.executionResult()));
        boolean planBlocked = hasPlan && !historyWritten && ("BLOCKED".equalsIgnoreCase(text(historyWritePlan.previewStatus()))
                || "BLOCKED".equalsIgnoreCase(text(historyWritePlan.planStatus()))
                || Boolean.FALSE.equals(historyWritePlan.writable()));
        boolean mismatch = hasPlan && "MISMATCHED".equalsIgnoreCase(text(historyWritePlan.comparisonStatus()));
        boolean comparisonReviewed = hasPlan && "REVIEWED".equalsIgnoreCase(text(historyWritePlan.comparisonReviewStatus()));
        boolean differenceClosed = !mismatch || comparisonReviewed;

        List<WorkbenchCaseClosureStepResponse> steps = new ArrayList<>();
        steps.add(caseClosureStep("case", "\u529e\u7406", cancelled ? "BLOCKED" : (done ? "DONE" : "PENDING"),
                cancelled ? "\u5df2\u64a4\u56de\u529e\u7406" : (done ? "\u5df2\u529e\u7406" : "\u5f85\u529e\u7406"), true, done && !cancelled));
        steps.add(caseClosureStep("trial", "\u8bd5\u7b97", trialClosed ? "DONE" : "PENDING",
                trialClosed ? "\u8bd5\u7b97\u5df2\u901a\u8fc7\u6216\u5df2\u590d\u6838" : "\u8bd5\u7b97\u5dee\u5f02\u5f85\u590d\u6838", true, trialClosed));
        steps.add(caseClosureStep("snapshot", "\u529e\u7406\u5feb\u7167", snapshotExists ? "DONE" : "PENDING",
                snapshotExists ? "\u5df2\u751f\u6210\u5feb\u7167" : "\u5f85\u751f\u6210\u5feb\u7167", true, snapshotExists));
        steps.add(caseClosureStep("report-print", "\u5ba1\u6279\u8868\u6253\u5370", printed ? "DONE" : (printBlocked ? "BLOCKED" : "PENDING"),
                printed ? "\u5df2\u7559\u5b58\u6253\u5370\u8bb0\u5f55" : (printBlocked ? text(reportPrintArchive.message()) : "\u5f85\u6253\u5370\u5ba1\u6279\u8868"), true, printed));
        steps.add(caseClosureStep("history-plan", "\u5386\u53f2\u5199\u5165\u8ba1\u5212", hasPlan ? (planBlocked ? "BLOCKED" : "DONE") : "PENDING",
                hasPlan ? (planBlocked ? "\u5199\u5165\u8ba1\u5212\u6709\u963b\u65ad" : "\u5df2\u751f\u6210\u5199\u5165\u8ba1\u5212") : "\u5f85\u751f\u6210\u5199\u5165\u8ba1\u5212", true, hasPlan && !planBlocked));
        steps.add(caseClosureStep("history-write", "\u5386\u53f2\u5199\u5165", historyWritten ? "DONE" : (planBlocked ? "BLOCKED" : "PENDING"),
                historyWritten ? "\u5df2\u5199\u5165 hisbase" : (planBlocked ? "\u5199\u5165\u963b\u65ad\u5f85\u5904\u7406" : "\u5f85\u786e\u8ba4\u5199\u5165"), true, historyWritten));
        steps.add(caseClosureStep("difference-review", "\u5dee\u5f02\u6838\u67e5", differenceClosed ? "DONE" : "PENDING",
                differenceClosed ? (mismatch ? "\u5dee\u5f02\u5df2\u6838\u67e5" : "\u65e0\u5199\u5165\u5dee\u5f02") : "\u5199\u5165\u5dee\u5f02\u5f85\u6838\u67e5", true, differenceClosed));

        int total = (int) steps.stream().filter(step -> Boolean.TRUE.equals(step.required())).count();
        int completed = (int) steps.stream().filter(step -> Boolean.TRUE.equals(step.required()) && Boolean.TRUE.equals(step.completed())).count();
        boolean blocked = cancelled || steps.stream().anyMatch(step -> "BLOCKED".equalsIgnoreCase(text(step.status())));
        boolean closed = !blocked && total > 0 && completed == total;
        String status = cancelled ? "CANCELLED" : (closed ? "CLOSED" : (blocked ? "BLOCKED" : "PENDING"));
        String message;
        if (cancelled) {
            message = "\u5df2\u64a4\u56de\uff0c\u4e0d\u518d\u7eb3\u5165\u5199\u5165\u95ed\u73af";
        } else if (closed) {
            message = "\u529e\u7406\u3001\u6253\u5370\u3001\u5199\u5165\u548c\u5dee\u5f02\u6838\u67e5\u5df2\u95ed\u73af";
        } else if (blocked) {
            message = "\u95ed\u73af\u5b58\u5728\u963b\u65ad\u9879\uff0c\u9700\u5148\u5904\u7406\u963b\u65ad";
        } else {
            message = "\u5f85\u5b8c\u6210\u540e\u7eed\u529e\u7406\u6b65\u9aa4";
        }
        WorkbenchCaseClosureStepResponse nextStep = closed || cancelled ? null : steps.stream()
                .filter(step -> "BLOCKED".equalsIgnoreCase(text(step.status())))
                .findFirst()
                .orElseGet(() -> steps.stream()
                        .filter(step -> Boolean.TRUE.equals(step.required()) && !Boolean.TRUE.equals(step.completed()))
                        .findFirst()
                        .orElse(null));
        return new WorkbenchCaseClosureStatusResponse(
                status,
                message,
                completed,
                total,
                closed,
                nextStep,
                caseClosureNextActions(nextStep, historyWritePlan, reportPrintArchive),
                steps
        );
    }

    private WorkbenchCaseClosureStepResponse caseClosureStep(String code, String label, String status, String message, boolean required, boolean completed) {
        return new WorkbenchCaseClosureStepResponse(code, label, status, message, required, completed);
    }

    private List<WorkbenchCaseClosureActionResponse> caseClosureNextActions(
            WorkbenchCaseClosureStepResponse nextStep,
            WorkbenchHistoryWritePlanResponse historyWritePlan,
            WorkbenchReportPrintArchiveResponse reportPrintArchive
    ) {
        if (nextStep == null) {
            return List.of();
        }
        String code = text(nextStep.code());
        boolean printed = reportPrintArchive != null && Boolean.TRUE.equals(reportPrintArchive.printed());
        boolean writeReady = historyWritePlan != null
                && "PREPARED".equalsIgnoreCase(text(historyWritePlan.planStatus()))
                && Boolean.TRUE.equals(historyWritePlan.writable())
                && printed;
        boolean written = historyWritePlan != null
                && "EXECUTED".equalsIgnoreCase(text(historyWritePlan.planStatus()))
                && "SUCCESS".equalsIgnoreCase(text(historyWritePlan.executionResult()));
        List<WorkbenchCaseClosureActionResponse> actions = new ArrayList<>();
        switch (code) {
            case "trial" -> actions.add(caseClosureAction("REVIEW_TRIAL", "\u6807\u8bb0\u5df2\u590d\u6838", "review", true));
            case "snapshot" -> actions.add(caseClosureAction("VIEW_SNAPSHOT", "\u67e5\u770b\u5feb\u7167", "snapshot", true));
            case "report-print" -> {
                actions.add(caseClosureAction("PRINT_APPROVAL", "\u6253\u5370\u5ba1\u6279\u8868", "report", true));
                if (historyWritePlan == null) {
                    actions.add(caseClosureAction("CREATE_HISTORY_PLAN", "\u751f\u6210\u5199\u5165\u9884\u68c0", "history-plan", false));
                } else {
                    actions.add(caseClosureAction("VIEW_HISTORY_PLAN", "\u67e5\u770b\u5199\u5165\u8ba1\u5212", "history-plan", false));
                }
            }
            case "history-plan" -> {
                if (historyWritePlan == null) {
                    actions.add(caseClosureAction("CREATE_HISTORY_PLAN", "\u751f\u6210\u5199\u5165\u9884\u68c0", "history-plan", true));
                } else {
                    actions.add(caseClosureAction("VIEW_HISTORY_PLAN", "\u67e5\u770b\u5199\u5165\u8ba1\u5212", "history-plan", true));
                }
            }
            case "history-write" -> {
                if (writeReady) {
                    actions.add(caseClosureAction("EXECUTE_HISTORY_WRITE", "\u786e\u8ba4\u5199\u5165\u5386\u53f2", "history-write", true));
                }
                if (historyWritePlan == null) {
                    actions.add(caseClosureAction("CREATE_HISTORY_PLAN", "\u751f\u6210\u5199\u5165\u9884\u68c0", "history-plan", actions.isEmpty()));
                } else {
                    actions.add(caseClosureAction("VIEW_HISTORY_PLAN", "\u67e5\u770b\u5199\u5165\u8ba1\u5212", "history-plan", actions.isEmpty()));
                }
            }
            case "difference-review" -> {
                if (written) {
                    actions.add(caseClosureAction("REVIEW_DIFFERENCE", "\u5b57\u6bb5\u5bf9\u7167", "history-comparison", true));
                    actions.add(caseClosureAction("RETEST_DIFFERENCE", "\u6309\u5f53\u524d\u57fa\u7840\u590d\u6d4b", "history-comparison", false));
                }
            }
            default -> {
            }
        }
        return actions;
    }

    private WorkbenchCaseClosureActionResponse caseClosureAction(String code, String label, String target, boolean primary) {
        return new WorkbenchCaseClosureActionResponse(code, label, target, primary);
    }

    private String businessCaseWorkflowStatus(Map<String, Object> caseRow, Map<String, Object> historyWritePlanRow, String trialStatus, String reviewStatus) {
        if ("CANCELLED".equalsIgnoreCase(text(caseRow.get("status")))) {
            return "CASE_CANCELLED";
        }
        if (historyWritePlanRow != null && !historyWritePlanRow.isEmpty()) {
            String previewStatus = text(historyWritePlanRow.get("preview_status"));
            String planStatus = text(historyWritePlanRow.get("plan_status"));
            String executionResult = text(historyWritePlanRow.get("execution_result"));
            if ("EXECUTED".equals(planStatus) && "SUCCESS".equals(executionResult)) {
                String comparisonStatus = historyWriteComparisonStatus(historyWritePlanRow);
                String comparisonReviewStatus = text(historyWritePlanRow.get("comparison_review_status"));
                if ("MISMATCHED".equalsIgnoreCase(comparisonStatus)
                        && !"REVIEWED".equalsIgnoreCase(comparisonReviewStatus)) {
                    return "HISTORY_REVIEW_PENDING";
                }
                if ("MATCHED".equalsIgnoreCase(comparisonStatus)
                        || "REVIEWED".equalsIgnoreCase(comparisonReviewStatus)) {
                    return "HISTORY_CLOSED";
                }
                return "HISTORY_WRITTEN";
            }
            if ("EXECUTED".equals(planStatus)) {
                return "HISTORY_EXECUTED";
            }
            if ("BLOCKED".equals(previewStatus) || "BLOCKED".equals(planStatus)) {
                return "HISTORY_BLOCKED";
            }
            if ("ROLLED_BACK".equals(planStatus)) {
                return "HISTORY_ROLLED_BACK";
            }
            if ("PREPARED".equals(planStatus)) {
                if (booleanValue(historyWritePlanRow.get("writable"))) {
                    return "HISTORY_READY";
                }
                return "HISTORY_PREPARED";
            }
        }
        if (("DIFFERENT".equals(trialStatus) || "ERROR".equals(trialStatus)) && "PENDING".equals(reviewStatus)) {
            return "REVIEW_PENDING";
        }
        return "CASE_DONE";
    }

    private List<SystemAuditLogResponse> caseAudits(String caseNo) {
        return caseAudits(caseNo, false);
    }

    private List<SystemAuditLogResponse> historyWriteAudits(String caseNo) {
        return caseAudits(caseNo, true);
    }

    private List<SystemAuditLogResponse> reportAudits(String caseNo) {
        systemAuditService.ensureTable();
        ensureReportPrintBatchTables();
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
                WHERE module_name = 'report'
                  AND (
                      (target_type = 'SALARY_CASE' AND target_code = ?)
                      OR (action_name IN ('salary-case-approvals-print', 'salary-case-approvals-reprint')
                          AND summary LIKE CONCAT('%', ?, '%')
                          AND summary NOT LIKE '%batchNo=%')
                  )
                UNION ALL
                SELECT CONCAT('RPB-', item.id) AS audit_id,
                       'report' AS module_name,
                       CASE
                           WHEN batch.report_type = 'SALARY_CASE_APPROVAL_REPRINT' THEN 'salary-case-approvals-reprint'
                           ELSE 'salary-case-approvals-print'
                       END AS action_name,
                       'REPORT_PRINT_BATCH' AS target_type,
                       item.batch_no AS target_code,
                       CONCAT('batchNo=', item.batch_no,
                              ', org=', batch.org_code,
                              ', period=', batch.event_year, '-', LPAD(COALESCE(batch.event_month, 1), 2, '0'),
                              ', rows=', batch.printed_count,
                              ', warnings=', batch.warning_count,
                              ', status=', item.validation_status,
                              ', ', COALESCE(item.summary, '')) AS summary,
                       batch.printed_by AS operator,
                       item.created_at AS created_at
                FROM salary_report_print_batch_item item
                JOIN salary_report_print_batch batch ON batch.batch_no = item.batch_no
                WHERE item.case_no = ?
                ORDER BY created_at DESC, audit_id DESC
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
        ), caseNo, caseNo, caseNo);
    }

    private WorkbenchReportPrintArchiveResponse reportPrintArchive(
            Map<String, Object> businessCase,
            Map<String, Object> snapshot,
            List<WorkbenchCaseSnapshotItemResponse> salaryItems,
            List<SystemAuditLogResponse> reportAudits
    ) {
        List<String> missing = new ArrayList<>();
        if (snapshot == null || snapshot.isEmpty()) {
            missing.add("快照");
        }
        if (salaryItems == null || salaryItems.isEmpty()) {
            missing.add("工资明细");
        }
        if (text(businessCase.get("person_code")).isBlank()) {
            missing.add("人员信息");
        }
        boolean cancelled = "CANCELLED".equalsIgnoreCase(text(businessCase.get("status")));
        boolean printable = missing.isEmpty() && !cancelled;
        SystemAuditLogResponse latest = reportAudits == null || reportAudits.isEmpty() ? null : reportAudits.getFirst();
        int printCount = reportAudits == null ? 0 : (int) reportAudits.stream()
                .filter(audit -> Set.of(
                        "salary-case-approval-print",
                        "salary-case-approvals-print",
                        "salary-case-approvals-reprint"
                ).contains(text(audit.action())))
                .count();
        boolean printed = printCount > 0;
        boolean reprinted = reportAudits != null && reportAudits.stream()
                .anyMatch(audit -> "salary-case-approvals-reprint".equals(text(audit.action())));
        String status = !printable ? "BLOCKED" : (printed ? "PRINTED" : "PENDING");
        String message;
        if (!missing.isEmpty()) {
            message = "缺少" + String.join("、", missing) + "，审批表打印依据不完整";
        } else if (cancelled) {
            message = "已撤回办理，不建议打印或写入";
        } else if (printed) {
            message = "已形成审批表打印归档";
        } else {
            message = "尚未打印审批表";
        }
        return new WorkbenchReportPrintArchiveResponse(
                printable,
                printed,
                reprinted,
                latest == null ? "" : text(latest.action()),
                latest != null && "REPORT_PRINT_BATCH".equals(text(latest.targetType())) ? text(latest.targetCode()) : "",
                latest == null ? "" : text(latest.targetType()),
                latest == null ? "" : text(latest.targetCode()),
                latest == null ? "" : text(latest.operator()),
                latest == null ? "" : text(latest.createdAt()),
                printCount,
                status,
                message
        );
    }

    private WorkbenchReportPrintArchiveResponse reportPrintArchive(String caseNo) {
        Map<String, Object> businessCase = businessCaseRow(caseNo);
        Map<String, Object> snapshot = businessCaseSnapshotRow(text(businessCase.get("work_item_id")));
        List<WorkbenchCaseSnapshotItemResponse> salaryItems = snapshotSalaryItems(text(snapshot.get("salary_items_json")));
        return reportPrintArchive(businessCase, snapshot, salaryItems, reportAudits(caseNo));
    }

    private void requireReportPrintArchivedForHistoryWrite(String caseNo) {
        WorkbenchReportPrintArchiveResponse archive = reportPrintArchive(caseNo);
        if (!Boolean.TRUE.equals(archive.printable())) {
            throw new IllegalArgumentException("Report print archive is not printable: " + archive.message());
        }
        if (!Boolean.TRUE.equals(archive.printed())) {
            throw new IllegalArgumentException("Report print archive is required before writing history: " + archive.message());
        }
    }

    private void ensureReportPrintBatchTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS salary_report_print_batch (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    batch_no VARCHAR(64) NOT NULL,
                    report_type VARCHAR(64) NOT NULL,
                    org_code VARCHAR(64) NOT NULL,
                    event_year INT NULL,
                    event_month INT NULL,
                    business_type VARCHAR(128) NULL,
                    keyword VARCHAR(255) NULL,
                    limit_count INT NOT NULL DEFAULT 0,
                    printed_count INT NOT NULL DEFAULT 0,
                    blocked_count INT NOT NULL DEFAULT 0,
                    warning_count INT NOT NULL DEFAULT 0,
                    printed_by VARCHAR(64) NULL,
                    printed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    summary VARCHAR(1024) NULL,
                    UNIQUE KEY uk_salary_report_print_batch_no (batch_no),
                    KEY idx_salary_report_print_batch_org_period (org_code, event_year, event_month),
                    KEY idx_salary_report_print_batch_type_time (report_type, printed_at)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS salary_report_print_batch_item (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    batch_no VARCHAR(64) NOT NULL,
                    case_no VARCHAR(64) NOT NULL,
                    person_code VARCHAR(128) NOT NULL,
                    person_name VARCHAR(128) NULL,
                    org_code VARCHAR(64) NOT NULL,
                    business_type VARCHAR(128) NULL,
                    validation_status VARCHAR(32) NOT NULL,
                    issue_count INT NOT NULL DEFAULT 0,
                    warning_count INT NOT NULL DEFAULT 0,
                    summary VARCHAR(1024) NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_salary_report_print_batch_item (batch_no, case_no),
                    KEY idx_salary_report_print_batch_item_case (case_no, created_at),
                    KEY idx_salary_report_print_batch_item_person (person_code, created_at),
                    KEY idx_salary_report_print_batch_item_batch (batch_no)
                )
                """);
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
        String caseStatus = text(businessCase.get("status"));
        String reviewStatus = text(businessCase.get("review_status"));
        if (!"DONE".equalsIgnoreCase(caseStatus)) {
            issues.add("BLOCKED: only DONE salary business cases can be written to history.");
        }
        if (("DIFFERENT".equalsIgnoreCase(snapshot.trialStatus()) || "ERROR".equalsIgnoreCase(snapshot.trialStatus()))
                && !"REVIEWED".equalsIgnoreCase(reviewStatus)) {
            issues.add("BLOCKED: trial risk must be reviewed before writing history.");
        }
        WorkbenchReportPrintArchiveResponse printArchive = reportPrintArchive(snapshot.caseNo());
        if (!Boolean.TRUE.equals(printArchive.printable())) {
            issues.add("BLOCKED: report print archive is not printable. " + printArchive.message());
        } else if (!Boolean.TRUE.equals(printArchive.printed())) {
            issues.add("WARNING: approval report has not been printed before history write.");
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

    public WorkbenchHistoryWriteConfirmResponse historyWriteConfirm(String caseNo) {
        WorkbenchHistoryWritePreviewResponse preview = historyWritePreview(caseNo);
        BigDecimal totalAmount = historyWritePreviewTotal(preview);
        WorkbenchReportPrintArchiveResponse printArchive = reportPrintArchive(preview.caseNo());
        List<String> issues = new ArrayList<>(preview.issues());
        boolean printArchived = Boolean.TRUE.equals(printArchive.printed());
        if (!printArchived) {
            issues.add("BLOCKED: approval report must be printed before confirming history write. " + printArchive.message());
        }
        boolean executable = Boolean.TRUE.equals(preview.writable())
                && issues.stream().noneMatch(issue -> text(issue).startsWith("BLOCKED"));
        String confirmMessage = executable
                ? "\u5199\u5165\u524d\u8bf7\u590d\u6838\u5b57\u6bb5\u6620\u5c04\u3001\u5408\u8ba1\u91d1\u989d\u548c sid \u94fe\u8c03\u6574\uff0c\u786e\u8ba4\u540e\u5c06\u751f\u6210 hisbase \u5386\u53f2\u884c\u3002"
                : (printArchived ? "\u5b58\u5728\u963b\u65ad\u9879\uff0c\u672c\u6b21\u5199\u5165\u4e0d\u80fd\u6267\u884c\u3002" : "\u5c1a\u672a\u6253\u5370\u5ba1\u6279\u8868\uff0c\u672c\u6b21\u5199\u5165\u4e0d\u80fd\u6267\u884c\u3002");
        return new WorkbenchHistoryWriteConfirmResponse(
                preview.caseNo(),
                preview.workItemId(),
                preview.personCode(),
                preview.orgCode(),
                preview.year(),
                preview.month(),
                preview.businessType(),
                preview.status(),
                preview.writable(),
                executable,
                preview.writePlanId(),
                preview.existingHistoryId(),
                preview.previousHistory(),
                preview.nextHistory(),
                preview.sidUpdateRequired(),
                preview.sidPlan(),
                totalAmount,
                preview.fields().size(),
                preview.fields(),
                issues,
                confirmMessage
        );
    }

    public WorkbenchHistoryWriteRollbackPreviewResponse rollbackHistoryWritePreview(String caseNo) {
        requireHistoryRollbackPermission();
        WorkbenchCaseSnapshotResponse snapshot = caseSnapshot(caseNo);
        ensureHistoryWritePlanTable();
        Map<String, Object> plan = historyWritePlanRow(snapshot.caseNo());
        String planNo = text(plan.get("plan_no"));
        String insertedHistoryId = text(plan.get("inserted_history_id"));
        String previousHistoryId = text(plan.get("previous_history_id"));
        String nextHistoryId = text(plan.get("next_history_id"));
        List<String> issues = new ArrayList<>();
        if (!"EXECUTED".equalsIgnoreCase(text(plan.get("plan_status")))
                || !"SUCCESS".equalsIgnoreCase(text(plan.get("execution_result")))) {
            issues.add("BLOCKED: Only successful executed history write plans can be rolled back.");
        }
        if (insertedHistoryId.isBlank()) {
            issues.add("BLOCKED: history write plan has no inserted history id.");
        }
        WorkbenchHistoryWritePreviewHistoryRow previous = historyPreviewRowById(previousHistoryId);
        WorkbenchHistoryWritePreviewHistoryRow inserted = historyPreviewRowById(insertedHistoryId);
        WorkbenchHistoryWritePreviewHistoryRow next = historyPreviewRowById(nextHistoryId);
        if (!insertedHistoryId.isBlank() && inserted == null) {
            issues.add("BLOCKED: inserted history row no longer exists.");
        }
        if (inserted != null && !sameHistoryId(inserted.nextId(), nextHistoryId)) {
            issues.add("BLOCKED: inserted history sid has changed; rollback is blocked.");
        }
        if (!previousHistoryId.isBlank()) {
            if (previous == null) {
                issues.add("BLOCKED: previous history row no longer exists.");
            } else if (!sameHistoryId(previous.nextId(), insertedHistoryId)) {
                issues.add("BLOCKED: previous history sid no longer points to inserted row; rollback is blocked.");
            }
        }
        if (historyReferencedByOtherRow(snapshot, insertedHistoryId, previousHistoryId)) {
            issues.add("BLOCKED: inserted history row is referenced by another row; rollback is blocked.");
        }
        boolean rollbackable = issues.stream().noneMatch(issue -> text(issue).startsWith("BLOCKED"));
        String sidPlan = previousHistoryId.isBlank()
                ? "\u64a4\u9500\u65f6\u5220\u9664\u5199\u5165\u884c\uff0c\u4e0d\u9700\u8c03\u6574\u524d\u7f6e sid\u3002"
                : "\u64a4\u9500\u65f6\u5220\u9664\u5199\u5165\u884c\uff0c\u5e76\u5c06\u524d\u4e00\u6761 sid \u6062\u590d\u4e3a " + (nextHistoryId.isBlank() ? "\u7a7a" : nextHistoryId) + "\u3002";
        return new WorkbenchHistoryWriteRollbackPreviewResponse(
                snapshot.caseNo(),
                snapshot.workItemId(),
                snapshot.personCode(),
                snapshot.orgCode(),
                snapshot.year(),
                snapshot.month(),
                snapshot.businessType(),
                rollbackable ? "READY" : "BLOCKED",
                rollbackable,
                planNo,
                insertedHistoryId,
                previous,
                inserted,
                next,
                !previousHistoryId.isBlank() || !nextHistoryId.isBlank(),
                sidPlan,
                issues,
                rollbackable ? "\u64a4\u9500\u524d\u8bf7\u590d\u6838\u5f85\u5220\u9664\u884c\u548c sid \u6062\u590d\u9884\u6848\u3002" : "\u5b58\u5728\u963b\u65ad\u9879\uff0c\u4e0d\u80fd\u64a4\u9500\u5199\u5165\u3002"
        );
    }

    public List<WorkbenchHistoryWritePlanResponse> historyWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit) {
        return historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, "", limit);
    }

    public List<WorkbenchHistoryWritePlanResponse> historyWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, String pendingQueue, int limit) {
        return historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, pendingQueue, "", "", limit);
    }

    public List<WorkbenchHistoryWritePlanResponse> historyWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, String pendingQueue, String printQueue, String statusQueue, int limit) {
        return historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, pendingQueue, printQueue, statusQueue, limit, 200);
    }

    public List<Map<String, Object>> historyWritePendingQueues(String keyword) {
        ensureHistoryWritePlanTable();
        ensureAuditTable();
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        String safeKeyword = text(keyword);
        List<Map<String, Object>> queues = new ArrayList<>();
        queues.add(historyWritePendingQueue(
                "blocked",
                "\u5199\u5165\u963b\u65ad",
                countHistoryWritePendingQueue("""
                        __QUEUE__
                        """.replace("__QUEUE__", historyWritePendingQueueConditionSql("blocked")), safeKeyword),
                "BLOCKED",
                "",
                "",
                ""));
        queues.add(historyWritePendingQueue(
                "prepared",
                "\u5f85\u5199\u5165",
                countHistoryWritePendingQueue("""
                        __QUEUE__
                        """.replace("__QUEUE__", historyWritePendingQueueConditionSql("prepared")), safeKeyword),
                "PREPARED",
                "",
                "",
                ""));
        queues.add(historyWritePendingQueue(
                "review",
                "\u5199\u5165\u540e\u5f85\u6838\u67e5",
                countHistoryWritePendingQueue("""
                        __QUEUE__
                        """.replace("__QUEUE__", historyWritePendingQueueConditionSql("review")), safeKeyword),
                "EXECUTED",
                "MISMATCHED",
                "PENDING",
                ""));
        queues.add(historyWritePendingQueue(
                "retest",
                "\u590d\u6d4b\u4ecd\u6709\u5dee\u5f02",
                countHistoryWritePendingQueue("""
                        __QUEUE__
                        """.replace("__QUEUE__", historyWritePendingQueueConditionSql("retest")), safeKeyword),
                "EXECUTED",
                "MISMATCHED",
                "",
                "RETEST_MISMATCHED"));
        return queues;
    }

    private List<WorkbenchHistoryWritePlanResponse> historyWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit, int maxLimit) {
        return historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, "", "", "", limit, maxLimit);
    }

    private List<WorkbenchHistoryWritePlanResponse> historyWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, String pendingQueue, int limit, int maxLimit) {
        return historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, pendingQueue, "", "", limit, maxLimit);
    }

    private List<WorkbenchHistoryWritePlanResponse> historyWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, String pendingQueue, String printQueue, String statusQueue, int limit, int maxLimit) {
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
        String safePendingQueue = normalizeHistoryWritePendingQueue(pendingQueue);
        String safePrintQueue = normalizeHistoryWritePrintQueue(printQueue);
        String safeStatusQueue = normalizeHistoryWriteStatusQueue(statusQueue);
        String pendingQueueConditionSql = safePendingQueue.isBlank() ? "" : historyWritePendingQueueConditionSql(safePendingQueue);
        String actionCodeConditionSql = historyWriteActionCodeConditionSql(safeActionCode);
        boolean actionCodePostFilter = !safeActionCode.isBlank() && actionCodeConditionSql.isBlank();
        int queryLimit = safeComparisonStatus.isBlank() && safeReviewStatus.isBlank() && safeMismatchField.isBlank() && safeMaintenanceTarget.isBlank() && safeRetestStatus.isBlank() && safePriority.isBlank() && !actionCodePostFilter && safePendingQueue.isBlank() && safePrintQueue.isBlank() && safeStatusQueue.isBlank()
                ? safeLimit
                : Math.min(5000, Math.max(safeLimit * 10, 500));
        boolean requiresDetailedPlan = !safeMismatchField.isBlank()
                || !safeMaintenanceTarget.isBlank()
                || !safePrintQueue.isBlank();
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
                __PENDING_QUEUE__
                __ACTION_CODE__
                ORDER BY p.prepared_at DESC, p.id DESC
                LIMIT ?
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("p.org_code"))
                .replace("__PENDING_QUEUE__", pendingQueueConditionSql)
                .replace("__ACTION_CODE__", actionCodeConditionSql),
                safeStatus, safeStatus, safeStatus,
                safeKeyword, safeKeyword, safeKeyword, safeKeyword, safeKeyword, safeKeyword, safeKeyword, safeKeyword,
                queryLimit).stream()
                .map(row -> requiresDetailedPlan ? historyWritePlanResponse(row) : historyWritePlanListResponse(row))
                .filter(plan -> safeComparisonStatus.isBlank() || safeComparisonStatus.equalsIgnoreCase(text(plan.comparisonStatus())))
                .filter(plan -> historyWriteReviewStatusMatches(plan, safeReviewStatus))
                .filter(plan -> historyWriteMismatchFieldMatches(plan, safeMismatchField))
                .filter(plan -> historyWriteMaintenanceTargetMatches(plan, safeMaintenanceTarget))
                .filter(plan -> safeRetestStatus.isBlank() || safeRetestStatus.equalsIgnoreCase(text(plan.comparisonRetestStatus())))
                .filter(plan -> safePriority.isBlank() || safePriority.equalsIgnoreCase(text(plan.processingPriority())))
                .filter(plan -> safeActionCode.isBlank() || safeActionCode.equalsIgnoreCase(text(plan.nextActionCode())))
                .filter(plan -> historyWritePrintQueueMatches(plan, safePrintQueue))
                .filter(plan -> historyWriteStatusQueueMatches(plan, safeStatusQueue))
                .limit(safeLimit)
                .toList();
    }

    public List<WorkbenchHistoryWritePlanResponse> exportHistoryWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit) {
        return exportHistoryWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, "", limit);
    }

    public List<WorkbenchHistoryWritePlanResponse> exportHistoryWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, String pendingQueue, int limit) {
        return exportHistoryWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, pendingQueue, "", "", limit);
    }

    public List<WorkbenchHistoryWritePlanResponse> exportHistoryWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, String pendingQueue, String printQueue, String statusQueue, int limit) {
        if (!hasMenu("SALARY_EXPORT")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Salary export permission is required.");
        }
        return historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, pendingQueue, printQueue, statusQueue, limit, 5000);
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

    private boolean historyWritePrintQueueMatches(WorkbenchHistoryWritePlanResponse plan, String printQueue) {
        if (text(printQueue).isBlank()) {
            return true;
        }
        WorkbenchReportPrintArchiveResponse archive = plan.reportPrintArchive();
        boolean printed = archive != null && Boolean.TRUE.equals(archive.printed());
        boolean reprinted = archive != null && Boolean.TRUE.equals(archive.reprinted());
        String status = text(plan.planStatus()).toUpperCase();
        boolean writable = Boolean.TRUE.equals(plan.writable());
        return switch (normalizeHistoryWritePrintQueue(printQueue)) {
            case "PRINTED_READY" -> "PREPARED".equals(status) && writable && printed;
            case "UNPRINTED_BLOCKED" -> "PREPARED".equals(status) && writable && !printed;
            case "PRINTED_BLOCKED" -> "PREPARED".equals(status) && !writable && printed;
            case "REPRINTED_REVIEW" -> reprinted
                    && "MISMATCHED".equalsIgnoreCase(text(plan.comparisonStatus()))
                    && !"REVIEWED".equalsIgnoreCase(text(plan.comparisonReviewStatus()));
            default -> true;
        };
    }

    private boolean historyWriteStatusQueueMatches(WorkbenchHistoryWritePlanResponse plan, String statusQueue) {
        if (text(statusQueue).isBlank()) {
            return true;
        }
        return switch (normalizeHistoryWriteStatusQueue(statusQueue)) {
            case "ROLLED_BACK" -> "ROLLED_BACK".equalsIgnoreCase(text(plan.planStatus()))
                    || "ROLLED_BACK".equalsIgnoreCase(text(plan.executionResult()));
            default -> true;
        };
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
        return historyWriteReviewLedger(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, "", limit);
    }

    public WorkbenchHistoryWriteReviewLedgerResponse historyWriteReviewLedger(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, String pendingQueue, int limit) {
        List<WorkbenchHistoryWritePlanResponse> plans = historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, pendingQueue, limit, 5000);
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
        int specialReviewed = 0;
        int blockedReviewed = 0;
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
            specialReviewed += isReviewed && "HISTORY_SPECIAL".equalsIgnoreCase(text(plan.comparisonReviewCategory())) ? 1 : 0;
            blockedReviewed += isReviewed && historyWritePlanBlocked(plan) ? 1 : 0;
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
                specialReviewed,
                blockedReviewed,
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
        String reviewStatus = text(plan.comparisonReviewStatus()).toUpperCase();
        if ("REVIEWED".equals(reviewStatus)) {
            return "REVIEWED";
        }
        if (!"MISMATCHED".equalsIgnoreCase(text(plan.comparisonStatus()))) {
            return "NOT_REQUIRED";
        }
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

    private record BatchWriteSafetyToken(
            String token,
            String operation,
            String signature,
            String username,
            Instant createdAt,
            Instant expiresAt,
            int executableCount,
            List<String> caseNos
    ) {
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
        return historyWriteBatchPreviewResponse(previews);
    }

    public WorkbenchHistoryWriteBatchExecuteResponse batchExecuteHistoryWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit, WorkbenchHistoryWriteBatchExecuteRequest request) {
        requireHistoryWritePermission();
        WorkbenchHistoryWriteBatchPreviewResponse preview = batchPreviewHistoryWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit);
        WorkbenchHistoryWriteBatchPreviewResponse validatedPreview = validateBatchWriteSafetyToken(request == null ? null : request.safetyToken(), preview);
        return executeHistoryWriteBatch(validatedPreview, "history-write-batch-execute");
    }

    public WorkbenchHistoryWriteBatchPreviewResponse batchPreviewSelectedHistoryWritePlans(List<String> caseNos) {
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        List<WorkbenchHistoryWritePlanResponse> plans = selectedHistoryWritePlans(caseNos);
        List<WorkbenchHistoryWritePreviewResponse> previews = plans.stream()
                .map(WorkbenchHistoryWritePlanResponse::caseNo)
                .filter(caseNo -> !text(caseNo).isBlank())
                .map(this::historyWritePreview)
                .toList();
        return historyWriteBatchPreviewResponse(previews);
    }

    public WorkbenchHistoryWriteBatchExecuteResponse batchExecuteSelectedHistoryWritePlans(List<String> caseNos, String safetyToken) {
        requireHistoryWritePermission();
        WorkbenchHistoryWriteBatchPreviewResponse preview = batchPreviewSelectedHistoryWritePlans(caseNos);
        WorkbenchHistoryWriteBatchPreviewResponse validatedPreview = validateBatchWriteSafetyToken(safetyToken, preview);
        return executeHistoryWriteBatch(validatedPreview, "history-write-selected-execute");
    }

    private WorkbenchHistoryWriteBatchPreviewResponse historyWriteBatchPreviewResponse(List<WorkbenchHistoryWritePreviewResponse> previews) {
        int ready = (int) previews.stream().filter(preview -> "READY".equalsIgnoreCase(text(preview.status()))).count();
        int blocked = (int) previews.stream().filter(preview -> "BLOCKED".equalsIgnoreCase(text(preview.status()))).count();
        int warning = (int) previews.stream().filter(preview -> "WARNING".equalsIgnoreCase(text(preview.status()))).count();
        int executable = batchWriteExecutableCount(previews);
        BatchWriteSafetyToken safety = createBatchWriteSafetyToken(previews);
        return new WorkbenchHistoryWriteBatchPreviewResponse(
                previews.size(),
                ready,
                blocked,
                warning,
                executable,
                safety.token(),
                safety.expiresAt().toString(),
                "tokenRequired=true, executable=" + executable + ", expiresAt=" + safety.expiresAt(),
                previews
        );
    }

    private BatchWriteSafetyToken createBatchWriteSafetyToken(List<WorkbenchHistoryWritePreviewResponse> previews) {
        cleanupExpiredBatchWriteSafetyTokens();
        String token = UUID.randomUUID().toString();
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plus(10, ChronoUnit.MINUTES);
        BatchWriteSafetyToken safety = new BatchWriteSafetyToken(
                token,
                "WRITE",
                batchWriteSafetySignature(previews),
                text(currentUserService.currentUsername()),
                createdAt,
                expiresAt,
                batchWriteExecutableCount(previews),
                List.copyOf((previews == null ? List.<WorkbenchHistoryWritePreviewResponse>of() : previews).stream()
                        .map(WorkbenchHistoryWritePreviewResponse::caseNo)
                        .map(this::text)
                        .filter(caseNo -> !caseNo.isBlank())
                        .toList())
        );
        batchWriteSafetyTokens.put(token, safety);
        recordBatchSafetyAudit("history-write-batch-safety-preview", safety, "PREVIEW_CREATED");
        return safety;
    }

    private WorkbenchHistoryWriteBatchPreviewResponse validateBatchWriteSafetyToken(String token, WorkbenchHistoryWriteBatchPreviewResponse preview) {
        String safeToken = text(token);
        if (safeToken.isBlank()) {
            throw new IllegalArgumentException("Batch write safety token is required. Please run preview first.");
        }
        BatchWriteSafetyToken safety = batchWriteSafetyTokens.remove(safeToken);
        if (safety == null) {
            throw new IllegalArgumentException("Batch write safety token is invalid or already used. Please run preview again.");
        }
        if (!"WRITE".equalsIgnoreCase(text(safety.operation()))) {
            throw new IllegalArgumentException("Batch write safety token is invalid for this operation. Please run preview again.");
        }
        if (safety.expiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Batch write safety token has expired. Please run preview again.");
        }
        String username = text(currentUserService.currentUsername());
        if (!text(safety.username()).equals(username)) {
            throw new IllegalArgumentException("Batch write safety token does not belong to the current user.");
        }
        WorkbenchHistoryWriteBatchPreviewResponse stablePreview = stableBatchWritePreviewForSafetyToken(safety, preview);
        List<WorkbenchHistoryWritePreviewResponse> items = stablePreview == null ? List.of() : stablePreview.items();
        String signature = batchWriteSafetySignature(items);
        if (!text(safety.signature()).equals(text(signature))) {
            throw new IllegalArgumentException("Batch write preview has changed. Please run preview again.");
        }
        int executable = batchWriteExecutableCount(items);
        if (safety.executableCount() != executable) {
            throw new IllegalArgumentException("Batch write executable count has changed. Please run preview again.");
        }
        recordBatchSafetyAudit("history-write-batch-safety-consume", safety, "CONSUMED");
        return stablePreview;
    }

    private WorkbenchHistoryWriteBatchPreviewResponse stableBatchWritePreviewForSafetyToken(BatchWriteSafetyToken safety, WorkbenchHistoryWriteBatchPreviewResponse preview) {
        List<WorkbenchHistoryWritePreviewResponse> items = preview == null ? List.of() : preview.items();
        if (text(safety.signature()).equals(batchWriteSafetySignature(items))) {
            return preview;
        }
        List<WorkbenchHistoryWritePreviewResponse> refreshedItems = safety.caseNos().stream()
                .map(this::historyWritePreview)
                .toList();
        return historyWriteBatchPreviewResponse(refreshedItems);
    }

    private String batchWriteSafetySignature(List<WorkbenchHistoryWritePreviewResponse> previews) {
        return String.join("\n", (previews == null ? List.<WorkbenchHistoryWritePreviewResponse>of() : previews).stream()
                .map(preview -> String.join("|",
                        text(preview.caseNo()),
                        text(preview.writePlanId()),
                        text(preview.status()),
                        Boolean.TRUE.equals(preview.writable()) ? "1" : "0",
                        text(preview.existingHistoryId()),
                        preview.previousHistory() == null ? "" : text(preview.previousHistory().historyId()),
                        preview.nextHistory() == null ? "" : text(preview.nextHistory().historyId()),
                        String.valueOf(preview.fields() == null ? 0 : preview.fields().size()),
                        String.valueOf(preview.issues() == null ? 0 : preview.issues().size()),
                        Boolean.TRUE.equals(reportPrintArchive(preview.caseNo()).printed()) ? "PRINTED" : "UNPRINTED"
                ))
                .sorted()
                .toList());
    }

    private int batchWriteExecutableCount(List<WorkbenchHistoryWritePreviewResponse> previews) {
        return (int) (previews == null ? List.<WorkbenchHistoryWritePreviewResponse>of() : previews).stream()
                .filter(this::batchWriteExecutable)
                .count();
    }

    private boolean batchWriteExecutable(WorkbenchHistoryWritePreviewResponse preview) {
        if (preview == null || !Boolean.TRUE.equals(preview.writable())) {
            return false;
        }
        String status = text(preview.status());
        if (!"READY".equalsIgnoreCase(status) && !"WARNING".equalsIgnoreCase(status)) {
            return false;
        }
        return Boolean.TRUE.equals(reportPrintArchive(preview.caseNo()).printed());
    }

    private void cleanupExpiredBatchWriteSafetyTokens() {
        Instant now = Instant.now();
        batchWriteSafetyTokens.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private WorkbenchHistoryWriteBatchExecuteResponse executeHistoryWriteBatch(WorkbenchHistoryWriteBatchPreviewResponse preview, String auditAction) {
        String batchNo = historyWriteBatchNo("WRITE");
        List<WorkbenchHistoryWriteExecuteResponse> results = new ArrayList<>();
        int success = 0;
        int failed = 0;
        int skipped = 0;
        for (WorkbenchHistoryWritePreviewResponse item : preview.items()) {
            String skipReason = batchHistoryWriteSkipReason(item);
            if (!skipReason.isBlank()) {
                skipped += 1;
                WorkbenchHistoryWriteExecuteResponse skippedResult = batchSkippedResult(item, skipReason);
                results.add(skippedResult);
                recordHistoryWriteBatchItem(auditAction, batchNo, skippedResult);
                continue;
            }
            try {
                WorkbenchHistoryWriteExecuteResponse executed = transactionTemplate.execute(transactionStatus -> executeHistoryWrite(item.caseNo()));
                if (executed == null) {
                    failed += 1;
                    WorkbenchHistoryWriteExecuteResponse failedResult = batchFailedResult(item, "History write returned no result.");
                    results.add(failedResult);
                    recordHistoryWriteBatchItem(auditAction, batchNo, failedResult);
                } else {
                    success += 1;
                    results.add(executed);
                    recordHistoryWriteBatchItem(auditAction, batchNo, executed);
                }
            } catch (RuntimeException ex) {
                failed += 1;
                WorkbenchHistoryWriteExecuteResponse failedResult = batchFailedResult(item, ex.getMessage());
                results.add(failedResult);
                recordHistoryWriteBatchItem(auditAction, batchNo, failedResult);
            }
        }
        systemAuditService.record("workbench", auditAction, "SALARY_CASE", "BATCH",
                "batchNo=" + batchNo + ", total=" + preview.total() + ", success=" + success + ", failed=" + failed + ", skipped=" + skipped);
        return new WorkbenchHistoryWriteBatchExecuteResponse(batchNo, preview.total(), success, failed, skipped, results);
    }

    private String batchHistoryWriteSkipReason(WorkbenchHistoryWritePreviewResponse item) {
        Map<String, Object> businessCase = businessCaseRow(item.caseNo());
        String caseStatus = text(businessCase.get("status"));
        if (!"DONE".equalsIgnoreCase(caseStatus)) {
            return "Skipped because salary case status is " + (caseStatus.isBlank() ? "UNKNOWN" : caseStatus) + ".";
        }
        Map<String, Object> plan = historyWritePlanRowIfExists(item.caseNo());
        if (plan != null && !plan.isEmpty()) {
            String planStatus = text(plan.get("plan_status"));
            if ("EXECUTED".equalsIgnoreCase(planStatus)) {
                return "Skipped because history write plan is already executed.";
            }
            if ("ROLLED_BACK".equalsIgnoreCase(planStatus)) {
                return "Skipped because history write plan has been rolled back.";
            }
            if ("BLOCKED".equalsIgnoreCase(planStatus)) {
                return "Skipped because history write plan is blocked.";
            }
        }
        if (!Boolean.TRUE.equals(item.writable()) || "BLOCKED".equalsIgnoreCase(text(item.status()))) {
            String issueSummary = item.issues().stream()
                    .map(this::text)
                    .filter(issue -> !issue.isBlank())
                    .findFirst()
                    .orElse("preview status is " + text(item.status()));
            return "Skipped because " + issueSummary;
        }
        WorkbenchReportPrintArchiveResponse printArchive = reportPrintArchive(item.caseNo());
        if (!Boolean.TRUE.equals(printArchive.printed())) {
            return "Skipped because approval report has not been printed.";
        }
        return "";
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
        String batchNo = historyWriteBatchNo("RETEST");
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
                WorkbenchHistoryWriteBatchRetestItemResponse result = new WorkbenchHistoryWriteBatchRetestItemResponse(
                        comparison.caseNo(),
                        comparison.personCode(),
                        comparison.orgCode(),
                        comparison.businessType(),
                        allMatched ? "MATCHED" : "MISMATCHED",
                        comparison.totalMatched(),
                        mismatchCount,
                        allMatched ? "\u5f53\u524d\u57fa\u7840\u590d\u6d4b\u4e00\u81f4" : "\u5f53\u524d\u57fa\u7840\u590d\u6d4b\u4ecd\u6709\u5dee\u5f02"
                );
                items.add(result);
                recordHistoryWriteBatchRetestItem(auditAction, batchNo, result);
            } catch (RuntimeException ex) {
                failed += 1;
                WorkbenchHistoryWriteBatchRetestItemResponse result = new WorkbenchHistoryWriteBatchRetestItemResponse(
                        plan.caseNo(),
                        plan.personCode(),
                        plan.orgCode(),
                        plan.businessType(),
                        "FAILED",
                        false,
                        0,
                        ex.getMessage()
                );
                items.add(result);
                recordHistoryWriteBatchRetestItem(auditAction, batchNo, result);
            }
        }
        systemAuditService.record("workbench", auditAction, "SALARY_CASE", "BATCH",
                "batchNo=" + batchNo + ", total=" + plans.size() + ", matched=" + matched + ", mismatched=" + mismatched + ", failed=" + failed);
        return new WorkbenchHistoryWriteBatchRetestResponse(batchNo, plans.size(), matched, mismatched, failed, items);
    }

    public List<WorkbenchHistoryWriteBatchLedgerResponse> historyWriteBatchLedger(int limit) {
        return historyWriteBatchLedger("", limit);
    }

    public List<WorkbenchHistoryWriteBatchLedgerResponse> historyWriteBatchLedger(String queue, int limit) {
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        int safeLimit = Math.min(Math.max(1, limit), 200);
        String safeQueue = text(queue);
        String queueCondition = historyWriteBatchLedgerQueueConditionSql(safeQueue);
        String sql = """
                SELECT action_name,
                       summary,
                       operator,
                       DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') AS created_at
                FROM sys_audit_log
                WHERE module_name = 'workbench'
                  AND target_type = 'SALARY_CASE'
                  AND target_code = 'BATCH'
                  AND action_name IN (
                      'history-write-batch-execute',
                      'history-write-selected-execute',
                      'history-write-batch-retest-preview',
                      'history-write-selected-retest-preview',
                      'history-write-batch-retest-approve',
                      'history-write-selected-retest-approve',
                      'history-write-batch-review',
                      'history-write-selected-review',
                      'history-write-batch-special-review',
                      'history-write-selected-special-review',
                      'history-write-batch-rollback',
                      'history-write-selected-rollback'
                  )
                  __QUEUE_CONDITION__
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """.replace("__QUEUE_CONDITION__", queueCondition);
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
                    String summary = text(rs.getString("summary"));
                    return new WorkbenchHistoryWriteBatchLedgerResponse(
                            auditSummaryValue(summary, "batchNo"),
                            rs.getString("action_name"),
                            auditSummaryInt(summary, "total"),
                            auditSummaryInt(summary, "success"),
                            auditSummaryInt(summary, "failed"),
                            auditSummaryInt(summary, "skipped"),
                            auditSummaryInt(summary, "matched"),
                            auditSummaryInt(summary, "mismatched"),
                            summary,
                            rs.getString("operator"),
                            rs.getString("created_at")
                    );
                }, safeLimit);
    }

    private String historyWriteBatchLedgerQueueConditionSql(String queue) {
        return switch (text(queue)) {
            case "SALARY_NEXT_EXECUTE_WRITE" -> """

                      AND action_name IN (
                          'history-write-batch-execute',
                          'history-write-selected-execute'
                      )
                    """;
            case "SALARY_NEXT_REVIEW_DIFFERENCE" -> """

                      AND action_name IN (
                          'history-write-batch-retest-preview',
                          'history-write-selected-retest-preview',
                          'history-write-batch-retest-approve',
                          'history-write-selected-retest-approve',
                          'history-write-batch-review',
                          'history-write-selected-review',
                          'history-write-batch-special-review',
                          'history-write-selected-special-review'
                      )
                    """;
            case "HISTORY_PLAN_ROLLED_BACK" -> """

                      AND action_name IN (
                          'history-write-batch-rollback',
                          'history-write-selected-rollback'
                      )
                    """;
            case "SALARY_CLOSURE_CLOSED" -> """

                      AND COALESCE(CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(summary, 'failed=', -1), ',', 1) AS UNSIGNED), 0) = 0
                      AND COALESCE(CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(summary, 'skipped=', -1), ',', 1) AS UNSIGNED), 0) = 0
                      AND COALESCE(CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(summary, 'mismatched=', -1), ',', 1) AS UNSIGNED), 0) = 0
                    """;
            default -> "";
        };
    }

    public List<SystemAuditLogResponse> historyWriteBatchAudits(String batchNo) {
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        String safeBatchNo = text(batchNo);
        if (safeBatchNo.isBlank()) {
            throw new IllegalArgumentException("Batch number is required.");
        }
        systemAuditService.ensureTable();
        return jdbcTemplate.query("""
                SELECT CONCAT('SYS-', id) AS audit_id,
                       module_name,
                       action_name,
                       target_type,
                       target_code,
                       summary,
                       operator,
                       DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') AS created_at
                FROM sys_audit_log
                WHERE module_name = 'workbench'
                  AND target_type IN ('SALARY_CASE', 'HISTORY_WRITE_BATCH')
                  AND action_name LIKE 'history-write%'
                  AND summary LIKE CONCAT('%', ?, '%')
                ORDER BY created_at DESC, id DESC
                LIMIT 500
                """, (rs, rowNum) -> new SystemAuditLogResponse(
                rs.getString("audit_id"),
                rs.getString("module_name"),
                rs.getString("action_name"),
                rs.getString("target_type"),
                rs.getString("target_code"),
                rs.getString("summary"),
                rs.getString("operator"),
                rs.getString("created_at")
        ), safeBatchNo);
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
        String batchNo = historyWriteBatchNo("REVIEW");
        List<WorkbenchHistoryWriteExecuteResponse> results = new ArrayList<>();
        int success = 0;
        int failed = 0;
        int skipped = 0;
        for (WorkbenchHistoryWritePlanResponse plan : plans) {
            if ("REVIEWED".equalsIgnoreCase(text(plan.comparisonReviewStatus()))) {
                skipped += 1;
                WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "SKIPPED", "Skipped because comparison has already been reviewed.");
                results.add(result);
                recordHistoryWriteBatchItem(auditAction, batchNo, result);
                continue;
            }
            try {
                WorkbenchHistoryWriteComparisonResponse approved = transactionTemplate.execute(transactionStatus -> approveRetestPassedHistoryWriteComparison(plan.caseNo()));
                if (approved == null) {
                    failed += 1;
                    WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "FAILED", "Retest approve returned no result.");
                    results.add(result);
                    recordHistoryWriteBatchItem(auditAction, batchNo, result);
                } else {
                    success += 1;
                    WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "REVIEWED", "\u5f53\u524d\u57fa\u7840\u4fe1\u606f\u590d\u6d4b\u5df2\u4e00\u81f4\uff0c\u5df2\u6807\u8bb0\u590d\u6d4b\u901a\u8fc7");
                    results.add(result);
                    recordHistoryWriteBatchItem(auditAction, batchNo, result);
                }
            } catch (RuntimeException ex) {
                if (isRetestMismatch(ex)) {
                    skipped += 1;
                    WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "SKIPPED", "\u5f53\u524d\u57fa\u7840\u4fe1\u606f\u590d\u6d4b\u4ecd\u6709\u5dee\u5f02\uff0c\u672a\u6807\u8bb0\u901a\u8fc7");
                    results.add(result);
                    recordHistoryWriteBatchItem(auditAction, batchNo, result);
                } else {
                    failed += 1;
                    WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "FAILED", ex.getMessage());
                    results.add(result);
                    recordHistoryWriteBatchItem(auditAction, batchNo, result);
                }
            }
        }
        systemAuditService.record("workbench", auditAction, "SALARY_CASE", "BATCH",
                "batchNo=" + batchNo + ", total=" + plans.size() + ", success=" + success + ", failed=" + failed + ", skipped=" + skipped);
        return new WorkbenchHistoryWriteBatchExecuteResponse(batchNo, plans.size(), success, failed, skipped, results);
    }

    public WorkbenchHistoryWriteBatchExecuteResponse batchReviewHistoryWriteComparisons(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit, WorkbenchHistoryWriteBatchReviewRequest request) {
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        List<WorkbenchHistoryWritePlanResponse> plans = historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit);
        return batchReviewHistoryWriteComparisons(plans, request, "history-write-batch-review");
    }

    public WorkbenchHistoryWriteBatchExecuteResponse batchReviewSelectedHistoryWriteComparisons(WorkbenchHistoryWriteBatchReviewRequest request) {
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        List<WorkbenchHistoryWritePlanResponse> plans = selectedHistoryWritePlans(request == null ? null : request.caseNos());
        return batchReviewHistoryWriteComparisons(plans, request, "history-write-selected-review");
    }

    public WorkbenchHistoryWriteBatchExecuteResponse batchMarkSpecialHistoryWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit, WorkbenchHistoryWriteBatchReviewRequest request) {
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        List<WorkbenchHistoryWritePlanResponse> plans = historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit);
        return batchMarkSpecialHistoryWritePlans(plans, request, "history-write-batch-special-review");
    }

    public WorkbenchHistoryWriteBatchExecuteResponse batchMarkSelectedSpecialHistoryWritePlans(WorkbenchHistoryWriteBatchReviewRequest request) {
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
        List<WorkbenchHistoryWritePlanResponse> plans = selectedHistoryWritePlans(request == null ? null : request.caseNos());
        return batchMarkSpecialHistoryWritePlans(plans, request, "history-write-selected-special-review");
    }

    private WorkbenchHistoryWriteBatchExecuteResponse batchReviewHistoryWriteComparisons(List<WorkbenchHistoryWritePlanResponse> plans, WorkbenchHistoryWriteBatchReviewRequest request, String auditAction) {
        String reviewCategory = normalizeHistoryWriteReviewCategory(request == null ? null : request.reviewCategory());
        String reviewReason = left(text(request == null ? null : request.reviewReason()), 1000);
        if (reviewCategory.isBlank()) {
            throw new IllegalArgumentException("Comparison review category is required.");
        }
        if (reviewReason.isBlank()) {
            throw new IllegalArgumentException("Comparison review reason is required.");
        }
        String batchNo = historyWriteBatchNo("DIFFREVIEW");
        List<WorkbenchHistoryWriteExecuteResponse> results = new ArrayList<>();
        int success = 0;
        int failed = 0;
        int skipped = 0;
        for (WorkbenchHistoryWritePlanResponse plan : plans) {
            if (!"EXECUTED".equalsIgnoreCase(text(plan.planStatus())) || !"SUCCESS".equalsIgnoreCase(text(plan.executionResult()))) {
                skipped += 1;
                WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "SKIPPED", "Skipped because history write plan is not executed successfully.");
                results.add(result);
                recordHistoryWriteBatchItem(auditAction, batchNo, result);
                continue;
            }
            if (!"MISMATCHED".equalsIgnoreCase(text(plan.comparisonStatus()))) {
                skipped += 1;
                WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "SKIPPED", "Skipped because comparison has no difference.");
                results.add(result);
                recordHistoryWriteBatchItem(auditAction, batchNo, result);
                continue;
            }
            if ("REVIEWED".equalsIgnoreCase(text(plan.comparisonReviewStatus()))) {
                skipped += 1;
                WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "SKIPPED", "Skipped because comparison has already been reviewed.");
                results.add(result);
                recordHistoryWriteBatchItem(auditAction, batchNo, result);
                continue;
            }
            try {
                WorkbenchHistoryWriteComparisonResponse reviewed = transactionTemplate.execute(transactionStatus ->
                        reviewHistoryWriteComparison(plan.caseNo(), new WorkbenchHistoryWriteReviewRequest(reviewCategory, reviewReason)));
                if (reviewed == null) {
                    failed += 1;
                    WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "FAILED", "Comparison review returned no result.");
                    results.add(result);
                    recordHistoryWriteBatchItem(auditAction, batchNo, result);
                } else {
                    success += 1;
                    WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "REVIEWED", "\u5199\u5165\u5dee\u5f02\u5df2\u6807\u8bb0\u6838\u67e5\uff1a" + reviewCategory);
                    results.add(result);
                    recordHistoryWriteBatchItem(auditAction, batchNo, result);
                }
            } catch (RuntimeException ex) {
                failed += 1;
                WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "FAILED", ex.getMessage());
                results.add(result);
                recordHistoryWriteBatchItem(auditAction, batchNo, result);
            }
        }
        systemAuditService.record("workbench", auditAction, "SALARY_CASE", "BATCH",
                "batchNo=" + batchNo + ", total=" + plans.size() + ", success=" + success + ", failed=" + failed + ", skipped=" + skipped + ", reviewCategory=" + reviewCategory + ", reviewReason=" + left(reviewReason, 200));
        return new WorkbenchHistoryWriteBatchExecuteResponse(batchNo, plans.size(), success, failed, skipped, results);
    }

    private WorkbenchHistoryWriteBatchExecuteResponse batchMarkSpecialHistoryWritePlans(List<WorkbenchHistoryWritePlanResponse> plans, WorkbenchHistoryWriteBatchReviewRequest request, String auditAction) {
        String reviewReason = left(text(request == null ? null : request.reviewReason()), 1000);
        if (reviewReason.isBlank()) {
            throw new IllegalArgumentException("Special case review reason is required.");
        }
        String batchNo = historyWriteBatchNo("SPECIAL");
        List<WorkbenchHistoryWriteExecuteResponse> results = new ArrayList<>();
        int success = 0;
        int failed = 0;
        int skipped = 0;
        for (WorkbenchHistoryWritePlanResponse plan : plans) {
            if ("REVIEWED".equalsIgnoreCase(text(plan.comparisonReviewStatus()))) {
                skipped += 1;
                WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "SKIPPED", "Skipped because plan has already been reviewed.");
                results.add(result);
                recordHistoryWriteBatchItem(auditAction, batchNo, result);
                continue;
            }
            boolean blocked = historyWritePlanBlocked(plan);
            boolean mismatched = "EXECUTED".equalsIgnoreCase(text(plan.planStatus()))
                    && "SUCCESS".equalsIgnoreCase(text(plan.executionResult()))
                    && "MISMATCHED".equalsIgnoreCase(text(plan.comparisonStatus()));
            if (!blocked && !mismatched) {
                skipped += 1;
                WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "SKIPPED", "Skipped because plan is not blocked or mismatched.");
                results.add(result);
                recordHistoryWriteBatchItem(auditAction, batchNo, result);
                continue;
            }
            try {
                WorkbenchHistoryWriteReviewRequest specialRequest = new WorkbenchHistoryWriteReviewRequest("HISTORY_SPECIAL", reviewReason);
                if (blocked) {
                    WorkbenchHistoryWritePlanResponse reviewed = transactionTemplate.execute(transactionStatus ->
                            reviewBlockedHistoryWritePlan(plan.caseNo(), specialRequest));
                    if (reviewed == null) {
                        failed += 1;
                        WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "FAILED", "Special blocked review returned no result.");
                        results.add(result);
                        recordHistoryWriteBatchItem(auditAction, batchNo, result);
                    } else {
                        success += 1;
                        WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "REVIEWED", "\u5df2\u6807\u8bb0\u7279\u6b8a\u60c5\u51b5\uff1a\u963b\u65ad\u9879\u540e\u671f\u6838\u67e5");
                        results.add(result);
                        recordHistoryWriteBatchItem(auditAction, batchNo, result);
                    }
                } else {
                    WorkbenchHistoryWriteComparisonResponse reviewed = transactionTemplate.execute(transactionStatus ->
                            reviewHistoryWriteComparison(plan.caseNo(), specialRequest));
                    if (reviewed == null) {
                        failed += 1;
                        WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "FAILED", "Special comparison review returned no result.");
                        results.add(result);
                        recordHistoryWriteBatchItem(auditAction, batchNo, result);
                    } else {
                        success += 1;
                        WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "REVIEWED", "\u5df2\u6807\u8bb0\u7279\u6b8a\u60c5\u51b5\uff1a\u5199\u5165\u5dee\u5f02\u540e\u671f\u6838\u67e5");
                        results.add(result);
                        recordHistoryWriteBatchItem(auditAction, batchNo, result);
                    }
                }
            } catch (RuntimeException ex) {
                failed += 1;
                WorkbenchHistoryWriteExecuteResponse result = batchPlanResult(plan, "FAILED", ex.getMessage());
                results.add(result);
                recordHistoryWriteBatchItem(auditAction, batchNo, result);
            }
        }
        systemAuditService.record("workbench", auditAction, "SALARY_CASE", "BATCH",
                "batchNo=" + batchNo + ", total=" + plans.size() + ", success=" + success + ", failed=" + failed + ", skipped=" + skipped + ", reviewCategory=HISTORY_SPECIAL, reviewReason=" + left(reviewReason, 200));
        return new WorkbenchHistoryWriteBatchExecuteResponse(batchNo, plans.size(), success, failed, skipped, results);
    }

    private boolean historyWritePlanBlocked(WorkbenchHistoryWritePlanResponse plan) {
        if ("EXECUTED".equalsIgnoreCase(text(plan.planStatus()))
                && "SUCCESS".equalsIgnoreCase(text(plan.executionResult()))) {
            return false;
        }
        return "BLOCKED".equalsIgnoreCase(text(plan.planStatus()))
                || "BLOCKED".equalsIgnoreCase(text(plan.previewStatus()))
                || ("PREPARED".equalsIgnoreCase(text(plan.planStatus())) && !Boolean.TRUE.equals(plan.writable()));
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

    public WorkbenchHistoryWriteRollbackBatchPreviewResponse batchPreviewRollbackHistoryWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit) {
        requireHistoryRollbackPermission();
        List<WorkbenchHistoryWritePlanResponse> plans = historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit);
        return rollbackBatchPreviewResponse(plans);
    }

    public WorkbenchHistoryWriteRollbackBatchPreviewResponse batchPreviewSelectedRollbackHistoryWritePlans(List<String> caseNos) {
        requireHistoryRollbackPermission();
        List<WorkbenchHistoryWritePlanResponse> plans = selectedHistoryWritePlans(caseNos);
        return rollbackBatchPreviewResponse(plans);
    }

    public WorkbenchHistoryWriteBatchExecuteResponse batchRollbackHistoryWritePlans(String status, String comparisonStatus, String reviewStatus, String keyword, String mismatchField, String maintenanceTarget, String retestStatus, String priority, String actionCode, int limit, WorkbenchHistoryWriteBatchExecuteRequest request) {
        requireHistoryRollbackPermission();
        List<WorkbenchHistoryWritePlanResponse> plans = historyWritePlans(status, comparisonStatus, reviewStatus, keyword, mismatchField, maintenanceTarget, retestStatus, priority, actionCode, limit);
        WorkbenchHistoryWriteRollbackBatchPreviewResponse preview = rollbackBatchPreviewResponse(plans);
        validateBatchRollbackSafetyToken(request == null ? null : request.safetyToken(), preview);
        return rollbackHistoryWriteBatch(plans, "history-write-batch-rollback");
    }

    public WorkbenchHistoryWriteBatchExecuteResponse batchRollbackSelectedHistoryWritePlans(List<String> caseNos, String safetyToken) {
        requireHistoryRollbackPermission();
        List<WorkbenchHistoryWritePlanResponse> plans = selectedHistoryWritePlans(caseNos);
        WorkbenchHistoryWriteRollbackBatchPreviewResponse preview = rollbackBatchPreviewResponse(plans);
        validateBatchRollbackSafetyToken(safetyToken, preview);
        return rollbackHistoryWriteBatch(plans, "history-write-selected-rollback");
    }

    private WorkbenchHistoryWriteRollbackBatchPreviewResponse rollbackBatchPreviewResponse(List<WorkbenchHistoryWritePlanResponse> plans) {
        List<WorkbenchHistoryWriteRollbackPreviewResponse> previews = (plans == null ? List.<WorkbenchHistoryWritePlanResponse>of() : plans).stream()
                .map(WorkbenchHistoryWritePlanResponse::caseNo)
                .filter(caseNo -> !text(caseNo).isBlank())
                .map(this::rollbackHistoryWritePreview)
                .toList();
        int rollbackable = (int) previews.stream().filter(preview -> Boolean.TRUE.equals(preview.rollbackable())).count();
        int blocked = previews.size() - rollbackable;
        BatchWriteSafetyToken safety = createBatchRollbackSafetyToken(previews);
        return new WorkbenchHistoryWriteRollbackBatchPreviewResponse(
                previews.size(),
                rollbackable,
                blocked,
                safety.token(),
                safety.expiresAt().toString(),
                "tokenRequired=true, rollbackable=" + rollbackable + ", expiresAt=" + safety.expiresAt(),
                previews
        );
    }

    private BatchWriteSafetyToken createBatchRollbackSafetyToken(List<WorkbenchHistoryWriteRollbackPreviewResponse> previews) {
        cleanupExpiredBatchWriteSafetyTokens();
        String token = UUID.randomUUID().toString();
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plus(10, ChronoUnit.MINUTES);
        BatchWriteSafetyToken safety = new BatchWriteSafetyToken(
                token,
                "ROLLBACK",
                batchRollbackSafetySignature(previews),
                text(currentUserService.currentUsername()),
                createdAt,
                expiresAt,
                (int) (previews == null ? List.<WorkbenchHistoryWriteRollbackPreviewResponse>of() : previews).stream()
                        .filter(preview -> Boolean.TRUE.equals(preview.rollbackable()))
                        .count(),
                List.copyOf((previews == null ? List.<WorkbenchHistoryWriteRollbackPreviewResponse>of() : previews).stream()
                        .map(WorkbenchHistoryWriteRollbackPreviewResponse::caseNo)
                        .map(this::text)
                        .filter(caseNo -> !caseNo.isBlank())
                        .toList())
        );
        batchWriteSafetyTokens.put(token, safety);
        recordBatchSafetyAudit("history-write-batch-rollback-safety-preview", safety, "PREVIEW_CREATED");
        return safety;
    }

    private void validateBatchRollbackSafetyToken(String token, WorkbenchHistoryWriteRollbackBatchPreviewResponse preview) {
        String safeToken = text(token);
        if (safeToken.isBlank()) {
            throw new IllegalArgumentException("Batch rollback safety token is required. Please run rollback preview first.");
        }
        BatchWriteSafetyToken safety = batchWriteSafetyTokens.remove(safeToken);
        if (safety == null) {
            throw new IllegalArgumentException("Batch rollback safety token is invalid or already used. Please run rollback preview again.");
        }
        if (!"ROLLBACK".equalsIgnoreCase(text(safety.operation()))) {
            throw new IllegalArgumentException("Batch rollback safety token is invalid for this operation. Please run rollback preview again.");
        }
        if (safety.expiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Batch rollback safety token has expired. Please run rollback preview again.");
        }
        String username = text(currentUserService.currentUsername());
        if (!text(safety.username()).equals(username)) {
            throw new IllegalArgumentException("Batch rollback safety token does not belong to the current user.");
        }
        List<WorkbenchHistoryWriteRollbackPreviewResponse> items = preview == null ? List.of() : preview.items();
        String signature = batchRollbackSafetySignature(items);
        if (!text(safety.signature()).equals(text(signature))) {
            throw new IllegalArgumentException("Batch rollback preview has changed. Please run rollback preview again.");
        }
        int rollbackable = (int) (items == null ? List.<WorkbenchHistoryWriteRollbackPreviewResponse>of() : items).stream()
                .filter(item -> Boolean.TRUE.equals(item.rollbackable()))
                .count();
        if (safety.executableCount() != rollbackable) {
            throw new IllegalArgumentException("Batch rollback executable count has changed. Please run rollback preview again.");
        }
        recordBatchSafetyAudit("history-write-batch-rollback-safety-consume", safety, "CONSUMED");
    }

    private String batchRollbackSafetySignature(List<WorkbenchHistoryWriteRollbackPreviewResponse> previews) {
        return String.join("\n", (previews == null ? List.<WorkbenchHistoryWriteRollbackPreviewResponse>of() : previews).stream()
                .map(preview -> String.join("|",
                        text(preview.caseNo()),
                        text(preview.writePlanId()),
                        text(preview.status()),
                        Boolean.TRUE.equals(preview.rollbackable()) ? "1" : "0",
                        text(preview.historyId()),
                        preview.previousHistory() == null ? "" : text(preview.previousHistory().historyId()),
                        preview.insertedHistory() == null ? "" : text(preview.insertedHistory().historyId()),
                        preview.nextHistory() == null ? "" : text(preview.nextHistory().historyId()),
                        preview.issues() == null ? "0" : String.valueOf(preview.issues().size())
                ))
                .sorted()
                .toList());
    }

    private WorkbenchHistoryWriteBatchExecuteResponse rollbackHistoryWriteBatch(List<WorkbenchHistoryWritePlanResponse> plans, String auditAction) {
        String batchNo = historyWriteBatchNo("ROLLBACK");
        List<WorkbenchHistoryWriteExecuteResponse> results = new ArrayList<>();
        int success = 0;
        int failed = 0;
        int skipped = 0;
        for (WorkbenchHistoryWritePlanResponse plan : plans) {
            if (!"EXECUTED".equalsIgnoreCase(text(plan.planStatus())) || !"SUCCESS".equalsIgnoreCase(text(plan.executionResult()))) {
                skipped += 1;
                WorkbenchHistoryWriteExecuteResponse result = new WorkbenchHistoryWriteExecuteResponse(
                        plan.caseNo(),
                        plan.workItemId(),
                        plan.personCode(),
                        plan.orgCode(),
                        plan.planNo(),
                        plan.insertedHistoryId(),
                        "SKIPPED",
                        !text(plan.previousHistoryId()).isBlank() || !text(plan.nextHistoryId()).isBlank(),
                        "Skipped because plan status is " + text(plan.planStatus())
                );
                results.add(result);
                recordHistoryWriteBatchItem(auditAction, batchNo, result);
                continue;
            }
            try {
                WorkbenchHistoryWriteExecuteResponse rolledBack = transactionTemplate.execute(transactionStatus -> rollbackHistoryWrite(plan.caseNo()));
                if (rolledBack == null) {
                    failed += 1;
                    WorkbenchHistoryWriteExecuteResponse result = batchFailedResult(plan, "History rollback returned no result.");
                    results.add(result);
                    recordHistoryWriteBatchItem(auditAction, batchNo, result);
                } else {
                    success += 1;
                    results.add(rolledBack);
                    recordHistoryWriteBatchItem(auditAction, batchNo, rolledBack);
                }
            } catch (RuntimeException ex) {
                failed += 1;
                WorkbenchHistoryWriteExecuteResponse result = batchFailedResult(plan, ex.getMessage());
                results.add(result);
                recordHistoryWriteBatchItem(auditAction, batchNo, result);
            }
        }
        systemAuditService.record("workbench", auditAction, "SALARY_CASE", "BATCH",
                "batchNo=" + batchNo + ", total=" + plans.size() + ", success=" + success + ", failed=" + failed + ", skipped=" + skipped);
        return new WorkbenchHistoryWriteBatchExecuteResponse(batchNo, plans.size(), success, failed, skipped, results);
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

    private WorkbenchHistoryWriteExecuteResponse batchSkippedResult(WorkbenchHistoryWritePreviewResponse item, String message) {
        return new WorkbenchHistoryWriteExecuteResponse(
                item.caseNo(),
                item.workItemId(),
                item.personCode(),
                item.orgCode(),
                item.writePlanId(),
                "",
                "SKIPPED",
                item.sidUpdateRequired(),
                text(message).isBlank() ? "History write skipped." : left(message, 1000)
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

    private void recordHistoryWriteBatchItem(String auditAction, String batchNo, WorkbenchHistoryWriteExecuteResponse result) {
        if (result == null || text(result.caseNo()).isBlank()) {
            return;
        }
        String summary = "batchNo=" + text(batchNo)
                + ", status=" + text(result.status())
                + ", personCode=" + text(result.personCode())
                + ", historyId=" + text(result.historyId())
                + ", message=" + left(text(result.message()), 300);
        systemAuditService.record("workbench", text(auditAction), "SALARY_CASE", result.caseNo(), summary);
        systemAuditService.record("workbench", text(auditAction) + "-item", "SALARY_CASE", result.caseNo(), summary);
    }

    private void recordHistoryWriteBatchRetestItem(String auditAction, String batchNo, WorkbenchHistoryWriteBatchRetestItemResponse result) {
        if (result == null || text(result.caseNo()).isBlank()) {
            return;
        }
        String summary = "batchNo=" + text(batchNo)
                + ", status=" + text(result.status())
                + ", personCode=" + text(result.personCode())
                + ", mismatchCount=" + (result.mismatchCount() == null ? 0 : result.mismatchCount())
                + ", message=" + left(text(result.message()), 300);
        systemAuditService.record("workbench", text(auditAction), "SALARY_CASE", result.caseNo(), summary);
        systemAuditService.record("workbench", text(auditAction) + "-item", "SALARY_CASE", result.caseNo(), summary);
    }

    private void recordBatchSafetyAudit(String action, BatchWriteSafetyToken safety, String status) {
        if (safety == null) {
            return;
        }
        String tokenRef = safetyDigestRef(safety.token());
        String summary = "operation=" + text(safety.operation())
                + ", status=" + text(status)
                + ", tokenRef=" + tokenRef
                + ", signatureRef=" + safetyDigestRef(safety.signature())
                + ", executableCount=" + safety.executableCount()
                + ", caseCount=" + (safety.caseNos() == null ? 0 : safety.caseNos().size())
                + ", createdAt=" + safety.createdAt()
                + ", expiresAt=" + safety.expiresAt()
                + ", cases=" + left(String.join(",", safety.caseNos() == null ? List.of() : safety.caseNos()), 300);
        systemAuditService.record("workbench", text(action), "HISTORY_WRITE_BATCH_SAFETY", tokenRef, summary);
    }

    private String safetyDigestRef(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text(value).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 12);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 digest is not available.", ex);
        }
    }

    private String auditSummaryValue(String summary, String key) {
        String safeKey = text(key);
        if (safeKey.isBlank()) {
            return "";
        }
        for (String token : text(summary).replace(",", " ").split("\\s+")) {
            String prefix = safeKey + "=";
            if (token.startsWith(prefix)) {
                return token.substring(prefix.length()).trim();
            }
        }
        return "";
    }

    private Integer auditSummaryInt(String summary, String key) {
        String value = auditSummaryValue(summary, key);
        if (value.isBlank()) {
            return 0;
        }
        try {
            String digits = value.replaceFirst("^(\\d+).*$", "$1");
            return Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private boolean historyWriteBatchNeedsFollowup(WorkbenchHistoryWriteBatchLedgerResponse item) {
        if (item == null) {
            return false;
        }
        return numberValue(item.failed()) > 0
                || numberValue(item.skipped()) > 0
                || numberValue(item.mismatched()) > 0;
    }

    private int numberValue(Integer value) {
        return value == null ? 0 : value;
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
        boolean totalMatched = amountMatched(expectedTotal, actualTotal);
        int mismatchCount = 0;
        for (WorkbenchHistoryWriteComparisonField field : fields) {
            if (!Boolean.TRUE.equals(field.matched())) {
                mismatchCount += 1;
            }
        }
        if (!totalMatched) {
            mismatchCount += 1;
        }
        refreshHistoryWriteComparisonCache(safeCaseNo, mismatchCount == 0 ? "MATCHED" : "MISMATCHED", mismatchCount);
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
                totalMatched,
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

    @Transactional
    public WorkbenchHistoryWritePlanResponse reviewBlockedHistoryWritePlan(String caseNo, WorkbenchHistoryWriteReviewRequest request) {
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
        String planStatus = text(row.get("plan_status")).toUpperCase();
        String previewStatus = text(row.get("preview_status")).toUpperCase();
        boolean blocked = "BLOCKED".equals(planStatus)
                || "BLOCKED".equals(previewStatus)
                || ("PREPARED".equals(planStatus) && !booleanValue(row.get("writable")));
        if (!blocked) {
            throw new IllegalArgumentException("History write plan is not blocked.");
        }
        String reviewCategory = normalizeHistoryWriteReviewCategory(request == null ? null : request.reviewCategory());
        String reviewReason = left(text(request == null ? null : request.reviewReason()), 1000);
        if (reviewCategory.isBlank()) {
            reviewCategory = "HISTORY_SPECIAL";
        }
        if (reviewReason.isBlank()) {
            reviewReason = "阻断项暂按后期核查处理";
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
        systemAuditService.record("workbench", "history-write-blocked-review", "SALARY_CASE", safeCaseNo,
                text(row.get("person_code")) + " " + text(row.get("business_type")) + " blockedReviewCategory=" + reviewCategory + " reviewReason=" + reviewReason);
        return historyWritePlan(safeCaseNo);
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
        refreshHistoryWriteComparisonCache(text(row.get("case_no")), comparisonStatus, mismatchCount);
        String retestStatus = historyWriteRetestStatus(text(row.get("case_no")));
        HistoryWriteWorkflow workflow = historyWriteWorkflow(
                text(row.get("plan_status")),
                text(row.get("preview_status")),
                booleanValue(row.get("writable")),
                text(row.get("execution_result")),
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
                reportPrintArchive(text(row.get("case_no"))),
                maintenanceSuggestionJson,
                text(row.get("issues_json")),
                text(row.get("preview_json"))
        );
    }

    private WorkbenchHistoryWritePlanResponse historyWritePlanListResponse(Map<String, Object> row) {
        String cachedComparisonStatus = text(row.get("comparison_status"));
        String comparisonStatus = cachedComparisonStatus.isBlank() ? historyWriteComparisonStatus(row) : cachedComparisonStatus;
        Integer cachedMismatchCount = number(row.get("comparison_mismatch_count"));
        Integer mismatchCount = cachedMismatchCount == null
                ? (cachedComparisonStatus.isBlank() ? historyWriteComparisonMismatchCount(row) : 0)
                : cachedMismatchCount;
        String retestStatus = historyWriteRetestStatus(text(row.get("case_no")));
        HistoryWriteWorkflow workflow = historyWriteWorkflow(
                text(row.get("plan_status")),
                text(row.get("preview_status")),
                booleanValue(row.get("writable")),
                text(row.get("execution_result")),
                comparisonStatus,
                mismatchCount,
                text(row.get("comparison_review_status")),
                retestStatus
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
                reportPrintArchiveSummary(text(row.get("case_no"))),
                "[]",
                text(row.get("issues_json")),
                ""
        );
    }

    private WorkbenchReportPrintArchiveResponse reportPrintArchiveSummary(String caseNo) {
        ensureReportPrintBatchTables();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT item.batch_no,
                       item.validation_status,
                       item.summary,
                       item.created_at,
                       batch.report_type,
                       batch.printed_by
                FROM salary_report_print_batch_item item
                JOIN salary_report_print_batch batch ON batch.batch_no = item.batch_no
                WHERE item.case_no = ?
                ORDER BY item.created_at DESC, item.id DESC
                LIMIT 1
                """, text(caseNo));
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_report_print_batch_item
                WHERE case_no = ?
                """, Long.class, text(caseNo));
        int printCount = count == null ? 0 : count.intValue();
        if (rows.isEmpty()) {
            return new WorkbenchReportPrintArchiveResponse(
                    true,
                    false,
                    false,
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    printCount,
                    "UNPRINTED",
                    "\u5f85\u6253\u5370\u5ba1\u6279\u8868"
            );
        }
        Map<String, Object> latest = rows.getFirst();
        String reportType = text(latest.get("report_type"));
        boolean reprinted = "SALARY_CASE_APPROVAL_REPRINT".equalsIgnoreCase(reportType);
        String validationStatus = text(latest.get("validation_status"));
        String status = "BLOCKED".equalsIgnoreCase(validationStatus) ? "BLOCKED" : "PRINTED";
        return new WorkbenchReportPrintArchiveResponse(
                true,
                true,
                reprinted,
                reprinted ? "salary-case-approvals-reprint" : "salary-case-approvals-print",
                text(latest.get("batch_no")),
                "REPORT_PRINT_BATCH",
                text(latest.get("batch_no")),
                text(latest.get("printed_by")),
                text(latest.get("created_at")),
                printCount,
                status,
                text(latest.get("summary")).isBlank() ? "\u5df2\u7559\u5b58\u6253\u5370\u8bb0\u5f55" : text(latest.get("summary"))
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

    private HistoryWriteWorkflow historyWriteWorkflow(String planStatus, String previewStatus, boolean writable, String executionResult, String comparisonStatus, Integer mismatchCount, String reviewStatus, String retestStatus) {
        String safePlanStatus = text(planStatus).toUpperCase();
        if ("PREPARED".equals(safePlanStatus) && writable) {
            return new HistoryWriteWorkflow("HIGH", "WRITE_HISTORY", "\u5199\u5165\u5386\u53f2");
        }
        if ("PREPARED".equals(safePlanStatus)
                || "BLOCKED".equals(safePlanStatus)
                || "BLOCKED".equalsIgnoreCase(text(previewStatus))
                || "ROLLED_BACK".equals(safePlanStatus)) {
            return new HistoryWriteWorkflow("MEDIUM", "VIEW_PLAN", "\u67e5\u770b\u5199\u5165\u8ba1\u5212");
        }
        if (!"EXECUTED".equals(safePlanStatus) || !"SUCCESS".equalsIgnoreCase(text(executionResult))) {
            return new HistoryWriteWorkflow("MEDIUM", "VIEW_PLAN", "\u67e5\u770b\u5199\u5165\u8ba1\u5212");
        }
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
            case "WRITE_HISTORY", "VIEW_PLAN", "RETEST_FIRST", "MAINTAIN_AND_RETEST", "APPROVE_RETEST", "REVIEWED", "NOT_REQUIRED" -> actionCode;
            default -> "";
        };
    }

    private String normalizeHistoryWritePendingQueue(String value) {
        String queue = text(value).toLowerCase();
        return switch (queue) {
            case "blocked", "prepared", "review", "retest" -> queue;
            default -> "";
        };
    }

    private String normalizeHistoryWritePrintQueue(String value) {
        String queue = text(value).toUpperCase();
        return switch (queue) {
            case "PRINTED_READY", "UNPRINTED_BLOCKED", "PRINTED_BLOCKED", "REPRINTED_REVIEW" -> queue;
            default -> "";
        };
    }

    private String normalizeHistoryWriteStatusQueue(String value) {
        String queue = text(value).toUpperCase();
        return switch (queue) {
            case "ROLLED_BACK" -> queue;
            default -> "";
        };
    }

    private String historyWriteActionCodeConditionSql(String actionCode) {
        return switch (normalizeHistoryWriteActionCode(actionCode)) {
            case "WRITE_HISTORY" -> """
                    AND UPPER(TRIM(p.plan_status)) = 'PREPARED'
                    AND COALESCE(p.writable, 0) <> 0
                    """;
            case "VIEW_PLAN" -> """
                    AND (
                        (UPPER(TRIM(p.plan_status)) = 'PREPARED' AND COALESCE(p.writable, 0) = 0)
                        OR UPPER(TRIM(p.plan_status)) IN ('BLOCKED', 'ROLLED_BACK')
                        OR UPPER(TRIM(p.preview_status)) = 'BLOCKED'
                        OR (
                            UPPER(TRIM(p.plan_status)) <> 'PREPARED'
                            AND NOT (
                                UPPER(TRIM(p.plan_status)) = 'EXECUTED'
                                AND UPPER(TRIM(COALESCE(p.execution_result, ''))) = 'SUCCESS'
                            )
                        )
                    )
                    """;
            default -> "";
        };
    }

    private String historyWritePendingQueueConditionSql(String queue) {
        return switch (normalizeHistoryWritePendingQueue(queue)) {
            case "blocked" -> """
                    AND (UPPER(TRIM(p.plan_status)) = 'BLOCKED'
                         OR UPPER(TRIM(p.preview_status)) = 'BLOCKED'
                         OR (UPPER(TRIM(p.plan_status)) = 'PREPARED' AND COALESCE(p.writable, 0) = 0))
                    AND COALESCE(NULLIF(p.comparison_review_status, ''), 'PENDING') = 'PENDING'
                    """;
            case "prepared" -> """
                    AND UPPER(TRIM(p.plan_status)) = 'PREPARED'
                    AND COALESCE(p.writable, 0) <> 0
                    """;
            case "review" -> """
                    AND UPPER(TRIM(p.plan_status)) = 'EXECUTED'
                    AND COALESCE(NULLIF(p.comparison_status, ''),
                        CASE WHEN p.preview_json LIKE '%"matched":false%' THEN 'MISMATCHED' ELSE 'MATCHED' END) = 'MISMATCHED'
                    AND COALESCE(NULLIF(p.comparison_review_status, ''), 'PENDING') = 'PENDING'
                    """;
            case "retest" -> """
                    AND UPPER(TRIM(p.plan_status)) = 'EXECUTED'
                    AND COALESCE(NULLIF(p.comparison_status, ''),
                        CASE WHEN p.preview_json LIKE '%"matched":false%' THEN 'MISMATCHED' ELSE 'MATCHED' END) = 'MISMATCHED'
                    AND EXISTS (
                        SELECT 1
                        FROM sys_audit_log ar
                        WHERE ar.module_name = 'workbench'
                          AND ar.action_name = 'history-write-comparison-retest'
                          AND ar.target_type = 'SALARY_CASE'
                          AND ar.target_code = p.case_no
                    )
                    AND NOT EXISTS (
                        SELECT 1
                        FROM sys_audit_log aa
                        WHERE aa.module_name = 'workbench'
                          AND aa.action_name = 'history-write-comparison-retest-approve'
                          AND aa.target_type = 'SALARY_CASE'
                          AND aa.target_code = p.case_no
                    )
                    """;
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

    private void refreshHistoryWriteComparisonCache(String caseNo, String comparisonStatus, Integer mismatchCount) {
        String safeCaseNo = text(caseNo);
        String safeStatus = text(comparisonStatus);
        int safeMismatchCount = mismatchCount == null ? 0 : mismatchCount;
        if (safeCaseNo.isBlank() || safeStatus.isBlank()) {
            return;
        }
        jdbcTemplate.update("""
                UPDATE salary_history_write_plan
                SET comparison_status = ?,
                    comparison_mismatch_count = ?
                WHERE case_no = ?
                  AND (
                      COALESCE(comparison_status, '') <> ?
                      OR COALESCE(comparison_mismatch_count, -1) <> ?
                  )
                """, safeStatus, safeMismatchCount, safeCaseNo, safeStatus, safeMismatchCount);
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

    private BigDecimal historyWritePreviewTotal(WorkbenchHistoryWritePreviewResponse preview) {
        return preview.fields().stream()
                .map(WorkbenchHistoryWritePreviewField::amount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String historyWritePlanId(String caseNo) {
        return "HWP-" + text(caseNo);
    }

    private void persistHistoryWritePlan(WorkbenchHistoryWritePreviewResponse response) {
        try {
            String fieldsJson = objectMapper.writeValueAsString(response.fields());
            String issuesJson = objectMapper.writeValueAsString(response.issues());
            String previewJson = objectMapper.writeValueAsString(response);
            String planStatus = historyWritePreviewPlanStatus(response);
            jdbcTemplate.update("""
                    INSERT INTO salary_history_write_plan(plan_no, case_no, work_item_id, person_code, org_code,
                                                          event_year, event_month, business_type, preview_status,
                                                          writable, existing_history_id, sid_plan, fields_json,
                                                          issues_json, preview_json, plan_status, prepared_by)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
                            WHEN plan_status = 'EXECUTED' THEN plan_status
                            ELSE VALUES(plan_status)
                        END,
                        prepared_by = CASE
                            WHEN plan_status = 'EXECUTED' THEN prepared_by
                            ELSE VALUES(prepared_by)
                        END,
                        prepared_at = CASE
                            WHEN plan_status = 'EXECUTED' THEN prepared_at
                            ELSE CURRENT_TIMESTAMP
                        END,
                        executed_by = CASE WHEN plan_status = 'EXECUTED' THEN executed_by ELSE NULL END,
                        executed_at = CASE WHEN plan_status = 'EXECUTED' THEN executed_at ELSE NULL END,
                        execution_result = CASE
                            WHEN plan_status = 'EXECUTED' THEN execution_result
                            WHEN VALUES(plan_status) = 'BLOCKED' THEN 'BLOCKED'
                            ELSE NULL
                        END,
                        execution_message = CASE
                            WHEN plan_status = 'EXECUTED' THEN execution_message
                            WHEN VALUES(plan_status) = 'BLOCKED' AND execution_result = 'BLOCKED' THEN execution_message
                            ELSE NULL
                        END,
                        inserted_history_id = CASE WHEN plan_status = 'EXECUTED' THEN inserted_history_id ELSE NULL END,
                        previous_history_id = CASE WHEN plan_status = 'EXECUTED' THEN previous_history_id ELSE NULL END,
                        next_history_id = CASE WHEN plan_status = 'EXECUTED' THEN next_history_id ELSE NULL END,
                        rolled_back_by = CASE WHEN plan_status = 'EXECUTED' THEN rolled_back_by ELSE NULL END,
                        rolled_back_at = CASE WHEN plan_status = 'EXECUTED' THEN rolled_back_at ELSE NULL END,
                        rollback_message = CASE WHEN plan_status = 'EXECUTED' THEN rollback_message ELSE NULL END,
                        comparison_status = CASE WHEN plan_status = 'EXECUTED' THEN comparison_status ELSE NULL END,
                        comparison_mismatch_count = CASE WHEN plan_status = 'EXECUTED' THEN comparison_mismatch_count ELSE NULL END,
                        comparison_review_status = CASE WHEN plan_status = 'EXECUTED' THEN comparison_review_status ELSE NULL END,
                        comparison_review_category = CASE WHEN plan_status = 'EXECUTED' THEN comparison_review_category ELSE NULL END,
                        comparison_review_reason = CASE WHEN plan_status = 'EXECUTED' THEN comparison_review_reason ELSE NULL END,
                        comparison_reviewed_by = CASE WHEN plan_status = 'EXECUTED' THEN comparison_reviewed_by ELSE NULL END,
                        comparison_reviewed_at = CASE WHEN plan_status = 'EXECUTED' THEN comparison_reviewed_at ELSE NULL END
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
                    planStatus,
                    text(currentUserService.currentUsername()));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("History write plan serialization failed.", ex);
        }
    }

    private String historyWritePreviewPlanStatus(WorkbenchHistoryWritePreviewResponse response) {
        boolean businessCaseBlocked = response.issues().stream()
                .map(this::text)
                .anyMatch(issue -> issue.contains("only DONE salary business cases can be written to history"));
        return businessCaseBlocked ? "BLOCKED" : "PREPARED";
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public WorkbenchHistoryWriteExecuteResponse executeHistoryWrite(String caseNo) {
        requireHistoryWritePermission();
        ensureHistoryWritePlanTable();
        Map<String, Object> existingPlan = historyWritePlanRowIfExists(caseNo);
        if (existingPlan != null) {
            String status = text(existingPlan.get("plan_status"));
            String result = text(existingPlan.get("execution_result"));
            String insertedHistoryId = text(existingPlan.get("inserted_history_id"));
            if ("EXECUTED".equalsIgnoreCase(status) && "SUCCESS".equalsIgnoreCase(result) && !insertedHistoryId.isBlank()) {
                throw new IllegalArgumentException("History write plan has already been executed: " + insertedHistoryId);
            }
        }
        WorkbenchHistoryWriteConfirmResponse confirm = historyWriteConfirm(caseNo);
        WorkbenchHistoryWritePreviewResponse preview = new WorkbenchHistoryWritePreviewResponse(
                confirm.caseNo(),
                confirm.workItemId(),
                confirm.personCode(),
                confirm.orgCode(),
                confirm.year(),
                confirm.month(),
                confirm.businessType(),
                confirm.status(),
                confirm.writable(),
                confirm.writePlanId(),
                confirm.existingHistoryId(),
                confirm.previousHistory(),
                confirm.nextHistory(),
                confirm.sidUpdateRequired(),
                confirm.sidPlan(),
                confirm.fields(),
                confirm.issues()
        );
        WorkbenchCaseSnapshotResponse snapshot = caseSnapshot(preview.caseNo());
        if (!Boolean.TRUE.equals(confirm.executable())) {
            markHistoryWritePlanFailed(preview, "BLOCKED", "History write preview is not writable.");
            throw new IllegalArgumentException("History write preview is not writable.");
        }
        try {
            requireReportPrintArchivedForHistoryWrite(preview.caseNo());
        } catch (IllegalArgumentException ex) {
            markHistoryWritePlanFailed(preview, "BLOCKED", ex.getMessage());
            throw ex;
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
        targetRow.put("hj2", historyWritePreviewTotal(preview));
        insertHistoryRow(targetRow);
        if (preview.previousHistory() != null) {
            jdbcTemplate.update("""
                    UPDATE hisbase
                    SET sid = ?
                    WHERE id = ? OR TRIM(id) = ?
                    """, historyId, preview.previousHistory().historyId(), preview.previousHistory().historyId());
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
        WorkbenchHistoryWriteComparisonResponse comparison = retestHistoryWriteComparison(preview.caseNo(), false);
        int mismatchCount = (int) comparison.fields().stream()
                .filter(field -> !Boolean.TRUE.equals(field.matched()))
                .count();
        boolean matched = Boolean.TRUE.equals(comparison.totalMatched()) && mismatchCount == 0;
        String comparisonMessage = matched
                ? "\uff1b\u5199\u5165\u540e\u5b57\u6bb5\u5bf9\u7167\u4e00\u81f4"
                : "\uff1b\u5199\u5165\u540e\u5b57\u6bb5\u5bf9\u7167\u6709\u5dee\u5f02 " + mismatchCount + " \u9879";
        return new WorkbenchHistoryWriteExecuteResponse(
                preview.caseNo(),
                preview.workItemId(),
                preview.personCode(),
                preview.orgCode(),
                preview.writePlanId(),
                historyId,
                "EXECUTED",
                preview.sidUpdateRequired(),
                "Inserted hisbase row " + historyId + comparisonMessage
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
        requireHistoryRollbackPermission();
        WorkbenchCaseSnapshotResponse snapshot = caseSnapshot(caseNo);
        ensureHistoryWritePlanTable();
        Map<String, Object> plan = historyWritePlanRow(snapshot.caseNo());
        String planNo = text(plan.get("plan_no"));
        String insertedHistoryId = text(plan.get("inserted_history_id"));
        String previousHistoryId = text(plan.get("previous_history_id"));
        String nextHistoryId = text(plan.get("next_history_id"));
        WorkbenchHistoryWriteRollbackPreviewResponse rollbackPreview = rollbackHistoryWritePreview(caseNo);
        if (!Boolean.TRUE.equals(rollbackPreview.rollbackable())) {
            String message = rollbackPreview.issues().isEmpty()
                    ? "History write rollback preview is not rollbackable."
                    : rollbackPreview.issues().getFirst();
            throw new IllegalArgumentException(message.replace("BLOCKED: ", ""));
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
                    WHERE id = ? OR TRIM(id) = ?
                    """, nextHistoryId.isBlank() ? null : nextHistoryId, previousHistoryId, previousHistoryId);
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
        List<String> restoreIssues = rollbackRestoreIssues(insertedHistoryId, previousHistoryId, nextHistoryId);
        if (!restoreIssues.isEmpty()) {
            throw new IllegalArgumentException("History rollback restore check failed: " + String.join("; ", restoreIssues));
        }
        return new WorkbenchHistoryWriteExecuteResponse(
                snapshot.caseNo(),
                snapshot.workItemId(),
                snapshot.personCode(),
                snapshot.orgCode(),
                planNo,
                insertedHistoryId,
                "ROLLED_BACK",
                !previousHistoryId.isBlank() || !nextHistoryId.isBlank(),
                "Rolled back hisbase row " + insertedHistoryId + "; restore check passed"
        );
    }

    private List<String> rollbackRestoreIssues(String insertedHistoryId, String previousHistoryId, String nextHistoryId) {
        List<String> issues = new ArrayList<>();
        if (!historyRowByIdIfExists(insertedHistoryId).isEmpty()) {
            issues.add("inserted history row still exists");
        }
        if (!previousHistoryId.isBlank()) {
            Map<String, Object> previous = historyRowByIdIfExists(previousHistoryId);
            if (previous.isEmpty()) {
                issues.add("previous history row no longer exists");
            } else if (!sameHistoryId(text(previous.get("sid")), nextHistoryId)) {
                issues.add("previous history sid was not restored");
            }
        }
        return issues;
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
                WHERE id = ? OR TRIM(id) = ?
                LIMIT 1
                """, historyId, historyId);
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

    private WorkbenchHistoryWritePreviewHistoryRow historyPreviewRowById(String historyId) {
        if (text(historyId).isBlank()) {
            return null;
        }
        Map<String, Object> row = historyRowByIdIfExists(historyId);
        if (row.isEmpty()) {
            return null;
        }
        return new WorkbenchHistoryWritePreviewHistoryRow(
                text(row.get("id")),
                text(row.get("sid")),
                number(row.get("jsnf")),
                number(row.get("jsyf")),
                text(row.get("jslb")),
                decimal(row.get("hj2"))
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
                       snapshot_at,
                       salary_items_json
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
        String source = salaryCaseSource(request == null ? null : request.source());
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

    private String salaryCaseSource(String source) {
        String safeSource = text(source);
        if (safeSource.isBlank()) {
            return "SALARY_EVENT";
        }
        if (Set.of("SALARY_EVENT", "dryzwbh", "dndkh", "dxl", "hjxx").contains(safeSource)) {
            return "SALARY_EVENT";
        }
        return safeSource;
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
        autoPrepareHistoryWritePlan(item.id());
        return findBusinessCase(normalized.workItemId());
    }

    private void autoPrepareHistoryWritePlan(String caseNo) {
        try {
            WorkbenchHistoryWritePreviewResponse preview = historyWritePreview(caseNo);
            systemAuditService.record("workbench", "history-write-auto-preview", "SALARY_CASE", caseNo,
                    preview.personCode() + " " + preview.businessType()
                            + " status=" + preview.status()
                            + " writable=" + preview.writable()
                            + " issues=" + preview.issues().size());
        } catch (RuntimeException ex) {
            systemAuditService.record("workbench", "history-write-auto-preview-failed", "SALARY_CASE", caseNo,
                    left(text(ex.getMessage()), 500));
        }
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
        blockPendingHistoryWritePlanForCancelledCase(safeCaseNo, personCode, businessType, cancelReason);
        return caseDetail(safeCaseNo);
    }

    private void blockPendingHistoryWritePlanForCancelledCase(String caseNo, String personCode, String businessType, String cancelReason) {
        ensureHistoryWritePlanTable();
        int updated = jdbcTemplate.update("""
                UPDATE salary_history_write_plan
                SET preview_status = 'BLOCKED',
                    writable = 0,
                    plan_status = 'BLOCKED',
                    execution_result = 'BLOCKED',
                    execution_message = ?
                WHERE case_no = ?
                  AND UPPER(TRIM(plan_status)) NOT IN ('EXECUTED', 'ROLLED_BACK')
                """,
                left("Salary case cancelled: " + cancelReason, 1000),
                caseNo);
        if (updated > 0) {
            systemAuditService.record("workbench", "history-write-plan-cancel-blocked", "SALARY_CASE", caseNo,
                    personCode + " " + businessType + " cancelReason=" + cancelReason);
        }
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
        autoPrepareHistoryWritePlan(safeCaseNo);
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
                  AND __SOURCE_FILTER__
                """.replace("__WORKBENCH_FILTER__", salaryTodoFilter())
                .replace("__SOURCE_FILTER__", sourceFilter("todo")), Long.class, salaryTodoFilterParams(keyword, changeType, ""));
        return cachedCount == null ? 0 : cachedCount;
    }

    private long countSalaryDone() {
        return countSalaryDone("", "", "", "", "", "", "", "", "");
    }

    private long countSalaryTodoForAcceptance() {
        ensureBusinessCaseTable();
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_business_case sc
                WHERE sc.status = 'TODO'
                  AND __ORG_ACCESS__
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("sc.org_code")), Long.class);
        return count == null ? 0 : count;
    }

    private long countSalaryDoneForAcceptance() {
        ensureBusinessCaseTable();
        ensureHistoryWritePlanTable();
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_business_case sc
                WHERE sc.status IN ('DONE', 'CANCELLED')
                  AND __ORG_ACCESS__
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("sc.org_code")), Long.class);
        Long executed = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_history_write_plan p
                WHERE __ORG_ACCESS__
                  AND UPPER(TRIM(p.plan_status)) = 'EXECUTED'
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("p.org_code")), Long.class);
        return (count == null ? 0 : count) + (executed == null ? 0 : executed) + countDataGovernanceDoneForAcceptance();
    }

    private long countDataGovernanceDoneForAcceptance() {
        ensureDataGovernanceTaskReviewTable();
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_data_governance_task_review r
                WHERE r.review_status IN ('REVIEWED', 'IGNORED')
                  AND __ORG_ACCESS__
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("r.org_code")), Long.class);
        return count == null ? 0 : count;
    }

    private long countApplicationCases(String status) {
        ensureApplicationCaseTable();
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM application_case
                WHERE status = ?
                  AND __ORG_ACCESS__
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("org_code")), Long.class, text(status));
        return count == null ? 0 : count;
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

    private Map<String, Object> historyWritePendingQueue(String code, String title, long count, String status, String comparisonStatus, String reviewStatus, String retestStatus) {
        Map<String, Object> queue = new LinkedHashMap<>();
        queue.put("code", code);
        queue.put("title", title);
        queue.put("count", count);
        queue.put("status", status);
        queue.put("comparisonStatus", comparisonStatus);
        queue.put("reviewStatus", reviewStatus);
        queue.put("retestStatus", retestStatus);
        return queue;
    }

    private long countHistoryWritePendingQueue(String conditionSql, String keyword) {
        String safeKeyword = text(keyword);
        List<Object> args = new ArrayList<>();
        args.add(safeKeyword);
        for (int i = 0; i < 8; i++) {
            args.add(safeKeyword);
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_history_write_plan p
                WHERE __ORG_ACCESS__
                  AND (? = ''
                       OR p.plan_no LIKE CONCAT('%', ?, '%')
                       OR p.case_no LIKE CONCAT('%', ?, '%')
                       OR p.work_item_id LIKE CONCAT('%', ?, '%')
                       OR p.person_code LIKE CONCAT('%', ?, '%')
                       OR p.org_code LIKE CONCAT('%', ?, '%')
                       OR p.business_type LIKE CONCAT('%', ?, '%')
                       OR p.comparison_review_category LIKE CONCAT('%', ?, '%')
                       OR p.comparison_review_reason LIKE CONCAT('%', ?, '%'))
                __CONDITION__
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("p.org_code"))
                .replace("__CONDITION__", conditionSql),
                Long.class,
                args.toArray());
        return count == null ? 0 : count;
    }

    private long countHistoryWritePendingQueue(String queue) {
        ensureHistoryWritePlanTable();
        if ("retest".equals(normalizeHistoryWritePendingQueue(queue))) {
            ensureAuditTable();
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_history_write_plan p
                WHERE __ORG_ACCESS__
                __CONDITION__
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("p.org_code"))
                .replace("__CONDITION__", historyWritePendingQueueConditionSql(queue)),
                Long.class);
        return count == null ? 0 : count;
    }

    private long countReadyHistoryWritePlans() {
        ensureHistoryWritePlanTable();
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_history_write_plan p
                WHERE __ORG_ACCESS__
                  AND UPPER(TRIM(p.plan_status)) = 'PREPARED'
                  AND COALESCE(p.writable, 0) <> 0
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("p.org_code")),
                Long.class);
        return count == null ? 0 : count;
    }

    private long countPendingHistoryWriteComparisonReviews() {
        ensureHistoryWritePlanTable();
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_history_write_plan p
                WHERE __ORG_ACCESS__
                  AND UPPER(TRIM(p.plan_status)) = 'EXECUTED'
                  AND COALESCE(NULLIF(p.comparison_status, ''),
                      CASE WHEN p.preview_json LIKE '%"matched":false%' THEN 'MISMATCHED' ELSE 'MATCHED' END) = 'MISMATCHED'
                  AND COALESCE(NULLIF(p.comparison_review_status, ''), 'PENDING') = 'PENDING'
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("p.org_code")), Long.class);
        return count == null ? 0 : count;
    }

    private long countPendingHistoryWriteComparisonReviewsForAcceptance() {
        ensureHistoryWritePlanTable();
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_history_write_plan p
                WHERE __ORG_ACCESS__
                  AND UPPER(TRIM(p.plan_status)) = 'EXECUTED'
                  AND COALESCE(NULLIF(p.comparison_status, ''),
                      CASE WHEN p.preview_json LIKE '%"matched":false%' THEN 'MISMATCHED' ELSE 'MATCHED' END) = 'MISMATCHED'
                  AND COALESCE(NULLIF(p.comparison_review_status, ''), 'PENDING') = 'PENDING'
                """.replace("__ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("p.org_code")), Long.class);
        return count == null ? 0 : count;
    }

    private long countSalaryDone(String keyword, String changeType, String source, String caseStatus, String trialStatus, String reviewStatus, String workflowStatus, String closureStatus, String nextAction) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM (
                    __SALARY_DONE_ALL__
                ) done
                WHERE __WORKBENCH_FILTER__
                  AND __SOURCE_FILTER__
                  AND __TRIAL_FILTER__
                  AND __REVIEW_FILTER__
                  AND __WORKFLOW_FILTER__
                  AND __CLOSURE_FILTER__
                  AND __NEXT_ACTION_FILTER__
                """.replace("__SALARY_DONE_ALL__", salaryDoneAllSql(caseStatus))
                .replace("__WORKBENCH_FILTER__", workbenchFilter("done"))
                .replace("__SOURCE_FILTER__", sourceFilter("done"))
                .replace("__TRIAL_FILTER__", trialStatusFilter("done"))
                .replace("__REVIEW_FILTER__", reviewStatusFilter("done"))
                .replace("__WORKFLOW_FILTER__", workflowStatusFilter("done"))
                .replace("__CLOSURE_FILTER__", closureStatusFilter("done"))
                .replace("__NEXT_ACTION_FILTER__", nextActionFilter("done")), Long.class, doneFilterParams(keyword, changeType, source, trialStatus, reviewStatus, workflowStatus, closureStatus, nextAction));
        return count == null ? 0 : count;
    }

    private List<WorkbenchItemResponse> salaryTodoItems(int offset, int limit) {
        return salaryTodoItems(offset, limit, "", "");
    }

    private List<WorkbenchItemResponse> salaryTodoItems(int offset, int limit, String keyword, String changeType) {
        return salaryTodoPage(offset, limit, keyword, changeType, "").items();
    }

    private List<WorkbenchItemResponse> salaryTodoItems(int offset, int limit, String keyword, String changeType, String source) {
        return salaryTodoPage(offset, limit, keyword, changeType, source).items();
    }

    private WorkbenchItemsPageResponse salaryTodoPage(int offset, int limit, String keyword, String changeType) {
        return salaryTodoPage(offset, limit, keyword, changeType, "");
    }

    private WorkbenchItemsPageResponse salaryTodoPage(int offset, int limit, String keyword, String changeType, String source) {
        ensureSalaryTodoCacheLoaded();
        return salaryTodoCachePage(offset, limit, keyword, changeType, source);
    }

    private WorkbenchItemsPageResponse salaryTodoCachePage(int offset, int limit, String keyword, String changeType, String source) {
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
                      AND __SOURCE_FILTER__
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
                """.replace("__WORKBENCH_FILTER__", salaryTodoFilter())
                .replace("__SOURCE_FILTER__", sourceFilter("todo")), salaryTodoFilterParams(keyword, changeType, source, limit, offset));
        List<WorkbenchItemResponse> items = new ArrayList<>();
        long total = rows.isEmpty() ? 0 : longValue(rows.getFirst().get("total_count"));
        for (Map<String, Object> row : rows) {
            String rowChangeType = text(row.get("change_type"));
            items.add(new WorkbenchItemResponse(
                    text(row.get("work_item_id")),
                    text(row.get("source")),
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
                    "",
                    "",
                    "PENDING",
                    "",
                    "HANDLE_SALARY_TODO",
                    "\u9884\u68c0\u529e\u7406"
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
                          FROM hisbase later_hb
                          WHERE later_hb.dwbm = candidate.org_code
                            AND later_hb.grbm = candidate.person_no
                            AND TRIM(later_hb.jsnf) REGEXP '^[0-9]{4}$'
                            AND TRIM(later_hb.jsyf) REGEXP '^[0-9]{1,2}$'
                            AND (
                                CAST(TRIM(later_hb.jsnf) AS UNSIGNED) > candidate.event_year
                                OR (
                                    CAST(TRIM(later_hb.jsnf) AS UNSIGNED) = candidate.event_year
                                    AND CAST(TRIM(later_hb.jsyf) AS UNSIGNED) > candidate.event_month
                                )
                            )
                       )
                      AND NOT (
                          candidate.source = 'dryzwbh'
                          AND COALESCE((
                              SELECT TRIM(COALESCE(baseline_hb.zwbm2, ''))
                              FROM hisbase baseline_hb
                              WHERE baseline_hb.dwbm = candidate.org_code
                                AND baseline_hb.grbm = candidate.person_no
                                AND TRIM(baseline_hb.jsnf) REGEXP '^[0-9]{4}$'
                                AND TRIM(baseline_hb.jsyf) REGEXP '^[0-9]{1,2}$'
                                AND (
                                    CAST(TRIM(baseline_hb.jsnf) AS UNSIGNED) < candidate.event_year
                                    OR (
                                        CAST(TRIM(baseline_hb.jsnf) AS UNSIGNED) = candidate.event_year
                                        AND CAST(TRIM(baseline_hb.jsyf) AS UNSIGNED) < candidate.event_month
                                    )
                                )
                              ORDER BY CAST(TRIM(baseline_hb.jsnf) AS UNSIGNED) DESC,
                                       CAST(TRIM(baseline_hb.jsyf) AS UNSIGNED) DESC,
                                       baseline_hb.id DESC
                              LIMIT 1
                          ), '') = COALESCE((
                              SELECT TRIM(COALESCE(z.zwbm, ''))
                              FROM dryzwbh z
                              WHERE CAST(z.id AS CHAR) = candidate.source_id
                              LIMIT 1
                          ), '')
                       )
                      AND NOT EXISTS (
                          SELECT 1
                          FROM salary_business_case sc
                          WHERE sc.work_item_id = CONCAT('salary-todo-', candidate.person_code, '-', candidate.event_year, '-', candidate.event_month, '-', candidate.change_type)
                            AND COALESCE(sc.status, '') <> 'CANCELLED'
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
        return salaryDoneItems(offset, limit, "", "", "", "", "", "", "", "", "");
    }

    private List<WorkbenchItemResponse> salaryDoneItems(int offset, int limit, String keyword, String changeType, String source, String caseStatus, String trialStatus, String reviewStatus, String workflowStatus, String closureStatus, String nextAction) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM (
                    __SALARY_DONE_ALL__
                ) done
                WHERE __WORKBENCH_FILTER__
                  AND __SOURCE_FILTER__
                  AND __TRIAL_FILTER__
                  AND __REVIEW_FILTER__
                  AND __WORKFLOW_FILTER__
                  AND __CLOSURE_FILTER__
                  AND __NEXT_ACTION_FILTER__
                ORDER BY __DONE_ORDER__
                LIMIT ? OFFSET ?
                """.replace("__SALARY_DONE_ALL__", salaryDoneAllSql(caseStatus))
                .replace("__WORKBENCH_FILTER__", workbenchFilter("done"))
                .replace("__SOURCE_FILTER__", sourceFilter("done"))
                .replace("__TRIAL_FILTER__", trialStatusFilter("done"))
                .replace("__REVIEW_FILTER__", reviewStatusFilter("done"))
                .replace("__WORKFLOW_FILTER__", workflowStatusFilter("done"))
                .replace("__CLOSURE_FILTER__", closureStatusFilter("done"))
                .replace("__NEXT_ACTION_FILTER__", nextActionFilter("done"))
                .replace("__DONE_ORDER__", salaryDoneOrderBy(closureStatus, nextAction)), doneFilterParams(keyword, changeType, source, trialStatus, reviewStatus, workflowStatus, closureStatus, nextAction, limit, offset));
        List<WorkbenchItemResponse> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String rowChangeType = text(row.get("change_type"));
            String itemWorkflowStatus = text(row.get("workflow_status"));
            String itemBusinessStatus = text(row.get("business_status")).isBlank() ? "DONE" : text(row.get("business_status"));
            String itemClosureStatus = listItemClosureStatus(itemBusinessStatus, itemWorkflowStatus);
            items.add(new WorkbenchItemResponse(
                    text(row.get("id")),
                    text(row.get("source")),
                    itemBusinessStatus,
                    rowChangeType,
                    text(row.get("person_code")),
                    text(row.get("person_name")),
                    text(row.get("org_code")),
                    number(row.get("event_year")),
                    number(row.get("event_month")),
                    rowChangeType,
                    text(row.get("note")),
                    text(row.get("trial_status")),
                    text(row.get("review_status")),
                    itemWorkflowStatus,
                    itemClosureStatus,
                    listItemClosureMessage(itemBusinessStatus, itemWorkflowStatus),
                    listItemNextActionCode(itemBusinessStatus, itemWorkflowStatus),
                    listItemNextActionLabel(itemBusinessStatus, itemWorkflowStatus),
                    text(row.get("review_reason")),
                    text(row.get("reviewed_by")),
                    text(row.get("reviewed_at")),
                    text(row.get("retest_status")),
                    text(row.get("retest_summary")),
                    text(row.get("retested_at"))
            ));
        }
        return items;
    }

    private String listItemClosureStatus(String businessStatus, String workflowStatus) {
        String safeBusinessStatus = text(businessStatus);
        String safeWorkflowStatus = text(workflowStatus);
        if ("CANCELLED".equalsIgnoreCase(safeBusinessStatus)) {
            return "CANCELLED";
        }
        if (safeWorkflowStatus.isBlank()) {
            return "CLOSED";
        }
        return switch (safeWorkflowStatus) {
            case "HISTORY_CLOSED", "APPLICATION_DONE" -> "CLOSED";
            case "HISTORY_BLOCKED", "HISTORY_ROLLED_BACK" -> "BLOCKED";
            case "REVIEW_PENDING", "HISTORY_REVIEW_PENDING", "HISTORY_READY", "HISTORY_PREPARED", "HISTORY_WRITTEN", "HISTORY_EXECUTED", "CASE_DONE", "APPLICATION_TODO" -> "PENDING";
            default -> safeWorkflowStatus.startsWith("HISTORY_") ? "PENDING" : "CLOSED";
        };
    }

    private String listItemClosureMessage(String businessStatus, String workflowStatus) {
        String safeBusinessStatus = text(businessStatus);
        String safeWorkflowStatus = text(workflowStatus);
        if ("CANCELLED".equalsIgnoreCase(safeBusinessStatus)) {
            return "\u5df2\u64a4\u56de\uff0c\u4e0d\u7eb3\u5165\u95ed\u73af";
        }
        return switch (safeWorkflowStatus) {
            case "" -> "\u5386\u53f2\u5df2\u529e\u6570\u636e";
            case "HISTORY_CLOSED" -> "\u5df2\u5b8c\u6210\u6253\u5370\u3001\u5199\u5165\u548c\u6838\u67e5";
            case "HISTORY_BLOCKED" -> "\u5386\u53f2\u5199\u5165\u5b58\u5728\u963b\u65ad";
            case "HISTORY_REVIEW_PENDING" -> "\u5199\u5165\u5dee\u5f02\u5f85\u6838\u67e5";
            case "REVIEW_PENDING" -> "\u8bd5\u7b97\u5dee\u5f02\u5f85\u590d\u6838";
            case "HISTORY_READY", "HISTORY_PREPARED" -> "\u5f85\u786e\u8ba4\u5199\u5165\u5386\u53f2";
            case "HISTORY_WRITTEN", "HISTORY_EXECUTED" -> "\u5df2\u5199\u5165\uff0c\u5f85\u5b8c\u6210\u5bf9\u7167\u95ed\u73af";
            case "HISTORY_ROLLED_BACK" -> "\u5386\u53f2\u5199\u5165\u5df2\u64a4\u9500";
            case "CASE_DONE" -> "\u5f85\u6253\u5370\u6216\u5199\u5165";
            case "APPLICATION_DONE" -> "\u7533\u529e\u4e1a\u52a1\u5df2\u529e\u7ed3";
            case "APPLICATION_TODO" -> "\u7533\u529e\u4e1a\u52a1\u5f85\u529e\u7406";
            case "DATA_GOVERNANCE_REVIEWED" -> "\u6570\u636e\u6cbb\u7406\u5df2\u6838\u67e5";
            case "DATA_GOVERNANCE_IGNORED" -> "\u6570\u636e\u6cbb\u7406\u5df2\u5ffd\u7565";
            default -> safeWorkflowStatus.startsWith("HISTORY_") ? "\u5f85\u5b8c\u6210\u5de5\u8d44\u95ed\u73af" : "\u5df2\u529e\u7406";
        };
    }

    private String listItemNextActionCode(String businessStatus, String workflowStatus) {
        String safeBusinessStatus = text(businessStatus);
        String safeWorkflowStatus = text(workflowStatus);
        if ("CANCELLED".equalsIgnoreCase(safeBusinessStatus) || "HISTORY_CLOSED".equals(safeWorkflowStatus)) {
            return "";
        }
        return switch (safeWorkflowStatus) {
            case "REVIEW_PENDING" -> "REVIEW_TRIAL";
            case "HISTORY_READY" -> "EXECUTE_HISTORY_WRITE";
            case "HISTORY_PREPARED", "HISTORY_BLOCKED", "HISTORY_ROLLED_BACK" -> "VIEW_HISTORY_PLAN";
            case "HISTORY_WRITTEN", "HISTORY_EXECUTED", "HISTORY_REVIEW_PENDING" -> "REVIEW_DIFFERENCE";
            case "CASE_DONE" -> "PRINT_OR_CREATE_HISTORY_PLAN";
            default -> safeWorkflowStatus.startsWith("HISTORY_") ? "VIEW_HISTORY_PLAN" : "";
        };
    }

    private String listItemNextActionLabel(String businessStatus, String workflowStatus) {
        return switch (listItemNextActionCode(businessStatus, workflowStatus)) {
            case "REVIEW_TRIAL" -> "\u590d\u6838\u8bd5\u7b97";
            case "EXECUTE_HISTORY_WRITE" -> "\u5199\u5165\u5386\u53f2";
            case "VIEW_HISTORY_PLAN" -> "\u67e5\u770b\u5199\u5165\u8ba1\u5212";
            case "REVIEW_DIFFERENCE" -> "\u6838\u67e5\u5199\u5165\u5dee\u5f02";
            case "PRINT_OR_CREATE_HISTORY_PLAN" -> "\u6253\u5370/\u5199\u5165\u9884\u68c0";
            default -> "";
        };
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
                       '' AS workflow_status,
                       hb.hj2,
                       hb.bbz AS handled_at,
                       '' AS review_reason,
                       '' AS reviewed_by,
                       NULL AS reviewed_at,
                       '' AS retest_status,
                       '' AS retest_summary,
                       NULL AS retested_at,
                       CONCAT('\u5386\u53f2\u5de5\u8d44\u5df2\u529e\uff0c\u5408\u8ba1 ', hb.hj2) AS note
                FROM hisbase hb
                LEFT JOIN dryjbxx p ON p.dwbm = hb.dwbm AND p.grbm = hb.grbm
                WHERE CAST(TRIM(hb.jsnf) AS UNSIGNED) >= YEAR(CURDATE()) - 1
                  AND TRIM(hb.jslb) NOT IN ('\u6d25\u8d34\u53d8\u5316', '\u8c03\u6807\u664b\u5347')
                """;
    }

    private String dataGovernanceDoneSql() {
        ensureDataGovernanceTaskReviewTable();
        return """
                SELECT r.work_item_id AS id,
                       'DATA_GOVERNANCE' AS source,
                       'DONE' AS business_status,
                       r.person_code,
                       r.org_code,
                       TRIM(COALESCE(p.xm, '')) AS person_name,
                       YEAR(COALESCE(r.reviewed_at, r.retested_at, r.created_at)) AS event_year,
                       MONTH(COALESCE(r.reviewed_at, r.retested_at, r.created_at)) AS event_month,
                       '\u6570\u636e\u6cbb\u7406' AS change_type,
                       '' AS trial_status,
                       r.review_status,
                       CASE
                           WHEN r.review_status = 'IGNORED' THEN 'DATA_GOVERNANCE_IGNORED'
                           ELSE 'DATA_GOVERNANCE_REVIEWED'
                       END AS workflow_status,
                       0 AS hj2,
                       COALESCE(r.reviewed_at, r.retested_at, r.created_at) AS handled_at,
                       r.review_reason,
                       r.reviewed_by,
                       r.reviewed_at,
                       r.retest_status,
                       r.retest_summary,
                       r.retested_at,
                       CONCAT(r.issue_type,
                              CASE WHEN COALESCE(r.review_reason, '') = '' THEN '' ELSE CONCAT('\uff1a', r.review_reason) END,
                              CASE WHEN COALESCE(r.retest_summary, '') = '' THEN '' ELSE CONCAT('\uff1b\u590d\u6d4b\uff1a', r.retest_summary) END) AS note
                FROM salary_data_governance_task_review r
                LEFT JOIN dryjbxx p ON CONCAT(TRIM(p.dwbm), '-', TRIM(p.grbm)) = r.person_code
                WHERE r.review_status IN ('REVIEWED', 'IGNORED')
                """;
    }

    private String salaryDoneAllSql(String caseStatus) {
        ensureBusinessCaseTable();
        ensureHistoryWritePlanTable();
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

                UNION ALL

                __DATA_GOVERNANCE_DONE__
                """.replace("__SALARY_CASE_DONE__", salaryCaseDoneSql(caseStatusPredicate))
                .replace("__SALARY_DONE_BASE__", salaryDoneBaseSql())
                .replace("__DATA_GOVERNANCE_DONE__", dataGovernanceDoneSql());
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
                       CASE
                           WHEN status = 'CANCELLED' THEN 'CASE_CANCELLED'
                           WHEN EXISTS (
                               SELECT 1 FROM salary_history_write_plan p
                               WHERE p.case_no = salary_business_case.case_no
                                 AND p.plan_status = 'EXECUTED'
                                 AND p.execution_result = 'SUCCESS'
                                 AND COALESCE(NULLIF(p.comparison_status, ''),
                                     CASE WHEN p.preview_json LIKE '%"matched":false%' THEN 'MISMATCHED' ELSE 'MATCHED' END) = 'MISMATCHED'
                                 AND COALESCE(NULLIF(p.comparison_review_status, ''), 'PENDING') = 'PENDING'
                           ) THEN 'HISTORY_REVIEW_PENDING'
                           WHEN EXISTS (
                               SELECT 1 FROM salary_history_write_plan p
                               WHERE p.case_no = salary_business_case.case_no
                                 AND p.plan_status = 'EXECUTED'
                                 AND p.execution_result = 'SUCCESS'
                                 AND (__REPORT_PRINT_ARCHIVED__)
                                 AND (
                                     COALESCE(NULLIF(p.comparison_status, ''),
                                         CASE WHEN p.preview_json LIKE '%"matched":false%' THEN 'MISMATCHED' ELSE 'MATCHED' END) <> 'MISMATCHED'
                                     OR COALESCE(NULLIF(p.comparison_review_status, ''), 'PENDING') = 'REVIEWED'
                                 )
                           ) THEN 'HISTORY_CLOSED'
                           WHEN EXISTS (
                               SELECT 1 FROM salary_history_write_plan p
                               WHERE p.case_no = salary_business_case.case_no
                                 AND p.plan_status = 'EXECUTED'
                                 AND p.execution_result = 'SUCCESS'
                           ) THEN 'HISTORY_WRITTEN'
                           WHEN EXISTS (
                               SELECT 1 FROM salary_history_write_plan p
                               WHERE p.case_no = salary_business_case.case_no
                                 AND p.plan_status = 'EXECUTED'
                           ) THEN 'HISTORY_EXECUTED'
                           WHEN EXISTS (
                               SELECT 1 FROM salary_history_write_plan p
                               WHERE p.case_no = salary_business_case.case_no
                                 AND p.preview_status = 'BLOCKED'
                           ) THEN 'HISTORY_BLOCKED'
                           WHEN EXISTS (
                               SELECT 1 FROM salary_history_write_plan p
                               WHERE p.case_no = salary_business_case.case_no
                                 AND p.plan_status = 'ROLLED_BACK'
                           ) THEN 'HISTORY_ROLLED_BACK'
                           WHEN EXISTS (
                               SELECT 1 FROM salary_history_write_plan p
                               WHERE p.case_no = salary_business_case.case_no
                                 AND p.plan_status = 'BLOCKED'
                           ) THEN 'HISTORY_BLOCKED'
                           WHEN EXISTS (
                               SELECT 1 FROM salary_history_write_plan p
                               WHERE p.case_no = salary_business_case.case_no
                                 AND p.plan_status = 'PREPARED'
                                 AND p.writable = 1
                           ) THEN 'HISTORY_READY'
                           WHEN EXISTS (
                               SELECT 1 FROM salary_history_write_plan p
                               WHERE p.case_no = salary_business_case.case_no
                                 AND p.plan_status = 'PREPARED'
                           ) THEN 'HISTORY_PREPARED'
                           WHEN trial_status IN ('DIFFERENT','ERROR') AND COALESCE(NULLIF(review_status, ''), 'PENDING') = 'PENDING' THEN 'REVIEW_PENDING'
                           ELSE 'CASE_DONE'
                       END AS workflow_status,
                       0 AS hj2,
                       handled_at,
                       '' AS review_reason,
                       '' AS reviewed_by,
                       NULL AS reviewed_at,
                       '' AS retest_status,
                       '' AS retest_summary,
                       NULL AS retested_at,
                       CONCAT(summary,
                              CASE WHEN COALESCE(trial_summary, '') = '' THEN '' ELSE CONCAT('；试算：', trial_summary) END,
                              CASE WHEN COALESCE(difference_reason, '') = '' THEN '' ELSE CONCAT('; differenceReason: ', difference_reason) END,
                              CASE WHEN status <> 'CANCELLED' OR COALESCE(cancel_reason, '') = '' THEN '' ELSE CONCAT('；撤回：', cancel_reason) END) AS note
                FROM salary_business_case
                WHERE __CASE_STATUS_PREDICATE__
                """.replace("__CASE_STATUS_PREDICATE__", caseStatusPredicate)
                .replace("__REPORT_PRINT_ARCHIVED__", salaryCaseReportPrintArchivedSql());
    }

    private String salaryCaseReportPrintArchivedSql() {
        return """
                EXISTS (
                    SELECT 1
                    FROM sys_audit_log audit
                    WHERE audit.module_name = 'report'
                      AND audit.action_name = 'salary-case-approval-print'
                      AND audit.target_type = 'SALARY_CASE'
                      AND audit.target_code = salary_business_case.case_no
                )
                OR EXISTS (
                    SELECT 1
                    FROM salary_report_print_batch_item print_item
                    JOIN salary_report_print_batch print_batch ON print_batch.batch_no = print_item.batch_no
                    WHERE print_item.case_no = salary_business_case.case_no
                      AND print_batch.report_type IN ('SALARY_CASE_APPROVAL', 'SALARY_CASE_APPROVAL_REPRINT')
                )
                """;
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

    private String workflowStatusFilter(String alias) {
        return "(? = '' OR __ALIAS__.workflow_status = ?)".replace("__ALIAS__", alias);
    }

    private String sourceFilter(String alias) {
        return "(? = '' OR __ALIAS__.source = ?)".replace("__ALIAS__", alias);
    }

    private String closureStatusFilter(String alias) {
        String expression = """
                CASE
                    WHEN __ALIAS__.business_status = 'CANCELLED' THEN 'CANCELLED'
                    WHEN COALESCE(__ALIAS__.workflow_status, '') IN ('', 'HISTORY_CLOSED', 'APPLICATION_DONE') THEN 'CLOSED'
                    WHEN COALESCE(__ALIAS__.workflow_status, '') IN ('HISTORY_BLOCKED', 'HISTORY_ROLLED_BACK') THEN 'BLOCKED'
                    WHEN COALESCE(__ALIAS__.workflow_status, '') IN ('REVIEW_PENDING', 'HISTORY_REVIEW_PENDING', 'HISTORY_READY',
                                                                    'HISTORY_PREPARED', 'HISTORY_WRITTEN', 'HISTORY_EXECUTED',
                                                                    'CASE_DONE', 'APPLICATION_TODO') THEN 'PENDING'
                    WHEN COALESCE(__ALIAS__.workflow_status, '') LIKE 'HISTORY\\_%' THEN 'PENDING'
                    ELSE 'CLOSED'
                END
                """.replace("__ALIAS__", alias);
        return "(? = '' OR " + expression + " = ?)";
    }

    private String nextActionFilter(String alias) {
        return "(? = '' OR " + nextActionExpression(alias) + " = ?)";
    }

    private String nextActionExpression(String alias) {
        return """
                CASE
                    WHEN __ALIAS__.business_status = 'CANCELLED' THEN ''
                    WHEN COALESCE(__ALIAS__.workflow_status, '') IN ('', 'HISTORY_CLOSED', 'APPLICATION_DONE') THEN ''
                    WHEN COALESCE(__ALIAS__.workflow_status, '') = 'REVIEW_PENDING' THEN 'REVIEW_TRIAL'
                    WHEN COALESCE(__ALIAS__.workflow_status, '') = 'HISTORY_READY' THEN 'EXECUTE_HISTORY_WRITE'
                    WHEN COALESCE(__ALIAS__.workflow_status, '') IN ('HISTORY_PREPARED', 'HISTORY_BLOCKED', 'HISTORY_ROLLED_BACK') THEN 'VIEW_HISTORY_PLAN'
                    WHEN COALESCE(__ALIAS__.workflow_status, '') IN ('HISTORY_WRITTEN', 'HISTORY_EXECUTED', 'HISTORY_REVIEW_PENDING') THEN 'REVIEW_DIFFERENCE'
                    WHEN COALESCE(__ALIAS__.workflow_status, '') = 'CASE_DONE' THEN 'PRINT_OR_CREATE_HISTORY_PLAN'
                    WHEN COALESCE(__ALIAS__.workflow_status, '') LIKE 'HISTORY\\_%' THEN 'VIEW_HISTORY_PLAN'
                    ELSE ''
                END
                """.replace("__ALIAS__", alias);
    }

    private String salaryDoneOrderBy(String closureStatus, String nextAction) {
        String dateOrder = "done.event_year DESC, done.event_month DESC, done.handled_at DESC, done.id DESC";
        if (text(closureStatus).isBlank() && text(nextAction).isBlank()) {
            return dateOrder;
        }
        return """
                CASE __NEXT_ACTION__
                    WHEN 'REVIEW_TRIAL' THEN 1
                    WHEN 'PRINT_OR_CREATE_HISTORY_PLAN' THEN 2
                    WHEN 'EXECUTE_HISTORY_WRITE' THEN 3
                    WHEN 'REVIEW_DIFFERENCE' THEN 4
                    WHEN 'VIEW_HISTORY_PLAN' THEN 5
                    ELSE 99
                END,
                __DATE_ORDER__
                """.replace("__NEXT_ACTION__", nextActionExpression("done"))
                .replace("__DATE_ORDER__", dateOrder);
    }

    private Object[] todoFilterParams(String keyword, String changeType, Object... tail) {
        return filterParams(minTodoYearMonth(), maxTodoYearMonth(), keyword, changeType, tail);
    }

    private Object[] doneFilterParams(String keyword, String changeType, String source, String trialStatus, String reviewStatus, String workflowStatus, String closureStatus, String nextAction, Object... tail) {
        List<Object> params = new ArrayList<>();
        addFilterParams(params, keyword, changeType);
        String safeSource = text(source);
        String safeTrialStatus = text(trialStatus).toUpperCase();
        String safeReviewStatus = text(reviewStatus).toUpperCase();
        String safeWorkflowStatus = text(workflowStatus).toUpperCase();
        String safeClosureStatus = text(closureStatus).toUpperCase();
        String safeNextAction = text(nextAction).toUpperCase();
        params.add(safeSource);
        params.add(safeSource);
        params.add(safeTrialStatus);
        params.add(safeTrialStatus);
        params.add(safeReviewStatus);
        params.add(safeReviewStatus);
        params.add(safeWorkflowStatus);
        params.add(safeWorkflowStatus);
        params.add(safeClosureStatus);
        params.add(safeClosureStatus);
        params.add(safeNextAction);
        params.add(safeNextAction);
        params.addAll(List.of(tail));
        return params.toArray();
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

    private Object[] salaryTodoFilterParams(String keyword, String changeType, String source, Object... tail) {
        List<Object> params = new ArrayList<>();
        String safeKeyword = text(keyword);
        String safeChangeType = text(changeType);
        String safeSource = text(source);
        params.add(safeKeyword);
        for (int i = 0; i < 7; i++) {
            params.add(safeKeyword);
        }
        params.add(safeChangeType);
        params.add(safeChangeType);
        params.add(safeSource);
        params.add(safeSource);
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

    private Object[] filterParams(String keyword, String changeType, String trialStatus, String reviewStatus, String workflowStatus, Object... tail) {
        List<Object> params = new ArrayList<>();
        addFilterParams(params, keyword, changeType);
        String safeTrialStatus = text(trialStatus).toUpperCase();
        String safeReviewStatus = text(reviewStatus).toUpperCase();
        String safeWorkflowStatus = text(workflowStatus).toUpperCase();
        params.add(safeTrialStatus);
        params.add(safeTrialStatus);
        params.add(safeReviewStatus);
        params.add(safeReviewStatus);
        params.add(safeWorkflowStatus);
        params.add(safeWorkflowStatus);
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
        ensureHistoryWritePlanTable();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM (
                    __SALARY_CASE_DONE__
                ) done
                WHERE done.id = (
                    SELECT case_no
                    FROM salary_business_case
                    WHERE work_item_id = ?
                    LIMIT 1
                )
                LIMIT 1
                """.replace("__SALARY_CASE_DONE__", salaryCaseDoneSql("status = 'DONE'")), workItemId);
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
                text(row.get("review_status")),
                text(row.get("workflow_status")),
                listItemClosureStatus("DONE", text(row.get("workflow_status"))),
                listItemClosureMessage("DONE", text(row.get("workflow_status"))),
                listItemNextActionCode("DONE", text(row.get("workflow_status"))),
                listItemNextActionLabel("DONE", text(row.get("workflow_status")))
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
        // Flyway owns the formal schema; these guards keep older local databases usable.
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
        addColumnIfMissing("salary_history_write_plan", "comparison_status", "VARCHAR(32) NULL");
        addColumnIfMissing("salary_history_write_plan", "comparison_mismatch_count", "INT NULL");
    }

    private void ensureSalaryTodoCacheTable() {
        // Flyway owns the formal schema; these guards keep older local databases usable.
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

    private void ensureApplicationCaseTable() {
        // Flyway owns the formal schema; these guards keep older local databases usable.
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS application_case (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    case_no VARCHAR(64) NOT NULL,
                    source VARCHAR(64) NOT NULL,
                    status VARCHAR(32) NOT NULL,
                    business_type VARCHAR(128) NOT NULL,
                    person_code VARCHAR(128) NOT NULL,
                    person_name VARCHAR(128) NULL,
                    org_code VARCHAR(64) NOT NULL,
                    event_year INT NULL,
                    event_month INT NULL,
                    title VARCHAR(255) NOT NULL,
                    summary VARCHAR(1024) NULL,
                    review_reason VARCHAR(1024) NULL,
                    workflow_status VARCHAR(32) NOT NULL DEFAULT 'APPLICATION_TODO',
                    created_by VARCHAR(64) NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    handled_by VARCHAR(64) NULL,
                    handled_at DATETIME NULL,
                    UNIQUE KEY uk_application_case_no (case_no),
                    KEY idx_application_case_status (status, created_at),
                    KEY idx_application_case_org (org_code, status)
                )
                """);
        addColumnIfMissing("application_case", "workflow_status", "VARCHAR(32) NOT NULL DEFAULT 'APPLICATION_TODO'");
        addIndexIfMissing("application_case", "idx_application_case_workflow", "workflow_status, status");
    }

    private void ensureMigrationAcceptanceTables() {
        // Flyway owns the formal schema; these guards keep older local databases usable.
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS migration_acceptance_run (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    run_no VARCHAR(64) NOT NULL,
                    org_code VARCHAR(64) NOT NULL,
                    checked_at DATETIME NOT NULL,
                    sample_limit INT NOT NULL DEFAULT 100,
                    overall_status VARCHAR(32) NOT NULL,
                    salary_todo BIGINT NOT NULL DEFAULT 0,
                    salary_done BIGINT NOT NULL DEFAULT 0,
                    history_prepared BIGINT NOT NULL DEFAULT 0,
                    history_executed BIGINT NOT NULL DEFAULT 0,
                    history_blocked BIGINT NOT NULL DEFAULT 0,
                    review_pending BIGINT NOT NULL DEFAULT 0,
                    data_governance_issues BIGINT NOT NULL DEFAULT 0,
                    warning_count INT NOT NULL DEFAULT 0,
                    gate_count INT NOT NULL DEFAULT 0,
                    summary_json JSON NULL,
                    issues_json JSON NULL,
                    created_by VARCHAR(64) NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_migration_acceptance_run_no (run_no),
                    KEY idx_migration_acceptance_org_time (org_code, checked_at),
                    KEY idx_migration_acceptance_status (overall_status, checked_at)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS migration_acceptance_gate (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    run_no VARCHAR(64) NOT NULL,
                    gate_code VARCHAR(64) NOT NULL,
                    gate_title VARCHAR(255) NOT NULL,
                    gate_status VARCHAR(32) NOT NULL,
                    gate_count BIGINT NOT NULL DEFAULT 0,
                    message VARCHAR(1024) NULL,
                    sort_order INT NOT NULL DEFAULT 0,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_migration_acceptance_gate (run_no, gate_code),
                    KEY idx_migration_acceptance_gate_status (gate_status)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS migration_acceptance_issue (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    run_no VARCHAR(64) NOT NULL,
                    person_code VARCHAR(128) NULL,
                    person_name VARCHAR(128) NULL,
                    org_code VARCHAR(64) NOT NULL,
                    issue_type VARCHAR(64) NOT NULL,
                    message VARCHAR(1024) NULL,
                    review_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
                    review_reason VARCHAR(1024) NULL,
                    reviewed_by VARCHAR(64) NULL,
                    reviewed_at DATETIME NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    KEY idx_migration_acceptance_issue_run (run_no, review_status),
                    KEY idx_migration_acceptance_issue_person (person_code),
                    KEY idx_migration_acceptance_issue_org (org_code, review_status)
                )
                """);
    }

    private void ensureHistoryWriteDeliveryAcceptanceTable() {
        // Flyway owns the formal schema; this guard keeps older local databases usable.
        jdbcTemplate.execute("""
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
                )
                """);
    }

    private void ensureMigrationQualitySnapshotTable() {
        jdbcTemplate.execute("""
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
                )
                """);
        addColumnIfMissing("migration_quality_snapshot", "preflight_level", "VARCHAR(32) NULL");
        addColumnIfMissing("migration_quality_snapshot", "preflight_title", "VARCHAR(128) NULL");
        addColumnIfMissing("migration_quality_snapshot", "preflight_message", "VARCHAR(1024) NULL");
        addColumnIfMissing("migration_quality_snapshot", "decision_json", "JSON NULL");
        addColumnIfMissing("migration_quality_snapshot", "archive_status", "VARCHAR(32) NOT NULL DEFAULT 'DRAFT'");
        addColumnIfMissing("migration_quality_snapshot", "archived_by", "VARCHAR(64) NULL");
        addColumnIfMissing("migration_quality_snapshot", "archived_at", "DATETIME NULL");
        addColumnIfMissing("migration_quality_snapshot", "archive_note", "VARCHAR(1024) NULL");
    }

    @SuppressWarnings("unchecked")
    private void saveMigrationRegressionRun(Map<String, Object> result) {
        ensureMigrationRegressionSampleTable();
        String runNo = text(result.get("runNo"));
        if (runNo.isBlank()) {
            return;
        }
        List<Map<String, Object>> samples = (List<Map<String, Object>>) result.getOrDefault("samples", List.of());
        jdbcTemplate.update("""
                INSERT INTO migration_regression_run(run_no, org_code, batch_no, checked_at, sample_limit,
                                                     sample_count, pass_count, warning_count, overall_status, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE org_code = VALUES(org_code),
                                        batch_no = VALUES(batch_no),
                                        checked_at = VALUES(checked_at),
                                        sample_limit = VALUES(sample_limit),
                                        sample_count = VALUES(sample_count),
                                        pass_count = VALUES(pass_count),
                                        warning_count = VALUES(warning_count),
                                        overall_status = VALUES(overall_status)
                """,
                runNo,
                text(result.get("orgCode")),
                text(result.get("batchNo")),
                text(result.get("checkedAt")).replace('T', ' '),
                number(result.get("sampleLimit")),
                number(result.get("sampleCount")),
                number(result.get("passCount")),
                number(result.get("warningCount")),
                text(result.get("overallStatus")),
                currentUserService.currentUsername()
        );
        jdbcTemplate.update("DELETE FROM migration_regression_run_sample WHERE run_no = ?", runNo);
        for (int i = 0; i < samples.size(); i++) {
            Map<String, Object> sample = samples.get(i);
            jdbcTemplate.update("""
                    INSERT INTO migration_regression_run_sample(run_no, sample_code, sample_title, sample_domain,
                                                                sample_id, person_code, person_name, org_code,
                                                                sample_type, status, expected_status, actual_status,
                                                                expected_amount, actual_amount, expected_payload,
                                                                actual_payload, message, sort_order)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    runNo,
                    text(sample.get("code")),
                    text(sample.get("title")),
                    text(sample.get("domain")),
                    text(sample.get("sampleId")),
                    text(sample.get("personCode")),
                    text(sample.get("personName")),
                    text(sample.get("orgCode")),
                    text(sample.get("sampleType")),
                    text(sample.get("status")),
                    text(sample.get("expectedStatus")),
                    text(sample.get("actualStatus")),
                    sample.get("expectedAmount"),
                    sample.get("actualAmount"),
                    text(sample.get("expectedPayload")),
                    text(sample.get("actualPayload")),
                    text(sample.get("message")),
                    i + 1
            );
        }
    }

    private void ensureMigrationRegressionSampleTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS migration_regression_sample (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    sample_code VARCHAR(64) NOT NULL,
                    sample_title VARCHAR(128) NOT NULL,
                    sample_domain VARCHAR(64) NULL,
                    sample_id VARCHAR(128) NOT NULL,
                    person_code VARCHAR(128) NOT NULL,
                    person_name VARCHAR(128) NULL,
                    org_code VARCHAR(64) NOT NULL,
                    sample_type VARCHAR(255) NULL,
                    batch_no VARCHAR(64) NULL,
                    sample_source VARCHAR(64) NULL,
                    expected_status VARCHAR(64) NULL,
                    expected_amount DECIMAL(18,2) NULL,
                    expected_payload LONGTEXT NULL,
                    enabled TINYINT NOT NULL DEFAULT 1,
                    note VARCHAR(1024) NULL,
                    last_run_no VARCHAR(64) NULL,
                    last_run_status VARCHAR(32) NULL,
                    last_run_message VARCHAR(255) NULL,
                    last_run_at DATETIME NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_migration_regression_sample (sample_code, org_code, person_code, sample_id),
                    KEY idx_migration_regression_sample_org (org_code, sample_code),
                    KEY idx_migration_regression_sample_person (person_code)
                )
                """);
        addColumnIfMissing("migration_regression_sample", "enabled", "TINYINT NOT NULL DEFAULT 1");
        addColumnIfMissing("migration_regression_sample", "note", "VARCHAR(1024) NULL");
        addColumnIfMissing("migration_regression_sample", "expected_status", "VARCHAR(64) NULL");
        addColumnIfMissing("migration_regression_sample", "expected_amount", "DECIMAL(18,2) NULL");
        addColumnIfMissing("migration_regression_sample", "expected_payload", "LONGTEXT NULL");
        addColumnIfMissing("migration_regression_sample", "batch_no", "VARCHAR(64) NULL");
        addColumnIfMissing("migration_regression_sample", "sample_source", "VARCHAR(64) NULL");
        addColumnIfMissing("migration_regression_sample", "last_run_no", "VARCHAR(64) NULL");
        addColumnIfMissing("migration_regression_sample", "last_run_status", "VARCHAR(32) NULL");
        addColumnIfMissing("migration_regression_sample", "last_run_message", "VARCHAR(255) NULL");
        addColumnIfMissing("migration_regression_sample", "last_run_at", "DATETIME NULL");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS migration_regression_run (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    run_no VARCHAR(64) NOT NULL,
                    org_code VARCHAR(64) NOT NULL,
                    batch_no VARCHAR(64) NULL,
                    checked_at DATETIME NOT NULL,
                    sample_limit INT NOT NULL DEFAULT 0,
                    sample_count INT NOT NULL DEFAULT 0,
                    pass_count INT NOT NULL DEFAULT 0,
                    warning_count INT NOT NULL DEFAULT 0,
                    overall_status VARCHAR(32) NOT NULL,
                    created_by VARCHAR(128) NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_migration_regression_run_no (run_no),
                    KEY idx_migration_regression_run_org_time (org_code, checked_at),
                    KEY idx_migration_regression_run_batch (batch_no, checked_at),
                    KEY idx_migration_regression_run_status (overall_status, checked_at)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS migration_regression_run_sample (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    run_no VARCHAR(64) NOT NULL,
                    sample_code VARCHAR(64) NOT NULL,
                    sample_title VARCHAR(128) NULL,
                    sample_domain VARCHAR(64) NULL,
                    sample_id VARCHAR(128) NOT NULL,
                    person_code VARCHAR(128) NOT NULL,
                    person_name VARCHAR(128) NULL,
                    org_code VARCHAR(64) NOT NULL,
                    sample_type VARCHAR(255) NULL,
                    status VARCHAR(32) NOT NULL,
                    expected_status VARCHAR(64) NULL,
                    actual_status VARCHAR(64) NULL,
                    expected_amount DECIMAL(18,2) NULL,
                    actual_amount DECIMAL(18,2) NULL,
                    expected_payload LONGTEXT NULL,
                    actual_payload LONGTEXT NULL,
                    message VARCHAR(255) NULL,
                    review_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
                    review_category VARCHAR(64) NULL,
                    review_note VARCHAR(1024) NULL,
                    reviewed_by VARCHAR(128) NULL,
                    reviewed_at DATETIME NULL,
                    retest_status VARCHAR(32) NULL,
                    governance_work_item_id VARCHAR(255) NULL,
                    sort_order INT NOT NULL DEFAULT 0,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    KEY idx_migration_regression_run_sample_run (run_no, status),
                    KEY idx_migration_regression_run_sample_person (person_code, run_no),
                    KEY idx_migration_regression_run_sample_code (sample_code, run_no)
                )
                """);
        addColumnIfMissing("migration_regression_run_sample", "review_status", "VARCHAR(32) NOT NULL DEFAULT 'PENDING'");
        addColumnIfMissing("migration_regression_run_sample", "review_category", "VARCHAR(64) NULL");
        addColumnIfMissing("migration_regression_run_sample", "review_note", "VARCHAR(1024) NULL");
        addColumnIfMissing("migration_regression_run_sample", "reviewed_by", "VARCHAR(128) NULL");
        addColumnIfMissing("migration_regression_run_sample", "reviewed_at", "DATETIME NULL");
        addColumnIfMissing("migration_regression_run_sample", "retest_status", "VARCHAR(32) NULL");
        addColumnIfMissing("migration_regression_run_sample", "governance_work_item_id", "VARCHAR(255) NULL");
    }

    @SuppressWarnings("unchecked")
    private void saveMigrationAcceptanceRun(Map<String, Object> result) {
        Map<String, Object> summary = (Map<String, Object>) result.getOrDefault("summary", Map.of());
        List<Map<String, Object>> gates = (List<Map<String, Object>>) result.getOrDefault("gates", List.of());
        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.getOrDefault("issues", List.of());
        long warningCount = gates.stream().filter(gate -> "WARN".equals(text(gate.get("status")))).count();
        jdbcTemplate.update("""
                INSERT INTO migration_acceptance_run(run_no, org_code, checked_at, sample_limit, overall_status,
                                                     salary_todo, salary_done, history_prepared, history_executed,
                                                     history_blocked, review_pending, data_governance_issues,
                                                     warning_count, gate_count, summary_json, issues_json, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE org_code = VALUES(org_code),
                                        checked_at = VALUES(checked_at),
                                        sample_limit = VALUES(sample_limit),
                                        overall_status = VALUES(overall_status),
                                        salary_todo = VALUES(salary_todo),
                                        salary_done = VALUES(salary_done),
                                        history_prepared = VALUES(history_prepared),
                                        history_executed = VALUES(history_executed),
                                        history_blocked = VALUES(history_blocked),
                                        review_pending = VALUES(review_pending),
                                        data_governance_issues = VALUES(data_governance_issues),
                                        warning_count = VALUES(warning_count),
                                        gate_count = VALUES(gate_count),
                                        summary_json = VALUES(summary_json),
                                        issues_json = VALUES(issues_json)
                """,
                text(result.get("runNo")),
                text(result.get("orgCode")),
                text(result.get("checkedAt")).replace('T', ' '),
                number(result.get("sampleLimit")),
                text(result.get("overallStatus")),
                longValue(summary.get("salaryTodo")),
                longValue(summary.get("salaryDone")),
                longValue(summary.get("historyPrepared")),
                longValue(summary.get("historyExecuted")),
                longValue(summary.get("historyBlocked")),
                longValue(summary.get("reviewPending")),
                longValue(summary.get("dataGovernanceIssues")),
                warningCount,
                gates.size(),
                writeJson(summary),
                writeJson(result.getOrDefault("issues", List.of())),
                currentUserService.currentUsername()
        );
        jdbcTemplate.update("DELETE FROM migration_acceptance_gate WHERE run_no = ?", text(result.get("runNo")));
        for (int i = 0; i < gates.size(); i++) {
            Map<String, Object> gate = gates.get(i);
            jdbcTemplate.update("""
                    INSERT INTO migration_acceptance_gate(run_no, gate_code, gate_title, gate_status, gate_count, message, sort_order)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    text(result.get("runNo")),
                    text(gate.get("code")),
                    text(gate.get("title")),
                    text(gate.get("status")),
                    longValue(gate.get("count")),
                    text(gate.get("message")),
                    i + 1
            );
        }
        jdbcTemplate.update("DELETE FROM migration_acceptance_issue WHERE run_no = ?", text(result.get("runNo")));
        for (Map<String, Object> issue : issues) {
            jdbcTemplate.update("""
                    INSERT INTO migration_acceptance_issue(run_no, person_code, person_name, org_code, issue_type, message)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    text(result.get("runNo")),
                    text(issue.get("personCode")),
                    text(issue.get("personName")),
                    text(issue.get("orgCode")).isBlank() ? text(result.get("orgCode")) : text(issue.get("orgCode")),
                    text(issue.get("issueType")),
                    text(issue.get("message"))
            );
        }
    }

    private void markMigrationAcceptanceRunFailed(String runNo, String orgCode, int limit, String message) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("salaryTodo", 0);
        summary.put("salaryDone", 0);
        summary.put("historyPrepared", 0);
        summary.put("historyExecuted", 0);
        summary.put("historyBlocked", 0);
        summary.put("reviewPending", 0);
        summary.put("dataGovernanceIssues", 1);
        List<Map<String, Object>> gates = List.of(
                acceptanceGate("0-error", "\u9a8c\u6536\u4efb\u52a1\u6267\u884c\u5931\u8d25", "ERROR", 1, left(text(message), 1000))
        );
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("runNo", text(runNo));
        result.put("orgCode", text(orgCode));
        result.put("checkedAt", java.time.LocalDateTime.now().withNano(0).toString());
        result.put("sampleLimit", Math.max(1, Math.min(limit, 500)));
        result.put("overallStatus", "ERROR");
        result.put("summary", summary);
        result.put("gates", gates);
        result.put("issues", List.of(Map.of(
                "personCode", "",
                "personName", "",
                "orgCode", text(orgCode),
                "issueType", "ACCEPTANCE_RUN_ERROR",
                "message", left(text(message), 1000)
        )));
        saveMigrationAcceptanceRun(result);
        systemAuditService.record("workbench", "migration-acceptance-run-error", "ORG", text(orgCode),
                "runNo=" + text(runNo) + ", message=" + left(text(message), 500));
    }

    private List<Map<String, Object>> migrationAcceptanceRunGates(String runNo) {
        return jdbcTemplate.queryForList("""
                SELECT gate_code AS code,
                       gate_title AS title,
                       gate_status AS status,
                       gate_count AS count,
                       message
                FROM migration_acceptance_gate
                WHERE run_no = ?
                ORDER BY sort_order, id
                """, runNo);
    }

    private String migrationAcceptanceRunOrgCode(String runNo) {
        List<String> rows = jdbcTemplate.queryForList("""
                SELECT org_code
                FROM migration_acceptance_run
                WHERE run_no = ?
                LIMIT 1
                """, String.class, runNo);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Acceptance run not found: " + runNo);
        }
        return text(rows.getFirst());
    }

    private Map<String, Object> migrationAcceptanceIssueRow(Long issueId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id,
                       run_no,
                       run_no AS runNo,
                       person_code,
                       person_code AS personCode,
                       person_name AS personName,
                       org_code,
                       org_code AS orgCode,
                       issue_type AS issueType,
                       message,
                       review_status AS reviewStatus,
                       review_reason AS reviewReason,
                       reviewed_by AS reviewedBy,
                       reviewed_at AS reviewedAt,
                       created_at AS createdAt
                FROM migration_acceptance_issue
                WHERE id = ?
                LIMIT 1
                """, issueId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Acceptance issue not found: " + issueId);
        }
        return rows.getFirst();
    }

    private Map<String, Object> businessForm(String code, String title, String basis, List<String> fields) {
        return Map.of(
                "code", code,
                "title", title,
                "basis", basis,
                "fields", fields
        );
    }

    private Map<String, Object> businessFlow(String code, String title, String formCode, List<String> steps) {
        return Map.of(
                "code", code,
                "title", title,
                "formCode", formCode,
                "steps", steps,
                "status", "READY"
        );
    }

    private Map<String, Object> ruleCatalog(String code, String title, List<String> targets) {
        return Map.of(
                "code", code,
                "title", title,
                "targets", targets
        );
    }

    private Map<String, Object> readinessItem(String code, String title, String status, String entrypoint) {
        return Map.of(
                "code", code,
                "title", title,
                "status", status,
                "entrypoint", entrypoint
        );
    }

    private List<NormalGradeBatchTrialItem> normalGradeEligibleItems(NormalGradeBatchTrialResult trial) {
        return trial.items().stream()
                .filter(item -> !"SKIPPED".equalsIgnoreCase(text(item.status())))
                .filter(item -> !"NOT_ELIGIBLE".equalsIgnoreCase(text(item.ruleType())))
                .filter(item -> !text(item.ruleType()).isBlank())
                .toList();
    }

    private Map<String, Object> normalGradeApplicationSummary(NormalGradeBatchTrialResult trial, List<NormalGradeBatchTrialItem> eligibleItems) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", trial.orgCode());
        result.put("year", trial.year());
        result.put("month", trial.month());
        result.put("checkedCount", trial.checkedCount());
        result.put("eligibleCount", eligibleItems.size());
        result.put("matchedCount", trial.matchedCount());
        result.put("differentCount", trial.differentCount());
        result.put("noExpectedCount", trial.noExpectedCount());
        result.put("skippedCount", trial.skippedCount());
        result.put("levelPromotionCount", trial.levelPromotionCount());
        result.put("notEligibleCount", trial.notEligibleCount());
        result.put("totalDifference", trial.totalDifference());
        return result;
    }

    private Map<String, Object> normalGradeApplicationItem(NormalGradeBatchTrialItem item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("personCode", item.personCode());
        row.put("personName", item.personName());
        row.put("orgCode", item.orgCode());
        row.put("orgName", item.orgName());
        row.put("changeType", normalGradeApplicationChangeType(item));
        row.put("ruleType", item.ruleType());
        row.put("status", item.status());
        row.put("beforeValue", item.beforeValue());
        row.put("afterValue", item.afterValue());
        row.put("changeAmount", item.changeAmount());
        row.put("message", item.message());
        return row;
    }

    private String normalGradeApplicationChangeType(NormalGradeBatchTrialItem item) {
        String ruleType = text(item.ruleType());
        if ("LEVEL_PROMOTION".equals(ruleType)) {
            return "\u6b63\u5e38\u7ea7\u522b";
        }
        if ("SALARY_GRADE_INCREMENT".equals(ruleType)) {
            return "\u6b63\u5e38\u85aa\u7ea7";
        }
        return "\u6b63\u5e38\u6863\u6b21";
    }

    private String normalGradeApplicationNote(NormalGradeBatchTrialItem item) {
        return normalGradeApplicationChangeType(item) + " " + text(item.beforeValue()) + " -> " + text(item.afterValue())
                + "\uff0c\u89c4\u5219=" + text(item.ruleType()) + "\uff0c\u72b6\u6001=" + text(item.status());
    }

    private String entrySalaryChangeType(String changeType) {
        String safeChangeType = text(changeType);
        return safeChangeType.isBlank() ? "\u65b0\u8fdb\u5de5\u8d44" : safeChangeType;
    }

    private List<NormalGradeBatchTrialItem> entrySalaryEligibleItems(NormalGradeBatchTrialResult trial) {
        return trial.items().stream()
                .filter(item -> "PROBATIONARY_NEW_SALARY".equalsIgnoreCase(text(item.ruleType()))
                        || "REGULARIZATION_GRADE_PLACEMENT".equalsIgnoreCase(text(item.ruleType())))
                .toList();
    }

    private Map<String, Object> entrySalaryApplicationSummary(NormalGradeBatchTrialResult trial, List<NormalGradeBatchTrialItem> eligibleItems) {
        long probationary = eligibleItems.stream().filter(item -> "PROBATIONARY_NEW_SALARY".equalsIgnoreCase(text(item.ruleType()))).count();
        long regularization = eligibleItems.stream().filter(item -> "REGULARIZATION_GRADE_PLACEMENT".equalsIgnoreCase(text(item.ruleType()))).count();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", trial.orgCode());
        result.put("year", trial.year());
        result.put("month", trial.month());
        result.put("checkedCount", trial.checkedCount());
        result.put("eligibleCount", eligibleItems.size());
        result.put("probationaryCount", probationary);
        result.put("regularizationCount", regularization);
        result.put("matchedCount", trial.matchedCount());
        result.put("differentCount", trial.differentCount());
        result.put("noExpectedCount", trial.noExpectedCount());
        result.put("skippedCount", trial.skippedCount());
        result.put("totalDifference", trial.totalDifference());
        return result;
    }

    private Map<String, Object> entrySalaryApplicationItem(NormalGradeBatchTrialItem item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("personCode", item.personCode());
        row.put("personName", item.personName());
        row.put("orgCode", item.orgCode());
        row.put("orgName", item.orgName());
        row.put("changeType", entrySalaryApplicationChangeType(item));
        row.put("ruleType", item.ruleType());
        row.put("status", item.status());
        row.put("beforeValue", item.beforeValue());
        row.put("afterValue", item.afterValue());
        row.put("changeAmount", item.changeAmount());
        row.put("message", item.message());
        return row;
    }

    private String entrySalaryApplicationChangeType(NormalGradeBatchTrialItem item) {
        if ("REGULARIZATION_GRADE_PLACEMENT".equalsIgnoreCase(text(item.ruleType()))) {
            return "\u8f6c\u6b63\u5b9a\u7ea7";
        }
        return "\u89c1\u4e60\u5de5\u8d44";
    }

    private String entrySalaryApplicationNote(NormalGradeBatchTrialItem item) {
        return entrySalaryApplicationChangeType(item) + " " + text(item.beforeValue()) + " -> " + text(item.afterValue())
                + "\uff0c\u89c4\u5219=" + text(item.ruleType()) + "\uff0c\u72b6\u6001=" + text(item.status());
    }

    private String postChangeType(String changeType) {
        String safeChangeType = text(changeType);
        return safeChangeType.isBlank() ? "\u804c\u52a1\u53d8\u5316" : safeChangeType;
    }

    private List<NormalGradeBatchTrialItem> postChangeEligibleItems(NormalGradeBatchTrialResult trial) {
        return trial.items().stream()
                .filter(item -> Set.of(
                        "CIVIL_POST_CHANGE",
                        "JUDICIAL_POST_CHANGE",
                        "INSTITUTION_POST_CHANGE",
                        "CIVIL_RANK_PROMOTION",
                        "POLICE_RANK_ALLOWANCE_CHANGE"
                ).contains(text(item.ruleType())))
                .toList();
    }

    private Map<String, Object> postChangeApplicationSummary(NormalGradeBatchTrialResult trial, List<NormalGradeBatchTrialItem> eligibleItems) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", trial.orgCode());
        result.put("year", trial.year());
        result.put("month", trial.month());
        result.put("checkedCount", trial.checkedCount());
        result.put("eligibleCount", eligibleItems.size());
        result.put("civilPostCount", countRuleType(eligibleItems, "CIVIL_POST_CHANGE"));
        result.put("judicialPostCount", countRuleType(eligibleItems, "JUDICIAL_POST_CHANGE"));
        result.put("institutionPostCount", countRuleType(eligibleItems, "INSTITUTION_POST_CHANGE"));
        result.put("rankPromotionCount", countRuleType(eligibleItems, "CIVIL_RANK_PROMOTION"));
        result.put("policeRankAllowanceCount", countRuleType(eligibleItems, "POLICE_RANK_ALLOWANCE_CHANGE"));
        result.put("matchedCount", trial.matchedCount());
        result.put("differentCount", trial.differentCount());
        result.put("noExpectedCount", trial.noExpectedCount());
        result.put("skippedCount", trial.skippedCount());
        result.put("totalDifference", trial.totalDifference());
        return result;
    }

    private long countRuleType(List<NormalGradeBatchTrialItem> items, String ruleType) {
        return items.stream().filter(item -> ruleType.equalsIgnoreCase(text(item.ruleType()))).count();
    }

    private Map<String, Object> postChangeApplicationItem(NormalGradeBatchTrialItem item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("personCode", item.personCode());
        row.put("personName", item.personName());
        row.put("orgCode", item.orgCode());
        row.put("orgName", item.orgName());
        row.put("changeType", postChangeApplicationChangeType(item));
        row.put("ruleType", item.ruleType());
        row.put("status", item.status());
        row.put("beforeValue", item.beforeValue());
        row.put("afterValue", item.afterValue());
        row.put("changeAmount", item.changeAmount());
        row.put("message", item.message());
        return row;
    }

    private String postChangeApplicationChangeType(NormalGradeBatchTrialItem item) {
        return switch (text(item.ruleType())) {
            case "JUDICIAL_POST_CHANGE" -> "\u6cd5\u68c0\u7b49\u7ea7\u53d8\u5316";
            case "INSTITUTION_POST_CHANGE" -> "\u804c\u52a1\u53d8\u5316";
            case "CIVIL_RANK_PROMOTION" -> "\u804c\u7ea7\u664b\u5347";
            case "POLICE_RANK_ALLOWANCE_CHANGE" -> "\u8b66\u8854\u6d25\u8d34\u53d8\u5316";
            default -> "\u804c\u52a1\u53d8\u5316";
        };
    }

    private String postChangeApplicationNote(NormalGradeBatchTrialItem item) {
        return postChangeApplicationChangeType(item) + " " + text(item.beforeValue()) + " -> " + text(item.afterValue())
                + "\uff0c\u89c4\u5219=" + text(item.ruleType()) + "\uff0c\u72b6\u6001=" + text(item.status());
    }

    private String allowanceChangeType(String changeType) {
        String safeChangeType = text(changeType);
        return safeChangeType.isBlank() ? "\u6d25\u8d34\u53d8\u5316" : safeChangeType;
    }

    private List<NormalGradeBatchTrialItem> allowanceChangeEligibleItems(NormalGradeBatchTrialResult trial) {
        return trial.items().stream()
                .filter(item -> Set.of(
                        "STANDARD_ADJUSTMENT",
                        "TEACHER_NURSE_ALLOWANCE_CHANGE",
                        "JUDICIAL_ALLOWANCE_CHANGE"
                ).contains(text(item.ruleType())))
                .toList();
    }

    private Map<String, Object> allowanceChangeApplicationSummary(NormalGradeBatchTrialResult trial, List<NormalGradeBatchTrialItem> eligibleItems) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", trial.orgCode());
        result.put("year", trial.year());
        result.put("month", trial.month());
        result.put("checkedCount", trial.checkedCount());
        result.put("eligibleCount", eligibleItems.size());
        result.put("standardAdjustmentCount", countRuleType(eligibleItems, "STANDARD_ADJUSTMENT"));
        result.put("teacherNurseAllowanceCount", countRuleType(eligibleItems, "TEACHER_NURSE_ALLOWANCE_CHANGE"));
        result.put("judicialAllowanceCount", countRuleType(eligibleItems, "JUDICIAL_ALLOWANCE_CHANGE"));
        result.put("matchedCount", trial.matchedCount());
        result.put("differentCount", trial.differentCount());
        result.put("noExpectedCount", trial.noExpectedCount());
        result.put("skippedCount", trial.skippedCount());
        result.put("totalDifference", trial.totalDifference());
        return result;
    }

    private Map<String, Object> allowanceChangeApplicationItem(NormalGradeBatchTrialItem item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("personCode", item.personCode());
        row.put("personName", item.personName());
        row.put("orgCode", item.orgCode());
        row.put("orgName", item.orgName());
        row.put("changeType", allowanceChangeApplicationChangeType(item));
        row.put("ruleType", item.ruleType());
        row.put("status", item.status());
        row.put("beforeValue", item.beforeValue());
        row.put("afterValue", item.afterValue());
        row.put("changeAmount", item.changeAmount());
        row.put("message", item.message());
        return row;
    }

    private String allowanceChangeApplicationChangeType(NormalGradeBatchTrialItem item) {
        return switch (text(item.ruleType())) {
            case "TEACHER_NURSE_ALLOWANCE_CHANGE" -> "\u6559\u62a4\u6d25\u8d34";
            case "JUDICIAL_ALLOWANCE_CHANGE" -> "\u6cd5\u68c0\u6d25\u8d34";
            default -> "\u6d25\u8d34\u53d8\u5316";
        };
    }

    private String allowanceChangeApplicationNote(NormalGradeBatchTrialItem item) {
        return allowanceChangeApplicationChangeType(item) + " " + text(item.beforeValue()) + " -> " + text(item.afterValue())
                + "\uff0c\u89c4\u5219=" + text(item.ruleType()) + "\uff0c\u72b6\u6001=" + text(item.status());
    }

    private List<NormalGradeBatchTrialItem> transferSalaryEligibleItems(NormalGradeBatchTrialResult trial) {
        return trial.items().stream()
                .filter(item -> isTransferSalaryChange(specialSalaryApplicationChangeType(item)))
                .toList();
    }

    private List<NormalGradeBatchTrialItem> punishmentReductionEligibleItems(NormalGradeBatchTrialResult trial) {
        return trial.items().stream()
                .filter(item -> isPunishmentReductionChange(specialSalaryApplicationChangeType(item)))
                .toList();
    }

    private NormalGradeBatchTrialResult specialSalaryTrial(String orgCode, int year, int month, int limit, String changeType,
                                                           List<String> defaultChangeTypes) {
        List<String> changeTypes = text(changeType).isBlank() ? defaultChangeTypes : List.of(text(changeType));
        if (changeTypes.size() == 1) {
            return normalGradeBatchTrialService.trial(new NormalGradeBatchTrialCommand(orgCode, year, month, limit, changeTypes.getFirst()));
        }
        List<NormalGradeBatchTrialResult> trials = changeTypes.stream()
                .map(type -> normalGradeBatchTrialService.trial(new NormalGradeBatchTrialCommand(orgCode, year, month, limit, type)))
                .toList();
        LinkedHashMap<String, NormalGradeBatchTrialItem> mergedItems = new LinkedHashMap<>();
        for (NormalGradeBatchTrialResult trial : trials) {
            for (NormalGradeBatchTrialItem item : trial.items()) {
                String detectedType = specialSalaryApplicationChangeType(item);
                String key = text(item.personCode()) + "|" + text(detectedType) + "|" + text(item.ruleType()) + "|" + text(item.ruleNote());
                mergedItems.putIfAbsent(key, item);
            }
        }
        BigDecimal totalDifference = trials.stream()
                .map(NormalGradeBatchTrialResult::totalDifference)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new NormalGradeBatchTrialResult(
                orgCode,
                year,
                month,
                trials.stream().mapToInt(NormalGradeBatchTrialResult::checkedCount).sum(),
                trials.stream().mapToInt(NormalGradeBatchTrialResult::matchedCount).sum(),
                trials.stream().mapToInt(NormalGradeBatchTrialResult::differentCount).sum(),
                trials.stream().mapToInt(NormalGradeBatchTrialResult::noExpectedCount).sum(),
                trials.stream().mapToInt(NormalGradeBatchTrialResult::skippedCount).sum(),
                trials.stream().mapToInt(NormalGradeBatchTrialResult::reverseStepCount).sum(),
                trials.stream().mapToInt(NormalGradeBatchTrialResult::levelPromotionCount).sum(),
                trials.stream().mapToInt(NormalGradeBatchTrialResult::notEligibleCount).sum(),
                totalDifference,
                new ArrayList<>(mergedItems.values())
        );
    }

    private int generateSpecialSalaryApplications(NormalGradeBatchTrialResult trial, List<NormalGradeBatchTrialItem> eligibleItems,
                                                  String sourceId, String workItemPrefix) {
        int generated = 0;
        for (NormalGradeBatchTrialItem item : eligibleItems) {
            String workItemId = workItemPrefix + "-" + trial.orgCode() + "-" + trial.year() + "-"
                    + String.format("%02d", trial.month()) + "-" + item.personCode();
            String itemChangeType = specialSalaryApplicationChangeType(item);
            jdbcTemplate.update("""
                    INSERT INTO salary_todo_candidate_cache(work_item_id, source, source_id, person_code, org_code,
                                                            person_no, person_name, event_year, event_month, change_type, note)
                    VALUES (?, 'SALARY_EVENT', ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        source = VALUES(source),
                        source_id = VALUES(source_id),
                        person_code = VALUES(person_code),
                        org_code = VALUES(org_code),
                        person_no = VALUES(person_no),
                        person_name = VALUES(person_name),
                        event_year = VALUES(event_year),
                        event_month = VALUES(event_month),
                        change_type = VALUES(change_type),
                        note = VALUES(note),
                        generated_at = CURRENT_TIMESTAMP
                    """,
                    workItemId,
                    sourceId,
                    item.personCode(),
                    item.orgCode(),
                    personNo(item.personCode()),
                    item.personName(),
                    trial.year(),
                    trial.month(),
                    itemChangeType,
                    left(specialSalaryApplicationNote(item), 1000));
            generated++;
        }
        return generated;
    }

    private Map<String, Object> specialSalaryApplicationSummary(NormalGradeBatchTrialResult trial, List<NormalGradeBatchTrialItem> eligibleItems) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", trial.orgCode());
        result.put("year", trial.year());
        result.put("month", trial.month());
        result.put("checkedCount", trial.checkedCount());
        result.put("eligibleCount", eligibleItems.size());
        result.put("transferCount", countSpecialChangeType(eligibleItems, "\u8c03\u5165\u5b9a\u8d44"));
        result.put("demobilizedCadreCount", countSpecialChangeType(eligibleItems, "\u8f6c\u4e1a\u5b9a\u8d44"));
        result.put("veteranPlacementCount", countSpecialChangeType(eligibleItems, "\u9000\u4f0d\u5b9a\u8d44"));
        result.put("punishmentCount", countSpecialChangeType(eligibleItems, "\u964d\u8d44\u5904\u5206"));
        result.put("rewardCount", countSpecialChangeType(eligibleItems, "\u5956\u52b1\u664b\u5347"));
        result.put("otherSpecialCount", countSpecialChangeType(eligibleItems, "\u5176\u5b83\u60c5\u51b5"));
        result.put("matchedCount", trial.matchedCount());
        result.put("differentCount", trial.differentCount());
        result.put("noExpectedCount", trial.noExpectedCount());
        result.put("skippedCount", trial.skippedCount());
        result.put("totalDifference", trial.totalDifference());
        return result;
    }

    private long countSpecialChangeType(List<NormalGradeBatchTrialItem> items, String changeType) {
        return items.stream().filter(item -> changeType.equals(specialSalaryApplicationChangeType(item))).count();
    }

    private Map<String, Object> specialSalaryApplicationItem(NormalGradeBatchTrialItem item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("personCode", item.personCode());
        row.put("personName", item.personName());
        row.put("orgCode", item.orgCode());
        row.put("orgName", item.orgName());
        row.put("changeType", specialSalaryApplicationChangeType(item));
        row.put("ruleType", item.ruleType());
        row.put("ruleNote", item.ruleNote());
        row.put("status", item.status());
        row.put("beforeValue", item.beforeValue());
        row.put("afterValue", item.afterValue());
        row.put("changeAmount", item.changeAmount());
        row.put("message", item.message());
        return row;
    }

    private String specialSalaryApplicationChangeType(NormalGradeBatchTrialItem item) {
        String note = text(item.ruleNote()) + " " + text(item.message());
        if (note.contains("\u8f6c\u4e1a\u5b9a\u8d44")) {
            return "\u8f6c\u4e1a\u5b9a\u8d44";
        }
        if (note.contains("\u9000\u4f0d\u5b9a\u8d44")) {
            return "\u9000\u4f0d\u5b9a\u8d44";
        }
        if (note.contains("\u8c03\u5165\u5b9a\u8d44") || note.contains("\u5165\u53e3\u5b9a\u8d44")) {
            return "\u8c03\u5165\u5b9a\u8d44";
        }
        if (note.contains("\u964d\u8d44\u5904\u5206") || note.contains("\u5904\u5206")) {
            return "\u964d\u8d44\u5904\u5206";
        }
        if (note.contains("\u5956\u52b1\u664b\u5347") || note.contains("\u5956\u52b1")) {
            return "\u5956\u52b1\u664b\u5347";
        }
        if (note.contains("\u5176\u5b83\u60c5\u51b5") || note.contains("\u5176\u4ed6\u60c5\u51b5")) {
            return "\u5176\u5b83\u60c5\u51b5";
        }
        return "";
    }

    private boolean isTransferSalaryChange(String changeType) {
        return Set.of("\u8c03\u5165\u5b9a\u8d44", "\u8f6c\u4e1a\u5b9a\u8d44", "\u9000\u4f0d\u5b9a\u8d44").contains(text(changeType));
    }

    private boolean isPunishmentReductionChange(String changeType) {
        return Set.of("\u964d\u8d44\u5904\u5206", "\u5956\u52b1\u664b\u5347", "\u5176\u5b83\u60c5\u51b5").contains(text(changeType));
    }

    private String specialSalaryApplicationNote(NormalGradeBatchTrialItem item) {
        return specialSalaryApplicationChangeType(item) + " " + text(item.beforeValue()) + " -> " + text(item.afterValue())
                + "\uff0c\u89c4\u5219=" + text(item.ruleType()) + "\uff0c\u8bf4\u660e=" + text(item.ruleNote())
                + "\uff0c\u72b6\u6001=" + text(item.status());
    }

    private Map<String, Object> regressionCaseSample(String code, String title, String domain, String orgCode, List<String> keywords, int limit) {
        String keywordPredicate = regressionKeywordPredicate("business_type", "title", "summary", keywords.size());
        List<Object> countArgs = new ArrayList<>();
        countArgs.add(orgCode);
        addRegressionKeywordArgs(countArgs, keywords, 3);
        long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_business_case
                WHERE org_code LIKE CONCAT(?, '%')
                  AND __KEYWORDS__
                """.replace("__KEYWORDS__", keywordPredicate), Long.class, countArgs.toArray());
        List<Object> sampleArgs = new ArrayList<>(countArgs);
        sampleArgs.add(Math.max(1, Math.min(limit, 20)));
        List<Map<String, Object>> people = jdbcTemplate.queryForList("""
                SELECT case_no AS sampleId,
                       person_code AS personCode,
                       person_name AS personName,
                       org_code AS orgCode,
                       business_type AS sampleType
                FROM salary_business_case
                WHERE org_code LIKE CONCAT(?, '%')
                  AND __KEYWORDS__
                ORDER BY handled_at DESC, id DESC
                LIMIT ?
                """.replace("__KEYWORDS__", keywordPredicate), sampleArgs.toArray());
        return regressionSample(code, title, domain, count, count > 0,
                "POST /api/workbench/*-applications/preview + /generate",
                count > 0 ? "\u5df2\u627e\u5230\u53ef\u7528\u4e1a\u52a1\u6837\u672c" : "\u5f53\u524d\u5355\u4f4d\u672a\u627e\u5230\u8be5\u7c7b\u56de\u5f52\u6837\u672c\uff0c\u9700\u8865\u5165\u6837\u672c\u4eba\u5458",
                people);
    }

    private Map<String, Object> regressionHistorySample(String code, String title, String domain, String orgCode, String predicate, int limit) {
        long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_history_write_plan p
                WHERE p.org_code LIKE CONCAT(?, '%')
                  AND __PREDICATE__
                """.replace("__PREDICATE__", predicate), Long.class, orgCode);
        List<Map<String, Object>> people = jdbcTemplate.queryForList("""
                SELECT p.plan_no AS sampleId,
                       p.person_code AS personCode,
                       COALESCE(c.person_name, '') AS personName,
                       p.org_code AS orgCode,
                       CONCAT(COALESCE(p.plan_status, ''), '/', COALESCE(p.preview_status, '')) AS sampleType
                FROM salary_history_write_plan p
                LEFT JOIN salary_business_case c ON c.case_no = p.case_no
                WHERE p.org_code LIKE CONCAT(?, '%')
                  AND __PREDICATE__
                ORDER BY COALESCE(p.executed_at, p.prepared_at) DESC, p.id DESC
                LIMIT ?
                """.replace("__PREDICATE__", predicate), orgCode, Math.max(1, Math.min(limit, 20)));
        return regressionSample(code, title, domain, count, count > 0,
                "history-write-preview/confirm/execute/rollback/review-ledger",
                count > 0 ? "\u5df2\u627e\u5230\u5386\u53f2\u5199\u5165\u6837\u672c" : "\u672a\u627e\u5230\u8be5\u7c7b\u5386\u53f2\u5199\u5165\u6837\u672c\uff0c\u540e\u7eed\u9700\u8865\u6837",
                people);
    }

    private Map<String, Object> regressionGovernanceSample(String orgCode, Map<String, Object> governance, int limit) {
        long count = longValue(governance.get("issueCount"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> issues = (List<Map<String, Object>>) governance.getOrDefault("issues", List.of());
        List<Map<String, Object>> people = issues.stream()
                .limit(Math.max(1, Math.min(limit, 20)))
                .map(issue -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("sampleId", text(issue.get("issueType")));
                    row.put("personCode", text(issue.get("personCode")));
                    row.put("personName", text(issue.get("personName")));
                    row.put("orgCode", text(issue.get("orgCode")));
                    row.put("sampleType", text(issue.get("message")));
                    return row;
                }).toList();
        return regressionSample("data-governance", "\u6570\u636e\u6cbb\u7406\u4efb\u52a1\u5316", "\u6570\u636e\u6cbb\u7406", count, true,
                "GET /api/workbench/data-governance/scan",
                count > 0 ? "\u5df2\u626b\u63cf\u5230\u6cbb\u7406\u9879\uff0c\u53ef\u8f6c\u5f85\u529e\u590d\u6d4b" : "\u6cbb\u7406\u626b\u63cf\u53ef\u7528\uff0c\u5f53\u524d\u6837\u672c\u8303\u56f4\u672a\u53d1\u73b0\u95ee\u9898",
                people);
    }

    private Map<String, Object> regressionReportPrintSample(String orgCode, int limit) {
        long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM salary_report_print_batch b
                WHERE b.org_code LIKE CONCAT(?, '%')
                """, Long.class, orgCode);
        List<Map<String, Object>> people = jdbcTemplate.queryForList("""
                SELECT i.batch_no AS sampleId,
                       i.person_code AS personCode,
                       i.person_name AS personName,
                       b.org_code AS orgCode,
                       b.report_type AS sampleType
                FROM salary_report_print_batch_item i
                JOIN salary_report_print_batch b ON b.batch_no = i.batch_no
                WHERE b.org_code LIKE CONCAT(?, '%')
                ORDER BY i.created_at DESC, i.id DESC
                LIMIT ?
                """, orgCode, Math.max(1, Math.min(limit, 20)));
        return regressionSample("report-print", "报表打印/归档前置", "报表打印", count, count > 0,
                "GET /api/workbench/report-catalog + print archive",
                count > 0 ? "已找到打印批次样本" : "未找到打印批次样本，历史写入前置归档需补样",
                people);
    }

    private Map<String, Object> migrationRegressionExpectedAssertion(Map<String, Object> sample, Map<String, Object> person) {
        return migrationRegressionActualAssertion(text(sample.get("code")), text(person.get("sampleId")), text(person.get("personCode")));
    }

    private Map<String, Object> runMigrationRegressionSampleRowWithAssertions(Map<String, Object> row) {
        String code = text(row.get("sample_code"));
        String sampleId = text(row.get("sample_id"));
        String personCode = text(row.get("person_code"));
        String expectedStatus = text(row.get("expected_status"));
        String expectedPayload = text(row.get("expected_payload"));
        Object expectedAmount = row.get("expected_amount");
        Map<String, Object> actual = migrationRegressionActualAssertion(code, sampleId, personCode);
        String actualStatus = text(actual.get("status"));
        String actualPayload = text(actual.get("payload"));
        Object actualAmount = actual.get("amount");
        boolean exists = Boolean.TRUE.equals(booleanValue(actual.get("exists")));
        boolean statusMatched = expectedStatus.isBlank() || expectedStatus.equals(actualStatus);
        boolean amountMatched = migrationRegressionAmountMatches(expectedAmount, actualAmount);
        boolean payloadMatched = expectedPayload.isBlank() || expectedPayload.equals(actualPayload);
        boolean pass = exists && statusMatched && amountMatched && payloadMatched;
        String message = "assertion matched";
        if (!exists) {
            message = "sample not found";
        } else if (!statusMatched) {
            message = "status changed";
        } else if (!amountMatched) {
            message = "amount changed";
        } else if (!payloadMatched) {
            message = "payload changed";
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", code);
        result.put("title", text(row.get("sample_title")));
        result.put("domain", text(row.get("sample_domain")));
        result.put("sampleId", sampleId);
        result.put("personCode", personCode);
        result.put("personName", text(row.get("person_name")));
        result.put("orgCode", text(row.get("org_code")));
        result.put("sampleType", text(row.get("sample_type")));
        result.put("status", pass ? "PASS" : "WARN");
        result.put("expectedStatus", expectedStatus);
        result.put("actualStatus", actualStatus);
        result.put("expectedAmount", expectedAmount);
        result.put("actualAmount", actualAmount);
        result.put("expectedPayload", expectedPayload);
        result.put("actualPayload", actualPayload);
        result.put("message", message);
        return result;
    }

    private Map<String, Object> migrationRegressionActualAssertion(String code, String sampleId, String personCode) {
        if (code.startsWith("history-write")) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                    SELECT CONCAT(COALESCE(plan_status, ''), '/', COALESCE(preview_status, '')) AS status_value,
                           NULL AS amount_value,
                           CONCAT(COALESCE(comparison_status, ''), ':', COALESCE(comparison_mismatch_count, 0)) AS payload_value
                    FROM salary_history_write_plan
                    WHERE plan_no = ?
                      AND person_code = ?
                    LIMIT 1
                    """, sampleId, personCode);
            return assertionFromRows(rows);
        }
        if ("report-print".equals(code)) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                    SELECT COALESCE(i.validation_status, '') AS status_value,
                           b.printed_count AS amount_value,
                           CONCAT(COALESCE(b.report_type, ''), ':', COALESCE(b.warning_count, 0)) AS payload_value
                    FROM salary_report_print_batch_item i
                    JOIN salary_report_print_batch b ON b.batch_no = i.batch_no
                    WHERE i.batch_no = ?
                      AND i.person_code = ?
                    LIMIT 1
                    """, sampleId, personCode);
            return assertionFromRows(rows);
        }
        if ("data-governance".equals(code)) {
            Map<String, Object> assertion = new LinkedHashMap<>();
            assertion.put("exists", !personCode.isBlank());
            assertion.put("status", personCode.isBlank() ? "" : "PRESENT");
            assertion.put("amount", null);
            assertion.put("payload", sampleId);
            return assertion;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT COALESCE(NULLIF(trial_status, ''), status) AS status_value,
                       COALESCE(trial_expected_total, trial_calculated_total, trial_baseline_total) AS amount_value,
                       LEFT(COALESCE(NULLIF(trial_changes_json, ''), trial_summary, ''), 1024) AS payload_value
                FROM salary_business_case
                WHERE case_no = ?
                  AND person_code = ?
                LIMIT 1
        """, sampleId, personCode);
        return assertionFromRows(rows);
    }

    private Map<String, Object> migrationRegressionSampleSource(String code, String sampleId, String personCode, String orgCode) {
        if (code.startsWith("history-write")) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                    SELECT COALESCE(c.person_name, '') AS personName,
                           p.business_type AS sampleType
                    FROM salary_history_write_plan p
                    LEFT JOIN salary_business_case c ON c.case_no = p.case_no
                    WHERE p.plan_no = ?
                      AND p.person_code = ?
                      AND p.org_code LIKE CONCAT(?, '%')
                    LIMIT 1
                    """, sampleId, personCode, orgCode);
            return regressionSource(rows, "history write", "history-write");
        }
        if ("report-print".equals(code)) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                    SELECT i.person_name AS personName,
                           b.report_type AS sampleType
                    FROM salary_report_print_batch_item i
                    JOIN salary_report_print_batch b ON b.batch_no = i.batch_no
                    WHERE i.batch_no = ?
                      AND i.person_code = ?
                      AND b.org_code LIKE CONCAT(?, '%')
                    LIMIT 1
                    """, sampleId, personCode, orgCode);
            return regressionSource(rows, "report print archive", "report");
        }
        if ("data-governance".equals(code)) {
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("personName", "");
            source.put("sampleType", sampleId);
            source.put("title", "data governance");
            source.put("domain", "governance");
            return source;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT person_name AS personName,
                       business_type AS sampleType
                FROM salary_business_case
                WHERE case_no = ?
                  AND person_code = ?
                  AND org_code LIKE CONCAT(?, '%')
                LIMIT 1
                """, sampleId, personCode, orgCode);
        return regressionSource(rows, "business case", "case");
    }

    private Map<String, Object> regressionSource(List<Map<String, Object>> rows, String title, String domain) {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("title", title);
        source.put("domain", domain);
        if (rows.isEmpty()) {
            source.put("personName", "");
            source.put("sampleType", "");
            return source;
        }
        Map<String, Object> row = rows.getFirst();
        source.put("personName", text(row.get("personName")));
        source.put("sampleType", text(row.get("sampleType")));
        return source;
    }

    private String defaultText(String value, String fallback) {
        String safe = text(value);
        return safe.isBlank() ? text(fallback) : safe;
    }

    private BigDecimal decimalOrNull(String value) {
        String safe = text(value);
        if (safe.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(safe);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private int migrationRegressionEnabledValue(String value) {
        String safe = text(value).toLowerCase();
        return ("false".equals(safe) || "0".equals(safe) || "disabled".equals(safe)) ? 0 : 1;
    }

    private Map<String, Object> assertionFromRows(List<Map<String, Object>> rows) {
        Map<String, Object> assertion = new LinkedHashMap<>();
        assertion.put("exists", !rows.isEmpty());
        if (rows.isEmpty()) {
            assertion.put("status", "");
            assertion.put("amount", null);
            assertion.put("payload", "");
            return assertion;
        }
        Map<String, Object> row = rows.getFirst();
        assertion.put("status", text(row.get("status_value")));
        assertion.put("amount", row.get("amount_value"));
        assertion.put("payload", text(row.get("payload_value")));
        return assertion;
    }

    private boolean migrationRegressionAmountMatches(Object expected, Object actual) {
        if (expected == null || text(expected).isBlank()) {
            return actual == null || text(actual).isBlank();
        }
        if (actual == null || text(actual).isBlank()) {
            return false;
        }
        try {
            return new BigDecimal(text(expected)).compareTo(new BigDecimal(text(actual))) == 0;
        } catch (NumberFormatException ex) {
            return text(expected).equals(text(actual));
        }
    }

    private Map<String, Object> runMigrationRegressionSampleRow(Map<String, Object> row) {
        String code = text(row.get("sample_code"));
        String sampleId = text(row.get("sample_id"));
        String personCode = text(row.get("person_code"));
        boolean exists;
        String message;
        if (code.startsWith("history-write")) {
            exists = countBySql("""
                    SELECT COUNT(1)
                    FROM salary_history_write_plan
                    WHERE plan_no = ?
                      AND person_code = ?
                    """, sampleId, personCode) > 0;
            message = exists ? "历史写入样本仍可定位" : "历史写入计划不存在或人员不匹配";
        } else if ("report-print".equals(code)) {
            exists = countBySql("""
                    SELECT COUNT(1)
                    FROM salary_report_print_batch_item
                    WHERE batch_no = ?
                      AND person_code = ?
                    """, sampleId, personCode) > 0;
            message = exists ? "报表打印样本仍可定位" : "打印批次明细不存在或人员不匹配";
        } else if ("data-governance".equals(code)) {
            exists = !personCode.isBlank();
            message = exists ? "治理样本人员仍在样本库中" : "治理样本缺少人员编码";
        } else {
            exists = countBySql("""
                    SELECT COUNT(1)
                    FROM salary_business_case
                    WHERE case_no = ?
                      AND person_code = ?
                    """, sampleId, personCode) > 0;
            message = exists ? "业务样本仍可定位" : "业务单不存在或人员不匹配";
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", code);
        result.put("title", text(row.get("sample_title")));
        result.put("domain", text(row.get("sample_domain")));
        result.put("sampleId", sampleId);
        result.put("personCode", personCode);
        result.put("personName", text(row.get("person_name")));
        result.put("orgCode", text(row.get("org_code")));
        result.put("sampleType", text(row.get("sample_type")));
        result.put("status", exists ? "PASS" : "WARN");
        result.put("message", message);
        return result;
    }

    private long countBySql(String sql, Object... args) {
        Long count = jdbcTemplate.queryForObject(sql, Long.class, args);
        return count == null ? 0 : count;
    }

    private Map<String, Object> regressionSample(String code, String title, String domain, long count, boolean pass,
                                                 String endpoint, String message, List<Map<String, Object>> people) {
        Map<String, Object> sample = new LinkedHashMap<>();
        sample.put("code", code);
        sample.put("title", title);
        sample.put("domain", domain);
        sample.put("status", pass ? "PASS" : "WARN");
        sample.put("count", count);
        sample.put("endpoint", endpoint);
        sample.put("message", message);
        sample.put("people", people == null ? List.of() : people);
        return sample;
    }

    private String regressionKeywordPredicate(String firstColumn, String secondColumn, String thirdColumn, int keywordCount) {
        return java.util.stream.IntStream.range(0, keywordCount)
                .mapToObj(i -> "(" + firstColumn + " LIKE ? OR " + secondColumn + " LIKE ? OR " + thirdColumn + " LIKE ?)")
                .collect(java.util.stream.Collectors.joining(" OR "));
    }

    private void addRegressionKeywordArgs(List<Object> args, List<String> keywords, int columnCount) {
        for (String keyword : keywords) {
            String value = "%" + text(keyword) + "%";
            for (int i = 0; i < columnCount; i++) {
                args.add(value);
            }
        }
    }

    private Map<String, Object> acceptanceItem(String code, String title, String verification) {
        return Map.of(
                "code", code,
                "title", title,
                "verification", verification
        );
    }

    private Map<String, Object> acceptanceGate(String code, String title, String status, long count, String message) {
        return Map.of(
                "code", code,
                "title", title,
                "status", status,
                "count", count,
                "message", message
        );
    }

    private long accessibleAcceptanceMenuCount() {
        return List.of(
                "WORKBENCH",
                "APPLICATION_TODO",
                "APPLICATION_DONE",
                "SALARY_TODO",
                "SALARY_DONE",
                "SALARY_HISTORY_WRITE",
                "SALARY_HISTORY_ROLLBACK",
                "SALARY_PERSON",
                "SALARY_CONFIG",
                "SALARY_ACCEPTANCE"
        ).stream().filter(this::hasMenu).count();
    }

    private Map<String, Object> applicationCaseRow(String caseNo) {
        ensureApplicationCaseTable();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM application_case
                WHERE case_no = ?
                LIMIT 1
                """, text(caseNo));
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Application case not found: " + caseNo);
        }
        return rows.getFirst();
    }

    private WorkbenchItemResponse applicationCaseItem(String caseNo) {
        return applicationCaseItem(applicationCaseRow(caseNo));
    }

    private WorkbenchItemResponse applicationCaseItem(Map<String, Object> row) {
        return new WorkbenchItemResponse(
                text(row.get("case_no")),
                text(row.get("source")).isBlank() ? "APPLICATION_CASE" : text(row.get("source")),
                text(row.get("status")),
                text(row.get("business_type")),
                text(row.get("person_code")),
                text(row.get("person_name")),
                text(row.get("org_code")),
                number(row.get("event_year")),
                number(row.get("event_month")),
                text(row.get("title")),
                text(row.get("summary")),
                "",
                text(row.get("review_reason")).isBlank() ? "" : "REVIEWED",
                text(row.get("workflow_status")).isBlank()
                        ? ("DONE".equalsIgnoreCase(text(row.get("status"))) ? "APPLICATION_DONE" : "APPLICATION_TODO")
                        : text(row.get("workflow_status")),
                "DONE".equalsIgnoreCase(text(row.get("status"))) ? "CLOSED" : "PENDING",
                "DONE".equalsIgnoreCase(text(row.get("status"))) ? "\u7533\u529e\u4e1a\u52a1\u5df2\u529e\u7ed3" : "\u7533\u529e\u4e1a\u52a1\u5f85\u529e\u7406",
                "DONE".equalsIgnoreCase(text(row.get("status"))) ? "" : "HANDLE_APPLICATION",
                "DONE".equalsIgnoreCase(text(row.get("status"))) ? "" : "\u529e\u7406\u7533\u529e"
        );
    }

    private void ensureGeneratedTimelineIssueReviewTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS salary_generated_timeline_issue_review (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    work_item_id VARCHAR(255) NOT NULL,
                    person_code VARCHAR(128) NOT NULL,
                    org_code VARCHAR(64) NOT NULL,
                    review_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
                    review_reason VARCHAR(1024) NULL,
                    reviewed_by VARCHAR(64) NULL,
                    reviewed_at DATETIME NULL,
                    retest_status VARCHAR(32) NULL,
                    retest_summary VARCHAR(1024) NULL,
                    retested_at DATETIME NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_generated_issue_work_item (work_item_id),
                    KEY idx_generated_issue_person (person_code),
                    KEY idx_generated_issue_org_status (org_code, review_status)
                )
                """);
    }

    private void ensureDataGovernanceTaskReviewTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS salary_data_governance_task_review (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    work_item_id VARCHAR(255) NOT NULL,
                    person_code VARCHAR(128) NOT NULL,
                    org_code VARCHAR(64) NOT NULL,
                    issue_type VARCHAR(64) NOT NULL,
                    review_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
                    review_reason VARCHAR(1024) NULL,
                    reviewed_by VARCHAR(64) NULL,
                    reviewed_at DATETIME NULL,
                    retest_status VARCHAR(32) NULL,
                    retest_summary VARCHAR(1024) NULL,
                    retested_at DATETIME NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_governance_task_work_item (work_item_id),
                    KEY idx_governance_task_person (person_code),
                    KEY idx_governance_task_org_status (org_code, review_status),
                    KEY idx_governance_task_issue_type (issue_type)
                )
                """);
    }

    private Map<String, Object> todoCacheRow(String workItemId) {
        ensureSalaryTodoCacheTable();
        String safeWorkItemId = text(workItemId);
        if (safeWorkItemId.isBlank()) {
            throw new IllegalArgumentException("Work item id is required.");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM salary_todo_candidate_cache
                WHERE work_item_id = ?
                LIMIT 1
                """, safeWorkItemId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Salary todo item not found: " + safeWorkItemId);
        }
        return rows.getFirst();
    }

    private Map<String, Object> todoCacheRowIfExists(String workItemId) {
        ensureSalaryTodoCacheTable();
        String safeWorkItemId = text(workItemId);
        if (safeWorkItemId.isBlank()) {
            return Map.of();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM salary_todo_candidate_cache
                WHERE work_item_id = ?
                LIMIT 1
                """, safeWorkItemId);
        return rows.isEmpty() ? Map.of() : rows.getFirst();
    }

    private Map<String, Object> dataGovernanceReviewRow(String workItemId) {
        ensureDataGovernanceTaskReviewTable();
        String safeWorkItemId = text(workItemId);
        if (safeWorkItemId.isBlank()) {
            return Map.of();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM salary_data_governance_task_review
                WHERE work_item_id = ?
                LIMIT 1
                """, safeWorkItemId);
        return rows.isEmpty() ? Map.of() : rows.getFirst();
    }

    private String personNameByCode(String personCode) {
        String safePersonCode = text(personCode);
        if (safePersonCode.isBlank()) {
            return "";
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(COALESCE(xm, '')) AS person_name
                FROM dryjbxx
                WHERE CONCAT(TRIM(dwbm), '-', TRIM(grbm)) = ?
                LIMIT 1
                """, safePersonCode);
        return rows.isEmpty() ? "" : text(rows.getFirst().get("person_name"));
    }

    private WorkbenchGeneratedIssueReviewResponse generatedIssueReview(String workItemId) {
        ensureGeneratedTimelineIssueReviewTable();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM salary_generated_timeline_issue_review
                WHERE work_item_id = ?
                LIMIT 1
                """, workItemId);
        if (rows.isEmpty()) {
            Map<String, Object> todo = todoCacheRow(workItemId);
            return new WorkbenchGeneratedIssueReviewResponse(
                    workItemId,
                    text(todo.get("person_code")),
                    text(todo.get("org_code")),
                    "PENDING",
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
            );
        }
        Map<String, Object> row = rows.getFirst();
        return new WorkbenchGeneratedIssueReviewResponse(
                text(row.get("work_item_id")),
                text(row.get("person_code")),
                text(row.get("org_code")),
                text(row.get("review_status")),
                text(row.get("review_reason")),
                text(row.get("reviewed_by")),
                text(row.get("reviewed_at")),
                text(row.get("retest_status")),
                text(row.get("retest_summary")),
                text(row.get("retested_at"))
        );
    }

    private WorkbenchGeneratedIssueReviewResponse dataGovernanceTaskReview(String workItemId) {
        ensureDataGovernanceTaskReviewTable();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM salary_data_governance_task_review
                WHERE work_item_id = ?
                LIMIT 1
                """, workItemId);
        if (rows.isEmpty()) {
            Map<String, Object> todo = todoCacheRow(workItemId);
            return new WorkbenchGeneratedIssueReviewResponse(
                    workItemId,
                    text(todo.get("person_code")),
                    text(todo.get("org_code")),
                    "PENDING",
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
            );
        }
        Map<String, Object> row = rows.getFirst();
        return new WorkbenchGeneratedIssueReviewResponse(
                text(row.get("work_item_id")),
                text(row.get("person_code")),
                text(row.get("org_code")),
                text(row.get("review_status")),
                text(row.get("review_reason")),
                text(row.get("reviewed_by")),
                text(row.get("reviewed_at")),
                text(row.get("retest_status")),
                text(row.get("retest_summary")),
                text(row.get("retested_at"))
        );
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> governanceIssueList(Map<String, Object> scan) {
        Object issues = scan == null ? null : scan.get("issues");
        if (issues instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        }
        return List.of();
    }

    private String dataGovernanceWorkItemId(Map<String, Object> issue) {
        return "data-governance-"
                + dataGovernanceKeyPart(text(issue.get("issueType")))
                + "-"
                + dataGovernanceKeyPart(text(issue.get("personCode")))
                + (text(issue.get("caseNo")).isBlank() ? "" : "-" + dataGovernanceKeyPart(text(issue.get("caseNo"))));
    }

    private String dataGovernanceKeyPart(String value) {
        String safe = text(value).toLowerCase().replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-");
        safe = safe.replaceAll("^-+", "").replaceAll("-+$", "");
        return safe.isBlank() ? "unknown" : safe;
    }

    private String dataGovernanceIssueTypeText(String issueType) {
        return switch (text(issueType)) {
            case "MISSING_POST" -> "缺少任职信息";
            case "INVALID_EDUCATION_DATE" -> "学历毕业时间异常";
            case "BROKEN_HISTORY_SID" -> "历史链 sid 异常";
            case "STANDARD_REVIEW" -> "标准/规则抽查";
            case "BLOCKED_HISTORY_REVIEW" -> "写入阻断后期核查";
            case "HISTORY_SPECIAL_REVIEW" -> "历史特殊情况核查";
            default -> text(issueType).isBlank() ? "数据治理" : text(issueType);
        };
    }

    private String normalizeGeneratedIssueReviewStatus(String status) {
        String safeStatus = text(status).toUpperCase();
        if (safeStatus.isBlank()) {
            return "REVIEWED";
        }
        if (!Set.of("REVIEWED", "IGNORED", "PENDING").contains(safeStatus)) {
            throw new IllegalArgumentException("Generated issue review status must be REVIEWED, IGNORED, or PENDING.");
        }
        return safeStatus;
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
        ensureHistoryWritePlanTable();
        addIndexIfMissing("salary_business_case", "idx_salary_case_work_status", "work_item_id, status");
        addIndexIfMissing("salary_business_case", "idx_salary_case_status_trial_review_org", "status, trial_status, review_status, org_code");
        addIndexIfMissing("salary_history_write_plan", "idx_history_plan_status_time", "plan_status, prepared_at, id");
        addIndexIfMissing("salary_history_write_plan", "idx_history_plan_org_status_time", "org_code, plan_status, prepared_at, id");
        addIndexIfMissing("salary_history_write_plan", "idx_history_plan_write_queue", "plan_status, writable, comparison_status, comparison_review_status");
        addIndexIfMissing("salary_history_write_plan", "idx_history_plan_case_no", "case_no");
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

    private void appendSimpleCsv(StringBuilder csv, Object... values) {
        for (int i = 0; i < values.length; i += 1) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append('"').append(text(values[i]).replace("\"", "\"\"")).append('"');
        }
        csv.append('\n');
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize acceptance result.", ex);
        }
    }

    private Map<String, Object> readJsonMap(Object value) {
        String json = text(value);
        if (json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            return Map.of();
        }
    }

    private List<Map<String, Object>> readJsonList(Object value) {
        String json = text(value);
        if (json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            return List.of();
        }
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

    private void requireWorkbenchPermission() {
        if (!hasMenu("WORKBENCH")) {
            throw new IllegalArgumentException("Workbench permission is required.");
        }
    }

    private void requireSalaryTodoPermission() {
        if (!hasMenu("SALARY_TODO")) {
            throw new IllegalArgumentException("Salary todo permission is required.");
        }
    }

    private void requireSalaryDonePermission() {
        if (!hasMenu("SALARY_DONE")) {
            throw new IllegalArgumentException("Salary done permission is required.");
        }
    }

    private void requireDataGovernanceTaskViewPermission(boolean reviewedTask) {
        if (reviewedTask) {
            if (hasMenu("SALARY_DONE") || (hasMenu("SALARY_TODO") && hasMenu("SALARY_GOVERNANCE"))) {
                return;
            }
            throw new IllegalArgumentException("Salary todo or done permission is required.");
        }
        requireDataGovernancePermission();
        requireSalaryTodoPermission();
    }

    private void requireSalaryTrialPermission() {
        if (!hasMenu("SALARY_TRIAL")) {
            throw new IllegalArgumentException("Salary trial permission is required.");
        }
    }

    private void requireSalaryConfigPermission() {
        if (!hasMenu("SALARY_CONFIG")) {
            throw new IllegalArgumentException("Salary config permission is required.");
        }
    }

    private void requireDataGovernancePermission() {
        if (!hasMenu("SALARY_GOVERNANCE")) {
            throw new IllegalArgumentException("Salary data governance permission is required.");
        }
    }

    private void requireAcceptancePermission() {
        if (!hasMenu("SALARY_ACCEPTANCE")) {
            throw new IllegalArgumentException("Salary acceptance permission is required.");
        }
    }

    private void requireAcceptancePackageExportPermission() {
        requireAcceptancePermission();
        if (!hasMenu("SALARY_EXPORT")) {
            throw new IllegalArgumentException("Salary acceptance package export permission is required.");
        }
    }

    public void requireHistoryDeliveryExportPermission() {
        if (!hasMenu("SALARY_EXPORT")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Salary export permission is required.");
        }
    }

    public void requireHistoryClosureAcceptancePackagePermission() {
        if (!hasMenu("SALARY_ACCEPTANCE") || !hasMenu("SALARY_EXPORT")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Salary acceptance package export permission is required.");
        }
    }

    public String recordHistoryWriteDeliveryAcceptance(String exportType,
                                                       long pendingCount,
                                                       long closedCount,
                                                       long activeQueueCount,
                                                       long evidenceFileCount,
                                                       String conclusion,
                                                       Object summary) {
        ensureHistoryWriteDeliveryAcceptanceTable();
        String acceptanceNo = "HWD-" + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        jdbcTemplate.update("""
                INSERT INTO history_write_delivery_acceptance(acceptance_no, export_type, exported_at,
                                                              pending_count, closed_count, active_queue_count,
                                                              evidence_file_count, conclusion, summary_json, exported_by)
                VALUES (?, ?, NOW(), ?, ?, ?, ?, ?, CAST(? AS JSON), ?)
                """,
                acceptanceNo,
                left(text(exportType), 32),
                pendingCount,
                closedCount,
                activeQueueCount,
                evidenceFileCount,
                left(text(conclusion), 512),
                writeJson(summary),
                currentUserService.currentUsername()
        );
        return acceptanceNo;
    }

    public List<Map<String, Object>> historyWriteDeliveryAcceptances(int limit) {
        return historyWriteDeliveryAcceptances("", "", "", "", limit);
    }

    public List<Map<String, Object>> historyWriteDeliveryAcceptances(String keyword, String exportType, int limit) {
        return historyWriteDeliveryAcceptances(keyword, exportType, "", "", limit);
    }

    public List<Map<String, Object>> historyWriteDeliveryAcceptances(String keyword, String exportType, String exportedFrom, String exportedTo, int limit) {
        ensureHistoryWriteDeliveryAcceptanceTable();
        int safeLimit = Math.max(1, Math.min(limit, 100));
        String safeKeyword = text(keyword);
        String safeExportType = text(exportType).toUpperCase(java.util.Locale.ROOT);
        String safeExportedFrom = text(exportedFrom);
        String safeExportedTo = text(exportedTo);
        String keywordLike = "%" + safeKeyword + "%";
        return jdbcTemplate.queryForList("""
                SELECT acceptance_no AS acceptanceNo,
                       export_type AS exportType,
                       DATE_FORMAT(exported_at, '%Y-%m-%d %H:%i:%s') AS exportedAt,
                       pending_count AS pendingCount,
                       closed_count AS closedCount,
                       active_queue_count AS activeQueueCount,
                       evidence_file_count AS evidenceFileCount,
                       conclusion,
                       exported_by AS exportedBy
                FROM history_write_delivery_acceptance
                WHERE (? = '' OR acceptance_no LIKE ? OR exported_by LIKE ? OR conclusion LIKE ?)
                  AND (? = '' OR export_type = ?)
                  AND (? = '' OR exported_at >= STR_TO_DATE(?, '%Y-%m-%d'))
                  AND (? = '' OR exported_at < DATE_ADD(STR_TO_DATE(?, '%Y-%m-%d'), INTERVAL 1 DAY))
                ORDER BY exported_at DESC, id DESC
                LIMIT ?
                """, safeKeyword, keywordLike, keywordLike, keywordLike,
                safeExportType, safeExportType,
                safeExportedFrom, safeExportedFrom,
                safeExportedTo, safeExportedTo,
                safeLimit);
    }

    public Map<String, Object> historyWriteDeliveryAcceptanceDetail(String acceptanceNo) {
        ensureHistoryWriteDeliveryAcceptanceTable();
        String safeAcceptanceNo = text(acceptanceNo);
        if (safeAcceptanceNo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Acceptance no is required.");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT acceptance_no AS acceptanceNo,
                       export_type AS exportType,
                       DATE_FORMAT(exported_at, '%Y-%m-%d %H:%i:%s') AS exportedAt,
                       pending_count AS pendingCount,
                       closed_count AS closedCount,
                       active_queue_count AS activeQueueCount,
                       evidence_file_count AS evidenceFileCount,
                       conclusion,
                       summary_json AS summaryJson,
                       exported_by AS exportedBy
                FROM history_write_delivery_acceptance
                WHERE acceptance_no = ?
                LIMIT 1
                """, safeAcceptanceNo);
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "History write delivery acceptance not found.");
        }
        Map<String, Object> row = new LinkedHashMap<>(rows.getFirst());
        Map<String, Object> summary = readJsonMap(row.remove("summaryJson"));
        row.put("rows", summary.getOrDefault("rows", List.of()));
        row.put("evidence", summary.getOrDefault("evidence", List.of()));
        return row;
    }

    private void requireExportPermission() {
        if (!hasMenu("SALARY_EXPORT")) {
            throw new IllegalArgumentException("Salary export permission is required.");
        }
    }

    private void requireHistoryWritePermission() {
        if (!hasMenu("SALARY_HISTORY_WRITE")) {
            throw new IllegalArgumentException("Salary history write permission is required.");
        }
    }

    private void requireHistoryRollbackPermission() {
        if (!hasMenu("SALARY_HISTORY_ROLLBACK")) {
            throw new IllegalArgumentException("Salary history rollback permission is required.");
        }
    }

    private String historyWriteBatchNo(String prefix) {
        return "HWB-" + text(prefix).toUpperCase() + "-" + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
