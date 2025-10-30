// com/opes/account/web/dto/analytics/TotalBreakdownResponse.java
package com.opes.account.web.dto.analytics;

import java.util.List;

public record TotalBreakdownResponse(
        String currency,          // "EUR"
        String total,             // totale del segno richiesto (income>=0, expenses<0)
        String groupBy,           // category|merchant|tag|account|provider
        List<Row> breakdown       // righe ordinate per impatto (|amount| desc)
) {
    public record Row(String key, String amount, long count) {}
}
