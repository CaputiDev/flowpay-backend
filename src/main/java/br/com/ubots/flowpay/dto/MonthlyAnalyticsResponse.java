package br.com.ubots.flowpay.dto;

import br.com.ubots.flowpay.model.enums.TeamEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO que encapsula as métricas de chamados consolidadas e agrupadas por mês.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyAnalyticsResponse {

    private OverallSummaryDto overallSummary;
    private List<MonthlyMetricDto> monthlyMetrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallSummaryDto {
        private long totalTickets;
        private long totalResolved;
        private long totalRejected;
        private long totalInProgress;
        private long totalPending;
        private double avgWaitingTimeSeconds;
        private double avgServiceTimeSeconds;
        private double avgTotalTimeSeconds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyMetricDto {
        private String month; // Formato YYYY-MM (ex: "2026-08")
        private long totalTickets;
        private long resolvedTickets;
        private long rejectedTickets;
        private long inProgressTickets;
        private long pendingTickets;
        private double avgWaitingTimeSeconds;
        private double avgServiceTimeSeconds;
        private double avgTotalTimeSeconds;
        private Map<TeamEnum, TeamMetricDto> byTeam;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamMetricDto {
        private TeamEnum team;
        private long totalTickets;
        private long resolvedTickets;
        private long rejectedTickets;
        private double avgWaitingTimeSeconds;
        private double avgServiceTimeSeconds;
    }
}
