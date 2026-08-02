package com.example.demo.repository;

import com.example.demo.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {
    boolean existsByAccountNumber(String accountNumber);
    Account findByAccountNumber(String accountNumber);

    /**
     * Finds an account by number and acquires a pessimistic write lock for the
     * surrounding transaction.
     *
     * @param accountNumber the account number to match
     * @return the locked account, or empty when no account has the number
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberForUpdate(String accountNumber);

    boolean existsByCustomerIdAndStatusIn(Long id, Iterable<com.example.demo.entity.AccountStatus> statuses);

    @Query("select a from Account a where a.customer.id = :customerId order by a.accountNumber asc, a.id asc")
    Page<Account> findByCustomerIdOrderByAccountNumber(Long customerId, Pageable pageable);

    @Query("select a from Account a where a.customer.id = :customerId and a.status = :status order by a.accountNumber asc, a.id asc")
    Page<Account> findByCustomerIdAndStatusOrderByAccountNumber(Long customerId,
            com.example.demo.entity.AccountStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(Long id);

    /**
     * Returns all accounts ordered by customer name ascending before pagination.
     *
     * @param pageable the requested page and size
     * @return a page of accounts in customer-name order
     */
    @Query("select a from Account a join a.customer c order by c.name ASC ")
    Page<Account> findAllSortedByCustomerName(Pageable pageable);

}