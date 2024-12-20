# Guía de contribución

## Flujo de trabajo (trunk-based)

- `main` está protegida y siempre es desplegable. Nadie hace push directo.
- Cada cambio va en una rama corta creada desde `main` y vive pocos días:

  | Prefijo | Uso | Ejemplo |
  |---|---|---|
  | `feat/` | funcionalidad | `feat/auth-otp` |
  | `fix/` | corrección | `fix/refresh-token-expirado` |
  | `refactor/`, `perf/`, `docs/`, `test/`, `build/`, `ci/`, `chore/` | resto | `docs/adr-cache` |

- Se integra por Pull Request con **squash merge**. El título del PR se convierte en el commit de `main`, así que debe seguir el formato de abajo.
- Requisitos para hacer merge: CI en verde (formato, build, tests, formato de commits) y al menos una revisión.
- Las versiones se marcan con tags semver (`v0.2.0`) y se registran en `CHANGELOG.md`.

## Formato de commits (Conventional Commits)

```
<tipo>(<alcance>): <descripción>

<cuerpo opcional: qué y por qué, no cómo>

<pie opcional: BREAKING CHANGE: ..., Refs: #123>
```

- **tipo:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`.
- **alcance:** el módulo afectado: `auth`, `usuario`, `rbac`, `sucursal`, `parametro`, `security`, `core`, `cache`, `redis`, `messaging`, `http`, `storage`, `observability`, `docker`.
- **descripción:** en infinitivo y minúscula, sin punto final y con 72 caracteres como máximo (`agregar`, `corregir`, `eliminar`…).
- Un cambio incompatible lleva `!` (`feat(auth)!: ...`) y un pie `BREAKING CHANGE:`.

Ejemplos:

```
feat(parametro): agregar filtro por grupo en el listado
fix(storage): permitir reintentos al subir archivos a S3
refactor(security): extraer manejo de errores 401/403
```

### Validación automática

```sh
git config core.hooksPath .githooks   # o: ./gradlew installGitHooks
```

El hook `commit-msg` rechaza los mensajes que no cumplan el formato. CI aplica la misma validación a cada commit de un PR.

## Antes de abrir un PR

```sh
./gradlew spotlessApply build
```

- No modifiques migraciones ya publicadas: crea una nueva (`V<n>__descripcion.sql`).
- Todo endpoint nuevo define su permiso `<modulo>:<accion>` en una migración y lo protege con `@PreAuthorize`.
- Agrega tests: unitarios para la lógica y de integración (`AbstractIntegrationTest`) para los endpoints.
