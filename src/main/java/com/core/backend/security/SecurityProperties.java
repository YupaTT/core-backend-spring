package com.core.backend.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        @Valid @NotNull Jwt jwt,
        @DefaultValue("true") boolean loginPasswordBase64,
        @DefaultValue("5") int loginMaxAttempts,
        @DefaultValue("15m") Duration loginLockDuration,
        @Valid @NotNull BootstrapAdmin bootstrapAdmin,
        @DefaultValue("http://localhost:3000") List<String> corsAllowedOrigins) {

    public record Jwt(
            @NotBlank @Size(min = 32, message = "JWT_SECRET debe tener al menos 32 caracteres") String secret,
            @DefaultValue("core-backend-spring") String issuer,
            @DefaultValue("15m") Duration accessTokenTtl,
            @DefaultValue("7d") Duration refreshTokenTtl) {}

    public record BootstrapAdmin(boolean enabled, String username, String email, String password) {}
}
