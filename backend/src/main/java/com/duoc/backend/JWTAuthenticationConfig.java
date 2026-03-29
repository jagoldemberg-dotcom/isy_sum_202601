package com.duoc.backend;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JWTAuthenticationConfig {

    private final SecretKey secretKey;
    private final long tokenExpirationMs;

    public JWTAuthenticationConfig(@Value("${app.jwt.secret}") String secret,
                                   @Value("${app.jwt.expiration-ms}") long tokenExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenExpirationMs = tokenExpirationMs;
    }

    public String getJWTToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", List.of(role));

        String token = Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpirationMs))
                .and()
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        return Constants.BEARER_PREFIX + token;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}
