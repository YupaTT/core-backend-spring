package com.core.backend.modules.rbac;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface RolRepository extends JpaRepository<Rol, UUID>, JpaSpecificationExecutor<Rol> {

    @EntityGraph(attributePaths = "permisos")
    Optional<Rol> findWithPermisosById(UUID id);

    Optional<Rol> findByCodigo(String codigo);

    List<Rol> findAllByIdIn(Collection<UUID> ids);

    boolean existsByCodigoIgnoreCase(String codigo);

    @Query("select count(u) > 0 from Usuario u join u.roles r where r.id = :rolId")
    boolean isAsignado(UUID rolId);
}
