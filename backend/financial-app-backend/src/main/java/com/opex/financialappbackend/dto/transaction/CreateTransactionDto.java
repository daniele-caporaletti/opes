package com.opex.financialappbackend.dto.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionDto(
    Long accountId,
    @NotNull @Positive(message = "Amount must be positive") BigDecimal amount,
    @NotBlank String type,          // EXPENSE / INCOME
    @NotBlank String category,
    @NotBlank String merchantName,
    @NotNull LocalDate date,
    String status
) {}