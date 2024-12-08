package com.core.backend.integration.messaging.rabbit;

import com.core.backend.integration.messaging.MessageBroker;
import com.core.backend.integration.messaging.MessagingProperties;
import java.nio.charset.StandardCharsets;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Exchange topic {@code <prefix>}; routing key = eventType (p. ej. {@code parametro.creado}). */
@EnableRabbit
@Configuration
@ConditionalOnProperty(prefix = "app.integrations.messaging", name = "provider", havingValue = "rabbit")
public class RabbitMessagingConfig {

    @Bean
    public TopicExchange eventsExchange(MessagingProperties properties) {
        return new TopicExchange(properties.topicPrefix(), true, false);
    }

    @Bean
    public MessageBroker rabbitMessageBroker(RabbitTemplate template, TopicExchange exchange) {
        return event -> {
            Message message = MessageBuilder.withBody(event.getPayload().getBytes(StandardCharsets.UTF_8))
                    .setContentType("application/json")
                    .setMessageId(event.getId().toString())
                    .setHeader("eventType", event.getEventType())
                    .setHeader("aggregateId", event.getAggregateId())
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();
            template.invoke(ops -> {
                ops.send(exchange.getName(), event.getEventType(), message);
                ops.waitForConfirmsOrDie(10_000);
                return null;
            });
        };
    }
}
