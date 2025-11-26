package com.opex.financialappbackend.controller;

import com.opex.financialappbackend.dto.account.AccountDto;
import com.opex.financialappbackend.dto.account.CreateAccountDto;
import com.opex.financialappbackend.service.AccountService;
import com.opex.financialappbackend.service.UserContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserContextService userContextService;

    @GetMapping
    public ResponseEntity<List<AccountDto>> getAccounts(@AuthenticationPrincipal Jwt jwt,
                                                        @RequestParam(required = false) boolean demo) {
        String userId = userContextService.getUserId(jwt, demo);
        return ResponseEntity.ok(accountService.getUserAccounts(userId));
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody CreateAccountDto dto,
                                                    @RequestParam(required = false) boolean demo) {
        String userId = userContextService.getUserId(jwt, demo);
        return ResponseEntity.ok(accountService.createAccount(userId, dto));
    }
}