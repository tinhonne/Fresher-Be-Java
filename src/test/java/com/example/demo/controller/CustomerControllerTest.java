package com.example.demo.controller;

import com.example.demo.dto.request.CustomerSearchRequest;
import com.example.demo.dto.response.CustomerResponse;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.service.AccountService;
import com.example.demo.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {
    @Mock
    private CustomerService customerService;
    @Mock
    private AccountService accountService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CustomerController(customerService, accountService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReturnsCreatedAndValidationFailureDoesNotInvokeService() throws Exception {
        CustomerResponse response = new CustomerResponse();
        response.setName("An");
        when(customerService.createCustomer(any())).thenReturn(response);

        mockMvc.perform(post("/customers").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"An","birthday":"1990-01-01","address":"Ha Noi","identityNo":"1234567890",
                        "mobile":"0912345678","customerType":"INDIVIDUAL","status":1}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.name").value("An"));

        mockMvc.perform(post("/customers").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1001));
        verify(customerService).createCustomer(any());
    }

    @Test
    void listUsesPaginationDefaultsAndExplicitValues() throws Exception {
        when(customerService.getAllCustomerSortByName(0, 10)).thenReturn(page(0, 10));
        when(customerService.getAllCustomerSortByName(2, 25)).thenReturn(page(2, 25));

        mockMvc.perform(get("/customers")).andExpect(status().isOk());
        mockMvc.perform(get("/customers").param("page", "2").param("size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.pageSize").value(25));

        verify(customerService).getAllCustomerSortByName(0, 10);
        verify(customerService).getAllCustomerSortByName(2, 25);
    }

    @Test
    void canonicalSearchAndCompatibilityAliasBindCriteriaAndPagination() throws Exception {
        when(customerService.getCustomerSortByField(any(), org.mockito.ArgumentMatchers.eq(1),
                org.mockito.ArgumentMatchers.eq(20))).thenReturn(page(1, 20));

        mockMvc.perform(get("/customers/search").param("name", "An").param("page", "1").param("size", "20"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/customers/by-field").param("name", "An").param("page", "1").param("size", "20"))
                .andExpect(status().isOk());

        verify(customerService, org.mockito.Mockito.times(2)).getCustomerSortByField(
                org.mockito.ArgumentMatchers.argThat(request -> "An".equals(request.getName())),
                org.mockito.ArgumentMatchers.eq(1), org.mockito.ArgumentMatchers.eq(20));
    }

    @Test
    void searchValidationRejectsMalformedCriteriaWithoutInvokingService() throws Exception {
        mockMvc.perform(get("/customers/search").param("identityNo", "123x"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1001));
        mockMvc.perform(get("/customers/search").param("status", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1001));

        verify(customerService, never()).getCustomerSortByField(any(), any(Integer.class), any(Integer.class));
    }

    @Test
    void updateValidationUsesGlobalHandlerAndDoesNotInvokeService() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/customers/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"An","birthday":"2999-01-01","address":" ","mobile":"0912345678",
                                "customerType":"INDIVIDUAL","status":2}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1001));

        verify(customerService, never()).updateCustomerById(any(), any());
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/customers/7"))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomerById(7L);
    }

    @Test
    void malformedIdDoesNotInvokeService() throws Exception {
        mockMvc.perform(get("/customers/not-a-number"))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).getCustomerById(any());
    }

    private PageResponse<CustomerResponse> page(int page, int size) {
        return new PageResponse<>(List.of(), page, size, 0, 0, true);
    }
}
