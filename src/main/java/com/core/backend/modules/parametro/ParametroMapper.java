package com.core.backend.modules.parametro;

import com.core.backend.modules.parametro.dto.ParametroDtos.ParametroItem;
import com.core.backend.modules.parametro.dto.ParametroDtos.ParametroRequest;
import com.core.backend.modules.parametro.dto.ParametroDtos.ParametroResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface ParametroMapper {

    ParametroResponse toResponse(Parametro parametro);

    ParametroItem toItem(Parametro parametro);

    @Mapping(target = "orden", expression = "java(request.orden() == null ? 0 : request.orden())")
    void update(@MappingTarget Parametro parametro, ParametroRequest request);
}
