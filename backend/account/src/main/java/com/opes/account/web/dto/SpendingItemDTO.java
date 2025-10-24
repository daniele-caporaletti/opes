// com/opes/account/web/dto/SpendingItemDTO.java
package com.opes.account.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SpendingItemDTO(
        String label,           // "Esselunga" o "Groceries" ecc.
        BigDecimal amount,      // importo positivo (assoluto) per display
        LocalDate date,         // presente solo in modalità LAST_EXPENSES
        UUID categoryId,        // opzionale
        UUID merchantId         // opzionale
) {}
