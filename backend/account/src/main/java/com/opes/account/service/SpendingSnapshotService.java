// com/opes/account/service/SpendingSnapshotService.java
package com.opes.account.service;

import com.opes.account.domain.entity.UserPreference;
import com.opes.account.domain.entity.transaction.Transaction;
import com.opes.account.repository.TransactionRepository;
import com.opes.account.repository.UserPreferenceRepository;
import com.opes.account.web.dto.analytics.SpendingSnapshotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpendingSnapshotService {

    private final TransactionRepository txRepo;
    private final UserPreferenceRepository prefRepo;

    public SpendingSnapshotResponse snapshot(String userId,
                                             String periodParam,  // auto|week|month|year|custom
                                             LocalDate customFrom,
                                             LocalDate customTo,
                                             String modeParam,    // auto|last|top
                                             String groupByParam, // category|merchant|tag|account
                                             Integer limitParam,
                                             boolean expandTotals) {

        // 1) Periodo
        Period p = resolvePeriod(userId, periodParam, customFrom, customTo);

        // 2) Base set per il periodo (no transfer)
        List<Transaction> all = txRepo.findForAnalytics(userId, p.from, p.to);

        // 3) Modalità (auto/last/top)
        int txCount30d = (int) txRepo.countNotTransferInRange(userId,
                LocalDate.now().minusDays(30), LocalDate.now());

        String mode = switch (safe(modeParam, "auto")) {
            case "last", "top" -> modeParam.toLowerCase();
            default -> (txCount30d < 20 ? "last" : "top");
        };

        String groupBy = safe(groupByParam, "category");
        int limit = (limitParam == null || limitParam < 1 || limitParam > 10) ? 3 : limitParam;

        // 4) Totali (se richiesti)
        SpendingSnapshotResponse.Totals totals = null;
        if (expandTotals) {
            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expenses = BigDecimal.ZERO;
            for (Transaction t : all) {
                if (t.getAmount().signum() >= 0) income = income.add(t.getAmount());
                else expenses = expenses.add(t.getAmount());
            }
            BigDecimal net = income.add(expenses);
            totals = new SpendingSnapshotResponse.Totals(
                    scale2(income), scale2(expenses), scale2(net));
        }

        // 5) Items
        List<SpendingSnapshotResponse.Item> items =
                "last".equals(mode)
                        ? buildLastItems(all, groupBy, limit)
                        : buildTopItems(userId, p, groupBy, limit, all);

        // 6) Output
        SpendingSnapshotResponse.Period period =
                new SpendingSnapshotResponse.Period(p.type, p.from.toString(), p.to.toString());
        SpendingSnapshotResponse.Meta meta = new SpendingSnapshotResponse.Meta(txCount30d);

        return new SpendingSnapshotResponse(mode, period, totals, items, meta);
    }

    // ---------- Helpers ----------

    private record Period(String type, LocalDate from, LocalDate to) {}

    private Period resolvePeriod(String userId, String periodParam, LocalDate from, LocalDate to) {
        String effective = periodParam;
        if (effective == null || effective.isBlank() || "auto".equalsIgnoreCase(effective)) {
            effective = prefRepo.findByUser_IdAndKey(userId, "period")
                    .map(UserPreference::getValue)
                    .map(String::toLowerCase)
                    .filter(v -> Set.of("week","month","year","custom").contains(v))
                    .orElse("month");
        } else {
            effective = effective.toLowerCase();
        }

        LocalDate start;
        LocalDate end;

        switch (effective) {
            case "week" -> {
                LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
                start = monday;
                end = monday.plusDays(6);
            }
            case "year" -> {
                int y = Year.now().getValue();
                start = LocalDate.of(y, 1, 1);
                end = LocalDate.of(y, 12, 31);
            }
            case "custom" -> {
                if (from == null || to == null || to.isBefore(from)) {
                    throw new IllegalArgumentException("Periodo custom non valido (from/to)");
                }
                start = from; end = to;
            }
            default -> { // month
                LocalDate today = LocalDate.now();
                start = today.withDayOfMonth(1);
                end = today.withDayOfMonth(today.lengthOfMonth());
            }
        }
        return new Period(effective, start, end);
    }

    private List<SpendingSnapshotResponse.Item> buildLastItems(List<Transaction> all,
                                                               String groupBy,
                                                               int limit) {
        // Uscite soltanto, escludi refund
        List<Transaction> expenses = all.stream()
                .filter(t -> t.getAmount().signum() < 0)
                .filter(t -> !t.isRefund())
                .sorted(Comparator.comparing(Transaction::getBookingDate).reversed())
                .toList();

        // Conteggio per key nel periodo (per oneOff)
        Map<String, Long> countsByKey = expenses.stream()
                .collect(Collectors.groupingBy(t -> keyOf(t, groupBy), Collectors.counting()));

        return expenses.stream()
                .limit(limit)
                .map(t -> {
                    String key = keyOf(t, groupBy);
                    String label = labelOf(t, groupBy, key);
                    boolean oneOff = countsByKey.getOrDefault(key, 0L) == 1L;
                    return new SpendingSnapshotResponse.Item(
                            "transaction",
                            groupBy,
                            key,
                            label,
                            scale2(t.getAmount()),
                            t.getBookingDate().toString(),
                            null, // count
                            oneOff,
                            null, // sharePct
                            null  // trend
                    );
                })
                .toList();
    }

    private List<SpendingSnapshotResponse.Item> buildTopItems(String userId,
                                                              Period p,
                                                              String groupBy,
                                                              int limit,
                                                              List<Transaction> baseAll) {
        // Totale uscite (assoluto) per share
        java.math.BigDecimal totalExpenses = baseAll.stream()
                .filter(t -> t.getAmount().signum() < 0)
                .map(Transaction::getAmount)
                .map(java.math.BigDecimal::abs)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Aggregati correnti
        List<Object[]> currentRows = "tag".equals(groupBy)
                ? aggregateTagWithNoTag(userId, p.from, p.to, false)
                : txRepo.aggregateByKey(userId, p.from, p.to, false, groupBy);

        // Periodo precedente omologo
        Period prev = previousPeriodLike(p);
        List<Object[]> prevRows = "tag".equals(groupBy)
                ? aggregateTagWithNoTag(userId, prev.from, prev.to, false)
                : txRepo.aggregateByKey(userId, prev.from, prev.to, false, groupBy);

        // Indicizzazione previous per confronto trend
        java.util.Map<String, java.math.BigDecimal> prevMap = prevRows.stream()
                .collect(java.util.stream.Collectors.toMap(
                        r -> (String) r[0],
                        r -> (java.math.BigDecimal) r[1],
                        (a,b) -> a
                ));

        return currentRows.stream()
                .limit(limit)
                .map(r -> {
                    String key = (String) r[0];
                    java.math.BigDecimal amount = (java.math.BigDecimal) r[1];
                    long count = ((Number) r[2]).longValue();

                    Double share = totalExpenses.compareTo(java.math.BigDecimal.ZERO) == 0
                            ? 0d
                            : amount.abs().doubleValue() / totalExpenses.doubleValue();

                    java.math.BigDecimal prevAmt = prevMap.getOrDefault(key, java.math.BigDecimal.ZERO);
                    java.math.BigDecimal delta = amount.subtract(prevAmt);
                    Double pct = prevAmt.compareTo(java.math.BigDecimal.ZERO) == 0
                            ? null
                            : delta.doubleValue() / Math.abs(prevAmt.doubleValue());

                    return new SpendingSnapshotResponse.Item(
                            "bucket",
                            groupBy,
                            key,
                            key,
                            scale2(amount),
                            null,
                            count,
                            (count == 1),
                            share,
                            new SpendingSnapshotResponse.Trend(scale2(delta), pct)
                    );
                })
                .toList();
    }

    // Helper per TAG: aggiunge anche il bucket "—" (no-tag) e ordina per impatto
    private List<Object[]> aggregateTagWithNoTag(String userId, LocalDate from, LocalDate to, boolean income) {
        List<Object[]> rows = new java.util.ArrayList<>(txRepo.aggregateByTag(userId, from, to, income));
        Object[] noTag = txRepo.aggregateNoTagBucket(userId, from, to, income);
        if (noTag != null && noTag[0] != null) {
            rows.add(new Object[]{"—", noTag[0], noTag[1]});
        }
        return rows.stream()
                .sorted((a,b) -> ((java.math.BigDecimal)b[1]).abs().compareTo(((java.math.BigDecimal)a[1]).abs()))
                .toList();
    }

    private Period previousPeriodLike(Period p) {
        return switch (p.type) {
            case "week" -> new Period("week", p.from.minusWeeks(1), p.to.minusWeeks(1));
            case "year" -> new Period("year",
                    p.from.minusYears(1).withDayOfYear(1),
                    p.to.minusYears(1).withDayOfYear(p.to.minusYears(1).lengthOfYear()));
            case "custom" -> {
                long days = ChronoUnit.DAYS.between(p.from, p.to) + 1;
                LocalDate toPrev = p.from.minusDays(1);
                LocalDate fromPrev = toPrev.minusDays(days - 1);
                yield new Period("custom", fromPrev, toPrev);
            }
            default -> { // month
                LocalDate pmStart = p.from.minusMonths(1).withDayOfMonth(1);
                LocalDate pmEnd = pmStart.withDayOfMonth(pmStart.lengthOfMonth());
                yield new Period("month", pmStart, pmEnd);
            }
        };
    }

    private String safe(String v, String def) {
        return (v == null || v.isBlank()) ? def : v.toLowerCase();
    }

    private String scale2(BigDecimal v) {
        return v.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    private String keyOf(Transaction t, String groupBy) {
        return switch (groupBy) {
            case "merchant" -> t.getMerchant() != null ? t.getMerchant().getName() : "—";
            case "account"  -> t.getAccount().getName();
            case "tag"      -> (t.getTags() != null && !t.getTags().isEmpty())
                    ? t.getTags().stream().map(tag -> tag.getName()).sorted().findFirst().orElse("—")
                    : "—";
            default         -> t.getCategory() != null ? t.getCategory().getName() : "—";
        };
    }

    private String labelOf(Transaction t, String groupBy, String key) {
        // per ora label = key; qui potresti applicare formati diversi
        return key;
    }

    private record Bucket(String key, BigDecimal amount, long count) {}
    private List<Bucket> mapAgg(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new Bucket((String) r[0], (BigDecimal) r[1], ((Number) r[2]).longValue()))
                .toList();
    }

    private List<Bucket> tagAgg(List<Transaction> txs) {
        Map<String, BigDecimal> sum = new HashMap<>();
        Map<String, Long> cnt = new HashMap<>();
        txs.stream()
                .filter(t -> t.getAmount().signum() < 0)
                .forEach(t -> {
                    if (t.getTags() == null || t.getTags().isEmpty()) {
                        sum.merge("—", t.getAmount(), BigDecimal::add);
                        cnt.merge("—", 1L, Long::sum);
                    } else {
                        t.getTags().forEach(tag -> {
                            sum.merge(tag.getName(), t.getAmount(), BigDecimal::add);
                            cnt.merge(tag.getName(), 1L, Long::sum);
                        });
                    }
                });
        return sum.entrySet().stream()
                .sorted((a,b) -> b.getValue().abs().compareTo(a.getValue().abs()))
                .map(e -> new Bucket(e.getKey(), e.getValue(), cnt.getOrDefault(e.getKey(), 0L)))
                .toList();
    }
}
