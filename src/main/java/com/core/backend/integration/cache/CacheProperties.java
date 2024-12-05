package com.core.backend.integration.cache;

import java.time.Duration;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.integrations.redis")
public record CacheProperties(
        boolean enabled, @DefaultValue("10m") Duration defaultTtl, @DefaultValue Map<String, Duration> ttls) {}
