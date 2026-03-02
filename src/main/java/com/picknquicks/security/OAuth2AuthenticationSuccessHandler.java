package com.picknquicks.security;
import com.picknquicks.domain.user.Role;
import com.picknquicks.domain.user.User;
import com.picknquicks.repository.user.RoleRepository;
import com.picknquicks.repository.user.UserRepository;
import com.picknquicks.service.notification.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/auth/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = oauthToken.getPrincipal();

            String registrationId = oauthToken.getAuthorizedClientRegistrationId();

            Map<String, Object> attributes = oAuth2User.getAttributes();
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String picture = (String) attributes.get("picture");
            String providerId = (String) attributes.get("sub");

            AuthenticationType provider = AuthenticationType.valueOf(registrationId.toUpperCase());

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> registerOAuth2User(email, name, picture, provider, providerId));

            // Generate tokens
            UserPrincipal userPrincipal = new UserPrincipal(user);
            String accessToken = jwtTokenProvider.generateTokenFromEmail(
                    user.getEmail(),
                    user.getRoles().stream().map(role -> "ROLE_" + role.getName()).toList(),
                    jwtTokenProvider.getJwtExpiration() * 1000
            );

            String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token", accessToken)
                    .queryParam("email", user.getEmail())
                    .build()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }

    private User registerOAuth2User(String email, String name, String picture, AuthenticationType provider, String providerId) {
        String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"", ""};
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Customer role not found"));

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Random password for OAuth users
                .firstName(firstName)
                .lastName(lastName)
                .provider(provider)
                .providerId(providerId)
                .avatarUrl(picture)
                .enabled(true)
                .emailVerified(true) // OAuth providers verify emails
                .roles(Set.of(customerRole))
                .build();

        User savedUser = userRepository.save(user);

        emailService.sendWelcomeEmail(savedUser);

        log.info("New OAuth2 user registered: {} via {}", email, provider);

        return savedUser;
    }
}
