package com.dx.rsgzgl.system.dto;

public record MenuAdminItemResponse(
        String code,
        String parentCode,
        String title,
        String icon,
        String view,
        int sequence,
        String status
) {
}
