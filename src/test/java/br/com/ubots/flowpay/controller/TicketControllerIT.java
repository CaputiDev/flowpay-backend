package br.com.ubots.flowpay.controller;

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
class TicketControllerIT {

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
    @DisplayName("Deve criar ticket e atribuir para atendente livre (HTTP 201)")
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
    @DisplayName("Deve enviar para fila (HTTP 202) quando atendentes estiverem lotados")
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
    @DisplayName("Deve retornar HTTP 400 (Bad Request) quando o JSON estiver malformado")
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
    @DisplayName("Não deve criar ticket duplicado para o mesmo chatRef com status ativo")
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
    @DisplayName("Deve finalizar ticket (PATCH /v1/tickets/{id}/finish) e puxar a solicitação mais antiga da fila (FIFO)")
    void shouldFinishTicketAndAssignNextFromQueue() throws Exception {
        // 1. Criar ticket 1 (atribuído a atendente)
        TicketResponse activeTicketResponse = routingService.routeNewTicket("chat_client_1", "Problema com cartão");

        // Lotar capacidade para que próximo ticket vá para a fila
        for (int i = 2; i <= 9; i++) {
            routingService.routeNewTicket("chat_client_" + i, "Dúvida sobre cartão " + i);
        }

        // Criar ticket 10 (vai para a fila como PENDING)
        TicketResponse queuedTicketResponse = routingService.routeNewTicket("chat_queued_fifo", "Preciso de limite no cartão");
        assertEquals("PENDING", queuedTicketResponse.getStatus().name());

        // 2. Finalizar o ticket 1 via PATCH
        MvcResult finishResult = mockMvc.perform(patch("/v1/tickets/" + activeTicketResponse.getId() + "/finish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.finishedAt").exists())
                .andReturn();

        TicketResponse finishedResponse = objectMapper.readValue(finishResult.getResponse().getContentAsString(), TicketResponse.class);
        assertNotNull(finishedResponse.getFinishedAt());

        // 3. Verificar que a solicitação da fila (chat_queued_fifo) foi automaticamente atribuída e mudou para IN_PROGRESS
        var reassignedTicketOpt = ticketRepository.findById(queuedTicketResponse.getId());
        assertTrue(reassignedTicketOpt.isPresent());
        var reassignedTicket = reassignedTicketOpt.get();

        assertEquals("IN_PROGRESS", reassignedTicket.getStatus().name());
        assertEquals("chat_queued_fifo", reassignedTicket.getChatRef());
        assertNotNull(reassignedTicket.getAgentId());
    }

    @Test
    @DisplayName("Deve retornar HTTP 404 ao tentar finalizar ticket inexistente")
    void shouldReturn404WhenTicketNotFound() throws Exception {
        mockMvc.perform(patch("/v1/tickets/" + UUID.randomUUID() + "/finish"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar HTTP 422 ao tentar finalizar ticket que já foi finalizado")
    void shouldReturn422WhenTicketAlreadyFinished() throws Exception {
        TicketResponse ticket = routingService.routeNewTicket("chat_test_finished", "Dúvida cartão");

        mockMvc.perform(patch("/v1/tickets/" + ticket.getId() + "/finish"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/v1/tickets/" + ticket.getId() + "/finish"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Deve lidar com concorrência usando Lock Otimista e Retry")
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
}