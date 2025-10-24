// com/opes/account/repository/transaction/TransactionRepository.java
package com.opes.account.repository.transaction;

import com.opes.account.domain.entity.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByUserIdAndBookingDateBetween(String userId, LocalDate start, LocalDate end);

    @Query("""
     select coalesce(sum(t.amount),0) from Transaction t
     where t.user.id = :userId
       and t.bookingDate between :start and :end
       and t.transfer = false and t.refund = false
       and t.amount > 0
  """)
    BigDecimal sumIncome(String userId, LocalDate start, LocalDate end);

    @Query("""
     select coalesce(sum(-t.amount),0) from Transaction t
     where t.user.id = :userId
       and t.bookingDate between :start and :end
       and t.transfer = false and t.refund = false
       and t.amount < 0
  """)
    BigDecimal sumExpensesAbs(String userId, LocalDate start, LocalDate end);

    @Query("""
     select count(t) from Transaction t
     where t.user.id = :userId
       and t.bookingDate between :start and :end
       and t.transfer = false and t.refund = false
       and t.amount < 0
  """)
    int countExpenses(String userId, LocalDate start, LocalDate end);

    @Query("""
     select t from Transaction t
     where t.user.id = :userId and t.bookingDate between :start and :end
       and t.transfer = false and t.refund = false and t.amount < 0
     order by t.bookingDate desc
  """)
    List<Transaction> findRecentExpensesForSnapshot(String userId, LocalDate start, LocalDate end);

    @Query("""
     select t.category.id, sum(-t.amount) from Transaction t
     where t.user.id = :userId and t.bookingDate between :start and :end
       and t.transfer = false and t.refund = false and t.amount < 0
     group by t.category.id
     order by sum(-t.amount) desc
  """)
    List<Object[]> aggregateExpensesByCategory(String userId, LocalDate start, LocalDate end);
}
