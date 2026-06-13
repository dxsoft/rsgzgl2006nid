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
import java.util.List;

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
            @RequestParam(defaultValue = "") String targetCode,
            @RequestParam(defaultValue = "") String start,
            @RequestParam(defaultValue = "") String end,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(systemAuditService.latest(module, operator, targetCode, start, end, limit));
    }

    @GetMapping(value = "/audits.csv", produces = "text/csv")
    public ResponseEntity<byte[]> auditsCsv(
            @RequestParam(defaultValue = "") String module,
            @RequestParam(defaultValue = "") String operator,
            @RequestParam(defaultValue = "") String targetCode,
            @RequestParam(defaultValue = "") String start,
            @RequestParam(defaultValue = "") String end,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        List<SystemAuditLogResponse> rows = systemAuditService.latest(module, operator, targetCode, start, end, limit);
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
        csv.append("\u65f6\u95f4,\u64cd\u4f5c\u4eba,\u6a21\u5757,\u52a8\u4f5c,\u5bf9\u8c61\u7c7b\u578b,\u5bf9\u8c61\u7f16\u7801,\u6458\u8981").append('\n');
        for (SystemAuditLogResponse row : rows) {
            csv.append(csv(row.createdAt())).append(',')
                    .append(csv(row.operator())).append(',')
                    .append(csv(row.module())).append(',')
                    .append(csv(row.action())).append(',')
                    .append(csv(row.targetType())).append(',')
                    .append(csv(row.targetCode())).append(',')
                    .append(csv(row.summary())).append('\n');
        }
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
}
