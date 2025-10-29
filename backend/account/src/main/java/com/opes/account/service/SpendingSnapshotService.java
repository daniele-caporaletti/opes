// com/opes/account/service/SpendingSnapshotService.java
package com.opes.account.service;

import com.opes.account.repository.taxonomy.CategoryRepository;
import com.opes.account.repository.taxonomy.MerchantRepository;
import com.opes.account.repository.taxonomy.TagRepository;
import com.opes.account.repository.transaction.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class SpendingSnapshotService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Rome");

    private final TransactionRepository txRepo;
    private final CategoryRepository categoryRepo;
    private final MerchantRepository merchantRepo;
    private final TagRepository tagRepo;

    public SpendingSnapshotService(TransactionRepository txRepo,
                                   CategoryRepository categoryRepo,
                                   MerchantRepository merchantRepo,
                                   TagRepository tagRepo) {
        this.txRepo = txRepo;
        this.categoryRepo = categoryRepo;
        this.merchantRepo = merchantRepo;
        this.tagRepo = tagRepo;
    }

    /**
     * Snapshot per la Home: mese corrente fino a ieri.
     */
    public Snapshot getMonthToDate(String userId) {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = today.minusDays(1);
        if (end.isBefore(start)) end = start; // giorno 1 del mese

        return compute(userId, start, end);
    }

    /**
     * Snapshot generico su un range (utile per viste estese).
     */
    public Snapshot compute(String userId, LocalDate start, LocalDate end) {
        // Totale uscite (denominatore per share%)
        BigDecimal totalExpenses = nz(txRepo.sumExpensesAbs(userId, start, end));

        // --- Top Category ---
        TopItem topCategory = null;
        var topCatRows = txRepo.topCategoriesByExpense(userId, start, end, PageRequest.of(0, 1));
        if (!topCatRows.isEmpty()) {
            UUID catId = (UUID) topCatRows.get(0)[0];
            BigDecimal amount = nz((BigDecimal) topCatRows.get(0)[1]);
            String label = resolveCategoryLabel(catId);
            Double pct = share(amount, totalExpenses);
            topCategory = new TopItem(catId != null ? catId.toString() : null, label, amount, pct);
        }

        // --- Top Merchant ---
        TopItem topMerchant = null;
        var topMerRows = txRepo.topMerchantsByExpense(userId, start, end, PageRequest.of(0, 1));
        if (!topMerRows.isEmpty()) {
            UUID mId = (UUID) topMerRows.get(0)[0];
            BigDecimal amount = nz((BigDecimal) topMerRows.get(0)[1]);
            String label = resolveMerchantLabel(mId);
            Double pct = share(amount, totalExpenses);
            topMerchant = new TopItem(mId != null ? mId.toString() : null, label, amount, pct);
        }

        // --- Top Tag ---
        TopItem topTag = null;
        var topTagRows = txRepo.topTagsByExpense(userId, start, end, PageRequest.of(0, 1));
        if (!topTagRows.isEmpty()) {
            UUID tId = (UUID) topTagRows.get(0)[0];
            BigDecimal amount = nz((BigDecimal) topTagRows.get(0)[1]);
            String label = resolveTagLabel(tId);
            Double pct = share(amount, totalExpenses);
            topTag = new TopItem(tId != null ? tId.toString() : null, label, amount, pct);
        }

        // --- Trend (serie giornaliera: mese corrente vs mese precedente allineato) ---
        Trend trend = buildTrend(userId, start, end);

        return new Snapshot(
                new Period(start, end),
                topCategory, topMerchant, topTag,
                trend
        );
    }

    // ----------------- Trend helpers -----------------

    private Trend buildTrend(String userId, LocalDate start, LocalDate end) {
        // Serie corrente
        var currentPoints = fillSeriesGaps(toPointMap(txRepo.dailyExpensesSeries(userId, start, end)), start, end);

        // Serie baseline: stesso numero di giorni del mese precedente
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        LocalDate prevStart = start.minusMonths(1);
        LocalDate prevEnd = prevStart.plusDays(days - 1);
        var prevPoints = fillSeriesGaps(toPointMap(txRepo.dailyExpensesSeries(userId, prevStart, prevEnd)), prevStart, prevEnd);

        // Delta
        BigDecimal currTotal = currentPoints.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal prevTotal = prevPoints.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal diff = currTotal.subtract(prevTotal);
        Double deltaPct = prevTotal.compareTo(BigDecimal.ZERO) == 0
                ? (currTotal.compareTo(BigDecimal.ZERO) == 0 ? 0d : 100d)
                : diff.divide(prevTotal, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();

        // Costruzione DTO
        List<TrendPoint> points = new ArrayList<>();
        LocalDate d = start;
        while (!d.isAfter(end)) {
            points.add(new TrendPoint(d, nz(currentPoints.get(d))));
            d = d.plusDays(1);
        }

        List<TrendPoint> baseline = new ArrayList<>();
        d = prevStart;
        while (!d.isAfter(prevEnd)) {
            baseline.add(new TrendPoint(d, nz(prevPoints.get(d))));
            d = d.plusDays(1);
        }

        return new Trend(
                new Period(start, end),
                points,
                baseline,
                diff, // deltaAbs
                round1(deltaPct) // deltaPct
        );
    }

    private static Map<LocalDate, BigDecimal> toPointMap(List<? extends Object> rows) {
        Map<LocalDate, BigDecimal> map = new HashMap<>();
        for (Object r : rows) {
            // r è una proxy che implementa l'interfaccia projection DailyExpensePoint
            var day = (LocalDate) invokeGetter(r, "getDay");
            var total = (BigDecimal) invokeGetter(r, "getTotalAbs");
            map.put(day, nz(total));
        }
        return map;
    }

    // riflette min. per non dipendere direttamente dall'interfaccia del repo in questo file
    private static Object invokeGetter(Object target, String method) {
        try { return target.getClass().getMethod(method).invoke(target); }
        catch (Exception e) { throw new IllegalStateException("Projection mapping failed", e); }
    }

    private static Map<LocalDate, BigDecimal> fillSeriesGaps(Map<LocalDate, BigDecimal> src, LocalDate start, LocalDate end) {
        Map<LocalDate, BigDecimal> res = new LinkedHashMap<>();
        LocalDate d = start;
        while (!d.isAfter(end)) {
            res.put(d, src.getOrDefault(d, BigDecimal.ZERO));
            d = d.plusDays(1);
        }
        return res;
    }

    // ----------------- Label resolvers -----------------

    private String resolveCategoryLabel(UUID id) {
        if (id == null) return "Uncategorized";
        var rows = categoryRepo.findIdAndNameByIds(Set.of(id));
        return rows.isEmpty() ? "Category" : String.valueOf(rows.get(0)[1]);
    }

    private String resolveMerchantLabel(UUID id) {
        if (id == null) return "No merchant";
        var rows = merchantRepo.findIdAndNameByIds(Set.of(id));
        return rows.isEmpty() ? "Merchant" : String.valueOf(rows.get(0)[1]);
    }

    private String resolveTagLabel(UUID id) {
        if (id == null) return "No tag";
        var rows = tagRepo.findIdAndNameByIds(Set.of(id));
        return rows.isEmpty() ? "Tag" : String.valueOf(rows.get(0)[1]);
    }

    // ----------------- utils -----------------

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private static Double share(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) return 0d;
        return part.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static double round1(Double v) {
        if (v == null) return 0d;
        return Math.round(v * 10.0) / 10.0;
    }

    // ================== DTO interni (per non “sporcare” i package web) ==================

    public record Snapshot(
            Period period,
            TopItem topCategory,
            TopItem topMerchant,
            TopItem topTag,
            Trend trend
    ) {}

    public record TopItem(
            String id,         // UUID string o null
            String label,      // risolto da repo
            BigDecimal amount, // totale assoluto speso
            Double sharePct    // % sul totale uscite periodo
    ) {}

    public record Trend(
            Period period,
            List<TrendPoint> points,        // serie corrente (giorni del periodo)
            List<TrendPoint> baselinePoints,// serie mese precedente allineata
            BigDecimal deltaAbs,            // currTotal - prevTotal
            Double deltaPct                 // in %
    ) {}

    public record TrendPoint(LocalDate date, BigDecimal expense) {}

    public record Period(LocalDate from, LocalDate to) {}
}
