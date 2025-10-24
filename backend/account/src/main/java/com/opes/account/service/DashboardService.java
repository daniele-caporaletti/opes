// com/opes/account/service/DashboardService.java
package com.opes.account.service;

import com.opes.account.web.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final BalanceService balanceService;
    private final TransactionAnalyticsService txService;
    private final GoalService goalService;

    public DashboardService(BalanceService balanceService,
                            TransactionAnalyticsService txService,
                            GoalService goalService) {
        this.balanceService = balanceService;
        this.txService = txService;
        this.goalService = goalService;
    }

    public DashboardSummaryDTO getSummary(String userId) {
        BigDecimal totalBalance = balanceService.computeTotalBalance(userId);

        var ranges = DateRanges.monthToDateVsPrevAligned();
        String headline = txService.headlineMoM(userId, ranges);

        var weekly = txService.weeklySummary(userId, DateRanges.weekStart(), DateRanges.weekEnd());
        var totals = txService.totalsForMonthToDate(userId, ranges.current().start(), ranges.current().end());
        var snapshot = txService.spendingSnapshot(userId);
        var goals = goalService.recap(userId);

        return new DashboardSummaryDTO(totalBalance, headline, weekly, totals, snapshot, goals);
    }
}
