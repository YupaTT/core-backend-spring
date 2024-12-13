package com.core.backend.integration.storage;

import com.core.backend.common.exception.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Para desarrollo o despliegues de una sola instancia con volumen persistente. */
@Service
@ConditionalOnProperty(prefix = "app.integrations.storage", name = "provider", havingValue = "local")
public class LocalStorageService implements StorageService {

    private final StorageProperties properties;
    private final Path root;

    public LocalStorageService(StorageProperties properties) {
        this.properties = properties;
        this.root = Path.of(properties.local().root()).toAbsolutePath().normalize();
    }

    @Override
    public StoredFile store(String folder, MultipartFile file) {
        StorageSupport.validate(file, properties);
        String key = StorageSupport.newKey(folder, file);
        Path target = resolve(key);
        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(target.getParent());
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new StoredFile(key, file.getOriginalFilename(), file.getContentType(), file.getSize());
    }

    @Override
    public InputStream load(String key) {
        Path path = resolve(key);
        if (!Files.exists(path)) {
            throw ApiException.notFound("Archivo", key);
        }
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<String> presignedUrl(String key) {
        return Optional.empty();
    }

    private Path resolve(String key) {
        StorageSupport.checkKey(key);
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) {
            throw ApiException.badRequest("Clave de archivo inválida");
        }
        return path;
    }
}
