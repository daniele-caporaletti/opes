// com/opes/account/web/dto/AmountPairDTO.java
package com.opes.account.web.dto;

import java.math.BigDecimal;

public record AmountPairDTO(
        BigDecimal income,
        BigDecimal expenses
) {}
