// com/opes/account/appuser/web/dto/UpdateProfileRequest.java
package com.opes.account.appuser.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateProfileRequest(
        String firstName,
        String lastName,
        @Past LocalDate birthDate
) {}
