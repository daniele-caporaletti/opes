// com/opes/account/repository/goal/GoalContributionRepository.java
package com.opes.account.repository.goal;

import com.opes.account.domain.entity.goal.GoalContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.UUID;

public interface GoalContributionRepository extends JpaRepository<GoalContribution, UUID> {

    @Query("select coalesce(sum(gc.amount),0) from GoalContribution gc where gc.goal.id = :goalId")
    BigDecimal sumForGoal(UUID goalId);
}
