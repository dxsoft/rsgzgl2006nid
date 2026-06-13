package com.dx.rsgzgl.salary.controller;

import com.dx.rsgzgl.common.api.ApiResponse;
import com.dx.rsgzgl.salary.dto.NormalGradeBatchTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeBatchTrialResult;
import com.dx.rsgzgl.salary.dto.NormalGradeTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeTrialResult;
import com.dx.rsgzgl.salary.dto.SalaryCalculationCommand;
import com.dx.rsgzgl.salary.dto.SalaryCalculationResult;
import com.dx.rsgzgl.salary.dto.SalaryBatchReconcileCommand;
import com.dx.rsgzgl.salary.dto.SalaryBatchReconcileResult;
import com.dx.rsgzgl.salary.dto.SalaryGeneratedTimelineResult;
import com.dx.rsgzgl.salary.dto.SalaryHistoryItem;
import com.dx.rsgzgl.salary.dto.SalaryPeriodItem;
import com.dx.rsgzgl.salary.dto.SalaryReconcileCommand;
import com.dx.rsgzgl.salary.dto.SalaryReconcileResult;
import com.dx.rsgzgl.salary.dto.SalaryTimelineResult;
import com.dx.rsgzgl.salary.service.NormalGradeBatchTrialService;
import com.dx.rsgzgl.salary.service.NormalGradeTrialService;
import com.dx.rsgzgl.salary.service.SalaryBatchReconcileService;
import com.dx.rsgzgl.salary.service.SalaryCalculationService;
import com.dx.rsgzgl.salary.service.SalaryGeneratedTimelineService;
import com.dx.rsgzgl.salary.service.SalaryHistoryService;
import com.dx.rsgzgl.salary.service.SalaryPeriodService;
import com.dx.rsgzgl.salary.service.SalaryReconcileService;
import com.dx.rsgzgl.salary.service.SalaryTimelineService;
import com.dx.rsgzgl.system.service.OrganizationAccessService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/salary")
public class SalaryController {

    private final SalaryCalculationService salaryCalculationService;
    private final SalaryHistoryService salaryHistoryService;
    private final SalaryReconcileService salaryReconcileService;
    private final SalaryBatchReconcileService salaryBatchReconcileService;
    private final SalaryPeriodService salaryPeriodService;
    private final NormalGradeTrialService normalGradeTrialService;
    private final NormalGradeBatchTrialService normalGradeBatchTrialService;
    private final SalaryTimelineService salaryTimelineService;
    private final SalaryGeneratedTimelineService salaryGeneratedTimelineService;
    private final OrganizationAccessService organizationAccessService;

    public SalaryController(
            SalaryCalculationService salaryCalculationService,
            SalaryHistoryService salaryHistoryService,
            SalaryReconcileService salaryReconcileService,
            SalaryBatchReconcileService salaryBatchReconcileService,
            SalaryPeriodService salaryPeriodService,
            NormalGradeTrialService normalGradeTrialService,
            NormalGradeBatchTrialService normalGradeBatchTrialService,
            SalaryTimelineService salaryTimelineService,
            SalaryGeneratedTimelineService salaryGeneratedTimelineService,
            OrganizationAccessService organizationAccessService
    ) {
        this.salaryCalculationService = salaryCalculationService;
        this.salaryHistoryService = salaryHistoryService;
        this.salaryReconcileService = salaryReconcileService;
        this.salaryBatchReconcileService = salaryBatchReconcileService;
        this.salaryPeriodService = salaryPeriodService;
        this.normalGradeTrialService = normalGradeTrialService;
        this.normalGradeBatchTrialService = normalGradeBatchTrialService;
        this.salaryTimelineService = salaryTimelineService;
        this.salaryGeneratedTimelineService = salaryGeneratedTimelineService;
        this.organizationAccessService = organizationAccessService;
    }

    @GetMapping("/periods")
    public ApiResponse<List<SalaryPeriodItem>> periods(
            @RequestParam String orgCode,
            @RequestParam(required = false) Integer limit
    ) {
        organizationAccessService.requireOrgAccess(orgCode);
        return ApiResponse.ok(salaryPeriodService.periods(orgCode, limit));
    }

    @GetMapping("/history/{personCode}")
    public ApiResponse<List<SalaryHistoryItem>> history(@PathVariable String personCode) {
        organizationAccessService.requirePersonAccess(personCode, null);
        return ApiResponse.ok(salaryHistoryService.history(personCode));
    }

    @GetMapping("/timeline/{personCode}")
    public ApiResponse<SalaryTimelineResult> timeline(
            @PathVariable String personCode,
            @RequestParam(required = false) Integer limit
    ) {
        organizationAccessService.requirePersonAccess(personCode, null);
        return ApiResponse.ok(salaryTimelineService.replay(personCode, limit));
    }

    @GetMapping("/timeline-generated/{personCode}")
    public ApiResponse<SalaryGeneratedTimelineResult> generatedTimeline(
            @PathVariable String personCode,
            @RequestParam(required = false) Integer limit
    ) {
        organizationAccessService.requirePersonAccess(personCode, null);
        return ApiResponse.ok(salaryGeneratedTimelineService.generateAndCompare(personCode, limit));
    }

    @GetMapping("/history-records/{historyId}")
    public ApiResponse<SalaryCalculationResult> historyDetail(@PathVariable String historyId) {
        organizationAccessService.requireHistoryAccess(historyId);
        return ApiResponse.ok(salaryHistoryService.detail(historyId));
    }

    @PostMapping("/trial-calc")
    public ApiResponse<SalaryCalculationResult> trialCalculate(@Valid @RequestBody SalaryCalculationCommand command) {
        organizationAccessService.requirePersonAccess(command.personCode(), command.orgCode());
        return ApiResponse.ok(salaryCalculationService.calculate(command));
    }

    @PostMapping("/rule-trial/normal-grade")
    public ApiResponse<NormalGradeTrialResult> normalGradeTrial(@Valid @RequestBody NormalGradeTrialCommand command) {
        organizationAccessService.requirePersonAccess(command.personCode(), command.orgCode());
        return ApiResponse.ok(normalGradeTrialService.trial(command));
    }

    @PostMapping("/rule-trial/normal-grade-batch")
    public ApiResponse<NormalGradeBatchTrialResult> normalGradeBatchTrial(@Valid @RequestBody NormalGradeBatchTrialCommand command) {
        organizationAccessService.requireOrgAccess(command.orgCode());
        return ApiResponse.ok(normalGradeBatchTrialService.trial(command));
    }

    @PostMapping("/reconcile")
    public ApiResponse<SalaryReconcileResult> reconcile(@Valid @RequestBody SalaryReconcileCommand command) {
        organizationAccessService.requirePersonAccess(command.personCode(), command.orgCode());
        return ApiResponse.ok(salaryReconcileService.reconcile(command));
    }

    @PostMapping("/reconcile-batch")
    public ApiResponse<SalaryBatchReconcileResult> reconcileBatch(@Valid @RequestBody SalaryBatchReconcileCommand command) {
        organizationAccessService.requireOrgAccess(command.orgCode());
        return ApiResponse.ok(salaryBatchReconcileService.reconcile(command));
    }

    @GetMapping(value = "/reconcile-batch.csv", produces = "text/csv")
    public ResponseEntity<byte[]> reconcileBatchCsv(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String changeType
    ) {
        organizationAccessService.requireOrgAccess(orgCode);
        SalaryBatchReconcileResult result = salaryBatchReconcileService.reconcile(
                new SalaryBatchReconcileCommand(orgCode, year, month, limit, changeType)
        );
        byte[] body = withUtf8Bom(toCsv(result));
        String filename = "salary-reconcile-" + result.orgCode() + "-" + result.year() + "-" + String.format("%02d", result.month()) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping(value = "/rule-trial/normal-grade-batch.csv", produces = "text/csv")
    public ResponseEntity<byte[]> normalGradeBatchTrialCsv(
            @RequestParam String orgCode,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String changeType
    ) {
        organizationAccessService.requireOrgAccess(orgCode);
        NormalGradeBatchTrialResult result = normalGradeBatchTrialService.trial(
                new NormalGradeBatchTrialCommand(orgCode, year, month, limit, changeType)
        );
        byte[] body = withUtf8Bom(toCsv(result));
        String filename = "normal-grade-trial-" + result.orgCode() + "-" + result.year() + "-" + String.format("%02d", result.month()) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    private String toCsv(SalaryBatchReconcileResult result) {
        StringBuilder csv = new StringBuilder();
        csv.append("单位编码,年度,月份,检查人数,通过人数,差异人数,跳过人数,差额合计").append('\n');
        csv.setLength(0);
        csv.append("\u5355\u4f4d\u7f16\u7801,\u5e74\u5ea6,\u6708\u4efd,\u68c0\u67e5\u4eba\u6570,\u901a\u8fc7\u4eba\u6570,\u5dee\u5f02\u4eba\u6570,\u8df3\u8fc7\u4eba\u6570,\u5dee\u989d\u5408\u8ba1").append('\n');
        csv.append(csv(result.orgCode())).append(',')
                .append(result.year()).append(',')
                .append(result.month()).append(',')
                .append(result.checkedCount()).append(',')
                .append(result.passedCount()).append(',')
                .append(result.failedCount()).append(',')
                .append(result.skippedCount()).append(',')
                .append(amount(result.totalDifference())).append('\n')
                .append('\n');
        csv.append("人员编码,姓名,单位编码,单位名称,老系统金额,试算金额,差额,状态,消息").append('\n');
        csv.setLength(csv.lastIndexOf("\n\n") + 2);
        csv.append("\u4eba\u5458\u7f16\u7801,\u59d3\u540d,\u5355\u4f4d\u7f16\u7801,\u5355\u4f4d\u540d\u79f0,\u8001\u7cfb\u7edf\u91d1\u989d,\u8bd5\u7b97\u91d1\u989d,\u5dee\u989d,\u72b6\u6001,\u6d88\u606f").append('\n');
        result.items().forEach(item -> csv.append(csv(item.personCode())).append(',')
                .append(csv(item.personName())).append(',')
                .append(csv(item.orgCode())).append(',')
                .append(csv(item.orgName())).append(',')
                .append(amount(item.legacyTotalAmount())).append(',')
                .append(amount(item.calculatedTotalAmount())).append(',')
                .append(amount(item.difference())).append(',')
                .append(csv(item.status())).append(',')
                .append(csv(item.message())).append('\n'));
        return csv.toString();
    }

    private String toCsv(NormalGradeBatchTrialResult result) {
        StringBuilder csv = new StringBuilder();
        csv.append("\u5355\u4f4d\u7f16\u7801,\u5e74\u5ea6,\u6708\u4efd,\u68c0\u67e5\u4eba\u6570,\u5339\u914d\u4eba\u6570,\u5dee\u5f02\u4eba\u6570,\u65e0\u76ee\u6807\u8bb0\u5f55,\u8df3\u8fc7\u4eba\u6570,\u5012\u6863\u5dee\u4eba\u6570,\u7ea7\u522b\u664b\u5347\u4eba\u6570,\u4e0d\u7b26\u5408\u6761\u4ef6\u4eba\u6570,\u5dee\u989d\u5408\u8ba1").append('\n');
        csv.append(csv(result.orgCode())).append(',')
                .append(result.year()).append(',')
                .append(result.month()).append(',')
                .append(result.checkedCount()).append(',')
                .append(result.matchedCount()).append(',')
                .append(result.differentCount()).append(',')
                .append(result.noExpectedCount()).append(',')
                .append(result.skippedCount()).append(',')
                .append(result.reverseStepCount()).append(',')
                .append(result.levelPromotionCount()).append(',')
                .append(result.notEligibleCount()).append(',')
                .append(amount(result.totalDifference())).append('\n')
                .append('\n');
        csv.append("\u4eba\u5458\u7f16\u7801,\u59d3\u540d,\u5355\u4f4d\u7f16\u7801,\u5355\u4f4d\u540d\u79f0,\u57fa\u7ebf\u8bb0\u5f55,\u76ee\u6807\u8bb0\u5f55,\u57fa\u7ebf\u5408\u8ba1,\u8bd5\u7b97\u5408\u8ba1,\u5386\u53f2\u5408\u8ba1,\u5dee\u989d,\u539f\u6863\u6b21,\u65b0\u6863\u6b21,\u539f\u7ea7\u522b\u5de5\u8d44,\u65b0\u7ea7\u522b\u5de5\u8d44,\u589e\u989d,\u89c4\u5219\u7c7b\u578b,\u89c4\u5219\u8bf4\u660e,\u72b6\u6001,\u6d88\u606f").append('\n');
        result.items().forEach(item -> csv.append(csv(item.personCode())).append(',')
                .append(csv(item.personName())).append(',')
                .append(csv(item.orgCode())).append(',')
                .append(csv(item.orgName())).append(',')
                .append(csv(item.baselineHistoryId())).append(',')
                .append(csv(item.expectedHistoryId())).append(',')
                .append(amount(item.baselineTotalAmount())).append(',')
                .append(amount(item.calculatedTotalAmount())).append(',')
                .append(amount(item.expectedTotalAmount())).append(',')
                .append(amount(item.differenceWithExpected())).append(',')
                .append(csv(item.beforeValue())).append(',')
                .append(csv(item.afterValue())).append(',')
                .append(amount(item.beforeAmount())).append(',')
                .append(amount(item.afterAmount())).append(',')
                .append(amount(item.changeAmount())).append(',')
                .append(csv(item.ruleType())).append(',')
                .append(csv(item.ruleNote())).append(',')
                .append(csv(item.status())).append(',')
                .append(csv(item.message())).append('\n'));
        return csv.toString();
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

    private String csv(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private String amount(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }
}
