package com.projects.Insight.Service.service;

import com.projects.Insight.Service.DTO.DeviceUsageResponse;
import com.projects.Insight.Service.DTO.InsightResponse;
import com.projects.Insight.Service.DTO.UserDevicesResponse;
import com.projects.Insight.Service.client.UsageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class InsightService {
    private final UsageClient usageClient;
    private final ChatClient chatClient;

    public InsightResponse getSavingTips(Long userId) {
        log.info("Get energy saving tips for user ID: {}", userId);

        return createInsightResponse(userId, """
                Give three practical ways to reduce this household's energy consumption.
                Prioritize the devices with the highest recorded consumption.
                """);
    }

    public InsightResponse getOverview(Long userId) {
        log.info("Get energy usage overview for user ID: {}", userId);

        return createInsightResponse(userId, """
                Give a concise overview of this household's energy usage.
                Identify the devices consuming the most energy and suggest one action.
                """);
    }

    private InsightResponse createInsightResponse(Long userId, String instruction) {
        // 1. Fetch this user's per-device totals for the past three days.
        final UserDevicesResponse usageResponse = usageClient.getUserUsage(userId, 3);
        if (usageResponse == null || usageResponse.getDevices() == null)
            throw new IllegalStateException("Usage Service returned no usage data for user ID: " + userId);


        final double totalUsage = usageResponse.getDevices().stream()
                .mapToDouble(DeviceUsageResponse::getEnergyConsumed)
                .sum();

        if (usageResponse.getDevices().isEmpty()) {
            return InsightResponse.builder()
                    .userId(userId)
                    .tips("No devices are available to analyze for this user.")
                    .energyUsage(totalUsage)
                    .build();
        }

        // 2. Supply the real usage figures alongside the requested kind of advice.
        final String prompt = """
                %s
                Reporting period: the past 3 days.
                All values are totals over that period, not daily averages.
                Total energy consumed: %s
                Device usage: %s
                Keep the answer under 150 words.
                """.formatted(instruction, totalUsage, usageResponse.getDevices());

        log.info("Calling Ollama for user ID: {} with total usage: {}", userId, totalUsage);

        // 3. This bean also adds the system instructions from OllamaConfig.
        final String tips = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        if (tips == null || tips.isBlank())
            throw new IllegalStateException("Ollama returned an empty response for user ID: " + userId);

        return InsightResponse.builder()
                .userId(userId)
                .tips(tips)
                .energyUsage(totalUsage)
                .build();
    }
}
