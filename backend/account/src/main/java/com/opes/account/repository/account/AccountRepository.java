// com/opes/account/repository/account/AccountRepository.java
package com.opes.account.repository.account;

import com.opes.account.domain.entity.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByUserIdAndActiveTrue(String userId);
}
