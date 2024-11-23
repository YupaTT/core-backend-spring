package com.core.backend.modules.sucursal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class SucursalDtos {

    private SucursalDtos() {}

    public record SucursalRequest(
            @NotBlank @Size(max = 120) String nombre,
            @NotBlank @Size(max = 20) String sigla,
            @Size(max = 255) String descripcion) {}

    public record SucursalResponse(
            UUID id, String nombre, String sigla, String descripcion, boolean activo, Instant createdAt) {}
}
