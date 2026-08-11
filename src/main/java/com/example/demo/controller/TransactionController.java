package com.example.demo.controller;

import static com.example.demo.constant.ValidationConstants.ACCOUNT_NUMBER_PATTERN;

import com.example.demo.constant.OpenApiConstants;
import com.example.demo.dto.request.transaction.TransferRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.transaction.TransactionResponse;
import com.example.demo.exception.AppException;
import com.example.demo.service.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConstants.SECURITY_SCHEME_NAME)
public class TransactionController {
  private final TransactionService transactionService;

  /**
   * Transfers funds between accounts.
   *
   * @param request the transfer details
   * @return the recorded transaction
   * @throws AppException if the amount is invalid ({@code INVALID_TRANSFER_AMOUNT}), the accounts
   *     are identical ({@code SAME_ACCOUNT_TRANSFER}), an account does not exist ({@code
   *     SOURCE_ACCOUNT_NOT_FOUND} or {@code DESTINATION_ACCOUNT_NOT_FOUND}), or an account is
   *     inactive ({@code SOURCE_ACCOUNT_INACTIVE} or {@code DESTINATION_ACCOUNT_INACTIVE})
   */
  @PostMapping("/transactions/transfer")
  public ApiResponse<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
    return ApiResponse.success(transactionService.transfer(request));
  }

  /**
   * Returns an account's transaction history.
   *
   * @param accountNumber the account number
   * @param fromDate the optional inclusive start date
   * @param toDate the optional inclusive end date
   * @param page the zero-based page index
   * @param size the page size
   * @return the requested page of transactions
   * @throws AppException if the account does not exist ({@code ACCOUNT_NOT_FOUND}), the date range
   *     is invalid ({@code INVALID_TRANSACTION_DATE_RANGE}), or pagination is invalid ({@code
   *     INVALID_PAGE_REQUEST})
   */
  @GetMapping("/accounts/{accountNumber}/transactions")
  public ApiResponse<PageResponse<TransactionResponse>> getHistory(
      @Pattern(regexp = ACCOUNT_NUMBER_PATTERN) @PathVariable String accountNumber,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime toDate,
      @PositiveOrZero @RequestParam(defaultValue = "0") int page,
      @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {
    return ApiResponse.success(
        transactionService.getHistory(accountNumber, fromDate, toDate, page, size));
  }
}
