package com.core.backend.integration.messaging;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.integrations.messaging")
public record MessagingProperties(
        @DefaultValue("none") String provider, @DefaultValue("core") String topicPrefix, @DefaultValue Outbox outbox) {

    public record Outbox(
            @DefaultValue("100") int batchSize,
            @DefaultValue("2s") Duration pollInterval,
            @DefaultValue("10") int maxAttempts) {}
}
