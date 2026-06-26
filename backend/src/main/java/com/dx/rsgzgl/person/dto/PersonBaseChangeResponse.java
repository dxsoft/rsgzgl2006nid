package com.dx.rsgzgl.person.dto;

public record PersonBaseChangeResponse(
        Long id,
        String personCode,
        String personName,
        String orgCode,
        String dataType,
        Integer changeYear,
        Integer changeMonth,
        String sourceTable,
        String sourceId,
        String summary,
        String createdBy,
        String createdAt
) {
}
