# ADR 0001 — Decisiones de la base

Fecha: 2026-10-21 · Estado: aceptada

| # | Decisión | Motivo | Consecuencia |
|---|---|---|---|
| 1 | Paquetes por funcionalidad (`modules/<x>`) + `common`, `security`, `integration` | Cada módulo se copia o elimina completo | `common` no depende de módulos (lo verifica ArchUnit) |
| 2 | Access token JWT de 15 min con authorities en los claims | Ninguna consulta a la BD por request (escala horizontalmente) | Los cambios de roles aplican en el siguiente refresh; al asignar roles, desactivar el usuario o cambiar su contraseña se revocan sus refresh tokens |
| 3 | Refresh token opaco guardado como hash, con rotación | Una fuga de la BD no expone tokens; detecta robos | Reutilizar un refresh revoca toda la sesión del usuario |
| 4 | RBAC en BD: rol → permisos `modulo:accion` | Se configura sin redeploy; el front recibe `permisos` en el perfil | Los permisos nuevos se agregan por migración Flyway |
| 5 | Flyway + `ddl-auto=validate` | Esquema versionado y reproducible | Todo cambio de entidad requiere su migración |
| 6 | Integraciones con `@ConditionalOnProperty` | Una sola base para proyectos con necesidades distintas | Las dependencias de todas las integraciones van en el jar; quita las que no uses en `build.gradle.kts` |
| 7 | Outbox transaccional + relay con `FOR UPDATE SKIP LOCKED` | Consistencia entre BD y broker sin 2PC; soporta varias instancias | Entrega al-menos-una-vez → consumidores idempotentes (`processed_messages`) |
| 8 | Caché transaccional (evicción tras el commit), serialización JDK | Evita repoblar la caché con datos viejos | Los valores cacheados deben ser `Serializable` |
| 9 | Errores RFC 7807 (`ProblemDetail`) | Estándar e igual para los errores de seguridad y de negocio | El front lee `detail` y `errores` |
| 10 | Virtual threads | Alto throughput en I/O bloqueante (JDBC, HTTP) sin programación reactiva | Dimensionar el pool de Hikari según la BD, no según los hilos |
