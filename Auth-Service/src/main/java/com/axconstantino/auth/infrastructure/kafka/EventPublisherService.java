package com.axconstantino.auth.infrastructure.kafka;

import com.axconstantino.auth.domain.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Service responsible for publishing domain events to Kafka topics.
 * <p>
 * This service abstracts the Kafka publishing logic and provides specific methods for each event type.
 * It ensures all Kafka interactions follow a consistent structure with proper logging and error handling.
 * </p>
 *
 * <h2>Supported Events</h2>
 * <ul>
 *     <li>{@link UserRegisteredEvent}</li>
 *     <li>{@link PasswordResetEvent}</li>
 *     <li>{@link EmailVerificationEvent}</li>
 * </ul>
 *
 * <p>New events can be added easily by creating a new publish method that delegates to {@code publishEvent()}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.user-registered}")
    private String userRegisteredTopic;

    @Value("${spring.kafka.topic.password-reset}")
    private String passwordResetTopic;

    @Value("${spring.kafka.topic.email-verification}")
    private String emailVerificationTopic;

    /**
     * Publishes a {@link UserRegisteredEvent} to the Kafka topic configured via {@code spring.kafka.topic.user-registered}.
     *
     * @param event the user registration event to be published. Must not be null.
     */
    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        Assert.notNull(event, "UserRegisteredEvent must not be null");
        publishEvent(userRegisteredTopic, event.userId().toString(), event);
    }

    /**
     * Publishes a {@link PasswordResetEvent} to the Kafka topic configured via {@code spring.kafka.topic.password-reset}.
     *
     * @param event the password reset request event to be published. Must not be null.
     */
    public void publishPasswordResetEvent(PasswordResetEvent event) {
        Assert.notNull(event, "PasswordResetEvent must not be null");
        publishEvent(passwordResetTopic, event.email(), event);
    }

    /**
     * Publishes an {@link EmailVerificationEvent} to the Kafka topic configured via {@code spring.kafka.topic.email-verification}.
     *
     * @param event the email verification event to be published. Must not be null.
     */
    public void publishEmailVerificationEvent(EmailVerificationEvent event) {
        Assert.notNull(event, "EmailVerificationEvent must not be null");
        publishEvent(emailVerificationTopic, event.email(), event);
    }

    /**
     * Generic method that publishes any event to the specified Kafka topic.
     * Uses the provided key to determine partitioning and logs success or failure accordingly.
     *
     * @param topic the Kafka topic to which the event should be published
     * @param key   the message key used for partitioning (e.g. user ID or email)
     * @param event the payload to send
     * @param <T>   the type of the event
     */
    private <T> void publishEvent(String topic, String key, T event) {
        log.info("[Kafka] Preparing to publish event to topic: '{}', key: '{}', payload type: {}",
                topic, key, event.getClass().getSimpleName());

        try {
            kafkaTemplate.send(topic, key, event)
                    .thenAccept(result -> {
                        log.info("[Kafka] Event published successfully.");
                        log.debug("[Kafka] Topic: {}, Partition: {}, Offset: {}, Key: {}, Payload: {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                key,
                                event);
                    })
                    .exceptionally(ex -> {
                        log.error("[Kafka] Failed to publish event. Topic: {}, Key: {}, Error: {}",
                                topic, key, ex.getMessage(), ex);
                        return null;
                    });

        } catch (Exception ex) {
            log.error("[Kafka] Unexpected error while publishing event. Topic: {}, Key: {}, Error: {}",
                    topic, key, ex.getMessage(), ex);
        }
    }
}

