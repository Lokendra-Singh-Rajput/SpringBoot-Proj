package com.lokendra.springecom.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lokendra.springecom.model.User;
import com.lokendra.springecom.model.dto.ForgotPasswordRequest;
import com.lokendra.springecom.model.dto.LoginRequest;
import com.lokendra.springecom.model.dto.LoginResponse;
import com.lokendra.springecom.model.dto.RegisterRequest;
import com.lokendra.springecom.model.dto.ResendOtpRequest;
import com.lokendra.springecom.model.dto.ResetPasswordRequest;
import com.lokendra.springecom.model.dto.VerifyOtpRequest;
import com.lokendra.springecom.service.JwtService;
import com.lokendra.springecom.service.UserService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required."));
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required."));
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters long."));
        }

        try {
            User user = userService.register(request);
            Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("message", "Verification code sent to " + user.getEmail() + ". Please verify to complete registration.");
            resp.put("email", user.getEmail());
            resp.put("username", user.getUsername());
            if (user.getVerificationOtp() != null) {
                resp.put("otp", user.getVerificationOtp());
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-account")
    public ResponseEntity<?> verifyAccount(@RequestBody VerifyOtpRequest request) {
        if (request.getEmail() == null || request.getOtp() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and OTP code are required."));
        }

        try {
            User user = userService.verifyAccount(request);
            String token = jwtService.generateToken(user.getUsername(), user.getRole());
            return ResponseEntity.ok(new LoginResponse(token, user.getRole(), user.getUsername()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/resend-verification-otp")
    public ResponseEntity<?> resendVerificationOtp(@RequestBody ResendOtpRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required."));
        }

        try {
            userService.resendVerificationOtp(request.getEmail());
            User user = userService.findByEmail(request.getEmail().trim().toLowerCase()).orElse(null);
            Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("message", "A new verification code has been dispatched to your email.");
            if (user != null && user.getVerificationOtp() != null) {
                resp.put("otp", user.getVerificationOtp());
            }
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required."));
        }

        try {
            userService.forgotPassword(request);
            User user = userService.findByEmail(request.getEmail().trim().toLowerCase()).orElse(null);
            Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("message", "Password reset code sent to your email.");
            if (user != null && user.getResetPasswordOtp() != null) {
                resp.put("otp", user.getResetPasswordOtp());
            }
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getEmail() == null || request.getOtp() == null || request.getNewPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email, OTP code, and new password are required."));
        }

        try {
            userService.resetPassword(request);
            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully. You can now log in."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String identifier = request.getUsername();
        if (identifier == null || identifier.isBlank() || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username/email and password are required."));
        }

        User user = userService.findByUsernameOrEmail(identifier.trim().toLowerCase())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username/email or password."));
        }

        if (!user.isVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Account is not verified. Please verify your email.",
                    "requiresVerification", true,
                    "email", user.getEmail() != null ? user.getEmail() : ""
            ));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username/email or password."));
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(new LoginResponse(token, user.getRole(), user.getUsername()));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();
        User user = userService.findByUsername(username).orElseThrow();

        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "email", user.getEmail() != null ? user.getEmail() : "",
                "role", user.getRole()
        ));
    }
}
