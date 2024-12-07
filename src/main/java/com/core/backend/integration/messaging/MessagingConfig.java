package com.core.backend.integration.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MessagingConfig {

    @Bean
    @ConditionalOnProperty(
            prefix = "app.integrations.messaging",
            name = "provider",
            havingValue = "none",
            matchIfMissing = true)
    public EventPublisher noOpEventPublisher() {
        return (aggregateType, aggregateId, eventType, payload) ->
                log.debug("Mensajería desactivada, evento descartado: {} {}", eventType, aggregateId);
    }
}
