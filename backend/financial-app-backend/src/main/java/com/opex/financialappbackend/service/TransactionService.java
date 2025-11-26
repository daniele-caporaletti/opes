package com.opex.financialappbackend.service;

import com.opex.financialappbackend.domain.Account;
import com.opex.financialappbackend.domain.Transaction;
import com.opex.financialappbackend.domain.enums.TransactionCategory;
import com.opex.financialappbackend.domain.enums.TransactionStatus;
import com.opex.financialappbackend.domain.enums.TransactionType;
import com.opex.financialappbackend.dto.PaginatedResponse;
import com.opex.financialappbackend.dto.transaction.CreateTransactionDto;
import com.opex.financialappbackend.dto.transaction.TransactionDto;
import com.opex.financialappbackend.repository.AccountRepository;
import com.opex.financialappbackend.repository.TransactionRepository;
import com.opex.financialappbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @org.springframework.transaction.annotation.Transactional
    public void createTransaction(String userId, CreateTransactionDto dto) {
        // 1. Recupera Account
        Account account;
        if (dto.accountId() != null) {
            account = accountRepository.findById(dto.accountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
        } else {
            // Fallback: prendi il primo conto dell'utente
            List<Account> accounts = accountRepository.findByUserId(userId);
            if (accounts.isEmpty()) throw new RuntimeException("No accounts found for user");
            account = accounts.get(0);
        }

        // 2. Gestione Segno Importo
        BigDecimal finalAmount = dto.amount().abs(); // Assicuriamoci sia positivo
        if ("EXPENSE".equalsIgnoreCase(dto.type())) {
            finalAmount = finalAmount.negate(); // Diventa negativo per il DB
        }

        // 3. Salva Transazione
        Transaction tx = Transaction.builder()
                .user(userRepository.getReferenceById(userId))
                .account(account)
                .amount(finalAmount)
                .type(TransactionType.valueOf(dto.type().toUpperCase()))
                .category(TransactionCategory.valueOf(dto.category().toUpperCase()))
                .merchantName(dto.merchantName())
                .bookingDate(dto.date())
                .status(dto.status() != null ? TransactionStatus.valueOf(dto.status()) : TransactionStatus.COMPLETED)
                .description("Manual Entry")
                .excluded(false)
                .build();

        transactionRepository.save(tx);

        // 4. Aggiorna Saldo Conto (Opzionale ma consigliato)
        account.setBalance(account.getBalance().add(finalAmount));
        accountRepository.save(account);
    }

    public PaginatedResponse<TransactionDto> getTransactions(String userId, int page, int size) {
        // Crea la richiesta di pagina: ordina per data decrescente
        Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());

        // Esegui query
        Page<Transaction> transactionPage = transactionRepository.findByUserIdOrderByBookingDateDesc(userId, pageable);

        // Mappa Entità -> DTO
        var dtos = transactionPage.getContent().stream()
                .map(t -> new TransactionDto(
                        t.getId(),
                        t.getMerchantName(),
                        t.getAmount(),
                        t.getCategory().name(),
                        t.getType().name(),
                        t.getStatus().name()
                ))
                .toList();

        // Costruisci risposta paginata
        return new PaginatedResponse<>(
                dtos,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isLast()
        );
    }
}