// com/opes/account/domain/entity/account/Account.java
package com.opes.account.domain.entity.account;

import com.opes.account.domain.entity.AppUser;
import com.opes.account.domain.enums.AccountProvider;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "account")
public class Account {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AccountProvider provider;

    @Column(name = "provider_account_id")
    private String providerAccountId; // nullable

    @Column(nullable = false)
    private String name;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode = "EUR";

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public AccountProvider getProvider() { return provider; }
    public void setProvider(AccountProvider provider) { this.provider = provider; }
    public String getProviderAccountId() { return providerAccountId; }
    public void setProviderAccountId(String providerAccountId) { this.providerAccountId = providerAccountId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
