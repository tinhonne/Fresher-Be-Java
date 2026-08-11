package com.example.demo.dto.request.account;

import static com.example.demo.constant.ValidationConstants.ACCOUNT_NUMBER_PATTERN;
import static com.example.demo.constant.ValidationConstants.MONEY_PRECISION;
import static com.example.demo.constant.ValidationConstants.MONEY_SCALE;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

  @NotBlank
  @Pattern(regexp = ACCOUNT_NUMBER_PATTERN)
  private String accountNumber;

  @NotNull @Positive private Long customerId;

  @NotNull
  @DecimalMin("0.00")
  @DecimalMax("0.00")
  @Digits(integer = MONEY_PRECISION - MONEY_SCALE, fraction = MONEY_SCALE)
  private BigDecimal balance;
}
