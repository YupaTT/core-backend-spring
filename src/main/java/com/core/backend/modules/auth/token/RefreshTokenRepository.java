package com.core.backend.modules.auth.token;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken t set t.revoked = true where t.usuario.id = :usuarioId and t.revoked = false")
    int revokeAllByUsuarioId(UUID usuarioId);

    @Modifying
    @Query("delete from RefreshToken t where t.expiresAt < :limite or t.revoked = true")
    int deleteExpiredOrRevoked(Instant limite);
}
