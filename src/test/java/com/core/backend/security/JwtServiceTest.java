package com.core.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auth0.jwt.exceptions.JWTVerificationException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static SecurityProperties props(String secret, Duration ttl) {
        return new SecurityProperties(
                new SecurityProperties.Jwt(secret, "test", ttl, Duration.ofDays(1)),
                false,
                5,
                Duration.ofMinutes(15),
                new SecurityProperties.BootstrapAdmin(false, null, null, null),
                List.of());
    }

    private final JwtService jwt =
            new JwtService(props("secret-0123456789-0123456789-0123456789", Duration.ofMinutes(5)));

    @Test
    void emiteYVerificaClaims() {
        UUID id = UUID.randomUUID();
        var token = jwt.issueAccessToken(id, "ana", List.of("ROLE_X", "parametro:read"));

        var verified = jwt.verifyAccessToken(token.value());

        assertThat(verified.user().id()).isEqualTo(id);
        assertThat(verified.user().usuario()).isEqualTo("ana");
        assertThat(verified.user().tokenId()).isNotBlank();
        assertThat(verified.authorities()).containsExactly("ROLE_X", "parametro:read");
    }

    @Test
    void rechazaFirmaDeOtraClave() {
        var otro = new JwtService(props("otra-clave-0123456789-0123456789-01234", Duration.ofMinutes(5)));
        String token =
                otro.issueAccessToken(UUID.randomUUID(), "ana", List.of()).value();

        assertThatThrownBy(() -> jwt.verifyAccessToken(token)).isInstanceOf(JWTVerificationException.class);
    }

    @Test
    void rechazaTokenExpirado() {
        var expira = new JwtService(props("secret-0123456789-0123456789-0123456789", Duration.ofSeconds(-30)));
        String token =
                expira.issueAccessToken(UUID.randomUUID(), "ana", List.of()).value();

        assertThatThrownBy(() -> jwt.verifyAccessToken(token)).isInstanceOf(JWTVerificationException.class);
    }
}
