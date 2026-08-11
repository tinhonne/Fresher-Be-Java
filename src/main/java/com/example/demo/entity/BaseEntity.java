package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class BaseEntity {

  @Column(nullable = false, updatable = false)
  private LocalDateTime createDatetime;

  @Column(nullable = false)
  private LocalDateTime updateDatetime;

  @PrePersist
  protected void onCreat() {
    this.createDatetime = LocalDateTime.now();
    this.updateDatetime = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updateDatetime = LocalDateTime.now();
  }
}
