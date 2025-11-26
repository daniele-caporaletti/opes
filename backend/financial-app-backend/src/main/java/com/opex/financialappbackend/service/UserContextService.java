package com.opex.financialappbackend.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class UserContextService {

    private static final String DEMO_USER_ID = "demo-user-123";

    public String getUserId(Jwt jwt, boolean isDemo) {
        if (isDemo) {
            return DEMO_USER_ID;
        }
        // Se jwt è null (es. test senza security) gestiamo l'eccezione o ritorniamo null
        if (jwt == null) {
            throw new IllegalArgumentException("JWT token is missing");
        }
        return jwt.getSubject();
    }
}