package com.projects.Usage.Service.controller;

import com.projects.Usage.Service.DTO.UserDevicesResponse;
import com.projects.Usage.Service.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usage")
@RequiredArgsConstructor
public class UsageController {
    private final UsageService usageService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserDevicesResponse> getUserDevicesUsage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "3") int days) {
        UserDevicesResponse usage = usageService.getUserDevicesUsage(userId, days);
        return ResponseEntity.ok(usage);
    }
}
