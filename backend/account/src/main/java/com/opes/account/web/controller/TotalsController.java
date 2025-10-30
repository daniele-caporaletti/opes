// com/opes/account/web/controller/TotalsController.java
package com.opes.account.web.controller;

import com.opes.account.service.TotalsService;
import com.opes.account.web.dto.analytics.TotalBreakdownResponse;
import com.opes.account.web.dto.analytics.TransactionsPageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class TotalsController {

    private final TotalsService service;

    // --------- SUMMARY ---------

    @GetMapping("/total-income")
    public ResponseEntity<TotalBreakdownResponse> totalIncome(
            @RequestParam("userId") @NotBlank String userId,
            @RequestParam(value = "groupBy", required = false) String groupBy, // category|merchant|tag|account|provider
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to",   required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(service.totalIncome(userId, groupBy, from, to));
    }

    @GetMapping("/total-expenses")
    public ResponseEntity<TotalBreakdownResponse> totalExpenses(
            @RequestParam("userId") @NotBlank String userId,
            @RequestParam(value = "groupBy", required = false) String groupBy, // category|merchant|tag|account|provider
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to",   required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(service.totalExpenses(userId, groupBy, from, to));
    }

    // --------- DRILL-DOWN ---------

    @GetMapping("/total-income/details")
    public ResponseEntity<TransactionsPageResponse> incomeDetails(
            @RequestParam("userId") @NotBlank String userId,
            @RequestParam("groupBy") String groupBy,
            @RequestParam("key") String key,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to",   required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "period", required = false) String period, // day|week|month|year
            @RequestParam(value = "anchorDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchorDate,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int pageSize
    ) {
        return ResponseEntity.ok(service.detailsIncome(userId, groupBy, key, from, to, period, anchorDate, page, pageSize));
    }

    @GetMapping("/total-expenses/details")
    public ResponseEntity<TransactionsPageResponse> expensesDetails(
            @RequestParam("userId") @NotBlank String userId,
            @RequestParam("groupBy") String groupBy,
            @RequestParam("key") String key,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to",   required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "period", required = false) String period, // day|week|month|year
            @RequestParam(value = "anchorDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchorDate,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int pageSize
    ) {
        return ResponseEntity.ok(service.detailsExpenses(userId, groupBy, key, from, to, period, anchorDate, page, pageSize));
    }
}
