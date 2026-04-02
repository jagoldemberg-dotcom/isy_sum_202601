package com.duoc.backend;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
public class LoginController {

    private final JWTAuthenticationConfig jwtAuthenticationConfig;
    private final MyUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public LoginController(JWTAuthenticationConfig jwtAuthenticationConfig,
                           MyUserDetailsService userDetailsService,
                           PasswordEncoder passwordEncoder) {
        this.jwtAuthenticationConfig = jwtAuthenticationConfig;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping(Constants.LOGIN_URL)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            User user = (User) userDetailsService.loadUserByUsername(loginRequest.getUsername());

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new ResponseStatusException(UNAUTHORIZED, "Credenciales inválidas");
            }

            String token = jwtAuthenticationConfig.getJWTToken(user.getUsername(), user.getRole());
            return ResponseEntity.ok(new LoginResponse(token, user.getUsername(), user.getRole()));
        } catch (UsernameNotFoundException | IllegalArgumentException ex) {
            throw new ResponseStatusException(UNAUTHORIZED, "Credenciales inválidas");
        }
    }
}