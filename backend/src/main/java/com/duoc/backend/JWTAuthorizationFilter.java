package com.duoc.backend;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private final JWTAuthenticationConfig jwtAuthenticationConfig;

    public JWTAuthorizationFilter(JWTAuthenticationConfig jwtAuthenticationConfig) {
        this.jwtAuthenticationConfig = jwtAuthenticationConfig;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean publicGetPath = HttpMethod.GET.matches(request.getMethod()) && (
                "/recipes".equals(path)
                        || "/recipes/latest".equals(path)
                        || "/recipes/popular".equals(path)
                        || "/recipes/search".equals(path)
        );
        return Constants.LOGIN_URL.equals(path) || publicGetPath;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(Constants.AUTHORIZATION_HEADER);

        if (header == null || !header.startsWith(Constants.BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(jwtAuthenticationConfig.getSecretKey())
                    .build()
                    .parseSignedClaims(header.replace(Constants.BEARER_PREFIX, ""))
                    .getPayload();

            List<String> authorities = claims.get("authorities", List.class);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(),
                    null,
                    authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException exception) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
