package com.example.demo.repository;

import com.example.demo.dto.response.permission.PermissionResponse;
import com.example.demo.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission,Long> {

    boolean existsBycode(String code);

    Optional<Permission> findBycode(String code);

    @Query("select new com.example.demo.dto.response.permission.PermissionResponse(p.id, p.code, p.description) from Permission p order by p.id asc")
    List<PermissionResponse> findAllResponsesOrderById();
}
