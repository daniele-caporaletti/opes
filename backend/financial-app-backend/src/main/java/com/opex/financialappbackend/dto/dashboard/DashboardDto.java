package com.opex.financialappbackend.dto.dashboard;
import java.math.BigDecimal;

public record DashboardDto(
        BigDecimal totalBalance,
        String currency,
        BigDecimal balanceTrendPercentage, // % variazione nel periodo scelto

        BigDecimal periodIncome,  // Totale Entrate nel periodo scelto
        BigDecimal incomeTrendPercentage,   // % variazione entrate

        BigDecimal periodExpense, // Totale Uscite nel periodo scelto
        BigDecimal expenseTrendPercentage, // <--- % variazione uscite

        SmartMessageDto smartMessage,
        WeeklySummaryDto weeklySummary,

        SnapshotDto snapshot
) {}