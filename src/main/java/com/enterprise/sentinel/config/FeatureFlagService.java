package com.enterprise.sentinel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FeatureFlagService {

    @Value("${app.ai.providers.anthropic.enabled-for-all-clients:false}")
    private boolean anthropicHaikuEnabledForAllClients;

    @Value("${app.ai.providers.anthropic.model:}")
    private String anthropicHaikuModel;

    public boolean isClaudeHaikuEnabledForAllClients() {
        return anthropicHaikuEnabledForAllClients;
    }

    public String getClaudeHaikuModel() {
        return anthropicHaikuModel;
    }
}
