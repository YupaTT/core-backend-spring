# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/). El proyecto usa [versionado semántico](https://semver.org/lang/es/).

## [0.1.0] - 2024-12-20

### Agregado

- Base Spring Boot 3.5 con Java 21, perfiles `local`/`prod`, Flyway y virtual threads.
- Respuestas estándar de API, errores RFC 7807 y entidad base auditada con bloqueo optimista.
- Seguridad JWT stateless con refresh tokens rotativos, logout con revocación y bloqueo por intentos fallidos.
- RBAC en base de datos: roles, permisos `modulo:accion` y usuarios.
- CRUDs de usuarios, roles, permisos, sucursales y parámetros.
- Login compatible con el contrato del frontend.
- Caché transaccional con Redis o en memoria y lock distribuido.
- Métricas Prometheus y trazas OTLP.
- Eventos con patrón outbox hacia Kafka o RabbitMQ y consumidores idempotentes.
- Clientes HTTP declarativos con Resilience4j.
- Almacenamiento de archivos local y S3 compatible.
- Imagen Docker multi-stage, docker-compose con perfiles y CI en GitHub Actions.

[0.1.0]: https://github.com/YupaTT/core-backend-spring/releases/tag/v0.1.0
