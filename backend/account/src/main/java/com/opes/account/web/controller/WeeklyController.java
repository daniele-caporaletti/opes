// com/opes/account/web/controller/WeeklyController.java
package com.opes.account.web.controller;

import com.opes.account.service.SpendingSnapshotService;
import com.opes.account.web.dto.analytics.SpendingSnapshotResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

@RestController
@RequestMapping("/analytics") // coerente con gli altri tuoi controller
@RequiredArgsConstructor
@Validated
public class WeeklyController {

    private final SpendingSnapshotService service;

    /**
     * Weekly Summary = preset di Spending Snapshot
     * Default: ultima settimana completa (lun-dom), groupBy=merchant, mode=top, limit=3
     */
    @GetMapping("/weekly")
    public ResponseEntity<SpendingSnapshotResponse> weekly(
            @RequestParam("userId") @NotBlank String userId,
            @RequestParam(value = "when", required = false, defaultValue = "last") String when, // last|current
            @RequestParam(value = "mode", required = false) String mode,                         // auto|last|top
            @RequestParam(value = "groupBy", required = false, defaultValue = "merchant") String groupBy,
            @RequestParam(value = "limit", required = false, defaultValue = "3") @Min(1) @Max(10) Integer limit,
            @RequestParam(value = "expand", required = false) String expand // "totals"
    ) {
        boolean expandTotals = "totals".equalsIgnoreCase(expand);

        LocalDate today = LocalDate.now();
        LocalDate thisMonday = today.with(DayOfWeek.MONDAY);

        LocalDate from, to;
        if ("current".equalsIgnoreCase(when)) {
            from = thisMonday;
        } else {
            from = thisMonday.minusWeeks(1); // default: LAST complete week
        }
        to = from.plusDays(6);

        // Re-use SpendingSnapshot con periodo custom e i preset weekly
        SpendingSnapshotResponse res = service.snapshot(
                userId,
                "custom",      // period
                from, to,
                mode == null ? "top" : mode, // default weekly = top
                groupBy,
                limit,
                expandTotals
        );
        return ResponseEntity.ok(res);
    }
}
