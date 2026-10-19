package com.projects.Alert.Service.service;

import com.projects.Alert.Service.entity.Alert;
import com.projects.Alert.Service.repository.AlertRepository;
import com.projects.kafka.event.AlertingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {
    private final EmailService emailService;
    private final AlertRepository alertRepository;

    @KafkaListener(topics = "energy-alerts", groupId = "alert-service")
    public void consumeAlertingEvent(AlertingEvent alertingEvent) {
        log.info("Consumed alerting event {}", alertingEvent);

        final String subject = "Energy Usage Alert for User " + alertingEvent.getUserId();
        final String message = "Alert: " + alertingEvent.getMessage() +
                "\nThreshold: " + alertingEvent.getThreshold() +
                "\nEnergy Consumed: " + alertingEvent.getEnergyConsumed();

        boolean isEmailSent = emailService.sendEmail(alertingEvent.getEmail(), subject, message);

        Alert alert = Alert.builder()
                .userId(alertingEvent.getUserId())
                .createdAt(LocalDateTime.now())
                .sent(isEmailSent)
                .build();

        alertRepository.saveAndFlush(alert);
    }
}
