package com.example.demo.service.impl;

import com.example.demo.dto.request.AccountRequest;
import com.example.demo.dto.response.AccountResponse;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.entity.Account;
import com.example.demo.entity.AccountStatus;
import com.example.demo.entity.Customer;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.AccountMapping;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.example.demo.constant.CustomerConstants.ACTIVE_STATUS;
import static com.example.demo.constant.TransactionConstants.MAXIMUM_PAGE_SIZE;
import static com.example.demo.constant.TransactionConstants.MINIMUM_PAGE_SIZE;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountMapping accountMapping;

    /** {@inheritDoc} */
    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ACCOUNT_CREATE')")
    public AccountResponse createAccount(AccountRequest accountRequest) {
        Customer customer = customerRepository.findByIdForUpdate(accountRequest.getCustomerId())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        if (!Integer.valueOf(ACTIVE_STATUS).equals(customer.getStatus())) {
            throw new AppException(ErrorCode.CUSTOMER_INACTIVE);
        }
        if (accountRepository.existsByAccountNumber(accountRequest.getAccountNumber())) {
            throw new AppException(ErrorCode.ACCOUNT_NUMBER_EXISTED);
        }
        if (accountRequest.getBalance() != null && accountRequest.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new AppException(ErrorCode.INVALID_INITIAL_BALANCE);
        }
        Account account = accountMapping.toEntity(accountRequest);
        account.setCustomer(customer);
        account.setStatus(AccountStatus.PENDING);
        account.setBalance(BigDecimal.ZERO);
        return accountMapping.toResponse(accountRepository.saveAndFlush(account));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ACCOUNT_VIEW')")
    public AccountResponse getAccountById(Long id) {
        return accountMapping.toResponse(accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND)));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ACCOUNT_VIEW')")
    public AccountResponse getAccountByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        return accountMapping.toResponse(account);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ACCOUNT_VIEW')")
    public PageResponse<AccountResponse> getAccountSortByNameCustomer(int page, int size) {
        validatePage(page, size);
        Page<Account> accounts = accountRepository.findAllSortedByCustomerName(PageRequest.of(page, size));
        return PageResponse.from(accounts.map(accountMapping::toResponse));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ACCOUNT_VIEW')")
    public PageResponse<AccountResponse> getActiveAccountByCustomerId(Long id, int page, int size) {
        return getCustomerAccounts(id, AccountStatus.ACTIVE, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ACCOUNT_VIEW')")
    public PageResponse<AccountResponse> getAccountsByCustomerId(Long customerId, int page, int size) {
        validateCustomerAndPage(customerId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.from(accountRepository.findByCustomerIdOrderByAccountNumber(customerId, pageable)
                .map(accountMapping::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ACCOUNT_VIEW')")
    public PageResponse<AccountResponse> getInactiveAccountsByCustomerId(Long customerId, int page, int size) {
        return getCustomerAccounts(customerId, AccountStatus.INACTIVE, page, size);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.demo.constant.SecurityConstants).ACCOUNT_APPROVE)")
    public AccountResponse approveAccount(Long id) {
        return transition(id, AccountStatus.PENDING, AccountStatus.ACTIVE, ErrorCode.ACCOUNT_NOT_PENDING_APPROVAL, false);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.demo.constant.SecurityConstants).ACCOUNT_REJECT)")
    public AccountResponse rejectAccount(Long id) {
        return transition(id, AccountStatus.PENDING, AccountStatus.INACTIVE, ErrorCode.ACCOUNT_NOT_PENDING_APPROVAL, false);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.demo.constant.SecurityConstants).ACCOUNT_FREEZE)")
    public AccountResponse freezeAccount(Long id) {
        return transition(id, AccountStatus.ACTIVE, AccountStatus.FROZEN, ErrorCode.INVALID_ACCOUNT_STATUS, false);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.demo.constant.SecurityConstants).ACCOUNT_UNFREEZE)")
    public AccountResponse unfreezeAccount(Long id) {
        return transition(id, AccountStatus.FROZEN, AccountStatus.ACTIVE, ErrorCode.INVALID_ACCOUNT_STATUS, false);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.demo.constant.SecurityConstants).ACCOUNT_CLOSE)")
    public AccountResponse closeAccount(Long id) {
        return transition(id, AccountStatus.ACTIVE, AccountStatus.INACTIVE, ErrorCode.INVALID_ACCOUNT_STATUS, true);
    }

    private PageResponse<AccountResponse> getCustomerAccounts(Long customerId, AccountStatus status, int page, int size) {
        validateCustomerAndPage(customerId, page, size);
        return PageResponse.from(accountRepository
                .findByCustomerIdAndStatusOrderByAccountNumber(customerId, status, PageRequest.of(page, size))
                .map(accountMapping::toResponse));
    }

    private AccountResponse transition(Long id, AccountStatus expected, AccountStatus target,
                                       ErrorCode transitionError, boolean requireZeroBalance) {
        Account resolvedAccount = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        Long customerId = resolvedAccount.getCustomer().getId();
        Customer customer = customerRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        if (target == AccountStatus.ACTIVE && !Integer.valueOf(ACTIVE_STATUS).equals(customer.getStatus())) {
            throw new AppException(ErrorCode.CUSTOMER_INACTIVE);
        }
        Account account = accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        if (account.getStatus() != expected) {
            throw new AppException(transitionError);
        }
        if (requireZeroBalance && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new AppException(ErrorCode.ACCOUNT_BALANCE_NOT_ZERO);
        }
        account.setStatus(target);
        return accountMapping.toResponse(accountRepository.save(account));
    }

    private void validateCustomerAndPage(Long customerId, int page, int size) {
        if (!customerRepository.existsById(customerId)) {
            throw new AppException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        validatePage(page, size);
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < MINIMUM_PAGE_SIZE || size > MAXIMUM_PAGE_SIZE) {
            throw new AppException(ErrorCode.INVALID_PAGE_REQUEST);
        }
    }
}
