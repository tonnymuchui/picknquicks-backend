package com.picknquicks.config;

import com.resend.Resend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResendConfig {

    @Value("${resend.api-key:dummy-key-for-testing}")
    private String apiKey;

    @Bean
    @ConditionalOnProperty(name = "resend.api-key", matchIfMissing = true)
    public Resend resend() {
        return new Resend(apiKey);
    }
}