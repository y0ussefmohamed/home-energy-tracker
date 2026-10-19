package com.projects.Ingestion.Service.service;

import com.projects.Ingestion.Service.DTO.EnergyUsageRequest;
import com.projects.kafka.event.EnergyUsageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IngestionService {
    private final KafkaTemplate<String, EnergyUsageEvent> kafkaTemplate;

    public void ingestEnergyUsage(EnergyUsageRequest energyUsageRequest) {
        EnergyUsageEvent event = EnergyUsageEvent.builder()
                .deviceId(energyUsageRequest.getDeviceId())
                .energyConsumed(energyUsageRequest.getEnergyConsumed())
                .timestamp(energyUsageRequest.getTimestamp())
                .build();

        kafkaTemplate.send("energy-usage", event);
    }
}
