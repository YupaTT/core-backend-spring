CREATE TABLE permisos (
    id          UUID PRIMARY KEY,
    codigo      VARCHAR(100) NOT NULL UNIQUE,
    modulo      VARCHAR(50)  NOT NULL,
    descripcion VARCHAR(255),
    version     BIGINT       NOT NULL DEFAULT 0,
    created_by  VARCHAR(100),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ
);
CREATE INDEX ix_permisos_modulo ON permisos (modulo);

CREATE TABLE roles (
    id          UUID PRIMARY KEY,
    codigo      VARCHAR(50)  NOT NULL UNIQUE,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    sistema     BOOLEAN      NOT NULL DEFAULT FALSE,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    version     BIGINT       NOT NULL DEFAULT 0,
    created_by  VARCHAR(100),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ
);

CREATE TABLE rol_permisos (
    rol_id     UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permiso_id UUID NOT NULL REFERENCES permisos (id) ON DELETE CASCADE,
    PRIMARY KEY (rol_id, permiso_id)
);
CREATE INDEX ix_rol_permisos_permiso ON rol_permisos (permiso_id);

INSERT INTO roles (id, codigo, nombre, descripcion, sistema, created_by, created_at)
VALUES (gen_random_uuid(), 'ADMINISTRADOR', 'Administrador', 'Acceso total al sistema', TRUE, 'system', NOW()),
       (gen_random_uuid(), 'USUARIO', 'Usuario', 'Acceso de consulta', TRUE, 'system', NOW());

INSERT INTO permisos (id, codigo, modulo, descripcion, created_by, created_at)
SELECT gen_random_uuid(), p.codigo, 'rol', p.descripcion, 'system', NOW()
FROM (VALUES
    ('rol:read', 'Listar y ver roles'),
    ('rol:create', 'Crear roles'),
    ('rol:update', 'Editar roles y sus permisos'),
    ('rol:delete', 'Eliminar roles')
) AS p (codigo, descripcion);

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p ON p.modulo = 'rol' WHERE r.codigo = 'ADMINISTRADOR';

INSERT INTO permisos (id, codigo, modulo, descripcion, created_by, created_at)
SELECT gen_random_uuid(), p.codigo, 'permiso', p.descripcion, 'system', NOW()
FROM (VALUES
    ('permiso:read', 'Listar permisos'),
    ('permiso:create', 'Crear permisos'),
    ('permiso:update', 'Editar permisos'),
    ('permiso:delete', 'Eliminar permisos')
) AS p (codigo, descripcion);

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p ON p.modulo = 'permiso' WHERE r.codigo = 'ADMINISTRADOR';
