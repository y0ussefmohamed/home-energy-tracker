package com.projects.Ingestion.Service.controller;

import com.projects.Ingestion.Service.DTO.EnergyUsageRequest;
import com.projects.Ingestion.Service.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ingestion")
@RequiredArgsConstructor
public class IngestionController {
    private final IngestionService ingestionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // When the method completes successfully, return HTTP status 201 Created.
    public void ingestData(@RequestBody EnergyUsageRequest energyUsageRequest) {
        ingestionService.ingestEnergyUsage(energyUsageRequest);
    }
}
