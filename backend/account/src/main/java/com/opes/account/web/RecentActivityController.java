// com/opes/account/web/RecentActivityController.java
package com.opes.account.web;

import com.opes.account.service.RecentActivityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/activity")
public class RecentActivityController {

    private static final ZoneId ZONE = ZoneId.of("Europe/Rome");

    private final RecentActivityService recentService;

    public RecentActivityController(RecentActivityService recentService) {
        this.recentService = recentService;
    }

    /**
     * Ultime N transazioni in base al periodo:
     *  - default: mese corrente fino a ieri (limit=10)
     *  - oppure from/to (YYYY-MM-DD)
     *  - oppure period=day|week|month|year (corrente, clamp a ieri per month/year)
     *
     * userId obbligatorio.
     */
    @GetMapping("/recent")
    public RecentActivityService.RecentActivity recent(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "period", required = false) String period
    ) {
        LocalDate[] range = resolveRange(from, to, period);
        LocalDate start = range[0];
        LocalDate end = range[1];

        // Scorciatoia: mese-to-date
        LocalDate today = LocalDate.now(ZONE);
        if (start.equals(today.withDayOfMonth(1)) && end.equals(today.minusDays(1))) {
            return recentService.getMonthToDate(userId, Math.max(1, limit));
        }
        return recentService.getInRange(userId, start, end, Math.max(1, limit));
    }

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
