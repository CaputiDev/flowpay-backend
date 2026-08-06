package br.com.ubots.flowpay.unit.controller;

import br.com.ubots.flowpay.controller.TicketController;
import br.com.ubots.flowpay.dto.TicketResponse;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import br.com.ubots.flowpay.service.RoutingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoutingService routingService;

    @Test
    @DisplayName("Controller: Deve retornar 201 Created quando o ticket for criado e atribuído")
    void shouldReturn201WhenTicketIsCreated() throws Exception {
        TicketResponse response = new TicketResponse(
                UUID.randomUUID(), 1L, "chat_123", "Assunto",
                StatusEnum.IN_PROGRESS, null, LocalDateTime.now(), null,
                UUID.randomUUID(), UUID.randomUUID()
        );

        when(routingService.routeNewTicket(anyString(), anyString())).thenReturn(response);

        String jsonPayload = """
                {
                  "chatRef": "chat_123",
                  "subject": "Dúvida cartão"
                }
                """;

        mockMvc.perform(post("/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.chatRef").value("chat_123"));
    }

    @Test
    @DisplayName("Controller: Deve retornar 202 Accepted quando o ticket for enviado para a fila")
    void shouldReturn202WhenTicketIsEnqueued() throws Exception {
        TicketResponse response = new TicketResponse(
                UUID.randomUUID(), 2L, "chat_456", "Assunto Fila",
                StatusEnum.PENDING, null, LocalDateTime.now(), null,
                UUID.randomUUID(), null
        );

        when(routingService.routeNewTicket(anyString(), anyString())).thenReturn(response);

        String jsonPayload = """
                {
                  "chatRef": "chat_456",
                  "subject": "Assunto Fila"
                }
                """;

        mockMvc.perform(post("/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Controller: Deve retornar 200 OK quando finalizar ticket")
    void shouldReturn200WhenTicketIsFinished() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketResponse response = new TicketResponse(
                ticketId, 1L, "chat_123", "Assunto",
                StatusEnum.RESOLVED, null, LocalDateTime.now(), LocalDateTime.now(),
                UUID.randomUUID(), UUID.randomUUID()
        );

        when(routingService.finishTicket(eq(ticketId))).thenReturn(response);

        mockMvc.perform(patch("/v1/tickets/" + ticketId + "/finish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.finishedAt").exists());
    }
}
