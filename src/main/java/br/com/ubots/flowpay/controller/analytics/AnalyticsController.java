package br.com.ubots.flowpay.controller.analytics;

import br.com.ubots.flowpay.controller.analytics.doc.AnalyticsControllerOpenApi;
import br.com.ubots.flowpay.dto.MonthlyAnalyticsResponse;
import br.com.ubots.flowpay.dto.TeamAnalyticsResponse;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import br.com.ubots.flowpay.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller responsável pelos relatórios e análises consolidadas de atendimento.
 */
@RestController
@RequestMapping("/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController implements AnalyticsControllerOpenApi {

    private final AnalyticsService analyticsService;

    @Override
    @GetMapping("/monthly")
    public ResponseEntity<MonthlyAnalyticsResponse> getMonthlyAnalytics() {
        MonthlyAnalyticsResponse response = analyticsService.getMonthlyAnalytics();
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/overview")
    public ResponseEntity<MonthlyAnalyticsResponse.OverallSummaryDto> getOverallOverview() {
        MonthlyAnalyticsResponse response = analyticsService.getMonthlyAnalytics();
        return ResponseEntity.ok(response.getOverallSummary());
    }

    @Override
    @GetMapping("/teams")
    public ResponseEntity<List<TeamAnalyticsResponse>> getAllTeamsAnalytics() {
        List<TeamAnalyticsResponse> responses = analyticsService.getAllTeamsAnalytics();
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/teams/{team}")
    public ResponseEntity<TeamAnalyticsResponse> getTeamAnalytics(@PathVariable TeamEnum team) {
        TeamAnalyticsResponse response = analyticsService.getTeamAnalytics(team);
        return ResponseEntity.ok(response);
    }
}
