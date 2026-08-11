package com.example.demo.repository;

import com.example.demo.entity.Customer;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface CustomerRepository
    extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
  boolean existsByIdentityNo(String identityNo);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from Customer c where c.id = :id")
  Optional<Customer> findByIdForUpdate(Long id);

  /**
   * Returns all customers ordered by name ascending before pagination.
   *
   * @param pageable the requested page and size
   * @return a page of customers in name order
   */
  @Query("Select c From Customer c Order by c.name ASC")
  Page<Customer> findAllSortedByName(Pageable pageable);

  /**
   * Returns customers satisfying the supplied specification.
   *
   * @param spec the criteria controlling which customers are visible
   * @param pageable the requested page, size, and sorting
   * @return a page of matching customers
   */
  Page<Customer> findAll(Specification<Customer> spec, Pageable pageable);
}
