package com.core.backend.modules.sucursal;

import com.core.backend.modules.sucursal.dto.SucursalDtos.SucursalRequest;
import com.core.backend.modules.sucursal.dto.SucursalDtos.SucursalResponse;
import com.core.backend.modules.sucursal.dto.SucursalResumen;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface SucursalMapper {

    SucursalResponse toResponse(Sucursal sucursal);

    SucursalResumen toResumen(Sucursal sucursal);

    Sucursal toEntity(SucursalRequest request);

    void update(@MappingTarget Sucursal sucursal, SucursalRequest request);
}
