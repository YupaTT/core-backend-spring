package com.core.backend.modules.rbac;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PermisoRepository extends JpaRepository<Permiso, UUID> {

    List<Permiso> findAllByModuloIgnoreCase(String modulo, Sort sort);

    List<Permiso> findAllByIdIn(Collection<UUID> ids);

    boolean existsByCodigoIgnoreCase(String codigo);

    @Query("select count(r) > 0 from Rol r join r.permisos p where p.id = :permisoId")
    boolean isAsignado(UUID permisoId);
}
