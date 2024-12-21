# core-backend-spring

Base reutilizable para iniciar backends con **Java 21 + Spring Boot 3.5**: seguridad JWT con RBAC en base de datos, CRUDs de ejemplo listos para copiar e integraciones profesionales que se activan por configuración (Redis, observabilidad, mensajería, clientes HTTP y almacenamiento de archivos).

## Incluye

| Área | Qué trae |
|---|---|
| Seguridad | JWT HS512 de vida corta (15 min) con authorities en los claims, sin consultar la BD en cada request. Refresh token opaco (7 días) guardado como hash SHA-256, con rotación y detección de reutilización. Logout que revoca el access token. Bloqueo por intentos fallidos. Respuestas 401/403 en RFC 7807 |
| RBAC | `roles` ↔ `permisos` (`modulo:accion`) ↔ `usuarios`, protegido con `@PreAuthorize("hasAuthority('x:y')")`. Roles de sistema protegidos |
| Módulos | `auth`, `usuarios`, `roles`, `permisos`, `sucursales`, `parametros` (catálogo genérico agrupado y cacheado) |
| Persistencia | PostgreSQL + Flyway (`ddl-auto=validate`), auditoría (`created_by/at`, `updated_by/at`), bloqueo optimista (`@Version`), UUID ordenados por tiempo, batch inserts y `open-in-view=false` |
| Rendimiento | Virtual threads, HikariCP afinado, `default_batch_fetch_size` contra N+1, compresión HTTP, caché transaccional |
| Calidad | Tests de integración con Testcontainers, test unitario de JWT, reglas de arquitectura con ArchUnit, Spotless, JaCoCo y CI en GitHub Actions |

## Requisitos

- Java 21
- Docker (para la infraestructura local y los tests)

## Arranque local

```sh
cp .env.example .env                  # ajustar valores; .env no se versiona
docker compose up -d                  # postgres + redis
./gradlew bootRun                     # perfil "local" por defecto (admin / Admin123*)
```

- Swagger: http://localhost:8080/api/swagger-ui.html (solo en `local`)
- Health: http://localhost:8080/api/actuator/health

Si el puerto 5432 o el 6379 ya están ocupados, usa `DB_PORT=5433 docker compose up -d` y exporta la misma variable al arrancar la app.

## Integraciones (todas opcionales)

Cada integración se activa con una variable de entorno. Si está apagada, la app arranca sin esa infraestructura.

| Integración | Activar | Infra local | Uso en código |
|---|---|---|---|
| Redis | `REDIS_ENABLED=true` | incluida por defecto | `@Cacheable` / `@CacheEvict` con TTL por caché (`app.integrations.redis.ttls`). Revocación de tokens y bloqueo de login compartidos entre instancias. `DistributedLock.runLocked(...)` |
| Observabilidad | métricas siempre activas; trazas con `TRACING_ENABLED=true` | `docker compose --profile observability up -d` (Grafana en :3001) | `/actuator/prometheus`, `@Observed` en servicios; logs JSON (ECS) en `prod` |
| Mensajería | `MESSAGING_PROVIDER=kafka` o `rabbit` | `--profile kafka` o `--profile rabbit` | `EventPublisher.publish(...)` dentro de la transacción (patrón outbox con `SKIP LOCKED`) e `IdempotentConsumer` en los consumidores |
| HTTP externo | declarar `app.integrations.http.clients.<nombre>` | — | `HttpClientFactory.create("nombre", MiApi.class)` con interfaces `@HttpExchange`, más `@CircuitBreaker` / `@Retry` (Resilience4j) |
| Archivos | `STORAGE_PROVIDER=local` o `s3` | `--profile storage` (S3 compatible en :9000) | `StorageService` y los endpoints `/archivos` (subir, descargar, URL prefirmada, eliminar) |

Ejemplo de cliente HTTP:

```yaml
app.integrations.http.clients.pagos:
  base-url: https://api.pagos.com
  read-timeout: 5s
  headers: { X-Api-Key: ${PAGOS_API_KEY} }
resilience4j.circuitbreaker.instances.pagos.base-config: default
```

## Endpoints principales (prefijo `/api`)

| Método | Ruta | Permiso |
|---|---|---|
| POST | `/auth/login` · `/auth` (formato del front) | público |
| POST | `/auth/refresh` | público (requiere refresh token) |
| POST | `/auth/logout` · GET `/auth/perfil` | autenticado |
| GET | `/auth/verify` | público |
| CRUD | `/usuarios` (+ `PUT /{id}/roles`, `PATCH /{id}/estado`, `PUT /me/password`) | `usuario:*` |
| CRUD | `/roles` (+ `PUT /{id}/permisos`) · `/permisos` | `rol:*` · `permiso:*` |
| CRUD | `/sucursales` · `/parametros` (+ `GET /grupo/{grupo}` cacheado) | `sucursal:*` · `parametro:*` |

Con `app.security.login-password-base64=true` (valor por defecto) la contraseña del login se envía en base64, tal como espera el frontend (`docs/requerimientofront.md`).

## Crear un módulo nuevo

1. Copia `modules/parametro` (entidad → repositorio → servicio → controlador → DTOs → mapper).
2. Crea la migración `V<n>__<modulo>.sql` con la tabla y sus permisos (`<modulo>:read|create|update|delete`), y asígnalos a los roles que correspondan.
3. Protege cada endpoint con `@PreAuthorize("hasAuthority('<modulo>:<accion>')")`.
4. Agrega un test de integración que extienda `AbstractIntegrationTest`.

## Comandos

```sh
./gradlew build            # compila, verifica formato, ejecuta tests y genera cobertura
./gradlew spotlessApply    # formatea el código
```

## Docker

```sh
docker build -t core-backend-spring:1.0.0 --build-arg VERSION=1.0.0 --build-arg REVISION=$(git rev-parse --short HEAD) .
docker compose --profile app up -d --build        # API + postgres + redis (agrega --profile kafka/storage si los usas)
```

- **Imagen:** multi-stage con JDK/JRE Temurin 21 fijados a una versión exacta y capas de Spring Boot extraídas (un cambio de código solo reconstruye la capa `application`). Corre con un usuario sin privilegios (UID 10001), tiene healthcheck sobre `/actuator/health/liveness` y labels OCI con la versión y el commit. La JVM se ajusta al límite de memoria del contenedor.
- **`.dockerignore`:** funciona como lista blanca; solo envía `gradlew`, `gradle/`, los archivos de build y `src/main`, y nunca `.env`, claves ni `build/`.
- **Compose:**
  - Los puertos se publican solo en `127.0.0.1`.
  - Todos los servicios tienen healthchecks, reinicio automático, rotación de logs y volúmenes persistentes.
  - Dentro de compose, la API usa las direcciones internas (`postgres`, `redis`, `kafka:29092`, `s3:9000`), que reemplazan las de `.env` pensadas para el host.
  - Kafka expone `localhost:9092` para la app en el host y `kafka:29092` para los contenedores.
- **Perfil del contenedor:** por defecto es `prod`, así que `.env` debe definir `JWT_SECRET` y, para el primer arranque, `ADMIN_BOOTSTRAP_ENABLED=true` y `ADMIN_PASSWORD`.

## Producción

- Activa `SPRING_PROFILES_ACTIVE=prod`. En ese perfil Swagger queda apagado, los logs salen en JSON, se respetan los headers `X-Forwarded-*` y el admin inicial solo se crea si `ADMIN_BOOTSTRAP_ENABLED=true`.
- Son obligatorios `JWT_SECRET` (al menos 32 caracteres; generar con `openssl rand -base64 48`) y las credenciales de la base de datos.
- Restringe `/actuator/prometheus` a la red interna o muévelo a otro puerto con `management.server.port`.

## Contribuir

El proyecto sigue un flujo trunk-based con Pull Requests y Conventional Commits. Ver [CONTRIBUTING.md](CONTRIBUTING.md) y activar el hook local con `./gradlew installGitHooks`. Los cambios por versión están en [CHANGELOG.md](CHANGELOG.md).

Las decisiones de diseño están en [docs/adr/0001-decisiones-base.md](docs/adr/0001-decisiones-base.md).
