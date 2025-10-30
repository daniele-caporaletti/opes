// com/opes/account/appuser/web/error/RestErrors.java
package com.opes.account.appuser.web.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class RestErrors {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ApiError("BAD_REQUEST", ex.getMessage()));
    }

    record ApiError(String code, String message) {}
}
