package br.com.ubots.flowpay.controller;

import br.com.ubots.flowpay.dto.TicketRequest;
import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import br.com.ubots.flowpay.service.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/tickets") // Representa o nosso /solicitacoes
@RequiredArgsConstructor
public class TicketController {

    private final RoutingService routingService;

    @PostMapping
    public ResponseEntity createTicket(@RequestBody TicketRequest request) {

        Ticket ticket = routingService.routeNewTicket(request.getChatRef(), request.getSubject());

        return switch (ticket.getStatus()) {
            case IN_PROGRESS -> ResponseEntity.status(HttpStatus.CREATED).body(ticket);
            case PENDING     -> ResponseEntity.status(HttpStatus.ACCEPTED).body(ticket);
            case REJECTED    -> ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ticket);
            default          -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ticket);
        };

    }
}