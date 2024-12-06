package com.core.backend.integration.redis;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

@RequiredArgsConstructor
class RedisDistributedLock implements DistributedLock {

    private static final String PREFIX = "core:lock:";
    // Libera solo si el lock sigue siendo nuestro
    private static final RedisScript<Long> RELEASE = RedisScript.of(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate redis;

    @Override
    public <T> Optional<T> runLocked(String name, Duration ttl, Supplier<T> task) {
        String key = PREFIX + name;
        String owner = UUID.randomUUID().toString();
        if (!Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key, owner, ttl))) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(task.get());
        } finally {
            redis.execute(RELEASE, List.of(key), owner);
        }
    }
}
