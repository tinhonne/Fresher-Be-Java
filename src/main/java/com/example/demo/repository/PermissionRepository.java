package com.example.demo.repository;

import com.example.demo.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission,Long> {

    boolean existsBycode(String code);

    Optional<Permission> findBycode(String code);
}
