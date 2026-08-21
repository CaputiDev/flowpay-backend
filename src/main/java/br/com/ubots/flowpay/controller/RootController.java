package br.com.ubots.flowpay.controller;

import br.com.ubots.flowpay.controller.doc.RootControllerOpenApi;
import br.com.ubots.flowpay.dto.HealthCheckResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsável pelo endpoint raiz com healthcheck e link para documentação Swagger.
 */
@RestController
public class RootController implements RootControllerOpenApi {

    @Override
    @GetMapping("/")
    public ResponseEntity<HealthCheckResponse> root() {
        return ResponseEntity.ok(HealthCheckResponse.up());
    }
}
