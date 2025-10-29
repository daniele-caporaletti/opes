// com/opes/account/web/HomeController.java
package com.opes.account.web;

import com.opes.account.service.BalanceService;
import com.opes.account.service.HeadlineService;
import com.opes.account.service.RecentActivityService;
import com.opes.account.service.SpendingSnapshotService;
import com.opes.account.service.TotalsService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/home")
public class HomeController {

    private final BalanceService balanceService;
    private final HeadlineService headlineService;
    private final RecentActivityService recentActivityService;
    private final TotalsService totalsService;
    private final SpendingSnapshotService spendingSnapshotService;

    public HomeController(BalanceService balanceService,
                          HeadlineService headlineService,
                          RecentActivityService recentActivityService,
                          TotalsService totalsService,
                          SpendingSnapshotService spendingSnapshotService) {
        this.balanceService = balanceService;
        this.headlineService = headlineService;
        this.recentActivityService = recentActivityService;
        this.totalsService = totalsService;
        this.spendingSnapshotService = spendingSnapshotService;
    }

    /**
     * Home summary (MVP).
     * Richiede sempre userId come query param.
     */
    @GetMapping("/summary")
    public HomeSummaryResponse getSummary(@RequestParam(name = "userId") String userId) {

        BigDecimal totalBalance = balanceService.computeTotalBalance(userId);
        String headline = headlineService.computeDailyHeadline(userId);
        var recent = recentActivityService.getMonthToDate(userId, 10);
        var totals = totalsService.monthToDate(userId);
        var snapshot = spendingSnapshotService.getMonthToDate(userId);

        return new HomeSummaryResponse(totalBalance, headline, recent, totals, snapshot);
    }

    // DTO risposta (riusa i record dei service per evitare mapping inutile)
    public record HomeSummaryResponse(
            BigDecimal totalBalance,
            String headlineMessage,
            RecentActivityService.RecentActivity recentActivity,
            TotalsService.AmountPair totals,
            SpendingSnapshotService.Snapshot spendingSnapshot
    ) {}
}
