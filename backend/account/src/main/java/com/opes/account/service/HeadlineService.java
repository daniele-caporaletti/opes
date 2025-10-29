// com/opes/account/service/HeadlineService.java
package com.opes.account.service;

import com.opes.account.repository.transaction.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.WeekFields;

@Service
@Transactional(readOnly = true)
public class HeadlineService {

    // --- Policy (MVP) ---
    private static final ZoneId ZONE = ZoneId.of("Europe/Rome");
    private static final BigDecimal ABS_THRESHOLD_EUR = BigDecimal.valueOf(50); // soglia anti-rumore €
    private static final BigDecimal PCT_THRESHOLD = BigDecimal.valueOf(5);      // soglia anti-rumore %

    private final TransactionRepository txRepo;

    public HeadlineService(TransactionRepository txRepo) {
        this.txRepo = txRepo;
    }

    /** Restituisce l'headline del giorno secondo priorità e soglie. */
    public String computeDailyHeadline(String userId) {
        // --- range mese corrente (fino a ieri) ---
        LocalDate today = LocalDate.now(ZONE);
        LocalDate startCurr = today.withDayOfMonth(1);
        LocalDate endCurr = today.minusDays(1);
        if (endCurr.isBefore(startCurr)) endCurr = startCurr; // giorno 1

        // fallback se dati insufficienti: <10 transazioni nel mese
        long txCount = txRepo
                .findRecentInPeriodExcludingTransfers(userId, startCurr, endCurr, PageRequest.of(0, 1))
                .getTotalElements();
        if (txCount < 10) {
            return "Benvenuto! Collega un conto o aggiungi movimenti per il riepilogo";
        }

        // --- range mese precedente allineato per giorni ---
        long days = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(startCurr, endCurr) + 1);
        LocalDate startPrev = startCurr.minusMonths(1);
        LocalDate endPrev = startPrev.plusDays(days - 1);

        // ----------------- 1) Spesa MoM -----------------
        BigDecimal currExp = nz(txRepo.sumExpensesAbs(userId, startCurr, endCurr)); // abs
        BigDecimal prevExp = nz(txRepo.sumExpensesAbs(userId, startPrev, endPrev));

        if (isSignificant(currExp, prevExp)) {
            BigDecimal pct = pctChange(currExp, prevExp);
            String sign = currExp.compareTo(prevExp) >= 0 ? "+" : "−";
            return "Hai speso " + sign + fmt1(abs(pct)) + "% rispetto al mese scorso";
        }

        // ----------------- 2) Entrate MoM ----------------
        BigDecimal currInc = nz(txRepo.sumIncome(userId, startCurr, endCurr));
        BigDecimal prevInc = nz(txRepo.sumIncome(userId, startPrev, endPrev));

        if (isSignificant(currInc, prevInc)) {
            BigDecimal pct = pctChange(currInc, prevInc);
            String sign = currInc.compareTo(prevInc) >= 0 ? "+" : "−";
            return "Le tue entrate sono " + sign + fmt1(abs(pct)) + "% rispetto al mese scorso";
        }

        // ----------------- 3) Risparmio mese --------------
        BigDecimal savings = currInc.subtract(currExp); // >0 => risparmio
        if (savings.compareTo(ABS_THRESHOLD_EUR) >= 0) {
            return "Hai messo da parte €" + fmtMoney(savings) + " questo mese";
        }

        // ----------------- 4) Cash-flow positivo N settimane ----
        int streak = positiveCashflowStreakWeeks(userId, today);
        if (streak >= 2) {
            return "Cash-flow positivo da " + streak + " settimane di fila";
        }

        // ----------------- fallback neutro ----------------
        return "Aggiornato al volo: tutto stabile rispetto al mese scorso";
    }

    // ================= helpers =================

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private static BigDecimal abs(BigDecimal v) { return v.abs(); }

    private static BigDecimal fmt1(BigDecimal pct) {
        return pct.setScale(1, RoundingMode.HALF_UP);
    }

    private static String fmtMoney(BigDecimal eur) {
        return eur.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /** |Δ%| ≥ 5% oppure Δ€ ≥ 50 */
    private boolean isSignificant(BigDecimal curr, BigDecimal prev) {
        BigDecimal diffAbs = abs(curr.subtract(prev));
        if (diffAbs.compareTo(ABS_THRESHOLD_EUR) >= 0) return true;
        BigDecimal pct = pctChange(curr, prev).abs();
        return pct.compareTo(PCT_THRESHOLD) >= 0;
    }

    /** (curr-prev)/prev*100; se prev=0 → 100 se curr>0 altrimenti 0 */
    private static BigDecimal pctChange(BigDecimal curr, BigDecimal prev) {
        if (prev.compareTo(BigDecimal.ZERO) == 0) {
            return curr.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100);
        }
        return curr.subtract(prev)
                .divide(prev, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Conta le settimane consecutive più recenti (fino alla settimana in corso inclusa)
     * con cash-flow positivo (entrate ≥ uscite, esclusi trasferimenti/rimborsi).
     * Semplice, MVP: guarda al massimo 8 settimane indietro.
     */
    private int positiveCashflowStreakWeeks(String userId, LocalDate today) {
        WeekFields wf = WeekFields.ISO;
        // settimana corrente (lun-dom)
        LocalDate weekStart = today.minusDays((today.getDayOfWeek().getValue() + 6) % 7);
        LocalDate weekEnd = weekStart.plusDays(6);

        int streak = 0;
        for (int i = 0; i < 8; i++) {
            LocalDate start = weekStart.minusWeeks(i);
            LocalDate end = weekEnd.minusWeeks(i);

            BigDecimal inc = nz(txRepo.sumIncome(userId, start, end));
            BigDecimal exp = nz(txRepo.sumExpensesAbs(userId, start, end));

            if (inc.compareTo(exp) >= 0) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}
