package br.com.ubots.flowpay.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriedades de configuração do Rate Limiting.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "flowpay.rate-limit")
public class RateLimitProperties {

    /**
     * Habilita ou desabilita o filtro de rate limiting.
     */
    private boolean enabled = true;

    /**
     * Capacidade máxima de tokens acumulados no balde (suporte a burst/rajadas).
     */
    private long capacity = 150;

    /**
     * Quantidade de tokens repostos a cada intervalo.
     */
    private long refillTokens = 120;

    /**
     * Intervalo de tempo (em segundos) para recarga dos tokens.
     */
    private long refillDurationSeconds = 60;
}
