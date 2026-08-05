package br.com.ubots.flowpay.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Garante que tudo que for salvo no banco durante o teste sofra ROLLBACK no final
class TicketControllerIT {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    @DisplayName("Deve rotear para fila DEFAULT quando assunto não bater com nenhum Regex")
    void shouldRouteToDefaultQueueWhenNoRegexMatches() throws Exception {
        String jsonPayload = """
    {
      "chatRef": "whatsapp_555199999933",
      "subject": "Oi, bom dia"
    }
    """;
        // Assert: HTTP 201 e a queueId ou status que comprove que foi pro lugar certo!
    }

    @Test
    @DisplayName("Deve rotear para LOANS ignorando letras maiúsculas no assunto")
    void shouldRouteToLoansIgnoringCase() throws Exception {
        String jsonPayload = """
    {
      "chatRef": "telegram_123456",
      "subject": "SIMULAR EMPRÉSTIMO"
    }
    """;
        // Assert: HTTP 201 e verificar se o atendente/fila escolhida foi da equipe LOANS
    }

    @Test
    @DisplayName("Deve retornar HTTP 400 (Bad Request) quando o JSON estiver malformado")
    void shouldReturn400WhenJsonIsMalformed() throws Exception {
        // Faltam aspas e chaves nesse JSON de propósito!
        String jsonPayload = """
    {
      "chatRef": "whatsapp_999",
      "subject": Preciso de ajuda
    """;

        mockMvc.perform(post("/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Não deve criar ticket duplicado para o mesmo chatRef com status ativo")
    void shouldPreventDuplicateTickets() throws Exception {
        String jsonPayload = """
    {
      "chatRef": "whatsapp_111222333",
      "subject": "Problema com a conta"
    }
    """;

        // Primeira requisição: Deve criar (201)
        mockMvc.perform(post("/v1/tickets").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
                .andExpect(status().isCreated());

        // Segunda requisição (logo em seguida): Deve retornar conflito (409) ou regra de negócio customizada
        mockMvc.perform(post("/v1/tickets").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
                .andExpect(status().isConflict());
        // Se retornar 201 de novo, significa que temos uma falha de negócio permitindo spam!
    }

    @Test
    @DisplayName("Deve retornar HTTP 400 quando o assunto exceder o limite de caracteres")
    void shouldReturn400WhenSubjectIsTooLong() throws Exception {
        // String gigante simulando um textão
        String textao = "a".repeat(300);
        String jsonPayload = """
    {
      "chatRef": "whatsapp_999",
      "subject": "%s"
    }
    """.formatted(textao);

        mockMvc.perform(post("/v1/tickets").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar HTTP 400 quando chatRef ou subject forem apenas espaços em branco")
    void shouldReturn400WhenFieldsAreOnlyWhitespaces() throws Exception {
        String jsonPayload = """
    {
      "chatRef": "   ",
      "subject": "    "
    }
    """;

        mockMvc.perform(post("/v1/tickets").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
                .andExpect(status().isBadRequest());
    }
}