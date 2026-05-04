package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class SeguridadcalidadApplicationTests {

    @Test
    void applicationExposesRestTemplateBean() {
        SeguridadcalidadApplication application = new SeguridadcalidadApplication();
        RestTemplate restTemplate = application.restTemplate();

        assertThat(application).isNotNull();
        assertThat(restTemplate).isNotNull();
    }
}
