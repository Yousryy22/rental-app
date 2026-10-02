package com.rentalapp.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendBookingConfirmation(String toEmail, String propertyTitle, String startDate, String endDate) {
        send(toEmail, "Booking confirmed: " + propertyTitle,
                "Your booking for \"" + propertyTitle + "\" from " + startDate + " to " + endDate
                        + " has been confirmed. Thank you!");
    }

    public void sendBookingCancellation(String toEmail, String propertyTitle) {
        send(toEmail, "Booking cancelled: " + propertyTitle,
                "Your booking for \"" + propertyTitle + "\" has been cancelled.");
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // Never let a notification failure roll back the booking/payment transaction that triggered it.
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
