package com.example.demo.mapper;

import com.example.demo.dto.request.CustomerCreateRequest;
import com.example.demo.dto.request.CustomerUpdateRequest;
import com.example.demo.dto.response.CustomerResponse;
import com.example.demo.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapping {
    Customer toEntity(CustomerCreateRequest request);
    CustomerResponse toResponse(Customer customer);

    /**
     * Updates the supplied customer in place from all mapped request properties.
     * MapStruct's default null handling applies, so null request properties overwrite
     * corresponding entity properties.
     *
     * @param customer the existing customer to mutate
     * @param request the source of replacement values
     */
    void toUpdateCustomerByID(@MappingTarget  Customer customer, CustomerUpdateRequest request);
}
