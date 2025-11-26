package com.opex.financialappbackend.dto.transaction;
import java.math.BigDecimal;

public record TransactionDto(
    Long id, String merchantName, BigDecimal amount, 
    String category, String type, String status
) {}