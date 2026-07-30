package com.example.demo.service.impl;

import com.example.demo.dto.request.CustomerCreateRequest;
import com.example.demo.dto.request.CustomerSearchRequest;
import com.example.demo.dto.response.CustomerResponse;
import com.example.demo.entity.Customer;
import com.example.demo.entity.CustomerType;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.CustomerMapping;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.CustomerRepository;
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
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.demo.constant.CustomerConstants.INACTIVE_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CustomerMapping customerMapping;

    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CustomerServiceImpl(customerRepository, accountRepository, customerMapping);
    }

    @Test
    void createRejectsDuplicateIdentityWithoutMappingOrSaving() {
        CustomerCreateRequest request = request();
        when(customerRepository.existsByIdentityNo(request.getIdentityNo())).thenReturn(true);

        assertError(ErrorCode.CUSTOMER_EXISTED, () -> service.createCustomer(request));

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
        DataIntegrityViolationException violation = new DataIntegrityViolationException("duplicate identity");
        when(customerMapping.toEntity(request)).thenReturn(customer);
        when(customerRepository.saveAndFlush(customer)).thenThrow(violation);

        assertSame(violation, assertThrows(DataIntegrityViolationException.class,
                () -> service.createCustomer(request)));

        verify(customerMapping, never()).toResponse(any());
    }

    @Test
    void deleteRejectsCustomerWithActiveAccount() {
        Customer customer = new Customer();
        customer.setStatus(1);
        when(customerRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(customer));
        when(accountRepository.existsByCustomerIdAndStatusIn(org.mockito.ArgumentMatchers.eq(5L), any()))
                .thenReturn(true);

        assertError(ErrorCode.CUSTOMER_HAS_ACCOUNT, () -> service.deleteCustomerById(5L));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void deleteIsSoftDeactivationWhenNoActiveAccountExists() {
        Customer customer = new Customer();
        customer.setStatus(1);
        when(customerRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(customer));

        service.deleteCustomerById(5L);

        assertEquals(INACTIVE_STATUS, customer.getStatus());
        verify(customerRepository).save(customer);
    }

    @Test
    void listAndSearchPreservePaginationAndSearchSort() {
        CustomerSearchRequest search = new CustomerSearchRequest();
        search.setName("An");
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(customerRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getAllCustomerSortByName(2, 20);
        service.getCustomerSortByField(search, 3, 15);

        ArgumentCaptor<Pageable> listPage = ArgumentCaptor.forClass(Pageable.class);
        ArgumentCaptor<Pageable> searchPage = ArgumentCaptor.forClass(Pageable.class);
        verify(customerRepository).findAll(listPage.capture());
        verify(customerRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), searchPage.capture());
        assertEquals(2, listPage.getValue().getPageNumber());
        assertEquals(20, listPage.getValue().getPageSize());
        assertEquals(3, searchPage.getValue().getPageNumber());
        assertEquals(15, searchPage.getValue().getPageSize());
        assertEquals(Sort.Direction.ASC, searchPage.getValue().getSort().getOrderFor("name").getDirection());
    }

    @Test
    void searchRejectsNullRequestWithoutRepositoryAccess() {
        assertError(ErrorCode.INVALID_CUSTOMER_SEARCH,
                () -> service.getCustomerSortByField(null, 0, 20));

        verifyNoInteractions(customerRepository, accountRepository, customerMapping);
    }

    @Test
    void searchReliesOnRequestValidationForFieldConstraints() {
        CustomerSearchRequest search = new CustomerSearchRequest();
        search.setIdentityNo("invalid");
        search.setMobile("invalid");
        search.setStatus(2);
        when(customerRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getCustomerSortByField(search, 0, 20);

        verify(customerRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }

    @Test
    void publicOperationsDeclareCustomerPermissions() {
        assertPermission("createCustomer", "CUSTOMER_CREATE", CustomerCreateRequest.class);
        assertPermission("getCustomerById", "CUSTOMER_VIEW", Long.class);
        assertPermission("getAllCustomerSortByName", "CUSTOMER_VIEW", int.class, int.class);
        assertPermission("getCustomerSortByField", "CUSTOMER_VIEW", CustomerSearchRequest.class, int.class, int.class);
        assertPermission("deleteCustomerById", "CUSTOMER_UPDATE", Long.class);
    }

    private void assertPermission(String name, String authority, Class<?>... parameters) {
        try {
            Method method = CustomerServiceImpl.class.getMethod(name, parameters);
            PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
            assertTrue(annotation != null && annotation.value().contains("'" + authority + "'"),
                    name + " must require " + authority);
        } catch (NoSuchMethodException exception) {
            throw new AssertionError("Missing approved customer operation: " + name, exception);
        }
    }

    private CustomerCreateRequest request() {
        return new CustomerCreateRequest("An", LocalDate.of(1990, 1, 1), "Ha Noi", "1234567890",
                "0912345678", CustomerType.INDIVIDUAL, 1);
    }

    private void assertError(ErrorCode expected, Runnable invocation) {
        AppException exception = assertThrows(AppException.class, invocation::run);
        assertEquals(expected, exception.getErrorCode());
    }
}
