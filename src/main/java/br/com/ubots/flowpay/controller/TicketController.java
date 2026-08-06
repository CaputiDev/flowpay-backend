package br.com.ubots.flowpay.controller;

import br.com.ubots.flowpay.dto.TicketRequest;
import br.com.ubots.flowpay.dto.TicketResponse;
import br.com.ubots.flowpay.service.RoutingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final RoutingService routingService;

    /**
     * Endpoint para criação e roteamento de novas solicitações de atendimento.
     */
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

    /**
     * Endpoint para finalização de um atendimento atual, liberando a vaga do atendente.
     */
    @PatchMapping({"/{id}/finish", "/{id}/finalizar"})
    public ResponseEntity<TicketResponse> finishTicket(@PathVariable UUID id) {
        TicketResponse response = routingService.finishTicket(id);
        return ResponseEntity.ok(response);
    }
}