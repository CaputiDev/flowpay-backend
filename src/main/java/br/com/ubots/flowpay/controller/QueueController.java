package br.com.ubots.flowpay.controller;

import br.com.ubots.flowpay.controller.doc.QueueControllerOpenApi;
import br.com.ubots.flowpay.dto.QueueStatusResponse;
import br.com.ubots.flowpay.service.QueueStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsável pelos endpoints de consulta de estado das filas.
 */
@RestController
@RequestMapping("/v1/queues")
@RequiredArgsConstructor
public class QueueController implements QueueControllerOpenApi {

    private final QueueStatusService queueStatusService;

    /**
     * Endpoint para obter o snapshot em tempo real do estado das filas e equipes.
     */
    @Override
    @GetMapping("/status")
    public ResponseEntity<QueueStatusResponse> getQueueStatus() {
        QueueStatusResponse response = queueStatusService.getQueueStatus();
        return ResponseEntity.ok(response);
    }
}
