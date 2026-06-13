package com.dx.rsgzgl.system.dto;

import java.util.List;

public record UserCreateRequest(String username, String displayName, List<String> roleCodes, List<String> orgCodes) {
}
