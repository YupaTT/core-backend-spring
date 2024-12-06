package com.core.backend.integration.redis;

import com.core.backend.security.LoginAttemptService;
import com.core.backend.security.SecurityProperties;
import com.core.backend.security.TokenRevocationStore;
import java.time.Duration;
import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Estado compartido entre instancias (revocación de tokens, intentos de login, locks). */
@Configuration
@ConditionalOnProperty(prefix = "app.integrations.redis", name = "enabled", havingValue = "true")
public class RedisIntegrationConfig {

    private static final String REVOKED_PREFIX = "core:auth:revoked:";
    private static final String ATTEMPTS_PREFIX = "core:auth:attempts:";

    @Bean
    public TokenRevocationStore tokenRevocationStore(StringRedisTemplate redis) {
        return new TokenRevocationStore() {
            @Override
            public void revoke(String tokenId, Instant expiresAt) {
                Duration ttl = Duration.between(Instant.now(), expiresAt);
                if (!ttl.isNegative() && !ttl.isZero()) {
                    redis.opsForValue().set(REVOKED_PREFIX + tokenId, "1", ttl);
                }
            }

            @Override
            public boolean isRevoked(String tokenId) {
                return Boolean.TRUE.equals(redis.hasKey(REVOKED_PREFIX + tokenId));
            }
        };
    }

    @Bean
    public LoginAttemptService loginAttemptService(StringRedisTemplate redis, SecurityProperties properties) {
        return new LoginAttemptService() {
            @Override
            public boolean isBlocked(String key) {
                String value = redis.opsForValue().get(ATTEMPTS_PREFIX + key);
                return value != null && Integer.parseInt(value) >= properties.loginMaxAttempts();
            }

            @Override
            public void loginFailed(String key) {
                Long count = redis.opsForValue().increment(ATTEMPTS_PREFIX + key);
                if (count != null && count == 1L) {
                    redis.expire(ATTEMPTS_PREFIX + key, properties.loginLockDuration());
                }
            }

            @Override
            public void loginSucceeded(String key) {
                redis.delete(ATTEMPTS_PREFIX + key);
            }
        };
    }

    @Bean
    public DistributedLock distributedLock(StringRedisTemplate redis) {
        return new RedisDistributedLock(redis);
    }
}
