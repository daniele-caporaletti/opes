// com/opes/account/web/controller/SpendingSnapshotController.java
package com.opes.account.web.controller;

import com.opes.account.service.SpendingSnapshotService;
import com.opes.account.web.dto.analytics.SpendingSnapshotResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Validated
public class SpendingSnapshotController {

    private final SpendingSnapshotService service;

    @GetMapping("/spending-snapshot")
    public ResponseEntity<SpendingSnapshotResponse> snapshot(
            @RequestParam("userId") @NotBlank String userId,
            @RequestParam(value = "period", required = false) String period,         // auto|week|month|year|custom
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to",   required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "mode", required = false) String mode,             // auto|last|top
            @RequestParam(value = "groupBy", required = false) String groupBy,       // category|merchant|tag|account
            @RequestParam(value = "limit", required = false) Integer limit,          // default 3 (1..10)
            @RequestParam(value = "expand", required = false) String expand          // "totals"
    ) {
        boolean expandTotals = "totals".equalsIgnoreCase(expand);
        return ResponseEntity.ok(
                service.snapshot(userId, period, from, to, mode, groupBy, limit, expandTotals)
        );
    }
}
