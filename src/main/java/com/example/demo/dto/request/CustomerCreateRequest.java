package com.example.demo.dto.request;

import com.example.demo.entity.CustomerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateRequest {

    @NotBlank(message = "Ten khong de trong")
    @Size(max=100,message = "Ten khong duoc qua dai")
    private String name;

    @NotNull
    private LocalDate birthday;

    @NotBlank(message = "Dia chi khong duoc trong")
    private String address;

    @NotBlank(message = "CCCD khong bo trong")
    @Pattern(regexp = "\\d{10}",message = "CCCD phai nhap so")
    private String identityNo;

    @Pattern(regexp = "\\d{9,10}", message = "SDT phai la so")
    private String mobile;

    @NotNull(message = "Kieu Khach hang khong duoc bo trong")
    private CustomerType customerType;

    @NotNull(message = "Trang thai khach hang khong duoc bo trong")
    private Integer status;
}
