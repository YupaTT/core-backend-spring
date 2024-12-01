package com.core.backend.modules.parametro;

import com.core.backend.common.api.PageResponse;
import com.core.backend.common.exception.ApiException;
import com.core.backend.modules.parametro.dto.ParametroDtos.ParametroItem;
import com.core.backend.modules.parametro.dto.ParametroDtos.ParametroRequest;
import com.core.backend.modules.parametro.dto.ParametroDtos.ParametroResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParametroService {

    private final ParametroRepository repository;
    private final ParametroMapper mapper;

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

    public List<ParametroItem> porGrupo(String grupo) {
        return repository.findAllByGrupoAndActivoTrueOrderByOrdenAscNombreAsc(grupo.toUpperCase()).stream()
                .map(mapper::toItem)
                .toList();
    }

    public List<String> grupos() {
        return repository.findGrupos();
    }

    @Transactional
    public ParametroResponse crear(ParametroRequest request) {
        if (repository.existsByGrupoAndCodigo(request.grupo(), request.codigo())) {
            throw ApiException.conflict(
                    "Ya existe el código %s en el grupo %s".formatted(request.codigo(), request.grupo()));
        }
        Parametro parametro = new Parametro();
        mapper.update(parametro, request);
        return mapper.toResponse(repository.save(parametro));
    }

    @Transactional
    public ParametroResponse actualizar(UUID id, ParametroRequest request) {
        Parametro parametro = buscar(id);
        if (repository.existsByGrupoAndCodigoAndIdNot(request.grupo(), request.codigo(), id)) {
            throw ApiException.conflict(
                    "Ya existe el código %s en el grupo %s".formatted(request.codigo(), request.grupo()));
        }
        mapper.update(parametro, request);
        return mapper.toResponse(parametro);
    }

    @Transactional
    public ParametroResponse cambiarEstado(UUID id, boolean activo) {
        Parametro parametro = buscar(id);
        parametro.setActivo(activo);
        return mapper.toResponse(parametro);
    }

    @Transactional
    public void eliminar(UUID id) {
        repository.delete(buscar(id));
    }

    private Parametro buscar(UUID id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Parámetro", id));
    }
}
