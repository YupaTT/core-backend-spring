package com.core.backend.modules.parametro;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ParametroRepository extends JpaRepository<Parametro, UUID>, JpaSpecificationExecutor<Parametro> {

    List<Parametro> findAllByGrupoAndActivoTrueOrderByOrdenAscNombreAsc(String grupo);

    boolean existsByGrupoAndCodigo(String grupo, String codigo);

    boolean existsByGrupoAndCodigoAndIdNot(String grupo, String codigo, UUID id);

    @Query("select distinct p.grupo from Parametro p order by p.grupo")
    List<String> findGrupos();
}
