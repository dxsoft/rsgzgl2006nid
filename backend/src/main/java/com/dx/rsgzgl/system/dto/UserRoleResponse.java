package com.dx.rsgzgl.system.dto;

import java.util.List;

public record UserRoleResponse(
        String username,
        String displayName,
        String status,
        List<String> roleCodes,
        List<String> orgCodes
) {
}
