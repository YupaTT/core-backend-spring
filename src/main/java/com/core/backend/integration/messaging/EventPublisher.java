package com.core.backend.integration.messaging;

/**
 * Publica eventos de dominio. Con mensajería activa se escriben en la tabla outbox dentro de la transacción
 * de negocio y un relay los envía al broker (entrega al-menos-una-vez); sin mensajería no hace nada.
 */
public interface EventPublisher {

    void publish(String aggregateType, String aggregateId, String eventType, Object payload);
}
