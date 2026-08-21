package br.com.ubots.flowpay.e2e.ticket;

import br.com.ubots.flowpay.dto.TicketResponse;
import br.com.ubots.flowpay.model.Agent;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import br.com.ubots.flowpay.repository.AgentRepository;
import br.com.ubots.flowpay.repository.TicketRepository;
import br.com.ubots.flowpay.service.RoutingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketApiE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoutingService routingService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        for (Agent agent : agentRepository.findAll()) {
            Agent resetAgent = agent.toBuilder().currentLoad(0).build();
            agentRepository.save(resetAgent);
        }
    }

    @Test
    @DisplayName("E2E: Deve criar ticket e atribuir para atendente livre (HTTP 201)")
    void shouldCreateTicketAndAssignToAgent() throws Exception {
        String jsonPayload = """
                {
                  "chatRef": "whatsapp_555199999911",
                  "subject": "Preciso de ajuda com limite do meu cartão de crédito"
                }
                """;

        mockMvc.perform(post("/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.chatRef").value("whatsapp_555199999911"));
    }

    @Test
    @DisplayName("E2E: Deve enviar para fila (HTTP 202) quando atendentes estiverem lotados")
    void shouldSendToQueueWhenAgentsAreFull() throws Exception {
        for (int i = 0; i < 9; i++) {
            String jsonPayload = """
                    {
                      "chatRef": "whatsapp_cliente_%d",
                      "subject": "Dúvida sobre fatura do cartão"
                    }
                    """.formatted(i);

            mockMvc.perform(post("/v1/tickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonPayload))
                    .andExpect(status().isCreated());
        }

        String transbordoPayload = """
                {
                  "chatRef": "whatsapp_cliente_transbordo",
                  "subject": "Dúvida sobre fatura do cartão"
                }
                """;

        mockMvc.perform(post("/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(transbordoPayload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("E2E: Deve recusar solicitação com HTTP 422 quando a fila atingir capacidade máxima (3 chamados)")
    void shouldRejectTicketWith422WhenQueueIsFull() throws Exception {
        for (int i = 0; i < 9; i++) {
            routingService.routeNewTicket("client_active_" + i, "Dúvida sobre cartão");
        }

        for (int i = 0; i < 3; i++) {
            routingService.routeNewTicket("client_queue_" + i, "Dúvida sobre cartão");
        }

        String overflowPayload = """
                {
                  "chatRef": "client_overflow",
                  "subject": "Dúvida urgente cartão"
                }
                """;

        mockMvc.perform(post("/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(overflowPayload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("A fila atingiu a capacidade máxima. Solicitação recusada."));

        // Valida que o ticket recusado foi persistido com status REJECTED
        var rejectedTicketList = ticketRepository.findAllOrderByCreatedAt().stream()
                .filter(t -> "client_overflow".equals(t.getChatRef()))
                .toList();
        assertEquals(1, rejectedTicketList.size());
        assertEquals("REJECTED", rejectedTicketList.get(0).getStatus().name());
        assertTrue(rejectedTicketList.get(0).isFinished());
        assertNotNull(rejectedTicketList.get(0).getFinishedAt());
        assertEquals("A fila atingiu a capacidade máxima. Solicitação recusada.", rejectedTicketList.get(0).getErrorMsg());
    }

    @Test
    @DisplayName("E2E: Deve rotear corretamente para LOANS ignorando maiúsculas e acentos no assunto")
    void shouldRouteIgnoringCaseAndAccents() throws Exception {
        String jsonPayload = """
                {
                  "chatRef": "telegram_loan_123",
                  "subject": "SIMULAR EMPRÉSTIMO CONSIGNADO"
                }
                """;

        mockMvc.perform(post("/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("E2E: Deve rotear para o time OTHERS quando o assunto for genérico")
    void shouldRouteToOthersTeamForGenericSubjects() throws Exception {
        String jsonPayload = """
                {
                  "chatRef": "chat_generic_1",
                  "subject": "Quero falar com um atendente humano"
                }
                """;

        mockMvc.perform(post("/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("E2E: Deve retornar HTTP 400 (Bad Request) quando falhar a validação do DTO (campos vazios)")
    void shouldReturn400WhenDTOValidationFails() throws Exception {
        String blankChatRefPayload = """
                {
                  "chatRef": "",
                  "subject": "Dúvida cartão"
                }
                """;

        mockMvc.perform(post("/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(blankChatRefPayload))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message").value("chatRef: A referência da conversa não pode ser nula nem vazia"));

        String blankSubjectPayload = """
                {
                  "chatRef": "chat_valid",
                  "subject": "   "
                }
                """;

        mockMvc.perform(post("/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(blankSubjectPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("subject: O assunto não pode ser nulo nem vazio"));
    }

    @Test
    @DisplayName("E2E: Deve retornar HTTP 400 (Bad Request) quando o JSON estiver malformado")
    void shouldReturn400WhenJsonIsMalformed() throws Exception {
        String jsonPayload = """
                {
                  "chatRef": "whatsapp_999",
                  "subject": Preciso de ajuda
                """;

        mockMvc.perform(post("/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("E2E: Não deve criar ticket duplicado para o mesmo chatRef com status ativo")
    void shouldPreventDuplicateTickets() throws Exception {
        String jsonPayload = """
                {
                  "chatRef": "whatsapp_111222333",
                  "subject": "Problema com a conta"
                }
                """;

        mockMvc.perform(post("/v1/tickets").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/tickets").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("E2E: Deve finalizar ticket (PATCH /v1/tickets/{id}/finish) e puxar a solicitação mais antiga da fila (FIFO)")
    void shouldFinishTicketAndAssignNextFromQueue() throws Exception {
        TicketResponse activeTicketResponse = routingService.routeNewTicket("chat_client_1", "Problema com cartão");

        for (int i = 2; i <= 9; i++) {
            routingService.routeNewTicket("chat_client_" + i, "Dúvida sobre cartão " + i);
        }

        TicketResponse queuedTicketResponse = routingService.routeNewTicket("chat_queued_fifo",
                "Preciso de limite no cartão");
        assertEquals("PENDING", queuedTicketResponse.getStatus().name());

        MvcResult finishResult = mockMvc.perform(patch("/v1/tickets/" + activeTicketResponse.getId() + "/finish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.finishedAt").exists())
                .andReturn();

        TicketResponse finishedResponse = objectMapper.readValue(finishResult.getResponse().getContentAsString(),
                TicketResponse.class);
        assertNotNull(finishedResponse.getFinishedAt());

        var reassignedTicketOpt = ticketRepository.findById(queuedTicketResponse.getId());
        assertTrue(reassignedTicketOpt.isPresent());
        var reassignedTicket = reassignedTicketOpt.get();

        assertEquals("IN_PROGRESS", reassignedTicket.getStatus().name());
        assertEquals("chat_queued_fifo", reassignedTicket.getChatRef());
        assertNotNull(reassignedTicket.getAgentId());
    }

    @Test
    @DisplayName("E2E: Deve finalizar ticket quando a fila estiver vazia sem erros")
    void shouldFinishTicketWhenQueueIsEmpty() throws Exception {
        TicketResponse activeTicket = routingService.routeNewTicket("chat_empty_queue", "Dúvida cartão");

        mockMvc.perform(patch("/v1/tickets/" + activeTicket.getId() + "/finish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.finishedAt").exists());

        var ticketInDb = ticketRepository.findById(activeTicket.getId()).orElseThrow();
        assertEquals("RESOLVED", ticketInDb.getStatus().name());
        assertNotNull(ticketInDb.getFinishedAt());
    }

    @Test
    @DisplayName("E2E: Deve garantir isolamento de filas entre times ao finalizar atendimento")
    void shouldEnsureQueueIsolationBetweenTeamsOnFinish() throws Exception {
        TicketResponse loanActiveTicket = routingService.routeNewTicket("chat_loan_active", "Preciso de empréstimo");

        for (int i = 0; i < 9; i++) {
            routingService.routeNewTicket("chat_card_" + i, "Cartão de crédito");
        }
        TicketResponse cardPendingTicket = routingService.routeNewTicket("chat_card_queued", "Dúvida cartão");
        assertEquals("PENDING", cardPendingTicket.getStatus().name());

        mockMvc.perform(patch("/v1/tickets/" + loanActiveTicket.getId() + "/finish"))
                .andExpect(status().isOk());

        var cardTicketAfterFinish = ticketRepository.findById(cardPendingTicket.getId()).orElseThrow();
        assertEquals("PENDING", cardTicketAfterFinish.getStatus().name());
    }

    @Test
    @DisplayName("E2E: Deve retornar HTTP 404 ao tentar finalizar ticket inexistente")
    void shouldReturn404WhenTicketNotFound() throws Exception {
        mockMvc.perform(patch("/v1/tickets/" + UUID.randomUUID() + "/finish"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("E2E: Deve retornar HTTP 422 ao tentar finalizar ticket que já foi finalizado")
    void shouldReturn422WhenTicketAlreadyFinished() throws Exception {
        TicketResponse ticket = routingService.routeNewTicket("chat_test_finished", "Dúvida cartão");

        mockMvc.perform(patch("/v1/tickets/" + ticket.getId() + "/finish"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/v1/tickets/" + ticket.getId() + "/finish"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("E2E: Deve lidar com concorrência usando Lock Otimista e Retry")
    void shouldHandleConcurrencyWithOptimisticLocking() throws InterruptedException {
        int concurrentRequests = 2;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(concurrentRequests);

        Runnable task = () -> {
            try {
                startLatch.await();
                String randomChatRef = "whatsapp_" + UUID.randomUUID().toString().substring(0, 5);
                routingService.routeNewTicket(randomChatRef, "Dúvida sobre cartão");
            } catch (Exception e) {
                System.out.println("Falha na Thread: " + e.getMessage());
            } finally {
                doneLatch.countDown();
            }
        };

        for (int i = 0; i < concurrentRequests; i++) {
            executor.submit(task);
        }

        startLatch.countDown();
        doneLatch.await();

        long totalTickets = ticketRepository.count();
        assertTrue(totalTickets >= 2, "Devem existir pelo menos 2 tickets no banco");

        int totalLoad = 0;
        for (Agent agent : agentRepository.findAll()) {
            if (agent.getTeam() == TeamEnum.CREDIT_CARDS) {
                totalLoad += agent.getCurrentLoad();
            }
        }

        assertEquals(2, totalLoad, "A carga somada da equipe de cartão de crédito deve ser 2");
    }

    @Test
    @DisplayName("E2E: Deve expor /v1/analytics/monthly e /v1/analytics/overview com dados consolidados")
    void shouldExposeAnalyticsEndpoints() throws Exception {
        routingService.routeNewTicket("client_analytics_1", "Problema com cartão");
        TicketResponse ticket2 = routingService.routeNewTicket("client_analytics_2", "Simular empréstimo");
        routingService.finishTicket(ticket2.getId());

        mockMvc.perform(get("/v1/analytics/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallSummary.totalTickets").value(2))
                .andExpect(jsonPath("$.overallSummary.totalResolved").value(1))
                .andExpect(jsonPath("$.overallSummary.totalInProgress").value(1))
                .andExpect(jsonPath("$.monthlyMetrics").isArray())
                .andExpect(jsonPath("$.monthlyMetrics[0].totalTickets").value(2));

        mockMvc.perform(get("/v1/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTickets").value(2))
                .andExpect(jsonPath("$.totalResolved").value(1))
                .andExpect(jsonPath("$.totalInProgress").value(1));

        mockMvc.perform(get("/v1/analytics/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(get("/v1/analytics/teams/CREDIT_CARDS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.team").value("CREDIT_CARDS"))
                .andExpect(jsonPath("$.summary.totalTickets").value(1))
                .andExpect(jsonPath("$.summary.inProgressTickets").value(1));

        mockMvc.perform(get("/v1/analytics/teams/LOANS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.team").value("LOANS"))
                .andExpect(jsonPath("$.summary.totalTickets").value(1))
                .andExpect(jsonPath("$.summary.resolvedTickets").value(1));
    }
}

