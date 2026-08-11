package com.example.demo.entity;

import static com.example.demo.constant.ValidationConstants.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Customer extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Long id;

  @Column(nullable = false, length = CUSTOMER_NAME_MAX_LENGTH)
  private String name;

  @Column(nullable = false)
  @PastOrPresent
  private LocalDate birthday;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false, length = IDENTITY_NUMBER_LENGTH, unique = true)
  private String identityNo;

  @Column() private String mobile;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CustomerType customerType;

  @Column(nullable = false)
  private Integer status;

  @Version
  @Column(nullable = false)
  private Long version;
}
