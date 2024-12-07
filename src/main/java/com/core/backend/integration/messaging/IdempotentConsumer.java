package com.core.backend.integration.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Garantiza procesamiento exactamente-una-vez en consumidores (la entrega es al-menos-una-vez):
 *
 * <pre>
 * &#64;KafkaListener(topics = "core.parametro")
 * &#64;Transactional
 * public void on(ConsumerRecord&lt;String, String&gt; record) {
 *     if (!idempotent.markProcessed(header(record, "eventId"))) return;
 *     ...
 * }
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class IdempotentConsumer {

    private final JdbcTemplate jdbc;

    /** @return false si el mensaje ya fue procesado. Debe ejecutarse en la misma transacción del procesamiento. */
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean markProcessed(String messageId) {
        return jdbc.update(
                        "INSERT INTO processed_messages (message_id, processed_at) VALUES (?, NOW()) "
                                + "ON CONFLICT (message_id) DO NOTHING",
                        messageId)
                == 1;
    }
}
