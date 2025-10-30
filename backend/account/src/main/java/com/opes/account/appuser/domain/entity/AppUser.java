// com/opes/account/appuser/domain/entity/AppUser.java
package com.opes.account.appuser.domain.entity;

import com.opes.account.appuser.domain.enums.CurrentSituation;
import com.opes.account.appuser.domain.enums.EmotionalGoal;
import com.opes.account.appuser.domain.enums.MonthlyIncome;
import com.opes.account.appuser.domain.enums.SparkChoice;
import com.opes.account.domain.entity.base.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "app_user",
        indexes = {
                @Index(name = "idx_app_user_email", columnList = "email")
        }
)
@Getter
@Setter
public class AppUser  extends Auditable {

    @Id
    @Column(length = 64, nullable = false, updatable = false) // Keycloak sub
    private String id;

    @Column(name = "email")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    // ---- Onboarding (tutte nullable = "Skip") ----
    @Enumerated(EnumType.STRING)
    @Column(name = "ob_spark")
    private SparkChoice sparkSelfRecognition;

    @Enumerated(EnumType.STRING)
    @Column(name = "ob_goal")
    private EmotionalGoal emotionalGoal;

    @Column(name = "ob_goal_other")
    private String emotionalGoalOther; // usato solo se emotionalGoal = OTHER

    @Enumerated(EnumType.STRING)
    @Column(name = "ob_situation")
    private CurrentSituation currentSituation;

    @Enumerated(EnumType.STRING)
    @Column(name = "ob_income")
    private MonthlyIncome monthlyIncome;

}
