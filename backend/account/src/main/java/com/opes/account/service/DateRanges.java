// com/opes/account/service/DateRanges.java
package com.opes.account.service;

import java.time.*;

public final class DateRanges {

    private static final ZoneId ZONE = ZoneId.of("Europe/Rome");

    private DateRanges() {}

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public static LocalDate yesterday() {
        return today().minusDays(1);
    }

    public static LocalDate firstDayOfCurrentMonth() {
        LocalDate t = today();
        return LocalDate.of(t.getYear(), t.getMonth(), 1);
    }

    public static LocalDate firstDayOfPreviousMonth() {
        LocalDate t = today().minusMonths(1);
        return LocalDate.of(t.getYear(), t.getMonth(), 1);
    }

    /** Dal lunedì al domenica della settimana corrente (ISO). */
    public static LocalDate weekStart() {
        LocalDate t = today();
        return t.minusDays((t.getDayOfWeek().getValue() + 6) % 7); // LUN=1 → 0
    }

    public static LocalDate weekEnd() {
        return weekStart().plusDays(6);
    }

    /**
     * Finestra mese corrente fino a ieri e finestra del mese precedente allineata
     * sullo stesso numero di giorni.
     */
    public static RangePair monthToDateVsPrevAligned() {
        LocalDate startCurr = firstDayOfCurrentMonth();
        LocalDate endCurr = yesterday();
        if (!endCurr.isAfter(startCurr.minusDays(1))) {
            // se è il primo del mese → niente "fino a ieri": usa fino a ieri comunque (può essere < start)
            endCurr = startCurr;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(startCurr, endCurr) + 1;
        LocalDate startPrev = firstDayOfPreviousMonth();
        LocalDate endPrev = startPrev.plusDays(days - 1);
        return new RangePair(new DateRange(startCurr, endCurr), new DateRange(startPrev, endPrev));
    }

    public record DateRange(LocalDate start, LocalDate end) {}
    public record RangePair(DateRange current, DateRange previous) {}
}
