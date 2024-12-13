package com.core.backend.integration.storage;

import java.io.InputStream;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    StoredFile store(String folder, MultipartFile file);

    InputStream load(String key);

    void delete(String key);

    /** URL temporal de descarga directa; vacío si el proveedor no la soporta. */
    Optional<String> presignedUrl(String key);

    record StoredFile(String key, String nombreOriginal, String contentType, long size) {}
}
