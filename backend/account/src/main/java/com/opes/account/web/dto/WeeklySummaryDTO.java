// com/opes/account/web/dto/WeeklySummaryDTO.java
package com.opes.account.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WeeklySummaryDTO(
        LocalDate weekStart,
        LocalDate weekEnd,
        BigDecimal income,
        BigDecimal expenses
) {}
