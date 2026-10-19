package com.projects.Usage.Service.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceUsageResponse {
    private Long id;
    private String name;
    private String type;
    private String location;
    private double energyConsumed;
}
