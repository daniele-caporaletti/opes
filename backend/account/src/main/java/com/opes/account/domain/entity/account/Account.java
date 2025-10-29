// com/opes/account/domain/entity/account/Account.java
package com.opes.account.domain.entity.account;

import com.opes.account.domain.entity.AppUser;
import com.opes.account.domain.enums.AccountProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "account")
@Getter @Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AccountProvider provider = AccountProvider.MANUAL;

    @Column(name = "provider_account_id")
    private String providerAccountId; // nullable, id dall’aggregatore

    @Column(nullable = false)
    private String name;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode = "EUR"; // MVP: sempre EUR

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
