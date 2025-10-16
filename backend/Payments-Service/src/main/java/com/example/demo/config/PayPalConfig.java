package com.example.demo.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "paypal")
@Data
public class PayPalConfig {
    private String clientId;
    private String clientSecret;
    private String mode;
    private String baseUrl;
    private String returnUrl;
    private String cancelUrl;
}