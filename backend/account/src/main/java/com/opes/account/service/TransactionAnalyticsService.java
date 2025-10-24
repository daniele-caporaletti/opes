// com/opes/account/service/TransactionAnalyticsService.java
package com.opes.account.service;

import com.opes.account.domain.entity.taxonomy.Category;
import com.opes.account.domain.entity.transaction.Transaction;
import com.opes.account.repository.taxonomy.CategoryRepository;
import com.opes.account.repository.transaction.TransactionRepository;
import com.opes.account.web.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TransactionAnalyticsService {

    private final TransactionRepository txRepo;
    private final CategoryRepository categoryRepo;

    public TransactionAnalyticsService(TransactionRepository txRepo, CategoryRepository categoryRepo) {
        this.txRepo = txRepo;
        this.categoryRepo = categoryRepo;
    }

    public AmountPairDTO totalsForMonthToDate(String userId, LocalDate from, LocalDate to) {
        BigDecimal income = txRepo.sumIncome(userId, from, to);
        BigDecimal expenses = txRepo.sumExpensesAbs(userId, from, to);
        return new AmountPairDTO(zeroIfNull(income), zeroIfNull(expenses));
    }

    public String headlineMoM(String userId, DateRanges.RangePair pair) {
        BigDecimal currExp = zeroIfNull(txRepo.sumExpensesAbs(userId, pair.current().start(), pair.current().end()));
        BigDecimal prevExp = zeroIfNull(txRepo.sumExpensesAbs(userId, pair.previous().start(), pair.previous().end()));

        if (prevExp.compareTo(BigDecimal.ZERO) == 0 && currExp.compareTo(BigDecimal.ZERO) == 0) {
            return "Aggiornato al volo: tutto stabile rispetto al mese scorso";
        }
        BigDecimal deltaAbs = currExp.subtract(prevExp).abs();
        BigDecimal deltaPct = prevExp.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.valueOf(100) // “infinito”: mostra comunque
                : currExp.subtract(prevExp).divide(prevExp, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        boolean noisy = deltaAbs.abs().compareTo(BigDecimal.valueOf(50)) < 0
                && deltaPct.abs().compareTo(BigDecimal.valueOf(5)) < 0;

        if (noisy) return "Aggiornato al volo: tutto stabile rispetto al mese scorso";

        String sign = currExp.compareTo(prevExp) > 0 ? "+" : "−";
        BigDecimal pctAbs = deltaPct.abs().setScale(1, java.math.RoundingMode.HALF_UP);
        return "Hai speso " + sign + pctAbs + "% rispetto al mese scorso";
    }

    public WeeklySummaryDTO weeklySummary(String userId, LocalDate weekStart, LocalDate weekEnd) {
        BigDecimal income = zeroIfNull(txRepo.sumIncome(userId, weekStart, weekEnd));
        BigDecimal expenses = zeroIfNull(txRepo.sumExpensesAbs(userId, weekStart, weekEnd));
        return new WeeklySummaryDTO(weekStart, weekEnd, income, expenses);
    }

    public SpendingSnapshotDTO spendingSnapshot(String userId) {
        LocalDate end = DateRanges.yesterday();
        LocalDate start30 = end.minusDays(29);
        int count = txRepo.countExpenses(userId, start30, end);

        if (count < 20) {
            List<Transaction> last = txRepo.findRecentExpensesForSnapshot(userId, start30, end);
            List<SpendingItemDTO> items = last.stream()
                    .sorted(Comparator.comparing(Transaction::getBookingDate).reversed())
                    .limit(3)
                    .map(t -> new SpendingItemDTO(
                            t.getMerchant() != null ? t.getMerchant().getName() :
                                    t.getCategory() != null ? t.getCategory().getName() : "Expense",
                            t.getAmount().abs(), t.getBookingDate(),
                            t.getCategory() != null ? t.getCategory().getId() : null,
                            t.getMerchant() != null ? t.getMerchant().getId() : null
                    )).toList();
            return new SpendingSnapshotDTO("LAST_EXPENSES", items);
        } else {
            // top 3 categorie nel mese corrente (fino a ieri)
            LocalDate monthStart = DateRanges.firstDayOfCurrentMonth();
            List<Object[]> agg = txRepo.aggregateExpensesByCategory(userId, monthStart, end);
            List<SpendingItemDTO> items = agg.stream().limit(3).map(row -> {
                java.util.UUID catId = (java.util.UUID) row[0];
                BigDecimal total = (BigDecimal) row[1];
                String name = "Uncategorized";
                if (catId != null) {
                    Optional<Category> c = categoryRepo.findById(catId);
                    name = c.map(Category::getName).orElse("Category");
                }
                return new SpendingItemDTO(name, total, null, catId, null);
            }).toList();
            return new SpendingSnapshotDTO("TOP_CATEGORIES", items);
        }
    }

    private static BigDecimal zeroIfNull(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
