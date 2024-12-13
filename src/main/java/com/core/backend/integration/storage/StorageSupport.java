package com.core.backend.integration.storage;

import com.core.backend.common.exception.ApiException;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

final class StorageSupport {

    private StorageSupport() {}

    static void validate(MultipartFile file, StorageProperties properties) {
        if (file.isEmpty()) {
            throw ApiException.badRequest("El archivo está vacío");
        }
        if (file.getSize() > properties.maxFileSize().toBytes()) {
            throw ApiException.badRequest("El archivo supera el tamaño máximo de " + properties.maxFileSize());
        }
        if (!properties.allowedContentTypes().isEmpty()
                && !properties.allowedContentTypes().contains(file.getContentType())) {
            throw ApiException.badRequest("Tipo de archivo no permitido: " + file.getContentType());
        }
    }

    /** Clave {@code carpeta/aaaa/mm/uuid.ext}: nunca usa el nombre del cliente para evitar path traversal. */
    static String newKey(String folder, MultipartFile file) {
        String safeFolder = folder == null ? "general" : folder.replaceAll("[^a-zA-Z0-9_-]", "");
        if (safeFolder.isBlank()) {
            safeFolder = "general";
        }
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String suffix =
                ext == null ? "" : "." + ext.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        LocalDate today = LocalDate.now();
        return "%s/%d/%02d/%s%s"
                .formatted(safeFolder, today.getYear(), today.getMonthValue(), UUID.randomUUID(), suffix);
    }

    static void checkKey(String key) {
        if (key == null || key.isBlank() || key.contains("..") || key.startsWith("/")) {
            throw ApiException.badRequest("Clave de archivo inválida");
        }
    }
}
