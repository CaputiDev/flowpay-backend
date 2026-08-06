package br.com.ubots.flowpay.classifier;

import br.com.ubots.flowpay.model.enums.TeamEnum;

import java.util.Optional;

public interface SubjectClassifier {
    
    /**
     * Classifica o assunto normalizado para uma equipe específica.
     * Retorna Optional.empty() se o classificador não se aplicar ao assunto.
     */
    Optional<TeamEnum> classify(String normalizedSubject);
}
