package br.com.ubots.flowpay.service;

import br.com.ubots.flowpay.model.enums.TeamEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoutingServiceTest {

    private RoutingService routingService;

    @BeforeEach
    void setUp() {
        // Como o método determineTeam não usa o banco de dados,
        // podemos passar "null" para os repositórios apenas para instanciar a classe no teste.
        routingService = new RoutingService(null, null, null);
    }

    @Test
    void shouldRouteToCreditCardsWhenSubjectContainsCartao() {
        // Arrange
        String subject1 = "Preciso de ajuda com meu CARTÃO de crédito";
        String subject2 = "cartao"; // Limite mínimo

        // Act & Assert
        assertEquals(TeamEnum.CREDIT_CARDS, routingService.determineTeam(subject1));
        assertEquals(TeamEnum.CREDIT_CARDS, routingService.determineTeam(subject2));
    }

    @Test
    void shouldRouteToLoansWhenSubjectContainsEmprestimo() {
        // Arrange, Act & Assert consolidados
        assertEquals(TeamEnum.LOANS, routingService.determineTeam("Quero um empréstimo agora"));
        assertEquals(TeamEnum.LOANS, routingService.determineTeam("EMPRÉSTIMO"));
    }

    @Test
    void shouldRouteToOthersWhenSubjectIsGenericOrEmpty() {
        // Verifica o fluxo de Fallback (Sensores de segurança)
        assertEquals(TeamEnum.OTHERS, routingService.determineTeam("Falar com humano"));
        assertEquals(TeamEnum.OTHERS, routingService.determineTeam("   ")); // Espaços vazios
        assertEquals(TeamEnum.OTHERS, routingService.determineTeam(null)); // Previne NullPointerException
    }
}