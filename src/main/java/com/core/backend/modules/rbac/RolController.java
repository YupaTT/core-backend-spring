package com.core.backend.modules.rbac;

import com.core.backend.common.api.ApiResponse;
import com.core.backend.common.api.EstadoRequest;
import com.core.backend.common.api.PageResponse;
import com.core.backend.modules.rbac.dto.RbacDtos.AsignarPermisosRequest;
import com.core.backend.modules.rbac.dto.RbacDtos.RolCreateRequest;
import com.core.backend.modules.rbac.dto.RbacDtos.RolResponse;
import com.core.backend.modules.rbac.dto.RbacDtos.RolUpdateRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Roles")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService service;

    @GetMapping
    @PreAuthorize("hasAuthority('rol:read')")
    public ApiResponse<PageResponse<RolResponse>> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 20, sort = "codigo") Pageable pageable) {
        return ApiResponse.ok(service.listar(q, activo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('rol:read')")
    public ApiResponse<RolResponse> obtener(@PathVariable UUID id) {
        return ApiResponse.ok(service.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('rol:create')")
    public ApiResponse<RolResponse> crear(@Valid @RequestBody RolCreateRequest request) {
        return ApiResponse.ok("Rol creado", service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('rol:update')")
    public ApiResponse<RolResponse> actualizar(@PathVariable UUID id, @Valid @RequestBody RolUpdateRequest request) {
        return ApiResponse.ok("Rol actualizado", service.actualizar(id, request));
    }

    @PutMapping("/{id}/permisos")
    @PreAuthorize("hasAuthority('rol:update')")
    public ApiResponse<RolResponse> asignarPermisos(
            @PathVariable UUID id, @Valid @RequestBody AsignarPermisosRequest request) {
        return ApiResponse.ok("Permisos asignados", service.asignarPermisos(id, request.permisoIds()));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('rol:update')")
    public ApiResponse<RolResponse> cambiarEstado(@PathVariable UUID id, @Valid @RequestBody EstadoRequest request) {
        return ApiResponse.ok("Estado actualizado", service.cambiarEstado(id, request.activo()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('rol:delete')")
    public void eliminar(@PathVariable UUID id) {
        service.eliminar(id);
    }
}
