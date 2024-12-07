package com.core.backend.integration.messaging.outbox;

import com.core.backend.integration.messaging.EventPublisher;
import com.core.backend.integration.messaging.MessageBroker;
import com.core.backend.integration.messaging.MessagingProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@ConditionalOnExpression("'${app.integrations.messaging.provider:none}' != 'none'")
public class OutboxConfig {

    @Bean
    public EventPublisher outboxEventPublisher(OutboxRepository repository, ObjectMapper objectMapper) {
        return new OutboxEventPublisher(repository, objectMapper);
    }

    @Bean
    public OutboxRelay outboxRelay(OutboxRepository repository, MessageBroker broker, MessagingProperties properties) {
        return new OutboxRelay(repository, broker, properties);
    }

    @RequiredArgsConstructor
    static class OutboxEventPublisher implements EventPublisher {

        private final OutboxRepository repository;
        private final ObjectMapper objectMapper;

        @Override
        @Transactional(propagation = Propagation.MANDATORY)
        public void publish(String aggregateType, String aggregateId, String eventType, Object payload) {
            OutboxEvent event = new OutboxEvent();
            event.setAggregateType(aggregateType);
            event.setAggregateId(aggregateId);
            event.setEventType(eventType);
            try {
                event.setPayload(objectMapper.writeValueAsString(payload));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Payload de evento no serializable", e);
            }
            repository.save(event);
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    public static class OutboxRelay {

        private final OutboxRepository repository;
        private final MessageBroker broker;
        private final MessagingProperties properties;

        @Scheduled(fixedDelayString = "${app.integrations.messaging.outbox.poll-interval:2s}")
        @Transactional
        public void relay() {
            List<OutboxEvent> batch =
                    repository.lockNextBatch(properties.outbox().batchSize());
            for (OutboxEvent event : batch) {
                try {
                    broker.send(event);
                    event.setStatus(OutboxEvent.Status.PUBLISHED);
                    event.setPublishedAt(Instant.now());
                } catch (RuntimeException ex) {
                    event.setAttempts(event.getAttempts() + 1);
                    event.setLastError(truncate(ex.getMessage()));
                    if (event.getAttempts() >= properties.outbox().maxAttempts()) {
                        event.setStatus(OutboxEvent.Status.FAILED);
                        log.error("Evento {} marcado FAILED tras {} intentos", event.getId(), event.getAttempts(), ex);
                    } else {
                        log.warn("Fallo publicando evento {}: {}", event.getId(), ex.getMessage());
                    }
                }
            }
        }

        @Scheduled(cron = "0 0 4 * * *")
        @Transactional
        public void purge() {
            repository.deletePublishedBefore(Instant.now().minus(Duration.ofDays(7)));
        }

        private static String truncate(String message) {
            if (message == null) return null;
            return message.length() > 1000 ? message.substring(0, 1000) : message;
        }
    }
}
