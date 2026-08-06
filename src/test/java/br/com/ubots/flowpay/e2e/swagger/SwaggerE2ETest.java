package br.com.ubots.flowpay.e2e.swagger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SwaggerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("E2E: Deve expor o endpoint OpenAPI v3 JSON com metadados da aplicação (HTTP 200)")
    void shouldExposeOpenApiJsonEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("FlowPay MVP - Sistema de Roteamento de Atendimento"))
                .andExpect(jsonPath("$.info.version").value("1.0.0"))
                .andExpect(jsonPath("$.paths['/v1/tickets']").exists())
                .andExpect(jsonPath("$.paths['/v1/tickets/{id}/finish']").exists());
    }

    @Test
    @DisplayName("E2E: Deve redirecionar ou expor a interface gráfica do Swagger UI (HTTP 200 ou 302)")
    void shouldExposeSwaggerUiEndpoint() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }
}
