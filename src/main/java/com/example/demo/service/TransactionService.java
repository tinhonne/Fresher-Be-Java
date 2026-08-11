package com.example.demo.service;

import com.example.demo.dto.request.transaction.TransferRequest;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.transaction.TransactionResponse;
import com.example.demo.exception.AppException;
import java.time.LocalDateTime;

public interface TransactionService {
  /**
   * Transfers funds between two active accounts.
   *
   * @param request the transfer details
   * @return the recorded transaction
   * @throws AppException if the amount is invalid ({@code INVALID_TRANSFER_AMOUNT}), the accounts
   *     are identical ({@code SAME_ACCOUNT_TRANSFER}), the source or destination does not exist
   *     ({@code SOURCE_ACCOUNT_NOT_FOUND} or {@code DESTINATION_ACCOUNT_NOT_FOUND}), or either
   *     account is inactive ({@code SOURCE_ACCOUNT_INACTIVE} or {@code
   *     DESTINATION_ACCOUNT_INACTIVE})
   */
  TransactionResponse transfer(TransferRequest request);

  /**
   * Returns transaction history for an account within an optional date range.
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
  PageResponse<TransactionResponse> getHistory(
      String accountNumber, LocalDateTime fromDate, LocalDateTime toDate, int page, int size);
}
