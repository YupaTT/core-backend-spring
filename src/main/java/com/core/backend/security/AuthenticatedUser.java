package com.core.backend.security;

import java.util.UUID;
import org.springframework.security.core.AuthenticatedPrincipal;

/** Principal construido desde los claims del access token, sin consultar la base de datos. */
public record AuthenticatedUser(UUID id, String usuario, String tokenId, java.time.Instant tokenExpiresAt)
        implements AuthenticatedPrincipal {

    @Override
    public String getName() {
        return usuario;
    }
}
