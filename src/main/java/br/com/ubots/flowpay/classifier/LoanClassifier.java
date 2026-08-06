package br.com.ubots.flowpay.classifier;

import br.com.ubots.flowpay.model.enums.TeamEnum;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Classificador responsável por identificar solicitações referentes à equipe de Empréstimos.
 */
@Component
@Order(2)
public class LoanClassifier implements SubjectClassifier {

    @Override
    public Optional<TeamEnum> classify(String normalizedSubject) {
        if (normalizedSubject != null && normalizedSubject.contains("emprestimo")) {
            return Optional.of(TeamEnum.LOANS);
        }
        return Optional.empty();
    }
}
