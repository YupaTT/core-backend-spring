package com.core.backend.integration.messaging.kafka;

import com.core.backend.integration.messaging.MessageBroker;
import com.core.backend.integration.messaging.MessagingProperties;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;

/** Tópico: {@code <prefix>.<aggregateType>}; key = aggregateId para mantener orden por agregado. */
@EnableKafka
@Configuration
@ConditionalOnProperty(prefix = "app.integrations.messaging", name = "provider", havingValue = "kafka")
public class KafkaMessagingConfig {

    @Bean
    public MessageBroker kafkaMessageBroker(KafkaTemplate<String, String> template, MessagingProperties properties) {
        return event -> {
            var record = new ProducerRecord<>(
                    properties.topicPrefix() + "." + event.getAggregateType(),
                    event.getAggregateId(),
                    event.getPayload());
            record.headers().add("eventId", event.getId().toString().getBytes(StandardCharsets.UTF_8));
            record.headers().add("eventType", event.getEventType().getBytes(StandardCharsets.UTF_8));
            try {
                template.send(record).get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Envío a Kafka interrumpido", e);
            } catch (ExecutionException | TimeoutException e) {
                throw new IllegalStateException("No se pudo publicar en Kafka: " + e.getMessage(), e);
            }
        };
    }
}
