package com.core.backend.integration.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

/**
 * Caché transaccional: las evicciones se aplican tras el commit para no repoblar con datos viejos. Los valores
 * cacheados deben ser {@link java.io.Serializable}.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.integrations.redis", name = "enabled", havingValue = "true")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory, CacheProperties properties) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(properties.defaultTtl())
                .disableCachingNullValues()
                .prefixCacheNameWith("core:cache:")
                .serializeValuesWith(SerializationPair.fromSerializer(new JdkSerializationRedisSerializer()));
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .transactionAware();
        properties.ttls().forEach((name, ttl) -> builder.withCacheConfiguration(name, defaults.entryTtl(ttl)));
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "app.integrations.redis",
            name = "enabled",
            havingValue = "false",
            matchIfMissing = true)
    public CacheManager localCacheManager() {
        return new TransactionAwareCacheManagerProxy(new ConcurrentMapCacheManager());
    }
}
