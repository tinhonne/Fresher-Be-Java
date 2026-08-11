package com.example.demo.dto.response.customer;

import com.example.demo.entity.CustomerType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
  private Long id;
  private String name;
  private LocalDate birthday;
  private String address;
  private String identityNo;
  private String mobile;
  private CustomerType customerType;
  private Integer status;
  private Long version;
  private LocalDateTime createDatetime;
  private LocalDateTime updateDatetime;
}
