package com.opex.financialappbackend.dto.account;
import java.math.BigDecimal;

public record AccountDto(
    Long id, String name, String institutionName, 
    BigDecimal balance, String currency, String type
) {}