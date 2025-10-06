package com.opes.bff.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal OidcUser user) {
        if (user == null) return Map.of("authenticated", false);
        return Map.of(
                "authenticated", true,
                "name", user.getFullName(),
                "email", user.getEmail(),
                "sub", user.getSubject(),
                "claims", user.getClaims()
        );
    }

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from BFF 👋");
    }
}
