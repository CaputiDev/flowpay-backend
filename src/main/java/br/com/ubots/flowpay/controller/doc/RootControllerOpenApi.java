package br.com.ubots.flowpay.controller.doc;

import br.com.ubots.flowpay.dto.HealthCheckResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * Interface dedicada exclusivamente à documentação OpenAPI / Swagger do RootController.
 */
@Tag(name = "Health Check / Raiz", description = "Endpoint raiz da API para verificação de status e documentação")
public interface RootControllerOpenApi {

    @Operation(
            summary = "Health Check e Rota da Documentação",
            description = "Retorna o status de saúde da aplicação e a rota de acesso para a documentação interativa Swagger UI."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Aplicação operacional com link para documentação",
                    content = @Content(schema = @Schema(implementation = HealthCheckResponse.class))
            )
    })
    ResponseEntity<HealthCheckResponse> root();
}
