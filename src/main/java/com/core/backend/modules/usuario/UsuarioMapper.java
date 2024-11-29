package com.core.backend.modules.usuario;

import com.core.backend.modules.auth.dto.AuthDtos.PerfilResponse;
import com.core.backend.modules.rbac.RbacMapper;
import com.core.backend.modules.sucursal.SucursalMapper;
import com.core.backend.modules.usuario.dto.UsuarioDtos.UsuarioResponse;
import java.util.Set;
import java.util.TreeSet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {RbacMapper.class, SucursalMapper.class})
public interface UsuarioMapper {

    UsuarioResponse toResponse(Usuario usuario);

    @Mapping(target = "permisos", expression = "java(permisos(usuario))")
    PerfilResponse toPerfil(Usuario usuario);

    default Set<String> permisos(Usuario usuario) {
        Set<String> permisos = new TreeSet<>(usuario.authorities());
        permisos.removeIf(a -> a.startsWith("ROLE_"));
        return permisos;
    }
}
