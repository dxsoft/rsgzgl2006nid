package com.dx.rsgzgl.system.dto;

import java.util.List;

public record MenuItemResponse(
        String code,
        String title,
        String icon,
        String view,
        int sequence,
        List<MenuItemResponse> children
) {
}
