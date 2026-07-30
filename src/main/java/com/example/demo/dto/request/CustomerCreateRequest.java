package com.example.demo.dto.request;

import com.example.demo.entity.CustomerType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static com.example.demo.constant.CustomerConstants.ACTIVE_STATUS;
import static com.example.demo.constant.CustomerConstants.INACTIVE_STATUS;
import static com.example.demo.constant.ValidationConstants.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateRequest {

    @NotBlank(message = "Ten khong de trong")
    @Size(max=CUSTOMER_NAME_MAX_LENGTH,message = "Ten khong duoc qua dai")
    private String name;

    @NotNull
    @PastOrPresent
    private LocalDate birthday;

    @NotBlank(message = "Dia chi khong duoc trong")
    @Size(max = CUSTOMER_ADDRESS_MAX_LENGTH)
    private String address;

    @NotBlank(message = "CCCD khong bo trong")
    @Pattern(regexp = IDENTITY_NUMBER_PATTERN,message = "CCCD phai 10 so")
    private String identityNo;

    @Pattern(regexp = MOBILE_PATTERN, message = "SDT phai du 9-10 chu so")
    private String mobile;

    @NotNull(message = "Kieu Khach hang khong duoc bo trong")
    private CustomerType customerType;

    @NotNull(message = "Trang thai khach hang khong duoc bo trong")
    @Min(INACTIVE_STATUS)
    @Max(ACTIVE_STATUS)
    private Integer status;
}
