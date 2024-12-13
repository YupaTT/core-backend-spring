INSERT INTO permisos (id, codigo, modulo, descripcion, created_by, created_at)
SELECT gen_random_uuid(), p.codigo, 'archivo', p.descripcion, 'system', NOW()
FROM (VALUES
    ('archivo:read', 'Descargar archivos'),
    ('archivo:create', 'Subir archivos'),
    ('archivo:delete', 'Eliminar archivos')
) AS p (codigo, descripcion);

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p ON p.modulo = 'archivo' WHERE r.codigo = 'ADMINISTRADOR';

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p ON p.codigo = 'archivo:read' WHERE r.codigo IN ('USUARIO');
