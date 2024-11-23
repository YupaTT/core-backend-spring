CREATE TABLE sucursales (
    id          UUID PRIMARY KEY,
    nombre      VARCHAR(120) NOT NULL UNIQUE,
    sigla       VARCHAR(20)  NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    version     BIGINT       NOT NULL DEFAULT 0,
    created_by  VARCHAR(100),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ
);

INSERT INTO permisos (id, codigo, modulo, descripcion, created_by, created_at)
SELECT gen_random_uuid(), p.codigo, 'sucursal', p.descripcion, 'system', NOW()
FROM (VALUES
    ('sucursal:read', 'Listar y ver sucursales'),
    ('sucursal:create', 'Crear sucursales'),
    ('sucursal:update', 'Editar sucursales'),
    ('sucursal:delete', 'Eliminar sucursales')
) AS p (codigo, descripcion);

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p ON p.modulo = 'sucursal' WHERE r.codigo = 'ADMINISTRADOR';

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p ON p.codigo = 'sucursal:read' WHERE r.codigo IN ('USUARIO');
