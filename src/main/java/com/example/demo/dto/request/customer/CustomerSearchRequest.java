package com.example.demo.dto.request.customer;

import static com.example.demo.constant.CustomerConstants.ACTIVE_STATUS;
import static com.example.demo.constant.CustomerConstants.INACTIVE_STATUS;
import static com.example.demo.constant.ValidationConstants.CUSTOMER_NAME_MAX_LENGTH;
import static com.example.demo.constant.ValidationConstants.IDENTITY_NUMBER_PATTERN;
import static com.example.demo.constant.ValidationConstants.MOBILE_PATTERN;

import com.example.demo.entity.CustomerType;
import com.example.demo.validation.annotation.OptionalTrimmedPattern;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerSearchRequest {
  @Size(max = CUSTOMER_NAME_MAX_LENGTH)
  private String name;

  @OptionalTrimmedPattern(regexp = IDENTITY_NUMBER_PATTERN)
  private String identityNo;

  @OptionalTrimmedPattern(regexp = MOBILE_PATTERN)
  private String mobile;

  private CustomerType customerType;

  @Min(INACTIVE_STATUS)
  @Max(ACTIVE_STATUS)
  private Integer status;
}
