// com/opes/account/service/BalanceService.java
package com.opes.account.service;

import com.opes.account.domain.entity.account.AccountBalanceSnapshot;
import com.opes.account.repository.account.AccountBalanceSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BalanceService {

    private final AccountBalanceSnapshotRepository snapshotRepo;

    public BalanceService(AccountBalanceSnapshotRepository snapshotRepo) {
        this.snapshotRepo = snapshotRepo;
    }

    /** Total Balance = somma degli ultimi snapshot per ogni account attivo (EUR, MVP). */
    public BigDecimal computeTotalBalance(String userId) {
        return snapshotRepo.sumLatestBalanceForUser(userId);
    }

    /** (Opzionale) Dettaglio per conto: ultimo snapshot per ciascun account attivo. */
    public List<AccountBalanceItem> latestPerAccount(String userId) {
        return snapshotRepo.findLatestSnapshotsForUserActiveAccounts(userId).stream()
                .map(AccountBalanceItem::from)
                .toList();
    }

    // --- DTO interno leggero ---
    public record AccountBalanceItem(
            java.util.UUID accountId,
            String accountName,
            BigDecimal balance,
            LocalDateTime asOf
    ) {
        static AccountBalanceItem from(AccountBalanceSnapshot s) {
            return new AccountBalanceItem(
                    s.getAccount().getId(),
                    s.getAccount().getName(),
                    s.getBalance(),
                    s.getAsOf()
            );
        }
    }
}
