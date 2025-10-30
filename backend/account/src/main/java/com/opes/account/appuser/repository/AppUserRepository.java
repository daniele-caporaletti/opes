// com/opes/account/appuser/repository/AppUserRepository.java
package com.opes.account.appuser.repository;

import com.opes.account.appuser.domain.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, String> { }
