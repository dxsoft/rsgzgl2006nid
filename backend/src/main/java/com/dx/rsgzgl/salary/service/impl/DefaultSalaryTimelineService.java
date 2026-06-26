package com.dx.rsgzgl.salary.service.impl;

import com.dx.rsgzgl.salary.dto.NormalGradeTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeTrialResult;
import com.dx.rsgzgl.salary.dto.SalaryHistoryLinkItem;
import com.dx.rsgzgl.salary.dto.SalaryRuleChange;
import com.dx.rsgzgl.salary.dto.SalaryTimelineItem;
import com.dx.rsgzgl.salary.dto.SalaryTimelineResult;
import com.dx.rsgzgl.salary.mapper.LegacySalaryMapper;
import com.dx.rsgzgl.salary.service.NormalGradeTrialService;
import com.dx.rsgzgl.salary.service.SalaryTimelineService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultSalaryTimelineService implements SalaryTimelineService {

    private static final String STATUS_MATCH = "MATCH";
    private static final String STATUS_DIFF = "DIFF";
    private static final String STATUS_ERROR = "ERROR";

    private final LegacySalaryMapper legacySalaryMapper;
    private final NormalGradeTrialService normalGradeTrialService;
    private final PersonCodeParser personCodeParser;
    private final SalaryHistoryChainOrderer historyChainOrderer;

    public DefaultSalaryTimelineService(
            LegacySalaryMapper legacySalaryMapper,
            NormalGradeTrialService normalGradeTrialService,
            PersonCodeParser personCodeParser,
            SalaryHistoryChainOrderer historyChainOrderer
    ) {
        this.legacySalaryMapper = legacySalaryMapper;
        this.normalGradeTrialService = normalGradeTrialService;
        this.personCodeParser = personCodeParser;
        this.historyChainOrderer = historyChainOrderer;
    }

    @Override
    public SalaryTimelineResult replay(String personCode, Integer limit) {
        PersonCodeParts parts = personCodeParser.parse(personCode);
        List<SalaryHistoryLinkItem> history = historyChainOrderer.orderBySidChain(
                legacySalaryMapper.findHistoryLinks(parts.orgCode(), parts.personNo())
        );
        int safeLimit = limit == null ? history.size() : Math.max(1, limit);
        List<SalaryTimelineItem> items = new ArrayList<>();
        int matched = 0;
        int different = 0;
        int errors = 0;
        String previousHistoryId = null;

        for (SalaryHistoryLinkItem row : history.stream().limit(safeLimit).toList()) {
            try {
                NormalGradeTrialResult trial = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                        row.personCode(),
                        parts.orgCode(),
                        row.year(),
                        row.month(),
                        row.changeType(),
                        previousHistoryId
                ));
                String status = trial.matchedExpected() ? STATUS_MATCH : STATUS_DIFF;
                if (trial.matchedExpected()) {
                    matched++;
                } else {
                    different++;
                }
                items.add(new SalaryTimelineItem(
                        row.id(),
                        row.year(),
                        row.month(),
                        row.changeType(),
                        row.totalAmount(),
                        trial.baselineHistoryId(),
                        trial.calculatedTotalAmount(),
                        trial.differenceWithExpected(),
                        trial.matchedExpected(),
                        status,
                        "",
                        trial.changes()
                ));
            } catch (RuntimeException error) {
                errors++;
                items.add(new SalaryTimelineItem(
                        row.id(),
                        row.year(),
                        row.month(),
                        row.changeType(),
                        row.totalAmount(),
                        null,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        false,
                        STATUS_ERROR,
                        error.getMessage(),
                        List.<SalaryRuleChange>of()
                ));
            }
            previousHistoryId = row.id();
        }

        return new SalaryTimelineResult(personCode, items.size(), matched, different, errors, items);
    }
}
