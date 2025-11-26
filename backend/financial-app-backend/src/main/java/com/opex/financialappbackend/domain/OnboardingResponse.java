package com.opex.financialappbackend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "onboarding_responses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "question_code", nullable = false)
    private String questionCode; // Es: "FINANCIAL_SITUATION"

    @Column(name = "selected_option")
    private String selectedOption; // Es: "STRESSED_PAYCHECK"

    @Column(name = "raw_value")
    private String rawValue; // Per risposte libere o "Other"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}