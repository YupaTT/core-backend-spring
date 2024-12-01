CREATE TABLE parametros (
    id          UUID PRIMARY KEY,
    grupo       VARCHAR(50)  NOT NULL,
    codigo      VARCHAR(50)  NOT NULL,
    nombre      VARCHAR(150) NOT NULL,
    valor       VARCHAR(500),
    descripcion VARCHAR(255),
    orden       INTEGER      NOT NULL DEFAULT 0,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    version     BIGINT       NOT NULL DEFAULT 0,
    created_by  VARCHAR(100),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    CONSTRAINT ux_parametros_grupo_codigo UNIQUE (grupo, codigo)
);

INSERT INTO permisos (id, codigo, modulo, descripcion, created_by, created_at)
SELECT gen_random_uuid(), p.codigo, 'parametro', p.descripcion, 'system', NOW()
FROM (VALUES
    ('parametro:read', 'Listar y ver parámetros'),
    ('parametro:create', 'Crear parámetros'),
    ('parametro:update', 'Editar parámetros'),
    ('parametro:delete', 'Eliminar parámetros')
) AS p (codigo, descripcion);

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p ON p.modulo = 'parametro' WHERE r.codigo = 'ADMINISTRADOR';

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p ON p.codigo = 'parametro:read' WHERE r.codigo IN ('USUARIO');
