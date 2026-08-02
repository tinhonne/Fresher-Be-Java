package com.example.demo.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import static com.example.demo.constant.TransactionConstants.MINIMUM_AMOUNT;
import static com.example.demo.constant.ValidationConstants.ACCOUNT_NUMBER_PATTERN;
import static com.example.demo.constant.ValidationConstants.MONEY_PRECISION;
import static com.example.demo.constant.ValidationConstants.MONEY_SCALE;
import static com.example.demo.constant.ValidationConstants.TRANSACTION_CONTENT_MAX_LENGTH;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    @NotBlank
    @Pattern(regexp = ACCOUNT_NUMBER_PATTERN)
    private String fromAccountNumber;
    @NotBlank
    @Pattern(regexp = ACCOUNT_NUMBER_PATTERN)
    private String toAccountNumber;
    @NotNull
    @DecimalMin(MINIMUM_AMOUNT)
    @Digits(integer = MONEY_PRECISION - MONEY_SCALE, fraction = MONEY_SCALE)
    private BigDecimal amount;
    @Size(max = TRANSACTION_CONTENT_MAX_LENGTH)
    private String content;
}
