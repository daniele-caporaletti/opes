package com.opex.financialappbackend.controller;

import com.opex.financialappbackend.domain.enums.DashboardPeriod;
import com.opex.financialappbackend.dto.dashboard.DashboardDto;
import com.opex.financialappbackend.service.DashboardService;
import com.opex.financialappbackend.service.UserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserContextService userContextService;

    @GetMapping
    public ResponseEntity<DashboardDto> getDashboard(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) boolean demo,
            @RequestParam(defaultValue = "LAST_30_DAYS") DashboardPeriod period
    ) {
        String userId = userContextService.getUserId(jwt, demo);
        return ResponseEntity.ok(dashboardService.getDashboardData(userId, period));
    }
}