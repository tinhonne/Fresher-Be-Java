package com.example.demo.dto.response.transaction;

import com.example.demo.entity.TransactionStatus;
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
public class TransactionResponse {
  private Long id;
  private LocalDateTime transactionDate;
  private String fromAccountNumber;
  private String toAccountNumber;
  private BigDecimal amount;
  private TransactionStatus status;
  private String content;
  private String errorReason;
}
