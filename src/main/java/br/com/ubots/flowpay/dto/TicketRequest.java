package br.com.ubots.flowpay.dto;
import lombok.Data;

@Data
public class TicketRequest {
    private String chatRef;
    private String subject;
}