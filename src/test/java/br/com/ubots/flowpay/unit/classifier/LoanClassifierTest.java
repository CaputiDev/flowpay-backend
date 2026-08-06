package br.com.ubots.flowpay.unit.classifier;

import br.com.ubots.flowpay.classifier.LoanClassifier;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LoanClassifierTest {

    private LoanClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new LoanClassifier();
    }

    @Test
    @DisplayName("Deve retornar LOANS quando o assunto contiver 'emprestimo'")
    void shouldClassifyLoanSubject() {
        Optional<TeamEnum> result = classifier.classify("quero simular um emprestimo consignado");
        assertTrue(result.isPresent());
        assertEquals(TeamEnum.LOANS, result.get());
    }

    @Test
    @DisplayName("Deve retornar Optional.empty() quando o assunto não contiver 'emprestimo'")
    void shouldReturnEmptyForOtherSubjects() {
        Optional<TeamEnum> result = classifier.classify("duvida sobre fatura do cartao");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar Optional.empty() quando o assunto for nulo")
    void shouldReturnEmptyForNullSubject() {
        Optional<TeamEnum> result = classifier.classify(null);
        assertTrue(result.isEmpty());
    }
}
