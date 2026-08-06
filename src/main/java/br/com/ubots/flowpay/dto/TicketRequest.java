package br.com.ubots.flowpay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * DTO de requisição para recebimento de novas solicitações de atendimento.
 */
@Getter
public class TicketRequest {

    @NotBlank(message = "A referência da conversa não pode ser nula nem vazia")
    private String chatRef;

    @NotBlank(message = "O assunto não pode ser nulo nem vazio")
    @Size(max = 255, message = "O assunto não deve exceder 255 caracteres")
    private String subject;
}