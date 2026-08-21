package br.com.ubots.flowpay.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de registro explícito do RateLimitingFilter na cadeia de filtros Servlet.
 */
@Configuration
public class RateLimitingConfiguration {

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration(RateLimitingService rateLimitingService,
                                                                                     RateLimitProperties properties,
                                                                                     ObjectMapper objectMapper) {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitingFilter(rateLimitingService, properties, objectMapper));
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
