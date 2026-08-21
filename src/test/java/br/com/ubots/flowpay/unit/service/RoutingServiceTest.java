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
    @DisplayName("Deve rotear para Cartões quando assunto contiver termos de cartões, faturas, segurança ou benefícios")
    void shouldRouteToCreditCardsWhenSubjectContainsCardKeywords() {
        assertEquals(TeamEnum.CREDIT_CARDS, routingService.determineTeam("Preciso de ajuda com meu CARTÃO de crédito"));
        assertEquals(TeamEnum.CREDIT_CARDS, routingService.determineTeam("segunda via da fatura"));
        assertEquals(TeamEnum.CREDIT_CARDS, routingService.determineTeam("solicitar 2ª via"));
        assertEquals(TeamEnum.CREDIT_CARDS, routingService.determineTeam("bloqueio por clonagem"));
        assertEquals(TeamEnum.CREDIT_CARDS, routingService.determineTeam("pontos e cashback do programa de fidelidade"));
        assertEquals(TeamEnum.CREDIT_CARDS, routingService.determineTeam("qual o meu CVC e código de barras"));
    }

    @Test
    @DisplayName("Deve rotear para Empréstimos quando assunto contiver termos de crédito, financiamento, taxas ou quitação")
    void shouldRouteToLoansWhenSubjectContainsLoanKeywords() {
        assertEquals(TeamEnum.LOANS, routingService.determineTeam("Quero um empréstimo consignado"));
        assertEquals(TeamEnum.LOANS, routingService.determineTeam("EMPRÉSTIMO"));
        assertEquals(TeamEnum.LOANS, routingService.determineTeam("simular financiamento"));
        assertEquals(TeamEnum.LOANS, routingService.determineTeam("quitar parcelamento em atraso"));
        assertEquals(TeamEnum.LOANS, routingService.determineTeam("renegociar dívida no Serasa e SPC"));
        assertEquals(TeamEnum.LOANS, routingService.determineTeam("qual a taxa de juros e CET"));
    }

    @Test
    @DisplayName("Deve desambiguar a palavra crédito: 'cartão de crédito' vai para Cartões, apenas 'crédito' ou 'crédito pessoal' vai para Empréstimos")
    void shouldDisambiguateCreditoProperly() {
        // Cartão de crédito -> CREDIT_CARDS
        assertEquals(TeamEnum.CREDIT_CARDS, routingService.determineTeam("Quero pedir um cartão de crédito"));
        assertEquals(TeamEnum.CREDIT_CARDS, routingService.determineTeam("Limite do cartão de crédito"));

        // Apenas crédito ou crédito pessoal -> LOANS
        assertEquals(TeamEnum.LOANS, routingService.determineTeam("Quero solicitar crédito pessoal"));
        assertEquals(TeamEnum.LOANS, routingService.determineTeam("Preciso de crédito urgente"));
        assertEquals(TeamEnum.LOANS, routingService.determineTeam("Linha de crédito"));
    }

    @Test
    @DisplayName("Deve rotear para time Outros quando assunto for genérico, não identificado ou vazio")
    void shouldRouteToOthersWhenSubjectIsGenericOrEmpty() {
        assertEquals(TeamEnum.OTHERS, routingService.determineTeam("Falar com atendente humano"));
        assertEquals(TeamEnum.OTHERS, routingService.determineTeam("Horário de funcionamento da agência"));
        assertEquals(TeamEnum.OTHERS, routingService.determineTeam("Bom dia"));
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

