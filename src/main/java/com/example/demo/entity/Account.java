package com.example.demo.entity;

import static com.example.demo.constant.ValidationConstants.*;

import com.example.demo.converter.AccountStatusConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Long id;

  @Column(length = ACCOUNT_NUMBER_LENGTH, nullable = false, unique = true)
  private String accountNumber;

  @ManyToOne
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  @Column(nullable = false, precision = MONEY_PRECISION, scale = MONEY_SCALE)
  @PositiveOrZero
  private BigDecimal balance;

  @Column(nullable = false)
  @Convert(converter = AccountStatusConverter.class)
  private AccountStatus status;
}
