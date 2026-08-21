package br.com.ubots.flowpay.model;

import br.com.ubots.flowpay.model.enums.StatusEnum;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Duration;
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

    @Column("started_at")
    private LocalDateTime startedAt;

    @Column("is_finished")
    @Builder.Default
    private Boolean finished = false;

    @Column("waiting_time_seconds")
    @Builder.Default
    private Long waitingTimeSeconds = 0L;

    @Column("service_time_seconds")
    @Builder.Default
    private Long serviceTimeSeconds = 0L;

    @Column("total_time_seconds")
    @Builder.Default
    private Long totalTimeSeconds = 0L;

    @Column("queue_id")
    private UUID queueId;

    @Column("agent_id")
    private UUID agentId;

    @Column("version")
    @Version
    private Long version;

    /**
     * Factory Method para um Ticket atribuído diretamente a um atendente.
     */
    public static Ticket createAssigned(String chatRef, String subject, UUID queueId, UUID agentId) {
        LocalDateTime now = LocalDateTime.now();
        return Ticket.builder()
                .chatRef(chatRef)
                .subject(subject)
                .queueId(queueId)
                .agentId(agentId)
                .status(StatusEnum.IN_PROGRESS)
                .createdAt(now)
                .startedAt(now)
                .finished(false)
                .waitingTimeSeconds(0L)
                .serviceTimeSeconds(0L)
                .totalTimeSeconds(0L)
                .build();
    }

    /**
     * Factory Method para um Ticket que aguarda na fila de espera.
     */
    public static Ticket createForQueue(String chatRef, String subject, UUID queueId, StatusEnum finalStatus) {
        LocalDateTime now = LocalDateTime.now();
        return Ticket.builder()
                .chatRef(chatRef)
                .subject(subject)
                .queueId(queueId)
                .status(finalStatus)
                .createdAt(now)
                .startedAt(null)
                .finished(false)
                .waitingTimeSeconds(0L)
                .serviceTimeSeconds(0L)
                .totalTimeSeconds(0L)
                .build();
    }

    /**
     * Factory Method para um Ticket recusado por capacidade máxima da fila.
     */
    public static Ticket createRejected(String chatRef, String subject, UUID queueId, String errorMsg) {
        LocalDateTime now = LocalDateTime.now();
        return Ticket.builder()
                .chatRef(chatRef)
                .subject(subject)
                .queueId(queueId)
                .status(StatusEnum.REJECTED)
                .errorMsg(errorMsg)
                .createdAt(now)
                .finishedAt(now)
                .startedAt(null)
                .finished(true)
                .waitingTimeSeconds(0L)
                .serviceTimeSeconds(0L)
                .totalTimeSeconds(0L)
                .build();
    }

    /**
     * Altera o status para RESOLVED (Finalizada), marca a flag finished e computa tempos de atendimento e total.
     */
    public void finish() {
        this.status = StatusEnum.RESOLVED;
        this.finishedAt = LocalDateTime.now();
        this.finished = true;

        if (this.startedAt != null) {
            this.serviceTimeSeconds = Math.max(0L, Duration.between(this.startedAt, this.finishedAt).toSeconds());
        } else {
            this.serviceTimeSeconds = 0L;
        }

        if (this.createdAt != null) {
            this.totalTimeSeconds = Math.max(0L, Duration.between(this.createdAt, this.finishedAt).toSeconds());
            if (this.waitingTimeSeconds == null || this.waitingTimeSeconds == 0L) {
                if (this.startedAt != null) {
                    this.waitingTimeSeconds = Math.max(0L, Duration.between(this.createdAt, this.startedAt).toSeconds());
                }
            }
        }
    }

    /**
     * Atribui o ticket a um atendente ao sair da fila, altera para IN_PROGRESS e registra tempo de espera.
     */
    public void assignTo(UUID agentId) {
        this.agentId = agentId;
        this.status = StatusEnum.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        if (this.createdAt != null) {
            this.waitingTimeSeconds = Math.max(0L, Duration.between(this.createdAt, this.startedAt).toSeconds());
        }
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
     * Verifica se o ticket foi finalizado (RESOLVED ou flag finished true).
     */
    public boolean isFinished() {
        return Boolean.TRUE.equals(this.finished) || StatusEnum.RESOLVED.equals(this.status) || StatusEnum.REJECTED.equals(this.status);
    }
}