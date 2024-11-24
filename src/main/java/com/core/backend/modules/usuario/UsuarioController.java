package com.core.backend.modules.usuario;

import com.core.backend.common.api.ApiResponse;
import com.core.backend.common.api.EstadoRequest;
import com.core.backend.common.api.PageResponse;
import com.core.backend.modules.usuario.dto.UsuarioDtos.AsignarRolesRequest;
import com.core.backend.modules.usuario.dto.UsuarioDtos.CambiarPasswordRequest;
import com.core.backend.modules.usuario.dto.UsuarioDtos.UsuarioCreateRequest;
import com.core.backend.modules.usuario.dto.UsuarioDtos.UsuarioResponse;
import com.core.backend.modules.usuario.dto.UsuarioDtos.UsuarioUpdateRequest;
import com.core.backend.security.CurrentUser;
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

@Tag(name = "Usuarios")
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping
    @PreAuthorize("hasAuthority('usuario:read')")
    public ApiResponse<PageResponse<UsuarioResponse>> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) UUID sucursalId,
            @PageableDefault(size = 20, sort = "usuario") Pageable pageable) {
        return ApiResponse.ok(service.listar(q, activo, sucursalId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('usuario:read')")
    public ApiResponse<UsuarioResponse> obtener(@PathVariable UUID id) {
        return ApiResponse.ok(service.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('usuario:create')")
    public ApiResponse<UsuarioResponse> crear(@Valid @RequestBody UsuarioCreateRequest request) {
        return ApiResponse.ok("Usuario creado", service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('usuario:update')")
    public ApiResponse<UsuarioResponse> actualizar(
            @PathVariable UUID id, @Valid @RequestBody UsuarioUpdateRequest request) {
        return ApiResponse.ok("Usuario actualizado", service.actualizar(id, request));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('usuario:update')")
    public ApiResponse<UsuarioResponse> asignarRoles(
            @PathVariable UUID id, @Valid @RequestBody AsignarRolesRequest request) {
        return ApiResponse.ok("Roles asignados", service.asignarRoles(id, request.rolIds()));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('usuario:update')")
    public ApiResponse<UsuarioResponse> cambiarEstado(
            @PathVariable UUID id, @Valid @RequestBody EstadoRequest request) {
        return ApiResponse.ok(
                "Estado actualizado",
                service.cambiarEstado(id, request.activo(), CurrentUser.get().id()));
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(@Valid @RequestBody CambiarPasswordRequest request) {
        service.cambiarPassword(CurrentUser.get().id(), request.actual(), request.nueva());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('usuario:delete')")
    public void eliminar(@PathVariable UUID id) {
        service.eliminar(id, CurrentUser.get().id());
    }
}
