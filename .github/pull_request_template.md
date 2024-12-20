## Qué cambia

<!-- Resumen breve del cambio y por qué es necesario. -->

## Tipo

- [ ] feat — nueva funcionalidad
- [ ] fix — corrección de error
- [ ] refactor / perf — sin cambio funcional
- [ ] docs / test / build / ci / chore

## Cómo se probó

<!-- Tests agregados o pasos manuales. -->

## Checklist

- [ ] El título del PR sigue Conventional Commits (`tipo(alcance): descripción`)
- [ ] `./gradlew build` pasa en local
- [ ] Si cambia el esquema, incluye migración Flyway nueva (nunca editar una existente)
- [ ] Si agrega endpoints, define sus permisos y `@PreAuthorize`
- [ ] Documentación actualizada (README / ADR / CHANGELOG) si aplica
