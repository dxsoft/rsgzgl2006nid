package com.dx.rsgzgl.salary.controller;

import com.dx.rsgzgl.common.api.ApiResponse;
import com.dx.rsgzgl.salary.dto.SalaryFieldConfigAdminItem;
import com.dx.rsgzgl.salary.dto.SalaryFieldConfigAuditItem;
import com.dx.rsgzgl.salary.dto.SalaryFieldConfigIssue;
import com.dx.rsgzgl.salary.dto.SalaryFieldConfigItem;
import com.dx.rsgzgl.salary.dto.SalaryFieldConfigUpdateCommand;
import com.dx.rsgzgl.salary.service.impl.SalaryFieldConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/salary")
public class SalaryFieldConfigController {

    private final SalaryFieldConfigService salaryFieldConfigService;

    public SalaryFieldConfigController(SalaryFieldConfigService salaryFieldConfigService) {
        this.salaryFieldConfigService = salaryFieldConfigService;
    }

    @GetMapping("/field-configs")
    public ApiResponse<List<SalaryFieldConfigItem>> fieldConfigs(
            @RequestParam(defaultValue = "01") String category,
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(defaultValue = "01") String dwsx
    ) {
        return ApiResponse.ok(salaryFieldConfigService.effectiveConfigs(category, year, dwsx));
    }

    @GetMapping("/field-config-issues")
    public ApiResponse<List<SalaryFieldConfigIssue>> fieldConfigIssues(
            @RequestParam(defaultValue = "2026") int year
    ) {
        return ApiResponse.ok(salaryFieldConfigService.configIssues(year));
    }

    @GetMapping("/field-config-admin")
    public ApiResponse<List<SalaryFieldConfigAdminItem>> fieldConfigAdmin() {
        return ApiResponse.ok(salaryFieldConfigService.allConfigs());
    }

    @GetMapping("/field-configs/{itemCode}")
    public ApiResponse<SalaryFieldConfigAdminItem> fieldConfig(@PathVariable String itemCode) {
        return ApiResponse.ok(salaryFieldConfigService.config(itemCode));
    }

    @GetMapping("/field-configs/{itemCode}/audit")
    public ApiResponse<List<SalaryFieldConfigAuditItem>> fieldConfigAudit(@PathVariable String itemCode) {
        return ApiResponse.ok(salaryFieldConfigService.configAudit(itemCode));
    }

    @PatchMapping("/field-configs/{itemCode}")
    public ApiResponse<SalaryFieldConfigAdminItem> updateFieldConfig(
            @PathVariable String itemCode,
            @RequestBody SalaryFieldConfigUpdateCommand command
    ) {
        return ApiResponse.ok(salaryFieldConfigService.updateConfig(itemCode, command));
    }
}
