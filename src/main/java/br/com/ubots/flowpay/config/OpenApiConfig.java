package br.com.ubots.flowpay.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração central da documentação OpenAPI 3 / Swagger UI do FlowPay MVP.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FlowPay MVP - Sistema de Roteamento de Atendimento")
                        .version("1.0.0")
                        .description("API REST para roteamento automático de solicitações de clientes entre equipes de atendimento "
                                + "(Cartões, Empréstimos e Outros Assuntos), controle inflexível de capacidade por atendente e "
                                + "gestão de filas FIFO com resiliência e tratamento de concorrência.")
                        .contact(new Contact()
                                .name("Thiago Caputi - Ubots")
                                .url("https://github.com/CaputiDev/flowpay-mvp"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
