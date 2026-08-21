package br.com.ubots.flowpay.dto;

import br.com.ubots.flowpay.model.enums.TeamEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que encapsula as métricas analíticas e histórico consolidados de uma equipe específica.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamAnalyticsResponse {

    private TeamEnum team;
    private String teamName;
    private TeamSummaryDto summary;
    private List<TeamMonthlyHistoryDto> monthlyHistory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamSummaryDto {
        private long totalTickets;
        private long resolvedTickets;
        private long rejectedTickets;
        private long inProgressTickets;
        private long pendingTickets;
        private double avgWaitingTimeSeconds;
        private double avgServiceTimeSeconds;
        private double avgTotalTimeSeconds;
        private double successRatePercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamMonthlyHistoryDto {
        private String month; // Formato YYYY-MM
        private long totalTickets;
        private long resolvedTickets;
        private long rejectedTickets;
        private long inProgressTickets;
        private long pendingTickets;
        private double avgWaitingTimeSeconds;
        private double avgServiceTimeSeconds;
        private double avgTotalTimeSeconds;
        private double successRatePercent;
    }
}
