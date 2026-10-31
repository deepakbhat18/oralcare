package com.dental.clinic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.notification.email-from:noreply@dentalclinic.com}")
    private String fromEmail;

    @Async
    public void sendRegistrationOtpEmail(String toEmail, String otp) {
        String subject = "OralCare - Verify Your Email";
        String body = """
                Hello,

                Thank you for registering with OralCare!

                Your Email Verification OTP is:

                ──────────────
                  %s
                ──────────────

                This OTP is valid for 5 minutes. Do not share it with anyone.

                If you did not create an account, please ignore this email.

                Best regards,
                OralCare Team
                """.formatted(otp);
        sendSimpleEmail(toEmail, subject, body);
    }

    @Async
    public void sendPasswordResetOtpEmail(String toEmail, String otp) {
        String subject = "OralCare - Password Reset OTP";
        String body = """
                Hello,

                You requested a password reset for your OralCare account.

                Your Password Reset OTP is:

                ──────────────
                  %s
                ──────────────

                This OTP is valid for 10 minutes. Do not share it with anyone.

                If you did not request this, please ignore this email.

                Best regards,
                OralCare Team
                """.formatted(otp);
        sendSimpleEmail(toEmail, subject, body);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String subject = "OralCare - Password Reset Request";
        String resetLink = "https://dentalclinic.com/reset-password?token=" + resetToken;
        String body = """
                Hello,

                You requested a password reset. Click the link below (valid 15 minutes):
                %s

                If you did not request this, please ignore this email.

                Best regards,
                OralCare Team
                """.formatted(resetLink);
        sendSimpleEmail(toEmail, subject, body);
    }

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        sendPasswordResetOtpEmail(toEmail, otp);
    }

    @Async
    public void sendEmailVerification(String toEmail, String token) {
        String subject = "OralCare - Verify Your Email";
        String verifyLink = "https://dentalclinic.com/verify-email?token=" + token;
        String body = """
                Hello,

                Welcome to OralCare! Please verify your email:
                %s

                This link is valid for 24 hours.

                Best regards,
                OralCare Team
                """.formatted(verifyLink);
        sendSimpleEmail(toEmail, subject, body);
    }

    @Async
    public void sendAppointmentConfirmation(String toEmail, String patientName,
                                            String dateTime, String treatmentType) {
        String subject = "OralCare - Appointment Confirmation";
        String body = """
                Dear %s,

                Your appointment has been confirmed:
                Treatment: %s
                Date & Time: %s

                Please arrive 10 minutes early.

                Best regards,
                OralCare Team
                """.formatted(patientName, treatmentType, dateTime);
        sendSimpleEmail(toEmail, subject, body);
    }

    @Async
    public void sendAppointmentReminder(String toEmail, String patientName,
                                        String dateTime, String treatmentType) {
        String subject = "OralCare - Appointment Reminder";
        String body = """
                Dear %s,

                Reminder for your upcoming appointment:
                Treatment: %s
                Date & Time: %s

                Best regards,
                OralCare Team
                """.formatted(patientName, treatmentType, dateTime);
        sendSimpleEmail(toEmail, subject, body);
    }

    @Async
    public void sendBillNotification(String toEmail, String patientName,
                                     String invoiceNumber, Double amount) {
        String subject = "OralCare - Invoice " + invoiceNumber;
        String body = """
                Dear %s,

                Invoice Number: %s
                Total Amount: $%.2f

                Please log in to view and pay your bill.

                Best regards,
                OralCare Team
                """.formatted(patientName, invoiceNumber, amount);
        sendSimpleEmail(toEmail, subject, body);
    }

    @Async
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("HTML email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", toEmail, e.getMessage());
        }
    }

    private void sendSimpleEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {} | Subject: {}", toEmail, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}

/*
 * This file contains the EmailService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
