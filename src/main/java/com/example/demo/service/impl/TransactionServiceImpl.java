package com.example.demo.service.impl;

import static com.example.demo.constant.TransactionConstants.*;

import com.example.demo.dto.request.transaction.TransferRequest;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.transaction.TransactionResponse;
import com.example.demo.entity.Account;
import com.example.demo.entity.AccountStatus;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.TransactionStatus;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorDefinition;
import com.example.demo.exception.error.AccountError;
import com.example.demo.exception.error.CommonError;
import com.example.demo.exception.error.TransactionError;
import com.example.demo.mapper.TransactionMapping;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.service.TransactionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
  private static final BigDecimal MINIMUM_TRANSFER_AMOUNT = new BigDecimal(MINIMUM_AMOUNT);

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final TransactionMapping transactionMapping;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public TransactionResponse transfer(TransferRequest request) {
    validateAmount(request);
    String sourceNumber = request.getFromAccountNumber();
    String destinationNumber = request.getToAccountNumber();
    if (sourceNumber.equals(destinationNumber)) {
      throw new AppException(TransactionError.SAME_ACCOUNT_TRANSFER);
    }

    boolean sourceFirst = sourceNumber.compareTo(destinationNumber) < 0;
    String firstNumber = sourceFirst ? sourceNumber : destinationNumber;
    String secondNumber = sourceFirst ? destinationNumber : sourceNumber;
    ErrorDefinition firstNotFound =
        sourceFirst
            ? TransactionError.SOURCE_ACCOUNT_NOT_FOUND
            : TransactionError.DESTINATION_ACCOUNT_NOT_FOUND;
    ErrorDefinition secondNotFound =
        sourceFirst
            ? TransactionError.DESTINATION_ACCOUNT_NOT_FOUND
            : TransactionError.SOURCE_ACCOUNT_NOT_FOUND;
    Account first = lockAccount(firstNumber, firstNotFound);
    Account second = lockAccount(secondNumber, secondNotFound);
    Account source = sourceFirst ? first : second;
    Account destination = sourceFirst ? second : first;
    requireActive(source, TransactionError.SOURCE_ACCOUNT_INACTIVE);
    requireActive(destination, TransactionError.DESTINATION_ACCOUNT_INACTIVE);

    Transaction transaction = new Transaction();
    transaction.setTransactionDate(LocalDateTime.now());
    transaction.setFromAccount(source);
    transaction.setToAccount(destination);
    transaction.setAmount(request.getAmount());
    transaction.setContent(request.getContent());

    if (source.getBalance().compareTo(request.getAmount()) < 0) {
      transaction.setStatus(TransactionStatus.INSUFFICIENT_BALANCE);
      transaction.setErrorReason(INSUFFICIENT_BALANCE_REASON);
      return transactionMapping.toResponse(transactionRepository.save(transaction));
    }

    source.setBalance(source.getBalance().subtract(request.getAmount()));
    destination.setBalance(destination.getBalance().add(request.getAmount()));
    accountRepository.save(source);
    accountRepository.save(destination);
    transaction.setStatus(TransactionStatus.SUCCESS);
    return transactionMapping.toResponse(transactionRepository.save(transaction));
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public PageResponse<TransactionResponse> getHistory(
      String accountNumber, LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {
    if (!accountRepository.existsByAccountNumber(accountNumber)) {
      throw new AppException(AccountError.ACCOUNT_NOT_FOUND);
    }
    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
      throw new AppException(TransactionError.INVALID_TRANSACTION_DATE_RANGE);
    }
    if (page < 0 || size < MINIMUM_PAGE_SIZE || size > MAXIMUM_PAGE_SIZE) {
      throw new AppException(CommonError.INVALID_PAGE_REQUEST);
    }
    PageRequest pageable =
        PageRequest.of(
            page,
            size,
            Sort.by(Sort.Order.desc(TRANSACTION_DATE_PROPERTY), Sort.Order.desc(ID_PROPERTY)));
    Page<TransactionResponse> history =
        transactionRepository
            .findHistory(accountNumber, fromDate, toDate, pageable)
            .map(transactionMapping::toResponse);
    return PageResponse.from(history);
  }

  private void validateAmount(TransferRequest request) {
    if (request.getAmount() == null || request.getAmount().compareTo(MINIMUM_TRANSFER_AMOUNT) < 0) {
      throw new AppException(TransactionError.INVALID_TRANSFER_AMOUNT);
    }
  }

  private Account lockAccount(String accountNumber, ErrorDefinition errorCode) {
    return accountRepository
        .findByAccountNumberForUpdate(accountNumber)
        .orElseThrow(() -> new AppException(errorCode));
  }

  private void requireActive(Account account, ErrorDefinition errorCode) {
    if (account.getStatus() != AccountStatus.ACTIVE) {
      throw new AppException(errorCode);
    }
  }
}
