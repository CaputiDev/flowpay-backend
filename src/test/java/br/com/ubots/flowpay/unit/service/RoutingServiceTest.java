package br.com.ubots.flowpay.unit.service;

import br.com.ubots.flowpay.classifier.CreditCardClassifier;
import br.com.ubots.flowpay.classifier.LoanClassifier;
import br.com.ubots.flowpay.dto.TicketResponse;
import br.com.ubots.flowpay.exception.InvalidTicketStatusException;
import br.com.ubots.flowpay.exception.TicketNotFoundException;
import br.com.ubots.flowpay.model.Agent;
import br.com.ubots.flowpay.model.Queue;
import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import br.com.ubots.flowpay.repository.AgentRepository;
import br.com.ubots.flowpay.repository.QueueRepository;
import br.com.ubots.flowpay.repository.TicketRepository;
import br.com.ubots.flowpay.service.RoutingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutingServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private QueueRepository queueRepository;

    private RoutingService routingService;

    @BeforeEach
    void setUp() {
        routingService = new RoutingService(
                ticketRepository,
                agentRepository,
                queueRepository,
                List.of(new CreditCardClassifier(), new LoanClassifier())
        );
    }

    @Test
    @DisplayName("Deve rotear para time de Cartões de Crédito quando assunto contiver a palavra cartão")
    void shouldRouteToCreditCardsWhenSubjectContainsCartao() {
        assertEquals(TeamEnum.CREDIT_CARDS, routingService.determineTeam("Preciso de ajuda com meu CARTÃO de crédito"));
        assertEquals(TeamEnum.CREDIT_CARDS, routingService.determineTeam("cartao"));
    }

    @Test
    @DisplayName("Deve rotear para time de Empréstimos quando assunto contiver a palavra empréstimo")
    void shouldRouteToLoansWhenSubjectContainsEmprestimo() {
        assertEquals(TeamEnum.LOANS, routingService.determineTeam("Quero um empréstimo agora"));
        assertEquals(TeamEnum.LOANS, routingService.determineTeam("EMPRÉSTIMO"));
    }

    @Test
    @DisplayName("Deve rotear para time Outros quando assunto for genérico ou vazio")
    void shouldRouteToOthersWhenSubjectIsGenericOrEmpty() {
        assertEquals(TeamEnum.OTHERS, routingService.determineTeam("Falar com humano"));
        assertEquals(TeamEnum.OTHERS, routingService.determineTeam("   "));
        assertEquals(TeamEnum.OTHERS, routingService.determineTeam(null));
    }

    @Test
    @DisplayName("Deve finalizar solicitação e decrementar carga do atendente quando a fila estiver vazia")
    void shouldFinishTicketWhenQueueIsEmpty() {
        UUID ticketId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        UUID queueId = UUID.randomUUID();

        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .chatRef("chat_123")
                .subject("Cartão de crédito")
                .status(StatusEnum.IN_PROGRESS)
                .agentId(agentId)
                .queueId(queueId)
                .build();

        Agent agent = Agent.builder()
                .id(agentId)
                .name("Maria")
                .team(TeamEnum.CREDIT_CARDS)
                .currentLoad(1)
                .maxCapacity(3)
                .build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));
        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));
        when(ticketRepository.findOldestTicketInQueue(queueId, "PENDING")).thenReturn(Optional.empty());

        TicketResponse response = routingService.finishTicket(ticketId);

        assertNotNull(response);
        assertEquals(StatusEnum.RESOLVED, response.getStatus());
        assertNotNull(response.getFinishedAt());

        ArgumentCaptor<Agent> agentCaptor = ArgumentCaptor.forClass(Agent.class);
        verify(agentRepository).save(agentCaptor.capture());
        assertEquals(0, agentCaptor.getValue().getCurrentLoad());
    }

    @Test
    @DisplayName("Deve finalizar solicitação, decrementar carga e puxar a solicitação mais antiga da fila FIFO")
    void shouldFinishTicketAndPullOldestFromQueue() {
        UUID ticketId = UUID.randomUUID();
        UUID pendingTicketId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        UUID queueId = UUID.randomUUID();

        Ticket activeTicket = Ticket.builder()
                .id(ticketId)
                .chatRef("chat_active")
                .subject("Fatura cartão")
                .status(StatusEnum.IN_PROGRESS)
                .agentId(agentId)
                .queueId(queueId)
                .build();

        Agent agent = Agent.builder()
                .id(agentId)
                .name("João")
                .team(TeamEnum.CREDIT_CARDS)
                .currentLoad(3)
                .maxCapacity(3)
                .build();

        Ticket pendingTicket = Ticket.builder()
                .id(pendingTicketId)
                .chatRef("chat_pending")
                .subject("Segunda via fatura")
                .status(StatusEnum.PENDING)
                .queueId(queueId)
                .build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(activeTicket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));
        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));
        when(ticketRepository.findOldestTicketInQueue(queueId, "PENDING")).thenReturn(Optional.of(pendingTicket));

        TicketResponse response = routingService.finishTicket(ticketId);

        assertEquals(StatusEnum.RESOLVED, response.getStatus());

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository, times(2)).save(ticketCaptor.capture());

        List<Ticket> savedTickets = ticketCaptor.getAllValues();
        Ticket savedPending = savedTickets.stream()
                .filter(t -> t.getId().equals(pendingTicketId))
                .findFirst()
                .orElseThrow();

        assertEquals(StatusEnum.IN_PROGRESS, savedPending.getStatus());
        assertEquals(agentId, savedPending.getAgentId());
        assertEquals("chat_pending", savedPending.getChatRef());
    }

    @Test
    @DisplayName("Deve lançar TicketNotFoundException quando o ID da solicitação não existir")
    void shouldThrowExceptionWhenTicketNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(ticketRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () -> routingService.finishTicket(nonExistentId));
    }

    @Test
    @DisplayName("Deve lançar InvalidTicketStatusException ao tentar finalizar solicitação que não está em andamento")
    void shouldThrowExceptionWhenTicketNotInProgress() {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .status(StatusEnum.RESOLVED)
                .build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStatusException.class, () -> routingService.finishTicket(ticketId));
    }

    @Test
    @DisplayName("Deve salvar ticket com status REJECTED e lançar QueueFullException quando a fila atingir a capacidade máxima")
    void shouldSaveRejectedTicketWhenQueueIsFull() {
        UUID queueId = UUID.randomUUID();
        Queue queue = Queue.builder().id(queueId).team(TeamEnum.CREDIT_CARDS).maxCapacity(3).build();

        when(ticketRepository.existsByChatRefAndStatusIn(anyString(), any())).thenReturn(false);
        when(queueRepository.findByTeam(TeamEnum.CREDIT_CARDS)).thenReturn(Optional.of(queue));
        when(agentRepository.findAvailableAgentByTeam(TeamEnum.CREDIT_CARDS)).thenReturn(Optional.empty());
        when(ticketRepository.countByQueueIdAndStatus(queueId, StatusEnum.PENDING)).thenReturn(3L);

        assertThrows(br.com.ubots.flowpay.exception.QueueFullException.class, () ->
                routingService.routeNewTicket("chat_overflow", "cartão de crédito")
        );

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticketCaptor.capture());

        Ticket rejectedSaved = ticketCaptor.getValue();
        assertEquals(StatusEnum.REJECTED, rejectedSaved.getStatus());
        assertTrue(rejectedSaved.isFinished());
        assertNotNull(rejectedSaved.getFinishedAt());
        assertEquals("A fila atingiu a capacidade máxima. Solicitação recusada.", rejectedSaved.getErrorMsg());
    }
}

