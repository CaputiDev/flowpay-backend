package br.com.ubots.flowpay.exception;

public class QueueFullException extends DomainException {
    public QueueFullException(String message) {
        super(message);
    }
}
