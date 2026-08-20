package br.com.ubots.flowpay.e2e.ticket;

import br.com.ubots.flowpay.model.Agent;
import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import br.com.ubots.flowpay.repository.AgentRepository;
import br.com.ubots.flowpay.repository.QueueRepository;
import br.com.ubots.flowpay.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QueueStatusE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private QueueRepository queueRepository;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        for (Agent agent : agentRepository.findAll()) {
            Agent resetAgent = agent.toBuilder().currentLoad(0).build();
            agentRepository.save(resetAgent);
        }
    }

    @Test
    @DisplayName("E2E: Deve retornar snapshot completo das filas sem alterar o estado do banco")
    void shouldReturnCompleteQueueSnapshotWithoutMutatingState() throws Exception {
        // 1. Cria 1 ticket de Cartões (deve ir para Fila Ativa - IN_PROGRESS)
        String requestCard1 = """
                {
                  "chatRef": "user_card_1",
                  "subject": "Problema na fatura do meu cartão de crédito"
                }
                """;
        mockMvc.perform(post("/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestCard1))
                .andExpect(status().isCreated());

        // 2. Enche a capacidade dos 3 atendentes de Cartões para forçar o transbordo (cada um aguenta 3 = 9 chamados)
        // Para simular de forma rápida e controlada, vamos ajustar a carga de 2 atendentes para 3 e 1 para 3
        var cardQueue = queueRepository.findByTeam(TeamEnum.CREDIT_CARDS).orElseThrow();
        var cardAgents = agentRepository.findAll();
        for (Agent agent : cardAgents) {
            if (agent.getTeam() == TeamEnum.CREDIT_CARDS) {
                agentRepository.save(agent.toBuilder().currentLoad(agent.getMaxCapacity()).build());
            }
        }

        // 3. Insere 2 tickets pendentes (Fila em Espera)
        Ticket pendingTicket1 = Ticket.createForQueue("user_pending_1", "Limite do cartão", cardQueue.getId(), StatusEnum.PENDING);
        ticketRepository.save(pendingTicket1);

        Ticket pendingTicket2 = Ticket.createForQueue("user_pending_2", "Novo cartão", cardQueue.getId(), StatusEnum.PENDING);
        ticketRepository.save(pendingTicket2);

        // 4. Executa a chamada GET /v1/queues/status
        long ticketCountBefore = ticketRepository.count();

        mockMvc.perform(get("/v1/queues/status")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeQueue", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.activeQueue[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.activeQueue[0].agentName").isNotEmpty())
                .andExpect(jsonPath("$.waitingQueue", hasSize(2)))
                .andExpect(jsonPath("$.waitingQueue[0].chatRef").value("user_pending_1"))
                .andExpect(jsonPath("$.waitingQueue[0].position").value(1))
                .andExpect(jsonPath("$.waitingQueue[1].chatRef").value("user_pending_2"))
                .andExpect(jsonPath("$.waitingQueue[1].position").value(2))
                .andExpect(jsonPath("$.teamSummaries", hasSize(3)));

        // 5. Garante idempotência e zero mutação
        long ticketCountAfter = ticketRepository.count();
        assertEquals(ticketCountBefore, ticketCountAfter);
    }
}
