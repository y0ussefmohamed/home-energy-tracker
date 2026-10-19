package com.projects.Alert.Service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public boolean sendEmail(String to, String subject, String body) {
        log.info("Sending email to {}, subject {}, body {}", to, subject, body);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom("noreply@joe.com");
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("Email sent to: {}", to);
            return true;
        } catch (MailException e) {
            log.error("Failed to send email to {}", to, e);
            return false;
        }
    }
}
