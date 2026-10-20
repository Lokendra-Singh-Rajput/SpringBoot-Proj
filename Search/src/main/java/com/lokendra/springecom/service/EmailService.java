package com.lokendra.springecom.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@ecom.local}")
    private String fromEmail;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    public void sendVerificationOtp(String toEmail, String username, String otp) {
        String subject = "Verify Your Account - Lokendra E-Commerce";
        String htmlContent = buildEmailTemplate(
                "Welcome to Lokendra E-Commerce!",
                "Hello <b>" + username + "</b>,",
                "Thank you for signing up. Please use the verification code below to verify your email address and activate your account. This code is valid for <b>10 minutes</b>.",
                otp,
                "If you did not create an account, please disregard this email."
        );

        sendEmail(toEmail, subject, htmlContent, otp, "ACCOUNT VERIFICATION");
    }

    public void sendPasswordResetOtp(String toEmail, String username, String otp) {
        String subject = "Password Reset Request - Lokendra E-Commerce";
        String htmlContent = buildEmailTemplate(
                "Reset Your Password",
                "Hello <b>" + username + "</b>,",
                "We received a request to reset your password. Use the verification code below to proceed with resetting your password. This code expires in <b>15 minutes</b>.",
                otp,
                "If you did not request a password reset, please ignore this email or secure your account."
        );

        sendEmail(toEmail, subject, htmlContent, otp, "PASSWORD RESET");
    }

    private void sendEmail(String toEmail, String subject, String htmlContent, String otp, String type) {
        log.info("\n=======================================================\n" +
                 " [EMAIL SERVICE - " + type + "]\n" +
                 " To: " + toEmail + "\n" +
                 " OTP Code: " + otp + "\n" +
                 "=======================================================\n");

        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            log.info("SMTP credentials not configured. OTP printed above for testing.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email successfully dispatched to {}", toEmail);
        } catch (Exception e) {
            log.warn("SMTP send skipped ({}); OTP is available above for verification.", e.getMessage());
        }
    }

    private String buildEmailTemplate(String header, String greeting, String message, String otp, String footer) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; background-color: #f4f6f8; margin: 0; padding: 24px;'>" +
                "  <div style='max-width: 520px; margin: 0 auto; background: #ffffff; border-radius: 12px; padding: 32px; box-shadow: 0 4px 16px rgba(0,0,0,0.06);'>" +
                "    <h2 style='color: #4f46e5; margin-top: 0; font-size: 24px; text-align: center;'>" + header + "</h2>" +
                "    <p style='color: #374151; font-size: 15px; line-height: 1.5;'>" + greeting + "</p>" +
                "    <p style='color: #4b5563; font-size: 14px; line-height: 1.6;'>" + message + "</p>" +
                "    <div style='text-align: center; margin: 28px 0;'>" +
                "      <div style='display: inline-block; background: #f3f4f6; border: 2px dashed #6366f1; border-radius: 8px; padding: 12px 28px; letter-spacing: 6px; font-size: 28px; font-weight: 700; color: #1e1b4b;'>" +
                otp +
                "      </div>" +
                "    </div>" +
                "    <p style='color: #6b7280; font-size: 13px; line-height: 1.5; text-align: center;'>" + footer + "</p>" +
                "    <hr style='border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;' />" +
                "    <p style='color: #9ca3af; font-size: 12px; text-align: center; margin: 0;'>Lokendra E-Commerce Platform</p>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }
}
