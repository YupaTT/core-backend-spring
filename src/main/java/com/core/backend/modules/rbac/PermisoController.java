package com.core.backend.modules.rbac;

import com.core.backend.common.api.ApiResponse;
import com.core.backend.modules.rbac.dto.RbacDtos.PermisoRequest;
import com.core.backend.modules.rbac.dto.RbacDtos.PermisoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Permisos")
@RestController
@RequestMapping("/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final PermisoService service;

    @GetMapping
    @PreAuthorize("hasAuthority('permiso:read')")
    public ApiResponse<List<PermisoResponse>> listar(@RequestParam(required = false) String modulo) {
        return ApiResponse.ok(service.listar(modulo));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('permiso:create')")
    public ApiResponse<PermisoResponse> crear(@Valid @RequestBody PermisoRequest request) {
        return ApiResponse.ok("Permiso creado", service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permiso:update')")
    public ApiResponse<PermisoResponse> actualizar(@PathVariable UUID id, @Valid @RequestBody PermisoRequest request) {
        return ApiResponse.ok("Permiso actualizado", service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('permiso:delete')")
    public void eliminar(@PathVariable UUID id) {
        service.eliminar(id);
    }
}
