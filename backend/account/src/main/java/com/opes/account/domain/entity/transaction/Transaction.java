// com/opes/account/domain/entity/transaction/Transaction.java
package com.opes.account.domain.entity.transaction;

import com.opes.account.domain.entity.AppUser;
import com.opes.account.domain.entity.account.Account;
import com.opes.account.domain.entity.taxonomy.Category;
import com.opes.account.domain.entity.taxonomy.Merchant;
import com.opes.account.domain.entity.taxonomy.Tag;
import com.opes.account.domain.enums.TransactionSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "transaction",
        indexes = {
                @Index(name = "idx_tx_user_date", columnList = "user_id,booking_date"),
                @Index(name = "idx_tx_user_cat_date", columnList = "user_id,category_id,booking_date"),
                @Index(name = "idx_tx_user_merchant_date", columnList = "user_id,merchant_id,booking_date"),
                @Index(name = "idx_tx_user_transfer", columnList = "user_id,is_transfer")
        }
)
@Getter @Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Owner (Keycloak sub)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    // Conto di appartenenza
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // Data di contabilizzazione (per “ultime 10 del mese” e aggregazioni)
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    // Importo in EUR (MVP): + entrata / - uscita
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    // Nota/descrizione libera
    @Column(name = "description", columnDefinition = "text")
    private String description;

    // Tassonomia (opzionali)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

    // Trasferimenti (esclusi da KPI): doppia scrittura A->B con stesso group id
    @Column(name = "is_transfer", nullable = false)
    private boolean transfer = false;

    @Column(name = "transfer_group_id")
    private String transferGroupId;

    // Rimborsi: positivi collegati a spesa originale (opzionale)
    @Column(name = "is_refund", nullable = false)
    private boolean refund = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_tx_id")
    private Transaction originalTransaction;

    // Provenienza dati
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 16)
    private TransactionSource source = TransactionSource.MANUAL;

    @Column(name = "external_id")
    private String externalId;

    // Tag (N:M)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "transaction_tag",
            joinColumns = @JoinColumn(name = "transaction_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}
