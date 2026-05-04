package com.picknquicks.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mpesa")
@Data
public class MpesaConfig {

    private String consumerKey;
    private String consumerSecret;
    private String passKey;
    private String shortCode;
    private String callbackUrl;
    private String authUrl;
    private String stkPushUrl;
    private String queryUrl;
    private Integer timeout = 30;
}