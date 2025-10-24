// com/opes/account/service/BalanceService.java
package com.opes.account.service;

import com.opes.account.domain.entity.account.Account;
import com.opes.account.domain.entity.account.AccountBalanceSnapshot;
import com.opes.account.repository.account.AccountBalanceSnapshotRepository;
import com.opes.account.repository.account.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BalanceService {

    private final AccountRepository accountRepository;
    private final AccountBalanceSnapshotRepository snapshotRepository;

    public BalanceService(AccountRepository accountRepository, AccountBalanceSnapshotRepository snapshotRepository) {
        this.accountRepository = accountRepository;
        this.snapshotRepository = snapshotRepository;
    }

    public BigDecimal computeTotalBalance(String userId) {
        List<Account> accounts = accountRepository.findByUserIdAndActiveTrue(userId);
        if (accounts.isEmpty()) return BigDecimal.ZERO;

        List<java.util.UUID> ids = accounts.stream().map(Account::getId).toList();
        List<AccountBalanceSnapshot> snaps = snapshotRepository.findLatestByAccountIds(ids);
        return snaps.stream()
                .map(AccountBalanceSnapshot::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
