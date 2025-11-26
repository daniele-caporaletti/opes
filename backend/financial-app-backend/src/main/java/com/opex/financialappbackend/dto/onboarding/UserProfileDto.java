package com.opex.financialappbackend.dto.onboarding;

import java.time.LocalDate;

// Onboarding
public record UserProfileDto(String firstName, String email, LocalDate birthDate) {
}
