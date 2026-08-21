package br.com.ubots.flowpay.classifier;

import br.com.ubots.flowpay.model.enums.TeamEnum;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Classificador responsável por identificar solicitações referentes à equipe de Cartões.
 */
@Component
@Order(1)
public class CreditCardClassifier implements SubjectClassifier {

    private static final Pattern CARD_PATTERN = Pattern.compile(
            "\\b("
            + "cartao|cartoes|debito|adicional|titular|"
            + "fatura|boleto|vencimento|fechamento|limite|anuidade|melhor dia|codigo de barras|"
            + "bloqueio|desbloqueio|senha|fraude|clonagem|clonado|contestacao|contestar|roubo|perda|"
            + "cvc|cvv|chip|aproximacao|contactless|"
            + "pontos|milhas|cashback|sala vip|programa de fidelidade|bandeira|visa|mastercard|elo|"
            + "2a via|segunda via"
            + ")\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    @Override
    public Optional<TeamEnum> classify(String normalizedSubject) {
        if (normalizedSubject != null && CARD_PATTERN.matcher(normalizedSubject).find()) {
            return Optional.of(TeamEnum.CREDIT_CARDS);
        }
        return Optional.empty();
    }
}
