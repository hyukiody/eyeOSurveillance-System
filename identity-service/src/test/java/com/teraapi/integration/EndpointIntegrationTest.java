package com.teraapi.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class EndpointIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Disabled("Integration test placeholder - requires full application context")
    public void contextLoads() {
        // Verify basic context loading
        assertThat(restTemplate).isNotNull();
    }
}

