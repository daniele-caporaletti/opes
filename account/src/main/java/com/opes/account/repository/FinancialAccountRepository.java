package com.opes.account.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.opes.account.entity.*;
import java.util.List;

public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, Long> {
    @Query("select coalesce(sum(f.balanceCents),0) from FinancialAccount f where f.account.id = :accountId and f.excludedFromTotal = false")
    long sumIncludedBalance(@Param("accountId") Long accountId);
    List<FinancialAccount> findByAccount_Id(Long accountId);
}
