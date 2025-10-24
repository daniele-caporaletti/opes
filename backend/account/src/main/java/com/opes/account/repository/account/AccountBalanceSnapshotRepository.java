// com/opes/account/repository/account/AccountBalanceSnapshotRepository.java
package com.opes.account.repository.account;

import com.opes.account.domain.entity.account.AccountBalanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface AccountBalanceSnapshotRepository extends JpaRepository<AccountBalanceSnapshot, UUID> {

    @Query("""
     select s from AccountBalanceSnapshot s
     where s.account.id in :accountIds
     and s.asOf in (
       select max(s2.asOf) from AccountBalanceSnapshot s2
       where s2.account.id = s.account.id
     )
  """)
    List<AccountBalanceSnapshot> findLatestByAccountIds(List<UUID> accountIds);
}
