// com/opes/account/web/SpendingSnapshotController.java
package com.opes.account.web;

import com.opes.account.service.SpendingSnapshotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/analytics/spending")
public class SpendingSnapshotController {

    private static final ZoneId ZONE = ZoneId.of("Europe/Rome");

    private final SpendingSnapshotService snapshotService;

    public SpendingSnapshotController(SpendingSnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    /**
     * Spending Snapshot (Top Category/Merchant/Tag + Trend).
     *
     * Esempi:
     *  GET /analytics/spending/snapshot?userId=demo-user-123            (mese corrente fino a ieri)
     *  GET /analytics/spending/snapshot?userId=demo-user-123&period=week
     *  GET /analytics/spending/snapshot?userId=demo-user-123&from=2025-10-01&to=2025-10-22
     */
    @GetMapping("/snapshot")
    public SpendingSnapshotService.Snapshot snapshot(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "period", required = false) String period
    ) {
        LocalDate[] range = resolveRange(from, to, period);
        LocalDate start = range[0];
        LocalDate end = range[1];

        // Se coincide col mese corrente MTD, usa la scorciatoia
        LocalDate today = LocalDate.now(ZONE);
        if (start.equals(today.withDayOfMonth(1)) && end.equals(today.minusDays(1))) {
            return snapshotService.getMonthToDate(userId);
        }
        return snapshotService.compute(userId, start, end);
    }

    /** Se from/to non sono passati, usa 'period' (day|week|month|year). */
    private static LocalDate[] resolveRange(LocalDate from, LocalDate to, String period) {
        if (from != null && to != null) {
            if (to.isBefore(from)) to = from;
            return new LocalDate[]{from, to};
        }
        LocalDate today = LocalDate.now(ZONE);
        LocalDate end = today.minusDays(1);
        if (period == null) {
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
