package br.com.ubots.flowpay.integration.repository;

import br.com.ubots.flowpay.model.Queue;
import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import br.com.ubots.flowpay.repository.QueueRepository;
import br.com.ubots.flowpay.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TicketRepositoryIT {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private QueueRepository queueRepository;

    private Queue creditCardQueue;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        creditCardQueue = queueRepository.findByTeam(TeamEnum.CREDIT_CARDS).orElseThrow();
    }

    @Test
    @DisplayName("Integration: Deve verificar existência de ticket ativo por chatRef")
    void shouldCheckActiveTicketByChatRef() {
        Ticket activeTicket = Ticket.createForQueue("chat_active_repo", "Assunto", creditCardQueue.getId(), StatusEnum.IN_PROGRESS);
        ticketRepository.save(activeTicket);

        boolean exists = ticketRepository.existsByChatRefAndStatusIn("chat_active_repo", List.of(StatusEnum.IN_PROGRESS, StatusEnum.PENDING));
        assertTrue(exists);

        boolean notExists = ticketRepository.existsByChatRefAndStatusIn("chat_non_existent", List.of(StatusEnum.IN_PROGRESS, StatusEnum.PENDING));
        assertFalse(notExists);
    }

    @Test
    @DisplayName("Integration: Deve buscar o ticket pendente mais antigo respeitando a ordem FIFO")
    void shouldFindOldestTicketInQueueFIFO() throws InterruptedException {
        Ticket ticket1 = Ticket.createForQueue("chat_first", "Primeiro", creditCardQueue.getId(), StatusEnum.PENDING);
        ticketRepository.save(ticket1);

        Thread.sleep(50); // Garantir diferença de timestamp de criação

        Ticket ticket2 = Ticket.createForQueue("chat_second", "Segundo", creditCardQueue.getId(), StatusEnum.PENDING);
        ticketRepository.save(ticket2);

        Optional<Ticket> oldestOpt = ticketRepository.findOldestTicketInQueue(creditCardQueue.getId(), StatusEnum.PENDING.name());
        assertTrue(oldestOpt.isPresent());
        assertEquals("chat_first", oldestOpt.get().getChatRef());
    }

    @Test
    @DisplayName("Integration: Deve contar solicitações pendentes por fila e status")
    void shouldCountPendingTicketsByQueue() {
        Ticket ticket1 = Ticket.createForQueue("chat_q1", "Dúvida 1", creditCardQueue.getId(), StatusEnum.PENDING);
        Ticket ticket2 = Ticket.createForQueue("chat_q2", "Dúvida 2", creditCardQueue.getId(), StatusEnum.PENDING);
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);

        long count = ticketRepository.countByQueueIdAndStatus(creditCardQueue.getId(), StatusEnum.PENDING);
        assertEquals(2, count);
    }
}
