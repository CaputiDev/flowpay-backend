package br.com.ubots.flowpay.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço responsável por gerenciar e verificar os baldes de requisições (Token Bucket) por endereço IP.
 */
@Service
@RequiredArgsConstructor
public class RateLimitingService {

    private final RateLimitProperties properties;
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    /**
     * Tenta consumir 1 token para o IP informado.
     *
     * @param clientIp IP do cliente
     * @return resultado do consumo contendo status de sucesso, tokens restantes e tempo para recarga
     */
    public ConsumptionProbe tryConsume(String clientIp) {
        Bucket bucket = bucketCache.computeIfAbsent(clientIp, this::createBucket);
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    /**
     * Cria um novo Bucket configurado de acordo com as propriedades da aplicação.
     */
    private Bucket createBucket(String clientIp) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(properties.getCapacity())
                .refillGreedy(properties.getRefillTokens(), Duration.ofSeconds(properties.getRefillDurationSeconds()))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Limpa o cache de baldes (útil em testes ou manutenção periódica).
     */
    public void clearCache() {
        bucketCache.clear();
    }
}
