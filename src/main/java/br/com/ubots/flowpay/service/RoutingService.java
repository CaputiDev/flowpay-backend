package br.com.ubots.flowpay.service;

import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import br.com.ubots.flowpay.repository.AgentRepository;
import br.com.ubots.flowpay.repository.QueueRepository;
import br.com.ubots.flowpay.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.regex.Pattern;

@Service
public class RoutingService {

    private final TicketRepository ticketRepository;
    private final AgentRepository agentRepository;
    private final QueueRepository queueRepository;

    // Injeção de dependências via construtor
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