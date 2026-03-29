package com.duoc.backend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private final JWTAuthorizationFilter jwtAuthorizationFilter;
    private final MyUserDetailsService userDetailsService;

    public WebSecurityConfig(JWTAuthorizationFilter jwtAuthorizationFilter,
                             MyUserDetailsService userDetailsService) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return userDetailsService.passwordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Corrección: Habilitar encabezados de seguridad (CSP, X-Frame-Options, X-Content-Type-Options)
                .headers(headers -> headers
                        // Mitigación Anti-Clickjacking
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                        // Mitigación MIME-sniffing
                        .contentTypeOptions(contentTypeOptions -> {})
                        // Mitigación XSS forzando CSP
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self'"))
                        // Mitigación HSTS (Strict-Transport-Security)
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                )
                // Reglas de autorización existentes...
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/", "/home", "/**.css").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login").permitAll()
                )
                .logout((logout) -> logout.permitAll());

        return http.build();
    }
}
