package com.core.backend.modules.parametro;

import com.core.backend.common.api.ApiResponse;
import com.core.backend.common.api.EstadoRequest;
import com.core.backend.common.api.PageResponse;
import com.core.backend.modules.parametro.dto.ParametroDtos.ParametroItem;
import com.core.backend.modules.parametro.dto.ParametroDtos.ParametroRequest;
import com.core.backend.modules.parametro.dto.ParametroDtos.ParametroResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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

@Tag(name = "Parámetros")
@RestController
@RequestMapping("/parametros")
@RequiredArgsConstructor
public class ParametroController {

    private final ParametroService service;

    @GetMapping
    @PreAuthorize("hasAuthority('parametro:read')")
    public ApiResponse<PageResponse<ParametroResponse>> listar(
            @RequestParam(required = false) String grupo,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(
                            size = 20,
                            sort = {"grupo", "orden", "nombre"})
                    Pageable pageable) {
        return ApiResponse.ok(service.listar(grupo, q, activo, pageable));
    }

    @GetMapping("/grupos")
    @PreAuthorize("hasAuthority('parametro:read')")
    public ApiResponse<List<String>> grupos() {
        return ApiResponse.ok(service.grupos());
    }

    @Operation(summary = "Ítems activos de un grupo (cacheado), para combos del frontend")
    @GetMapping("/grupo/{grupo}")
    public ApiResponse<List<ParametroItem>> porGrupo(@PathVariable String grupo) {
        return ApiResponse.ok(service.porGrupo(grupo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('parametro:read')")
    public ApiResponse<ParametroResponse> obtener(@PathVariable UUID id) {
        return ApiResponse.ok(service.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('parametro:create')")
    public ApiResponse<ParametroResponse> crear(@Valid @RequestBody ParametroRequest request) {
        return ApiResponse.ok("Parámetro creado", service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('parametro:update')")
    public ApiResponse<ParametroResponse> actualizar(
            @PathVariable UUID id, @Valid @RequestBody ParametroRequest request) {
        return ApiResponse.ok("Parámetro actualizado", service.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('parametro:update')")
    public ApiResponse<ParametroResponse> cambiarEstado(
            @PathVariable UUID id, @Valid @RequestBody EstadoRequest request) {
        return ApiResponse.ok("Estado actualizado", service.cambiarEstado(id, request.activo()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('parametro:delete')")
    public void eliminar(@PathVariable UUID id) {
        service.eliminar(id);
    }
}
