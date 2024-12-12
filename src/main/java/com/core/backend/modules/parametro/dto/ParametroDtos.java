package com.core.backend.modules.parametro.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public final class ParametroDtos {

    private static final String CODE_REGEX = "^[A-Z0-9_]+$";
    private static final String CODE_MSG = "Solo mayúsculas, números y guion bajo";

    private ParametroDtos() {}

    public record ParametroRequest(
            @NotBlank @Size(max = 50) @Pattern(regexp = CODE_REGEX, message = CODE_MSG) String grupo,
            @NotBlank @Size(max = 50) @Pattern(regexp = CODE_REGEX, message = CODE_MSG) String codigo,
            @NotBlank @Size(max = 150) String nombre,
            @Size(max = 500) String valor,
            @Size(max = 255) String descripcion,
            @Min(0) Integer orden) {}

    public record ParametroResponse(
            UUID id,
            String grupo,
            String codigo,
            String nombre,
            String valor,
            String descripcion,
            int orden,
            boolean activo,
            Long version,
            Instant updatedAt)
            implements Serializable {}

    /** Versión liviana para combos/selects del frontend; es la que se cachea. */
    public record ParametroItem(String codigo, String nombre, String valor) implements Serializable {}

    public record ParametroEvent(UUID id, String grupo, String codigo, String accion) {}
}
