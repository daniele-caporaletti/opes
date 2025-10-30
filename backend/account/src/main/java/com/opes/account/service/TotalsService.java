// com/opes/account/service/TotalsService.java
package com.opes.account.service;

import com.opes.account.domain.entity.transaction.Transaction;
import com.opes.account.repository.TransactionRepository;
import com.opes.account.web.dto.analytics.TotalBreakdownResponse;
import com.opes.account.web.dto.analytics.TransactionsPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TotalsService {

    private final TransactionRepository txRepo;

    // --------- PUBLIC API ---------

    public TotalBreakdownResponse totalIncome(String userId, String groupBy,
                                              LocalDate from, LocalDate to) {
        return aggregate(userId, true, groupBy, from, to);
    }

    public TotalBreakdownResponse totalExpenses(String userId, String groupBy,
                                                LocalDate from, LocalDate to) {
        return aggregate(userId, false, groupBy, from, to);
    }

    public TransactionsPageResponse detailsIncome(String userId, String groupBy, String key,
                                                  LocalDate from, LocalDate to,
                                                  String period, LocalDate anchorDate,
                                                  int page, int pageSize) {
        Range r = resolveRange(from, to, period, anchorDate);
        return details(userId, true, groupBy, key, r.from, r.to, page, pageSize);
    }

    public TransactionsPageResponse detailsExpenses(String userId, String groupBy, String key,
                                                    LocalDate from, LocalDate to,
                                                    String period, LocalDate anchorDate,
                                                    int page, int pageSize) {
        Range r = resolveRange(from, to, period, anchorDate);
        return details(userId, false, groupBy, key, r.from, r.to, page, pageSize);
    }

    // --------- CORE ---------

    private TotalBreakdownResponse aggregate(String userId, boolean income, String groupBy,
                                             LocalDate from, LocalDate to) {
        Range r = resolveRange(from, to, null, null);

        String gb = (groupBy == null || groupBy.isBlank()) ? "category" : groupBy.toLowerCase();
        List<Object[]> rows;

        if ("tag".equals(gb)) {
            // Tag: query N:M + bucket "—" (no-tag)
            rows = new java.util.ArrayList<>(txRepo.aggregateByTag(userId, r.from, r.to, income));
            Object[] noTag = txRepo.aggregateNoTagBucket(userId, r.from, r.to, income);
            if (noTag != null && noTag[0] != null) {
                // noTag[0]=sum; noTag[1]=count
                rows.add(new Object[]{"—", noTag[0], noTag[1]});
            }
            // ordina per impatto
            rows = rows.stream()
                    .sorted((a,b) -> ((java.math.BigDecimal)b[1]).abs().compareTo(((java.math.BigDecimal)a[1]).abs()))
                    .toList();
        } else {
            rows = txRepo.aggregateByKey(userId, r.from, r.to, income, gb);
        }

        java.math.BigDecimal total = rows.stream()
                .map(rw -> (java.math.BigDecimal) rw[1])
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.util.List<TotalBreakdownResponse.Row> breakdown = rows.stream()
                .map(rw -> new TotalBreakdownResponse.Row(
                        (String) rw[0],
                        scale2((java.math.BigDecimal) rw[1]),
                        ((Number) rw[2]).longValue()
                ))
                .toList();

        return new TotalBreakdownResponse("EUR", scale2(total), gb, breakdown);
    }


    private TransactionsPageResponse details(String userId, boolean income,
                                             String groupBy, String key,
                                             LocalDate from, LocalDate to,
                                             int page, int pageSize) {
        List<Transaction> base = txRepo.findForAnalytics(userId, from, to).stream()
                .filter(t -> income ? t.getAmount().signum() >= 0 : t.getAmount().signum() < 0)
                .filter(t -> matchesGroup(t, groupBy, key))
                .sorted(Comparator.comparing(Transaction::getBookingDate).reversed())
                .toList();

        int fromIdx = Math.max(0, (page - 1) * pageSize);
        int toIdx   = Math.min(base.size(), fromIdx + pageSize);
        List<Transaction> slice = fromIdx >= toIdx ? List.of() : base.subList(fromIdx, toIdx);

        List<TransactionsPageResponse.Tx> data = slice.stream()
                .map(t -> new TransactionsPageResponse.Tx(
                        t.getId().toString(),
                        t.getBookingDate().toString(),
                        scale2(t.getAmount()),
                        Optional.ofNullable(t.getDescription()).orElse("")
                ))
                .toList();

        return new TransactionsPageResponse(data, page, pageSize, null);
    }

    // --------- HELPERS ---------

    private record Range(LocalDate from, LocalDate to) {}

    /** Se from/to non specificati, default = ultimi 30 giorni (incluso oggi).
     *  Se period ∈ {day,week,month,year}, usa anchorDate (default oggi) per risolvere l'intervallo. */
    private Range resolveRange(LocalDate from, LocalDate to, String period, LocalDate anchorDate) {
        if (period != null && !period.isBlank()) {
            LocalDate anchor = (anchorDate == null) ? LocalDate.now() : anchorDate;
            return switch (period.toLowerCase()) {
                case "day" -> new Range(anchor, anchor);
                case "week" -> {
                    LocalDate start = anchor.with(java.time.DayOfWeek.MONDAY);
                    yield new Range(start, start.plusDays(6));
                }
                case "month" -> {
                    LocalDate start = anchor.withDayOfMonth(1);
                    yield new Range(start, start.withDayOfMonth(start.lengthOfMonth()));
                }
                case "year" -> {
                    LocalDate start = anchor.withDayOfYear(1);
                    yield new Range(start, anchor.withDayOfYear(anchor.lengthOfYear()));
                }
                default -> fallback30d();
            };
        }
        if (from != null && to != null) return new Range(from, to);
        return fallback30d();
    }

    private Range fallback30d() {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minus(30, ChronoUnit.DAYS);
        return new Range(from, to);
    }

    private boolean matchesGroup(Transaction t, String groupBy, String key) {
        String gb = (groupBy == null || groupBy.isBlank()) ? "category" : groupBy.toLowerCase();
        return switch (gb) {
            case "merchant" -> Objects.equals(
                    Optional.ofNullable(t.getMerchant()).map(m -> m.getName()).orElse("—"), key);
            case "account" -> Objects.equals(t.getAccount().getName(), key);
            case "provider" -> Objects.equals(t.getAccount().getProvider().name(), key);
            case "tag" -> (t.getTags() != null && t.getTags().stream().anyMatch(tag -> tag.getName().equals(key)))
                    || ("—".equals(key) && (t.getTags() == null || t.getTags().isEmpty()));
            default -> Objects.equals(
                    Optional.ofNullable(t.getCategory()).map(c -> c.getName()).orElse("—"), key);
        };
    }

    private String scale2(BigDecimal v) {
        return v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
