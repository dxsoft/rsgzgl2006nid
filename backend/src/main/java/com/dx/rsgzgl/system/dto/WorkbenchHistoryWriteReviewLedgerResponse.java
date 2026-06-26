package com.dx.rsgzgl.system.dto;

import java.util.List;

public record WorkbenchHistoryWriteReviewLedgerResponse(
        Integer total,
        Integer pending,
        Integer reviewed,
        Integer matched,
        Integer mismatched,
        Integer retested,
        Integer retestMatched,
        Integer retestMismatched,
        Integer suggestedReviewed,
        Integer retestReviewed,
        Integer manualReviewed,
        Integer specialReviewed,
        Integer blockedReviewed,
        Integer pendingRetestFirst,
        Integer pendingMaintainAndRetest,
        Integer highPriority,
        Integer mediumPriority,
        Integer donePriority,
        List<Group> byOrg,
        List<Group> byBusinessType,
        List<Group> byReviewStatus,
        List<Group> byReviewCategory,
        List<Group> byRetestStatus,
        List<Group> byReviewSource,
        List<Group> byMaintenanceTarget,
        List<Group> byPriority,
        List<Group> byNextAction,
        List<FieldGroup> topMismatchFields
) {
    public record Group(
            String key,
            String title,
            Integer count,
            Integer pending,
            Integer reviewed
    ) {
    }

    public record FieldGroup(
            String itemCode,
            String itemName,
            String historyField,
            Integer count
    ) {
    }
}
