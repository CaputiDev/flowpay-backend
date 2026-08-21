package br.com.ubots.flowpay.controller.analytics.doc;

import br.com.ubots.flowpay.dto.MonthlyAnalyticsResponse;
import br.com.ubots.flowpay.dto.TeamAnalyticsResponse;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Interface OpenAPI para os endpoints de relatórios e análises.
 */
@Tag(name = "Analytics", description = "Endpoints para relatórios, tempos de atendimento e métricas consolidadas por mês e por equipe")
public interface AnalyticsControllerOpenApi {

    @Operation(
            summary = "Consultar Métricas Mensais e Histórico Geral",
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

    @Operation(
            summary = "Consultar Métricas Analíticas de Todas as Equipes",
            description = "Retorna o sumário consolidado e histórico mês a mês de todas as equipes de atendimento."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de métricas de todas as equipes retornada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TeamAnalyticsResponse.class)))
            )
    })
    ResponseEntity<List<TeamAnalyticsResponse>> getAllTeamsAnalytics();

    @Operation(
            summary = "Consultar Métricas Analíticas de uma Equipe Específica",
            description = "Retorna o sumário consolidado, taxa de resolução e histórico mensal de uma equipe específica (ex: CREDIT_CARDS, LOANS, OTHERS)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Métricas da equipe retornadas com sucesso",
                    content = @Content(schema = @Schema(implementation = TeamAnalyticsResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nome de equipe inválido"
            )
    })
    ResponseEntity<TeamAnalyticsResponse> getTeamAnalytics(
            @Parameter(description = "Identificador da equipe (CREDIT_CARDS, LOANS ou OTHERS)", required = true)
            TeamEnum team
    );
}
