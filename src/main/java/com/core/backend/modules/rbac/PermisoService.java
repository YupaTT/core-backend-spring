package com.core.backend.modules.rbac;

import com.core.backend.common.exception.ApiException;
import com.core.backend.modules.rbac.dto.RbacDtos.PermisoRequest;
import com.core.backend.modules.rbac.dto.RbacDtos.PermisoResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermisoService {

    private static final Sort POR_CODIGO = Sort.by("codigo");

    private final PermisoRepository repository;
    private final RbacMapper mapper;

    public List<PermisoResponse> listar(String modulo) {
        List<Permiso> permisos = modulo == null || modulo.isBlank()
                ? repository.findAll(POR_CODIGO)
                : repository.findAllByModuloIgnoreCase(modulo, POR_CODIGO);
        return permisos.stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public PermisoResponse crear(PermisoRequest request) {
        if (repository.existsByCodigoIgnoreCase(request.codigo())) {
            throw ApiException.conflict("Ya existe el permiso " + request.codigo());
        }
        Permiso permiso = new Permiso();
        aplicar(permiso, request);
        return mapper.toResponse(repository.save(permiso));
    }

    @Transactional
    public PermisoResponse actualizar(UUID id, PermisoRequest request) {
        Permiso permiso = buscar(id);
        if (!permiso.getCodigo().equalsIgnoreCase(request.codigo())
                && repository.existsByCodigoIgnoreCase(request.codigo())) {
            throw ApiException.conflict("Ya existe el permiso " + request.codigo());
        }
        aplicar(permiso, request);
        return mapper.toResponse(permiso);
    }

    @Transactional
    public void eliminar(UUID id) {
        Permiso permiso = buscar(id);
        if (repository.isAsignado(id)) {
            throw ApiException.conflict("El permiso está asignado a uno o más roles");
        }
        repository.delete(permiso);
    }

    private Permiso buscar(UUID id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Permiso", id));
    }

    private static void aplicar(Permiso permiso, PermisoRequest request) {
        permiso.setCodigo(request.codigo());
        permiso.setModulo(request.codigo().substring(0, request.codigo().indexOf(':')));
        permiso.setDescripcion(request.descripcion());
    }
}
