package com.dx.rsgzgl.system.dto;

import java.util.List;

public record RoleTemplateResponse(
        String code,
        String name,
        String description,
        List<String> menuCodes
) {
}
