package com.core.backend.modules.auth;

import com.core.backend.common.api.ApiResponse;
import com.core.backend.modules.auth.dto.AuthDtos.FrontDatos;
import com.core.backend.modules.auth.dto.AuthDtos.FrontLoginResponse;
import com.core.backend.modules.auth.dto.AuthDtos.LoginRequest;
import com.core.backend.modules.auth.dto.AuthDtos.LoginResponse;
import com.core.backend.modules.auth.dto.AuthDtos.LogoutRequest;
import com.core.backend.modules.auth.dto.AuthDtos.PerfilResponse;
import com.core.backend.modules.auth.dto.AuthDtos.RefreshRequest;
import com.core.backend.modules.auth.dto.AuthDtos.TokenResponse;
import com.core.backend.modules.auth.dto.AuthDtos.VerifyResponse;
import com.core.backend.security.AuthenticatedUser;
import com.core.backend.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticación")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login estándar")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return ApiResponse.ok("Login correcto", authService.login(request, http.getRemoteAddr()));
    }

    @Operation(summary = "Login compatible con el frontend (docs/requerimientofront.md)")
    @PostMapping
    public FrontLoginResponse loginFront(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        LoginResponse login = authService.login(request, http.getRemoteAddr());
        TokenResponse tokens = login.tokens();
        return new FrontLoginResponse(
                "success",
                "Login correcto",
                new FrontDatos(login.perfil(), tokens.accessToken(), tokens.refreshToken(), tokens.exp()));
    }

    @Operation(summary = "Renueva el access token rotando el refresh token")
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok("Token renovado", authService.refresh(request.refreshToken()));
    }

    @Operation(summary = "Revoca el access token actual y el refresh token (o todos si no se envía)")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody(required = false) LogoutRequest request) {
        authService.logout(CurrentUser.get(), request == null ? null : request.refreshToken());
    }

    @GetMapping("/perfil")
    public ApiResponse<PerfilResponse> perfil() {
        return ApiResponse.ok(authService.perfil(CurrentUser.get().id()));
    }

    @GetMapping("/verify")
    public VerifyResponse verify(@AuthenticationPrincipal AuthenticatedUser user) {
        return new VerifyResponse(user != null);
    }
}
