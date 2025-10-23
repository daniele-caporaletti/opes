package com.opes.account.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "goals", indexes = {
        @Index(name="idx_goal_account_status", columnList = "account_id, status")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Goal {

    public enum Status { ACTIVE, PAUSED, ACHIEVED, CANCELLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(name = "target_cents", nullable = false)
    private long targetCents;

    @Column(name = "saved_cents",  nullable = false)
    private long savedCents;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist(){ var now=Instant.now(); createdAt=now; updatedAt=now; }
    @PreUpdate
    void preUpdate(){ updatedAt=Instant.now(); }
}
