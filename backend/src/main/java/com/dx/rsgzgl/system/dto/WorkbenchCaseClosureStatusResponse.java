package com.dx.rsgzgl.system.dto;

import java.util.List;

public record WorkbenchCaseClosureStatusResponse(
        String status,
        String message,
        Integer completedSteps,
        Integer totalSteps,
        Boolean closed,
        WorkbenchCaseClosureStepResponse nextStep,
        List<WorkbenchCaseClosureActionResponse> nextActions,
        List<WorkbenchCaseClosureStepResponse> steps
) {
}
