package com.opex.financialappbackend.service;

import com.opex.financialappbackend.domain.Account;
import com.opex.financialappbackend.domain.User;
import com.opex.financialappbackend.dto.account.AccountDto;
import com.opex.financialappbackend.dto.account.CreateAccountDto;
import com.opex.financialappbackend.repository.AccountRepository;
import com.opex.financialappbackend.repository.UserRepository;
import com.opex.financialappbackend.domain.enums.AccountType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public List<AccountDto> getUserAccounts(String userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(a -> new AccountDto(
                        a.getId(),
                        a.getName(),
                        a.getInstitutionName(),
                        a.getBalance(),
                        a.getCurrency(),
                        a.getType() != null ? a.getType().name() : "OTHER" // Gestione null safe
                ))
                .toList();
    }

    @Transactional
    public AccountDto createAccount(String userId, CreateAccountDto dto) {
        User user = userRepository.getReferenceById(userId);

        // Default type se non specificato
        AccountType type = dto.type() != null ? AccountType.valueOf(dto.type()) : AccountType.CASH;

        Account account = Account.builder()
                .user(user)
                .name(dto.name())
                .institutionName(dto.institutionName())
                .balance(dto.balance())
                .currency(dto.currency() != null ? dto.currency() : "EUR")
                .type(type) // <--- Salvataggio
                .build();

        Account saved = accountRepository.save(account);

        return new AccountDto(
                saved.getId(),
                saved.getName(),
                saved.getInstitutionName(),
                saved.getBalance(),
                saved.getCurrency(),
                saved.getType().name()
        );
    }
}