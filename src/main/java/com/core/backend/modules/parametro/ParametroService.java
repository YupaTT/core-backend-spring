package com.core.backend.modules.parametro;

import com.core.backend.common.api.PageResponse;
import com.core.backend.common.exception.ApiException;
import com.core.backend.integration.cache.CacheNames;
import com.core.backend.integration.messaging.EventPublisher;
import com.core.backend.modules.parametro.dto.ParametroDtos.ParametroEvent;
import com.core.backend.modules.parametro.dto.ParametroDtos.ParametroItem;
import com.core.backend.modules.parametro.dto.ParametroDtos.ParametroRequest;
import com.core.backend.modules.parametro.dto.ParametroDtos.ParametroResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParametroService {

    private static final String AGGREGATE = "parametro";

    private final ParametroRepository repository;
    private final ParametroMapper mapper;
    private final EventPublisher eventPublisher;

    public PageResponse<ParametroResponse> listar(String grupo, String q, Boolean activo, Pageable pageable) {
        Specification<Parametro> spec = (root, query, cb) -> cb.conjunction();
        if (grupo != null && !grupo.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("grupo"), grupo.toUpperCase()));
        }
        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(cb.like(cb.lower(root.get("codigo")), like), cb.like(cb.lower(root.get("nombre")), like)));
        }
        if (activo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("activo"), activo));
        }
        return PageResponse.from(repository.findAll(spec, pageable).map(mapper::toResponse));
    }

    public ParametroResponse obtener(UUID id) {
        return mapper.toResponse(buscar(id));
    }

    @Cacheable(cacheNames = CacheNames.PARAMETROS, key = "#grupo.toUpperCase()")
    public List<ParametroItem> porGrupo(String grupo) {
        return new ArrayList<>(
                repository.findAllByGrupoAndActivoTrueOrderByOrdenAscNombreAsc(grupo.toUpperCase()).stream()
                        .map(mapper::toItem)
                        .toList());
    }

    public List<String> grupos() {
        return repository.findGrupos();
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.PARAMETROS, key = "#request.grupo()")
    public ParametroResponse crear(ParametroRequest request) {
        if (repository.existsByGrupoAndCodigo(request.grupo(), request.codigo())) {
            throw ApiException.conflict(
                    "Ya existe el código %s en el grupo %s".formatted(request.codigo(), request.grupo()));
        }
        Parametro parametro = new Parametro();
        mapper.update(parametro, request);
        Parametro guardado = repository.save(parametro);
        publicar(guardado, "CREADO");
        return mapper.toResponse(guardado);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.PARAMETROS, allEntries = true)
    public ParametroResponse actualizar(UUID id, ParametroRequest request) {
        Parametro parametro = buscar(id);
        if (repository.existsByGrupoAndCodigoAndIdNot(request.grupo(), request.codigo(), id)) {
            throw ApiException.conflict(
                    "Ya existe el código %s en el grupo %s".formatted(request.codigo(), request.grupo()));
        }
        mapper.update(parametro, request);
        publicar(parametro, "ACTUALIZADO");
        return mapper.toResponse(parametro);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.PARAMETROS, allEntries = true)
    public ParametroResponse cambiarEstado(UUID id, boolean activo) {
        Parametro parametro = buscar(id);
        parametro.setActivo(activo);
        publicar(parametro, activo ? "ACTIVADO" : "DESACTIVADO");
        return mapper.toResponse(parametro);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.PARAMETROS, allEntries = true)
    public void eliminar(UUID id) {
        Parametro parametro = buscar(id);
        repository.delete(parametro);
        publicar(parametro, "ELIMINADO");
    }

    private void publicar(Parametro p, String accion) {
        eventPublisher.publish(
                AGGREGATE,
                p.getId().toString(),
                "parametro." + accion.toLowerCase(),
                new ParametroEvent(p.getId(), p.getGrupo(), p.getCodigo(), accion));
    }

    private Parametro buscar(UUID id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Parámetro", id));
    }
}
