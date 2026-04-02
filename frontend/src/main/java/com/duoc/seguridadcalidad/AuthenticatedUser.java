package com.duoc.seguridadcalidad;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.List;

public class AuthenticatedUser extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String jwtToken;
    private final String role;

    public AuthenticatedUser(String username, String jwtToken, String role) {
        super(username, "N/A", List.of(new SimpleGrantedAuthority(role)));
        this.jwtToken = jwtToken;
        this.role = role;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public String getRole() {
        return role;
    }

    public boolean isAdmin() {
        return getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
