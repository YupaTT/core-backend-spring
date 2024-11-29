package com.core.backend.security;

import com.core.backend.modules.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Solo se usa al hacer login; las peticiones autenticadas se resuelven con los claims del JWT. */
@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        return usuarioRepository
                .findWithAccesosByUsuarioIgnoreCase(username)
                .map(u -> User.withUsername(u.getUsuario())
                        .password(u.getPassword())
                        .disabled(!u.isActivo())
                        .authorities(AuthorityUtils.createAuthorityList(u.authorities()))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
