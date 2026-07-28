package com.example.demo.repository;

import com.example.demo.entity.Role;
import com.example.demo.dto.response.role.RoleSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {

    boolean existsByName(String name);
    Optional<Role> findByName(String name);

    @Query("select distinct r from Role r left join fetch r.permissions order by r.id asc")
    List<Role> findAllWithPermissionsOrderById();

    @Query("select distinct r from Role r left join fetch r.permissions where r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);

    @Query("select new com.example.demo.dto.response.role.RoleSummaryResponse(r.id, r.name) from Role r order by r.id asc")
    List<RoleSummaryResponse> findAllOptionsOrderById();
}
