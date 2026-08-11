package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.transaction.TransactionResponse;
import com.example.demo.entity.TransactionStatus;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.error.CommonError;
import com.example.demo.exception.mapper.ValidationErrorMapper;
import com.example.demo.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {
  @Mock private TransactionService transactionService;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new TransactionController(transactionService))
            .setControllerAdvice(new GlobalExceptionHandler(new ValidationErrorMapper()))
            .build();
    objectMapper = new ObjectMapper();
  }

  @Test
  void postTransferUsesExactRouteAndWrapsResponse() throws Exception {
    TransactionResponse response = transactionResponse();
    when(transactionService.transfer(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"fromAccountNumber":"1234567890123","toAccountNumber":"9876543210987","amount":1000.00,"content":"rent"}
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("SUCCESS"))
        .andExpect(jsonPath("$.message").value("Thành công"))
        .andExpect(jsonPath("$.result.fromAccountNumber").value("1"))
        .andExpect(jsonPath("$.result.toAccountNumber").value("2"))
        .andExpect(jsonPath("$.result.amount").value(1000.00));

    verify(transactionService)
        .transfer(
            org.mockito.ArgumentMatchers.argThat(
                request ->
                    "1234567890123".equals(request.getFromAccountNumber())
                        && "9876543210987".equals(request.getToAccountNumber())
                        && new BigDecimal("1000.00").compareTo(request.getAmount()) == 0));
    mockMvc
        .perform(
            post("/transaction/transfer").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void postTransferValidatesRequestAndWrapsValidationError() throws Exception {
    mockMvc
        .perform(
            post("/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{"
                        + "\"fromAccountNumber\":\"\",\"toAccountNumber\":\"2\",\"amount\":0"
                        + "}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
        .andExpect(jsonPath("$.message").value(CommonError.INVALID_INPUT.getMessage()))
        .andExpect(jsonPath("$.result.errors").isArray())
        .andExpect(jsonPath("$.result.errors.length()").value(4));

    verify(transactionService, never()).transfer(any());
  }

  @Test
  void getHistoryUsesExactRoutePassesDatesAndPaginationAndWrapsResponse() throws Exception {
    LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
    LocalDateTime to = LocalDateTime.of(2026, 1, 31, 23, 59, 59);
    PageResponse<TransactionResponse> page =
        new PageResponse<>(List.of(transactionResponse()), 2, 25, 51, 3, true);
    when(transactionService.getHistory("1", from, to, 2, 25)).thenReturn(page);

    mockMvc
        .perform(
            get("/accounts/1/transactions")
                .param("fromDate", "2026-01-01T00:00:00")
                .param("toDate", "2026-01-31T23:59:59")
                .param("page", "2")
                .param("size", "25"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("SUCCESS"))
        .andExpect(jsonPath("$.message").value("Thành công"))
        .andExpect(jsonPath("$.result.content[0].fromAccountNumber").value("1"))
        .andExpect(jsonPath("$.result.pageNumber").value(2))
        .andExpect(jsonPath("$.result.pageSize").value(25))
        .andExpect(jsonPath("$.result.totalElements").value(51))
        .andExpect(jsonPath("$.result.last").value(true));

    verify(transactionService).getHistory("1", from, to, 2, 25);
    mockMvc.perform(get("/accounts/1/transaction")).andExpect(status().isNotFound());
  }

  @Test
  void getHistoryUsesPaginationDefaults() throws Exception {
    when(transactionService.getHistory("1", null, null, 0, 10))
        .thenReturn(new PageResponse<>(List.of(), 0, 10, 0, 0, true));

    mockMvc
        .perform(get("/accounts/1/transactions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("SUCCESS"));

    verify(transactionService).getHistory("1", null, null, 0, 10);
  }

  private TransactionResponse transactionResponse() {
    return new TransactionResponse(
        1L,
        LocalDateTime.of(2026, 1, 2, 3, 4),
        "1",
        "2",
        new BigDecimal("1000.00"),
        TransactionStatus.SUCCESS,
        "rent",
        null);
  }
}
