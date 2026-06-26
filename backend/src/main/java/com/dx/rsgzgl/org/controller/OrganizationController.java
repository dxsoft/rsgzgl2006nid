package com.dx.rsgzgl.org.controller;

import com.dx.rsgzgl.common.api.ApiResponse;
import com.dx.rsgzgl.org.dto.OrganizationNode;
import com.dx.rsgzgl.org.service.OrganizationQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/org")
public class OrganizationController {

    private final OrganizationQueryService organizationQueryService;

    public OrganizationController(OrganizationQueryService organizationQueryService) {
        this.organizationQueryService = organizationQueryService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<OrganizationNode>> tree() {
        return ApiResponse.ok(organizationQueryService.tree());
    }
}
