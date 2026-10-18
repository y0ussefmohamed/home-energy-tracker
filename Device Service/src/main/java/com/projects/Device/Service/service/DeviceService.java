package com.projects.Device.Service.service;

import com.projects.Device.Service.DTO.DeviceRequest;
import com.projects.Device.Service.DTO.DeviceResponse;
import com.projects.Device.Service.entity.Device;
import com.projects.Device.Service.exception.DeviceNotFoundException;
import com.projects.Device.Service.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;

    public DeviceResponse getDeviceById(final Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + deviceId));
        return this.toDeviceResponse(device);
    }

    public DeviceResponse createDevice(final DeviceRequest deviceRequest) {
        Device device = Device.builder()
                .name(deviceRequest.getName())
                .type(deviceRequest.getType())
                .location(deviceRequest.getLocation())
                .userId(deviceRequest.getUserId())
                .build();

        return this.toDeviceResponse(deviceRepository.save(device));
    }

    public DeviceResponse updateDevice(final Long deviceId, final DeviceRequest deviceRequest) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + deviceId));

        device.setName(deviceRequest.getName());
        device.setType(deviceRequest.getType());
        device.setLocation(deviceRequest.getLocation());
        device.setUserId(deviceRequest.getUserId());

        return this.toDeviceResponse(deviceRepository.save(device));
    }

    public void deleteDevice(final Long deviceId) {
        if (!deviceRepository.existsById(deviceId)) {
            throw new DeviceNotFoundException("Device not found with id: " + deviceId);
        }

        deviceRepository.deleteById(deviceId);
    }

    private DeviceResponse toDeviceResponse(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .name(device.getName())
                .type(device.getType())
                .location(device.getLocation())
                .userId(device.getUserId())
                .build();
    }
}
