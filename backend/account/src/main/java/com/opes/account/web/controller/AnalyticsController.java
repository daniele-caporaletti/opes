// com/opes/account/web/controller/AnalyticsController.java
package com.opes.account.web.controller;

import com.opes.account.service.AnalyticsService;
import com.opes.account.web.dto.analytics.TotalBalanceResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Validated
public class AnalyticsController {

    private final AnalyticsService service;

    @GetMapping("/total-balance")
    public ResponseEntity<TotalBalanceResponse> totalBalance(
            @RequestParam("userId") @NotBlank String userId,
            @RequestParam(value = "expand", required = false) String expand
    ) {
        boolean expandAccounts = "accounts".equalsIgnoreCase(expand);
        return ResponseEntity.ok(service.totalBalance(userId, expandAccounts));
    }
}
