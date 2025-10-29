// com/opes/account/service/RecentActivityService.java
package com.opes.account.service;

import com.opes.account.domain.entity.transaction.Transaction;
import com.opes.account.repository.transaction.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RecentActivityService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Rome");

    private final TransactionRepository txRepo;

    public RecentActivityService(TransactionRepository txRepo) {
        this.txRepo = txRepo;
    }

    /** Ultime N transazioni del mese corrente (fino a ieri). Default N=10. */
    public RecentActivity getMonthToDate(String userId) {
        return getMonthToDate(userId, 10);
    }

    public RecentActivity getMonthToDate(String userId, int limit) {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = today.minusDays(1);
        if (end.isBefore(start)) end = start; // giorno 1 → mostra quel giorno

        return getInRange(userId, start, end, limit);
    }

    /** Ultime N transazioni in un range arbitrario (ordinamento desc). */
    public RecentActivity getInRange(String userId, LocalDate from, LocalDate to, int limit) {
        var page = txRepo.findRecentInPeriodExcludingTransfers(
                userId, from, to, PageRequest.of(0, Math.max(1, limit))
        );

        var items = page.getContent().stream()
                .map(RecentActivityService::toItem)
                .toList();

        return new RecentActivity(new Period(from, to), items);
    }

    // ----------------- mapping -----------------

    private static Item toItem(Transaction t) {
        String label = t.getMerchant() != null ? t.getMerchant().getName()
                : t.getCategory() != null ? t.getCategory().getName()
                : "Transaction";
        String subtitle = t.getDescription() != null ? truncate(t.getDescription(), 60) : null;
        Direction dir = t.getAmount().compareTo(BigDecimal.ZERO) >= 0 ? Direction.IN : Direction.OUT;

        UUID categoryId = t.getCategory() != null ? t.getCategory().getId() : null;
        UUID merchantId = t.getMerchant() != null ? t.getMerchant().getId() : null;

        return new Item(
                t.getId(),
                label,
                subtitle,
                t.getBookingDate(),
                t.getAmount().abs(),   // importo positivo per display
                dir,
                categoryId,
                merchantId
        );
    }

    private static String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 1)).trim() + "…";
    }

    // ----------------- DTO interni -----------------

    public record RecentActivity(Period period, List<Item> items) {}

    public record Period(LocalDate from, LocalDate to) {}

    public enum Direction { IN, OUT }

    public record Item(
            UUID txId,
            String label,
            String subtitle,       // opzionale (description truncata)
            LocalDate date,
            BigDecimal amountAbs,  // sempre positivo per UI
            Direction direction,   // IN / OUT
            UUID categoryId,       // opzionale
            UUID merchantId        // opzionale
    ) {}
}
