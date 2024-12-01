package com.core.backend.modules.auth.dto;

import com.core.backend.modules.rbac.dto.RolResumen;
import com.core.backend.modules.sucursal.dto.SucursalResumen;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {}

    public record LoginRequest(
            @NotBlank @Size(max = 50) String usuario, @NotBlank @Size(max = 512) String contrasena) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record LogoutRequest(String refreshToken) {}

    public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn, long exp) {}

    public record PerfilResponse(
            UUID id,
            String usuario,
            String email,
            String nombres,
            String apellidos,
            SucursalResumen sucursal,
            List<RolResumen> roles,
            Set<String> permisos) {}

    public record LoginResponse(TokenResponse tokens, PerfilResponse perfil) {}

    public record VerifyResponse(boolean isAuthenticated) {}

    /** Contrato de {@code docs/requerimientofront.md}: {status, message, datos}. */
    public record FrontLoginResponse(String status, String message, FrontDatos datos) {}

    public record FrontDatos(
            @JsonUnwrapped PerfilResponse perfil,
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken,
            long exp) {}
}
