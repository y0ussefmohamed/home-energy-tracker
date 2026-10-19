package com.projects.Usage.Service.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDevicesResponse {
    private Long userId;
    private List<DeviceUsageResponse> devices;
}
