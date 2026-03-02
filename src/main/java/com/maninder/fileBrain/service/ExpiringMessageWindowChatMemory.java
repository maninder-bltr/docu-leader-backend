package com.maninder.fileBrain.service;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExpiringMessageWindowChatMemory implements ChatMemory {

    private final int maxMessages;
    private final Duration ttl;

    /**
     * conversationId -> messages with timestamps
     */
    private final ConcurrentMap<String, Deque<TimestampedMessage>> store =
            new ConcurrentHashMap<>();

    public ExpiringMessageWindowChatMemory(int maxMessages, Duration ttl) {
        Assert.isTrue(maxMessages > 0, "maxMessages must be > 0");
        Assert.notNull(ttl, "ttl must not be null");
        this.maxMessages = maxMessages;
        this.ttl = ttl;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notEmpty(messages, "messages cannot be null or empty");

        Deque<TimestampedMessage> queue =
                store.computeIfAbsent(conversationId, id -> new ArrayDeque<>());

        synchronized (queue) {
            Instant now = Instant.now();

            // 1. Remove expired messages
            evictExpired(queue, now);

            // 2. Add new messages
            for (Message message : messages) {
                if (message == null) {
                    continue;
                }
                queue.addLast(new TimestampedMessage(message, now));
            }

            // 3. Enforce max window size
            while (queue.size() > maxMessages) {
                queue.pollFirst();
            }
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");

        Deque<TimestampedMessage> queue = store.get(conversationId);
        if (queue == null) {
            return List.of();
        }

        synchronized (queue) {
            evictExpired(queue, Instant.now());

            return queue.stream()
                    .map(TimestampedMessage::message)
                    .toList();
        }
    }

    @Override
    public void clear(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        store.remove(conversationId);
    }

    private void evictExpired(Deque<TimestampedMessage> queue, Instant now) {
        while (!queue.isEmpty()) {
            TimestampedMessage head = queue.peekFirst();
            if (Duration.between(head.timestamp(), now).compareTo(ttl) > 0) {
                queue.pollFirst();
            } else {
                break;
            }
        }
    }

    private record TimestampedMessage(Message message, Instant timestamp) {}
}
