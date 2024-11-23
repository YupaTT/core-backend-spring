package com.core.backend.modules.usuario;

import com.core.backend.common.persistence.BaseEntity;
import com.core.backend.modules.rbac.Permiso;
import com.core.backend.modules.rbac.Rol;
import com.core.backend.modules.sucursal.Sucursal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuario extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String usuario;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @ManyToMany
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles = new HashSet<>();

    /** Authorities RBAC: {@code ROLE_<codigo>} por cada rol activo más los códigos de sus permisos. */
    public Set<String> authorities() {
        Set<String> authorities = new TreeSet<>();
        roles.stream().filter(Rol::isActivo).forEach(rol -> {
            authorities.add("ROLE_" + rol.getCodigo());
            rol.getPermisos().stream().map(Permiso::getCodigo).forEach(authorities::add);
        });
        return authorities;
    }
}
