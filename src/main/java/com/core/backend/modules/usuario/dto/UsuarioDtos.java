package com.core.backend.modules.usuario.dto;

import com.core.backend.modules.rbac.dto.RolResumen;
import com.core.backend.modules.sucursal.dto.SucursalResumen;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class UsuarioDtos {

    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,72}$";
    private static final String PASSWORD_MSG = "Mínimo 8 caracteres con mayúscula, minúscula y número";

    private UsuarioDtos() {}

    public record UsuarioCreateRequest(
            @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^[a-zA-Z0-9._-]+$") String usuario,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(max = 100) String nombres,
            @NotBlank @Size(max = 100) String apellidos,
            @NotBlank @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MSG) String password,
            UUID sucursalId,
            @NotEmpty Set<UUID> rolIds) {}

    public record UsuarioUpdateRequest(
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(max = 100) String nombres,
            @NotBlank @Size(max = 100) String apellidos,
            UUID sucursalId) {}

    public record AsignarRolesRequest(@NotEmpty Set<UUID> rolIds) {}

    public record CambiarPasswordRequest(
            @NotBlank String actual,
            @NotBlank @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MSG) String nueva) {}

    public record UsuarioResponse(
            UUID id,
            String usuario,
            String email,
            String nombres,
            String apellidos,
            boolean activo,
            SucursalResumen sucursal,
            List<RolResumen> roles,
            Instant createdAt) {}
}
