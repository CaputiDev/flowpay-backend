package br.com.ubots.flowpay.model;

import br.com.ubots.flowpay.model.enums.StatusEnum;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table("ticket")
public class Ticket {

    @Id
    @Column("id")
    private UUID id;

    @ReadOnlyProperty
    @Column("ticket_number")
    private Long ticketNumber;

    @Column("chat_ref")
    private String chatRef;

    @Column("subject")
    private String subject;

    @Column("status")
    private StatusEnum status;

    @Column("error_msg")
    private String errorMsg;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("finished_at")
    private LocalDateTime finishedAt;

    @Column("queue_id")
    private UUID queueId;

    @Column("agent_id")
    private UUID agentId;

    @Column("version")
    @Version
    private Long version;

    /**
     * Factory Method para um Ticket atribuído a um atendente.
     */
    public static Ticket createAssigned(String chatRef, String subject, UUID queueId, UUID agentId) {
        return Ticket.builder()
                .chatRef(chatRef)
                .subject(subject)
                .queueId(queueId)
                .agentId(agentId)
                .status(StatusEnum.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Factory Method para um Ticket que aguarda na fila ou foi rejeitado.
     */
    public static Ticket createForQueue(String chatRef, String subject, UUID queueId, StatusEnum finalStatus) {
        return Ticket.builder()
                .chatRef(chatRef)
                .subject(subject)
                .queueId(queueId)
                .status(finalStatus)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Altera o status para RESOLVED (Finalizada) e preenche a data de encerramento.
     */
    public void finish() {
        this.status = StatusEnum.RESOLVED;
        this.finishedAt = LocalDateTime.now();
    }

    /**
     * Atribui o ticket a um atendente e altera o status para IN_PROGRESS.
     */
    public void assignTo(UUID agentId) {
        this.agentId = agentId;
        this.status = StatusEnum.IN_PROGRESS;
    }

    /**
     * Verifica se o ticket está ativo (em andamento).
     */
    public boolean isInProgress() {
        return StatusEnum.IN_PROGRESS.equals(this.status);
    }

    /**
     * Verifica se o ticket está pendente na fila.
     */
    public boolean isPending() {
        return StatusEnum.PENDING.equals(this.status);
    }

    /**
     * Verifica se o ticket foi finalizado.
     */
    public boolean isFinished() {
        return StatusEnum.RESOLVED.equals(this.status);
    }
}