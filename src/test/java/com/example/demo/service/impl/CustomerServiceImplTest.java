package com.example.demo.service.impl;

import static com.example.demo.constant.CustomerConstants.INACTIVE_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.demo.dto.request.customer.CustomerCreateRequest;
import com.example.demo.dto.request.customer.CustomerSearchRequest;
import com.example.demo.dto.request.customer.CustomerStatusUpdateRequest;
import com.example.demo.dto.request.customer.CustomerUpdateRequest;
import com.example.demo.dto.response.customer.CustomerResponse;
import com.example.demo.entity.Customer;
import com.example.demo.entity.CustomerType;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorDefinition;
import com.example.demo.exception.error.CommonError;
import com.example.demo.exception.error.CustomerError;
import com.example.demo.mapper.CustomerMapping;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.CustomerRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {
  @Mock private CustomerRepository customerRepository;
  @Mock private AccountRepository accountRepository;
  @Mock private CustomerMapping customerMapping;

  private CustomerServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new CustomerServiceImpl(customerRepository, accountRepository, customerMapping);
  }

  @Test
  void createRejectsDuplicateIdentityWithoutMappingOrSaving() {
    CustomerCreateRequest request = request();
    when(customerRepository.existsByIdentityNo(request.getIdentityNo())).thenReturn(true);

    assertError(CustomerError.CUSTOMER_EXISTED, () -> service.createCustomer(request));

    verifyNoInteractions(accountRepository, customerMapping);
    verify(customerRepository, never()).saveAndFlush(any());
  }

  @Test
  void createMapsSavesAndReturnsCustomer() {
    CustomerCreateRequest request = request();
    Customer customer = new Customer();
    CustomerResponse response = new CustomerResponse();
    when(customerMapping.toEntity(request)).thenReturn(customer);
    when(customerMapping.toResponse(customer)).thenReturn(response);

    assertSame(response, service.createCustomer(request));

    verify(customerRepository).saveAndFlush(customer);
  }

  @Test
  void createPropagatesConcurrentIntegrityViolation() {
    CustomerCreateRequest request = request();
    Customer customer = new Customer();
    DataIntegrityViolationException violation =
        new DataIntegrityViolationException("duplicate identity");
    when(customerMapping.toEntity(request)).thenReturn(customer);
    when(customerRepository.saveAndFlush(customer)).thenThrow(violation);

    assertSame(
        violation,
        assertThrows(DataIntegrityViolationException.class, () -> service.createCustomer(request)));

    verify(customerMapping, never()).toResponse(any());
  }

  @Test
  void updateProfileUsesOptimisticVersionWithoutPessimisticLock() {
    Customer customer = new Customer();
    customer.setStatus(1);
    customer.setVersion(3L);
    CustomerUpdateRequest request = updateRequest(3L);
    CustomerResponse response = new CustomerResponse();
    when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));
    when(customerMapping.toResponse(customer)).thenReturn(response);

    assertSame(response, service.updateCustomerById(5L, request));

    verify(customerRepository, never()).findByIdForUpdate(5L);
    verify(accountRepository, never()).existsByCustomerIdAndStatusIn(any(), any());
    verify(customerMapping).toUpdateCustomerByID(customer, request);
    verify(customerRepository).saveAndFlush(customer);
    assertEquals(1, customer.getStatus());
    assertEquals(3L, customer.getVersion());
  }

  @Test
  void updateProfileRejectsStaleVersionWithoutMutation() {
    Customer customer = new Customer();
    customer.setVersion(4L);
    CustomerUpdateRequest request = updateRequest(3L);
    when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));

    assertError(CommonError.CONCURRENT_MODIFICATION, () -> service.updateCustomerById(5L, request));

    verifyNoInteractions(accountRepository, customerMapping);
    verify(customerRepository, never()).saveAndFlush(any());
  }

  @Test
  void statusDeactivateRejectsPendingActiveOrFrozenAccount() {
    Customer customer = customer(5L, 1, 3L);
    when(customerRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(customer));
    when(accountRepository.existsByCustomerIdAndStatusIn(
            org.mockito.ArgumentMatchers.eq(5L), any()))
        .thenReturn(true);

    assertError(
        CustomerError.CUSTOMER_HAS_ACCOUNT,
        () -> service.updateCustomerStatus(5L, new CustomerStatusUpdateRequest(0)));

    assertEquals(1, customer.getStatus());
    verify(customerRepository, never()).saveAndFlush(any());
  }

  @Test
  void statusDeactivateSucceedsWhenAllAccountsAreClosed() {
    Customer customer = customer(5L, 1, 3L);
    CustomerResponse response = new CustomerResponse();
    response.setStatus(0);
    response.setVersion(4L);
    when(customerRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(customer));
    when(customerMapping.toResponse(customer)).thenReturn(response);

    assertSame(response, service.updateCustomerStatus(5L, new CustomerStatusUpdateRequest(0)));

    assertEquals(0, customer.getStatus());
    verify(accountRepository)
        .existsByCustomerIdAndStatusIn(
            org.mockito.ArgumentMatchers.eq(5L),
            org.mockito.ArgumentMatchers.argThat(
                statuses -> {
                  List<com.example.demo.entity.AccountStatus> values = new java.util.ArrayList<>();
                  statuses.forEach(values::add);
                  return values.size() == 3
                      && values.containsAll(
                          List.of(
                              com.example.demo.entity.AccountStatus.PENDING,
                              com.example.demo.entity.AccountStatus.ACTIVE,
                              com.example.demo.entity.AccountStatus.FROZEN));
                }));
    verify(customerRepository).saveAndFlush(customer);
  }

  @Test
  void statusReactivatesInactiveCustomerWithoutAccountCheck() {
    Customer customer = customer(5L, 0, 3L);
    CustomerResponse response = new CustomerResponse();
    when(customerRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(customer));
    when(customerMapping.toResponse(customer)).thenReturn(response);

    assertSame(response, service.updateCustomerStatus(5L, new CustomerStatusUpdateRequest(1)));

    assertEquals(1, customer.getStatus());
    verifyNoInteractions(accountRepository);
    verify(customerRepository).saveAndFlush(customer);
  }

  @Test
  void sameStatusIsIdempotentWithoutSavingOrAccountCheck() {
    Customer customer = customer(5L, 0, 3L);
    CustomerResponse response = new CustomerResponse();
    when(customerRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(customer));
    when(customerMapping.toResponse(customer)).thenReturn(response);

    assertSame(response, service.updateCustomerStatus(5L, new CustomerStatusUpdateRequest(0)));

    verifyNoInteractions(accountRepository);
    verify(customerRepository, never()).saveAndFlush(any());
  }

  @Test
  void deleteRejectsCustomerWithActiveAccount() {
    Customer customer = new Customer();
    customer.setStatus(1);
    when(customerRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(customer));
    when(accountRepository.existsByCustomerIdAndStatusIn(
            org.mockito.ArgumentMatchers.eq(5L), any()))
        .thenReturn(true);

    assertError(CustomerError.CUSTOMER_HAS_ACCOUNT, () -> service.deleteCustomerById(5L));

    verify(customerRepository, never()).save(any());
  }

  @Test
  void deleteIsSoftDeactivationWhenNoActiveAccountExists() {
    Customer customer = new Customer();
    customer.setStatus(1);
    when(customerRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(customer));

    service.deleteCustomerById(5L);

    assertEquals(INACTIVE_STATUS, customer.getStatus());
    verify(customerRepository).saveAndFlush(customer);
  }

  @Test
  void listAndSearchPreservePaginationAndSearchSort() {
    CustomerSearchRequest search = new CustomerSearchRequest();
    search.setName("An");
    when(customerRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
    when(customerRepository.findAll(
            any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    service.getAllCustomerSortByName(2, 20);
    service.getCustomerSortByField(search, 3, 15);

    ArgumentCaptor<Pageable> listPage = ArgumentCaptor.forClass(Pageable.class);
    ArgumentCaptor<Pageable> searchPage = ArgumentCaptor.forClass(Pageable.class);
    verify(customerRepository).findAll(listPage.capture());
    verify(customerRepository)
        .findAll(
            any(org.springframework.data.jpa.domain.Specification.class), searchPage.capture());
    assertEquals(2, listPage.getValue().getPageNumber());
    assertEquals(20, listPage.getValue().getPageSize());
    assertEquals(3, searchPage.getValue().getPageNumber());
    assertEquals(15, searchPage.getValue().getPageSize());
    assertEquals(
        Sort.Direction.ASC, searchPage.getValue().getSort().getOrderFor("name").getDirection());
  }

  @Test
  void searchRejectsNullRequestWithoutRepositoryAccess() {
    assertError(
        CustomerError.INVALID_CUSTOMER_SEARCH, () -> service.getCustomerSortByField(null, 0, 20));

    verifyNoInteractions(customerRepository, accountRepository, customerMapping);
  }

  @Test
  void searchReliesOnRequestValidationForFieldConstraints() {
    CustomerSearchRequest search = new CustomerSearchRequest();
    search.setIdentityNo("invalid");
    search.setMobile("invalid");
    search.setStatus(2);
    when(customerRepository.findAll(
            any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    service.getCustomerSortByField(search, 0, 20);

    verify(customerRepository)
        .findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
  }

  private CustomerCreateRequest request() {
    return new CustomerCreateRequest(
        "An",
        LocalDate.of(1990, 1, 1),
        "Ha Noi",
        "1234567890",
        "0912345678",
        CustomerType.INDIVIDUAL,
        1);
  }

  private CustomerUpdateRequest updateRequest(Long version) {
    return new CustomerUpdateRequest(
        "An", LocalDate.of(1990, 1, 1), "Ha Noi", "0912345678", CustomerType.INDIVIDUAL, version);
  }

  private Customer customer(Long id, Integer status, Long version) {
    return new Customer(id, null, null, null, null, null, null, status, version);
  }

  private void assertError(ErrorDefinition expected, Runnable invocation) {
    AppException exception = assertThrows(AppException.class, invocation::run);
    assertEquals(expected, exception.getErrorCode());
  }
}
