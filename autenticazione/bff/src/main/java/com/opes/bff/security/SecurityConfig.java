package com.opes.bff.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

import static org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            ClientRegistrationRepository clients) throws Exception {

        // Logout OIDC → chiama end_session e poi torna alla home
        var oidcLogout = new OidcClientInitiatedLogoutSuccessHandler(clients);
        oidcLogout.setPostLogoutRedirectUri("{baseUrl}/");

        http
                // === Autorizzazioni ===
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/health", "/actuator/**", "/api/public/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())

                // === Login OIDC (pagina di ingresso al provider) ===
                .oauth2Login(o -> o.loginPage("/oauth2/authorization/keycloak"))
                .oauth2Client(Customizer.withDefaults())

                // === Logout locale + OIDC ===
                .logout(l -> l
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET")) // comodo in dev
                        .logoutSuccessHandler(oidcLogout)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true))

                // === API: 401 invece di redirect alla login ===
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new RegexRequestMatcher("^/api/.*", null)
                        )
                )

                // === CSRF: tienilo per le pagine; escludi le API JSON ===
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

                // === Security headers base ===
                .headers(h -> h
                                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                                .referrerPolicy(r -> r.policy(NO_REFERRER))
                                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                                .addHeaderWriter(new StaticHeadersWriter("X-XSS-Protection", "0"))
                                .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", "geolocation=(), microphone=(), camera=()"))
                        // .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000)) // abilita in HTTPS
                );

        return http.build();
    }
}
