package com.projects.Usage.Service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceEnergy { // To convert the output of the influx db table to this model
    private Long deviceId;
    private double energyConsumed;
    private Long userId;
}
