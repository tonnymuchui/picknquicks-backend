package com.picknquicks.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.picknquicks.security.AuthenticationType;
import com.picknquicks.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_provider", columnList = "provider, provider_id"),
        @Index(name = "idx_user_enabled", columnList = "enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", nullable = false, length = 64)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 64)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "account_locked")
    @Builder.Default
    private Boolean accountLocked = false;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 20, nullable = false)
    @Builder.Default
    private AuthenticationType provider = AuthenticationType.DATABASE;

    @Column(name = "provider_id")
    private String providerId; // OAuth provider's user ID


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Transient
    public boolean isActive() {
        return enabled && emailVerified && !accountLocked;
    }

    @Transient
    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(roleName));
    }

    @Transient
    public boolean hasAnyRole(String... roleNames) {
        for (String roleName : roleNames) {
            if (hasRole(roleName)) return true;
        }
        return false;
    }

    @Transient
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    @Transient
    public boolean isCustomer() {
        return hasRole("CUSTOMER");
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 10) {
            this.accountLocked = true;
        }
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", provider=" + provider +
                ", enabled=" + enabled +
                ", roles=" + roles.size() +
                '}';
    }
}