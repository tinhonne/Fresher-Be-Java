package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.security.authorization.AppRole;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByUsername(String username);

  @EntityGraph(attributePaths = "roles")
  Optional<User> findByUsername(String username);

  @Query("select distinct u from User u left join fetch u.roles order by u.id asc")
  List<User> findAllWithRolesOrderById();

  @Query(
      """
      select distinct u from User u join u.roles employeeRole left join fetch u.roles
      where employeeRole = :employeeRole
        and not exists (select 1 from User scopedUser join scopedUser.roles restrictedRole
                        where scopedUser = u and restrictedRole in :restrictedRoles)
      order by u.id asc
      """)
  List<User> findEmployeeScopedWithRolesOrderById(
      @Param("employeeRole") AppRole employeeRole,
      @Param("restrictedRoles") Set<AppRole> restrictedRoles);

  @EntityGraph(attributePaths = "roles")
  @Query("select u from User u where u.id = :id")
  Optional<User> findByIdWithRoles(@Param("id") Long id);

  @EntityGraph(attributePaths = "roles")
  @Query(
      """
      select distinct u from User u join u.roles employeeRole
      where u.id = :id and employeeRole = :employeeRole
        and not exists (select 1 from User scopedUser join scopedUser.roles restrictedRole
                        where scopedUser = u and restrictedRole in :restrictedRoles)
      """)
  Optional<User> findEmployeeScopedByIdWithRoles(
      @Param("id") Long id,
      @Param("employeeRole") AppRole employeeRole,
      @Param("restrictedRoles") Set<AppRole> restrictedRoles);
}
