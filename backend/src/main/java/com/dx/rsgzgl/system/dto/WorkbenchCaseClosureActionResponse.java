package com.dx.rsgzgl.system.dto;

public record WorkbenchCaseClosureActionResponse(
        String code,
        String label,
        String target,
        Boolean primary
) {
}
