package com.core.backend.modules.parametro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.core.backend.AbstractIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class ParametroIT extends AbstractIntegrationTest {

    @Test
    void crudCompletoConCacheInvalidada() throws Exception {
        String admin = adminToken();

        String body = mvc.perform(post("/parametros")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "grupo",
                                "TIPO_DOCUMENTO",
                                "codigo",
                                "CI",
                                "nombre",
                                "Cédula de identidad",
                                "orden",
                                1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datos.codigo").value("CI"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = json.readTree(body).path("datos").path("id").asText();

        mvc.perform(get("/parametros/grupo/TIPO_DOCUMENTO").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(jsonPath("$.datos.length()").value(1));

        mvc.perform(put("/parametros/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(
                                Map.of("grupo", "TIPO_DOCUMENTO", "codigo", "CI", "nombre", "Carnet de identidad"))))
                .andExpect(status().isOk());

        mvc.perform(get("/parametros/grupo/tipo_documento").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(jsonPath("$.datos[0].nombre").value("Carnet de identidad"));

        mvc.perform(patch("/parametros/{id}/estado", id)
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("activo", false))))
                .andExpect(status().isOk());

        mvc.perform(get("/parametros/grupo/TIPO_DOCUMENTO").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(jsonPath("$.datos.length()").value(0));

        mvc.perform(delete("/parametros/{id}", id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());
        mvc.perform(get("/parametros/{id}", id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void validacionYDuplicados() throws Exception {
        String admin = adminToken();
        mvc.perform(post("/parametros")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("grupo", "minusculas", "codigo", "", "nombre", "x"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.grupo").exists())
                .andExpect(jsonPath("$.errores.codigo").exists());

        Map<String, Object> request = Map.of("grupo", "ESTADO", "codigo", "ACTIVO", "nombre", "Activo");
        mvc.perform(post("/parametros")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated());
        mvc.perform(post("/parametros")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isConflict());
    }
}
