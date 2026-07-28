package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findByUsername(String username);

    @Query("select distinct u from User u left join fetch u.roles order by u.id asc")
    List<User> findAllWithRolesOrderById();

    @Query("""
            select distinct u from User u
            join u.roles assignedRole
            left join fetch u.roles
            where assignedRole.id = :roleId
            order by u.id asc
            """)
    List<User> findByRoleIdWithRolesOrderById(@Param("roleId") Long roleId);

    @Query("""
            select distinct u from User u
            join u.roles employeeRole
            left join fetch u.roles
            where employeeRole.name = 'Employee'
              and not exists (select 1 from User scopedUser join scopedUser.roles restrictedRole
                              where scopedUser = u and restrictedRole.name in ('Manager', 'Admin'))
            order by u.id asc
            """)
    List<User> findEmployeeScopedWithRolesOrderById();

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdWithRolesAndPermissions(@Param("id") Long id);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("""
            select distinct u from User u
            join u.roles employeeRole
            where u.id = :id and employeeRole.name = 'Employee'
              and not exists (select 1 from User scopedUser join scopedUser.roles restrictedRole
                              where scopedUser = u and restrictedRole.name in ('Manager', 'Admin'))
            """)
    Optional<User> findEmployeeScopedByIdWithRolesAndPermissions(@Param("id") Long id);
}
