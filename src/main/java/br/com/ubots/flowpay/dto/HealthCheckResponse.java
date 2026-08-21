package br.com.ubots.flowpay.dto;

import java.time.LocalDateTime;

public record HealthCheckResponse(
        String status,
        String message,
        String docs,
        LocalDateTime timestamp
) {
    public static HealthCheckResponse up() {
        return new HealthCheckResponse(
                "UP",
                "FlowPay MVP API operacional",
                "/swagger-ui/index.html",
                LocalDateTime.now()
        );
    }
}
