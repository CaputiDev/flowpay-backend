package br.com.ubots.flowpay.controller.dashboard;

import br.com.ubots.flowpay.controller.dashboard.doc.QueueControllerOpenApi;
import br.com.ubots.flowpay.dto.QueueStatusResponse;
import br.com.ubots.flowpay.service.QueueStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsável pela consulta em tempo real do estado das filas do Dashboard.
 */
@RestController
@RequestMapping("/v1/queues")
@RequiredArgsConstructor
public class QueueController implements QueueControllerOpenApi {

    private final QueueStatusService queueStatusService;

    @Override
    @GetMapping("/status")
    public ResponseEntity<QueueStatusResponse> getQueueStatus() {
        QueueStatusResponse response = queueStatusService.getQueueStatus();
        return ResponseEntity.ok(response);
    }
}
