package com.core.backend.modules.auth;

import com.core.backend.modules.rbac.RolRepository;
import com.core.backend.modules.usuario.Usuario;
import com.core.backend.modules.usuario.UsuarioRepository;
import com.core.backend.security.SecurityProperties;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/** Crea el administrador inicial si no existe. La contraseña nunca se versiona: llega por ADMIN_PASSWORD. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.security.bootstrap-admin", name = "enabled", havingValue = "true")
public class AdminBootstrap implements ApplicationRunner {

    private final SecurityProperties properties;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void run(ApplicationArguments args) {
        SecurityProperties.BootstrapAdmin admin = properties.bootstrapAdmin();
        if (admin.password() == null || admin.password().isBlank()) {
            log.warn("ADMIN_PASSWORD no definido: no se crea el administrador inicial");
            return;
        }
        transactionTemplate.executeWithoutResult(tx -> {
            if (usuarioRepository.existsByUsuarioIgnoreCase(admin.username())) {
                return;
            }
            Usuario usuario = new Usuario();
            usuario.setUsuario(admin.username());
            usuario.setEmail(admin.email());
            usuario.setNombres("Administrador");
            usuario.setApellidos("Sistema");
            usuario.setPassword(passwordEncoder.encode(admin.password()));
            rolRepository.findByCodigo("ADMINISTRADOR").ifPresent(rol -> usuario.setRoles(Set.of(rol)));
            usuarioRepository.save(usuario);
            log.info("Administrador inicial '{}' creado", admin.username());
        });
    }
}
