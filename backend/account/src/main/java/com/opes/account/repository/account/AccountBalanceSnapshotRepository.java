// com/opes/account/repository/account/AccountBalanceSnapshotRepository.java
package com.opes.account.repository.account;

import com.opes.account.domain.entity.account.AccountBalanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountBalanceSnapshotRepository extends JpaRepository<AccountBalanceSnapshot, UUID> {

    /**
     * Somma i saldi degli ULTIMI snapshot per ogni account ATTIVO dell'utente.
     * (MVP: EUR, nessuna conversione)
     */
    @Query("""
        select coalesce(sum(s.balance), 0)
        from AccountBalanceSnapshot s
        join s.account a
        where a.user.id = :userId
          and a.active = true
          and s.asOf = (
             select max(s2.asOf) from AccountBalanceSnapshot s2
             where s2.account.id = a.id
          )
    """)
    BigDecimal sumLatestBalanceForUser(String userId);

    /**
     * Ritorna gli ULTIMI snapshot per ciascun account ATTIVO dell'utente.
     * Utile se vuoi mostrare anche il dettaglio per conto.
     */
    @Query("""
        select s
        from AccountBalanceSnapshot s
        join s.account a
        where a.user.id = :userId
          and a.active = true
          and s.asOf = (
             select max(s2.asOf) from AccountBalanceSnapshot s2
             where s2.account.id = a.id
          )
        order by a.name asc
    """)
    List<AccountBalanceSnapshot> findLatestSnapshotsForUserActiveAccounts(String userId);
}
