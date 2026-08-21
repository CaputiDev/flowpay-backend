package br.com.ubots.flowpay.unit.classifier;

import br.com.ubots.flowpay.classifier.LoanClassifier;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanClassifierTest {

    private final LoanClassifier classifier = new LoanClassifier();

    @ParameterizedTest(name = "Deve classificar para Empréstimos: \"{0}\"")
    @ValueSource(strings = {
            // Termos principais
            "emprestimo",
            "financiamento",
            "credito pessoal",
            "credito",
            "consignado",
            "antecipacao",
            "fgts",

            // Valores e Taxas
            "juros",
            "taxa",
            "cet",
            "iof",
            "montante",
            "saldo devedor",
            "amortizacao",

            // Contrato e Pagamento
            "parcela",
            "parcelamento",
            "renegociacao",
            "carne",
            "quitacao",
            "quitar",
            "atraso",
            "inadimplencia",
            "renegociar",
            "refinanciamento",
            "refinanciar",

            // Processo
            "simulacao",
            "simular",
            "analise",
            "aprovacao",
            "contrato",
            "garantia",
            "avalista",
            "score",
            "serasa",
            "spc"
    })
    @DisplayName("Deve reconhecer todas as palavras-chave do time de Empréstimos")
    void shouldClassifyLoanKeywords(String keyword) {
        Optional<TeamEnum> result = classifier.classify("preciso de " + keyword);
        assertTrue(result.isPresent());
        assertEquals(TeamEnum.LOANS, result.get());
    }
}
