package com.core.backend.integration.redis;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/** Lock entre instancias para tareas que no deben ejecutarse en paralelo (jobs, procesos únicos). */
public interface DistributedLock {

    /** Ejecuta la tarea solo si se obtiene el lock; si otra instancia lo tiene devuelve vacío. */
    <T> Optional<T> runLocked(String name, Duration ttl, Supplier<T> task);
}
