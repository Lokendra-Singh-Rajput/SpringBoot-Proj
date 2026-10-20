package com.lokendra.springecom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lokendra.springecom.model.User;
import com.lokendra.springecom.model.dto.ForgotPasswordRequest;
import com.lokendra.springecom.model.dto.LoginRequest;
import com.lokendra.springecom.model.dto.RegisterRequest;
import com.lokendra.springecom.model.dto.ResetPasswordRequest;
import com.lokendra.springecom.model.dto.VerifyOtpRequest;
import com.lokendra.springecom.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthFlowIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepo userRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void testCompleteRegistrationVerificationAndResetFlow() throws Exception {
        String testUser = "testuser_" + System.currentTimeMillis();
        String testEmail = testUser + "@example.com";
        String initialPassword = "initialPassword123";
        String newPassword = "newPassword456";

        RegisterRequest regReq = new RegisterRequest(testUser, testEmail, initialPassword);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(testEmail));

        User createdUser = userRepo.findByEmail(testEmail).orElseThrow();
        assertFalse(createdUser.isVerified());
        assertNotNull(createdUser.getVerificationOtp());
        String verificationOtp = createdUser.getVerificationOtp();

        LoginRequest unverifiedLogin = new LoginRequest(testUser, initialPassword);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unverifiedLogin)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.requiresVerification").value(true));

        VerifyOtpRequest verifyReq = new VerifyOtpRequest(testEmail, verificationOtp);
        mockMvc.perform(post("/auth/verify-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value(testUser));

        User verifiedUser = userRepo.findByEmail(testEmail).orElseThrow();
        assertTrue(verifiedUser.isVerified());
        assertNull(verifiedUser.getVerificationOtp());

        LoginRequest verifiedLogin = new LoginRequest(testEmail, initialPassword);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifiedLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        ForgotPasswordRequest forgotReq = new ForgotPasswordRequest(testEmail);
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotReq)))
                .andExpect(status().isOk());

        User forgotUser = userRepo.findByEmail(testEmail).orElseThrow();
        assertNotNull(forgotUser.getResetPasswordOtp());
        String resetOtp = forgotUser.getResetPasswordOtp();

        ResetPasswordRequest resetReq = new ResetPasswordRequest(testEmail, resetOtp, newPassword);
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isOk());

        LoginRequest newLogin = new LoginRequest(testUser, newPassword);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        userRepo.delete(forgotUser);
    }
}
