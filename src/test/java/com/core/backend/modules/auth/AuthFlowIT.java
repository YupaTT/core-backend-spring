package com.core.backend.modules.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.core.backend.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class AuthFlowIT extends AbstractIntegrationTest {

    @Test
    void loginDevuelveTokensYPerfilConPermisos() throws Exception {
        JsonNode datos = login("admin", "Admin123*");

        assertThat(datos.path("tokens").path("accessToken").asText()).isNotBlank();
        assertThat(datos.path("tokens").path("refreshToken").asText()).isNotBlank();
        assertThat(datos.path("perfil").path("permisos").toString()).contains("usuario:read", "rol:create");
    }

    @Test
    void credencialesInvalidasDevuelven401() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("usuario", "admin", "contrasena", "incorrecta"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinTokenOTokenInvalidoDevuelve401() throws Exception {
        mvc.perform(get("/auth/perfil")).andExpect(status().isUnauthorized());
        mvc.perform(get("/auth/perfil").header(HttpHeaders.AUTHORIZATION, "Bearer basura"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void perfilYVerifyConToken() throws Exception {
        String token = adminToken();
        mvc.perform(get("/auth/perfil").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.usuario").value("admin"));
        mvc.perform(get("/auth/verify").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(jsonPath("$.isAuthenticated").value(true));
        mvc.perform(get("/auth/verify")).andExpect(jsonPath("$.isAuthenticated").value(false));
    }

    @Test
    void loginCompatibleConFrontend() throws Exception {
        mvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("usuario", "admin", "contrasena", "Admin123*"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.datos.access_token").isNotEmpty())
                .andExpect(jsonPath("$.datos.usuario").value("admin"));
    }

    @Test
    void refreshRotaElTokenYDetectaReutilizacion() throws Exception {
        String refresh =
                login("admin", "Admin123*").path("tokens").path("refreshToken").asText();

        String body = mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refresh))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String nuevo = json.readTree(body).path("datos").path("refreshToken").asText();
        assertThat(nuevo).isNotEqualTo(refresh);

        // Reutilizar el token consumido revoca toda la familia, incluido el nuevo
        mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refresh))))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", nuevo))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevocaElAccessToken() throws Exception {
        String token = adminToken();
        mvc.perform(post("/auth/logout").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/auth/perfil").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isUnauthorized());
    }
}
