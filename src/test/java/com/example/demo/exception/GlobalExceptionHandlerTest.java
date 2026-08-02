package com.example.demo.exception;

import com.example.demo.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void accountNumberUniqueViolationMapsToAccountNumberExisted() {
        DataIntegrityViolationException exception = violation(
                "Duplicate entry '123' for key 'accounts.UK_random'", "account_number");

        assertResponse(ErrorCode.ACCOUNT_NUMBER_EXISTED, handler.handlingDataIntegrityViolation(exception));
    }

    @Test
    void identityNumberUniqueViolationMapsToCustomerExisted() {
        DataIntegrityViolationException exception = violation(
                "Duplicate entry '123' for key 'customers.UK_random'", "identity_no");

        assertResponse(ErrorCode.CUSTOMER_EXISTED, handler.handlingDataIntegrityViolation(exception));
    }

    @Test
    void unknownIntegrityViolationMapsToInvalidInputWithoutDatabaseMessage() {
        String databaseMessage = "Duplicate entry 'secret' for key 'UK_random'";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "write failed", new SQLException(databaseMessage));

        ResponseEntity<ApiResponse<?>> response = handler.handlingDataIntegrityViolation(exception);

        assertResponse(ErrorCode.INVALID_INPUT, response);
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INVALID_INPUT.getMessage(), response.getBody().getMessage());
    }

    private DataIntegrityViolationException violation(String message, String column) {
        return new DataIntegrityViolationException("write failed", new SQLException(message + " (" + column + ")"));
    }

    private void assertResponse(ErrorCode expected, ResponseEntity<ApiResponse<?>> response) {
        assertEquals(expected.getHttpStatus(), response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expected.getCode(), response.getBody().getCode());
        assertEquals(expected.getMessage(), response.getBody().getMessage());
    }
}
