package com.projects.Insight.Service.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are an energy efficiency advisor. Give concise, practical advice
                        based on the supplied household device usage. Use only the supplied
                        figures. Do not invent measurement units, electricity prices,
                        household averages, or missing usage data.
                        """)
                .build();
    }
}
