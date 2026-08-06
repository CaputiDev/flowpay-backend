package br.com.ubots.flowpay.exception;

public class TicketConflictException extends DomainException {
    public TicketConflictException(String message) {
        super(message);
    }
}
