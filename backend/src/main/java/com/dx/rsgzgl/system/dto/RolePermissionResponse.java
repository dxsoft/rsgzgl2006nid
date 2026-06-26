package com.dx.rsgzgl.system.dto;

import java.util.List;

public record RolePermissionResponse(
        String code,
        String name,
        String status,
        List<String> menuCodes
) {
}
