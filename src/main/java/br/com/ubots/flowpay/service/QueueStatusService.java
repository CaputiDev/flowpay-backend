package br.com.ubots.flowpay.service;

import br.com.ubots.flowpay.dto.QueueStatusResponse;
import br.com.ubots.flowpay.model.Agent;
import br.com.ubots.flowpay.model.Queue;
import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import br.com.ubots.flowpay.repository.AgentRepository;
import br.com.ubots.flowpay.repository.QueueRepository;
import br.com.ubots.flowpay.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Serviço de leitura responsável por consolidar o estado em tempo real das filas e atendimentos.
 * Opera em modo somente leitura (read-only), sem alterar nenhuma entidade no banco de dados.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueStatusService {

    private final TicketRepository ticketRepository;
    private final AgentRepository agentRepository;
    private final QueueRepository queueRepository;

    /**
     * Retorna a fotografia atual das filas ativas, filas em espera e resumo por equipe.
     */
    @Transactional(readOnly = true)
    public QueueStatusResponse getQueueStatus() {
        log.debug("Recuperando status consolidado das filas");

        // 1. Carrega todas as filas cadastradas
        List<Queue> queues = StreamSupport.stream(queueRepository.findAll().spliterator(), false)
                .toList();
        Map<UUID, Queue> queueMap = queues.stream()
                .collect(Collectors.toMap(Queue::getId, q -> q));

        // 2. Carrega todos os atendentes
        List<Agent> agents = StreamSupport.stream(agentRepository.findAll().spliterator(), false)
                .toList();
        Map<UUID, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getId, a -> a));
        Map<TeamEnum, List<Agent>> agentsByTeam = agents.stream()
                .collect(Collectors.groupingBy(Agent::getTeam));

        // 3. Carrega chamados em andamento (Fila Ativa)
        List<Ticket> activeTickets = ticketRepository.findAllActiveTickets();
        List<QueueStatusResponse.ActiveTicketDto> activeTicketDtos = activeTickets.stream()
                .map(ticket -> {
                    Queue queue = queueMap.get(ticket.getQueueId());
                    Agent agent = ticket.getAgentId() != null ? agentMap.get(ticket.getAgentId()) : null;

                    return QueueStatusResponse.ActiveTicketDto.builder()
                            .id(ticket.getId())
                            .ticketNumber(ticket.getTicketNumber())
                            .chatRef(ticket.getChatRef())
                            .subject(ticket.getSubject())
                            .status(ticket.getStatus())
                            .team(queue != null ? queue.getTeam() : null)
                            .agentId(ticket.getAgentId())
                            .agentName(agent != null ? agent.getName() : null)
                            .createdAt(ticket.getCreatedAt())
                            .build();
                })
                .toList();

        // 4. Carrega chamados pendentes (Fila em Espera - FIFO)
        List<Ticket> waitingTickets = ticketRepository.findAllWaitingTickets();
        Map<UUID, Integer> positionCounter = new HashMap<>();

        List<QueueStatusResponse.WaitingTicketDto> waitingTicketDtos = waitingTickets.stream()
                .map(ticket -> {
                    Queue queue = queueMap.get(ticket.getQueueId());
                    int position = positionCounter.compute(ticket.getQueueId(), (key, current) -> current == null ? 1 : current + 1);

                    return QueueStatusResponse.WaitingTicketDto.builder()
                            .id(ticket.getId())
                            .ticketNumber(ticket.getTicketNumber())
                            .chatRef(ticket.getChatRef())
                            .subject(ticket.getSubject())
                            .status(ticket.getStatus())
                            .team(queue != null ? queue.getTeam() : null)
                            .queueId(ticket.getQueueId())
                            .position(position)
                            .createdAt(ticket.getCreatedAt())
                            .build();
                })
                .toList();

        // 5. Consolida resumo por equipe
        List<QueueStatusResponse.TeamSummaryDto> teamSummaries = queues.stream()
                .map(queue -> {
                    List<Agent> teamAgents = agentsByTeam.getOrDefault(queue.getTeam(), List.of());

                    List<QueueStatusResponse.AgentSummaryDto> agentDtos = teamAgents.stream()
                            .map(agent -> {
                                int currentLoad = agent.getCurrentLoad() != null ? agent.getCurrentLoad() : 0;
                                int maxCap = agent.getMaxCapacity() != null ? agent.getMaxCapacity() : 3;
                                int availCap = Math.max(0, maxCap - currentLoad);

                                return QueueStatusResponse.AgentSummaryDto.builder()
                                        .id(agent.getId())
                                        .name(agent.getName())
                                        .team(agent.getTeam())
                                        .currentLoad(currentLoad)
                                        .maxCapacity(maxCap)
                                        .availableCapacity(availCap)
                                        .build();
                            })
                            .toList();

                    int totalCapacity = teamAgents.stream()
                            .mapToInt(a -> a.getMaxCapacity() != null ? a.getMaxCapacity() : 3)
                            .sum();

                    int currentLoad = teamAgents.stream()
                            .mapToInt(a -> a.getCurrentLoad() != null ? a.getCurrentLoad() : 0)
                            .sum();

                    int waitingCount = (int) waitingTickets.stream()
                            .filter(t -> queue.getId().equals(t.getQueueId()))
                            .count();

                    return QueueStatusResponse.TeamSummaryDto.builder()
                            .team(queue.getTeam())
                            .queueId(queue.getId())
                            .maxQueueCapacity(queue.getMaxCapacity())
                            .waitingCount(waitingCount)
                            .totalAgents(teamAgents.size())
                            .totalCapacity(totalCapacity)
                            .currentLoad(currentLoad)
                            .agents(agentDtos)
                            .build();
                })
                .toList();

        return QueueStatusResponse.builder()
                .activeQueue(activeTicketDtos)
                .waitingQueue(waitingTicketDtos)
                .teamSummaries(teamSummaries)
                .build();
    }
}
