package com.example.demo.service;

import com.example.demo.dto.request.customer.CustomerCreateRequest;
import com.example.demo.dto.request.customer.CustomerSearchRequest;
import com.example.demo.dto.request.customer.CustomerStatusUpdateRequest;
import com.example.demo.dto.request.customer.CustomerUpdateRequest;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.customer.CustomerResponse;
import com.example.demo.exception.AppException;

public interface CustomerService {
  /**
   * Creates a customer.
   *
   * @param request the customer creation request
   * @return the created customer
   * @throws AppException if the customer already exists ({@code CUSTOMER_EXISTED})
   */
  CustomerResponse createCustomer(CustomerCreateRequest request);

  /**
   * Returns a customer by identifier.
   *
   * @param id the customer identifier
   * @return the customer details
   * @throws AppException if the customer does not exist ({@code CUSTOMER_NOT_FOUND})
   */
  CustomerResponse getCustomerById(Long id);

  /**
   * Updates a customer by identifier.
   *
   * @param id the customer identifier
   * @param request the customer update request
   * @return the updated customer
   * @throws AppException if the customer does not exist ({@code CUSTOMER_NOT_FOUND})
   */
  CustomerResponse updateCustomerById(Long id, CustomerUpdateRequest request);

  CustomerResponse updateCustomerStatus(Long id, CustomerStatusUpdateRequest request);

  /**
   * Deactivates a customer that has no active account.
   *
   * @param id the customer identifier
   * @throws AppException if the customer does not exist ({@code CUSTOMER_NOT_FOUND}) or has an
   *     active account ({@code CUSTOMER_HAS_ACCOUNT})
   */
  void deleteCustomerById(Long id);

  /**
   * Returns customers ordered by name.
   *
   * @param page the zero-based page index
   * @param size the page size
   * @return the requested page of customers
   */
  PageResponse<CustomerResponse> getAllCustomerSortByName(int page, int size);

  /**
   * Searches for customers and orders the results by name.
   *
   * @param request the customer search criteria
   * @param page the zero-based page index
   * @param size the page size
   * @return the requested page of matching customers
   */
  PageResponse<CustomerResponse> getCustomerSortByField(
      CustomerSearchRequest request, int page, int size);
}
