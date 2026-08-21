package br.com.ubots.flowpay.service;

import br.com.ubots.flowpay.classifier.SubjectClassifier;
import br.com.ubots.flowpay.dto.TicketResponse;
import br.com.ubots.flowpay.exception.InvalidTicketStatusException;
import br.com.ubots.flowpay.exception.QueueFullException;
import br.com.ubots.flowpay.exception.TicketConflictException;
import br.com.ubots.flowpay.exception.TicketNotFoundException;
import br.com.ubots.flowpay.model.Agent;
import br.com.ubots.flowpay.model.Queue;
import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import br.com.ubots.flowpay.repository.AgentRepository;
import br.com.ubots.flowpay.repository.QueueRepository;
import br.com.ubots.flowpay.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingService {

    private final TicketRepository ticketRepository;
    private final AgentRepository agentRepository;
    private final QueueRepository queueRepository;
    private final List<SubjectClassifier> subjectClassifiers;

    /**
     * Ponto de entrada para rotear novas solicitações de clientes.
     */
    @Retryable(
            retryFor = { OptimisticLockingFailureException.class, DbActionExecutionException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 100)
    )
    @Transactional(noRollbackFor = QueueFullException.class)
    public TicketResponse routeNewTicket(String chatRef, String subject) {

        // Trava de Idempotência
        if (ticketRepository.existsByChatRefAndStatusIn(chatRef, List.of(StatusEnum.IN_PROGRESS, StatusEnum.PENDING))) {
            throw new TicketConflictException("Uma solicitação ativa já existe para esta referência de chat: " + chatRef);
        }

        // Classificação do assunto usando Strategy Pattern
        TeamEnum targetTeam = determineTeam(subject);

        // Busca a fila correspondente
        Queue queue = queueRepository.findByTeam(targetTeam)
                .orElseThrow(() -> new RuntimeException("Fila não encontrada para o time: " + targetTeam));

        // Busca atendente disponível
        Optional<Agent> availableAgent = agentRepository.findAvailableAgentByTeam(targetTeam);

        if (availableAgent.isPresent()) {
            Ticket ticket = assignTicketToAgent(chatRef, subject, queue, availableAgent.get());
            return TicketResponse.fromEntity(ticket);
        }

        // Enfileira ou rejeita a solicitação
        Ticket ticket = sendTicketToQueue(chatRef, subject, queue);
        return TicketResponse.fromEntity(ticket);
    }

    /**
     * Finaliza uma solicitação ativa e puxa automaticamente o chamado pendente mais antigo da fila (FIFO).
     */
    @Retryable(
            retryFor = { OptimisticLockingFailureException.class, DbActionExecutionException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public TicketResponse finishTicket(UUID ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Solicitação não encontrada para o ID: " + ticketId));

        if (!ticket.isInProgress()) {
            throw new InvalidTicketStatusException("A solicitação não está em andamento (status atual: " + ticket.getStatus() + ")");
        }

        ticket.finish();
        Ticket savedTicket = ticketRepository.save(ticket);

        if (ticket.getAgentId() != null) {
            Agent agent = agentRepository.findById(ticket.getAgentId()).orElse(null);

            if (agent != null) {
                agent.decrementLoad();
                agentRepository.save(agent);

                // FIFO: Busca a solicitação pendente mais antiga na mesma fila
                Optional<Ticket> oldestPendingTicket = ticketRepository.findOldestTicketInQueue(ticket.getQueueId(), StatusEnum.PENDING.name());
                log.info("Solicitação finalizada {}, buscando chamado pendente na fila {}. Encontrado: {}", ticketId, ticket.getQueueId(), oldestPendingTicket);

                if (oldestPendingTicket.isPresent()) {
                    Ticket pendingTicket = oldestPendingTicket.get();
                    pendingTicket.assignTo(agent.getId());
                    agent.incrementLoad();

                    agentRepository.save(agent);
                    Ticket savedPending = ticketRepository.save(pendingTicket);
                    log.info("Solicitação pendente {} reatribuída ao atendente {}. Novo status: {}", pendingTicket.getId(), agent.getId(), savedPending.getStatus());
                }
            }
        }

        return TicketResponse.fromEntity(savedTicket);
    }

    /**
     * Atribui a solicitação diretamente a um atendente disponível.
     */
    private Ticket assignTicketToAgent(String chatRef, String subject, Queue queue, Agent agent) {
        agent.incrementLoad();
        agentRepository.save(agent);

        Ticket ticket = Ticket.createAssigned(chatRef, subject, queue.getId(), agent.getId());
        return ticketRepository.save(ticket);
    }

    /**
     * Enfileira ou rejeita a solicitação com base no limite máximo da fila.
     */
    private Ticket sendTicketToQueue(String chatRef, String subject, Queue queue) {
        long pendingCount = ticketRepository.countByQueueIdAndStatus(queue.getId(), StatusEnum.PENDING);

        if (pendingCount >= queue.getMaxCapacity()) {
            String errorMsg = "A fila atingiu a capacidade máxima. Solicitação recusada.";
            Ticket rejectedTicket = Ticket.createRejected(chatRef, subject, queue.getId(), errorMsg);
            ticketRepository.save(rejectedTicket);
            log.warn("Capacidade máxima da fila {} atingida. Ticket recusado e salvo com status REJECTED para o chatRef {}", queue.getTeam(), chatRef);
            throw new QueueFullException(errorMsg);
        }

        Ticket ticket = Ticket.createForQueue(chatRef, subject, queue.getId(), StatusEnum.PENDING);
        return ticketRepository.save(ticket);
    }

    /**
     * Aplicação do Strategy Pattern para classificação de assunto.
     */
    public TeamEnum determineTeam(String subject) {
        if (subject == null || subject.isBlank()) {
            return TeamEnum.OTHERS;
        }

        String normalizedSubject = normalizeText(subject);

        if (subjectClassifiers != null) {
            for (SubjectClassifier classifier : subjectClassifiers) {
                Optional<TeamEnum> classifiedTeam = classifier.classify(normalizedSubject);
                if (classifiedTeam.isPresent()) {
                    return classifiedTeam.get();
                }
            }
        }

        return TeamEnum.OTHERS;
    }

    public String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        String replaced = text.replace("ª", "a").replace("º", "o");
        String normalized = Normalizer.normalize(replaced, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String textWithoutAccents = pattern.matcher(normalized).replaceAll("");
        return textWithoutAccents.toLowerCase().trim();
    }
}