// com/opes/account/web/dto/GoalRecapItemDTO.java
package com.opes.account.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GoalRecapItemDTO(
        UUID goalId,
        String title,
        BigDecimal targetAmount,
        BigDecimal savedAmount,
        double progressPercent
) {}
