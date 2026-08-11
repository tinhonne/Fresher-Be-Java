package com.example.demo.converter;

import com.example.demo.entity.TransactionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TransactionStatusConverter implements AttributeConverter<TransactionStatus, Integer> {

  /**
   * Converts a status to its persistent integer code.
   *
   * @param status the status to persist
   * @return the status code, or {@code null} when the status is null
   */
  @Override
  public Integer convertToDatabaseColumn(TransactionStatus status) {
    return status == null ? null : status.getCode();
  }

  /**
   * Converts a persistent integer code to its status.
   *
   * @param code the persistent code
   * @return the matching status, or {@code null} when the code is null
   * @throws IllegalArgumentException when the code is not recognized
   */
  @Override
  public TransactionStatus convertToEntityAttribute(Integer code) {
    return TransactionStatus.fromCode(code);
  }
}
