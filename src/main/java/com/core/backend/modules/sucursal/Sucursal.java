package com.core.backend.modules.sucursal;

import com.core.backend.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sucursales")
public class Sucursal extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 20)
    private String sigla;

    private String descripcion;

    @Column(nullable = false)
    private boolean activo = true;
}
