package com.example.demo.dto.request.customer;

import static com.example.demo.constant.CustomerConstants.ACTIVE_STATUS;
import static com.example.demo.constant.CustomerConstants.INACTIVE_STATUS;

import com.example.demo.validation.ValidationCode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatusUpdateRequest {

  @NotNull(message = ValidationCode.NOT_NULL)
  @Min(value = INACTIVE_STATUS, message = ValidationCode.MIN)
  @Max(value = ACTIVE_STATUS, message = ValidationCode.MAX)
  private Integer status;
}
