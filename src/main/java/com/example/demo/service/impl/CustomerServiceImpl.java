package com.example.demo.service.impl;

import com.example.demo.dto.request.CustomerCreateRequest;
import com.example.demo.dto.request.CustomerUpdateRequest;
import com.example.demo.dto.response.CustomerResponse;
import com.example.demo.entity.Customer;
import com.example.demo.mapper.CustomerMapping;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerMapping customerMapping;

    @Override
    public CustomerResponse createCustomer(CustomerCreateRequest request){
        Customer customer= customerMapping.toEntity(request);
        customer.setCreateDatetime(LocalDateTime.now());
        customer.setUpdateDatetime(LocalDateTime.now());
        customerRepository.save(customer);

        CustomerResponse response=customerMapping.toResponse(customer);
        return response;

    }

    @Override
    public List<CustomerResponse> getCustomer(){
        List<Customer> customers =customerRepository.findAll();
        List<CustomerResponse> responses=new ArrayList<>();
        for(Customer customer: customers){
            responses.add(customerMapping.toResponse(customer));
        }
        return responses;
    }

    @Override
    public CustomerResponse getCustomerById(Long id){
        Customer customer=customerRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Khach hang khong ton tai"));

        return customerMapping.toResponse(customer);
    }

    @Override
    public CustomerResponse updateCustomerById(Long id, CustomerUpdateRequest request){
        Customer customer=customerRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Khach hang khong ton tai"));
        customerMapping.toUpdateCustomerByID(customer,request);
        customer.setUpdateDatetime(LocalDateTime.now());

        customerRepository.save(customer);
        return customerMapping.toResponse(customer);

    }

    @Override
    public void deleteCustomerById(Long id){
        Customer customer=customerRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Khach hang khong ton tai"));
        customerRepository.delete(customer);
    }
}
