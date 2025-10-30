// com/opes/account/web/dto/analytics/TransactionsPageResponse.java
package com.opes.account.web.dto.analytics;

import java.util.List;

public record TransactionsPageResponse(
        List<Tx> data,
        int page,
        int pageSize,
        String nextPageToken // per MVP null
) {
    public record Tx(String transactionId, String date, String amount, String description) {}
}
