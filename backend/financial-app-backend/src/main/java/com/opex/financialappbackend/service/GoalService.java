package com.opex.financialappbackend.service;

import com.opex.financialappbackend.domain.Goal;
import com.opex.financialappbackend.domain.User;
import com.opex.financialappbackend.domain.enums.TransactionCategory;
import com.opex.financialappbackend.dto.goal.*;
import com.opex.financialappbackend.repository.GoalRepository;
import com.opex.financialappbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public List<GoalDto> getUserGoals(String userId) {
        return goalRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public GoalDto createGoal(String userId, CreateGoalDto dto) {
        User user = userRepository.getReferenceById(userId);

        Goal goal = Goal.builder()
                .user(user)
                .title(dto.title())
                .category(TransactionCategory.valueOf(dto.category()))
                .targetAmount(dto.targetAmount())
                .currentAmount(BigDecimal.ZERO) // Parte da 0
                .deadline(dto.deadline())
                .build();

        return mapToDto(goalRepository.save(goal));
    }

    private GoalDto mapToDto(Goal goal) {
        BigDecimal current = goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO;
        BigDecimal target = goal.getTargetAmount();

        // 1. Calcolo Percentuale
        BigDecimal percentage = BigDecimal.ZERO;
        if (target.compareTo(BigDecimal.ZERO) > 0) {
            percentage = current.divide(target, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        }

        // 2. Calcolo Monthly Contribution (Target - Current) / Mesi Rimanenti
        BigDecimal monthlyNeeded = BigDecimal.ZERO;
        if (goal.getDeadline() != null && goal.getDeadline().isAfter(LocalDate.now())) {
            long monthsBetween = ChronoUnit.MONTHS.between(LocalDate.now(), goal.getDeadline());
            if (monthsBetween <= 0) monthsBetween = 1; // Evita divisione per zero se scade questo mese

            BigDecimal remaining = target.subtract(current);
            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                monthlyNeeded = remaining.divide(new BigDecimal(monthsBetween), 2, RoundingMode.HALF_UP);
            }
        }

        return new GoalDto(
                goal.getId(),
                goal.getTitle(),
                goal.getCategory().name(),
                target,
                current,
                percentage,
                monthlyNeeded,
                goal.getDeadline()
        );
    }
}