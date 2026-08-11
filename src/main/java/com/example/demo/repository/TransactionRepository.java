package com.example.demo.repository;

import com.example.demo.entity.Transaction;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
  /**
   * Returns transactions sent from or received by an account while eagerly loading both account
   * associations. Date bounds are inclusive and independently optional.
   *
   * @param accountNumber the sending or receiving account number
   * @param fromDate the optional earliest transaction date, inclusive
   * @param toDate the optional latest transaction date, inclusive
   * @param pageable the requested page, size, and sorting
   * @return a page of matching transactions with both accounts loaded
   */
  @EntityGraph(attributePaths = {"fromAccount", "toAccount"})
  @Query(
      """
            select t from Transaction t
            where (t.fromAccount.accountNumber = :accountNumber or t.toAccount.accountNumber = :accountNumber)
              and (:fromDate is null or t.transactionDate >= :fromDate)
              and (:toDate is null or t.transactionDate <= :toDate)
            """)
  Page<Transaction> findHistory(
      String accountNumber, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
}
