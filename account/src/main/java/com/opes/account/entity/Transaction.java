package com.opes.account.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name="idx_tx_account_date", columnList="account_id, booking_ts"),
        @Index(name="idx_tx_account_kind_date", columnList="account_id, kind, booking_ts")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    public enum Kind { INCOME, EXPENSE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "financial_account_id", nullable = false)
    private FinancialAccount financialAccount;

    @Column(name = "booking_ts", nullable = false)
    private Instant bookingTs;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents; // +income, -expense

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 8)
    private Kind kind;

    @Column(name = "category_code", length = 64)
    private String categoryCode; // es. "FOOD_GROCERIES"

    @Column(name = "merchant_name", length = 160)

    private String merchantName;

    @Column(length = 255)
    private String description;
}
