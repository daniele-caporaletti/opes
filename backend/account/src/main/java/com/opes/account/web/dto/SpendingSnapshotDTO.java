// com/opes/account/web/dto/SpendingSnapshotDTO.java
package com.opes.account.web.dto;

import java.util.List;

public record SpendingSnapshotDTO(
        String mode, // "LAST_EXPENSES" oppure "TOP_CATEGORIES"
        List<SpendingItemDTO> items
) {}
