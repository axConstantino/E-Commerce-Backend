package com.axconstantino.auth.application.service;

import com.axconstantino.auth.domain.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.user-registered}")
    private String userRegisteredTopic;

    /**
     * Publishes a RegisteredUserEvent to Kafka asynchronously.
     * The user's ID is used as the message key to ensure that events
     * from the same user are sent to the same partition, preserving order.
     *
     * @param event The event payload containing the user's registration details. Must not be null.
     * @apiNote This method operates in a "fire-and-forget" mode. The result of the send operation is handled
     * asynchronously via logging. Requires the KafkaTemplate to be configured with a serializer
     * compatible with RegisteredUserEvent, such as JsonSerializer.
     */
    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        Assert.notNull(event, "UserRegisteredEvent must not be null");

        try {
            String key = event.userId().toString();
            log.info("Trying to publish event for registered user with email: {}", event.email());

            kafkaTemplate.send(userRegisteredTopic, key, event)
                    .thenAccept(result ->
                            log.info("Event successfully published to user ID {}. Topic: {}, Partition: {}, Offset: {}",
                                    key,
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset()))
                    .exceptionally(ex -> {
                        log.error("Failed to publish event for user ID {} asynchronously.", key, ex);
                        return null;
                    });
        } catch (Exception e) {
            log.error("Unexpected synchronous error while attempting to send event for user ID {}: {}", event.userId(), e.getMessage(), e);
        }
    }
}
