package br.com.ubots.flowpay.service;

import br.com.ubots.flowpay.model.Agent;
import br.com.ubots.flowpay.model.Queue;
import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import br.com.ubots.flowpay.repository.AgentRepository;
import br.com.ubots.flowpay.repository.QueueRepository;
import br.com.ubots.flowpay.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RoutingService {

    private final TicketRepository ticketRepository;
    private final AgentRepository agentRepository;
    private final QueueRepository queueRepository;

    /**
     * Ponto de entrada para novos chamados no FlowPay.
     */
    @Retryable(
            value = { OptimisticLockingFailureException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public Ticket routeNewTicket(String chatRef, String subject) {

        // Trava de Idempotência
        if (ticketRepository.existsByChatRefAndStatusIn(chatRef, List.of(StatusEnum.IN_PROGRESS, StatusEnum.PENDING))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An active ticket already exists for this chat reference");
        }

        // Classificação do assunto
        TeamEnum targetTeam = determineTeam(subject);

        // Busca da Fila correspondente
        Queue queue = queueRepository.findByTeam(targetTeam)
                .orElseThrow(() -> new RuntimeException("Queue not found for team: " + targetTeam));

        // Busca atendente disponivel

        Optional<Agent> availableAgent = agentRepository.findAvailableAgentByTeam(targetTeam);



        if (availableAgent.isPresent()) {
            return assignTicketToAgent(chatRef, subject, queue, availableAgent.get());
        }

        // Manda atendimento pra fila
        return sendTicketToQueue(chatRef, subject, queue);
    }

    /**
     * Atribui o ticket.
     */
    private Ticket assignTicketToAgent(String chatRef, String subject, Queue queue, Agent agent) {

        agent.incrementLoad();
        agentRepository.save(agent);

        Ticket ticket = Ticket.createAssigned(chatRef, subject, queue.getId(), agent.getId());

        return ticketRepository.save(ticket);
    }

    /**
     * Enfileira ou rejeita o ticket.
     */
    private Ticket sendTicketToQueue(String chatRef, String subject, Queue queue) {
        long pendingCount = ticketRepository.countByQueueIdAndStatus(queue.getId(), StatusEnum.PENDING);

        // Bateu no teto? Aborta a missão e devolve 422!
        if (pendingCount >= queue.getMaxCapacity()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "The queue is currently at maximum capacity. Ticket rejected."
            );
        }

        // Se chegou aqui, tem vaga. Salva como PENDING!
        Ticket ticket = Ticket.createForQueue(chatRef, subject, queue.getId(), StatusEnum.PENDING);
        return ticketRepository.save(ticket);
    }

    /**
     * Método auxiliar para classificar o assunto.
     */
    TeamEnum determineTeam(String subject) {
        if (subject == null || subject.isBlank()) {
            return TeamEnum.OTHERS; // Proteção contra NullPointerException
        }

        String normalizedSubject = normalizeText(subject);

        if (normalizedSubject.contains("cartao")) {
            return TeamEnum.CREDIT_CARDS;
        }

        if (normalizedSubject.contains("emprestimo")) {
            return TeamEnum.LOANS;
        }

        return TeamEnum.OTHERS; // Fallback (Se não for nenhum dos acima)
    }

    private String normalizeText(String text) {
        // Desmonta os acentos da letra base
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);

        // Regex que arranca qualquer acento (marca diacrítica)
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String textWithoutAccents = pattern.matcher(normalized).replaceAll("");

        // Tudo minúsculo e sem espaços sobrando nas pontas
        return textWithoutAccents.toLowerCase().trim();
    }
}