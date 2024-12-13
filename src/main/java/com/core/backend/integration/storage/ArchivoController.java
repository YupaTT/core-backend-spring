package com.core.backend.integration.storage;

import com.core.backend.common.api.ApiResponse;
import com.core.backend.integration.storage.StorageService.StoredFile;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Archivos")
@RestController
@RequestMapping("/archivos")
@RequiredArgsConstructor
@ConditionalOnExpression("'${app.integrations.storage.provider:none}' != 'none'")
public class ArchivoController {

    private final StorageService storage;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('archivo:create')")
    public ApiResponse<StoredFile> subir(
            @RequestPart("archivo") MultipartFile archivo, @RequestParam(defaultValue = "general") String carpeta) {
        return ApiResponse.ok("Archivo almacenado", storage.store(carpeta, archivo));
    }

    @GetMapping("/descargar")
    @PreAuthorize("hasAuthority('archivo:read')")
    public ResponseEntity<InputStreamResource> descargar(@RequestParam String key) {
        InputStreamResource body = new InputStreamResource(storage.load(key));
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(key.substring(key.lastIndexOf('/') + 1))
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(body);
    }

    @GetMapping("/url")
    @PreAuthorize("hasAuthority('archivo:read')")
    public ApiResponse<Map<String, String>> url(@RequestParam String key) {
        return ApiResponse.ok(storage.presignedUrl(key)
                .map(u -> Map.of("url", u))
                .orElseGet(() -> Map.of("url", "/archivos/descargar?key=" + key)));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('archivo:delete')")
    public void eliminar(@RequestParam String key) {
        storage.delete(key);
    }
}
