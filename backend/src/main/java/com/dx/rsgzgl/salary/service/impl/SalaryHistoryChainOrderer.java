package com.dx.rsgzgl.salary.service.impl;

import com.dx.rsgzgl.salary.dto.SalaryHistoryLinkItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class SalaryHistoryChainOrderer {

    public List<SalaryHistoryLinkItem> orderBySidChain(List<SalaryHistoryLinkItem> rows) {
        if (rows.isEmpty()) {
            return rows;
        }

        Comparator<SalaryHistoryLinkItem> fallbackOrder = Comparator
                .comparingInt(SalaryHistoryLinkItem::year)
                .thenComparingInt(SalaryHistoryLinkItem::month)
                .thenComparing(SalaryHistoryLinkItem::totalAmount)
                .thenComparing(row -> row.changeType() == null ? "" : row.changeType())
                .thenComparing(SalaryHistoryLinkItem::id);
        List<SalaryHistoryLinkItem> sortedRows = rows.stream().sorted(fallbackOrder).toList();

        Map<String, SalaryHistoryLinkItem> byId = new HashMap<>();
        Set<String> referencedNextIds = new HashSet<>();
        for (SalaryHistoryLinkItem row : sortedRows) {
            byId.put(row.id(), row);
        }
        for (SalaryHistoryLinkItem row : sortedRows) {
            if (hasLinkedId(row.nextId()) && byId.containsKey(row.nextId())) {
                referencedNextIds.add(row.nextId());
            }
        }

        List<SalaryHistoryLinkItem> heads = sortedRows.stream()
                .filter(row -> !referencedNextIds.contains(row.id()))
                .toList();
        if (heads.isEmpty()) {
            heads = List.of(sortedRows.get(0));
        }

        List<SalaryHistoryLinkItem> ordered = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        for (SalaryHistoryLinkItem head : heads) {
            appendChain(head, byId, visited, ordered);
        }
        for (SalaryHistoryLinkItem row : sortedRows) {
            if (!visited.contains(row.id())) {
                appendChain(row, byId, visited, ordered);
            }
        }
        return ordered;
    }

    private void appendChain(
            SalaryHistoryLinkItem start,
            Map<String, SalaryHistoryLinkItem> byId,
            Set<String> visited,
            List<SalaryHistoryLinkItem> ordered
    ) {
        SalaryHistoryLinkItem current = start;
        while (current != null && visited.add(current.id())) {
            ordered.add(current);
            current = hasLinkedId(current.nextId()) ? byId.get(current.nextId()) : null;
        }
    }

    private boolean hasLinkedId(String value) {
        return value != null && !value.isBlank();
    }
}
