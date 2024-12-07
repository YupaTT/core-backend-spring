package com.core.backend.integration.observability;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Métricas (Prometheus en /actuator/prometheus) siempre activas; trazas OTLP con TRACING_ENABLED=true. Anotar
 * servicios con {@code @Observed} genera métricas y spans sin código adicional.
 */
@Configuration
public class ObservabilityConfig {

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }
}
