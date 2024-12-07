package com.core.backend.modules.auth;

import com.core.backend.common.exception.ApiException;
import com.core.backend.modules.auth.dto.AuthDtos.LoginRequest;
import com.core.backend.modules.auth.dto.AuthDtos.LoginResponse;
import com.core.backend.modules.auth.dto.AuthDtos.PerfilResponse;
import com.core.backend.modules.auth.dto.AuthDtos.TokenResponse;
import com.core.backend.modules.auth.token.RefreshTokenService;
import com.core.backend.modules.usuario.Usuario;
import com.core.backend.modules.usuario.UsuarioMapper;
import com.core.backend.modules.usuario.UsuarioRepository;
import com.core.backend.security.AuthenticatedUser;
import com.core.backend.security.JwtService;
import com.core.backend.security.LoginAttemptService;
import com.core.backend.security.SecurityProperties;
import com.core.backend.security.TokenRevocationStore;
import io.micrometer.observation.annotation.Observed;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Observed(name = "auth")
public class AuthService {

    private static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos";

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenRevocationStore revocationStore;
    private final LoginAttemptService loginAttemptService;
    private final SecurityProperties properties;

    @Transactional
    public LoginResponse login(LoginRequest request, String clientIp) {
        String attemptKey = request.usuario().toLowerCase(Locale.ROOT) + "|" + clientIp;
        if (loginAttemptService.isBlocked(attemptKey)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos, intente más tarde");
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.usuario(), decodePassword(request.contrasena())));
        } catch (AuthenticationException ex) {
            loginAttemptService.loginFailed(attemptKey);
            throw ApiException.unauthorized(CREDENCIALES_INVALIDAS);
        }
        loginAttemptService.loginSucceeded(attemptKey);
        Usuario usuario = usuarioRepository
                .findWithAccesosByUsuarioIgnoreCase(request.usuario())
                .orElseThrow(() -> ApiException.unauthorized(CREDENCIALES_INVALIDAS));
        return new LoginResponse(issueTokens(usuario), usuarioMapper.toPerfil(usuario));
    }

    @Transactional(noRollbackFor = ApiException.class)
    public TokenResponse refresh(String refreshToken) {
        UUID usuarioId = refreshTokenService.consume(refreshToken);
        Usuario usuario = usuarioRepository
                .findWithAccesosById(usuarioId)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> ApiException.unauthorized("Usuario inactivo"));
        return issueTokens(usuario);
    }

    @Transactional
    public void logout(AuthenticatedUser current, String refreshToken) {
        revocationStore.revoke(current.tokenId(), current.tokenExpiresAt());
        if (refreshToken == null || refreshToken.isBlank()) {
            refreshTokenService.revokeAll(current.id());
        } else {
            refreshTokenService.revoke(refreshToken, current.id());
        }
    }

    @Transactional(readOnly = true)
    public PerfilResponse perfil(UUID usuarioId) {
        return usuarioRepository
                .findWithAccesosById(usuarioId)
                .map(usuarioMapper::toPerfil)
                .orElseThrow(() -> ApiException.notFound("Usuario", usuarioId));
    }

    private TokenResponse issueTokens(Usuario usuario) {
        JwtService.IssuedToken access =
                jwtService.issueAccessToken(usuario.getId(), usuario.getUsuario(), usuario.authorities());
        String refresh = refreshTokenService.create(usuario);
        long expiresIn = Duration.between(Instant.now(), access.expiresAt()).toSeconds();
        return new TokenResponse(
                access.value(), refresh, "Bearer", expiresIn, access.expiresAt().getEpochSecond());
    }

    private String decodePassword(String contrasena) {
        if (!properties.loginPasswordBase64()) {
            return contrasena;
        }
        try {
            return new String(Base64.getDecoder().decode(contrasena), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("La contraseña debe enviarse en base64");
        }
    }
}
