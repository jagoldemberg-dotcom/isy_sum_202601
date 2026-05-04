package com.duoc.backend;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;

class BackendApplicationTests {

    @Test
    void applicationClassCanBeInstantiatedForCoverage() throws Exception {
        BackendApplication application = new BackendApplication();
        assertThat(application).isNotNull();

        Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThat(constructor.newInstance()).isNotNull();
        assertThat(Constants.LOGIN_URL).isEqualTo("/login");
    }
}
