// com/opes/account/service/AnalyticsService.java
package com.opes.account.service;

import com.opes.account.domain.entity.account.Account;
import com.opes.account.domain.entity.account.AccountBalanceSnapshot;
import com.opes.account.repository.AccountBalanceSnapshotRepository;
import com.opes.account.repository.AccountRepository;
import com.opes.account.web.dto.analytics.TotalBalanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AccountRepository accountRepo;
    private final AccountBalanceSnapshotRepository snapshotRepo;

    public TotalBalanceResponse totalBalance(String userId, boolean expandAccounts) {
        List<Account> accounts = accountRepo.findByUser_IdAndActiveTrue(userId);

        if (accounts.isEmpty()) {
            return new TotalBalanceResponse(
                    "EUR",
                    "0.00",
                    expandAccounts ? List.of() : null,
                    new TotalBalanceResponse.Meta(0, 0, null)
            );
        }

        Map<UUID, Account> byId = accounts.stream()
                .collect(Collectors.toMap(Account::getId, a -> a));

        List<AccountBalanceSnapshot> latest = snapshotRepo.findLatestByAccountIds(
                accounts.stream().map(Account::getId).toList()
        );

        BigDecimal total = BigDecimal.ZERO;
        int withSnapshot = 0;
        LocalDateTime latestAt = null;

        List<TotalBalanceResponse.AccountItem> items = new ArrayList<>();

        for (AccountBalanceSnapshot s : latest) {
            withSnapshot++;
            total = total.add(s.getBalance());
            if (latestAt == null || s.getAsOf().isAfter(latestAt)) latestAt = s.getAsOf();

            if (expandAccounts) {
                Account a = byId.get(s.getAccount().getId());
                items.add(new TotalBalanceResponse.AccountItem(
                        a.getId().toString(),
                        a.getName(),
                        s.getBalance().setScale(2).toPlainString(),
                        s.getAsOf()
                ));
            }
        }

        items.sort(Comparator.comparing(TotalBalanceResponse.AccountItem::name));

        return new TotalBalanceResponse(
                "EUR",
                total.setScale(2).toPlainString(),
                expandAccounts ? items : null,
                new TotalBalanceResponse.Meta(accounts.size(), withSnapshot, latestAt)
        );
    }
}
