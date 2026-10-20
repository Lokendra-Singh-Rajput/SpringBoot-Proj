package com.lokendra.springecom.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lokendra.springecom.model.User;
import com.lokendra.springecom.model.dto.ForgotPasswordRequest;
import com.lokendra.springecom.model.dto.RegisterRequest;
import com.lokendra.springecom.model.dto.ResetPasswordRequest;
import com.lokendra.springecom.model.dto.VerifyOtpRequest;
import com.lokendra.springecom.repo.UserRepo;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public User register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }

        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered.");
        }

        String otp = generateOtp();

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setVerified(false);
        user.setVerificationOtp(otp);
        user.setVerificationOtpExpiry(LocalDateTime.now().plusMinutes(10));

        User savedUser = userRepo.save(user);

        emailService.sendVerificationOtp(email, username, otp);

        return savedUser;
    }

    @Transactional
    public User verifyAccount(VerifyOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String otp = request.getOtp().trim();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found with email: " + email));

        if (user.isVerified()) {
            return user;
        }

        if (user.getVerificationOtp() == null || !user.getVerificationOtp().equals(otp)) {
            throw new IllegalArgumentException("Invalid verification code.");
        }

        if (user.getVerificationOtpExpiry() == null || LocalDateTime.now().isAfter(user.getVerificationOtpExpiry())) {
            throw new IllegalArgumentException("Verification code has expired. Please request a new code.");
        }

        user.setVerified(true);
        user.setVerificationOtp(null);
        user.setVerificationOtpExpiry(null);

        return userRepo.save(user);
    }

    @Transactional
    public void resendVerificationOtp(String email) {
        String cleanEmail = email.trim().toLowerCase();

        User user = userRepo.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("No account found with email: " + cleanEmail));

        if (user.isVerified()) {
            throw new IllegalArgumentException("Account is already verified.");
        }

        String otp = generateOtp();
        user.setVerificationOtp(otp);
        user.setVerificationOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepo.save(user);

        emailService.sendVerificationOtp(user.getEmail(), user.getUsername(), otp);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account registered with email: " + email));

        String otp = generateOtp();
        user.setResetPasswordOtp(otp);
        user.setResetPasswordOtpExpiry(LocalDateTime.now().plusMinutes(15));
        userRepo.save(user);

        emailService.sendPasswordResetOtp(user.getEmail(), user.getUsername(), otp);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String otp = request.getOtp().trim();
        String newPassword = request.getNewPassword();

        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account registered with email: " + email));

        if (user.getResetPasswordOtp() == null || !user.getResetPasswordOtp().equals(otp)) {
            throw new IllegalArgumentException("Invalid password reset code.");
        }

        if (user.getResetPasswordOtpExpiry() == null || LocalDateTime.now().isAfter(user.getResetPasswordOtpExpiry())) {
            throw new IllegalArgumentException("Password reset code has expired. Please request a new code.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordOtp(null);
        user.setResetPasswordOtpExpiry(null);
        user.setVerified(true);

        userRepo.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public Optional<User> findByUsernameOrEmail(String identifier) {
        return userRepo.findByUsernameOrEmail(identifier);
    }

    private String generateOtp() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }
}
