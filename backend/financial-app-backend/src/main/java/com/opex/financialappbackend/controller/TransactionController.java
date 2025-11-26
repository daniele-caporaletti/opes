package com.opex.financialappbackend.controller;

import com.opex.financialappbackend.domain.enums.TransactionCategory;
import com.opex.financialappbackend.dto.PaginatedResponse;
import com.opex.financialappbackend.dto.transaction.CreateTransactionDto;
import com.opex.financialappbackend.dto.transaction.TransactionDto;
import com.opex.financialappbackend.service.TransactionService;
import com.opex.financialappbackend.service.UserContextService;
import jakarta.validation.Valid; // Importante per validazione DTO
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserContextService userContextService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<TransactionDto>> getTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) boolean demo
    ) {
        String userId = userContextService.getUserId(jwt, demo);
        return ResponseEntity.ok(transactionService.getTransactions(userId, page, size));
    }

    @PostMapping
    public ResponseEntity<String> createTransaction(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody CreateTransactionDto dto,
                                                    @RequestParam(required = false) boolean demo) {
        String userId = userContextService.getUserId(jwt, demo);
        transactionService.createTransaction(userId, dto);
        return ResponseEntity.ok("Transaction created");
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(Arrays.stream(TransactionCategory.values()).map(Enum::name).toList());
    }
}