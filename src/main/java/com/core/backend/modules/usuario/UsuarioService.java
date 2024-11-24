package com.core.backend.modules.usuario;

import com.core.backend.common.api.PageResponse;
import com.core.backend.common.exception.ApiException;
import com.core.backend.modules.rbac.RolService;
import com.core.backend.modules.sucursal.SucursalService;
import com.core.backend.modules.usuario.dto.UsuarioDtos.UsuarioCreateRequest;
import com.core.backend.modules.usuario.dto.UsuarioDtos.UsuarioResponse;
import com.core.backend.modules.usuario.dto.UsuarioDtos.UsuarioUpdateRequest;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;
    private final RolService rolService;
    private final SucursalService sucursalService;
    private final PasswordEncoder passwordEncoder;

    public PageResponse<UsuarioResponse> listar(String q, Boolean activo, UUID sucursalId, Pageable pageable) {
        Specification<Usuario> spec = (root, query, cb) -> cb.conjunction();
        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("usuario")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("nombres")), like),
                    cb.like(cb.lower(root.get("apellidos")), like)));
        }
        if (activo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("activo"), activo));
        }
        if (sucursalId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sucursal").get("id"), sucursalId));
        }
        return PageResponse.from(repository.findAll(spec, pageable).map(mapper::toResponse));
    }

    public UsuarioResponse obtener(UUID id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest request) {
        if (repository.existsByUsuarioIgnoreCase(request.usuario())) {
            throw ApiException.conflict("El nombre de usuario ya está en uso");
        }
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw ApiException.conflict("El email ya está en uso");
        }
        Usuario usuario = new Usuario();
        usuario.setUsuario(request.usuario());
        usuario.setEmail(request.email());
        usuario.setNombres(request.nombres());
        usuario.setApellidos(request.apellidos());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setSucursal(sucursalService.referencia(request.sucursalId()));
        usuario.setRoles(rolService.resolverRoles(request.rolIds()));
        return mapper.toResponse(repository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(UUID id, UsuarioUpdateRequest request) {
        Usuario usuario = buscar(id);
        if (repository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw ApiException.conflict("El email ya está en uso");
        }
        usuario.setEmail(request.email());
        usuario.setNombres(request.nombres());
        usuario.setApellidos(request.apellidos());
        usuario.setSucursal(sucursalService.referencia(request.sucursalId()));
        return mapper.toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse asignarRoles(UUID id, Set<UUID> rolIds) {
        Usuario usuario = buscar(id);
        usuario.getRoles().clear();
        usuario.getRoles().addAll(rolService.resolverRoles(rolIds));
        return mapper.toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse cambiarEstado(UUID id, boolean activo, UUID actorId) {
        if (id.equals(actorId) && !activo) {
            throw ApiException.conflict("No puede desactivar su propio usuario");
        }
        Usuario usuario = buscar(id);
        usuario.setActivo(activo);
        return mapper.toResponse(usuario);
    }

    @Transactional
    public void cambiarPassword(UUID id, String actual, String nueva) {
        Usuario usuario = buscar(id);
        if (!passwordEncoder.matches(actual, usuario.getPassword())) {
            throw ApiException.badRequest("La contraseña actual no es correcta");
        }
        usuario.setPassword(passwordEncoder.encode(nueva));
    }

    @Transactional
    public void eliminar(UUID id, UUID actorId) {
        if (id.equals(actorId)) {
            throw ApiException.conflict("No puede eliminar su propio usuario");
        }
        repository.delete(buscar(id));
    }

    private Usuario buscar(UUID id) {
        return repository.findWithAccesosById(id).orElseThrow(() -> ApiException.notFound("Usuario", id));
    }
}
