package com.opes.account.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "financial_accounts", indexes = {
        @Index(name="idx_fa_account", columnList = "account_id")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FinancialAccount {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    private String name;          // es. "Main Current"
    @Column(nullable = false, length = 32)
    private String type; // CURRENT|CARD|SAVINGS

    @Column(name = "balance_cents", nullable = false)
    private long balanceCents; // EUR in cent
    @Column(name = "balance_updated_at")
    private Instant balanceUpdatedAt;

    @Column(name = "excluded_from_total", nullable = false)
    private boolean excludedFromTotal;
}
