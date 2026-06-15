package com.example.demo.service;

import com.example.demo.dto.request.CustomerCreateRequest;
import com.example.demo.dto.request.CustomerUpdateRequest;
import com.example.demo.dto.response.CustomerResponse;

import java.util.List;

public interface CustomerService {
    CustomerResponse createCustomer(CustomerCreateRequest request);
    List<CustomerResponse> getCustomer();
    CustomerResponse getCustomerById(Long id);
    CustomerResponse updateCustomerById(Long id, CustomerUpdateRequest request);
    void deleteCustomerById(Long id);
}
