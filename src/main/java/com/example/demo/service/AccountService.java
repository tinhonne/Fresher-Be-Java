package com.example.demo.service;

import com.example.demo.dto.request.account.AccountRequest;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.account.AccountResponse;
import com.example.demo.exception.AppException;

public interface AccountService {
  /**
   * Creates an account for a customer.
   *
   * @param accountRequest the account creation request
   * @return the created account
   * @throws AppException if the customer does not exist ({@code CUSTOMER_NOT_FOUND}) or the account
   *     number already exists ({@code ACCOUNT_NUMBER_EXISTED})
   */
  AccountResponse createAccount(AccountRequest accountRequest);

  /**
   * Returns an account by identifier.
   *
   * @param id the account identifier
   * @return the account details
   * @throws AppException if the account does not exist ({@code ACCOUNT_NOT_FOUND})
   */
  AccountResponse getAccountById(Long id);

  /**
   * Returns an account by account number.
   *
   * @param accountNumber the account number
   * @return the account details
   * @throws AppException if the account does not exist ({@code ACCOUNT_NOT_FOUND})
   */
  AccountResponse getAccountByAccountNumber(String accountNumber);

  /**
   * Returns a page of accounts ordered by customer name.
   *
   * @param page the zero-based page index
   * @param size the page size
   * @return the requested page of accounts
   */
  PageResponse<AccountResponse> getAccountSortByNameCustomer(int page, int size);

  /**
   * Returns a customer's active accounts.
   *
   * @param id the customer identifier
   * @param page the zero-based page index
   * @param size the page size
   * @return the requested page of active accounts
   */
  PageResponse<AccountResponse> getActiveAccountByCustomerId(Long id, int page, int size);

  PageResponse<AccountResponse> getAccountsByCustomerId(Long customerId, int page, int size);

  PageResponse<AccountResponse> getInactiveAccountsByCustomerId(
      Long customerId, int page, int size);

  AccountResponse approveAccount(Long id);

  AccountResponse rejectAccount(Long id);

  AccountResponse freezeAccount(Long id);

  AccountResponse unfreezeAccount(Long id);

  AccountResponse closeAccount(Long id);
}
