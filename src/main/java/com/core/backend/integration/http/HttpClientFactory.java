package com.core.backend.integration.http;

import java.net.http.HttpClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Crea {@link RestClient} por nombre con timeouts, headers y trazas propagadas (el builder de Spring ya incluye
 * la instrumentación de Micrometer). Combinar con Resilience4j en el servicio que lo usa:
 *
 * <pre>
 * &#64;CircuitBreaker(name = "pagos", fallbackMethod = "fallback")
 * &#64;Retry(name = "pagos")
 * public PagoResponse pagar(...) { return pagosClient.pagar(...); }
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpClientFactory {

    private final RestClient.Builder builder;
    private final HttpClientsProperties properties;
    private final Map<String, RestClient> clients = new ConcurrentHashMap<>();

    public RestClient restClient(String name) {
        return clients.computeIfAbsent(name, this::build);
    }

    private RestClient build(String name) {
        HttpClientsProperties.Client config = properties.clients().get(name);
        if (config == null) {
            throw new IllegalArgumentException("Cliente HTTP no configurado: app.integrations.http.clients." + name);
        }
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(config.connectTimeout())
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(config.readTimeout());
        return builder.clone()
                .baseUrl(config.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeaders(h -> config.headers().forEach(h::add))
                .requestInterceptor(logging(name))
                .build();
    }

    /** Cliente declarativo a partir de una interfaz anotada con {@code @HttpExchange}. */
    public <T> T create(String name, Class<T> clientInterface) {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient(name)))
                .build()
                .createClient(clientInterface);
    }

    private static ClientHttpRequestInterceptor logging(String name) {
        return (request, body, execution) -> {
            long start = System.nanoTime();
            var response = execution.execute(request, body);
            log.debug(
                    "[{}] {} {} -> {} ({} ms)",
                    name,
                    request.getMethod(),
                    request.getURI(),
                    response.getStatusCode().value(),
                    (System.nanoTime() - start) / 1_000_000);
            return response;
        };
    }
}
