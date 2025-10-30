// com/opes/account/repository/TransactionRepository.java
package com.opes.account.repository;

import com.opes.account.domain.entity.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // Base set: no transfer, by user & date
    @Query("""
    select t from Transaction t
    where t.user.id = :userId
      and t.transfer = false
      and t.bookingDate between :from and :to
  """)
    List<Transaction> findForAnalytics(String userId, LocalDate from, LocalDate to);

    // Conteggio ultimi 30 giorni (no transfer)
    @Query("""
    select count(t) from Transaction t
    where t.user.id = :userId
      and t.transfer = false
      and t.bookingDate between :from and :to
  """)
    long countNotTransferInRange(String userId, LocalDate from, LocalDate to);

    // ===== AGGREGAZIONE GENERICA (category | merchant | account | provider) =====
    @Query("""
    select
      case
        when :groupBy = 'category' then coalesce(c.name,'—')
        when :groupBy = 'merchant' then coalesce(m.name,'—')
        when :groupBy = 'account'  then a.name
        when :groupBy = 'provider' then cast(a.provider as string)
        else coalesce(c.name,'—')
      end as key,
      sum(t.amount) as total,
      count(t) as cnt
    from Transaction t
      left join t.category c
      left join t.merchant m
      join t.account a
    where t.user.id = :userId
      and t.transfer = false
      and t.bookingDate between :from and :to
      and ( (:income = true and t.amount >= 0) or (:income = false and t.amount < 0) )
    group by
      case
        when :groupBy = 'category' then coalesce(c.name,'—')
        when :groupBy = 'merchant' then coalesce(m.name,'—')
        when :groupBy = 'account'  then a.name
        when :groupBy = 'provider' then cast(a.provider as string)
        else coalesce(c.name,'—')
      end
    order by abs(sum(t.amount)) desc
  """)
    List<Object[]> aggregateByKey(String userId, LocalDate from, LocalDate to, boolean income, String groupBy);

    // ===== TAG: N:M → bucket per singolo tag =====
    @Query("""
    select tag.name as key, sum(t.amount) as total, count(t) as cnt
    from Transaction t join t.tags tag
    where t.user.id = :userId
      and t.transfer = false
      and t.bookingDate between :from and :to
      and ( (:income = true and t.amount >= 0) or (:income = false and t.amount < 0) )
    group by tag.name
    order by abs(sum(t.amount)) desc
  """)
    List<Object[]> aggregateByTag(String userId, LocalDate from, LocalDate to, boolean income);

    // Bucket "—" per transazioni senza alcun tag
    @Query("""
    select sum(t.amount) as total, count(t) as cnt
    from Transaction t
    where t.user.id = :userId
      and t.transfer = false
      and t.bookingDate between :from and :to
      and ( (:income = true and t.amount >= 0) or (:income = false and t.amount < 0) )
      and t.tags is empty
  """)
    Object[] aggregateNoTagBucket(String userId, LocalDate from, LocalDate to, boolean income);
}
