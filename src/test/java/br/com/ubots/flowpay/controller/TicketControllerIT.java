package br.com.ubots.flowpay.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.http.RequestEntity.post;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Garante que tudo que for salvo no banco durante o teste sofra ROLLBACK no final!
class TicketControllerIT {

    @Autowired
    private MockMvc mockMvc;

    // Os testes vão aqui!
    @Test
    @DisplayName("Deve criar ticket e atribuir para atendente livre (HTTP 201)")
    void shouldCreateTicketAndAssignToAgent() throws Exception {
        String jsonPayload = """
        {
          "chatRef": "whatsapp_555199999911",
          "subject": "Preciso de ajuda com limite do meu cartão de crédito"
        }
        """;

        mockMvc.perform(post("/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.chatRef").value("whatsapp_555199999911"));
    }

    @Test
    @DisplayName("Deve enviar para fila (HTTP 202) quando atendentes estiverem lotados")
    void shouldSendToQueueWhenAgentsAreFull() throws Exception {
        String jsonPayload = """
        {
          "chatRef": "whatsapp_555199999922",
          "subject": "Dúvida sobre fatura do cartão"
        }
        """;

        // Considerando que temos 3 atendentes e capacidade 3 cada (total 9).
        // Disparamos 9 vezes para lotar todo mundo.
        for (int i = 0; i < 9; i++) {
            mockMvc.perform(post("/v1/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonPayload))
                    .andExpect(status().isCreated());
        }

        // A 10ª requisição DEVE ir para a fila (PENDING / 202)
        mockMvc.perform(post("/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}