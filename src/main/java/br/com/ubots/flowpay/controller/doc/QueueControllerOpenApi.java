package br.com.ubots.flowpay.controller.doc;

import br.com.ubots.flowpay.dto.QueueStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * Interface dedicada exclusivamente à documentação OpenAPI / Swagger do QueueController.
 * Mantém o Controller 100% limpo e desacoplado de anotações de documentação.
 */
@Tag(name = "Status das Filas", description = "Endpoints para consulta do estado em tempo real das filas e atendentes")
public interface QueueControllerOpenApi {

    @Operation(
            summary = "Consultar Estado Consolidado das Filas",
            description = "Retorna uma foto em tempo real do sistema contendo a Fila Ativa (atendimentos em andamento), "
                    + "a Fila em Espera (chamados pendentes com ordem FIFO e posição) e o resumo de capacidade por equipe. "
                    + "Operação de leitura segura sem alteração de estado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado consolidado das filas retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = QueueStatusResponse.class))
            )
    })
    ResponseEntity<QueueStatusResponse> getQueueStatus();
}
