package com.projects.Device.Service.DTO;

import com.projects.Device.Service.model.DeviceType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceResponse {
    private Long id;
    private String name;
    private DeviceType type;
    private String location;
    private Long userId;
}
