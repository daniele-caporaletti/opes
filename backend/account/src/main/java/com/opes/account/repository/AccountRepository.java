// com/opes/account/repository/AccountRepository.java
package com.opes.account.repository;

import com.opes.account.domain.entity.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByUser_IdAndActiveTrue(String userId);
}
