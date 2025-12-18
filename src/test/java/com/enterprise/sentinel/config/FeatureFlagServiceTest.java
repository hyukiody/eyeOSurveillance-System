package com.enterprise.sentinel.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = FeatureFlagService.class)
@TestPropertySource(properties = {
        "app.ai.providers.anthropic.enabled-for-all-clients=true",
        "app.ai.providers.anthropic.model=claude-haiku-4.5"
})
class FeatureFlagServiceTest {

    @Autowired
    private FeatureFlagService featureFlagService;

    @Test
    void shouldEnableClaudeHaikuForAllClients() {
        assertTrue(featureFlagService.isClaudeHaikuEnabledForAllClients());
    }

    @Test
    void shouldExposeConfiguredModelName() {
        assertEquals("claude-haiku-4.5", featureFlagService.getClaudeHaikuModel());
    }
}
