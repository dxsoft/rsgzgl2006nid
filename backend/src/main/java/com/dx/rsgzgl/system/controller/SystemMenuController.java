package com.dx.rsgzgl.system.controller;

import com.dx.rsgzgl.common.api.ApiResponse;
import com.dx.rsgzgl.system.dto.MenuAdminItemResponse;
import com.dx.rsgzgl.system.dto.MenuItemResponse;
import com.dx.rsgzgl.system.dto.MenuStatusUpdateRequest;
import com.dx.rsgzgl.system.dto.PasswordResetRequest;
import com.dx.rsgzgl.system.dto.RolePermissionResponse;
import com.dx.rsgzgl.system.dto.RoleTemplateResponse;
import com.dx.rsgzgl.system.dto.RoleCreateRequest;
import com.dx.rsgzgl.system.dto.RoleMenusUpdateRequest;
import com.dx.rsgzgl.system.dto.SystemAuditLogResponse;
import com.dx.rsgzgl.system.dto.UserCreateRequest;
import com.dx.rsgzgl.system.dto.UserOrgCodesUpdateRequest;
import com.dx.rsgzgl.system.dto.UserRoleResponse;
import com.dx.rsgzgl.system.dto.UserRolesUpdateRequest;
import com.dx.rsgzgl.system.service.SystemAdminQueryService;
import com.dx.rsgzgl.system.service.SystemAuditService;
import com.dx.rsgzgl.system.service.SystemMenuService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemMenuController {

    private final SystemMenuService systemMenuService;
    private final SystemAdminQueryService systemAdminQueryService;
    private final SystemAuditService systemAuditService;

    public SystemMenuController(
            SystemMenuService systemMenuService,
            SystemAdminQueryService systemAdminQueryService,
            SystemAuditService systemAuditService
    ) {
        this.systemMenuService = systemMenuService;
        this.systemAdminQueryService = systemAdminQueryService;
        this.systemAuditService = systemAuditService;
    }

    @GetMapping("/menus")
    public ApiResponse<List<MenuItemResponse>> menus() {
        return ApiResponse.ok(systemMenuService.menusForCurrentUser());
    }

    @GetMapping("/menu-admin")
    public ApiResponse<List<MenuAdminItemResponse>> menuAdmin() {
        return ApiResponse.ok(systemAdminQueryService.menus());
    }

    @GetMapping("/roles")
    public ApiResponse<List<RolePermissionResponse>> roles() {
        return ApiResponse.ok(systemAdminQueryService.roles());
    }

    @GetMapping("/role-templates")
    public ApiResponse<List<RoleTemplateResponse>> roleTemplates() {
        return ApiResponse.ok(systemAdminQueryService.roleTemplates());
    }

    @GetMapping("/users")
    public ApiResponse<List<UserRoleResponse>> users() {
        return ApiResponse.ok(systemAdminQueryService.users());
    }

    @GetMapping("/audits")
    public ApiResponse<List<SystemAuditLogResponse>> audits(
            @RequestParam(defaultValue = "") String module,
            @RequestParam(defaultValue = "") String operator,
            @RequestParam(defaultValue = "") String action,
            @RequestParam(defaultValue = "") String targetCode,
            @RequestParam(defaultValue = "") String auditId,
            @RequestParam(defaultValue = "") String start,
            @RequestParam(defaultValue = "") String end,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(systemAuditService.latest(module, operator, action, targetCode, auditId, start, end, limit));
    }

    @GetMapping(value = "/audits.csv", produces = "text/csv")
    public ResponseEntity<byte[]> auditsCsv(
            @RequestParam(defaultValue = "") String module,
            @RequestParam(defaultValue = "") String operator,
            @RequestParam(defaultValue = "") String action,
            @RequestParam(defaultValue = "") String targetCode,
            @RequestParam(defaultValue = "") String auditId,
            @RequestParam(defaultValue = "") String start,
            @RequestParam(defaultValue = "") String end,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        List<SystemAuditLogResponse> rows = systemAuditService.latest(module, operator, action, targetCode, auditId, start, end, limit);
        byte[] body = withUtf8Bom(auditsCsv(rows));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("system-audits.csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @PostMapping("/roles")
    public ApiResponse<List<RolePermissionResponse>> createRole(@RequestBody RoleCreateRequest request) {
        systemAdminQueryService.createRole(request.code(), request.name(), request.menuCodes());
        return ApiResponse.ok(systemAdminQueryService.roles());
    }

    @PostMapping("/users")
    public ApiResponse<List<UserRoleResponse>> createUser(@RequestBody UserCreateRequest request) {
        systemAdminQueryService.createUser(request.username(), request.displayName(), request.roleCodes(), request.orgCodes());
        return ApiResponse.ok(systemAdminQueryService.users());
    }

    @PutMapping("/menus/{code}/status")
    public ApiResponse<List<MenuAdminItemResponse>> updateMenuStatus(
            @PathVariable String code,
            @RequestBody MenuStatusUpdateRequest request
    ) {
        systemAdminQueryService.updateMenuStatus(code, request.status());
        return ApiResponse.ok(systemAdminQueryService.menus());
    }

    @PutMapping("/roles/{code}/menus")
    public ApiResponse<List<RolePermissionResponse>> updateRoleMenus(
            @PathVariable String code,
            @RequestBody RoleMenusUpdateRequest request
    ) {
        systemAdminQueryService.updateRoleMenus(code, request.menuCodes());
        return ApiResponse.ok(systemAdminQueryService.roles());
    }

    @PutMapping("/roles/{code}/status")
    public ApiResponse<List<RolePermissionResponse>> updateRoleStatus(
            @PathVariable String code,
            @RequestBody MenuStatusUpdateRequest request
    ) {
        systemAdminQueryService.updateRoleStatus(code, request.status());
        return ApiResponse.ok(systemAdminQueryService.roles());
    }

    @PutMapping("/roles/{code}/template/{templateCode}")
    public ApiResponse<List<RolePermissionResponse>> applyRoleTemplate(
            @PathVariable String code,
            @PathVariable String templateCode
    ) {
        systemAdminQueryService.applyRoleTemplate(code, templateCode);
        return ApiResponse.ok(systemAdminQueryService.roles());
    }

    @PutMapping("/users/{username}/roles")
    public ApiResponse<List<UserRoleResponse>> updateUserRoles(
            @PathVariable String username,
            @RequestBody UserRolesUpdateRequest request
    ) {
        systemAdminQueryService.updateUserRoles(username, request.roleCodes());
        return ApiResponse.ok(systemAdminQueryService.users());
    }

    @PutMapping("/users/{username}/orgs")
    public ApiResponse<List<UserRoleResponse>> updateUserOrgCodes(
            @PathVariable String username,
            @RequestBody UserOrgCodesUpdateRequest request
    ) {
        systemAdminQueryService.updateUserOrgCodes(username, request.orgCodes());
        return ApiResponse.ok(systemAdminQueryService.users());
    }

    @PutMapping("/users/{username}/status")
    public ApiResponse<List<UserRoleResponse>> updateUserStatus(
            @PathVariable String username,
            @RequestBody MenuStatusUpdateRequest request
    ) {
        systemAdminQueryService.updateUserStatus(username, request.status());
        return ApiResponse.ok(systemAdminQueryService.users());
    }

    @PutMapping("/users/{username}/password")
    public ApiResponse<Void> resetUserPassword(
            @PathVariable String username,
            @RequestBody(required = false) PasswordResetRequest request
    ) {
        systemAdminQueryService.resetUserPassword(username, request == null ? null : request.newPassword());
        return ApiResponse.ok();
    }

    private String auditsCsv(List<SystemAuditLogResponse> rows) {
        StringBuilder csv = new StringBuilder();
        csv.append("\u5ba1\u8ba1\u53f7,\u65f6\u95f4,\u64cd\u4f5c\u4eba,\u6a21\u5757,\u6a21\u5757\u7f16\u7801,\u52a8\u4f5c,\u52a8\u4f5c\u7f16\u7801,\u5bf9\u8c61,\u5bf9\u8c61\u7c7b\u578b,\u5bf9\u8c61\u7f16\u7801,\u6458\u8981,\u9a8c\u6536\u53f7,\u5173\u952e\u5b57,\u5bfc\u51fa\u7c7b\u578b,\u5f00\u59cb\u65e5\u671f,\u7ed3\u675f\u65e5\u671f,\u9650\u5236\u6761\u6570,\u672c\u6b21\u6761\u6570,\u5f85\u5904\u7406,\u5df2\u95ed\u73af,\u961f\u5217\u5feb\u7167,\u8bc1\u636e\u6587\u4ef6").append('\n');
        for (SystemAuditLogResponse row : rows) {
            Map<String, String> scope = auditScopeValues(row.summary());
            csv.append(csv(row.id())).append(',')
                    .append(csv(row.createdAt())).append(',')
                    .append(csv(row.operator())).append(',')
                    .append(csv(auditModuleText(row.module()))).append(',')
                    .append(csv(row.module())).append(',')
                    .append(csv(auditActionText(row.action()))).append(',')
                    .append(csv(row.action())).append(',')
                    .append(csv(auditTargetText(row))).append(',')
                    .append(csv(row.targetType())).append(',')
                    .append(csv(row.targetCode())).append(',')
                    .append(csv(row.summary())).append(',')
                    .append(csv(scope.get("acceptanceNo"))).append(',')
                    .append(csv(scope.get("keyword"))).append(',')
                    .append(csv(auditScopeExportTypeText(scope.get("exportType")))).append(',')
                    .append(csv(scope.get("exportedFrom"))).append(',')
                    .append(csv(scope.get("exportedTo"))).append(',')
                    .append(csv(scope.get("limit"))).append(',')
                    .append(csv(scope.get("count"))).append(',')
                    .append(csv(scope.get("pending"))).append(',')
                    .append(csv(scope.get("closed"))).append(',')
                    .append(csv(scope.get("rows"))).append(',')
                    .append(csv(scope.get("evidence"))).append('\n');
        }
        return csv.toString();
    }

    private Map<String, String> auditScopeValues(String summary) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String part : (summary == null ? "" : summary).split(",")) {
            String[] pieces = part.trim().split("=", 2);
            String key = pieces.length > 0 ? pieces[0].trim() : "";
            String value = pieces.length > 1 ? pieces[1].trim() : "";
            if (key.isBlank()) {
                continue;
            }
            if ("scope".equals(key)) {
                String[] scoped = value.split("=", 2);
                if (scoped.length == 2 && !scoped[0].isBlank()) {
                    values.put(scoped[0].trim(), scoped[1].trim());
                } else {
                    values.put("scope", value);
                }
            } else {
                values.put(key, value);
            }
        }
        return values;
    }

    private String auditScopeExportTypeText(String exportType) {
        return switch (exportType == null ? "" : exportType) {
            case "OVERVIEW" -> "\u4ea4\u4ed8\u603b\u89c8";
            case "PACKAGE" -> "\u9a8c\u6536\u5305";
            default -> exportType;
        };
    }

    private String auditModuleText(String module) {
        return switch (module == null ? "" : module) {
            case "system" -> "\u7cfb\u7edf\u7ba1\u7406";
            case "workbench" -> "\u5de5\u8d44\u4e1a\u52a1";
            case "salary-config" -> "\u5de5\u8d44\u9879\u914d\u7f6e";
            case "\u8d26\u6237\u5b89\u5168" -> "\u8d26\u6237\u5b89\u5168";
            default -> module == null || module.isBlank() ? "-" : module;
        };
    }

    private String auditActionText(String action) {
        return switch (action == null ? "" : action) {
            case "history-write-plans-csv" -> "\u5bfc\u51fa\u5386\u53f2\u5199\u5165\u8ba1\u5212";
            case "history-write-batch-ledger-csv" -> "\u5bfc\u51fa\u5386\u53f2\u5199\u5165\u6279\u6b21\u53f0\u8d26";
            case "history-write-batch-audits-csv" -> "\u5bfc\u51fa\u5386\u53f2\u5199\u5165\u6279\u6b21\u6d41\u6c34";
            case "history-write-report-batch-queue" -> "\u6253\u5370\u6279\u6b21\u9001\u5165\u5386\u53f2\u5199\u5165\u961f\u5217";
            case "history-write-rollback-preview-csv" -> "\u5bfc\u51fa\u64a4\u9500\u9884\u68c0";
            case "history-write-closure-acceptance-package" -> "\u5bfc\u51fa\u5386\u53f2\u5199\u5165\u95ed\u73af\u9a8c\u6536\u5305";
            case "history-write-delivery-overview-csv" -> "\u5bfc\u51fa\u5386\u53f2\u5199\u5165\u4ea4\u4ed8\u603b\u89c8";
            case "salary-migration-closure-checklist-csv" -> "\u5bfc\u51fa\u5de5\u8d44\u8fc1\u79fb\u95ed\u73af\u603b\u6e05\u5355";
            case "salary-migration-delivery-package" -> "\u5bfc\u51fa\u5de5\u8d44\u8fc1\u79fb\u603b\u4ea4\u4ed8\u5305";
            case "history-write-delivery-acceptance-index-csv" -> "\u5bfc\u51fa\u5386\u53f2\u5199\u5165\u4ea4\u4ed8\u5f52\u6863\u7d22\u5f15";
            case "history-write-delivery-acceptance-detail-csv" -> "\u5bfc\u51fa\u5386\u53f2\u5199\u5165\u4ea4\u4ed8\u8be6\u60c5";
            case "history-write-delivery-acceptance-print" -> "\u6253\u5370\u5386\u53f2\u5199\u5165\u4ea4\u4ed8\u786e\u8ba4\u5355";
            case "history-write-delivery-acceptance-batch-print" -> "\u6279\u91cf\u6253\u5370\u5386\u53f2\u5199\u5165\u4ea4\u4ed8\u786e\u8ba4\u5355";
            case "salary-case-approval-print" -> "\u6253\u5370\u5ba1\u6279\u8868";
            case "salary-case-approvals-print" -> "\u6279\u91cf\u6253\u5370\u5ba1\u6279\u8868";
            case "salary-case-approvals-reprint" -> "\u91cd\u6253\u6279\u91cf\u5ba1\u6279\u8868";
            case "salary-case-approval-roster-print" -> "\u6253\u5370\u5ba1\u6279\u6e05\u518c";
            case "salary-case-approval-roster-csv" -> "\u5bfc\u51fa\u5ba1\u6279\u6e05\u518c";
            case "salary-history-print" -> "\u6253\u5370\u5386\u53f2\u660e\u7ec6";
            case "salary-history-csv" -> "\u5bfc\u51fa\u5386\u53f2\u660e\u7ec6";
            case "salary-change-ledger-print" -> "\u6253\u5370\u53d8\u52a8\u53f0\u8d26";
            case "salary-change-ledger-csv" -> "\u5bfc\u51fa\u53d8\u52a8\u53f0\u8d26";
            case "report-print-archive-csv" -> "\u5bfc\u51fa\u5ba1\u6279\u8868\u5f52\u6863\u53f0\u8d26";
            case "report-print-batch-csv" -> "\u5bfc\u51fa\u6253\u5370\u6279\u6b21\u660e\u7ec6";
            case "report-print-batch-acceptance-package" -> "\u5bfc\u51fa\u6253\u5370\u6279\u6b21\u9a8c\u6536\u5305";
            case "report-print-batch-acceptance-package-bulk" -> "\u6279\u91cf\u5bfc\u51fa\u6253\u5370\u6279\u6b21\u9a8c\u6536\u5305";
            case "report-print-self-check-csv" -> "\u5bfc\u51fa\u62a5\u8868\u6253\u5370\u81ea\u68c0\u9a8c\u6536\u5355";
            case "report-migration-guide-csv" -> "\u5bfc\u51fa\u62a5\u8868\u6253\u5370\u8fc1\u79fb\u8bf4\u660e";
            case "report-migration-delivery-package" -> "\u5bfc\u51fa\u62a5\u8868\u6253\u5370\u8fc1\u79fb\u4ea4\u4ed8\u5305";
            case "history-write-batch-execute" -> "\u6279\u91cf\u5199\u5165\u5386\u53f2";
            case "history-write-selected-execute" -> "\u9009\u4e2d\u5199\u5165\u5386\u53f2";
            case "history-write-batch-rollback" -> "\u6279\u91cf\u64a4\u9500\u5199\u5165";
            case "history-write-selected-rollback" -> "\u9009\u4e2d\u64a4\u9500\u5199\u5165";
            case "history-write-batch-safety-preview" -> "\u6279\u91cf\u5199\u5165\u5b89\u5168\u9884\u68c0";
            case "history-write-batch-safety-consume" -> "\u6279\u91cf\u5199\u5165\u5b89\u5168\u786e\u8ba4";
            case "history-write-batch-rollback-safety-preview" -> "\u6279\u91cf\u64a4\u9500\u5b89\u5168\u9884\u68c0";
            case "history-write-batch-rollback-safety-consume" -> "\u6279\u91cf\u64a4\u9500\u5b89\u5168\u786e\u8ba4";
            default -> action == null || action.isBlank() ? "-" : action;
        };
    }

    private String auditTargetText(SystemAuditLogResponse row) {
        String type = row.targetType() == null ? "" : row.targetType();
        String code = row.targetCode() == null ? "" : row.targetCode();
        return switch (type) {
            case "HISTORY_WRITE_BATCH_SAFETY" -> "\u6279\u91cf\u5b89\u5168\u786e\u8ba4:" + (code.isBlank() ? "-" : code);
            case "HISTORY_WRITE_PLAN" -> "\u5386\u53f2\u5199\u5165\u8ba1\u5212:" + (code.isBlank() ? "-" : code);
            case "HISTORY_WRITE_BATCH_LEDGER" -> "\u5386\u53f2\u5199\u5165\u6279\u6b21\u53f0\u8d26:" + (code.isBlank() ? "-" : code);
            case "HISTORY_WRITE_BATCH" -> "\u5386\u53f2\u5199\u5165\u6279\u6b21:" + (code.isBlank() ? "-" : code);
            case "HISTORY_WRITE_ACCEPTANCE" -> "\u5386\u53f2\u5199\u5165\u95ed\u73af\u9a8c\u6536:" + (code.isBlank() ? "-" : code);
            case "HISTORY_WRITE_DELIVERY" -> "\u5386\u53f2\u5199\u5165\u4ea4\u4ed8:" + (code.isBlank() ? "-" : code);
            case "HISTORY_WRITE_DELIVERY_ACCEPTANCE" -> "\u5386\u53f2\u5199\u5165\u4ea4\u4ed8\u9a8c\u6536:" + (code.isBlank() ? "-" : code);
            case "SALARY_CASE" -> "\u5de5\u8d44\u4e1a\u52a1:" + (code.isBlank() ? "-" : code);
            default -> type.isBlank() && code.isBlank() ? "-" : String.join(":", type, code);
        };
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
}
