package com.duoc.backend;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationAndSecurityTest {

    private static final String SECRET = "SeguridadCalidadJWTSecretKey2026SeguridadCalidadJWT!";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void jwtConfigGeneratesBearerTokenWithSubjectAndRole() {
        JWTAuthenticationConfig config = new JWTAuthenticationConfig(SECRET, 60000);

        String token = config.getJWTToken("chefana", Constants.USER_ROLE);

        assertThat(token).startsWith(Constants.BEARER_PREFIX);
        assertThat(config.getSecretKey()).isNotNull();
    }

    @Test
    void authorizationFilterSkipsPublicPaths() throws ServletException, IOException {
        JWTAuthorizationFilter filter = new JWTAuthorizationFilter(new JWTAuthenticationConfig(SECRET, 60000));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/recipes/latest");
        request.setServletPath("/recipes/latest");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void authorizationFilterAuthenticatesValidJwt() throws ServletException, IOException {
        JWTAuthenticationConfig config = new JWTAuthenticationConfig(SECRET, 60000);
        JWTAuthorizationFilter filter = new JWTAuthorizationFilter(config);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/recipes/1/comments");
        request.setServletPath("/recipes/1/comments");
        request.addHeader(Constants.AUTHORIZATION_HEADER, config.getJWTToken("chefana", Constants.USER_ROLE));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("chefana");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly(Constants.USER_ROLE);
    }

    @Test
    void authorizationFilterClearsInvalidJwtAndContinuesChain() throws ServletException, IOException {
        JWTAuthorizationFilter filter = new JWTAuthorizationFilter(new JWTAuthenticationConfig(SECRET, 60000));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/recipes/1/comments");
        request.setServletPath("/recipes/1/comments");
        request.addHeader(Constants.AUTHORIZATION_HEADER, Constants.BEARER_PREFIX + "invalid.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
