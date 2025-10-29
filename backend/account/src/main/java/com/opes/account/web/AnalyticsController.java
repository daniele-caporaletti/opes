// com/opes/account/web/AnalyticsController.java
package com.opes.account.web;

import com.opes.account.service.AnalyticsAggregationService;
import com.opes.account.service.AnalyticsAggregationService.GroupBy;
import com.opes.account.service.AnalyticsAggregationService.Sort;
import com.opes.account.service.AnalyticsAggregationService.TxType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private static final ZoneId ZONE = ZoneId.of("Europe/Rome");

    private final AnalyticsAggregationService analytics;

    public AnalyticsController(AnalyticsAggregationService analytics) {
        this.analytics = analytics;
    }

    /**
     * Drill-down aggregazioni transazioni.
     *
     * Esempi:
     *  GET /analytics/transactions/aggregate?userId=demo-user-123&type=EXPENSE&groupBy=CATEGORY&period=month
     *  GET /analytics/transactions/aggregate?userId=demo-user-123&type=INCOME&groupBy=MERCHANT&from=2025-10-01&to=2025-10-22&limit=20&sort=name_asc
     *
     * Parametri:
     *  - userId: obbligatorio
     *  - type: INCOME|EXPENSE (default EXPENSE)
     *  - groupBy: CATEGORY|MERCHANT|TAG (default CATEGORY)
     *  - from/to: YYYY-MM-DD (se assenti, usa 'period')
     *  - period: day|week|month|year (corrente, fino a ieri per month/year; per day è ieri; per week lun→dom con ieri come cap)
     *  - limit: default 10
     *  - offset: default 0
     *  - sort: amount_desc (default) | amount_asc | name_asc
     */
    @GetMapping("/transactions/aggregate")
    public AnalyticsAggregationService.AggregateResponse aggregate(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "type", defaultValue = "EXPENSE") TxType type,
            @RequestParam(name = "groupBy", defaultValue = "CATEGORY") GroupBy groupBy,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false)   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "period", required = false) String period,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "sort", defaultValue = "amount_desc") String sortParam
    ) {
        // Risolvi range
        LocalDate[] range = resolveRange(from, to, period);
        LocalDate start = range[0];
        LocalDate end = range[1];

        // Risolvi sort
        Sort sort = switch (sortParam.toLowerCase()) {
            case "name_asc"   -> Sort.NAME_ASC;
            case "amount_asc" -> Sort.AMOUNT_ASC;
            default           -> Sort.AMOUNT_DESC;
        };

        return analytics.aggregate(
                userId, type, groupBy, start, end,
                Math.max(1, limit),
                Math.max(0, offset),
                sort
        );
    }

    /** Se from/to non sono passati, usa 'period' (day|week|month|year). */
    private static LocalDate[] resolveRange(LocalDate from, LocalDate to, String period) {
        if (from != null && to != null) return new LocalDate[]{from, to};

        LocalDate today = LocalDate.now(ZONE);
        LocalDate end = today.minusDays(1); // coerenza con Home
        if (period == null) {
            // default: mese corrente fino a ieri
            LocalDate start = today.withDayOfMonth(1);
            if (end.isBefore(start)) end = start;
            return new LocalDate[]{start, end};
        }
        switch (period.toLowerCase()) {
            case "day" -> {
                LocalDate start = end; // ieri
                return new LocalDate[]{start, end};
            }
            case "week" -> {
                LocalDate weekStart = today.minusDays((today.getDayOfWeek().getValue() + 6) % 7);
                if (end.isBefore(weekStart)) end = weekStart;
                return new LocalDate[]{weekStart, end};
            }
            case "month" -> {
                LocalDate start = today.withDayOfMonth(1);
                if (end.isBefore(start)) end = start;
                return new LocalDate[]{start, end};
            }
            case "year" -> {
                LocalDate start = LocalDate.of(today.getYear(), 1, 1);
                if (end.isBefore(start)) end = start;
                return new LocalDate[]{start, end};
            }
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        }
    }
}
