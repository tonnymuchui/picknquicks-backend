package com.picknquicks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

@Configuration
public class TogglzConfig {

    @Bean
    public UserProvider togglzUserProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken) {
                return null;
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> "ADMIN".equals(authority.getAuthority()));

            return new SimpleFeatureUser(authentication.getName(), isAdmin);
        };
    }
}

