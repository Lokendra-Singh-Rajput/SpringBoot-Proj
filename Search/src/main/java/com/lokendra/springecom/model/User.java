package com.lokendra.springecom.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private boolean verified = false;

    private String verificationOtp;

    private LocalDateTime verificationOtpExpiry;

    private String resetPasswordOtp;

    private LocalDateTime resetPasswordOtpExpiry;

    public User() {
    }

    public User(Integer id, String username, String email, String password, String role, boolean verified) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.verified = verified;
    }
}
