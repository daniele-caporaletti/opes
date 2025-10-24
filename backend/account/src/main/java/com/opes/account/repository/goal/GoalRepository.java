// com/opes/account/repository/goal/GoalRepository.java
package com.opes.account.repository.goal;

import com.opes.account.domain.entity.goal.Goal;
import com.opes.account.domain.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID> {
    List<Goal> findByUserIdAndStatus(String userId, GoalStatus status);
}
