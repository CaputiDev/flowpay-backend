package br.com.ubots.flowpay.controller.dashboard;

import br.com.ubots.flowpay.controller.dashboard.doc.TicketControllerOpenApi;
import br.com.ubots.flowpay.dto.TicketRequest;
import br.com.ubots.flowpay.dto.TicketResponse;
import br.com.ubots.flowpay.service.RoutingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller responsável pelos endpoints operacionais de criação e encerramento de tickets do Dashboard.
 */
@RestController
@RequestMapping("/v1/tickets")
@RequiredArgsConstructor
public class TicketController implements TicketControllerOpenApi {

    private final RoutingService routingService;

    @Override
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@RequestBody @Valid TicketRequest request) {
        TicketResponse response = routingService.routeNewTicket(request.getChatRef(), request.getSubject());

        return switch (response.getStatus()) {
            case IN_PROGRESS -> ResponseEntity.status(HttpStatus.CREATED).body(response);
            case PENDING     -> ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            case REJECTED    -> ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
            default          -> ResponseEntity.status(HttpStatus.OK).body(response);
        };
    }

    @Override
    @PatchMapping("/{id}/finish")
    public ResponseEntity<TicketResponse> finishTicket(@PathVariable UUID id) {
        TicketResponse response = routingService.finishTicket(id);
        return ResponseEntity.ok(response);
    }
}
