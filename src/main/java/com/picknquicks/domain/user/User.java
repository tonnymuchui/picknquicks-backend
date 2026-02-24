package com.picknquicks.domain.user;

import com.picknquicks.security.AuthenticationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    @Column(nullable = false, unique = true, length = 128)
    private String email;
    @Column(nullable = false, length = 128)
    private String password;
    @Column(name = "first_name", nullable = false, length = 64)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 64)
    private String lastName;

    @Column(length = 15)
    private String phone;

    @Column(length = 64)
    private String photos;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "account_locked")
    private Boolean accountLocked = false;
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // Authentication type for this user (LOCAL, GOOGLE, FACEBOOK, etc.)
    @Enumerated(EnumType.STRING)
    @Column(name = "authentication_type", length = 20)
    private AuthenticationType authenticationType = AuthenticationType.DATABASE;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "reset_password_token", length = 64)
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expiry")
    private LocalDateTime resetPasswordTokenExpiry;

    @Column(name = "verification_code", length = 64)
    private String verificationCode;

    @Column(name = "verification_code_expiry")
    private LocalDateTime verificationCodeExpiry;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "User [id=" + id + ", email=" + email + ", firstName=" + firstName +
                ", lastName=" + lastName + ", roles=" + roles + ", enabled=" + enabled + "]";
    }

    @Transient
    public String getPhotosImagePath() {
        if (id == null || photos == null) return "/uploads/default-user.png";
        return "/uploads/user-photos/" + this.id + "/" + this.photos;
    }

    @Transient
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }

    public boolean hasAnyRole(String... roleNames) {
        for (String roleName : roleNames) {
            if (hasRole(roleName)) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public Role getPrimaryRole() {
        return roles.isEmpty() ? null : roles.iterator().next();
    }

    @Transient
    public boolean isActive() {
        return enabled && !accountLocked;
    }

    @Transient
    public boolean isAdmin() {
        return hasRole("Admin");
    }

    @Transient
    public boolean isSalesperson() {
        return hasRole("SALESPERSON");
    }

    @Transient
    public boolean isDeliveryPerson() {
        return hasRole("Delivery Person");
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null) ? 1 : this.failedLoginAttempts + 1;
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    public void lockAccount() {
        this.accountLocked = true;
    }

    public void unlockAccount() {
        this.accountLocked = false;
        resetFailedLoginAttempts();
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
}
