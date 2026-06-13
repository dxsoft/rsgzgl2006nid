package com.dx.rsgzgl.person.dto;

public record PersonBaseChangeRequest(
        String dataType,
        Integer changeYear,
        Integer changeMonth,
        String sourceTable,
        String sourceId,
        String summary
) {
}
