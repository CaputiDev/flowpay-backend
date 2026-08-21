package br.com.ubots.flowpay.dto;

import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de resposta para encapsular e proteger dados de saída da solicitação.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private UUID id;
    private Long ticketNumber;
    private String chatRef;
    private String subject;
    private StatusEnum status;
    private String errorMsg;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Boolean isFinished;
    private Long waitingTimeSeconds;
    private Long serviceTimeSeconds;
    private Long totalTimeSeconds;
    private UUID queueId;
    private UUID agentId;

    public TicketResponse(Ticket ticket) {
        if (ticket != null) {
            this.id = ticket.getId();
            this.ticketNumber = ticket.getTicketNumber();
            this.chatRef = ticket.getChatRef();
            this.subject = ticket.getSubject();
            this.status = ticket.getStatus();
            this.errorMsg = ticket.getErrorMsg();
            this.createdAt = ticket.getCreatedAt();
            this.startedAt = ticket.getStartedAt();
            this.finishedAt = ticket.getFinishedAt();
            this.isFinished = ticket.isFinished();
            this.waitingTimeSeconds = ticket.getWaitingTimeSeconds() != null ? ticket.getWaitingTimeSeconds() : 0L;
            this.serviceTimeSeconds = ticket.getServiceTimeSeconds() != null ? ticket.getServiceTimeSeconds() : 0L;
            this.totalTimeSeconds = ticket.getTotalTimeSeconds() != null ? ticket.getTotalTimeSeconds() : 0L;
            this.queueId = ticket.getQueueId();
            this.agentId = ticket.getAgentId();
        }
    }

    public static TicketResponse fromEntity(Ticket ticket) {
        return new TicketResponse(ticket);
    }
}
