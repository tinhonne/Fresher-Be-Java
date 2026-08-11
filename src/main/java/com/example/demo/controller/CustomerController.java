package com.example.demo.controller;

import com.example.demo.constant.OpenApiConstants;
import com.example.demo.dto.request.customer.CustomerCreateRequest;
import com.example.demo.dto.request.customer.CustomerSearchRequest;
import com.example.demo.dto.request.customer.CustomerStatusUpdateRequest;
import com.example.demo.dto.request.customer.CustomerUpdateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.account.AccountResponse;
import com.example.demo.dto.response.customer.CustomerResponse;
import com.example.demo.exception.AppException;
import com.example.demo.service.AccountService;
import com.example.demo.service.CustomerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/customers")
@SecurityRequirement(name = OpenApiConstants.SECURITY_SCHEME_NAME)
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerService customerService;
  private final AccountService accountService;

  /**
   * Creates a customer.
   *
   * @param request the validated customer creation request
   * @return the created customer
   * @throws AppException if the customer exists or the requested status is invalid
   */
  @PostMapping
  public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
      @Valid @RequestBody CustomerCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(customerService.createCustomer(request)));
  }

  /**
   * Returns a customer by identifier.
   *
   * @param id the customer identifier
   * @return the customer details
   * @throws AppException if the customer does not exist
   */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
      @Positive @PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerById(id)));
  }

  /**
   * Updates a customer by identifier.
   *
   * @param id the customer identifier
   * @param request the validated customer update request
   * @return the updated customer
   * @throws AppException if the customer does not exist or the status transition is invalid
   */
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomerById(
      @Positive @PathVariable Long id, @Valid @RequestBody CustomerUpdateRequest request) {
    return ResponseEntity.ok(ApiResponse.success(customerService.updateCustomerById(id, request)));
  }

  /**
   * Deactivates a customer.
   *
   * @param id the customer identifier
   * @return an empty response
   * @throws AppException if the customer does not exist or has a blocking account
   */
  @PutMapping("/{id}/status")
  public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomerStatus(
      @Positive @PathVariable Long id, @Valid @RequestBody CustomerStatusUpdateRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(customerService.updateCustomerStatus(id, request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteCustomerById(@Positive @PathVariable Long id) {
    customerService.deleteCustomerById(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Returns customers ordered by name.
   *
   * @param page the zero-based page index
   * @param size the page size
   * @return the requested page of customers
   * @throws AppException if pagination is invalid
   */
  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> getAllCustomerSortByName(
      @PositiveOrZero @RequestParam(defaultValue = "0") int page,
      @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
        ApiResponse.success(customerService.getAllCustomerSortByName(page, size)));
  }

  /**
   * Searches customers through the canonical and compatibility routes.
   *
   * @param request the validated customer search criteria
   * @param page the zero-based page index
   * @param size the page size
   * @return the requested page of matching customers
   * @throws AppException if the criteria or pagination is invalid
   */
  @GetMapping({"/search", "/by-field"})
  public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> getCustomerSortByField(
      @Valid @ModelAttribute CustomerSearchRequest request,
      @PositiveOrZero @RequestParam(defaultValue = "0") int page,
      @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
        ApiResponse.success(customerService.getCustomerSortByField(request, page, size)));
  }

  /**
   * Returns all accounts owned by a customer.
   *
   * @param id the customer identifier
   * @param page the zero-based page index
   * @param size the page size
   * @return the requested page of accounts
   * @throws AppException if the customer does not exist or pagination is invalid
   */
  @GetMapping("/{id}/accounts")
  public ResponseEntity<ApiResponse<PageResponse<AccountResponse>>> getCustomerAccounts(
      @Positive @PathVariable Long id,
      @PositiveOrZero @RequestParam(defaultValue = "0") int page,
      @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
        ApiResponse.success(accountService.getAccountsByCustomerId(id, page, size)));
  }

  /**
   * Returns a customer's active accounts.
   *
   * @param id the customer identifier
   * @param page the zero-based page index
   * @param size the page size
   * @return the requested page of active accounts
   * @throws AppException if the customer does not exist or pagination is invalid
   */
  @GetMapping("/{id}/accounts/active")
  public ResponseEntity<ApiResponse<PageResponse<AccountResponse>>> getActiveCustomerAccounts(
      @Positive @PathVariable Long id,
      @PositiveOrZero @RequestParam(defaultValue = "0") int page,
      @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
        ApiResponse.success(accountService.getActiveAccountByCustomerId(id, page, size)));
  }

  /**
   * Returns a customer's inactive accounts.
   *
   * @param id the customer identifier
   * @param page the zero-based page index
   * @param size the page size
   * @return the requested page of inactive accounts
   * @throws AppException if the customer does not exist or pagination is invalid
   */
  @GetMapping("/{id}/accounts/inactive")
  public ResponseEntity<ApiResponse<PageResponse<AccountResponse>>> getInactiveCustomerAccounts(
      @Positive @PathVariable Long id,
      @PositiveOrZero @RequestParam(defaultValue = "0") int page,
      @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
        ApiResponse.success(accountService.getInactiveAccountsByCustomerId(id, page, size)));
  }
}
