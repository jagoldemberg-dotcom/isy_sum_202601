package com.duoc.seguridadcalidad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/", "/home", "/buscar", "/login", "/css/**", "/images/**", "/app.css").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService users(PasswordEncoder passwordEncoder) {
        UserDetails userOne = User.builder()
                .username("chefana")
                .password(passwordEncoder.encode("Receta123!"))
                .roles("USER")
                .build();

        UserDetails userTwo = User.builder()
                .username("martinchef")
                .password(passwordEncoder.encode("Cocina123!"))
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("adminrecetas")
                .password(passwordEncoder.encode("Admin123!"))
                .roles("USER", "ADMIN")
                .build();

        return new InMemoryUserDetailsManager(userOne, userTwo, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
