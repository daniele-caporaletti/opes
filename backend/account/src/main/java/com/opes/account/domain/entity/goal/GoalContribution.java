// com/opes/account/domain/entity/goal/GoalContribution.java
package com.opes.account.domain.entity.goal;

import com.opes.account.domain.entity.AppUser;
import com.opes.account.domain.entity.transaction.Transaction;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "goal_contribution",
        indexes = @Index(name = "idx_goal_contribution_date", columnList = "goal_id,contribution_date"))
public class GoalContribution {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction; // nullable

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount; // + verso goal, - prelievo

    @Column(name = "contribution_date", nullable = false)
    private LocalDate contributionDate;

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Goal getGoal() { return goal; }
    public void setGoal(Goal goal) { this.goal = goal; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getContributionDate() { return contributionDate; }
    public void setContributionDate(LocalDate contributionDate) { this.contributionDate = contributionDate; }
}
