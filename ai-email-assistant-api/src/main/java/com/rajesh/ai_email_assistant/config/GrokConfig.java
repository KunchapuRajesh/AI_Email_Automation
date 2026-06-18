package com.rajesh.ai_email_assistant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class GrokConfig {

    @Value("${grok.api.key}")
    private String apiKey;

    @Value("${grok.api.url:https://api.x.ai/v1/chat/completions}")
    private String apiUrl;

    @Value("${grok.model:grok-3-mini}")
    private String model;
}