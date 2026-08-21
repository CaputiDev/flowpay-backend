package br.com.ubots.flowpay.unit.service;

import br.com.ubots.flowpay.dto.MonthlyAnalyticsResponse;
import br.com.ubots.flowpay.model.Queue;
import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import br.com.ubots.flowpay.repository.QueueRepository;
import br.com.ubots.flowpay.repository.TicketRepository;
import br.com.ubots.flowpay.service.AnalyticsService;
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
class AnalyticsServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private QueueRepository queueRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("Analytics: Deve consolidar métricas mensais agrupadas por YYYY-MM com médias de tempo")
    void shouldConsolidateMonthlyMetrics() {
        UUID cardsQueueId = UUID.randomUUID();
        Queue cardsQueue = Queue.builder().id(cardsQueueId).team(TeamEnum.CREDIT_CARDS).maxCapacity(3).build();

        UUID loansQueueId = UUID.randomUUID();
        Queue loansQueue = Queue.builder().id(loansQueueId).team(TeamEnum.LOANS).maxCapacity(3).build();

        LocalDateTime august = LocalDateTime.of(2026, 8, 15, 10, 0);

        Ticket resolvedTicket = Ticket.builder()
                .id(UUID.randomUUID())
                .queueId(cardsQueueId)
                .status(StatusEnum.RESOLVED)
                .createdAt(august)
                .startedAt(august.plusSeconds(30))
                .finishedAt(august.plusSeconds(150))
                .finished(true)
                .waitingTimeSeconds(30L)
                .serviceTimeSeconds(120L)
                .totalTimeSeconds(150L)
                .build();

        Ticket rejectedTicket = Ticket.builder()
                .id(UUID.randomUUID())
                .queueId(loansQueueId)
                .status(StatusEnum.REJECTED)
                .createdAt(august)
                .finishedAt(august)
                .finished(true)
                .errorMsg("Fila cheia")
                .waitingTimeSeconds(0L)
                .serviceTimeSeconds(0L)
                .totalTimeSeconds(0L)
                .build();

        Ticket inProgressTicket = Ticket.builder()
                .id(UUID.randomUUID())
                .queueId(cardsQueueId)
                .status(StatusEnum.IN_PROGRESS)
                .createdAt(august)
                .startedAt(august.plusSeconds(10))
                .finished(false)
                .waitingTimeSeconds(10L)
                .build();

        when(ticketRepository.findAllOrderByCreatedAt()).thenReturn(List.of(resolvedTicket, rejectedTicket, inProgressTicket));
        when(queueRepository.findAll()).thenReturn(List.of(cardsQueue, loansQueue));

        MonthlyAnalyticsResponse response = analyticsService.getMonthlyAnalytics();

        assertNotNull(response);
        assertNotNull(response.getOverallSummary());
        assertEquals(3, response.getOverallSummary().getTotalTickets());
        assertEquals(1, response.getOverallSummary().getTotalResolved());
        assertEquals(1, response.getOverallSummary().getTotalRejected());
        assertEquals(1, response.getOverallSummary().getTotalInProgress());

        assertEquals(1, response.getMonthlyMetrics().size());
        MonthlyAnalyticsResponse.MonthlyMetricDto augustMetric = response.getMonthlyMetrics().get(0);
        assertEquals("2026-08", augustMetric.getMonth());
        assertEquals(3, augustMetric.getTotalTickets());
        assertEquals(1, augustMetric.getResolvedTickets());
        assertEquals(1, augustMetric.getRejectedTickets());
        assertEquals(1, augustMetric.getInProgressTickets());

        // Tempo médio de espera: (30 + 10) / 2 = 20.0
        assertEquals(20.0, augustMetric.getAvgWaitingTimeSeconds());
        // Tempo médio de atendimento: 120.0
        assertEquals(120.0, augustMetric.getAvgServiceTimeSeconds());

        assertNotNull(augustMetric.getByTeam());
        assertEquals(2, augustMetric.getByTeam().get(TeamEnum.CREDIT_CARDS).getTotalTickets());
        assertEquals(1, augustMetric.getByTeam().get(TeamEnum.LOANS).getTotalTickets());
    }
}
