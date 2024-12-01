package com.core.backend.modules.parametro;

import com.core.backend.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Catálogo genérico clave-valor agrupado (tipos de documento, estados, configuraciones, etc.). */
@Getter
@Setter
@Entity
@Table(name = "parametros")
public class Parametro extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String grupo;

    @Column(nullable = false, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String valor;

    private String descripcion;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false)
    private boolean activo = true;
}
