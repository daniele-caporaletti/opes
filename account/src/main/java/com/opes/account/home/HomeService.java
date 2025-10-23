package com.opes.account.home;

import com.opes.account.entity.Goal;
import com.opes.account.entity.Transaction;
import com.opes.account.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final FinancialAccountRepository faRepo;
    private final TransactionRepository txRepo;
    private final GoalRepository goalRepo;

    private static final ZoneId TZ = ZoneId.of("Europe/Zurich");
    private record Range(Instant from, Instant to) {}

    public long totalBalanceCents(Long accountId) {
        return faRepo.sumIncludedBalance(accountId);
    }

    public Map<String, Long> weeklySummary(Long accountId) {
        var r = weekRange();
        var tx = txRepo.findByAccount_IdAndBookingTsBetween(accountId, r.from, r.to);
        long inc = tx.stream().filter(t -> t.getKind()==Transaction.Kind.INCOME).mapToLong(Transaction::getAmountCents).sum();
        long exp = tx.stream().filter(t -> t.getKind()==Transaction.Kind.EXPENSE).mapToLong(t -> -t.getAmountCents()).sum();
        return Map.of("incomeCents", inc, "expensesCents", exp);
    }

    public long totalByKindCents(Long accountId, Transaction.Kind kind, Period period) {
        var r = rangeFor(period);
        var tx = txRepo.findByAccount_IdAndKindAndBookingTsBetween(accountId, kind, r.from, r.to);
        long sum = tx.stream().mapToLong(Transaction::getAmountCents).sum();
        return (kind == Transaction.Kind.EXPENSE) ? Math.abs(sum) : sum;
    }

    public Map<String, Object> spendingSnapshot(Long accountId) {
        var to = LocalDate.now(TZ).plusDays(1).atStartOfDay(TZ).toInstant();
        var from = LocalDate.now(TZ).minusDays(30).atStartOfDay(TZ).toInstant();
        var txs = txRepo.findByAccount_IdAndBookingTsBetween(accountId, from, to).stream()
                .filter(t -> t.getKind()==Transaction.Kind.EXPENSE)
                .sorted(Comparator.comparing(Transaction::getBookingTs).reversed())
                .toList();

        if (txs.size() < 20) {
            var latest = txs.stream().limit(3).map(t -> Map.of(
                    "date", t.getBookingTs(),
                    "merchant", Optional.ofNullable(t.getMerchantName()).orElse(""),
                    "category", Optional.ofNullable(t.getCategoryCode()).orElse(""),
                    "amountCents", Math.abs(t.getAmountCents())
            )).toList();
            return Map.of("mode", "LATEST_EXPENSES", "items", latest);
        } else {
            var monthStart = LocalDate.now(TZ).withDayOfMonth(1).atStartOfDay(TZ).toInstant();
            var totals = txRepo.topCategories(accountId, monthStart, to);
            long total = totals.stream().map(m -> ((Number)m.get("total")).longValue()).mapToLong(Long::longValue).sum();
            var top3 = totals.stream().limit(3).map(m -> Map.of(
                    "category", (String)m.get("category"),
                    "totalCents", ((Number)m.get("total")).longValue(),
                    "sharePct", total == 0 ? 0.0 : Math.round(((Number)m.get("total")).longValue() * 1000.0 / total) / 10.0
            )).toList();
            return Map.of("mode", "TOP_CATEGORIES", "items", top3);
        }
    }

    public List<Map<String,Object>> activeGoals(Long accountId) {
        return goalRepo.findByAccount_IdAndStatus(accountId, Goal.Status.ACTIVE).stream()
                .map(g -> Map.<String,Object>of(
                        "title", g.getTitle(),
                        "targetCents", Long.valueOf(g.getTargetCents()),
                        "savedCents", Long.valueOf(g.getSavedCents()),
                        "progressPct", g.getTargetCents() <= 0 ? 0.0 :
                                Math.round(g.getSavedCents() * 1000.0 / g.getTargetCents()) / 10.0
                ))
                .toList();
    }

    public enum Period { DAY, WEEK, MONTH }

    private Range weekRange() {
        var today = LocalDate.now(TZ);
        var from = today.with(DayOfWeek.MONDAY).atStartOfDay(TZ).toInstant();
        var to   = today.plusDays(1).atStartOfDay(TZ).toInstant();
        return new Range(from, to);
    }
    private Range rangeFor(Period p) {
        var today = LocalDate.now(TZ);
        return switch (p) {
            case DAY -> new Range(today.atStartOfDay(TZ).toInstant(), today.plusDays(1).atStartOfDay(TZ).toInstant());
            case WEEK -> weekRange();
            case MONTH -> new Range(today.withDayOfMonth(1).atStartOfDay(TZ).toInstant(), today.plusDays(1).atStartOfDay(TZ).toInstant());
        };
    }
}
