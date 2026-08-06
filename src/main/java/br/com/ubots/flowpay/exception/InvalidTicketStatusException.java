package br.com.ubots.flowpay.exception;

public class InvalidTicketStatusException extends DomainException {
    public InvalidTicketStatusException(String message) {
        super(message);
    }
}
