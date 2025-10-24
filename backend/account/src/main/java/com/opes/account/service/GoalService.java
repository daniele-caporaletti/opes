// com/opes/account/service/GoalService.java
package com.opes.account.service;

import com.opes.account.domain.entity.goal.Goal;
import com.opes.account.domain.enums.GoalStatus;
import com.opes.account.repository.goal.GoalContributionRepository;
import com.opes.account.repository.goal.GoalRepository;
import com.opes.account.web.dto.GoalRecapItemDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GoalService {

    private final GoalRepository goalRepo;
    private final GoalContributionRepository contribRepo;

    public GoalService(GoalRepository goalRepo, GoalContributionRepository contribRepo) {
        this.goalRepo = goalRepo;
        this.contribRepo = contribRepo;
    }

    public List<GoalRecapItemDTO> recap(String userId) {
        return goalRepo.findByUserIdAndStatus(userId, GoalStatus.ACTIVE).stream().map(g -> {
            BigDecimal saved = contribRepo.sumForGoal(g.getId());
            if (saved == null) saved = BigDecimal.ZERO;
            double percent = g.getTargetAmount().compareTo(BigDecimal.ZERO) == 0
                    ? 0d
                    : saved.divide(g.getTargetAmount(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            return new GoalRecapItemDTO(g.getId(), g.getTitle(), g.getTargetAmount(), saved,
                    Math.round(percent * 10.0) / 10.0);
        }).toList();
    }
}
