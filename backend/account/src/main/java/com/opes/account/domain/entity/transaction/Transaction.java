// com/opes/account/domain/entity/transaction/Transaction.java
package com.opes.account.domain.entity.transaction;

import com.opes.account.domain.entity.AppUser;
import com.opes.account.domain.entity.account.Account;
import com.opes.account.domain.entity.taxonomy.Category;
import com.opes.account.domain.entity.taxonomy.Merchant;
import com.opes.account.domain.entity.taxonomy.Tag;
import com.opes.account.domain.enums.TransactionSource;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

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
public class Transaction {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount; // + entrata, - uscita (EUR)

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

    @Column(name = "is_transfer", nullable = false)
    private boolean transfer;

    @Column(name = "transfer_group_id")
    private String transferGroupId;

    @Column(name = "is_refund", nullable = false)
    private boolean refund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_tx_id")
    private Transaction originalTransaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TransactionSource source;

    @Column(name = "external_id")
    private String externalId;

    // N:M con Tag
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "transaction_tag",
            joinColumns = @JoinColumn(name = "transaction_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Merchant getMerchant() { return merchant; }
    public void setMerchant(Merchant merchant) { this.merchant = merchant; }
    public boolean isTransfer() { return transfer; }
    public void setTransfer(boolean transfer) { this.transfer = transfer; }
    public String getTransferGroupId() { return transferGroupId; }
    public void setTransferGroupId(String transferGroupId) { this.transferGroupId = transferGroupId; }
    public boolean isRefund() { return refund; }
    public void setRefund(boolean refund) { this.refund = refund; }
    public Transaction getOriginalTransaction() { return originalTransaction; }
    public void setOriginalTransaction(Transaction originalTransaction) { this.originalTransaction = originalTransaction; }
    public TransactionSource getSource() { return source; }
    public void setSource(TransactionSource source) { this.source = source; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public Set<Tag> getTags() { return tags; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }
}
