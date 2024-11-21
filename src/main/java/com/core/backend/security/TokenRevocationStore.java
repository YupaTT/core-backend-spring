package com.core.backend.security;

import java.time.Instant;

/** Lista de access tokens revocados (logout) hasta su expiración natural. */
public interface TokenRevocationStore {

    void revoke(String tokenId, Instant expiresAt);

    boolean isRevoked(String tokenId);
}
