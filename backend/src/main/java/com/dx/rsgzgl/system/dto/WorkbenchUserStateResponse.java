package com.dx.rsgzgl.system.dto;

import java.util.Map;

public record WorkbenchUserStateResponse(String stateKey, Map<String, Object> state) {
}
