package com.core.backend.modules.sucursal;

import com.core.backend.common.api.PageResponse;
import com.core.backend.common.exception.ApiException;
import com.core.backend.modules.sucursal.dto.SucursalDtos.SucursalRequest;
import com.core.backend.modules.sucursal.dto.SucursalDtos.SucursalResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SucursalService {

    private final SucursalRepository repository;
    private final SucursalMapper mapper;

    public PageResponse<SucursalResponse> listar(String q, Boolean activo, Pageable pageable) {
        Specification<Sucursal> spec = (root, query, cb) -> cb.conjunction();
        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(cb.like(cb.lower(root.get("nombre")), like), cb.like(cb.lower(root.get("sigla")), like)));
        }
        if (activo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("activo"), activo));
        }
        return PageResponse.from(repository.findAll(spec, pageable).map(mapper::toResponse));
    }

    public SucursalResponse obtener(UUID id) {
        return mapper.toResponse(buscar(id));
    }

    /** Usado por el módulo de usuarios. */
    public Sucursal referencia(UUID id) {
        return id == null ? null : buscar(id);
    }

    @Transactional
    public SucursalResponse crear(SucursalRequest request) {
        if (repository.existsByNombreIgnoreCase(request.nombre())
                || repository.existsBySiglaIgnoreCase(request.sigla())) {
            throw ApiException.conflict("Ya existe una sucursal con ese nombre o sigla");
        }
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public SucursalResponse actualizar(UUID id, SucursalRequest request) {
        Sucursal sucursal = buscar(id);
        if (repository.existsByNombreIgnoreCaseAndIdNot(request.nombre(), id)
                || repository.existsBySiglaIgnoreCaseAndIdNot(request.sigla(), id)) {
            throw ApiException.conflict("Ya existe una sucursal con ese nombre o sigla");
        }
        mapper.update(sucursal, request);
        return mapper.toResponse(sucursal);
    }

    @Transactional
    public SucursalResponse cambiarEstado(UUID id, boolean activo) {
        Sucursal sucursal = buscar(id);
        sucursal.setActivo(activo);
        return mapper.toResponse(sucursal);
    }

    @Transactional
    public void eliminar(UUID id) {
        Sucursal sucursal = buscar(id);
        repository.delete(sucursal);
    }

    private Sucursal buscar(UUID id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Sucursal", id));
    }
}
