package com.projects.Insight.Service.client;

import com.projects.Insight.Service.DTO.UserDevicesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class UsageClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UsageClient(@Value("${usage.service.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public UserDevicesResponse getUserUsage(Long userId, int days) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/{userId}")
                .queryParam("days", days)
                .buildAndExpand(userId)
                .toUriString();

        ResponseEntity<UserDevicesResponse> response = restTemplate.getForEntity(url, UserDevicesResponse.class);
        return response.getBody();
    }
}
