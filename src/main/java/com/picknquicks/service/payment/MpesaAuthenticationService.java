package com.picknquicks.service.payment;

import com.picknquicks.config.MpesaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Service for M-Pesa authentication and token management.
 * Single Responsibility: Only handles M-Pesa protocol-level authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaAuthenticationService {

    private final MpesaConfig mpesaConfig;
    private final RestTemplate restTemplate;

    /**
     * Obtain access token from M-Pesa OAuth endpoint
     * Uses Consumer Key and Consumer Secret for authentication
     */
    public String obtainAccessToken() {
        String auth = mpesaConfig.getConsumerKey() + ":" + mpesaConfig.getConsumerSecret();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(encodedAuth);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                    mpesaConfig.getAuthUrl(),
                    HttpMethod.GET,
                    entity,
                    AccessTokenResponse.class
            );

            if (response.getBody() != null && response.getBody().getAccessToken() != null) {
                log.debug("✅ M-Pesa access token obtained successfully");
                return response.getBody().getAccessToken();
            }

            log.error("❌ Failed to get M-Pesa access token: Empty response");
            throw new RuntimeException("Failed to get M-Pesa access token: Empty response from M-Pesa API");
        } catch (Exception e) {
            log.error("❌ M-Pesa Authentication Failed: Check your Consumer Key and Consumer Secret", e);
            throw new RuntimeException("Failed to get M-Pesa access token. Check M-Pesa credentials: " + e.getMessage(), e);
        }
    }

    /**
     * Generate timestamp in M-Pesa required format (yyyyMMddHHmmss)
     */
    public String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * Generate password for M-Pesa STK push
     * Format: Base64(ShortCode + PassKey + Timestamp)
     */
    public String generatePassword(String timestamp) {
        String str = mpesaConfig.getShortCode() + mpesaConfig.getPassKey() + timestamp;
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Format phone number to M-Pesa required format (254XXXXXXXXX)
     * Handles multiple input formats:
     * - +254XXXXXXXXX -> 254XXXXXXXXX
     * - 0XXXXXXXXX -> 254XXXXXXXXX
     * - 254XXXXXXXXX -> 254XXXXXXXXX (no change)
     */
    public String formatPhoneNumber(String phone) {
        if (phone.startsWith("+254")) {
            return phone.substring(1);
        } else if (phone.startsWith("0")) {
            return "254" + phone.substring(1);
        } else if (phone.startsWith("254")) {
            return phone;
        }
        return "254" + phone;
    }

    /**
     * DTO for M-Pesa access token response
     */
    private static class AccessTokenResponse {
        private String access_token;

        public String getAccessToken() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }
    }
}

