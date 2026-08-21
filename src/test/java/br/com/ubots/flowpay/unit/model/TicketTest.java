package br.com.ubots.flowpay.unit.model;

import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    @Test
    @DisplayName("Deve criar ticket atribuído com status IN_PROGRESS e startedAt preenchido")
    void shouldCreateAssignedTicket() {
        UUID queueId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();

        Ticket ticket = Ticket.createAssigned("chat_123", "Assunto", queueId, agentId);

        assertNotNull(ticket);
        assertEquals("chat_123", ticket.getChatRef());
        assertEquals("Assunto", ticket.getSubject());
        assertEquals(queueId, ticket.getQueueId());
        assertEquals(agentId, ticket.getAgentId());
        assertEquals(StatusEnum.IN_PROGRESS, ticket.getStatus());
        assertTrue(ticket.isInProgress());
        assertFalse(ticket.isFinished());
        assertNotNull(ticket.getCreatedAt());
        assertNotNull(ticket.getStartedAt());
        assertEquals(0L, ticket.getWaitingTimeSeconds());
    }

    @Test
    @DisplayName("Deve criar ticket para fila com status PENDING e startedAt nulo")
    void shouldCreateTicketForQueue() {
        UUID queueId = UUID.randomUUID();

        Ticket ticket = Ticket.createForQueue("chat_456", "Assunto Fila", queueId, StatusEnum.PENDING);

        assertNotNull(ticket);
        assertEquals("chat_456", ticket.getChatRef());
        assertEquals(queueId, ticket.getQueueId());
        assertNull(ticket.getAgentId());
        assertEquals(StatusEnum.PENDING, ticket.getStatus());
        assertTrue(ticket.isPending());
        assertFalse(ticket.isFinished());
        assertNull(ticket.getStartedAt());
    }

    @Test
    @DisplayName("Deve criar ticket com status REJECTED, finishedAt e mensagem de erro")
    void shouldCreateRejectedTicket() {
        UUID queueId = UUID.randomUUID();
        String errorMsg = "A fila atingiu a capacidade máxima. Solicitação recusada.";

        Ticket ticket = Ticket.createRejected("chat_rejected", "Assunto", queueId, errorMsg);

        assertNotNull(ticket);
        assertEquals(StatusEnum.REJECTED, ticket.getStatus());
        assertEquals(errorMsg, ticket.getErrorMsg());
        assertTrue(ticket.isFinished());
        assertNotNull(ticket.getCreatedAt());
        assertNotNull(ticket.getFinishedAt());
    }

    @Test
    @DisplayName("Deve alterar status para RESOLVED, preencher finishedAt, flag finished e calcular tempos ao chamar finish()")
    void shouldFinishTicketAndCalculateTimes() {
        LocalDateTime t0 = LocalDateTime.now().minusSeconds(120);
        LocalDateTime t1 = LocalDateTime.now().minusSeconds(60);

        Ticket ticket = Ticket.builder()
                .status(StatusEnum.IN_PROGRESS)
                .createdAt(t0)
                .startedAt(t1)
                .waitingTimeSeconds(60L)
                .finished(false)
                .build();

        ticket.finish();

        assertEquals(StatusEnum.RESOLVED, ticket.getStatus());
        assertTrue(ticket.isFinished());
        assertTrue(ticket.getFinished());
        assertNotNull(ticket.getFinishedAt());
        assertTrue(ticket.getServiceTimeSeconds() >= 59L);
        assertTrue(ticket.getTotalTimeSeconds() >= 119L);
        assertEquals(60L, ticket.getWaitingTimeSeconds());
    }

    @Test
    @DisplayName("Deve atribuir atendente, registrar startedAt e calcular waitingTimeSeconds ao chamar assignTo()")
    void shouldAssignTicketToAgentAndCalculateWaitingTime() {
        UUID agentId = UUID.randomUUID();
        LocalDateTime t0 = LocalDateTime.now().minusSeconds(45);

        Ticket ticket = Ticket.builder()
                .status(StatusEnum.PENDING)
                .createdAt(t0)
                .build();

        ticket.assignTo(agentId);

        assertEquals(agentId, ticket.getAgentId());
        assertEquals(StatusEnum.IN_PROGRESS, ticket.getStatus());
        assertTrue(ticket.isInProgress());
        assertNotNull(ticket.getStartedAt());
        assertTrue(ticket.getWaitingTimeSeconds() >= 44L);
    }
}
