package com.example.demo.service.impl;

import static com.example.demo.constant.CustomerConstants.*;

import com.example.demo.dto.request.customer.CustomerCreateRequest;
import com.example.demo.dto.request.customer.CustomerSearchRequest;
import com.example.demo.dto.request.customer.CustomerStatusUpdateRequest;
import com.example.demo.dto.request.customer.CustomerUpdateRequest;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.customer.CustomerResponse;
import com.example.demo.entity.AccountStatus;
import com.example.demo.entity.Customer;
import com.example.demo.exception.AppException;
import com.example.demo.exception.error.CommonError;
import com.example.demo.exception.error.CustomerError;
import com.example.demo.mapper.CustomerMapping;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.service.CustomerService;
import com.example.demo.specification.CustomerSpecification;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

  private static final List<AccountStatus> BLOCKING_ACCOUNT_STATUSES =
      List.of(AccountStatus.ACTIVE, AccountStatus.FROZEN, AccountStatus.PENDING);

  private final CustomerRepository customerRepository;
  private final AccountRepository accountRepository;
  private final CustomerMapping customerMapping;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public CustomerResponse createCustomer(CustomerCreateRequest request) {
    validateStatus(request.getStatus());
    if (request.getStatus() != ACTIVE_STATUS) {
      throw new AppException(CustomerError.INVALID_CUSTOMER_STATUS);
    }
    if (customerRepository.existsByIdentityNo(request.getIdentityNo())) {
      throw new AppException(CustomerError.CUSTOMER_EXISTED);
    }
    Customer customer = customerMapping.toEntity(request);
    customer.setStatus(ACTIVE_STATUS);
    customerRepository.saveAndFlush(customer);
    return customerMapping.toResponse(customer);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public CustomerResponse getCustomerById(Long id) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new AppException(CustomerError.CUSTOMER_NOT_FOUND));
    return customerMapping.toResponse(customer);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public CustomerResponse updateCustomerById(Long id, CustomerUpdateRequest request) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new AppException(CustomerError.CUSTOMER_NOT_FOUND));
    if (!request.getVersion().equals(customer.getVersion())) {
      throw new AppException(CommonError.CONCURRENT_MODIFICATION);
    }
    customerMapping.toUpdateCustomerByID(customer, request);
    customerRepository.saveAndFlush(customer);
    return customerMapping.toResponse(customer);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public CustomerResponse updateCustomerStatus(Long id, CustomerStatusUpdateRequest request) {
    validateStatus(request.getStatus());
    Customer customer = findCustomerForStatusUpdate(id);
    if (customer.getStatus().equals(request.getStatus())) {
      return customerMapping.toResponse(customer);
    }
    updateStatus(customer, id, request.getStatus());
    customerRepository.saveAndFlush(customer);
    return customerMapping.toResponse(customer);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void deleteCustomerById(Long id) {
    Customer customer = findCustomerForStatusUpdate(id);
    if (customer.getStatus() == INACTIVE_STATUS) {
      return;
    }
    updateStatus(customer, id, INACTIVE_STATUS);
    customerRepository.saveAndFlush(customer);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public PageResponse<CustomerResponse> getAllCustomerSortByName(int page, int size) {
    validatePage(page, size);
    Pageable pageable = PageRequest.of(page, size, stableNameSort());
    Page<Customer> customers = customerRepository.findAll(pageable);
    return PageResponse.from(customers.map(customerMapping::toResponse));
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public PageResponse<CustomerResponse> getCustomerSortByField(
      CustomerSearchRequest request, int page, int size) {
    validatePage(page, size);
    if (request == null) {
      throw new AppException(CustomerError.INVALID_CUSTOMER_SEARCH);
    }
    Pageable pageable = PageRequest.of(page, size, stableNameSort());
    Page<Customer> customers =
        customerRepository.findAll(CustomerSpecification.filter(request), pageable);
    return PageResponse.from(customers.map(customerMapping::toResponse));
  }

  private void validateStatus(Integer status) {
    if (status == null || status < INACTIVE_STATUS || status > ACTIVE_STATUS) {
      throw new AppException(CustomerError.INVALID_CUSTOMER_STATUS);
    }
  }

  private Customer findCustomerForStatusUpdate(Long id) {
    return customerRepository
        .findByIdForUpdate(id)
        .orElseThrow(() -> new AppException(CustomerError.CUSTOMER_NOT_FOUND));
  }

  private void updateStatus(Customer customer, Long customerId, Integer requestedStatus) {
    if (requestedStatus == INACTIVE_STATUS) {
      ensureNoBlockingAccount(customerId);
    }
    customer.setStatus(requestedStatus);
  }

  private void ensureNoBlockingAccount(Long customerId) {
    if (accountRepository.existsByCustomerIdAndStatusIn(customerId, BLOCKING_ACCOUNT_STATUSES)) {
      throw new AppException(CustomerError.CUSTOMER_HAS_ACCOUNT);
    }
  }

  private void validatePage(int page, int size) {
    if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
      throw new AppException(CustomerError.INVALID_CUSTOMER_PAGE_REQUEST);
    }
  }

  private Sort stableNameSort() {
    return Sort.by(Sort.Order.asc(NAME_PROPERTY), Sort.Order.asc(ID_PROPERTY));
  }
}
