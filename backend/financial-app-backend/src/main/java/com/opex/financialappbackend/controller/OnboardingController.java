package com.opex.financialappbackend.controller;

import com.opex.financialappbackend.dto.onboarding.OnboardingAnswerDto;
import com.opex.financialappbackend.dto.onboarding.UserProfileDto;
import com.opex.financialappbackend.service.OnboardingService;
import com.opex.financialappbackend.service.UserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final UserContextService userContextService;

    // Step 1: Salva Nome e Data di nascita
    @PostMapping("/profile")
    public ResponseEntity<String> saveProfile(@AuthenticationPrincipal Jwt jwt,
                                              @RequestBody UserProfileDto profileDto) {
        String userId = jwt.getSubject(); // Questo è il 'sub' del token Keycloak
        onboardingService.saveUserProfile(userId, profileDto);
        return ResponseEntity.ok("Profile updated");
    }

    // Step 2: Salva le risposte del questionario (facoltativo)
    @PostMapping("/answers")
    public ResponseEntity<String> submitAnswers(@AuthenticationPrincipal Jwt jwt,
                                                @RequestBody List<OnboardingAnswerDto> answers) {
        String userId = jwt.getSubject();
        onboardingService.saveOnboardingAnswers(userId, answers);
        return ResponseEntity.ok("Answers saved");
    }
}