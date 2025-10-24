// com/opes/account/domain/entity/account/AccountBalanceSnapshot.java
package com.opes.account.domain.entity.account;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_balance_snapshot",
        indexes = @Index(name = "idx_abs_account_asof", columnList = "account_id,as_of"))
public class AccountBalanceSnapshot {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "as_of", nullable = false)
    private LocalDateTime asOf;

    @Column(name = "balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public LocalDateTime getAsOf() { return asOf; }
    public void setAsOf(LocalDateTime asOf) { this.asOf = asOf; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
