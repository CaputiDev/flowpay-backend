package br.com.ubots.flowpay.dto;

import br.com.ubots.flowpay.model.enums.StatusEnum;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO que encapsula a visualização agregada do estado das filas e atendimentos para o Dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusResponse {

    /**
     * Lista de atendimentos em andamento no momento.
     */
    private List<ActiveTicketDto> activeQueue;

    /**
     * Lista de chamados aguardando na fila de espera (ordenados por FIFO).
     */
    private List<WaitingTicketDto> waitingQueue;

    /**
     * Resumo consolidado por equipe de atendimento.
     */
    private List<TeamSummaryDto> teamSummaries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveTicketDto {
        private UUID id;
        private Long ticketNumber;
        private String chatRef;
        private String subject;
        private StatusEnum status;
        private TeamEnum team;
        private UUID agentId;
        private String agentName;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WaitingTicketDto {
        private UUID id;
        private Long ticketNumber;
        private String chatRef;
        private String subject;
        private StatusEnum status;
        private TeamEnum team;
        private UUID queueId;
        private Integer position;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamSummaryDto {
        private TeamEnum team;
        private UUID queueId;
        private Integer maxQueueCapacity;
        private Integer waitingCount;
        private Integer totalAgents;
        private Integer totalCapacity;
        private Integer currentLoad;
        private List<AgentSummaryDto> agents;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentSummaryDto {
        private UUID id;
        private String name;
        private TeamEnum team;
        private Integer currentLoad;
        private Integer maxCapacity;
        private Integer availableCapacity;
    }
}
