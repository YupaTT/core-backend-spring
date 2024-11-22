package com.core.backend.modules.rbac;

import com.core.backend.modules.rbac.dto.RbacDtos.PermisoResponse;
import com.core.backend.modules.rbac.dto.RbacDtos.RolResponse;
import com.core.backend.modules.rbac.dto.RolResumen;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper
public interface RbacMapper {

    PermisoResponse toResponse(Permiso permiso);

    RolResumen toResumen(Rol rol);

    RolResponse toResponse(Rol rol);

    default List<PermisoResponse> toPermisoResponses(Collection<Permiso> permisos) {
        return permisos.stream()
                .sorted(Comparator.comparing(Permiso::getCodigo))
                .map(this::toResponse)
                .toList();
    }

    default List<RolResumen> toResumenes(Collection<Rol> roles) {
        return roles.stream()
                .sorted(Comparator.comparing(Rol::getCodigo))
                .map(this::toResumen)
                .toList();
    }
}
