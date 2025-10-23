package com.opes.account.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.opes.account.entity.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccount_IdAndBookingTsBetween(Long accountId, Instant from, Instant to);
    List<Transaction> findByAccount_IdAndKindAndBookingTsBetween(Long accountId, Transaction.Kind kind, Instant from, Instant to);

    @Query("""
    select t.categoryCode as category, sum(abs(t.amountCents)) as total
    from Transaction t
    where t.account.id = :accountId and t.kind = 'EXPENSE' and t.bookingTs between :from and :to
    group by t.categoryCode
    order by total desc
  """)
    List<Map<String,Object>> topCategories(@Param("accountId") Long accountId, @Param("from") Instant from, @Param("to") Instant to);
}
