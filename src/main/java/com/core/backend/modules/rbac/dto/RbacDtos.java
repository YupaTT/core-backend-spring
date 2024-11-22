package com.core.backend.modules.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class RbacDtos {

    private RbacDtos() {}

    public record PermisoRequest(
            @NotBlank
                    @Size(max = 100)
                    @Pattern(regexp = "^[a-z0-9_-]+:[a-z0-9_-]+$", message = "Formato esperado modulo:accion")
                    String codigo,
            @Size(max = 255) String descripcion) {}

    public record PermisoResponse(UUID id, String codigo, String modulo, String descripcion) {}

    public record RolCreateRequest(
            @NotBlank
                    @Size(max = 50)
                    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Solo mayúsculas, números y guion bajo")
                    String codigo,
            @NotBlank @Size(max = 100) String nombre,
            @Size(max = 255) String descripcion,
            Set<UUID> permisoIds) {}

    public record RolUpdateRequest(@NotBlank @Size(max = 100) String nombre, @Size(max = 255) String descripcion) {}

    public record AsignarPermisosRequest(@NotNull Set<UUID> permisoIds) {}

    public record RolResponse(
            UUID id,
            String codigo,
            String nombre,
            String descripcion,
            boolean sistema,
            boolean activo,
            List<PermisoResponse> permisos,
            Instant createdAt) {}
}
