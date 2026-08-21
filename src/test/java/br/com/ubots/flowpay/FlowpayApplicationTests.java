package br.com.ubots.flowpay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class FlowpayApplicationTests {

	@Test
	@DisplayName("Deve carregar o contexto e garantir que o fuso horário padrão é America/Sao_Paulo (UTC-3)")
	void contextLoadsAndDefaultTimeZoneIsBrasilia() {
		assertEquals("America/Sao_Paulo", TimeZone.getDefault().getID());
	}

}
