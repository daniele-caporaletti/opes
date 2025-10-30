// com/opes/account/repository/AccountBalanceSnapshotRepository.java
package com.opes.account.repository;

import com.opes.account.domain.entity.account.AccountBalanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AccountBalanceSnapshotRepository extends JpaRepository<AccountBalanceSnapshot, UUID> {

    // Ultimo snapshot per ciascun account di input
    @Query("""
        select s from AccountBalanceSnapshot s
        where s.account.id in :accountIds
          and s.asOf = (
            select max(x.asOf) from AccountBalanceSnapshot x
            where x.account.id = s.account.id
          )
    """)
    List<AccountBalanceSnapshot> findLatestByAccountIds(List<UUID> accountIds);
}
