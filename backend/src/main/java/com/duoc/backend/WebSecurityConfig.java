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
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.POST, Constants.LOGIN_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, "/recipes", "/recipes/latest", "/recipes/popular", "/recipes/search").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/recipes/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
