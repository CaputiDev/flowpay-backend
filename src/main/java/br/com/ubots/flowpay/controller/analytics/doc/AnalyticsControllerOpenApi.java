package br.com.ubots.flowpay.controller.analytics.doc;

import br.com.ubots.flowpay.dto.MonthlyAnalyticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * Interface OpenAPI para os endpoints de relatórios e análises.
 */
@Tag(name = "Analytics", description = "Endpoints para relatórios, tempos de atendimento e métricas consolidadas por mês")
public interface AnalyticsControllerOpenApi {

    @Operation(
            summary = "Consultar Métricas Mensais e Histórico",
            description = "Retorna o total consolidado de chamados, taxa de sucesso, tempo médio em fila, tempo médio de atendimento e detalhamento por equipe agrupado mês a mês."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Relatório mensal de métricas retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = MonthlyAnalyticsResponse.class))
            )
    })
    ResponseEntity<MonthlyAnalyticsResponse> getMonthlyAnalytics();

    @Operation(
            summary = "Consultar Resumo Geral Consolidado",
            description = "Retorna o sumário global de chamados criados, resolvidos, recusados e médias gerais de tempo."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sumário global retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = MonthlyAnalyticsResponse.OverallSummaryDto.class))
            )
    })
    ResponseEntity<MonthlyAnalyticsResponse.OverallSummaryDto> getOverallOverview();
}
