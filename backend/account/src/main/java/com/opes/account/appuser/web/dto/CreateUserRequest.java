// com/opes/account/appuser/web/dto/CreateUserRequest.java
package com.opes.account.appuser.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank @Email String email
) {}
