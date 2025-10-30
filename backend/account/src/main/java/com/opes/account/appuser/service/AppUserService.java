// com/opes/account/appuser/service/AppUserService.java
package com.opes.account.appuser.service;

import com.opes.account.appuser.domain.entity.AppUser;
import com.opes.account.appuser.repository.AppUserRepository;
import com.opes.account.appuser.web.dto.CreateUserRequest;
import com.opes.account.appuser.web.dto.UpdateOnboardingRequest;
import com.opes.account.appuser.web.dto.UpdateProfileRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository repository;

    @Transactional
    public boolean createOrUpdateEmail(String userId, CreateUserRequest req) {
        Optional<AppUser> existing = repository.findById(userId);
        if (existing.isPresent()) {
            AppUser u = existing.get();
            u.setEmail(req.email());
            // updatedAt si aggiorna via @UpdateTimestamp
            return false; // updated (200 OK)
        } else {
            AppUser u = new AppUser();
            u.setId(userId);
            u.setEmail(req.email());
            repository.save(u);
            return true; // created (201 Created)
        }
    }

    @Transactional
    public void updateProfile(String userId, UpdateProfileRequest req) {
        AppUser u = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (req.firstName() != null) u.setFirstName(req.firstName());
        if (req.lastName() != null)  u.setLastName(req.lastName());
        if (req.birthDate() != null) u.setBirthDate(req.birthDate());
    }

    @Transactional
    public void updateOnboarding(String userId, UpdateOnboardingRequest req) {
        AppUser u = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (req.sparkSelfRecognition() != null) u.setSparkSelfRecognition(req.sparkSelfRecognition());
        if (req.emotionalGoal() != null)        u.setEmotionalGoal(req.emotionalGoal());
        if (req.emotionalGoalOther() != null)   u.setEmotionalGoalOther(req.emotionalGoalOther());
        if (req.currentSituation() != null)     u.setCurrentSituation(req.currentSituation());
        if (req.monthlyIncome() != null)        u.setMonthlyIncome(req.monthlyIncome());
    }
}
