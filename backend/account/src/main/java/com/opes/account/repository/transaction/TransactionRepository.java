// com/opes/account/repository/transaction/TransactionRepository.java
package com.opes.account.repository.transaction;

import com.opes.account.domain.entity.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Ultime transazioni del periodo (per la Home: mese corrente), escludendo i trasferimenti.
     * Include entrate/uscite e anche i rimborsi (sono movimenti reali).
     */
    @Query("""
        select t from Transaction t
        where t.user.id = :userId
          and t.bookingDate between :start and :end
          and t.transfer = false
        order by t.bookingDate desc, t.id desc
    """)
    Page<Transaction> findRecentInPeriodExcludingTransfers(
            @Param("userId") String userId,
            @Param("start") LocalDate startInclusive,
            @Param("end") LocalDate endInclusive,
            Pageable pageable
    );

    /**
     * Totale spese (valore assoluto) nel periodo, esclusi trasferimenti e rimborsi.
     * Utile per calcolare sharePct nei "top".
     */
    @Query("""
        select coalesce(sum(-t.amount), 0) from Transaction t
        where t.user.id = :userId
          and t.bookingDate between :start and :end
          and t.transfer = false
          and t.refund = false
          and t.amount < 0
    """)
    BigDecimal sumExpensesAbs(
            @Param("userId") String userId,
            @Param("start") LocalDate startInclusive,
            @Param("end") LocalDate endInclusive
    );

    /**
     * Top categorie per spesa nel periodo (amount < 0), esclusi transfer/refund.
     * Ritorna righe (categoryId, totalAbs) ordinate desc. Usa Pageable per limitare (es. top 1/3/10).
     */
    @Query("""
        select t.category.id as categoryId, sum(-t.amount) as totalAbs
        from Transaction t
        where t.user.id = :userId
          and t.bookingDate between :start and :end
          and t.transfer = false
          and t.refund = false
          and t.amount < 0
        group by t.category.id
        order by totalAbs desc
    """)
    List<Object[]> topCategoriesByExpense(
            @Param("userId") String userId,
            @Param("start") LocalDate startInclusive,
            @Param("end") LocalDate endInclusive,
            Pageable pageable
    );

    /**
     * Top merchant per spesa nel periodo, esclusi transfer/refund.
     * Ritorna righe (merchantId, totalAbs).
     */
    @Query("""
        select t.merchant.id as merchantId, sum(-t.amount) as totalAbs
        from Transaction t
        where t.user.id = :userId
          and t.bookingDate between :start and :end
          and t.transfer = false
          and t.refund = false
          and t.amount < 0
        group by t.merchant.id
        order by totalAbs desc
    """)
    List<Object[]> topMerchantsByExpense(
            @Param("userId") String userId,
            @Param("start") LocalDate startInclusive,
            @Param("end") LocalDate endInclusive,
            Pageable pageable
    );

    /**
     * Top tag per spesa nel periodo, esclusi transfer/refund.
     * Join N:M su t.tags. Ritorna righe (tagId, totalAbs).
     */
    @Query("""
        select tag.id as tagId, sum(-t.amount) as totalAbs
        from Transaction t
        join t.tags tag
        where t.user.id = :userId
          and t.bookingDate between :start and :end
          and t.transfer = false
          and t.refund = false
          and t.amount < 0
        group by tag.id
        order by totalAbs desc
    """)
    List<Object[]> topTagsByExpense(
            @Param("userId") String userId,
            @Param("start") LocalDate startInclusive,
            @Param("end") LocalDate endInclusive,
            Pageable pageable
    );

    /** Punto della serie giornaliera spese (MVP: solo uscite, abs). */
    public interface DailyExpensePoint {
        java.time.LocalDate getDay();      // t.bookingDate
        java.math.BigDecimal getTotalAbs(); // sum(-t.amount)
    }

    /**
     * Serie giornaliera delle spese nel periodo [start, end], esclusi transfer/refund.
     * Ritorna solo i giorni con spesa > 0 (i "buchi" li riempiamo a livello service).
     */
    @Query("""
        select t.bookingDate as day, sum(-t.amount) as totalAbs
        from Transaction t
        where t.user.id = :userId
          and t.bookingDate between :start and :end
          and t.transfer = false
          and t.refund = false
          and t.amount < 0
        group by t.bookingDate
        order by t.bookingDate asc
    """)
    java.util.List<DailyExpensePoint> dailyExpensesSeries(
            @Param("userId") String userId,
            @Param("start") java.time.LocalDate startInclusive,
            @Param("end") java.time.LocalDate endInclusive
    );

    /**
     * Totale entrate nel periodo, esclusi trasferimenti e rimborsi.
     * (Usalo per il widget "Totals" e per le headline MoM.)
     */
    @Query("""
        select coalesce(sum(t.amount), 0) from Transaction t
        where t.user.id = :userId
          and t.bookingDate between :start and :end
          and t.transfer = false
          and t.refund = false
          and t.amount > 0
    """)
    java.math.BigDecimal sumIncome(
            @Param("userId") String userId,
            @Param("start") java.time.LocalDate startInclusive,
            @Param("end") java.time.LocalDate endInclusive
    );
}


