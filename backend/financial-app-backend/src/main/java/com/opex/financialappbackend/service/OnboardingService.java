package com.opex.financialappbackend.service;

import com.opex.financialappbackend.domain.OnboardingResponse;
import com.opex.financialappbackend.domain.User;
import com.opex.financialappbackend.dto.onboarding.*;
import com.opex.financialappbackend.repository.OnboardingResponseRepository;
import com.opex.financialappbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserRepository userRepository;
    private final OnboardingResponseRepository responseRepository;

    @Transactional
    public User saveUserProfile(String userId, UserProfileDto dto) {
        User user = userRepository.findById(userId)
                .orElse(User.builder().id(userId).build());

        // Accesso ai campi record (senza get)
        user.setFirstName(dto.firstName());
        user.setBirthDate(dto.birthDate());
        if (dto.email() != null) {
            user.setEmail(dto.email());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void saveOnboardingAnswers(String userId, List<OnboardingAnswerDto> answers) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<OnboardingResponse> entities = answers.stream().map(dto -> OnboardingResponse.builder()
                .user(user)
                .questionCode(dto.questionCode())
                .selectedOption(dto.selectedOption())
                .rawValue(dto.rawValue())
                .build()).toList();

        responseRepository.saveAll(entities);
    }
}