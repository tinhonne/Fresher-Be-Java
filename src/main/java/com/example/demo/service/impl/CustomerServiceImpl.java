package com.example.demo.service.impl;

import com.example.demo.dto.request.CustomerCreateRequest;
import com.example.demo.dto.request.CustomerSearchRequest;
import com.example.demo.dto.request.CustomerUpdateRequest;
import com.example.demo.dto.response.CustomerResponse;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.entity.AccountStatus;
import com.example.demo.entity.Customer;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.CustomerMapping;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.service.CustomerService;
import com.example.demo.specification.CustomerSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.demo.constant.CustomerConstants.*;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private static final List<AccountStatus> BLOCKING_ACCOUNT_STATUSES = List.of(
            AccountStatus.ACTIVE, AccountStatus.FROZEN, AccountStatus.PENDING);

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CustomerMapping customerMapping;

    /** {@inheritDoc} */
    @Override
    @Transactional
    @PreAuthorize(CUSTOMER_CREATE)
    public CustomerResponse createCustomer(CustomerCreateRequest request){
        validateStatus(request.getStatus());
        if (request.getStatus() != ACTIVE_STATUS) {
            throw new AppException(ErrorCode.INVALID_CUSTOMER_STATUS);
        }
        if(customerRepository.existsByIdentityNo(request.getIdentityNo())) {
            throw new AppException(ErrorCode.CUSTOMER_EXISTED);
        }
        Customer customer = customerMapping.toEntity(request);
        customer.setStatus(ACTIVE_STATUS);
        customerRepository.saveAndFlush(customer);
        return customerMapping.toResponse(customer);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize(CUSTOMER_VIEW)
    public CustomerResponse getCustomerById(Long id){
        Customer customer=customerRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        return customerMapping.toResponse(customer);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    @PreAuthorize(CUSTOMER_UPDATE)
    public CustomerResponse updateCustomerById(Long id, CustomerUpdateRequest request){
        validateStatus(request.getStatus());
        Customer customer=customerRepository.findByIdForUpdate(id)
                .orElseThrow(()-> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        validateStatusTransition(customer, request.getStatus());
        if (customer.getStatus() == ACTIVE_STATUS && request.getStatus() == INACTIVE_STATUS) {
            ensureNoBlockingAccount(id);
        }
        customerMapping.toUpdateCustomerByID(customer,request);
        customerRepository.save(customer);
        return customerMapping.toResponse(customer);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    @PreAuthorize(CUSTOMER_UPDATE)
    public void deleteCustomerById(Long id){
        Customer customer=customerRepository.findByIdForUpdate(id)
                .orElseThrow(()->new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        if (customer.getStatus() == INACTIVE_STATUS) {
            return;
        }
        ensureNoBlockingAccount(id);
        customer.setStatus(INACTIVE_STATUS);
        customerRepository.save(customer);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize(CUSTOMER_VIEW)
    public PageResponse<CustomerResponse> getAllCustomerSortByName(int page, int size){
        validatePage(page, size);
        Pageable pageable=PageRequest.of(page,size,stableNameSort());
        Page<Customer> customers=customerRepository.findAll(pageable);
        return PageResponse.from(customers.map(customerMapping::toResponse));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize(CUSTOMER_VIEW)
    public PageResponse<CustomerResponse> getCustomerSortByField(CustomerSearchRequest request, int page, int size){
        validatePage(page, size);
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_CUSTOMER_SEARCH);
        }
        Pageable pageable=PageRequest.of(page,size,stableNameSort());
        Page<Customer> customers=customerRepository.findAll(CustomerSpecification.filter(request),pageable);
        return PageResponse.from(customers.map(customerMapping::toResponse));
    }

    private void validateStatus(Integer status) {
        if (status == null || status < INACTIVE_STATUS || status > ACTIVE_STATUS) {
            throw new AppException(ErrorCode.INVALID_CUSTOMER_STATUS);
        }
    }

    private void validateStatusTransition(Customer customer, Integer requestedStatus) {
        if (customer.getStatus() == INACTIVE_STATUS && requestedStatus == ACTIVE_STATUS) {
            throw new AppException(ErrorCode.INVALID_CUSTOMER_STATUS_TRANSITION);
        }
    }

    private void ensureNoBlockingAccount(Long customerId) {
        if(accountRepository.existsByCustomerIdAndStatusIn(customerId, BLOCKING_ACCOUNT_STATUSES)){
            throw new AppException(ErrorCode.CUSTOMER_HAS_ACCOUNT);
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new AppException(ErrorCode.INVALID_CUSTOMER_PAGE_REQUEST);
        }
    }

    private Sort stableNameSort() {
        return Sort.by(Sort.Order.asc(NAME_PROPERTY), Sort.Order.asc(ID_PROPERTY));
    }
}
