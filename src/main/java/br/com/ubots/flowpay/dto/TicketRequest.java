package br.com.ubots.flowpay.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class TicketRequest {
    @NotBlank(message = "Chat reference cannot be null or blank")
    private String chatRef;

    @NotBlank(message = "Subject cannot be null or blank")
    @Size(max = 255, message = "Subject must not exceed 255 characters")
    private String subject;
}