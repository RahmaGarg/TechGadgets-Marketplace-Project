package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "konnect")
@Data
public class KonnectConfig {
    private String apiKey;
    private String walletId;
    private String baseUrl;
    private String mode;
}