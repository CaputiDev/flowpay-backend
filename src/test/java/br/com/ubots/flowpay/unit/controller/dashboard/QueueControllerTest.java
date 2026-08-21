package br.com.ubots.flowpay.unit.controller.dashboard;

import br.com.ubots.flowpay.controller.dashboard.QueueController;
import br.com.ubots.flowpay.dto.QueueStatusResponse;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import br.com.ubots.flowpay.service.QueueStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QueueController.class)
class QueueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QueueStatusService queueStatusService;

    @Test
    @DisplayName("Controller: Deve retornar 200 OK e o snapshot das filas")
    void shouldReturn200AndQueueStatusSnapshot() throws Exception {
        UUID ticketId = UUID.randomUUID();
        QueueStatusResponse response = QueueStatusResponse.builder()
                .activeQueue(List.of(
                        QueueStatusResponse.ActiveTicketDto.builder()
                                .id(ticketId)
                                .ticketNumber(1L)
                                .chatRef("chat_01")
                                .subject("Problema Cartão")
                                .status(StatusEnum.IN_PROGRESS)
                                .team(TeamEnum.CREDIT_CARDS)
                                .agentName("Ana (Cartões)")
                                .createdAt(LocalDateTime.now())
                                .build()
                ))
                .waitingQueue(List.of(
                        QueueStatusResponse.WaitingTicketDto.builder()
                                .id(UUID.randomUUID())
                                .ticketNumber(2L)
                                .chatRef("chat_02")
                                .subject("Dúvida Empréstimo")
                                .status(StatusEnum.PENDING)
                                .team(TeamEnum.LOANS)
                                .position(1)
                                .createdAt(LocalDateTime.now())
                                .build()
                ))
                .teamSummaries(List.of(
                        QueueStatusResponse.TeamSummaryDto.builder()
                                .team(TeamEnum.CREDIT_CARDS)
                                .totalAgents(3)
                                .totalCapacity(9)
                                .currentLoad(1)
                                .waitingCount(0)
                                .build()
                ))
                .build();

        when(queueStatusService.getQueueStatus()).thenReturn(response);

        mockMvc.perform(get("/v1/queues/status")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeQueue").isArray())
                .andExpect(jsonPath("$.activeQueue[0].id").value(ticketId.toString()))
                .andExpect(jsonPath("$.activeQueue[0].agentName").value("Ana (Cartões)"))
                .andExpect(jsonPath("$.waitingQueue").isArray())
                .andExpect(jsonPath("$.waitingQueue[0].position").value(1))
                .andExpect(jsonPath("$.teamSummaries").isArray())
                .andExpect(jsonPath("$.teamSummaries[0].team").value("CREDIT_CARDS"));
    }
}
