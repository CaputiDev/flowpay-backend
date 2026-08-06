package br.com.ubots.flowpay.classifier;

import br.com.ubots.flowpay.model.enums.TeamEnum;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Classificador responsável por identificar solicitações referentes à equipe de Cartões de Crédito.
 */
@Component
@Order(1)
public class CreditCardClassifier implements SubjectClassifier {

    @Override
    public Optional<TeamEnum> classify(String normalizedSubject) {
        if (normalizedSubject != null && normalizedSubject.contains("cartao")) {
            return Optional.of(TeamEnum.CREDIT_CARDS);
        }
        return Optional.empty();
    }
}
