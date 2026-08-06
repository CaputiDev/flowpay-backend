package br.com.ubots.flowpay.controller.doc;

import br.com.ubots.flowpay.dto.TicketRequest;
import br.com.ubots.flowpay.dto.TicketResponse;
import br.com.ubots.flowpay.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * Interface dedicada exclusivamente à documentação OpenAPI / Swagger do
 * TicketController.
 * Mantém o Controller 100% limpo e desacoplado de anotações de documentação.
 */
@Tag(name = "Solicitações de Atendimento", description = "Endpoints para criação, roteamento automático e encerramento de solicitações de atendimento")
public interface TicketControllerOpenApi {

        @Operation(summary = "Criar e Rotear Solicitação", description = "Recebe uma nova solicitação de atendimento contendo a referência da conversa (chatRef) e o assunto. "
                        + "Classifica o assunto entre as equipes ('Cartões', 'Empréstimos' ou 'Outros Assuntos'), atribui a solicitação "
                        + "ao atendente com menor carga (até 3 atendimentos simultâneos) ou transborda para a fila FIFO caso todos os atendentes estejam lotados.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Solicitação criada e atribuída com sucesso a um atendente disponível (Status: IN_PROGRESS)", content = @Content(schema = @Schema(implementation = TicketResponse.class))),
                        @ApiResponse(responseCode = "202", description = "Atendentes lotados. Solicitação transbordada com sucesso para a fila de espera (Status: PENDING)", content = @Content(schema = @Schema(implementation = TicketResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Requisição inválida (JSON malformado ou campos obrigatórios nulos/vazios)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "409", description = "Conflito: Já existe uma solicitação ativa para a mesma referência de conversa (chatRef)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "422", description = "Entidade Não Processável: A fila de espera da equipe atingiu a capacidade máxima (3 chamados pendentes)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        ResponseEntity<TicketResponse> createTicket(TicketRequest request);

        @Operation(summary = "Finalizar Atendimento", description = "Finaliza a solicitação de atendimento ativa identificada pelo ID (Status: RESOLVED). "
                        + "Ao finalizar, o sistema decrementa a carga de trabalho do atendente e puxa automaticamente a solicitação "
                        + "pendente mais antiga da fila (respeitando a regra FIFO).")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Solicitação finalizada com sucesso e atendente liberado", content = @Content(schema = @Schema(implementation = TicketResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Solicitação não encontrada para o ID informado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "422", description = "Operação inválida: A solicitação não está em andamento (já se encontra finalizada)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        ResponseEntity<TicketResponse> finishTicket(UUID id);
}
