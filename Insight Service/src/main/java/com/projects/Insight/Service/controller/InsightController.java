package com.projects.Insight.Service.controller;

import com.projects.Insight.Service.DTO.InsightResponse;
import com.projects.Insight.Service.service.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/insight")
@RequiredArgsConstructor
public class InsightController {
    private final InsightService insightService;

    @GetMapping("/saving-tips/{userId}")
    public ResponseEntity<InsightResponse> getSavingTips(@PathVariable Long userId) {
        InsightResponse insightResponse = insightService.getSavingTips(userId);
        return ResponseEntity.ok(insightResponse);
    }

    @GetMapping("/overview/{userId}")
    public ResponseEntity<InsightResponse> getOverview(@PathVariable Long userId) {
        InsightResponse insightResponse = insightService.getOverview(userId);
        return ResponseEntity.ok(insightResponse);
    }
}
