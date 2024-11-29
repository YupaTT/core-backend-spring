package com.core.backend.modules.auth.token;

import com.core.backend.common.exception.ApiException;
import com.core.backend.modules.usuario.Usuario;
import com.core.backend.security.SecurityProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository repository;
    private final SecurityProperties properties;

    @Transactional
    public String create(Usuario usuario) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        repository.save(new RefreshToken(
                usuario, hash(raw), Instant.now().plus(properties.jwt().refreshTokenTtl())));
        return raw;
    }

    /** Consume el token (rotación). Si llega uno ya revocado se asume robo y se revocan todos los del usuario. */
    @Transactional(noRollbackFor = ApiException.class)
    public UUID consume(String raw) {
        RefreshToken token = repository
                .findByTokenHash(hash(raw))
                .orElseThrow(() -> ApiException.unauthorized("Refresh token inválido"));
        UUID usuarioId = token.getUsuario().getId();
        if (token.isRevoked()) {
            log.warn("Reutilización de refresh token detectada para usuario {}", usuarioId);
            repository.revokeAllByUsuarioId(usuarioId);
            throw ApiException.unauthorized("Refresh token inválido");
        }
        if (token.isExpired()) {
            throw ApiException.unauthorized("Refresh token expirado");
        }
        token.setRevoked(true);
        return usuarioId;
    }

    @Transactional
    public void revoke(String raw, UUID usuarioId) {
        repository
                .findByTokenHash(hash(raw))
                .filter(t -> t.getUsuario().getId().equals(usuarioId))
                .ifPresent(t -> t.setRevoked(true));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void revokeAll(UUID usuarioId) {
        repository.revokeAllByUsuarioId(usuarioId);
    }

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void purge() {
        int borrados = repository.deleteExpiredOrRevoked(Instant.now());
        log.info("Refresh tokens purgados: {}", borrados);
    }

    static String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
