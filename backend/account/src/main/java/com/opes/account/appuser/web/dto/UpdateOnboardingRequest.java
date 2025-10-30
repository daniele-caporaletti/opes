// com/opes/account/appuser/web/dto/UpdateOnboardingRequest.java
package com.opes.account.appuser.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.opes.account.appuser.domain.enums.CurrentSituation;
import com.opes.account.appuser.domain.enums.EmotionalGoal;
import com.opes.account.appuser.domain.enums.MonthlyIncome;
import com.opes.account.appuser.domain.enums.SparkChoice;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateOnboardingRequest(
        SparkChoice sparkSelfRecognition,
        EmotionalGoal emotionalGoal,
        String emotionalGoalOther,
        CurrentSituation currentSituation,
        MonthlyIncome monthlyIncome
) {}
