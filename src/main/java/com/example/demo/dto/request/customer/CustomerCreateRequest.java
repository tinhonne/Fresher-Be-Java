package com.example.demo.dto.request.customer;

import static com.example.demo.constant.CustomerConstants.ACTIVE_STATUS;
import static com.example.demo.constant.CustomerConstants.INACTIVE_STATUS;
import static com.example.demo.constant.ValidationConstants.*;

import com.example.demo.entity.CustomerType;
import com.example.demo.validation.ValidationCode;
import com.example.demo.validation.annotation.MinimumAge;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateRequest {

  @NotBlank(message = ValidationCode.NOT_BLANK)
  @Size(max = CUSTOMER_NAME_MAX_LENGTH, message = ValidationCode.SIZE)
  private String name;

  @NotNull
  @PastOrPresent
  @MinimumAge(min = 18)
  private LocalDate birthday;

  @NotBlank(message = ValidationCode.NOT_BLANK)
  @Size(max = CUSTOMER_ADDRESS_MAX_LENGTH)
  private String address;

  @NotBlank(message = ValidationCode.NOT_BLANK)
  @Pattern(regexp = IDENTITY_NUMBER_PATTERN, message = ValidationCode.PATTERN)
  private String identityNo;

  @Pattern(regexp = MOBILE_PATTERN, message = ValidationCode.PATTERN)
  private String mobile;

  @NotNull(message = ValidationCode.NOT_NULL)
  private CustomerType customerType;

  @NotNull(message = ValidationCode.NOT_NULL)
  @Min(INACTIVE_STATUS)
  @Max(ACTIVE_STATUS)
  private Integer status;
}
