package com.example.demo.controller;

import static com.example.demo.constant.ValidationConstants.ACCOUNT_NUMBER_PATTERN;

import com.example.demo.dto.request.AccountRequest;
import com.example.demo.dto.response.AccountResponse;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.exception.AppException;
import com.example.demo.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Creates an account.
     *
     * @param accountRequest the validated account creation request
     * @return the created account
     * @throws AppException if the customer does not exist, is inactive, the account number exists, or the balance is invalid
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody AccountRequest accountRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(accountService.createAccount(accountRequest)));
    }

    /**
     * Returns an account by identifier.
     *
     * @param id the account identifier
     * @return the account details
     * @throws AppException if the account does not exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@Positive @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAccountById(id)));
    }

    /**
     * Returns an account by account number through the canonical route.
     *
     * @param accountNumber the account number
     * @return the account details
     * @throws AppException if the account does not exist
     */
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(@Pattern(regexp = ACCOUNT_NUMBER_PATTERN) @PathVariable String accountNumber) {
        return accountByNumber(accountNumber);
    }

    /**
     * Returns an account by account number through the compatibility route.
     *
     * @param accountNumber the account number
     * @return the account details
     * @throws AppException if the account does not exist
     */
    @GetMapping("/by-number/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByAccountNumber(
            @Pattern(regexp = ACCOUNT_NUMBER_PATTERN) @PathVariable String accountNumber) {
        return accountByNumber(accountNumber);
    }

    /**
     * Returns accounts ordered by customer name.
     *
     * @param page the zero-based page index
     * @param size the page size
     * @return the requested page of accounts
     * @throws AppException if pagination is invalid
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AccountResponse>>> getAccountSortByNameCustomer(
            @PositiveOrZero @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAccountSortByNameCustomer(page, size)));
    }

    /**
     * Returns a customer's active accounts through the compatibility route.
     *
     * @param id the customer identifier
     * @param page the zero-based page index
     * @param size the page size
     * @return the requested page of active accounts
     * @throws AppException if the customer does not exist or pagination is invalid
     */
    @GetMapping("/{id}/active")
    public ResponseEntity<ApiResponse<PageResponse<AccountResponse>>> getActiveAccountByCustomerId(
            @Positive @PathVariable Long id,
            @PositiveOrZero @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "1") int size) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getActiveAccountByCustomerId(id, page, size)));
    }

    /**
     * Approves a pending account.
     *
     * @param id the account identifier
     * @return the approved account
     * @throws AppException if the account does not exist or is not pending
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<AccountResponse>> approveAccount(@Positive @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.approveAccount(id)));
    }

    /**
     * Rejects a pending account.
     *
     * @param id the account identifier
     * @return the rejected account
     * @throws AppException if the account does not exist or is not pending
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<AccountResponse>> rejectAccount(@Positive @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.rejectAccount(id)));
    }

    /**
     * Freezes an active account.
     *
     * @param id the account identifier
     * @return the frozen account
     * @throws AppException if the account does not exist or is not active
     */
    @PutMapping("/{id}/freeze")
    public ResponseEntity<ApiResponse<AccountResponse>> freezeAccount(@Positive @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.freezeAccount(id)));
    }

    /**
     * Unfreezes a frozen account.
     *
     * @param id the account identifier
     * @return the active account
     * @throws AppException if the account does not exist or is not frozen
     */
    @PutMapping("/{id}/unfreeze")
    public ResponseEntity<ApiResponse<AccountResponse>> unfreezeAccount(@Positive @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.unfreezeAccount(id)));
    }

    /**
     * Closes an active account with a zero balance.
     *
     * @param id the account identifier
     * @return the closed account
     * @throws AppException if the account does not exist, is not active, or has a non-zero balance
     */
    @PutMapping("/{id}/close")
    public ResponseEntity<ApiResponse<AccountResponse>> closeAccount(@Positive @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.closeAccount(id)));
    }

    private ResponseEntity<ApiResponse<AccountResponse>> accountByNumber(String accountNumber) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAccountByAccountNumber(accountNumber)));
    }
}
