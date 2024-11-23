package com.core.backend.modules.sucursal;

import com.core.backend.common.api.ApiResponse;
import com.core.backend.common.api.EstadoRequest;
import com.core.backend.common.api.PageResponse;
import com.core.backend.modules.sucursal.dto.SucursalDtos.SucursalRequest;
import com.core.backend.modules.sucursal.dto.SucursalDtos.SucursalResponse;
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

@Tag(name = "Sucursales")
@RestController
@RequestMapping("/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalService service;

    @GetMapping
    @PreAuthorize("hasAuthority('sucursal:read')")
    public ApiResponse<PageResponse<SucursalResponse>> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return ApiResponse.ok(service.listar(q, activo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sucursal:read')")
    public ApiResponse<SucursalResponse> obtener(@PathVariable UUID id) {
        return ApiResponse.ok(service.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('sucursal:create')")
    public ApiResponse<SucursalResponse> crear(@Valid @RequestBody SucursalRequest request) {
        return ApiResponse.ok("Sucursal creada", service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sucursal:update')")
    public ApiResponse<SucursalResponse> actualizar(
            @PathVariable UUID id, @Valid @RequestBody SucursalRequest request) {
        return ApiResponse.ok("Sucursal actualizada", service.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('sucursal:update')")
    public ApiResponse<SucursalResponse> cambiarEstado(
            @PathVariable UUID id, @Valid @RequestBody EstadoRequest request) {
        return ApiResponse.ok("Estado actualizado", service.cambiarEstado(id, request.activo()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('sucursal:delete')")
    public void eliminar(@PathVariable UUID id) {
        service.eliminar(id);
    }
}
