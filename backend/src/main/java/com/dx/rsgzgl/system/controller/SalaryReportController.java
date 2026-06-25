package com.dx.rsgzgl.system.controller;

import com.dx.rsgzgl.common.api.ApiResponse;
import com.dx.rsgzgl.system.service.CurrentUserService;
import com.dx.rsgzgl.system.service.OrganizationAccessService;
import com.dx.rsgzgl.system.service.SystemAuditService;
import com.dx.rsgzgl.system.service.UserPermissionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/reports")
public class SalaryReportController {

    private static final Set<String> STANDARD_REPORT_TABLES = Set.of(
            "bz06_blfb",
            "bz06_djgz",
            "bz06_fjtgb",
            "bz06_jbgz",
            "bz06_jbt",
            "bz06_jjjy",
            "bz06_jxgz",
            "bz06_tgb",
            "bz06_xjgz",
            "bz06_zw_gw",
            "bz06_zw_jb_xj",
            "bz06_zwgz",
            "bz06_zwgz_fj",
            "bz06_zwgz_gr",
            "bz06_zzdz"
    );

    private final JdbcTemplate jdbcTemplate;
    private final CurrentUserService currentUserService;
    private final UserPermissionService userPermissionService;
    private final OrganizationAccessService organizationAccessService;
    private final SystemAuditService systemAuditService;
    private final ObjectMapper objectMapper;

    public SalaryReportController(
            JdbcTemplate jdbcTemplate,
            CurrentUserService currentUserService,
            UserPermissionService userPermissionService,
            OrganizationAccessService organizationAccessService,
            SystemAuditService systemAuditService,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.currentUserService = currentUserService;
        this.userPermissionService = userPermissionService;
        this.organizationAccessService = organizationAccessService;
        this.systemAuditService = systemAuditService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/catalog")
    public ApiResponse<List<ReportCatalogItem>> catalog() {
        requireReportPermission();
        return ApiResponse.ok(reportCatalog());
    }

    @GetMapping(value = "/catalog.csv", produces = "text/csv")
    public ResponseEntity<byte[]> catalogCsv() {
        requireReportPermission();
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "报表编码", "报表名称", "类别", "旧模板", "迁移状态", "打印入口");
        for (ReportCatalogItem item : reportCatalog()) {
            csv.append(csv(item.code())).append(',')
                    .append(csv(item.title())).append(',')
                    .append(csv(item.category())).append(',')
                    .append(csv(item.legacyTemplate())).append(',')
                    .append(csv(item.migrationStatus())).append(',')
                    .append(csv(item.printUrl())).append('\n');
        }
        return csvResponse("salary-report-catalog.csv", csv.toString());
    }

    @GetMapping("/migration-matrix")
    public ApiResponse<Map<String, Object>> reportMigrationMatrix() {
        requireReportPermission();
        List<Map<String, Object>> rows = reportMigrationMatrixRows();
        long migrated = rows.stream().filter(row -> "MIGRATED".equals(text(row.get("status")))).count();
        long pending = rows.stream().filter(row -> "PENDING".equals(text(row.get("status")))).count();
        long partial = rows.stream().filter(row -> "PARTIAL".equals(text(row.get("status")))).count();
        return ApiResponse.ok(Map.of(
                "items", rows,
                "total", rows.size(),
                "migrated", migrated,
                "pending", pending,
                "partial", partial,
                "checkedAt", LocalDateTime.now().withNano(0).toString()
        ));
    }

    @GetMapping(value = "/migration-matrix.csv", produces = "text/csv")
    public ResponseEntity<byte[]> reportMigrationMatrixCsv() {
        requireReportPermission();
        String csv = reportMigrationMatrixCsvContent();
        systemAuditService.record("report", "report-migration-matrix-csv", "REPORT_MIGRATION_MATRIX", "REPORT_PRINT",
                reportAuditSummary(
                        auditPart("rows", reportMigrationMatrixRows().size()),
                        auditPart("status", "READY")
                ));
        return csvResponse("salary-report-migration-matrix.csv", csv);
    }

    @GetMapping("/migration-acceptance-checklist")
    public ApiResponse<Map<String, Object>> reportMigrationAcceptanceChecklist(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String personCode,
            @RequestParam(defaultValue = "2006") int yearFrom,
            @RequestParam(defaultValue = "2099") int yearTo,
            @RequestParam(defaultValue = "300") int limit
    ) {
        requireReportPermission();
        Map<String, Object> result = reportMigrationAcceptanceChecklistResult(orgCode, year, month, businessType, keyword, personCode, yearFrom, yearTo, limit);
        return ApiResponse.ok(result);
    }

    @GetMapping(value = "/migration-acceptance-checklist.csv", produces = "text/csv")
    public ResponseEntity<byte[]> reportMigrationAcceptanceChecklistCsv(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String personCode,
            @RequestParam(defaultValue = "2006") int yearFrom,
            @RequestParam(defaultValue = "2099") int yearTo,
            @RequestParam(defaultValue = "300") int limit
    ) {
        requireReportPermission();
        Map<String, Object> result = reportMigrationAcceptanceChecklistResult(orgCode, year, month, businessType, keyword, personCode, yearFrom, yearTo, limit);
        String csv = reportMigrationAcceptanceChecklistCsvContent(result);
        String safeOrgCode = text(result.get("orgCode"));
        systemAuditService.record("report", "report-migration-acceptance-checklist-csv", "REPORT_MIGRATION_ACCEPTANCE", safeOrgCode.isBlank() ? "ALL" : safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode.isBlank() ? "ALL" : safeOrgCode),
                        auditPart("period", result.get("period")),
                        auditPart("status", result.get("status")),
                        auditPart("pass", result.get("pass")),
                        auditPart("todo", result.get("todo")),
                        auditPart("warn", result.get("warn"))
                ));
        String suffix = safeOrgCode.isBlank() ? "all" : safeOrgCode;
        return csvResponse("salary-report-migration-acceptance-checklist-" + suffix + ".csv", csv);
    }

    @GetMapping("/migration-sample-evidence")
    public ApiResponse<Map<String, Object>> reportMigrationSampleEvidence(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String personCode,
            @RequestParam(defaultValue = "2006") int yearFrom,
            @RequestParam(defaultValue = "2099") int yearTo,
            @RequestParam(defaultValue = "5") int limit
    ) {
        requireReportPermission();
        Map<String, Object> result = reportMigrationSampleEvidenceResult(orgCode, year, month, businessType, keyword, personCode, yearFrom, yearTo, limit);
        return ApiResponse.ok(result);
    }

    @GetMapping(value = "/migration-sample-evidence.csv", produces = "text/csv")
    public ResponseEntity<byte[]> reportMigrationSampleEvidenceCsv(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String personCode,
            @RequestParam(defaultValue = "2006") int yearFrom,
            @RequestParam(defaultValue = "2099") int yearTo,
            @RequestParam(defaultValue = "5") int limit
    ) {
        requireReportPermission();
        Map<String, Object> result = reportMigrationSampleEvidenceResult(orgCode, year, month, businessType, keyword, personCode, yearFrom, yearTo, limit);
        String csv = reportMigrationSampleEvidenceCsvContent(result);
        String safeOrgCode = text(result.get("orgCode"));
        systemAuditService.record("report", "report-migration-sample-evidence-csv", "REPORT_MIGRATION_SAMPLE", safeOrgCode.isBlank() ? "ALL" : safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode.isBlank() ? "ALL" : safeOrgCode),
                        auditPart("period", result.get("period")),
                        auditPart("rows", result.get("rows")),
                        auditPart("limit", result.get("limit"))
                ));
        String suffix = safeOrgCode.isBlank() ? "all" : safeOrgCode;
        return csvResponse("salary-report-migration-sample-evidence-" + suffix + ".csv", csv);
    }

    @GetMapping(value = "/migration-guide.csv", produces = "text/csv")
    public ResponseEntity<byte[]> reportMigrationGuideCsv() {
        requireReportPermission();
        String csv = reportMigrationGuideCsvContent();
        systemAuditService.record("report", "report-migration-guide-csv", "REPORT_MIGRATION_GUIDE", "REPORT_PRINT",
                reportAuditSummary(
                        auditPart("sections", 6),
                        auditPart("status", "READY")
                ));
        return csvResponse("salary-report-migration-guide.csv", csv);
    }

    @GetMapping(value = "/migration-delivery-package.zip", produces = "application/zip")
    public ResponseEntity<byte[]> reportMigrationDeliveryPackage(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "300") int limit
    ) throws IOException {
        Map<String, Object> result = reportMigrationClosure(orgCode, year, month, businessType, keyword, limit).data();
        String safeOrgCode = text(result.get("orgCode"));
        String suffix = safeOrgCode.isBlank() ? "all" : safeOrgCode;
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("README.txt", """
                Report print migration delivery package
                Summary:
                - orgCode: %s
                - status: %s
                - fileCount: 10
                Files:
                - salary-report-catalog.csv
                - salary-report-migration-matrix.csv
                - salary-report-migration-acceptance-checklist-%s.csv
                - salary-report-migration-sample-evidence-%s.csv
                - salary-report-migration-guide.csv
                - salary-report-migration-closure-%s.csv
                - salary-report-print-self-check-%s.csv
                - delivery-package-meta.csv
                - delivery-package-audits.csv
                GeneratedAt: %s
                """.formatted(safeOrgCode.isBlank() ? "ALL" : safeOrgCode, result.get("status"), suffix, suffix, suffix, suffix, LocalDateTime.now().withNano(0)));
        entries.put("salary-report-catalog.csv", reportCatalogCsvContent());
        entries.put("salary-report-migration-matrix.csv", reportMigrationMatrixCsvContent());
        entries.put("salary-report-migration-acceptance-checklist-" + suffix + ".csv",
                reportMigrationAcceptanceChecklistCsvContent(reportMigrationAcceptanceChecklistResult(safeOrgCode, year, month, businessType, keyword, "", 2006, 2099, limit)));
        entries.put("salary-report-migration-sample-evidence-" + suffix + ".csv",
                reportMigrationSampleEvidenceCsvContent(reportMigrationSampleEvidenceResult(safeOrgCode, year, month, businessType, keyword, "", 2006, 2099, Math.min(limit, 20))));
        entries.put("salary-report-migration-guide.csv", reportMigrationGuideCsvContent());
        entries.put("salary-report-migration-closure-" + suffix + ".csv", reportMigrationClosureCsvContent(result));
        entries.put("salary-report-print-self-check-" + suffix + ".csv", reportPrintSelfCheckCsvContent(result));
        entries.put("delivery-package-meta.csv", reportMigrationDeliveryPackageMetaCsv(result, 10));
        systemAuditService.record("report", "report-migration-delivery-package", "REPORT_MIGRATION_DELIVERY", safeOrgCode.isBlank() ? "ALL" : safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode.isBlank() ? "ALL" : safeOrgCode),
                        auditPart("status", result.get("status")),
                        auditPart("files", 10)
                ));
        entries.put("delivery-package-audits.csv", reportMigrationDeliveryAuditsCsv(safeOrgCode.isBlank() ? "ALL" : safeOrgCode));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("salary-report-migration-delivery-" + suffix + ".zip", StandardCharsets.UTF_8)
                        .build().toString())
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zipResponse(entries));
    }

    @GetMapping("/migration-closure")
    public ApiResponse<Map<String, Object>> reportMigrationClosure(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "300") int limit
    ) {
        requireReportPermission();
        ensureReportPrintBatchTables();
        systemAuditService.ensureTable();
        systemAuditService.ensureTable();
        String safeOrgCode = text(orgCode);
        if (!safeOrgCode.isBlank()) {
            organizationAccessService.requireOrgAccess(safeOrgCode);
        }
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : 0;
        int safeMonth = month > 0 ? Math.max(1, Math.min(month, 12)) : 0;
        int safeLimit = Math.max(1, Math.min(limit, 10000));
        List<ReportCatalogItem> catalog = reportCatalog();
        long migratedReports = catalog.stream().filter(item -> !text(item.printUrl()).isBlank()).count();
        long pendingReports = Math.max(0, catalog.size() - migratedReports);

        List<Map<String, Object>> archiveRows = reportPrintArchiveRows(safeOrgCode, safeYear, safeMonth, businessType, keyword, "ALL", safeLimit);
        long printed = archiveRows.stream().filter(row -> Boolean.TRUE.equals(booleanValue(row.get("printed")))).count();
        long unprinted = archiveRows.size() - printed;
        long reprinted = archiveRows.stream().filter(row -> Boolean.TRUE.equals(booleanValue(row.get("reprinted")))).count();
        long writeReady = archiveRows.stream().filter(row -> Boolean.TRUE.equals(booleanValue(row.get("writeReady")))).count();

        Map<String, Object> batchSummary = reportMigrationClosureBatchSummary(safeOrgCode, safeYear, safeMonth, keyword);
        Map<String, Object> auditSummary = reportMigrationClosureAuditSummary(safeOrgCode, safeYear, safeMonth, keyword);
        long blockedBatches = number(batchSummary.get("blockedBatches"));
        long pendingAcceptanceBatches = number(batchSummary.get("pendingAcceptanceBatches"));
        long auditCount = number(auditSummary.get("auditCount"));
        String status = pendingReports > 0 || unprinted > 0 || blockedBatches > 0 || pendingAcceptanceBatches > 0 || auditCount == 0 ? "WARN" : "READY";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", status);
        result.put("orgCode", safeOrgCode);
        result.put("year", safeYear);
        result.put("month", safeMonth);
        result.put("businessType", text(businessType));
        result.put("keyword", text(keyword));
        result.put("limit", safeLimit);
        result.put("catalogTotal", catalog.size());
        result.put("migratedReports", migratedReports);
        result.put("pendingReports", pendingReports);
        result.put("archiveTotal", archiveRows.size());
        result.put("printed", printed);
        result.put("unprinted", unprinted);
        result.put("reprinted", reprinted);
        result.put("writeReady", writeReady);
        result.put("batchSummary", batchSummary);
        result.put("auditSummary", auditSummary);
        result.put("checkedAt", LocalDateTime.now().withNano(0).toString());
        return ApiResponse.ok(result);
    }

    @GetMapping(value = "/migration-closure.csv", produces = "text/csv")
    public ResponseEntity<byte[]> reportMigrationClosureCsv(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "300") int limit
    ) {
        Map<String, Object> result = reportMigrationClosure(orgCode, year, month, businessType, keyword, limit).data();
        @SuppressWarnings("unchecked")
        Map<String, Object> batch = (Map<String, Object>) result.getOrDefault("batchSummary", Map.of());
        @SuppressWarnings("unchecked")
        Map<String, Object> audit = (Map<String, Object>) result.getOrDefault("auditSummary", Map.of());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> actions = (List<Map<String, Object>>) audit.getOrDefault("actions", List.of());
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "section", "item", "value");
        csvRow(csv, "summary", "status", result.get("status"));
        csvRow(csv, "summary", "orgCode", result.get("orgCode"));
        csvRow(csv, "summary", "year", result.get("year"));
        csvRow(csv, "summary", "month", result.get("month"));
        csvRow(csv, "summary", "businessType", result.get("businessType"));
        csvRow(csv, "summary", "keyword", result.get("keyword"));
        csvRow(csv, "summary", "checkedAt", result.get("checkedAt"));
        csvRow(csv, "catalog", "catalogTotal", result.get("catalogTotal"));
        csvRow(csv, "catalog", "migratedReports", result.get("migratedReports"));
        csvRow(csv, "catalog", "pendingReports", result.get("pendingReports"));
        csvRow(csv, "archive", "archiveTotal", result.get("archiveTotal"));
        csvRow(csv, "archive", "printed", result.get("printed"));
        csvRow(csv, "archive", "unprinted", result.get("unprinted"));
        csvRow(csv, "archive", "reprinted", result.get("reprinted"));
        csvRow(csv, "archive", "writeReady", result.get("writeReady"));
        csvRow(csv, "batch", "batchCount", batch.get("batchCount"));
        csvRow(csv, "batch", "printedRows", batch.get("printedRows"));
        csvRow(csv, "batch", "blockedRows", batch.get("blockedRows"));
        csvRow(csv, "batch", "warningRows", batch.get("warningRows"));
        csvRow(csv, "batch", "blockedBatches", batch.get("blockedBatches"));
        csvRow(csv, "batch", "pendingAcceptanceBatches", batch.get("pendingAcceptanceBatches"));
        csvRow(csv, "batch", "acceptanceExportedBatches", batch.get("acceptanceExportedBatches"));
        csvRow(csv, "batch", "latestPrintedAt", batch.get("latestPrintedAt"));
        csvRow(csv, "batch", "latestAcceptanceExportedAt", batch.get("latestAcceptanceExportedAt"));
        csvRow(csv, "audit", "auditCount", audit.get("auditCount"));
        csvRow(csv, "audit", "latestAuditAt", audit.get("latestAuditAt"));
        for (Map<String, Object> action : actions) {
            csvRow(csv, "auditAction", action.get("actionName"), action.get("actionCount"));
        }
        String safeOrgCode = text(result.get("orgCode"));
        systemAuditService.record("report", "report-migration-closure-csv", "REPORT_MIGRATION_CLOSURE", safeOrgCode.isBlank() ? "ALL" : safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode.isBlank() ? "ALL" : safeOrgCode),
                        auditPart("period", number(result.get("year")) > 0 ? periodText(number(result.get("year")), Math.max(1, number(result.get("month")))) : "ALL"),
                        auditPart("status", result.get("status")),
                        auditPart("pendingReports", result.get("pendingReports")),
                        auditPart("unprinted", result.get("unprinted")),
                        auditPart("blockedRows", batch.get("blockedRows")),
                        auditPart("pendingAcceptanceBatches", batch.get("pendingAcceptanceBatches")),
                        auditPart("auditCount", audit.get("auditCount"))
                ));
        String suffix = safeOrgCode.isBlank() ? "all" : safeOrgCode;
        return csvResponse("salary-report-migration-closure-" + suffix + ".csv", csv.toString());
    }

    @GetMapping(value = "/migration-print-self-check.csv", produces = "text/csv")
    public ResponseEntity<byte[]> reportPrintSelfCheckCsv(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "300") int limit
    ) {
        Map<String, Object> result = reportMigrationClosure(orgCode, year, month, businessType, keyword, limit).data();
        @SuppressWarnings("unchecked")
        Map<String, Object> batch = (Map<String, Object>) result.getOrDefault("batchSummary", Map.of());
        @SuppressWarnings("unchecked")
        Map<String, Object> audit = (Map<String, Object>) result.getOrDefault("auditSummary", Map.of());
        long archiveTotal = number(result.get("archiveTotal"));
        long printed = number(result.get("printed"));
        long pendingAcceptance = number(batch.get("pendingAcceptanceBatches"));
        long exportedAcceptance = number(batch.get("acceptanceExportedBatches"));
        long blockedBatches = number(batch.get("blockedBatches"));
        long auditCount = number(audit.get("auditCount"));
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "filter", "orgCode", text(result.get("orgCode")).isBlank() ? "ALL" : result.get("orgCode"));
        csvRow(csv, "filter", "period", number(result.get("year")) > 0 ? periodText(number(result.get("year")), Math.max(1, number(result.get("month")))) : "ALL");
        csvRow(csv, "filter", "businessType", text(result.get("businessType")).isBlank() ? "ALL" : result.get("businessType"));
        csvRow(csv, "filter", "keyword", text(result.get("keyword")).isBlank() ? "ALL" : result.get("keyword"));
        csvRow(csv, "filter", "limit", result.get("limit"));
        csvRow(csv, "filter", "checkedAt", result.get("checkedAt"));
        csv.append('\n');
        csvRow(csv, "item", "status", "metric", "detail");
        csvRow(csv, "archive", archiveTotal == 0 || printed > 0 ? "PASS" : "TODO", "approvalArchive",
                "printed=" + printed + ";archiveTotal=" + archiveTotal + ";unprinted=" + number(result.get("unprinted")));
        csvRow(csv, "batch", number(batch.get("batchCount")) > 0 ? "PASS" : "TODO", "printBatch",
                "batchCount=" + number(batch.get("batchCount")) + ";printedRows=" + number(batch.get("printedRows")) + ";blockedRows=" + number(batch.get("blockedRows")));
        csvRow(csv, "acceptance", pendingAcceptance == 0 && exportedAcceptance > 0 ? "PASS" : "TODO", "acceptancePackage",
                "exported=" + exportedAcceptance + ";pending=" + pendingAcceptance + ";latest=" + text(batch.get("latestAcceptanceExportedAt")));
        csvRow(csv, "audit", auditCount > 0 ? "PASS" : "TODO", "auditTrail",
                "auditCount=" + auditCount + ";latest=" + text(audit.get("latestAuditAt")));
        csvRow(csv, "risk", "READY".equals(text(result.get("status"))) && blockedBatches == 0 ? "PASS" : "TODO", "closureRisk",
                "status=" + text(result.get("status")) + ";blockedBatches=" + blockedBatches + ";pendingReports=" + number(result.get("pendingReports")));
        String safeOrgCode = text(result.get("orgCode"));
        systemAuditService.record("report", "report-print-self-check-csv", "REPORT_PRINT_SELF_CHECK", safeOrgCode.isBlank() ? "ALL" : safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode.isBlank() ? "ALL" : safeOrgCode),
                        auditPart("period", number(result.get("year")) > 0 ? periodText(number(result.get("year")), Math.max(1, number(result.get("month")))) : "ALL"),
                        auditPart("status", result.get("status")),
                        auditPart("pendingAcceptanceBatches", pendingAcceptance),
                        auditPart("auditCount", auditCount)
                ));
        String suffix = safeOrgCode.isBlank() ? "all" : safeOrgCode;
        return csvResponse("salary-report-print-self-check-" + suffix + ".csv", csv.toString());
    }

    private String reportCatalogCsvContent() {
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "code", "title", "category", "legacyTemplate", "migrationStatus", "printUrl");
        for (ReportCatalogItem item : reportCatalog()) {
            csvRow(csv, item.code(), item.title(), item.category(), item.legacyTemplate(), item.migrationStatus(), item.printUrl());
        }
        return csv.toString();
    }

    private String reportMigrationMatrixCsvContent() {
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "code", "title", "category", "legacyTemplate", "status", "printUrl", "csvUrl",
                "auditActions", "validation", "acceptanceEvidence", "nextAction");
        for (Map<String, Object> row : reportMigrationMatrixRows()) {
            csvRow(csv,
                    row.get("code"),
                    row.get("title"),
                    row.get("category"),
                    row.get("legacyTemplate"),
                    row.get("status"),
                    row.get("printUrl"),
                    row.get("csvUrl"),
                    row.get("auditActions"),
                    row.get("validation"),
                    row.get("acceptanceEvidence"),
                    row.get("nextAction"));
        }
        return csv.toString();
    }

    private List<Map<String, Object>> reportMigrationMatrixRows() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ReportCatalogItem item : reportCatalog()) {
            Map<String, Object> row = new LinkedHashMap<>();
            String code = item.code();
            String printUrl = text(item.printUrl());
            boolean migrated = !printUrl.isBlank();
            row.put("code", code);
            row.put("title", item.title());
            row.put("category", item.category());
            row.put("legacyTemplate", item.legacyTemplate());
            row.put("status", migrated ? "MIGRATED" : "PENDING");
            row.put("printUrl", printUrl);
            row.put("csvUrl", reportMigrationMatrixCsvUrl(code));
            row.put("auditActions", reportMigrationMatrixAuditActions(code));
            row.put("validation", migrated ? reportMigrationMatrixValidation(code) : "Legacy template discovered; confirm business owner and data source.");
            row.put("acceptanceEvidence", migrated ? reportMigrationMatrixAcceptanceEvidence(code) : "Pending template mapping and sample print comparison.");
            row.put("nextAction", migrated ? reportMigrationMatrixMigratedNextAction(code) : "Map legacy FRX fields, add print/export endpoint, add audit and regression sample.");
            rows.add(row);
        }
        return rows;
    }

    private Map<String, Object> reportMigrationAcceptanceChecklistResult(
            String orgCode,
            int year,
            int month,
            String businessType,
            String keyword,
            String personCode,
            int yearFrom,
            int yearTo,
            int limit
    ) {
        String safeOrgCode = text(orgCode);
        if (!safeOrgCode.isBlank()) {
            organizationAccessService.requireOrgAccess(safeOrgCode);
        }
        LocalDateTime now = LocalDateTime.now();
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : now.getYear();
        int safeMonth = month > 0 ? Math.max(1, Math.min(month, 12)) : now.getMonthValue();
        int safeYearFrom = Math.max(1900, Math.min(yearFrom, 2099));
        int safeYearTo = Math.max(safeYearFrom, Math.min(yearTo, 2099));
        int safeLimit = Math.max(1, Math.min(limit, 10000));
        String safeBusinessType = text(businessType);
        String safeKeyword = text(keyword);
        String safePersonCode = text(personCode);
        List<Map<String, Object>> items = new ArrayList<>();
        boolean missingOrg = safeOrgCode.isBlank();
        long caseCount = missingOrg ? 0 : countSalaryCases(safeOrgCode, safeYear, safeMonth, safeBusinessType, safeKeyword);
        items.add(reportMigrationAcceptanceChecklistItem("approvalRoster", "Approval roster", caseCount, safeLimit, missingOrg,
                "/api/reports/salary-case-approval-roster/print", "salary-case-approval-roster-print,salary-case-approval-roster-csv",
                "Approval roster can be printed/exported for salary cases."));
        items.add(reportMigrationAcceptanceChecklistItem("approvalBatch", "Approval batch print", caseCount, Math.min(safeLimit, 500), missingOrg,
                "/api/reports/salary-case-approvals/print", "salary-case-approvals-print,salary-case-approvals-reprint",
                "Approval batch print creates report print batch archive."));
        items.add(reportMigrationAcceptanceChecklistItem("changeLedger", "Salary change ledger", caseCount, safeLimit, missingOrg,
                "/api/reports/salary-change-ledger/print", "salary-change-ledger-print,salary-change-ledger-csv",
                "Change ledger can be printed/exported for salary cases."));
        items.add(reportMigrationAcceptanceChecklistItem("personRoster", "Person roster", missingOrg ? 0 : countPersonRoster(safeOrgCode, safeYear, safeMonth, safeKeyword), safeLimit, missingOrg,
                "/api/reports/person-roster/print", "person-roster-print,person-roster-csv",
                "Person roster uses base person and latest salary context."));
        items.add(reportMigrationAcceptanceChecklistItem("salaryRoster", "Salary roster", missingOrg ? 0 : countSalaryRoster(safeOrgCode, safeYear, safeMonth), safeLimit, missingOrg,
                "/api/reports/salary-roster/print", "salary-roster-print,salary-roster-csv",
                "Salary roster uses hisbase salary rows by org and period."));
        items.add(reportMigrationAcceptanceChecklistItem("salaryHistory", "Salary history detail", missingOrg ? 0 : countSalaryHistory(safeOrgCode, safePersonCode, safeYearFrom, safeYearTo), safeLimit, missingOrg,
                "/api/reports/salary-history/print", "salary-history-print,salary-history-csv",
                "Salary history can be traced by org, person and year range."));
        items.add(reportMigrationAcceptanceChecklistItem("assessment", "Assessment summary", missingOrg ? 0 : countAssessment(safeOrgCode, safeYearFrom > 0 ? safeYearFrom : safeYear, safeKeyword), safeLimit, missingOrg,
                "/api/reports/assessment-summary/print", "assessment-summary-print",
                "Assessment summary is available for annual assessment rows."));
        items.add(reportMigrationAcceptanceChecklistItem("standardTable", "Standard table print", countStandardTable("bz06_jbt", safeKeyword), safeLimit, false,
                "/api/reports/standard-tables/print?tableName=bz06_jbt", "standard-table-print",
                "Standard table print is available for whitelisted bz06 tables."));
        items.add(reportMigrationAcceptanceChecklistItem("auditTrail", "Report audit trail", countReportAudits(safeOrgCode, safeKeyword), safeLimit, false,
                "/api/reports/audits", "report-audits-csv",
                "Report operations can be traced by action, target and operator."));
        long pass = items.stream().filter(item -> "PASS".equals(text(item.get("status")))).count();
        long warn = items.stream().filter(item -> "WARN".equals(text(item.get("status")))).count();
        long todo = items.stream().filter(item -> "TODO".equals(text(item.get("status")))).count();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", todo > 0 ? "TODO" : (warn > 0 ? "WARN" : "PASS"));
        result.put("orgCode", safeOrgCode);
        result.put("year", safeYear);
        result.put("month", safeMonth);
        result.put("period", periodText(safeYear, safeMonth));
        result.put("yearFrom", safeYearFrom);
        result.put("yearTo", safeYearTo);
        result.put("businessType", safeBusinessType);
        result.put("keyword", safeKeyword);
        result.put("personCode", safePersonCode);
        result.put("limit", safeLimit);
        result.put("pass", pass);
        result.put("warn", warn);
        result.put("todo", todo);
        result.put("items", items);
        result.put("checkedAt", LocalDateTime.now().withNano(0).toString());
        return result;
    }

    private Map<String, Object> reportMigrationAcceptanceChecklistItem(
            String code,
            String title,
            long sampleCount,
            int limit,
            boolean missingOrg,
            String endpoint,
            String auditActions,
            String acceptanceEvidence
    ) {
        Map<String, Object> item = new LinkedHashMap<>();
        String status = missingOrg ? "WARN" : (sampleCount > 0 ? "PASS" : "TODO");
        item.put("code", code);
        item.put("title", title);
        item.put("status", status);
        item.put("sampleCount", sampleCount);
        item.put("limit", limit);
        item.put("limited", sampleCount > limit);
        item.put("endpoint", endpoint);
        item.put("auditActions", auditActions);
        item.put("acceptanceEvidence", acceptanceEvidence);
        item.put("nextAction", missingOrg
                ? "Select orgCode and period, then rerun checklist."
                : (sampleCount > 0 ? "Compare a printed/exported sample with the legacy report." : "Prepare sample data or confirm this report has no records in current scope."));
        return item;
    }

    @SuppressWarnings("unchecked")
    private String reportMigrationAcceptanceChecklistCsvContent(Map<String, Object> result) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getOrDefault("items", List.of());
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "filter", "status", result.get("status"));
        csvRow(csv, "filter", "orgCode", text(result.get("orgCode")).isBlank() ? "ALL" : result.get("orgCode"));
        csvRow(csv, "filter", "period", result.get("period"));
        csvRow(csv, "filter", "yearRange", result.get("yearFrom") + "-" + result.get("yearTo"));
        csvRow(csv, "filter", "businessType", text(result.get("businessType")).isBlank() ? "ALL" : result.get("businessType"));
        csvRow(csv, "filter", "keyword", text(result.get("keyword")).isBlank() ? "ALL" : result.get("keyword"));
        csvRow(csv, "filter", "personCode", text(result.get("personCode")).isBlank() ? "ALL" : result.get("personCode"));
        csvRow(csv, "filter", "pass", result.get("pass"));
        csvRow(csv, "filter", "warn", result.get("warn"));
        csvRow(csv, "filter", "todo", result.get("todo"));
        csvRow(csv, "filter", "checkedAt", result.get("checkedAt"));
        csv.append('\n');
        csvRow(csv, "code", "title", "status", "sampleCount", "limit", "limited", "endpoint", "auditActions", "acceptanceEvidence", "nextAction");
        for (Map<String, Object> item : items) {
            csvRow(csv,
                    item.get("code"),
                    item.get("title"),
                    item.get("status"),
                    item.get("sampleCount"),
                    item.get("limit"),
                    item.get("limited"),
                    item.get("endpoint"),
                    item.get("auditActions"),
                    item.get("acceptanceEvidence"),
                    item.get("nextAction"));
        }
        return csv.toString();
    }

    private Map<String, Object> reportMigrationSampleEvidenceResult(
            String orgCode,
            int year,
            int month,
            String businessType,
            String keyword,
            String personCode,
            int yearFrom,
            int yearTo,
            int limit
    ) {
        String safeOrgCode = text(orgCode);
        if (!safeOrgCode.isBlank()) {
            organizationAccessService.requireOrgAccess(safeOrgCode);
        }
        LocalDateTime now = LocalDateTime.now();
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : now.getYear();
        int safeMonth = month > 0 ? Math.max(1, Math.min(month, 12)) : now.getMonthValue();
        int safeYearFrom = Math.max(1900, Math.min(yearFrom, 2099));
        int safeYearTo = Math.max(safeYearFrom, Math.min(yearTo, 2099));
        int safeLimit = Math.max(1, Math.min(limit, 50));
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!safeOrgCode.isBlank()) {
            rows.addAll(reportMigrationCaseEvidenceRows("approvalBatch", safeOrgCode, safeYear, safeMonth, businessType, keyword, safeLimit));
            rows.addAll(reportMigrationCaseEvidenceRows("changeLedger", safeOrgCode, safeYear, safeMonth, businessType, keyword, safeLimit));
            rows.addAll(reportMigrationPersonEvidenceRows(safeOrgCode, keyword, safeLimit));
            rows.addAll(reportMigrationSalaryRosterEvidenceRows(safeOrgCode, safeYear, safeMonth, safeLimit));
            rows.addAll(reportMigrationSalaryHistoryEvidenceRows(safeOrgCode, personCode, safeYearFrom, safeYearTo, safeLimit));
            rows.addAll(reportMigrationAssessmentEvidenceRows(safeOrgCode, safeYearFrom > 0 ? safeYearFrom : safeYear, keyword, safeLimit));
        }
        rows.addAll(reportMigrationStandardTableEvidenceRows(keyword, safeLimit));
        rows.addAll(reportMigrationAuditEvidenceRows(safeOrgCode, keyword, safeLimit));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", safeOrgCode);
        result.put("year", safeYear);
        result.put("month", safeMonth);
        result.put("period", periodText(safeYear, safeMonth));
        result.put("yearFrom", safeYearFrom);
        result.put("yearTo", safeYearTo);
        result.put("businessType", text(businessType));
        result.put("keyword", text(keyword));
        result.put("personCode", text(personCode));
        result.put("limit", safeLimit);
        result.put("rows", rows.size());
        result.put("items", rows);
        result.put("checkedAt", LocalDateTime.now().withNano(0).toString());
        return result;
    }

    @SuppressWarnings("unchecked")
    private String reportMigrationSampleEvidenceCsvContent(Map<String, Object> result) {
        List<Map<String, Object>> rows = (List<Map<String, Object>>) result.getOrDefault("items", List.of());
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "filter", "orgCode", text(result.get("orgCode")).isBlank() ? "ALL" : result.get("orgCode"));
        csvRow(csv, "filter", "period", result.get("period"));
        csvRow(csv, "filter", "yearRange", result.get("yearFrom") + "-" + result.get("yearTo"));
        csvRow(csv, "filter", "businessType", text(result.get("businessType")).isBlank() ? "ALL" : result.get("businessType"));
        csvRow(csv, "filter", "keyword", text(result.get("keyword")).isBlank() ? "ALL" : result.get("keyword"));
        csvRow(csv, "filter", "personCode", text(result.get("personCode")).isBlank() ? "ALL" : result.get("personCode"));
        csvRow(csv, "filter", "limitPerReport", result.get("limit"));
        csvRow(csv, "filter", "rows", result.get("rows"));
        csvRow(csv, "filter", "checkedAt", result.get("checkedAt"));
        csv.append('\n');
        csvRow(csv, "reportCode", "sampleKey", "personCode", "personName", "orgCode", "period", "sourceTable", "printUrl", "csvUrl", "note");
        for (Map<String, Object> row : rows) {
            csvRow(csv,
                    row.get("reportCode"),
                    row.get("sampleKey"),
                    row.get("personCode"),
                    row.get("personName"),
                    row.get("orgCode"),
                    row.get("period"),
                    row.get("sourceTable"),
                    row.get("printUrl"),
                    row.get("csvUrl"),
                    row.get("note"));
        }
        return csv.toString();
    }

    private List<Map<String, Object>> reportMigrationCaseEvidenceRows(String reportCode, String orgCode, int year, int month, String businessType, String keyword, int limit) {
        List<Object> args = new ArrayList<>();
        args.add(orgCode);
        args.add(year);
        args.add(month);
        String businessTypeWhere = "";
        if (!text(businessType).isBlank()) {
            businessTypeWhere = "AND business_type = ?";
            args.add(text(businessType));
        }
        String keywordWhere = "";
        String safeKeyword = text(keyword);
        String caseKeyword = safeKeyword.startsWith("UT-PRINT-") ? safeKeyword.substring("UT-PRINT-".length()) : "";
        if (!safeKeyword.isBlank()) {
            keywordWhere = """
                      AND (
                          case_no LIKE CONCAT('%', ?, '%')
                          __CASE_KEYWORD_WHERE__
                          OR person_code LIKE CONCAT('%', ?, '%')
                          OR person_name LIKE CONCAT('%', ?, '%')
                          OR title LIKE CONCAT('%', ?, '%')
                          OR summary LIKE CONCAT('%', ?, '%')
                      )
                    """;
            if (!caseKeyword.isBlank()) {
                keywordWhere = keywordWhere.replace("__CASE_KEYWORD_WHERE__", "OR case_no LIKE CONCAT('%', ?, '%')");
            } else {
                keywordWhere = keywordWhere.replace("__CASE_KEYWORD_WHERE__", "");
            }
            args.add(safeKeyword);
            if (!caseKeyword.isBlank()) {
                args.add(caseKeyword);
            }
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
        }
        args.add(limit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT case_no AS sampleKey, person_code AS personCode, person_name AS personName,
                       org_code AS orgCode, CONCAT(event_year, '-', LPAD(event_month, 2, '0')) AS period,
                       business_type AS note
                FROM salary_business_case
                WHERE org_code LIKE CONCAT(?, '%')
                  AND event_year = ?
                  AND event_month = ?
                __BUSINESS_TYPE_WHERE__
                __KEYWORD_WHERE__
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """.replace("__BUSINESS_TYPE_WHERE__", businessTypeWhere)
                .replace("__KEYWORD_WHERE__", keywordWhere), args.toArray());
        return rows.stream()
                .map(row -> reportMigrationEvidenceRow(reportCode, row, "salary_business_case",
                        "approvalBatch".equals(reportCode) ? "/api/reports/salary-case-approvals/print" : "/api/reports/salary-change-ledger/print",
                        "approvalBatch".equals(reportCode) ? "/api/reports/salary-case-approval-roster.csv" : "/api/reports/salary-change-ledger.csv"))
                .toList();
    }

    private List<Map<String, Object>> reportMigrationPersonEvidenceRows(String orgCode, String keyword, int limit) {
        List<Object> args = new ArrayList<>();
        args.add(orgCode);
        String keywordWhere = "";
        if (!text(keyword).isBlank()) {
            keywordWhere = "AND (TRIM(p.grbm) LIKE CONCAT('%', ?, '%') OR TRIM(COALESCE(p.xm, '')) LIKE CONCAT('%', ?, '%'))";
            args.add(text(keyword));
            args.add(text(keyword));
        }
        args.add(limit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT CONCAT(TRIM(p.dwbm), '-', TRIM(p.grbm)) AS sampleKey,
                       CONCAT(TRIM(p.dwbm), '-', TRIM(p.grbm)) AS personCode,
                       TRIM(COALESCE(p.xm, '')) AS personName,
                       TRIM(p.dwbm) AS orgCode,
                       '' AS period,
                       TRIM(COALESCE(p.sfzh, '')) AS note
                FROM dryjbxx p
                WHERE TRIM(p.dwbm) LIKE CONCAT(?, '%')
                __KEYWORD_WHERE__
                ORDER BY TRIM(p.dwbm), TRIM(p.grbm)
                LIMIT ?
                """.replace("__KEYWORD_WHERE__", keywordWhere), args.toArray());
        return rows.stream()
                .map(row -> reportMigrationEvidenceRow("personRoster", row, "dryjbxx", "/api/reports/person-roster/print", "/api/reports/person-roster.csv"))
                .toList();
    }

    private List<Map<String, Object>> reportMigrationSalaryRosterEvidenceRows(String orgCode, int year, int month, int limit) {
        int periodKey = year * 100 + month;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(h.id) AS sampleKey,
                       CONCAT(TRIM(h.dwbm), '-', TRIM(h.grbm)) AS personCode,
                       TRIM(COALESCE(p.xm, '')) AS personName,
                       TRIM(h.dwbm) AS orgCode,
                       CONCAT(TRIM(h.jsnf), '-', LPAD(TRIM(h.jsyf), 2, '0')) AS period,
                       CONCAT(TRIM(COALESCE(h.jslb, '')), ':', COALESCE(h.hj2, 0)) AS note
                FROM hisbase h
                LEFT JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE TRIM(h.dwbm) LIKE CONCAT(?, '%')
                  AND TRIM(COALESCE(h.jsnf, '')) REGEXP '^[0-9]{4}$'
                  AND TRIM(COALESCE(h.jsyf, '')) REGEXP '^[0-9]{1,2}$'
                  AND (CAST(TRIM(h.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(h.jsyf) AS UNSIGNED)) <= ?
                ORDER BY CAST(TRIM(h.jsnf) AS UNSIGNED) DESC, CAST(TRIM(h.jsyf) AS UNSIGNED) DESC, TRIM(h.id) DESC
                LIMIT ?
                """, orgCode, periodKey, limit);
        return rows.stream()
                .map(row -> reportMigrationEvidenceRow("salaryRoster", row, "hisbase", "/api/reports/salary-roster/print", "/api/reports/salary-roster.csv"))
                .toList();
    }

    private List<Map<String, Object>> reportMigrationSalaryHistoryEvidenceRows(String orgCode, String personCode, int yearFrom, int yearTo, int limit) {
        List<Object> args = new ArrayList<>();
        args.add(orgCode);
        args.add(yearFrom);
        args.add(yearTo);
        String personWhere = "";
        if (!text(personCode).isBlank()) {
            String[] parts = text(personCode).split("-", 2);
            if (parts.length == 2) {
                personWhere = "AND TRIM(h.dwbm) = ? AND TRIM(h.grbm) = ?";
                args.add(parts[0].trim());
                args.add(parts[1].trim());
            } else {
                personWhere = "AND CONCAT(TRIM(h.dwbm), '-', TRIM(h.grbm)) = ?";
                args.add(text(personCode));
            }
        }
        args.add(limit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(h.id) AS sampleKey,
                       CONCAT(TRIM(h.dwbm), '-', TRIM(h.grbm)) AS personCode,
                       TRIM(COALESCE(p.xm, '')) AS personName,
                       TRIM(h.dwbm) AS orgCode,
                       CONCAT(TRIM(h.jsnf), '-', LPAD(TRIM(h.jsyf), 2, '0')) AS period,
                       TRIM(COALESCE(h.jslb, '')) AS note
                FROM hisbase h
                LEFT JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE TRIM(h.dwbm) LIKE CONCAT(?, '%')
                  AND TRIM(COALESCE(h.jsnf, '')) REGEXP '^[0-9]{4}$'
                  AND CAST(TRIM(h.jsnf) AS UNSIGNED) BETWEEN ? AND ?
                __PERSON_WHERE__
                ORDER BY CAST(TRIM(h.jsnf) AS UNSIGNED) DESC, CAST(TRIM(COALESCE(h.jsyf, '0')) AS UNSIGNED) DESC, TRIM(h.id) DESC
                LIMIT ?
                """.replace("__PERSON_WHERE__", personWhere), args.toArray());
        return rows.stream()
                .map(row -> reportMigrationEvidenceRow("salaryHistory", row, "hisbase", "/api/reports/salary-history/print", "/api/reports/salary-history.csv"))
                .toList();
    }

    private List<Map<String, Object>> reportMigrationAssessmentEvidenceRows(String orgCode, int year, String keyword, int limit) {
        List<Object> args = new ArrayList<>();
        args.add(orgCode);
        args.add(String.valueOf(year));
        String keywordWhere = "";
        if (!text(keyword).isBlank()) {
            keywordWhere = "AND (TRIM(k.grbm) LIKE CONCAT('%', ?, '%') OR TRIM(COALESCE(p.xm, '')) LIKE CONCAT('%', ?, '%') OR TRIM(COALESCE(k.khjg, '')) LIKE CONCAT('%', ?, '%'))";
            args.add(text(keyword));
            args.add(text(keyword));
            args.add(text(keyword));
        }
        args.add(limit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT CONCAT(TRIM(k.dwbm), '-', TRIM(k.grbm), '-', TRIM(COALESCE(k.khnd, ''))) AS sampleKey,
                       CONCAT(TRIM(k.dwbm), '-', TRIM(k.grbm)) AS personCode,
                       TRIM(COALESCE(p.xm, '')) AS personName,
                       TRIM(k.dwbm) AS orgCode,
                       TRIM(COALESCE(k.khnd, '')) AS period,
                       TRIM(COALESCE(k.khjg, '')) AS note
                FROM dndkh k
                LEFT JOIN dryjbxx p ON p.dwbm = k.dwbm AND p.grbm = k.grbm
                WHERE TRIM(k.dwbm) LIKE CONCAT(?, '%')
                  AND TRIM(COALESCE(k.khnd, '')) = ?
                __KEYWORD_WHERE__
                ORDER BY TRIM(k.dwbm), TRIM(k.grbm)
                LIMIT ?
                """.replace("__KEYWORD_WHERE__", keywordWhere), args.toArray());
        return rows.stream()
                .map(row -> reportMigrationEvidenceRow("assessment", row, "dndkh", "/api/reports/assessment-summary/print", ""))
                .toList();
    }

    private List<Map<String, Object>> reportMigrationStandardTableEvidenceRows(String keyword, int limit) {
        String safeTable = "bz06_jbt";
        List<String> columns = standardTableColumns(safeTable);
        if (columns.isEmpty()) {
            return List.of();
        }
        List<Object> args = new ArrayList<>();
        String keywordWhere = "";
        if (!text(keyword).isBlank()) {
            String concatSql = columns.stream()
                    .map(column -> "COALESCE(CAST(" + quotedIdentifier(column) + " AS CHAR), '')")
                    .collect(java.util.stream.Collectors.joining(", "));
            keywordWhere = " WHERE CONCAT_WS('|', " + concatSql + ") LIKE CONCAT('%', ?, '%')";
            args.add(text(keyword));
        }
        args.add(limit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT CONCAT('row-', ROW_NUMBER() OVER (ORDER BY " + quotedIdentifier(columns.getFirst()) + ")) AS sampleKey, "
                        + "'' AS personCode, '' AS personName, '' AS orgCode, '' AS period, "
                        + "CONCAT('table=', '" + safeTable + "') AS note FROM " + quotedIdentifier(safeTable) + keywordWhere + " LIMIT ?",
                args.toArray()
        );
        return rows.stream()
                .map(row -> reportMigrationEvidenceRow("standardTable", row, safeTable, "/api/reports/standard-tables/print?tableName=bz06_jbt", ""))
                .toList();
    }

    private List<Map<String, Object>> reportMigrationAuditEvidenceRows(String orgCode, String keyword, int limit) {
        List<Object> args = new ArrayList<>();
        String targetWhere = "";
        if (!text(orgCode).isBlank()) {
            targetWhere = "AND target_code LIKE CONCAT(?, '%')";
            args.add(text(orgCode));
        }
        String actionWhere = "";
        if (!text(keyword).isBlank()) {
            actionWhere = "AND action_name LIKE CONCAT('%', ?, '%')";
            args.add(text(keyword));
        }
        args.add(limit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT CONCAT('SYS-', id) AS sampleKey,
                       '' AS personCode,
                       '' AS personName,
                       target_code AS orgCode,
                       DATE_FORMAT(created_at, '%Y-%m-%d') AS period,
                       CONCAT(action_name, ':', target_type) AS note
                FROM sys_audit_log
                WHERE module_name = 'report'
                __TARGET_WHERE__
                __ACTION_WHERE__
                ORDER BY id DESC
                LIMIT ?
                """.replace("__TARGET_WHERE__", targetWhere)
                .replace("__ACTION_WHERE__", actionWhere), args.toArray());
        return rows.stream()
                .map(row -> reportMigrationEvidenceRow("auditTrail", row, "sys_audit_log", "/api/reports/audits", "/api/reports/audits.csv"))
                .toList();
    }

    private Map<String, Object> reportMigrationEvidenceRow(String reportCode, Map<String, Object> row, String sourceTable, String printUrl, String csvUrl) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reportCode", reportCode);
        result.put("sampleKey", row.get("sampleKey"));
        result.put("personCode", row.get("personCode"));
        result.put("personName", row.get("personName"));
        result.put("orgCode", row.get("orgCode"));
        result.put("period", row.get("period"));
        result.put("sourceTable", sourceTable);
        result.put("printUrl", printUrl);
        result.put("csvUrl", csvUrl);
        result.put("note", row.get("note"));
        return result;
    }

    private String reportMigrationMatrixCsvUrl(String code) {
        return switch (text(code)) {
            case "SALARY_CHANGE_LEDGER_PRINT" -> "/api/reports/salary-change-ledger.csv?orgCode={orgCode}";
            case "PERSON_ROSTER_PRINT" -> "/api/reports/person-roster.csv?orgCode={orgCode}";
            case "SALARY_ROSTER_PRINT" -> "/api/reports/salary-roster.csv?orgCode={orgCode}";
            case "SALARY_HISTORY_PRINT" -> "/api/reports/salary-history.csv?orgCode={orgCode}";
            case "SALARY_CASE_APPROVAL_PRINT" -> "";
            case "ASSESSMENT_SUMMARY_PRINT" -> "";
            case "STANDARD_TABLE_PRINT" -> "";
            default -> "";
        };
    }

    private String reportMigrationMatrixAuditActions(String code) {
        return switch (text(code)) {
            case "SALARY_CHANGE_LEDGER_PRINT" -> "salary-change-ledger-print, salary-change-ledger-csv";
            case "PERSON_ROSTER_PRINT" -> "person-roster-print, person-roster-csv";
            case "SALARY_ROSTER_PRINT" -> "salary-roster-print, salary-roster-csv";
            case "SALARY_HISTORY_PRINT" -> "salary-history-print, salary-history-csv";
            case "SALARY_CASE_APPROVAL_PRINT" -> "salary-case-approval-print, salary-case-approvals-print, salary-case-approvals-reprint";
            case "ASSESSMENT_SUMMARY_PRINT" -> "assessment-summary-print";
            case "STANDARD_TABLE_PRINT" -> "standard-table-print";
            default -> "";
        };
    }

    private String reportMigrationMatrixValidation(String code) {
        return switch (text(code)) {
            case "SALARY_CASE_APPROVAL_PRINT" -> "Requires case snapshot, salary items, approval validation, and print archive before history write.";
            case "SALARY_CHANGE_LEDGER_PRINT" -> "Uses salary business cases and write plans; supports print and CSV count preview.";
            case "PERSON_ROSTER_PRINT" -> "Uses person base and latest salary context by org/period.";
            case "SALARY_ROSTER_PRINT" -> "Uses hisbase salary rows and selected salary columns by org/period.";
            case "SALARY_HISTORY_PRINT" -> "Uses personal hisbase chain filtered by org/person/year range.";
            case "ASSESSMENT_SUMMARY_PRINT" -> "Uses annual assessment rows and org/year filter.";
            case "STANDARD_TABLE_PRINT" -> "Uses whitelisted bz06_* standard tables.";
            default -> "Endpoint available; validate against legacy template sample.";
        };
    }

    private String reportMigrationMatrixAcceptanceEvidence(String code) {
        return switch (text(code)) {
            case "SALARY_CASE_APPROVAL_PRINT" -> "Print batch detail, acceptance package, archive ledger, and report audit log.";
            case "SALARY_CHANGE_LEDGER_PRINT", "PERSON_ROSTER_PRINT", "SALARY_ROSTER_PRINT", "SALARY_HISTORY_PRINT" ->
                    "Print/CSV audit log plus report migration delivery package.";
            case "ASSESSMENT_SUMMARY_PRINT", "STANDARD_TABLE_PRINT" ->
                    "Print audit log and report catalog/matrix entry.";
            default -> "Report catalog row only until mapped.";
        };
    }

    private String reportMigrationMatrixMigratedNextAction(String code) {
        return switch (text(code)) {
            case "SALARY_CASE_APPROVAL_PRINT" -> "Keep comparing approval form layout with legacy SPB/SPBDB samples.";
            case "SALARY_ROSTER_PRINT" -> "Add more legacy salary roster column presets if business requires.";
            case "SALARY_HISTORY_PRINT" -> "Add side-by-side legacy history sample comparison.";
            case "STANDARD_TABLE_PRINT" -> "Expand standard table labels after each bz06 table is confirmed.";
            default -> "Collect sample output and mark layout differences during acceptance.";
        };
    }

    private String reportMigrationGuideCsvContent() {
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "section", "status", "detail");
        csvRow(csv, "report-entry", "READY", "Report center covers approvals, rosters, history ledgers, statistics, and standard tables.");
        csvRow(csv, "print-batch", "READY", "Batch approval printing creates traceable print batches with detail CSV, reprint, and history queue actions.");
        csvRow(csv, "archive-ledger", "READY", "Print archive ledger links person, case, latest batch, acceptance status, and write readiness.");
        csvRow(csv, "acceptance-package", "READY", "Single and bulk acceptance packages include README, detail, audits, issue lists, and meta CSV.");
        csvRow(csv, "self-check", "READY", "Print self-check reports archive, batch, acceptance, audit, and closure-risk PASS/TODO results.");
        csvRow(csv, "audit-trace", "READY", "Printing, export, acceptance packages, and self-check documents are recorded in report audit logs.");
        csvRow(csv, "generatedAt", "INFO", LocalDateTime.now().withNano(0));
        return csv.toString();
    }

    private String reportMigrationDeliveryPackageMetaCsv(Map<String, Object> result, int fileCount) {
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "meta", "orgCode", text(result.get("orgCode")).isBlank() ? "ALL" : result.get("orgCode"));
        csvRow(csv, "meta", "period", number(result.get("year")) > 0 ? periodText(number(result.get("year")), Math.max(1, number(result.get("month")))) : "ALL");
        csvRow(csv, "meta", "businessType", text(result.get("businessType")).isBlank() ? "ALL" : result.get("businessType"));
        csvRow(csv, "meta", "keyword", text(result.get("keyword")).isBlank() ? "ALL" : result.get("keyword"));
        csvRow(csv, "meta", "limit", result.get("limit"));
        csvRow(csv, "meta", "status", result.get("status"));
        csvRow(csv, "meta", "auditAction", "report-migration-delivery-package");
        csvRow(csv, "meta", "auditTargetType", "REPORT_MIGRATION_DELIVERY");
        csvRow(csv, "meta", "fileCount", fileCount);
        csvRow(csv, "meta", "generatedAt", LocalDateTime.now().withNano(0));
        return csv.toString();
    }

    private String reportMigrationDeliveryAuditsCsv(String targetCode) {
        systemAuditService.ensureTable();
        String safeTargetCode = text(targetCode);
        List<Object> args = new ArrayList<>();
        String targetWhere = "";
        if (!safeTargetCode.isBlank() && !"ALL".equalsIgnoreCase(safeTargetCode)) {
            targetWhere = "AND target_code = ?";
            args.add(safeTargetCode);
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id, module_name, action_name, target_type, target_code, operator, created_at, summary
                FROM sys_audit_log
                WHERE module_name = 'report'
                  AND action_name IN (
                      'report-migration-delivery-package',
                      'report-migration-guide-csv',
                      'report-print-self-check-csv',
                      'report-migration-closure-csv',
                      'report-print-batch-acceptance-package',
                      'report-print-batch-acceptance-package-bulk'
                  )
                __TARGET_WHERE__
                ORDER BY id DESC
                LIMIT 100
                """.replace("__TARGET_WHERE__", targetWhere), args.toArray());
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "filter", "targetCode", safeTargetCode.isBlank() ? "ALL" : safeTargetCode);
        csvRow(csv, "filter", "rows", rows.size());
        csv.append('\n');
        csvRow(csv, "auditId", "module", "action", "targetType", "targetCode", "operator", "createdAt", "summary");
        for (Map<String, Object> row : rows) {
            csvRow(csv,
                    "SYS-" + row.get("id"),
                    row.get("module_name"),
                    row.get("action_name"),
                    row.get("target_type"),
                    row.get("target_code"),
                    row.get("operator"),
                    row.get("created_at"),
                    row.get("summary"));
        }
        return csv.toString();
    }

    @SuppressWarnings("unchecked")
    private String reportMigrationClosureCsvContent(Map<String, Object> result) {
        Map<String, Object> batch = (Map<String, Object>) result.getOrDefault("batchSummary", Map.of());
        Map<String, Object> audit = (Map<String, Object>) result.getOrDefault("auditSummary", Map.of());
        List<Map<String, Object>> actions = (List<Map<String, Object>>) audit.getOrDefault("actions", List.of());
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "section", "item", "value");
        csvRow(csv, "summary", "status", result.get("status"));
        csvRow(csv, "summary", "orgCode", result.get("orgCode"));
        csvRow(csv, "summary", "year", result.get("year"));
        csvRow(csv, "summary", "month", result.get("month"));
        csvRow(csv, "summary", "businessType", result.get("businessType"));
        csvRow(csv, "summary", "keyword", result.get("keyword"));
        csvRow(csv, "summary", "checkedAt", result.get("checkedAt"));
        csvRow(csv, "catalog", "catalogTotal", result.get("catalogTotal"));
        csvRow(csv, "catalog", "migratedReports", result.get("migratedReports"));
        csvRow(csv, "catalog", "pendingReports", result.get("pendingReports"));
        csvRow(csv, "archive", "archiveTotal", result.get("archiveTotal"));
        csvRow(csv, "archive", "printed", result.get("printed"));
        csvRow(csv, "archive", "unprinted", result.get("unprinted"));
        csvRow(csv, "archive", "reprinted", result.get("reprinted"));
        csvRow(csv, "archive", "writeReady", result.get("writeReady"));
        csvRow(csv, "batch", "batchCount", batch.get("batchCount"));
        csvRow(csv, "batch", "printedRows", batch.get("printedRows"));
        csvRow(csv, "batch", "blockedRows", batch.get("blockedRows"));
        csvRow(csv, "batch", "warningRows", batch.get("warningRows"));
        csvRow(csv, "batch", "blockedBatches", batch.get("blockedBatches"));
        csvRow(csv, "batch", "pendingAcceptanceBatches", batch.get("pendingAcceptanceBatches"));
        csvRow(csv, "batch", "acceptanceExportedBatches", batch.get("acceptanceExportedBatches"));
        csvRow(csv, "batch", "latestPrintedAt", batch.get("latestPrintedAt"));
        csvRow(csv, "batch", "latestAcceptanceExportedAt", batch.get("latestAcceptanceExportedAt"));
        csvRow(csv, "audit", "auditCount", audit.get("auditCount"));
        csvRow(csv, "audit", "latestAuditAt", audit.get("latestAuditAt"));
        for (Map<String, Object> action : actions) {
            csvRow(csv, "auditAction", action.get("actionName"), action.get("actionCount"));
        }
        return csv.toString();
    }

    @SuppressWarnings("unchecked")
    private String reportPrintSelfCheckCsvContent(Map<String, Object> result) {
        Map<String, Object> batch = (Map<String, Object>) result.getOrDefault("batchSummary", Map.of());
        Map<String, Object> audit = (Map<String, Object>) result.getOrDefault("auditSummary", Map.of());
        long archiveTotal = number(result.get("archiveTotal"));
        long printed = number(result.get("printed"));
        long pendingAcceptance = number(batch.get("pendingAcceptanceBatches"));
        long exportedAcceptance = number(batch.get("acceptanceExportedBatches"));
        long blockedBatches = number(batch.get("blockedBatches"));
        long auditCount = number(audit.get("auditCount"));
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "filter", "orgCode", text(result.get("orgCode")).isBlank() ? "ALL" : result.get("orgCode"));
        csvRow(csv, "filter", "period", number(result.get("year")) > 0 ? periodText(number(result.get("year")), Math.max(1, number(result.get("month")))) : "ALL");
        csvRow(csv, "filter", "businessType", text(result.get("businessType")).isBlank() ? "ALL" : result.get("businessType"));
        csvRow(csv, "filter", "keyword", text(result.get("keyword")).isBlank() ? "ALL" : result.get("keyword"));
        csvRow(csv, "filter", "limit", result.get("limit"));
        csvRow(csv, "filter", "checkedAt", result.get("checkedAt"));
        csv.append('\n');
        csvRow(csv, "item", "status", "metric", "detail");
        csvRow(csv, "archive", archiveTotal == 0 || printed > 0 ? "PASS" : "TODO", "approvalArchive",
                "printed=" + printed + ";archiveTotal=" + archiveTotal + ";unprinted=" + number(result.get("unprinted")));
        csvRow(csv, "batch", number(batch.get("batchCount")) > 0 ? "PASS" : "TODO", "printBatch",
                "batchCount=" + number(batch.get("batchCount")) + ";printedRows=" + number(batch.get("printedRows")) + ";blockedRows=" + number(batch.get("blockedRows")));
        csvRow(csv, "acceptance", pendingAcceptance == 0 && exportedAcceptance > 0 ? "PASS" : "TODO", "acceptancePackage",
                "exported=" + exportedAcceptance + ";pending=" + pendingAcceptance + ";latest=" + text(batch.get("latestAcceptanceExportedAt")));
        csvRow(csv, "audit", auditCount > 0 ? "PASS" : "TODO", "auditTrail",
                "auditCount=" + auditCount + ";latest=" + text(audit.get("latestAuditAt")));
        csvRow(csv, "risk", "READY".equals(text(result.get("status"))) && blockedBatches == 0 ? "PASS" : "TODO", "closureRisk",
                "status=" + text(result.get("status")) + ";blockedBatches=" + blockedBatches + ";pendingReports=" + number(result.get("pendingReports")));
        return csv.toString();
    }

    @GetMapping("/audits")
    public ApiResponse<Map<String, Object>> reportAudits(
            @RequestParam(defaultValue = "") String action,
            @RequestParam(defaultValue = "") String operator,
            @RequestParam(defaultValue = "") String targetCode,
            @RequestParam(defaultValue = "") String start,
            @RequestParam(defaultValue = "") String end,
            @RequestParam(defaultValue = "50") int limit
    ) {
        requireReportPermission();
        int safeLimit = Math.max(1, Math.min(limit, 500));
        List<Object> args = new ArrayList<>();
        String actionWhere = "";
        String safeAction = text(action);
        if (!safeAction.isBlank()) {
            actionWhere = "AND action_name LIKE CONCAT('%', ?, '%')";
            args.add(safeAction);
        }
        String operatorWhere = "";
        String safeOperator = text(operator);
        if (!safeOperator.isBlank()) {
            operatorWhere = "AND operator LIKE CONCAT('%', ?, '%')";
            args.add(safeOperator);
        }
        String targetWhere = "";
        String safeTargetCode = text(targetCode);
        if (!safeTargetCode.isBlank()) {
            targetWhere = "AND (target_code LIKE CONCAT('%', ?, '%') OR target_type LIKE CONCAT('%', ?, '%'))";
            args.add(safeTargetCode);
            args.add(safeTargetCode);
        }
        String accessWhere = "AND " + organizationAccessService.orgCodeAccessSql("target_code");
        String startWhere = "";
        String safeStart = normalizeDateTime(start);
        if (!safeStart.isBlank()) {
            startWhere = "AND created_at >= ?";
            args.add(safeStart);
        }
        String endWhere = "";
        String safeEnd = normalizeDateTime(end);
        if (!safeEnd.isBlank()) {
            endWhere = "AND created_at <= ?";
            args.add(safeEnd);
        }
        args.add(safeLimit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT CONCAT('SYS-', id) AS auditId,
                       module_name AS moduleName,
                       action_name AS actionName,
                       target_type AS targetType,
                       target_code AS targetCode,
                       summary,
                       operator,
                       created_at AS createdAt
                FROM sys_audit_log
                WHERE module_name = 'report'
                __ACTION_WHERE__
                __OPERATOR_WHERE__
                __TARGET_WHERE__
                __ACCESS_WHERE__
                __START_WHERE__
                __END_WHERE__
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """.replace("__ACTION_WHERE__", actionWhere)
                .replace("__OPERATOR_WHERE__", operatorWhere)
                .replace("__TARGET_WHERE__", targetWhere)
                .replace("__ACCESS_WHERE__", accessWhere)
                .replace("__START_WHERE__", startWhere)
                .replace("__END_WHERE__", endWhere), args.toArray());
        return ApiResponse.ok(Map.of(
                "items", rows,
                "limit", safeLimit,
                "action", safeAction,
                "operator", safeOperator,
                "targetCode", safeTargetCode,
                "start", safeStart,
                "end", safeEnd
        ));
    }

    @GetMapping("/print-batches/{batchNo}")
    public ApiResponse<Map<String, Object>> reportPrintBatch(@PathVariable String batchNo) {
        requireReportPermission();
        ReportPrintBatchData data = reportPrintBatchData(batchNo);
        return ApiResponse.ok(Map.of(
                "batch", data.batch(),
                "items", data.items(),
                "audits", reportPrintBatchAudits(batchNo, data.batch())
        ));
    }

    @GetMapping("/print-batches")
    public ApiResponse<Map<String, Object>> reportPrintBatches(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "ALL") String acceptanceStatus,
            @RequestParam(defaultValue = "80") int limit
    ) {
        requireReportPermission();
        ensureReportPrintBatchTables();
        systemAuditService.ensureTable();
        String safeOrgCode = text(orgCode);
        if (!safeOrgCode.isBlank()) {
            organizationAccessService.requireOrgAccess(safeOrgCode);
        }
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : 0;
        int safeMonth = month > 0 ? Math.max(1, Math.min(month, 12)) : 0;
        int safeLimit = Math.max(1, Math.min(limit, 500));
        String safeKeyword = text(keyword);
        List<Object> args = new ArrayList<>();
        String orgWhere = "";
        if (!safeOrgCode.isBlank()) {
            orgWhere = " AND batch.org_code = ? ";
            args.add(safeOrgCode);
        }
        String yearWhere = "";
        if (safeYear > 0) {
            yearWhere = " AND batch.event_year = ? ";
            args.add(safeYear);
        }
        String monthWhere = "";
        if (safeMonth > 0) {
            monthWhere = " AND batch.event_month = ? ";
            args.add(safeMonth);
        }
        String safeAcceptanceStatus = normalizeReportPrintBatchAcceptanceStatus(acceptanceStatus);
        String acceptanceWhere = switch (safeAcceptanceStatus) {
            case "EXPORTED" -> " AND acceptance.id IS NOT NULL ";
            case "PENDING" -> " AND acceptance.id IS NULL ";
            default -> "";
        };
        String keywordWhere = "";
        if (!safeKeyword.isBlank()) {
            keywordWhere = """
                     AND (
                         batch.batch_no LIKE CONCAT('%', ?, '%')
                         OR batch.report_type LIKE CONCAT('%', ?, '%')
                         OR batch.keyword LIKE CONCAT('%', ?, '%')
                         OR EXISTS (
                             SELECT 1
                             FROM salary_report_print_batch_item item
                             WHERE item.batch_no = batch.batch_no
                               AND (
                                   item.case_no LIKE CONCAT('%', ?, '%')
                                   OR item.person_code LIKE CONCAT('%', ?, '%')
                                   OR item.person_name LIKE CONCAT('%', ?, '%')
                                   OR item.business_type LIKE CONCAT('%', ?, '%')
                               )
                         )
                     )
                    """;
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
        }
        args.add(safeLimit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT batch.batch_no AS batchNo,
                       batch.report_type AS reportType,
                       batch.org_code AS orgCode,
                       batch.event_year AS eventYear,
                       batch.event_month AS eventMonth,
                       batch.business_type AS businessType,
                       batch.keyword,
                       batch.limit_count AS limitCount,
                       batch.printed_count AS printedCount,
                       batch.blocked_count AS blockedCount,
                       batch.warning_count AS warningCount,
                       batch.printed_by AS printedBy,
                       batch.printed_at AS printedAt,
                       batch.summary,
                       COALESCE(items.caseCount, 0) AS itemCount,
                       COALESCE(items.personSample, '') AS personSample,
                       CASE WHEN acceptance.id IS NULL THEN 0 ELSE 1 END AS acceptanceExported,
                       CASE WHEN acceptance.id IS NULL THEN '' ELSE CONCAT('SYS-', acceptance.id) END AS acceptanceAuditId,
                       COALESCE(acceptance.operator, '') AS acceptanceOperator,
                       acceptance.created_at AS acceptanceExportedAt
                FROM salary_report_print_batch batch
                LEFT JOIN (
                    SELECT batch_no,
                           COUNT(*) AS caseCount,
                           GROUP_CONCAT(CONCAT(person_code, ' ', person_name) ORDER BY id SEPARATOR '；') AS personSample
                    FROM salary_report_print_batch_item
                    GROUP BY batch_no
                ) items ON items.batch_no = batch.batch_no
                LEFT JOIN (
                    SELECT audit.*
                    FROM sys_audit_log audit
                    JOIN (
                        SELECT target_code, MAX(id) AS latest_id
                        FROM sys_audit_log
                        WHERE module_name = 'report'
                          AND action_name = 'report-print-batch-acceptance-package'
                          AND target_type = 'REPORT_PRINT_BATCH'
                        GROUP BY target_code
                    ) latest ON latest.latest_id = audit.id
                ) acceptance ON acceptance.target_code = batch.batch_no
                WHERE 1 = 1
                __ORG_WHERE__
                __YEAR_WHERE__
                __MONTH_WHERE__
                __ACCEPTANCE_WHERE__
                __KEYWORD_WHERE__
                ORDER BY batch.printed_at DESC, batch.id DESC
                LIMIT ?
                """.replace("__ORG_WHERE__", orgWhere)
                .replace("__YEAR_WHERE__", yearWhere)
                .replace("__MONTH_WHERE__", monthWhere)
                .replace("__ACCEPTANCE_WHERE__", acceptanceWhere)
                .replace("__KEYWORD_WHERE__", keywordWhere), args.toArray());
        return ApiResponse.ok(Map.of(
                "items", rows,
                "orgCode", safeOrgCode,
                "year", safeYear,
                "month", safeMonth,
                "keyword", safeKeyword,
                "acceptanceStatus", safeAcceptanceStatus,
                "limit", safeLimit
        ));
    }

    @GetMapping("/print-archive")
    public ApiResponse<Map<String, Object>> reportPrintArchive(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "ALL") String printStatus,
            @RequestParam(defaultValue = "300") int limit
    ) {
        requireReportPermission();
        List<Map<String, Object>> rows = reportPrintArchiveRows(orgCode, year, month, businessType, keyword, printStatus, limit);
        long printed = rows.stream().filter(row -> Boolean.TRUE.equals(booleanValue(row.get("printed")))).count();
        long unprinted = rows.stream().filter(row -> !Boolean.TRUE.equals(booleanValue(row.get("printed")))).count();
        long reprinted = rows.stream().filter(row -> Boolean.TRUE.equals(booleanValue(row.get("reprinted")))).count();
        long writeReady = rows.stream().filter(row -> Boolean.TRUE.equals(booleanValue(row.get("writeReady")))).count();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", rows);
        result.put("limit", Math.max(1, Math.min(limit, 5000)));
        result.put("orgCode", text(orgCode));
        result.put("year", year);
        result.put("month", month);
        result.put("businessType", text(businessType));
        result.put("keyword", text(keyword));
        result.put("printStatus", normalizePrintArchiveStatus(printStatus));
        result.put("printed", printed);
        result.put("unprinted", unprinted);
        result.put("reprinted", reprinted);
        result.put("writeReady", writeReady);
        return ApiResponse.ok(result);
    }

    @GetMapping(value = "/print-archive.csv", produces = "text/csv")
    public ResponseEntity<byte[]> reportPrintArchiveCsv(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "ALL") String printStatus,
            @RequestParam(defaultValue = "5000") int limit
    ) {
        requireReportPermission();
        List<Map<String, Object>> rows = reportPrintArchiveRows(orgCode, year, month, businessType, keyword, printStatus, limit);
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "filter", "orgCode", text(orgCode).isBlank() ? "ALL" : text(orgCode));
        csvRow(csv, "filter", "period", year > 0 ? periodText(year, month > 0 ? month : 1) : "ALL");
        csvRow(csv, "filter", "businessType", text(businessType).isBlank() ? "ALL" : text(businessType));
        csvRow(csv, "filter", "keyword", text(keyword).isBlank() ? "ALL" : text(keyword));
        csvRow(csv, "filter", "printStatus", normalizePrintArchiveStatus(printStatus));
        csvRow(csv, "filter", "limit", Math.max(1, Math.min(limit, 5000)));
        csvRow(csv, "filter", "rows", rows.size());
        csv.append('\n');
        csvRow(csv, "caseNo", "personCode", "personName", "orgCode", "period", "businessType",
                "archiveStatus", "printed", "reprinted", "printCount", "latestBatchNo",
                "latestAction", "latestOperator", "latestPrintedAt", "planStatus",
                "previewStatus", "executionResult", "writeReady", "acceptanceExported",
                "acceptanceAuditId", "acceptanceOperator", "acceptanceExportedAt");
        for (Map<String, Object> row : rows) {
            csvRow(csv,
                    row.get("caseNo"),
                    row.get("personCode"),
                    row.get("personName"),
                    row.get("orgCode"),
                    periodText(number(row.get("eventYear")), number(row.get("eventMonth"))),
                    row.get("businessType"),
                    row.get("archiveStatus"),
                    row.get("printed"),
                    row.get("reprinted"),
                    row.get("printCount"),
                    row.get("latestBatchNo"),
                    row.get("latestAction"),
                    row.get("latestOperator"),
                    row.get("latestPrintedAt"),
                    row.get("planStatus"),
                    row.get("previewStatus"),
                    row.get("executionResult"),
                    row.get("writeReady"),
                    row.get("acceptanceExported"),
                    row.get("acceptanceAuditId"),
                    row.get("acceptanceOperator"),
                    row.get("acceptanceExportedAt"));
        }
        systemAuditService.record("report", "report-print-archive-csv", "REPORT_PRINT_ARCHIVE", text(orgCode),
                reportAuditSummary(
                        auditPart("org", text(orgCode).isBlank() ? "ALL" : text(orgCode)),
                        auditPart("period", year > 0 ? periodText(year, month > 0 ? month : 1) : "ALL"),
                        auditPart("status", normalizePrintArchiveStatus(printStatus)),
                        auditPart("rows", rows.size())
                ));
        String suffix = text(orgCode).isBlank() ? "all" : text(orgCode);
        return csvResponse("report-print-archive-" + suffix + ".csv", csv.toString());
    }

    @GetMapping(value = "/print-batches/acceptance-packages.zip", produces = "application/zip")
    public ResponseEntity<byte[]> reportPrintBatchAcceptancePackages(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "PENDING") String acceptanceStatus,
            @RequestParam(defaultValue = "50") int limit
    ) throws IOException {
        requireReportAcceptanceExportPermission();
        int safeLimit = Math.max(1, Math.min(limit, 50));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) reportPrintBatches(
                orgCode,
                year,
                month,
                keyword,
                acceptanceStatus,
                safeLimit
        ).data().getOrDefault("items", List.of());
        Map<String, String> entries = new LinkedHashMap<>();
        String exportNo = "bulk-" + LocalDateTime.now().withNano(0).toString().replace(":", "").replace("-", "").replace("T", "-");
        StringBuilder index = new StringBuilder();
        csvRow(index, "filter", "bulkExport", exportNo);
        csvRow(index, "filter", "orgCode", text(orgCode).isBlank() ? "ALL" : text(orgCode));
        csvRow(index, "filter", "period", year > 0 ? periodText(year, month > 0 ? month : 1) : "ALL");
        csvRow(index, "filter", "keyword", text(keyword).isBlank() ? "ALL" : text(keyword));
        csvRow(index, "filter", "acceptanceStatus", normalizeReportPrintBatchAcceptanceStatus(acceptanceStatus));
        csvRow(index, "filter", "limit", safeLimit);
        csvRow(index, "filter", "rows", rows.size());
        index.append('\n');
        csvRow(index, "批次号", "单位", "年月", "人数", "阻断", "提示", "验收目录");
        for (Map<String, Object> row : rows) {
            ReportPrintBatchData data = reportPrintBatchData(text(row.get("batchNo")));
            Map<String, Object> batch = data.batch();
            String safeBatchNo = text(batch.get("batchNo"));
            List<Map<String, Object>> unwritten = data.items().stream()
                    .filter(item -> !reportPrintBatchHistoryWritten(item))
                    .toList();
            List<Map<String, Object>> unclosed = data.items().stream()
                    .filter(item -> !reportPrintBatchClosureReady(item))
                    .toList();
            List<Map<String, Object>> blocked = data.items().stream()
                    .filter(this::reportPrintBatchBlocked)
                    .toList();
            systemAuditService.record("report", "report-print-batch-acceptance-package", "REPORT_PRINT_BATCH", safeBatchNo,
                    reportAuditSummary(
                            auditPart("batchNo", safeBatchNo),
                            auditPart("rows", data.items().size()),
                            auditPart("unwritten", unwritten.size()),
                            auditPart("unclosed", unclosed.size()),
                            auditPart("blocked", blocked.size()),
                            auditPart("bulkExport", exportNo),
                            auditPart("org", batch.get("orgCode"))
                    ));
            List<Map<String, Object>> audits = reportPrintBatchAudits(safeBatchNo, batch);
            String filePart = safeFilePart(safeBatchNo);
            String folder = filePart + "/";
            entries.put(folder + "README.txt", reportPrintBatchAcceptanceReadme(data, audits, unwritten, unclosed, blocked, exportNo));
            entries.put(folder + "acceptance-package-meta.csv", reportPrintBatchAcceptanceMetaCsv(exportNo, data, unwritten, unclosed, blocked));
            entries.put(folder + "report-print-batch-detail-" + filePart + ".csv", reportPrintBatchDetailCsv(data));
            entries.put(folder + "report-print-batch-audits-" + filePart + ".csv", reportPrintBatchAuditsCsv(batch, audits));
            entries.put(folder + "report-print-batch-unwritten-" + filePart + ".csv", reportPrintBatchItemsCsv(batch, "未写入清单", unwritten));
            entries.put(folder + "report-print-batch-unclosed-" + filePart + ".csv", reportPrintBatchItemsCsv(batch, "未闭环清单", unclosed));
            entries.put(folder + "report-print-batch-blocked-" + filePart + ".csv", reportPrintBatchItemsCsv(batch, "阻断清单", blocked));
            csvRow(index,
                    safeBatchNo,
                    batch.get("orgCode"),
                    periodText(number(batch.get("eventYear")), number(batch.get("eventMonth"))),
                    data.items().size(),
                    blocked.size(),
                    number(batch.get("warningCount")),
                    folder);
        }
        entries.put("README.txt", """
                打印批次批量验收包

                导出编号：%s
                批次数：%s
                单位：%s
                年月：%s
                关键字：%s
                验收筛选：%s
                生成时间：%s
                """.formatted(
                exportNo,
                rows.size(),
                text(orgCode).isBlank() ? "ALL" : text(orgCode),
                year > 0 ? periodText(year, month > 0 ? month : 1) : "ALL",
                text(keyword).isBlank() ? "ALL" : text(keyword),
                normalizeReportPrintBatchAcceptanceStatus(acceptanceStatus),
                LocalDateTime.now().withNano(0)
        ));
        entries.put("report-print-batch-acceptance-package-index.csv", index.toString());
        systemAuditService.record("report", "report-print-batch-acceptance-package-bulk", "REPORT_PRINT_BATCH", "BULK",
                reportAuditSummary(
                        auditPart("bulkExport", exportNo),
                        auditPart("rows", rows.size()),
                        auditPart("org", text(orgCode).isBlank() ? "ALL" : text(orgCode)),
                        auditPart("keyword", text(keyword)),
                        auditPart("acceptanceStatus", normalizeReportPrintBatchAcceptanceStatus(acceptanceStatus))
                ));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("report-print-batch-acceptance-packages-" + exportNo + ".zip", StandardCharsets.UTF_8)
                        .build().toString())
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zipResponse(entries));
    }

    @GetMapping(value = "/print-batches/{batchNo}.csv", produces = "text/csv")
    public ResponseEntity<byte[]> reportPrintBatchCsv(@PathVariable String batchNo) {
        requireReportPermission();
        ReportPrintBatchData data = reportPrintBatchData(batchNo);
        Map<String, Object> batch = data.batch();
        StringBuilder csv = new StringBuilder();
        appendReportPrintBatchFilterRows(csv, batch, data.items().size());
        csvRow(csv, "批次号", batch.get("batchNo"));
        csvRow(csv, "报表类型", batch.get("reportType"));
        csvRow(csv, "单位", batch.get("orgCode"));
        csvRow(csv, "年月", periodText(number(batch.get("eventYear")), number(batch.get("eventMonth"))));
        csvRow(csv, "业务类型", text(batch.get("businessType")).isBlank() ? "全部" : batch.get("businessType"));
        csvRow(csv, "关键字", text(batch.get("keyword")).isBlank() ? "全部" : batch.get("keyword"));
        csvRow(csv, "打印人数", batch.get("printedCount"));
        csvRow(csv, "阻断数", batch.get("blockedCount"));
        csvRow(csv, "提示数", batch.get("warningCount"));
        csvRow(csv, "操作人", batch.get("printedBy"));
        csvRow(csv, "操作时间", batch.get("printedAt"));
        csv.append('\n');
        csvRow(csv, "办理编号", "人员编码", "姓名", "单位", "业务类型", "校验状态", "缺口数", "提示数",
                "办理状态", "试算状态", "复核状态", "计划状态", "预检状态", "执行结果", "写入历史ID", "已写入", "已闭环",
                "摘要", "创建时间");
        for (Map<String, Object> item : data.items()) {
            csvRow(csv,
                    item.get("caseNo"),
                    item.get("personCode"),
                    item.get("personName"),
                    item.get("orgCode"),
                    item.get("businessType"),
                    item.get("validationStatus"),
                    item.get("issueCount"),
                    item.get("warningCount"),
                    reportStatusText("case", item.get("caseStatus")),
                    reportStatusText("trial", item.get("trialStatus")),
                    reportStatusText("review", item.get("reviewStatus")),
                    reportStatusText("plan", item.get("planStatus")),
                    reportStatusText("plan", item.get("previewStatus")),
                    reportStatusText("execution", item.get("executionResult")),
                    item.get("insertedHistoryId"),
                    Boolean.TRUE.equals(booleanValue(item.get("historyWritten"))) ? "是" : "否",
                    Boolean.TRUE.equals(booleanValue(item.get("closureReady"))) ? "是" : "否",
                    item.get("summary"),
                    item.get("createdAt"));
        }
        String safeBatchNo = text(batch.get("batchNo"));
        systemAuditService.record("report", "report-print-batch-csv", "REPORT_PRINT_BATCH", safeBatchNo,
                reportAuditSummary(
                        auditPart("batchNo", safeBatchNo),
                        auditPart("rows", data.items().size()),
                        auditPart("org", batch.get("orgCode"))
                ));
        return csvResponse("report-print-batch-" + safeBatchNo + ".csv", csv.toString());
    }

    @GetMapping(value = "/print-batches/{batchNo}/acceptance-package.zip", produces = "application/zip")
    public ResponseEntity<byte[]> reportPrintBatchAcceptancePackage(@PathVariable String batchNo) throws IOException {
        requireReportAcceptanceExportPermission();
        ReportPrintBatchData data = reportPrintBatchData(batchNo);
        Map<String, Object> batch = data.batch();
        String safeBatchNo = text(batch.get("batchNo"));
        List<Map<String, Object>> unwritten = data.items().stream()
                .filter(item -> !reportPrintBatchHistoryWritten(item))
                .toList();
        List<Map<String, Object>> unclosed = data.items().stream()
                .filter(item -> !reportPrintBatchClosureReady(item))
                .toList();
        List<Map<String, Object>> blocked = data.items().stream()
                .filter(this::reportPrintBatchBlocked)
                .toList();
        String exportNo = "single-" + LocalDateTime.now().withNano(0).toString().replace(":", "").replace("-", "").replace("T", "-");
        systemAuditService.record("report", "report-print-batch-acceptance-package", "REPORT_PRINT_BATCH", safeBatchNo,
                reportAuditSummary(
                        auditPart("batchNo", safeBatchNo),
                        auditPart("exportNo", exportNo),
                        auditPart("rows", data.items().size()),
                        auditPart("unwritten", unwritten.size()),
                        auditPart("unclosed", unclosed.size()),
                        auditPart("blocked", blocked.size()),
                        auditPart("org", batch.get("orgCode"))
                ));
        List<Map<String, Object>> audits = reportPrintBatchAudits(safeBatchNo, batch);
        String filePart = safeFilePart(safeBatchNo);
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("README.txt", reportPrintBatchAcceptanceReadme(data, audits, unwritten, unclosed, blocked, exportNo));
        entries.put("acceptance-package-meta.csv", reportPrintBatchAcceptanceMetaCsv(exportNo, data, unwritten, unclosed, blocked));
        entries.put("report-print-batch-detail-" + filePart + ".csv", reportPrintBatchDetailCsv(data));
        entries.put("report-print-batch-audits-" + filePart + ".csv", reportPrintBatchAuditsCsv(batch, audits));
        entries.put("report-print-batch-unwritten-" + filePart + ".csv", reportPrintBatchItemsCsv(batch, "未写入清单", unwritten));
        entries.put("report-print-batch-unclosed-" + filePart + ".csv", reportPrintBatchItemsCsv(batch, "未闭环清单", unclosed));
        entries.put("report-print-batch-blocked-" + filePart + ".csv", reportPrintBatchItemsCsv(batch, "阻断清单", blocked));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("report-print-batch-acceptance-package-" + filePart + ".zip", StandardCharsets.UTF_8)
                        .build().toString())
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zipResponse(entries));
    }

    @GetMapping(value = "/print-batches/{batchNo}/reprint", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> reportPrintBatchReprint(@PathVariable String batchNo) {
        requireReportPermission();
        ReportPrintBatchData data = reportPrintBatchData(batchNo);
        Map<String, Object> batch = data.batch();
        List<String> caseNos = data.items().stream()
                .map(item -> text(item.get("caseNo")))
                .filter(caseNo -> !caseNo.isBlank())
                .toList();
        List<Map<String, Object>> validations = caseNos.stream()
                .map(this::salaryCaseApprovalValidation)
                .toList();
        List<Map<String, Object>> blocked = validations.stream()
                .filter(item -> !Boolean.TRUE.equals(item.get("printable")))
                .toList();
        if (!blocked.isEmpty()) {
            throw new IllegalArgumentException("Salary approval batch reprint blocked: " + batchValidationIssueText(blocked));
        }
        List<SalaryCasePrintData> cases = caseNos.stream()
                .map(this::salaryCasePrintData)
                .toList();
        int year = number(batch.get("eventYear"));
        int month = number(batch.get("eventMonth"));
        String orgCode = text(batch.get("orgCode"));
        String businessType = text(batch.get("businessType"));
        String keyword = text(batch.get("keyword"));
        String newBatchNo = recordReportPrintBatch(
                "SALARY_CASE_APPROVAL_REPRINT",
                orgCode,
                year,
                month,
                businessType,
                keyword,
                cases.size(),
                cases,
                validations
        );
        long warningCount = validations.stream()
                .filter(item -> "WARNING".equals(text(item.get("status"))))
                .count();
        systemAuditService.record("report", "salary-case-approvals-reprint", "REPORT_PRINT_BATCH", newBatchNo,
                reportAuditSummary(
                        auditPart("sourceBatch", batch.get("batchNo")),
                        auditPart("batchNo", newBatchNo),
                        auditPart("org", orgCode),
                        auditPart("period", periodText(year, month)),
                        auditPart("rows", cases.size()),
                        auditPart("warnings", warningCount),
                        auditPart("caseNos", caseNos.stream().limit(5).collect(java.util.stream.Collectors.joining("|")))
                ));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .body(salaryCaseApprovalsBatchHtml(orgCode, year, month, businessType, keyword, cases, validations));
    }

    @GetMapping(value = "/audits.csv", produces = "text/csv")
    public ResponseEntity<byte[]> reportAuditsCsv(
            @RequestParam(defaultValue = "") String action,
            @RequestParam(defaultValue = "") String operator,
            @RequestParam(defaultValue = "") String targetCode,
            @RequestParam(defaultValue = "") String start,
            @RequestParam(defaultValue = "") String end,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        requireReportPermission();
        int safeLimit = Math.max(1, Math.min(limit, 10000));
        List<Object> args = new ArrayList<>();
        String actionWhere = "";
        String safeAction = text(action);
        if (!safeAction.isBlank()) {
            actionWhere = "AND action_name LIKE CONCAT('%', ?, '%')";
            args.add(safeAction);
        }
        String operatorWhere = "";
        String safeOperator = text(operator);
        if (!safeOperator.isBlank()) {
            operatorWhere = "AND operator LIKE CONCAT('%', ?, '%')";
            args.add(safeOperator);
        }
        String targetWhere = "";
        String safeTargetCode = text(targetCode);
        if (!safeTargetCode.isBlank()) {
            targetWhere = "AND (target_code LIKE CONCAT('%', ?, '%') OR target_type LIKE CONCAT('%', ?, '%'))";
            args.add(safeTargetCode);
            args.add(safeTargetCode);
        }
        String accessWhere = "AND " + organizationAccessService.orgCodeAccessSql("target_code");
        String startWhere = "";
        String safeStart = normalizeDateTime(start);
        if (!safeStart.isBlank()) {
            startWhere = "AND created_at >= ?";
            args.add(safeStart);
        }
        String endWhere = "";
        String safeEnd = normalizeDateTime(end);
        if (!safeEnd.isBlank()) {
            endWhere = "AND created_at <= ?";
            args.add(safeEnd);
        }
        args.add(safeLimit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT CONCAT('SYS-', id) AS auditId,
                       module_name AS moduleName,
                       action_name AS actionName,
                       target_type AS targetType,
                       target_code AS targetCode,
                       summary,
                       operator,
                       created_at AS createdAt
                FROM sys_audit_log
                WHERE module_name = 'report'
                __ACTION_WHERE__
                __OPERATOR_WHERE__
                __TARGET_WHERE__
                __ACCESS_WHERE__
                __START_WHERE__
                __END_WHERE__
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """.replace("__ACTION_WHERE__", actionWhere)
                .replace("__OPERATOR_WHERE__", operatorWhere)
                .replace("__TARGET_WHERE__", targetWhere)
                .replace("__ACCESS_WHERE__", accessWhere)
                .replace("__START_WHERE__", startWhere)
                .replace("__END_WHERE__", endWhere), args.toArray());
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "filter", "action", safeAction.isBlank() ? "ALL" : safeAction);
        csvRow(csv, "filter", "operator", safeOperator.isBlank() ? "ALL" : safeOperator);
        csvRow(csv, "filter", "target", safeTargetCode.isBlank() ? "ALL" : safeTargetCode);
        csvRow(csv, "filter", "start", safeStart.isBlank() ? "ALL" : safeStart);
        csvRow(csv, "filter", "end", safeEnd.isBlank() ? "ALL" : safeEnd);
        csvRow(csv, "filter", "limit", safeLimit);
        csvRow(csv, "filter", "rows", rows.size());
        csv.append('\n');
        csvRow(csv, "审计ID", "模块", "动作", "对象类型", "对象编码", "摘要", "操作人", "操作时间");
        for (Map<String, Object> row : rows) {
            csvRow(csv,
                    row.get("auditId"),
                    row.get("moduleName"),
                    row.get("actionName"),
                    row.get("targetType"),
                    row.get("targetCode"),
                    row.get("summary"),
                    row.get("operator"),
                    row.get("createdAt"));
        }
        systemAuditService.record("report", "report-audits-csv", "REPORT_AUDIT", "report",
                reportAuditSummary(
                        auditPart("action", safeAction.isBlank() ? "ALL" : safeAction),
                        auditPart("operator", safeOperator.isBlank() ? "ALL" : safeOperator),
                        auditPart("target", safeTargetCode.isBlank() ? "ALL" : safeTargetCode),
                        auditPart("start", safeStart.isBlank() ? "ALL" : safeStart),
                        auditPart("end", safeEnd.isBlank() ? "ALL" : safeEnd),
                        auditPart("limit", safeLimit),
                        auditPart("rows", rows.size())
                ));
        return csvResponse("salary-report-audits.csv", csv.toString());
    }

    @GetMapping("/preview")
    public ApiResponse<Map<String, Object>> reportPreview(
            @RequestParam(defaultValue = "") String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "2006") int yearFrom,
            @RequestParam(defaultValue = "2099") int yearTo,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String personCode,
            @RequestParam(defaultValue = "bz06_jbt") String tableName,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        requireReportPermission();
        String safeOrgCode = text(orgCode);
        if (!safeOrgCode.isBlank()) {
            organizationAccessService.requireOrgAccess(safeOrgCode);
        }
        LocalDateTime now = LocalDateTime.now();
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : now.getYear();
        int safeMonth = month > 0 ? Math.max(1, Math.min(month, 12)) : now.getMonthValue();
        int safeYearFrom = Math.max(1900, Math.min(yearFrom, 2099));
        int safeYearTo = Math.max(safeYearFrom, Math.min(yearTo, 2099));
        int safeLimit = Math.max(1, Math.min(limit, 10000));
        String safeBusinessType = text(businessType);
        String safeKeyword = text(keyword);
        String safePersonCode = text(personCode);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", safeOrgCode);
        result.put("year", safeYear);
        result.put("month", safeMonth);
        result.put("yearFrom", safeYearFrom);
        result.put("yearTo", safeYearTo);
        result.put("limit", safeLimit);
        List<Map<String, Object>> items = new ArrayList<>();
        if (safeOrgCode.isBlank()) {
            items.add(previewItem("approvalRoster", "审批清册", 0, safeLimit, true));
            items.add(previewItem("approvalBatch", "批量审批表", 0, Math.min(safeLimit, 500), true));
            items.add(previewItem("changeLedger", "变动台账", 0, safeLimit, true));
            items.add(previewItem("personRoster", "人员工资花名册", 0, safeLimit, true));
            items.add(previewItem("salaryRoster", "工资表", 0, safeLimit, true));
            items.add(previewItem("salaryHistory", "工资历史明细", 0, safeLimit, true));
            items.add(previewItem("assessment", "考核统计", 0, safeLimit, true));
        } else {
            long caseCount = countSalaryCases(safeOrgCode, safeYear, safeMonth, safeBusinessType, safeKeyword);
            items.add(previewItem("approvalRoster", "审批清册", caseCount, safeLimit, false));
            items.add(previewItem("approvalBatch", "批量审批表", caseCount, Math.min(safeLimit, 500), false));
            items.add(previewItem("changeLedger", "变动台账", caseCount, safeLimit, false));
            items.add(previewItem("personRoster", "人员工资花名册", countPersonRoster(safeOrgCode, safeYear, safeMonth, safeKeyword), safeLimit, false));
            items.add(previewItem("salaryRoster", "工资表", countSalaryRoster(safeOrgCode, safeYear, safeMonth), safeLimit, false));
            items.add(previewItem("salaryHistory", "工资历史明细", countSalaryHistory(safeOrgCode, safePersonCode, safeYearFrom, safeYearTo), safeLimit, false));
            items.add(previewItem("assessment", "考核统计", countAssessment(safeOrgCode, safeYearFrom > 0 ? safeYearFrom : safeYear, safeKeyword), safeLimit, false));
        }
        String safeTableName = standardTableName(tableName);
        items.add(previewItem("standardTable", standardTableTitle(safeTableName), countStandardTable(safeTableName, safeKeyword), safeLimit, false));
        items.add(previewItem("reportAudit", "报表操作审计", countReportAudits(safeOrgCode, safeKeyword), safeLimit, false));
        result.put("items", items);
        return ApiResponse.ok(result);
    }

    @GetMapping(value = "/salary-history/print", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> salaryHistoryPrint(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "") String personCode,
            @RequestParam(defaultValue = "2006") int yearFrom,
            @RequestParam(defaultValue = "2099") int yearTo,
            @RequestParam(defaultValue = "500") int limit
    ) {
        requireReportPermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        String safePersonCode = text(personCode);
        if (!safePersonCode.isBlank()) {
            organizationAccessService.requirePersonAccess(safePersonCode, safeOrgCode);
        }
        int safeYearFrom = Math.max(1900, Math.min(yearFrom, 2099));
        int safeYearTo = Math.max(safeYearFrom, Math.min(yearTo, 2099));
        int safeLimit = Math.max(1, Math.min(limit, 2000));
        List<Object> args = new ArrayList<>();
        args.add(safeOrgCode);
        args.add(safeYearFrom);
        args.add(safeYearTo);
        String personWhere = "";
        if (!safePersonCode.isBlank()) {
            String[] parts = safePersonCode.split("-", 2);
            if (parts.length == 2) {
                personWhere = "AND TRIM(h.dwbm) = ? AND TRIM(h.grbm) = ?";
                args.add(parts[0].trim());
                args.add(parts[1].trim());
            } else {
                personWhere = "AND CONCAT(TRIM(h.dwbm), '-', TRIM(h.grbm)) = ?";
                args.add(safePersonCode);
            }
        }
        args.add(safeLimit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(h.dwbm) AS orgCode,
                       TRIM(h.grbm) AS personNo,
                       CONCAT(TRIM(h.dwbm), '-', TRIM(h.grbm)) AS personCode,
                       TRIM(COALESCE(p.xm, '')) AS personName,
                       TRIM(COALESCE(h.jsnf, '')) AS yearText,
                       TRIM(COALESCE(h.jsyf, '')) AS monthText,
                       TRIM(COALESCE(h.jslb, '')) AS changeType,
                       TRIM(COALESCE(h.zwbm2, '')) AS postCode,
                       TRIM(COALESCE(h.jbgzjb2, '')) AS levelCode,
                       TRIM(COALESCE(h.zwgzdc2, '')) AS gradeStep,
                       h.zwgzse2 AS postSalary,
                       h.jbgzse2 AS levelSalary,
                       h.jcgz2 AS baseSalary,
                       h.jxgz AS performanceSalary,
                       h.hj2 AS totalSalary,
                       TRIM(COALESCE(h.id, '')) AS historyId
                FROM hisbase h
                LEFT JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE TRIM(h.dwbm) LIKE CONCAT(?, '%')
                  AND TRIM(COALESCE(h.jsnf, '')) REGEXP '^[0-9]{4}$'
                  AND CAST(TRIM(h.jsnf) AS UNSIGNED) BETWEEN ? AND ?
                  __PERSON_WHERE__
                ORDER BY TRIM(h.dwbm), TRIM(h.grbm),
                         CAST(TRIM(h.jsnf) AS UNSIGNED),
                         CAST(NULLIF(TRIM(h.jsyf), '') AS UNSIGNED),
                         h.id
                LIMIT ?
                """.replace("__PERSON_WHERE__", personWhere), args.toArray());
        systemAuditService.record("report", "salary-history-print", "ORG", safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode),
                        auditPart("person", safePersonCode.isBlank() ? "ALL" : safePersonCode),
                        auditPart("years", safeYearFrom + "-" + safeYearTo),
                        auditPart("limit", safeLimit),
                        auditPart("rows", rows.size())
                ));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .body(salaryHistoryHtmlV2(safeOrgCode, safePersonCode, safeYearFrom, safeYearTo, rows));
    }

    @GetMapping(value = "/salary-history.csv", produces = "text/csv")
    public ResponseEntity<byte[]> salaryHistoryCsv(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "") String personCode,
            @RequestParam(defaultValue = "2006") int yearFrom,
            @RequestParam(defaultValue = "2099") int yearTo,
            @RequestParam(defaultValue = "5000") int limit
    ) {
        requireReportPermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        String safePersonCode = text(personCode);
        if (!safePersonCode.isBlank()) {
            organizationAccessService.requirePersonAccess(safePersonCode, safeOrgCode);
        }
        int safeYearFrom = Math.max(1900, Math.min(yearFrom, 2099));
        int safeYearTo = Math.max(safeYearFrom, Math.min(yearTo, 2099));
        int safeLimit = Math.max(1, Math.min(limit, 10000));
        List<Object> args = new ArrayList<>();
        args.add(safeOrgCode);
        args.add(safeYearFrom);
        args.add(safeYearTo);
        String personWhere = "";
        if (!safePersonCode.isBlank()) {
            String[] parts = safePersonCode.split("-", 2);
            if (parts.length == 2) {
                personWhere = "AND TRIM(h.dwbm) = ? AND TRIM(h.grbm) = ?";
                args.add(parts[0].trim());
                args.add(parts[1].trim());
            } else {
                personWhere = "AND CONCAT(TRIM(h.dwbm), '-', TRIM(h.grbm)) = ?";
                args.add(safePersonCode);
            }
        }
        args.add(safeLimit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(h.dwbm) AS orgCode,
                       TRIM(h.grbm) AS personNo,
                       CONCAT(TRIM(h.dwbm), '-', TRIM(h.grbm)) AS personCode,
                       TRIM(COALESCE(p.xm, '')) AS personName,
                       TRIM(COALESCE(h.jsnf, '')) AS yearText,
                       TRIM(COALESCE(h.jsyf, '')) AS monthText,
                       TRIM(COALESCE(h.jslb, '')) AS changeType,
                       TRIM(COALESCE(h.zwbm2, '')) AS postCode,
                       TRIM(COALESCE(h.jbgzjb2, '')) AS levelCode,
                       TRIM(COALESCE(h.zwgzdc2, '')) AS gradeStep,
                       h.zwgzse2 AS postSalary,
                       h.jbgzse2 AS levelSalary,
                       h.jcgz2 AS baseSalary,
                       h.jxgz AS performanceSalary,
                       h.hj2 AS totalSalary,
                       TRIM(COALESCE(h.id, '')) AS historyId
                FROM hisbase h
                LEFT JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE TRIM(h.dwbm) LIKE CONCAT(?, '%')
                  AND TRIM(COALESCE(h.jsnf, '')) REGEXP '^[0-9]{4}$'
                  AND CAST(TRIM(h.jsnf) AS UNSIGNED) BETWEEN ? AND ?
                  __PERSON_WHERE__
                ORDER BY TRIM(h.dwbm), TRIM(h.grbm),
                         CAST(TRIM(h.jsnf) AS UNSIGNED),
                         CAST(NULLIF(TRIM(h.jsyf), '') AS UNSIGNED),
                         h.id
                LIMIT ?
                """.replace("__PERSON_WHERE__", personWhere), args.toArray());
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "人员编码", "姓名", "执行年月", "变动类别", "职务/岗位", "级别", "档次/薪级",
                "职务工资", "级别工资", "基础总", "奖励总", "合计", "历史ID");
        for (Map<String, Object> row : rows) {
            csvRow(csv,
                    row.get("personCode"),
                    row.get("personName"),
                    text(row.get("yearText")) + "." + text(row.get("monthText")),
                    row.get("changeType"),
                    row.get("postCode"),
                    row.get("levelCode"),
                    row.get("gradeStep"),
                    amountText(row.get("postSalary")),
                    amountText(row.get("levelSalary")),
                    amountText(row.get("baseSalary")),
                    amountText(row.get("performanceSalary")),
                    amountText(row.get("totalSalary")),
                    row.get("historyId"));
        }
        systemAuditService.record("report", "salary-history-csv", "ORG", safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode),
                        auditPart("person", safePersonCode.isBlank() ? "ALL" : safePersonCode),
                        auditPart("years", safeYearFrom + "-" + safeYearTo),
                        auditPart("limit", safeLimit),
                        auditPart("rows", rows.size())
                ));
        return csvResponse("salary-history-" + safeOrgCode + "-" + safeYearFrom + "-" + safeYearTo + ".csv", csv.toString());
    }

    @GetMapping(value = "/salary-roster/print", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> salaryRosterPrint(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String columns,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        requireReportPermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        LocalDateTime now = LocalDateTime.now();
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : now.getYear();
        int safeMonth = month > 0 ? Math.max(1, Math.min(month, 12)) : now.getMonthValue();
        int periodKey = safeYear * 100 + safeMonth;
        int safeLimit = Math.max(1, Math.min(limit, 5000));
        List<ReportSalaryColumn> salaryColumns = salaryReportColumns(columns);
        String dynamicSalarySelect = dynamicSalarySelect(salaryColumns);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(h.dwbm) AS orgCode,
                       TRIM(h.grbm) AS personNo,
                       CONCAT(TRIM(h.dwbm), '-', TRIM(h.grbm)) AS personCode,
                       TRIM(COALESCE(p.xm, '')) AS personName,
                       TRIM(COALESCE(h.jsnf, '')) AS yearText,
                       TRIM(COALESCE(h.jsyf, '')) AS monthText,
                       TRIM(COALESCE(h.jslb, '')) AS changeType,
                       TRIM(COALESCE(h.zwbm2, '')) AS postCode,
                       TRIM(COALESCE(h.jbgzjb2, '')) AS levelCode,
                       TRIM(COALESCE(h.zwgzdc2, '')) AS gradeStep,
                       h.zwgzse2 AS postSalary,
                       h.jbgzse2 AS levelSalary,
                       h.jcgz2 AS baseSalary,
                       h.jxgz AS performanceSalary,
                       h.hj2 AS totalSalary,
                       TRIM(COALESCE(h.id, '')) AS historyId
                       __DYNAMIC_SALARY_SELECT__
                FROM hisbase h
                LEFT JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE TRIM(h.dwbm) LIKE CONCAT(?, '%')
                  AND TRIM(COALESCE(h.jsnf, '')) REGEXP '^[0-9]{4}$'
                  AND TRIM(COALESCE(h.jsyf, '')) REGEXP '^[0-9]{1,2}$'
                  AND (CAST(TRIM(h.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(h.jsyf) AS UNSIGNED)) <= ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM hisbase n
                      WHERE n.dwbm = h.dwbm
                        AND n.grbm = h.grbm
                        AND TRIM(COALESCE(n.jsnf, '')) REGEXP '^[0-9]{4}$'
                        AND TRIM(COALESCE(n.jsyf, '')) REGEXP '^[0-9]{1,2}$'
                        AND (CAST(TRIM(n.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(n.jsyf) AS UNSIGNED)) <= ?
                        AND (
                            (CAST(TRIM(n.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(n.jsyf) AS UNSIGNED))
                                > (CAST(TRIM(h.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(h.jsyf) AS UNSIGNED))
                            OR (
                                (CAST(TRIM(n.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(n.jsyf) AS UNSIGNED))
                                    = (CAST(TRIM(h.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(h.jsyf) AS UNSIGNED))
                                AND TRIM(COALESCE(n.id, '')) > TRIM(COALESCE(h.id, ''))
                            )
                        )
                  )
                ORDER BY TRIM(h.dwbm), TRIM(h.grbm)
                LIMIT ?
                """.replace("__DYNAMIC_SALARY_SELECT__", dynamicSalarySelect), safeOrgCode, periodKey, periodKey, safeLimit);
        systemAuditService.record("report", "salary-roster-print", "ORG", safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode),
                        auditPart("period", periodText(safeYear, safeMonth)),
                        auditPart("columns", salaryColumnAuditText(salaryColumns)),
                        auditPart("limit", safeLimit),
                        auditPart("rows", rows.size())
                ));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .body(salaryRosterHtml(safeOrgCode, safeYear, safeMonth, rows, salaryColumns));
    }

    @GetMapping(value = "/salary-roster.csv", produces = "text/csv")
    public ResponseEntity<byte[]> salaryRosterCsv(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String columns,
            @RequestParam(defaultValue = "5000") int limit
    ) {
        requireReportPermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        LocalDateTime now = LocalDateTime.now();
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : now.getYear();
        int safeMonth = month > 0 ? Math.max(1, Math.min(month, 12)) : now.getMonthValue();
        int periodKey = safeYear * 100 + safeMonth;
        int safeLimit = Math.max(1, Math.min(limit, 10000));
        List<ReportSalaryColumn> salaryColumns = salaryReportColumns(columns);
        String dynamicSalarySelect = dynamicSalarySelect(salaryColumns);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(h.dwbm) AS orgCode,
                       TRIM(h.grbm) AS personNo,
                       CONCAT(TRIM(h.dwbm), '-', TRIM(h.grbm)) AS personCode,
                       TRIM(COALESCE(p.xm, '')) AS personName,
                       TRIM(COALESCE(h.jsnf, '')) AS yearText,
                       TRIM(COALESCE(h.jsyf, '')) AS monthText,
                       TRIM(COALESCE(h.jslb, '')) AS changeType,
                       TRIM(COALESCE(h.zwbm2, '')) AS postCode,
                       TRIM(COALESCE(h.jbgzjb2, '')) AS levelCode,
                       TRIM(COALESCE(h.zwgzdc2, '')) AS gradeStep,
                       h.zwgzse2 AS postSalary,
                       h.jbgzse2 AS levelSalary,
                       h.jcgz2 AS baseSalary,
                       h.jxgz AS performanceSalary,
                       h.hj2 AS totalSalary,
                       TRIM(COALESCE(h.id, '')) AS historyId
                       __DYNAMIC_SALARY_SELECT__
                FROM hisbase h
                LEFT JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE TRIM(h.dwbm) LIKE CONCAT(?, '%')
                  AND TRIM(COALESCE(h.jsnf, '')) REGEXP '^[0-9]{4}$'
                  AND TRIM(COALESCE(h.jsyf, '')) REGEXP '^[0-9]{1,2}$'
                  AND (CAST(TRIM(h.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(h.jsyf) AS UNSIGNED)) <= ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM hisbase n
                      WHERE n.dwbm = h.dwbm
                        AND n.grbm = h.grbm
                        AND TRIM(COALESCE(n.jsnf, '')) REGEXP '^[0-9]{4}$'
                        AND TRIM(COALESCE(n.jsyf, '')) REGEXP '^[0-9]{1,2}$'
                        AND (CAST(TRIM(n.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(n.jsyf) AS UNSIGNED)) <= ?
                        AND (
                            (CAST(TRIM(n.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(n.jsyf) AS UNSIGNED))
                                > (CAST(TRIM(h.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(h.jsyf) AS UNSIGNED))
                            OR (
                                (CAST(TRIM(n.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(n.jsyf) AS UNSIGNED))
                                    = (CAST(TRIM(h.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(h.jsyf) AS UNSIGNED))
                                AND TRIM(COALESCE(n.id, '')) > TRIM(COALESCE(h.id, ''))
                            )
                        )
                  )
                ORDER BY TRIM(h.dwbm), TRIM(h.grbm)
                LIMIT ?
                """.replace("__DYNAMIC_SALARY_SELECT__", dynamicSalarySelect), safeOrgCode, periodKey, periodKey, safeLimit);
        if (!salaryColumns.isEmpty()) {
            String csv = salaryRosterDynamicCsv(rows, salaryColumns);
            systemAuditService.record("report", "salary-roster-csv", "ORG", safeOrgCode,
                    reportAuditSummary(
                            auditPart("org", safeOrgCode),
                            auditPart("period", periodText(safeYear, safeMonth)),
                            auditPart("columns", salaryColumnAuditText(salaryColumns)),
                            auditPart("limit", safeLimit),
                            auditPart("rows", rows.size())
                    ));
            return csvResponse("salary-roster-" + safeOrgCode + "-" + safeYear + String.format("%02d", safeMonth) + ".csv", csv);
        }
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "序号", "人员编码", "姓名", "执行年月", "最近变动", "职务/岗位", "级别", "档次/薪级",
                "职务工资", "级别工资", "基础总", "奖励总", "合计", "历史ID");
        int index = 1;
        for (Map<String, Object> row : rows) {
            csvRow(csv, index++,
                    row.get("personCode"),
                    row.get("personName"),
                    text(row.get("yearText")) + "." + text(row.get("monthText")),
                    row.get("changeType"),
                    row.get("postCode"),
                    row.get("levelCode"),
                    row.get("gradeStep"),
                    amountText(row.get("postSalary")),
                    amountText(row.get("levelSalary")),
                    amountText(row.get("baseSalary")),
                    amountText(row.get("performanceSalary")),
                    amountText(row.get("totalSalary")),
                    row.get("historyId"));
        }
        systemAuditService.record("report", "salary-roster-csv", "ORG", safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode),
                        auditPart("period", periodText(safeYear, safeMonth)),
                        auditPart("columns", "fixed"),
                        auditPart("limit", safeLimit),
                        auditPart("rows", rows.size())
                ));
        return csvResponse("salary-roster-" + safeOrgCode + "-" + safeYear + String.format("%02d", safeMonth) + ".csv", csv.toString());
    }

    @GetMapping(value = "/salary-case-approval/print", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> salaryCaseApprovalPrint(@RequestParam String caseNo) {
        requireReportPermission();
        String safeCaseNo = text(caseNo);
        if (safeCaseNo.isBlank()) {
            throw new IllegalArgumentException("Salary business case number is required.");
        }
        Map<String, Object> validation = salaryCaseApprovalValidation(safeCaseNo);
        if (!Boolean.TRUE.equals(validation.get("printable"))) {
            throw new IllegalArgumentException("Salary approval print blocked: " + validationIssueText(validation));
        }
        SalaryCasePrintData data = salaryCasePrintData(safeCaseNo);
        systemAuditService.record("report", "salary-case-approval-print", "SALARY_CASE", safeCaseNo,
                reportAuditSummary(
                        auditPart("caseNo", safeCaseNo),
                        auditPart("org", data.businessCase().get("org_code")),
                        auditPart("person", data.businessCase().get("person_code")),
                        auditPart("businessType", data.businessCase().get("business_type")),
                        auditPart("period", periodText(number(data.businessCase().get("event_year")), Math.max(1, number(data.businessCase().get("event_month"))))),
                        auditPart("items", data.salaryItems().size())
                ));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .body(salaryCaseApprovalHtml(data.businessCase(), data.snapshot(), data.salaryItems(), data.person(), data.writePlan()));
    }

    @GetMapping("/salary-case-approval/validate")
    public ApiResponse<Map<String, Object>> salaryCaseApprovalValidate(@RequestParam String caseNo) {
        requireReportPermission();
        String safeCaseNo = text(caseNo);
        if (safeCaseNo.isBlank()) {
            throw new IllegalArgumentException("Salary business case number is required.");
        }
        return ApiResponse.ok(salaryCaseApprovalValidation(safeCaseNo));
    }

    @GetMapping(value = "/person-roster/print", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> personRosterPrint(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        requireReportPermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        LocalDateTime now = LocalDateTime.now();
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : now.getYear();
        int safeMonth = month > 0 ? Math.max(1, Math.min(month, 12)) : now.getMonthValue();
        int periodKey = safeYear * 100 + safeMonth;
        int safeLimit = Math.max(1, Math.min(limit, 5000));
        String safeKeyword = text(keyword);
        List<Object> args = new ArrayList<>();
        args.add(periodKey);
        args.add(safeOrgCode);
        String keywordWhere = "";
        if (!safeKeyword.isBlank()) {
            keywordWhere = """
                      AND (
                          TRIM(p.grbm) LIKE CONCAT('%', ?, '%')
                          OR TRIM(COALESCE(p.xm, '')) LIKE CONCAT('%', ?, '%')
                          OR TRIM(COALESCE(p.sfzh, '')) LIKE CONCAT('%', ?, '%')
                      )
                    """;
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
        }
        args.add(safeLimit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(p.dwbm) AS orgCode,
                       TRIM(p.grbm) AS personNo,
                       CONCAT(TRIM(p.dwbm), '-', TRIM(p.grbm)) AS personCode,
                       TRIM(COALESCE(p.xm, '')) AS personName,
                       TRIM(COALESCE(p.xb, '')) AS gender,
                       TRIM(COALESCE(p.sfzh, '')) AS idCard,
                       TRIM(COALESCE(p.csny, '')) AS birthDate,
                       TRIM(COALESCE(p.cjgzny, '')) AS workStart,
                       TRIM(COALESCE(p.xlbm, '')) AS educationCode,
                       TRIM(COALESCE(p.zgxl, '')) AS educationName,
                       TRIM(COALESCE(p.zwjb, '')) AS postLevel,
                       TRIM(COALESCE(p.xrzw, '')) AS currentPost,
                       TRIM(COALESCE(p.srny, '')) AS postStart,
                       TRIM(COALESCE(h.jslb, '')) AS changeType,
                       TRIM(COALESCE(h.jsnf, '')) AS salaryYear,
                       TRIM(COALESCE(h.jsyf, '')) AS salaryMonth,
                       TRIM(COALESCE(h.zwbm2, '')) AS salaryPostCode,
                       h.hj2 AS totalSalary
                FROM dryjbxx p
                LEFT JOIN hisbase h ON h.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = p.dwbm
                      AND x.grbm = p.grbm
                      AND TRIM(COALESCE(x.jsnf, '')) REGEXP '^[0-9]{4}$'
                      AND TRIM(COALESCE(x.jsyf, '')) REGEXP '^[0-9]{1,2}$'
                      AND (CAST(TRIM(x.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(x.jsyf) AS UNSIGNED)) <= ?
                    ORDER BY CAST(TRIM(x.jsnf) AS UNSIGNED) DESC,
                             CAST(TRIM(x.jsyf) AS UNSIGNED) DESC,
                             TRIM(COALESCE(x.id, '')) DESC
                    LIMIT 1
                )
                WHERE TRIM(p.dwbm) LIKE CONCAT(?, '%')
                __KEYWORD_WHERE__
                ORDER BY TRIM(p.dwbm), TRIM(p.grbm)
                LIMIT ?
                """.replace("__KEYWORD_WHERE__", keywordWhere), args.toArray());
        systemAuditService.record("report", "person-roster-print", "ORG", safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode),
                        auditPart("period", periodText(safeYear, safeMonth)),
                        auditPart("keyword", safeKeyword.isBlank() ? "ALL" : safeKeyword),
                        auditPart("limit", safeLimit),
                        auditPart("rows", rows.size())
                ));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .body(personRosterHtmlV2(safeOrgCode, safeYear, safeMonth, safeKeyword, rows));
    }

    @GetMapping(value = "/person-roster.csv", produces = "text/csv")
    public ResponseEntity<byte[]> personRosterCsv(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "5000") int limit
    ) {
        requireReportPermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        LocalDateTime now = LocalDateTime.now();
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : now.getYear();
        int safeMonth = month > 0 ? Math.max(1, Math.min(month, 12)) : now.getMonthValue();
        int periodKey = safeYear * 100 + safeMonth;
        int safeLimit = Math.max(1, Math.min(limit, 10000));
        String safeKeyword = text(keyword);
        List<Object> args = new ArrayList<>();
        args.add(periodKey);
        args.add(safeOrgCode);
        String keywordWhere = "";
        if (!safeKeyword.isBlank()) {
            keywordWhere = """
                      AND (
                          TRIM(p.grbm) LIKE CONCAT('%', ?, '%')
                          OR TRIM(COALESCE(p.xm, '')) LIKE CONCAT('%', ?, '%')
                          OR TRIM(COALESCE(p.sfzh, '')) LIKE CONCAT('%', ?, '%')
                      )
                    """;
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
        }
        args.add(safeLimit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(p.dwbm) AS orgCode,
                       TRIM(p.grbm) AS personNo,
                       CONCAT(TRIM(p.dwbm), '-', TRIM(p.grbm)) AS personCode,
                       TRIM(COALESCE(p.xm, '')) AS personName,
                       TRIM(COALESCE(p.xb, '')) AS gender,
                       TRIM(COALESCE(p.sfzh, '')) AS idCard,
                       TRIM(COALESCE(p.csny, '')) AS birthDate,
                       TRIM(COALESCE(p.cjgzny, '')) AS workStart,
                       TRIM(COALESCE(p.xlbm, '')) AS educationCode,
                       TRIM(COALESCE(p.zgxl, '')) AS educationName,
                       TRIM(COALESCE(p.zwjb, '')) AS postLevel,
                       TRIM(COALESCE(p.xrzw, '')) AS currentPost,
                       TRIM(COALESCE(p.srny, '')) AS postStart,
                       TRIM(COALESCE(h.jslb, '')) AS changeType,
                       TRIM(COALESCE(h.jsnf, '')) AS salaryYear,
                       TRIM(COALESCE(h.jsyf, '')) AS salaryMonth,
                       TRIM(COALESCE(h.zwbm2, '')) AS salaryPostCode,
                       h.hj2 AS totalSalary
                FROM dryjbxx p
                LEFT JOIN hisbase h ON h.id = (
                    SELECT x.id
                    FROM hisbase x
                    WHERE x.dwbm = p.dwbm
                      AND x.grbm = p.grbm
                      AND TRIM(COALESCE(x.jsnf, '')) REGEXP '^[0-9]{4}$'
                      AND TRIM(COALESCE(x.jsyf, '')) REGEXP '^[0-9]{1,2}$'
                      AND (CAST(TRIM(x.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(x.jsyf) AS UNSIGNED)) <= ?
                    ORDER BY CAST(TRIM(x.jsnf) AS UNSIGNED) DESC,
                             CAST(TRIM(x.jsyf) AS UNSIGNED) DESC,
                             TRIM(COALESCE(x.id, '')) DESC
                    LIMIT 1
                )
                WHERE TRIM(p.dwbm) LIKE CONCAT(?, '%')
                __KEYWORD_WHERE__
                ORDER BY TRIM(p.dwbm), TRIM(p.grbm)
                LIMIT ?
                """.replace("__KEYWORD_WHERE__", keywordWhere), args.toArray());
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "序号", "人员编码", "姓名", "性别", "身份证号", "出生年月", "参加工作", "学历",
                "职务层次", "现任职务", "任职时间", "工资执行年月", "最近变动", "工资职务", "工资合计");
        int index = 1;
        for (Map<String, Object> row : rows) {
            String education = text(row.get("educationName"));
            if (education.isBlank()) {
                education = text(row.get("educationCode"));
            }
            csvRow(csv, index++,
                    row.get("personCode"),
                    row.get("personName"),
                    row.get("gender"),
                    row.get("idCard"),
                    row.get("birthDate"),
                    row.get("workStart"),
                    education,
                    row.get("postLevel"),
                    row.get("currentPost"),
                    row.get("postStart"),
                    text(row.get("salaryYear")) + "." + text(row.get("salaryMonth")),
                    row.get("changeType"),
                    row.get("salaryPostCode"),
                    amountText(row.get("totalSalary")));
        }
        systemAuditService.record("report", "person-roster-csv", "ORG", safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode),
                        auditPart("period", periodText(safeYear, safeMonth)),
                        auditPart("keyword", safeKeyword.isBlank() ? "ALL" : safeKeyword),
                        auditPart("limit", safeLimit),
                        auditPart("rows", rows.size())
                ));
        return csvResponse("person-roster-" + safeOrgCode + "-" + safeYear + String.format("%02d", safeMonth) + ".csv", csv.toString());
    }

    @GetMapping(value = "/assessment-summary/print", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> assessmentSummaryPrint(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        requireReportPermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : LocalDateTime.now().getYear() - 1;
        int safeLimit = Math.max(1, Math.min(limit, 5000));
        String safeKeyword = text(keyword);
        List<Object> args = new ArrayList<>();
        args.add(safeOrgCode);
        args.add(String.valueOf(safeYear));
        String keywordWhere = "";
        if (!safeKeyword.isBlank()) {
            keywordWhere = """
                      AND (
                          TRIM(k.grbm) LIKE CONCAT('%', ?, '%')
                          OR TRIM(COALESCE(p.xm, '')) LIKE CONCAT('%', ?, '%')
                          OR TRIM(COALESCE(k.khjg, '')) LIKE CONCAT('%', ?, '%')
                      )
                    """;
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
        }
        args.add(safeLimit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(k.dwbm) AS orgCode,
                       TRIM(k.grbm) AS personNo,
                       CONCAT(TRIM(k.dwbm), '-', TRIM(k.grbm)) AS personCode,
                       TRIM(COALESCE(p.xm, '')) AS personName,
                       TRIM(COALESCE(k.khnd, '')) AS assessmentYear,
                       TRIM(COALESCE(k.khjg, '')) AS assessmentResult,
                       CASE
                           WHEN TRIM(COALESCE(k.khjg, '')) IN ('优秀', '称职', '合格') THEN '合格及以上'
                           WHEN TRIM(COALESCE(k.khjg, '')) IN ('未定等次', '不定等次') THEN '未定等次'
                           WHEN TRIM(COALESCE(k.khjg, '')) = '' THEN '空值'
                           ELSE '其他'
                       END AS resultGroup
                FROM dndkh k
                LEFT JOIN dryjbxx p ON p.dwbm = k.dwbm AND p.grbm = k.grbm
                WHERE TRIM(k.dwbm) LIKE CONCAT(?, '%')
                  AND TRIM(COALESCE(k.khnd, '')) = ?
                __KEYWORD_WHERE__
                ORDER BY TRIM(k.dwbm), TRIM(k.grbm)
                LIMIT ?
                """.replace("__KEYWORD_WHERE__", keywordWhere), args.toArray());
        systemAuditService.record("report", "assessment-summary-print", "ORG", safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode),
                        auditPart("year", safeYear),
                        auditPart("keyword", safeKeyword.isBlank() ? "ALL" : safeKeyword),
                        auditPart("limit", safeLimit),
                        auditPart("rows", rows.size())
                ));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .body(assessmentSummaryHtmlV2(safeOrgCode, safeYear, safeKeyword, rows));
    }

    @GetMapping(value = "/salary-change-ledger/print", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> salaryChangeLedgerPrint(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        requireReportPermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        LocalDateTime now = LocalDateTime.now();
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : now.getYear();
        int safeMonth = month > 0 ? Math.max(1, Math.min(month, 12)) : now.getMonthValue();
        int safeLimit = Math.max(1, Math.min(limit, 5000));
        String safeBusinessType = text(businessType);
        String safeKeyword = text(keyword);
        List<Object> args = new ArrayList<>();
        args.add(safeOrgCode);
        args.add(safeYear);
        args.add(safeMonth);
        String businessTypeWhere = "";
        if (!safeBusinessType.isBlank()) {
            businessTypeWhere = "AND business_type = ?";
            args.add(safeBusinessType);
        }
        String keywordWhere = "";
        if (!safeKeyword.isBlank()) {
            keywordWhere = """
                      AND (
                          case_no LIKE CONCAT('%', ?, '%')
                          OR person_code LIKE CONCAT('%', ?, '%')
                          OR person_name LIKE CONCAT('%', ?, '%')
                          OR title LIKE CONCAT('%', ?, '%')
                          OR summary LIKE CONCAT('%', ?, '%')
                      )
                    """;
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
        }
        args.add(safeLimit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT case_no AS caseNo,
                       work_item_id AS workItemId,
                       source,
                       status,
                       business_type AS businessType,
                       person_code AS personCode,
                       person_name AS personName,
                       org_code AS orgCode,
                       event_year AS eventYear,
                       event_month AS eventMonth,
                       title,
                       summary,
                       handled_by AS handledBy,
                       handled_at AS handledAt,
                       trial_status AS trialStatus,
                       trial_matched AS trialMatched,
                       trial_baseline_total AS trialBaselineTotal,
                       trial_calculated_total AS trialCalculatedTotal,
                       trial_expected_total AS trialExpectedTotal,
                       trial_difference AS trialDifference,
                       review_status AS reviewStatus,
                       review_reason AS reviewReason
                FROM salary_business_case
                WHERE org_code LIKE CONCAT(?, '%')
                  AND event_year = ?
                  AND event_month = ?
                  __BUSINESS_TYPE_WHERE__
                  __KEYWORD_WHERE__
                ORDER BY org_code, person_code, case_no
                LIMIT ?
                """.replace("__BUSINESS_TYPE_WHERE__", businessTypeWhere)
                .replace("__KEYWORD_WHERE__", keywordWhere), args.toArray());
        systemAuditService.record("report", "salary-change-ledger-print", "ORG", safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode),
                        auditPart("period", periodText(safeYear, safeMonth)),
                        auditPart("businessType", safeBusinessType.isBlank() ? "ALL" : safeBusinessType),
                        auditPart("keyword", safeKeyword.isBlank() ? "ALL" : safeKeyword),
                        auditPart("limit", safeLimit),
                        auditPart("rows", rows.size())
                ));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .body(salaryChangeLedgerHtmlV2(safeOrgCode, safeYear, safeMonth, safeBusinessType, safeKeyword, rows));
    }

    @GetMapping(value = "/salary-change-ledger.csv", produces = "text/csv")
    public ResponseEntity<byte[]> salaryChangeLedgerCsv(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "5000") int limit
    ) {
        requireReportPermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        LocalDateTime now = LocalDateTime.now();
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : now.getYear();
        int safeMonth = month > 0 ? Math.max(1, Math.min(month, 12)) : now.getMonthValue();
        int safeLimit = Math.max(1, Math.min(limit, 10000));
        String safeBusinessType = text(businessType);
        String safeKeyword = text(keyword);
        List<Object> args = new ArrayList<>();
        args.add(safeOrgCode);
        args.add(safeYear);
        args.add(safeMonth);
        String businessTypeWhere = "";
        if (!safeBusinessType.isBlank()) {
            businessTypeWhere = "AND business_type = ?";
            args.add(safeBusinessType);
        }
        String keywordWhere = "";
        if (!safeKeyword.isBlank()) {
            keywordWhere = """
                      AND (
                          case_no LIKE CONCAT('%', ?, '%')
                          OR person_code LIKE CONCAT('%', ?, '%')
                          OR person_name LIKE CONCAT('%', ?, '%')
                          OR title LIKE CONCAT('%', ?, '%')
                          OR summary LIKE CONCAT('%', ?, '%')
                      )
                    """;
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
        }
        args.add(safeLimit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT case_no AS caseNo,
                       work_item_id AS workItemId,
                       source,
                       status,
                       business_type AS businessType,
                       person_code AS personCode,
                       person_name AS personName,
                       org_code AS orgCode,
                       event_year AS eventYear,
                       event_month AS eventMonth,
                       title,
                       summary,
                       handled_by AS handledBy,
                       handled_at AS handledAt,
                       trial_status AS trialStatus,
                       trial_matched AS trialMatched,
                       trial_baseline_total AS trialBaselineTotal,
                       trial_calculated_total AS trialCalculatedTotal,
                       trial_expected_total AS trialExpectedTotal,
                       trial_difference AS trialDifference,
                       review_status AS reviewStatus,
                       review_reason AS reviewReason
                FROM salary_business_case
                WHERE org_code LIKE CONCAT(?, '%')
                  AND event_year = ?
                  AND event_month = ?
                  __BUSINESS_TYPE_WHERE__
                  __KEYWORD_WHERE__
                ORDER BY org_code, person_code, case_no
                LIMIT ?
                """.replace("__BUSINESS_TYPE_WHERE__", businessTypeWhere)
                .replace("__KEYWORD_WHERE__", keywordWhere), args.toArray());
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "办理编号", "人员编码", "姓名", "单位", "年月", "业务类型", "标题", "办理状态",
                "试算状态", "试算前", "试算后", "历史期望", "差额", "复核状态", "复核说明", "经办人", "经办时间", "摘要");
        for (Map<String, Object> row : rows) {
            csvRow(csv,
                    row.get("caseNo"),
                    row.get("personCode"),
                    row.get("personName"),
                    row.get("orgCode"),
                    text(row.get("eventYear")) + "." + String.format("%02d", Math.max(1, number(row.get("eventMonth")))),
                    row.get("businessType"),
                    row.get("title"),
                    reportStatusText("case", row.get("status")),
                    reportStatusText("trial", row.get("trialStatus")),
                    amountText(row.get("trialBaselineTotal")),
                    amountText(row.get("trialCalculatedTotal")),
                    amountText(row.get("trialExpectedTotal")),
                    amountText(row.get("trialDifference")),
                    row.get("reviewStatus"),
                    row.get("reviewReason"),
                    row.get("handledBy"),
                    row.get("handledAt"),
                    row.get("summary")
            );
        }
        systemAuditService.record("report", "salary-change-ledger-csv", "ORG", safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode),
                        auditPart("period", periodText(safeYear, safeMonth)),
                        auditPart("businessType", safeBusinessType.isBlank() ? "ALL" : safeBusinessType),
                        auditPart("keyword", safeKeyword.isBlank() ? "ALL" : safeKeyword),
                        auditPart("limit", safeLimit),
                        auditPart("rows", rows.size())
                ));
        return csvResponse("salary-change-ledger-" + safeOrgCode + "-" + safeYear + String.format("%02d", safeMonth) + ".csv", csv.toString());
    }

    @GetMapping(value = "/salary-case-approval-roster/print", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> salaryCaseApprovalRosterPrint(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        requireReportPermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        LocalDateTime now = LocalDateTime.now();
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : now.getYear();
        int safeMonth = month > 0 ? Math.max(1, Math.min(month, 12)) : now.getMonthValue();
        int safeLimit = Math.max(1, Math.min(limit, 5000));
        String safeBusinessType = text(businessType);
        String safeKeyword = text(keyword);
        List<Object> args = new ArrayList<>();
        args.add(safeOrgCode);
        args.add(safeYear);
        args.add(safeMonth);
        String businessTypeWhere = "";
        if (!safeBusinessType.isBlank()) {
            businessTypeWhere = "AND sc.business_type = ?";
            args.add(safeBusinessType);
        }
        String keywordWhere = "";
        if (!safeKeyword.isBlank()) {
            keywordWhere = """
                      AND (
                          sc.case_no LIKE CONCAT('%', ?, '%')
                          OR sc.person_code LIKE CONCAT('%', ?, '%')
                          OR sc.person_name LIKE CONCAT('%', ?, '%')
                          OR sc.title LIKE CONCAT('%', ?, '%')
                          OR sc.summary LIKE CONCAT('%', ?, '%')
                      )
                    """;
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
        }
        args.add(safeLimit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT sc.case_no AS caseNo,
                       sc.status,
                       sc.business_type AS businessType,
                       sc.person_code AS personCode,
                       sc.person_name AS personName,
                       sc.org_code AS orgCode,
                       sc.event_year AS eventYear,
                       sc.event_month AS eventMonth,
                       sc.title,
                       sc.trial_status AS trialStatus,
                       sc.trial_matched AS trialMatched,
                       sc.trial_baseline_total AS trialBaselineTotal,
                       sc.trial_calculated_total AS trialCalculatedTotal,
                       sc.trial_expected_total AS trialExpectedTotal,
                       sc.trial_difference AS trialDifference,
                       sc.review_status AS reviewStatus,
                       sc.review_reason AS reviewReason,
                       sc.handled_by AS handledBy,
                       sc.handled_at AS handledAt,
                       p.plan_no AS planNo,
                       p.plan_status AS planStatus,
                       p.preview_status AS previewStatus,
                       p.writable AS writable,
                       p.inserted_history_id AS insertedHistoryId,
                       p.execution_result AS executionResult,
                       p.comparison_review_status AS comparisonReviewStatus
                FROM salary_business_case sc
                LEFT JOIN salary_history_write_plan p ON p.case_no = sc.case_no
                WHERE sc.org_code LIKE CONCAT(?, '%')
                  AND sc.event_year = ?
                  AND sc.event_month = ?
                  __BUSINESS_TYPE_WHERE__
                  __KEYWORD_WHERE__
                ORDER BY sc.org_code, sc.business_type, sc.person_code, sc.case_no
                LIMIT ?
                """.replace("__BUSINESS_TYPE_WHERE__", businessTypeWhere)
                .replace("__KEYWORD_WHERE__", keywordWhere), args.toArray());
        systemAuditService.record("report", "salary-case-approval-roster-print", "ORG", safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode),
                        auditPart("period", periodText(safeYear, safeMonth)),
                        auditPart("businessType", safeBusinessType.isBlank() ? "ALL" : safeBusinessType),
                        auditPart("keyword", safeKeyword.isBlank() ? "ALL" : safeKeyword),
                        auditPart("limit", safeLimit),
                        auditPart("rows", rows.size())
                ));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .body(salaryCaseApprovalRosterHtmlV2(safeOrgCode, safeYear, safeMonth, safeBusinessType, safeKeyword, rows));
    }

    @GetMapping(value = "/salary-case-approvals/print", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> salaryCaseApprovalsPrint(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "200") int limit
    ) {
        requireReportPermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        LocalDateTime now = LocalDateTime.now();
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : now.getYear();
        int safeMonth = month > 0 ? Math.max(1, Math.min(month, 12)) : now.getMonthValue();
        int safeLimit = Math.max(1, Math.min(limit, 500));
        String safeBusinessType = text(businessType);
        String safeKeyword = text(keyword);
        List<String> caseNos = salaryCaseApprovalCaseNos(safeOrgCode, safeYear, safeMonth, safeBusinessType, safeKeyword, safeLimit);
        List<Map<String, Object>> validations = caseNos.stream()
                .map(this::salaryCaseApprovalValidation)
                .toList();
        List<Map<String, Object>> blocked = validations.stream()
                .filter(item -> !Boolean.TRUE.equals(item.get("printable")))
                .toList();
        if (!blocked.isEmpty()) {
            throw new IllegalArgumentException("Salary approval batch print blocked: " + batchValidationIssueText(blocked));
        }
        long warningCount = validations.stream()
                .filter(item -> "WARNING".equals(text(item.get("status"))))
                .count();
        List<SalaryCasePrintData> cases = caseNos.stream()
                .map(this::salaryCasePrintData)
                .toList();
        String batchNo = recordReportPrintBatch(
                "SALARY_CASE_APPROVAL_BATCH",
                safeOrgCode,
                safeYear,
                safeMonth,
                safeBusinessType,
                safeKeyword,
                safeLimit,
                cases,
                validations
        );
        systemAuditService.record("report", "salary-case-approvals-print", "ORG", safeOrgCode,
                reportAuditSummary(
                        auditPart("batchNo", batchNo),
                        auditPart("org", safeOrgCode),
                        auditPart("period", periodText(safeYear, safeMonth)),
                        auditPart("businessType", safeBusinessType.isBlank() ? "ALL" : safeBusinessType),
                        auditPart("keyword", safeKeyword.isBlank() ? "ALL" : safeKeyword),
                        auditPart("limit", safeLimit),
                        auditPart("rows", cases.size()),
                        auditPart("blocked", blocked.size()),
                        auditPart("warnings", warningCount),
                        auditPart("caseNos", caseNos.stream().limit(5).collect(java.util.stream.Collectors.joining("|")))
                ));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .body(salaryCaseApprovalsBatchHtml(safeOrgCode, safeYear, safeMonth, safeBusinessType, safeKeyword, cases, validations));
    }

    @PostMapping(value = "/salary-case-approvals/selected/print",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> salaryCaseApprovalsSelectedPrint(@RequestParam("caseNo") List<String> caseNos) {
        requireReportPermission();
        List<String> safeCaseNos = normalizeSelectedApprovalCaseNos(caseNos);
        List<SalaryCasePrintData> cases = safeCaseNos.stream()
                .map(this::salaryCasePrintData)
                .toList();
        for (SalaryCasePrintData item : cases) {
            organizationAccessService.requireOrgAccess(text(item.businessCase().get("org_code")));
        }
        List<Map<String, Object>> validations = safeCaseNos.stream()
                .map(this::salaryCaseApprovalValidation)
                .toList();
        List<Map<String, Object>> blocked = validations.stream()
                .filter(item -> !Boolean.TRUE.equals(item.get("printable")))
                .toList();
        if (!blocked.isEmpty()) {
            throw new IllegalArgumentException("Salary approval selected print blocked: " + batchValidationIssueText(blocked));
        }
        int year = cases.stream()
                .map(item -> number(item.businessCase().get("event_year")))
                .filter(value -> value > 0)
                .findFirst()
                .orElse(LocalDateTime.now().getYear());
        int month = cases.stream()
                .map(item -> number(item.businessCase().get("event_month")))
                .filter(value -> value > 0)
                .findFirst()
                .orElse(LocalDateTime.now().getMonthValue());
        Set<String> orgs = cases.stream()
                .map(item -> text(item.businessCase().get("org_code")))
                .filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        Set<String> businessTypes = cases.stream()
                .map(item -> text(item.businessCase().get("business_type")))
                .filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        String orgCode = orgs.size() == 1 ? orgs.iterator().next() : "SELECTED";
        String businessType = businessTypes.size() == 1 ? businessTypes.iterator().next() : "SELECTED";
        String keyword = "selected:" + safeCaseNos.size();
        String batchNo = recordReportPrintBatch(
                "SALARY_CASE_APPROVAL_SELECTED",
                orgCode,
                year,
                month,
                businessType,
                keyword,
                safeCaseNos.size(),
                cases,
                validations
        );
        long warningCount = validations.stream()
                .filter(item -> "WARNING".equals(text(item.get("status"))))
                .count();
        systemAuditService.record("report", "salary-case-approvals-selected-print", "REPORT_PRINT_BATCH", batchNo,
                reportAuditSummary(
                        auditPart("batchNo", batchNo),
                        auditPart("rows", cases.size()),
                        auditPart("org", orgCode),
                        auditPart("period", periodText(year, month)),
                        auditPart("businessType", businessType),
                        auditPart("warnings", warningCount),
                        auditPart("caseNos", safeCaseNos.stream().limit(5).collect(java.util.stream.Collectors.joining("|")))
                ));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .body(salaryCaseApprovalsBatchHtml(orgCode, year, month, businessType, "selected", cases, validations));
    }

    @PostMapping("/salary-case-approvals/selected/validate")
    public ApiResponse<Map<String, Object>> salaryCaseApprovalsSelectedValidate(@RequestParam("caseNo") List<String> caseNos) {
        requireReportPermission();
        List<String> safeCaseNos = normalizeSelectedApprovalCaseNos(caseNos);
        List<SalaryCasePrintData> cases = safeCaseNos.stream()
                .map(this::salaryCasePrintData)
                .toList();
        for (SalaryCasePrintData item : cases) {
            organizationAccessService.requireOrgAccess(text(item.businessCase().get("org_code")));
        }
        List<Map<String, Object>> caseResults = safeCaseNos.stream()
                .map(this::salaryCaseApprovalValidation)
                .toList();
        long blockedCount = caseResults.stream()
                .filter(item -> !Boolean.TRUE.equals(item.get("printable")))
                .count();
        long warningCount = caseResults.stream()
                .filter(item -> "WARNING".equals(text(item.get("status"))))
                .count();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("caseCount", caseResults.size());
        result.put("printable", blockedCount == 0);
        result.put("blockedCount", blockedCount);
        result.put("warningCount", warningCount);
        result.put("items", caseResults);
        result.put("message", blockedCount == 0
                ? "Selected salary approval reports can be printed."
                : batchValidationIssueText(caseResults.stream()
                .filter(item -> !Boolean.TRUE.equals(item.get("printable")))
                .toList()));
        return ApiResponse.ok(result);
    }

    @GetMapping("/salary-case-approvals/validate")
    public ApiResponse<Map<String, Object>> salaryCaseApprovalsValidate(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "200") int limit
    ) {
        requireReportPermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        LocalDateTime now = LocalDateTime.now();
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : now.getYear();
        int safeMonth = month > 0 ? Math.max(1, Math.min(month, 12)) : now.getMonthValue();
        int safeLimit = Math.max(1, Math.min(limit, 500));
        String safeBusinessType = text(businessType);
        String safeKeyword = text(keyword);
        List<String> caseNos = salaryCaseApprovalCaseNos(safeOrgCode, safeYear, safeMonth, safeBusinessType, safeKeyword, safeLimit);
        List<Map<String, Object>> caseResults = caseNos.stream()
                .map(this::salaryCaseApprovalValidation)
                .toList();
        long blockedCount = caseResults.stream()
                .filter(item -> !Boolean.TRUE.equals(item.get("printable")))
                .count();
        long warningCount = caseResults.stream()
                .filter(item -> "WARNING".equals(text(item.get("status"))))
                .count();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgCode", safeOrgCode);
        result.put("period", periodText(safeYear, safeMonth));
        result.put("businessType", safeBusinessType);
        result.put("keyword", safeKeyword);
        result.put("limit", safeLimit);
        result.put("caseCount", caseResults.size());
        result.put("printable", blockedCount == 0);
        result.put("blockedCount", blockedCount);
        result.put("warningCount", warningCount);
        result.put("items", caseResults.stream().limit(20).toList());
        return ApiResponse.ok(result);
    }

    @GetMapping(value = "/salary-case-approval-roster.csv", produces = "text/csv")
    public ResponseEntity<byte[]> salaryCaseApprovalRosterCsv(
            @RequestParam String orgCode,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "5000") int limit
    ) {
        requireReportPermission();
        String safeOrgCode = text(orgCode);
        if (safeOrgCode.isBlank()) {
            throw new IllegalArgumentException("Organization code is required.");
        }
        organizationAccessService.requireOrgAccess(safeOrgCode);
        LocalDateTime now = LocalDateTime.now();
        int safeYear = year > 0 ? Math.max(1900, Math.min(year, 2099)) : now.getYear();
        int safeMonth = month > 0 ? Math.max(1, Math.min(month, 12)) : now.getMonthValue();
        int safeLimit = Math.max(1, Math.min(limit, 10000));
        String safeBusinessType = text(businessType);
        String safeKeyword = text(keyword);
        List<Object> args = new ArrayList<>();
        args.add(safeOrgCode);
        args.add(safeYear);
        args.add(safeMonth);
        String businessTypeWhere = "";
        if (!safeBusinessType.isBlank()) {
            businessTypeWhere = "AND sc.business_type = ?";
            args.add(safeBusinessType);
        }
        String keywordWhere = "";
        if (!safeKeyword.isBlank()) {
            keywordWhere = """
                      AND (
                          sc.case_no LIKE CONCAT('%', ?, '%')
                          OR sc.person_code LIKE CONCAT('%', ?, '%')
                          OR sc.person_name LIKE CONCAT('%', ?, '%')
                          OR sc.title LIKE CONCAT('%', ?, '%')
                          OR sc.summary LIKE CONCAT('%', ?, '%')
                      )
                    """;
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
        }
        args.add(safeLimit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT sc.case_no AS caseNo,
                       sc.status,
                       sc.business_type AS businessType,
                       sc.person_code AS personCode,
                       sc.person_name AS personName,
                       sc.org_code AS orgCode,
                       sc.event_year AS eventYear,
                       sc.event_month AS eventMonth,
                       sc.title,
                       sc.trial_status AS trialStatus,
                       sc.trial_matched AS trialMatched,
                       sc.trial_baseline_total AS trialBaselineTotal,
                       sc.trial_calculated_total AS trialCalculatedTotal,
                       sc.trial_expected_total AS trialExpectedTotal,
                       sc.trial_difference AS trialDifference,
                       sc.review_status AS reviewStatus,
                       sc.review_reason AS reviewReason,
                       sc.handled_by AS handledBy,
                       sc.handled_at AS handledAt,
                       p.plan_no AS planNo,
                       p.plan_status AS planStatus,
                       p.preview_status AS previewStatus,
                       p.writable AS writable,
                       p.inserted_history_id AS insertedHistoryId,
                       p.execution_result AS executionResult,
                       p.comparison_review_status AS comparisonReviewStatus
                FROM salary_business_case sc
                LEFT JOIN salary_history_write_plan p ON p.case_no = sc.case_no
                WHERE sc.org_code LIKE CONCAT(?, '%')
                  AND sc.event_year = ?
                  AND sc.event_month = ?
                  __BUSINESS_TYPE_WHERE__
                  __KEYWORD_WHERE__
                ORDER BY sc.org_code, sc.business_type, sc.person_code, sc.case_no
                LIMIT ?
                """.replace("__BUSINESS_TYPE_WHERE__", businessTypeWhere)
                .replace("__KEYWORD_WHERE__", keywordWhere), args.toArray());
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "办理编号", "人员编码", "姓名", "单位", "年月", "业务类型", "标题", "办理状态",
                "试算状态", "试算前", "试算后", "差额", "复核", "写入计划", "计划状态", "预检",
                "可写", "历史ID", "执行结果", "经办人", "经办时间", "复核说明");
        for (Map<String, Object> row : rows) {
            Boolean writable = booleanValue(row.get("writable"));
            csvRow(csv,
                    row.get("caseNo"),
                    row.get("personCode"),
                    row.get("personName"),
                    row.get("orgCode"),
                    text(row.get("eventYear")) + "." + String.format("%02d", Math.max(1, number(row.get("eventMonth")))),
                    row.get("businessType"),
                    row.get("title"),
                    reportStatusText("case", row.get("status")),
                    reportStatusText("trial", row.get("trialStatus")),
                    amountText(row.get("trialBaselineTotal")),
                    amountText(row.get("trialCalculatedTotal")),
                    amountText(row.get("trialDifference")),
                    reportStatusText("review", firstPresent(row.get("reviewStatus"), row.get("comparisonReviewStatus"))),
                    row.get("planNo"),
                    reportStatusText("plan", row.get("planStatus")),
                    reportStatusText("plan", row.get("previewStatus")),
                    writable == null ? "" : writable ? "是" : "否",
                    row.get("insertedHistoryId"),
                    reportStatusText("execution", row.get("executionResult")),
                    row.get("handledBy"),
                    row.get("handledAt"),
                    row.get("reviewReason")
            );
        }
        systemAuditService.record("report", "salary-case-approval-roster-csv", "ORG", safeOrgCode,
                reportAuditSummary(
                        auditPart("org", safeOrgCode),
                        auditPart("period", periodText(safeYear, safeMonth)),
                        auditPart("businessType", safeBusinessType.isBlank() ? "ALL" : safeBusinessType),
                        auditPart("keyword", safeKeyword.isBlank() ? "ALL" : safeKeyword),
                        auditPart("limit", safeLimit),
                        auditPart("rows", rows.size())
                ));
        return csvResponse("salary-case-approval-roster-" + safeOrgCode + "-" + safeYear + String.format("%02d", safeMonth) + ".csv", csv.toString());
    }

    @GetMapping(value = "/standard-tables/print", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> standardTablePrint(
            @RequestParam(defaultValue = "bz06_jbt") String tableName,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        requireReportPermission();
        String safeTableName = standardTableName(tableName);
        String safeKeyword = text(keyword);
        int safeLimit = Math.max(1, Math.min(limit, 5000));
        List<String> columns = standardTableColumns(safeTableName);
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Standard table has no columns: " + safeTableName);
        }
        String columnSql = columns.stream()
                .map(this::quotedIdentifier)
                .collect(java.util.stream.Collectors.joining(", "));
        String keywordWhere = "";
        List<Object> args = new ArrayList<>();
        if (!safeKeyword.isBlank()) {
            String concatSql = columns.stream()
                    .map(column -> "COALESCE(CAST(" + quotedIdentifier(column) + " AS CHAR), '')")
                    .collect(java.util.stream.Collectors.joining(", "));
            keywordWhere = " WHERE CONCAT_WS('|', " + concatSql + ") LIKE CONCAT('%', ?, '%')";
            args.add(safeKeyword);
        }
        args.add(safeLimit);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT " + columnSql + " FROM " + quotedIdentifier(safeTableName) + keywordWhere + " LIMIT ?",
                args.toArray()
        );
        systemAuditService.record("report", "standard-table-print", "STANDARD_TABLE", safeTableName,
                reportAuditSummary(
                        auditPart("table", safeTableName),
                        auditPart("keyword", safeKeyword.isBlank() ? "ALL" : safeKeyword),
                        auditPart("limit", safeLimit),
                        auditPart("columns", columns.size()),
                        auditPart("rows", rows.size())
                ));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .body(standardTableHtmlV2(safeTableName, standardTableTitle(safeTableName), safeKeyword, columns, rows));
    }

    private List<ReportCatalogItem> reportCatalog() {
        Map<String, ReportCatalogItem> items = new LinkedHashMap<>();
        items.put("STANDARD_TABLE_PRINT", new ReportCatalogItem(
                "STANDARD_TABLE_PRINT",
                "工资标准表打印",
                "标准维护",
                "bz06_*.frx / 标准表",
                "已迁移",
                "/api/reports/standard-tables/print?tableName=bz06_jbt"
        ));
        items.put("SALARY_CHANGE_LEDGER_PRINT", new ReportCatalogItem(
                "SALARY_CHANGE_LEDGER_PRINT",
                "工资变动管理台账打印",
                "统计汇总",
                "GZGLTZ.frx / gzbdmx.frx",
                "已迁移",
                "/api/reports/salary-change-ledger/print?orgCode={orgCode}"
        ));
        items.put("ASSESSMENT_SUMMARY_PRINT", new ReportCatalogItem(
                "ASSESSMENT_SUMMARY_PRINT",
                "年度考核统计表打印",
                "统计汇总",
                "KHQKTJB.frx / khtj.frx",
                "已迁移",
                "/api/reports/assessment-summary/print?orgCode={orgCode}"
        ));
        items.put("PERSON_ROSTER_PRINT", new ReportCatalogItem(
                "PERSON_ROSTER_PRINT",
                "人员工资花名册打印",
                "工资名册",
                "HMC.frx / hmc4.frx",
                "已迁移",
                "/api/reports/person-roster/print?orgCode={orgCode}"
        ));
        items.put("SALARY_CASE_APPROVAL_PRINT", new ReportCatalogItem(
                "SALARY_CASE_APPROVAL_PRINT",
                "工资变动审批表打印",
                "审批打印",
                "spb.frx / SPBDB.frx",
                "已迁移",
                "/api/reports/salary-case-approval/print?caseNo={caseNo}"
        ));
        items.put("SALARY_ROSTER_PRINT", new ReportCatalogItem(
                "SALARY_ROSTER_PRINT",
                "工资表/工资名册打印",
                "工资名册",
                "GZB_XZ.frx / GZB_SY.frx",
                "已迁移",
                "/api/reports/salary-roster/print?orgCode={orgCode}"
        ));
        items.put("SALARY_HISTORY_PRINT", new ReportCatalogItem(
                "SALARY_HISTORY_PRINT",
                "工资历史变动明细打印",
                "工资业务",
                "gzbdmx.frx",
                "已迁移",
                "/api/reports/salary-history/print?orgCode={orgCode}"
        ));
        Path reportsDir = legacyReportsDir();
        if (Files.isDirectory(reportsDir)) {
            try (var stream = Files.list(reportsDir)) {
                stream.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".frx"))
                        .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            String code = fileName.substring(0, fileName.length() - 4).toUpperCase(Locale.ROOT);
                            items.putIfAbsent(code, new ReportCatalogItem(
                                    code,
                                    legacyReportTitle(code),
                                    legacyReportCategory(code),
                                    fileName,
                                    "待迁移",
                                    ""
                            ));
                        });
            } catch (Exception ignored) {
                // Catalog remains usable even when the legacy REPORTS folder is not available.
            }
        }
        return List.copyOf(items.values());
    }

    private Map<String, Object> reportMigrationClosureBatchSummary(String orgCode, int year, int month, String keyword) {
        List<Object> args = new ArrayList<>();
        String orgWhere = "AND " + organizationAccessService.orgCodeAccessSql("batch.org_code");
        String safeOrgCode = text(orgCode);
        if (!safeOrgCode.isBlank()) {
            orgWhere += " AND batch.org_code = ?";
            args.add(safeOrgCode);
        }
        String yearWhere = "";
        if (year > 0) {
            yearWhere = "AND batch.event_year = ?";
            args.add(year);
        }
        String monthWhere = "";
        if (month > 0) {
            monthWhere = "AND batch.event_month = ?";
            args.add(month);
        }
        String keywordWhere = "";
        String safeKeyword = text(keyword);
        if (!safeKeyword.isBlank()) {
            keywordWhere = """
                    AND (
                        batch.batch_no LIKE CONCAT('%', ?, '%')
                        OR batch.report_type LIKE CONCAT('%', ?, '%')
                        OR batch.keyword LIKE CONCAT('%', ?, '%')
                        OR batch.summary LIKE CONCAT('%', ?, '%')
                    )
                    """;
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
        }
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT COUNT(1) AS batchCount,
                       COALESCE(SUM(printed_count), 0) AS printedRows,
                       COALESCE(SUM(blocked_count), 0) AS blockedRows,
                       COALESCE(SUM(warning_count), 0) AS warningRows,
                       COALESCE(SUM(CASE WHEN blocked_count > 0 THEN 1 ELSE 0 END), 0) AS blockedBatches,
                       COALESCE(SUM(CASE WHEN acceptance.id IS NULL THEN 1 ELSE 0 END), 0) AS pendingAcceptanceBatches,
                       COALESCE(SUM(CASE WHEN acceptance.id IS NULL THEN 0 ELSE 1 END), 0) AS acceptanceExportedBatches,
                       MAX(batch.printed_at) AS latestPrintedAt,
                       MAX(acceptance.created_at) AS latestAcceptanceExportedAt
                FROM salary_report_print_batch batch
                LEFT JOIN (
                    SELECT audit.*
                    FROM sys_audit_log audit
                    JOIN (
                        SELECT target_code, MAX(id) AS latest_id
                        FROM sys_audit_log
                        WHERE module_name = 'report'
                          AND action_name = 'report-print-batch-acceptance-package'
                          AND target_type = 'REPORT_PRINT_BATCH'
                        GROUP BY target_code
                    ) latest ON latest.latest_id = audit.id
                ) acceptance ON acceptance.target_code = batch.batch_no
                WHERE 1 = 1
                __ORG_WHERE__
                __YEAR_WHERE__
                __MONTH_WHERE__
                __KEYWORD_WHERE__
                """.replace("__ORG_WHERE__", orgWhere)
                .replace("__YEAR_WHERE__", yearWhere)
                .replace("__MONTH_WHERE__", monthWhere)
                .replace("__KEYWORD_WHERE__", keywordWhere), args.toArray());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("batchCount", number(row.get("batchCount")));
        result.put("printedRows", number(row.get("printedRows")));
        result.put("blockedRows", number(row.get("blockedRows")));
        result.put("warningRows", number(row.get("warningRows")));
        result.put("blockedBatches", number(row.get("blockedBatches")));
        result.put("pendingAcceptanceBatches", number(row.get("pendingAcceptanceBatches")));
        result.put("acceptanceExportedBatches", number(row.get("acceptanceExportedBatches")));
        result.put("latestPrintedAt", text(row.get("latestPrintedAt")));
        result.put("latestAcceptanceExportedAt", text(row.get("latestAcceptanceExportedAt")));
        return result;
    }

    private Map<String, Object> reportMigrationClosureAuditSummary(String orgCode, int year, int month, String keyword) {
        List<Object> args = new ArrayList<>();
        String moduleWhere = "module_name = 'report'";
        String orgWhere = "AND " + organizationAccessService.orgCodeAccessSql("target_code");
        String safeOrgCode = text(orgCode);
        if (!safeOrgCode.isBlank()) {
            moduleWhere = """
                    (
                        module_name = 'report'
                        OR (
                            module_name = 'workbench'
                            AND action_name IN (
                                'migration-quality-acceptance-package-preview',
                                'migration-quality-acceptance-package-export'
                            )
                        )
                    )
                    """;
            orgWhere = """
                    AND (
                        (
                            module_name = 'report'
                            AND __REPORT_ORG_ACCESS__
                            AND (target_code = ? OR summary LIKE CONCAT('%org=', ?, '%') OR summary LIKE CONCAT('%org:', ?, '%'))
                        )
                        OR (
                            module_name = 'workbench'
                            AND (summary LIKE CONCAT('%org=', ?, '%') OR summary LIKE CONCAT('%org:', ?, '%'))
                        )
                    )
                    """.replace("__REPORT_ORG_ACCESS__", organizationAccessService.orgCodeAccessSql("target_code"));
            args.add(safeOrgCode);
            args.add(safeOrgCode);
            args.add(safeOrgCode);
            args.add(safeOrgCode);
            args.add(safeOrgCode);
        }
        String yearWhere = "";
        if (year > 0) {
            yearWhere = "AND (summary LIKE CONCAT('%period=', ?, '%') OR summary LIKE CONCAT('%', ?, '.%', '%'))";
            args.add(String.valueOf(year));
            args.add(String.valueOf(year));
        }
        String monthWhere = "";
        if (month > 0) {
            monthWhere = "AND summary LIKE CONCAT('%.', ?, '%')";
            args.add(String.format("%02d", month));
        }
        String keywordWhere = "";
        String safeKeyword = text(keyword);
        if (!safeKeyword.isBlank()) {
            keywordWhere = """
                    AND (
                        action_name LIKE CONCAT('%', ?, '%')
                        OR target_code LIKE CONCAT('%', ?, '%')
                        OR summary LIKE CONCAT('%', ?, '%')
                    )
                    """;
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
        }
        String sql = """
                FROM sys_audit_log
                WHERE __MODULE_WHERE__
                __ORG_WHERE__
                __YEAR_WHERE__
                __MONTH_WHERE__
                __KEYWORD_WHERE__
                """.replace("__MODULE_WHERE__", moduleWhere)
                .replace("__ORG_WHERE__", orgWhere)
                .replace("__YEAR_WHERE__", yearWhere)
                .replace("__MONTH_WHERE__", monthWhere)
                .replace("__KEYWORD_WHERE__", keywordWhere);
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT COUNT(1) AS auditCount,
                       MAX(created_at) AS latestAuditAt
                """ + sql, args.toArray());
        List<Map<String, Object>> actions = jdbcTemplate.queryForList("""
                SELECT action_name AS actionName, COUNT(1) AS actionCount
                """ + sql + """
                GROUP BY action_name
                ORDER BY actionCount DESC, actionName
                LIMIT 8
                """, args.toArray());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("auditCount", number(row.get("auditCount")));
        result.put("latestAuditAt", text(row.get("latestAuditAt")));
        result.put("actions", actions);
        return result;
    }

    private Path legacyReportsDir() {
        List<Path> candidates = List.of(
                Path.of("REPORTS"),
                Path.of("..", "REPORTS")
        );
        for (Path candidate : candidates) {
            Path normalized = candidate.toAbsolutePath().normalize();
            if (Files.isDirectory(normalized)) {
                return normalized;
            }
        }
        return candidates.get(0).toAbsolutePath().normalize();
    }

    private String salaryHistoryHtml(String orgCode, String personCode, int yearFrom, int yearTo, List<Map<String, Object>> rows) {
        String title = "工资历史变动明细";
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>工资历史变动明细</title>
                    <style>
                        * { box-sizing: border-box; }
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 18px; background: #fff; }
                        .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 12px; }
                        button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        .print-sheet { max-width: 1120px; margin: 0 auto; }
                        h1 { text-align: center; font-size: 22px; margin: 4px 0 6px; letter-spacing: 0; }
                        body > h1:first-of-type { display: none; }
                        .print-subtitle { display: flex; justify-content: space-between; font-size: 12px; color: #4b5563; margin-bottom: 10px; }
                        .meta { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px 18px; font-size: 13px; margin-bottom: 10px; }
                        table { width: 100%; border-collapse: collapse; font-size: 12px; }
                        th, td { border: 1px solid #4b5563; padding: 4px 6px; vertical-align: middle; }
                        th { background: #eef2f7; font-weight: 700; }
                        td.amount { text-align: right; }
                        tr.person-row td { background: #f8fafc; font-weight: 700; }
                        @page { size: A4 landscape; margin: 12mm; }
                        @media print { .toolbar { display: none; } body { margin: 0; } .print-sheet { max-width: none; } thead { display: table-header-group; } tfoot { display: table-footer-group; } tr { break-inside: avoid; } }
                    </style>
                </head>
                <body>
                    <div class="toolbar"><button onclick="window.print()">打印</button></div>
                """);
        html.append("<h1>").append(escapeHtml(title)).append("</h1>");
        html.append("<div class=\"meta\"><span>单位：").append(escapeHtml(orgCode)).append("</span>")
                .append("<span>人员：").append(escapeHtml(personCode.isBlank() ? "全部" : personCode)).append("</span>")
                .append("<span>年度：").append(yearFrom).append("-").append(yearTo).append("</span>")
                .append("<span>生成时间：").append(escapeHtml(LocalDateTime.now().withNano(0).toString())).append("</span></div>");
        html.append("""
                <table>
                    <thead>
                    <tr>
                        <th>人员编码</th><th>姓名</th><th>执行年月</th><th>变动类别</th><th>职务/岗位</th>
                        <th>级别</th><th>档次/薪级</th><th>职务工资</th><th>级别工资</th>
                        <th>基础性</th><th>奖励性</th><th>合计</th><th>历史ID</th>
                    </tr>
                    </thead>
                    <tbody>
                """);
        if (rows.isEmpty()) {
            html.append("<tr><td colspan=\"13\" style=\"text-align:center;\">无数据</td></tr>");
        }
        for (Map<String, Object> row : rows) {
            html.append("<tr>")
                    .append(td(row.get("personCode")))
                    .append(td(row.get("personName")))
                    .append(td(text(row.get("yearText")) + "." + text(row.get("monthText"))))
                    .append(td(row.get("changeType")))
                    .append(td(row.get("postCode")))
                    .append(td(row.get("levelCode")))
                    .append(td(row.get("gradeStep")))
                    .append(amountTd(row.get("postSalary")))
                    .append(amountTd(row.get("levelSalary")))
                    .append(amountTd(row.get("baseSalary")))
                    .append(amountTd(row.get("performanceSalary")))
                    .append(amountTd(row.get("totalSalary")))
                    .append(td(row.get("historyId")))
                    .append("</tr>");
        }
        html.append("</tbody></table></body></html>");
        return html.toString();
    }

    private String salaryHistoryHtmlV2(String orgCode, String personCode, int yearFrom, int yearTo, List<Map<String, Object>> rows) {
        BigDecimal totalSalary = rows.stream()
                .map(row -> amount(row.get("totalSalary")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Long> changeCounts = rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        row -> text(row.get("changeType")).isBlank() ? "-" : text(row.get("changeType")),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()
                ));
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>&#24037;&#36164;&#21382;&#21490;&#21464;&#21160;&#26126;&#32454;</title>
                    <style>
                        * { box-sizing: border-box; }
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 18px; background: #fff; }
                        .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 12px; }
                        button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        .print-sheet { max-width: 1120px; margin: 0 auto; }
                        h1 { text-align: center; font-size: 22px; margin: 4px 0 6px; letter-spacing: 0; }
                        .print-subtitle { display: flex; justify-content: space-between; font-size: 12px; color: #4b5563; margin-bottom: 10px; }
                        .meta { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px 18px; font-size: 13px; margin-bottom: 10px; }
                        .summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); border: 1px solid #4b5563; border-bottom: 0; margin-bottom: 12px; }
                        .summary-grid span { border-right: 1px solid #4b5563; border-bottom: 1px solid #4b5563; padding: 7px 8px; font-size: 13px; min-height: 30px; }
                        .summary-grid span:nth-child(4n) { border-right: 0; }
                        table { width: 100%; border-collapse: collapse; font-size: 11px; }
                        th, td { border: 1px solid #4b5563; padding: 4px 5px; vertical-align: middle; }
                        th { background: #eef2f7; font-weight: 700; }
                        td.amount, th.amount { text-align: right; }
                        @page { size: A4 landscape; margin: 10mm; }
                        @media print { .toolbar { display: none; } body { margin: 0; } .print-sheet { max-width: none; } thead { display: table-header-group; } tr, .summary-grid { break-inside: avoid; } }
                    </style>
                </head>
                <body>
                    <div class="toolbar"><button onclick="window.print()">&#25171;&#21360;</button></div>
                    <main class="print-sheet">
                """);
        html.append("<h1>&#24037;&#36164;&#21382;&#21490;&#21464;&#21160;&#26126;&#32454;</h1>");
        html.append("<div class=\"print-subtitle\"><span>&#21382;&#21490;&#24037;&#36164;&#38142;&#26126;&#32454;</span><span>&#25171;&#21360;&#26102;&#38388;&#65306;")
                .append(escapeHtml(LocalDateTime.now().withNano(0).toString()))
                .append("</span></div>");
        html.append("<div class=\"meta\"><span>&#21333;&#20301;&#65306;").append(escapeHtml(orgCode)).append("</span>")
                .append("<span>&#20154;&#21592;&#65306;").append(escapeHtml(personCode.isBlank() ? "全部" : personCode)).append("</span>")
                .append("<span>&#24180;&#24230;&#65306;").append(yearFrom).append("-").append(yearTo).append("</span>")
                .append("<span>&#29983;&#25104;&#26102;&#38388;&#65306;").append(escapeHtml(LocalDateTime.now().withNano(0).toString())).append("</span></div>");
        html.append("<div class=\"summary-grid\">")
                .append(approvalCell("&#26126;&#32454;&#31508;&#25968;", rows.size()))
                .append(approvalCell("&#21464;&#21160;&#31867;&#22411;", changeCounts.size()))
                .append(approvalCell("&#24037;&#36164;&#21512;&#35745;&#27719;&#24635;", totalSalary))
                .append(approvalCell("&#20154;&#21592;&#33539;&#22260;", personCode.isBlank() ? "全部" : personCode));
        for (Map.Entry<String, Long> entry : changeCounts.entrySet()) {
            html.append(approvalCell(entry.getKey(), entry.getValue()));
        }
        html.append("</div>");
        html.append("""
                <table>
                    <thead>
                    <tr>
                        <th>&#20154;&#21592;&#32534;&#30721;</th><th>&#22995;&#21517;</th><th>&#25191;&#34892;&#24180;&#26376;</th><th>&#21464;&#21160;&#31867;&#21035;</th><th>&#32844;&#21153;/&#23703;&#20301;</th>
                        <th>&#32423;&#21035;</th><th>&#26723;&#27425;/&#34218;&#32423;</th><th class="amount">&#32844;&#21153;&#24037;&#36164;</th><th class="amount">&#32423;&#21035;&#24037;&#36164;</th>
                        <th class="amount">&#22522;&#30784;&#24635;</th><th class="amount">&#22870;&#21169;&#24635;</th><th class="amount">&#21512;&#35745;</th><th>&#21382;&#21490;ID</th>
                    </tr>
                    </thead>
                    <tbody>
                """);
        if (rows.isEmpty()) {
            html.append("<tr><td colspan=\"13\" style=\"text-align:center;\">&#26080;&#25968;&#25454;</td></tr>");
        }
        for (Map<String, Object> row : rows) {
            html.append("<tr>")
                    .append(td(row.get("personCode")))
                    .append(td(row.get("personName")))
                    .append(td(text(row.get("yearText")) + "." + text(row.get("monthText"))))
                    .append(td(row.get("changeType")))
                    .append(td(row.get("postCode")))
                    .append(td(row.get("levelCode")))
                    .append(td(row.get("gradeStep")))
                    .append(amountTd(row.get("postSalary")))
                    .append(amountTd(row.get("levelSalary")))
                    .append(amountTd(row.get("baseSalary")))
                    .append(amountTd(row.get("performanceSalary")))
                    .append(amountTd(row.get("totalSalary")))
                    .append(td(row.get("historyId")))
                    .append("</tr>");
        }
        html.append("</tbody></table></main></body></html>");
        return html.toString();
    }

    private String salaryRosterHtml(String orgCode, int year, int month, List<Map<String, Object>> rows, List<ReportSalaryColumn> salaryColumns) {
        if (!salaryColumns.isEmpty()) {
            return salaryRosterDynamicHtml(orgCode, year, month, rows, salaryColumns);
        }
        BigDecimal total = rows.stream()
                .map(row -> amount(row.get("totalSalary")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>工资表</title>
                    <style>
                        * { box-sizing: border-box; }
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 18px; background: #fff; }
                        .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 12px; }
                        button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        .print-sheet { max-width: 1120px; margin: 0 auto; }
                        h1 { text-align: center; font-size: 22px; margin: 4px 0 6px; letter-spacing: 0; }
                        body > h1:first-of-type { display: none; }
                        .print-subtitle { display: flex; justify-content: space-between; font-size: 12px; color: #4b5563; margin-bottom: 10px; }
                        .meta { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px 18px; font-size: 13px; margin-bottom: 10px; }
                        table { width: 100%; border-collapse: collapse; font-size: 12px; }
                        th, td { border: 1px solid #4b5563; padding: 4px 6px; vertical-align: middle; }
                        th { background: #eef2f7; font-weight: 700; }
                        td.amount, th.amount { text-align: right; }
                        tfoot td { font-weight: 700; background: #f8fafc; }
                        @page { size: A4 landscape; margin: 12mm; }
                        @media print { .toolbar { display: none; } body { margin: 0; } .print-sheet { max-width: none; } thead { display: table-header-group; } tfoot { display: table-footer-group; } tr { break-inside: avoid; } }
                    </style>
                </head>
                <body>
                    <div class="toolbar"><button onclick="window.print()">打印</button></div>
                """);
        html.append("<h1>工资表</h1>");
        html.append("<div class=\"meta\"><span>单位：").append(escapeHtml(orgCode)).append("</span>")
                .append("<span>工资期间：").append(year).append(".").append(String.format("%02d", month)).append("</span>")
                .append("<span>人数：").append(rows.size()).append("</span>")
                .append("<span>生成时间：").append(escapeHtml(LocalDateTime.now().withNano(0).toString())).append("</span></div>");
        html.append("""
                <table>
                    <thead>
                    <tr>
                        <th>序号</th><th>人员编码</th><th>姓名</th><th>执行年月</th><th>最近变动</th><th>职务/岗位</th>
                        <th>级别</th><th>档次/薪级</th><th class="amount">职务工资</th><th class="amount">级别工资</th>
                        <th class="amount">基础性</th><th class="amount">奖励性</th><th class="amount">合计</th>
                    </tr>
                    </thead>
                    <tbody>
                """);
        if (rows.isEmpty()) {
            html.append("<tr><td colspan=\"13\" style=\"text-align:center;\">无数据</td></tr>");
        }
        int index = 1;
        for (Map<String, Object> row : rows) {
            html.append("<tr>")
                    .append(td(index++))
                    .append(td(row.get("personCode")))
                    .append(td(row.get("personName")))
                    .append(td(text(row.get("yearText")) + "." + text(row.get("monthText"))))
                    .append(td(row.get("changeType")))
                    .append(td(row.get("postCode")))
                    .append(td(row.get("levelCode")))
                    .append(td(row.get("gradeStep")))
                    .append(amountTd(row.get("postSalary")))
                    .append(amountTd(row.get("levelSalary")))
                    .append(amountTd(row.get("baseSalary")))
                    .append(amountTd(row.get("performanceSalary")))
                    .append(amountTd(row.get("totalSalary")))
                    .append("</tr>");
        }
        html.append("<tfoot><tr><td colspan=\"12\" class=\"amount\">合计</td><td class=\"amount\">")
                .append(escapeHtml(total.stripTrailingZeros().toPlainString()))
                .append("</td></tr></tfoot></table></body></html>");
        return html.toString();
    }

    private String salaryRosterDynamicHtml(String orgCode, int year, int month, List<Map<String, Object>> rows, List<ReportSalaryColumn> salaryColumns) {
        BigDecimal total = rows.stream()
                .map(row -> amount(row.get("totalSalary")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>&#24037;&#36164;&#34920;</title>
                    <style>
                        * { box-sizing: border-box; }
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 18px; background: #fff; }
                        .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 12px; }
                        button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        .print-sheet { max-width: 1120px; margin: 0 auto; }
                        h1 { text-align: center; font-size: 22px; margin: 4px 0 6px; letter-spacing: 0; }
                        .print-subtitle { display: flex; justify-content: space-between; font-size: 12px; color: #4b5563; margin-bottom: 10px; }
                        .meta { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px 18px; font-size: 13px; margin-bottom: 10px; }
                        table { width: 100%; border-collapse: collapse; font-size: 11px; }
                        th, td { border: 1px solid #4b5563; padding: 4px 5px; vertical-align: middle; }
                        th { background: #eef2f7; font-weight: 700; }
                        td.amount, th.amount { text-align: right; }
                        tfoot td { font-weight: 700; background: #f8fafc; }
                        @page { size: A4 landscape; margin: 10mm; }
                        @media print { .toolbar { display: none; } body { margin: 0; } .print-sheet { max-width: none; } thead { display: table-header-group; } tfoot { display: table-footer-group; } tr { break-inside: avoid; } }
                    </style>
                </head>
                <body>
                    <div class="toolbar"><button onclick="window.print()">&#25171;&#21360;</button></div>
                """);
        html.append("<main class=\"print-sheet\"><h1>&#24037;&#36164;&#34920;</h1>");
        html.append("<div class=\"print-subtitle\"><span>&#24037;&#36164;&#21517;&#20876;&#25171;&#21360;</span><span>&#25171;&#21360;&#26102;&#38388;&#65306;")
                .append(escapeHtml(LocalDateTime.now().withNano(0).toString()))
                .append("</span></div>");
        html.append("<div class=\"meta\"><span>&#21333;&#20301;&#65306;").append(escapeHtml(orgCode)).append("</span>")
                .append("<span>&#24037;&#36164;&#26399;&#38388;&#65306;").append(year).append(".").append(String.format("%02d", month)).append("</span>")
                .append("<span>&#20154;&#25968;&#65306;").append(rows.size()).append("</span>")
                .append("<span>&#21015;&#25968;&#65306;").append(salaryColumns.size()).append("</span>")
                .append("<span>&#29983;&#25104;&#26102;&#38388;&#65306;").append(escapeHtml(LocalDateTime.now().withNano(0).toString())).append("</span></div>");
        html.append("<table><thead><tr>")
                .append("<th>&#24207;&#21495;</th><th>&#20154;&#21592;&#32534;&#30721;</th><th>&#22995;&#21517;</th><th>&#25191;&#34892;&#24180;&#26376;</th><th>&#26368;&#36817;&#21464;&#21160;</th><th>&#32844;&#21153;/&#23703;&#20301;</th><th>&#32423;&#21035;</th><th>&#26723;&#27425;/&#34218;&#32423;</th>");
        for (ReportSalaryColumn column : salaryColumns) {
            html.append("<th class=\"amount\">").append(escapeHtml(column.title())).append("</th>");
        }
        html.append("<th class=\"amount\">&#21512;&#35745;</th></tr></thead><tbody>");
        if (rows.isEmpty()) {
            html.append("<tr><td colspan=\"").append(9 + salaryColumns.size()).append("\" style=\"text-align:center;\">&#26080;&#25968;&#25454;</td></tr>");
        }
        int index = 1;
        for (Map<String, Object> row : rows) {
            html.append("<tr>")
                    .append(td(index++))
                    .append(td(row.get("personCode")))
                    .append(td(row.get("personName")))
                    .append(td(text(row.get("yearText")) + "." + text(row.get("monthText"))))
                    .append(td(row.get("changeType")))
                    .append(td(row.get("postCode")))
                    .append(td(row.get("levelCode")))
                    .append(td(row.get("gradeStep")));
            for (ReportSalaryColumn column : salaryColumns) {
                html.append(amountTd(row.get("salary_" + column.code())));
            }
            html.append(amountTd(row.get("totalSalary"))).append("</tr>");
        }
        html.append("<tfoot><tr><td colspan=\"").append(8 + salaryColumns.size()).append("\" class=\"amount\">&#21512;&#35745;</td><td class=\"amount\">")
                .append(escapeHtml(total.stripTrailingZeros().toPlainString()))
                .append("</td></tr></tfoot></table></main></body></html>");
        return html.toString();
    }

    private String salaryRosterDynamicCsv(List<Map<String, Object>> rows, List<ReportSalaryColumn> salaryColumns) {
        StringBuilder csv = new StringBuilder();
        List<Object> header = new ArrayList<>(List.of("序号", "人员编码", "姓名", "执行年月", "最近变动", "职务/岗位", "级别", "档次/薪级"));
        salaryColumns.forEach(column -> header.add(column.title()));
        header.add("合计");
        header.add("历史ID");
        csvRow(csv, header.toArray());
        int index = 1;
        for (Map<String, Object> row : rows) {
            List<Object> values = new ArrayList<>(List.of(
                    index++,
                    row.get("personCode"),
                    row.get("personName"),
                    text(row.get("yearText")) + "." + text(row.get("monthText")),
                    row.get("changeType"),
                    row.get("postCode"),
                    row.get("levelCode"),
                    row.get("gradeStep")
            ));
            salaryColumns.forEach(column -> values.add(amountText(row.get("salary_" + column.code()))));
            values.add(amountText(row.get("totalSalary")));
            values.add(row.get("historyId"));
            csvRow(csv, values.toArray());
        }
        return csv.toString();
    }

    private String salaryCaseApprovalHtml(
            Map<String, Object> businessCase,
            Map<String, Object> snapshot,
            List<Map<String, Object>> salaryItems,
            Map<String, Object> person,
            Map<String, Object> writePlan
    ) {
        String caseNo = text(businessCase.get("case_no"));
        String period = text(businessCase.get("event_year")) + "." + String.format("%02d", Math.max(1, number(businessCase.get("event_month"))));
        String businessType = text(businessCase.get("business_type"));
        String title = text(businessCase.get("title"));
        if (title.isBlank()) {
            title = businessType;
        }
        String printTitle = salaryCaseApprovalTitleText(businessType);
        BigDecimal total = salaryItems.stream()
                .map(item -> {
                    Object amountValue = item.containsKey("amount") ? item.get("amount") : item.get("afterAmount");
                    return amount(amountValue);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String calculatedTotal = amountText(firstPresent(
                snapshot.get("trial_calculated_total"),
                businessCase.get("trial_calculated_total"),
                total
        ));
        String expectedTotal = amountText(firstPresent(
                snapshot.get("trial_expected_total"),
                businessCase.get("trial_expected_total"),
                total
        ));
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>工资变动审批表</title>
                    <style>
                        * { box-sizing: border-box; }
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 20px; background: #fff; }
                        .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 12px; }
                        button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        .approval-doc { max-width: 780px; margin: 0 auto; }
                        h1 { text-align: center; font-size: 24px; margin: 4px 0 6px; letter-spacing: 0; }
                        body > h1:first-of-type { display: none; }
                        .approval-subtitle { display: flex; justify-content: space-between; font-size: 12px; color: #4b5563; margin-bottom: 10px; }
                        .meta { display: grid; grid-template-columns: repeat(4, 1fr); gap: 0; border: 1px solid #4b5563; border-bottom: 0; font-size: 13px; }
                        .meta span { border-right: 1px solid #4b5563; border-bottom: 1px solid #4b5563; padding: 6px 8px; min-height: 22px; }
                        .meta span:nth-child(4n) { border-right: 0; }
                        table { width: 100%; border-collapse: collapse; font-size: 12px; margin-top: 12px; }
                        th, td { border: 1px solid #4b5563; padding: 5px 7px; vertical-align: middle; }
                        th { background: #eef2f7; font-weight: 700; }
                        td.amount, th.amount { text-align: right; }
                        .section-title { margin-top: 12px; padding: 6px 8px; border: 1px solid #4b5563; background: #f8fafc; font-weight: 700; font-size: 13px; }
                        .basis { display: grid; grid-template-columns: repeat(4, 1fr); border: 1px solid #4b5563; border-top: 0; font-size: 13px; }
                        .basis span { border-right: 1px solid #4b5563; border-bottom: 1px solid #4b5563; padding: 6px 8px; min-height: 22px; }
                        .basis span:nth-child(4n) { border-right: 0; }
                        .basis span.wide { grid-column: span 2; }
                        .opinion { margin-top: 12px; display: grid; grid-template-columns: 1fr 1fr 1fr; border: 1px solid #4b5563; }
                        .opinion div { min-height: 72px; border-right: 1px solid #4b5563; padding: 8px; font-size: 13px; }
                        .opinion div:last-child { border-right: 0; }
                        .approval-total-grid, .approval-workflow { display: grid; grid-template-columns: repeat(4, 1fr); border: 1px solid #4b5563; border-top: 0; font-size: 13px; }
                        .approval-total-grid span, .approval-workflow span { border-right: 1px solid #4b5563; border-bottom: 1px solid #4b5563; padding: 6px 8px; min-height: 22px; }
                        .approval-total-grid span:nth-child(4n), .approval-workflow span:nth-child(4n) { border-right: 0; }
                        .approval-signature { margin-top: 14px; border: 1px solid #4b5563; font-size: 13px; }
                        .approval-signature-row { display: grid; grid-template-columns: 92px 1fr 92px 1fr; min-height: 58px; border-bottom: 1px solid #4b5563; }
                        .approval-signature-row:last-child { border-bottom: 0; }
                        .approval-signature-row b, .approval-signature-row span { padding: 8px; border-right: 1px solid #4b5563; }
                        .approval-signature-row span:last-child { border-right: 0; }
                        .summary { margin-top: 10px; font-size: 13px; line-height: 1.7; }
                        .approval-print-note { margin-top: 8px; color: #6b7280; font-size: 11px; text-align: right; }
                        @page { size: A4 portrait; margin: 12mm; }
                        @media print {
                            .toolbar { display: none; }
                            body { margin: 0; }
                            .approval-doc { max-width: none; }
                            table, .meta, .basis, .approval-total-grid, .approval-workflow, .approval-signature { break-inside: avoid; }
                        }
                    </style>
                </head>
                <body>
                    <div class="toolbar"><button onclick="window.print()">打印</button></div>
                """);
        html.append("<h1>工资变动审批表</h1>");
        html.append("<main class=\"approval-doc\">");
        html.append("<h1 class=\"approval-real-title\">").append(escapeHtml(printTitle)).append("</h1>");
        html.append("<div class=\"approval-subtitle\"><span>&#24037;&#36164;&#19994;&#21153;&#23457;&#25209;&#34920;</span><span>&#25171;&#21360;&#26102;&#38388;&#65306;")
                .append(escapeHtml(LocalDateTime.now().withNano(0).toString()))
                .append("</span></div>");
        html.append("<div class=\"meta\">")
                .append(meta("办理编号", caseNo))
                .append(meta("人员编码", businessCase.get("person_code")))
                .append(meta("姓名", businessCase.get("person_name")))
                .append(meta("单位", businessCase.get("org_code")))
                .append(meta("执行年月", period))
                .append(meta("业务类型", businessCase.get("business_type")))
                .append(meta("办理状态", reportStatusText("case", businessCase.get("status"))))
                .append(meta("试算状态", reportStatusText("trial", businessCase.get("trial_status"))))
                .append(meta("试算前合计", businessCase.get("trial_baseline_total")))
                .append(meta("试算后合计", calculatedTotal))
                .append(meta("历史期望合计", expectedTotal))
                .append(meta("差额", businessCase.get("trial_difference")))
                .append(meta("办理人", businessCase.get("handled_by")))
                .append(meta("办理时间", businessCase.get("handled_at")))
                .append(meta("快照人", snapshot.get("snapshot_by")))
                .append(meta("快照时间", snapshot.get("snapshot_at")))
                .append("</div>");
        html.append(salaryCaseApprovalTotalsHtml(businessCase, snapshot, salaryItems, writePlan));
        html.append(salaryCaseBasisHtml(businessCase, snapshot, person, writePlan, businessType));
        html.append(salaryCaseApprovalWorkflowHtml(businessCase, snapshot, writePlan));
        html.append("<div class=\"summary\"><strong>")
                .append(escapeHtml(title))
                .append("</strong><br>")
                .append(escapeHtml(text(businessCase.get("summary"))))
                .append("<br>试算说明：")
                .append(escapeHtml(text(businessCase.get("trial_summary"))))
                .append("</div>");
        html.append("""
                <table>
                    <thead>
                    <tr>
                        <th>序号</th><th>工资项编码</th><th>工资项名称</th>
                        <th class="amount">原金额</th><th class="amount">新金额</th><th class="amount">差额</th><th>说明</th>
                    </tr>
                    </thead>
                    <tbody>
                """);
        if (salaryItems.isEmpty()) {
            html.append("<tr><td colspan=\"7\" style=\"text-align:center;\">无工资项目明细</td></tr>");
        }
        int index = 1;
        for (Map<String, Object> item : salaryItems) {
            Object beforeAmount = item.get("beforeAmount");
            Object afterAmount = item.containsKey("amount") ? item.get("amount") : item.get("afterAmount");
            Object difference = item.get("difference");
            html.append("<tr>")
                    .append(td(index++))
                    .append(td(item.get("itemCode")))
                    .append(td(item.get("itemName")))
                    .append(amountTd(beforeAmount))
                    .append(amountTd(afterAmount))
                    .append(amountTd(difference))
                    .append(td(item.get("ruleNote")))
                    .append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("<div class=\"opinion\"><div>经办意见：<br><br></div><div>审核意见：<br><br></div><div>审批意见：<br><br></div></div>");
        html.append(salaryCaseApprovalSignatureHtml());
        html.append("<div class=\"approval-print-note\">&#26412;&#34920;&#30001;&#24037;&#36164;&#19994;&#21153;&#36801;&#31227;&#31995;&#32479;&#25353;&#24403;&#21069;&#22522;&#30784;&#20449;&#24687;&#12289;&#25919;&#31574;&#35268;&#21017;&#21644;&#21150;&#29702;&#24555;&#29031;&#29983;&#25104;&#12290;</div>");
        html.append("</main></body></html>");
        return html.toString();
    }

    private String salaryCaseApprovalTotalsHtml(
            Map<String, Object> businessCase,
            Map<String, Object> snapshot,
            List<Map<String, Object>> salaryItems,
            Map<String, Object> writePlan
    ) {
        BigDecimal beforeTotal = salaryItems.stream()
                .map(item -> amount(item.get("beforeAmount")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal afterTotal = salaryItems.stream()
                .map(item -> amount(item.containsKey("amount") ? item.get("amount") : item.get("afterAmount")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal itemDifference = salaryItems.stream()
                .map(item -> amount(item.get("difference")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Object baselineTotal = firstPresent(businessCase.get("trial_baseline_total"), beforeTotal);
        Object calculatedTotal = firstPresent(snapshot.get("trial_calculated_total"), businessCase.get("trial_calculated_total"), afterTotal);
        Object expectedTotal = firstPresent(snapshot.get("trial_expected_total"), businessCase.get("trial_expected_total"));
        Object difference = firstPresent(businessCase.get("trial_difference"), itemDifference);
        Boolean writable = booleanValue(writePlan.get("writable"));
        return new StringBuilder()
                .append("<div class=\"section-title\">&#21464;&#21160;&#27719;&#24635;</div>")
                .append("<div class=\"approval-total-grid\">")
                .append(approvalCell("&#21464;&#21160;&#21069;&#21512;&#35745;", baselineTotal))
                .append(approvalCell("&#21464;&#21160;&#21518;&#21512;&#35745;", calculatedTotal))
                .append(approvalCell("&#21382;&#21490;&#26399;&#26395;&#21512;&#35745;", expectedTotal))
                .append(approvalCell("&#24046;&#39069;", difference))
                .append(approvalCell("&#24037;&#36164;&#39033;&#25968;", salaryItems.size()))
                .append(approvalCell("&#24037;&#36164;&#39033;&#24046;&#39069;&#21512;&#35745;", itemDifference))
                .append(approvalCell("&#20889;&#20837;&#35745;&#21010;", writePlan.get("planNo")))
                .append(approvalCell("&#26159;&#21542;&#21487;&#20889;", writable == null ? "" : writable ? "\u662f" : "\u5426"))
                .append("</div>")
                .toString();
    }

    private String salaryCaseApprovalWorkflowHtml(
            Map<String, Object> businessCase,
            Map<String, Object> snapshot,
            Map<String, Object> writePlan
    ) {
        return new StringBuilder()
                .append("<div class=\"section-title\">&#21150;&#29702;&#36712;&#36857;</div>")
                .append("<div class=\"approval-workflow\">")
                .append(approvalStatusCell("&#19994;&#21153;&#29366;&#24577;", "case", businessCase.get("status")))
                .append(approvalStatusCell("&#35797;&#31639;&#29366;&#24577;", "trial", businessCase.get("trial_status")))
                .append(approvalStatusCell("&#22797;&#26680;&#29366;&#24577;", "review", businessCase.get("review_status")))
                .append(approvalStatusCell("&#24046;&#24322;&#22797;&#26680;", "review", writePlan.get("comparisonReviewStatus")))
                .append(approvalCell("&#24555;&#29031;&#26102;&#38388;", snapshot.get("snapshot_at")))
                .append(approvalStatusCell("&#39044;&#26816;&#29366;&#24577;", "plan", writePlan.get("previewStatus")))
                .append(approvalStatusCell("&#35745;&#21010;&#29366;&#24577;", "plan", writePlan.get("planStatus")))
                .append(approvalStatusCell("&#25191;&#34892;&#32467;&#26524;", "execution", writePlan.get("executionResult")))
                .append(approvalCell("&#21407;&#21382;&#21490;ID", writePlan.get("existingHistoryId")))
                .append(approvalCell("&#20889;&#20837;&#21382;&#21490;ID", writePlan.get("insertedHistoryId")))
                .append(approvalCell("&#21069;&#24207;&#21382;&#21490;ID", writePlan.get("previousHistoryId")))
                .append(approvalCell("&#21518;&#32493;&#21382;&#21490;ID", writePlan.get("nextHistoryId")))
                .append("</div>")
                .toString();
    }

    private String salaryCaseApprovalSignatureHtml() {
        return """
                <div class="approval-signature">
                    <div class="approval-signature-row"><b>&#32463;&#21150;&#24847;&#35265;</b><span></span><b>&#32463;&#21150;&#20154;</b><span></span></div>
                    <div class="approval-signature-row"><b>&#22797;&#26680;&#24847;&#35265;</b><span></span><b>&#22797;&#26680;&#20154;</b><span></span></div>
                    <div class="approval-signature-row"><b>&#23457;&#25209;&#24847;&#35265;</b><span></span><b>&#23457;&#25209;&#20154;</b><span></span></div>
                    <div class="approval-signature-row"><b>&#22791;&#27880;</b><span></span><b>&#26085;&#26399;</b><span></span></div>
                </div>
                """;
    }

    private String approvalCell(String labelHtml, Object value) {
        return "<span><b>" + labelHtml + "&#65306;</b>" + escapeHtml(amountOrText(value)) + "</span>";
    }

    private String approvalStatusCell(String labelHtml, String category, Object value) {
        return approvalCell(labelHtml, reportStatusText(category, value));
    }

    private String reportStatusText(String category, Object value) {
        String safe = text(value);
        if (safe.isBlank()) {
            return "";
        }
        String key = safe.toUpperCase(Locale.ROOT);
        if ("trial".equals(category)) {
            return switch (key) {
                case "MATCH", "MATCHED", "PASS", "PASSED" -> "\u5339\u914d";
                case "DIFFERENT", "DIFF" -> "\u6709\u5dee\u5f02";
                case "ERROR", "FAILED" -> "\u8bd5\u7b97\u5f02\u5e38";
                case "SKIPPED" -> "\u672a\u8bd5\u7b97";
                case "PENDING" -> "\u5f85\u8bd5\u7b97";
                default -> safe;
            };
        }
        if ("review".equals(category)) {
            return switch (key) {
                case "PENDING" -> "\u5f85\u590d\u6838";
                case "REVIEWED" -> "\u5df2\u590d\u6838";
                case "IGNORED" -> "\u5df2\u5ffd\u7565";
                case "NOT_REQUIRED" -> "\u65e0\u9700\u590d\u6838";
                case "APPROVED" -> "\u5df2\u5ba1\u6279";
                case "REJECTED" -> "\u5df2\u9000\u56de";
                default -> safe;
            };
        }
        if ("plan".equals(category)) {
            return switch (key) {
                case "READY" -> "\u53ef\u5199\u5165";
                case "BLOCKED" -> "\u963b\u65ad";
                case "WARNING", "WARN" -> "\u9884\u8b66";
                case "PREPARED", "PENDING" -> "\u5f85\u5199\u5165";
                case "EXECUTED", "HISTORY_WRITTEN" -> "\u5df2\u5199\u5165";
                case "ROLLED_BACK", "HISTORY_ROLLED_BACK" -> "\u5df2\u64a4\u9500";
                case "PASS", "PASSED" -> "\u901a\u8fc7";
                case "ERROR", "FAILED" -> "\u5f02\u5e38";
                default -> safe;
            };
        }
        if ("execution".equals(category)) {
            return switch (key) {
                case "SUCCESS" -> "\u6210\u529f";
                case "FAILED", "ERROR" -> "\u5931\u8d25";
                case "ROLLED_BACK" -> "\u5df2\u64a4\u9500";
                case "SKIPPED" -> "\u5df2\u8df3\u8fc7";
                default -> safe;
            };
        }
        return switch (key) {
            case "TODO", "PENDING" -> "\u5f85\u529e";
            case "DONE", "CASE_DONE" -> "\u5df2\u529e";
            case "REVIEW_PENDING" -> "\u5f85\u590d\u6838";
            case "HISTORY_READY" -> "\u53ef\u5199\u5165\u5386\u53f2";
            case "HISTORY_PREPARED" -> "\u5f85\u5199\u5165";
            case "HISTORY_WRITTEN", "HISTORY_EXECUTED" -> "\u5df2\u5199\u5165";
            case "HISTORY_BLOCKED" -> "\u5199\u5165\u963b\u65ad";
            case "HISTORY_ROLLED_BACK" -> "\u5199\u5165\u5df2\u64a4\u9500";
            case "CASE_CANCELLED", "CANCELLED" -> "\u5df2\u64a4\u56de";
            default -> safe;
        };
    }

    private String personRosterHtmlV2(String orgCode, int year, int month, String keyword, List<Map<String, Object>> rows) {
        BigDecimal totalSalary = rows.stream()
                .map(row -> amount(row.get("totalSalary")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Long> postCounts = rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        row -> text(firstPresent(row.get("postLevel"), row.get("currentPost"), row.get("salaryPostCode"))).isBlank()
                                ? "-" : text(firstPresent(row.get("postLevel"), row.get("currentPost"), row.get("salaryPostCode"))),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()
                ));
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>&#20154;&#21592;&#24037;&#36164;&#33457;&#21517;&#20876;</title>
                    <style>
                        * { box-sizing: border-box; }
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 18px; background: #fff; }
                        .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 12px; }
                        button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        .print-sheet { max-width: 1120px; margin: 0 auto; }
                        h1 { text-align: center; font-size: 22px; margin: 4px 0 6px; letter-spacing: 0; }
                        .print-subtitle { display: flex; justify-content: space-between; font-size: 12px; color: #4b5563; margin-bottom: 10px; }
                        .meta { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px 18px; font-size: 13px; margin-bottom: 10px; }
                        .summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); border: 1px solid #4b5563; border-bottom: 0; margin-bottom: 12px; }
                        .summary-grid span { border-right: 1px solid #4b5563; border-bottom: 1px solid #4b5563; padding: 7px 8px; font-size: 13px; min-height: 30px; }
                        .summary-grid span:nth-child(4n) { border-right: 0; }
                        table { width: 100%; border-collapse: collapse; font-size: 10.5px; }
                        th, td { border: 1px solid #4b5563; padding: 4px 5px; vertical-align: middle; }
                        th { background: #eef2f7; font-weight: 700; }
                        td.amount, th.amount { text-align: right; }
                        @page { size: A4 landscape; margin: 10mm; }
                        @media print { .toolbar { display: none; } body { margin: 0; } .print-sheet { max-width: none; } thead { display: table-header-group; } tr, .summary-grid { break-inside: avoid; } }
                    </style>
                </head>
                <body>
                    <div class="toolbar"><button onclick="window.print()">&#25171;&#21360;</button></div>
                    <main class="print-sheet">
                """);
        html.append("<h1>&#20154;&#21592;&#24037;&#36164;&#33457;&#21517;&#20876;</h1>");
        html.append("<div class=\"print-subtitle\"><span>&#20154;&#21592;&#22522;&#26412;&#20449;&#24687;&#19982;&#25191;&#34892;&#24037;&#36164;</span><span>&#25171;&#21360;&#26102;&#38388;&#65306;")
                .append(escapeHtml(LocalDateTime.now().withNano(0).toString()))
                .append("</span></div>");
        html.append("<div class=\"meta\"><span>&#21333;&#20301;&#65306;").append(escapeHtml(orgCode)).append("</span>")
                .append("<span>&#24037;&#36164;&#26399;&#38388;&#65306;").append(year).append(".").append(String.format("%02d", month)).append("</span>")
                .append("<span>&#20851;&#38190;&#23383;&#65306;").append(escapeHtml(keyword.isBlank() ? "全部" : keyword)).append("</span>")
                .append("<span>&#29983;&#25104;&#26102;&#38388;&#65306;").append(escapeHtml(LocalDateTime.now().withNano(0).toString())).append("</span></div>");
        html.append("<div class=\"summary-grid\">")
                .append(approvalCell("&#20154;&#25968;", rows.size()))
                .append(approvalCell("&#24037;&#36164;&#21512;&#35745;&#27719;&#24635;", totalSalary))
                .append(approvalCell("&#32844;&#21153;/&#23618;&#27425;&#31867;&#22411;", postCounts.size()))
                .append(approvalCell("&#26597;&#35810;&#33539;&#22260;", keyword.isBlank() ? "全部" : keyword));
        for (Map.Entry<String, Long> entry : postCounts.entrySet()) {
            html.append(approvalCell(entry.getKey(), entry.getValue()));
        }
        html.append("</div>");
        html.append("""
                <table>
                    <thead>
                    <tr>
                        <th>&#24207;&#21495;</th><th>&#20154;&#21592;&#32534;&#30721;</th><th>&#22995;&#21517;</th><th>&#24615;&#21035;</th><th>&#36523;&#20221;&#35777;&#21495;</th>
                        <th>&#20986;&#29983;&#24180;&#26376;</th><th>&#21442;&#21152;&#24037;&#20316;</th><th>&#23398;&#21382;</th><th>&#32844;&#21153;&#23618;&#27425;</th><th>&#29616;&#20219;&#32844;&#21153;</th>
                        <th>&#20219;&#32844;&#26102;&#38388;</th><th>&#24037;&#36164;&#25191;&#34892;&#24180;&#26376;</th><th>&#26368;&#36817;&#21464;&#21160;</th><th>&#24037;&#36164;&#32844;&#21153;</th><th class="amount">&#24037;&#36164;&#21512;&#35745;</th>
                    </tr>
                    </thead>
                    <tbody>
                """);
        if (rows.isEmpty()) {
            html.append("<tr><td colspan=\"15\" style=\"text-align:center;\">&#26080;&#25968;&#25454;</td></tr>");
        }
        int index = 1;
        for (Map<String, Object> row : rows) {
            String education = text(row.get("educationName"));
            if (education.isBlank()) {
                education = text(row.get("educationCode"));
            }
            html.append("<tr>")
                    .append(td(index++))
                    .append(td(row.get("personCode")))
                    .append(td(row.get("personName")))
                    .append(td(row.get("gender")))
                    .append(td(row.get("idCard")))
                    .append(td(row.get("birthDate")))
                    .append(td(row.get("workStart")))
                    .append(td(education))
                    .append(td(row.get("postLevel")))
                    .append(td(row.get("currentPost")))
                    .append(td(row.get("postStart")))
                    .append(td(text(row.get("salaryYear")) + "." + text(row.get("salaryMonth"))))
                    .append(td(row.get("changeType")))
                    .append(td(row.get("salaryPostCode")))
                    .append(amountTd(row.get("totalSalary")))
                    .append("</tr>");
        }
        html.append("</tbody></table></main></body></html>");
        return html.toString();
    }

    private String personRosterHtml(String orgCode, int year, int month, String keyword, List<Map<String, Object>> rows) {
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>人员工资花名册</title>
                    <style>
                        * { box-sizing: border-box; }
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 18px; background: #fff; }
                        .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 12px; }
                        button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        .print-sheet { max-width: 1120px; margin: 0 auto; }
                        h1 { text-align: center; font-size: 22px; margin: 4px 0 6px; letter-spacing: 0; }
                        body > h1:first-of-type { display: none; }
                        .print-subtitle { display: flex; justify-content: space-between; font-size: 12px; color: #4b5563; margin-bottom: 10px; }
                        .meta { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 10px; }
                        table { width: 100%; border-collapse: collapse; font-size: 11px; }
                        th, td { border: 1px solid #4b5563; padding: 4px 5px; vertical-align: middle; }
                        th { background: #eef2f7; font-weight: 700; }
                        td.amount, th.amount { text-align: right; }
                        @page { size: A4 landscape; margin: 10mm; }
                        @media print { .toolbar { display: none; } body { margin: 0; } .print-sheet { max-width: none; } thead { display: table-header-group; } tr, .summary-grid, .sign { break-inside: avoid; } }
                    </style>
                </head>
                <body>
                    <div class="toolbar"><button onclick="window.print()">打印</button></div>
                """);
        html.append("<h1>人员工资花名册</h1>");
        html.append("<div class=\"meta\"><span>单位：").append(escapeHtml(orgCode)).append("</span>")
                .append("<span>工资期间：").append(year).append(".").append(String.format("%02d", month)).append("</span>")
                .append("<span>关键字：").append(escapeHtml(keyword.isBlank() ? "全部" : keyword)).append("</span>")
                .append("<span>人数：").append(rows.size()).append("</span>")
                .append("<span>生成时间：").append(escapeHtml(LocalDateTime.now().withNano(0).toString())).append("</span></div>");
        html.append("""
                <table>
                    <thead>
                    <tr>
                        <th>序号</th><th>人员编码</th><th>姓名</th><th>性别</th><th>身份证号</th>
                        <th>出生年月</th><th>参加工作</th><th>学历</th><th>职务层次</th><th>现任职务</th>
                        <th>任职时间</th><th>工资执行年月</th><th>最近变动</th><th>工资职务</th><th class="amount">工资合计</th>
                    </tr>
                    </thead>
                    <tbody>
                """);
        if (rows.isEmpty()) {
            html.append("<tr><td colspan=\"15\" style=\"text-align:center;\">无数据</td></tr>");
        }
        int index = 1;
        for (Map<String, Object> row : rows) {
            String education = text(row.get("educationName"));
            if (education.isBlank()) {
                education = text(row.get("educationCode"));
            }
            html.append("<tr>")
                    .append(td(index++))
                    .append(td(row.get("personCode")))
                    .append(td(row.get("personName")))
                    .append(td(row.get("gender")))
                    .append(td(row.get("idCard")))
                    .append(td(row.get("birthDate")))
                    .append(td(row.get("workStart")))
                    .append(td(education))
                    .append(td(row.get("postLevel")))
                    .append(td(row.get("currentPost")))
                    .append(td(row.get("postStart")))
                    .append(td(text(row.get("salaryYear")) + "." + text(row.get("salaryMonth"))))
                    .append(td(row.get("changeType")))
                    .append(td(row.get("salaryPostCode")))
                    .append(amountTd(row.get("totalSalary")))
                    .append("</tr>");
        }
        html.append("</tbody></table></body></html>");
        return html.toString();
    }

    private String assessmentSummaryHtmlV2(String orgCode, int year, String keyword, List<Map<String, Object>> rows) {
        Map<String, Long> grouped = rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        row -> text(row.get("resultGroup")).isBlank() ? "-" : text(row.get("resultGroup")),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()
                ));
        Map<String, Long> resultCounts = rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        row -> text(row.get("assessmentResult")).isBlank() ? "空值" : text(row.get("assessmentResult")),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()
                ));
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>&#24180;&#24230;&#32771;&#26680;&#32479;&#35745;&#34920;</title>
                    <style>
                        * { box-sizing: border-box; }
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 18px; background: #fff; }
                        .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 12px; }
                        button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        .print-sheet { max-width: 900px; margin: 0 auto; }
                        h1 { text-align: center; font-size: 22px; margin: 4px 0 6px; letter-spacing: 0; }
                        .print-subtitle { display: flex; justify-content: space-between; font-size: 12px; color: #4b5563; margin-bottom: 10px; }
                        .meta { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px 18px; font-size: 13px; margin-bottom: 10px; }
                        .summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); border: 1px solid #4b5563; border-bottom: 0; margin-bottom: 12px; }
                        .summary-grid span { border-right: 1px solid #4b5563; border-bottom: 1px solid #4b5563; padding: 7px 8px; font-size: 13px; min-height: 30px; }
                        .summary-grid span:nth-child(4n) { border-right: 0; }
                        table { width: 100%; border-collapse: collapse; font-size: 12px; }
                        th, td { border: 1px solid #4b5563; padding: 4px 6px; vertical-align: middle; }
                        th { background: #eef2f7; font-weight: 700; }
                        @page { size: A4 portrait; margin: 12mm; }
                        @media print { .toolbar { display: none; } body { margin: 0; } .print-sheet { max-width: none; } thead { display: table-header-group; } tr, .summary-grid { break-inside: avoid; } }
                    </style>
                </head>
                <body>
                    <div class="toolbar"><button onclick="window.print()">&#25171;&#21360;</button></div>
                    <main class="print-sheet">
                """);
        html.append("<h1>&#24180;&#24230;&#32771;&#26680;&#32479;&#35745;&#34920;</h1>");
        html.append("<div class=\"print-subtitle\"><span>&#24037;&#36164;&#27491;&#24120;&#26187;&#21319;&#32771;&#26680;&#20381;&#25454;</span><span>&#25171;&#21360;&#26102;&#38388;&#65306;")
                .append(escapeHtml(LocalDateTime.now().withNano(0).toString()))
                .append("</span></div>");
        html.append("<div class=\"meta\"><span>&#21333;&#20301;&#65306;").append(escapeHtml(orgCode)).append("</span>")
                .append("<span>&#32771;&#26680;&#24180;&#24230;&#65306;").append(year).append("</span>")
                .append("<span>&#20851;&#38190;&#23383;&#65306;").append(escapeHtml(keyword.isBlank() ? "全部" : keyword)).append("</span>")
                .append("<span>&#20154;&#25968;&#65306;").append(rows.size()).append("</span></div>");
        html.append("<div class=\"summary-grid\">")
                .append(approvalCell("&#21512;&#26684;&#21450;&#20197;&#19978;", grouped.getOrDefault("合格及以上", 0L)))
                .append(approvalCell("&#26410;&#23450;&#31561;&#27425;", grouped.getOrDefault("未定等次", 0L)))
                .append(approvalCell("&#20854;&#20182;", grouped.getOrDefault("其他", 0L)))
                .append(approvalCell("&#31354;&#20540;", resultCounts.getOrDefault("空值", 0L)));
        for (Map.Entry<String, Long> entry : resultCounts.entrySet()) {
            html.append(approvalCell(entry.getKey(), entry.getValue()));
        }
        html.append("</div>");
        html.append("""
                <table>
                    <thead>
                    <tr>
                        <th>&#24207;&#21495;</th><th>&#20154;&#21592;&#32534;&#30721;</th><th>&#22995;&#21517;</th><th>&#32771;&#26680;&#24180;&#24230;</th><th>&#32771;&#26680;&#32467;&#26524;</th><th>&#24037;&#36164;&#21475;&#24452;&#20998;&#32452;</th>
                    </tr>
                    </thead>
                    <tbody>
                """);
        if (rows.isEmpty()) {
            html.append("<tr><td colspan=\"6\" style=\"text-align:center;\">&#26080;&#25968;&#25454;</td></tr>");
        }
        int index = 1;
        for (Map<String, Object> row : rows) {
            html.append("<tr>")
                    .append(td(index++))
                    .append(td(row.get("personCode")))
                    .append(td(row.get("personName")))
                    .append(td(row.get("assessmentYear")))
                    .append(td(row.get("assessmentResult")))
                    .append(td(row.get("resultGroup")))
                    .append("</tr>");
        }
        html.append("</tbody></table></main></body></html>");
        return html.toString();
    }

    private String assessmentSummaryHtml(String orgCode, int year, String keyword, List<Map<String, Object>> rows) {
        Map<String, Long> grouped = rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        row -> text(row.get("resultGroup")).isBlank() ? "其他" : text(row.get("resultGroup")),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()
                ));
        Map<String, Long> resultCounts = rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        row -> text(row.get("assessmentResult")).isBlank() ? "空值" : text(row.get("assessmentResult")),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()
                ));
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>年度考核统计表</title>
                    <style>
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 24px; }
                        .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 12px; }
                        button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        h1 { text-align: center; font-size: 22px; margin: 8px 0 12px; }
                        .meta { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 10px; }
                        .summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); border: 1px solid #4b5563; border-bottom: 0; margin-bottom: 12px; }
                        .summary-grid span { border-right: 1px solid #4b5563; border-bottom: 1px solid #4b5563; padding: 7px 8px; font-size: 13px; }
                        .summary-grid span:nth-child(4n) { border-right: 0; }
                        table { width: 100%; border-collapse: collapse; font-size: 12px; }
                        th, td { border: 1px solid #4b5563; padding: 4px 6px; vertical-align: middle; }
                        th { background: #eef2f7; }
                        @page { size: A4 portrait; margin: 12mm; }
                        @media print { .toolbar { display: none; } body { margin: 0; } }
                    </style>
                </head>
                <body>
                    <div class="toolbar"><button onclick="window.print()">打印</button></div>
                """);
        html.append("<h1>年度考核统计表</h1>");
        html.append("<div class=\"meta\"><span>单位：").append(escapeHtml(orgCode)).append("</span>")
                .append("<span>考核年度：").append(year).append("</span>")
                .append("<span>关键字：").append(escapeHtml(keyword.isBlank() ? "全部" : keyword)).append("</span>")
                .append("<span>人数：").append(rows.size()).append("</span>")
                .append("<span>生成时间：").append(escapeHtml(LocalDateTime.now().withNano(0).toString())).append("</span></div>");
        html.append("<div class=\"summary-grid\">")
                .append(meta("合格及以上", grouped.getOrDefault("合格及以上", 0L)))
                .append(meta("未定等次", grouped.getOrDefault("未定等次", 0L)))
                .append(meta("其他", grouped.getOrDefault("其他", 0L)))
                .append(meta("空值", resultCounts.getOrDefault("空值", 0L)));
        for (Map.Entry<String, Long> entry : resultCounts.entrySet()) {
            html.append(meta(entry.getKey(), entry.getValue()));
        }
        html.append("</div>");
        html.append("""
                <table>
                    <thead>
                    <tr>
                        <th>序号</th><th>人员编码</th><th>姓名</th><th>考核年度</th><th>考核结果</th><th>工资口径分组</th>
                    </tr>
                    </thead>
                    <tbody>
                """);
        if (rows.isEmpty()) {
            html.append("<tr><td colspan=\"6\" style=\"text-align:center;\">无数据</td></tr>");
        }
        int index = 1;
        for (Map<String, Object> row : rows) {
            html.append("<tr>")
                    .append(td(index++))
                    .append(td(row.get("personCode")))
                    .append(td(row.get("personName")))
                    .append(td(row.get("assessmentYear")))
                    .append(td(row.get("assessmentResult")))
                    .append(td(row.get("resultGroup")))
                    .append("</tr>");
        }
        html.append("</tbody></table></body></html>");
        return html.toString();
    }

    private String salaryChangeLedgerHtmlV2(String orgCode, int year, int month, String businessType, String keyword, List<Map<String, Object>> rows) {
        Map<String, Long> businessCounts = rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        row -> text(row.get("businessType")).isBlank() ? "-" : text(row.get("businessType")),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()
                ));
        BigDecimal totalDifference = rows.stream()
                .map(row -> amount(row.get("trialDifference")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long matchedCount = rows.stream()
                .filter(row -> Boolean.TRUE.equals(booleanValue(row.get("trialMatched"))))
                .count();
        long reviewPending = rows.stream()
                .filter(row -> {
                    String trialStatus = text(row.get("trialStatus"));
                    String reviewStatus = text(row.get("reviewStatus"));
                    return ("DIFFERENT".equals(trialStatus) || "ERROR".equals(trialStatus))
                            && !"REVIEWED".equals(reviewStatus);
                })
                .count();
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>&#24037;&#36164;&#21464;&#21160;&#31649;&#29702;&#21488;&#36134;</title>
                    <style>
                        * { box-sizing: border-box; }
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 18px; background: #fff; }
                        .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 12px; }
                        button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        .print-sheet { max-width: 1120px; margin: 0 auto; }
                        h1 { text-align: center; font-size: 22px; margin: 4px 0 6px; letter-spacing: 0; }
                        .print-subtitle { display: flex; justify-content: space-between; font-size: 12px; color: #4b5563; margin-bottom: 10px; }
                        .meta { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px 18px; font-size: 13px; margin-bottom: 10px; }
                        .summary-grid { display: grid; grid-template-columns: repeat(5, 1fr); border: 1px solid #4b5563; border-bottom: 0; margin-bottom: 12px; }
                        .summary-grid span { border-right: 1px solid #4b5563; border-bottom: 1px solid #4b5563; padding: 7px 8px; font-size: 13px; min-height: 30px; }
                        .summary-grid span:nth-child(5n) { border-right: 0; }
                        table { width: 100%; border-collapse: collapse; font-size: 10.5px; }
                        th, td { border: 1px solid #4b5563; padding: 4px 5px; vertical-align: middle; }
                        th { background: #eef2f7; font-weight: 700; }
                        td.amount, th.amount { text-align: right; }
                        @page { size: A4 landscape; margin: 10mm; }
                        @media print { .toolbar { display: none; } body { margin: 0; } .print-sheet { max-width: none; } thead { display: table-header-group; } tr, .summary-grid { break-inside: avoid; } }
                    </style>
                </head>
                <body>
                    <div class="toolbar"><button onclick="window.print()">&#25171;&#21360;</button></div>
                    <main class="print-sheet">
                """);
        html.append("<h1>&#24037;&#36164;&#21464;&#21160;&#31649;&#29702;&#21488;&#36134;</h1>");
        html.append("<div class=\"print-subtitle\"><span>&#24037;&#36164;&#19994;&#21153;&#21464;&#21160;&#26680;&#26597;&#21488;&#36134;</span><span>&#25171;&#21360;&#26102;&#38388;&#65306;")
                .append(escapeHtml(LocalDateTime.now().withNano(0).toString()))
                .append("</span></div>");
        html.append("<div class=\"meta\"><span>&#21333;&#20301;&#65306;").append(escapeHtml(orgCode)).append("</span>")
                .append("<span>&#26399;&#38388;&#65306;").append(year).append(".").append(String.format("%02d", month)).append("</span>")
                .append("<span>&#19994;&#21153;&#31867;&#22411;&#65306;").append(escapeHtml(businessType.isBlank() ? "全部" : businessType)).append("</span>")
                .append("<span>&#20851;&#38190;&#23383;&#65306;").append(escapeHtml(keyword.isBlank() ? "全部" : keyword)).append("</span>")
                .append("<span>&#29983;&#25104;&#26102;&#38388;&#65306;").append(escapeHtml(LocalDateTime.now().withNano(0).toString())).append("</span></div>");
        html.append("<div class=\"summary-grid\">")
                .append(approvalCell("&#19994;&#21153;&#31508;&#25968;", rows.size()))
                .append(approvalCell("&#35797;&#31639;&#19968;&#33268;", matchedCount))
                .append(approvalCell("&#24453;&#22797;&#26680;", reviewPending))
                .append(approvalCell("&#24046;&#39069;&#21512;&#35745;", totalDifference))
                .append(approvalCell("&#19994;&#21153;&#31867;&#22411;&#25968;", businessCounts.size()));
        for (Map.Entry<String, Long> entry : businessCounts.entrySet()) {
            html.append(approvalCell(entry.getKey(), entry.getValue()));
        }
        html.append("</div>");
        html.append("""
                <table>
                    <thead>
                    <tr>
                        <th>&#24207;&#21495;</th><th>&#21150;&#29702;&#32534;&#21495;</th><th>&#20154;&#21592;&#32534;&#30721;</th><th>&#22995;&#21517;</th><th>&#19994;&#21153;&#31867;&#22411;</th><th>&#26631;&#39064;</th>
                        <th>&#21150;&#29702;&#29366;&#24577;</th><th>&#35797;&#31639;&#29366;&#24577;</th><th class="amount">&#35797;&#31639;&#21069;&#21512;&#35745;</th><th class="amount">&#35797;&#31639;&#21518;&#21512;&#35745;</th>
                        <th class="amount">&#24046;&#39069;</th><th>&#22797;&#26680;&#29366;&#24577;</th><th>&#21150;&#29702;&#20154;</th><th>&#21150;&#29702;&#26102;&#38388;</th>
                    </tr>
                    </thead>
                    <tbody>
                """);
        if (rows.isEmpty()) {
            html.append("<tr><td colspan=\"14\" style=\"text-align:center;\">&#26080;&#25968;&#25454;</td></tr>");
        }
        int index = 1;
        for (Map<String, Object> row : rows) {
            html.append("<tr>")
                    .append(td(index++))
                    .append(td(row.get("caseNo")))
                    .append(td(row.get("personCode")))
                    .append(td(row.get("personName")))
                    .append(td(row.get("businessType")))
                    .append(td(row.get("title")))
                    .append(td(reportStatusText("case", row.get("status"))))
                    .append(td(reportStatusText("trial", row.get("trialStatus"))))
                    .append(amountTd(row.get("trialBaselineTotal")))
                    .append(amountTd(row.get("trialCalculatedTotal")))
                    .append(amountTd(row.get("trialDifference")))
                    .append(td(reportStatusText("review", row.get("reviewStatus"))))
                    .append(td(row.get("handledBy")))
                    .append(td(row.get("handledAt")))
                    .append("</tr>");
        }
        html.append("</tbody></table></main></body></html>");
        return html.toString();
    }

    private String salaryChangeLedgerHtml(String orgCode, int year, int month, String businessType, String keyword, List<Map<String, Object>> rows) {
        Map<String, Long> businessCounts = rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        row -> text(row.get("businessType")).isBlank() ? "未分类" : text(row.get("businessType")),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()
                ));
        BigDecimal totalDifference = rows.stream()
                .map(row -> amount(row.get("trialDifference")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long matchedCount = rows.stream()
                .filter(row -> Boolean.TRUE.equals(booleanValue(row.get("trialMatched"))))
                .count();
        long reviewPending = rows.stream()
                .filter(row -> {
                    String trialStatus = text(row.get("trialStatus"));
                    String reviewStatus = text(row.get("reviewStatus"));
                    return ("DIFFERENT".equals(trialStatus) || "ERROR".equals(trialStatus))
                            && !"REVIEWED".equals(reviewStatus);
                })
                .count();
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>工资变动管理台账</title>
                    <style>
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 24px; }
                        .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 12px; }
                        button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        h1 { text-align: center; font-size: 22px; margin: 8px 0 12px; }
                        .meta { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 10px; }
                        .summary-grid { display: grid; grid-template-columns: repeat(5, 1fr); border: 1px solid #4b5563; border-bottom: 0; margin-bottom: 12px; }
                        .summary-grid span { border-right: 1px solid #4b5563; border-bottom: 1px solid #4b5563; padding: 7px 8px; font-size: 13px; }
                        .summary-grid span:nth-child(5n) { border-right: 0; }
                        table { width: 100%; border-collapse: collapse; font-size: 11px; }
                        th, td { border: 1px solid #4b5563; padding: 4px 5px; vertical-align: middle; }
                        th { background: #eef2f7; }
                        td.amount, th.amount { text-align: right; }
                        @page { size: A4 landscape; margin: 10mm; }
                        @media print { .toolbar { display: none; } body { margin: 0; } }
                    </style>
                </head>
                <body>
                    <div class="toolbar"><button onclick="window.print()">打印</button></div>
                """);
        html.append("<h1>工资变动管理台账</h1>");
        html.append("<div class=\"meta\"><span>单位：").append(escapeHtml(orgCode)).append("</span>")
                .append("<span>期间：").append(year).append(".").append(String.format("%02d", month)).append("</span>")
                .append("<span>业务类型：").append(escapeHtml(businessType.isBlank() ? "全部" : businessType)).append("</span>")
                .append("<span>关键字：").append(escapeHtml(keyword.isBlank() ? "全部" : keyword)).append("</span>")
                .append("<span>生成时间：").append(escapeHtml(LocalDateTime.now().withNano(0).toString())).append("</span></div>");
        html.append("<div class=\"summary-grid\">")
                .append(meta("业务笔数", rows.size()))
                .append(meta("试算一致", matchedCount))
                .append(meta("待复核", reviewPending))
                .append(meta("差额合计", totalDifference))
                .append(meta("业务类型数", businessCounts.size()));
        for (Map.Entry<String, Long> entry : businessCounts.entrySet()) {
            html.append(meta(entry.getKey(), entry.getValue()));
        }
        html.append("</div>");
        html.append("""
                <table>
                    <thead>
                    <tr>
                        <th>序号</th><th>办理编号</th><th>人员编码</th><th>姓名</th><th>业务类型</th><th>标题</th>
                        <th>办理状态</th><th>试算状态</th><th class="amount">试算前合计</th><th class="amount">试算后合计</th>
                        <th class="amount">差额</th><th>复核状态</th><th>办理人</th><th>办理时间</th>
                    </tr>
                    </thead>
                    <tbody>
                """);
        if (rows.isEmpty()) {
            html.append("<tr><td colspan=\"14\" style=\"text-align:center;\">无数据</td></tr>");
        }
        int index = 1;
        for (Map<String, Object> row : rows) {
            html.append("<tr>")
                    .append(td(index++))
                    .append(td(row.get("caseNo")))
                    .append(td(row.get("personCode")))
                    .append(td(row.get("personName")))
                    .append(td(row.get("businessType")))
                    .append(td(row.get("title")))
                    .append(td(reportStatusText("case", row.get("status"))))
                    .append(td(reportStatusText("trial", row.get("trialStatus"))))
                    .append(amountTd(row.get("trialBaselineTotal")))
                    .append(amountTd(row.get("trialCalculatedTotal")))
                    .append(amountTd(row.get("trialDifference")))
                    .append(td(reportStatusText("review", row.get("reviewStatus"))))
                    .append(td(row.get("handledBy")))
                    .append(td(row.get("handledAt")))
                    .append("</tr>");
        }
        html.append("</tbody></table></body></html>");
        return html.toString();
    }

    private SalaryCasePrintData salaryCasePrintData(String caseNo) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT *
                FROM salary_business_case
                WHERE case_no = ?
                LIMIT 1
                """, caseNo);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Salary business case not found: " + caseNo);
        }
        Map<String, Object> businessCase = rows.getFirst();
        organizationAccessService.requireOrgAccess(text(businessCase.get("org_code")));
        List<Map<String, Object>> snapshots = jdbcTemplate.queryForList("""
                SELECT *
                FROM salary_business_case_snapshot
                WHERE case_no = ?
                   OR work_item_id = ?
                ORDER BY id DESC
                LIMIT 1
                """, caseNo, text(businessCase.get("work_item_id")));
        Map<String, Object> snapshot = snapshots.isEmpty() ? Map.of() : snapshots.getFirst();
        List<Map<String, Object>> salaryItems = readJsonList(snapshot.get("salary_items_json"));
        if (salaryItems.isEmpty()) {
            salaryItems = readJsonList(businessCase.get("trial_changes_json"));
        }
        return new SalaryCasePrintData(
                businessCase,
                snapshot,
                salaryItems,
                salaryCasePerson(businessCase),
                salaryCaseWritePlan(caseNo)
        );
    }

    private List<Map<String, Object>> reportPrintArchiveRows(
            String orgCode,
            int year,
            int month,
            String businessType,
            String keyword,
            String printStatus,
            int limit
    ) {
        systemAuditService.ensureTable();
        ensureReportPrintBatchTables();
        String safeOrgCode = text(orgCode);
        if (!safeOrgCode.isBlank()) {
            organizationAccessService.requireOrgAccess(safeOrgCode);
        }
        int safeLimit = Math.max(1, Math.min(limit, 10000));
        List<Object> args = new ArrayList<>();
        String orgWhere = "AND " + organizationAccessService.orgCodeAccessSql("base.orgCode");
        if (!safeOrgCode.isBlank()) {
            orgWhere += " AND base.orgCode = ?";
            args.add(safeOrgCode);
        }
        String yearWhere = "";
        if (year > 0) {
            yearWhere = "AND base.eventYear = ?";
            args.add(Math.max(1900, Math.min(year, 2099)));
        }
        String monthWhere = "";
        if (month > 0) {
            monthWhere = "AND base.eventMonth = ?";
            args.add(Math.max(1, Math.min(month, 12)));
        }
        String businessTypeWhere = "";
        String safeBusinessType = text(businessType);
        if (!safeBusinessType.isBlank()) {
            businessTypeWhere = "AND base.businessType = ?";
            args.add(safeBusinessType);
        }
        String keywordWhere = "";
        String safeKeyword = text(keyword);
        if (!safeKeyword.isBlank()) {
            keywordWhere = """
                    AND (
                        base.caseNo LIKE CONCAT('%', ?, '%')
                        OR base.personCode LIKE CONCAT('%', ?, '%')
                        OR base.personName LIKE CONCAT('%', ?, '%')
                        OR base.orgCode LIKE CONCAT('%', ?, '%')
                        OR COALESCE(base.latestBatchNo, '') LIKE CONCAT('%', ?, '%')
                    )
                    """;
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
        }
        String statusWhere = switch (normalizePrintArchiveStatus(printStatus)) {
            case "PRINTED" -> "AND base.printed = 1";
            case "UNPRINTED" -> "AND base.printed = 0";
            case "REPRINTED" -> "AND base.reprinted = 1";
            case "WRITE_READY" -> "AND base.writeReady = 1";
            case "WRITE_BLOCKED" -> "AND base.writeReady = 0";
            default -> "";
        };
        args.add(safeLimit);
        return jdbcTemplate.queryForList("""
                WITH print_events AS (
                    SELECT target_code AS case_no,
                           action_name AS action_name,
                           target_type AS target_type,
                           target_code AS target_code,
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
                           'REPORT_PRINT_BATCH' AS target_type,
                           item.batch_no AS target_code,
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
                ),
                latest_print AS (
                    SELECT ranked.*
                    FROM (
                        SELECT event.*,
                               ROW_NUMBER() OVER (PARTITION BY event.case_no ORDER BY event.printed_at DESC, event.target_code DESC) AS rn
                        FROM print_events event
                    ) ranked
                    WHERE ranked.rn = 1
                ),
                acceptance_export AS (
                    SELECT audit.target_code AS batchNo,
                           CONCAT('SYS-', audit.id) AS acceptanceAuditId,
                           audit.operator AS acceptanceOperator,
                           audit.created_at AS acceptanceExportedAt
                    FROM sys_audit_log audit
                    JOIN (
                        SELECT target_code, MAX(id) AS latest_id
                        FROM sys_audit_log
                        WHERE module_name = 'report'
                          AND action_name = 'report-print-batch-acceptance-package'
                          AND target_type = 'REPORT_PRINT_BATCH'
                        GROUP BY target_code
                    ) latest ON latest.latest_id = audit.id
                ),
                base AS (
                    SELECT sc.case_no AS caseNo,
                           sc.person_code AS personCode,
                           sc.person_name AS personName,
                           sc.org_code AS orgCode,
                           sc.event_year AS eventYear,
                           sc.event_month AS eventMonth,
                           sc.business_type AS businessType,
                           sc.status AS caseStatus,
                           COALESCE(plan.plan_status, '') AS planStatus,
                           COALESCE(plan.preview_status, '') AS previewStatus,
                           COALESCE(plan.execution_result, '') AS executionResult,
                           COALESCE(plan.writable, 0) AS writable,
                           COALESCE(agg.print_count, 0) AS printCount,
                           CASE WHEN COALESCE(agg.print_count, 0) > 0 THEN 1 ELSE 0 END AS printed,
                           COALESCE(agg.reprinted, 0) AS reprinted,
                           COALESCE(latest.action_name, '') AS latestAction,
                           COALESCE(latest.target_type, '') AS latestTargetType,
                           COALESCE(latest.target_code, '') AS latestTargetCode,
                           COALESCE(latest.batch_no, '') AS latestBatchNo,
                           COALESCE(latest.operator, '') AS latestOperator,
                           latest.printed_at AS latestPrintedAt,
                           CASE
                               WHEN sc.status = 'CANCELLED' THEN 'CANCELLED'
                               WHEN COALESCE(agg.print_count, 0) > 0 AND COALESCE(agg.reprinted, 0) > 0 THEN 'REPRINTED'
                               WHEN COALESCE(agg.print_count, 0) > 0 THEN 'PRINTED'
                               ELSE 'UNPRINTED'
                           END AS archiveStatus,
                           CASE
                               WHEN sc.status = 'DONE'
                                AND COALESCE(plan.plan_status, '') = 'PREPARED'
                                AND COALESCE(plan.writable, 0) = 1
                                AND COALESCE(agg.print_count, 0) > 0 THEN 1
                               ELSE 0
                           END AS writeReady
                    FROM salary_business_case sc
                    LEFT JOIN salary_history_write_plan plan ON plan.case_no = sc.case_no
                    LEFT JOIN print_agg agg ON agg.case_no = sc.case_no
                    LEFT JOIN latest_print latest ON latest.case_no = sc.case_no
                    WHERE sc.status IN ('DONE', 'CANCELLED')
                )
                SELECT base.*,
                       CASE WHEN acceptance.batchNo IS NULL THEN 0 ELSE 1 END AS acceptanceExported,
                       COALESCE(acceptance.acceptanceAuditId, '') AS acceptanceAuditId,
                       COALESCE(acceptance.acceptanceOperator, '') AS acceptanceOperator,
                       acceptance.acceptanceExportedAt AS acceptanceExportedAt
                FROM base
                LEFT JOIN acceptance_export acceptance ON acceptance.batchNo = base.latestBatchNo
                WHERE 1 = 1
                __ORG_WHERE__
                __YEAR_WHERE__
                __MONTH_WHERE__
                __BUSINESS_TYPE_WHERE__
                __KEYWORD_WHERE__
                __STATUS_WHERE__
                ORDER BY eventYear DESC, eventMonth DESC, orgCode, businessType, personCode, caseNo
                LIMIT ?
                """.replace("__ORG_WHERE__", orgWhere)
                .replace("__YEAR_WHERE__", yearWhere)
                .replace("__MONTH_WHERE__", monthWhere)
                .replace("__BUSINESS_TYPE_WHERE__", businessTypeWhere)
                .replace("__KEYWORD_WHERE__", keywordWhere)
                .replace("__STATUS_WHERE__", statusWhere), args.toArray());
    }

    private String normalizePrintArchiveStatus(String value) {
        String status = text(value).toUpperCase(Locale.ROOT);
        return switch (status) {
            case "PRINTED", "UNPRINTED", "REPRINTED", "WRITE_READY", "WRITE_BLOCKED" -> status;
            default -> "ALL";
        };
    }

    private String normalizeReportPrintBatchAcceptanceStatus(String value) {
        String status = text(value).toUpperCase(Locale.ROOT);
        return switch (status) {
            case "EXPORTED", "PENDING" -> status;
            default -> "ALL";
        };
    }

    private ReportPrintBatchData reportPrintBatchData(String batchNo) {
        ensureReportPrintBatchTables();
        String safeBatchNo = text(batchNo);
        if (safeBatchNo.isBlank()) {
            throw new IllegalArgumentException("Report print batch number is required.");
        }
        List<Map<String, Object>> batches = jdbcTemplate.queryForList("""
                SELECT batch.batch_no AS batchNo,
                       batch.report_type AS reportType,
                       batch.org_code AS orgCode,
                       batch.event_year AS eventYear,
                       batch.event_month AS eventMonth,
                       batch.business_type AS businessType,
                       batch.keyword,
                       batch.limit_count AS limitCount,
                       batch.printed_count AS printedCount,
                       batch.blocked_count AS blockedCount,
                       batch.warning_count AS warningCount,
                       batch.printed_by AS printedBy,
                       batch.printed_at AS printedAt,
                       batch.summary,
                       CASE WHEN acceptance.id IS NULL THEN 0 ELSE 1 END AS acceptanceExported,
                       CASE WHEN acceptance.id IS NULL THEN '' ELSE CONCAT('SYS-', acceptance.id) END AS acceptanceAuditId,
                       COALESCE(acceptance.operator, '') AS acceptanceOperator,
                       acceptance.created_at AS acceptanceExportedAt
                FROM salary_report_print_batch batch
                LEFT JOIN (
                    SELECT audit.*
                    FROM sys_audit_log audit
                    JOIN (
                        SELECT target_code, MAX(id) AS latest_id
                        FROM sys_audit_log
                        WHERE module_name = 'report'
                          AND action_name = 'report-print-batch-acceptance-package'
                          AND target_type = 'REPORT_PRINT_BATCH'
                        GROUP BY target_code
                    ) latest ON latest.latest_id = audit.id
                ) acceptance ON acceptance.target_code = batch.batch_no
                WHERE batch.batch_no = ?
                LIMIT 1
                """, safeBatchNo);
        if (batches.isEmpty()) {
            throw new IllegalArgumentException("Report print batch not found: " + safeBatchNo);
        }
        Map<String, Object> batch = batches.getFirst();
        organizationAccessService.requireOrgAccess(text(batch.get("orgCode")));
        List<Map<String, Object>> items = jdbcTemplate.queryForList("""
                SELECT item.batch_no AS batchNo,
                       item.case_no AS caseNo,
                       item.person_code AS personCode,
                       item.person_name AS personName,
                       item.org_code AS orgCode,
                       item.business_type AS businessType,
                       item.validation_status AS validationStatus,
                       item.issue_count AS issueCount,
                       item.warning_count AS warningCount,
                       item.summary,
                       item.created_at AS createdAt,
                       sc.status AS caseStatus,
                       sc.trial_status AS trialStatus,
                       sc.review_status AS reviewStatus,
                       plan.plan_status AS planStatus,
                       plan.preview_status AS previewStatus,
                       plan.execution_result AS executionResult,
                       plan.inserted_history_id AS insertedHistoryId,
                       CASE
                           WHEN plan.plan_status = 'EXECUTED'
                             AND plan.execution_result = 'SUCCESS'
                             AND TRIM(COALESCE(plan.inserted_history_id, '')) <> ''
                           THEN 1 ELSE 0
                       END AS historyWritten,
                       CASE
                           WHEN plan.plan_status = 'EXECUTED'
                             AND plan.execution_result = 'SUCCESS'
                             AND TRIM(COALESCE(plan.inserted_history_id, '')) <> ''
                           THEN 1 ELSE 0
                       END AS closureReady
                FROM salary_report_print_batch_item item
                LEFT JOIN salary_business_case sc ON sc.case_no = item.case_no
                LEFT JOIN salary_history_write_plan plan ON plan.case_no = item.case_no
                WHERE item.batch_no = ?
                ORDER BY item.id
                """, safeBatchNo);
        return new ReportPrintBatchData(batch, items);
    }

    private List<Map<String, Object>> reportPrintBatchAudits(String batchNo, Map<String, Object> batch) {
        String safeBatchNo = text(batchNo);
        String orgCode = text(batch.get("orgCode"));
        if (safeBatchNo.isBlank()) {
            return List.of();
        }
        return jdbcTemplate.queryForList("""
                SELECT CONCAT('SYS-', id) AS auditId,
                       module_name AS moduleName,
                       action_name AS actionName,
                       target_type AS targetType,
                       target_code AS targetCode,
                       summary,
                       operator,
                       created_at AS createdAt
                FROM sys_audit_log
                WHERE module_name IN ('report', 'workbench')
                  AND (
                      target_code = ?
                      OR summary LIKE CONCAT('%', ?, '%')
                      OR (target_code = ? AND summary LIKE CONCAT('%', ?, '%'))
                  )
                ORDER BY created_at DESC, id DESC
                LIMIT 80
                """, safeBatchNo, safeBatchNo, orgCode, safeBatchNo);
    }

    private String reportPrintBatchAcceptanceReadme(
            ReportPrintBatchData data,
            List<Map<String, Object>> audits,
            List<Map<String, Object>> unwritten,
            List<Map<String, Object>> unclosed,
            List<Map<String, Object>> blocked,
            String exportNo
    ) {
        Map<String, Object> batch = data.batch();
        return """
                打印批次验收包

                批次号：%s
                报表类型：%s
                单位：%s
                年月：%s
                打印人数：%s
                未写入：%s
                未闭环：%s
                阻断：%s
                审计流水：%s
                生成时间：%s

                文件说明：
                - report-print-batch-detail-*.csv：批次明细及写入闭环状态。
                - report-print-batch-audits-*.csv：本批次关联的报表和历史写入审计流水。
                - report-print-batch-unwritten-*.csv：尚未写入历史的数据清单。
                - report-print-batch-unclosed-*.csv：尚未完成写入闭环的数据清单。
                - report-print-batch-blocked-*.csv：打印校验存在阻断的数据清单。
                """.formatted(
                text(batch.get("batchNo")),
                text(batch.get("reportType")),
                text(batch.get("orgCode")),
                periodText(number(batch.get("eventYear")), number(batch.get("eventMonth"))),
                data.items().size(),
                unwritten.size(),
                unclosed.size(),
                blocked.size(),
                audits.size(),
                LocalDateTime.now().withNano(0)
        );
    }

    private String reportPrintBatchAcceptanceMetaCsv(
            String exportNo,
            ReportPrintBatchData data,
            List<Map<String, Object>> unwritten,
            List<Map<String, Object>> unclosed,
            List<Map<String, Object>> blocked
    ) {
        Map<String, Object> batch = data.batch();
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "meta", "exportNo", text(exportNo).isBlank() ? "SINGLE" : text(exportNo));
        csvRow(csv, "meta", "batchNo", batch.get("batchNo"));
        csvRow(csv, "meta", "reportType", batch.get("reportType"));
        csvRow(csv, "meta", "orgCode", batch.get("orgCode"));
        csvRow(csv, "meta", "period", periodText(number(batch.get("eventYear")), number(batch.get("eventMonth"))));
        csvRow(csv, "meta", "rows", data.items().size());
        csvRow(csv, "meta", "unwritten", unwritten.size());
        csvRow(csv, "meta", "unclosed", unclosed.size());
        csvRow(csv, "meta", "blocked", blocked.size());
        csvRow(csv, "meta", "generatedAt", LocalDateTime.now().withNano(0));
        return csv.toString();
    }

    private void appendReportPrintBatchFilterRows(StringBuilder csv, Map<String, Object> batch, int rows) {
        csvRow(csv, "filter", "batchNo", batch.get("batchNo"));
        csvRow(csv, "filter", "reportType", batch.get("reportType"));
        csvRow(csv, "filter", "orgCode", batch.get("orgCode"));
        csvRow(csv, "filter", "period", periodText(number(batch.get("eventYear")), number(batch.get("eventMonth"))));
        csvRow(csv, "filter", "businessType", text(batch.get("businessType")).isBlank() ? "ALL" : batch.get("businessType"));
        csvRow(csv, "filter", "keyword", text(batch.get("keyword")).isBlank() ? "ALL" : batch.get("keyword"));
        csvRow(csv, "filter", "rows", rows);
        csvRow(csv, "filter", "blocked", batch.get("blockedCount"));
        csvRow(csv, "filter", "warnings", batch.get("warningCount"));
        csv.append('\n');
    }

    private String reportPrintBatchDetailCsv(ReportPrintBatchData data) {
        Map<String, Object> batch = data.batch();
        StringBuilder csv = new StringBuilder();
        appendReportPrintBatchFilterRows(csv, batch, data.items().size());
        csvRow(csv, "批次号", batch.get("batchNo"));
        csvRow(csv, "报表类型", batch.get("reportType"));
        csvRow(csv, "单位", batch.get("orgCode"));
        csvRow(csv, "年月", periodText(number(batch.get("eventYear")), number(batch.get("eventMonth"))));
        csvRow(csv, "业务类型", text(batch.get("businessType")).isBlank() ? "全部" : batch.get("businessType"));
        csvRow(csv, "关键字", text(batch.get("keyword")).isBlank() ? "全部" : batch.get("keyword"));
        csvRow(csv, "打印人数", batch.get("printedCount"));
        csvRow(csv, "阻断数", batch.get("blockedCount"));
        csvRow(csv, "提示数", batch.get("warningCount"));
        csvRow(csv, "操作人", batch.get("printedBy"));
        csvRow(csv, "操作时间", batch.get("printedAt"));
        csv.append('\n');
        appendReportPrintBatchItems(csv, data.items());
        return csv.toString();
    }

    private String reportPrintBatchItemsCsv(Map<String, Object> batch, String title, List<Map<String, Object>> items) {
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "filter", "batchNo", batch.get("batchNo"));
        csvRow(csv, "filter", "list", title);
        csvRow(csv, "filter", "rows", items.size());
        csv.append('\n');
        csvRow(csv, "清单", title);
        csvRow(csv, "人数", items.size());
        csv.append('\n');
        appendReportPrintBatchItems(csv, items);
        return csv.toString();
    }

    private void appendReportPrintBatchItems(StringBuilder csv, List<Map<String, Object>> items) {
        csvRow(csv, "办理编号", "人员编码", "姓名", "单位", "业务类型", "校验状态", "缺口数", "提示数",
                "办理状态", "试算状态", "复核状态", "计划状态", "预检状态", "执行结果", "写入历史ID", "已写入", "已闭环",
                "摘要", "创建时间");
        for (Map<String, Object> item : items) {
            csvRow(csv,
                    item.get("caseNo"),
                    item.get("personCode"),
                    item.get("personName"),
                    item.get("orgCode"),
                    item.get("businessType"),
                    item.get("validationStatus"),
                    item.get("issueCount"),
                    item.get("warningCount"),
                    reportStatusText("case", item.get("caseStatus")),
                    reportStatusText("trial", item.get("trialStatus")),
                    reportStatusText("review", item.get("reviewStatus")),
                    reportStatusText("plan", item.get("planStatus")),
                    reportStatusText("plan", item.get("previewStatus")),
                    reportStatusText("execution", item.get("executionResult")),
                    item.get("insertedHistoryId"),
                    reportPrintBatchHistoryWritten(item) ? "是" : "否",
                    reportPrintBatchClosureReady(item) ? "是" : "否",
                    item.get("summary"),
                    item.get("createdAt"));
        }
    }

    private String reportPrintBatchAuditsCsv(Map<String, Object> batch, List<Map<String, Object>> audits) {
        StringBuilder csv = new StringBuilder();
        csvRow(csv, "filter", "batchNo", batch.get("batchNo"));
        csvRow(csv, "filter", "rows", audits.size());
        csv.append('\n');
        csvRow(csv, "审计号", "模块", "动作", "对象类型", "对象编码", "操作人", "时间", "摘要");
        for (Map<String, Object> audit : audits) {
            csvRow(csv,
                    audit.get("auditId"),
                    audit.get("moduleName"),
                    audit.get("actionName"),
                    audit.get("targetType"),
                    audit.get("targetCode"),
                    audit.get("operator"),
                    audit.get("createdAt"),
                    audit.get("summary"));
        }
        return csv.toString();
    }

    private boolean reportPrintBatchHistoryWritten(Map<String, Object> item) {
        return Boolean.TRUE.equals(booleanValue(item.get("historyWritten")));
    }

    private boolean reportPrintBatchClosureReady(Map<String, Object> item) {
        return Boolean.TRUE.equals(booleanValue(item.get("closureReady")));
    }

    private boolean reportPrintBatchBlocked(Map<String, Object> item) {
        return number(item.get("issueCount")) > 0 || "BLOCKED".equalsIgnoreCase(text(item.get("validationStatus")));
    }

    private Map<String, Object> salaryCaseApprovalValidation(String caseNo) {
        SalaryCasePrintData data = salaryCasePrintData(caseNo);
        List<Map<String, Object>> issues = new ArrayList<>();
        List<Map<String, Object>> warnings = new ArrayList<>();
        if (data.snapshot().isEmpty()) {
            issues.add(validationMessage("MISSING_SNAPSHOT", "缺少办理快照，审批表缺少固化依据"));
        }
        if (data.salaryItems().isEmpty()) {
            issues.add(validationMessage("MISSING_SALARY_ITEMS", "缺少工资明细，审批表无法列示本次工资项"));
        }
        if (data.person().isEmpty()) {
            issues.add(validationMessage("MISSING_PERSON", "缺少人员基础信息，审批表人员栏不完整"));
        }
        if ("CANCELLED".equalsIgnoreCase(text(data.businessCase().get("status")))) {
            warnings.add(validationMessage("CASE_CANCELLED", "该业务已撤回，不建议继续打印审批表"));
        }
        if (data.writePlan().isEmpty()) {
            warnings.add(validationMessage("MISSING_WRITE_PLAN", "尚未生成历史写入计划，可打印但无法展示写入状态"));
        } else if ("BLOCKED".equalsIgnoreCase(text(firstPresent(data.writePlan().get("previewStatus"), data.writePlan().get("planStatus"))))) {
            warnings.add(validationMessage("WRITE_PLAN_BLOCKED", "历史写入计划存在阻断，打印前建议核查"));
        }
        String trialStatus = text(data.businessCase().get("trial_status"));
        String reviewStatus = text(data.businessCase().get("review_status"));
        if (("DIFFERENT".equalsIgnoreCase(trialStatus) || "ERROR".equalsIgnoreCase(trialStatus))
                && (reviewStatus.isBlank() || "PENDING".equalsIgnoreCase(reviewStatus))) {
            warnings.add(validationMessage("REVIEW_PENDING", "试算差异或异常尚未复核，打印前建议确认"));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("caseNo", caseNo);
        result.put("printable", issues.isEmpty());
        result.put("status", issues.isEmpty() ? (warnings.isEmpty() ? "READY" : "WARNING") : "BLOCKED");
        result.put("issueCount", issues.size());
        result.put("warningCount", warnings.size());
        result.put("issues", issues);
        result.put("warnings", warnings);
        result.put("snapshotExists", !data.snapshot().isEmpty());
        result.put("salaryItemCount", data.salaryItems().size());
        result.put("personExists", !data.person().isEmpty());
        result.put("writePlanExists", !data.writePlan().isEmpty());
        return result;
    }

    private List<String> normalizeSelectedApprovalCaseNos(List<String> caseNos) {
        List<String> safeCaseNos = caseNos.stream()
                .map(this::text)
                .filter(value -> !value.isBlank())
                .distinct()
                .limit(500)
                .toList();
        if (safeCaseNos.isEmpty()) {
            throw new IllegalArgumentException("Salary case numbers are required.");
        }
        return safeCaseNos;
    }

    private Map<String, Object> validationMessage(String code, String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", code);
        result.put("message", message);
        return result;
    }

    @SuppressWarnings("unchecked")
    private String validationIssueText(Map<String, Object> validation) {
        List<Map<String, Object>> issues = (List<Map<String, Object>>) validation.getOrDefault("issues", List.of());
        return issues.stream()
                .map(issue -> text(issue.get("message")).isBlank() ? text(issue.get("code")) : text(issue.get("message")))
                .filter(message -> !message.isBlank())
                .collect(java.util.stream.Collectors.joining("; "));
    }

    private String batchValidationIssueText(List<Map<String, Object>> blocked) {
        return blocked.stream()
                .limit(5)
                .map(item -> text(item.get("caseNo")) + ":" + validationIssueText(item))
                .collect(java.util.stream.Collectors.joining("; "));
    }

    @SuppressWarnings("unchecked")
    private boolean validationHasMessage(Map<String, Object> validation, String code) {
        List<Map<String, Object>> warnings = (List<Map<String, Object>>) validation.getOrDefault("warnings", List.of());
        List<Map<String, Object>> issues = (List<Map<String, Object>>) validation.getOrDefault("issues", List.of());
        return java.util.stream.Stream.concat(warnings.stream(), issues.stream())
                .anyMatch(item -> code.equals(text(item.get("code"))));
    }

    private String recordReportPrintBatch(
            String reportType,
            String orgCode,
            int year,
            int month,
            String businessType,
            String keyword,
            int limit,
            List<SalaryCasePrintData> cases,
            List<Map<String, Object>> validations
    ) {
        ensureReportPrintBatchTables();
        String batchNo = "RPB-" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        long blockedCount = validations.stream()
                .filter(item -> !Boolean.TRUE.equals(item.get("printable")))
                .count();
        long warningCount = validations.stream()
                .filter(item -> "WARNING".equals(text(item.get("status"))))
                .count();
        String username = text(currentUserService.currentUsername());
        String summary = reportAuditSummary(
                auditPart("period", periodText(year, month)),
                auditPart("businessType", text(businessType).isBlank() ? "ALL" : businessType),
                auditPart("keyword", text(keyword).isBlank() ? "ALL" : keyword),
                auditPart("rows", cases.size()),
                auditPart("warnings", warningCount)
        );
        jdbcTemplate.update("""
                INSERT INTO salary_report_print_batch(batch_no, report_type, org_code, event_year, event_month,
                                                       business_type, keyword, limit_count, printed_count,
                                                       blocked_count, warning_count, printed_by, summary)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                batchNo, reportType, orgCode, year, month, text(businessType), text(keyword),
                limit, cases.size(), blockedCount, warningCount, username, summary);
        Map<String, Map<String, Object>> validationByCaseNo = validations.stream()
                .collect(java.util.stream.Collectors.toMap(
                        item -> text(item.get("caseNo")),
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        for (SalaryCasePrintData item : cases) {
            Map<String, Object> businessCase = item.businessCase();
            Map<String, Object> validation = validationByCaseNo.getOrDefault(text(businessCase.get("case_no")), Map.of());
            jdbcTemplate.update("""
                    INSERT INTO salary_report_print_batch_item(batch_no, case_no, person_code, person_name, org_code,
                                                               business_type, validation_status, issue_count,
                                                               warning_count, summary)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    batchNo,
                    text(businessCase.get("case_no")),
                    text(businessCase.get("person_code")),
                    text(businessCase.get("person_name")),
                    text(businessCase.get("org_code")),
                    text(businessCase.get("business_type")),
                    text(validation.get("status")).isBlank() ? "READY" : text(validation.get("status")),
                    number(validation.get("issueCount")),
                    number(validation.get("warningCount")),
                    reportAuditSummary(
                            auditPart("snapshot", validation.get("snapshotExists")),
                            auditPart("items", validation.get("salaryItemCount")),
                            auditPart("writePlan", validation.get("writePlanExists"))
                    ));
        }
        return batchNo;
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

    private List<String> salaryCaseApprovalCaseNos(String orgCode, int year, int month, String businessType, String keyword, int limit) {
        List<Object> args = new ArrayList<>();
        args.add(orgCode);
        args.add(year);
        args.add(month);
        String businessTypeWhere = "";
        String safeBusinessType = text(businessType);
        if (!safeBusinessType.isBlank()) {
            businessTypeWhere = "AND business_type = ?";
            args.add(safeBusinessType);
        }
        String keywordWhere = "";
        String safeKeyword = text(keyword);
        if (!safeKeyword.isBlank()) {
            keywordWhere = """
                      AND (
                          case_no LIKE CONCAT('%', ?, '%')
                          OR person_code LIKE CONCAT('%', ?, '%')
                          OR person_name LIKE CONCAT('%', ?, '%')
                          OR title LIKE CONCAT('%', ?, '%')
                          OR summary LIKE CONCAT('%', ?, '%')
                      )
                    """;
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
            args.add(safeKeyword);
        }
        args.add(Math.max(1, Math.min(limit, 500)));
        return jdbcTemplate.queryForList("""
                SELECT case_no
                FROM salary_business_case
                WHERE org_code LIKE CONCAT(?, '%')
                  AND event_year = ?
                  AND event_month = ?
                  __BUSINESS_TYPE_WHERE__
                  __KEYWORD_WHERE__
                ORDER BY org_code, business_type, person_code, case_no
                LIMIT ?
                """.replace("__BUSINESS_TYPE_WHERE__", businessTypeWhere)
                .replace("__KEYWORD_WHERE__", keywordWhere), String.class, args.toArray());
    }

    private String salaryCaseApprovalsBatchHtml(
            String orgCode,
            int year,
            int month,
            String businessType,
            String keyword,
            List<SalaryCasePrintData> cases,
            List<Map<String, Object>> validations
    ) {
        long warningCount = validations.stream()
                .filter(item -> "WARNING".equals(text(item.get("status"))))
                .count();
        long writePlanBlockedCount = validations.stream()
                .filter(item -> validationHasMessage(item, "WRITE_PLAN_BLOCKED"))
                .count();
        long reviewPendingCount = validations.stream()
                .filter(item -> validationHasMessage(item, "REVIEW_PENDING"))
                .count();
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>批量工资审批表</title>
                    <style>
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 0; }
                        .batch-toolbar { display: flex; justify-content: flex-end; gap: 8px; padding: 12px 18px; }
                        .batch-toolbar button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        .batch-cover { padding: 24px; }
                        .batch-cover h1 { text-align: center; font-size: 22px; margin: 8px 0 16px; }
                        .batch-cover .meta { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; font-size: 13px; }
                        .approval-page { page-break-before: always; padding: 18px; }
                        .approval-page:first-of-type { page-break-before: auto; }
                        .approval-page .toolbar { display: none; }
                        .approval-page body { margin: 0; }
                        .approval-page h1 { text-align: center; font-size: 22px; margin: 8px 0 12px; }
                        .approval-page > h1:first-of-type { display: none; }
                        .approval-page .approval-real-title { display: block; font-size: 24px; margin: 4px 0 6px; }
                        .approval-page .approval-subtitle { display: flex; justify-content: space-between; font-size: 12px; color: #4b5563; margin-bottom: 10px; }
                        .approval-page .approval-doc { max-width: 780px; margin: 0 auto; }
                        .approval-page .meta, .approval-page .basis, .approval-page .approval-total-grid, .approval-page .approval-workflow { display: grid; grid-template-columns: repeat(4, 1fr); border: 1px solid #4b5563; border-bottom: 0; font-size: 13px; }
                        .approval-page .basis, .approval-page .approval-total-grid, .approval-page .approval-workflow { border-top: 0; }
                        .approval-page .meta span, .approval-page .basis span, .approval-page .approval-total-grid span, .approval-page .approval-workflow span { border-right: 1px solid #4b5563; border-bottom: 1px solid #4b5563; padding: 6px 8px; min-height: 22px; }
                        .approval-page .meta span:nth-child(4n), .approval-page .basis span:nth-child(4n), .approval-page .approval-total-grid span:nth-child(4n), .approval-page .approval-workflow span:nth-child(4n) { border-right: 0; }
                        .approval-page .basis span.wide { grid-column: span 2; }
                        .approval-page .section-title { margin-top: 12px; padding: 6px 8px; border: 1px solid #4b5563; background: #f8fafc; font-weight: 700; font-size: 13px; }
                        .approval-page .summary { margin-top: 10px; font-size: 13px; line-height: 1.7; }
                        .approval-page table { width: 100%; border-collapse: collapse; font-size: 12px; margin-top: 12px; }
                        .approval-page th, .approval-page td { border: 1px solid #4b5563; padding: 5px 7px; vertical-align: middle; }
                        .approval-page th { background: #eef2f7; }
                        .approval-page td.amount, .approval-page th.amount { text-align: right; }
                        .approval-page .approval-signature { margin-top: 14px; border: 1px solid #4b5563; font-size: 13px; }
                        .approval-page .approval-signature-row { display: grid; grid-template-columns: 92px 1fr 92px 1fr; min-height: 58px; border-bottom: 1px solid #4b5563; }
                        .approval-page .approval-signature-row:last-child { border-bottom: 0; }
                        .approval-page .approval-signature-row b, .approval-page .approval-signature-row span { padding: 8px; border-right: 1px solid #4b5563; }
                        .approval-page .approval-signature-row span:last-child { border-right: 0; }
                        .approval-page .approval-print-note { margin-top: 8px; color: #6b7280; font-size: 11px; text-align: right; }
                        @page { size: A4 portrait; margin: 10mm; }
                        @media print { .batch-toolbar { display: none; } .batch-cover { page-break-after: always; } }
                    </style>
                </head>
                <body>
                    <div class="batch-toolbar"><button onclick="window.print()">打印全部</button></div>
                """);
        html.append("<div class=\"batch-cover\"><h1>批量工资审批表</h1><div class=\"meta\">")
                .append(meta("单位", orgCode))
                .append(meta("办理年月", year + "." + String.format("%02d", month)))
                .append(meta("业务类型", businessType.isBlank() ? "全部" : businessType))
                .append(meta("关键字", keyword.isBlank() ? "全部" : keyword))
                .append(meta("审批表数量", cases.size()))
                .append(meta("校验提示", warningCount))
                .append(meta("写入阻断", writePlanBlockedCount))
                .append(meta("待复核", reviewPendingCount))
                .append(meta("生成时间", LocalDateTime.now().withNano(0).toString()))
                .append("</div></div>");
        if (cases.isEmpty()) {
            html.append("<div class=\"approval-page\"><p style=\"text-align:center;\">无数据</p></div>");
        }
        for (SalaryCasePrintData item : cases) {
            html.append("<section class=\"approval-page\">")
                    .append(htmlBodyContent(salaryCaseApprovalHtml(
                            item.businessCase(),
                            item.snapshot(),
                            item.salaryItems(),
                            item.person(),
                            item.writePlan()
                    )))
                    .append("</section>");
        }
        html.append("</body></html>");
        return html.toString();
    }

    private String salaryCaseApprovalRosterHtmlV2(String orgCode, int year, int month, String businessType, String keyword, List<Map<String, Object>> rows) {
        Map<String, Long> businessCounts = rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        row -> text(row.get("businessType")).isBlank() ? "-" : text(row.get("businessType")),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()
                ));
        long writableCount = rows.stream()
                .filter(row -> Boolean.TRUE.equals(booleanValue(row.get("writable"))))
                .count();
        long reviewedCount = rows.stream()
                .filter(row -> !text(row.get("reviewStatus")).isBlank() || !text(row.get("comparisonReviewStatus")).isBlank())
                .count();
        BigDecimal totalDifference = rows.stream()
                .map(row -> amount(row.get("trialDifference")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>&#24037;&#36164;&#23457;&#25209;&#28165;&#20876;</title>
                    <style>
                        * { box-sizing: border-box; }
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 18px; background: #fff; }
                        .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 12px; }
                        button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        .print-sheet { max-width: 1120px; margin: 0 auto; }
                        h1 { text-align: center; font-size: 22px; margin: 4px 0 6px; letter-spacing: 0; }
                        .print-subtitle { display: flex; justify-content: space-between; font-size: 12px; color: #4b5563; margin-bottom: 10px; }
                        .meta { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px 18px; font-size: 13px; margin-bottom: 10px; }
                        .summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); border: 1px solid #4b5563; border-bottom: 0; margin-bottom: 12px; }
                        .summary-grid span { border-right: 1px solid #4b5563; border-bottom: 1px solid #4b5563; padding: 7px 8px; font-size: 13px; min-height: 30px; }
                        .summary-grid span:nth-child(4n) { border-right: 0; }
                        table { width: 100%; border-collapse: collapse; font-size: 10.5px; }
                        th, td { border: 1px solid #4b5563; padding: 4px 5px; vertical-align: middle; }
                        th { background: #eef2f7; font-weight: 700; }
                        td.amount, th.amount { text-align: right; }
                        .sign { margin-top: 14px; display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; font-size: 13px; }
                        .sign span { border-top: 1px solid #4b5563; padding-top: 8px; min-height: 34px; }
                        @page { size: A4 landscape; margin: 10mm; }
                        @media print { .toolbar { display: none; } body { margin: 0; } .print-sheet { max-width: none; } thead { display: table-header-group; } tr, .summary-grid, .sign { break-inside: avoid; } }
                    </style>
                </head>
                <body>
                    <div class="toolbar"><button onclick="window.print()">&#25171;&#21360;</button></div>
                    <main class="print-sheet">
                """);
        html.append("<h1>&#24037;&#36164;&#23457;&#25209;&#28165;&#20876;</h1>");
        html.append("<div class=\"print-subtitle\"><span>&#24037;&#36164;&#19994;&#21153;&#21150;&#29702;&#27719;&#24635;</span><span>&#25171;&#21360;&#26102;&#38388;&#65306;")
                .append(escapeHtml(LocalDateTime.now().withNano(0).toString()))
                .append("</span></div>");
        html.append("<div class=\"meta\"><span>&#21333;&#20301;&#65306;").append(escapeHtml(orgCode)).append("</span>")
                .append("<span>&#21150;&#29702;&#24180;&#26376;&#65306;").append(year).append(".").append(String.format("%02d", month)).append("</span>")
                .append("<span>&#19994;&#21153;&#31867;&#22411;&#65306;").append(escapeHtml(businessType.isBlank() ? "全部" : businessType)).append("</span>")
                .append("<span>&#20851;&#38190;&#23383;&#65306;").append(escapeHtml(keyword.isBlank() ? "全部" : keyword)).append("</span>")
                .append("<span>&#29983;&#25104;&#26102;&#38388;&#65306;").append(escapeHtml(LocalDateTime.now().withNano(0).toString())).append("</span></div>");
        html.append("<div class=\"summary-grid\">")
                .append(approvalCell("&#19994;&#21153;&#31508;&#25968;", rows.size()))
                .append(approvalCell("&#21487;&#20889;&#20837;", writableCount))
                .append(approvalCell("&#24050;&#22797;&#26680;/&#24050;&#30830;&#35748;", reviewedCount))
                .append(approvalCell("&#24046;&#39069;&#21512;&#35745;", totalDifference));
        for (Map.Entry<String, Long> entry : businessCounts.entrySet()) {
            html.append(approvalCell(entry.getKey(), entry.getValue()));
        }
        html.append("</div>");
        html.append("""
                <table>
                    <thead>
                    <tr>
                        <th>&#24207;&#21495;</th><th>&#21150;&#29702;&#32534;&#21495;</th><th>&#20154;&#21592;&#32534;&#30721;</th><th>&#22995;&#21517;</th><th>&#19994;&#21153;&#31867;&#22411;</th><th>&#26631;&#39064;</th>
                        <th>&#21150;&#29702;&#29366;&#24577;</th><th>&#35797;&#31639;&#29366;&#24577;</th><th class="amount">&#35797;&#31639;&#21069;</th><th class="amount">&#35797;&#31639;&#21518;</th><th class="amount">&#24046;&#39069;</th>
                        <th>&#22797;&#26680;</th><th>&#20889;&#20837;&#35745;&#21010;</th><th>&#39044;&#26816;</th><th>&#21487;&#20889;</th><th>&#21382;&#21490;ID</th><th>&#32463;&#21150;</th>
                    </tr>
                    </thead>
                    <tbody>
                """);
        if (rows.isEmpty()) {
            html.append("<tr><td colspan=\"17\" style=\"text-align:center;\">&#26080;&#25968;&#25454;</td></tr>");
        }
        int index = 1;
        for (Map<String, Object> row : rows) {
            Boolean writable = booleanValue(row.get("writable"));
            html.append("<tr>")
                    .append(td(index++))
                    .append(td(row.get("caseNo")))
                    .append(td(row.get("personCode")))
                    .append(td(row.get("personName")))
                    .append(td(row.get("businessType")))
                    .append(td(row.get("title")))
                    .append(td(reportStatusText("case", row.get("status"))))
                    .append(td(reportStatusText("trial", row.get("trialStatus"))))
                    .append(amountTd(row.get("trialBaselineTotal")))
                    .append(amountTd(row.get("trialCalculatedTotal")))
                    .append(amountTd(row.get("trialDifference")))
                    .append(td(reportStatusText("review", firstPresent(row.get("reviewStatus"), row.get("comparisonReviewStatus")))))
                    .append(td(row.get("planNo")))
                    .append(td(reportStatusText("plan", row.get("previewStatus"))))
                    .append(td(writable == null ? "" : writable ? "是" : "否"))
                    .append(td(row.get("insertedHistoryId")))
                    .append(td(text(row.get("handledBy")) + " " + text(row.get("handledAt"))))
                    .append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("<div class=\"sign\"><span>&#32463;&#21150;&#20154;&#65306;</span><span>&#22797;&#26680;&#20154;&#65306;</span><span>&#23457;&#25209;&#20154;&#65306;</span><span>&#26085;&#26399;&#65306;</span></div>");
        html.append("</main></body></html>");
        return html.toString();
    }

    private String salaryCaseApprovalRosterHtml(String orgCode, int year, int month, String businessType, String keyword, List<Map<String, Object>> rows) {
        Map<String, Long> businessCounts = rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        row -> text(row.get("businessType")).isBlank() ? "其他" : text(row.get("businessType")),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()
                ));
        long writableCount = rows.stream()
                .filter(row -> Boolean.TRUE.equals(booleanValue(row.get("writable"))))
                .count();
        long reviewedCount = rows.stream()
                .filter(row -> !text(row.get("reviewStatus")).isBlank() || !text(row.get("comparisonReviewStatus")).isBlank())
                .count();
        BigDecimal totalDifference = rows.stream()
                .map(row -> amount(row.get("trialDifference")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>工资审批清册</title>
                    <style>
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 24px; }
                        .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 12px; }
                        button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        h1 { text-align: center; font-size: 22px; margin: 8px 0 12px; }
                        .meta { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px 18px; font-size: 13px; margin-bottom: 10px; }
                        .summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); border: 1px solid #4b5563; border-bottom: 0; margin-bottom: 12px; }
                        .summary-grid span { border-right: 1px solid #4b5563; border-bottom: 1px solid #4b5563; padding: 7px 8px; font-size: 13px; }
                        .summary-grid span:nth-child(4n) { border-right: 0; }
                        table { width: 100%; border-collapse: collapse; font-size: 11px; }
                        th, td { border: 1px solid #4b5563; padding: 4px 5px; vertical-align: middle; }
                        th { background: #eef2f7; }
                        td.amount, th.amount { text-align: right; }
                        .sign { margin-top: 14px; display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; font-size: 13px; }
                        .sign span { border-top: 1px solid #4b5563; padding-top: 8px; min-height: 34px; }
                        @page { size: A4 landscape; margin: 10mm; }
                        @media print { .toolbar { display: none; } body { margin: 0; } }
                    </style>
                </head>
                <body>
                    <div class="toolbar"><button onclick="window.print()">打印</button></div>
                """);
        html.append("<h1>工资审批清册</h1>");
        html.append("<div class=\"meta\"><span>单位：").append(escapeHtml(orgCode)).append("</span>")
                .append("<span>办理年月：").append(year).append(".").append(String.format("%02d", month)).append("</span>")
                .append("<span>业务类型：").append(escapeHtml(businessType.isBlank() ? "全部" : businessType)).append("</span>")
                .append("<span>关键字：").append(escapeHtml(keyword.isBlank() ? "全部" : keyword)).append("</span>")
                .append("<span>生成时间：").append(escapeHtml(LocalDateTime.now().withNano(0).toString())).append("</span></div>");
        html.append("<div class=\"summary-grid\">")
                .append(meta("业务笔数", rows.size()))
                .append(meta("可写入", writableCount))
                .append(meta("已复核/已确认", reviewedCount))
                .append(meta("差额合计", totalDifference));
        for (Map.Entry<String, Long> entry : businessCounts.entrySet()) {
            html.append(meta(entry.getKey(), entry.getValue()));
        }
        html.append("</div>");
        html.append("""
                <table>
                    <thead>
                    <tr>
                        <th>序号</th><th>办理编号</th><th>人员编码</th><th>姓名</th><th>业务类型</th><th>标题</th>
                        <th>办理状态</th><th>试算状态</th><th class="amount">试算前</th><th class="amount">试算后</th><th class="amount">差额</th>
                        <th>复核</th><th>写入计划</th><th>预检</th><th>可写</th><th>历史ID</th><th>经办</th>
                    </tr>
                    </thead>
                    <tbody>
                """);
        if (rows.isEmpty()) {
            html.append("<tr><td colspan=\"17\" style=\"text-align:center;\">无数据</td></tr>");
        }
        int index = 1;
        for (Map<String, Object> row : rows) {
            Boolean writable = booleanValue(row.get("writable"));
            html.append("<tr>")
                    .append(td(index++))
                    .append(td(row.get("caseNo")))
                    .append(td(row.get("personCode")))
                    .append(td(row.get("personName")))
                    .append(td(row.get("businessType")))
                    .append(td(row.get("title")))
                    .append(td(reportStatusText("case", row.get("status"))))
                    .append(td(reportStatusText("trial", row.get("trialStatus"))))
                    .append(amountTd(row.get("trialBaselineTotal")))
                    .append(amountTd(row.get("trialCalculatedTotal")))
                    .append(amountTd(row.get("trialDifference")))
                    .append(td(reportStatusText("review", firstPresent(row.get("reviewStatus"), row.get("comparisonReviewStatus")))))
                    .append(td(row.get("planNo")))
                    .append(td(reportStatusText("plan", row.get("previewStatus"))))
                    .append(td(writable == null ? "" : writable ? "是" : "否"))
                    .append(td(row.get("insertedHistoryId")))
                    .append(td(text(row.get("handledBy")) + " " + text(row.get("handledAt"))))
                    .append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("<div class=\"sign\"><span>经办人：</span><span>复核人：</span><span>审批人：</span><span>日期：</span></div>");
        html.append("</body></html>");
        return html.toString();
    }

    private Map<String, Object> salaryCasePerson(Map<String, Object> businessCase) {
        String personCode = text(businessCase.get("person_code"));
        String orgCode = text(businessCase.get("org_code"));
        String personNo = "";
        String[] parts = personCode.split("-", 2);
        if (parts.length == 2) {
            orgCode = parts[0].trim();
            personNo = parts[1].trim();
        }
        if (orgCode.isBlank() || personNo.isBlank()) {
            return Map.of();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT TRIM(COALESCE(dwbm, '')) AS orgCode,
                       TRIM(COALESCE(grbm, '')) AS personNo,
                       TRIM(COALESCE(xm, '')) AS personName,
                       TRIM(COALESCE(xb, '')) AS gender,
                       TRIM(COALESCE(sfzh, '')) AS idCard,
                       TRIM(COALESCE(csny, '')) AS birthDate,
                       TRIM(COALESCE(cjgzny, '')) AS workStart,
                       TRIM(COALESCE(xlbm, '')) AS educationCode,
                       TRIM(COALESCE(zgxl, '')) AS educationName,
                       TRIM(COALESCE(zwjb, '')) AS postLevel,
                       TRIM(COALESCE(xrzw, '')) AS currentPost,
                       TRIM(COALESCE(srny, '')) AS postStart
                FROM dryjbxx
                WHERE dwbm = ?
                  AND grbm = ?
                LIMIT 1
                """, orgCode, personNo);
        return rows.isEmpty() ? Map.of() : rows.getFirst();
    }

    private Map<String, Object> salaryCaseWritePlan(String caseNo) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT plan_no AS planNo,
                       plan_status AS planStatus,
                       preview_status AS previewStatus,
                       writable AS writable,
                       existing_history_id AS existingHistoryId,
                       inserted_history_id AS insertedHistoryId,
                       previous_history_id AS previousHistoryId,
                       next_history_id AS nextHistoryId,
                       sid_plan AS sidPlan,
                       execution_result AS executionResult,
                       execution_message AS executionMessage,
                       comparison_review_status AS comparisonReviewStatus,
                       comparison_review_category AS comparisonReviewCategory,
                       comparison_review_reason AS comparisonReviewReason,
                       issues_json AS issuesJson
                FROM salary_history_write_plan
                WHERE case_no = ?
                ORDER BY id DESC
                LIMIT 1
                """, caseNo);
        return rows.isEmpty() ? Map.of() : rows.getFirst();
    }

    private String salaryCaseBasisHtml(
            Map<String, Object> businessCase,
            Map<String, Object> snapshot,
            Map<String, Object> person,
            Map<String, Object> writePlan,
            String businessType
    ) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"section-title\">办理依据</div><div class=\"basis\">")
                .append(basis("人员类别", firstPresent(person.get("postLevel"), person.get("postCode"))))
                .append(basis("现任职务", firstPresent(person.get("currentPost"), person.get("postCode"))))
                .append(basis("任职时间", person.get("postStart")))
                .append(basis("参加工作", person.get("workStart")))
                .append(basis("学历", firstPresent(person.get("educationName"), person.get("educationCode"))))
                .append(basis("业务口径", salaryCasePolicyHint(businessType)))
                .append(basis("强制办理说明", businessCase.get("force_reason"), true))
                .append(basis("差异确认说明", businessCase.get("difference_reason"), true))
                .append("</div>");
        html.append("<div class=\"section-title\">试算与归档</div><div class=\"basis\">")
                .append(basis("快照来源", firstPresent(snapshot.get("snapshot_by"), businessCase.get("handled_by"))))
                .append(basis("快照时间", snapshot.get("snapshot_at")))
                .append(basis("复核状态", reportStatusText("review", businessCase.get("review_status"))))
                .append(basis("复核说明", businessCase.get("review_reason")))
                .append(basis("写入计划", writePlan.get("planNo")))
                .append(basis("计划状态", reportStatusText("plan", writePlan.get("planStatus"))))
                .append(basis("预检状态", reportStatusText("plan", writePlan.get("previewStatus"))))
                .append(basis("是否可写", booleanValue(writePlan.get("writable")) == null ? "" : Boolean.TRUE.equals(booleanValue(writePlan.get("writable"))) ? "是" : "否"))
                .append(basis("既有历史ID", writePlan.get("existingHistoryId")))
                .append(basis("写入历史ID", writePlan.get("insertedHistoryId")))
                .append(basis("前序历史ID", writePlan.get("previousHistoryId")))
                .append(basis("后续历史ID", writePlan.get("nextHistoryId")))
                .append(basis("执行结果", reportStatusText("execution", writePlan.get("executionResult"))))
                .append(basis("执行说明", writePlan.get("executionMessage"), true))
                .append(basis("sid计划", writePlan.get("sidPlan"), true))
                .append(basis("阻断/预警", compactJson(writePlan.get("issuesJson")), true))
                .append("</div>");
        return html.toString();
    }

    private String basis(String label, Object value) {
        return basis(label, value, false);
    }

    private String basis(String label, Object value, boolean wide) {
        return "<span" + (wide ? " class=\"wide\"" : "") + "><b>"
                + escapeHtml(label) + "：</b>" + escapeHtml(amountOrText(value)) + "</span>";
    }

    private String salaryCaseApprovalTitleText(String businessType) {
        String safe = text(businessType);
        if (containsAny(safe, "调入", "新进", "转业", "退伍")) {
            return "新进/调入工资定资审批表";
        }
        if (containsAny(safe, "转正", "见习")) {
            return "见习期及转正定级工资审批表";
        }
        if (containsAny(safe, "职务", "职级", "岗位")) {
            return "职务职级岗位变动工资审批表";
        }
        if (containsAny(safe, "正常", "晋档", "薪级", "级别")) {
            return "正常晋升工资审批表";
        }
        if (containsAny(safe, "津贴", "补贴", "教护")) {
            return "津贴补贴调整审批表";
        }
        if (containsAny(safe, "处分", "降资")) {
            return "降资处分工资审批表";
        }
        return "工资变动审批表";
    }

    private String salaryCasePolicyHintText(String businessType) {
        String safe = text(businessType);
        if (containsAny(safe, "调入", "新进", "转业", "退伍")) {
            return "按新进、调入或安置定资口径确定执行工资。";
        }
        if (safe.contains("转正")) {
            return "按学历和转正时任职信息确定转正定级工资。";
        }
        if (safe.contains("见习")) {
            return "按见习时学历查见习/试用期工资标准。";
        }
        if (containsAny(safe, "职级", "职务", "岗位")) {
            return "按任职信息、岗位职级和对应工资标准重新确定工资。";
        }
        if (containsAny(safe, "正常", "晋档", "薪级", "级别")) {
            return "按年度考核累计条件生成正常晋档、薪级或级别晋升。";
        }
        if (containsAny(safe, "津贴", "补贴", "教护")) {
            return "按职务编码、执行标准和教护龄等基础信息重算津补贴。";
        }
        if (containsAny(safe, "处分", "降资")) {
            return "按处分记录和降资口径生成工资变动。";
        }
        return "按当前工资政策和基础信息试算生成。";
    }

    private boolean containsAny(String value, String... tokens) {
        String safe = text(value);
        for (String token : tokens) {
            if (safe.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private String salaryCaseApprovalTitle(String businessType) {
        String safe = text(businessType);
        if (safe.contains("调入") || safe.contains("新进") || safe.contains("转业") || safe.contains("退伍")) {
            return "新进/调入工资定资审批表";
        }
        if (safe.contains("转正") || safe.contains("见习")) {
            return "见习期及转正定级工资审批表";
        }
        if (safe.contains("职务") || safe.contains("职级") || safe.contains("岗位")) {
            return "职务职级岗位变动工资审批表";
        }
        if (safe.contains("正常") || safe.contains("晋档") || safe.contains("薪级") || safe.contains("级别")) {
            return "正常晋升工资审批表";
        }
        if (safe.contains("津贴") || safe.contains("补贴") || safe.contains("教护")) {
            return "津贴补贴调整审批表";
        }
        if (safe.contains("处分") || safe.contains("降资")) {
            return "降资处分工资审批表";
        }
        return "工资变动审批表";
    }

    private String salaryCasePolicyHint(String businessType) {
        String preferred = salaryCasePolicyHintText(businessType);
        if (!preferred.isBlank()) {
            return preferred;
        }
        String safe = text(businessType);
        if (safe.contains("调入") || safe.contains("新进") || safe.contains("转业") || safe.contains("退伍")) {
            return "按新进、调入或安置定资口径确定执行工资。";
        }
        if (safe.contains("转正")) {
            return "按学历和转正时任职信息确定转正定级工资。";
        }
        if (safe.contains("见习")) {
            return "按见习时学历查见习/试用期工资标准。";
        }
        if (safe.contains("职级") || safe.contains("职务") || safe.contains("岗位")) {
            return "按任职信息、岗位职级和对应工资标准重新确定工资。";
        }
        if (safe.contains("正常") || safe.contains("晋档") || safe.contains("薪级") || safe.contains("级别")) {
            return "按年度考核累计条件生成正常晋档、薪级或级别晋升。";
        }
        if (safe.contains("津贴") || safe.contains("补贴") || safe.contains("教护")) {
            return "按职务编码、执行标准和教护龄等基础信息重算津补贴。";
        }
        if (safe.contains("处分") || safe.contains("降资")) {
            return "按处分记录和降资口径生成工资变动。";
        }
        return "按当前工资政策和基础信息试算生成。";
    }

    private String compactJson(Object value) {
        String json = text(value);
        if (json.isBlank()) {
            return "";
        }
        return json.length() > 180 ? json.substring(0, 180) + "..." : json;
    }

    private String htmlBodyContent(String html) {
        String safe = html == null ? "" : html;
        String lower = safe.toLowerCase(Locale.ROOT);
        int bodyStart = lower.indexOf("<body");
        if (bodyStart >= 0) {
            int startClose = lower.indexOf(">", bodyStart);
            int bodyEnd = lower.lastIndexOf("</body>");
            if (startClose >= 0 && bodyEnd > startClose) {
                return safe.substring(startClose + 1, bodyEnd);
            }
        }
        return safe;
    }

    private Map<String, Object> previewItem(String code, String title, long count, int limit, boolean requiresOrg) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("code", code);
        item.put("title", title);
        item.put("count", count);
        item.put("limit", limit);
        item.put("limited", count > limit);
        item.put("requiresOrg", requiresOrg);
        return item;
    }

    private long countSalaryCases(String orgCode, int year, int month, String businessType, String keyword) {
        List<Object> args = new ArrayList<>();
        args.add(orgCode);
        args.add(year);
        args.add(month);
        String businessTypeWhere = "";
        if (!businessType.isBlank()) {
            businessTypeWhere = "AND business_type = ?";
            args.add(businessType);
        }
        String keywordWhere = "";
        if (!keyword.isBlank()) {
            keywordWhere = """
                      AND (
                          case_no LIKE CONCAT('%', ?, '%')
                          OR person_code LIKE CONCAT('%', ?, '%')
                          OR person_name LIKE CONCAT('%', ?, '%')
                          OR title LIKE CONCAT('%', ?, '%')
                          OR summary LIKE CONCAT('%', ?, '%')
                      )
                    """;
            args.add(keyword);
            args.add(keyword);
            args.add(keyword);
            args.add(keyword);
            args.add(keyword);
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM salary_business_case
                WHERE org_code LIKE CONCAT(?, '%')
                  AND event_year = ?
                  AND event_month = ?
                  __BUSINESS_TYPE_WHERE__
                  __KEYWORD_WHERE__
                """.replace("__BUSINESS_TYPE_WHERE__", businessTypeWhere)
                .replace("__KEYWORD_WHERE__", keywordWhere), Long.class, args.toArray());
        return count == null ? 0L : count;
    }

    private long countPersonRoster(String orgCode, int year, int month, String keyword) {
        List<Object> args = new ArrayList<>();
        args.add(orgCode);
        String keywordWhere = "";
        if (!keyword.isBlank()) {
            keywordWhere = """
                      AND (
                          TRIM(p.grbm) LIKE CONCAT('%', ?, '%')
                          OR TRIM(COALESCE(p.xm, '')) LIKE CONCAT('%', ?, '%')
                          OR TRIM(COALESCE(p.sfzh, '')) LIKE CONCAT('%', ?, '%')
                      )
                    """;
            args.add(keyword);
            args.add(keyword);
            args.add(keyword);
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dryjbxx p
                WHERE TRIM(p.dwbm) LIKE CONCAT(?, '%')
                __KEYWORD_WHERE__
                """.replace("__KEYWORD_WHERE__", keywordWhere), Long.class, args.toArray());
        return count == null ? 0L : count;
    }

    private long countSalaryRoster(String orgCode, int year, int month) {
        int periodKey = year * 100 + month;
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM hisbase h
                WHERE TRIM(h.dwbm) LIKE CONCAT(?, '%')
                  AND TRIM(COALESCE(h.jsnf, '')) REGEXP '^[0-9]{4}$'
                  AND TRIM(COALESCE(h.jsyf, '')) REGEXP '^[0-9]{1,2}$'
                  AND (CAST(TRIM(h.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(h.jsyf) AS UNSIGNED)) <= ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM hisbase n
                      WHERE n.dwbm = h.dwbm
                        AND n.grbm = h.grbm
                        AND TRIM(COALESCE(n.jsnf, '')) REGEXP '^[0-9]{4}$'
                        AND TRIM(COALESCE(n.jsyf, '')) REGEXP '^[0-9]{1,2}$'
                        AND (CAST(TRIM(n.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(n.jsyf) AS UNSIGNED)) <= ?
                        AND (
                            (CAST(TRIM(n.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(n.jsyf) AS UNSIGNED))
                                > (CAST(TRIM(h.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(h.jsyf) AS UNSIGNED))
                            OR (
                                (CAST(TRIM(n.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(n.jsyf) AS UNSIGNED))
                                    = (CAST(TRIM(h.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(h.jsyf) AS UNSIGNED))
                                AND TRIM(COALESCE(n.id, '')) > TRIM(COALESCE(h.id, ''))
                            )
                        )
                  )
                """, Long.class, orgCode, periodKey, periodKey);
        return count == null ? 0L : count;
    }

    private long countSalaryHistory(String orgCode, String personCode, int yearFrom, int yearTo) {
        List<Object> args = new ArrayList<>();
        args.add(orgCode);
        args.add(yearFrom);
        args.add(yearTo);
        String personWhere = "";
        if (!personCode.isBlank()) {
            String[] parts = personCode.split("-", 2);
            if (parts.length == 2) {
                personWhere = "AND TRIM(h.dwbm) = ? AND TRIM(h.grbm) = ?";
                args.add(parts[0].trim());
                args.add(parts[1].trim());
            } else {
                personWhere = "AND CONCAT(TRIM(h.dwbm), '-', TRIM(h.grbm)) = ?";
                args.add(personCode);
            }
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM hisbase h
                WHERE TRIM(h.dwbm) LIKE CONCAT(?, '%')
                  AND TRIM(COALESCE(h.jsnf, '')) REGEXP '^[0-9]{4}$'
                  AND CAST(TRIM(h.jsnf) AS UNSIGNED) BETWEEN ? AND ?
                  __PERSON_WHERE__
                """.replace("__PERSON_WHERE__", personWhere), Long.class, args.toArray());
        return count == null ? 0L : count;
    }

    private long countAssessment(String orgCode, int year, String keyword) {
        List<Object> args = new ArrayList<>();
        args.add(orgCode);
        args.add(String.valueOf(year));
        String keywordWhere = "";
        if (!keyword.isBlank()) {
            keywordWhere = """
                      AND (
                          TRIM(k.grbm) LIKE CONCAT('%', ?, '%')
                          OR TRIM(COALESCE(p.xm, '')) LIKE CONCAT('%', ?, '%')
                          OR TRIM(COALESCE(k.khjg, '')) LIKE CONCAT('%', ?, '%')
                      )
                    """;
            args.add(keyword);
            args.add(keyword);
            args.add(keyword);
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dndkh k
                LEFT JOIN dryjbxx p ON p.dwbm = k.dwbm AND p.grbm = k.grbm
                WHERE TRIM(k.dwbm) LIKE CONCAT(?, '%')
                  AND TRIM(COALESCE(k.khnd, '')) = ?
                __KEYWORD_WHERE__
                """.replace("__KEYWORD_WHERE__", keywordWhere), Long.class, args.toArray());
        return count == null ? 0L : count;
    }

    private long countStandardTable(String tableName, String keyword) {
        List<String> columns = standardTableColumns(tableName);
        if (columns.isEmpty()) {
            return 0L;
        }
        String keywordWhere = "";
        List<Object> args = new ArrayList<>();
        if (!keyword.isBlank()) {
            String concatSql = columns.stream()
                    .map(column -> "COALESCE(CAST(" + quotedIdentifier(column) + " AS CHAR), '')")
                    .collect(java.util.stream.Collectors.joining(", "));
            keywordWhere = " WHERE CONCAT_WS('|', " + concatSql + ") LIKE CONCAT('%', ?, '%')";
            args.add(keyword);
        }
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + quotedIdentifier(tableName) + keywordWhere,
                Long.class,
                args.toArray()
        );
        return count == null ? 0L : count;
    }

    private long countReportAudits(String targetCode, String action) {
        List<Object> args = new ArrayList<>();
        String targetWhere = "";
        if (!targetCode.isBlank()) {
            targetWhere = "AND (target_code LIKE CONCAT('%', ?, '%') OR target_type LIKE CONCAT('%', ?, '%'))";
            args.add(targetCode);
            args.add(targetCode);
        }
        String actionWhere = "";
        if (!action.isBlank()) {
            actionWhere = "AND action_name LIKE CONCAT('%', ?, '%')";
            args.add(action);
        }
        String accessWhere = "AND " + organizationAccessService.orgCodeAccessSql("target_code");
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys_audit_log
                WHERE module_name = 'report'
                __TARGET_WHERE__
                __ACCESS_WHERE__
                __ACTION_WHERE__
                """.replace("__TARGET_WHERE__", targetWhere)
                .replace("__ACCESS_WHERE__", accessWhere)
                .replace("__ACTION_WHERE__", actionWhere), Long.class, args.toArray());
        return count == null ? 0L : count;
    }

    private String standardTableHtmlV2(String tableName, String title, String keyword, List<String> columns, List<Map<String, Object>> rows) {
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>&#24037;&#36164;&#26631;&#20934;&#34920;</title>
                    <style>
                        * { box-sizing: border-box; }
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 18px; background: #fff; }
                        .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 12px; }
                        button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        .print-sheet { max-width: 1120px; margin: 0 auto; }
                        h1 { text-align: center; font-size: 22px; margin: 4px 0 6px; letter-spacing: 0; }
                        .print-subtitle { display: flex; justify-content: space-between; font-size: 12px; color: #4b5563; margin-bottom: 10px; }
                        .meta { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px 18px; font-size: 13px; margin-bottom: 10px; }
                        table { width: 100%; border-collapse: collapse; font-size: 10.5px; }
                        th, td { border: 1px solid #4b5563; padding: 4px 5px; vertical-align: middle; white-space: nowrap; }
                        th { background: #eef2f7; font-weight: 700; }
                        td.amount, th.amount { text-align: right; }
                        @page { size: A4 landscape; margin: 10mm; }
                        @media print { .toolbar { display: none; } body { margin: 0; } .print-sheet { max-width: none; } thead { display: table-header-group; } tr { break-inside: avoid; } }
                    </style>
                </head>
                <body>
                    <div class="toolbar"><button onclick="window.print()">&#25171;&#21360;</button></div>
                    <main class="print-sheet">
                """);
        html.append("<h1>").append(escapeHtml(title.isBlank() ? "工资标准表" : title)).append("</h1>");
        html.append("<div class=\"print-subtitle\"><span>&#24037;&#36164;&#26631;&#20934;&#21462;&#20540;&#21442;&#32771;</span><span>&#25171;&#21360;&#26102;&#38388;&#65306;")
                .append(escapeHtml(LocalDateTime.now().withNano(0).toString()))
                .append("</span></div>");
        html.append("<div class=\"meta\"><span>&#26631;&#20934;&#34920;&#65306;").append(escapeHtml(tableName)).append("</span>")
                .append("<span>&#20851;&#38190;&#23383;&#65306;").append(escapeHtml(keyword.isBlank() ? "全部" : keyword)).append("</span>")
                .append("<span>&#35760;&#24405;&#25968;&#65306;").append(rows.size()).append("</span>")
                .append("<span>&#21015;&#25968;&#65306;").append(columns.size()).append("</span></div>");
        html.append("<table><thead><tr><th>&#24207;&#21495;</th>");
        for (String column : columns) {
            html.append("<th>").append(escapeHtml(column)).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        if (rows.isEmpty()) {
            html.append("<tr><td colspan=\"").append(columns.size() + 1).append("\" style=\"text-align:center;\">&#26080;&#25968;&#25454;</td></tr>");
        }
        int index = 1;
        for (Map<String, Object> row : rows) {
            html.append("<tr>").append(td(index++));
            for (String column : columns) {
                Object value = row.get(column);
                if (value instanceof Number) {
                    html.append(amountTd(value));
                } else {
                    html.append(td(value));
                }
            }
            html.append("</tr>");
        }
        html.append("</tbody></table></main></body></html>");
        return html.toString();
    }

    private String standardTableHtml(String tableName, String title, String keyword, List<String> columns, List<Map<String, Object>> rows) {
        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>工资标准表</title>
                    <style>
                        body { font-family: "Microsoft YaHei", SimSun, sans-serif; color: #111827; margin: 24px; }
                        .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 12px; }
                        button { border: 1px solid #9ca3af; background: #fff; padding: 6px 12px; cursor: pointer; }
                        h1 { text-align: center; font-size: 22px; margin: 8px 0 12px; }
                        .meta { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px 18px; font-size: 13px; margin-bottom: 10px; }
                        table { width: 100%; border-collapse: collapse; font-size: 11px; }
                        th, td { border: 1px solid #4b5563; padding: 4px 5px; vertical-align: middle; white-space: nowrap; }
                        th { background: #eef2f7; }
                        td.amount, th.amount { text-align: right; }
                        @page { size: A4 landscape; margin: 10mm; }
                        @media print { .toolbar { display: none; } body { margin: 0; } }
                    </style>
                </head>
                <body>
                    <div class="toolbar"><button onclick="window.print()">打印</button></div>
                """);
        html.append("<h1>").append(escapeHtml(title)).append("</h1>");
        html.append("<div class=\"meta\"><span>标准表：").append(escapeHtml(tableName)).append("</span>")
                .append("<span>关键字：").append(escapeHtml(keyword.isBlank() ? "全部" : keyword)).append("</span>")
                .append("<span>记录数：").append(rows.size()).append("</span>")
                .append("<span>生成时间：").append(escapeHtml(LocalDateTime.now().withNano(0).toString())).append("</span></div>");
        html.append("<table><thead><tr><th>序号</th>");
        for (String column : columns) {
            html.append("<th>").append(escapeHtml(column)).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        if (rows.isEmpty()) {
            html.append("<tr><td colspan=\"").append(columns.size() + 1).append("\" style=\"text-align:center;\">无数据</td></tr>");
        }
        int index = 1;
        for (Map<String, Object> row : rows) {
            html.append("<tr>").append(td(index++));
            for (String column : columns) {
                Object value = row.get(column);
                if (value instanceof Number) {
                    html.append(amountTd(value));
                } else {
                    html.append(td(value));
                }
            }
            html.append("</tr>");
        }
        html.append("</tbody></table></body></html>");
        return html.toString();
    }

    private String meta(String label, Object value) {
        return "<span><b>" + escapeHtml(label) + "：</b>" + escapeHtml(amountOrText(value)) + "</span>";
    }

    private String amountOrText(Object value) {
        if (value instanceof Number) {
            return amountText(value);
        }
        return text(value);
    }

    private String td(Object value) {
        return "<td>" + escapeHtml(text(value)) + "</td>";
    }

    private String amountTd(Object value) {
        return "<td class=\"amount\">" + escapeHtml(amountText(value)) + "</td>";
    }

    private String amountText(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue()).stripTrailingZeros().toPlainString();
        }
        return text(value);
    }

    private BigDecimal amount(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(text(value));
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
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
        } catch (Exception ex) {
            return List.of();
        }
    }

    private Object firstPresent(Object... values) {
        for (Object value : values) {
            if (!text(value).isBlank()) {
                return value;
            }
        }
        return "";
    }

    private int number(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(text(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
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
        String safe = text(value);
        return safe.isBlank() ? null : !"0".equals(safe) && !"false".equalsIgnoreCase(safe);
    }

    private String reportAuditSummary(String... parts) {
        return java.util.Arrays.stream(parts)
                .filter(part -> part != null && !part.isBlank())
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private String auditPart(String key, Object value) {
        return key + "=" + text(value);
    }

    private String periodText(int year, int month) {
        int safeMonth = Math.max(1, Math.min(month, 12));
        return year + "-" + String.format("%02d", safeMonth);
    }

    private String salaryColumnAuditText(List<ReportSalaryColumn> columns) {
        if (columns == null || columns.isEmpty()) {
            return "fixed";
        }
        return columns.stream()
                .map(ReportSalaryColumn::code)
                .collect(java.util.stream.Collectors.joining("|"));
    }

    private String standardTableName(String tableName) {
        String safe = text(tableName).toLowerCase(Locale.ROOT);
        if (!STANDARD_REPORT_TABLES.contains(safe)) {
            throw new IllegalArgumentException("Unsupported standard table: " + tableName);
        }
        return safe;
    }

    private List<String> standardTableColumns(String tableName) {
        return jdbcTemplate.queryForList("""
                SELECT column_name
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                ORDER BY ordinal_position
                """, String.class, tableName);
    }

    private String quotedIdentifier(String name) {
        return "`" + text(name).replace("`", "``") + "`";
    }

    private List<ReportSalaryColumn> salaryReportColumns(String columns) {
        String safe = text(columns);
        if (safe.isBlank()) {
            return List.of();
        }
        Map<String, ReportSalaryColumn> available = new LinkedHashMap<>();
        jdbcTemplate.queryForList("""
                SELECT UPPER(TRIM(field_name)) AS itemCode,
                       TRIM(COALESCE(NULLIF(field_cap, ''), NULLIF(field_caps, ''), field_name)) AS itemName
                FROM fldgz
                WHERE field_type = 'N'
                  AND UPPER(TRIM(field_name)) <> 'HJ2'
                ORDER BY sequence, field_name
                """).forEach(row -> {
            String code = text(row.get("itemCode")).toUpperCase(Locale.ROOT);
            if (!code.isBlank()) {
                available.put(code, new ReportSalaryColumn(code, text(row.get("itemName")).isBlank() ? code : text(row.get("itemName"))));
            }
        });
        List<ReportSalaryColumn> selected = new ArrayList<>();
        for (String part : safe.split("[,;\\s]+")) {
            String code = text(part).toUpperCase(Locale.ROOT);
            if (code.isBlank()) {
                continue;
            }
            ReportSalaryColumn column = available.get(code);
            if (column == null) {
                throw new IllegalArgumentException("Unsupported salary report column: " + code);
            }
            if (selected.stream().noneMatch(item -> item.code().equals(code))) {
                selected.add(column);
            }
            if (selected.size() > 24) {
                throw new IllegalArgumentException("Salary report columns cannot exceed 24.");
            }
        }
        return selected;
    }

    private String dynamicSalarySelect(List<ReportSalaryColumn> salaryColumns) {
        if (salaryColumns.isEmpty()) {
            return "";
        }
        return salaryColumns.stream()
                .map(column -> ", h." + quotedIdentifier(column.code()) + " AS " + quotedIdentifier("salary_" + column.code()))
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    private String standardTableTitle(String tableName) {
        return switch (tableName) {
            case "bz06_blfb" -> "保留补发标准表";
            case "bz06_djgz" -> "等级工资标准表";
            case "bz06_fjtgb" -> "法检提高标准表";
            case "bz06_jbgz" -> "级别工资标准表";
            case "bz06_jbt" -> "津补贴标准表";
            case "bz06_jjjy" -> "警衔津贴标准表";
            case "bz06_jxgz" -> "绩效工资标准表";
            case "bz06_tgb" -> "提高标准表";
            case "bz06_xjgz" -> "薪级工资标准表";
            case "bz06_zw_gw" -> "职务岗位对照标准表";
            case "bz06_zw_jb_xj" -> "职务级别薪级对照标准表";
            case "bz06_zwgz" -> "职务工资标准表";
            case "bz06_zwgz_fj" -> "法检职务工资标准表";
            case "bz06_zwgz_gr" -> "工人工资标准表";
            case "bz06_zzdz" -> "见习/试用期工资标准表";
            default -> "工资标准表";
        };
    }

    private String legacyReportTitle(String code) {
        String upper = text(code).toUpperCase(Locale.ROOT);
        if (upper.contains("GZB")) {
            return "工资表";
        }
        if (upper.contains("HMC")) {
            return "花名册";
        }
        if (upper.contains("SPB")) {
            return "审批表";
        }
        if (upper.contains("JDB")) {
            return "级别档次表";
        }
        if (upper.contains("KH")) {
            return "考核统计表";
        }
        if (upper.contains("GZBD") || upper.contains("GZGLTZ")) {
            return "工资变动报表";
        }
        return upper;
    }

    private String legacyReportCategory(String code) {
        String upper = text(code).toUpperCase(Locale.ROOT);
        if (upper.contains("SPB")) {
            return "审批打印";
        }
        if (upper.contains("HMC") || upper.contains("GZB")) {
            return "工资名册";
        }
        if (upper.contains("KH") || upper.contains("TJ")) {
            return "统计汇总";
        }
        if (upper.contains("BZ")) {
            return "标准维护";
        }
        return "旧系统模板";
    }

    private void requireReportPermission() {
        String username = currentUserService.currentUsername();
        if (username == null || (!userPermissionService.hasMenu(username, "SALARY_REPORT")
                && !userPermissionService.hasMenu(username, "SALARY_EXPORT"))) {
            throw new IllegalArgumentException("Salary report permission is required.");
        }
    }

    private void requireReportAcceptanceExportPermission() {
        String username = currentUserService.currentUsername();
        if (username == null || !userPermissionService.hasMenu(username, "SALARY_EXPORT")
                || !userPermissionService.hasMenu(username, "SALARY_ACCEPTANCE")) {
            throw new IllegalArgumentException("Salary report acceptance export permission is required.");
        }
    }

    private ResponseEntity<byte[]> csvResponse(String filename, String text) {
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] body = new byte[textBytes.length + 3];
        body[0] = (byte) 0xEF;
        body[1] = (byte) 0xBB;
        body[2] = (byte) 0xBF;
        System.arraycopy(textBytes, 0, body, 3, textBytes.length);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    private byte[] zipResponse(Map<String, String> entries) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(utf8BomText(entry.getValue()));
                zip.closeEntry();
            }
            zip.finish();
            return output.toByteArray();
        }
    }

    private byte[] utf8BomText(String text) {
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] body = new byte[textBytes.length + 3];
        body[0] = (byte) 0xEF;
        body[1] = (byte) 0xBB;
        body[2] = (byte) 0xBF;
        System.arraycopy(textBytes, 0, body, 3, textBytes.length);
        return body;
    }

    private String safeFilePart(String value) {
        String safe = text(value).replaceAll("[^A-Za-z0-9._-]", "_");
        return safe.isBlank() ? "batch" : safe;
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private void csvRow(StringBuilder csv, Object... values) {
        for (int i = 0; i < values.length; i += 1) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(csv(text(values[i])));
        }
        csv.append('\n');
    }

    private String escapeHtml(String value) {
        String safe = value == null ? "" : value;
        return safe.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String normalizeDateTime(String value) {
        String safe = text(value);
        return safe.isBlank() ? "" : safe.replace('T', ' ');
    }

    public record ReportCatalogItem(
            String code,
            String title,
            String category,
            String legacyTemplate,
            String migrationStatus,
            String printUrl
    ) {
    }

    private record SalaryCasePrintData(
            Map<String, Object> businessCase,
            Map<String, Object> snapshot,
            List<Map<String, Object>> salaryItems,
            Map<String, Object> person,
            Map<String, Object> writePlan
    ) {
    }

    private record ReportPrintBatchData(
            Map<String, Object> batch,
            List<Map<String, Object>> items
    ) {
    }

    private record ReportSalaryColumn(
            String code,
            String title
    ) {
    }
}
