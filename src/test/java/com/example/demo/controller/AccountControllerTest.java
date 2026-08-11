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
import com.example.demo.dto.response.account.AccountResponse;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.mapper.ValidationErrorMapper;
import com.example.demo.service.AccountService;
import com.example.demo.service.CustomerService;
import java.math.BigDecimal;
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
class AccountControllerTest {
  @Mock private AccountService accountService;
  @Mock private CustomerService customerService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new AccountController(accountService),
                new CustomerController(customerService, accountService))
            .setControllerAdvice(new GlobalExceptionHandler(new ValidationErrorMapper()))
            .build();
  }

  @Test
  void createReturnsCreatedAndValidatesInput() throws Exception {
    AccountResponse response = response("1234567890123");
    when(accountService.createAccount(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"accountNumber":"1234567890123","customerId":7,"balance":0}
                                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.result.accountNumber").value("1234567890123"));

    mockMvc
        .perform(
            post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountNumber\":\"\",\"balance\":-1}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    verify(accountService).createAccount(any());
  }

  @Test
  void canonicalAndCompatibilityNumberRoutesResolve() throws Exception {
    when(accountService.getAccountByAccountNumber("1234567890123"))
        .thenReturn(response("1234567890123"));

    mockMvc.perform(get("/accounts/number/1234567890123")).andExpect(status().isOk());
    mockMvc.perform(get("/accounts/by-number/1234567890123")).andExpect(status().isOk());
  }

  @Test
  void listUsesDefaultsAndExplicitPagination() throws Exception {
    when(accountService.getAccountSortByNameCustomer(0, 10)).thenReturn(page(0, 10));
    when(accountService.getAccountSortByNameCustomer(2, 25)).thenReturn(page(2, 25));

    mockMvc.perform(get("/accounts")).andExpect(status().isOk());
    mockMvc
        .perform(get("/accounts").param("page", "2").param("size", "25"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result.pageNumber").value(2));

    verify(accountService).getAccountSortByNameCustomer(0, 10);
    verify(accountService).getAccountSortByNameCustomer(2, 25);
  }

  @Test
  void canonicalCustomerActiveRouteAndCompatibilityRouteResolve() throws Exception {
    when(accountService.getActiveAccountByCustomerId(7L, 0, 10)).thenReturn(page(0, 10));

    mockMvc.perform(get("/customers/7/accounts/active")).andExpect(status().isOk());
    mockMvc.perform(get("/accounts/7/active").param("size", "10")).andExpect(status().isOk());
  }

  @Test
  void malformedIdDoesNotInvokeService() throws Exception {
    mockMvc.perform(get("/accounts/not-a-number")).andExpect(status().isBadRequest());

    verify(accountService, never()).getAccountById(any());
  }

  private AccountResponse response(String number) {
    AccountResponse response = new AccountResponse();
    response.setAccountNumber(number);
    response.setBalance(BigDecimal.ZERO);
    response.setStatus(3);
    return response;
  }

  private PageResponse<AccountResponse> page(int page, int size) {
    return new PageResponse<>(List.of(), page, size, 0, 0, true);
  }
}
