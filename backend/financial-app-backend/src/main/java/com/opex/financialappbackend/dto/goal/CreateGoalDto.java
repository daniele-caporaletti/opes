package com.opex.financialappbackend.dto.goal;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateGoalDto(
    @NotBlank String title,
    @NotBlank String category,
    @NotNull @Positive BigDecimal targetAmount,
    @NotNull @Future(message = "Deadline must be in the future") LocalDate deadline
) {}