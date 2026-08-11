package com.example.demo.dto.response.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

  private Long id;
  private Long customerId;
  private String customerName;
  private String accountNumber;
  private BigDecimal balance;
  private Integer status;
  private LocalDateTime createDatetime;
  private LocalDateTime updateDatetime;
}
