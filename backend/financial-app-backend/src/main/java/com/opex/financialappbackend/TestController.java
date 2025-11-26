package com.opex.financialappbackend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/me")
    public String getMyInfo(Principal principal) {
        // Se arrivi qui, Keycloak ha validato il token!
        return "Ciao, il tuo ID utente (sub) è: " + principal.getName();
    }
}
