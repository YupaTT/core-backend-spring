CREATE TABLE usuarios (
    id          UUID PRIMARY KEY,
    usuario     VARCHAR(50)  NOT NULL,
    email       VARCHAR(150) NOT NULL,
    nombres     VARCHAR(100) NOT NULL,
    apellidos   VARCHAR(100) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    sucursal_id UUID REFERENCES sucursales (id),
    version     BIGINT       NOT NULL DEFAULT 0,
    created_by  VARCHAR(100),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ
);
CREATE UNIQUE INDEX ux_usuarios_usuario ON usuarios (LOWER(usuario));
CREATE UNIQUE INDEX ux_usuarios_email ON usuarios (LOWER(email));

CREATE TABLE usuario_roles (
    usuario_id UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    rol_id     UUID NOT NULL REFERENCES roles (id),
    PRIMARY KEY (usuario_id, rol_id)
);
CREATE INDEX ix_usuario_roles_rol ON usuario_roles (rol_id);

INSERT INTO permisos (id, codigo, modulo, descripcion, created_by, created_at)
SELECT gen_random_uuid(), p.codigo, 'usuario', p.descripcion, 'system', NOW()
FROM (VALUES
    ('usuario:read', 'Listar y ver usuarios'),
    ('usuario:create', 'Crear usuarios'),
    ('usuario:update', 'Editar usuarios, estado y roles'),
    ('usuario:delete', 'Eliminar usuarios')
) AS p (codigo, descripcion);

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p ON p.modulo = 'usuario' WHERE r.codigo = 'ADMINISTRADOR';
