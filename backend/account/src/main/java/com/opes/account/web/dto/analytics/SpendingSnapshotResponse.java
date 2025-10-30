// com/opes/account/web/dto/analytics/SpendingSnapshotResponse.java
package com.opes.account.web.dto.analytics;

import java.util.List;

public record SpendingSnapshotResponse(
        String mode,                 // "last" | "top"
        Period period,               // periodo risolto
        Totals totals,               // opzionale (se expand=totals)
        List<Item> items,            // lista compatta per la card
        Meta meta                    // info utili (es. txCount30d)
) {
    public record Period(String type, String from, String to) {}
    public record Totals(String income, String expenses, String net) {}
    public record Trend(String delta, Double pct) {}

    // Unico tipo di item per semplicità: alcuni campi saranno null a seconda della modalità
    public record Item(
            String type,             // "transaction" | "bucket"
            String groupBy,          // "merchant" | "category" | "tag" | "account"
            String key,              // chiave del bucket o chiave calcolata per la tx
            String label,            // etichetta da mostrare (in genere = key)
            String amount,           // "-124.00"
            String date,             // solo per type=transaction (YYYY-MM-DD), altrimenti null
            Long count,              // solo per type=bucket (conteggio nel periodo), altrimenti null
            Boolean oneOff,          // true se count==1 nel periodo (per tx: valutato sul suo key)
            Double sharePct,         // quota sul totale uscite del periodo (0..1), solo per bucket
            Trend trend              // delta/pct rispetto al periodo precedente, solo per bucket
    ) {}

    public record Meta(int txCount30d) {}
}
