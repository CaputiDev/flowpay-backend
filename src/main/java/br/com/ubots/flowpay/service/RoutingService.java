package br.com.ubots.flowpay.service;

import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import br.com.ubots.flowpay.repository.AgentRepository;
import br.com.ubots.flowpay.repository.QueueRepository;
import br.com.ubots.flowpay.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoutingService {

    private final TicketRepository ticketRepository;
    private final AgentRepository agentRepository;
    private final QueueRepository queueRepository;

    // Injeção de dependências via construtor (Boas práticas de Sênior)
    public RoutingService(TicketRepository ticketRepository, AgentRepository agentRepository, QueueRepository queueRepository) {
        this.ticketRepository = ticketRepository;
        this.agentRepository = agentRepository;
        this.queueRepository = queueRepository;
    }

    /**
     * Ponto de entrada para novos chamados no FlowPay.
     */
    @Transactional
    public Ticket routeNewTicket(String chatRef, String subject) {

        // TODO Passo 1: Descobrir o TeamEnum correto baseado na string 'subject'
        TeamEnum targetTeam = determineTeam(subject);

        // TODO Passo 2: Buscar a Fila (Queue) no banco usando o targetTeam

        // TODO Passo 3: Criar a entidade Ticket e popular os dados iniciais

        // TODO Passo 4: Aplicar a regra de distribuição (Tem atendente? A fila tá cheia?)

        return null; // Temporário até implementarmos
    }

    /**
     * Método auxiliar para classificar o assunto.
     */
    private TeamEnum determineTeam(String subject) {
        // implementar a lógica de palavras-chave aqui
        return TeamEnum.OTHERS;
    }
}