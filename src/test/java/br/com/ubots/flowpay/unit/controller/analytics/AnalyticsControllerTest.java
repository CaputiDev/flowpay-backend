package br.com.ubots.flowpay.unit.controller.analytics;

import br.com.ubots.flowpay.controller.analytics.AnalyticsController;
import br.com.ubots.flowpay.dto.MonthlyAnalyticsResponse;
import br.com.ubots.flowpay.dto.TeamAnalyticsResponse;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import br.com.ubots.flowpay.service.AnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("AnalyticsController: Deve retornar 200 OK no endpoint /v1/analytics/monthly")
    void shouldReturnMonthlyAnalytics() throws Exception {
        MonthlyAnalyticsResponse response = MonthlyAnalyticsResponse.builder()
                .overallSummary(MonthlyAnalyticsResponse.OverallSummaryDto.builder()
                        .totalTickets(10)
                        .totalResolved(8)
                        .totalRejected(2)
                        .avgWaitingTimeSeconds(15.5)
                        .avgServiceTimeSeconds(120.0)
                        .build())
                .monthlyMetrics(List.of(
                        MonthlyAnalyticsResponse.MonthlyMetricDto.builder()
                                .month("2026-08")
                                .totalTickets(10)
                                .resolvedTickets(8)
                                .rejectedTickets(2)
                                .avgWaitingTimeSeconds(15.5)
                                .avgServiceTimeSeconds(120.0)
                                .build()
                ))
                .build();

        when(analyticsService.getMonthlyAnalytics()).thenReturn(response);

        mockMvc.perform(get("/v1/analytics/monthly")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallSummary.totalTickets").value(10))
                .andExpect(jsonPath("$.overallSummary.totalResolved").value(8))
                .andExpect(jsonPath("$.monthlyMetrics[0].month").value("2026-08"))
                .andExpect(jsonPath("$.monthlyMetrics[0].totalTickets").value(10));
    }

    @Test
    @DisplayName("AnalyticsController: Deve retornar 200 OK no endpoint /v1/analytics/overview")
    void shouldReturnAnalyticsOverview() throws Exception {
        MonthlyAnalyticsResponse response = MonthlyAnalyticsResponse.builder()
                .overallSummary(MonthlyAnalyticsResponse.OverallSummaryDto.builder()
                        .totalTickets(25)
                        .totalResolved(20)
                        .totalRejected(5)
                        .avgWaitingTimeSeconds(12.0)
                        .avgServiceTimeSeconds(95.0)
                        .build())
                .build();

        when(analyticsService.getMonthlyAnalytics()).thenReturn(response);

        mockMvc.perform(get("/v1/analytics/overview")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTickets").value(25))
                .andExpect(jsonPath("$.totalResolved").value(20))
                .andExpect(jsonPath("$.totalRejected").value(5));
    }

    @Test
    @DisplayName("AnalyticsController: Deve retornar 200 OK no endpoint /v1/analytics/teams")
    void shouldReturnAllTeamsAnalytics() throws Exception {
        TeamAnalyticsResponse cards = TeamAnalyticsResponse.builder()
                .team(TeamEnum.CREDIT_CARDS)
                .teamName("CREDIT_CARDS")
                .summary(TeamAnalyticsResponse.TeamSummaryDto.builder().totalTickets(5).build())
                .build();

        when(analyticsService.getAllTeamsAnalytics()).thenReturn(List.of(cards));

        mockMvc.perform(get("/v1/analytics/teams")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].team").value("CREDIT_CARDS"))
                .andExpect(jsonPath("$[0].summary.totalTickets").value(5));
    }

    @Test
    @DisplayName("AnalyticsController: Deve retornar 200 OK no endpoint /v1/analytics/teams/{team}")
    void shouldReturnSpecificTeamAnalytics() throws Exception {
        TeamAnalyticsResponse loans = TeamAnalyticsResponse.builder()
                .team(TeamEnum.LOANS)
                .teamName("LOANS")
                .summary(TeamAnalyticsResponse.TeamSummaryDto.builder()
                        .totalTickets(12)
                        .resolvedTickets(10)
                        .rejectedTickets(2)
                        .successRatePercent(83.33)
                        .build())
                .build();

        when(analyticsService.getTeamAnalytics(TeamEnum.LOANS)).thenReturn(loans);

        mockMvc.perform(get("/v1/analytics/teams/LOANS")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.team").value("LOANS"))
                .andExpect(jsonPath("$.summary.totalTickets").value(12))
                .andExpect(jsonPath("$.summary.resolvedTickets").value(10))
                .andExpect(jsonPath("$.summary.successRatePercent").value(83.33));
    }
}
