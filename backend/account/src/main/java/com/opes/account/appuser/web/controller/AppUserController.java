// com/opes/account/appuser/web/controller/AppUserController.java
package com.opes.account.appuser.web.controller;

import com.opes.account.appuser.service.AppUserService;
import com.opes.account.appuser.web.dto.CreateUserRequest;
import com.opes.account.appuser.web.dto.UpdateOnboardingRequest;
import com.opes.account.appuser.web.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appUsers")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService service;

    // 1) Creazione account da Keycloak: PUT /appUsers?userId=ID
    @PutMapping
    public ResponseEntity<Void> createOrUpdate(
            @RequestParam("userId") String userId,
            @Valid @RequestBody CreateUserRequest body
    ) {
        boolean created = service.createOrUpdateEmail(userId, body);
        return created ? ResponseEntity.created(null).build() : ResponseEntity.ok().build();
    }

    // 2) Finalizzare l’utente (dati anagrafici): PATCH /appUsers/profile?userId=ID
    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @RequestParam("userId") String userId,
            @Valid @RequestBody UpdateProfileRequest body
    ) {
        service.updateProfile(userId, body);
        return ResponseEntity.noContent().build();
    }

    // 3) Inviare risposte onboarding: PATCH /appUsers/onboarding?userId=ID
    @PatchMapping("/onboarding")
    public ResponseEntity<Void> updateOnboarding(
            @RequestParam("userId") String userId,
            @Valid @RequestBody UpdateOnboardingRequest body
    ) {
        service.updateOnboarding(userId, body);
        return ResponseEntity.noContent().build();
    }
}
