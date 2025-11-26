package com.opex.financialappbackend.dto.dashboard;

import com.opex.financialappbackend.dto.transaction.TransactionDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailySummaryDto(
        LocalDate date,
        String dayLabel,       // Es. "Mon", "Tue"
        BigDecimal dailyIncome,
        BigDecimal dailyExpense,
        List<TransactionDto> transactions
) {}