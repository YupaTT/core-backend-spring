package com.core.backend.modules.rbac;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.core.backend.AbstractIntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class RbacIT extends AbstractIntegrationTest {

    @Test
    void usuarioConRolDeConsultaNoPuedeEscribir() throws Exception {
        String admin = adminToken();
        String rolUsuarioId = rolId(admin, "USUARIO");

        mvc.perform(post("/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "usuario", "consulta",
                                "email", "consulta@test.local",
                                "nombres", "Ana",
                                "apellidos", "Pérez",
                                "password", "Consulta123",
                                "rolIds", List.of(rolUsuarioId)))))
                .andExpect(status().isCreated());

        String token = bearer(login("consulta", "Consulta123")
                .path("tokens")
                .path("accessToken")
                .asText());

        mvc.perform(get("/sucursales").header(HttpHeaders.AUTHORIZATION, token)).andExpect(status().isOk());
        mvc.perform(get("/usuarios").header(HttpHeaders.AUTHORIZATION, token)).andExpect(status().isForbidden());
        mvc.perform(post("/sucursales")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Central", "sigla", "CEN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearRolConPermisosYProtegerRolesDeSistema() throws Exception {
        String admin = adminToken();
        String body = mvc.perform(get("/permisos").param("modulo", "sucursal").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String permisoId = json.readTree(body).path("datos").get(0).path("id").asText();

        String creado = mvc.perform(post("/roles")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(
                                Map.of("codigo", "OPERADOR", "nombre", "Operador", "permisoIds", List.of(permisoId)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datos.permisos.length()").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String rolId = json.readTree(creado).path("datos").path("id").asText();

        mvc.perform(put("/roles/{id}/permisos", rolId)
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("permisoIds", List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.permisos.length()").value(0));

        mvc.perform(delete("/roles/{id}", rolId(admin, "ADMINISTRADOR")).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isConflict());
        mvc.perform(delete("/roles/{id}", rolId).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());
    }

    private String rolId(String token, String codigo) throws Exception {
        String body = mvc.perform(get("/roles").param("q", codigo).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        for (var rol : json.readTree(body).path("datos").path("contenido")) {
            if (rol.path("codigo").asText().equals(codigo)) {
                return rol.path("id").asText();
            }
        }
        throw new IllegalStateException("Rol no encontrado: " + codigo);
    }
}
