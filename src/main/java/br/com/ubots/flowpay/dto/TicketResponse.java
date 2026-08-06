package br.com.ubots.flowpay.dto;

import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de resposta para encapsular e proteger dados de saída da solicitação.
 */
@Getter
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
    private LocalDateTime finishedAt;
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
            this.finishedAt = ticket.getFinishedAt();
            this.queueId = ticket.getQueueId();
            this.agentId = ticket.getAgentId();
        }
    }

    public static TicketResponse fromEntity(Ticket ticket) {
        return new TicketResponse(ticket);
    }
}
