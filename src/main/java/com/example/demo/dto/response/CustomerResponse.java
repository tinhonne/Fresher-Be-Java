package com.example.demo.dto.response;

import com.example.demo.entity.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDateTime createDatetime;
    private LocalDateTime updateDatetime;
}
