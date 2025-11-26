package com.opex.financialappbackend.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateAccountDto(
    @NotBlank(message = "Name is required") String name,
    String institutionName,
    @NotNull(message = "Balance is required") BigDecimal balance,
    String currency,
    String type
) {}