// com/opes/account/repository/AppUserRepository.java
package com.opes.account.repository;

import com.opes.account.domain.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, String> { }
