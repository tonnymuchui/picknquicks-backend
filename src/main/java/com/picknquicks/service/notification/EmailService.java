package com.picknquicks.service.notification;

import com.picknquicks.domain.user.User;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final Resend resend;
    private final TemplateEngine templateEngine;

    @Value("${resend.from-email}")
    private String fromEmail;

    @Value("${resend.from-name}")
    private String fromName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Async
    public void sendVerificationEmail(User user, String token) {
        String verificationUrl = frontendUrl + "/auth/verify-email?token=" + token;

        Context context = new Context();
        context.setVariable("firstName", user.getFirstName());
        context.setVariable("verificationUrl", verificationUrl);

        String htmlBody = templateEngine.process("email/verification", context);
        sendEmail(user.getEmail(), "Verify Your Email - PickNQuicks", htmlBody);
    }

    @Async
    public void sendPasswordResetEmail(User user, String token) {
        String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("firstName", user.getFirstName());
        context.setVariable("resetUrl", resetUrl);

        String htmlBody = templateEngine.process("email/password-reset", context);
        sendEmail(user.getEmail(), "Reset Your Password - PickNQuicks", htmlBody);
    }

    @Async
    public void sendWelcomeEmail(User user) {
        Context context = new Context();
        context.setVariable("firstName", user.getFirstName());
        context.setVariable("frontendUrl", frontendUrl);

        String htmlBody = templateEngine.process("email/welcome", context);
        sendEmail(user.getEmail(), "Welcome to PickNQuicks", htmlBody);
    }

    @Async
    public void sendStaffWelcomeEmail(User user, String temporaryPassword) {
        String loginUrl = frontendUrl + "/auth/login";

        Context context = new Context();
        context.setVariable("firstName", user.getFirstName());
        context.setVariable("temporaryPassword", temporaryPassword);
        context.setVariable("loginUrl", loginUrl);

        String htmlBody = templateEngine.process("email/staff-welcome", context);
        sendEmail(user.getEmail(), "Welcome to PickNQuicks Team", htmlBody);
    }

    private void sendEmail(String to, String subject, String htmlBody) {
        try {
            CreateEmailOptions request = CreateEmailOptions.builder()
                    .from(fromName + " <" + fromEmail + ">")
                    .to(to)
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            CreateEmailResponse response = resend.emails().send(request);
            log.info("Email sent successfully to {} - ID: {}", to, response.getId());

        } catch (ResendException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }
}