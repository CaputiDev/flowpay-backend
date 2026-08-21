package br.com.ubots.flowpay.unit.classifier;

import br.com.ubots.flowpay.classifier.CreditCardClassifier;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreditCardClassifierTest {

    private final CreditCardClassifier classifier = new CreditCardClassifier();

    @ParameterizedTest(name = "Deve classificar para Cartões: \"{0}\"")
    @ValueSource(strings = {
            // Termos principais
            "cartao",
            "cartoes",
            "debito",
            "adicional",
            "titular",

            // Fatura e Pagamentos
            "fatura",
            "boleto",
            "vencimento",
            "fechamento",
            "limite",
            "anuidade",
            "melhor dia",
            "codigo de barras",

            // Segurança e Problemas
            "bloqueio",
            "desbloqueio",
            "senha",
            "fraude",
            "clonagem",
            "clonado",
            "contestacao",
            "contestar",
            "roubo",
            "perda",
            "cvc",
            "cvv",
            "chip",
            "aproximacao",
            "contactless",

            // Benefícios
            "pontos",
            "milhas",
            "cashback",
            "sala vip",
            "programa de fidelidade",
            "bandeira",
            "visa",
            "mastercard",
            "elo",

            // Ações comuns / vias
            "2a via",
            "segunda via"
    })
    @DisplayName("Deve reconhecer todas as palavras-chave do time de Cartões")
    void shouldClassifyCardKeywords(String keyword) {
        Optional<TeamEnum> result = classifier.classify("quero resolver sobre " + keyword);
        assertTrue(result.isPresent());
        assertEquals(TeamEnum.CREDIT_CARDS, result.get());
    }
}
