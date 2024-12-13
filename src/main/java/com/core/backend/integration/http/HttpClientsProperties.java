package com.core.backend.integration.http;

import java.time.Duration;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Clientes HTTP externos declarados por configuración:
 *
 * <pre>
 * app.integrations.http.clients.pagos:
 *   base-url: https://api.pagos.com
 *   connect-timeout: 2s
 *   read-timeout: 5s
 *   headers: { X-Api-Key: ${PAGOS_API_KEY} }
 * </pre>
 */
@ConfigurationProperties(prefix = "app.integrations.http")
public record HttpClientsProperties(@DefaultValue Map<String, Client> clients) {

    public record Client(
            String baseUrl,
            @DefaultValue("2s") Duration connectTimeout,
            @DefaultValue("5s") Duration readTimeout,
            @DefaultValue Map<String, String> headers) {}
}
