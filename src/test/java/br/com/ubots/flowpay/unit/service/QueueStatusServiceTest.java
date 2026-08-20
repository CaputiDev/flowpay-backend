package br.com.ubots.flowpay.unit.service;

import br.com.ubots.flowpay.dto.QueueStatusResponse;
import br.com.ubots.flowpay.model.Agent;
import br.com.ubots.flowpay.model.Queue;
import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import br.com.ubots.flowpay.repository.AgentRepository;
import br.com.ubots.flowpay.repository.QueueRepository;
import br.com.ubots.flowpay.repository.TicketRepository;
import br.com.ubots.flowpay.service.QueueStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueueStatusServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private QueueRepository queueRepository;

    @InjectMocks
    private QueueStatusService queueStatusService;

    @Test
    @DisplayName("Service: Deve consolidar o estado das filas separando fila ativa e fila em espera")
    void shouldConsolidateQueueStatusSuccessfully() {
        // Cenário: 1 Fila (Cartões), 1 Atendente, 1 Ticket Ativo, 2 Tickets em Espera
        UUID queueId = UUID.randomUUID();
        Queue queue = Queue.builder()
                .id(queueId)
                .team(TeamEnum.CREDIT_CARDS)
                .maxCapacity(3)
                .build();

        UUID agentId = UUID.randomUUID();
        Agent agent = Agent.builder()
                .id(agentId)
                .name("Ana (Cartões)")
                .team(TeamEnum.CREDIT_CARDS)
                .currentLoad(1)
                .maxCapacity(3)
                .build();

        Ticket activeTicket = Ticket.builder()
                .id(UUID.randomUUID())
                .ticketNumber(10L)
                .chatRef("chat_01")
                .subject("Problema no cartão")
                .status(StatusEnum.IN_PROGRESS)
                .queueId(queueId)
                .agentId(agentId)
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();

        Ticket waitingTicket1 = Ticket.builder()
                .id(UUID.randomUUID())
                .ticketNumber(11L)
                .chatRef("chat_02")
                .subject("Aumento de limite")
                .status(StatusEnum.PENDING)
                .queueId(queueId)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();

        Ticket waitingTicket2 = Ticket.builder()
                .id(UUID.randomUUID())
                .ticketNumber(12L)
                .chatRef("chat_03")
                .subject("Cancelamento de cartão")
                .status(StatusEnum.PENDING)
                .queueId(queueId)
                .createdAt(LocalDateTime.now().minusMinutes(2))
                .build();

        when(queueRepository.findAll()).thenReturn(List.of(queue));
        when(agentRepository.findAll()).thenReturn(List.of(agent));
        when(ticketRepository.findAllActiveTickets()).thenReturn(List.of(activeTicket));
        when(ticketRepository.findAllWaitingTickets()).thenReturn(List.of(waitingTicket1, waitingTicket2));

        // Execução
        QueueStatusResponse response = queueStatusService.getQueueStatus();

        // Verificações
        assertNotNull(response);
        assertEquals(1, response.getActiveQueue().size());
        assertEquals(2, response.getWaitingQueue().size());
        assertEquals(1, response.getTeamSummaries().size());

        // Validação da Fila Ativa
        QueueStatusResponse.ActiveTicketDto activeDto = response.getActiveQueue().get(0);
        assertEquals(activeTicket.getId(), activeDto.getId());
        assertEquals("Ana (Cartões)", activeDto.getAgentName());
        assertEquals(TeamEnum.CREDIT_CARDS, activeDto.getTeam());
        assertEquals(StatusEnum.IN_PROGRESS, activeDto.getStatus());

        // Validação da Fila em Espera (FIFO e posições)
        QueueStatusResponse.WaitingTicketDto waitingDto1 = response.getWaitingQueue().get(0);
        assertEquals(waitingTicket1.getId(), waitingDto1.getId());
        assertEquals(1, waitingDto1.getPosition());
        assertEquals(TeamEnum.CREDIT_CARDS, waitingDto1.getTeam());

        QueueStatusResponse.WaitingTicketDto waitingDto2 = response.getWaitingQueue().get(1);
        assertEquals(waitingTicket2.getId(), waitingDto2.getId());
        assertEquals(2, waitingDto2.getPosition());

        // Validação do Resumo do Time
        QueueStatusResponse.TeamSummaryDto summary = response.getTeamSummaries().get(0);
        assertEquals(TeamEnum.CREDIT_CARDS, summary.getTeam());
        assertEquals(1, summary.getTotalAgents());
        assertEquals(3, summary.getTotalCapacity());
        assertEquals(1, summary.getCurrentLoad());
        assertEquals(2, summary.getWaitingCount());
        assertEquals(1, summary.getAgents().size());
        assertEquals(2, summary.getAgents().get(0).getAvailableCapacity());
    }
}
