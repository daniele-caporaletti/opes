package com.opex.financialappbackend.dto.goal;
import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalDto(
    Long id, String title, String category, 
    BigDecimal targetAmount, BigDecimal currentAmount, 
    BigDecimal savedPercentage, BigDecimal monthlyContribution, 
    LocalDate deadline
) {}