package com.core.backend.modules.sucursal;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SucursalRepository extends JpaRepository<Sucursal, UUID>, JpaSpecificationExecutor<Sucursal> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsBySiglaIgnoreCase(String sigla);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, UUID id);

    boolean existsBySiglaIgnoreCaseAndIdNot(String sigla, UUID id);
}
