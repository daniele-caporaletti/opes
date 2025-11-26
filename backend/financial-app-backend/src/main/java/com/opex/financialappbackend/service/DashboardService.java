package com.opex.financialappbackend.service;

import com.opex.financialappbackend.domain.Account;
import com.opex.financialappbackend.domain.Transaction;
import com.opex.financialappbackend.domain.enums.DashboardPeriod;
import com.opex.financialappbackend.domain.enums.TransactionCategory;
import com.opex.financialappbackend.domain.enums.TransactionType;
import com.opex.financialappbackend.dto.dashboard.*;
import com.opex.financialappbackend.dto.transaction.TransactionDto;
import com.opex.financialappbackend.repository.AccountRepository;
import com.opex.financialappbackend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DashboardDto getDashboardData(String userId, DashboardPeriod period) {
        LocalDate now = LocalDate.now();
        
        // 1. Setup Date
        LocalDate currentStart = now.minusDays(period.getDays());
        LocalDate previousEnd = currentStart;
        LocalDate previousStart = previousEnd.minusDays(period.getDays());

        // 2. Total Balance & Trend
        BigDecimal currentBalance = accountRepository.findByUserId(userId).stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netFlowCurrent = orZero(transactionRepository.sumNetFlowSince(userId, currentStart));
        BigDecimal balanceTrendPct = calculateTrend(currentBalance, currentBalance.subtract(netFlowCurrent));

        // 3. Income & Trend
        BigDecimal currentIncome = orZero(transactionRepository.sumTotalByUserIdAndTypeAndDateRange(userId, TransactionType.INCOME, currentStart, now));
        BigDecimal prevIncome = orZero(transactionRepository.sumTotalByUserIdAndTypeAndDateRange(userId, TransactionType.INCOME, previousStart, previousEnd));
        BigDecimal incomeTrendPct = calculateTrend(currentIncome, prevIncome);

        // 4. Expense & Trend
        BigDecimal currentExpense = orZero(transactionRepository.sumTotalByUserIdAndTypeAndDateRange(userId, TransactionType.EXPENSE, currentStart, now));
        BigDecimal prevExpense = orZero(transactionRepository.sumTotalByUserIdAndTypeAndDateRange(userId, TransactionType.EXPENSE, previousStart, previousEnd));
        BigDecimal expenseTrendPct = calculateTrend(currentExpense, prevExpense);

        // 5. Widget Complessi
        WeeklySummaryDto weeklySummary = generateWeeklySummary(userId, now);
        SmartMessageDto smartMessage = generateSmartMessage(userId, now, period);
        SnapshotDto snapshot = generateSnapshot(userId, now, period);

        return new DashboardDto(
                currentBalance, "EUR", balanceTrendPct,
                currentIncome, incomeTrendPct,
                currentExpense, expenseTrendPct,
                smartMessage, weeklySummary, snapshot
        );
    }

    private WeeklySummaryDto generateWeeklySummary(String userId, LocalDate now) {
        LocalDate endPeriod = now;
        LocalDate startPeriod = now.minusDays(6);
        List<Transaction> periodTxs = transactionRepository.findByUserIdAndBookingDateBetween(userId, startPeriod, endPeriod);
        Map<LocalDate, List<Transaction>> txsByDate = periodTxs.stream().collect(Collectors.groupingBy(Transaction::getBookingDate));

        List<DailySummaryDto> days = new ArrayList<>();
        BigDecimal totalInc = BigDecimal.ZERO;
        BigDecimal totalExp = BigDecimal.ZERO;

        for (int i = 0; i < 7; i++) {
            LocalDate date = startPeriod.plusDays(i);
            List<Transaction> dailyTxs = txsByDate.getOrDefault(date, new ArrayList<>());
            
            BigDecimal dInc = BigDecimal.ZERO;
            BigDecimal dExp = BigDecimal.ZERO;
            List<TransactionDto> txDtos = new ArrayList<>();

            for (Transaction t : dailyTxs) {
                txDtos.add(new TransactionDto(t.getId(), t.getMerchantName(), t.getAmount(), t.getCategory().name(), t.getType().name(), t.getStatus().name()));
                if (t.getAmount().compareTo(BigDecimal.ZERO) > 0) dInc = dInc.add(t.getAmount());
                else dExp = dExp.add(t.getAmount());
            }
            totalInc = totalInc.add(dInc);
            totalExp = totalExp.add(dExp);
            
            days.add(new DailySummaryDto(date, date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH), dInc, dExp, txDtos));
        }
        return new WeeklySummaryDto(totalInc, totalExp, days);
    }

    // --- Helpers Privati ---
    private BigDecimal orZero(BigDecimal val) { return val != null ? val : BigDecimal.ZERO; }

    private BigDecimal calculateTrend(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) return current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : new BigDecimal(100);
        return current.subtract(previous).divide(previous.abs(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
    }

    private SmartMessageDto generateSmartMessage(String userId, LocalDate now, DashboardPeriod period) {
        // (Logica invarata dal tuo codice precedente, corretta)
        // ... (Omessa per brevità, usa la tua versione) ...
        return new SmartMessageDto("Spending is stable", "NEUTRAL"); // Placeholder per compilazione
    }

    private SnapshotDto generateSnapshot(String userId, LocalDate now, DashboardPeriod period) {
        LocalDate start = now.minusDays(period.getDays());
        if (transactionRepository.countByUserIdAndBookingDateAfter(userId, start) < 5) {
            // Fallback recenti
            var items = transactionRepository.findTop3ByUserIdOrderByBookingDateDesc(userId).stream()
                .map(t -> new SnapshotItemDto(t.getMerchantName(), t.getAmount(), t.getBookingDate().toString())).toList();
            return new SnapshotDto("RECENT_TRANSACTIONS", items);
        } else {
            // Top Categorie
            var items = transactionRepository.findTopCategoriesBySpend(userId, start, now, PageRequest.of(0, 3)).stream()
                .map(row -> new SnapshotItemDto(((TransactionCategory)row[0]).name(), (BigDecimal)row[1], row[2] + " transactions")).toList();
            return new SnapshotDto("TOP_CATEGORIES", items);
        }
    }
}