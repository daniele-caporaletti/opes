// com/opes/account/web/dto/DashboardSummaryDTO.java
package com.opes.account.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardSummaryDTO(
        BigDecimal totalBalance,
        String headlineMessage,
        WeeklySummaryDTO weeklySummary,
        AmountPairDTO totals, // income & expenses del mese corrente (opzionale ma comodo)
        SpendingSnapshotDTO spendingSnapshot,
        List<GoalRecapItemDTO> goalRecap
) {}

