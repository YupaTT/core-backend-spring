package com.core.backend.modules.usuario;

import com.core.backend.modules.rbac.RbacMapper;
import com.core.backend.modules.sucursal.SucursalMapper;
import com.core.backend.modules.usuario.dto.UsuarioDtos.UsuarioResponse;
import org.mapstruct.Mapper;

@Mapper(uses = {RbacMapper.class, SucursalMapper.class})
public interface UsuarioMapper {

    UsuarioResponse toResponse(Usuario usuario);
}
