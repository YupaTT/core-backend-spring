package com.core.backend.modules.usuario;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID>, JpaSpecificationExecutor<Usuario> {

    @EntityGraph(attributePaths = {"roles", "roles.permisos", "sucursal"})
    Optional<Usuario> findWithAccesosByUsuarioIgnoreCase(String usuario);

    @EntityGraph(attributePaths = {"roles", "roles.permisos", "sucursal"})
    Optional<Usuario> findWithAccesosById(UUID id);

    boolean existsByUsuarioIgnoreCase(String usuario);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
}
