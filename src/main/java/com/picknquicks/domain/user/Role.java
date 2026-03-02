package com.picknquicks.domain.user;

import com.picknquicks.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name; // ADMIN, CUSTOMER, STAFF, MANAGER

    @Column(length = 255)
    private String description;

    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<User> users = new HashSet<>();

    // Predefined role names
    public static final String ADMIN = "ADMIN";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String STAFF = "STAFF";
    public static final String MANAGER = "MANAGER";

    @Override
    public String toString() {
        return "Role{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return name != null && name.equals(role.getName());
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}