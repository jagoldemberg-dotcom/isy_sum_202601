package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    @Test
    void passwordEncoderIsDelegatedToUserDetailsService() {
        MyUserDetailsService service = mock(MyUserDetailsService.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(service.passwordEncoder()).thenReturn(encoder);

        WebSecurityConfig config = new WebSecurityConfig(mock(JWTAuthorizationFilter.class), service);

        assertThat(config.passwordEncoder()).isSameAs(encoder);
    }
}
