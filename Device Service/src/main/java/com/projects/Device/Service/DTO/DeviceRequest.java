package com.projects.Device.Service.DTO;

import com.projects.Device.Service.model.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRequest {
    private String name;
    private DeviceType type;
    private String location;
    private Long userId;
}
