package com.dx.rsgzgl.person.dto;

public record PersonBaseStatusResponse(
        String personCode,
        String personName,
        String orgCode,
        long postCount,
        long educationCount,
        long assessmentCount,
        String latestChangeType,
        String latestChangeSummary,
        String latestChangeAt,
        String todoCacheStatus,
        String todoCacheRefreshedAt,
        String todoCacheDirtyAt
) {
}
