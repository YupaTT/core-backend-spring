package com.core.backend.modules.rbac;

import com.core.backend.common.api.PageResponse;
import com.core.backend.common.exception.ApiException;
import com.core.backend.modules.rbac.dto.RbacDtos.RolCreateRequest;
import com.core.backend.modules.rbac.dto.RbacDtos.RolResponse;
import com.core.backend.modules.rbac.dto.RbacDtos.RolUpdateRequest;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final RbacMapper mapper;

    public PageResponse<RolResponse> listar(String q, Boolean activo, Pageable pageable) {
        Specification<Rol> spec = (root, query, cb) -> cb.conjunction();
        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(cb.like(cb.lower(root.get("codigo")), like), cb.like(cb.lower(root.get("nombre")), like)));
        }
        if (activo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("activo"), activo));
        }
        return PageResponse.from(rolRepository.findAll(spec, pageable).map(mapper::toResponse));
    }

    public RolResponse obtener(UUID id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional
    public RolResponse crear(RolCreateRequest request) {
        if (rolRepository.existsByCodigoIgnoreCase(request.codigo())) {
            throw ApiException.conflict("Ya existe el rol " + request.codigo());
        }
        Rol rol = new Rol();
        rol.setCodigo(request.codigo());
        rol.setNombre(request.nombre());
        rol.setDescripcion(request.descripcion());
        if (request.permisoIds() != null) {
            rol.setPermisos(resolverPermisos(request.permisoIds()));
        }
        return mapper.toResponse(rolRepository.save(rol));
    }

    @Transactional
    public RolResponse actualizar(UUID id, RolUpdateRequest request) {
        Rol rol = buscar(id);
        rol.setNombre(request.nombre());
        rol.setDescripcion(request.descripcion());
        return mapper.toResponse(rol);
    }

    @Transactional
    public RolResponse asignarPermisos(UUID id, Set<UUID> permisoIds) {
        Rol rol = buscar(id);
        rol.getPermisos().clear();
        rol.getPermisos().addAll(resolverPermisos(permisoIds));
        return mapper.toResponse(rol);
    }

    @Transactional
    public RolResponse cambiarEstado(UUID id, boolean activo) {
        Rol rol = buscar(id);
        if (rol.isSistema() && !activo) {
            throw ApiException.conflict("Los roles de sistema no se pueden desactivar");
        }
        rol.setActivo(activo);
        return mapper.toResponse(rol);
    }

    @Transactional
    public void eliminar(UUID id) {
        Rol rol = buscar(id);
        if (rol.isSistema()) {
            throw ApiException.conflict("Los roles de sistema no se pueden eliminar");
        }
        if (rolRepository.isAsignado(id)) {
            throw ApiException.conflict("El rol está asignado a uno o más usuarios");
        }
        rolRepository.delete(rol);
    }

    /** Usado por el módulo de usuarios para validar los roles a asignar. */
    public Set<Rol> resolverRoles(Collection<UUID> ids) {
        List<Rol> roles = rolRepository.findAllByIdIn(ids);
        if (roles.size() != new HashSet<>(ids).size()) {
            throw ApiException.badRequest("Uno o más roles no existen");
        }
        return new HashSet<>(roles);
    }

    private Set<Permiso> resolverPermisos(Collection<UUID> ids) {
        List<Permiso> permisos = permisoRepository.findAllByIdIn(ids);
        if (permisos.size() != new HashSet<>(ids).size()) {
            throw ApiException.badRequest("Uno o más permisos no existen");
        }
        return new HashSet<>(permisos);
    }

    private Rol buscar(UUID id) {
        return rolRepository.findWithPermisosById(id).orElseThrow(() -> ApiException.notFound("Rol", id));
    }
}
