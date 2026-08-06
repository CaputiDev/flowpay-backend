package br.com.ubots.flowpay.exception;

public class TicketNotFoundException extends DomainException {
    public TicketNotFoundException(String message) {
        super(message);
    }
}
