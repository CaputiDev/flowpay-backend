package br.com.ubots.flowpay.unit.classifier;

import br.com.ubots.flowpay.classifier.CreditCardClassifier;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CreditCardClassifierTest {

    private CreditCardClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new CreditCardClassifier();
    }

    @Test
    @DisplayName("Deve retornar CREDIT_CARDS quando o assunto contiver 'cartao'")
    void shouldClassifyCreditCardSubject() {
        Optional<TeamEnum> result = classifier.classify("duvida sobre cartao de credito");
        assertTrue(result.isPresent());
        assertEquals(TeamEnum.CREDIT_CARDS, result.get());
    }

    @Test
    @DisplayName("Deve retornar Optional.empty() quando o assunto não contiver 'cartao'")
    void shouldReturnEmptyForOtherSubjects() {
        Optional<TeamEnum> result = classifier.classify("preciso de um emprestimo");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar Optional.empty() quando o assunto for nulo")
    void shouldReturnEmptyForNullSubject() {
        Optional<TeamEnum> result = classifier.classify(null);
        assertTrue(result.isEmpty());
    }
}
