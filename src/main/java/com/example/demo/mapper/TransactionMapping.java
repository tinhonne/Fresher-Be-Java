package com.example.demo.mapper;

import com.example.demo.dto.response.transaction.TransactionResponse;
import com.example.demo.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapping {
  @Mapping(source = "fromAccount.accountNumber", target = "fromAccountNumber")
  @Mapping(source = "toAccount.accountNumber", target = "toAccountNumber")
  TransactionResponse toResponse(Transaction transaction);
}
