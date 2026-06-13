package com.dx.rsgzgl.person.service.impl;

import com.dx.rsgzgl.common.api.PageResponse;
import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.person.dto.PersonDetail;
import com.dx.rsgzgl.person.dto.PersonSummary;
import com.dx.rsgzgl.person.mapper.LegacyPersonMapper;
import com.dx.rsgzgl.person.service.PersonQueryService;
import com.dx.rsgzgl.system.service.OrganizationAccessService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class LegacyPersonQueryService implements PersonQueryService {

    private static final long MAX_PAGE_SIZE = 200;

    private final LegacyPersonMapper legacyPersonMapper;
    private final OrganizationAccessService organizationAccessService;

    public LegacyPersonQueryService(
            LegacyPersonMapper legacyPersonMapper,
            OrganizationAccessService organizationAccessService
    ) {
        this.legacyPersonMapper = legacyPersonMapper;
        this.organizationAccessService = organizationAccessService;
    }

    @Override
    public PageResponse<PersonSummary> page(long page, long size, String keyword, String orgCode) {
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        long offset = (safePage - 1) * safeSize;
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        String normalizedOrgCode = StringUtils.hasText(orgCode) ? orgCode.trim() : null;
        boolean fullAccess = organizationAccessService.hasFullAccess();
        List<String> allowedOrgCodes = organizationAccessService.allowedOrgCodes();
        if (StringUtils.hasText(normalizedOrgCode)) {
            organizationAccessService.requireOrgAccess(normalizedOrgCode);
        }
        return PageResponse.of(
                legacyPersonMapper.findPage(normalizedKeyword, normalizedOrgCode, fullAccess, allowedOrgCodes, offset, safeSize),
                legacyPersonMapper.count(normalizedKeyword, normalizedOrgCode, fullAccess, allowedOrgCodes),
                safePage,
                safeSize
        );
    }

    @Override
    public PersonDetail detail(String personCode) {
        PersonDetail detail = legacyPersonMapper.findByPersonCode(personCode)
                .orElseThrow(() -> new BusinessException("PERSON_NOT_FOUND", "Person not found: " + personCode));
        organizationAccessService.requireOrgAccess(detail.orgCode());
        return detail;
    }
}
