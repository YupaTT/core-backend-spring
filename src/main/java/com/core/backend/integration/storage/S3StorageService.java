package com.core.backend.integration.storage;

import com.core.backend.common.exception.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/** AWS S3 o compatibles (MinIO, Cloudflare R2, DigitalOcean Spaces) vía {@code endpoint}. */
@Service
@ConditionalOnProperty(prefix = "app.integrations.storage", name = "provider", havingValue = "s3")
public class S3StorageService implements StorageService, AutoCloseable {

    private final StorageProperties properties;
    private final S3Client client;
    private final S3Presigner presigner;

    public S3StorageService(StorageProperties properties) {
        this.properties = properties;
        StorageProperties.S3 s3 = properties.s3();
        Region region = Region.of(s3.region());
        AwsCredentialsProvider credentials =
                s3.accessKey() == null || s3.accessKey().isBlank()
                        ? DefaultCredentialsProvider.builder().build()
                        : StaticCredentialsProvider.create(AwsBasicCredentials.create(s3.accessKey(), s3.secretKey()));
        S3Configuration serviceConfig =
                S3Configuration.builder().pathStyleAccessEnabled(s3.pathStyle()).build();
        var clientBuilder = S3Client.builder()
                .region(region)
                .credentialsProvider(credentials)
                .serviceConfiguration(serviceConfig);
        var presignerBuilder = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentials)
                .serviceConfiguration(serviceConfig);
        if (s3.endpoint() != null && !s3.endpoint().isBlank()) {
            clientBuilder.endpointOverride(URI.create(s3.endpoint()));
            presignerBuilder.endpointOverride(URI.create(s3.endpoint()));
        }
        this.client = clientBuilder.build();
        this.presigner = presignerBuilder.build();
    }

    @Override
    public StoredFile store(String folder, MultipartFile file) {
        StorageSupport.validate(file, properties);
        String key = StorageSupport.newKey(folder, file);
        RequestBody body;
        try {
            body = RequestBody.fromInputStream(file.getInputStream(), file.getSize());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        client.putObject(b -> b.bucket(properties.s3().bucket()).key(key).contentType(file.getContentType()), body);
        return new StoredFile(key, file.getOriginalFilename(), file.getContentType(), file.getSize());
    }

    @Override
    public InputStream load(String key) {
        StorageSupport.checkKey(key);
        try {
            return client.getObject(b -> b.bucket(properties.s3().bucket()).key(key));
        } catch (NoSuchKeyException e) {
            throw ApiException.notFound("Archivo", key);
        }
    }

    @Override
    public void delete(String key) {
        StorageSupport.checkKey(key);
        client.deleteObject(b -> b.bucket(properties.s3().bucket()).key(key));
    }

    @Override
    public Optional<String> presignedUrl(String key) {
        StorageSupport.checkKey(key);
        var request = presigner.presignGetObject(p -> p.signatureDuration(
                        properties.s3().presignedTtl())
                .getObjectRequest(g -> g.bucket(properties.s3().bucket()).key(key)));
        return Optional.of(request.url().toString());
    }

    @Override
    public void close() {
        presigner.close();
        client.close();
    }
}
