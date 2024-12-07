package com.core.backend.integration.messaging.outbox;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    /** SKIP LOCKED permite varias instancias del relay sin procesar dos veces el mismo evento. */
    @Query(
            value = "SELECT * FROM outbox_events WHERE status = 'PENDING' ORDER BY created_at LIMIT :limit "
                    + "FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<OutboxEvent> lockNextBatch(int limit);

    @Modifying
    @Query("delete from OutboxEvent e where e.status = 'PUBLISHED' and e.publishedAt < :limite")
    int deletePublishedBefore(Instant limite);
}
