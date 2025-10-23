package com.opes.account.repository;

import org.springframework.data.jpa.repository.*;
import com.opes.account.entity.*;
import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByAccount_IdAndStatus(Long accountId, Goal.Status status);
}
