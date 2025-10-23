package com.opes.account.repository;

import org.springframework.data.jpa.repository.*;
import com.opes.account.entity.*;

public interface AccountRepository extends JpaRepository<Account, Long> { }
