package com.example.demo.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AccountStatusConverter implements AttributeConverter<AccountStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(AccountStatus status) {
        return status == null ? null : status.getCode();
    }

    @Override
    public AccountStatus convertToEntityAttribute(Integer code) {
        return AccountStatus.fromCode(code);
    }
}
