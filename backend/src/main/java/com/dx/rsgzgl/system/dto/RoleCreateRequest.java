package com.dx.rsgzgl.system.dto;

import java.util.List;

public record RoleCreateRequest(String code, String name, List<String> menuCodes) {
}
