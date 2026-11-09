package com.project.backend.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp, String purpose) {
        String subject = switch (purpose) {
            case "SIGNUP" -> "Your signup verification code";
            case "LOGIN" -> "Your login verification code";
            default -> "Your verification code";
        };

        String content = """
                <p>Hello,</p>
                <p>Your %s code is <b>%s</b>. It will expire in 5 minutes.</p>
                <p>If you didn't request this, please ignore this email.</p>
                """.formatted(purpose.toLowerCase(), otp);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}