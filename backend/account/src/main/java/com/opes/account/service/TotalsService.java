// com/opes/account/service/TotalsService.java
package com.opes.account.service;

import com.opes.account.repository.transaction.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@Transactional(readOnly = true)
public class TotalsService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Rome");
    private final TransactionRepository txRepo;

    public TotalsService(TransactionRepository txRepo) {
        this.txRepo = txRepo;
    }

    /** Totali del mese corrente (fino a ieri). */
    public AmountPair monthToDate(String userId) {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate from = today.withDayOfMonth(1);
        LocalDate to = today.minusDays(1);
        if (to.isBefore(from)) to = from; // primo giorno del mese
        return inRange(userId, from, to);
    }

    /** Totali in un range arbitrario. Spese come valore assoluto. */
    public AmountPair inRange(String userId, LocalDate from, LocalDate to) {
        BigDecimal income = nz(txRepo.sumIncome(userId, from, to));
        BigDecimal expenses = nz(txRepo.sumExpensesAbs(userId, from, to)); // assoluto
        return new AmountPair(income, expenses);
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    // DTO leggero
    public record AmountPair(BigDecimal income, BigDecimal expenses) {}
}
