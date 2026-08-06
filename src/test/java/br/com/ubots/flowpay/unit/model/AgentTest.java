package br.com.ubots.flowpay.unit.model;

import br.com.ubots.flowpay.model.Agent;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentTest {

    @Test
    @DisplayName("Deve incrementar a carga do atendente corretamente")
    void shouldIncrementAgentLoad() {
        Agent agent = Agent.builder()
                .name("Ana")
                .team(TeamEnum.CREDIT_CARDS)
                .currentLoad(1)
                .maxCapacity(3)
                .build();

        agent.incrementLoad();

        assertEquals(2, agent.getCurrentLoad());
    }

    @Test
    @DisplayName("Deve inicializar carga em 0 se estiver nula e incrementar para 1")
    void shouldInitializeAndIncrementNullLoad() {
        Agent agent = Agent.builder()
                .name("Carlos")
                .currentLoad(null)
                .build();

        agent.incrementLoad();

        assertEquals(1, agent.getCurrentLoad());
    }

    @Test
    @DisplayName("Deve decrementar a carga do atendente quando maior que zero")
    void shouldDecrementAgentLoad() {
        Agent agent = Agent.builder()
                .currentLoad(2)
                .build();

        agent.decrementLoad();

        assertEquals(1, agent.getCurrentLoad());
    }

    @Test
    @DisplayName("Não deve decrementar a carga se já for zero")
    void shouldNotDecrementLoadBelowZero() {
        Agent agent = Agent.builder()
                .currentLoad(0)
                .build();

        agent.decrementLoad();

        assertEquals(0, agent.getCurrentLoad());
    }

    @Test
    @DisplayName("Deve verificar se atendente possui capacidade disponível")
    void shouldCheckHasAvailableCapacity() {
        Agent agentAvailable = Agent.builder()
                .currentLoad(2)
                .maxCapacity(3)
                .build();

        Agent agentFull = Agent.builder()
                .currentLoad(3)
                .maxCapacity(3)
                .build();

        assertTrue(agentAvailable.hasAvailableCapacity());
        assertFalse(agentFull.hasAvailableCapacity());
    }
}
