package com.core.backend.integration.messaging;

import com.core.backend.integration.messaging.outbox.OutboxEvent;

/** Envío síncrono al broker: debe lanzar excepción si el mensaje no fue confirmado. */
public interface MessageBroker {

    void send(OutboxEvent event);
}
