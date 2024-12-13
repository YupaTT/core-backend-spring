package com.core.backend.integration.storage;

import java.time.Duration;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "app.integrations.storage")
public record StorageProperties(
        @DefaultValue("none") String provider,
        @DefaultValue("10MB") DataSize maxFileSize,
        @DefaultValue Set<String> allowedContentTypes,
        @DefaultValue Local local,
        @DefaultValue S3 s3) {

    public record Local(@DefaultValue("./storage-data") String root) {}

    public record S3(
            String endpoint,
            @DefaultValue("us-east-1") String region,
            String bucket,
            String accessKey,
            String secretKey,
            @DefaultValue("true") boolean pathStyle,
            @DefaultValue("15m") Duration presignedTtl) {}
}
