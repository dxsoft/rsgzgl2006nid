package com.dx.rsgzgl.system.dto;

public record SystemAuditLogResponse(
        String id,
        String module,
        String action,
        String targetType,
        String targetCode,
        String summary,
        String operator,
        String createdAt
) {
}
