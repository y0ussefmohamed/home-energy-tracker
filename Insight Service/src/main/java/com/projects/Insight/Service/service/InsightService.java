package com.projects.Insight.Service.service;

import com.projects.Insight.Service.DTO.DeviceUsageResponse;
import com.projects.Insight.Service.DTO.InsightResponse;
import com.projects.Insight.Service.DTO.UserDevicesResponse;
import com.projects.Insight.Service.client.UsageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InsightService {
    private final UsageClient usageClient;
    private final ChatClient chatClient;

    public InsightResponse getSavingTips(Long userId) {
        log.info("Get energy saving tips for user ID: {}", userId);

        return createInsightResponse(userId, """
                Return exactly three numbered energy-saving tips, one short sentence each.
                Start immediately with "1." and give practical actions for the listed devices.
                Do not add a heading, introduction, or device-by-device consumption list.
                """);
    }

    public InsightResponse getOverview(Long userId) {
        log.info("Get energy usage overview for user ID: {}", userId);

        return createInsightResponse(userId, """
                Return a brief overview in two short sentences, without a heading or list.
                First describe which devices contribute most to the household's recorded usage.
                Then suggest one practical action to reduce consumption.
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

        // 2. Keep the prompt focused; the household total still includes every device.
        final String deviceSummary = usageResponse.getDevices().stream()
                .sorted(Comparator.comparingDouble(DeviceUsageResponse::getEnergyConsumed).reversed())
                .limit(3)
                .map(device -> "- %s (%s): %s".formatted(
                        device.getName(), device.getType(), device.getEnergyConsumed()))
                .collect(Collectors.joining("\n"));

        final String prompt = """
                Reporting period: the past 3 days.
                All values are totals over that period, not daily averages.
                Household total across all %d devices: %s
                Highest-consuming devices (up to three; other devices are omitted):
                %s

                Answer request:
                %s
                Answer rules:
                Use only the listed device names and types; do not introduce other devices.
                Focus actions on devices with positive recorded usage.
                Use conditional wording for device features that are not specified.
                Do not repeat consumption figures, measurement units, percentages, or savings estimates.
                Numbers are allowed only as the requested numbered-list markers.
                Keep the answer under 100 words.
                """.formatted(usageResponse.getDevices().size(), totalUsage, deviceSummary, instruction);

        log.info("Calling Ollama for user ID: {} with total usage: {}", userId, totalUsage);

        // 3. This bean also adds the system instructions from OllamaConfig.
        final ChatResponse chatResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .chatResponse();

        if (chatResponse == null || chatResponse.getResult() == null)
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ollama returned no response");

        // 4. A token-limit stop means the text is incomplete, even if Ollama returned HTTP 200.
        final Generation generation = chatResponse.getResult();
        final String finishReason = generation.getMetadata().getFinishReason();
        log.debug("Ollama response finish reason for user ID {}: {}", userId, finishReason);

        if ("length".equalsIgnoreCase(finishReason)) {
            log.warn("Ollama response reached the output limit for user ID: {}", userId);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "AI response reached its output limit. Please try again.");
        }

        final String tips = generation.getOutput().getText();
        if (tips == null || tips.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ollama returned an empty response");

        return InsightResponse.builder()
                .userId(userId)
                .tips(tips)
                .energyUsage(totalUsage)
                .build();
    }
}
