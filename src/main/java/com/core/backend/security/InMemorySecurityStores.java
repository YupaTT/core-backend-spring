package com.core.backend.security;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Implementaciones en memoria para una sola instancia; con Redis activo se usan las de integration.redis. */
@Configuration
@ConditionalOnProperty(
        prefix = "app.integrations.redis",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true)
public class InMemorySecurityStores {

    @Bean
    public TokenRevocationStore tokenRevocationStore() {
        return new InMemoryTokenRevocationStore(Clock.systemUTC());
    }

    @Bean
    public LoginAttemptService loginAttemptService(SecurityProperties properties) {
        return new InMemoryLoginAttemptService(properties, Clock.systemUTC());
    }

    static final class InMemoryTokenRevocationStore implements TokenRevocationStore {

        private final Map<String, Instant> revoked = new ConcurrentHashMap<>();
        private final Clock clock;

        InMemoryTokenRevocationStore(Clock clock) {
            this.clock = clock;
        }

        @Override
        public void revoke(String tokenId, Instant expiresAt) {
            Instant now = clock.instant();
            revoked.values().removeIf(exp -> exp.isBefore(now));
            revoked.put(tokenId, expiresAt);
        }

        @Override
        public boolean isRevoked(String tokenId) {
            Instant exp = revoked.get(tokenId);
            return exp != null && exp.isAfter(clock.instant());
        }
    }

    static final class InMemoryLoginAttemptService implements LoginAttemptService {

        private record Attempts(int count, Instant windowEnd) {}

        private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();
        private final SecurityProperties properties;
        private final Clock clock;

        InMemoryLoginAttemptService(SecurityProperties properties, Clock clock) {
            this.properties = properties;
            this.clock = clock;
        }

        @Override
        public boolean isBlocked(String key) {
            Attempts a = attempts.get(key);
            return a != null && a.windowEnd().isAfter(clock.instant()) && a.count() >= properties.loginMaxAttempts();
        }

        @Override
        public void loginFailed(String key) {
            Instant now = clock.instant();
            attempts.values().removeIf(a -> a.windowEnd().isBefore(now));
            attempts.compute(
                    key,
                    (k, a) -> a == null || a.windowEnd().isBefore(now)
                            ? new Attempts(1, now.plus(properties.loginLockDuration()))
                            : new Attempts(a.count() + 1, a.windowEnd()));
        }

        @Override
        public void loginSucceeded(String key) {
            attempts.remove(key);
        }
    }
}
