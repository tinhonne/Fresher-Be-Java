package com.example.demo.dto.request.customer;

import static com.example.demo.constant.ValidationConstants.*;

import com.example.demo.entity.CustomerType;
import com.example.demo.validation.ValidationCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateRequest {

  @NotBlank(message = ValidationCode.NOT_BLANK)
  @Size(max = CUSTOMER_NAME_MAX_LENGTH, message = ValidationCode.SIZE)
  private String name;

  @NotNull @PastOrPresent private LocalDate birthday;

  @NotBlank(message = ValidationCode.NOT_BLANK)
  @Size(max = CUSTOMER_ADDRESS_MAX_LENGTH)
  private String address;

  @Pattern(regexp = MOBILE_PATTERN, message = ValidationCode.PATTERN)
  private String mobile;

  @NotNull(message = ValidationCode.NOT_NULL)
  private CustomerType customerType;

  @NotNull(message = ValidationCode.NOT_NULL)
  @PositiveOrZero(message = ValidationCode.POSITIVE_OR_ZERO)
  private Long version;
}
