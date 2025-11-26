package com.opex.financialappbackend.domain;

import com.opex.financialappbackend.domain.enums.TransactionCategory;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "goals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;

    @Enumerated(EnumType.STRING)
    private TransactionCategory category;

    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadline;
}