package com.opex.financialappbackend.dto.dashboard;

import java.math.BigDecimal;

public record SnapshotItemDto(String label, BigDecimal amount, String subLabel) {
}
