package com.maninder.fileBrain.config;

import com.maninder.fileBrain.service.ExpiringMessageWindowChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ChatClientConfig {
    private static final int MAX_MESSAGES = 10;
    private static final int MAX_CHAT_WINDOW_DURATION = 30;
    @Bean
    public ChatMemory chatMemory() {
        return new ExpiringMessageWindowChatMemory(
                MAX_MESSAGES,
                Duration.ofMinutes(MAX_CHAT_WINDOW_DURATION)
        );
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();
        return chatClientBuilder
                .defaultAdvisors(messageChatMemoryAdvisor)
                .build();
    }
}
