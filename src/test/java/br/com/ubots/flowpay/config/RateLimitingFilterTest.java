package br.com.ubots.flowpay.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RateLimitingFilterTest {

    private RateLimitProperties properties;
    private RateLimitingService rateLimitingService;
    private ObjectMapper objectMapper;
    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setCapacity(2);
        properties.setRefillTokens(2);
        properties.setRefillDurationSeconds(60);

        rateLimitingService = new RateLimitingService(properties);
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        filter = new RateLimitingFilter(rateLimitingService, properties, objectMapper);
    }

    @Test
    @DisplayName("Deve permitir requisições dentro da capacidade e retornar cabeçalho X-Rate-Limit-Remaining")
    void shouldAllowRequestsWithinCapacity() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/queues/status");
        request.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("X-Rate-Limit-Remaining")).isEqualTo("1");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve bloquear requisições com 429 Too Many Requests ao ultrapassar o limite")
    void shouldBlockRequestsWhenRateLimitExceeded() throws ServletException, IOException {
        String clientIp = "192.168.1.101";
        FilterChain filterChain = mock(FilterChain.class);

        // 1ª requisição permitida (resta 1)
        MockHttpServletRequest req1 = new MockHttpServletRequest("GET", "/v1/queues/status");
        req1.setRemoteAddr(clientIp);
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilter(req1, res1, filterChain);
        assertThat(res1.getStatus()).isEqualTo(200);

        // 2ª requisição permitida (resta 0)
        MockHttpServletRequest req2 = new MockHttpServletRequest("GET", "/v1/queues/status");
        req2.setRemoteAddr(clientIp);
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        filter.doFilter(req2, res2, filterChain);
        assertThat(res2.getStatus()).isEqualTo(200);

        // 3ª requisição bloqueada (excedeu)
        MockHttpServletRequest req3 = new MockHttpServletRequest("GET", "/v1/queues/status");
        req3.setRemoteAddr(clientIp);
        MockHttpServletResponse res3 = new MockHttpServletResponse();
        filter.doFilter(req3, res3, filterChain);

        assertThat(res3.getStatus()).isEqualTo(429);
        assertThat(res3.getHeader("Retry-After")).isNotNull();
        assertThat(res3.getHeader("X-Rate-Limit-Remaining")).isEqualTo("0");
        assertThat(res3.getContentAsString()).contains("Too Many Requests");
        verify(filterChain, times(2)).doFilter(any(), any());
    }

    @Test
    @DisplayName("Deve ignorar rotas de documentação OpenAPI / Swagger")
    void shouldBypassSwaggerEndpoints() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        request.setRemoteAddr("192.168.1.102");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("X-Rate-Limit-Remaining")).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve utilizar o header X-Forwarded-For para identificar IP real do cliente")
    void shouldUseXForwardedForHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/tickets");
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178");
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("X-Rate-Limit-Remaining")).isEqualTo("1");
    }

    @Test
    @DisplayName("Não deve filtrar quando rate limit estiver desabilitado")
    void shouldNotFilterWhenDisabled() throws ServletException, IOException {
        properties.setEnabled(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/queues/status");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("X-Rate-Limit-Remaining")).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
