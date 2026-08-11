package com.example.demo.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.demo.dto.request.account.AccountRequest;
import com.example.demo.dto.response.account.AccountResponse;
import com.example.demo.entity.Account;
import com.example.demo.entity.Customer;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorDefinition;
import com.example.demo.exception.error.AccountError;
import com.example.demo.exception.error.CustomerError;
import com.example.demo.mapper.AccountMapping;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.CustomerRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
  @Mock private AccountRepository accountRepository;
  @Mock private CustomerRepository customerRepository;
  @Mock private AccountMapping accountMapping;

  private AccountServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new AccountServiceImpl(accountRepository, customerRepository, accountMapping);
  }

  @Test
  void createRejectsMissingCustomerBeforeCheckingAccountNumber() {
    AccountRequest request = request("1234567890", 7L, BigDecimal.ZERO);
    when(customerRepository.findByIdForUpdate(7L)).thenReturn(Optional.empty());

    assertError(CustomerError.CUSTOMER_NOT_FOUND, () -> service.createAccount(request));

    verifyNoInteractions(accountRepository, accountMapping);
  }

  @Test
  void createRejectsDuplicateAccountNumber() {
    AccountRequest request = request("1234567890", 7L, BigDecimal.ZERO);
    Customer customer = new Customer();
    customer.setStatus(1);
    when(customerRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(customer));
    when(accountRepository.existsByAccountNumber("1234567890")).thenReturn(true);

    assertError(AccountError.ACCOUNT_NUMBER_EXISTED, () -> service.createAccount(request));

    verify(accountMapping, never()).toEntity(any());
    verify(accountRepository, never()).saveAndFlush(any());
  }

  @Test
  void createAssociatesCustomerAndStartsPendingApproval() {
    Customer customer = new Customer();
    customer.setStatus(1);
    Account account = new Account();
    AccountResponse response = new AccountResponse();
    AccountRequest request = request("1234567890", 7L, BigDecimal.ZERO);
    when(customerRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(customer));
    when(accountMapping.toEntity(request)).thenReturn(account);
    when(accountRepository.saveAndFlush(account)).thenReturn(account);
    when(accountMapping.toResponse(account)).thenReturn(response);

    assertSame(response, service.createAccount(request));

    assertSame(customer, account.getCustomer());
    assertEquals(com.example.demo.entity.AccountStatus.PENDING, account.getStatus());
    assertEquals(BigDecimal.ZERO, account.getBalance());
    verify(accountRepository).saveAndFlush(account);
  }

  @Test
  void createPropagatesConcurrentIntegrityViolation() {
    Customer customer = new Customer();
    customer.setStatus(1);
    Account account = new Account();
    AccountRequest request = request("1234567890", 7L, BigDecimal.ZERO);
    DataIntegrityViolationException violation = new DataIntegrityViolationException("duplicate");
    when(customerRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(customer));
    when(accountMapping.toEntity(request)).thenReturn(account);
    when(accountRepository.saveAndFlush(account)).thenThrow(violation);

    assertSame(
        violation,
        assertThrows(DataIntegrityViolationException.class, () -> service.createAccount(request)));
  }

  @Test
  void listAndActiveQueriesPreservePagination() {
    when(accountRepository.findAllSortedByCustomerName(any()))
        .thenReturn(new PageImpl<>(List.of()));
    when(customerRepository.existsById(9L)).thenReturn(true);
    when(accountRepository.findByCustomerIdAndStatusOrderByAccountNumber(
            org.mockito.ArgumentMatchers.eq(9L),
            org.mockito.ArgumentMatchers.eq(com.example.demo.entity.AccountStatus.ACTIVE),
            any()))
        .thenReturn(new PageImpl<>(List.of()));

    service.getAccountSortByNameCustomer(2, 25);
    service.getActiveAccountByCustomerId(9L, 3, 15);

    ArgumentCaptor<Pageable> all = ArgumentCaptor.forClass(Pageable.class);
    ArgumentCaptor<Pageable> active = ArgumentCaptor.forClass(Pageable.class);
    verify(accountRepository).findAllSortedByCustomerName(all.capture());
    verify(accountRepository)
        .findByCustomerIdAndStatusOrderByAccountNumber(
            org.mockito.ArgumentMatchers.eq(9L),
            org.mockito.ArgumentMatchers.eq(com.example.demo.entity.AccountStatus.ACTIVE),
            active.capture());
    assertEquals(2, all.getValue().getPageNumber());
    assertEquals(25, all.getValue().getPageSize());
    assertEquals(3, active.getValue().getPageNumber());
    assertEquals(15, active.getValue().getPageSize());
  }

  @Test
  void approveLocksCustomerBeforeReloadingAndLockingAccount() {
    Customer customer = new Customer(21L, null, null, null, null, null, null, 1, 0L);
    Account resolved = new Account();
    resolved.setCustomer(customer);
    Account locked = new Account();
    locked.setCustomer(customer);
    locked.setStatus(com.example.demo.entity.AccountStatus.PENDING);
    AccountResponse response = new AccountResponse();
    when(accountRepository.findById(11L)).thenReturn(Optional.of(resolved));
    when(customerRepository.findByIdForUpdate(21L)).thenReturn(Optional.of(customer));
    when(accountRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(locked));
    when(accountRepository.save(locked)).thenReturn(locked);
    when(accountMapping.toResponse(locked)).thenReturn(response);

    assertSame(response, service.approveAccount(11L));

    InOrder locks = inOrder(accountRepository, customerRepository);
    locks.verify(accountRepository).findById(11L);
    locks.verify(customerRepository).findByIdForUpdate(21L);
    locks.verify(accountRepository).findByIdForUpdate(11L);
    assertEquals(com.example.demo.entity.AccountStatus.ACTIVE, locked.getStatus());
  }

  @Test
  void unfreezeRejectsInactiveCustomerBeforeLockingAccount() {
    Customer customer = new Customer(21L, null, null, null, null, null, null, 0, 0L);
    Account resolved = new Account();
    resolved.setCustomer(customer);
    when(accountRepository.findById(12L)).thenReturn(Optional.of(resolved));
    when(customerRepository.findByIdForUpdate(21L)).thenReturn(Optional.of(customer));

    assertError(AccountError.CUSTOMER_INACTIVE, () -> service.unfreezeAccount(12L));

    verify(accountRepository, never()).findByIdForUpdate(any());
    verify(accountRepository, never()).save(any());
  }

  @Test
  void freezeUsesCustomerBeforeAccountLockWithoutRequiringActiveCustomer() {
    Customer customer = new Customer(21L, null, null, null, null, null, null, 0, 0L);
    Account resolved = new Account();
    resolved.setCustomer(customer);
    Account locked = new Account();
    locked.setCustomer(customer);
    locked.setStatus(com.example.demo.entity.AccountStatus.ACTIVE);
    locked.setBalance(BigDecimal.ZERO);
    when(accountRepository.findById(13L)).thenReturn(Optional.of(resolved));
    when(customerRepository.findByIdForUpdate(21L)).thenReturn(Optional.of(customer));
    when(accountRepository.findByIdForUpdate(13L)).thenReturn(Optional.of(locked));
    when(accountRepository.save(locked)).thenReturn(locked);

    service.freezeAccount(13L);

    InOrder locks = inOrder(accountRepository, customerRepository);
    locks.verify(customerRepository).findByIdForUpdate(21L);
    locks.verify(accountRepository).findByIdForUpdate(13L);
    assertEquals(com.example.demo.entity.AccountStatus.FROZEN, locked.getStatus());
  }

  private AccountRequest request(String number, Long customerId, BigDecimal balance) {
    return new AccountRequest(number, customerId, balance);
  }

  private void assertError(ErrorDefinition expected, Runnable invocation) {
    AppException exception = assertThrows(AppException.class, invocation::run);
    assertEquals(expected, exception.getErrorCode());
  }
}
