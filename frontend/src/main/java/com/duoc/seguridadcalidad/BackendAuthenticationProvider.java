package com.duoc.seguridadcalidad;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class BackendAuthenticationProvider implements AuthenticationProvider {

    private final RestClient backendRestClient;

    public BackendAuthenticationProvider(RestClient backendRestClient) {
        this.backendRestClient = backendRestClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials() == null ? "" : authentication.getCredentials().toString();

        try {
            FrontendLoginResponse response = backendRestClient.post()
                    .uri("/login")
                    .body(new FrontendLoginRequest(username, password))
                    .retrieve()
                    .body(FrontendLoginResponse.class);

            if (response == null || response.getToken() == null || response.getRole() == null) {
                throw new BadCredentialsException("No fue posible validar la sesión");
            }

            AuthenticatedUser principal = new AuthenticatedUser(response.getUsername(), response.getToken(), response.getRole());
            return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        } catch (RestClientException exception) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos", exception);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
