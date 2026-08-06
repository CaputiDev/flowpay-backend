package br.com.ubots.flowpay.unit.model;

import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    @Test
    @DisplayName("Deve criar ticket atribuído com status IN_PROGRESS")
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
        assertNotNull(ticket.getCreatedAt());
    }

    @Test
    @DisplayName("Deve criar ticket para fila com status PENDING")
    void shouldCreateTicketForQueue() {
        UUID queueId = UUID.randomUUID();

        Ticket ticket = Ticket.createForQueue("chat_456", "Assunto Fila", queueId, StatusEnum.PENDING);

        assertNotNull(ticket);
        assertEquals("chat_456", ticket.getChatRef());
        assertEquals(queueId, ticket.getQueueId());
        assertNull(ticket.getAgentId());
        assertEquals(StatusEnum.PENDING, ticket.getStatus());
        assertTrue(ticket.isPending());
    }

    @Test
    @DisplayName("Deve alterar status para RESOLVED e preencher finishedAt ao chamar finish()")
    void shouldFinishTicket() {
        Ticket ticket = Ticket.builder()
                .status(StatusEnum.IN_PROGRESS)
                .build();

        ticket.finish();

        assertEquals(StatusEnum.RESOLVED, ticket.getStatus());
        assertTrue(ticket.isFinished());
        assertNotNull(ticket.getFinishedAt());
    }

    @Test
    @DisplayName("Deve atribuir atendente e alterar status para IN_PROGRESS ao chamar assignTo()")
    void shouldAssignTicketToAgent() {
        UUID agentId = UUID.randomUUID();
        Ticket ticket = Ticket.builder()
                .status(StatusEnum.PENDING)
                .build();

        ticket.assignTo(agentId);

        assertEquals(agentId, ticket.getAgentId());
        assertEquals(StatusEnum.IN_PROGRESS, ticket.getStatus());
        assertTrue(ticket.isInProgress());
    }
}
