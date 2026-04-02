package com.duoc.seguridadcalidad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    private final BackendAuthenticationProvider backendAuthenticationProvider;

    public WebSecurityConfig(BackendAuthenticationProvider backendAuthenticationProvider) {
        this.backendAuthenticationProvider = backendAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(backendAuthenticationProvider)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/", "/home", "/buscar", "/login", "/css/**", "/images/**", "/app.css").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "img-src 'self' https://images.unsplash.com data:; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "script-src 'self'; " +
                                        "object-src 'none'; " +
                                        "base-uri 'self'; " +
                                        "frame-ancestors 'none'; " +
                                        "form-action 'self'"
                        ))
                        .frameOptions(frame -> frame.deny())
                );

        return http.build();
    }
}
