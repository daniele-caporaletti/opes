package com.opex.financialappbackend.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record WeeklySummaryDto(
        BigDecimal totalWeekIncome,
        BigDecimal totalWeekExpense,
        List<DailySummaryDto> days // Lista di 7 elementi (Lun-Dom)
) {}