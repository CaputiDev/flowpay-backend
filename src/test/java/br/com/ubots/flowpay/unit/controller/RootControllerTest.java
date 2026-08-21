package br.com.ubots.flowpay.unit.controller;

import br.com.ubots.flowpay.controller.RootController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RootController.class)
class RootControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("RootController: Deve retornar 200 OK com healthcheck status 'UP' e rota do swagger")
    void shouldReturn200AndHealthcheckWithSwaggerUrl() throws Exception {
        mockMvc.perform(get("/")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").value("FlowPay MVP API operacional"))
                .andExpect(jsonPath("$.docs").value("/swagger-ui/index.html"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
