// com/opes/account/service/AnalyticsAggregationService.java
package com.opes.account.service;

import com.opes.account.repository.transaction.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsAggregationService {

    public enum TxType { INCOME, EXPENSE }
    public enum GroupBy { CATEGORY, MERCHANT, TAG }
    public enum Sort { AMOUNT_DESC, AMOUNT_ASC, NAME_ASC }

    @PersistenceContext
    private EntityManager em;

    private final TransactionRepository txRepo;

    public AnalyticsAggregationService(TransactionRepository txRepo) {
        this.txRepo = txRepo;
    }

    /**
     * Aggrega per categoria / merchant / tag nel range [from, to].
     * - Filtri standard: transfer=false, refund=false
     * - INCOME: sum(t.amount) su amount>0
     * - EXPENSE: sum(-t.amount) su amount<0
     */
    public AggregateResponse aggregate(String userId,
                                       TxType type,
                                       GroupBy groupBy,
                                       LocalDate from,
                                       LocalDate to,
                                       int limit,
                                       int offset,
                                       Sort sort) {

        // 1) totale per share%
        BigDecimal total = switch (type) {
            case INCOME -> nz(txRepo.sumIncome(userId, from, to));
            case EXPENSE -> nz(txRepo.sumExpensesAbs(userId, from, to));
        };

        // 2) JPQL dinamica
        String entityAlias = switch (groupBy) {
            case CATEGORY -> "c";
            case MERCHANT -> "m";
            case TAG      -> "tag";
        };
        String join = switch (groupBy) {
            case CATEGORY -> " left join t.category c ";
            case MERCHANT -> " left join t.merchant m ";
            case TAG      -> " join t.tags tag ";
        };
        // somma in base al tipo
        String sumExpr = (type == TxType.INCOME) ? "sum(t.amount)" : "sum(-t.amount)";

        StringBuilder jpql = new StringBuilder();
        jpql.append("select ").append(entityAlias).append(".id, ")
                .append(entityAlias).append(".name, ")
                .append(sumExpr).append(" as total ")
                .append(" from Transaction t ")
                .append(join)
                .append(" where t.user.id = :userId ")
                .append(" and t.bookingDate between :from and :to ")
                .append(" and t.transfer = false ")
                .append(" and t.refund = false ");

        // direzione importi
        if (type == TxType.INCOME) {
            jpql.append(" and t.amount > 0 ");
        } else {
            jpql.append(" and t.amount < 0 ");
        }

        jpql.append(" group by ").append(entityAlias).append(".id, ").append(entityAlias).append(".name ");

        // sorting
        switch (sort) {
            case NAME_ASC   -> jpql.append(" order by ").append(entityAlias).append(".name asc ");
            case AMOUNT_ASC -> jpql.append(" order by total asc ");
            default         -> jpql.append(" order by total desc ");
        }

        Query q = em.createQuery(jpql.toString());
        q.setParameter("userId", userId);
        q.setParameter("from", from);
        q.setParameter("to", to);
        q.setFirstResult(Math.max(0, offset));
        q.setMaxResults(Math.max(1, limit));

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        // 3) mapping
        List<Item> items = new ArrayList<>();
        for (Object[] r : rows) {
            UUID id = (UUID) r[0];
            String label = (String) r[1];
            BigDecimal amount = nz((BigDecimal) r[2]);
            double sharePct = total.compareTo(BigDecimal.ZERO) == 0
                    ? 0d
                    : amount.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
            items.add(new Item(id, label != null ? label : fallbackLabel(groupBy), amount, sharePct));
        }

        return new AggregateResponse(new Period(from, to), groupBy, type, total, items);
    }

    private static String fallbackLabel(GroupBy groupBy) {
        return switch (groupBy) {
            case CATEGORY -> "Uncategorized";
            case MERCHANT -> "No merchant";
            case TAG      -> "No tag";
        };
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    // ---------------- DTO ----------------

    public record Period(LocalDate from, LocalDate to) {}

    public record Item(UUID id, String label, BigDecimal amount, double sharePct) {}

    public record AggregateResponse(
            Period period,
            GroupBy groupBy,
            TxType type,
            BigDecimal total,
            List<Item> items
    ) {}
}
