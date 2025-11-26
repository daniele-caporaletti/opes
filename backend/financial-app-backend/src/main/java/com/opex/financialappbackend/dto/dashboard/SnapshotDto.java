package com.opex.financialappbackend.dto.dashboard;

import java.util.List;

public record SnapshotDto(String type, List<SnapshotItemDto> items) {
}
