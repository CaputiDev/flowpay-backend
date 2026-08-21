package br.com.ubots.flowpay.service;

import br.com.ubots.flowpay.dto.MonthlyAnalyticsResponse;
import br.com.ubots.flowpay.dto.TeamAnalyticsResponse;
import br.com.ubots.flowpay.model.Queue;
import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import br.com.ubots.flowpay.repository.QueueRepository;
import br.com.ubots.flowpay.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TicketRepository ticketRepository;
    private final QueueRepository queueRepository;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Gera o relatório consolidado de métricas gerais e separadas mês a mês.
     */
    @Transactional(readOnly = true)
    public MonthlyAnalyticsResponse getMonthlyAnalytics() {
        List<Ticket> allTickets = ticketRepository.findAllOrderByCreatedAt();

        Map<UUID, Queue> queueMap = StreamSupport.stream(queueRepository.findAll().spliterator(), false)
                .collect(Collectors.toMap(Queue::getId, q -> q));

        // 1. Métricas Gerais (Overall Summary)
        MonthlyAnalyticsResponse.OverallSummaryDto overallSummary = calculateOverallSummary(allTickets);

        // 2. Agrupamento por Mês (YYYY-MM)
        Map<String, List<Ticket>> ticketsByMonth = allTickets.stream()
                .filter(t -> t.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().format(MONTH_FORMATTER),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<MonthlyAnalyticsResponse.MonthlyMetricDto> monthlyMetrics = ticketsByMonth.entrySet().stream()
                .map(entry -> buildMonthlyMetric(entry.getKey(), entry.getValue(), queueMap))
                .toList();

        return MonthlyAnalyticsResponse.builder()
                .overallSummary(overallSummary)
                .monthlyMetrics(monthlyMetrics)
                .build();
    }

    /**
     * Gera as métricas analíticas e histórico mensal de uma equipe específica.
     */
    @Transactional(readOnly = true)
    public TeamAnalyticsResponse getTeamAnalytics(TeamEnum team) {
        List<Ticket> allTickets = ticketRepository.findAllOrderByCreatedAt();
        Map<UUID, Queue> queueMap = StreamSupport.stream(queueRepository.findAll().spliterator(), false)
                .collect(Collectors.toMap(Queue::getId, q -> q));

        List<Ticket> teamTickets = allTickets.stream()
                .filter(t -> t.getQueueId() != null && queueMap.containsKey(t.getQueueId()) && queueMap.get(t.getQueueId()).getTeam() == team)
                .toList();

        return buildTeamAnalyticsResponse(team, teamTickets);
    }

    /**
     * Gera as métricas analíticas de todas as equipes.
     */
    @Transactional(readOnly = true)
    public List<TeamAnalyticsResponse> getAllTeamsAnalytics() {
        return Arrays.stream(TeamEnum.values())
                .map(this::getTeamAnalytics)
                .toList();
    }

    private TeamAnalyticsResponse buildTeamAnalyticsResponse(TeamEnum team, List<Ticket> teamTickets) {
        long totalTickets = teamTickets.size();
        long resolvedTickets = teamTickets.stream().filter(t -> StatusEnum.RESOLVED.equals(t.getStatus())).count();
        long rejectedTickets = teamTickets.stream().filter(t -> StatusEnum.REJECTED.equals(t.getStatus())).count();
        long inProgressTickets = teamTickets.stream().filter(t -> StatusEnum.IN_PROGRESS.equals(t.getStatus())).count();
        long pendingTickets = teamTickets.stream().filter(t -> StatusEnum.PENDING.equals(t.getStatus())).count();

        double avgWaitingTime = calculateAverage(
                teamTickets.stream()
                        .filter(t -> t.getWaitingTimeSeconds() != null && t.getStartedAt() != null)
                        .mapToLong(Ticket::getWaitingTimeSeconds)
                        .toArray()
        );

        double avgServiceTime = calculateAverage(
                teamTickets.stream()
                        .filter(t -> StatusEnum.RESOLVED.equals(t.getStatus()) && t.getServiceTimeSeconds() != null)
                        .mapToLong(Ticket::getServiceTimeSeconds)
                        .toArray()
        );

        double avgTotalTime = calculateAverage(
                teamTickets.stream()
                        .filter(t -> t.isFinished() && t.getTotalTimeSeconds() != null)
                        .mapToLong(Ticket::getTotalTimeSeconds)
                        .toArray()
        );

        double successRate = totalTickets > 0
                ? BigDecimal.valueOf(((double) resolvedTickets / totalTickets) * 100).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 100.0;

        TeamAnalyticsResponse.TeamSummaryDto summary = TeamAnalyticsResponse.TeamSummaryDto.builder()
                .totalTickets(totalTickets)
                .resolvedTickets(resolvedTickets)
                .rejectedTickets(rejectedTickets)
                .inProgressTickets(inProgressTickets)
                .pendingTickets(pendingTickets)
                .avgWaitingTimeSeconds(avgWaitingTime)
                .avgServiceTimeSeconds(avgServiceTime)
                .avgTotalTimeSeconds(avgTotalTime)
                .successRatePercent(successRate)
                .build();

        Map<String, List<Ticket>> ticketsByMonth = teamTickets.stream()
                .filter(t -> t.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().format(MONTH_FORMATTER),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<TeamAnalyticsResponse.TeamMonthlyHistoryDto> monthlyHistory = ticketsByMonth.entrySet().stream()
                .map(entry -> {
                    String month = entry.getKey();
                    List<Ticket> mTickets = entry.getValue();

                    long mTotal = mTickets.size();
                    long mResolved = mTickets.stream().filter(t -> StatusEnum.RESOLVED.equals(t.getStatus())).count();
                    long mRejected = mTickets.stream().filter(t -> StatusEnum.REJECTED.equals(t.getStatus())).count();
                    long mInProgress = mTickets.stream().filter(t -> StatusEnum.IN_PROGRESS.equals(t.getStatus())).count();
                    long mPending = mTickets.stream().filter(t -> StatusEnum.PENDING.equals(t.getStatus())).count();

                    double mAvgWaiting = calculateAverage(
                            mTickets.stream()
                                    .filter(t -> t.getWaitingTimeSeconds() != null && t.getStartedAt() != null)
                                    .mapToLong(Ticket::getWaitingTimeSeconds)
                                    .toArray()
                    );

                    double mAvgService = calculateAverage(
                            mTickets.stream()
                                    .filter(t -> StatusEnum.RESOLVED.equals(t.getStatus()) && t.getServiceTimeSeconds() != null)
                                    .mapToLong(Ticket::getServiceTimeSeconds)
                                    .toArray()
                    );

                    double mAvgTotal = calculateAverage(
                            mTickets.stream()
                                    .filter(t -> t.isFinished() && t.getTotalTimeSeconds() != null)
                                    .mapToLong(Ticket::getTotalTimeSeconds)
                                    .toArray()
                    );

                    double mSuccessRate = mTotal > 0
                            ? BigDecimal.valueOf(((double) mResolved / mTotal) * 100).setScale(2, RoundingMode.HALF_UP).doubleValue()
                            : 100.0;

                    return TeamAnalyticsResponse.TeamMonthlyHistoryDto.builder()
                            .month(month)
                            .totalTickets(mTotal)
                            .resolvedTickets(mResolved)
                            .rejectedTickets(mRejected)
                            .inProgressTickets(mInProgress)
                            .pendingTickets(mPending)
                            .avgWaitingTimeSeconds(mAvgWaiting)
                            .avgServiceTimeSeconds(mAvgService)
                            .avgTotalTimeSeconds(mAvgTotal)
                            .successRatePercent(mSuccessRate)
                            .build();
                })
                .toList();

        return TeamAnalyticsResponse.builder()
                .team(team)
                .teamName(team.name())
                .summary(summary)
                .monthlyHistory(monthlyHistory)
                .build();
    }

    private MonthlyAnalyticsResponse.OverallSummaryDto calculateOverallSummary(List<Ticket> tickets) {
        long totalTickets = tickets.size();
        long totalResolved = tickets.stream().filter(t -> StatusEnum.RESOLVED.equals(t.getStatus())).count();
        long totalRejected = tickets.stream().filter(t -> StatusEnum.REJECTED.equals(t.getStatus())).count();
        long totalInProgress = tickets.stream().filter(t -> StatusEnum.IN_PROGRESS.equals(t.getStatus())).count();
        long totalPending = tickets.stream().filter(t -> StatusEnum.PENDING.equals(t.getStatus())).count();

        double avgWaitingTime = calculateAverage(
                tickets.stream()
                        .filter(t -> t.getWaitingTimeSeconds() != null && t.getStartedAt() != null)
                        .mapToLong(Ticket::getWaitingTimeSeconds)
                        .toArray()
        );

        double avgServiceTime = calculateAverage(
                tickets.stream()
                        .filter(t -> StatusEnum.RESOLVED.equals(t.getStatus()) && t.getServiceTimeSeconds() != null)
                        .mapToLong(Ticket::getServiceTimeSeconds)
                        .toArray()
        );

        double avgTotalTime = calculateAverage(
                tickets.stream()
                        .filter(t -> t.isFinished() && t.getTotalTimeSeconds() != null)
                        .mapToLong(Ticket::getTotalTimeSeconds)
                        .toArray()
        );

        return MonthlyAnalyticsResponse.OverallSummaryDto.builder()
                .totalTickets(totalTickets)
                .totalResolved(totalResolved)
                .totalRejected(totalRejected)
                .totalInProgress(totalInProgress)
                .totalPending(totalPending)
                .avgWaitingTimeSeconds(avgWaitingTime)
                .avgServiceTimeSeconds(avgServiceTime)
                .avgTotalTimeSeconds(avgTotalTime)
                .build();
    }

    private MonthlyAnalyticsResponse.MonthlyMetricDto buildMonthlyMetric(
            String month,
            List<Ticket> monthTickets,
            Map<UUID, Queue> queueMap
    ) {
        long totalTickets = monthTickets.size();
        long resolvedTickets = monthTickets.stream().filter(t -> StatusEnum.RESOLVED.equals(t.getStatus())).count();
        long rejectedTickets = monthTickets.stream().filter(t -> StatusEnum.REJECTED.equals(t.getStatus())).count();
        long inProgressTickets = monthTickets.stream().filter(t -> StatusEnum.IN_PROGRESS.equals(t.getStatus())).count();
        long pendingTickets = monthTickets.stream().filter(t -> StatusEnum.PENDING.equals(t.getStatus())).count();

        double avgWaitingTime = calculateAverage(
                monthTickets.stream()
                        .filter(t -> t.getWaitingTimeSeconds() != null && t.getStartedAt() != null)
                        .mapToLong(Ticket::getWaitingTimeSeconds)
                        .toArray()
        );

        double avgServiceTime = calculateAverage(
                monthTickets.stream()
                        .filter(t -> StatusEnum.RESOLVED.equals(t.getStatus()) && t.getServiceTimeSeconds() != null)
                        .mapToLong(Ticket::getServiceTimeSeconds)
                        .toArray()
        );

        double avgTotalTime = calculateAverage(
                monthTickets.stream()
                        .filter(t -> t.isFinished() && t.getTotalTimeSeconds() != null)
                        .mapToLong(Ticket::getTotalTimeSeconds)
                        .toArray()
        );

        // Agrupamento por Equipe
        Map<TeamEnum, MonthlyAnalyticsResponse.TeamMetricDto> byTeam = new EnumMap<>(TeamEnum.class);
        Map<TeamEnum, List<Ticket>> ticketsByTeam = monthTickets.stream()
                .filter(t -> t.getQueueId() != null && queueMap.containsKey(t.getQueueId()))
                .collect(Collectors.groupingBy(t -> queueMap.get(t.getQueueId()).getTeam()));

        for (TeamEnum team : TeamEnum.values()) {
            List<Ticket> teamTickets = ticketsByTeam.getOrDefault(team, List.of());
            long teamTotal = teamTickets.size();
            long teamResolved = teamTickets.stream().filter(t -> StatusEnum.RESOLVED.equals(t.getStatus())).count();
            long teamRejected = teamTickets.stream().filter(t -> StatusEnum.REJECTED.equals(t.getStatus())).count();

            double teamAvgWaiting = calculateAverage(
                    teamTickets.stream()
                            .filter(t -> t.getWaitingTimeSeconds() != null && t.getStartedAt() != null)
                            .mapToLong(Ticket::getWaitingTimeSeconds)
                            .toArray()
            );

            double teamAvgService = calculateAverage(
                    teamTickets.stream()
                            .filter(t -> StatusEnum.RESOLVED.equals(t.getStatus()) && t.getServiceTimeSeconds() != null)
                            .mapToLong(Ticket::getServiceTimeSeconds)
                            .toArray()
            );

            byTeam.put(team, MonthlyAnalyticsResponse.TeamMetricDto.builder()
                    .team(team)
                    .totalTickets(teamTotal)
                    .resolvedTickets(teamResolved)
                    .rejectedTickets(teamRejected)
                    .avgWaitingTimeSeconds(teamAvgWaiting)
                    .avgServiceTimeSeconds(teamAvgService)
                    .build());
        }

        return MonthlyAnalyticsResponse.MonthlyMetricDto.builder()
                .month(month)
                .totalTickets(totalTickets)
                .resolvedTickets(resolvedTickets)
                .rejectedTickets(rejectedTickets)
                .inProgressTickets(inProgressTickets)
                .pendingTickets(pendingTickets)
                .avgWaitingTimeSeconds(avgWaitingTime)
                .avgServiceTimeSeconds(avgServiceTime)
                .avgTotalTimeSeconds(avgTotalTime)
                .byTeam(byTeam)
                .build();
    }

    private double calculateAverage(long[] values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (long v : values) {
            sum += v;
        }
        double avg = sum / values.length;
        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
