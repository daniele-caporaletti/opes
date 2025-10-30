// com/opes/account/web/dto/analytics/TotalBalanceResponse.java
package com.opes.account.web.dto.analytics;

import java.time.LocalDateTime;
import java.util.List;

public record TotalBalanceResponse(
        String currency,
        String total,
        List<AccountItem> accounts, // null se non richiesto
        Meta meta
) {
    public record AccountItem(
            String accountId,
            String name,
            String lastBalance,
            LocalDateTime asOf
    ) {}

    public record Meta(
            int accountCount,
            int accountsWithSnapshot,
            LocalDateTime latestSnapshotAt
    ) {}
}
