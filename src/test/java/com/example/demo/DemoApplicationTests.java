package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

class DemoApplicationTests {
  @Test
  void applicationIsBootConfigured() {
    assertNotNull(DemoApplication.class.getAnnotation(SpringBootApplication.class));
  }
}
