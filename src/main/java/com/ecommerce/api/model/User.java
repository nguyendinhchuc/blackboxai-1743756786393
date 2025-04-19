package com.ecommerce.api.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    private String password;

    @NotBlank
    @Size(max = 50)
    @Column(name = "first_name")
    private String firstName;

    @NotBlank
    @Size(max = 50)
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "tenant_id")
    private Long tenantId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "is_email_verified")
    private Boolean isEmailVerified;

    @Column(name = "is_two_factor_enabled")
    private boolean isTwoFactorEnabled;

    @Column(name = "last_logout_at")
    private LocalDateTime lastLogoutAt;

    public User() {
        this.isEmailVerified = true;
        this.isTwoFactorEnabled = false;
        this.avatar = "";
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = "";
        this.lastName = "";
        this.isEmailVerified = true;
        this.isTwoFactorEnabled = true;
    }

    public User(String username, String email, String password, Long tenantId, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = "";
        this.lastName = "";
        this.tenantId = tenantId;
        this.role = role;
        this.isEmailVerified = true;
        this.isTwoFactorEnabled = false;
        this.avatar = "";
    }
}
