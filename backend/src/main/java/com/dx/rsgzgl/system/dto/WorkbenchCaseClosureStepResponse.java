package com.dx.rsgzgl.system.dto;

public record WorkbenchCaseClosureStepResponse(
        String code,
        String label,
        String status,
        String message,
        Boolean required,
        Boolean completed
) {
}
