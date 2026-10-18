package com.projects.Device.Service.controller;

import com.projects.Device.Service.DTO.DeviceRequest;
import com.projects.Device.Service.DTO.DeviceResponse;
import com.projects.Device.Service.repository.DeviceRepository;
import com.projects.Device.Service.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/device")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDeviceById(@PathVariable Long id) {
        DeviceResponse deviceResponse = deviceService.getDeviceById(id);
        return ResponseEntity.ok(deviceResponse);
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@RequestBody DeviceRequest deviceRequest) {
        DeviceResponse createdResponse = deviceService.createDevice(deviceRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> updateDevice(@PathVariable Long id,
                                                       @RequestBody DeviceRequest deviceRequest) {
        DeviceResponse updatedDevice = deviceService.updateDevice(id, deviceRequest);
        return ResponseEntity.ok(updatedDevice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}
