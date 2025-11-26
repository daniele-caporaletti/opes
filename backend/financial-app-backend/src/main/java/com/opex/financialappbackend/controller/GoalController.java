package com.opex.financialappbackend.controller;

import com.opex.financialappbackend.dto.goal.*;
import com.opex.financialappbackend.service.GoalService;
import com.opex.financialappbackend.service.UserContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final UserContextService userContextService;

    @GetMapping
    public ResponseEntity<List<GoalDto>> getGoals(@AuthenticationPrincipal Jwt jwt,
                                                  @RequestParam(required = false) boolean demo) {
        String userId = userContextService.getUserId(jwt, demo);
        return ResponseEntity.ok(goalService.getUserGoals(userId));
    }

    @PostMapping
    public ResponseEntity<GoalDto> createGoal(@AuthenticationPrincipal Jwt jwt,
                                              @Valid @RequestBody CreateGoalDto dto,
                                              @RequestParam(required = false) boolean demo) {
        String userId = userContextService.getUserId(jwt, demo);
        return ResponseEntity.ok(goalService.createGoal(userId, dto));
    }
}