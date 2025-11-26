package com.opex.financialappbackend.repository;

import com.opex.financialappbackend.domain.Transaction;
import com.opex.financialappbackend.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Per Weekly Summary e calcoli
    List<Transaction> findByUserIdAndBookingDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    // Per Snapshot Fallback
    long countByUserIdAndBookingDateAfter(String userId, LocalDate date);
    List<Transaction> findTop3ByUserIdOrderByBookingDateDesc(String userId);

    // Per lista transazioni paginata
    Page<Transaction> findByUserIdOrderByBookingDateDesc(String userId, Pageable pageable);

    // Calcolo Expense per Smart Message
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.bookingDate BETWEEN :startDate AND :endDate " +
            "AND t.type = 'EXPENSE'")
    BigDecimal sumExpenseByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate);

    // Calcolo Trend Saldo (Net Flow)
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.bookingDate > :startDate")
    BigDecimal sumNetFlowSince(@Param("userId") String userId, @Param("startDate") LocalDate startDate);

    // Calcolo Income/Expense Totali del Periodo
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.type = :type " +
            "AND t.bookingDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalByUserIdAndTypeAndDateRange(@Param("userId") String userId,
                                                   @Param("type") TransactionType type,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    // Snapshot Top Categorie
    @Query("SELECT t.category, SUM(t.amount), COUNT(t.id) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.type = 'EXPENSE' " +
            "AND t.bookingDate BETWEEN :startDate AND :endDate " +
            "GROUP BY t.category " +
            "ORDER BY SUM(t.amount) ASC")
    List<Object[]> findTopCategoriesBySpend(@Param("userId") String userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate,
                                            Pageable pageable);
}