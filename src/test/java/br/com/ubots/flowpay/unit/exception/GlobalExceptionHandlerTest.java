package br.com.ubots.flowpay.unit.exception;

import br.com.ubots.flowpay.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/v1/tickets");
    }

    @Test
    @DisplayName("Deve tratar TicketConflictException com HTTP 409 Conflict")
    void shouldHandleTicketConflictException() {
        TicketConflictException ex = new TicketConflictException("Ticket já existe");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTicketConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Ticket já existe", response.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar falhas de concorrência OptimisticLockingFailureException com HTTP 409 Conflict")
    void shouldHandleConcurrencyFailure() {
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException("Lock fail");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConcurrencyFailure(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertTrue(response.getBody().message().contains("Concorrência detectada"));
    }

    @Test
    @DisplayName("Deve tratar QueueFullException com HTTP 422 Unprocessable Entity")
    void shouldHandleQueueFullException() {
        QueueFullException ex = new QueueFullException("Fila cheia");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleQueueFull(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(422, response.getBody().status());
        assertEquals("Fila cheia", response.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar TicketNotFoundException com HTTP 404 Not Found")
    void shouldHandleTicketNotFoundException() {
        TicketNotFoundException ex = new TicketNotFoundException("Ticket não encontrado");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTicketNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Ticket não encontrado", response.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar InvalidTicketStatusException com HTTP 422 Unprocessable Entity")
    void shouldHandleInvalidTicketStatusException() {
        InvalidTicketStatusException ex = new InvalidTicketStatusException("Status inválido");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidTicketStatus(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(422, response.getBody().status());
        assertEquals("Status inválido", response.getBody().message());
    }
}
