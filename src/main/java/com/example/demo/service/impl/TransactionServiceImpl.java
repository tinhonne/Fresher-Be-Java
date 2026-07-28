package com.example.demo.service.impl;

import com.example.demo.dto.request.TransferRequest;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.TransactionResponse;
import com.example.demo.entity.Account;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.TransactionStatus;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.TransactionMapping;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private static final BigDecimal MINIMUM_AMOUNT = new BigDecimal("1000");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapping transactionMapping;

    @PreAuthorize("hasAuthority('TRANSACTION_CREATE')")
    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        validateAmount(request);
        String sourceNumber = request.getFromAccountNumber();
        String destinationNumber = request.getToAccountNumber();
        if (sourceNumber.equals(destinationNumber)) {
            throw new AppException(ErrorCode.SAME_ACCOUNT_TRANSFER);
        }

        boolean sourceFirst = sourceNumber.compareTo(destinationNumber) < 0;
        String firstNumber = sourceFirst ? sourceNumber : destinationNumber;
        String secondNumber = sourceFirst ? destinationNumber : sourceNumber;
        ErrorCode firstNotFound = sourceFirst
                ? ErrorCode.SOURCE_ACCOUNT_NOT_FOUND : ErrorCode.DESTINATION_ACCOUNT_NOT_FOUND;
        ErrorCode secondNotFound = sourceFirst
                ? ErrorCode.DESTINATION_ACCOUNT_NOT_FOUND : ErrorCode.SOURCE_ACCOUNT_NOT_FOUND;
        Account first = lockAccount(firstNumber, firstNotFound);
        Account second = lockAccount(secondNumber, secondNotFound);
        Account source = sourceFirst ? first : second;
        Account destination = sourceFirst ? second : first;
        requireActive(source, ErrorCode.SOURCE_ACCOUNT_INACTIVE);
        requireActive(destination, ErrorCode.DESTINATION_ACCOUNT_INACTIVE);

        Transaction transaction = new Transaction();
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setFromAccount(source);
        transaction.setToAccount(destination);
        transaction.setAmount(request.getAmount());
        transaction.setContent(request.getContent());

        if (source.getBalance().compareTo(request.getAmount()) < 0) {
            transaction.setStatus(TransactionStatus.INSUFFICIENT_BALANCE);
            transaction.setErrorReason("Insufficient balance");
            return transactionMapping.toResponse(transactionRepository.save(transaction));
        }

        source.setBalance(source.getBalance().subtract(request.getAmount()));
        destination.setBalance(destination.getBalance().add(request.getAmount()));
        accountRepository.save(source);
        accountRepository.save(destination);
        transaction.setStatus(TransactionStatus.SUCCESS);
        return transactionMapping.toResponse(transactionRepository.save(transaction));
    }

    @PreAuthorize("hasAuthority('TRANSACTION_VIEW')")
    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getHistory(String accountNumber, LocalDateTime fromDate,
                                                         LocalDateTime toDate, int page, int size) {
        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new AppException(ErrorCode.INVALID_TRANSACTION_DATE_RANGE);
        }
        if (page < 0 || size < 1 || size > 100) {
            throw new AppException(ErrorCode.INVALID_PAGE_REQUEST);
        }
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        Page<TransactionResponse> history = transactionRepository
                .findHistory(accountNumber, fromDate, toDate, pageable)
                .map(transactionMapping::toResponse);
        return PageResponse.from(history);
    }

    private void validateAmount(TransferRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(MINIMUM_AMOUNT) < 0) {
            throw new AppException(ErrorCode.INVALID_TRANSFER_AMOUNT);
        }
    }

    private Account lockAccount(String accountNumber, ErrorCode errorCode) {
        return accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new AppException(errorCode));
    }

    private void requireActive(Account account, ErrorCode errorCode) {
        if (!Integer.valueOf(1).equals(account.getStatus())) {
            throw new AppException(errorCode);
        }
    }
}
