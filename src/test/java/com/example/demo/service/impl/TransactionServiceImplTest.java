package com.example.demo.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.demo.dto.request.transaction.TransferRequest;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.transaction.TransactionResponse;
import com.example.demo.entity.Account;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.TransactionStatus;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorDefinition;
import com.example.demo.exception.error.CommonError;
import com.example.demo.exception.error.TransactionError;
import com.example.demo.mapper.TransactionMapping;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
  @Mock private AccountRepository accountRepository;
  @Mock private TransactionRepository transactionRepository;
  @Mock private TransactionMapping transactionMapping;

  private TransactionServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new TransactionServiceImpl(accountRepository, transactionRepository, transactionMapping);
  }

  @Test
  void missingSourceMapsErrorWhenSourceSortsFirst() {
    when(accountRepository.findByAccountNumberForUpdate("1")).thenReturn(Optional.empty());

    assertError(
        TransactionError.SOURCE_ACCOUNT_NOT_FOUND,
        () -> service.transfer(request("1", "2", "1000.00")));

    verify(accountRepository).findByAccountNumberForUpdate("1");
    verify(accountRepository, never()).findByAccountNumberForUpdate("2");
    verifyNoInteractions(transactionRepository);
  }

  @Test
  void missingSourceMapsErrorWhenSourceSortsSecond() {
    when(accountRepository.findByAccountNumberForUpdate("1"))
        .thenReturn(Optional.of(account("1", "2000.00", 1)));
    when(accountRepository.findByAccountNumberForUpdate("2")).thenReturn(Optional.empty());

    assertError(
        TransactionError.SOURCE_ACCOUNT_NOT_FOUND,
        () -> service.transfer(request("2", "1", "1000.00")));

    InOrder order = inOrder(accountRepository);
    order.verify(accountRepository).findByAccountNumberForUpdate("1");
    order.verify(accountRepository).findByAccountNumberForUpdate("2");
    verifyNoInteractions(transactionRepository);
  }

  @Test
  void missingDestinationMapsErrorWhenDestinationSortsFirst() {
    when(accountRepository.findByAccountNumberForUpdate("1")).thenReturn(Optional.empty());

    assertError(
        TransactionError.DESTINATION_ACCOUNT_NOT_FOUND,
        () -> service.transfer(request("2", "1", "1000.00")));

    verify(accountRepository).findByAccountNumberForUpdate("1");
    verify(accountRepository, never()).findByAccountNumberForUpdate("2");
    verifyNoInteractions(transactionRepository);
  }

  @Test
  void inactiveSourceIsCheckedOnLockedValue() {
    when(accountRepository.findByAccountNumberForUpdate("1"))
        .thenReturn(Optional.of(account("1", "2000.00", 0)));
    when(accountRepository.findByAccountNumberForUpdate("2"))
        .thenReturn(Optional.of(account("2", "700.00", 1)));

    assertError(
        TransactionError.SOURCE_ACCOUNT_INACTIVE,
        () -> service.transfer(request("1", "2", "1000.00")));

    verifyNoInteractions(transactionRepository);
  }

  @Test
  void inactiveDestinationIsCheckedOnLockedValue() {
    when(accountRepository.findByAccountNumberForUpdate("1"))
        .thenReturn(Optional.of(account("1", "2000.00", 1)));
    when(accountRepository.findByAccountNumberForUpdate("2"))
        .thenReturn(Optional.of(account("2", "700.00", 0)));

    assertError(
        TransactionError.DESTINATION_ACCOUNT_INACTIVE,
        () -> service.transfer(request("1", "2", "1000.00")));

    verifyNoInteractions(transactionRepository);
  }

  @Test
  void sameAccountFailsWithoutRepositoryQuery() {
    assertError(
        TransactionError.SAME_ACCOUNT_TRANSFER,
        () -> service.transfer(request("1", "1", "1000.00")));

    verifyNoInteractions(accountRepository, transactionRepository, transactionMapping);
  }

  @Test
  void insufficientBalanceSavesFailedTransactionWithoutSavingBalances() {
    Account source = account("1", "500.00", 1);
    Account destination = account("2", "700.00", 1);
    TransactionResponse expected = new TransactionResponse();
    mockLocks(source, destination);
    when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(transactionMapping.toResponse(any())).thenReturn(expected);

    TransactionResponse actual = service.transfer(request("1", "2", "1000.00"));

    assertSame(expected, actual);
    assertEquals(new BigDecimal("500.00"), source.getBalance());
    assertEquals(new BigDecimal("700.00"), destination.getBalance());
    verify(transactionRepository)
        .save(
            org.mockito.ArgumentMatchers.argThat(
                transaction ->
                    transaction.getStatus() == TransactionStatus.INSUFFICIENT_BALANCE
                        && "Insufficient balance".equals(transaction.getErrorReason())));
    verify(accountRepository, never()).save(any());
  }

  @Test
  void successfulTransferLocksEachAccountOnceInAscendingOrderAndUpdatesBalances() {
    Account source = account("2", "2000.00", 1);
    Account destination = account("1", "700.00", 1);
    TransactionResponse expected = new TransactionResponse();
    mockLocks(source, destination);
    when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(transactionMapping.toResponse(any())).thenReturn(expected);

    TransactionResponse actual = service.transfer(request("2", "1", "1000.00"));

    assertSame(expected, actual);
    assertEquals(new BigDecimal("1000.00"), source.getBalance());
    assertEquals(new BigDecimal("1700.00"), destination.getBalance());
    InOrder order = inOrder(accountRepository);
    order.verify(accountRepository, times(1)).findByAccountNumberForUpdate("1");
    order.verify(accountRepository, times(1)).findByAccountNumberForUpdate("2");
    verify(accountRepository).save(source);
    verify(accountRepository).save(destination);
    verify(transactionRepository)
        .save(
            org.mockito.ArgumentMatchers.argThat(
                transaction ->
                    transaction.getStatus() == TransactionStatus.SUCCESS
                        && transaction.getFromAccount() == source
                        && transaction.getToAccount() == destination
                        && transaction.getTransactionDate() != null));
  }

  @Test
  void historyMapsSentAndReceivedTransactionsFromRepositoryPage() {
    Transaction sent = transaction("1", "2", "10.00");
    Transaction received = transaction("3", "1", "4.00");
    TransactionResponse sentResponse = response("1", "2", "10.00");
    TransactionResponse receivedResponse = response("3", "1", "4.00");
    when(accountRepository.existsByAccountNumber("1")).thenReturn(true);
    when(transactionRepository.findHistory(
            org.mockito.ArgumentMatchers.eq("1"), any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of(sent, received)));
    when(transactionMapping.toResponse(sent)).thenReturn(sentResponse);
    when(transactionMapping.toResponse(received)).thenReturn(receivedResponse);

    PageResponse<TransactionResponse> result = service.getHistory("1", null, null, 0, 10);

    assertEquals(List.of(sentResponse, receivedResponse), result.getContent());
    verify(transactionMapping).toResponse(sent);
    verify(transactionMapping).toResponse(received);
  }

  @Test
  void historyPassesInclusiveDateBoundsPaginationAndDescendingOrdering() {
    LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
    LocalDateTime to = LocalDateTime.of(2026, 1, 31, 23, 59, 59);
    when(accountRepository.existsByAccountNumber("1")).thenReturn(true);
    when(transactionRepository.findHistory(
            org.mockito.ArgumentMatchers.eq("1"),
            org.mockito.ArgumentMatchers.eq(from),
            org.mockito.ArgumentMatchers.eq(to),
            any()))
        .thenReturn(new PageImpl<>(List.of()));

    service.getHistory("1", from, to, 2, 25);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(transactionRepository)
        .findHistory(
            org.mockito.ArgumentMatchers.eq("1"),
            org.mockito.ArgumentMatchers.eq(from),
            org.mockito.ArgumentMatchers.eq(to),
            captor.capture());
    Pageable pageable = captor.getValue();
    assertEquals(2, pageable.getPageNumber());
    assertEquals(25, pageable.getPageSize());
    assertEquals(
        Sort.Direction.DESC, pageable.getSort().getOrderFor("transactionDate").getDirection());
    assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("id").getDirection());
  }

  @Test
  void invalidDateRangeDoesNotQueryHistory() {
    when(accountRepository.existsByAccountNumber("1")).thenReturn(true);
    LocalDateTime from = LocalDateTime.of(2026, 2, 1, 0, 0);
    LocalDateTime to = from.minusNanos(1);

    assertError(
        TransactionError.INVALID_TRANSACTION_DATE_RANGE,
        () -> service.getHistory("1", from, to, 0, 10));

    verifyNoInteractions(transactionRepository);
  }

  @Test
  void invalidPaginationDoesNotQueryHistory() {
    when(accountRepository.existsByAccountNumber("1")).thenReturn(true);

    assertError(
        CommonError.INVALID_PAGE_REQUEST, () -> service.getHistory("1", null, null, -1, 10));
    assertError(CommonError.INVALID_PAGE_REQUEST, () -> service.getHistory("1", null, null, 0, 0));
    assertError(
        CommonError.INVALID_PAGE_REQUEST, () -> service.getHistory("1", null, null, 0, 101));

    verifyNoInteractions(transactionRepository);
  }

  private void assertError(ErrorDefinition expected, Runnable invocation) {
    AppException exception = assertThrows(AppException.class, invocation::run);
    assertEquals(expected, exception.getErrorCode());
  }

  private void mockLocks(Account source, Account destination) {
    when(accountRepository.findByAccountNumberForUpdate(source.getAccountNumber()))
        .thenReturn(Optional.of(source));
    when(accountRepository.findByAccountNumberForUpdate(destination.getAccountNumber()))
        .thenReturn(Optional.of(destination));
  }

  private Account account(String number, String balance, int status) {
    Account account = new Account();
    account.setAccountNumber(number);
    account.setBalance(new BigDecimal(balance));
    account.setStatus(com.example.demo.entity.AccountStatus.fromCode(status));
    return account;
  }

  private Transaction transaction(String source, String destination, String amount) {
    Transaction transaction = new Transaction();
    transaction.setFromAccount(account(source, "0.00", 1));
    transaction.setToAccount(account(destination, "0.00", 1));
    transaction.setAmount(new BigDecimal(amount));
    return transaction;
  }

  private TransactionResponse response(String source, String destination, String amount) {
    TransactionResponse response = new TransactionResponse();
    response.setFromAccountNumber(source);
    response.setToAccountNumber(destination);
    response.setAmount(new BigDecimal(amount));
    return response;
  }

  private TransferRequest request(String source, String destination, String amount) {
    return new TransferRequest(source, destination, new BigDecimal(amount), "content");
  }
}
