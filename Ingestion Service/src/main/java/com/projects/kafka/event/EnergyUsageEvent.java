package com.projects.kafka.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyUsageEvent {
    private Long deviceId;
    private double energyConsumed;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
}
